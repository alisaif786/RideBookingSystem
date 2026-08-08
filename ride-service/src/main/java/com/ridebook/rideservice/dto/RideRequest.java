package com.ridebook.rideservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class RideRequest {

    @NotBlank(message = "Rider Id is Required")
    private String riderId;

    @NotNull(message = "Pickup Latitude is Required")
    private Double pickupLatitude;

    @NotNull(message = "Pickup Longitude is Required")
    private Double pickupLongitude;

    @NotBlank(message = "Pickup Address is Required")
    private String pickupAddress;

    @NotNull(message = "Drop Latitude is Required")
    private Double dropLatitude;

    @NotNull(message = "Drop Longitude is Required")
    private Double dropLongitude;

    @NotBlank(message = "Drop Address is Required")
    private String dropAddress;
}