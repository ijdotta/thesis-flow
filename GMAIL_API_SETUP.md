# Gmail API Setup Quick Reference

## Overview

ThesisFlow now supports sending emails via **Google Workspace Gmail API** in addition to the default Spring Mail SMTP backend.

## Quick Start

### 1. Enable Gmail API (2 minutes)

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create or select a project
3. Go to **APIs & Services** → **Library**
4. Search for "Gmail API" and click **Enable**

### 2. Create Service Account (3 minutes)

1. Go to **APIs & Services** → **Credentials**
2. Click **Create Credentials** → **Service Account**
3. Enter name: `thesis-flow-mailer`
4. Grant role: **Editor** (for testing)
5. Create JSON key: Click account → **Keys** → **Add Key** → **JSON**
6. Download and save the credentials file

### 3. Configure Application (1 minute)

**Option A: Environment Variables**
```bash
export GMAIL_ENABLED=true
export GMAIL_CREDENTIALS_PATH=/path/to/credentials.json
export GMAIL_USER_EMAIL=your-email@gmail.com
```

**Option B: Properties File**
```yaml
gmail:
  enabled: true
  credentials-path: /path/to/credentials.json
  user-email: your-email@gmail.com
```

### 4. Start Application
```bash
./gradlew bootRun
```

## Architecture

```
EmailService (high-level)
    ↓
    ├─→ Gmail API (if enabled) → Google Workspace
    └─→ Spring Mail (fallback) → SMTP Server
```

## Testing Email Sending

### 1. Request Login Link
```bash
curl -X POST http://localhost:8080/auth/professor/request-login-link \
  -H "Content-Type: application/json" \
  -d '{"email":"professor@example.com"}'
```

### 2. Check Logs
```
INFO: Using Gmail API to send login link email to: professor@example.com
```

### 3. Check Gmail Inbox
Email should arrive in the configured Gmail account.

## Production Deployment

### Docker
```dockerfile
FROM openjdk:21-slim
COPY build/libs/thesis-flow*.jar app.jar
COPY credentials.json /app/credentials/gmail-credentials.json
ENV GMAIL_ENABLED=true
ENV GMAIL_CREDENTIALS_PATH=/app/credentials/gmail-credentials.json
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Kubernetes
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: gmail-credentials
data:
  gmail-credentials.json: <base64-encoded-json>
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: thesis-flow
spec:
  template:
    spec:
      containers:
      - name: app
        env:
        - name: GMAIL_ENABLED
          value: "true"
        - name: GMAIL_CREDENTIALS_PATH
          value: /etc/secrets/gmail-credentials.json
        volumeMounts:
        - name: gmail-creds
          mountPath: /etc/secrets
          readOnly: true
      volumes:
      - name: gmail-creds
        secret:
          secretName: gmail-credentials
```

## Troubleshooting

### 1. Credentials File Not Found
```
FileNotFoundException: /path/to/credentials.json
```
**Fix:** Verify path is correct and file exists:
```bash
chmod 600 credentials.json
ls -la credentials.json
```

### 2. Authentication Failed
```
Authorization failed: invalid_grant
```
**Fix:** 
- Regenerate credentials JSON from Google Cloud Console
- Ensure service account has correct permissions
- Check credentials JSON is valid

### 3. Gmail API Not Enabled
```
Gmail API has not been used in project
```
**Fix:** Go to Google Cloud Console → APIs & Services → Library → Enable Gmail API

### 4. Permission Denied (403)
```
The user does not have sufficient permissions for the requested action
```
**Fix:** 
- For Google Workspace: Add service account email to domain-wide delegation with `https://www.googleapis.com/auth/gmail.send` scope
- For personal account: Ensure service account has owner permissions on the email account

## Key Files

- **Implementation**: `src/main/kotlin/ar/edu/uns/cs/thesisflow/auth/service/GmailEmailSender.kt`
- **Interface**: `src/main/kotlin/ar/edu/uns/cs/thesisflow/auth/service/IEmailSender.kt`
- **Service**: `src/main/kotlin/ar/edu/uns/cs/thesisflow/auth/service/EmailService.kt` (updated)
- **Configuration**: `src/main/resources/application.yml`
- **Dependencies**: `build.gradle.kts`
- **Documentation**: `docs/GMAIL_API_INTEGRATION.md`

## References

- [Gmail API Documentation](https://developers.google.com/gmail/api)
- [Google Auth Library for Java](https://github.com/googleapis/google-auth-library-java)
- [Google Cloud Console](https://console.cloud.google.com/)

## Support

For detailed setup instructions, see `docs/GMAIL_API_INTEGRATION.md`.

---

**Last Updated:** 2025-11-24
