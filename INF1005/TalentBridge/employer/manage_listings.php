<?php
/**
 * Manage listings page — employer views, edits, and deletes their job listings.
 *
 * Edit uses a pre-filled inline form shown when ?edit=job_id is present.
 * Delete is a POST with CSRF — never a GET link.
 * All queries verify ownership via company_id JOIN to prevent cross-employer access.
 *
 * @package TalentBridge
 */

session_start();
require_once '../includes/helpers.php';
require_once '../includes/auth.php';
require_once '../includes/db.php';
require_once '../includes/csrf.php';

requireRole('employer');

$userId = (int) $_SESSION['user_id'];
$errors = [];

// resolve this employer's company_id
try {
    $pdo      = getConnection();
    $compStmt = $pdo->prepare("SELECT company_id FROM companies WHERE user_id = :uid");
    $compStmt->execute([':uid' => $userId]);
    $company  = $compStmt->fetch();
} catch (PDOException $e) {
    $company = null;
}

if (!$company) {
    setFlash('error', 'Please complete your company profile first.');
    redirect('/employer/company_profile.php');
}

$companyId   = (int) $company['company_id'];
$typeOptions = ['Full-time', 'Part-time', 'Contract', 'Internship', 'Remote'];

// ---- handle POST actions (edit update or delete) ----
if ($_SERVER['REQUEST_METHOD'] === 'POST') {

    if (!validateCsrfToken($_POST['csrf_token'] ?? '')) {
        http_response_code(403);
        exit('Invalid CSRF token.');
    }

    $action = $_POST['action'] ?? '';
    $jobId  = (int) ($_POST['job_id'] ?? 0);

    if ($action === 'delete' && $jobId > 0) {
        // verify ownership before deleting
        $stmt = $pdo->prepare("
            DELETE FROM job_listings WHERE job_id = :jid AND company_id = :cid
        ");
        $stmt->execute([':jid' => $jobId, ':cid' => $companyId]);
        setFlash('success', 'Listing deleted successfully.');
        redirect('/employer/manage_listings.php');
    }

    if ($action === 'update' && $jobId > 0) {
        $title        = trim($_POST['title']         ?? '');
        $description  = trim($_POST['description']   ?? '');
        $location     = trim($_POST['location']      ?? '');
        $type         = trim($_POST['type']          ?? '');
        $salaryMin    = trim($_POST['salary_min']    ?? '');
        $salaryMax    = trim($_POST['salary_max']    ?? '');
        $salaryPeriod = trim($_POST['salary_period'] ?? '');
        $status       = trim($_POST['status']        ?? 'active');

        $validStatuses = ['active', 'closed', 'draft'];

        if (empty($title)) {
            $errors['title'] = 'Job title is required.';
        }
        if (empty($description)) {
            $errors['description'] = 'Description is required.';
        }
        if (!in_array($type, $typeOptions, true)) {
            $errors['type'] = 'Invalid job type.';
        }
        if (!in_array($status, $validStatuses, true)) {
            $errors['status'] = 'Invalid status.';
        }

        $periodOptions = ['per hour', 'per day', 'per month', 'per annum'];

        if ($salaryMin !== '') {
            if (!is_numeric($salaryMin) || (float) $salaryMin <= 0) {
                $errors['salary_min'] = 'Please enter a valid minimum salary greater than 0.';
            } elseif (!in_array($salaryPeriod, $periodOptions, true)) {
                $errors['salary_period'] = 'Please select a salary period.';
            }
        }

        if ($salaryMax !== '') {
            if (!is_numeric($salaryMax) || (float) $salaryMax <= 0) {
                $errors['salary_max'] = 'Please enter a valid maximum salary greater than 0.';
            } elseif ($salaryMin === '') {
                $errors['salary_min'] = 'Please enter a minimum salary.';
            } elseif ((float) $salaryMax < (float) $salaryMin) {
                $errors['salary_max'] = 'Maximum salary must be at least the minimum salary.';
            }
        }

        if ($salaryPeriod !== '' && $salaryMin === '' && !isset($errors['salary_min'])) {
            $errors['salary_min'] = 'Please enter a minimum salary.';
        }

        if (empty($errors)) {
            try {
                // update only if the listing belongs to this employer's company
                $stmt = $pdo->prepare("
                    UPDATE job_listings
                       SET title         = :title,
                           description   = :description,
                           location      = :location,
                           type          = :type,
                           salary_min    = :salary_min,
                           salary_max    = :salary_max,
                           salary_period = :salary_period,
                           status        = :status
                     WHERE job_id     = :jid
                       AND company_id = :cid
                ");
                $stmt->execute([
                    ':title'         => $title,
                    ':description'   => $description,
                    ':location'      => $location ?: null,
                    ':type'          => $type,
                    ':salary_min'    => $salaryMin    ?: null,
                    ':salary_max'    => $salaryMax    ?: null,
                    ':salary_period' => $salaryPeriod ?: null,
                    ':status'        => $status,
                    ':jid'           => $jobId,
                    ':cid'           => $companyId,
                ]);

                setFlash('success', 'Listing updated successfully.');
                redirect('/employer/manage_listings.php');

            } catch (PDOException $e) {
                $errors['db'] = 'Could not update the listing. Please try again.';
            }
        }
    }
}

// ---- fetch all listings for this employer ----
try {
    $stmt = $pdo->prepare("
        SELECT job_id, title, location, type, status, created_at,
               (SELECT COUNT(*) FROM applications WHERE job_id = job_listings.job_id) AS app_count
          FROM job_listings
         WHERE company_id = :cid
         ORDER BY created_at DESC
    ");
    $stmt->execute([':cid' => $companyId]);
    $listings = $stmt->fetchAll();
} catch (PDOException $e) {
    $listings = [];
}

// which listing is being edited (if any)
$editJobId = (int) ($_GET['edit'] ?? $_POST['job_id'] ?? 0);
$editJob   = null;

if ($editJobId > 0) {
    foreach ($listings as $l) {
        if ((int)$l['job_id'] === $editJobId) {
            // fetch full data including description for the edit form
            $stmt = $pdo->prepare("
                SELECT * FROM job_listings WHERE job_id = :jid AND company_id = :cid
            ");
            $stmt->execute([':jid' => $editJobId, ':cid' => $companyId]);
            $editJob = $stmt->fetch();
            break;
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
    <title>Manage Listings — TalentBridge</title>
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
                    <h1 class="tb-section-title mb-1">Manage Listings</h1>
                    <div class="tb-divider"></div>
                </div>
                <a href="/employer/post_job.php" class="btn btn-primary btn-sm">+ Post a New Job</a>
            </div>

            <?php foreach ($flash as $type => $msg): ?>
                <div class="alert alert-<?= $type === 'error' ? 'danger' : sanitise($type) ?> tb-flash" role="alert">
                    <?= sanitise($msg) ?>
                </div>
            <?php endforeach; ?>
            <?php if (!empty($errors['db'])): ?>
                <div class="alert alert-danger tb-flash" role="alert"><?= sanitise($errors['db']) ?></div>
            <?php endif; ?>

            <!-- inline edit form — shown when ?edit=job_id is active -->
            <?php if ($editJob): ?>
                <div class="tb-card mb-4" style="border-color:var(--tb-accent)">
                    <h2 class="h5 fw-bold mb-3" style="color:var(--tb-primary)">
                        Editing: <?= sanitise($editJob['title']) ?>
                    </h2>

                    <form id="editJobForm" method="post" action="/employer/manage_listings.php" novalidate>
                        <input type="hidden" name="csrf_token" value="<?= sanitise($csrfToken) ?>">
                        <input type="hidden" name="action"  value="update">
                        <input type="hidden" name="job_id"  value="<?= (int)$editJob['job_id'] ?>">

                        <div class="mb-3">
                            <label for="edit_title" class="form-label">Job Title <span aria-hidden="true" class="text-danger">*</span></label>
                            <input type="text" id="edit_title" name="title"
                                class="form-control <?= !empty($errors['title']) ? 'is-invalid' : '' ?>"
                                value="<?= sanitise($editJob['title']) ?>"
                                required maxlength="200"
                                <?= !empty($errors['title']) ? 'aria-describedby="edit_title_error" aria-invalid="true"' : '' ?>>
                            <?php if (!empty($errors['title'])): ?>
                                <div id="edit_title_error" class="field-error invalid-feedback d-block" role="alert"><?= sanitise($errors['title']) ?></div>
                            <?php endif; ?>
                        </div>

                        <div class="row g-3 mb-3">
                            <div class="col-sm-4">
                                <label for="edit_type" class="form-label">Type</label>
                                <select id="edit_type" name="type" class="form-select" required>
                                    <?php foreach ($typeOptions as $t): ?>
                                        <option value="<?= sanitise($t) ?>" <?= $editJob['type'] === $t ? 'selected' : '' ?>>
                                            <?= sanitise($t) ?>
                                        </option>
                                    <?php endforeach; ?>
                                </select>
                            </div>
                            <div class="col-sm-4">
                                <label for="edit_status" class="form-label">Status</label>
                                <select id="edit_status" name="status" class="form-select">
                                    <option value="active" <?= $editJob['status'] === 'active' ? 'selected' : '' ?>>Active</option>
                                    <option value="closed" <?= $editJob['status'] === 'closed' ? 'selected' : '' ?>>Closed</option>
                                    <option value="draft"  <?= $editJob['status'] === 'draft'  ? 'selected' : '' ?>>Draft</option>
                                </select>
                            </div>
                            <div class="col-sm-4">
                                <label for="edit_location" class="form-label">Location</label>
                                <input type="text" id="edit_location" name="location"
                                    class="form-control"
                                    value="<?= sanitise($editJob['location'] ?? '') ?>"
                                    maxlength="150">
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
                                        value="<?= $editJob['salary_min'] !== null ? (float) $editJob['salary_min'] : '' ?>"
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
                                        value="<?= $editJob['salary_max'] !== null ? (float) $editJob['salary_max'] : '' ?>"
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
                                    <?php
                                    $currentPeriod = $editJob['salary_period'] ?? '';
                                    foreach (['per hour' => 'Per Hour', 'per day' => 'Per Day', 'per month' => 'Per Month', 'per annum' => 'Per Annum'] as $val => $label):
                                    ?>
                                        <option value="<?= sanitise($val) ?>" <?= $currentPeriod === $val ? 'selected' : '' ?>><?= sanitise($label) ?></option>
                                    <?php endforeach; ?>
                                </select>
                                <?php if (!empty($errors['salary_period'])): ?>
                                    <div id="salary_period_error" class="field-error invalid-feedback d-block" role="alert"><?= sanitise($errors['salary_period']) ?></div>
                                <?php endif; ?>
                            </div>
                        </div>

                        <div class="mb-4">
                            <label for="edit_description" class="form-label">Description <span aria-hidden="true" class="text-danger">*</span></label>
                            <textarea id="edit_description" name="description"
                                class="form-control <?= !empty($errors['description']) ? 'is-invalid' : '' ?>"
                                rows="10" required
                                data-charcount="true" data-maxlength="5000"
                                <?= !empty($errors['description']) ? 'aria-describedby="edit_description_error" aria-invalid="true"' : '' ?>><?= sanitise($editJob['description']) ?></textarea>
                            <?php if (!empty($errors['description'])): ?>
                                <div id="edit_description_error" class="field-error invalid-feedback d-block" role="alert"><?= sanitise($errors['description']) ?></div>
                            <?php endif; ?>
                        </div>

                        <div class="d-flex gap-2">
                            <button type="submit" class="btn btn-primary">Save Changes</button>
                            <a href="/employer/manage_listings.php" class="btn btn-outline-secondary">Cancel</a>
                        </div>
                    </form>
                </div>
            <?php endif; ?>

            <!-- listings table -->
            <?php if (empty($listings)): ?>
                <div class="alert alert-info text-center py-4">
                    <strong>No listings yet.</strong>
                    <a href="/employer/post_job.php" class="btn btn-primary ms-3 btn-sm">Post Your First Job</a>
                </div>

            <?php else: ?>
                <div class="tb-card p-0 overflow-hidden">
                    <div class="table-responsive">
                        <table class="table table-hover mb-0 align-middle">
                            <thead style="background:var(--tb-light-bg)">
                                <tr>
                                    <th scope="col" class="ps-4">Job Title</th>
                                    <th scope="col">Type</th>
                                    <th scope="col">Status</th>
                                    <th scope="col">Applications</th>
                                    <th scope="col">Posted</th>
                                    <th scope="col" class="pe-4">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <?php foreach ($listings as $listing): ?>
                                    <tr>
                                        <td class="ps-4 fw-semibold">
                                            <?= sanitise($listing['title']) ?>
                                        </td>
                                        <td><?= sanitise($listing['type']) ?></td>
                                        <td>
                                            <?php
                                            $statusClass = [
                                                'active' => 'bg-success',
                                                'closed' => 'bg-secondary',
                                                'draft'  => 'bg-warning text-dark',
                                            ][$listing['status']] ?? 'bg-secondary';
                                            ?>
                                            <span class="badge <?= $statusClass ?>">
                                                <?= sanitise(ucfirst($listing['status'])) ?>
                                            </span>
                                        </td>
                                        <td>
                                            <a href="/employer/view_applicants.php?job_id=<?= (int)$listing['job_id'] ?>"
                                               class="text-decoration-none">
                                                <?= (int)$listing['app_count'] ?>
                                                <?= $listing['app_count'] == 1 ? 'applicant' : 'applicants' ?>
                                            </a>
                                        </td>
                                        <td><?= date('d M Y', strtotime($listing['created_at'])) ?></td>
                                        <td class="pe-4">
                                            <div class="d-flex gap-2 flex-nowrap">
                                                <!-- edit link — opens the inline form above -->
                                                <a href="/employer/manage_listings.php?edit=<?= (int)$listing['job_id'] ?>"
                                                   class="btn btn-outline-primary btn-sm">
                                                    Edit
                                                </a>

                                                <!-- delete — POST with CSRF, never a GET link -->
                                                <form method="post" action="/employer/manage_listings.php"
                                                      onsubmit="return confirm('Delete this listing? This cannot be undone.')">
                                                    <input type="hidden" name="csrf_token" value="<?= sanitise($csrfToken) ?>">
                                                    <input type="hidden" name="action"  value="delete">
                                                    <input type="hidden" name="job_id"  value="<?= (int)$listing['job_id'] ?>">
                                                    <button type="submit" class="btn btn-outline-danger btn-sm">Delete</button>
                                                </form>
                                            </div>
                                        </td>
                                    </tr>
                                <?php endforeach; ?>
                            </tbody>
                        </table>
                    </div>
                </div>
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
<script src="../assets/js/charcount.js"></script>
<?php if ($editJob): ?>
<script>
    document.getElementById('editJobForm').addEventListener('submit', function (e) {
        if (!validateForm(this)) e.preventDefault();
    });
    // scroll to the edit form automatically
    document.getElementById('editJobForm').scrollIntoView({ behavior: 'smooth', block: 'start' });
</script>
<?php endif; ?>
</body>
</html>
