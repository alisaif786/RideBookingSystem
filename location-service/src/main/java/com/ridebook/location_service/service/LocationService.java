package com.ridebook.location_service.service;

import com.ridebook.location_service.dto.DriverLocationReq;
import com.ridebook.location_service.dto.NearbyDriverResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class LocationService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String DRIVERS_GEO_KEY = "drivers";

    /**
     * Update driver's current location in Redis GEO
     */
    public void updateDriverLocation(DriverLocationReq driverLocationReq) {

        log.info("Updating location for driver: {}",
                driverLocationReq.getDriverId());

        // IMPORTANT: longitude first, latitude second
        Point driverPoint = new Point(
                driverLocationReq.getLongitude(),
                driverLocationReq.getLatitude()
        );

        redisTemplate.opsForGeo().add(
                DRIVERS_GEO_KEY,
                driverPoint,
                driverLocationReq.getDriverId()
        );

        log.info("Location updated for driver: {}",
                driverLocationReq.getDriverId());
    }

    /**
     * Find nearby drivers within given radius
     */
    public List<NearbyDriverResponse> findNearbyDrivers(
            double latitude,
            double longitude,
            double radiusInKm) {

        log.info("Finding drivers near lat: {}, long: {} within {} km",
                latitude, longitude, radiusInKm);

        Circle searchArea = new Circle(
                new Point(longitude, latitude),
                new Distance(radiusInKm, Metrics.KILOMETERS)
        );

        GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                redisTemplate.opsForGeo().radius(
                        DRIVERS_GEO_KEY,
                        searchArea,
                        RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                                .includeCoordinates()
                                .includeDistance()
                                .sortAscending()
                                .limit(10)
                );

        List<NearbyDriverResponse> nearbyDrivers = new ArrayList<>();

        if (results != null) {

            results.getContent().forEach(result -> {

                RedisGeoCommands.GeoLocation<String> location =
                        result.getContent();

                double distance = result.getDistance() != null
                        ? result.getDistance().getValue()
                        : 0.0;

                nearbyDrivers.add(
                        new NearbyDriverResponse(
                                location.getName(),
                                location.getPoint().getY(), // latitude
                                location.getPoint().getX(), // longitude
                                distance
                        )
                );
            });
        }

        log.info("Found {} nearby drivers", nearbyDrivers.size());

        return nearbyDrivers;
    }

    /**
     * Remove driver from Redis GEO
     */
    public void removeDriver(String driverId) {

        log.info("Removing driver: {}", driverId);

        Long removed = redisTemplate.opsForZSet()
                .remove(DRIVERS_GEO_KEY, driverId);

        if (removed != null && removed > 0) {
            log.info("Driver {} removed successfully", driverId);
        } else {
            log.warn("Driver {} not found", driverId);
        }
    }
}