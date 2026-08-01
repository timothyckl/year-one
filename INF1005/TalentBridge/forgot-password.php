<?php
/**
 * Forgot password form — allows users to request a password reset.
 *
 * Users enter their email, and if the account exists, a reset token is sent
 * to their email. A generic success message is shown regardless (anti-enumeration).
 *
 * @package TalentBridge
 */

session_start();
require_once 'includes/helpers.php';
require_once 'includes/auth.php';
require_once 'includes/csrf.php';

// already logged-in users should not be here
if (isLoggedIn()) {
    redirect('/index.php');
}

$flash = getFlash();
$csrfToken = generateCsrfToken();
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Forgot Password — TalentBridge</title>
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
                        <h1 class="h3 fw-bold mb-1" style="color:var(--tb-primary)">Forgot Password</h1>
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

                        <p class="text-muted mb-4">
                            Enter the email address associated with your account, and we'll send you a link to reset your password.
                        </p>

                        <form id="forgotPasswordForm" method="post" action="/forgot-password-request.php" novalidate>
                            <input type="hidden" name="csrf_token" value="<?= sanitise($csrfToken) ?>">

                            <!-- email -->
                            <div class="mb-4">
                                <label for="email" class="form-label">Email Address <span aria-hidden="true" class="text-danger">*</span></label>
                                <input type="email" id="email" name="email"
                                    class="form-control"
                                    required autocomplete="email"
                                    placeholder="you@example.com">
                            </div>

                            <button type="submit" class="btn btn-primary w-100 py-2 fw-bold">
                                Send Reset Link
                            </button>
                        </form>

                        <p class="text-center text-muted mt-3 mb-0 small">
                            Remember your password? <a href="/login.php">Log in here</a>
                        </p>
                    </div>

                </div>
            </div>
        </div>
    </section>
</main>

<?php require_once 'includes/footer.php'; ?>
