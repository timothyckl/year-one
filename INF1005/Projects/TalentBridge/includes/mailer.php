<?php
/**
 * Email/SMTP helper using PHPMailer.
 *
 * Provides a simple wrapper around PHPMailer for sending account verification
 * and password reset emails. Requires PHPMailer via Composer.
 *
 * Configuration constants (MAIL_HOST, MAIL_PORT, etc.) must be defined
 * in config.php before calling functions here.
 *
 * @package TalentBridge
 */

use PHPMailer\PHPMailer\PHPMailer;

$autoloadPath = dirname(__DIR__) . '/vendor/autoload.php';
if (file_exists($autoloadPath)) {
    require_once $autoloadPath;
}

/**
 * Sends an email verification link to a new account.
 *
 * @param string $toEmail   The recipient email address.
 * @param string $userName  The recipient's name for personalisation.
 * @param string $token     The plain verification token (will be hashed in DB, but plain here for the link).
 * @return bool  True on success, false on failure.
 */
function sendVerificationEmail(string $toEmail, string $userName, string $token): bool
{
    try {
        $mail = getMailer();

        $mail->addAddress($toEmail, $userName);
        $mail->Subject = 'Verify Your TalentBridge Email Address';

        $verifyLink = APP_BASE_URL . '/verify-email.php?token=' . urlencode($token);

        $mail->Body = <<<HTML
<html lang="en">
<head>
    <style>
        body { font-family: Arial, sans-serif; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .button { display: inline-block; background-color: #007bff; color: white; padding: 12px 24px; text-decoration: none; border-radius: 4px; }
        .footer { font-size: 12px; color: #666; margin-top: 30px; }
    </style>
</head>
<body>
    <div class="container">
        <h2>Welcome to TalentBridge, {$userName}!</h2>
        <p>Thank you for signing up. Please verify your email address to activate your account.</p>
        <p>
            <a href="{$verifyLink}" class="button">Verify Email Address</a>
        </p>
        <p>Or copy and paste this link into your browser:</p>
        <p><code>{$verifyLink}</code></p>
        <p>This link will expire in 24 hours.</p>
        <div class="footer">
            <p>&copy; 2026 TalentBridge. All rights reserved.</p>
        </div>
    </div>
</body>
</html>
HTML;

        $mail->isHTML(true);
        $mail->send();
        return true;

    } catch (\Throwable $e) {
        error_log('Email verification send failed: ' . $e->getMessage());
        return false;
    }
}

/**
 * Sends a password reset link to a user.
 *
 * @param string $toEmail   The recipient email address.
 * @param string $userName  The recipient's name for personalisation.
 * @param string $token     The plain reset token (will be hashed in DB, but plain here for the link).
 * @return bool  True on success, false on failure.
 */
function sendPasswordResetEmail(string $toEmail, string $userName, string $token): bool
{
    try {
        $mail = getMailer();

        $mail->addAddress($toEmail, $userName);
        $mail->Subject = 'Reset Your TalentBridge Password';

        $resetLink = APP_BASE_URL . '/reset-password.php?token=' . urlencode($token);

        $mail->Body = <<<HTML
<html lang="en">
<head>
    <style>
        body { font-family: Arial, sans-serif; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .button { display: inline-block; background-color: #007bff; color: white; padding: 12px 24px; text-decoration: none; border-radius: 4px; }
        .footer { font-size: 12px; color: #666; margin-top: 30px; }
    </style>
</head>
<body>
    <div class="container">
        <h2>Reset Your Password</h2>
        <p>You requested a password reset for your TalentBridge account. Click the button below to reset your password.</p>
        <p>
            <a href="{$resetLink}" class="button">Reset Password</a>
        </p>
        <p>Or copy and paste this link into your browser:</p>
        <p><code>{$resetLink}</code></p>
        <p>This link will expire in 30 minutes.</p>
        <p><strong>If you did not request this, please ignore this email. Your password has not been changed.</strong></p>
        <div class="footer">
            <p>&copy; 2026 TalentBridge. All rights reserved.</p>
        </div>
    </div>
</body>
</html>
HTML;

        $mail->isHTML(true);
        $mail->send();
        return true;

    } catch (\Throwable $e) {
        error_log('Password reset email send failed: ' . $e->getMessage());
        return false;
    }
}

/**
 * Returns a configured PHPMailer instance.
 *
 * Uses SMTP settings from config.php. Requires PHPMailer to be installed
 * via Composer: composer require phpmailer/phpmailer
 *
 * @return PHPMailer  A configured mailer instance ready for use.
 * @throws Exception  If PHPMailer class cannot be found.
 */
function getMailer(): PHPMailer
{
    // attempt to load PHPMailer; provide helpful error if missing
    if (!class_exists('\PHPMailer\PHPMailer\PHPMailer')) {
        throw new \RuntimeException(
            'PHPMailer not installed. Run: composer require phpmailer/phpmailer'
        );
    }

    $mail = new PHPMailer(true);

    // use SMTP
    $mail->isSMTP();
    $mail->Host = MAIL_HOST;
    $mail->Port = MAIL_PORT;

    // authentication if needed
    if (!empty(MAIL_USERNAME) && !empty(MAIL_PASSWORD)) {
        $mail->SMTPAuth = true;
        $mail->Username = MAIL_USERNAME;
        $mail->Password = MAIL_PASSWORD;
    }

    // encryption if configured
    if (MAIL_ENCRYPTION === 'tls') {
        $mail->SMTPSecure = PHPMailer::ENCRYPTION_STARTTLS;
    } elseif (MAIL_ENCRYPTION === 'ssl') {
        $mail->SMTPSecure = PHPMailer::ENCRYPTION_SMTPS;
    }

    // set sender
    $mail->setFrom(MAIL_FROM_ADDRESS, MAIL_FROM_NAME);

    // charset
    $mail->CharSet = 'UTF-8';

    return $mail;
}
