package Food.Delivery.System.order_service.repository;

import Food.Delivery.System.order_service.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order,Long> {
}
