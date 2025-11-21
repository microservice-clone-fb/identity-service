package com.tamm.identity.controller;

import java.text.ParseException;

import org.springframework.web.bind.annotation.*;

import com.nimbusds.jose.JOSEException;
import com.tamm.identity.dto.request.*;
import com.tamm.identity.dto.response.AuthenticationResponse;
import com.tamm.identity.dto.response.IntrospectResponse;
import com.tamm.identity.service.AuthenticationService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/login")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        var result = authenticationService.login(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/register")
    public ApiResponse<AuthenticationResponse> register(@RequestBody RegistrationRequest request) {
        var result = authenticationService.register(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @GetMapping("/get-profile-by-userid/{userId}")
    ApiResponse<AuthenticationResponse> getProfileByUserId(@PathVariable String userId) {
        var result = authenticationService.getProfileById(userId);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) {
        log.info("Introspect endpoint called");
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder().result(result).build();
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refreshToken(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {
        log.info("Refresh token endpoint called");
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) LogoutRequest request)
            throws ParseException, JOSEException {
        //        log.info("Logout endpoint called");
        //        log.info("Authorization header: {}", authHeader);
        //        log.info("Received logout request: {}", request);

        // Build logout request with tokens from different sources
        String accessToken = null;
        String refreshToken = null;

        // Priority 1: Get access token from Authorization header
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
            //            log.info("Access token extracted from Authorization header");
        }

        // Priority 2: Get tokens from request body
        if (request != null) {
            if (accessToken == null
                    && request.getToken() != null
                    && !request.getToken().trim().isEmpty()) {
                accessToken = request.getToken();
                //                log.info("Access token extracted from request body");
            }
            if (request.getRefreshToken() != null
                    && !request.getRefreshToken().trim().isEmpty()) {
                refreshToken = request.getRefreshToken();
                //                log.info("Refresh token extracted from request body");
            }
        }

        // Build logout request
        LogoutRequest logoutRequest = LogoutRequest.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .build();

        //        log.info(
        //                "Logout request prepared - hasAccessToken: {}, hasRefreshToken: {}",
        //                accessToken != null,
        //                refreshToken != null);
        authenticationService.logout(logoutRequest);
        return ApiResponse.<Void>builder().message("Logout successful").build();
    }
}
