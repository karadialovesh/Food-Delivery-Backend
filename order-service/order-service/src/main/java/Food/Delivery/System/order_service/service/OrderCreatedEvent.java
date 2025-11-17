package Food.Delivery.System.order_service.service;

public record OrderCreatedEvent(Long orderId, Long restaurantId, Long userId, Long totalPriceInPaise ) {}