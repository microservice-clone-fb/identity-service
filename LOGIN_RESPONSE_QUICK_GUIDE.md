# Login Response - Quick Guide

## 📦 Full Response Structure

```json
{
  "code": 200,
  "message": "Success",
  "result": {
    // ===== User Identity =====
    "userId": "9d76bf69-d277-43de-a8a4-5a393c30b7f0",
    
    // ===== Access Token (30 min) =====
    "token": "eyJhbGci...",                    // JWT Access Token
    "expirationTime": 1762500926000,          // Unix timestamp (milliseconds)
    
    // ===== Refresh Token (7 days) =====
    "refreshToken": "eyJhbGci...",            // JWT Refresh Token
    "refreshTokenExpiration": 1763104926000,  // Unix timestamp (milliseconds)
    
    // ===== Status =====
    "authenticated": true,
    
    // ===== User Profile =====
    "userProfile": {
      "id": "68ff904ddd61010574c86b05",      // MongoDB _id
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

## ✅ What You Have (Frontend)
```
✓ userId
✓ token (access token)
✓ userProfile
```

## ❌ What You're Missing (Frontend)
```
✗ expirationTime          → Token expires when?
✗ refreshToken            → How to refresh?
✗ refreshTokenExpiration  → When to re-login?
✗ authenticated          → Status flag
```

## 🔥 Quick Fix Frontend

```javascript
// ✅ Save ALL fields from response
const saveAuth = (response) => {
    const auth = response.data.result;
    
    localStorage.setItem('accessToken', auth.token);
    localStorage.setItem('accessExpire', auth.expirationTime);
    localStorage.setItem('refreshToken', auth.refreshToken);
    localStorage.setItem('refreshExpire', auth.refreshTokenExpiration);
    localStorage.setItem('userId', auth.userId);
    localStorage.setItem('userProfile', JSON.stringify(auth.userProfile));
};

// ✅ Check if need refresh
const needsRefresh = () => {
    const expire = localStorage.getItem('accessExpire');
    return Date.now() > parseInt(expire);
};

// ✅ Check if need re-login
const needsLogin = () => {
    const expire = localStorage.getItem('refreshExpire');
    return Date.now() > parseInt(expire);
};
```

## 🎯 Token Lifecycle

```
30 min: Access Token expires  → Use Refresh Token
7 days: Refresh Token expires → Force Re-login
```

## 🧪 Test Backend Response

```bash
# Get full response
curl -X POST http://localhost:5001/identity/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"0321456654","password":"xxx"}' \
  | jq '.result | keys'

# Should show:
# [
#   "authenticated",
#   "expirationTime",
#   "refreshToken",
#   "refreshTokenExpiration",
#   "token",
#   "userId",
#   "userProfile"
# ]
```

## 💡 Why You Need Missing Fields

| Field | Why Need It | Example Use |
|-------|-------------|-------------|
| `expirationTime` | Know when to refresh | Auto-refresh before expiry |
| `refreshToken` | Refresh access token | Get new token without re-login |
| `refreshTokenExpiration` | Know when to re-login | Force login after 7 days |
| `authenticated` | Quick status check | if (authenticated) { ... } |

## 🚨 Common Issues

### Issue 1: Token expires → 401 Error
**Solution:** Use `refreshToken` to get new access token

### Issue 2: Silent logout after 30 min
**Solution:** Store `expirationTime` and auto-refresh

### Issue 3: User forced to login frequently
**Solution:** Use `refreshToken` (valid 7 days)

## 📱 Frontend Implementation Priority

1. **Critical:** Store `refreshToken` & `refreshTokenExpiration`
2. **Important:** Store `expirationTime`
3. **Good to have:** Store `authenticated`

**Fix frontend to use all fields! 🚀**
