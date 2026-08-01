<?php
/**
 * PDO database singleton.
 *
 * Provides a single shared PDO connection throughout the request lifecycle.
 * All callers use getConnection() rather than creating their own PDO instances.
 *
 * @package TalentBridge
 */

/**
 * Returns the shared PDO connection, creating it on first call.
 *
 * Configuration constants (DB_HOST, DB_NAME, DB_USER, DB_PASS) must be
 * defined before this function is called — typically via config.php.
 *
 * @return PDO  A configured PDO instance ready for prepared statements.
 * @throws PDOException  If the connection cannot be established.
 */
function getConnection(): PDO
{
    // hold the single pdo instance across calls
    static $pdo = null;

    if ($pdo !== null) {
        return $pdo;
    }

    // load credentials if not already defined
    $configPath = dirname(__DIR__) . '/config.php';
    if (file_exists($configPath)) {
        require_once $configPath;
    }

    $dsn = sprintf(
        'mysql:host=%s;dbname=%s;charset=utf8mb4',
        DB_HOST,
        DB_NAME
    );

    $options = [
        // throw exceptions on errors rather than silent failures
        PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
        // return rows as associative arrays by default
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        // disable emulated prepares for proper type safety
        PDO::ATTR_EMULATE_PREPARES   => false,
    ];

    $pdo = new PDO($dsn, DB_USER, DB_PASS, $options);

    // --- poor man's cron for login failure cleanup ---
    // on a 1% chance, clear out old IP-based login failure records
    // this prevents the login_failures table from growing indefinitely
    if (rand(1, 100) === 1) {
        try {
            // delete records older than the throttle window plus a small buffer
            $cleanupInterval = IP_THROTTLE_WINDOW + 600;
            $pdo->exec("DELETE FROM login_failures WHERE attempted_at < NOW() - INTERVAL {$cleanupInterval} SECOND");
        } catch (PDOException $e) {
            // fail silently. it's not critical if cleanup doesn't run on every attempt.
            error_log('Login failure cleanup task failed: ' . $e->getMessage());
        }
    }

    return $pdo;
}
