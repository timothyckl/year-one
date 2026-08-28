<?php
/**
 * Seeker profile page — view and update personal details and CV.
 *
 * Handles two forms: the profile details form (name, headline, skills,
 * location) and the CV upload form. CV uploads are restricted to PDF,
 * MIME-checked, signature-checked, renamed
 * to a SHA-256 hash, and stored outside the web root at /var/uploads/cvs/.
 *
 * @package TalentBridge
 */

session_start();
require_once '../includes/helpers.php';
require_once '../includes/auth.php';
require_once '../includes/db.php';
require_once '../includes/csrf.php';

// only job seekers may access this page
requireRole('seeker');

$userId   = (int) $_SESSION['user_id'];
$errors   = [];
$success  = '';

// allowed cv file type: PDF only
$allowedMime = 'application/pdf';
$allowedExt  = 'pdf';

// cv upload directory — outside web root for security
$uploadDir = '/var/uploads/cvs/';

// maximum file size: 2 MB
$maxFileSize = 2 * 1024 * 1024;

// ---- fetch current profile data ----
try {
    $pdo  = getConnection();
    $stmt = $pdo->prepare("
        SELECT u.name, u.email, sp.headline, sp.skills, sp.cv_path, sp.location
          FROM users u
          JOIN seeker_profiles sp ON sp.user_id = u.user_id
         WHERE u.user_id = :uid
    ");
    $stmt->execute([':uid' => $userId]);
    $profile = $stmt->fetch();
} catch (PDOException $e) {
    $profile = null;
}

// ---- handle profile details update ----
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['action']) && $_POST['action'] === 'update_profile') {

    if (!validateCsrfToken($_POST['csrf_token'] ?? '')) {
        http_response_code(403);
        exit('Invalid CSRF token.');
    }

    $name     = trim($_POST['name']     ?? '');
    $headline = trim($_POST['headline'] ?? '');
    $skills   = trim($_POST['skills']   ?? '');
    $location = trim($_POST['location'] ?? '');

    if (empty($name)) {
        $errors['name'] = 'Name is required.';
    } elseif (strlen($name) > 100) {
        $errors['name'] = 'Name must not exceed 100 characters.';
    }

    if (strlen($headline) > 255) {
        $errors['headline'] = 'Headline must not exceed 255 characters.';
    }

    if (strlen($location) > 150) {
        $errors['location'] = 'Location must not exceed 150 characters.';
    }

    if (empty($errors)) {
        try {
            $pdo->beginTransaction();

            $pdo->prepare("UPDATE users SET name = :name WHERE user_id = :uid")
                ->execute([':name' => $name, ':uid' => $userId]);

            $pdo->prepare("
                UPDATE seeker_profiles
                   SET headline = :headline, skills = :skills, location = :location
                 WHERE user_id = :uid
            ")->execute([
                ':headline' => $headline ?: null,
                ':skills'   => $skills   ?: null,
                ':location' => $location ?: null,
                ':uid'      => $userId,
            ]);

            $pdo->commit();

            // keep session name in sync
            $_SESSION['name'] = $name;
            $success = 'Profile updated successfully.';

            // refresh profile data for display
            $profile['name']     = $name;
            $profile['headline'] = $headline;
            $profile['skills']   = $skills;
            $profile['location'] = $location;

        } catch (PDOException $e) {
            $pdo->rollBack();
            $errors['db'] = 'Could not save profile. Please try again.';
        }
    }
}

