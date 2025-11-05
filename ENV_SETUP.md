# Environment Configuration

## Local Development

1. Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```

2. Update values in `.env` for your local environment

3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

## Render Deployment

On Render, set the following Environment Variables in your service settings:

- `PORT` - Auto-set by Render (usually 10000)
- `DB_URL` - Your MySQL database URL
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password
- `KAFKA_BOOTSTRAP_SERVERS` - Kafka server address
- `JWT_SIGNER_KEY` - Your secure JWT signing key
- `JWT_VALID_DURATION` - Token validity duration (seconds)
- `JWT_REFRESHABLE_DURATION` - Refresh token duration (seconds)
- `PROFILE_SERVICE_URL` - Profile service endpoint
- `FILE_SERVICE_URL` - File service endpoint
- `LOG_LEVEL` - Logging level (INFO, DEBUG, etc.)

## Notes

- The `.env` file is gitignored for security
- Never commit sensitive credentials to the repository
- Use `.env.example` as a template for required variables
