<?php
/**
 * View applicants page — employer sees all applications for one of their listings.
 *
 * Verifies that the job belongs to this employer via a company JOIN before
 * displaying any data. Allows status updates (Reviewed/Shortlisted/Rejected)
 * via CSRF-protected POST.
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
$jobId  = (int) ($_GET['job_id'] ?? 0);

if ($jobId <= 0) {
    redirect('/employer/manage_listings.php');
}

try {
    $pdo = getConnection();

    // verify the employer owns this listing via the companies join
    $ownerStmt = $pdo->prepare("
        SELECT jl.job_id, jl.title
          FROM job_listings jl
          JOIN companies c ON c.company_id = jl.company_id
         WHERE jl.job_id = :jid AND c.user_id = :uid
    ");
    $ownerStmt->execute([':jid' => $jobId, ':uid' => $userId]);
    $listing = $ownerStmt->fetch();

} catch (PDOException $e) {
    $listing = null;
}

if (!$listing) {
    // listing not found or not owned by this employer
    http_response_code(403);
    require_once '../includes/nav.php';
    exit('<div class="container mt-5"><div class="alert alert-danger">You do not have access to this listing.</div></div>');
}

$validStatuses = ['Reviewed', 'Shortlisted', 'Rejected', 'Pending'];

// ---- handle status update POST ----
if ($_SERVER['REQUEST_METHOD'] === 'POST') {

    if (!validateCsrfToken($_POST['csrf_token'] ?? '')) {
        http_response_code(403);
        exit('Invalid CSRF token.');
    }

    $appId    = (int) ($_POST['application_id'] ?? 0);
    $newStatus = trim($_POST['status'] ?? '');

    if ($appId > 0 && in_array($newStatus, $validStatuses, true)) {
        try {
            // only update if the application belongs to a job owned by this employer
            $pdo->prepare("
                UPDATE applications a
                  JOIN job_listings jl ON jl.job_id = a.job_id
                  JOIN companies c     ON c.company_id = jl.company_id
                   SET a.status = :status
                 WHERE a.application_id = :aid
                   AND c.user_id = :uid
            ")->execute([
                ':status' => $newStatus,
                ':aid'    => $appId,
                ':uid'    => $userId,
            ]);

            setFlash('success', 'Application status updated.');

        } catch (PDOException $e) {
            setFlash('error', 'Could not update the status. Please try again.');
        }
    }

    redirect('/employer/view_applicants.php?job_id=' . $jobId);
}

// ---- fetch applicants ----
try {
    $stmt = $pdo->prepare("
        SELECT a.application_id, a.status, a.cover_letter, a.applied_at,
               u.user_id, u.name, u.email,
               sp.headline, sp.skills, sp.location, sp.cv_path
          FROM applications a
          JOIN users u            ON u.user_id = a.user_id
          LEFT JOIN seeker_profiles sp ON sp.user_id = a.user_id
         WHERE a.job_id = :jid
         ORDER BY a.applied_at ASC
    ");
    $stmt->execute([':jid' => $jobId]);
    $applicants = $stmt->fetchAll();

} catch (PDOException $e) {
    $applicants = [];
}

$flash     = getFlash();
$csrfToken = generateCsrfToken();

/**
 * Returns the CSS badge class for an application status.
 *
 * @param string $status - The application status string.
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
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Applicants for <?= sanitise($listing['title']) ?> — TalentBridge</title>
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
                    <h1 class="tb-section-title mb-1">
                        Applicants for: <?= sanitise($listing['title']) ?>
                    </h1>
                    <div class="tb-divider"></div>
                </div>
                <a href="/employer/manage_listings.php" class="btn btn-outline-primary btn-sm">
                    <span aria-hidden="true">←</span> Back to Listings
                </a>
            </div>

            <?php foreach ($flash as $type => $msg): ?>
                <div class="alert alert-<?= $type === 'error' ? 'danger' : sanitise($type) ?> tb-flash" role="alert">
                    <?= sanitise($msg) ?>
                </div>
            <?php endforeach; ?>

            <?php if (empty($applicants)): ?>
                <div class="alert alert-info text-center py-4">
                    <strong>No applications yet for this listing.</strong><br>
                    Share your job posting to attract candidates.
                </div>

            <?php else: ?>
                <p class="text-muted mb-4">
                    <?= count($applicants) ?> application<?= count($applicants) !== 1 ? 's' : '' ?> received.
                </p>

                <div class="d-flex flex-column gap-4">
                    <?php foreach ($applicants as $app): ?>
                        <div class="tb-card">
                            <div class="row align-items-start g-3">

                                <!-- applicant info -->
                                <div class="col-md-8">
                                    <div class="d-flex align-items-center gap-2 mb-1 flex-wrap">
                                        <h2 class="h6 fw-bold mb-0" style="color:var(--tb-primary)">
                                            <?= sanitise($app['name']) ?>
                                        </h2>
                                        <span class="badge <?= getStatusBadgeClass($app['status']) ?>">
                                            <?= sanitise($app['status']) ?>
                                        </span>
                                    </div>

                                    <p class="text-muted small mb-1">
                                        <a href="mailto:<?= sanitise($app['email']) ?>">
                                            <?= sanitise($app['email']) ?>
                                        </a>
                                    </p>

                                    <?php if ($app['headline']): ?>
                                        <p class="small mb-1"><em><?= sanitise($app['headline']) ?></em></p>
                                    <?php endif; ?>

                                    <?php if ($app['location']): ?>
                                        <p class="small text-muted mb-1"><span aria-hidden="true">📍</span> <?= sanitise($app['location']) ?></p>
                                    <?php endif; ?>

                                    <?php if ($app['skills']): ?>
                                        <p class="small mb-2">
                                            <strong>Skills:</strong> <?= sanitise($app['skills']) ?>
                                        </p>
                                    <?php endif; ?>

                                    <p class="text-muted small mb-2">
                                        Applied on <?= date('d M Y, g:ia', strtotime($app['applied_at'])) ?>
                                    </p>

                                    <?php if ($app['cover_letter']): ?>
                                        <details>
                                            <summary class="text-muted small" style="cursor:pointer;">
                                                View cover letter
                                            </summary>
                                            <div class="mt-2 p-3 rounded small" style="background:var(--tb-light-bg);">
                                                <?= nl2br(sanitise($app['cover_letter'])) ?>
                                            </div>
                                        </details>
                                    <?php endif; ?>

                                    <?php if ($app['cv_path']): ?>
                                        <a href="/download.php?user_id=<?= (int)$app['user_id'] ?>"
                                           class="btn btn-outline-primary btn-sm mt-2">
                                            Download CV
                                        </a>
                                    <?php else: ?>
                                        <span class="text-muted small d-block mt-2">No CV uploaded.</span>
                                    <?php endif; ?>
                                </div>

                                <!-- status update form -->
                                <div class="col-md-4">
                                    <form method="post"
                                          action="/employer/view_applicants.php?job_id=<?= $jobId ?>"
                                          class="d-flex flex-column gap-2">
                                        <input type="hidden" name="csrf_token"      value="<?= sanitise($csrfToken) ?>">
                                        <input type="hidden" name="application_id"  value="<?= (int)$app['application_id'] ?>">

                                        <label for="status_<?= (int)$app['application_id'] ?>" class="form-label small fw-semibold">
                                            Update Status
                                        </label>
                                        <select id="status_<?= (int)$app['application_id'] ?>"
                                                name="status" class="form-select form-select-sm">
                                            <?php foreach ($validStatuses as $s): ?>
                                                <option value="<?= sanitise($s) ?>"
                                                    <?= $app['status'] === $s ? 'selected' : '' ?>>
                                                    <?= sanitise($s) ?>
                                                </option>
                                            <?php endforeach; ?>
                                        </select>

                                        <button type="submit" class="btn btn-primary btn-sm">Update</button>
                                    </form>
                                </div>

                            </div>
                        </div>
                    <?php endforeach; ?>
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
</body>
</html>
