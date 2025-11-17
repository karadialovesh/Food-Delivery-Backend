package Food.Delivery.System.restaurant_service.repository;

import Food.Delivery.System.restaurant_service.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
}
