package com.justlife.booking.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Table(
        name = "booking_cleaners",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"booking_id", "cleaner_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookingCleaner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cleaner_id", nullable = false)
    private Cleaner cleaner;

    public BookingCleaner(Booking booking, Cleaner cleaner) {
        this.booking = Objects.requireNonNull(booking);
        this.cleaner = Objects.requireNonNull(cleaner);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookingCleaner that)) return false;
        return booking.equals(that.booking)
                && cleaner.equals(that.cleaner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(booking, cleaner);
    }
}