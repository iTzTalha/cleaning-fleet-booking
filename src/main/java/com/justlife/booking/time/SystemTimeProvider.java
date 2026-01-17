package com.justlife.booking.time;

import com.justlife.booking.config.SchedulingConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Component
@RequiredArgsConstructor
public class SystemTimeProvider implements TimeProvider {

    private final SchedulingConfig schedulingConfig;

    @Override
    public ZonedDateTime now() {
        return ZonedDateTime.now(schedulingConfig.timezone());
    }
}