package Food.Delivery.System.Payment_Service.dto;

import Food.Delivery.System.Payment_Service.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    public Long paymentId;
    public PaymentStatus status;
    public String message;
}
