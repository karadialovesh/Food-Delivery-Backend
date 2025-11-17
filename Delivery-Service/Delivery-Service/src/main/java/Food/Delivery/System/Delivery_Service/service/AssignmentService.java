package Food.Delivery.System.Delivery_Service.service;

import Food.Delivery.System.Delivery_Service.model.DeliveryAssignedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class AssignmentService {

    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String geoKey;
    private final long assignmentTtlSeconds;
    private final ObjectMapper mapper = new ObjectMapper();

    public AssignmentService(RedisTemplate<String, String> redisTemplate,
                             KafkaTemplate<String, String> kafkaTemplate,
                             @Value("${delivery.geo.key:riders:locations}") String geoKey,
                             @Value("${delivery.assignment.ttl-seconds:300}") long assignmentTtlSeconds) {
        this.redisTemplate = redisTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.geoKey = geoKey;
        this.assignmentTtlSeconds = assignmentTtlSeconds;
    }


    public Optional<Long> findNearestAvailableRider(double lat, double lon, double radiusMeters, int maxResults) {
        GeoOperations<String, String> geoOps = redisTemplate.opsForGeo();

        // Redis geo radius
        double radiusKm = radiusMeters / 1000.0;
        Circle circle = new Circle(new Point(lon, lat), new Distance(radiusKm, Metrics.KILOMETERS));

        // Request results with distance and sorted ascending
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance()
                .sortAscending()
                .limit(maxResults);

        GeoResults<RedisGeoCommands.GeoLocation<String>> results = geoOps.radius(geoKey, circle, args);

        if (results == null) return Optional.empty();
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> list = results.getContent();
        for (GeoResult<RedisGeoCommands.GeoLocation<String>> r : list) {
            String member = r.getContent().getName(); // expects "rider:<id>"
            if (member == null) continue;
            String idStr = member.replace("rider:", "");
            if (!idStr.matches("\\d+")) continue;
            String statusKey = "rider:" + idStr + ":status";
            String status = redisTemplate.opsForValue().get(statusKey);
            if ("AVAILABLE".equalsIgnoreCase(status)) {
                return Optional.of(Long.parseLong(idStr));
            }
        }

        return Optional.empty();
    }


    public boolean assignRiderToOrder(Long orderId, Long riderId) {
        String assignKey = "order:assigned:" + orderId;

        // Atomic attempt to set assignment
        Boolean set = redisTemplate.opsForValue().setIfAbsent(assignKey, String.valueOf(riderId), assignmentTtlSeconds, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(set)) {
            return false;
        }

        // mark rider busy
        redisTemplate.opsForValue().set("rider:" + riderId + ":status", "BUSY");

        // create assignment hash (for quick inspection)
        Map<String, String> map = new HashMap<>();
        map.put("riderId", String.valueOf(riderId));
        map.put("assignedAt", Instant.now().toString());
        map.put("status", "ASSIGNED");
        redisTemplate.opsForHash().putAll("order:" + orderId + ":assignment", map);
        redisTemplate.expire("order:" + orderId + ":assignment", assignmentTtlSeconds, TimeUnit.SECONDS);

        // publish delivery_assigned event
        try {
            DeliveryAssignedEvent event = new DeliveryAssignedEvent();
            event.setOrderId(orderId);
            event.setRiderId(riderId);
            event.setAssignedAt(Instant.now().toString());
            event.setEtaSeconds(10 * 60L);
            kafkaTemplate.send("delivery_assigned", orderId.toString(), mapper.writeValueAsString(event));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
}
