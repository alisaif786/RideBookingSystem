package com.ridebook.rideservice.service;

import com.ridebook.rideservice.dto.RideRequest;
import com.ridebook.rideservice.dto.RideResponse;
import com.ridebook.rideservice.event.RideRequestedEvent;
import com.ridebook.rideservice.model.Ride;
import com.ridebook.rideservice.model.RideStatus;
import com.ridebook.rideservice.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor

public class RideService {

    private final RideRepository rideRepository;
    private final KafkaTemplate<String, RideRequestedEvent> kafkaTemplate;

    private static final String RIDE_REQUESTED_TOPIC = "ride.requested";

    //Create ride in DB with Ride_requested....
    public RideResponse requestRide(RideRequest request){
        log.info("New Ride Request from Rider: {}", request.getRiderId());

        // save ride to the DB....
        Ride ride = new Ride();
        ride.setRiderId(request.getRiderId());
        ride.setPickupLatitude(request.getPickupLatitude());
        ride.setPickupLongitude(request.getPickupLongitude());
        ride.setPickupAddress(request.getPickupAddress());
        ride.setDropLatitude(request.getDropLatitude());
        ride.setDropLongitude(request.getDropLongitude());
        ride.setDropAddress(request.getDropAddress());
        ride.setStatus(RideStatus.REQUESTED);
        ride.setEstimatedFare(calculateEstimateFare(request));

        Ride savedRide = rideRepository.save(ride);

        //publish event to Kafka
        //Matching service will consume this and find nearest driver
        RideRequestedEvent event = new RideRequestedEvent(
                savedRide.getId(),
                savedRide.getRiderId(),
                savedRide.getPickupLatitude(),
                savedRide.getPickupLongitude(),
                savedRide.getPickupAddress(),
                savedRide.getDropLatitude(),
                savedRide.getDropLongitude(),
                savedRide.getDropAddress()
        );
        kafkaTemplate.send(RIDE_REQUESTED_TOPIC, savedRide.getId(), event);
        log.info("RideRequestedEvent published to Kafka for ride: {}", savedRide.getId());

        //update status to matching
        savedRide.setStatus(RideStatus.MATCHING);
        rideRepository.save(savedRide);

        return mapToResponse(savedRide);
    }

    public void updateRideWithDriver(String rideId, String driverId){
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not Found"));
        ride.setDriverId(driverId);
        ride.setStatus(RideStatus.ACCEPTED);
        rideRepository.save(ride);
    }

    public  RideResponse startRide(String rideId){
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not Found"));

        if(ride.getStatus() != RideStatus.ACCEPTED){
            throw new RuntimeException("Ride Can not be Started. Current Status : "+ride.getStatus());
        }
        ride.setStatus(RideStatus.RIDE_STARTED);
        ride.setStartedAt((LocalDateTime.now()));
        rideRepository.save(ride);

        return  mapToResponse(ride);
    }

    public RideResponse completeRide(String rideId){
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not Found"));

        if(ride.getStatus() != RideStatus.RIDE_STARTED) {
            throw new RuntimeException("Ride Can not be Completed. Current Status : " + ride.getStatus());
        }
            ride.setStatus(RideStatus.COMPLETED);
            ride.setCompletedAt(LocalDateTime.now());
            ride.setActualFare(ride.getEstimatedFare());
            rideRepository.save(ride);

            return  mapToResponse(ride);
    }

    public RideResponse cancelRide(String rideId){
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not Found"));

        ride.setStatus(RideStatus.CANCELLED);
        rideRepository.save(ride);
        return  mapToResponse(ride);
    }

    public RideResponse getRideById(String rideId){
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not Found"));

        return mapToResponse(ride);
    }

    public List<RideResponse> getRidesByRider(String riderId){
        return rideRepository.findByRiderIdOrderByCreatedAtDesc(riderId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private  double calculateEstimateFare(RideRequest request){
        double lat1= Math.toRadians(request.getPickupLatitude());
        double lon1= Math.toRadians(request.getPickupLongitude());
        double lat2= Math.toRadians(request.getDropLatitude());
        double lon2= Math.toRadians(request.getDropLongitude());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.pow(Math.sin(dLat/2), 2)
                +Math.cos(lat1)*Math.cos(lat2)
                +Math.pow(Math.sin(dLon/2),2);

        double c = 2 * Math.asin(Math.sqrt(a));
        double distanceKm = 6371*c;

        // BAseFare 50rs+12rs perKm
        double fare = 50 + (distanceKm*12);
        return Math.round(fare*100.0)/100.0;
    }


    private RideResponse mapToResponse(Ride ride){
        RideResponse response = new RideResponse();
        response.setId(ride.getId());
        response.setRiderId(ride.getRiderId());
        response.setDriverId(ride.getDriverId());
        response.setPickupLatitude(ride.getPickupLatitude());
        response.setPickupLongitude(ride.getPickupLongitude());
        response.setPickupAddress(ride.getPickupAddress());
        response.setDropLatitude(ride.getDropLatitude());
        response.setDropLongitude(ride.getDropLongitude());
        response.setDropAddress(ride.getDropAddress());
        response.setStatus(ride.getStatus());
        response.setEstimatedFare(ride.getEstimatedFare());
        response.setActualFare(ride.getActualFare());
        response.setCreatedAt(ride.getCreatedAt());
        response.setStartedAt(ride.getStartedAt());
        response.setCompletedAt(ride.getCompletedAt());
        return response;
    }

}
