# Login Flow Refactoring - Complete Guide

## 🎯 Mục tiêu

Refactor hàm `login()` để:
1. **Case 1**: Tìm user bằng username → Lấy profile qua `getProfileByUserId()`
2. **Case 2**: Tìm profile bằng email/phone → Lấy user qua `findById(userId)`
3. **Luôn trả về** `userProfile` trong response

## 🔄 Logic Flow Mới

### Case 1: Login bằng Username
```
Input: { username: "admin", password: "admin" }

Step 1: findByUsername("admin") → Found User ✅
Step 2: getProfileByUserId(user.id) → Get UserProfile ✅
Step 3: Validate password → Success ✅
Step 4: Return { user, userProfile, tokens }
```

### Case 2: Login bằng Email/Phone
```
Input: { username: "test@gmail.com", password: "password" }

Step 1: findByUsername("test@gmail.com") → Not Found ❌
Step 2: findProfileByAnyField("test@gmail.com") → Found Profile ✅
Step 3: findById(profile.userId) → Found User ✅
Step 4: Validate password → Success ✅
Step 5: Return { user, userProfile, tokens }
```

## 📝 Code Implementation

### New Login Method

```java
public AuthenticationResponse login(AuthenticationRequest request) {
    log.info("Login attempt for username: {}", request.getUsername());

    User user = null;
    UserProfileResponse userProfile = null;

    // Try to find user by username in database first
    Optional<User> userOpt = userRepository.findByUsername(request.getUsername());

    if (userOpt.isPresent()) {
        // ========== CASE 1: Found by username ==========
        user = userOpt.get();
        log.info("User found by username: id={}, username={}", user.getId(), user.getUsername());

        // Get profile from ProfileService using userId
        try {
            ApiResponse<UserProfileResponse> profileResponse = 
                profileClient.getProfileByUserId(user.getId());
            userProfile = profileResponse.getResult();
            log.info("Profile fetched by userId: {}", userProfile != null ? userProfile.getUserId() : "null");
        } catch (Exception e) {
            log.warn("Failed to fetch profile for userId: {}", user.getId(), e);
        }
        
    } else {
        // ========== CASE 2: Search by email/phone ==========
        log.info("Username not found directly, searching by email/phone in profile service");
        
        try {
            ApiResponse<UserProfileResponse> profileResponse = 
                profileClient.findProfileByAnyField(request.getUsername());
            userProfile = profileResponse.getResult();

            if (userProfile != null && userProfile.getUserId() != null) {
                log.info("Profile found by any field, userId: {}", userProfile.getUserId());

                // Get user from database using userId from profile
                user = userRepository.findById(userProfile.getUserId())
                    .orElseThrow(() -> {
                        log.error("User not found in database for userId: {}", userProfile.getUserId());
                        return new AppException(ErrorCode.LOGIN_FAILED);
                    });

                log.info("User found by userId from profile: id={}, username={}", 
                    user.getId(), user.getUsername());
            } else {
                log.warn("No profile found for username: {}", request.getUsername());
            }
        } catch (Exception e) {
            log.error("Failed to find profile by any field for: {}", request.getUsername(), e);
        }
    }

    // Validate user was found
    if (user == null) {
        log.error("Login failed for username: {}", request.getUsername());
        throw new AppException(ErrorCode.LOGIN_FAILED);
    }

    // Validate password
    boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
    if (!authenticated) {
        log.warn("Invalid password for user: {}", user.getUsername());
        throw new AppException(ErrorCode.LOGIN_FAILED);
    }

    log.info("Authentication successful for user: {}", user.getUsername());

    // Generate tokens
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
        .userProfile(userProfile)  // ✅ Always included
        .build();
}
```

## 🔍 API Calls Breakdown

### Case 1: Login with Username

| Step | Method | Endpoint | Purpose |
|------|--------|----------|---------|
| 1 | `userRepository.findByUsername()` | Database | Find user by username |
| 2 | `profileClient.getProfileByUserId()` | GET `/internal/users/{userId}` | Get full profile |

