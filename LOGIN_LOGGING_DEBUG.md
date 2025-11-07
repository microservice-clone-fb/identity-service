# Login Flow Logging - Debug Guide

## Vấn đề

Log `log.info("Profile response at login: {}", profile.toString());` không xuất hiện dù login thành công.

## Nguyên nhân

### Code Flow:
```java
public AuthenticationResponse login(AuthenticationRequest request) {
    var userOpt = userRepository.findByUsername(request.getUsername())
        .or(() -> {
            // ❌ Block này CHỈ chạy khi findByUsername() trả về empty
            var profile = profileClient.findProfileByAnyField(request.getUsername());
            log.info("Profile response at login: {}", profile.toString());
            ...
        });
}
```

### Khi nào log KHÔNG hiện:
- ✅ Login với **username trực tiếp** (`admin`, `user`, etc.)
- → `findByUsername()` tìm thấy ngay
- → Không cần vào `.or()` block
- → **Log không chạy**

### Khi nào log CÓ hiện:
- ✅ Login với **email** hoặc **phone** (`test@gmail.com`)
- → `findByUsername()` không tìm thấy
- → Vào `.or()` block
- → Gọi ProfileService
- → **Log chạy**

## Giải pháp

### Đã thêm comprehensive logging:

```java
public AuthenticationResponse login(AuthenticationRequest request) {
    // 1️⃣ Log mọi login attempt
    log.info("Login attempt for username: {}", request.getUsername());
    
    var userOpt = userRepository.findByUsername(request.getUsername())
        .or(() -> {
            // 2️⃣ Log khi search by email/phone
            log.info("Username not found directly, searching by email/phone in profile service");
            
            var profile = profileClient.findProfileByAnyField(request.getUsername());
            
            // 3️⃣ Log profile response
            log.info("Profile response at login: {}", profile.toString());
            
            var userId = profile.getResult() != null ? profile.getResult().getUserId() : null;

            if (userId == null) {
                // 4️⃣ Log khi không tìm thấy userId
                log.warn("No userId found in profile response");
                return Optional.empty();
            }
            
            // 5️⃣ Log khi tìm thấy userId
            log.info("Found userId from profile: {}", userId);
            return userRepository.findById(userId);
        });

    User user;
    if (userOpt.isPresent()) {
        user = userOpt.get();
        // 6️⃣ Log khi tìm thấy user
        log.info("User found: id={}, username={}", user.getId(), user.getUsername());
    } else {
        // 7️⃣ Log khi login failed
        log.error("Login failed for username: {}", request.getUsername());
        throw new AppException(ErrorCode.LOGIN_FAILED);
    }
}
```

## Log Output Examples

### Scenario 1: Login với username trực tiếp
```
INFO  Login attempt for username: admin
INFO  User found: id=uuid-123, username=admin
```

### Scenario 2: Login với email
```
INFO  Login attempt for username: test@gmail.com
INFO  Username not found directly, searching by email/phone in profile service
INFO  Profile response at login: ApiResponse{code=200, message='Success', result=UserProfileResponse{...}}
INFO  Found userId from profile: uuid-456
INFO  User found: id=uuid-456, username=testuser
```

### Scenario 3: Login failed
```
INFO  Login attempt for username: nonexistent@gmail.com
INFO  Username not found directly, searching by email/phone in profile service
INFO  Profile response at login: ApiResponse{code=3101, message='User profile not found', result=null}
WARN  No userId found in profile response
ERROR Login failed for username: nonexistent@gmail.com
```

## Login Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│ LOGIN REQUEST                                               │
│ { username: "test@gmail.com", password: "xxx" }            │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
         ┌─────────────────────────────┐
         │ findByUsername(test@...)    │
         └──────────┬──────────────────┘
                    │
            ┌───────┴────────┐
            │                │
        Found ❌          Not Found ✅
            │                │
            │                ▼
            │    ┌──────────────────────────────┐
            │    │ ProfileClient.findByAnyField │
            │    └────────────┬─────────────────┘
            │                 │
            │         ┌───────┴────────┐
            │         │                │
            │     Found ✅         Not Found ❌
            │         │                │
            │    ┌────▼─────┐         │
            │    │ Get User │         │
            │    │ by userId│         │
            │    └────┬─────┘         │
            │         │                │
            └─────────┴────────────────┘
                      │
                      ▼
            ┌──────────────────┐
            │ Validate Password│
            └─────────┬────────┘
                      │
              ┌───────┴────────┐
              │                │
          Valid ✅         Invalid ❌
              │                │
        ┌─────▼──────┐   ┌────▼──────┐
        │ Get Profile│   │   ERROR   │
        │ Generate   │   │ LOGIN_    │
        │ Tokens     │   │ FAILED    │
        └────────────┘   └───────────┘
```

## Testing

### Test 1: Login với username
```bash
curl -X POST http://localhost:5001/identity/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

**Expected logs:**
```
INFO  Login attempt for username: admin
INFO  User found: id=xxx, username=admin
```

### Test 2: Login với email
```bash
curl -X POST http://localhost:5001/identity/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test@gmail.com","password":"password"}'
```

**Expected logs:**
```
INFO  Login attempt for username: test@gmail.com
INFO  Username not found directly, searching by email/phone in profile service
INFO  Profile response at login: ApiResponse{...}
INFO  Found userId from profile: xxx
INFO  User found: id=xxx, username=testuser
```

### Test 3: Login failed
```bash
curl -X POST http://localhost:5001/identity/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"wrong@email.com","password":"wrong"}'
```

**Expected logs:**
```
INFO  Login attempt for username: wrong@email.com
INFO  Username not found directly, searching by email/phone in profile service
INFO  Profile response at login: ApiResponse{code=3101,...}
WARN  No userId found in profile response
ERROR Login failed for username: wrong@email.com
```

## Debugging Tips

### 1. Check log level
In `application.yaml`:
```yaml
logging:
  level:
    com.tamm.identity: DEBUG  # or INFO
```

### 2. Check ProfileService is running
```bash
curl http://localhost:5002/profile/internal/users/by-any-field/test@gmail.com
```

### 3. Check database
```sql
SELECT username, id FROM users WHERE username = 'admin';
```

### 4. Enable Feign logging
```yaml
logging:
  level:
    com.tamm.identity.repository.httpclient: DEBUG
```

## Important Notes

### Log Levels Used:
- `INFO`: Normal flow, successful operations
- `WARN`: Unexpected but recoverable situations
- `ERROR`: Failed operations that throw exceptions

### Performance Consideration:
Logging với `.toString()` có thể tốn performance nếu object lớn. Consider:
```java
// Better for production
if (log.isDebugEnabled()) {
    log.debug("Profile response: {}", profile);
}
```

### Security Consideration:
**⚠️ KHÔNG log password!**
```java
// ❌ BAD
log.info("Login request: {}", request.toString());

// ✅ GOOD
log.info("Login attempt for username: {}", request.getUsername());
```

## Summary

✅ **Đã thêm logging ở mọi bước** trong login flow
✅ **Giờ sẽ thấy logs** cho tất cả scenarios
✅ **Dễ debug** khi có vấn đề
✅ **Track được** user login với username hay email

**Login logging đã complete! 🎯**
