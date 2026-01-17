package com.justlife.booking.repository;

import com.justlife.booking.model.BookingCleaner;
import com.justlife.booking.model.Cleaner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface BookingCleanerRepository extends JpaRepository<BookingCleaner, Long> {

    @Query("""
        SELECT bc
        FROM BookingCleaner bc
        JOIN FETCH bc.booking b
        WHERE bc.cleaner IN :cleaners
          AND b.date = :date
        ORDER BY b.startTime
        """)
    List<BookingCleaner> findAssignmentsForCleanersOnDate(
            @Param("cleaners") Collection<Cleaner> cleaners,
            @Param("date") LocalDate date
    );

    @Query("""
        SELECT bc
        FROM BookingCleaner bc
        JOIN FETCH bc.booking b
        WHERE bc.cleaner IN :cleaners
          AND b.date = :date
          AND (b.startTime < :endWithBuffer AND b.endTime > :startWithBuffer)
        """)
    List<BookingCleaner> findConflictingAssignmentsForCleaners(
            @Param("cleaners") Collection<Cleaner> cleaners,
            @Param("date") LocalDate date,
            @Param("startWithBuffer") LocalTime startWithBuffer,
            @Param("endWithBuffer") LocalTime endWithBuffer
    );

    @Query("""
    SELECT bc
    FROM BookingCleaner bc
    JOIN FETCH bc.booking b
    WHERE bc.cleaner IN :cleaners
      AND b.id <> :bookingId
      AND b.date = :date
      AND (b.startTime < :endTime AND b.endTime > :startTime)
""")
    List<BookingCleaner> findConflictsExcludingBooking(
            @Param("bookingId") Long bookingId,
            @Param("cleaners") List<Cleaner> cleaners,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );
}