### Case 2: Login with Email/Phone

| Step | Method | Endpoint | Purpose |
|------|--------|----------|---------|
| 1 | `userRepository.findByUsername()` | Database | Try to find by username (fail) |
| 2 | `profileClient.findProfileByAnyField()` | GET `/internal/users/by-any-field/{username}` | Find profile by email/phone |
| 3 | `userRepository.findById()` | Database | Get user by userId from profile |

## 📊 Flow Diagrams

### Case 1: Username Login Flow
```
┌─────────────────────────────────────────────────┐
│ Request: { username: "admin", password: "xxx" } │
└──────────────────┬──────────────────────────────┘
                   │
                   ▼
      ┌────────────────────────┐
      │ findByUsername("admin")│
      └──────────┬───────────────┘
                 │
            Found ✅
                 │
                 ▼
      ┌──────────────────────────┐
      │ getProfileByUserId(id)   │
      │ → UserProfileResponse    │
      └──────────┬───────────────┘
                 │
                 ▼
      ┌──────────────────────────┐
      │ Validate Password        │
      └──────────┬───────────────┘
                 │
            Valid ✅
                 │
                 ▼
      ┌──────────────────────────┐
      │ Generate Tokens          │
      │ Return with userProfile  │
      └──────────────────────────┘
```

### Case 2: Email/Phone Login Flow
```
┌───────────────────────────────────────────────────────┐
│ Request: { username: "test@gmail.com", password: "" } │
└──────────────────────┬────────────────────────────────┘
                       │
                       ▼
      ┌────────────────────────────────┐
      │ findByUsername("test@gmail...") │
      └──────────────┬─────────────────┘
                     │
                Not Found ❌
                     │
                     ▼
      ┌────────────────────────────────────┐
      │ findProfileByAnyField("test@...") │
      │ → UserProfileResponse              │
      └──────────────┬─────────────────────┘
                     │
                Found ✅
                     │
                     ▼
      ┌──────────────────────────────┐
      │ findById(profile.userId)     │
      │ → User                       │
      └──────────────┬───────────────┘
                     │
                Found ✅
                     │
                     ▼
      ┌──────────────────────────────┐
      │ Validate Password            │
      └──────────────┬───────────────┘
                     │
                Valid ✅
                     │
                     ▼
      ┌──────────────────────────────┐
      │ Generate Tokens              │
      │ Return with userProfile      │
      └──────────────────────────────┘
```

## 🧪 Testing

### Test Case 1: Login with Username

**Request:**
```bash
curl -X POST http://localhost:5001/identity/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin"
  }'
```

**Response:**
```json
{
  "code": 200,
  "message": "Success",
  "result": {
    "token": "eyJhbGciOiJIUzUxMi...",
    "expirationTime": 1699456789000,
    "refreshToken": "eyJhbGciOiJIUzUxMi...",
    "refreshTokenExpiration": 1700061589000,
    "authenticated": true,
    "userId": "uuid-123",
    "userProfile": {
      "userId": "uuid-123",
      "email": "admin@system.com",
      "firstName": "Admin",
      "lastName": "User",
      "phone": "0123456789"
    }
  }
}
```

**Logs:**
```
INFO  Login attempt for username: admin
INFO  User found by username: id=uuid-123, username=admin
INFO  Profile fetched by userId: uuid-123
INFO  Authentication successful for user: admin
```

### Test Case 2: Login with Email

**Request:**
```bash
curl -X POST http://localhost:5001/identity/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test@gmail.com",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "code": 200,
  "message": "Success",
  "result": {
    "token": "eyJhbGciOiJIUzUxMi...",
    "expirationTime": 1699456789000,
    "refreshToken": "eyJhbGciOiJIUzUxMi...",
    "refreshTokenExpiration": 1700061589000,
    "authenticated": true,
    "userId": "uuid-456",
    "userProfile": {
      "userId": "uuid-456",
      "email": "test@gmail.com",
      "firstName": "Test",
      "lastName": "User",
      "phone": "0987654321"
    }
  }
}
```

