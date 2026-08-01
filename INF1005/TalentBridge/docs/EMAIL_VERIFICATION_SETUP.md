# Email Verification & Password Reset Setup Guide

## Overview

This document explains how to set up and test the email verification and password reset features locally and in production.

## Components Added

### Database Changes

- Added `email_verified_at` column to `users` table (nullable DATETIME)
- Added `email_verification_tokens` table for account verification
- Added `password_reset_tokens` table for password reset flows

Both token tables store only hashed tokens (SHA-256) for security; plain tokens are never persisted.

### Configuration

Add the following to your `config.php` (copy from `config.example.php`):

```php
// Email / SMTP Settings
define('MAIL_FROM_ADDRESS', 'noreply@talentbridge.local');
define('MAIL_FROM_NAME', 'TalentBridge');

// SMTP server configuration
// For local testing with Mailpit: use localhost:1025
// For production: use your mail server
define('MAIL_HOST', 'localhost');
define('MAIL_PORT', 1025);
define('MAIL_USERNAME', '');
define('MAIL_PASSWORD', '');
define('MAIL_ENCRYPTION', ''); // 'tls', 'ssl', or ''

// Application base URL (used in email links)
define('APP_BASE_URL', 'http://localhost');

// Token expiry settings (in seconds)
define('EMAIL_VERIFICATION_TOKEN_EXPIRY', 86400);  // 24 hours
define('PASSWORD_RESET_TOKEN_EXPIRY', 1800);       // 30 minutes
```

## Local Testing Setup

### Option 1: Mailpit (Recommended)

Mailpit is a lightweight SMTP server with a web-based UI for viewing emails.

