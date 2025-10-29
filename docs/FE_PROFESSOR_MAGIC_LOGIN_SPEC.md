# Frontend Feature Specification: Professor Magic Link Login System

**Date:** 2025-10-28  
**Status:** Ready for Implementation  
**Backend Feature Branch:** `feature/professor-magic-link-login`

---

## 1. Overview

This feature implements a passwordless login system for professors using one-time magic links delivered via email. Professors no longer need to remember passwords—they simply provide their email address and receive a secure link to login.

### Key Benefits
- 🔒 **No Password Management** - Professors don't need to remember passwords
- 🛡️ **Enhanced Security** - One-time use tokens with 15-minute expiry
- 📧 **Simple Flow** - Email-based verification
- ✨ **Better UX** - Seamless login experience

---

## 2. User Journey

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. Professor navigates to login page                            │
│    ↓                                                             │
│ 2. Enters email address                                         │
│    ↓                                                             │
│ 3. Clicks "Send Magic Link" button                              │
│    ↓                                                             │
│ 4. FE sends POST to /auth/professor/request-login-link          │
│    ↓                                                             │
│ 5. BE generates token, stores in DB, sends email               │
│    ↓                                                             │
│ 6. Shows confirmation message: "Check your email"              │
│    ↓                                                             │
│ 7. Professor receives email with magic link                     │
│    ↓                                                             │
│ 8. Professor clicks link (contains token in URL)               │
│    ↓                                                             │
│ 9. FE auto-extracts token, shows loading state                 │
│    ↓                                                             │
│ 10. FE submits token to /auth/professor/verify-login-link       │
│     ↓                                                             │
│ 11. BE validates token, marks as used, issues JWT              │
│     ↓                                                             │
│ 12. FE receives JWT, stores in localStorage                    │
│     ↓                                                             │
│ 13. FE redirects to home page                                  │
│     ↓                                                             │
│ 14. User is logged in ✅                                        │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. Pages & Components

### 3.1 Professor Login Page

**Route:** `/professor-login`

**Purpose:** Allow professors to request a magic login link

**Layout:**
```
┌─────────────────────────────────────────────┐
│                                             │
│        🎓 Professor Login                   │
│                                             │
│  Email Address                              │
│  ┌─────────────────────────────────────┐   │
│  │ prof@university.edu                 │   │
│  └─────────────────────────────────────┘   │
│                                             │
│  ┌─────────────────────────────────────┐   │
│  │  Send Magic Link (disabled/loading)  │   │
│  └─────────────────────────────────────┘   │
│                                             │
│  ℹ️ Check your email for the login link    │
│  (shown after sending)                     │
│                                             │
│  Back to Admin Login                        │
│                                             │
└─────────────────────────────────────────────┘
```

**Components Needed:**
1. **EmailInput Component**
   - Input field for email
   - Email validation (client-side)
   - Error message display
   - Props: value, onChange, error, disabled

2. **SendLinkButton Component**
   - Primary button "Send Magic Link"
   - Disabled during loading/after send
   - Loading spinner
   - Props: onClick, loading, disabled

3. **SuccessMessage Component**
   - Shows after link sent
   - Message: "Check your email for a login link"
   - Resend option (after 60 seconds)
   - Props: show, onResend

4. **ErrorMessage Component**
   - Display form errors
   - User-friendly error messages
   - Props: error, onDismiss

**Form State:**
```typescript
{
  email: string;
  loading: boolean;
  error: string | null;
  success: boolean;
  resendTimeout: number;
}
```

**Form Validation:**
- Email format validation (regex or library)
- Show inline error for invalid email
- Disable button if email invalid
- Client-side validation prevents unnecessary API calls

**API Integration:**
```typescript
// POST /auth/professor/request-login-link
requestLoginLink(email: string): Promise<{ 
  message: string 
}>

// Handle responses:
- 200: Show success message, offer resend after 60s
- 400: Show error "Invalid email address"
- 409: Show error "Professor not found"
- 429: Show error "Too many requests. Try again in 1 hour"
- 500: Show error "Server error. Please try again later"
```

