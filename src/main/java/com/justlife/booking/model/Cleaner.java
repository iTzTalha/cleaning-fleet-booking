package com.justlife.booking.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "cleaners")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cleaner extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @OneToMany(
            mappedBy = "cleaner",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private final Set<BookingCleaner> bookings = new HashSet<>();

    public Cleaner(String name) {
        this.name = Objects.requireNonNull(name, "Cleaner name must not be null");
    }

    public void setVehicle(Vehicle vehicle) {
        Objects.requireNonNull(vehicle, "Vehicle must not be null");

        removeVehicle();

        this.vehicle = vehicle;
        vehicle.getCleaners().add(this);
    }

    public void removeVehicle() {
        if (this.vehicle != null) {
            this.vehicle.getCleaners().remove(this);
            this.vehicle = null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cleaner)) return false;
        return id != null && id.equals(((Cleaner) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}