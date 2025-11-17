package Food.Delivery.System.Delivery_Service.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderCreatedEvent {
    private Long orderId;
    private Long restaurantId;
    private Long userId;
    private Long totalPrice;


}
