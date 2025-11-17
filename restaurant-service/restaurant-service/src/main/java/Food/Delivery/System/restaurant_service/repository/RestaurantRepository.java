package Food.Delivery.System.restaurant_service.repository;

import Food.Delivery.System.restaurant_service.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
}