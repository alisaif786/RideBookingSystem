package com.ridebook.location_service.controller;

import com.ridebook.location_service.dto.DriverLocationReq;
import com.ridebook.location_service.dto.NearbyDriverResponse;
import com.ridebook.location_service.service.LocationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@Slf4j
@AllArgsConstructor
public class LocationController {
    private final LocationService locationService;

    @PostMapping("/drivers/update")
    public ResponseEntity<String> updateLocation(@RequestBody DriverLocationReq driverLocationReq){
        locationService.updateDriverLocation(driverLocationReq);
        return ResponseEntity.ok("Driver Location Updated");
    }

    @GetMapping("/drivers/nearby")
    public ResponseEntity<List<NearbyDriverResponse>> getNearyByDriver(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5.0") double radius){
        return ResponseEntity.ok(locationService.findNearbyDrivers(latitude, longitude, radius));
    }

    @DeleteMapping("/drivers/{driverID}")
    public ResponseEntity<String> deleteDriverLocation(@PathVariable String driverID){
        locationService.removeDriver(driverID);
        return ResponseEntity.ok("Driver Location Deleted");
    }
}
