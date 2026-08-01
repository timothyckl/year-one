<?php
/**
 * Post a job page — employer creates a new job listing.
 *
 * Validates all fields server-side, looks up the employer's company_id,
 * and INSERTs into job_listings. CSRF protected.
 *
 * @package TalentBridge
 */

session_start();
require_once '../includes/helpers.php';
require_once '../includes/auth.php';
require_once '../includes/db.php';
require_once '../includes/csrf.php';

requireRole('employer');

$userId  = (int) $_SESSION['user_id'];
$errors  = [];
$formData = [
    'title'        => '',
    'description'  => '',
    'location'     => '',
    'type'         => 'Full-time',
    'salary_min'   => '',
    'salary_max'   => '',
    'salary_period'=> '',
];

$typeOptions = ['Full-time', 'Part-time', 'Contract', 'Internship', 'Remote'];

// look up the employer's company_id
try {
    $pdo       = getConnection();
    $compStmt  = $pdo->prepare("SELECT company_id FROM companies WHERE user_id = :uid");
    $compStmt->execute([':uid' => $userId]);
    $company   = $compStmt->fetch();
} catch (PDOException $e) {
    $company = null;
}

if (!$company) {
    setFlash('error', 'Please complete your company profile before posting a job.');
    redirect('/employer/company_profile.php');
}

$companyId = (int) $company['company_id'];

