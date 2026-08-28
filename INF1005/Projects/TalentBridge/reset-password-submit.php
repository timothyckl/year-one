<?php
/**
 * Password reset submit handler — processes password reset completion.
 *
 * Accepts POST request with reset token and new password, validates both,
 * updates the user's password hash, consumes the reset token, and records
 * the event in the audit log.
 *
 * @package TalentBridge
 */

session_start();
require_once 'includes/helpers.php';
require_once 'includes/db.php';
require_once 'includes/csrf.php';
require_once 'includes/tokens.php';

$token = trim($_POST['token'] ?? '');
$password = $_POST['password'] ?? '';
$passwordConfirm = $_POST['password_confirm'] ?? '';
$errors = [];

// validate csrf token first
if (!validateCsrfToken($_POST['csrf_token'] ?? '')) {
    http_response_code(403);
    exit('Invalid CSRF token.');
}

// validate token format
if (empty($token)) {
    $errors['token'] = 'Invalid reset token.';
} else {
    // validate token
    $userId = validateToken($token, 'password_reset');
    if ($userId === null) {
        $errors['token'] = 'This password reset link is invalid or has expired.';
    }
}

// validate password
if (empty($password)) {
    $errors['password'] = 'Password is required.';
} elseif (strlen($password) < 8) {
    $errors['password'] = 'Password must be at least 8 characters.';
}

if ($password !== $passwordConfirm) {
    $errors['password_confirm'] = 'Passwords do not match.';
}

// if any errors, redirect back to form
if (!empty($errors)) {
    setFlash('error', 'Please fix the errors below.');
    redirect('/reset-password.php?token=' . urlencode($token));
}

try {
    $pdo = getConnection();
    $pdo->beginTransaction();

    // update the password
    $hash = password_hash($password, PASSWORD_BCRYPT);
    $stmt = $pdo->prepare("
        UPDATE users
           SET password_hash = :hash,
               failed_login_attempts = 0,
               last_failed_login_at = NULL,
               lockout_count = 0
         WHERE user_id = :user_id
    ");
    $stmt->execute([
        ':hash' => $hash,
        ':user_id' => $userId,
    ]);

    // invalidate all existing reset tokens for this user
    invalidateAllTokens($userId, 'password_reset');

    // consume the current reset token
    if (!consumeToken($token, 'password_reset')) {
        throw new Exception('Failed to consume password reset token.');
    }

    $pdo->commit();

    // log the password reset event
    log_audit_event('PASSWORD_RESET_SUCCESS', $userId);

    setFlash('success', 'Your password has been reset successfully. You can now log in with your new password.');
    redirect('/login.php');

} catch (PDOException $e) {
    if ($pdo->inTransaction()) {
        $pdo->rollBack();
    }
    error_log('Password reset error: ' . $e->getMessage());
    setFlash('error', 'An error occurred during password reset. Please try again later.');
    redirect('/reset-password.php?token=' . urlencode($token));
}
