<?php
/**
 * General-purpose helper functions.
 *
 * Provides output sanitisation, HTTP redirects, and session-based flash
 * messages used throughout every page in the application.
 *
 * @package TalentBridge
 */

/**
 * Sanitises a string for safe HTML output.
 *
 * Wraps htmlspecialchars() with ENT_QUOTES to encode both single and double
 * quotes, preventing XSS in attribute values as well as element content.
 *
 * @param string|null $value  The raw string to sanitise.
 * @return string  The HTML-safe escaped string.
 */
function sanitise(?string $value): string
{
    if ($value === null) {
        return '';
    }

    return htmlspecialchars($value, ENT_QUOTES, 'UTF-8');
}

/**
 * Formats salary DB columns into a human-readable display string.
 *
 * Combines the minimum salary, optional maximum, and rate period into a
 * single formatted string. Returns an empty string when no salary is set.
 * PDO returns DECIMAL columns as strings, so parameters are typed as nullable strings.
 *
 * @param string|null $min    - The minimum (or sole) salary value from the DB.
 * @param string|null $max    - The maximum salary value, or null for a single figure.
 * @param string|null $period - The rate period (e.g. 'per month').
 * @return string  The formatted salary string, or an empty string if min is absent.
 */
function formatSalary(?string $min, ?string $max, ?string $period): string
{
    if ($min === null || $min === '') {
        return '';
    }
    // format as integer display — cents are not meaningful for salary ranges
    $result = '$' . number_format((float) $min, 0);
    if ($max !== null && $max !== '') {
        $result .= ' – $' . number_format((float) $max, 0);
    }
    if ($period) {
        $result .= ' ' . $period;
    }
    return $result;
}

/**
 * Sends a Location redirect header and terminates the script.
 *
 * @param string $url   The URL to redirect to. Should be an absolute path.
 * @param int    $code  The HTTP status code to use (default 302 Found).
 * @return void
 */
function redirect(string $url, int $code = 302): void
{
    http_response_code($code);
    header('Location: ' . $url);
    exit;
}

/**
 * Stores a flash message in the session for display on the next request.
 *
 * Messages are stored under $_SESSION['flash'] keyed by type so that
 * multiple concurrent message types can coexist (e.g. 'success' and 'error').
 *
 * @param string $type     The message category, e.g. 'success', 'error', 'warning'.
 * @param string $message  The human-readable message text.
 * @return void
 */
function setFlash(string $type, string $message): void
{
    $_SESSION['flash'][$type] = $message;
}

/**
 * Retrieves and removes all flash messages from the session.
 *
 * Each flash message is consumed exactly once; subsequent calls return an
 * empty array until new messages are set.
 *
 * @return array<string, string>  Associative array of type => message strings.
 */
function getFlash(): array
{
    $messages = $_SESSION['flash'] ?? [];
    unset($_SESSION['flash']);
    return $messages;
}

/**
 * Returns the visitor's IP address.
 *
 * A simple wrapper around $_SERVER['REMOTE_ADDR'] for consistency.
 *
 * @return string The visitor's IP address.
 */
function getIpAddress(): string
{
    return isset($_SERVER['REMOTE_ADDR']) ? $_SERVER['REMOTE_ADDR'] : '0.0.0.0';
}

/**
 * Logs a security-sensitive event to the audit log.
 *
 * @param string  The action code being logged (e.g., 'LOGIN_SUCCESS').
 * @param ?int  The ID of the user performing the action, if available.
 * @param array  Additional context for the event, to be stored as JSON.
 * @return void
 */
function log_audit_event(string $action, ?int $userId = null, array $details = []): void
{
    try {
        $pdoconn = getConnection(); // Get PDO instance from the helper
        $ipAddress = getIpAddress();
        
        // gracefully handle missing json extension
        $detailsJson = null;
        if (count($details) > 0 && function_exists('json_encode')) {
            $detailsJson = json_encode($details);
        }

        $sql = "INSERT INTO audit_log (user_id, ip_address, action, details) VALUES (:user_id, :ip_address, :action, :details)";

        $stmt = $pdoconn->prepare($sql);
        $stmt->execute([
            ':user_id' => $userId,
            ':ip_address' => $ipAddress,
            ':action' => $action,
            ':details' => $detailsJson,
        ]);
    } catch (PDOException $e) {
        // In a real-world application, you might want to log this error
        // to a file instead of failing silently, especially if the audit
        // log itself fails.
        // For now, we will suppress the error to avoid breaking the user's
        // experience if the logging fails.
        error_log('Audit log failed: ' . $e->getMessage());
    }
}
