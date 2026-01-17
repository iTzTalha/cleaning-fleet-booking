package com.justlife.booking.service;

import com.justlife.booking.dto.BookingRequestDto;
import com.justlife.booking.dto.BookingRescheduleRequestDto;
import com.justlife.booking.dto.BookingResponseDto;
import com.justlife.booking.model.Booking;
import com.justlife.booking.model.Cleaner;
import com.justlife.booking.model.Vehicle;
import com.justlife.booking.repository.BookingRepository;
import com.justlife.booking.repository.CleanerRepository;
import com.justlife.booking.repository.VehicleRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase
@ActiveProfiles("test")
class BookingServiceIntegrationTest {

    @Autowired
    BookingService bookingService;

    @Autowired
    CleanerRepository cleanerRepository;

    @Autowired
    VehicleRepository vehicleRepository;

    @Autowired
    BookingRepository bookingRepository;

    @BeforeEach
    void setup() {
        Vehicle vehicle = vehicleRepository.saveAndFlush(new Vehicle("V1"));

        for (int i = 0; i < 3; i++) {
            Cleaner cleaner = new Cleaner("C" + i);
            cleaner.setVehicle(vehicle);
            cleanerRepository.save(cleaner);
        }
    }

    @Test
    void shouldCreateBookingWithCleaners() {
        BookingRequestDto request = new BookingRequestDto(
                LocalDate.of(2026, 1, 20),
                LocalTime.of(10, 0),
                60,
                2
        );

        BookingResponseDto response =
                bookingService.createBooking(request);

        Booking booking =
                bookingRepository.findById(response.bookingId()).orElseThrow();

        assertEquals(2, booking.getCleaners().size());
    }

    @Test
    void rescheduleShouldReleaseOldCleaners() {
        BookingResponseDto booking =
                bookingService.createBooking(
                        new BookingRequestDto(
                                LocalDate.of(2026, 1, 20),
                                LocalTime.of(10, 0),
                                60,
                                2
                        )
                );

        BookingResponseDto updated =
                bookingService.rescheduleBooking(
                        booking.bookingId(),
                        new BookingRescheduleRequestDto(
                                LocalDate.of(2026, 1, 21),
                                LocalTime.of(12, 0)
                        )
                );

        Booking dbBooking =
                bookingRepository.findById(updated.bookingId()).orElseThrow();

        assertEquals(
                LocalDate.of(2026, 1, 21),
                dbBooking.getDate()
        );
        assertEquals(2, dbBooking.getCleaners().size());
    }
}