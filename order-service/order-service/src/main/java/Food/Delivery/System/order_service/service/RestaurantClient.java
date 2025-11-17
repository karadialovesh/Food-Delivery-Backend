package Food.Delivery.System.order_service.service;

import Food.Delivery.System.order_service.dto.MenuItemDto;
import Food.Delivery.System.order_service.dto.RestaurantDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class RestaurantClient {

    private final RestTemplate restTemplate = new RestTemplate();
    //fetch restaurant from restaurant
    public RestaurantDto getRestaurant(Long restaurantId) {
        String url = "http://localhost:8081/api/restaurants/" + restaurantId;
        return restTemplate.getForObject(url, RestaurantDto.class);
    }
    //fetch menu
    public List<MenuItemDto> getMenu(Long restaurantId) {
        String url = "http://localhost:8081/api/restaurants/" + restaurantId + "/menu";
        ResponseEntity<List<MenuItemDto>> resp = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<MenuItemDto>>() {}
        );
        return resp.getBody();
    }
}
