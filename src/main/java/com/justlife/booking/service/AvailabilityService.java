package com.justlife.booking.service;

import com.justlife.booking.config.SchedulingConfig;
import com.justlife.booking.dto.CleanerDailyAvailabilityDto;
import com.justlife.booking.dto.CleanerDto;
import com.justlife.booking.dto.VehicleAvailabilityDto;
import com.justlife.booking.dto.VehicleDailyAvailabilityDto;
import com.justlife.booking.model.BookingCleaner;
import com.justlife.booking.model.Cleaner;
import com.justlife.booking.model.TimeSlot;
import com.justlife.booking.model.Vehicle;
import com.justlife.booking.repository.BookingCleanerRepository;
import com.justlife.booking.repository.CleanerRepository;
import com.justlife.booking.time.TimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final CleanerRepository cleanerRepository;
    private final BookingCleanerRepository bookingCleanerRepository;
    private final SchedulingConfig schedulingConfig;
    private final TimeProvider timeProvider;

    public List<VehicleDailyAvailabilityDto> getDailyAvailabilityByVehicle(
            LocalDate date
    ) {
        if (!schedulingConfig.isWorkingDay(date.getDayOfWeek())) {
            throw new IllegalArgumentException("Non-working day");
        }

        ZonedDateTime businessDay =
                ZonedDateTime.of(
                        date,
                        schedulingConfig.workingHours().end(),
                        schedulingConfig.timezone()
                );

        ZonedDateTime now = timeProvider.now();

        if (businessDay.isBefore(now)) {
            throw new IllegalArgumentException("Cannot check availability for past dates");
        }

        List<Cleaner> cleaners = cleanerRepository.findAllWithVehicle();

        if (cleaners.isEmpty()) {
            return List.of();
        }

        List<BookingCleaner> assignments =
                bookingCleanerRepository
                        .findAssignmentsForCleanersOnDate(cleaners, date);

        Map<Cleaner, List<BookingCleaner>> assignmentsByCleaner =
                assignments.stream()
                        .collect(Collectors.groupingBy(
                                BookingCleaner::getCleaner
                        ));

        Map<Long, List<Cleaner>> cleanersByVehicle =
                cleaners.stream()
                        .collect(Collectors.groupingBy(
                                c -> c.getVehicle().getId()
                        ));

        return cleanersByVehicle.values().stream()
                .filter(c -> !c.isEmpty())
                .map(vehicleCleaners -> {

                    List<CleanerDailyAvailabilityDto> availableCleaners =
                            vehicleCleaners
                                    .stream()
                                    .map(cleaner -> {
                                        List<TimeSlot> slots =
                                                calculateAvailability(
                                                        date,
                                                        assignmentsByCleaner
                                                                .getOrDefault(
                                                                        cleaner,
                                                                        List.of()
                                                                )
                                                );
                                        return new CleanerDailyAvailabilityDto(
                                                cleaner.getId(),
                                                cleaner.getName(),
                                                slots
                                        );
                                    })
                                    .filter(c -> !c.timeSlots().isEmpty())
                                    .toList();

                    Vehicle vehicle = vehicleCleaners.get(0).getVehicle();

                    return new VehicleDailyAvailabilityDto(
                            vehicle.getId(),
                            vehicle.getName(),
                            availableCleaners
                    );
                })
                .filter(v -> !v.cleaners().isEmpty())
                .toList();
    }

    public List<VehicleAvailabilityDto> getAvailabilityByVehicle(
            LocalDate date,
            LocalTime startTime,
            int durationMinutes
    ) {
        validateRequest(date, startTime, durationMinutes);

        List<Cleaner> cleaners = cleanerRepository.findAllWithVehicle();

        if (cleaners.isEmpty()) {
            return List.of();
        }

        LocalTime endTime = startTime.plusMinutes(durationMinutes);

        LocalTime startWithBuffer =
                startTime.minusMinutes(schedulingConfig.breakMinutes());

        LocalTime endWithBuffer =
                endTime.plusMinutes(schedulingConfig.breakMinutes());

        List<BookingCleaner> conflicts =
                bookingCleanerRepository
                        .findConflictingAssignmentsForCleaners(
                                cleaners,
                                date,
                                startWithBuffer,
                                endWithBuffer
                        );

        Map<Cleaner, List<BookingCleaner>> conflictsByCleaner =
                conflicts.stream()
                        .collect(Collectors.groupingBy(
                                BookingCleaner::getCleaner
                        ));

        Map<Long, List<Cleaner>> cleanersByVehicle =
                cleaners.stream()
                        .collect(Collectors.groupingBy(
                                c -> c.getVehicle().getId()
                        ));

        return cleanersByVehicle.values().stream()
                .filter(c -> !c.isEmpty())
                .map(vehicleCleaners -> {

                    List<CleanerDto> availableCleaners =
                            vehicleCleaners.stream()
                                    .filter(cleaner ->
                                            conflictsByCleaner
                                                    .getOrDefault(
                                                            cleaner,
                                                            List.of()
                                                    )
                                                    .isEmpty()
                                    )
                                    .map(cleaner ->
                                            new CleanerDto(
                                                    cleaner.getId(),
                                                    cleaner.getName()
                                            )
                                    )
                                    .toList();

                    Vehicle vehicle = vehicleCleaners.get(0).getVehicle();

                    return new VehicleAvailabilityDto(
                            vehicle.getId(),
                            vehicle.getName(),
                            availableCleaners
                    );
                })
                .filter(v -> !v.cleaners().isEmpty())
                .toList();
    }

    private LocalTime effectiveDayStart(LocalDate date) {
        LocalDate today = timeProvider.now().toLocalDate();

        if (!date.equals(today)) {
            return schedulingConfig.workingHours().start();
        }

        LocalTime nowWithBuffer =
                timeProvider.now()
                        .toLocalTime()
                        .plusMinutes(schedulingConfig.breakMinutes());

        return nowWithBuffer.isAfter(schedulingConfig.workingHours().start())
                ? nowWithBuffer
                : schedulingConfig.workingHours().start();
    }

    private List<TimeSlot> calculateAvailability(
            LocalDate date,
            List<BookingCleaner> assignments
    ) {
        LocalTime start = effectiveDayStart(date);
        LocalTime end = schedulingConfig.workingHours().end();

        if (!start.isBefore(end)) {
            return List.of(); // day already over
        }

        List<TimeSlot> slots = List.of(new TimeSlot(start, end));
        return subtractBookings(slots, assignments);
    }

    private List<TimeSlot> subtractBookings(
            List<TimeSlot> slots,
            List<BookingCleaner> assignments
    ) {

        List<TimeSlot> result = new ArrayList<>(slots);

        for (BookingCleaner bc : assignments) {
            LocalTime start =
                    bc.getBooking().getStartTime()
                            .minusMinutes(schedulingConfig.breakMinutes());

            LocalTime end =
                    bc.getBooking().getEndTime()
                            .plusMinutes(schedulingConfig.breakMinutes());

            result = result.stream()
                    .flatMap(slot -> slot.subtract(start, end).stream())
                    .toList();
        }

        return result;
    }

    private void validateRequest(
            LocalDate date,
            LocalTime startTime,
            int durationMinutes
    ) {
        ZonedDateTime requestedStart = ZonedDateTime.of(date, startTime, schedulingConfig.timezone());
        ZonedDateTime now = timeProvider.now();

        if (!requestedStart.isAfter(now)) {
            throw new IllegalArgumentException("Cannot check availability for past dates");
        }

        if (!schedulingConfig.isWorkingDay(date.getDayOfWeek())) {
            throw new IllegalArgumentException("Cleaners do not work on Fridays");
        }

        if (startTime.isBefore(schedulingConfig.workingHours().start())) {
            throw new IllegalArgumentException("Start time before 08:00");
        }

        LocalTime endTime = startTime.plusMinutes(durationMinutes);

        if (endTime.isAfter(schedulingConfig.workingHours().end())) {
            throw new IllegalArgumentException("Booking ends after 22:00");
        }

        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Invalid duration");
        }
    }
}