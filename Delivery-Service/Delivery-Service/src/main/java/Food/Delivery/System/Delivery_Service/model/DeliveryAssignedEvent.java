package Food.Delivery.System.Delivery_Service.model;

import lombok.Getter;

@Getter
public class DeliveryAssignedEvent {
    private Long orderId;
    private Long riderId;
    private String assignedAt;
    private Long etaSeconds;

    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public void setRiderId(Long riderId) { this.riderId = riderId; }

    public void setAssignedAt(String assignedAt) { this.assignedAt = assignedAt; }

    public void setEtaSeconds(Long etaSeconds) { this.etaSeconds = etaSeconds; }
}
