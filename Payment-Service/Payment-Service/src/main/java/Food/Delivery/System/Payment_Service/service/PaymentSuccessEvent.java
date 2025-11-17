package Food.Delivery.System.Payment_Service.service;

public class PaymentSuccessEvent {
    public Long orderId;
    public Long userId;
    public Long restaurantId;
    public Long paymentId;
    public Long amountInPaise;
    public String gatewayTxId;
}
