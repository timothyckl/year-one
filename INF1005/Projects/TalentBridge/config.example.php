<?php
/**
 * Database configuration template.
 *
 * Copy this file to config.php and fill in the real values.
 * config.php is gitignored and must never be committed.
 *
 * @package TalentBridge
 */

// database host (usually localhost on a lamp stack)
define('DB_HOST', 'localhost');

// database name
define('DB_NAME', 'talentbridge');

// database username
define('DB_USER', 'root');

// database password
define('DB_PASS', 'your_db_password');

// --- Security Settings ---

// max failed login attempts before account lockout
define('MAX_LOGIN_ATTEMPTS', 5);
// lockout duration in seconds (e.g., 900 = 15 minutes)
define('LOCKOUT_DURATION', 900);

// progressive lockout durations in seconds for repeated lockouts
define('PROGRESSIVE_LOCKOUTS', [
    900,    // 1st lockout: 15 minutes
    3600,   // 2nd lockout: 1 hour
    86400,  // 3rd and subsequent lockouts: 24 hours
]);

// ip-based login throttling (global)
// max failed logins from one ip in IP_THROTTLE_WINDOW seconds
define('IP_THROTTLE_LIMIT', 50);
// time window in seconds (e.g., 3600 = 1 hour)
define('IP_THROTTLE_WINDOW', 3600);
// duration to block the ip for in seconds (e.g., 1800 = 30 minutes)
define('IP_THROTTLE_DURATION', 1800);

// --- Email / SMTP Settings ---

// email sender address (e.g., noreply@talentbridge.com)
define('MAIL_FROM_ADDRESS', 'noreply@talentbridge.local');
define('MAIL_FROM_NAME', 'TalentBridge');

// SMTP server configuration
// For local testing with Mailpit: use localhost:1025
// For production: use actual mail server credentials
define('MAIL_HOST', 'localhost');
define('MAIL_PORT', 1025);
define('MAIL_USERNAME', '');
define('MAIL_PASSWORD', '');
// 'tls', 'ssl', or '' for none
define('MAIL_ENCRYPTION', '');

// Application base URL (used in email links)
// e.g. 'https://talentbridge.com' for production, 'http://localhost' for local
define('APP_BASE_URL', 'http://localhost');

// Email token expiry in seconds (e.g., 86400 = 1 day, 1800 = 30 minutes)
define('EMAIL_VERIFICATION_TOKEN_EXPIRY', 86400); // 24 hours
define('PASSWORD_RESET_TOKEN_EXPIRY', 1800);     // 30 minutes

