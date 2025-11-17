package Food.Delivery.System.Delivery_Service.controller;

import Food.Delivery.System.Delivery_Service.model.LocationDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/riders")
public class RiderController {

    private final RedisTemplate<String, String> redisTemplate;
    private final String geoKey;

    public RiderController(RedisTemplate<String, String> redisTemplate,
                           @Value("${delivery.geo.key:riders:locations}") String geoKey) {
        this.redisTemplate = redisTemplate;
        this.geoKey = geoKey;
    }


    @PostMapping("/{riderId}/location")
    public ResponseEntity<Void> updateLocation(@PathVariable("riderId") Long riderId,
                                               @RequestBody LocationDto loc) {

        GeoOperations<String, String> geoOps = redisTemplate.opsForGeo();


        geoOps.add(geoKey, new Point(loc.getLon(), loc.getLat()), "rider:" + riderId);
        redisTemplate.opsForValue().set("rider:" + riderId + ":status", "AVAILABLE");
        System.out.println(">>> RIDER LOCATION UPDATE CALLED, riderId=" + riderId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{riderId}/status")
    public ResponseEntity<Void> setStatus(@PathVariable("riderId") Long riderId,
                                          @RequestBody Map<String, String> payload) {
        String status = payload.get("status");
        if (status == null) status = "OFFLINE";

        redisTemplate.opsForValue().set("rider:" + riderId + ":status", status.toUpperCase());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/assignment/{orderId}")
    public ResponseEntity<Map<String, String>> getAssignment(@PathVariable("orderId") Long orderId) {
        Map<Object, Object> map = redisTemplate.opsForHash().entries("order:" + orderId + ":assignment");
        Map<String, String> res = new HashMap<>();
        if (map != null) {
            map.forEach((k, v) -> res.put(k.toString(), v == null ? null : v.toString()));
        }
        return ResponseEntity.ok(res);
    }


}
