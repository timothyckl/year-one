<?php
/**
 * Seeker applications page — lists all job applications with status badges.
 *
 * Colour-coded status badges:
 *   Pending     → grey
 *   Reviewed    → blue
 *   Shortlisted → green
 *   Rejected    → red
 *
 * @package TalentBridge
 */

session_start();
require_once '../includes/helpers.php';
require_once '../includes/auth.php';
require_once '../includes/db.php';

requireRole('seeker');

$userId = (int) $_SESSION['user_id'];

try {
    $pdo  = getConnection();
    $stmt = $pdo->prepare("
        SELECT a.application_id, a.status, a.cover_letter, a.applied_at,
               jl.job_id, jl.title, jl.location, jl.type,
               c.company_name
          FROM applications a
          JOIN job_listings jl ON jl.job_id = a.job_id
          JOIN companies c     ON c.company_id = jl.company_id
         WHERE a.user_id = :uid
         ORDER BY a.applied_at DESC
    ");
    $stmt->execute([':uid' => $userId]);
    $applications = $stmt->fetchAll();

} catch (PDOException $e) {
    $applications = [];
}

/**
 * Returns the CSS badge class for an application status.
 *
 * @param string $status - The application status string.
 * @return string  The CSS badge class name.
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
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Applications — TalentBridge</title>
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
                    <h1 class="tb-section-title mb-1">My Applications</h1>
                    <div class="tb-divider"></div>
                </div>
                <a href="/jobs.php" class="btn btn-primary btn-sm">Browse More Jobs</a>
            </div>

            <?php if (empty($applications)): ?>
                <div class="alert alert-info text-center py-4">
                    <strong>You haven't applied for any jobs yet.</strong><br>
                    <a href="/jobs.php" class="btn btn-primary mt-3">Browse Jobs</a>
                </div>

            <?php else: ?>
                <!-- status legend -->
                <div class="d-flex flex-wrap gap-2 mb-4" aria-label="Status colour legend">
                    <span class="badge badge-pending px-3 py-2">Pending</span>
                    <span class="badge badge-reviewed px-3 py-2">Reviewed</span>
                    <span class="badge badge-shortlisted px-3 py-2">Shortlisted</span>
                    <span class="badge badge-rejected px-3 py-2">Rejected</span>
                </div>

                <div class="d-flex flex-column gap-3">
                    <?php foreach ($applications as $app): ?>
                        <div class="tb-card p-3 p-md-4">
                            <div class="row align-items-start g-3">

                                <div class="col-md-8">
                                    <div class="d-flex align-items-start gap-2 flex-wrap mb-1">
                                        <h2 class="h6 fw-bold mb-0" style="color:var(--tb-primary)">
                                            <a href="/job_detail.php?id=<?= (int)$app['job_id'] ?>"
                                               style="color:inherit;text-decoration:none;">
                                                <?= sanitise($app['title']) ?>
                                            </a>
                                        </h2>
                                    </div>
                                    <p class="company-name mb-1"><?= sanitise($app['company_name']) ?></p>
                                    <div class="card-meta d-flex gap-3 flex-wrap small">
                                        <?php if ($app['location']): ?>
                                            <span><span aria-hidden="true">📍</span> <?= sanitise($app['location']) ?></span>
                                        <?php endif; ?>
                                        <span><span aria-hidden="true">📅</span> Applied <?= date('d M Y', strtotime($app['applied_at'])) ?></span>
                                    </div>

                                    <?php if ($app['cover_letter']): ?>
                                        <details class="mt-2">
                                            <summary class="text-muted small" style="cursor:pointer;">View cover letter</summary>
                                            <div class="mt-2 p-3 rounded small" style="background:var(--tb-light-bg);">
                                                <?= nl2br(sanitise($app['cover_letter'])) ?>
                                            </div>
                                        </details>
                                    <?php endif; ?>
                                </div>

                                <div class="col-md-4 text-md-end">
                                    <span class="badge <?= getStatusBadgeClass($app['status']) ?> px-3 py-2 fs-6">
                                        <?= sanitise($app['status']) ?>
                                    </span>
                                </div>

                            </div>
                        </div>
                    <?php endforeach; ?>
                </div>

                <p class="text-muted mt-3 small">
                    Showing <?= count($applications) ?> application<?= count($applications) !== 1 ? 's' : '' ?>.
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
</body>
</html>
