package Food.Delivery.System.Payment_Service.service;

import Food.Delivery.System.Payment_Service.dto.OrderCreatedEvent;
import Food.Delivery.System.Payment_Service.dto.PaymentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    @Autowired
    private PaymentProcessorService processor;

    @Autowired
    private ObjectMapper mapper;

    @KafkaListener(topics = "order_created", groupId = "payment-service-group")
    public void onOrderCreated(String payload) throws Exception {

        OrderCreatedEvent ev = mapper.readValue(payload, OrderCreatedEvent.class);

        System.out.println("✔ Received OrderCreated event → Processing payment for order: " + ev.orderId);

        PaymentRequest req = new PaymentRequest();
        req.orderId = ev.orderId;
        req.userId = ev.userId;
        req.idempotencyKey = "ORDER-" + ev.orderId;

        processor.processPayment(req);
    }
}
