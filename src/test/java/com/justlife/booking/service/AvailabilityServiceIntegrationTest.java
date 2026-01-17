package com.justlife.booking.service;

import com.justlife.booking.config.TimeProviderTestConfig;
import com.justlife.booking.dto.VehicleAvailabilityDto;
import com.justlife.booking.dto.VehicleDailyAvailabilityDto;
import com.justlife.booking.model.Booking;
import com.justlife.booking.model.Cleaner;
import com.justlife.booking.model.TimeSlot;
import com.justlife.booking.model.Vehicle;
import com.justlife.booking.repository.BookingRepository;
import com.justlife.booking.repository.CleanerRepository;
import com.justlife.booking.repository.VehicleRepository;
import com.justlife.booking.time.TimeProvider;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase
@Import(TimeProviderTestConfig.class)
@ActiveProfiles("test")
class AvailabilityServiceIntegrationTest {

    @Autowired
    private AvailabilityService availabilityService;

    @Autowired
    private CleanerRepository cleanerRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TimeProvider timeProvider;

    @Test
    void shouldReturnFullDayAvailabilityWhenNoBookingsExist() {
        Vehicle vehicle = vehicleRepository.saveAndFlush(
                new Vehicle("V1")
        );

        Cleaner cleaner = new Cleaner("A");
        cleaner.setVehicle(vehicle);
        cleanerRepository.saveAndFlush(cleaner);

        LocalDate date = LocalDate.of(2026, 1, 20);

        when(timeProvider.now()).thenReturn(
                ZonedDateTime.of(
                        2026, 1, 19, 10, 0, 0, 0,
                        ZoneId.of("Asia/Kolkata")
                )
        );

        List<VehicleDailyAvailabilityDto> result =
                availabilityService.getDailyAvailabilityByVehicle(date);

        TimeSlot slot = result.get(0)
                .cleaners().get(0)
                .timeSlots().get(0);

        assertEquals(LocalTime.of(8, 0), slot.start());
        assertEquals(LocalTime.of(22, 0), slot.end());
    }

    @Test
    void shouldExcludeCleanerWithConflictingBooking() {
        // given
        Vehicle vehicle = vehicleRepository.saveAndFlush(
                new Vehicle("V1")
        );

        Cleaner cleaner = new Cleaner("A");
        cleaner.setVehicle(vehicle);
        cleanerRepository.saveAndFlush(cleaner);

        Booking booking = new Booking(
                LocalDate.of(2026, 1, 20),
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                vehicle
        );

        booking.assignCleaner(cleaner);
        bookingRepository.saveAndFlush(booking);

        when(timeProvider.now()).thenReturn(
                ZonedDateTime.of(
                        2026, 1, 19, 9, 0, 0, 0,
                        ZoneId.of("Asia/Kolkata")
                )
        );

        List<VehicleAvailabilityDto> result =
                availabilityService.getAvailabilityByVehicle(
                        LocalDate.of(2026, 1, 20),
                        LocalTime.of(10, 0),
                        60
                );

        assertTrue(result.isEmpty());
    }
}