<?php
/**
 * Saved jobs page — seeker's bookmarked job listings.
 *
 * Lists all saved jobs and provides a one-click POST unsave action
 * (CSRF protected). Saving happens via job_detail.php.
 *
 * @package TalentBridge
 */

session_start();
require_once '../includes/helpers.php';
require_once '../includes/auth.php';
require_once '../includes/db.php';
require_once '../includes/csrf.php';

requireRole('seeker');

$userId = (int) $_SESSION['user_id'];

// ---- handle unsave POST ----
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if (!validateCsrfToken($_POST['csrf_token'] ?? '')) {
        http_response_code(403);
        exit('Invalid CSRF token.');
    }

    $jobId = (int) ($_POST['job_id'] ?? 0);
    if ($jobId > 0) {
        try {
            $pdo = getConnection();
            $pdo->prepare("
                DELETE FROM saved_jobs WHERE user_id = :uid AND job_id = :jid
            ")->execute([':uid' => $userId, ':jid' => $jobId]);

        } catch (PDOException $e) {
            setFlash('error', 'Could not remove the saved job. Please try again.');
        }
    }
    redirect('/seeker/saved_jobs.php');
}

// ---- fetch saved jobs ----
try {
    $pdo  = getConnection();
    $stmt = $pdo->prepare("
        SELECT sj.save_id, sj.saved_at,
               jl.job_id, jl.title, jl.location, jl.type, jl.salary_min, jl.salary_max, jl.salary_period, jl.status,
               c.company_name, c.industry
          FROM saved_jobs sj
          JOIN job_listings jl ON jl.job_id = sj.job_id
          JOIN companies c     ON c.company_id = jl.company_id
         WHERE sj.user_id = :uid
         ORDER BY sj.saved_at DESC
    ");
    $stmt->execute([':uid' => $userId]);
    $savedJobs = $stmt->fetchAll();

} catch (PDOException $e) {
    $savedJobs = [];
}

$flash     = getFlash();
$csrfToken = generateCsrfToken();

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
    <title>Saved Jobs — TalentBridge</title>
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
                    <h1 class="tb-section-title mb-1">Saved Jobs</h1>
                    <div class="tb-divider"></div>
                </div>
                <a href="/jobs.php" class="btn btn-primary btn-sm">Browse More Jobs</a>
            </div>

            <?php foreach ($flash as $type => $msg): ?>
                <div class="alert alert-<?= $type === 'error' ? 'danger' : sanitise($type) ?> tb-flash" role="alert">
                    <?= sanitise($msg) ?>
                </div>
            <?php endforeach; ?>

            <?php if (empty($savedJobs)): ?>
                <div class="alert alert-info text-center py-4">
                    <strong>You haven't saved any jobs yet.</strong><br>
                    <a href="/jobs.php" class="btn btn-primary mt-3">Browse Jobs</a>
                </div>

            <?php else: ?>
                <div class="d-flex flex-column gap-3">
                    <?php foreach ($savedJobs as $job): ?>
                        <div class="tb-card p-3 p-md-4">
                            <div class="row align-items-center g-3">

                                <div class="col-md-8">
                                    <div class="d-flex align-items-start gap-2 flex-wrap mb-1">
                                        <h2 class="h6 fw-bold mb-0" style="color:var(--tb-primary)">
                                            <?= sanitise($job['title']) ?>
                                        </h2>
                                        <span class="badge badge-type <?= getTypeBadgeClass($job['type']) ?>">
                                            <?= sanitise($job['type']) ?>
                                        </span>
                                        <?php if ($job['status'] !== 'active'): ?>
                                            <span class="badge bg-secondary">Closed</span>
                                        <?php endif; ?>
                                    </div>

                                    <p class="company-name mb-1"><?= sanitise($job['company_name']) ?></p>

                                    <div class="card-meta d-flex gap-3 flex-wrap small">
                                        <?php if ($job['location']): ?>
                                            <span><span aria-hidden="true">📍</span> <?= sanitise($job['location']) ?></span>
                                        <?php endif; ?>
                                        <?php if ($job['salary_min']): ?>
                                            <span><span aria-hidden="true">💰</span> <?= sanitise(formatSalary($job['salary_min'], $job['salary_max'], $job['salary_period'])) ?></span>
                                        <?php endif; ?>
                                        <span><span aria-hidden="true">🔖</span> Saved <?= date('d M Y', strtotime($job['saved_at'])) ?></span>
                                    </div>
                                </div>

                                <div class="col-md-4 d-flex gap-2 justify-content-md-end flex-wrap">
                                    <?php if ($job['status'] === 'active'): ?>
                                        <a href="/job_detail.php?id=<?= (int)$job['job_id'] ?>"
                                           class="btn btn-primary btn-sm">
                                            View &amp; Apply
                                        </a>
                                    <?php endif; ?>

                                    <!-- unsave form — POST with CSRF, never a GET link -->
                                    <form method="post" action="/seeker/saved_jobs.php"
                                          onsubmit="return confirm('Remove this job from your saved list?')">
                                        <input type="hidden" name="csrf_token" value="<?= sanitise($csrfToken) ?>">
                                        <input type="hidden" name="job_id" value="<?= (int)$job['job_id'] ?>">
                                        <button type="submit" class="btn btn-outline-secondary btn-sm">
                                            Unsave
                                        </button>
                                    </form>
                                </div>

                            </div>
                        </div>
                    <?php endforeach; ?>
                </div>

                <p class="text-muted mt-3 small">
                    <?= count($savedJobs) ?> saved job<?= count($savedJobs) !== 1 ? 's' : '' ?>.
                </p>
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
