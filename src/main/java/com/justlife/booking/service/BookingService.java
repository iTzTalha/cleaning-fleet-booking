package com.justlife.booking.service;

import com.justlife.booking.config.SchedulingConfig;
import com.justlife.booking.dto.BookingRequestDto;
import com.justlife.booking.dto.BookingRescheduleRequestDto;
import com.justlife.booking.dto.BookingResponseDto;
import com.justlife.booking.dto.CleanerDto;
import com.justlife.booking.model.*;
import com.justlife.booking.repository.BookingCleanerRepository;
import com.justlife.booking.repository.BookingRepository;
import com.justlife.booking.repository.CleanerRepository;
import com.justlife.booking.time.TimeProvider;
import com.justlife.booking.util.SchedulingUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final CleanerRepository cleanerRepository;
    private final BookingRepository bookingRepository;
    private final BookingCleanerRepository bookingCleanerRepository;
    private final SchedulingConfig schedulingConfig;
    private final TimeProvider timeProvider;

    @Transactional
    public BookingResponseDto createBooking(BookingRequestDto request) {

        validateCreate(request);

        LocalDate date = request.date();
        LocalTime startTime = request.startTime();
        LocalTime endTime = startTime.plusMinutes(request.durationMinutes());

        LocalTime startWithBuffer = startTime.minusMinutes(schedulingConfig.breakMinutes());
        LocalTime endWithBuffer = endTime.plusMinutes(schedulingConfig.breakMinutes());

        List<Cleaner> cleaners = cleanerRepository.findAllWithVehicle();

        if (cleaners.isEmpty()) {
            throw new IllegalStateException("No cleaners configured");
        }

        List<BookingCleaner> conflicts =
                bookingCleanerRepository.findConflictingAssignmentsForCleaners(
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

        for (List<Cleaner> vehicleCleaners : cleanersByVehicle.values()) {
            List<Cleaner> availableCleaners =
                    vehicleCleaners.stream()
                            .filter(c ->
                                    conflictsByCleaner
                                            .getOrDefault(c, List.of())
                                            .isEmpty()
                            )
                            .toList();

            if (request.cleanerCount() <= availableCleaners.size()) {

                Vehicle vehicle = vehicleCleaners.get(0).getVehicle();

                Booking booking = new Booking(
                        date,
                        startTime,
                        endTime,
                        vehicle
                );

                List<Cleaner> assigned =
                        availableCleaners.subList(
                                0,
                                request.cleanerCount()
                        );

                assigned.forEach(booking::assignCleaner);

                booking.setStatus(BookingStatus.CREATED);
                bookingRepository.save(booking);

                return new BookingResponseDto(
                        booking.getId(),
                        booking.getDate(),
                        booking.getStartTime(),
                        booking.getEndTime(),
                        vehicle.getId(),
                        vehicle.getName(),
                        assigned.stream()
                                .map(c ->
                                        new CleanerDto(
                                                c.getId(),
                                                c.getName()
                                        )
                                )
                                .toList()
                );
            }
        }

        throw new IllegalStateException(
                "Not enough cleaners available for the requested time"
        );
    }

    @Transactional
    public void cancelBooking(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Booking not found: " + bookingId
                        )
                );

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }

        ZonedDateTime bookingStart =
                ZonedDateTime.of(
                        booking.getDate(),
                        booking.getStartTime(),
                        schedulingConfig.timezone()
                );


        ZonedDateTime now = timeProvider.now();

        if (!bookingStart.isAfter(now)) {
            throw new IllegalStateException(
                    "Cannot cancel a booking that has already started"
            );
        }

        booking.cancel();
    }

    @Transactional
    public BookingResponseDto rescheduleBooking(Long bookingId, BookingRescheduleRequestDto request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Booking not found: " + bookingId
                        )
                );

        if (booking.getDate().equals(request.date())
                && booking.getStartTime().equals(request.startTime())) {

            return new BookingResponseDto(
                    booking.getId(),
                    booking.getDate(),
                    booking.getStartTime(),
                    booking.getEndTime(),
                    booking.getVehicle().getId(),
                    booking.getVehicle().getName(),
                    booking.getCleaners().stream()
                            .map(c -> new CleanerDto(c.getCleaner().getId(), c.getCleaner().getName()))
                            .toList()
            );
        }

        validateReschedule(booking, request);

        LocalDate date = request.date();
        LocalTime startTime = request.startTime();
        LocalTime endTime = request.startTime().plusMinutes(booking.getDurationMinutes());

        LocalTime startWithBuffer =
                startTime.minusMinutes(schedulingConfig.breakMinutes());
        LocalTime endWithBuffer =
                endTime.plusMinutes(schedulingConfig.breakMinutes());

        List<Cleaner> cleaners = cleanerRepository.findAllWithVehicle();

        if (cleaners.isEmpty()) {
            throw new IllegalStateException("No cleaners configured");
        }

        List<BookingCleaner> conflicts =
                bookingCleanerRepository
                        .findConflictsExcludingBooking(
                                booking.getId(),
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

        for (List<Cleaner> vehicleCleaners : cleanersByVehicle.values()) {
            List<Cleaner> available =
                    vehicleCleaners.stream()
                            .filter(c ->
                                    conflictsByCleaner
                                            .getOrDefault(c, List.of())
                                            .isEmpty()
                            )
                            .toList();

            if (booking.getCleanerCount() <= available.size()) {

                Vehicle vehicle = vehicleCleaners.get(0).getVehicle();

                booking.setDate(date);
                booking.setStartTime(startTime);
                booking.setEndTime(endTime);
                booking.setVehicle(vehicle);

                List<Cleaner> assigned =
                        available.subList(0, booking.getCleanerCount());

                booking.clearCleaners();
                bookingRepository.flush();

                assigned.forEach(booking::assignCleaner);

                bookingRepository.save(booking);

                return new BookingResponseDto(
                        booking.getId(),
                        booking.getDate(),
                        booking.getStartTime(),
                        booking.getEndTime(),
                        vehicle.getId(),
                        vehicle.getName(),
                        assigned.stream()
                                .map(c ->
                                        new CleanerDto(
                                                c.getId(),
                                                c.getName()
                                        )
                                )
                                .toList()
                );
            }
        }
        throw new IllegalStateException("Not enough cleaners available for the requested update");
    }

    private void validateCreate(BookingRequestDto request) {
        validateRequest(
                request.date(),
                request.startTime(),
                request.durationMinutes(),
                request.cleanerCount()
        );
    }

    private void validateReschedule(Booking booking, BookingRescheduleRequestDto request) {
        if (booking.getStatus() != BookingStatus.CREATED) {
            if (booking.getStatus() == BookingStatus.CANCELLED) {
                throw new IllegalArgumentException("Cannot update a booking that has been cancelled");
            }

            throw new IllegalStateException("Only active bookings can be rescheduled");
        }

        ZonedDateTime bookingStart =
                ZonedDateTime.of(
                        booking.getDate(),
                        booking.getStartTime(),
                        schedulingConfig.timezone()
                );

        if (!bookingStart.isAfter(timeProvider.now())) {
            throw new IllegalStateException("Cannot reschedule a booking that has already started");
        }

        if (booking.getCleanerCount() > schedulingConfig.defaults().maxCleanerPerVehicle()) {
            throw new IllegalStateException(
                    "Existing booking cleaner count exceeds current system limits"
            );
        }

        validateRequest(request.date(), request.startTime(), booking.getDurationMinutes(), booking.getCleanerCount());
    }

    private void validateRequest(
            LocalDate date,
            LocalTime startTime,
            Integer durationMinutes,
            Integer cleanerCount
    ) {
        if (date != null) {
            if (!SchedulingUtil.isWorkingDay(
                    date.getDayOfWeek(),
                    schedulingConfig.nonWorkingDay()
            )) {
                throw new IllegalArgumentException("Non-working day");
            }

            if (startTime != null) {
                ZonedDateTime bookingStart = ZonedDateTime.of(date, startTime, schedulingConfig.timezone());

                ZonedDateTime now =  timeProvider.now();

                if(!bookingStart.isAfter(now)) {
                    throw new IllegalArgumentException("Booking must be in the future");
                }

                if (startTime.isBefore(schedulingConfig.workingHours().start())) {
                    throw new IllegalArgumentException("Before working hours");
                }

                if (durationMinutes != null && durationMinutes <= 0) {
                    throw new IllegalArgumentException("Invalid duration");
                }

                if (durationMinutes != null && startTime.plusMinutes(durationMinutes.longValue())
                        .isAfter(schedulingConfig.workingHours().end())) {
                    throw new IllegalArgumentException("After working hours");
                }
            }
        }

        if (cleanerCount != null &&
                (cleanerCount < 1 ||
                        cleanerCount > schedulingConfig.defaults().maxCleanerPerVehicle())) {
            throw new IllegalArgumentException("Cleaner count must be between 1 and " + schedulingConfig.defaults().maxCleanerPerVehicle());
        }
    }
}