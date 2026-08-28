<?php
/**
 * Job detail page — full listing view with apply and save actions.
 *
 * Handles three POST actions on the same page:
 *   action=apply   — inserts a row into applications (seeker only)
 *   action=save    — inserts a row into saved_jobs   (seeker only)
 *   action=unsave  — deletes the saved_jobs row      (seeker only)
 *
 * The apply form is shown/hidden via application.js. The cover letter
 * textarea uses charcount.js for the character counter.
 *
 * @package TalentBridge
 */

session_start();
require_once 'includes/helpers.php';
require_once 'includes/auth.php';
require_once 'includes/db.php';
require_once 'includes/csrf.php';
require_once 'includes/review_widget.php';

$jobId = (int) ($_GET['id'] ?? 0);

if ($jobId <= 0) {
    redirect('/jobs.php');
}

// ---- handle review POST first — must run before any output ----
if ($_SERVER['REQUEST_METHOD'] === 'POST' && ($_POST['action'] ?? '') === 'review_submit') {
    $reviewTargetId = (int) ($_POST['target_id'] ?? 0);
    handleReviewPost('company', $reviewTargetId);
    // handleReviewPost always exits via redirect, so nothing below runs on review POST
}

// ---- handle POST actions ----
if ($_SERVER['REQUEST_METHOD'] === 'POST') {

    if (!validateCsrfToken($_POST['csrf_token'] ?? '')) {
        http_response_code(403);
        exit('Invalid CSRF token.');
    }

    $action = $_POST['action'] ?? '';

    // only seekers can apply or save jobs
    if (in_array($action, ['apply', 'save', 'unsave'], true)) {
        requireRole('seeker');
        $userId = (int) $_SESSION['user_id'];

        if ($action === 'apply') {
            $coverLetter = trim($_POST['cover_letter'] ?? '');

            if (strlen($coverLetter) > 2000) {
                setFlash('error', 'Cover letter must not exceed 2,000 characters.');
            } else {
                try {
                    $pdo  = getConnection();
                    $stmt = $pdo->prepare("
                        INSERT INTO applications (job_id, user_id, cover_letter)
                        VALUES (:job_id, :user_id, :cover_letter)
                    ");
                    $stmt->execute([
                        ':job_id'       => $jobId,
                        ':user_id'      => $userId,
                        ':cover_letter' => $coverLetter ?: null,
                    ]);
                    setFlash('success', 'Your application has been submitted successfully!');

                } catch (PDOException $e) {
                    // error code 23000 is an integrity constraint violation (duplicate application)
                    if ($e->getCode() === '23000') {
                        setFlash('warning', 'You have already applied for this job.');
                    } else {
                        setFlash('error', 'Could not submit your application. Please try again.');
                    }
                }
            }
            redirect('/job_detail.php?id=' . $jobId);
        }

        if ($action === 'save') {
            try {
                $pdo = getConnection();
                // insert ignore silently handles the unique constraint on duplicate saves
                $pdo->prepare("
                    INSERT IGNORE INTO saved_jobs (user_id, job_id) VALUES (:uid, :jid)
                ")->execute([':uid' => $userId, ':jid' => $jobId]);

            } catch (PDOException $e) {
                setFlash('error', 'Could not save this job. Please try again.');
            }
            redirect('/job_detail.php?id=' . $jobId);
        }

        if ($action === 'unsave') {
            try {
                $pdo = getConnection();
                $pdo->prepare("
                    DELETE FROM saved_jobs WHERE user_id = :uid AND job_id = :jid
                ")->execute([':uid' => $userId, ':jid' => $jobId]);

            } catch (PDOException $e) {
                setFlash('error', 'Could not unsave this job. Please try again.');
            }
            redirect('/job_detail.php?id=' . $jobId);
        }
    }
}

// ---- fetch job details ----
try {
    $pdo  = getConnection();
    $stmt = $pdo->prepare("
        SELECT jl.job_id, jl.title, jl.description, jl.location, jl.type,
               jl.salary_min, jl.salary_max, jl.salary_period, jl.status, jl.created_at,
               c.company_id, c.company_name, c.industry, c.description AS company_description
          FROM job_listings jl
          JOIN companies c ON c.company_id = jl.company_id
         WHERE jl.job_id = :job_id
    ");
    $stmt->execute([':job_id' => $jobId]);
    $job = $stmt->fetch();

} catch (PDOException $e) {
    $job = null;
}

if (!$job || $job['status'] !== 'active') {
    setFlash('error', 'This job listing is no longer available.');
    redirect('/jobs.php');
}

// ---- seeker-specific state ----
$hasApplied    = false;
$applicationStatus = null;
$isSaved       = false;
$userId        = isLoggedIn() ? (int) $_SESSION['user_id'] : 0;

if (isLoggedIn() && getUserRole() === 'seeker' && $userId > 0) {
    try {
        // check for existing application
        $appStmt = $pdo->prepare("
            SELECT status FROM applications
             WHERE job_id = :jid AND user_id = :uid
        ");
        $appStmt->execute([':jid' => $jobId, ':uid' => $userId]);
        $application = $appStmt->fetch();
        if ($application) {
            $hasApplied        = true;
            $applicationStatus = $application['status'];
        }

        // check if the job is saved
        $saveStmt = $pdo->prepare("
            SELECT 1 FROM saved_jobs WHERE user_id = :uid AND job_id = :jid
        ");
        $saveStmt->execute([':uid' => $userId, ':jid' => $jobId]);
        $isSaved = (bool) $saveStmt->fetch();

    } catch (PDOException $e) {
        // degrade silently — page still renders, actions may fail
    }
}

$flash     = getFlash();
$csrfToken = generateCsrfToken();

/**
 * Returns the CSS badge class for an application status string.
 *
 * @param string $status - The application status value.
 * @return string  The corresponding CSS badge class.
 */
function getStatusBadgeClass(string $status): string
{
    $map = [
        'Pending'     => 'badge-pending',
        'Reviewed'    => 'badge-reviewed',
        'Shortlisted' => 'badge-shortlisted',
        'Rejected'    => 'badge-rejected',
    ];
    return $map[$status] ?? 'bg-secondary';
}

/**
 * Returns the CSS badge class for a job type string.
 *
 * @param string $type - The job type value.
 * @return string  The corresponding CSS badge class.
 */
function getTypeBadgeClass(string $type): string
{
    $map = [
        'Full-time'  => 'badge-full-time',
        'Part-time'  => 'badge-part-time',
        'Contract'   => 'badge-contract',
        'Internship' => 'badge-internship',
        'Remote'     => 'badge-remote',
    ];
    return $map[$type] ?? 'bg-secondary';
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?= sanitise($job['title']) ?> at <?= sanitise($job['company_name']) ?> — TalentBridge</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="assets/css/style.css" rel="stylesheet">
</head>
<body>

<?php require_once 'includes/nav.php'; ?>

<main id="main-content">
    <section class="tb-section">
        <div class="container">

            <!-- breadcrumb -->
            <nav aria-label="Breadcrumb" class="mb-4">
                <ol class="breadcrumb">
                    <li class="breadcrumb-item"><a href="/jobs.php">Jobs</a></li>
                    <li class="breadcrumb-item active" aria-current="page"><?= sanitise($job['title']) ?></li>
                </ol>
            </nav>

            <!-- flash messages -->
            <?php foreach ($flash as $type => $msg): ?>
                <div class="alert alert-<?= $type === 'error' ? 'danger' : sanitise($type) ?> tb-flash" role="alert">
                    <?= sanitise($msg) ?>
                </div>
            <?php endforeach; ?>

            <div class="row g-4">

                <!-- main job content -->
                <div class="col-lg-8">
                    <div class="tb-card">

                        <!-- job header -->
                        <div class="d-flex justify-content-between align-items-start flex-wrap gap-2 mb-3">
                            <div>
                                <h1 class="h3 fw-bold mb-1" style="color:var(--tb-primary)">
                                    <?= sanitise($job['title']) ?>
                                </h1>
                                <p class="company-name fs-6 mb-0"><?= sanitise($job['company_name']) ?></p>
                            </div>
                            <span class="badge badge-type <?= getTypeBadgeClass($job['type']) ?>">
                                <?= sanitise($job['type']) ?>
                            </span>
                        </div>

                        <!-- job meta -->
                        <div class="d-flex flex-wrap gap-3 mb-4 text-muted small">
                            <?php if ($job['location']): ?>
                                <span><span aria-hidden="true">📍</span> <?= sanitise($job['location']) ?></span>
                            <?php endif; ?>
                            <?php if ($job['salary_min']): ?>
                                <span><span aria-hidden="true">💰</span> <?= sanitise(formatSalary($job['salary_min'], $job['salary_max'], $job['salary_period'])) ?></span>
                            <?php endif; ?>
                            <?php if ($job['industry']): ?>
                                <span><span aria-hidden="true">🏢</span> <?= sanitise($job['industry']) ?></span>
                            <?php endif; ?>
                            <span><span aria-hidden="true">📅</span> Posted <?= date('d M Y', strtotime($job['created_at'])) ?></span>
                        </div>

                        <hr>

                        <!-- job description -->
                        <div class="mb-4">
                            <h2 class="h5 fw-bold mb-3" style="color:var(--tb-primary)">Job Description</h2>
                            <div class="job-description">
                                <?= nl2br(sanitise($job['description'])) ?>
                            </div>
                        </div>

                        <!-- apply / already applied section -->
                        <?php if (!isLoggedIn()): ?>
                            <div class="alert alert-info">
                                <a href="/login.php">Log in</a> or <a href="/register.php?role=seeker">register as a seeker</a>
                                to apply for this role.
                            </div>

                        <?php elseif (getUserRole() === 'seeker'): ?>
                            <?php if ($hasApplied): ?>
                                <div class="alert alert-success d-flex align-items-center gap-3" role="status">
                                    <div>
                                        <strong>You have already applied for this job.</strong><br>
                                        Application status:
                                        <span class="badge ms-1 <?= getStatusBadgeClass($applicationStatus) ?>">
                                            <?= sanitise($applicationStatus) ?>
                                        </span>
                                    </div>
                                </div>
                            <?php else: ?>
                                <!-- toggle button — application.js manages visibility -->
                                <button id="applyToggleBtn" type="button"
                                    class="btn btn-primary btn-lg"
                                    aria-expanded="false"
                                    aria-controls="applyFormPanel">
                                    Apply Now
                                </button>

                                <!-- application form — hidden until toggle -->
                                <div id="applyFormPanel" class="mt-4" hidden>
                                    <div class="tb-card" style="background:var(--tb-light-bg);border-color:var(--tb-border)">
                                        <h2 class="h5 fw-bold mb-3" style="color:var(--tb-primary)">
                                            Submit Your Application
                                        </h2>

                                        <form method="post" action="/job_detail.php?id=<?= $jobId ?>" novalidate>
                                            <input type="hidden" name="csrf_token" value="<?= sanitise($csrfToken) ?>">
                                            <input type="hidden" name="action" value="apply">

                                            <div class="mb-4">
                                                <label for="cover_letter" class="form-label">
                                                    Cover Letter <span class="text-muted small">(optional, max 2,000 characters)</span>
                                                </label>
                                                <textarea id="cover_letter" name="cover_letter"
                                                    class="form-control" rows="8"
                                                    data-charcount="true"
                                                    data-maxlength="2000"
                                                    placeholder="Tell the employer why you're a great fit for this role…"></textarea>
                                            </div>

                                            <div class="d-flex gap-2">
                                                <button type="submit" class="btn btn-primary px-4">
                                                    Submit Application
                                                </button>
                                            </div>
                                        </form>
                                    </div>
                                </div>
                            <?php endif; ?>

                        <?php elseif (getUserRole() === 'employer'): ?>
                            <div class="alert alert-info">
                                Employer accounts cannot apply for jobs.
                            </div>
                        <?php endif; ?>

                    </div>

                    <!-- =====================================================
                         REVIEWS — inserted here (Step 3)
                         $job['company_id'] is already fetched above.
                         pathPrefix '' = we are at the root level.
                         ===================================================== -->
                    <?php renderReviews('company', (int) $job['company_id']); ?>

                </div>

                <!-- sidebar -->
                <div class="col-lg-4">

                    <!-- save job card (seekers only) -->
                    <?php if (isLoggedIn() && getUserRole() === 'seeker'): ?>
                        <div class="tb-card mb-4">
                            <h2 class="h6 fw-bold mb-3" style="color:var(--tb-primary)">Save This Job</h2>
                            <form method="post" action="/job_detail.php?id=<?= $jobId ?>">
                                <input type="hidden" name="csrf_token" value="<?= sanitise($csrfToken) ?>">
                                <input type="hidden" name="action" value="<?= $isSaved ? 'unsave' : 'save' ?>">
                                <button type="submit"
                                    class="btn w-100 <?= $isSaved ? 'btn-outline-secondary' : 'btn-outline-primary' ?>">
                                    <span aria-hidden="true">🔖</span> <?= $isSaved ? 'Saved — Click to Unsave' : 'Save Job' ?>
                                </button>
                            </form>
                        </div>
                    <?php endif; ?>

                    <!-- company info card -->
                    <div class="tb-card mb-4">
                        <h2 class="h6 fw-bold mb-3" style="color:var(--tb-primary)">About the Company</h2>
                        <p class="fw-bold mb-1"><?= sanitise($job['company_name']) ?></p>
                        <?php if ($job['industry']): ?>
                            <p class="text-muted small mb-2">Industry: <?= sanitise($job['industry']) ?></p>
                        <?php endif; ?>
                        <?php if ($job['company_description']): ?>
                            <p class="text-muted small mb-0"><?= nl2br(sanitise($job['company_description'])) ?></p>
                        <?php else: ?>
                            <p class="text-muted small mb-0">No company description provided.</p>
                        <?php endif; ?>
                    </div>

                    <!-- job summary card -->
                    <div class="tb-card">
                        <h2 class="h6 fw-bold mb-3" style="color:var(--tb-primary)">Job Summary</h2>
                        <dl class="mb-0 small">
                            <dt class="text-muted">Job Type</dt>
                            <dd class="mb-2"><?= sanitise($job['type']) ?></dd>

                            <?php if ($job['location']): ?>
                                <dt class="text-muted">Location</dt>
                                <dd class="mb-2"><?= sanitise($job['location']) ?></dd>
                            <?php endif; ?>

                            <?php if ($job['salary_min']): ?>
                                <dt class="text-muted">Salary Range</dt>
                                <dd class="mb-2"><?= sanitise(formatSalary($job['salary_min'], $job['salary_max'], $job['salary_period'])) ?></dd>
                            <?php endif; ?>

                            <dt class="text-muted">Date Posted</dt>
                            <dd class="mb-0"><?= date('d M Y', strtotime($job['created_at'])) ?></dd>
                        </dl>
                    </div>

                </div>
            </div>

        </div>
    </section>
</main>

<?php require_once 'includes/footer.php'; ?>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="assets/js/application.js"></script>
<script src="assets/js/charcount.js"></script>
</body>
</html>