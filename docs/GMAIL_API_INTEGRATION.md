# Gmail API Email Sender Implementation

**Date:** 2025-11-24  
**Status:** Ready for Implementation  
**References:** [Google Workspace Gmail API Quickstart](https://developers.google.com/workspace/gmail/api/quickstart/java)

---

## Overview

This implementation adds support for sending emails via **Google Workspace Gmail API** as an alternative to the default Spring Mail SMTP backend. The `EmailService` automatically routes emails to the Gmail API if enabled, otherwise falls back to Spring Mail.

### Benefits

- ✅ **No SMTP Configuration** - Uses OAuth2 authentication instead of SMTP credentials
- ✅ **Higher Deliverability** - Gmail's infrastructure handles delivery
- ✅ **Advanced Features** - Access to Gmail labels, drafts, and other Gmail features in future versions
- ✅ **Flexible** - Easy switching between providers without code changes
- ✅ **Backward Compatible** - Spring Mail remains default if Gmail API is disabled

---

## Architecture

```
┌─────────────────────────────────────────────┐
│         EmailService (High-level)           │
│  - Abstracts email sending logic             │
│  - Routes to Gmail API or Spring Mail        │
│  - Builds email body HTML                    │
└──────────────┬──────────────────────────────┘
               │
               ├─────────────────────┬──────────────────────┐
               │                     │                      │
         ┌─────▼──────┐      ┌──────▼────────┐      ┌─────▼──────┐
         │ Gmail API   │      │ Spring Mail   │      │ [Future]   │
         │ Sender      │      │ (SMTP)        │      │ Other      │
         │             │      │               │      │ Providers  │
         │ ✅ Enabled  │      │ Default       │      │            │
         │ Uses OAuth2 │      │ Uses SMTP     │      │            │
         └────────────┘      └───────────────┘      └────────────┘
```

---

## Implementation Details

### 1. New Dependencies

Added to `build.gradle.kts`:

```gradle
implementation("com.google.auth:google-auth-library-oauth2-http:1.23.0")
implementation("com.google.apis:google-api-services-gmail:v1-rev20240603-2.0.0")
```

### 2. New Classes

#### `GmailEmailSender.kt`

Handles Gmail API communication:
- Initializes Gmail service with OAuth2 credentials
- Creates MIME messages and encodes them for Gmail API
- Sends emails via Gmail API
- Provides health check via `isEnabled()` method

**Key Methods:**
- `send(to: String, subject: String, htmlBody: String)` - Send email
- `isEnabled(): Boolean` - Check if Gmail API is ready

#### `IEmailSender.kt` (Interface)

Abstract interface for email providers:
- Allows future implementations of other email services
- Standardizes email sending behavior

### 3. Updated Classes

#### `EmailService.kt`

Enhanced to support multiple backends:
- Checks if Gmail API is enabled
- Routes to Gmail API if available
- Falls back to Spring Mail if Gmail API is disabled
- Maintains same public API for backward compatibility

---

## Configuration

### Environment Variables

Add these environment variables to enable Gmail API:

```bash
# Gmail API Configuration
GMAIL_ENABLED=true
GMAIL_CREDENTIALS_PATH=/path/to/credentials.json
GMAIL_USER_EMAIL=your-email@gmail.com
```

### Properties File

In `application.yml` or `application.properties`:

```yaml
gmail:
  enabled: ${GMAIL_ENABLED:false}
  credentials-path: ${GMAIL_CREDENTIALS_PATH:}
  user-email: ${GMAIL_USER_EMAIL:me}
```

### Spring Mail (Fallback)

Default configuration remains:

```yaml
spring:
  mail:
    host: ${SPRING_MAIL_HOST:smtp.mailtrap.io}
    port: ${SPRING_MAIL_PORT:2525}
    username: ${SPRING_MAIL_USERNAME:}
    password: ${SPRING_MAIL_PASSWORD:}
```

---

## Setup Instructions

### Step 1: Create Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project: **ThesisFlow**
3. Enable the **Gmail API**:
   - Go to **APIs & Services** → **Library**
   - Search for "Gmail API"
   - Click **Enable**

### Step 2: Create Service Account (Recommended for Backend)

For server-to-server communication without user interaction:

1. Go to **APIs & Services** → **Credentials**
2. Click **Create Credentials** → **Service Account**
3. Fill in:
   - Service account name: `thesis-flow-mailer`
   - Service account ID: (auto-generated)
4. Grant role: **Editor** (for testing; use more restrictive role in production)
5. Create a new JSON key:
   - Click on the service account
   - Go to **Keys** tab
   - Click **Add Key** → **Create new key** → **JSON**
   - Download the JSON file

### Step 3: Grant Gmail API Permissions

#### For Service Account:

If your Gmail account uses Google Workspace (or domain):
1. Go to [Google Workspace Admin Console](https://admin.google.com/)
2. Go to **Security** → **API Controls** → **Domain-wide Delegation**
3. Add the service account client ID with OAuth scopes:
   - Scopes: `https://www.googleapis.com/auth/gmail.send`

#### For User Account (OAuth 2.0):

Alternative: Use OAuth 2.0 flow for user authentication:
1. Go to **APIs & Services** → **Credentials**
2. Click **Create Credentials** → **OAuth 2.0 Client ID**
3. Choose **Desktop Application**
4. Download credentials as JSON
5. Follow Google's OAuth flow to authorize the app

### Step 4: Configure Application

**Option A: Using Service Account JSON**

1. Place `credentials.json` in a secure location:
   ```bash
   cp /path/to/downloaded/credentials.json /var/lib/thesis-flow/gmail-credentials.json
   chmod 600 /var/lib/thesis-flow/gmail-credentials.json
   ```

2. Set environment variables:
   ```bash
   export GMAIL_ENABLED=true
   export GMAIL_CREDENTIALS_PATH=/var/lib/thesis-flow/gmail-credentials.json
   export GMAIL_USER_EMAIL=your-email@gmail.com
   ```

3. Start the application:
   ```bash
   ./gradlew bootRun
   ```

**Option B: Docker/Deployment**

Create a `.env` file:
```bash
GMAIL_ENABLED=true
GMAIL_CREDENTIALS_PATH=/app/credentials/gmail-credentials.json
GMAIL_USER_EMAIL=your-email@gmail.com
```

In `Dockerfile`, mount credentials:
```dockerfile
COPY credentials.json /app/credentials/gmail-credentials.json
```

---

## Usage

### In Code

No code changes needed! The existing `EmailService` automatically uses Gmail API if enabled:

```kotlin
// This works with both Spring Mail and Gmail API
emailService.sendProfessorLoginLink(professor, loginLink)
```

### Switching Providers

**Enable Gmail API:**
```bash
export GMAIL_ENABLED=true
export GMAIL_CREDENTIALS_PATH=/path/to/credentials.json
```

**Use Spring Mail (default):**
```bash
export GMAIL_ENABLED=false
export SPRING_MAIL_HOST=smtp.mailtrap.io
export SPRING_MAIL_USERNAME=your-username
export SPRING_MAIL_PASSWORD=your-password
```

---

## Error Handling

### Common Issues

#### 1. Credentials File Not Found

**Error:** `FileNotFoundException: /path/to/credentials.json`

**Solution:**
- Verify path is correct
- Check file permissions: `chmod 600 credentials.json`
- Use absolute path in configuration

#### 2. Invalid Credentials

**Error:** `invalid_grant` or `Authorization failed`

**Solution:**
- Verify service account has Gmail API access
- Check credentials JSON is valid
- Regenerate credentials if expired

#### 3. Gmail API Not Enabled

**Error:** `The Gmail API has not been used in project...`

**Solution:**
- Go to Google Cloud Console
- Enable Gmail API for the project
- Wait 1-2 minutes for activation

#### 4. Permission Denied

**Error:** `403 Forbidden` or insufficient permissions

**Solution:**
- For service account: Add Gmail scope in domain-wide delegation
- For OAuth2: Re-authorize with correct scopes
- Check sender email matches authorized account

### Fallback Behavior

If Gmail API fails to initialize:
- Service still boots successfully
- `gmailSender.isEnabled()` returns `false`
- Emails automatically route to Spring Mail
- Check logs for initialization errors:
  ```
  WARN: Gmail API is disabled. Configure gmail.enabled=true and provide credentials-path.
  ```

---

## Testing

### Unit Tests

```kotlin
@SpringBootTest
class EmailServiceTest {
    @MockBean
    lateinit var gmailSender: GmailEmailSender

    @Autowired
    lateinit var emailService: EmailService

    @Test
    fun `should send email via Gmail API when enabled`() {
        // Arrange
        every { gmailSender.isEnabled() } returns true
        every { gmailSender.send(any(), any(), any()) } just runs

        val professor = mockk<Professor> {
            every { email } returns "prof@example.com"
            every { person.name } returns "Dr. Smith"
        }

        // Act
        emailService.sendProfessorLoginLink(professor, "https://example.com/verify?token=abc123")

        // Assert
        verify { gmailSender.send("prof@example.com", any(), any()) }
    }

    @Test
    fun `should send email via Spring Mail when Gmail API disabled`() {
        // Arrange
        every { gmailSender.isEnabled() } returns false

        val professor = mockk<Professor> {
            every { email } returns "prof@example.com"
            every { person.name } returns "Dr. Smith"
        }

        // Act
        emailService.sendProfessorLoginLink(professor, "https://example.com/verify?token=abc123")

        // Assert
        // Spring Mail sends the email
    }
}
```

### Integration Test

```bash
# Set credentials
export GMAIL_ENABLED=true
export GMAIL_CREDENTIALS_PATH=/path/to/credentials.json
export GMAIL_USER_EMAIL=your-email@gmail.com

# Run tests
./gradlew test -Dtest=EmailServiceIntegrationTest
```

### Manual Testing

1. **Start the application:**
   ```bash
   ./gradlew bootRun
   ```

2. **Request professor login link:**
   ```bash
   curl -X POST http://localhost:8080/auth/professor/request-login-link \
     -H "Content-Type: application/json" \
     -d '{"email":"professor@example.com"}'
   ```

3. **Check Gmail inbox** for the email

4. **Check logs** for confirmation:
   ```
   INFO: Using Gmail API to send login link email to: professor@example.com
   ```

---

## Production Deployment

### Security Considerations

1. **Credentials File**
   - Store outside Git repository
   - Use restricted file permissions: `chmod 600`
   - Rotate credentials periodically
   - Use secret management system (e.g., AWS Secrets Manager, Vault)

2. **OAuth Scopes**
   - Use minimal required scopes: `https://www.googleapis.com/auth/gmail.send`
   - Avoid overly permissive scopes

3. **Service Account**
   - Use service account for server-to-server communication
   - Enable MFA for Google Cloud account
   - Monitor API usage in Cloud Console

### Docker Deployment

```dockerfile
FROM openjdk:21-slim

# Copy application JAR
COPY build/libs/thesis-flow-0.0.1-SNAPSHOT.jar app.jar

# Copy credentials (mounted separately for security)
# COPY credentials.json /app/credentials/gmail-credentials.json

EXPOSE 8080

ENV GMAIL_ENABLED=true
ENV GMAIL_CREDENTIALS_PATH=/app/credentials/gmail-credentials.json
ENV GMAIL_USER_EMAIL=your-email@gmail.com

ENTRYPOINT ["java", "-jar", "app.jar"]
```

**docker-compose.yml:**
```yaml
version: '3.8'

services:
  thesis-flow:
    build: .
    ports:
      - "8080:8080"
    environment:
      GMAIL_ENABLED: 'true'
      GMAIL_CREDENTIALS_PATH: /app/credentials/gmail-credentials.json
      GMAIL_USER_EMAIL: your-email@gmail.com
    volumes:
      - ./credentials.json:/app/credentials/gmail-credentials.json:ro
    depends_on:
      - db
  
  db:
    image: postgres:15
    environment:
      POSTGRES_DB: thesis_flow
      POSTGRES_USER: thesis_flow_owner
      POSTGRES_PASSWORD: owner
    volumes:
      - pgdata:/var/lib/postgresql/data

volumes:
  pgdata:
```

### Kubernetes Deployment

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: thesis-flow-config
data:
  GMAIL_ENABLED: "true"
  GMAIL_USER_EMAIL: "your-email@gmail.com"
  GMAIL_CREDENTIALS_PATH: "/etc/secrets/gmail-credentials.json"

---
apiVersion: v1
kind: Secret
metadata:
  name: gmail-credentials
type: Opaque
data:
  gmail-credentials.json: <base64-encoded-json>

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: thesis-flow
spec:
  replicas: 2
  selector:
    matchLabels:
      app: thesis-flow
  template:
    metadata:
      labels:
        app: thesis-flow
    spec:
      containers:
      - name: app
        image: thesis-flow:latest
        ports:
        - containerPort: 8080
        envFrom:
        - configMapRef:
            name: thesis-flow-config
        volumeMounts:
        - name: gmail-credentials
          mountPath: /etc/secrets
          readOnly: true
      volumes:
      - name: gmail-credentials
        secret:
          secretName: gmail-credentials
```

---

## Monitoring & Logging

### Log Levels

Set log level for debugging:

```yaml
logging:
  level:
    ar.edu.uns.cs.thesisflow.auth.service: DEBUG
```

### Key Log Messages

| Level | Message | Meaning |
|-------|---------|---------|
| INFO | "Gmail API service initialized successfully" | Gmail API is ready |
| INFO | "Using Gmail API to send login link email" | Email sent via Gmail |
| INFO | "Using Spring Mail to send login link email" | Email sent via SMTP |
| WARN | "Gmail API is disabled" | Gmail API not configured |
| ERROR | "Failed to initialize Gmail API service" | Credentials or API error |
| ERROR | "Failed to send email via Gmail API" | Email send failed |

### Metrics (Optional)

Track email sending metrics:

```kotlin
@Service
class EmailService(
    private val mailSender: JavaMailSender,
    private val gmailSender: GmailEmailSender,
    private val meterRegistry: MeterRegistry, // Spring Boot Micrometer
) {
    fun sendProfessorLoginLink(professor: Professor, loginLink: String) {
        try {
            // ... send email
            meterRegistry.counter("email.sent", "provider", "gmail").increment()
        } catch (e: Exception) {
            meterRegistry.counter("email.failed", "provider", "gmail").increment()
            throw e
        }
    }
}
```

---

## Future Enhancements

1. **Email Templates** - Support template engines (Thymeleaf, FreeMarker)
2. **Batch Sending** - Optimize for bulk email sending
3. **Email Attachments** - Support file attachments
4. **Scheduled Sending** - Send emails at specific times
5. **Read Receipts** - Track email opens
6. **Multiple Providers** - Support Sendgrid, AWS SES, etc.
7. **Queue System** - Add message queue for async sending (RabbitMQ, Kafka)

---

## References

- [Google Workspace Gmail API Quickstart](https://developers.google.com/workspace/gmail/api/quickstart/java)
- [Gmail API Documentation](https://developers.google.com/gmail/api)
- [Google Auth Library for Java](https://github.com/googleapis/google-auth-library-java)
- [Google API Client Library for Java](https://developers.google.com/api-client-library/java)
- [Email Security Best Practices](https://www.owasp.org/index.php/Email)

---

**Document Version:** 1.0  
**Last Updated:** 2025-11-24  
**Status:** Complete