// ---- handle POST ----
if ($_SERVER['REQUEST_METHOD'] === 'POST') {

    if (!validateCsrfToken($_POST['csrf_token'] ?? '')) {
        http_response_code(403);
        exit('Invalid CSRF token.');
    }

    $formData['title']         = trim($_POST['title']         ?? '');
    $formData['description']   = trim($_POST['description']   ?? '');
    $formData['location']      = trim($_POST['location']      ?? '');
    $formData['type']          = trim($_POST['type']          ?? 'Full-time');
    $formData['salary_min']    = trim($_POST['salary_min']    ?? '');
    $formData['salary_max']    = trim($_POST['salary_max']    ?? '');
    $formData['salary_period'] = trim($_POST['salary_period'] ?? '');

    // validation
    if (empty($formData['title'])) {
        $errors['title'] = 'Job title is required.';
    } elseif (strlen($formData['title']) > 200) {
        $errors['title'] = 'Title must not exceed 200 characters.';
    }

    if (empty($formData['description'])) {
        $errors['description'] = 'Job description is required.';
    } elseif (strlen($formData['description']) < 20) {
        $errors['description'] = 'Description must be at least 20 characters.';
    }

    if (!in_array($formData['type'], $typeOptions, true)) {
        $errors['type'] = 'Please select a valid job type.';
    }

    if (strlen($formData['location']) > 150) {
        $errors['location'] = 'Location must not exceed 150 characters.';
    }

    $periodOptions = ['per hour', 'per day', 'per month', 'per annum'];

    if ($formData['salary_min'] !== '') {
        if (!is_numeric($formData['salary_min']) || (float) $formData['salary_min'] <= 0) {
            $errors['salary_min'] = 'Please enter a valid minimum salary greater than 0.';
        } elseif (!in_array($formData['salary_period'], $periodOptions, true)) {
            $errors['salary_period'] = 'Please select a salary period.';
        }
    }

    if ($formData['salary_max'] !== '') {
        if (!is_numeric($formData['salary_max']) || (float) $formData['salary_max'] <= 0) {
            $errors['salary_max'] = 'Please enter a valid maximum salary greater than 0.';
        } elseif ($formData['salary_min'] === '') {
            $errors['salary_min'] = 'Please enter a minimum salary.';
        } elseif ((float) $formData['salary_max'] < (float) $formData['salary_min']) {
            $errors['salary_max'] = 'Maximum salary must be at least the minimum salary.';
        }
    }

    if ($formData['salary_period'] !== '' && $formData['salary_min'] === '' && !isset($errors['salary_min'])) {
        $errors['salary_min'] = 'Please enter a minimum salary.';
    }

    if (empty($errors)) {
        try {
            $stmt = $pdo->prepare("
                INSERT INTO job_listings (company_id, title, description, location, type, salary_min, salary_max, salary_period)
                VALUES (:company_id, :title, :description, :location, :type, :salary_min, :salary_max, :salary_period)
            ");
            $stmt->execute([
                ':company_id'    => $companyId,
                ':title'         => $formData['title'],
                ':description'   => $formData['description'],
                ':location'      => $formData['location'] ?: null,
                ':type'          => $formData['type'],
                ':salary_min'    => $formData['salary_min']    ?: null,
                ':salary_max'    => $formData['salary_max']    ?: null,
                ':salary_period' => $formData['salary_period'] ?: null,
            ]);

            setFlash('success', 'Job listing published successfully!');
            redirect('/employer/manage_listings.php');

        } catch (PDOException $e) {
            $errors['db'] = 'Could not publish the listing. Please try again.';
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
    <title>Post a Job — TalentBridge</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="../assets/css/style.css" rel="stylesheet">
</head>
<body>

<?php require_once '../includes/nav.php'; ?>

<main id="main-content">
    <section class="tb-section">
        <div class="container">
            <div class="row justify-content-center">
                <div class="col-lg-8">

                    <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-3">
                        <div>
                            <h1 class="tb-section-title mb-1">Post a Job</h1>
                            <div class="tb-divider"></div>
                        </div>
                        <a href="/employer/manage_listings.php" class="btn btn-outline-primary btn-sm">
                            <span aria-hidden="true">←</span> My Listings
                        </a>
                    </div>

                    <?php if (!empty($flash['success'])): ?>
                        <div class="alert alert-success tb-flash" role="alert"><?= sanitise($flash['success']) ?></div>
                    <?php endif; ?>
                    <?php if (!empty($errors['db'])): ?>
                        <div class="alert alert-danger tb-flash" role="alert"><?= sanitise($errors['db']) ?></div>
                    <?php endif; ?>

                    <div class="tb-card">
                        <form id="postJobForm" method="post" action="/employer/post_job.php" novalidate>
                            <input type="hidden" name="csrf_token" value="<?= sanitise($csrfToken) ?>">

                            <!-- job title -->
                            <div class="mb-3">
                                <label for="title" class="form-label">Job Title <span aria-hidden="true" class="text-danger">*</span></label>
                                <input type="text" id="title" name="title"
                                    class="form-control <?= !empty($errors['title']) ? 'is-invalid' : '' ?>"
                                    value="<?= sanitise($formData['title']) ?>"
                                    required maxlength="200"
                                    placeholder="e.g. Senior Software Engineer"
                                    <?= !empty($errors['title']) ? 'aria-describedby="title_error" aria-invalid="true"' : '' ?>>
                                <?php if (!empty($errors['title'])): ?>
                                    <div id="title_error" class="field-error invalid-feedback d-block" role="alert"><?= sanitise($errors['title']) ?></div>
                                <?php endif; ?>
                            </div>

                            <!-- job type and location row -->
                            <div class="row g-3 mb-3">
                                <div class="col-sm-6">
                                    <label for="type" class="form-label">Job Type <span aria-hidden="true" class="text-danger">*</span></label>
                                    <select id="type" name="type"
                                        class="form-select <?= !empty($errors['type']) ? 'is-invalid' : '' ?>"
                                        required>
                                        <?php foreach ($typeOptions as $t): ?>
                                            <option value="<?= sanitise($t) ?>"
                                                <?= $formData['type'] === $t ? 'selected' : '' ?>>
                                                <?= sanitise($t) ?>
                                            </option>
                                        <?php endforeach; ?>
                                    </select>
                                </div>
                                <div class="col-sm-6">
                                    <label for="location" class="form-label">Location</label>
                                    <input type="text" id="location" name="location"
                                        class="form-control"
                                        value="<?= sanitise($formData['location']) ?>"
                                        maxlength="150"
                                        placeholder="e.g. Singapore, Remote">
                                </div>
                            </div>

                            <!-- salary fields — min, max (optional), and period -->
                            <div class="row g-3 mb-3">
                                <div class="col-sm-4">
                                    <label for="salary_min" class="form-label">Min Salary</label>
                                    <div class="input-group">
                                        <span class="input-group-text">$</span>
                                        <input type="number" id="salary_min" name="salary_min"
                                            class="form-control <?= !empty($errors['salary_min']) ? 'is-invalid' : '' ?>"
                                            value="<?= sanitise($formData['salary_min']) ?>"
                                            min="0" step="0.01"
                                            data-salary-min
                                            <?= !empty($errors['salary_min']) ? 'aria-describedby="salary_min_error" aria-invalid="true"' : '' ?>>
                                    </div>
                                    <?php if (!empty($errors['salary_min'])): ?>
                                        <div id="salary_min_error" class="field-error invalid-feedback d-block" role="alert"><?= sanitise($errors['salary_min']) ?></div>
                                    <?php endif; ?>
                                </div>
                                <div class="col-sm-4">
                                    <label for="salary_max" class="form-label">Max Salary <em class="text-muted small">(optional)</em></label>
                                    <div class="input-group">
                                        <span class="input-group-text">$</span>
                                        <input type="number" id="salary_max" name="salary_max"
                                            class="form-control <?= !empty($errors['salary_max']) ? 'is-invalid' : '' ?>"
                                            value="<?= sanitise($formData['salary_max']) ?>"
                                            min="0" step="0.01"
                                            <?= !empty($errors['salary_max']) ? 'aria-describedby="salary_max_error" aria-invalid="true"' : '' ?>>
                                    </div>
                                    <?php if (!empty($errors['salary_max'])): ?>
                                        <div id="salary_max_error" class="field-error invalid-feedback d-block" role="alert"><?= sanitise($errors['salary_max']) ?></div>
                                    <?php endif; ?>
                                </div>
                                <div class="col-sm-4">
                                    <label for="salary_period" class="form-label">Period</label>
                                    <select id="salary_period" name="salary_period"
                                        class="form-select <?= !empty($errors['salary_period']) ? 'is-invalid' : '' ?>"
                                        <?= !empty($errors['salary_period']) ? 'aria-describedby="salary_period_error" aria-invalid="true"' : '' ?>>
                                        <option value="">— Select —</option>
                                        <option value="per hour"  <?= $formData['salary_period'] === 'per hour'  ? 'selected' : '' ?>>Per Hour</option>
                                        <option value="per day"   <?= $formData['salary_period'] === 'per day'   ? 'selected' : '' ?>>Per Day</option>
                                        <option value="per month" <?= $formData['salary_period'] === 'per month' ? 'selected' : '' ?>>Per Month</option>
                                        <option value="per annum" <?= $formData['salary_period'] === 'per annum' ? 'selected' : '' ?>>Per Annum</option>
                                    </select>
                                    <?php if (!empty($errors['salary_period'])): ?>
                                        <div id="salary_period_error" class="field-error invalid-feedback d-block" role="alert"><?= sanitise($errors['salary_period']) ?></div>
                                    <?php endif; ?>
                                </div>
                            </div>

                            <!-- description -->
                            <div class="mb-4">
                                <label for="description" class="form-label">
                                    Job Description <span aria-hidden="true" class="text-danger">*</span>
                                </label>
                                <textarea id="description" name="description"
                                    class="form-control <?= !empty($errors['description']) ? 'is-invalid' : '' ?>"
                                    rows="12"
                                    required
                                    data-charcount="true"
                                    data-maxlength="5000"
                                    placeholder="Describe the role, responsibilities, requirements, and any other relevant details…"
                                    <?= !empty($errors['description']) ? 'aria-describedby="description_error" aria-invalid="true"' : '' ?>><?= sanitise($formData['description']) ?></textarea>
                                <?php if (!empty($errors['description'])): ?>
                                    <div id="description_error" class="field-error invalid-feedback d-block" role="alert"><?= sanitise($errors['description']) ?></div>
                                <?php endif; ?>
                            </div>

                            <div class="d-flex gap-2">
                                <button type="submit" class="btn btn-primary px-4">Publish Listing</button>
                                <a href="/employer/manage_listings.php" class="btn btn-outline-secondary">Cancel</a>
                            </div>
                        </form>
                    </div>

                </div>
            </div>
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
<script src="../assets/js/charcount.js"></script>
<script>
    document.getElementById('postJobForm').addEventListener('submit', function (e) {
        if (!validateForm(this)) e.preventDefault();
    });
</script>
</body>
</html>
