<?php
/**
 * Logout handler — CSRF-protected session termination.
 *
 * Only processes POST requests carrying a valid CSRF token. A bare GET
 * request is silently redirected home without touching the session,
 * preventing forced-logout attacks via embedded images or links.
 *
 * @package TalentBridge
 */

session_start();
require_once 'includes/helpers.php';
require_once 'includes/csrf.php';
require_once 'includes/db.php';

// only act on post requests with a valid csrf token
if ($_SERVER['REQUEST_METHOD'] === 'POST') {

    if (!validateCsrfToken($_POST['csrf_token'] ?? '')) {
        http_response_code(403);
        exit('Invalid CSRF token.');
    }

    // if we know which user is logged in, mark them as offline in the
    // chat status column before destroying the session.
    if (isset($_SESSION['user_id']) && $_SESSION['user_id']) {
        try {
            $pdo  = getConnection();
            $stmt = $pdo->prepare("UPDATE users SET status = 'Offline now' WHERE user_id = :uid");
            $stmt->execute([':uid' => (int) $_SESSION['user_id']]);
        } catch (PDOException $e) {
            // logging out should still succeed even if this update fails
            error_log('Logout status update failed: ' . $e->getMessage());
        }
    }

    // overwrite all session variables before destroying
    $_SESSION = [];

    // remove the session cookie from the browser
    if (ini_get('session.use_cookies')) {
        $params = session_get_cookie_params();
        setcookie(
            session_name(),
            '',
            time() - 42000,
            $params['path'],
            $params['domain'],
            $params['secure'],
            $params['httponly']
        );
    }

    session_destroy();
}

// get requests (and post requests that fail csrf) are simply sent home
redirect('/index.php');
