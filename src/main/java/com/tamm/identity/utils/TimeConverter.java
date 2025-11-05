package com.tamm.identity.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TimeConverter {
    public static String convertToStandardTime(Instant instant, String zoneId, String countryName) {
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of(zoneId));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss_dd/MM/yyyy", Locale.ENGLISH);

        String time = zonedDateTime.format(formatter);
        String offset = zonedDateTime.getOffset().getId(); // ví dụ: +07:00
        String gmt = "GMT" + offset;

        return gmt + "_" + time + "_" + countryName;
    }
}
