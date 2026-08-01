<?php
/**
 * Forgot password request handler — processes password reset requests.
 *
 * Accepts POST request with email address, validates CSRF token, and if the
 * account exists, generates a reset token and sends it via email. Always
 * returns generic success message (anti-enumeration protection).
 *
 * @package TalentBridge
 */

session_start();
require_once 'includes/helpers.php';
require_once 'includes/db.php';
require_once 'includes/csrf.php';
require_once 'includes/mailer.php';
require_once 'includes/tokens.php';

$email = trim($_POST['email'] ?? '');

// validate csrf token first
if (!validateCsrfToken($_POST['csrf_token'] ?? '')) {
    http_response_code(403);
    exit('Invalid CSRF token.');
}

if (empty($email)) {
    setFlash('error', 'Please enter an email address.');
    redirect('/forgot-password.php');
}

if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    setFlash('error', 'Please enter a valid email address.');
    redirect('/forgot-password.php');
}

try {
    $pdo = getConnection();

    // find the account (don't reveal whether it exists)
    $stmt = $pdo->prepare("
        SELECT user_id, name, email
          FROM users
         WHERE email = :email
         LIMIT 1
    ");
    $stmt->execute([':email' => $email]);
    $user = $stmt->fetch();

    if ($user) {
        // invalidate any existing reset tokens for this user
        invalidateAllTokens($user['user_id'], 'password_reset');

        // generate new reset token
        $resetToken = generateAndStoreToken(
            $user['user_id'],
            'password_reset',
            PASSWORD_RESET_TOKEN_EXPIRY
        );

        if ($resetToken && sendPasswordResetEmail($user['email'], $user['name'], $resetToken)) {
            log_audit_event('PASSWORD_RESET_REQUESTED', $user['user_id']);
        } else {
            error_log("Password reset email failed for user {$user['user_id']}");
            // still show generic success to prevent account enumeration
        }
    } else {
        // user doesn't exist, but don't reveal this
        log_audit_event('PASSWORD_RESET_INVALID_EMAIL', null, ['email_attempted' => $email]);
    }

    // always show generic success message
    setFlash('success', 'If an account with that email exists, you will receive a password reset link shortly.');
    redirect('/forgot-password.php');

} catch (PDOException $e) {
    error_log('Forgot password request error: ' . $e->getMessage());
    setFlash('error', 'An error occurred. Please try again later.');
    redirect('/forgot-password.php');
}
