package Food.Delivery.System.order_service.dto;

import Food.Delivery.System.order_service.model.OrderStatus;

public class OrderResponse {
    public Long orderId;
    public Long totalPrice;
    public OrderStatus status;
    public Long restaurantId;   // ADD
    public Long userId;         // ADD
}
