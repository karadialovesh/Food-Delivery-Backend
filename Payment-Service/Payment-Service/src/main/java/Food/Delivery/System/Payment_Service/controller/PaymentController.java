package Food.Delivery.System.Payment_Service.controller;

import Food.Delivery.System.Payment_Service.dto.PaymentRequest;
import Food.Delivery.System.Payment_Service.dto.PaymentResponse;
import Food.Delivery.System.Payment_Service.model.Payment;
import Food.Delivery.System.Payment_Service.repository.PaymentRepository;
import Food.Delivery.System.Payment_Service.service.PaymentProcessorService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentProcessorService processor;

    @Autowired
    private PaymentRepository repo;

    @PostMapping("/charge")
    public ResponseEntity<PaymentResponse> charge(@RequestBody PaymentRequest req) throws JsonProcessingException {

        if (req.idempotencyKey == null)
            return ResponseEntity.badRequest().body(
                    new PaymentResponse(null, null, "idempotencyKey required")
            );

        return ResponseEntity.ok(processor.processPayment(req));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPayment(@PathVariable Long paymentId) {
        return repo.findById(paymentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
