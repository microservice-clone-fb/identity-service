package com.tamm.identity.service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.extern.slf4j.Slf4j;

// @Deprecated // shouldn't use
@Slf4j
public class AuditServices {

    private static final String DEFAULT_ZONE_ID = "Asia/Ho_Chi_Minh";
    private static final String DEFAULT_COUNTRY = "Vietnam";
    private static final String ANONYMOUS_USER = "SYSTEM";

    /**
     * Lấy username hiện tại từ SecurityContext
     */
    public String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return ANONYMOUS_USER;
            }

            String username = authentication.getName();
            return (username != null && !username.equals("anonymousUser")) ? username : ANONYMOUS_USER;

        } catch (Exception e) {
            log.warn("Cannot get current username from SecurityContext: {}", e.getMessage());
            return ANONYMOUS_USER;
        }
    }

    /**
     * Tạo history entry với format: username_action_time
     */
    public String createHistoryEntry(String username, String action, Instant instant) {
        String zoneId = getZoneIdFromRequest();
        String countryName = getCountryFromRequest();

        String formattedTime = convertToStandardTime(instant, zoneId, countryName);

        return username + "_" + action + "_" + formattedTime;
    }

    /**
     * Lấy zoneId từ request header hoặc dùng default
     */
    private String getZoneIdFromRequest() {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attr.getRequest();

            String zoneId = request.getHeader("X-Timezone");
            if (zoneId != null && !zoneId.trim().isEmpty()) {
                // Validate zoneId
                ZoneId.of(zoneId);
                return zoneId;
            }
        } catch (Exception e) {
            log.debug("Cannot get timezone from request: {}", e.getMessage());
        }

        return DEFAULT_ZONE_ID;
    }

    /**
     * Lấy country từ request header hoặc dùng default
     */
    private String getCountryFromRequest() {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attr.getRequest();

            String country = request.getHeader("X-Country");
            if (country != null && !country.trim().isEmpty()) {
                return country;
            }
        } catch (Exception e) {
            log.debug("Cannot get country from request: {}", e.getMessage());
        }

        return DEFAULT_COUNTRY;
    }

    /**
     * Convert Instant to standard time format
     * Format: GMT+07:00_23:45:30_15/08/2025_Vietnam
     */
    public static String convertToStandardTime(Instant instant, String zoneId, String countryName) {
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of(zoneId));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss_dd/MM/yyyy", Locale.ENGLISH);

        String time = zonedDateTime.format(formatter);
        String offset = zonedDateTime.getOffset().getId(); // ví dụ: +07:00
        String gmt = "GMT" + offset;

        return gmt + "_" + time + "_" + countryName;
    }
}
