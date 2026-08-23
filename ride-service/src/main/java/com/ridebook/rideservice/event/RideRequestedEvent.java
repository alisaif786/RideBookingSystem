package com.ridebook.rideservice.event;

//event published to kafka when a ride is requested
//Matching service consume this event-- Topicc ride.requested

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideRequestedEvent {
    private String rideId;
    private String riderId;

    private double pickupLatitude;
    private double pickupLongitude;
    private String pickupAddress;
    private double dropLatitude;
    private double dropLongitude;
    private String dropAddress;
}
