package com.justlife.booking.service;

import com.justlife.booking.config.SchedulingConfig;
import com.justlife.booking.config.TestSchedulingConfig;
import com.justlife.booking.dto.BookingRequestDto;
import com.justlife.booking.model.Booking;
import com.justlife.booking.model.BookingStatus;
import com.justlife.booking.model.Vehicle;
import com.justlife.booking.repository.BookingCleanerRepository;
import com.justlife.booking.repository.BookingRepository;
import com.justlife.booking.repository.CleanerRepository;
import com.justlife.booking.time.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    CleanerRepository cleanerRepository;

    @Mock
    BookingRepository bookingRepository;

    @Mock
    BookingCleanerRepository bookingCleanerRepository;

    @Mock
    TimeProvider timeProvider;

    SchedulingConfig schedulingConfig;

    BookingService bookingService;

    @BeforeEach
    void setup() {
        schedulingConfig = TestSchedulingConfig.defaultConfig();
        bookingService = new BookingService(
                cleanerRepository,
                bookingRepository,
                bookingCleanerRepository,
                schedulingConfig,
                timeProvider
        );
    }

    @Test
    void shouldRejectBookingInPast() {
        when(timeProvider.now()).thenReturn(
                ZonedDateTime.of(
                        2026, 1, 20, 10, 0, 0, 0,
                        ZoneId.of("Asia/Kolkata")
                )
        );

        BookingRequestDto request = new BookingRequestDto(
                LocalDate.of(2026, 1, 20),
                LocalTime.of(9, 0),
                60,
                1
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.createBooking(request)
        );

        verifyNoInteractions(
                cleanerRepository,
                bookingRepository,
                bookingCleanerRepository
        );
    }

    @Test
    void shouldRejectCancelAfterStart() {
        Vehicle vehicle = new Vehicle("V1");

        Booking booking = new Booking(
                LocalDate.of(2026, 1, 20),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                vehicle
        );
        booking.setStatus(BookingStatus.CREATED);

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        when(timeProvider.now()).thenReturn(
                ZonedDateTime.of(
                        2026, 1, 20, 9, 5, 0, 0,
                        ZoneId.of("Asia/Kolkata")
                )
        );

        assertThrows(
                IllegalStateException.class,
                () -> bookingService.cancelBooking(1L)
        );
    }
}