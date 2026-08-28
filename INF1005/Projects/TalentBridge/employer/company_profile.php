<?php
/**
 * Employer company profile page — view and update company details.
 *
 * Allows an authenticated employer to manage their company name, industry,
 * and description. CSRF protected.
 *
 * @package TalentBridge
 */

session_start();
require_once '../includes/helpers.php';
require_once '../includes/auth.php';
require_once '../includes/db.php';
require_once '../includes/csrf.php';
require_once '../includes/review_widget.php';

// only employers may access this page
requireRole('employer');

$userId  = (int) $_SESSION['user_id'];
$errors  = [];
$success = '';

$industryOptions = [
    'Technology', 'Finance', 'Healthcare', 'Education', 'Retail',
    'Manufacturing', 'Logistics', 'Media', 'Hospitality', 'Consulting', 'Other',
];

// ---- handle review POST (must be before any HTML output) ----
if ($_SERVER['REQUEST_METHOD'] === 'POST' && ($_POST['action'] ?? '') === 'review_submit') {
    $reviewTargetId = (int) ($_POST['target_id'] ?? 0);
    handleReviewPost('company', $reviewTargetId);
}

// ---- fetch current company data ----
try {
    $pdo  = getConnection();
    $stmt = $pdo->prepare("
        SELECT u.name, u.email, c.company_id, c.company_name, c.industry, c.description
          FROM users u
          JOIN companies c ON c.user_id = u.user_id
         WHERE u.user_id = :uid
    ");
    $stmt->execute([':uid' => $userId]);
    $company = $stmt->fetch();
} catch (PDOException $e) {
    $company = null;
}

// ---- handle profile update ----
if ($_SERVER['REQUEST_METHOD'] === 'POST') {

    if (!validateCsrfToken($_POST['csrf_token'] ?? '')) {
        http_response_code(403);
        exit('Invalid CSRF token.');
    }

    $name        = trim($_POST['name']         ?? '');
    $companyName = trim($_POST['company_name'] ?? '');
    $industry    = trim($_POST['industry']     ?? '');
    $description = trim($_POST['description']  ?? '');

    // validation
    if (empty($name)) {
        $errors['name'] = 'Your name is required.';
    } elseif (strlen($name) > 100) {
        $errors['name'] = 'Name must not exceed 100 characters.';
    }

    if (empty($companyName)) {
        $errors['company_name'] = 'Company name is required.';
    } elseif (strlen($companyName) > 200) {
        $errors['company_name'] = 'Company name must not exceed 200 characters.';
    }

    if (!empty($industry) && !in_array($industry, $industryOptions, true)) {
        $errors['industry'] = 'Please select a valid industry.';
    }

    if (empty($errors)) {
        try {
            $pdo->beginTransaction();

            $pdo->prepare("UPDATE users SET name = :name WHERE user_id = :uid")
                ->execute([':name' => $name, ':uid' => $userId]);

            $pdo->prepare("
                UPDATE companies
                   SET company_name = :company_name,
                       industry     = :industry,
                       description  = :description
                 WHERE user_id = :uid
            ")->execute([
                ':company_name' => $companyName,
                ':industry'     => $industry    ?: null,
                ':description'  => $description ?: null,
                ':uid'          => $userId,
            ]);

            $pdo->commit();

            $_SESSION['name'] = $name;
            $success = 'Company profile updated successfully.';

            // refresh display data
            $company['name']         = $name;
            $company['company_name'] = $companyName;
            $company['industry']     = $industry;
            $company['description']  = $description;

        } catch (PDOException $e) {
            $pdo->rollBack();
            $errors['db'] = 'Could not save changes. Please try again.';
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
    <title>Company Profile — TalentBridge</title>
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
                    <h1 class="tb-section-title mb-1">Company Profile</h1>
                    <div class="tb-divider"></div>
                </div>
                <div class="d-flex gap-2">
                    <a href="/employer/post_job.php" class="btn btn-primary btn-sm">Post a Job</a>
                    <a href="/employer/manage_listings.php" class="btn btn-outline-primary btn-sm">Manage Listings</a>
                </div>
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

                <!-- company details form -->
                <div class="col-lg-8">
                    <div class="tb-card">
                        <h2 class="h5 fw-bold mb-4" style="color:var(--tb-primary)">Company Details</h2>

                        <form id="companyForm" method="post" action="/employer/company_profile.php" novalidate>
                            <input type="hidden" name="csrf_token" value="<?= sanitise($csrfToken) ?>">

                            <!-- contact name -->
                            <div class="mb-3">
                                <label for="name" class="form-label">Your Name <span aria-hidden="true" class="text-danger">*</span></label>
                                <input type="text" id="name" name="name"
                                    class="form-control <?= !empty($errors['name']) ? 'is-invalid' : '' ?>"
                                    value="<?= sanitise($company['name'] ?? '') ?>"
                                    required maxlength="100"
                                    <?= !empty($errors['name']) ? 'aria-describedby="name_error" aria-invalid="true"' : '' ?>>
                                <?php if (!empty($errors['name'])): ?>
                                    <div id="name_error" class="field-error invalid-feedback d-block" role="alert"><?= sanitise($errors['name']) ?></div>
                                <?php endif; ?>
                            </div>

                            <!-- company name -->
                            <div class="mb-3">
                                <label for="company_name" class="form-label">Company Name <span aria-hidden="true" class="text-danger">*</span></label>
                                <input type="text" id="company_name" name="company_name"
                                    class="form-control <?= !empty($errors['company_name']) ? 'is-invalid' : '' ?>"
                                    value="<?= sanitise($company['company_name'] ?? '') ?>"
                                    required maxlength="200"
                                    <?= !empty($errors['company_name']) ? 'aria-describedby="company_name_error" aria-invalid="true"' : '' ?>>
                                <?php if (!empty($errors['company_name'])): ?>
                                    <div id="company_name_error" class="field-error invalid-feedback d-block" role="alert"><?= sanitise($errors['company_name']) ?></div>
                                <?php endif; ?>
                            </div>

                            <!-- industry -->
                            <div class="mb-3">
                                <label for="industry" class="form-label">Industry</label>
                                <select id="industry" name="industry"
                                    class="form-select <?= !empty($errors['industry']) ? 'is-invalid' : '' ?>">
                                    <option value="">— Select industry —</option>
                                    <?php foreach ($industryOptions as $ind): ?>
                                        <option value="<?= sanitise($ind) ?>"
                                            <?= ($company['industry'] ?? '') === $ind ? 'selected' : '' ?>>
                                            <?= sanitise($ind) ?>
                                        </option>
                                    <?php endforeach; ?>
                                </select>
                                <?php if (!empty($errors['industry'])): ?>
                                    <div id="industry_error" class="field-error invalid-feedback d-block" role="alert"><?= sanitise($errors['industry']) ?></div>
                                <?php endif; ?>
                            </div>

                            <!-- description -->
                            <div class="mb-4">
                                <label for="description" class="form-label">Company Description</label>
                                <textarea id="description" name="description"
                                    class="form-control" rows="6"
                                    data-maxlength="2000"
                                    placeholder="Tell candidates about your company culture, mission, and what makes you a great place to work."><?= sanitise($company['description'] ?? '') ?></textarea>
                                <div class="char-count" id="descCount" aria-live="polite"></div>
                            </div>

                            <button type="submit" class="btn btn-primary px-4">Save Changes</button>
                        </form>
                    </div>
                </div>

                <!-- account info sidebar -->
                <div class="col-lg-4">
                    <div class="tb-card mb-4">
                        <h2 class="h5 fw-bold mb-3" style="color:var(--tb-primary)">Account</h2>
                        <p class="mb-1"><strong>Email:</strong> <?= sanitise($company['email'] ?? '') ?></p>
                        <p class="mb-0"><strong>Role:</strong> Employer</p>
                    </div>

                    <div class="tb-card">
                        <h2 class="h5 fw-bold mb-3" style="color:var(--tb-primary)">Quick Actions</h2>
                        <div class="d-flex flex-column gap-2">
                            <a href="/employer/post_job.php" class="btn btn-primary w-100">Post a New Job</a>
                            <a href="/employer/manage_listings.php" class="btn btn-outline-primary w-100">Manage Listings</a>
                        </div>
                    </div>
                </div>

            </div>

            <!-- ==========================================================
                 REVIEWS — inserted here (Step 4)
                 $company['company_id'] is already fetched above.
                 pathPrefix '../' = we are inside /employer/ subfolder.
                 ========================================================== -->
            <?php if ($company && isset($company['company_id'])): ?>
                <?php renderReviews('company', (int) $company['company_id'], '../'); ?>
            <?php endif; ?>

        </div>
    </section>
</main>

<footer class="tb-footer" role="contentinfo">
    <div class="container">
        <hr>
        <small>&copy; <?= date('Y') ?> TalentBridge Pte. Ltd. All rights reserved.</small>
    </div>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="../assets/js/validation.js"></script>
<script>
    // character counter for the description textarea
    (function () {
        const descEl  = document.getElementById('description');
        const countEl = document.getElementById('descCount');
        const maxLen  = 2000;

        function updateCount() {
            const remaining = maxLen - descEl.value.length;
            countEl.textContent = remaining + ' / ' + maxLen + ' characters remaining';
            countEl.className = 'char-count ' + (
                remaining < 0   ? 'count-danger' :
                remaining < 200 ? 'count-warn'   :
                                  'count-ok'
            );
        }

        descEl.addEventListener('input', updateCount);
        updateCount();
    }());

    document.getElementById('companyForm').addEventListener('submit', function (e) {
        if (!validateForm(this)) e.preventDefault();
    });
</script>
</body>
</html>