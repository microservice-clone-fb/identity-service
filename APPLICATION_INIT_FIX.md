# ApplicationInitConfig Fix ✅

## Vấn đề
`ApplicationInitConfig` không chạy sau khi chuyển sang PostgreSQL vì có điều kiện:

```java
@ConditionalOnProperty(
    prefix = "spring",
    value = "datasource.driverClassName",
    havingValue = "com.mysql.cj.jdbc.Driver")  // ❌ Chỉ chạy với MySQL
```

## Giải pháp
Đã xóa `@ConditionalOnProperty` để bean luôn chạy bất kể database driver:

```java
@Bean
ApplicationRunner applicationRunner(
    UserRepository userRepository,
    RoleRepository roleRepository,
    // ...
) {
    // Initialization code
}
```

## Kết quả
✅ ApplicationInitConfig sẽ chạy khi application start
✅ Tự động tạo permissions, roles, và users
✅ Hoạt động với cả MySQL và PostgreSQL

## ApplicationInitConfig sẽ tạo:

### 1. **Permissions** (72 permissions)
Tạo tất cả combinations của:
- **Actions**: CREATE, UPDATE, GET, FIND, GETALL, DELETE, ACTIVE, INACTIVE
- **Resources**: USER, PROFILE, ROLE, PERMISSION, RELATIONSHIP, FILE, POST, MESSAGE, GROUP

Example:
- `CREATE_USER`
- `UPDATE_PROFILE`
- `DELETE_POST`
- etc.

### 2. **Roles** (2 roles)

#### ADMIN Role
- Có TẤT CẢ 72 permissions
- Full access to system

#### USER Role
- Chỉ có permissions với actions: CREATE, GET, UPDATE, FIND, ACTIVE, INACTIVE
- Không có: GETALL, DELETE
- Total: 54 permissions (6 actions × 9 resources)

### 3. **Users** (2 default users)

#### Admin User
```
Username: admin
Password: admin (encrypted)
Role: ADMIN
```

#### Normal User
```
Username: user
Password: user (encrypted)
Role: USER
```

## Cách kiểm tra

### 1. Xem logs khi application start:
```
Initializing application.....
Creating permissions...
Created 72 permissions
User will have 54 permissions
Creating USER role...
Created USER role with ID: xxx
Creating ADMIN role...
Created ADMIN role with ID: xxx
Creating users...
Application initialization completed .....
```

### 2. Query database:
```sql
-- Check permissions
SELECT COUNT(*) FROM permissions;  -- Should be 72

-- Check roles
SELECT * FROM roles;  -- Should have USER and ADMIN

-- Check role_permissions
SELECT r.name, COUNT(rp.permission_id) as permission_count
FROM roles r
LEFT JOIN roles_permissions rp ON r.id = rp.role_id
GROUP BY r.name;
-- ADMIN: 72 permissions
-- USER: 54 permissions

-- Check users
SELECT u.username, r.name as role
FROM users u
JOIN users_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id;
-- admin | ADMIN
-- user  | USER
```

## Testing

### Test Login với admin:
```bash
POST /identity/auth/login
{
  "username": "admin",
  "password": "admin"
}
```

### Test Login với user:
```bash
POST /identity/auth/login
{
  "username": "user",
  "password": "user"
}
```

## Quan trọng

⚠️ **Production Security:**
1. Đổi default passwords trong production
2. Hoặc disable user creation trong production
3. Set environment variable để control:

```java
@Bean
@ConditionalOnProperty(name = "app.init.enabled", havingValue = "true")
ApplicationRunner applicationRunner(...) {
    // ...
}
```

Then in `.env`:
```env
# Local development
APP_INIT_ENABLED=true

# Production - disable auto user creation
# APP_INIT_ENABLED=false
```

## Đã fix ✅
- ✅ Xóa condition MySQL-specific
- ✅ Bean sẽ chạy với PostgreSQL
- ✅ Tự động khởi tạo data khi start
- ✅ Clean imports

**ApplicationInitConfig đã sẵn sàng! 🚀**
