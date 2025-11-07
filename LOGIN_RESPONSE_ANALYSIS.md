# Login Response Analysis - Frontend vs Backend

## 🔍 Frontend Response (Actual)

```json
{
    "userId": "9d76bf69-d277-43de-a8a4-5a393c30b7f0",
    "token": "eyJhbGci...",
    "userProfile": {
        "id": "68ff904ddd61010574c86b05",
        "userId": "9d76bf69-d277-43de-a8a4-5a393c30b7f0",
        "gender": "Nam",
        "firstName": "Địa",
        "lastName": "Kiếp",
        "dateOfBirth": "2004-07-23",
        "bio": "...",
        "contactInfo": { ... }
    }
}
```

## ✅ Backend Full Response (Expected)

```json
{
    "code": 200,
    "message": "Success",
    "result": {
        "userId": "9d76bf69-d277-43de-a8a4-5a393c30b7f0",
        "token": "eyJhbGci...",
        "expirationTime": 1762500926000,           // ❌ THIẾU ở frontend
        "refreshToken": "eyJhbGci...",             // ❌ THIẾU ở frontend
        "refreshTokenExpiration": 1763104926000,   // ❌ THIẾU ở frontend
        "authenticated": true,                      // ❌ THIẾU ở frontend
        "userProfile": { ... }
    }
}
```

## 🎯 Phân Tích

### ✅ Có trong Frontend Response:
- `userId` ✓
- `token` (access token) ✓
- `userProfile` ✓

### ❌ Thiếu trong Frontend Response:
- `expirationTime` - Thời gian hết hạn access token
- `refreshToken` - Token để refresh
- `refreshTokenExpiration` - Thời gian hết hạn refresh token
- `authenticated` - Status flag

## 🤔 Tại sao thiếu?

### Nguyên nhân 1: Frontend chỉ extract một số fields

Frontend code có thể như này:

```javascript
// ❌ Chỉ lấy một số fields
const { userId, token, userProfile } = response.data.result;

// ✅ Nên lấy tất cả
const authResponse = response.data.result;
// Hoặc
const { 
    userId, 
    token, 
    expirationTime,
    refreshToken, 
    refreshTokenExpiration,
    authenticated,
    userProfile 
} = response.data.result;
```

### Nguyên nhân 2: Backend response có vấn đề

Check xem service có return đủ fields không:

```java
return AuthenticationResponse.builder()
    .token(accessToken)
    .expirationTime(accessTokenExpiration)        // ⚠️ Check này
    .refreshToken(refreshToken)                   // ⚠️ Check này
    .refreshTokenExpiration(refreshTokenExpiration) // ⚠️ Check này
    .authenticated(true)                           // ⚠️ Check này
    .userId(user.getId())
    .userProfile(userProfile)
    .build();
```

### Nguyên nhân 3: Jackson Serialization Settings

Có thể backend config bỏ qua null values:

```java
// application.yaml hoặc @JsonInclude
spring:
  jackson:
    default-property-inclusion: non_null  // ⚠️ Bỏ qua null
```

## 🔧 Giải pháp

### Option 1: Fix Frontend (Recommended)

**Frontend nên lưu tất cả fields:**

```javascript
// Login service/API call
async login(credentials) {
    try {
        const response = await axios.post('/auth/login', credentials);
        const authData = response.data.result;
        
        // ✅ Save ALL fields
        localStorage.setItem('accessToken', authData.token);
        localStorage.setItem('tokenExpiration', authData.expirationTime);
        localStorage.setItem('refreshToken', authData.refreshToken);
        localStorage.setItem('refreshExpiration', authData.refreshTokenExpiration);
        localStorage.setItem('userId', authData.userId);
        localStorage.setItem('userProfile', JSON.stringify(authData.userProfile));
        
        return authData;
    } catch (error) {
        throw error;
    }
}
```

### Option 2: Verify Backend Response

**Check AuthenticationService returns all fields:**

```bash
# Test với curl để xem full response
curl -X POST http://localhost:5001/identity/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"0321456654","password":"your_password"}' \
  | jq '.'
```

**Expected full response:**
```json
{
  "code": 200,
  "message": "Success",
  "result": {
    "userId": "9d76bf69-d277-43de-a8a4-5a393c30b7f0",
    "token": "eyJhbGci...",
    "expirationTime": 1762500926000,
    "refreshToken": "eyJhbGci...",
    "refreshTokenExpiration": 1763104926000,
    "authenticated": true,
    "userProfile": {
      "id": "68ff904ddd61010574c86b05",
      "userId": "9d76bf69-d277-43de-a8a4-5a393c30b7f0",
      "gender": "Nam",
      "firstName": "Địa",
      "lastName": "Kiếp",
      "dateOfBirth": "2004-07-23",
      "bio": "...",
      "contactInfo": {
        "phoneNumber": "0321456654",
        "address": "123f",
        "socialMediaLinks": "[...]"
      }
    }
  }
}
```

## 💡 Tại sao cần các fields thiếu?

