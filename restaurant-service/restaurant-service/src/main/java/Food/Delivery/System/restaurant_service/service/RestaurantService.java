package Food.Delivery.System.restaurant_service.service;

import Food.Delivery.System.restaurant_service.dto.MenuItemDto;
import Food.Delivery.System.restaurant_service.dto.RestaurantDto;
import Food.Delivery.System.restaurant_service.model.MenuItem;
import Food.Delivery.System.restaurant_service.model.Restaurant;
import Food.Delivery.System.restaurant_service.repository.RestaurantRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RestaurantService {

  @Autowired
  private RestaurantRepository repo;

  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;

  @Autowired
  private ObjectMapper objectMapper;

  // create restaurant
  public RestaurantDto create(RestaurantDto dto) {
    Restaurant r = new Restaurant();
    r.setName(dto.getName());
    r.setAddress(dto.getAddress());
    r.setLat(dto.getLat());
    r.setLon(dto.getLon());
    r.setOpen(dto.isOpen());

    Restaurant saved = repo.save(r);
    dto.setId(saved.getId());
    return dto;
  }


  // get restaurant
  public RestaurantDto getById(Long id) {
    return repo.findById(id)
            .map(r -> {
              RestaurantDto dto = new RestaurantDto();
              dto.setId(r.getId());
              dto.setName(r.getName());
              dto.setAddress(r.getAddress());
              dto.setLat(r.getLat());
              dto.setLon(r.getLon());
              dto.setOpen(r.isOpen());
              return dto;
            })
            .orElse(null);
  }

  // ============================
  // get menu
  // ============================
  @Cacheable(value = "restaurantMenu", key = "#restaurantId")
  public List<MenuItemDto> getMenu(Long restaurantId) {
    Restaurant r = repo.findById(restaurantId)
            .orElseThrow(() -> new RuntimeException("Restaurant not found"));

    System.out.println("connecting to db");

    return r.getMenu().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
  }

  // add menu
  @Transactional
  @CacheEvict(value = "restaurantMenu", key = "#restaurantId")
  public MenuItemDto addMenuItem(Long restaurantId, MenuItemDto dto) {

    Restaurant restaurant = repo.findById(restaurantId)
            .orElseThrow(() -> new RuntimeException("Restaurant not found"));

    MenuItem item = new MenuItem();
    item.setName(dto.getName());
    item.setPriceInPaise(dto.getPriceInPaise());
    item.setAvailable(dto.isAvailable());
    item.setRestaurant(restaurant);

    restaurant.getMenu().add(item);
    repo.save(restaurant);

    MenuItemDto created = toDto(item);
    publishEvent(restaurantId, created);

    return created;
  }


  //update menu
  @Transactional
  @CacheEvict(value = "restaurantMenu", key = "#restaurantId")
  public MenuItemDto updateMenuItem(Long restaurantId, Long menuId, MenuItemDto dto) {

    Restaurant restaurant = repo.findById(restaurantId)
            .orElseThrow(() -> new RuntimeException("Restaurant not found"));

    MenuItem item = restaurant.getMenu().stream()
            .filter(m -> m.getId().equals(menuId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Menu item not found"));

    item.setName(dto.getName());
    item.setPriceInPaise(dto.getPriceInPaise());
    item.setAvailable(dto.isAvailable());

    MenuItemDto updated = toDto(item);
    publishEvent(restaurantId, updated);

    return updated;
  }

  // KAFKA EVENT PUBLISHING
  private void publishEvent(Long restaurantId, MenuItemDto dto) {
    try {
      MenuUpdatedEvent event = new MenuUpdatedEvent(
              restaurantId,
              dto.getId(),
              dto.getName(),
              dto.getPriceInPaise(),
              dto.isAvailable()
      );

      String json = objectMapper.writeValueAsString(event);
      kafkaTemplate.send("menu_updates", restaurantId.toString(), json);

    } catch (Exception e) {
      throw new RuntimeException("Kafka publish failed", e);
    }
  }


  // dto mapper
  private MenuItemDto toDto(MenuItem item) {
    MenuItemDto dto = new MenuItemDto();
    dto.setId(item.getId());
    dto.setName(item.getName());
    dto.setPriceInPaise(item.getPriceInPaise());
    dto.setAvailable(item.isAvailable());
    return dto;
  }

  // event model
  record MenuUpdatedEvent(Long restaurantId, Long menuId, String name, long priceInPaise, boolean available) {}
}
