package com.ridebook.matchingservice.service;

import com.ridebook.matchingservice.client.LocationServiceClient;
import com.ridebook.matchingservice.dto.NearByDriverResponse;
import com.ridebook.matchingservice.event.RideMatchedEvent;
import com.ridebook.matchingservice.event.RideRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchingService {

    public final LocationServiceClient locationServiceClient;

    private final KafkaTemplate<String, RideMatchedEvent> kafkaTemplate;

    private static final String RIDE_MATCHED_EVENT_TOPIC = "ride.matched";
    private static final double DEFAULT_SEARCH_RADIUS_KM = 5.0;

    /**
     * Main matching alogorithm
     * called when RideRequestedEvent consumed from kafka
     * @param event
     * Steps
     * 1. ask location service for nearby drivers
     */

    public void matchedDriverForRide(RideRequestedEvent event){
        List<NearByDriverResponse> nearByDrivers = locationServiceClient.getNearByDrivers(
                event.getPickupLatitude(),
                event.getPickupLongitude(),
                DEFAULT_SEARCH_RADIUS_KM

        );

        if(nearByDrivers.isEmpty()){
            log.warn("No drivers found near for ride: {}", event.getRideId());
            return;
        }

        //step 2: score each driver and Pick the best one
        Optional<NearByDriverResponse> bestDriver = findBestDriver(nearByDrivers);

        if(bestDriver.isEmpty()){
            log.warn("could not find suitable driver for ride");
            return;
        }

        NearByDriverResponse assignedDriver = bestDriver.get();

        //step 3: publish RideMatchedEvent to Kafka
        RideMatchedEvent matchedEvent = new RideMatchedEvent(
                event.getRideId(),
                event.getRiderId(),
                assignedDriver.getDriverId(),
                assignedDriver.getLatitude(),
                assignedDriver.getLongitude(),
                assignedDriver.getDistanceInKm()
        );

        kafkaTemplate.send(RIDE_MATCHED_EVENT_TOPIC, event.getRideId(), matchedEvent);
        log.info("RideMatchedEvent Published");

    }

    /** distance : 70%
     * Rating 30%
     *
     * score = (1/distance) * distanceWweight + rating * ratingWeight
     * @param drivers
     * @return
     */
    private Optional<NearByDriverResponse> findBestDriver(
            List<NearByDriverResponse> drivers
    ){

        double distanceWeight = 0.7;
        double ratingWeight = 0.3;

        return drivers.stream()
                .max(Comparator.comparingDouble(driver -> {
                    // distance score: clzr = higher score
                    // add 0.1 to avoid division by zero
                    double distanceScore = 1.0 / (driver.getDistanceInKm() + 0.1);

                    //Simulated rating between 4.0 and 5.0
                    // In production: fetch from driver service

                    double simulatedRating = 4.0 + Math.random();

                    //Final weightedd score
                    return (distanceScore * distanceWeight)
                            + (simulatedRating * ratingWeight);
                }));
    }
}
