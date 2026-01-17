package com.justlife.booking.seed;

import com.justlife.booking.model.Cleaner;
import com.justlife.booking.model.Vehicle;
import com.justlife.booking.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "app.seed.enabled",
        havingValue = "true"
)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final VehicleRepository vehicleRepository;

    @Override
    public void run(String... args) {

        if (vehicleRepository.count() > 0) {
            return;
        }

        for (int v = 1; v <= 5; v++) {
            Vehicle vehicle = new Vehicle("Vehicle-" + v);

            for (int c = 1; c <= 5; c++) {
                Cleaner cleaner = new Cleaner("Cleaner-" + v + "-" + c);
                vehicle.addCleaner(cleaner);
            }

            vehicleRepository.save(vehicle);
        }

        System.out.println("✅ Initialized 5 vehicles and 25 cleaners");
    }
}