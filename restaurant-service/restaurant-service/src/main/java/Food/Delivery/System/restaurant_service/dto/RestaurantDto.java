package Food.Delivery.System.restaurant_service.dto;

import lombok.Data;
import java.util.List;
@Data
public class RestaurantDto {
  public Long id;
  public String name;
  public String address;
  public double lat, lon;
  public boolean open;
  public List<MenuItemDto> menu;
}
