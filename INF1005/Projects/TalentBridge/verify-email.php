<?php
/**
 * Email verification endpoint — validates email tokens and marks accounts verified.
 *
 * Accepts GET parameter 'token', validates it, marks the user email as verified,
 * then redirects to login with success or error message.
 *
 * @package TalentBridge
 */

session_start();
require_once 'includes/helpers.php';
require_once 'includes/db.php';
require_once 'includes/tokens.php';

// if already logged in, redirect to home
if (isset($_SESSION['user_id'])) {
    redirect('/index.php');
}

$token = trim($_GET['token'] ?? '');

if (empty($token)) {
    setFlash('error', 'Invalid verification link.');
    redirect('/login.php');
}

// validate the token
$userId = validateToken($token, 'email_verification');

if ($userId === null) {
    setFlash('error', 'This verification link is invalid or has expired. Please request a new one.');
    redirect('/login.php');
}

try {
    $pdo = getConnection();
    $pdo->beginTransaction();

    // mark the email as verified
    $stmt = $pdo->prepare("
        UPDATE users
           SET email_verified_at = NOW()
         WHERE user_id = :user_id
    ");
    $stmt->execute([':user_id' => $userId]);

    // consume the token to prevent reuse
    if (!consumeToken($token, 'email_verification')) {
        throw new Exception('Failed to consume verification token.');
    }

    $pdo->commit();

    // log the verification event
    log_audit_event('EMAIL_VERIFIED', $userId);

    setFlash('success', 'Your email has been verified successfully. You can now log in.');
    redirect('/login.php');

} catch (PDOException $e) {
    if ($pdo->inTransaction()) {
        $pdo->rollBack();
    }
    error_log('Email verification error: ' . $e->getMessage());
    setFlash('error', 'An error occurred during verification. Please try again later.');
    redirect('/login.php');
}