**UX Features:**
- ✅ Auto-focus on email input
- ✅ Submit on Enter key
- ✅ Clear button or × to clear input
- ✅ Loading spinner during submission
- ✅ Disable form while loading
- ✅ Show character count if needed
- ✅ Link to admin login page

---

### 3.2 Login Link Verification Page

**Route:** `/professor-login/verify`

**Query Parameter:** `?token={token}`

**Purpose:** Verify the magic link token and issue JWT

**Behavior:**
1. User lands on page with token in URL
2. Page automatically extracts token from query params
3. Shows loading state with message
4. Auto-submits token to backend
5. On success: redirects to home page (`/` or `/home`)
6. On error: shows error with retry option

**Layout:**
```
┌─────────────────────────────────────────────┐
│                                             │
│        🔑 Verifying your login...           │
│                                             │
│        ⟳ [Loading spinner]                  │
│                                             │
│   Please wait while we verify your link    │
│                                             │
└─────────────────────────────────────────────┘
```

**Error States Layout:**
```
┌─────────────────────────────────────────────┐
│                                             │
│        ❌ Login Link Invalid                │
│                                             │
│   This link has expired or is invalid      │
│                                             │
│   ┌─────────────────────────────────────┐  │
│   │  Request New Link                    │  │
│   └─────────────────────────────────────┘  │
│                                             │
│   This link was valid for 15 minutes       │
│                                             │
└─────────────────────────────────────────────┘
```

**Components Needed:**
1. **VerificationLoader Component**
   - Loading spinner animation
   - Status message: "Verifying your login..."
   - Props: show

2. **VerificationError Component**
   - Error icon/message
   - Error description
   - Action button to request new link
   - Props: error, onRetry

3. **TokenExtractor Hook**
   - Extract token from query params
   - Validate token format (basic check)
   - Return token or null

**Component Logic:**
```typescript
useEffect(() => {
  // On mount, extract token from URL
  const token = new URLSearchParams(window.location.search).get('token');
  
  if (!token) {
    setError('Invalid or missing token');
    return;
  }
  
  // Auto-submit verification
  verifyLoginLink(token)
    .then(response => {
      // Store JWT
      localStorage.setItem('authToken', response.token);
      
      // Redirect after short delay
      setTimeout(() => {
        window.location.href = '/'; // or home page
      }, 500);
    })
    .catch(error => {
      setError(getErrorMessage(error));
    });
}, []);
```

**API Integration:**
```typescript
// POST /auth/professor/verify-login-link
verifyLoginLink(token: string): Promise<{
  accessToken: string;
  redirectUrl: string;
}>

// Handle responses:
- 200: {
    accessToken: "eyJhbGc...",
    redirectUrl: "/"
  }
  → Store token in localStorage
  → Redirect to home page

- 400: Invalid or expired token
  → Show error message
  → Offer button to request new link

- 404: Token not found
  → Show error message
  → Offer button to request new link

- 500: Server error
  → Show error message
  → Offer button to try again
```

**Error Messages:**
```
- "Invalid or missing token" → Token not in URL
- "This link has expired" → Token older than 15 minutes
- "This link has already been used" → Token already verified
- "Server error. Please try again" → Backend error
```

---

## 4. Email Template

**Subject:** `Your Magic Login Link for Thesis Flow`

