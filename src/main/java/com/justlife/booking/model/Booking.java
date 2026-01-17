package com.justlife.booking.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "bookings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Booking extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false)
    private LocalDate date;

    @Setter
    @Column(nullable = false)
    private LocalTime startTime;

    @Setter
    @Column(nullable = false)
    private LocalTime endTime;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @OneToMany(
            mappedBy = "booking",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private final Set<BookingCleaner> cleaners = new HashSet<>();

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    public Booking(
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            Vehicle vehicle
    ) {
        this.date = Objects.requireNonNull(date);
        this.startTime = Objects.requireNonNull(startTime);
        this.endTime = Objects.requireNonNull(endTime);
        this.vehicle = Objects.requireNonNull(vehicle);
        this.status = BookingStatus.CREATED;

        validateTimeRange();
    }

    public void assignCleaner(Cleaner cleaner) {
        BookingCleaner assignment = new BookingCleaner(this, cleaner);
        cleaners.add(assignment);
    }

    public int getCleanerCount() {
        return cleaners.size();
    }

    public int getDurationMinutes() {
        return (int) Duration.between(startTime, endTime).toMinutes();
    }

    public void clearCleaners() {
        cleaners.clear();
    }

    public void cancel() {
        clearCleaners();
        setStatus(BookingStatus.CANCELLED);
    }

    private void validateTimeRange() {
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booking)) return false;
        return id != null && id.equals(((Booking) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}