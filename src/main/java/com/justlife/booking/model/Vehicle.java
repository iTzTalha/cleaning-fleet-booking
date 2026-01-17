package com.justlife.booking.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "vehicles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Vehicle extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(
            mappedBy = "vehicle",
            cascade = CascadeType.ALL
    )
    private final Set<Cleaner> cleaners = new HashSet<>();

    public Vehicle(String name) {
        this.name = Objects.requireNonNull(name, "Vehicle name must not be null");
    }

    public void addCleaner(Cleaner cleaner) {
        cleaners.add(cleaner);
        cleaner.setVehicle(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicle)) return false;
        return id != null && id.equals(((Vehicle) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
