package com.ridebook.location_service.service;

import com.ridebook.location_service.dto.DriverLocationReq;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.geo.Point;

@Slf4j
@AllArgsConstructor
public class LocationService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String DRIVERS_GEO_KEY = "drivers";

    public void updateDriverLocation(DriverLocationReq driverLocationReq) {

        log.info("Updating location for driver: {}",
                driverLocationReq.getDriverId());

        // IMPORTANT: longitude FIRST, latitude SECOND
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
    public  void findNearbyDrivers(DriverLocationReq driverLocationReq){

    }
    public void removeDriver(){

    }
}
