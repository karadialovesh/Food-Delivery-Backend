package Food.Delivery.System.Payment_Service.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments", indexes = {
    @Index(columnList = "idempotencyKey", name = "idx_payment_idem", unique = true)
})
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;
    private Long userId;
    private Long amountInPaise;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String paymentGatewayTxId;

    private String idempotencyKey;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String failureReason;

}
