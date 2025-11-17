package Food.Delivery.System.order_service.service;

import Food.Delivery.System.order_service.model.Order;
import Food.Delivery.System.order_service.model.OrderStatus;
import Food.Delivery.System.order_service.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.time.LocalDateTime;

@Component
public class PaymentEventListener {

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "payment_success", groupId = "order-service-group")
    public void onPaymentSuccess(String payload) throws Exception {

        System.out.println(" Received payment_success event: " + payload);

        Map<String, Object> data = objectMapper.readValue(payload, Map.class);
        Long orderId = Long.valueOf(data.get("orderId").toString());

        Order order = orderRepo.findById(orderId).orElse(null);
        if (order == null) {
            System.err.println(" Order not found for payment_success: " + orderId);
            return;
        }

        order.setStatus(OrderStatus.PAYMENT_CONFIRMED);
        order.setUpdatedAt(LocalDateTime.now());

        orderRepo.save(order);

        System.out.println(" Order " + orderId + " marked as PAYMENT_CONFIRMED");
    }
}
