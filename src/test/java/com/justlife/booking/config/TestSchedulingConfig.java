package com.justlife.booking.config;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;

public final class TestSchedulingConfig {

    public static SchedulingConfig defaultConfig() {
        return new SchedulingConfig(
                ZoneId.of("Asia/Kolkata"),
                new SchedulingConfig.WorkingHours(
                        LocalTime.of(8, 0),
                        LocalTime.of(22, 0)
                ),
                30,
                DayOfWeek.FRIDAY,
                new SchedulingConfig.Defaults(5)
        );
    }
}