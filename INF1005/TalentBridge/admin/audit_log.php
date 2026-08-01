<?php
/**
 * Admin audit log page — paginated viewer for security events.
 *
 * Provides a read-only, paginated interface for administrators to review
 * all events captured in the `audit_log` table. Includes search functionality
 * to filter logs by a given term.
 *
 * @package TalentBridge
 */

session_start();
require_once '../includes/helpers.php';
require_once '../includes/auth.php';
require_once '../includes/db.php';

requireRole('admin');

// --- search & filter ---
$searchTerm = trim($_GET['q'] ?? '');

// --- pagination settings ---
$perPage     = 30;
$currentPage = max(1, (int) ($_GET['page'] ?? 1));
$offset      = ($currentPage - 1) * $perPage;

// --- build dynamic query ---
$whereClause = '';
$params = [];

if (!empty($searchTerm)) {
    // Use unique parameter names for each LIKE clause
    $whereClause = "
        WHERE al.action LIKE :search_action
           OR al.ip_address LIKE :search_ip
           OR u.name LIKE :search_name
           OR u.email LIKE :search_email
           OR al.details LIKE :search_details
    ";
    $likeTerm = "%{$searchTerm}%";
    $params[':search_action'] = $likeTerm;
    $params[':search_ip'] = $likeTerm;
    $params[':search_name'] = $likeTerm;
    $params[':search_email'] = $likeTerm;
    $params[':search_details'] = $likeTerm;
}

// ---- fetch paginated log entries ----
try {
    $pdo = getConnection();

    $countQuery = "SELECT COUNT(al.id) FROM audit_log al LEFT JOIN users u ON u.user_id = al.user_id $whereClause";
    $countStmt = $pdo->prepare($countQuery);
    $countStmt->execute($params);
    $totalLogs  = (int) $countStmt->fetchColumn();
    $totalPages = (int) ceil($totalLogs / $perPage);

    // add ordering and limits for the main data fetch
    $listQuery = "
        SELECT al.*, u.name as user_name, u.email as user_email
          FROM audit_log al
          LEFT JOIN users u ON u.user_id = al.user_id
        $whereClause
         ORDER BY al.created_at DESC
         LIMIT :limit OFFSET :offset
    ";

    $listStmt = $pdo->prepare($listQuery);

    // bind where clause params (if any)
    foreach ($params as $key => $value) {
        $listStmt->bindValue($key, $value);
    }
    // bind pagination params
    $listStmt->bindValue(':limit',  $perPage, PDO::PARAM_INT);
    $listStmt->bindValue(':offset', $offset,  PDO::PARAM_INT);

    $listStmt->execute();
    $logs = $listStmt->fetchAll();

} catch (PDOException $e) {
    error_log("Audit log page PDOException: " . $e->getMessage());
    $logs       = [];
    $totalPages = 1;
    $totalLogs  = 0;
}

