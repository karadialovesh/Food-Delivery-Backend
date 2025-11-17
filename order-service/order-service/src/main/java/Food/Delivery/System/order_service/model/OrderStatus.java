package Food.Delivery.System.order_service.model;

public enum OrderStatus {
    CREATED,
    ACCEPTED_BY_RESTAURANT,
    REJECTED_BY_RESTAURANT,
    ASSIGNED_DELIVERY_PARTNER,
    PAYMENT_CONFIRMED,
    PAYMENT_FAILED,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}
