<?php

/**
 * Contact page — public enquiry form.
 *
 * Security controls applied:
 *   - CSRF token validation before any input is processed
 *   - Server-side validation for all fields (required, length, format)
 *   - Name field restricted to letters, spaces, hyphens and apostrophes only
 *   - Email validated with filter_var(FILTER_VALIDATE_EMAIL)
 *   - All output passed through sanitise() (htmlspecialchars) to prevent XSS
 *   - PDO prepared statements to prevent SQL injection
 *   - Honeypot field to block basic bots
 *   - POST-redirect-GET pattern to prevent duplicate submissions on refresh
 *
 * @package TalentBridge
 */

session_start();
require_once 'includes/helpers.php';
require_once 'includes/auth.php';
require_once 'includes/db.php';
require_once 'includes/csrf.php';

$errors   = [];
$formData = ['name' => '', 'email' => '', 'subject' => '', 'body' => ''];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {

    // ---- 1. CSRF guard ----
    if (!validateCsrfToken($_POST['csrf_token'] ?? '')) {
        http_response_code(403);
        exit('Invalid CSRF token.');
    }

    // ---- 2. Honeypot — bots fill hidden fields, humans don't ----
    if (!empty($_POST['website'])) {
        // silently redirect so bots don't know they were blocked
        redirect('/contact.php');
    }

    // ---- 3. Collect raw input (trim only — sanitise() used only at output) ----
    $formData['name']    = trim($_POST['name']    ?? '');
    $formData['email']   = trim($_POST['email']   ?? '');
    $formData['subject'] = trim($_POST['subject'] ?? '');
    $formData['body']    = trim($_POST['body']    ?? '');

    // ---- 4. Server-side validation ----

    // Name: required, max length, letters/spaces/hyphens/apostrophes only
    if (empty($formData['name'])) {
        $errors['name'] = 'Your name is required.';
    } elseif (strlen($formData['name']) > 100) {
        $errors['name'] = 'Name must not exceed 100 characters.';
    } elseif (!preg_match("/^[\p{L}\s'\-]+$/u", $formData['name'])) {
        $errors['name'] = 'Name may only contain letters, spaces, hyphens, and apostrophes.';
    }

    // Email: required, valid format
    if (empty($formData['email'])) {
        $errors['email'] = 'Your email address is required.';
    } elseif (!filter_var($formData['email'], FILTER_VALIDATE_EMAIL)) {
        $errors['email'] = 'Please enter a valid email address.';
    } elseif (strlen($formData['email']) > 255) {
        $errors['email'] = 'Email address must not exceed 255 characters.';
    }

    // Subject: required, max length
    if (empty($formData['subject'])) {
        $errors['subject'] = 'A subject is required.';
    } elseif (strlen($formData['subject']) > 255) {
        $errors['subject'] = 'Subject must not exceed 255 characters.';
    }

    // Body: required, min and max length
    if (empty($formData['body'])) {
        $errors['body'] = 'A message body is required.';
    } elseif (strlen($formData['body']) < 10) {
        $errors['body'] = 'Message must be at least 10 characters.';
    } elseif (strlen($formData['body']) > 2000) {
        $errors['body'] = 'Message must not exceed 2,000 characters.';
    }

    // ---- 5. Insert only if all validation passes ----
    if (empty($errors)) {
        try {
            $pdo  = getConnection();
            // Prepared statement — input is bound as parameters, never interpolated
            // into the query string, fully preventing SQL injection
            $stmt = $pdo->prepare("
                INSERT INTO contact_messages (name, email, subject, body)
                VALUES (:name, :email, :subject, :body)
            ");
            $stmt->execute([
                ':name'    => $formData['name'],
                ':email'   => $formData['email'],
                ':subject' => $formData['subject'],
                ':body'    => $formData['body'],
            ]);

            setFlash('success', 'Thank you for your message! We will be in touch within two business days.');
            // POST-redirect-GET: redirect prevents duplicate submission on refresh
            redirect('/contact.php');

        } catch (PDOException $e) {
            $errors['db'] = 'Sorry, we could not send your message at this time. Please try again later.';
            error_log('Contact form DB error: ' . $e->getMessage());
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
    <title>Contact Us — TalentBridge</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="assets/css/style.css" rel="stylesheet">
</head>

<body>

    <?php require_once 'includes/nav.php'; ?>

    <main id="main-content">

        <!-- page header -->
        <section class="tb-hero py-5" aria-label="Contact page header">
            <div class="container text-center">
                <h1 class="fw-bold mb-3">Get in Touch</h1>
                <p class="lead mb-0" style="max-width:550px;margin:0 auto;">
                    Questions, partnership enquiries, or feedback — we'd love to hear from you.
                </p>
            </div>
        </section>

        <!-- contact content -->
        <section class="tb-section" aria-labelledby="contact-heading">
            <div class="container">
                <h2 id="contact-heading" class="visually-hidden">Contact form and office details</h2>

                <div class="row g-5 justify-content-center">

                    <!-- contact form -->
                    <div class="col-lg-7">
                        <div class="tb-card">
                            <h2 class="h4 fw-bold mb-1" style="color:var(--tb-primary)">Send Us a Message</h2>
                            <div class="tb-divider"></div>

                            <!-- flash success message -->
                            <?php if (!empty($flash['success'])): ?>
                                <div class="alert alert-success tb-flash" role="alert">
                                    <?= sanitise($flash['success']) ?>
                                </div>
                            <?php endif; ?>

                            <!-- db error -->
                            <?php if (!empty($errors['db'])): ?>
                                <div class="alert alert-danger tb-flash" role="alert">
                                    <?= sanitise($errors['db']) ?>
                                </div>
                            <?php endif; ?>

                            <p class="text-muted small mb-3">Fields marked <span aria-hidden="true">*</span><span class="visually-hidden">with an asterisk</span> are required.</p>

                            <form
                                id="contactForm"
                                method="post"
                                action="/contact.php"
                                novalidate
                                aria-label="Contact enquiry form">

                                <!-- CSRF token — validated server-side before any processing -->
                                <input type="hidden" name="csrf_token" value="<?= sanitise($csrfToken) ?>">

                                <!-- Honeypot — hidden from real users via CSS, bots fill it in -->
                                <div style="display:none" aria-hidden="true">
                                    <label for="website">Leave this blank</label>
                                    <input type="text" id="website" name="website" tabindex="-1" autocomplete="off">
                                </div>

                                <!-- name -->
                                <div class="mb-3">
                                    <label for="name" class="form-label">
                                        Full Name <span aria-hidden="true" class="text-danger">*</span>
                                    </label>
                                    <input
                                        type="text"
                                        id="name"
                                        name="name"
                                        class="form-control <?= !empty($errors['name']) ? 'is-invalid' : '' ?>"
                                        value="<?= sanitise($formData['name']) ?>"
                                        required
                                        maxlength="100"
                                        autocomplete="name"
                                        data-validate-name="true"
                                        <?= !empty($errors['name']) ? 'aria-describedby="name_error" aria-invalid="true"' : '' ?>>
                                    <?php if (!empty($errors['name'])): ?>
                                        <div id="name_error" class="field-error invalid-feedback d-block" role="alert">
                                            <?= sanitise($errors['name']) ?>
                                        </div>
                                    <?php endif; ?>
                                    <div class="form-text text-muted small">Letters, spaces, hyphens and apostrophes only.</div>
                                </div>

                                <!-- email -->
                                <div class="mb-3">
                                    <label for="email" class="form-label">
                                        Email Address <span aria-hidden="true" class="text-danger">*</span>
                                    </label>
                                    <input
                                        type="email"
                                        id="email"
                                        name="email"
                                        class="form-control <?= !empty($errors['email']) ? 'is-invalid' : '' ?>"
                                        value="<?= sanitise($formData['email']) ?>"
                                        required
                                        maxlength="255"
                                        autocomplete="email"
                                        <?= !empty($errors['email']) ? 'aria-describedby="email_error" aria-invalid="true"' : '' ?>>
                                    <?php if (!empty($errors['email'])): ?>
                                        <div id="email_error" class="field-error invalid-feedback d-block" role="alert">
                                            <?= sanitise($errors['email']) ?>
                                        </div>
                                    <?php endif; ?>
                                </div>

                                <!-- subject -->
                                <div class="mb-3">
                                    <label for="subject" class="form-label">
                                        Subject <span aria-hidden="true" class="text-danger">*</span>
                                    </label>
                                    <input
                                        type="text"
                                        id="subject"
                                        name="subject"
                                        class="form-control <?= !empty($errors['subject']) ? 'is-invalid' : '' ?>"
                                        value="<?= sanitise($formData['subject']) ?>"
                                        required
                                        maxlength="255"
                                        <?= !empty($errors['subject']) ? 'aria-describedby="subject_error" aria-invalid="true"' : '' ?>>
                                    <?php if (!empty($errors['subject'])): ?>
                                        <div id="subject_error" class="field-error invalid-feedback d-block" role="alert">
                                            <?= sanitise($errors['subject']) ?>
                                        </div>
                                    <?php endif; ?>
                                </div>

                                <!-- message body -->
                                <div class="mb-4">
                                    <label for="body" class="form-label">
                                        Message <span aria-hidden="true" class="text-danger">*</span>
                                    </label>
                                    <textarea
                                        id="body"
                                        name="body"
                                        class="form-control <?= !empty($errors['body']) ? 'is-invalid' : '' ?>"
                                        rows="6"
                                        required
                                        data-minlength="10"
                                        data-maxlength="2000"
                                        <?= !empty($errors['body']) ? 'aria-describedby="body_error" aria-invalid="true"' : '' ?>><?= sanitise($formData['body']) ?></textarea>
                                    <?php if (!empty($errors['body'])): ?>
                                        <div id="body_error" class="field-error invalid-feedback d-block" role="alert">
                                            <?= sanitise($errors['body']) ?>
                                        </div>
                                    <?php endif; ?>
                                    <div class="char-count" id="bodyCount" aria-live="polite"></div>
                                </div>

                                <button type="submit" class="btn btn-primary px-4">
                                    Send Message
                                </button>
                            </form>
                        </div>
                    </div>

                    <!-- office info sidebar -->
                    <div class="col-lg-4 col-xl-3">
                        <div class="tb-card mb-4">
                            <h3 class="h6 fw-bold mb-3" style="color:var(--tb-primary)">Our Office</h3>
                            <address class="text-muted small mb-0" style="font-style:normal;line-height:1.8;">
                                TalentBridge Pte. Ltd.<br>
                                1 Fusionopolis Way, #13-01<br>
                                Connexis South Tower<br>
                                Singapore 138632
                            </address>
                        </div>
                        <div class="tb-card mb-4">
                            <h3 class="h6 fw-bold mb-3" style="color:var(--tb-primary)">Business Hours</h3>
                            <p class="text-muted small mb-0" style="line-height:1.8;">
                                Monday – Friday<br>
                                9:00 am – 6:00 pm SGT<br><br>
                                We aim to respond to all enquiries within two business days.
                            </p>
                        </div>
                        <div class="tb-card">
                            <h3 class="h6 fw-bold mb-3" style="color:var(--tb-primary)">Email Us</h3>
                            <p class="text-muted small mb-0">
                                <a href="mailto:hello@talentbridge.sg">hello@talentbridge.sg</a>
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
    (function () {
        const form    = document.getElementById('contactForm');
        const nameEl  = document.getElementById('name');
        const bodyEl  = document.getElementById('body');
        const countEl = document.getElementById('bodyCount');
        const maxLen  = 2000;

        // ---- character counter ----
        function updateCount() {
            const remaining = maxLen - bodyEl.value.length;
            countEl.textContent = remaining + ' / ' + maxLen + ' characters remaining';
            countEl.className = 'char-count ' + (
                remaining < 0   ? 'count-danger' :
                remaining < 200 ? 'count-warn'   :
                                  'count-ok'
            );
        }
        bodyEl.addEventListener('input', updateCount);
        updateCount();

        // ---- name field: block digits and special chars in real time ----
        nameEl.addEventListener('input', function () {
            // Unicode letter categories + allowed punctuation
            const namePattern = /^[\p{L}\s'\-]*$/u;
            if (!namePattern.test(nameEl.value)) {
                // strip the offending character immediately
                nameEl.value = nameEl.value.replace(/[^\p{L}\s'\-]/gu, '');
                showError(nameEl, 'Name may only contain letters, spaces, hyphens, and apostrophes.');
            } else {
                // clear the error once input is clean
                const existingErr = document.getElementById('name_error');
                if (existingErr) existingErr.remove();
                nameEl.classList.remove('is-invalid');
                nameEl.removeAttribute('aria-invalid');
            }
        });

        // ---- form submit ----
        form.addEventListener('submit', function (e) {
            // run the shared validateForm() from validation.js first
            if (!validateForm(form)) {
                e.preventDefault();
                return;
            }

            // extra name check in case the regex wasn't caught above
            const namePattern = /^[\p{L}\s'\-]+$/u;
            if (!namePattern.test(nameEl.value.trim())) {
                e.preventDefault();
                showError(nameEl, 'Name may only contain letters, spaces, hyphens, and apostrophes.');
                return;
            }

            // body length guard
            if (bodyEl.value.length > maxLen) {
                e.preventDefault();
                showError(bodyEl, 'Message must not exceed ' + maxLen + ' characters.');
            }
        });
    }());
    </script>
</body>

</html>