?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Security Audit Log — TalentBridge Admin</title>
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
                    <h1 class="tb-section-title mb-1">Security Audit Log</h1>
                    <div class="tb-divider"></div>
                </div>
                <a href="dashboard.php" class="btn btn-outline-primary btn-sm"><span aria-hidden="true">←</span> Dashboard</a>
            </div>

            <!-- search form -->
            <div class="tb-card mb-4">
                <form id="audit-log-search-form" method="get" action="audit_log.php" class="d-flex gap-3 realtime-search-form">
                    <input type="search" id="audit-log-search-input" name="q" class="form-control"
                           aria-label="Search audit log"
                           placeholder="Search by action, user, IP, or details..." value="<?= sanitise($searchTerm) ?>">
                    <a href="audit_log.php" class="btn btn-outline-secondary btn-clear-search" style="display: <?= !empty($searchTerm) ? 'inline-block' : 'none' ?>;">Clear</a>
                </form>
            </div>


            <p id="audit-log-results-count" class="text-muted small mb-3 realtime-update-container">
                Showing <?= count($logs) ?> of <?= $totalLogs ?> event<?= $totalLogs !== 1 ? 's' : '' ?>
                <?= !empty($searchTerm) ? '(filtered)' : '' ?>
            </p>

            <div id="audit-log-table-container" class="realtime-update-container">
                <div class="tb-card p-0 overflow-hidden mb-4">
                    <div class="table-responsive">
                        <table class="table table-hover mb-0 align-middle">
                            <thead style="background:var(--tb-light-bg)">
                                <tr>
                                    <th scope="col" class="ps-4">Timestamp</th>
                                    <th scope="col">Action</th>
                                    <th scope="col">User</th>
                                    <th scope="col">IP Address</th>
                                    <th scope="col" class="pe-4">Details</th>
                                </tr>
                            </thead>
                            <tbody>
                                <?php if (empty($logs)): ?>
                                    <tr>
                                        <td colspan="5">
                                            <div class="alert alert-info text-center py-4 mb-0">
                                                <?= !empty($searchTerm) ? 'No events found matching your search term.' : 'No security events have been logged yet.' ?>
                                            </div>
                                        </td>
                                    </tr>
                                <?php else: ?>
                                    <?php foreach ($logs as $log): ?>
                                        <tr>
                                            <td class="ps-4 text-muted small" style="white-space: nowrap;"><?= date('d M Y, H:i:s', strtotime($log['created_at'])) ?></td>
                                            <td class="fw-semibold"><span class="badge bg-secondary"><?= sanitise($log['action']) ?></span></td>
                                            <td>
                                                <?php if ($log['user_id']): ?>
                                                    <div class="d-flex flex-column">
                                                        <strong class="small"><?= sanitise($log['user_name'] ?? 'N/A') ?></strong>
                                                        <small class="text-muted"><?= sanitise($log['user_email'] ?? 'ID: ' . $log['user_id']) ?></small>
                                                    </div>
                                                <?php else: ?>
                                                    <span class="text-muted small">Guest / Unauthenticated</span>
                                                <?php endif; ?>
                                            </td>
                                            <td class="text-muted small"><?= sanitise($log['ip_address']) ?></td>
                                            <td class="pe-4 small">
                                                <?php
                                                if (!empty($log['details'])) {
                                                    $details = json_decode($log['details'], true);
                                                    if (json_last_error() === JSON_ERROR_NONE) {
                                                        echo '<pre class="small bg-light p-2 rounded mb-0"><code>';
                                                        echo sanitise(print_r($details, true));
                                                        echo '</code></pre>';
                                                    } else {
                                                        echo sanitise($log['details']);
                                                    }
                                                }
                                                ?>
                                            </td>
                                        </tr>
                                    <?php endforeach; ?>
                                <?php endif; ?>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- pagination -->
                <nav id="audit-log-pagination" aria-label="Audit log pagination">
                    <?php if ($totalPages > 1): ?>
                        <ul class="pagination justify-content-center">
                            <li class="page-item <?= $currentPage <= 1 ? 'disabled' : '' ?>">
                                <a class="page-link" href="?<?= http_build_query(array_merge($_GET, ['page' => $currentPage - 1])) ?>">&laquo; Previous</a>
                            </li>
                            <?php for ($p = 1; $p <= $totalPages; $p++): ?>
                                <li class="page-item <?= $p === $currentPage ? 'active' : '' ?>">
                                    <a class="page-link" href="?<?= http_build_query(array_merge($_GET, ['page' => $p])) ?>" <?= $p === $currentPage ? 'aria-current="page"' : '' ?>><?= $p ?></a>
                                </li>
                            <?php endfor; ?>
                            <li class="page-item <?= $currentPage >= $totalPages ? 'disabled' : '' ?>">
                                <a class="page-link" href="?<?= http_build_query(array_merge($_GET, ['page' => $currentPage + 1])) ?>">Next &raquo;</a>
                            </li>
                        </ul>
                    <?php endif; ?>
                </nav>
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
<script src="../assets/js/admin_search.js"></script>
</body>
</html>
