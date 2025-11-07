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
    public ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        log.info("Logout endpoint called");
        authenticationService.logout(request);
        return ApiResponse.<Void>builder().message("Logout successful").build();
    }
}
