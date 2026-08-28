<?php
/**
 * Authentication helpers.
 *
 * Provides role-based access control guards used at the top of every
 * protected page. Depends on an active PHP session.
 *
 * @package TalentBridge
 */

/**
 * Checks whether a user is currently logged in.
 *
 * @return bool  True if a valid session with user_id exists.
 */
function isLoggedIn(): bool
{
    return isset($_SESSION['user_id']) && !empty($_SESSION['user_id']);
}

/**
 * Returns the role of the currently authenticated user.
 *
 * @return string|null  Role string ('seeker', 'employer', 'admin') or null if not logged in.
 */
function getUserRole(): ?string
{
    return $_SESSION['role'] ?? null;
}

/**
 * Enforces that the current visitor holds one of the permitted roles.
 *
 * If the visitor is not logged in, they are redirected to login.php.
 * If they are logged in but hold the wrong role, a 403 response is sent.
 *
 * @param string|string[] $roles  A single role string or array of acceptable roles.
 * @return void
 */
function requireRole($roles): void
{
    // normalise to array for uniform handling
    if (is_string($roles)) {
        $roles = [$roles];
    }

    if (!isLoggedIn()) {
        // preserve the intended destination so login can redirect back
        $intended = urlencode($_SERVER['REQUEST_URI'] ?? '');
        header('Location: /login.php?redirect=' . $intended);
        exit;
    }

    $currentRole = getUserRole();

    if (!in_array($currentRole, $roles, true)) {
        http_response_code(403);
        echo '<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"><title>403 Forbidden — TalentBridge</title>';
        echo '<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"></head>';
        echo '<body class="d-flex align-items-center justify-content-center" style="min-height:100vh">';
        echo '<div class="text-center"><h1 class="display-4">403</h1>';
        echo '<p class="lead">You do not have permission to access this page.</p>';
        echo '<a href="/index.php" class="btn btn-primary">Go Home</a></div></body></html>';
        exit;
    }

    // check if the account is active (suspended accounts cannot continue)
    if (isset($_SESSION['is_active']) && $_SESSION['is_active'] == 0) {
        session_destroy();
        header('Location: /login.php?error=suspended');
        exit;
    }
}
