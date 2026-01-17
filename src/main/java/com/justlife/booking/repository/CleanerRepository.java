package com.justlife.booking.repository;

import com.justlife.booking.model.Cleaner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CleanerRepository extends JpaRepository<Cleaner, Long> {

    @Query("select c from Cleaner c where c.vehicle is not null")
    List<Cleaner> findAllWithVehicle();
}