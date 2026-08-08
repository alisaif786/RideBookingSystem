package com.ridebook.rideservice.repository;

import com.ridebook.rideservice.model.Ride;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RideRepository  extends  JpaRepository<Ride, Long> {
}
