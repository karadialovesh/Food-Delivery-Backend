package Food.Delivery.System.restaurant_service.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class MenuItemDto{
    public Long id;
    public String name;
    public long priceInPaise;
    public boolean available;

}
