package Food.Delivery.System.restaurant_service.controller;

import Food.Delivery.System.restaurant_service.dto.MenuItemDto;
import Food.Delivery.System.restaurant_service.dto.RestaurantDto;
import Food.Delivery.System.restaurant_service.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    @Autowired
    private RestaurantService service;

    // create restaurant
    @PostMapping
    public ResponseEntity<RestaurantDto> create(@RequestBody RestaurantDto dto) {
        RestaurantDto saved = service.create(dto);
        return ResponseEntity.status(201).body(saved);
    }

    // get restaurant
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantDto> getById(@PathVariable Long id) {
        RestaurantDto dto = service.getById(id);
        return dto == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(dto);
    }

    // get menu
    @GetMapping("/{id}/menu")
    public ResponseEntity<List<MenuItemDto>> getMenu(@PathVariable Long id) {
        return ResponseEntity.ok(service.getMenu(id));
    }

    // add menu
    @PostMapping("/{id}/menu")
    public ResponseEntity<MenuItemDto> addMenuItem(
            @PathVariable Long id,
            @RequestBody MenuItemDto dto) {

        MenuItemDto created = service.addMenuItem(id, dto);
        return ResponseEntity.status(201).body(created);
    }

    // update menu
    @PutMapping("/{id}/menu/{menuId}")
    public ResponseEntity<MenuItemDto> updateMenu(
            @PathVariable Long id,
            @PathVariable Long menuId,
            @RequestBody MenuItemDto dto) {

        MenuItemDto updated = service.updateMenuItem(id, menuId, dto);
        return ResponseEntity.ok(updated);
    }
}