**Email Body (HTML):**
```html
<!DOCTYPE html>
<html>
<head>
  <style>
    body { font-family: Arial, sans-serif; }
    .container { max-width: 500px; margin: 0 auto; }
    .header { background: #007bff; color: white; padding: 20px; text-align: center; }
    .content { padding: 20px; background: #f9f9f9; }
    .button { 
      display: inline-block; 
      background: #007bff; 
      color: white; 
      padding: 12px 30px; 
      text-decoration: none;
      border-radius: 5px;
      margin: 20px 0;
    }
    .footer { font-size: 12px; color: #666; padding: 20px; text-align: center; }
  </style>
</head>
<body>
  <div class="container">
    <div class="header">
      <h1>🎓 Thesis Flow</h1>
    </div>
    
    <div class="content">
      <h2>Hello {{PROFESSOR_NAME}},</h2>
      
      <p>You requested a login link to access Thesis Flow. Click the button below to login:</p>
      
      <center>
        <a href="{{LOGIN_LINK}}" class="button">
          Login to Thesis Flow
        </a>
      </center>
      
      <p>Or copy and paste this link in your browser:</p>
      <p style="word-break: break-all; background: white; padding: 10px; border-radius: 3px;">
        {{LOGIN_LINK}}
      </p>
      
      <p><strong>⚠️ Important:</strong></p>
      <ul>
        <li>This link expires in <strong>15 minutes</strong></li>
        <li>This link can only be used once</li>
        <li>If you didn't request this link, you can safely ignore this email</li>
      </ul>
    </div>
    
    <div class="footer">
      <p>© 2025 Thesis Flow. All rights reserved.</p>
      <p>This is an automated email, please do not reply.</p>
    </div>
  </div>
</body>
</html>
```

**Plain Text Fallback:**
```
Hello {{PROFESSOR_NAME}},

You requested a login link to access Thesis Flow.

Login Link:
{{LOGIN_LINK}}

This link expires in 15 minutes and can only be used once.

If you didn't request this link, you can safely ignore this email.

---
© 2025 Thesis Flow
```

---

## 5. API Endpoints

### 5.1 Request Login Link

**Endpoint:** `POST /auth/professor/request-login-link`

**Authentication:** None required

**Request Body:**
```json
{
  "email": "professor@university.edu"
}
```

**Success Response (200 OK):**
```json
{
  "message": "A magic login link has been sent to your email"
}
```

**Error Responses:**

- **400 Bad Request** - Invalid email format
  ```json
  {
    "error": "Invalid email address",
    "message": "Please provide a valid email address"
  }
  ```

- **404 Not Found** - Professor not found
  ```json
  {
    "error": "Professor not found",
    "message": "No professor account found with this email address"
  }
  ```

- **429 Too Many Requests** - Rate limited
  ```json
  {
    "error": "Too many requests",
    "message": "Maximum 10 requests per hour. Please try again later."
  }
  ```

- **500 Internal Server Error** - Email sending failed
  ```json
  {
    "error": "Failed to send email",
    "message": "We couldn't send the email. Please try again later."
  }
  ```

---

### 5.2 Verify Login Link

**Endpoint:** `POST /auth/professor/verify-login-link`

**Authentication:** None required

**Request Body:**
```json
{
  "token": "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e"
}
```

**Success Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "redirectUrl": "/"
}
```

**Error Responses:**

- **400 Bad Request** - Invalid token format
  ```json
  {
    "error": "Invalid token",
    "message": "The provided token is invalid or malformed"
  }
  ```

- **400 Bad Request** - Token expired
  ```json
  {
    "error": "Token expired",
    "message": "This login link has expired. Please request a new one."
  }
  ```

- **400 Bad Request** - Token already used
  ```json
  {
    "error": "Token already used",
    "message": "This login link has already been used. Please request a new one."
  }
  ```

- **404 Not Found** - Token not found
  ```json
  {
    "error": "Token not found",
    "message": "This login link is invalid. Please request a new one."
  }
  ```

- **500 Internal Server Error** - Server error
  ```json
  {
    "error": "Server error",
    "message": "An error occurred while processing your login. Please try again."
  }
  ```

---

## 6. State Management

### Redux Store Structure (Recommended)

```typescript
interface ProfessorAuthState {
  loading: boolean;
  error: string | null;
  success: boolean;
  resendTimeout: number;
  verifying: boolean;
  verificationError: string | null;
}