### 1. `expirationTime` (Access Token)
```javascript
// Check if token expired
const isTokenExpired = () => {
    const expiration = localStorage.getItem('tokenExpiration');
    return Date.now() > expiration;
};

// Auto refresh before expiration
if (isTokenExpired()) {
    await refreshAccessToken();
}
```

### 2. `refreshToken`
```javascript
// Refresh access token khi hết hạn
async refreshAccessToken() {
    const refreshToken = localStorage.getItem('refreshToken');
    const response = await axios.post('/auth/refresh', {
        token: refreshToken
    });
    // Update new tokens
    updateTokens(response.data.result);
}
```

### 3. `refreshTokenExpiration`
```javascript
// Check if need to re-login
const isRefreshTokenExpired = () => {
    const expiration = localStorage.getItem('refreshExpiration');
    return Date.now() > expiration;
};

if (isRefreshTokenExpired()) {
    // Force user to login again
    redirectToLogin();
}
```

### 4. `authenticated`
```javascript
// Simple status check
if (authResponse.authenticated) {
    console.log('Login successful');
    redirectToDashboard();
} else {
    console.log('Login failed');
    showError();
}
```

## 📊 Token Lifecycle

```
Login → Access Token (30 min) + Refresh Token (7 days)
  │
  ├─ Access Token Expires (after 30 min)
  │    └─ Use Refresh Token → New Access Token
  │
  └─ Refresh Token Expires (after 7 days)
       └─ Force Re-login
```

## 🎯 Recommendations

### Frontend Changes Needed:

1. **Store ALL tokens and timestamps**
```javascript
const authData = {
    userId: response.userId,
    token: response.token,
    expirationTime: response.expirationTime,
    refreshToken: response.refreshToken,
    refreshTokenExpiration: response.refreshTokenExpiration,
    userProfile: response.userProfile
};
```

2. **Implement token refresh logic**
```javascript
// Before API call
if (isTokenExpired()) {
    await refreshAccessToken();
}
```

3. **Handle refresh token expiration**
```javascript
// On refresh token expired
if (isRefreshTokenExpired()) {
    clearAuth();
    router.push('/login');
}
```

### Backend Verification:

1. **Check service returns all fields**
```bash
# Test full response
curl -X POST http://localhost:5001/identity/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"0321456654","password":"xxx"}' | jq '.result'
```

2. **Verify timestamps are generated**
```java
long accessTokenExpiration = 
    Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli();
long refreshTokenExpiration = 
    Instant.now().plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli();
```

## ✅ Complete Frontend Example

```javascript
// auth.service.js
class AuthService {
    async login(username, password) {
        try {
            const response = await axios.post('/auth/login', {
                username,
                password
            });
            
            const auth = response.data.result;
            
            // ✅ Store ALL authentication data
            this.setAuth({
                userId: auth.userId,
                accessToken: auth.token,
                accessTokenExpiration: auth.expirationTime,
                refreshToken: auth.refreshToken,
                refreshTokenExpiration: auth.refreshTokenExpiration,
                userProfile: auth.userProfile
            });
            
            return auth;
        } catch (error) {
            console.error('Login failed:', error);
            throw error;
        }
    }
    
    setAuth(authData) {
        localStorage.setItem('userId', authData.userId);
        localStorage.setItem('accessToken', authData.accessToken);
        localStorage.setItem('accessTokenExp', authData.accessTokenExpiration);
        localStorage.setItem('refreshToken', authData.refreshToken);
        localStorage.setItem('refreshTokenExp', authData.refreshTokenExpiration);
        localStorage.setItem('userProfile', JSON.stringify(authData.userProfile));
    }
    
    isAuthenticated() {
        const refreshExp = localStorage.getItem('refreshTokenExp');
        return refreshExp && Date.now() < parseInt(refreshExp);
    }
    
    async ensureValidToken() {
        const accessExp = localStorage.getItem('accessTokenExp');
        
        if (Date.now() > parseInt(accessExp)) {
            // Access token expired, refresh it
            await this.refreshAccessToken();
        }
    }
    
    async refreshAccessToken() {
        const refreshToken = localStorage.getItem('refreshToken');
        const response = await axios.post('/auth/refresh', {
            token: refreshToken
        });
        
        const auth = response.data.result;
        this.setAuth({
            userId: auth.userId,
            accessToken: auth.token,
            accessTokenExpiration: auth.expirationTime,
            refreshToken: auth.refreshToken,
            refreshTokenExpiration: auth.refreshTokenExpiration,
            userProfile: auth.userProfile
        });
    }
}
```

## 🎯 Summary

### Response hiện tại:
✅ **Đủ để login** nhưng **thiếu data quan trọng** cho token management

### Cần thêm:
- ✅ `expirationTime` - Để biết khi nào refresh
- ✅ `refreshToken` - Để refresh access token
- ✅ `refreshTokenExpiration` - Để biết khi nào force re-login
- ✅ `authenticated` - Status flag

### Action Items:
1. **Frontend**: Update code để lưu tất cả fields
2. **Backend**: Verify response có đầy đủ fields
3. **Frontend**: Implement token refresh logic
4. **Frontend**: Handle token expiration

**Response structure là đúng, chỉ cần frontend sử dụng đầy đủ! ✅**
