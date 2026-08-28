<?php
/**
 * CSRF token helpers.
 *
 * Generates and validates synchroniser tokens for all state-changing POST
 * requests. Tokens are stored in the session and compared with
 * hash_equals() to prevent timing attacks.
 *
 * @package TalentBridge
 */

/**
 * Generates a cryptographically secure CSRF token and stores it in the session.
 *
 * If a token already exists in the session, the same token is returned so that
 * multiple forms on the same page all share one token per session.
 *
 * @return string  A 64-character hexadecimal token.
 */
function generateCsrfToken(): string
{
    if (empty($_SESSION['csrf_token'])) {
        // random_bytes provides a cryptographically secure source of entropy
        $_SESSION['csrf_token'] = bin2hex(random_bytes(32));
    }

    return $_SESSION['csrf_token'];
}

/**
 * Validates a CSRF token supplied by the client against the session token.
 *
 * Uses hash_equals() to avoid timing side-channel attacks.
 *
 * @param string $token  The token submitted by the client (e.g. $_POST['csrf_token']).
 * @return bool  True if the token matches the stored session token.
 */
function validateCsrfToken(string $token): bool
{
    $sessionToken = $_SESSION['csrf_token'] ?? '';

    // both strings must be non-empty before comparison
    if (empty($token) || empty($sessionToken)) {
        return false;
    }

    return hash_equals($sessionToken, $token);
}