**Logs:**
```
INFO  Login attempt for username: test@gmail.com
INFO  Username not found directly, searching by email/phone in profile service
INFO  Profile found by any field, userId: uuid-456
INFO  User found by userId from profile: id=uuid-456, username=testuser
INFO  Authentication successful for user: testuser
```

### Test Case 3: Login Failed - User Not Found

**Request:**
```bash
curl -X POST http://localhost:5001/identity/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "nonexistent@gmail.com",
    "password": "password"
  }'
```

**Response:**
```json
{
  "code": 9997,
  "message": "Username or password is incorrect"
}
```

**Logs:**
```
INFO  Login attempt for username: nonexistent@gmail.com
INFO  Username not found directly, searching by email/phone in profile service
WARN  No profile found for username: nonexistent@gmail.com
ERROR Login failed for username: nonexistent@gmail.com
```

### Test Case 4: Login Failed - Wrong Password

**Request:**
```bash
curl -X POST http://localhost:5001/identity/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "wrongpassword"
  }'
```

**Response:**
```json
{
  "code": 9997,
  "message": "Username or password is incorrect"
}
```

**Logs:**
```
INFO  Login attempt for username: admin
INFO  User found by username: id=uuid-123, username=admin
INFO  Profile fetched by userId: uuid-123
WARN  Invalid password for user: admin
```

## 🎯 Key Benefits

### ✅ **Consistency**
- Luôn trả về `userProfile` trong response
- Cùng DTO cho cả 2 cases

### ✅ **Better Error Handling**
- Try-catch riêng cho từng API call
- Detailed logging cho debugging
- Graceful degradation nếu ProfileService down

### ✅ **Clear Logic**
- Tách biệt 2 cases rõ ràng
- Dễ đọc và maintain
- Không dùng `.or()` phức tạp

### ✅ **Performance**
- Chỉ gọi API khi cần
- Cache có thể thêm sau

### ✅ **Security**
- Validate password sau khi có user
- Log chi tiết để audit
- Không expose sensitive info trong error

## 🔒 Error Handling

### Scenario 1: ProfileService Down
```java
try {
    profileResponse = profileClient.getProfileByUserId(user.getId());
    userProfile = profileResponse.getResult();
} catch (Exception e) {
    log.warn("Failed to fetch profile for userId: {}", user.getId(), e);
    // userProfile = null, but login still succeeds
}
```

**Result:** User có thể login nhưng không có profile info

### Scenario 2: User Exists in Profile but Not in Identity DB
```java
user = userRepository.findById(userProfile.getUserId())
    .orElseThrow(() -> {
        log.error("User not found in database for userId: {}", userProfile.getUserId());
        return new AppException(ErrorCode.LOGIN_FAILED);
    });
```

**Result:** Login failed với error message

### Scenario 3: Profile Not Found
```java
if (userProfile != null && userProfile.getUserId() != null) {
    // Process...
} else {
    log.warn("No profile found for username: {}", request.getUsername());
}
```

**Result:** `user` remains null → Login failed

## 📋 Requirements Checklist

- ✅ Tìm bằng username → Get profile qua `getProfileByUserId()`
- ✅ Tìm bằng email/phone → Get user qua `findById(userId)`
- ✅ Luôn gán `userProfile` vào response
- ✅ Comprehensive logging
- ✅ Error handling cho mọi case
- ✅ Maintain existing token generation logic
- ✅ Same response structure

## 🚀 Deployment Notes

### Environment Variables
```env
# Profile Service URL
APP_SERVICES_PROFILE=http://localhost:5002/profile

# Token durations
JWT_VALID_DURATION=1800
JWT_REFRESHABLE_DURATION=604800
```

### Dependencies
- ProfileService phải running
- Database connection
- FeignClient configuration

### Monitoring
- Monitor ProfileService availability
- Track login success/failure rate
- Alert on high failure rate

**Login flow refactoring complete! ✅**
