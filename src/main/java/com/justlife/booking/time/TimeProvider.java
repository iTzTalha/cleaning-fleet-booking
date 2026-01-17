package com.justlife.booking.time;

import java.time.ZonedDateTime;

public interface TimeProvider {
    ZonedDateTime now();
}