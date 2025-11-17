package Food.Delivery.System.Payment_Service.dto;

public class OrderCreatedEvent {
    public Long orderId;
    public Long restaurantId;
    public Long userId;
    public Long totalPriceInPaise;
}
