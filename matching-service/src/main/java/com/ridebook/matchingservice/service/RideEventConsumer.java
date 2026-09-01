package com.ridebook.matchingservice.service;

import com.ridebook.matchingservice.event.RideRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideEventConsumer {

    private final MatchingService matchingService;

    /**
     * Listens to ride.requested Kafka topic
     * Triggered everyy time ride service publishes a new ride request
     *
     * Flow:
     * Ride Service >> Kafka (ride.requested) >> This Consumer >> MatchingService
     */

    @KafkaListener(
            topics = "ride.requested",
            groupId = "matching-service-group"
    )
    public void consumeRideRequestedEvent(RideRequestedEvent event) {
        try {
            matchingService.matchedDriverForRide(event);
        } catch (Exception e) {
            log.error(
                    "Error processing ride request: {} - {}",
                    event.getRideId(),
                    e.getMessage()
            );
        }
    }
}