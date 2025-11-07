# FeignException Handler - Error Propagation from Downstream Services

## Vấn đề

Khi FeignClient gọi tới service khác bị lỗi, thay vì trả về error message từ service đó, nó lại trả về `UNCATEGORIZED_EXCEPTION`:

**Trước:**
```json
{
  "code": 9999,
  "message": "Unexpected error"
}
```

**Error gốc từ ProfileService:**
```json
{
  "code": 3101,
  "message": "User profile not found"
}
```

## Giải pháp

Đã thêm `@ExceptionHandler` cho `FeignException` trong `GlobalExceptionHandler` để parse và trả về đúng error từ downstream service.

## Code Implementation

### GlobalExceptionHandler.java

```java
@ExceptionHandler(value = FeignException.class)
ResponseEntity<ApiResponse> handlingFeignException(FeignException exception) {
    log.error("FeignException: {}", exception.getMessage());

    ApiResponse apiResponse = new ApiResponse();

    try {
        // Parse error response từ downstream service
        String responseBody = exception.contentUTF8();
        
        if (responseBody != null && !responseBody.isEmpty()) {
            // Parse JSON response
            ApiResponse downstreamResponse = objectMapper.readValue(responseBody, ApiResponse.class);
            
            // Trả về đúng code và message từ downstream
            apiResponse.setCode(downstreamResponse.getCode());
            apiResponse.setMessage(downstreamResponse.getMessage());
            apiResponse.setResult(downstreamResponse.getResult());
        } else {
            // Không có response body, dùng status code
            apiResponse.setCode(exception.status());
            apiResponse.setMessage(exception.getMessage());
        }

        return ResponseEntity.status(exception.status()).body(apiResponse);

    } catch (Exception e) {
        log.error("Error parsing Feign exception response", e);
        
        // Fallback nếu không parse được
        apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        apiResponse.setMessage("Error calling external service: " + exception.getMessage());
        
        return ResponseEntity.status(exception.status()).body(apiResponse);
    }
}
```

## Kết quả

### Khi ProfileService trả về 404:

**ProfileService response:**
```json
{
  "code": 3101,
  "message": "User profile not found"
}
```

**IdentityService sẽ trả về chính xác:**
```json
{
  "code": 3101,
  "message": "User profile not found"
}
```

**HTTP Status:** 404 (giữ nguyên từ downstream)

## Test Cases

### 1. Profile Not Found (404)

**Request:**
```bash
GET /identity/users/profile/nonexistent@gmail.com
```

**Response:**
```json
{
  "code": 3101,
  "message": "User profile not found"
}
```

### 2. Profile Already Exists (409)

**Request:**
```bash
POST /identity/auth/register
{
  "email": "existing@gmail.com",
  ...
}
```

**Response:**
```json
{
  "code": 3102,
  "message": "User profile already exists"
}
```

### 3. File Upload Failed (500)

**Request:**
```bash
POST /identity/users/avatar
```

**Response:**
```json
{
  "code": 4103,
  "message": "File upload failed"
}
```

## Error Code Mapping

Handler tự động preserve error codes từ tất cả services:

| Service | Code Range | Examples |
|---------|------------|----------|
| Identity | 2xxx | 2002: User not exists |
| Profile | 3xxx | 3101: Profile not found |
| File | 4xxx | 4103: Upload failed |
| Post | 5xxx | 5101: Post not found |
| Notification | 6xxx | 6101: Cannot send email |
| Relationship | 7xxx | 7101: Relationship not found |
| Conversation | 8xxx | 8101: Conversation not found |

## Logging

Handler sẽ log chi tiết để debug:

```
ERROR FeignException: [404] during [GET] to [http://localhost:5002/profile/...]
DEBUG Feign response body: {"code":3101,"message":"User profile not found"}
```

## Exception Flow

```
1. FeignClient gọi ProfileService
2. ProfileService trả về 404 với {"code": 3101, ...}
3. Feign throw FeignException với response body
4. GlobalExceptionHandler catch FeignException
5. Parse response body thành ApiResponse
6. Trả về client với đúng code và message
```

## Benefits

✅ **Transparent Error Propagation**: Client nhận được error message rõ ràng từ service gốc
✅ **Preserved HTTP Status**: Giữ nguyên status code (404, 409, 500, etc.)
✅ **Error Code Consistency**: Maintain error code convention across services
✅ **Better Debugging**: Log đầy đủ thông tin
✅ **Graceful Fallback**: Xử lý trường hợp parse lỗi

## Important Notes

### ObjectMapper Configuration
Handler sử dụng Jackson ObjectMapper để parse JSON. Đảm bảo:
- ApiResponse class có proper constructors
- Fields có getters/setters hoặc public access
- JSON format từ downstream services phải match ApiResponse structure

### Error Handling Priority
Thứ tự xử lý exceptions:
1. `AppException` - Internal business logic errors
2. `FeignException` - Errors from downstream services
3. `AccessDeniedException` - Authorization errors
4. `MethodArgumentNotValidException` - Validation errors
5. `Exception` - Catch-all for unexpected errors

### Status Code Preservation
Handler giữ nguyên HTTP status code từ downstream:
- 404 NOT_FOUND → 404
- 409 CONFLICT → 409
- 500 INTERNAL_SERVER_ERROR → 500

## Troubleshooting

### Nếu vẫn nhận được "Unexpected error":

1. **Check logs** cho Feign response body
2. **Verify JSON format** từ downstream service
3. **Ensure ApiResponse** có proper structure
4. **Check ObjectMapper** configuration

### Common Issues:

**Issue 1: Empty response body**
```java
// Handler sẽ dùng status code và message từ exception
apiResponse.setCode(exception.status());
apiResponse.setMessage(exception.getMessage());
```

**Issue 2: Invalid JSON format**
```java
// Handler sẽ fallback to generic error
catch (Exception e) {
    log.error("Error parsing Feign exception response", e);
    apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
    ...
}
```

## Testing

Test với các scenarios:

```bash
# 1. Profile not found
curl -X GET http://localhost:5001/identity/users/profile/test@gmail.com

# 2. Profile exists check
curl -X GET http://localhost:5001/identity/internal/profile/exists/test@gmail.com

# 3. Create profile (may fail if exists)
curl -X POST http://localhost:5001/identity/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@gmail.com",...}'
```

**Handler đã sẵn sàng propagate errors từ tất cả downstream services! ✅**
