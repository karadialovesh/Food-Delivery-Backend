package Food.Delivery.System.order_service.service;

import Food.Delivery.System.order_service.dto.MenuItemDto;
import Food.Delivery.System.order_service.dto.OrderCreateRequest;
import Food.Delivery.System.order_service.dto.OrderResponse;
import Food.Delivery.System.order_service.dto.RestaurantDto;

import Food.Delivery.System.order_service.model.Order;
import Food.Delivery.System.order_service.model.OrderItem;
import Food.Delivery.System.order_service.model.OrderStatus;
import Food.Delivery.System.order_service.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private RestaurantClient restaurantClient;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Transactional
    public OrderResponse createOrder(OrderCreateRequest req) {

        // restuarant dto
        RestaurantDto restaurant = restaurantClient.getRestaurant(req.restaurantId);

        if (restaurant == null)
            throw new RuntimeException("Restaurant not found");

        if (!restaurant.open)
            throw new RuntimeException("Restaurant is closed");

        //  call fetch menu
        List<MenuItemDto> menu = restaurantClient.getMenu(req.restaurantId);
        Map<Long, MenuItemDto> menuMap = menu.stream()
                .collect(Collectors.toMap(m -> m.id, m -> m));

        // compute price
        long total = 0;

        for (OrderCreateRequest.Item i : req.items) {
            MenuItemDto m = menuMap.get(i.menuItemId);

            if (m == null)
                throw new RuntimeException("Invalid item: " + i.menuItemId);

            if (!m.available)
                throw new RuntimeException("Item unavailable: " + i.menuItemId);

            total += m.priceInPaise * i.quantity;
        }

        // create order
        Order order = new Order();
        order.setRestaurantId(req.restaurantId);
        order.setUserId(req.userId);
        order.setTotalPriceInPaise(total);
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // items list
        List<OrderItem> orderItems = req.items.stream().map(i -> {
            OrderItem oi = new OrderItem();
            oi.setMenuItemId(i.menuItemId);
            oi.setQuantity(i.quantity);
            oi.setPriceInPaise(menuMap.get(i.menuItemId).priceInPaise);
            oi.setOrder(order);
            return oi;
        }).collect(Collectors.toList());

        order.setItems(orderItems);

        Order saved = orderRepo.save(order);

        // kafka publish
        try {
            OrderCreatedEvent event = new OrderCreatedEvent(
                    saved.getId(),
                    saved.getRestaurantId(),
                    saved.getUserId(),
                    saved.getTotalPriceInPaise()
            );
            kafkaTemplate.send("order_created",
                    saved.getId().toString(),
                    mapper.writeValueAsString(event));

        } catch (Exception e) {
            throw new RuntimeException("Failed to publish event", e);
        }

        // 6️⃣ RESPONSE
        OrderResponse res = new OrderResponse();
        res.orderId = saved.getId();
        res.totalPrice = saved.getTotalPriceInPaise();
        res.status = saved.getStatus();

        return res;
    }
    // get restaurant dto
    public Optional<OrderResponse> getOrderResponseById(Long id) {
        return orderRepo.findById(id).map(o -> {
            OrderResponse dto = new OrderResponse();
            dto.orderId = o.getId();
            dto.status = o.getStatus();
            dto.totalPrice = o.getTotalPriceInPaise();
            dto.restaurantId=o.getRestaurantId();
            dto.userId=o.getUserId();
            return dto;
        });
    }

}
