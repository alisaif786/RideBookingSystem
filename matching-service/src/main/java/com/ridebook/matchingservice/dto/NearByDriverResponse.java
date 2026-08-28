package com.ridebook.matchingservice.dto;
//Response receivng from location Service
//When querying for nearby drivers.
public class NearByDriverResponse {
    private String driverId;
    private double latitude;
    private double longitude;
    private double distanceInKm;
}
