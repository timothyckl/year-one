<?php
/**
 * Password reset form — allows users to set a new password.
 *
 * Validates the reset token from the URL and provides a form to enter
 * a new password. Token must be valid, not expired, and not yet used.
 *
 * @package TalentBridge
 */

session_start();
require_once 'includes/helpers.php';
require_once 'includes/auth.php';
require_once 'includes/csrf.php';
require_once 'includes/db.php';
require_once 'includes/tokens.php';

// already logged-in users should not be here
if (isLoggedIn()) {
    redirect('/index.php');
}

$token = trim($_GET['token'] ?? '');
$error = '';
$csrfToken = generateCsrfToken();

// validate token format
if (empty($token)) {
    $error = 'Invalid password reset link.';
} else {
    // check if token is valid
    $userId = validateToken($token, 'password_reset');
    if ($userId === null) {
        $error = 'This password reset link is invalid or has expired. Please request a new one.';
    }
}

$flash = getFlash();
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reset Password — TalentBridge</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="assets/css/style.css" rel="stylesheet">
</head>
<body>

<?php require_once 'includes/nav.php'; ?>

<main id="main-content">
    <section class="tb-section">
        <div class="container">
            <div class="row justify-content-center">
                <div class="col-md-7 col-lg-5">

                    <div class="tb-card">
                        <h1 class="h3 fw-bold mb-1" style="color:var(--tb-primary)">Reset Password</h1>
                        <div class="tb-divider"></div>

                        <?php if (!empty($flash['success'])): ?>
                            <div class="alert alert-success tb-flash" role="alert">
                                <?= sanitise($flash['success']) ?>
                            </div>
                        <?php endif; ?>

                        <?php if (!empty($flash['error'])): ?>
                            <div class="alert alert-danger tb-flash" role="alert">
                                <?= sanitise($flash['error']) ?>
                            </div>
                        <?php endif; ?>

                        <?php if (!empty($error)): ?>
                            <div class="alert alert-danger tb-flash" role="alert">
                                <?= sanitise($error) ?>
                            </div>
                            <p class="text-center">
                                <a href="/forgot-password.php" class="btn btn-primary btn-sm">Request New Reset Link</a>
                            </p>
                        <?php else: ?>
                            <form id="resetPasswordForm" method="post" action="/reset-password-submit.php" novalidate>
                                <input type="hidden" name="csrf_token" value="<?= sanitise($csrfToken) ?>">
                                <input type="hidden" name="token" value="<?= sanitise($token) ?>">

                                <!-- password -->
                                <div class="mb-3">
                                    <label for="password" class="form-label">New Password <span aria-hidden="true" class="text-danger">*</span></label>
                                    <input type="password" id="password" name="password"
                                        class="form-control"
                                        required minlength="8"
                                        autocomplete="new-password"
                                        aria-describedby="password_help">
                                    <div id="password_help" class="form-text">At least 8 characters.</div>
                                </div>

                                <!-- password confirm -->
                                <div class="mb-4">
                                    <label for="password_confirm" class="form-label">Confirm Password <span aria-hidden="true" class="text-danger">*</span></label>
                                    <input type="password" id="password_confirm" name="password_confirm"
                                        class="form-control"
                                        required minlength="8"
                                        autocomplete="new-password"
                                        aria-describedby="password_confirm_hint">
                                    <div id="password_confirm_hint" class="form-text">Must match the password above.</div>
                                </div>

                                <button type="submit" class="btn btn-primary w-100 py-2 fw-bold">
                                    Reset Password
                                </button>
                            </form>

                            <p class="text-center text-muted mt-3 mb-0 small">
                                Remember your password? <a href="/login.php">Log in here</a>
                            </p>
                        <?php endif; ?>

                    </div>

                </div>
            </div>
        </div>
    </section>
</main>

<?php require_once 'includes/footer.php'; ?>
