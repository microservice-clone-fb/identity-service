package com.tamm.identity.service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.tamm.identity.dto.request.*;
import com.tamm.identity.dto.response.AuthenticationResponse;
import com.tamm.identity.dto.response.IntrospectResponse;
import com.tamm.identity.entity.InvalidatedToken;
import com.tamm.identity.entity.Permission;
import com.tamm.identity.entity.Role;
import com.tamm.identity.entity.User;
import com.tamm.identity.exception.AppException;
import com.tamm.identity.exception.ErrorCode;
import com.tamm.identity.mapper.UserMapper;
import com.tamm.identity.repository.InvalidatedTokenRepository;
import com.tamm.identity.repository.UserRepository;
import com.tamm.identity.repository.httpclient.FileClient;
import com.tamm.identity.repository.httpclient.ProfileClient;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    ProfileClient profileClient;
    FileClient fileClient;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION; // Access token duration (seconds) - 15 minutes

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION; // Refresh token duration (seconds) - 7 days

    public IntrospectResponse introspect(IntrospectRequest request) {
        log.info("Run into authenticationService.introspect");
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token, false);
        } catch (AppException | JOSEException | ParseException e) {
            isValid = false;
        }

        return IntrospectResponse.builder()
                .valid(isValid)
                .message(isValid ? "Token is valid" : "Token is invalid")
                .build();
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signedJWT = verifyToken(request.getToken(), true);

        var jti = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        // ✅ Invalidate old refresh token
        InvalidatedToken invalidatedToken =
                InvalidatedToken.builder().id(jti).expiryTime(expiryTime).build();
        invalidatedTokenRepository.save(invalidatedToken);

        // ✅ Get user by subject (userId)
        var userId = signedJWT.getJWTClaimsSet().getSubject();
        var user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        // ✅ Generate new tokens
        String newAccessToken = generateAccessToken(user);
        String newRefreshToken = generateRefreshToken(user);

        // ✅ Calculate expiration times
        long accessTokenExpiration =
                Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli();
        long refreshTokenExpiration =
                Instant.now().plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli();

        return AuthenticationResponse.builder()
                .token(newAccessToken)
                .expirationTime(accessTokenExpiration)
                .refreshToken(newRefreshToken)
                .refreshTokenExpiration(refreshTokenExpiration)
                .authenticated(true)
                .userId(user.getId())
                .build();
    }

    /**
     * Generate Access Token (15 minutes)
     */
    private String generateAccessToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        Instant now = Instant.now();
        Instant expiration = now.plus(VALID_DURATION, ChronoUnit.SECONDS);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId())
                .issuer("devteria.com")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(expiration))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .claim("tokenType", "ACCESS")
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create access token", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Generate Refresh Token (7 days)
     */
    private String generateRefreshToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        Instant now = Instant.now();
        Instant expiration = now.plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId())
                .issuer("devteria.com")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(expiration))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .claim("tokenType", "REFRESH")
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create refresh token", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Verify token - supports both access and refresh tokens
     * @param token JWT token to verify
     * @param isRefresh true if verifying refresh token, false for access token
     */
    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        // ✅ Verify signature first (mandatory)
        if (!signedJWT.verify(verifier)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // ✅ Check if token is blacklisted (regardless of type)
        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        Date now = new Date();

        if (isRefresh) {
            // ✅ For refresh token: allow expired, just must be within refreshable duration
            if (!expiryTime.after(now)) {
                throw new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED);
            }
        } else {
            // ✅ For access token: must not be expired
            if (!expiryTime.after(now)) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }
        }

        return signedJWT;
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (!CollectionUtils.isEmpty(user.getUserRoles())) {
            user.getUserRoles().forEach(userRole -> {
                Role role = userRole.getRole();
                stringJoiner.add("ROLE_" + role.getName());

                // ✅ Get permissions through RolePermission
                if (!CollectionUtils.isEmpty(role.getRolePermissions())) {
                    role.getRolePermissions().forEach(rolePermission -> {
                        Permission permission = rolePermission.getPermission();
                        stringJoiner.add(permission.getName());
                    });
                }
            });
        }

        return stringJoiner.toString();
    }

    public AuthenticationResponse register(RegistrationRequest request) {
        log.info("Run into register service with body: {}", request.toString());

        // Check if account already exists
        if (userRepository.existsByUsername(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // Check if email exists in profile service
        if (profileClient.findByEmail(request.getEmail()).getResult() != null) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // Create user in identity service
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user = userRepository.save(user);

        // Create profile (without file)
        ProfileCreationRequest profileCreationRequest = ProfileCreationRequest.builder()
                .userId(user.getId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .bio(request.getBio())
                .contactInfo(request.getContactInfo())
                .type(request.getType())
                .gender(request.getGender())
                .build();

        var profileResponse = profileClient.createProfile(profileCreationRequest);

        // ✅ Generate both access and refresh tokens
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        long accessTokenExpiration =
                Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli();
        long refreshTokenExpiration =
                Instant.now().plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli();

        return AuthenticationResponse.builder()
                .token(accessToken)
                .expirationTime(accessTokenExpiration)
                .refreshToken(refreshToken)
                .refreshTokenExpiration(refreshTokenExpiration)
                .authenticated(true)
                .userId(user.getId())
                .userProfile(profileResponse.getResult())
                .build();
    }

    public AuthenticationResponse login(AuthenticationRequest request) {
        var userOpt = userRepository.findByUsername(request.getUsername()).or(() -> {
            var profile = profileClient.findProfileByAnyField(request.getUsername());
            var userId = profile.getResult() != null ? profile.getResult().getUserId() : null;

            if (userId == null) {
                return Optional.empty();
            }
            return userRepository.findById(userId);
        });

        User user;
        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            throw new AppException(ErrorCode.LOGIN_FAILED);
        }

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!authenticated) {
            throw new AppException(ErrorCode.LOGIN_FAILED);
        }

        // ✅ Generate both access and refresh tokens
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        long accessTokenExpiration =
                Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli();
        long refreshTokenExpiration =
                Instant.now().plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli();

        return AuthenticationResponse.builder()
                .token(accessToken)
                .expirationTime(accessTokenExpiration)
                .refreshToken(refreshToken)
                .refreshTokenExpiration(refreshTokenExpiration)
                .authenticated(true)
                .userId(user.getId())
                .build();
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            var signToken = verifyToken(request.getToken(), true);

            String jti = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder().id(jti).expiryTime(expiryTime).build();

            invalidatedTokenRepository.save(invalidatedToken);
        } catch (AppException exception) {
            log.info("Token already expired or invalid");
        }
    }
}
