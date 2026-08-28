<?php
/**
 * Token helpers for email verification and password reset.
 *
 * Tokens are generated with random_bytes(), stored as SHA-256 hashes,
 * validated against expiry and used_at, and never persisted in plain text.
 *
 * @package TalentBridge
 */

/**
 * Generates a secure token and stores its hash.
 *
 * @param int    $userId
 * @param string $tokenType       'email_verification' or 'password_reset'
 * @param int    $expirySeconds   Token lifetime in seconds
 * @return string|null            Plain token for email link, or null on failure
 */
function generateAndStoreToken(int $userId, string $tokenType, int $expirySeconds): ?string
{
    if (!in_array($tokenType, ['email_verification', 'password_reset'], true)) {
        error_log("Invalid token type: {$tokenType}");
        return null;
    }

    try {
        $plainToken = bin2hex(random_bytes(32));
        $tokenHash = hash('sha256', $plainToken);
        $pdo = getConnection();

        if ($tokenType === 'email_verification') {
            $stmt = $pdo->prepare(
                "INSERT INTO email_verification_tokens (user_id, token_hash, expires_at)
                 VALUES (:user_id, :token_hash, FROM_UNIXTIME(UNIX_TIMESTAMP(NOW()) + :expiry_seconds))"
            );
        } else {
            $ipAddress = getIpAddress();
            $stmt = $pdo->prepare(
                "INSERT INTO password_reset_tokens (user_id, token_hash, expires_at, requested_ip)
                 VALUES (:user_id, :token_hash, FROM_UNIXTIME(UNIX_TIMESTAMP(NOW()) + :expiry_seconds), :requested_ip)"
            );
            $stmt->bindValue(':requested_ip', $ipAddress);
        }

        $stmt->bindValue(':user_id', $userId, PDO::PARAM_INT);
        $stmt->bindValue(':token_hash', $tokenHash);
        $stmt->bindValue(':expiry_seconds', $expirySeconds, PDO::PARAM_INT);
        $stmt->execute();

        return $plainToken;

    } catch (PDOException $e) {
        error_log("Token generation failed: {$e->getMessage()}");
        return null;
    }
}

/**
 * Validates a token and returns associated user_id.
 *
 * @param string $plainToken
 * @param string $tokenType       'email_verification' or 'password_reset'
 * @return int|null
 */
function validateToken(string $plainToken, string $tokenType): ?int
{
    if (!in_array($tokenType, ['email_verification', 'password_reset'], true)) {
        return null;
    }

    try {
        $tokenHash = hash('sha256', $plainToken);
        $pdo = getConnection();

        if ($tokenType === 'email_verification') {
            $stmt = $pdo->prepare(
                "SELECT user_id
                   FROM email_verification_tokens
                  WHERE token_hash = :token_hash
                    AND expires_at > NOW()
                    AND used_at IS NULL
                  LIMIT 1"
            );
        } else {
            $stmt = $pdo->prepare(
                "SELECT user_id
                   FROM password_reset_tokens
                  WHERE token_hash = :token_hash
                    AND expires_at > NOW()
                    AND used_at IS NULL
                  LIMIT 1"
            );
        }

        $stmt->execute([':token_hash' => $tokenHash]);
        $result = $stmt->fetch(PDO::FETCH_ASSOC);

        return $result ? (int) $result['user_id'] : null;

    } catch (PDOException $e) {
        error_log("Token validation failed: {$e->getMessage()}");
        return null;
    }
}

/**
 * Marks a token as used.
 *
 * @param string $plainToken
 * @param string $tokenType       'email_verification' or 'password_reset'
 * @return bool
 */
function consumeToken(string $plainToken, string $tokenType): bool
{
    if (!in_array($tokenType, ['email_verification', 'password_reset'], true)) {
        return false;
    }

    try {
        $tokenHash = hash('sha256', $plainToken);
        $pdo = getConnection();

        if ($tokenType === 'email_verification') {
            $stmt = $pdo->prepare(
                "UPDATE email_verification_tokens
                    SET used_at = NOW()
                  WHERE token_hash = :token_hash"
            );
        } else {
            $stmt = $pdo->prepare(
                "UPDATE password_reset_tokens
                    SET used_at = NOW()
                  WHERE token_hash = :token_hash"
            );
        }

        $stmt->execute([':token_hash' => $tokenHash]);
        return true;

    } catch (PDOException $e) {
        error_log("Token consumption failed: {$e->getMessage()}");
        return false;
    }
}

/**
 * Invalidates all unused tokens of a given type for a user.
 *
 * @param int    $userId
 * @param string $tokenType       'email_verification' or 'password_reset'
 * @return bool
 */
function invalidateAllTokens(int $userId, string $tokenType): bool
{
    if (!in_array($tokenType, ['email_verification', 'password_reset'], true)) {
        return false;
    }

    try {
        $pdo = getConnection();

        if ($tokenType === 'email_verification') {
            $stmt = $pdo->prepare(
                "UPDATE email_verification_tokens
                    SET used_at = NOW()
                  WHERE user_id = :user_id
                    AND used_at IS NULL"
            );
        } else {
            $stmt = $pdo->prepare(
                "UPDATE password_reset_tokens
                    SET used_at = NOW()
                  WHERE user_id = :user_id
                    AND used_at IS NULL"
            );
        }

        $stmt->execute([':user_id' => $userId]);
        return true;

    } catch (PDOException $e) {
        error_log("Token invalidation failed: {$e->getMessage()}");
        return false;
    }
}