1. **Download Mailpit** from [mailpit.axllent.org](https://mailpit.axllent.org/)

2. **Run Mailpit**:
   ```bash
   ./mailpit
   ```
   - SMTP server listens on: `localhost:1025`
   - Web UI available at: `http://localhost:8025`

3. **Configure your `config.php`**:
   ```php
   define('MAIL_HOST', 'localhost');
   define('MAIL_PORT', 1025);
   define('MAIL_ENCRYPTION', '');
   define('APP_BASE_URL', 'http://localhost');
   ```

4. **Add PHPMailer**:
   ```bash
   composer install
   ```

5. **Test the flow**:
   - Register a new account
   - Check Mailpit at `http://localhost:8025` for the verification email
   - Click the verification link or copy the token
   - Visit `http://localhost/verify-email.php?token=<TOKEN>`
   - Account is now verified; you can log in

### Option 2: MailHog

MailHog is another lightweight SMTP + web UI alternative.

1. **Download MailHog** from [github.com/mailhog/MailHog](https://github.com/mailhog/MailHog)

2. **Run MailHog**:
   ```bash
   ./MailHog
   ```
   - SMTP server listens on: `localhost:1025`
   - Web UI available at: `http://localhost:8025`

3. Configuration and testing steps are identical to Mailpit above.

## Feature Workflows

### Registration with Email Verification

1. User visits `/register.php`
2. Fills in account details and submits the form
3. Account is created with `email_verified_at = NULL`
4. A verification token is generated and sent via email
5. User receives email with verification link: `/verify-email.php?token=<TOKEN>`
6. Clicking the link verifies the account (`email_verified_at` is set to current time)
7. User can now log in

**Login Check**: If a user tries to log in before verifying their email, they see:
> "Please verify your email address before logging in. Check your inbox for the verification link."

### Password Reset Flow

1. User visits `/forgot-password.php` and enters their email
2. If the account exists, a password reset token is generated and emailed
3. Generic success message is shown regardless (prevents account enumeration)
4. User clicks the reset link: `/reset-password.php?token=<TOKEN>`
5. Form appears to set a new password (if token is valid)
6. On submission to `/reset-password-submit.php`:
   - Password is validated (min 8 chars, matching confirmation)
   - Password hash is updated
   - All unused reset tokens for the user are invalidated
   - Current token is marked as consumed
   - Audit log records the password reset
7. User can log in with the new password

## Token Security

### Token Design

- **Token Generation**: 32 random bytes converted to hex (256 bits)
- **Storage**: Only SHA-256 hash of the token is stored in the database
- **Plain Token**: Only sent once via email; never stored in database
- **Expiry**: Each token type has a configurable expiry (24h for verification, 30m for reset)
- **One-Time Use**: Tokens are marked as used after consumption; cannot be reused
- **IP Tracking**: Password reset tokens record the requesting IP for audit purposes

### Token Validation

The `validateToken()` function checks:
- Token hash matches a record in the database
- Token has not expired (`expires_at > NOW()`)
- Token has not been used (`used_at IS NULL`)

If any check fails, the function returns `null`.

## Audit Logging

The following events are logged to the `audit_log` table:

- `REGISTRATION_COMPLETE` - New account registered
- `EMAIL_VERIFIED` - Account email verified
- `PASSWORD_RESET_REQUESTED` - User requested password reset (user_id set)
- `PASSWORD_RESET_INVALID_EMAIL` - Reset requested for non-existent account (email logged)
- `PASSWORD_RESET_SUCCESS` - Password successfully reset
- `LOGIN_UNVERIFIED_EMAIL` - Login attempt with unverified email

## Files Added / Modified

### New Files

- `includes/mailer.php` - PHPMailer wrapper functions
- `includes/tokens.php` - Token generation, validation, and consumption
- `verify-email.php` - Email verification endpoint
- `forgot-password.php` - Password reset request form
- `forgot-password-request.php` - Password reset request handler
- `reset-password.php` - Password reset form
- `reset-password-submit.php` - Password reset submission handler

### Modified Files

- `sql/schema.sql` - Added email_verified_at column and two token tables
- `config.example.php` - Added email configuration constants
- `register.php` - Integrated email verification on registration
- `login.php` - Added email verification check before login
- `composer.json` - Added PHPMailer dependency

## Production Deployment

### Environment Variables

When deploying to production, update your `config.php` with real SMTP credentials:

```php
define('MAIL_HOST', 'smtp.yourmail server.com');
define('MAIL_PORT', 587); // or 465 for SSL
define('MAIL_USERNAME', 'your-email@example.com');
define('MAIL_PASSWORD', 'your-app-password');
define('MAIL_ENCRYPTION', 'tls'); // or 'ssl'
define('APP_BASE_URL', 'https://talentbridge.com'); // HTTPS for production
define('MAIL_FROM_ADDRESS', 'noreply@talentbridge.com');
```

### Database Migration

No manual migration is needed if you re-run the schema file. The `CREATE TABLE IF NOT EXISTS` statements will skip existing tables.

To safely update an existing database:

```bash
# Backup your database first
mysql -u USER -p DB_NAME < backup.sql

# Run the schema (applies only new tables/columns)
mysql -u USER -p DB_NAME < sql/schema.sql
```

### Composer Install

On the production server:

```bash
composer install --no-dev --optimize-autoloader
```

This installs PHPMailer and optimizes autoloading for production.

## Testing Checklist

- [ ] Local SMTP server (Mailpit or MailHog) is running
- [ ] `config.php` has correct MAIL_* and APP_BASE_URL settings
- [ ] `composer install` has been run (PHPMailer is available)
- [ ] Database has been migrated (new tables exist)
- [ ] Register a new account and verify email works in SMTP inbox
- [ ] Verify email link correctly activates the account
- [ ] Attempt login before email verification - correct error message shown
- [ ] Attempt login after email verification - login succeeds
- [ ] Request password reset for existing account - email sent
- [ ] Request password reset for non-existent account - generic success message (no enumeration leak)
- [ ] Click password reset link - form appears if token valid
- [ ] Submit password reset form - new password works
- [ ] Old password no longer works after reset
- [ ] Audit log contains appropriate events for all actions

## Troubleshooting

### "PHPMailer not installed" error

Run `composer install` to install dependencies from `composer.json`.

### Email not sending

1. Verify SMTP server is running and accessible (telnet to MAIL_HOST:MAIL_PORT)
2. Check error logs in `error_log()` output or application error file
3. Verify MAIL_FROM_ADDRESS is a valid email format
4. For production with TLS, ensure `MAIL_ENCRYPTION` is set to `'tls'`

### Token validation always fails

1. Verify `email_verification_tokens` and `password_reset_tokens` tables exist
2. Check that token hash in DB matches the SHA-256 of the plain token
3. Verify `expires_at` is set to a future time `NOW() + interval`
4. Ensure the token has not been marked as used

## Security Considerations

1. **Token Storage**: Only hashed tokens are stored; plain tokens exist only in email
2. **Token Reuse**: Each token can only be used once (marked used after consumption)
3. **Token Expiry**: Tokens expire automatically; no manual cleanup required (old tokens ignored)
4. **Account Enumeration**: Password reset always returns generic success (prevents email harvesting)
5. **Email Headers**: Emails include standard security headers (no-reply addresses, etc.)
6. **CSRF Protection**: All forms use CSRF tokens; token manipulation is logged
7. **Audit Logging**: All security-relevant events are logged to `audit_log` table
8. **Password Hashing**: Passwords are hashed with `PASSWORD_BCRYPT` (bcrypt, salt+hash)

## Future Enhancements

- Add rate limiting to registration and password reset endpoints
- Implement email resend functionality for unverified accounts
- Add admin ability to manually verify accounts or reset passwords
- Implement optional 2FA for password-protected actions
- Add email change verification (send confirmation to new address before updating)
