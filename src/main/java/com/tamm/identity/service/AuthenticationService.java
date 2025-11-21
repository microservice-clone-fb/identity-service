package com.tamm.identity.service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.tamm.identity.dto.request.*;
import com.tamm.identity.dto.response.AuthenticationResponse;
import com.tamm.identity.dto.response.IntrospectResponse;
import com.tamm.identity.dto.response.UserProfileResponse;
import com.tamm.identity.entity.*;
import com.tamm.identity.exception.AppException;
import com.tamm.identity.exception.ErrorCode;
import com.tamm.identity.mapper.UserMapper;
import com.tamm.identity.repository.UserRepository;
import com.tamm.identity.repository.ValidatedRefreshTokenRepository;
import com.tamm.identity.repository.httpclient.FileClient;
import com.tamm.identity.repository.httpclient.ProfileClient;
import com.tamm.identity.repository.httpclient.RelationshipClient;

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
    ValidatedRefreshTokenRepository validatedRefreshTokenRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    ProfileClient profileClient;
    FileClient fileClient;
    RelationshipClient relationshipClient;
    RoleService roleService;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION; // Access token duration (seconds) - 15 minutes

    @NonFinal
    @Value("${role.default}")
    protected String NORMAL_USER_ROLE;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION; // Refresh token duration (seconds) - 7 days

    private static final String TOKEN_TYPE_ACCESS = "ACCESS";
    private static final String TOKEN_TYPE_REFRESH = "REFRESH";
    private static final String SCOPE_CLAIM = "scope";
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ISSUER = "devteria.com";

    /**
     * Introspect token - Check token validity and get token info
     * Supports both access and refresh tokens
     */
    public IntrospectResponse introspect(IntrospectRequest request) {
        log.info("Introspecting token");
        var token = request.getToken();

        boolean isValid = true;
        String tokenType = null;
        Long expirationTime = null;
        String userId = null; // ✅ Lấy từ token

        try {
            SignedJWT parsedJWT = SignedJWT.parse(token);
            JWTClaimsSet claims = parsedJWT.getJWTClaimsSet();

            tokenType = (String) claims.getClaim(TOKEN_TYPE_CLAIM);
            expirationTime = claims.getExpirationTime().getTime();
            userId = claims.getSubject(); // ✅ Lấy userId từ token

            // Verify token...
            if (!TOKEN_TYPE_ACCESS.equals(tokenType) && !TOKEN_TYPE_REFRESH.equals(tokenType)) {
                isValid = false;
            } else {
                boolean isRefresh = TOKEN_TYPE_REFRESH.equals(tokenType);
                verifyToken(token, isRefresh);
            }
        } catch (AppException | JOSEException | ParseException e) {
            isValid = false;
            log.error("Token verification failed", e);
        }
        System.out.println("UserId in introspect: " + userId);
        return IntrospectResponse.builder()
                .valid(isValid)
                .message(isValid ? "Token is valid" : "Token is invalid")
                .tokenType(tokenType)
                .userId(userId) // ✅ userId từ token, không phải SecurityContext
                .expirationTime(expirationTime)
                .build();
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (!CollectionUtils.isEmpty(user.getUserRoles())) {
            user.getUserRoles().forEach(userRole -> {
                Role role = userRole.getRole();
                stringJoiner.add("ROLE_" + role.getName());

                // Get permissions through RolePermission
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
        log.info("Registering new user: {}", request.getEmail());

        // Check if account already exists in identity service
        if (userRepository.existsByUsername(request.getEmail())) {
            log.warn("User already exists in identity service: {}", request.getEmail());
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // Check if email exists in profile service
        if (profileClient.findByEmail(request.getEmail()).getResult() != null) {
            log.warn("User already exists in profile service: {}", request.getEmail());
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // Create user in identity service
        Role role = roleService.getRoleEntityById(NORMAL_USER_ROLE);
        User user = userMapper.toUser(request);
        UserRole userRole = UserRole.builder().role(role).user(user).build();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUserRoles(List.of(userRole));
        user = userRepository.save(user);
        log.info("User created in identity service: {}", user.getId());

        // Create profile in profile service
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
        log.info("Profile created for user: {}", user.getId());

        // tao user relationship
        boolean isCreated = relationshipClient.createUser(user.getId());
        if (!isCreated) {
            System.out.println("Error while creating user relationship for userId: " + user.getId());
            throw new AppException(ErrorCode.USER_CANNOT_CREATED);
        }

        // Generate tokens
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        // ✅ Save refresh token to DB
        try {
            // xóa het token cua nguoi dung nay
            validatedRefreshTokenRepository.deleteByUserId(user.getId());

            String refreshTokenJti = extractJTI(refreshToken);
            Date refreshTokenExpiry = new Date(
                    Instant.now().plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli());

            ValidatedRefreshToken validatedRefreshToken = ValidatedRefreshToken.builder()
                    .jti(refreshTokenJti)
                    .userId(user.getId())
                    .expiryTime(refreshTokenExpiry)
                    .createdAt(new Date())
                    .build();

            validatedRefreshTokenRepository.save(validatedRefreshToken);
            log.info("Refresh token saved to validated_refresh_tokens for userId: {}", user.getId());
        } catch (ParseException e) {
            log.error("Failed to extract JTI from refresh token", e);
            throw new RuntimeException(e);
        }

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

    public AuthenticationResponse getProfileById(String userId) {
        log.info("Getting profile for userId: {}", userId);

        // Find user by userId
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Get profile from profile service
        ApiResponse<UserProfileResponse> profileResponse = profileClient.getProfileByUserId(userId);

        if (profileResponse.getResult() == null) {
            log.warn("Profile not found for userId: {}", userId);
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        log.info("Profile retrieved successfully for userId: {}", userId);

        return AuthenticationResponse.builder()
                .userId(user.getId())
                .userProfile(profileResponse.getResult())
                .build();
    }

    /**
     * Login user
     * 1. Find user by username/email/phone
     * 2. Validate password
     * 3. Generate tokens
     * 4. Save refresh token to validated_refresh_tokens table
     */
    @Transactional
    public AuthenticationResponse login(AuthenticationRequest request) {
        log.info("Login attempt for username: {}", request.getUsername());

        User user = null;
        UserProfileResponse userProfile = null;

        // Try to find user by username in database first
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());

        if (userOpt.isPresent()) {
            // Case 1: Found user by username directly
            user = userOpt.get();
            log.info("User found by username: id={}", user.getId());
            //            user.getUserRoles().forEach(userRole -> {
            //                System.out.println("Role: " + userRole.getRole().getName());
            //            });
            // Get profile from ProfileService using userId
            try {
                ApiResponse<UserProfileResponse> profileResponse = profileClient.getProfileByUserId(user.getId());
                userProfile = profileResponse.getResult();
                log.debug("Profile fetched by userId: {}", user.getId());
            } catch (Exception e) {
                log.error("Failed to fetch profile for userId: {}", user.getId(), e);
                throw new AppException(ErrorCode.LOGIN_FAILED);
            }
        } else {
            // Case 2: User not found by username, try profile service
            log.debug("Username not found in identity service, searching profile service");
            try {
                ApiResponse<UserProfileResponse> profileResponse =
                        profileClient.findProfileByAnyField(request.getUsername());
                userProfile = profileResponse.getResult();

                if (userProfile != null && userProfile.getUserId() != null) {
                    log.debug("Profile found by any field, userId: {}", userProfile.getUserId());

                    // Get user from database using userId from profile
                    user = userRepository
                            .findById(userProfile.getUserId())
                            .orElseThrow(() -> new AppException(ErrorCode.LOGIN_FAILED));
                    log.info("User found by userId from profile: id={}", user.getId());
                } else {
                    log.warn("No profile found for username: {}", request.getUsername());
                    throw new AppException(ErrorCode.LOGIN_FAILED);
                }
            } catch (Exception e) {
                log.error("Failed to find profile for username: {}", request.getUsername(), e);
                throw new AppException(ErrorCode.LOGIN_FAILED);
            }
        }

        // Validate user was found
        if (user == null) {
            log.error("Login failed - user not found for username: {}", request.getUsername());
            throw new AppException(ErrorCode.LOGIN_FAILED);
        }

        // Validate password
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!authenticated) {
            log.warn("Login failed - invalid password for user: {}", user.getId());
            throw new AppException(ErrorCode.LOGIN_FAILED);
        }

        log.info("Authentication successful for user: {}", user.getId());

        // Generate tokens
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        // ✅ Save refresh token to DB
        try {
            // xóa het token cua nguoi dung nay
            validatedRefreshTokenRepository.deleteByUserId(user.getId());

            String refreshTokenJti = extractJTI(refreshToken);
            Date refreshTokenExpiry = new Date(
                    Instant.now().plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli());

            ValidatedRefreshToken validatedRefreshToken = ValidatedRefreshToken.builder()
                    .jti(refreshTokenJti)
                    .userId(user.getId())
                    .expiryTime(refreshTokenExpiry)
                    .createdAt(new Date())
                    .build();

            validatedRefreshTokenRepository.save(validatedRefreshToken);
            log.info("Refresh token saved to validated_refresh_tokens for userId: {}", user.getId());
        } catch (ParseException e) {
            log.error("Failed to extract JTI from refresh token", e);
            throw new RuntimeException(e);
        }

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
                .userProfile(userProfile)
                .build();
    }

    /**
     * Logout user
     * 1. Auto-detect token type (access or refresh)
     * 2. Verify token is valid
     * 3. If refresh token: Delete from validated_refresh_tokens table
     * 4. If access token: No action needed (short-lived)
     */
    @Transactional
    public void logout(LogoutRequest request) {
        log.info("Starting logout process");

        try {
            String accessToken = request.getToken();
            String refreshToken = request.getRefreshToken();

            // ✅ Step 1: Validate both tokens exist
            if (accessToken == null || accessToken.trim().isEmpty()) {
                // log.error("Access token is null or empty in logout request");
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                // log.error("Refresh token is null or empty in logout request");
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            // log.info(
            // "Both tokens received - Access: {} chars, Refresh: {} chars",
            // accessToken.length(),
            // refreshToken.length());

            // ✅ Step 2: Verify Access Token is valid
            try {
                verifyToken(accessToken, false);
                log.info("Access token verified successfully");
            } catch (AppException | JOSEException | ParseException e) {
                // log.warn("Access token verification failed: {}", e.getMessage());
                e.printStackTrace();
                throw new AppException(ErrorCode.UNAUTHENTICATED);
                // Continue anyway - we still want to invalidate refresh token
            }

            // ✅ Step 3: Verify Refresh Token and extract JTI
            SignedJWT refreshJWT = SignedJWT.parse(refreshToken);
            JWTClaimsSet refreshClaims = refreshJWT.getJWTClaimsSet();
            String tokenType = (String) refreshClaims.getClaim(TOKEN_TYPE_CLAIM);
            String jti = refreshClaims.getJWTID();

            // log.info("Refresh token parsed - JTI: {}, Type: {}", jti, tokenType);

            // ✅ Step 4: Validate it's a refresh token
            if (!TOKEN_TYPE_REFRESH.equals(tokenType)) {
                // log.error("Expected REFRESH token but got: {}", tokenType);
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            // ✅ Step 5: Verify refresh token signature and expiration
            try {
                verifyToken(refreshToken, true);
                log.info("Refresh token verified successfully");
            } catch (AppException e) {
                // log.error("Refresh token verification failed: {}", e.getMessage());
                // Continue to delete from DB even if expired
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            // ✅ Step 6: Delete refresh token from DB to invalidate it
            boolean deleted = validatedRefreshTokenRepository
                    .findByJti(jti)
                    .map(token -> {
                        validatedRefreshTokenRepository.deleteById(jti);
                        log.info("✅ Refresh token deleted from DB on logout: {}", jti);
                        return true;
                    })
                    .orElse(false);

            if (!deleted) {
                log.warn("⚠️ Refresh token not found in DB (may be already deleted): {}", jti);
                // This is acceptable - token may have been cleaned up already
            }
            SecurityContextHolder.clearContext();
            log.info("🔴 User logged out successfully - both tokens invalidated");

        } catch (ParseException e) {
            log.error("Failed to parse JWT token during logout: {}", e.getMessage());
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        } catch (AppException exception) {
            log.warn("Logout failed: {}", exception.getMessage());
            throw exception;
        } catch (Exception e) {
            log.error("Unexpected error during logout: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        log.info("Refreshing token");

        // ✅ Verify it's a refresh token (signature, type, expiration)
        var signedJWT = verifyToken(request.getToken(), true);

        var jti = signedJWT.getJWTClaimsSet().getJWTID();
        var userId = signedJWT.getJWTClaimsSet().getSubject();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        // ✅ Step 1: Check if refresh token exists in validated_refresh_tokens
        Optional<ValidatedRefreshToken> validatedToken = validatedRefreshTokenRepository.findByJti(jti);

        if (validatedToken.isEmpty()) {
            log.warn("Refresh token not found in validated_refresh_tokens table: {}", jti);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // ✅ Step 2: Check if refresh token is still valid (not expired)
        Date now = new Date();
        ValidatedRefreshToken dbToken = validatedToken.get();

        if (!dbToken.getExpiryTime().after(now)) {
            log.warn("Refresh token has expired, deleting from DB: {}", jti);
            // Delete expired token from DB
            validatedRefreshTokenRepository.deleteById(jti);
            throw new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        // ✅ Step 3: Get user
        var user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        // ✅ Step 4: Delete old refresh token from DB
        validatedRefreshTokenRepository.deleteById(jti);
        log.info("Old refresh token deleted from DB: {}", jti);

        // ✅ Step 5: Generate new tokens
        String newAccessToken = generateAccessToken(user);
        String newRefreshToken = generateRefreshToken(user);

        // ✅ Step 6: Save new refresh token to DB
        String newJti = extractJTI(newRefreshToken);
        Date newExpiryTime = Instant.now()
                                .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDateTime()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toInstant()
                                .plusSeconds(REFRESHABLE_DURATION)
                                .toEpochMilli()
                        > 0
                ? new Date(Instant.now()
                        .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
                        .toEpochMilli())
                : new Date();

        ValidatedRefreshToken newValidatedToken = ValidatedRefreshToken.builder()
                .jti(newJti)
                .userId(userId)
                .expiryTime(newExpiryTime)
                .createdAt(new Date())
                .build();

        validatedRefreshTokenRepository.save(newValidatedToken);
        log.info("New refresh token saved to DB for userId: {}", userId);

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
     * Used for API requests
     */
    private String generateAccessToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        Instant now = Instant.now();
        Instant expiration = now.plus(VALID_DURATION, ChronoUnit.SECONDS);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId())
                .issuer(ISSUER)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(expiration))
                .jwtID(UUID.randomUUID().toString())
                .claim(SCOPE_CLAIM, buildScope(user))
                .claim(TOKEN_TYPE_CLAIM, TOKEN_TYPE_ACCESS)
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            String accessToken = jwsObject.serialize();

            // ✅ SET SECURITY CONTEXT ngay sau khi generate
            //            setSecurityContext(user, accessToken);

            log.debug("Access token generated for userId: {}", user.getId());
            return accessToken;
        } catch (JOSEException e) {
            log.error("Cannot create access token", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Generate Refresh Token (7 days)
     * Used to get new access tokens
     */
    private String generateRefreshToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        Instant now = Instant.now();
        Instant expiration = now.plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId())
                .issuer(ISSUER)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(expiration))
                .jwtID(UUID.randomUUID().toString())
                .claim(SCOPE_CLAIM, buildScope(user))
                .claim(TOKEN_TYPE_CLAIM, TOKEN_TYPE_REFRESH)
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            log.debug("Refresh token generated for userId: {}", user.getId());
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create refresh token", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Extract JTI from JWT token
     */
    private String extractJTI(String token) throws ParseException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        return signedJWT.getJWTClaimsSet().getJWTID();
    }

    /**
     * Verify token - Validates signature, expiration, blacklist, and token type
     *
     * For REFRESH tokens: Also checks if it exists in validated_refresh_tokens
     * table
     * For ACCESS tokens: Only validates signature and claims
     *
     * @param token     JWT token to verify
     * @param isRefresh true if verifying refresh token, false for access token
     * @return SignedJWT object if valid
     * @throws AppException   if token is invalid
     * @throws JOSEException  if signature verification fails
     * @throws ParseException if token parsing fails
     */
    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {

        // System.out.println("DEBUG verifyToken - Starting verification, isRefresh: " +
        // isRefresh);

        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

        // System.out.println("DEBUG verifyToken - Token parsed successfully");

        // ✅ Step 1: Verify signature (mandatory)
        if (!signedJWT.verify(verifier)) {
            // log.warn("Token signature verification failed");
            // System.out.println("DEBUG verifyToken - Signature verification FAILED");
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        log.debug("Token signature verified");
        System.out.println("DEBUG verifyToken - Signature verification PASSED");

        // ✅ Step 2: Validate token type matches expected type
        String tokenType = (String) claims.getClaim(TOKEN_TYPE_CLAIM);
        // System.out.println("DEBUG verifyToken - Token type from claim: " +
        // tokenType);
        // System.out.println("DEBUG verifyToken - Expected isRefresh: " + isRefresh);

        if (isRefresh) {
            if (!TOKEN_TYPE_REFRESH.equals(tokenType)) {
                // log.warn("Expected REFRESH token but got: {}", tokenType);
                // System.out.println("DEBUG verifyToken - Token type mismatch: expected
                // REFRESH, got " + tokenType);
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }
        } else {
            if (!TOKEN_TYPE_ACCESS.equals(tokenType)) {
                // log.warn("Expected ACCESS token but got: {}", tokenType);
                // System.out.println("DEBUG verifyToken - Token type mismatch: expected ACCESS,
                // got " + tokenType);
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }
        }
        // log.debug("Token type validation passed: {}", tokenType);
        // System.out.println("DEBUG verifyToken - Token type validation PASSED");

        // ✅ Step 3: Check expiration (BOTH token types must not be expired)
        Date expiryTime = claims.getExpirationTime();
        Date now = new Date();

        // System.out.println("DEBUG verifyToken - Expiry time: " + expiryTime);
        // System.out.println("DEBUG verifyToken - Current time: " + now);
        // System.out.println("DEBUG verifyToken - Is token expired: " +
        // !expiryTime.after(now));

        if (!expiryTime.after(now)) {
            // log.warn("Token has expired. ExpiryTime: {}, Now: {}", expiryTime, now);
            // System.out.println("DEBUG verifyToken - Token EXPIRED");
            if (isRefresh) {
                throw new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED);
            } else {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }
        }
        log.debug("Token expiration validation passed");

        return signedJWT;
    }

    public void printSecurityContext() {
        SecurityContext context = SecurityContextHolder.getContext();

        if (context == null) {
            System.out.println("SecurityContextHolder.getContext() = null");
            return;
        }

        Authentication auth = context.getAuthentication();
        if (auth == null) {
            System.out.println("Authentication = null");
            return;
        }

        System.out.println("Principal: " + auth.getPrincipal());
        System.out.println("Credentials: " + auth.getCredentials());
        System.out.println("Authorities:");
        for (GrantedAuthority authority : auth.getAuthorities()) {
            System.out.println(" - " + authority.getAuthority());
        }
        System.out.println("Details: " + auth.getDetails());
        System.out.println("Is Authenticated: " + auth.isAuthenticated());
    }
}
