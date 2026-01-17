package com.justlife.booking.service;

import com.justlife.booking.config.SchedulingConfig;
import com.justlife.booking.config.TestSchedulingConfig;
import com.justlife.booking.dto.VehicleDailyAvailabilityDto;
import com.justlife.booking.repository.BookingCleanerRepository;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private CleanerRepository cleanerRepository;

    @Mock
    private BookingCleanerRepository bookingCleanerRepository;

    @Mock
    private TimeProvider timeProvider;

    private AvailabilityService availabilityService;

    @BeforeEach
    void setUp() {
        SchedulingConfig schedulingConfig = TestSchedulingConfig.defaultConfig();

        availabilityService = new AvailabilityService(
                cleanerRepository,
                bookingCleanerRepository,
                schedulingConfig,
                timeProvider
        );
    }

    @Test
    void shouldThrowExceptionForNonWorkingDay() {
        LocalDate friday = LocalDate.of(2026, 1, 16); // Friday

        assertThrows(
                IllegalArgumentException.class,
                () -> availabilityService.getDailyAvailabilityByVehicle(friday)
        );
    }

    @Test
    void shouldThrowExceptionForPastDate() {
        LocalDate date = LocalDate.of(2026, 1, 10);

        when(timeProvider.now())
                .thenReturn(ZonedDateTime.of(
                        2026, 1, 12, 10, 0, 0, 0,
                        ZoneId.of("Asia/Kolkata")
                ));

        assertThrows(
                IllegalArgumentException.class,
                () -> availabilityService.getDailyAvailabilityByVehicle(date)
        );
    }

    @Test
    void shouldReturnEmptyListWhenNoCleanersExist() {
        LocalDate date = LocalDate.of(2026, 1, 20);

        when(timeProvider.now()).thenReturn(
                ZonedDateTime.of(2026, 1, 19, 10, 0, 0, 0,
                        ZoneId.of("Asia/Kolkata"))
        );

        when(cleanerRepository.findAllWithVehicle())
                .thenReturn(List.of());

        List<VehicleDailyAvailabilityDto> result =
                availabilityService.getDailyAvailabilityByVehicle(date);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRejectPastStartTime() {
        LocalDate date = LocalDate.of(2026, 1, 20);
        LocalTime start = LocalTime.of(9, 0);

        when(timeProvider.now()).thenReturn(
                ZonedDateTime.of(2026, 1, 20, 10, 0, 0, 0,
                        ZoneId.of("Asia/Kolkata"))
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> availabilityService.getAvailabilityByVehicle(date, start, 60)
        );
    }
}