interface RootState {
  professorAuth: ProfessorAuthState;
  // ... other state
}
```

### Actions

```typescript
// Request login link
type RequestLoginLinkAction = {
  type: 'professor/requestLoginLink';
  payload: { email: string };
};

// Request login link success
type RequestLoginLinkSuccessAction = {
  type: 'professor/requestLoginLinkSuccess';
};

// Request login link error
type RequestLoginLinkErrorAction = {
  type: 'professor/requestLoginLinkError';
  payload: { error: string };
};

// Verify login link
type VerifyLoginLinkAction = {
  type: 'professor/verifyLoginLink';
  payload: { token: string };
};

// Verify login link success
type VerifyLoginLinkSuccessAction = {
  type: 'professor/verifyLoginLinkSuccess';
  payload: { token: string };
};

// Verify login link error
type VerifyLoginLinkErrorAction = {
  type: 'professor/verifyLoginLinkError';
  payload: { error: string };
};
```

---

## 7. Error Handling

### Error Types & User Messages

| Error Code | Backend Error | User Message | Action |
|-----------|--------------|--------------|--------|
| 400 | Invalid email | "Please provide a valid email address" | Show error, focus input |
| 404 | Professor not found | "No professor account found with this email" | Show error, suggest admin contact |
| 400 | Token expired | "This link has expired. Please request a new one." | Show error, offer new link button |
| 400 | Token already used | "This link has already been used" | Show error, offer new link button |
| 404 | Token not found | "This login link is invalid" | Show error, offer new link button |
| 429 | Rate limited | "Too many requests. Try again in 1 hour" | Show error, disable for period |
| 500 | Server error | "Server error. Please try again later" | Show error, offer retry |

### Error Recovery

- **On Email Request Error:**
  - Keep form visible
  - Show error message
  - Allow user to retry
  - Offer "Back" link

- **On Token Verification Error:**
  - Show error page
  - Provide "Request New Link" button
  - Redirect to request page
  - Offer "Back to Login" link

---

## 8. Token Management

### Storage

```typescript
// Store JWT in localStorage
localStorage.setItem('authToken', response.accessToken);

// Retrieve for API calls
const token = localStorage.getItem('authToken');

