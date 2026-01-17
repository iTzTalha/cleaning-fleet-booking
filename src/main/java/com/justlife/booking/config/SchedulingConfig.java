package com.justlife.booking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;

@ConfigurationProperties(prefix = "app")
public record SchedulingConfig(
        ZoneId timezone,
        WorkingHours workingHours,
        int breakMinutes,
        DayOfWeek nonWorkingDay,
        Defaults defaults
) {
    public record WorkingHours(LocalTime start, LocalTime end) {}
    public record Defaults(int maxCleanerPerVehicle) {}
}