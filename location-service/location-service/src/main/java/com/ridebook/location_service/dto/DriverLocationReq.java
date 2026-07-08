package com.ridebook.location_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriverLocationReq {
    private String driverId;
    private double latitude;
    private double longitude;
}