// Clear on logout
localStorage.removeItem('authToken');
```

### Token Usage in API Calls

```typescript
// In API interceptor
axios.interceptors.request.use((config) => {
  const token = localStorage.getItem('authToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

### Session Management

- **No automatic expiry on frontend** - Let JWT expiry handle it
- **On 401 Unauthorized** - Clear token, redirect to login
- **On Logout** - Clear token, redirect to login
- **On New Tab** - Read token from localStorage

---

## 9. Routing

### Route Configuration

```typescript
const routes = [
  {
    path: '/professor-login',
    component: ProfessorLoginPage,
    name: 'Professor Login',
    requiresAuth: false,
  },
  {
    path: '/professor-login/verify',
    component: LoginLinkVerificationPage,
    name: 'Verify Login Link',
    requiresAuth: false,
  },
  // Protected routes
  {
    path: '/',
    component: HomePage,
    requiresAuth: true,
  },
];
```

### Route Guards

```typescript
// Before each route, check if token exists
router.beforeEach((to, from, next) => {
  const requiresAuth = to.matched.some(record => record.requiresAuth);
  const token = localStorage.getItem('authToken');
  
  if (requiresAuth && !token) {
    // Redirect to login
    next('/professor-login');
  } else {
    next();
  }
});
```

---

## 10. Testing Checklist

### Unit Tests

- [ ] Email validation (valid/invalid formats)
- [ ] Token extraction from URL query params
- [ ] Error message mapping
- [ ] localStorage operations
- [ ] Token expiry calculation

### Integration Tests

- [ ] Full request-to-verify flow
- [ ] Invalid email handling
- [ ] Professor not found handling
- [ ] Token expiry handling
- [ ] Token already used handling
- [ ] Rate limiting handling
- [ ] Server error handling
- [ ] JWT storage and retrieval
- [ ] Redirect on success
- [ ] Auto-extraction of token from URL

### E2E Tests

- [ ] Happy path: Email → Link → Login
- [ ] Email validation and error
- [ ] Invalid token error with retry
- [ ] Expired token error
- [ ] Session persistence across page refresh
- [ ] Logout clears token
- [ ] Protected routes redirect to login

### Manual Testing

- [ ] Test on different email formats
- [ ] Test resend link functionality
- [ ] Test with expired token
- [ ] Test with already-used token
- [ ] Test rate limiting (10 requests)
- [ ] Test redirect URL after login
- [ ] Test browser back button
- [ ] Test on mobile devices
- [ ] Test accessibility (screen readers, keyboard nav)
- [ ] Test loading states

---

## 11. Configuration

### Environment Variables

```bash
# .env (or .env.local)
VITE_API_BASE_URL=http://localhost:8080
VITE_PROFESSOR_LOGIN_PATH=/professor-login
VITE_HOME_REDIRECT_PATH=/
VITE_TOKEN_STORAGE_KEY=authToken
VITE_TOKEN_EXPIRY_MINUTES=15
```

### API Configuration

```typescript
const API_CONFIG = {
  REQUEST_LINK_ENDPOINT: '/auth/professor/request-login-link',
  VERIFY_LINK_ENDPOINT: '/auth/professor/verify-login-link',
  TIMEOUT: 10000,
  RETRY_ATTEMPTS: 2,
};
```

---

## 12. Accessibility Requirements

- [ ] ARIA labels on all inputs
- [ ] ARIA descriptions for error messages
- [ ] Keyboard navigation (Tab, Enter)
- [ ] Focus management
- [ ] Color contrast (WCAG AA)
- [ ] Loading announcements for screen readers
- [ ] Error announcements
- [ ] Form labels properly associated with inputs

---

## 13. Performance Considerations

- **Lazy load verification page** - Only load when needed
- **Memoize components** - Use React.memo for static components
- **Debounce email input** - Avoid re-rendering on every keystroke
- **Cancel previous requests** - Use AbortController for cleanup
- **Optimize bundle** - Keep components lightweight
- **Cache professor data** - Don't re-fetch on every request

---

## 14. Security Considerations

- ✅ **HTTPS only** - Ensure all communication is encrypted
- ✅ **Token in localStorage** - Acceptable for single-page apps
- ✅ **CSRF protection** - Backend should handle CSRF tokens
- ✅ **XSS prevention** - Always sanitize/escape user input
- ✅ **Rate limiting** - Shown in error responses
- ✅ **Secure token format** - 64-character random string
- ✅ **No token in URL** - Token only in request body
- ✅ **HTTP-only cookies** (optional) - Consider for sensitive deployments

---

## 15. Component Examples

### ProfessorLoginPage.tsx

```typescript
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { requestLoginLink } from '../api/auth';

export const ProfessorLoginPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [resendTimeout, setResendTimeout] = useState(0);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      await requestLoginLink(email);
      setSuccess(true);
      
      // Resend timeout
      setResendTimeout(60);
      const interval = setInterval(() => {
        setResendTimeout(prev => {
          if (prev <= 1) {
            clearInterval(interval);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="professor-login-page">
      <h1>🎓 Professor Login</h1>
      
      {success ? (
        <div className="success-message">
          <p>✅ Check your email for a magic login link</p>
          <button 
            onClick={() => {
              setSuccess(false);
              setEmail('');
            }}
            disabled={resendTimeout > 0}
          >
            {resendTimeout > 0 ? `Resend in ${resendTimeout}s` : 'Send Again'}
          </button>
        </div>
      ) : (
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="email">Email Address</label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="professor@university.edu"
              disabled={loading}
              autoFocus
            />
          </div>

          {error && <div className="error-message">{error}</div>}

          <button 
            type="submit" 
            disabled={loading || !email}
          >
            {loading ? 'Sending...' : 'Send Magic Link'}
          </button>
        </form>
      )}
    </div>
  );
};
```

### LoginLinkVerificationPage.tsx

```typescript
import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { verifyLoginLink } from '../api/auth';

export const LoginLinkVerificationPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = searchParams.get('token');

    if (!token) {
      setError('Invalid or missing token');
      setLoading(false);
      return;
    }

    verifyLoginLink(token)
      .then((response) => {
        localStorage.setItem('authToken', response.accessToken);
        
        // Redirect after short delay
        setTimeout(() => {
          navigate('/');
        }, 500);
      })
      .catch((err) => {
        setError(getErrorMessage(err));
        setLoading(false);
      });
  }, [searchParams, navigate]);

  if (loading) {
    return (
      <div className="verification-page">
        <div className="loading-spinner">
          <h2>🔑 Verifying your login...</h2>
          <p>Please wait while we verify your link</p>
        </div>
      </div>
    );
  }

  return (
    <div className="verification-page">
      <div className="error-state">
        <h2>❌ Login Link Invalid</h2>
        <p>{error}</p>
        <button onClick={() => navigate('/professor-login')}>
          Request New Link
        </button>
      </div>
    </div>
  );
};
```

---

## 16. API Client Example

```typescript
import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const apiClient = axios.create({
  baseURL: API_BASE,
  timeout: 10000,
});

// Add token to requests
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('authToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle 401 errors
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('authToken');
      window.location.href = '/professor-login';
    }
    return Promise.reject(error);
  }
);

export const requestLoginLink = (email: string) => {
  return apiClient.post('/auth/professor/request-login-link', { email });
};

export const verifyLoginLink = (token: string) => {
  return apiClient.post('/auth/professor/verify-login-link', { token });
};

export const logout = () => {
  localStorage.removeItem('authToken');
};
```

---

## 17. Styling Guide

### Color Scheme

```css
/* Primary Colors */
--primary-color: #007bff;      /* Blue */
--success-color: #28a745;      /* Green */
--error-color: #dc3545;        /* Red */
--warning-color: #ffc107;      /* Yellow */

/* Neutral Colors */
--text-primary: #333333;
--text-secondary: #666666;
--bg-light: #f9f9f9;
--bg-white: #ffffff;
--border-color: #ddd;

/* Spacing */
--spacing-xs: 4px;
--spacing-sm: 8px;
--spacing-md: 16px;
--spacing-lg: 24px;
--spacing-xl: 32px;

/* Typography */
--font-size-sm: 12px;
--font-size-base: 14px;
--font-size-lg: 16px;
--font-size-xl: 20px;
--font-size-xxl: 24px;
```

### Component Sizes

```css
/* Form Elements */
input, button {
  height: 40px;
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 14px;
}

/* Buttons */
button {
  min-width: 120px;
  font-weight: 600;
}

/* Container */
.container {
  max-width: 500px;
  margin: 0 auto;
  padding: 16px;
}
```

---

## 18. Summary

This professor magic link login system provides:

✅ **Seamless UX** - Simple email input, automatic verification
✅ **Security** - One-time tokens, 15-minute expiry, rate limiting
✅ **Scalability** - Stateless JWT tokens
✅ **Reliability** - Error handling, retry mechanisms
✅ **Accessibility** - WCAG compliant, keyboard navigation
✅ **Mobile Ready** - Responsive design, mobile-friendly

---

**Backend Feature Branch:** `feature/professor-magic-link-login`  
**Last Updated:** 2025-10-28  
**Ready for Implementation:** ✅ Yes
