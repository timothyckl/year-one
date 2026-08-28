<?php
/**
 * Login page — authenticates users and starts an authorised session.
 *
 * Uses password_verify() against the bcrypt hash, regenerates the session
 * ID on success to prevent fixation, and redirects each role to its
 * appropriate starting page.
 *
 * @package TalentBridge
 */

session_start();
require_once 'includes/helpers.php';
require_once 'includes/auth.php';
require_once 'includes/db.php';
require_once 'includes/csrf.php';

// already authenticated users are sent away
if (isLoggedIn()) {
    redirect('/index.php');
}

$error    = '';
$email    = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {

    // validate csrf token before touching any user input
    if (!validateCsrfToken($_POST['csrf_token'] ?? '')) {
        http_response_code(403);
        exit('Invalid CSRF token.');
    }

    $email    = trim($_POST['email']    ?? '');
    $password = $_POST['password'] ?? '';
    $ipAddress = getIpAddress();

    if (empty($email) || empty($password)) {
        $error = 'Please enter your email address and password.';

    } elseif (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        $error = 'Please enter a valid email address.';

    } else {
        try {
            $pdo  = getConnection();

            // --- IP-based throttling check ---
            $ipStmt = $pdo->prepare("
                SELECT COUNT(*) as fail_count, UNIX_TIMESTAMP(MAX(attempted_at)) as last_attempt_ts
                  FROM login_failures
                 WHERE ip_address = :ip
                   AND attempted_at > NOW() - INTERVAL :window SECOND
            ");
            $ipStmt->execute([':ip' => $ipAddress, ':window' => IP_THROTTLE_WINDOW]);
            $ipFailures = $ipStmt->fetch();

            if ($ipFailures && (int) $ipFailures['fail_count'] >= IP_THROTTLE_LIMIT) {
                // use the timestamp directly from the db, no strtotime
                $throttleExpiry = (int) $ipFailures['last_attempt_ts'] + IP_THROTTLE_DURATION;
                if (time() < $throttleExpiry) {
                    $remaining = ceil(($throttleExpiry - time()) / 60);
                    $error = "Too many failed login attempts from your IP address. Please try again in {$remaining} minute(s).";
                    throw new Exception($error); // will be caught by the generic catch block
                }
            }

            // fetch the user and their current login attempt status
            $stmt = $pdo->prepare("
                SELECT user_id, unique_id, name, password_hash, role, is_active, failed_login_attempts,
                       UNIX_TIMESTAMP(last_failed_login_at) as last_failed_login_timestamp,
                       lockout_count, email_verified_at
                  FROM users
                 WHERE email = :email
                 LIMIT 1
            ");
            $stmt->execute([':email' => $email]);
            $user = $stmt->fetch();

            // --- account lockout check ---
            if ($user && (int) $user['failed_login_attempts'] >= MAX_LOGIN_ATTEMPTS) {
                // determine which lockout duration to use based on the number of prior lockouts
                $lockoutIndex = max(0, (int) $user['lockout_count'] - 1);
                $lockoutIndex = min($lockoutIndex, count(PROGRESSIVE_LOCKOUTS) - 1); // cap at max duration
                $currentLockoutDuration = PROGRESSIVE_LOCKOUTS[$lockoutIndex];

                // use the timestamp directly from the db, no strtotime
                $lockoutExpiry = (int) $user['last_failed_login_timestamp'] + $currentLockoutDuration;

                if ($user['last_failed_login_timestamp'] && time() < $lockoutExpiry) {
                    $remaining = ceil(($lockoutExpiry - time()) / 60);
                    $error = "Your account is temporarily locked due to too many failed login attempts. Please try again in {$remaining} minute(s).";
                    log_audit_event('LOGIN_LOCKED', (int) $user['user_id']);
                    $user = false; // skip all further checks
                } else {
                    // lockout has expired; reset attempts AND lockout counter so the user can try again
                    $pdo->prepare("UPDATE users SET failed_login_attempts = 0, lockout_count = 0 WHERE user_id = :uid")
                        ->execute([':uid' => $user['user_id']]);
                    $user['failed_login_attempts'] = 0; // reflect change for the current script
                    $user['lockout_count']         = 0; // reflect change for the current script
                }
            }

            if ($user && password_verify($password, $user['password_hash'])) {
                // --- successful login ---
                if ($user['is_active'] == 0) {
                    $error = 'Your account has been suspended. Please contact support.';
                    log_audit_event('LOGIN_SUSPENDED', (int) $user['user_id']);
                } elseif (empty($user['email_verified_at'])) {
                    // account exists and password is correct, but email not verified yet
                    $error = 'Please verify your email address before logging in. Check your inbox for the verification link.';
                    log_audit_event('LOGIN_UNVERIFIED_EMAIL', (int) $user['user_id']);
                } else {
                    // reset failed attempts AND the progressive lockout counter,
                    // and mark the user as online in the chat status column.
                    $resetStmt = $pdo->prepare("
                        UPDATE users
                           SET failed_login_attempts = 0,
                               last_failed_login_at  = NULL,
                               lockout_count         = 0,
                               status                = 'Online now'
                         WHERE user_id = :uid
                    ");
                    $resetStmt->execute([':uid' => $user['user_id']]);

                    // successful authentication — regenerate session id to prevent fixation
                    session_regenerate_id(true);

                    $_SESSION['user_id']    = (int) $user['user_id'];
                    $_SESSION['unique_id']  = (int) $user['unique_id'];
                    $_SESSION['role']       = $user['role'];
                    $_SESSION['name']       = $user['name'];
                    $_SESSION['is_active']  = (int) $user['is_active'];

                    // log successful login
                    log_audit_event('LOGIN_SUCCESS', (int) $user['user_id']);

                    // honour any post-login redirect stored in the query string
                    $redirectTo = $_GET['redirect'] ?? '';

                    // only follow safe same-origin redirects
                    if (!empty($redirectTo) && str_starts_with($redirectTo, '/') && !str_starts_with($redirectTo, '//')) {
                        redirect($redirectTo);
                    }

                    // role-based default landing page
                    switch ($user['role']) {
                        case 'admin':
                            redirect('/admin/dashboard.php');
                            break;
                        case 'employer':
                            redirect('/employer/company_profile.php');
                            break;
                        default:
                            redirect('/seeker/profile.php');
                    }
                }
            } else {
                // --- failed login ---
                if ($user) { // only act if the user actually exists
                    $newAttemptCount = (int) $user['failed_login_attempts'] + 1;

                    $sql = "UPDATE users SET failed_login_attempts = :new_attempts, last_failed_login_at = NOW()";
                    $params = [':new_attempts' => $newAttemptCount, ':uid' => $user['user_id']];

                    // if this specific attempt triggers the lockout, increment the progressive counter
                    if ($newAttemptCount === MAX_LOGIN_ATTEMPTS) {
                        $sql .= ", lockout_count = lockout_count + 1";
                    }

                    $sql .= " WHERE user_id = :uid";
                    $updateStmt = $pdo->prepare($sql);
                    $updateStmt->execute($params);
                }

                // log the ip-based failure regardless of whether the user exists
                $logIpStmt = $pdo->prepare("
                    INSERT INTO login_failures (ip_address, email_attempted)
                    VALUES (:ip, :email)
                ");
                $logIpStmt->execute([':ip' => $ipAddress, ':email' => $email]);

                // log failed login audit event
                log_audit_event('LOGIN_FAILURE', $user ? (int) $user['user_id'] : null, ['email_attempt' => $email]);

                // if $error is not already set by the lockout logic, set the generic one
                if (empty($error)) {
                    $error = 'Incorrect email address or password.';
                }
            }

        } catch (PDOException $e) {
            $error = 'Login is temporarily unavailable. Please try again later.';
            error_log('Login PDOException: ' . $e->getMessage());
        } catch (Exception $e) {
            // this catches our custom exception for IP throttling
            // the $error message is already set, so we just log and let it render
            error_log('Login Exception: ' . $e->getMessage());
        }
    }
}

$flash     = getFlash();
$csrfToken = generateCsrfToken();
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Log In — TalentBridge</title>
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
                        <h1 class="h3 fw-bold mb-1" style="color:var(--tb-primary)">Log In</h1>
                        <div class="tb-divider"></div>

                        <?php if (!empty($flash['success'])): ?>
                            <div class="alert alert-success tb-flash" role="alert">
                                <?= sanitise($flash['success']) ?>
                            </div>
                        <?php endif; ?>

                        <?php if (!empty($_GET['error']) && $_GET['error'] === 'suspended'): ?>
                            <div class="alert alert-warning tb-flash" role="alert">
                                Your account has been suspended. Please contact support.
                            </div>
                        <?php endif; ?>

                        <?php if (!empty($error)): ?>
                            <div class="alert alert-danger tb-flash" role="alert">
                                <?= sanitise($error) ?>
                            </div>
                        <?php endif; ?>

                        <form id="loginForm" method="post" action="/login.php" novalidate>
                            <input type="hidden" name="csrf_token" value="<?= sanitise($csrfToken) ?>">

                            <!-- email -->
                            <div class="mb-3">
                                <label for="email" class="form-label">Email Address</label>
                                <input type="email" id="email" name="email"
                                    class="form-control"
                                    value="<?= sanitise($email) ?>"
                                    required autocomplete="email">
                            </div>

                            <!-- password -->
                            <div class="mb-4">
                                <label for="password" class="form-label">Password</label>
                                <input type="password" id="password" name="password"
                                    class="form-control"
                                    required autocomplete="current-password">
                            </div>

                            <button type="submit" class="btn btn-primary w-100 py-2 fw-bold">
                                Log In
                            </button>
                        </form>

                        <p class="text-center text-muted mt-3 mb-0 small">
                            Don't have an account? <a href="/register.php">Register here</a> • <a href="/forgot-password.php">Forgot password?</a>
                        </p>
                    </div>

                </div>
            </div>
        </div>
    </section>
</main>

<?php require_once 'includes/footer.php'; ?>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="assets/js/validation.js"></script>
<script>
    document.getElementById('loginForm').addEventListener('submit', function (e) {
        if (!validateForm(this)) {
            e.preventDefault();
        }
    });
</script>
</body>
</html>