// ---- handle cv upload ----
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['action']) && $_POST['action'] === 'upload_cv') {

    if (!validateCsrfToken($_POST['csrf_token'] ?? '')) {
        http_response_code(403);
        exit('Invalid CSRF token.');
    }

    if (!isset($_FILES['cv']) || $_FILES['cv']['error'] === UPLOAD_ERR_NO_FILE) {
        $errors['cv'] = 'Please select a CV file to upload.';

    } elseif ($_FILES['cv']['error'] !== UPLOAD_ERR_OK) {
        $errors['cv'] = 'Upload failed. Please try again.';

    } elseif ($_FILES['cv']['size'] > $maxFileSize) {
        $errors['cv'] = 'File size must not exceed 2 MB.';

    } else {
        // check the real mime type using the file contents — not the client-supplied type
        $finfo    = new finfo(FILEINFO_MIME_TYPE);
        $realMime = $finfo->file($_FILES['cv']['tmp_name']);

        // get the submitted extension and normalise to lowercase
        $origExt  = strtolower(pathinfo($_FILES['cv']['name'], PATHINFO_EXTENSION));

        if (!is_uploaded_file($_FILES['cv']['tmp_name'])) {
            $errors['cv'] = 'Invalid upload source.';

        } elseif ($realMime !== $allowedMime) {
            $errors['cv'] = 'Only PDF files are accepted.';

        } elseif ($origExt !== $allowedExt) {
            $errors['cv'] = 'File extension must be .pdf.';

        } else {
            // verify PDF signature to block spoofed content with a .pdf extension
            $header = @file_get_contents($_FILES['cv']['tmp_name'], false, null, 0, 5);
            if ($header !== '%PDF-') {
                $errors['cv'] = 'The uploaded file is not a valid PDF.';
            }
        }

        if (empty($errors['cv'])) {
            // generate a cryptographically unpredictable filename — random_bytes cannot be brute-forced
            $hashedName = bin2hex(random_bytes(32)) . '.' . $allowedExt;
            $destPath   = $uploadDir . $hashedName;

            // ensure the upload directory exists on the server
            if (!is_dir($uploadDir)) {
                @mkdir($uploadDir, 0750, true);
            }

            if (!move_uploaded_file($_FILES['cv']['tmp_name'], $destPath)) {
                $errors['cv'] = 'Could not save the file. Please check server permissions.';
            } else {
                try {
                    // delete the old cv file if one exists
                    if (!empty($profile['cv_path']) && file_exists($profile['cv_path'])) {
                        @unlink($profile['cv_path']);
                    }

                    $pdo->prepare("
                        UPDATE seeker_profiles SET cv_path = :cv_path WHERE user_id = :uid
                    ")->execute([':cv_path' => $destPath, ':uid' => $userId]);

                    $profile['cv_path'] = $destPath;
                    $success = 'CV uploaded successfully.';
                    log_audit_event('CV_UPLOAD_SUCCESS', $userId, ['mime' => $realMime]);

                } catch (PDOException $e) {
                    @unlink($destPath);
                    $errors['cv'] = 'Could not update your profile. Please try again.';
                }
            }
        } else {
            log_audit_event('CV_UPLOAD_REJECTED', $userId, ['reason' => $errors['cv']]);
        }
    }
}

$csrfToken = generateCsrfToken();
$flash     = getFlash();
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Profile — TalentBridge</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="../assets/css/style.css" rel="stylesheet">
</head>
<body>

<?php require_once '../includes/nav.php'; ?>

<main id="main-content">
    <section class="tb-section">
        <div class="container">

            <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-3">
                <div>
                    <h1 class="tb-section-title mb-1">My Profile</h1>
                    <div class="tb-divider"></div>
                </div>
                <a href="/seeker/applications.php" class="btn btn-outline-primary btn-sm">
                    View My Applications
                </a>
            </div>

            <?php if (!empty($flash['success'])): ?>
                <div class="alert alert-success tb-flash" role="alert"><?= sanitise($flash['success']) ?></div>
            <?php endif; ?>
            <?php if (!empty($success)): ?>
                <div class="alert alert-success tb-flash" role="alert"><?= sanitise($success) ?></div>
            <?php endif; ?>
            <?php if (!empty($errors['db'])): ?>
                <div class="alert alert-danger tb-flash" role="alert"><?= sanitise($errors['db']) ?></div>
            <?php endif; ?>

            <div class="row g-4">

                <!-- profile details form -->
                <div class="col-lg-7">
                    <div class="tb-card">
                        <h2 class="h5 fw-bold mb-3" style="color:var(--tb-primary)">Personal Details</h2>

                        <p class="text-muted small mb-3">Fields marked <span aria-hidden="true">*</span><span class="visually-hidden">with an asterisk</span> are required.</p>

                        <form id="profileForm" method="post" action="/seeker/profile.php" novalidate>
                            <input type="hidden" name="csrf_token" value="<?= sanitise($csrfToken) ?>">
                            <input type="hidden" name="action" value="update_profile">

                            <!-- name -->
                            <div class="mb-3">
                                <label for="name" class="form-label">Full Name <span aria-hidden="true" class="text-danger">*</span></label>
                                <input type="text" id="name" name="name"
                                    class="form-control <?= !empty($errors['name']) ? 'is-invalid' : '' ?>"
                                    value="<?= sanitise($profile['name'] ?? '') ?>"
                                    required maxlength="100"
                                    <?= !empty($errors['name']) ? 'aria-describedby="name_error" aria-invalid="true"' : '' ?>>
                                <?php if (!empty($errors['name'])): ?>
                                    <div id="name_error" class="field-error invalid-feedback d-block" role="alert"><?= sanitise($errors['name']) ?></div>
                                <?php endif; ?>
                            </div>

                            <!-- headline -->
                            <div class="mb-3">
                                <label for="headline" class="form-label">Professional Headline</label>
                                <input type="text" id="headline" name="headline"
                                    class="form-control <?= !empty($errors['headline']) ? 'is-invalid' : '' ?>"
                                    value="<?= sanitise($profile['headline'] ?? '') ?>"
                                    maxlength="255"
                                    placeholder="e.g. Senior Software Engineer | 5 years experience">
                                <?php if (!empty($errors['headline'])): ?>
                                    <div id="headline_error" class="field-error invalid-feedback d-block" role="alert"><?= sanitise($errors['headline']) ?></div>
                                <?php endif; ?>
                            </div>

                            <!-- skills -->
                            <div class="mb-3">
                                <label for="skills" class="form-label">Skills</label>
                                <textarea id="skills" name="skills" class="form-control" rows="3"
                                    placeholder="e.g. PHP, MySQL, JavaScript, React, Docker"><?= sanitise($profile['skills'] ?? '') ?></textarea>
                                <div class="form-text">Separate skills with commas.</div>
                            </div>

                            <!-- location -->
                            <div class="mb-4">
                                <label for="location" class="form-label">Location</label>
                                <input type="text" id="location" name="location"
                                    class="form-control <?= !empty($errors['location']) ? 'is-invalid' : '' ?>"
                                    value="<?= sanitise($profile['location'] ?? '') ?>"
                                    maxlength="150"
                                    placeholder="e.g. Singapore">
                                <?php if (!empty($errors['location'])): ?>
                                    <div id="location_error" class="field-error invalid-feedback d-block" role="alert"><?= sanitise($errors['location']) ?></div>
                                <?php endif; ?>
                            </div>

                            <button type="submit" class="btn btn-primary">Save Changes</button>
                        </form>
                    </div>
                </div>

                <!-- cv upload card -->
                <div class="col-lg-5">
                    <div class="tb-card">
                        <h2 class="h5 fw-bold mb-3" style="color:var(--tb-primary)">CV / Résumé</h2>

                        <!-- current cv status -->
                        <?php if (!empty($profile['cv_path'])): ?>
                            <div class="alert alert-info py-2 mb-3" role="status">
                                <strong>CV on file.</strong>
                                <a href="/download.php?user_id=<?= $userId ?>" class="ms-2">Download my CV</a>
                            </div>
                        <?php else: ?>
                            <p class="text-muted small mb-3">No CV uploaded yet. Upload one to let employers download it.</p>
                        <?php endif; ?>

                        <?php if (!empty($errors['cv'])): ?>
                            <div class="alert alert-danger tb-flash" role="alert"><?= sanitise($errors['cv']) ?></div>
                        <?php endif; ?>

                        <form id="cvForm" method="post" action="/seeker/profile.php"
                              enctype="multipart/form-data" novalidate>
                            <input type="hidden" name="csrf_token" value="<?= sanitise($csrfToken) ?>">
                            <input type="hidden" name="action" value="upload_cv">

                            <div class="mb-3">
                                <label for="cv" class="form-label">
                                    Upload CV <span aria-hidden="true" class="text-danger">*</span>
                                </label>
                                <input type="file" id="cv" name="cv"
                                    class="form-control"
                                    accept=".pdf,application/pdf"
                                    required
                                    aria-describedby="cvHelp">
                                <div id="cvHelp" class="form-text">
                                    PDF only. Maximum 2 MB.
                                </div>
                            </div>

                            <button type="submit" class="btn btn-primary w-100">Upload CV</button>
                        </form>
                    </div>

                    <!-- account info card -->
                    <div class="tb-card mt-4">
                        <h2 class="h5 fw-bold mb-3" style="color:var(--tb-primary)">Account</h2>
                        <p class="mb-1"><strong>Email:</strong> <?= sanitise($profile['email'] ?? '') ?></p>
                        <p class="mb-0"><strong>Role:</strong> Job Seeker</p>
                    </div>
                </div>

            </div>
        </div>
    </section>
</main>

<footer class="tb-footer" role="contentinfo">
    <div class="container">
        <div class="row g-4 mb-4">
            <div class="col-md-4">
                <p class="footer-brand mb-2">Talent<span>Bridge</span></p>
                <p class="small">Connecting talent with opportunity across Singapore and beyond.</p>
            </div>
        </div>
        <hr>
        <small>&copy; <?= date('Y') ?> TalentBridge Pte. Ltd. All rights reserved.</small>
    </div>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="../assets/js/validation.js"></script>
<script>
    document.getElementById('profileForm').addEventListener('submit', function (e) {
        if (!validateForm(this)) e.preventDefault();
    });

    // basic client-side file size and extension check before upload
    document.getElementById('cvForm').addEventListener('submit', function (e) {
        const fileInput = document.getElementById('cv');
        const file = fileInput.files[0];

        clearErrors(fileInput);

        if (!file) {
            e.preventDefault();
            showError(fileInput, 'Please select a file.');
            return;
        }

        const allowedExts = ['pdf'];
        const ext = file.name.split('.').pop().toLowerCase();

        if (!allowedExts.includes(ext)) {
            e.preventDefault();
            showError(fileInput, 'Only PDF files are accepted.');
            return;
        }

        if (file.size > 2 * 1024 * 1024) {
            e.preventDefault();
            showError(fileInput, 'File size must not exceed 2 MB.');
        }
    });
</script>
</body>
</html>
