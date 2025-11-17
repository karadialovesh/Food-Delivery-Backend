package Food.Delivery.System.Delivery_Service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Component
public class PaymentSuccessListener {

    private final AssignmentService assignmentService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public PaymentSuccessListener(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @KafkaListener(topics = "payment_success", groupId = "delivery-service-group")
    public void onPaymentSuccess(ConsumerRecord<String, String> record) {
        try {
            String payload = record.value();
            Map<String, Object> data = mapper.readValue(payload, Map.class);

            Long orderId = Long.valueOf(data.get("orderId").toString());
            Long restaurantId = null;

            // Preferred: payment_success includes restaurantId
            if (data.containsKey("restaurantId")) {
                restaurantId = Long.valueOf(data.get("restaurantId").toString());
            } else {
                // fallback: fetch order details from order service to get restaurantId
                String orderUrl = "http://localhost:8082/api/orders/" + orderId;
                Map<String, Object> orderResp = restTemplate.getForObject(orderUrl, Map.class);
                if (orderResp != null && orderResp.containsKey("restaurantId")) {
                    restaurantId = Long.valueOf(orderResp.get("restaurantId").toString());
                }
            }

            if (restaurantId == null) {
                System.err.println("Missing restaurantId for order " + orderId);
                return;
            }

            // fetch restaurant coords
            String restUrl = "http://localhost:8081/api/restaurants/" + restaurantId;
            Map<String, Object> rest = restTemplate.getForObject(restUrl, Map.class);
            if (rest == null || !rest.containsKey("lat") || !rest.containsKey("lon")) {
                System.err.println("Missing restaurant coords for restaurant " + restaurantId);
                return;
            }

            double lat = Double.parseDouble(rest.get("lat").toString());
            double lon = Double.parseDouble(rest.get("lon").toString());

            Optional<Long> riderOpt = assignmentService.findNearestAvailableRider(lat, lon, 5000, 5);
            if (riderOpt.isPresent()) {
                boolean ok = assignmentService.assignRiderToOrder(orderId, riderOpt.get());
                if (!ok) {
                    System.out.println("Assignment race or duplicate for order " + orderId);
                } else {
                    System.out.println("Assigned rider " + riderOpt.get() + " to order " + orderId);
                }
            } else {
                System.out.println("No rider available for order " + orderId);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
