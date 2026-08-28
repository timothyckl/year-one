<?php
/**
 * Registration page — new seeker or employer account.
 *
 * Role is toggled via a JS-driven tab; the server re-validates the chosen
 * role. On success, inserts a row into users then seeker_profiles or
 * companies, then redirects to the appropriate dashboard.
 *
 * @package TalentBridge
 */

session_start();
require_once 'includes/helpers.php';
require_once 'includes/auth.php';
require_once 'includes/db.php';
require_once 'includes/csrf.php';
require_once 'includes/mailer.php';
require_once 'includes/tokens.php';

// already logged-in users have no business here
if (isLoggedIn()) {
    redirect('/index.php');
}

$errors   = [];
$formData = [
    'name'         => '',
    'email'        => '',
    'role'         => $_GET['role'] ?? 'seeker',
    'company_name' => '',
    'industry'     => '',
];

// industry options used in the select and for server-side whitelist
$industryOptions = [
    'Technology', 'Finance', 'Healthcare', 'Education', 'Retail',
    'Manufacturing', 'Logistics', 'Media', 'Hospitality', 'Consulting', 'Other',
];

$validRoles = ['seeker', 'employer'];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {

    // validate csrf token first
    if (!validateCsrfToken($_POST['csrf_token'] ?? '')) {
        http_response_code(403);
        exit('Invalid CSRF token.');
    }

    // collect raw input
    $formData['name']         = trim($_POST['name']         ?? '');
    $formData['email']        = trim($_POST['email']        ?? '');
    $formData['role']         = trim($_POST['role']         ?? 'seeker');
    $formData['company_name'] = trim($_POST['company_name'] ?? '');
    $formData['industry']     = trim($_POST['industry']     ?? '');
    $password                 = $_POST['password']          ?? '';
    $passwordConfirm          = $_POST['password_confirm']  ?? '';

    // ---- validate common fields ----
    if (empty($formData['name'])) {
        $errors['name'] = 'Full name is required.';
    } elseif (strlen($formData['name']) > 100) {
        $errors['name'] = 'Name must not exceed 100 characters.';
    }

    if (empty($formData['email'])) {
        $errors['email'] = 'Email address is required.';
    } elseif (!filter_var($formData['email'], FILTER_VALIDATE_EMAIL)) {
        $errors['email'] = 'Please enter a valid email address.';
    }

    if (!in_array($formData['role'], $validRoles, true)) {
        $errors['role'] = 'Please select a valid role.';
    }

    if (empty($password)) {
        $errors['password'] = 'Password is required.';
    } elseif (strlen($password) < 8) {
        $errors['password'] = 'Password must be at least 8 characters.';
    }

    if ($password !== $passwordConfirm) {
        $errors['password_confirm'] = 'Passwords do not match.';
    }

    // ---- employer-specific validation ----
    if ($formData['role'] === 'employer') {
        if (empty($formData['company_name'])) {
            $errors['company_name'] = 'Company name is required.';
        } elseif (strlen($formData['company_name']) > 200) {
            $errors['company_name'] = 'Company name must not exceed 200 characters.';
        }

        if (!empty($formData['industry']) && !in_array($formData['industry'], $industryOptions, true)) {
            $errors['industry'] = 'Please select a valid industry.';
        }
    }

    // ---- check email is not already taken ----
    if (empty($errors)) {
        try {
            $pdo   = getConnection();
            $check = $pdo->prepare("SELECT user_id FROM users WHERE email = :email LIMIT 1");
            $check->execute([':email' => $formData['email']]);
            if ($check->fetch()) {
                $errors['email'] = 'This email address is already registered.';
            }
        } catch (PDOException $e) {
            $errors['db'] = 'Registration is temporarily unavailable. Please try again later.';
        }
    }

    // ---- insert new user and profile ----
    if (empty($errors)) {
        try {
            $pdo = getConnection();
            $pdo->beginTransaction();

            // hash the password with bcrypt
            $hash = password_hash($password, PASSWORD_BCRYPT);

            // generate a ChatApp-style unique_id for this user. We use a large random
            // integer and perform a quick uniqueness check to avoid collisions.
            do {
                $uniqueId = random_int(100000000, 999999999);
                $checkUid = $pdo->prepare("SELECT user_id FROM users WHERE unique_id = :uid LIMIT 1");
                $checkUid->execute([':uid' => $uniqueId]);
                $exists = $checkUid->fetch();
            } while ($exists);

            $userStmt = $pdo->prepare("
                INSERT INTO users (name, email, password_hash, role, unique_id, status)
                VALUES (:name, :email, :hash, :role, :unique_id, 'Offline now')
            ");
            $userStmt->execute([
                ':name'      => $formData['name'],
                ':email'     => $formData['email'],
                ':hash'      => $hash,
                ':role'      => $formData['role'],
                ':unique_id' => $uniqueId,
            ]);
            $newUserId = (int) $pdo->lastInsertId();

            if ($formData['role'] === 'seeker') {
                // create empty seeker profile row
                $profileStmt = $pdo->prepare("
                    INSERT INTO seeker_profiles (user_id) VALUES (:user_id)
                ");
                $profileStmt->execute([':user_id' => $newUserId]);

            } else {
                // create company profile row
                $companyStmt = $pdo->prepare("
                    INSERT INTO companies (user_id, company_name, industry)
                    VALUES (:user_id, :company_name, :industry)
                ");
                $companyStmt->execute([
                    ':user_id'      => $newUserId,
                    ':company_name' => $formData['company_name'],
                    ':industry'     => $formData['industry'] ?: null,
                ]);
            }

            $pdo->commit();

            // Generate email verification token and send verification email
            $verificationToken = generateAndStoreToken(
                $newUserId,
                'email_verification',
                EMAIL_VERIFICATION_TOKEN_EXPIRY
            );

            if ($verificationToken) {
                sendVerificationEmail($formData['email'], $formData['name'], $verificationToken);
                log_audit_event('REGISTRATION_COMPLETE', $newUserId, ['role' => $formData['role']]);
            } else {
                // token generation failed, but don't block registration
                error_log("Failed to generate verification token for user {$newUserId}");
            }

            // Registration successful. Set a flash message and redirect to login.
            setFlash('success', 'Registration successful. Please check your email to verify your account before logging in.');
            redirect('/login.php');

        } catch (PDOException $e) {
            if ($pdo->inTransaction()) {
                $pdo->rollBack();
            }
            $errors['db'] = 'Registration failed. Please try again later.';
        }
    }
}

$csrfToken = generateCsrfToken();
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register — TalentBridge</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="assets/css/style.css" rel="stylesheet">
</head>
<body>

<?php require_once 'includes/nav.php'; ?>

<main id="main-content">
    <section class="tb-section">
        <div class="container">
            <div class="row justify-content-center">
                <div class="col-md-8 col-lg-6">

                    <div class="tb-card">
                        <h1 class="h3 fw-bold mb-1" style="color:var(--tb-primary)">Create an Account</h1>
                        <div class="tb-divider"></div>

                        <?php if (!empty($errors['db'])): ?>
                            <div class="alert alert-danger tb-flash" role="alert">
                                <?= sanitise($errors['db']) ?>
                            </div>
                        <?php endif; ?>

                        <!-- role selector tabs -->
                        <div class="mb-4">
                            <p class="form-label mb-2">I am a…</p>
                            <div class="d-flex gap-2" role="group" aria-label="Account type selection">
                                <button type="button" id="btnSeeker"
                                    class="btn <?= $formData['role'] === 'seeker' ? 'btn-primary' : 'btn-outline-primary' ?> flex-fill"
                                    aria-pressed="<?= $formData['role'] === 'seeker' ? 'true' : 'false' ?>"
                                    onclick="switchRole('seeker')">
                                    Job Seeker
                                </button>
                                <button type="button" id="btnEmployer"
                                    class="btn <?= $formData['role'] === 'employer' ? 'btn-primary' : 'btn-outline-primary' ?> flex-fill"
                                    aria-pressed="<?= $formData['role'] === 'employer' ? 'true' : 'false' ?>"
                                    onclick="switchRole('employer')">
                                    Employer
                                </button>
                            </div>
                        </div>

                        <p class="text-muted small mb-3">Fields marked <span aria-hidden="true">*</span><span class="visually-hidden">with an asterisk</span> are required.</p>

                        <form id="registerForm" method="post" action="/register.php" novalidate>
                            <input type="hidden" name="csrf_token" value="<?= sanitise($csrfToken) ?>">
                            <input type="hidden" name="role" id="roleInput" value="<?= sanitise($formData['role']) ?>">

                            <!-- name -->
                            <div class="mb-3">
                                <label for="name" class="form-label">Full Name <span aria-hidden="true" class="text-danger">*</span></label>
                                <input type="text" id="name" name="name"
                                    class="form-control <?= !empty($errors['name']) ? 'is-invalid' : '' ?>"
                                    value="<?= sanitise($formData['name']) ?>"
                                    required maxlength="100" autocomplete="name"
                                    <?= !empty($errors['name']) ? 'aria-describedby="name_error" aria-invalid="true"' : '' ?>>
                                <?php if (!empty($errors['name'])): ?>
                                    <div id="name_error" class="field-error invalid-feedback d-block" role="alert"><?= sanitise($errors['name']) ?></div>
                                <?php endif; ?>
                            </div>

                            <!-- email -->
                            <div class="mb-3">
                                <label for="email" class="form-label">Email Address <span aria-hidden="true" class="text-danger">*</span></label>
                                <input type="email" id="email" name="email"
                                    class="form-control <?= !empty($errors['email']) ? 'is-invalid' : '' ?>"
                                    value="<?= sanitise($formData['email']) ?>"
                                    required maxlength="255" autocomplete="email"
                                    <?= !empty($errors['email']) ? 'aria-describedby="email_error" aria-invalid="true"' : '' ?>>
                                <?php if (!empty($errors['email'])): ?>
                                    <div id="email_error" class="field-error invalid-feedback d-block" role="alert"><?= sanitise($errors['email']) ?></div>
                                <?php endif; ?>
                            </div>

                            <!-- employer-only fields -->
                            <div id="employerFields" style="display:<?= $formData['role'] === 'employer' ? 'block' : 'none' ?>">
                                <div class="mb-3">
                                    <label for="company_name" class="form-label">Company Name <span aria-hidden="true" class="text-danger">*</span></label>
                                    <input type="text" id="company_name" name="company_name"
                                        class="form-control <?= !empty($errors['company_name']) ? 'is-invalid' : '' ?>"
                                        value="<?= sanitise($formData['company_name']) ?>"
                                        maxlength="200"
                                        <?= !empty($errors['company_name']) ? 'aria-describedby="company_name_error" aria-invalid="true"' : '' ?>>
                                    <?php if (!empty($errors['company_name'])): ?>
                                        <div id="company_name_error" class="field-error invalid-feedback d-block" role="alert"><?= sanitise($errors['company_name']) ?></div>
                                    <?php endif; ?>
                                </div>
                                <div class="mb-3">
                                    <label for="industry" class="form-label">Industry</label>
                                    <select id="industry" name="industry" class="form-select">
                                        <option value="">— Select industry —</option>
                                        <?php foreach ($industryOptions as $ind): ?>
                                            <option value="<?= sanitise($ind) ?>"
                                                <?= $formData['industry'] === $ind ? 'selected' : '' ?>>
                                                <?= sanitise($ind) ?>
                                            </option>
                                        <?php endforeach; ?>
                                    </select>
                                </div>
                            </div>

                            <!-- password -->
                            <div class="mb-3">
                                <label for="password" class="form-label">Password <span aria-hidden="true" class="text-danger">*</span></label>
                                <input type="password" id="password" name="password"
                                    class="form-control <?= !empty($errors['password']) ? 'is-invalid' : '' ?>"
                                    required autocomplete="new-password"
                                    data-minlength="8"
                                    <?= !empty($errors['password']) ? 'aria-describedby="password_error" aria-invalid="true"' : '' ?>>
                                <?php if (!empty($errors['password'])): ?>
                                    <div id="password_error" class="field-error invalid-feedback d-block" role="alert"><?= sanitise($errors['password']) ?></div>
                                <?php endif; ?>
                                <div class="form-text">Must be at least 8 characters.</div>
                            </div>

                            <!-- confirm password -->
                            <div class="mb-4">
                                <label for="password_confirm" class="form-label">Confirm Password <span aria-hidden="true" class="text-danger">*</span></label>
                                <input type="password" id="password_confirm" name="password_confirm"
                                    class="form-control <?= !empty($errors['password_confirm']) ? 'is-invalid' : '' ?>"
                                    required autocomplete="new-password"
                                    aria-describedby="password_confirm_hint<?= !empty($errors['password_confirm']) ? ' password_confirm_error' : '' ?>"
                                    <?= !empty($errors['password_confirm']) ? 'aria-invalid="true"' : '' ?>>
                                <div id="password_confirm_hint" class="form-text">Must match the password above.</div>
                                <?php if (!empty($errors['password_confirm'])): ?>
                                    <div id="password_confirm_error" class="field-error invalid-feedback d-block" role="alert"><?= sanitise($errors['password_confirm']) ?></div>
                                <?php endif; ?>
                            </div>

                            <button type="submit" class="btn btn-primary w-100 py-2 fw-bold">
                                Create Account
                            </button>
                        </form>

                        <p class="text-center text-muted mt-3 mb-0 small">
                            Already have an account? <a href="/login.php">Log in here</a>
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
    /**
     * Switches the visible role section and updates the hidden role input.
     *
     * @param {string} role - Either 'seeker' or 'employer'.
     */
    function switchRole(role) {
        document.getElementById('roleInput').value = role;

        // toggle employer-only fields
        document.getElementById('employerFields').style.display =
            role === 'employer' ? 'block' : 'none';

        // make employer fields required only when visible
        const companyInput = document.getElementById('company_name');
        companyInput.required = (role === 'employer');

        // update button appearance and pressed state
        document.getElementById('btnSeeker').className =
            'btn flex-fill ' + (role === 'seeker' ? 'btn-primary' : 'btn-outline-primary');
        document.getElementById('btnEmployer').className =
            'btn flex-fill ' + (role === 'employer' ? 'btn-primary' : 'btn-outline-primary');
        document.getElementById('btnSeeker').setAttribute('aria-pressed', role === 'seeker' ? 'true' : 'false');
        document.getElementById('btnEmployer').setAttribute('aria-pressed', role === 'employer' ? 'true' : 'false');
    }

    // initialise required state based on the server-side role value
    switchRole(document.getElementById('roleInput').value);

    // client-side validation on submit
    document.getElementById('registerForm').addEventListener('submit', function (e) {
        if (!validateForm(this)) {
            e.preventDefault();
            return;
        }

        // additional password match check
        const pw  = document.getElementById('password');
        const pwc = document.getElementById('password_confirm');
        if (pw.value !== pwc.value) {
            e.preventDefault();
            showError(pwc, 'Passwords do not match.');
        }
    });
</script>
</body>
</html>
