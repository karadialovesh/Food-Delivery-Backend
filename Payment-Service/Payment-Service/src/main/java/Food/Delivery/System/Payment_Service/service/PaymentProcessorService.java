package Food.Delivery.System.Payment_Service.service;

import Food.Delivery.System.Payment_Service.dto.PaymentRequest;
import Food.Delivery.System.Payment_Service.dto.PaymentResponse;

import Food.Delivery.System.Payment_Service.model.Payment;
import Food.Delivery.System.Payment_Service.model.PaymentStatus;
import Food.Delivery.System.Payment_Service.repository.PaymentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentProcessorService {

    @Autowired private PaymentRepository repo;
    @Autowired private KafkaTemplate<String, String> kafka;
    @Autowired private RestTemplate rest;
    @Autowired private ObjectMapper mapper;

    @Value("${payment.simulate.success-rate}")
    private double successRate;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest req) throws JsonProcessingException {


        Optional<Payment> existing = repo.findByIdempotencyKey(req.idempotencyKey);
        if (existing.isPresent()) {
            Payment p = existing.get();

            return new PaymentResponse(
                    p.getId(),
                    p.getStatus(),
                    "Idempotent hit – returning existing payment"
            );
        }

        ResponseEntity<Map> orderResp =
                rest.getForEntity("http://localhost:8082/api/orders/" + req.orderId, Map.class);

        if (!orderResp.getStatusCode().is2xxSuccessful() || orderResp.getBody() == null)
            throw new RuntimeException("Order not found");

        Map<String, Object> o = orderResp.getBody();

        Long total = Long.valueOf(o.get("totalPrice").toString());
        String status = o.get("status").toString();
        Long restaurantId = Long.valueOf(o.get("restaurantId").toString());
        Long userId = Long.valueOf(o.get("userId").toString());

        if (!status.equals("CREATED"))
            throw new RuntimeException("Order not payable. Status = " + status);


        Payment p = new Payment();
        p.setOrderId(req.orderId);
        p.setUserId(userId);
        p.setAmountInPaise(total);
        p.setIdempotencyKey(req.idempotencyKey);
        p.setStatus(PaymentStatus.INITIATED);
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());

        p = repo.save(p);


        boolean success = Math.random() < successRate;

        if (success) {
            p.setStatus(PaymentStatus.SUCCESS);
            p.setPaymentGatewayTxId("SIM-" + UUID.randomUUID());
            p.setUpdatedAt(LocalDateTime.now());
            repo.save(p);


            PaymentSuccessEvent event = new PaymentSuccessEvent();
            event.orderId = req.orderId;
            event.paymentId = p.getId();
            event.amountInPaise = p.getAmountInPaise();
            event.gatewayTxId = p.getPaymentGatewayTxId();
            event.userId = userId;
            event.restaurantId = restaurantId;

            kafka.send("payment_success",
                    req.orderId.toString(),
                    mapper.writeValueAsString(event));

            return new PaymentResponse(p.getId(), PaymentStatus.SUCCESS, "Payment successful");
        }


        p.setStatus(PaymentStatus.FAILED);
        p.setFailureReason("SIMULATED_FAILURE");
        p.setUpdatedAt(LocalDateTime.now());
        repo.save(p);

        kafka.send("payment_failure",
                req.orderId.toString(),
                mapper.writeValueAsString(Map.of(
                        "orderId", req.orderId,
                        "paymentId", p.getId(),
                        "reason", "SIMULATED_FAILURE"
                )));

        return new PaymentResponse(p.getId(), PaymentStatus.FAILED, "Payment failed");
    }
}
