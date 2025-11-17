package Food.Delivery.System.order_service.dto;

import java.util.List;

public class OrderCreateRequest {
    public Long userId;
    public Long restaurantId;
    public List<Item> items;

    public static class Item {
        public Long menuItemId;
        public int quantity;
    }
}
