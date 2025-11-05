# Migration from MySQL to PostgreSQL

## Changes Made

### 1. Entity Classes
- **User.java**: Removed MySQL-specific `COLLATE utf8mb4_unicode_ci`, added `@Table` annotation
- **Role.java**: Added explicit column names and `@Table` annotation
- **Permission.java**: Added explicit column names and `@Table` annotation
- **InvalidatedToken.java**: Added explicit column names and `@Table` annotation
- **AuditableBaseEntity.java**: Removed `@Lob` annotation (not needed for TEXT in PostgreSQL)
- **UserRole.java**: Already compatible
- **RolePermission.java**: Already compatible

### 2. Dependencies (pom.xml)
- Added: `org.postgresql:postgresql` driver
- Commented out: `com.mysql:mysql-connector-j` (can be removed later)

### 3. Configuration (application.yaml)
- Changed `driverClassName` from `com.mysql.cj.jdbc.Driver` to `org.postgresql.Driver`
- Changed `dialect` from `MySQLDialect` to `PostgreSQLDialect`

### 4. Environment Variables
- Updated `.env` with PostgreSQL connection string (port 5432)
- Updated `.env.example` with PostgreSQL example

## Database Setup

### Local Development

1. Install PostgreSQL:
   ```bash
   # Windows (using Chocolatey)
   choco install postgresql
   
   # Or download from: https://www.postgresql.org/download/
   ```

2. Create database:
   ```sql
   CREATE DATABASE clone_fb_identity;
   ```

3. Update `.env` file with your PostgreSQL credentials:
   ```env
   DB_URL=jdbc:postgresql://localhost:5432/clone_fb_identity
   DB_USERNAME=postgres
   DB_PASSWORD=your_password
   ```

### Render Deployment

Set these environment variables in Render:

```env
PORT=10000
DB_URL=jdbc:postgresql://your-db-host:5432/database_name?sslmode=require
DB_USERNAME=your_username
DB_PASSWORD=your_password
JPA_DDL_AUTO=update
JPA_SHOW_SQL=false
```

## Data Migration (if needed)

If you need to migrate existing data from MySQL to PostgreSQL:

### Option 1: Using pgloader
```bash
pgloader mysql://user:pass@localhost/clone_fb_identity postgresql://user:pass@localhost/clone_fb_identity
```

### Option 2: Manual Export/Import
```bash
# Export from MySQL
mysqldump -u root -p clone_fb_identity > backup.sql

# Convert and import to PostgreSQL (may need manual adjustments)
psql -U postgres -d clone_fb_identity < backup_converted.sql
```

### Option 3: Fresh Start
If data is not critical:
1. Drop existing PostgreSQL database
2. Recreate it
3. Set `JPA_DDL_AUTO=create` for first run
4. Change back to `JPA_DDL_AUTO=update` after first run

## Key Differences to Note

### Data Types
- MySQL `TEXT` → PostgreSQL `TEXT` (both unlimited)
- MySQL `VARCHAR(255) COLLATE utf8mb4_unicode_ci` → PostgreSQL `VARCHAR(255)` (UTF-8 by default)
- UUID generation works the same in both

### SQL Syntax
- MySQL: Uses backticks `` `table` ``
- PostgreSQL: Uses double quotes `"table"` or no quotes

### Case Sensitivity
- PostgreSQL is case-sensitive for unquoted identifiers
- All entity column names are lowercase with underscores (snake_case)

## Testing

After migration, test these features:
- [ ] User registration and login
- [ ] Role and permission assignment
- [ ] JWT token generation and validation
- [ ] Token invalidation
- [ ] Audit logging
- [ ] Kafka event publishing

## Rollback Plan

If you need to rollback to MySQL:
1. Uncomment MySQL driver in `pom.xml`
2. Comment out PostgreSQL driver
3. Change `application.yaml` back to MySQL settings
4. Update `.env` with MySQL connection string
5. Run `./mvnw clean install`

## Performance Considerations

PostgreSQL advantages:
- Better performance for complex queries
- More robust transaction handling
- Better JSON support (if needed later)
- More advanced indexing options
- Better for read-heavy workloads

## Next Steps

1. Test locally with PostgreSQL
2. Run all unit tests
3. Verify all API endpoints
4. Update CI/CD pipeline if needed
5. Deploy to staging first
6. Monitor performance
7. Deploy to production
