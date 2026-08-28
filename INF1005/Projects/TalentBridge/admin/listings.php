<?php
/**
 * Admin listings page — platform-wide job listing management.
 *
 * Allows the admin to change the status of any listing or permanently
 * delete it. All state-changing actions use POST with CSRF.
 *
 * @package TalentBridge
 */

session_start();
require_once '../includes/helpers.php';
require_once '../includes/auth.php';
require_once '../includes/db.php';
require_once '../includes/csrf.php';

requireRole('admin');

// error_log("DEBUG: listings.php - GET parameters: " . print_r($_GET, true));

$validStatuses = ['active', 'closed', 'draft'];

// pagination settings
$perPage = (int) max(5, min(100, (int) ($_GET['per_page'] ?? 20)));
$pageNum = (int) max(1, (int) ($_GET['page'] ?? 1)); 
$offset  = (int) (($pageNum - 1) * $perPage);

// optional status filter
$filterStatus = $_GET['status'] ?? '';
if (!in_array($filterStatus, array_merge($validStatuses, ['']), true)) {
    $filterStatus = '';
}

// optional type filter
$filterType = $_GET['type'] ?? '';
// Validation for type will happen after fetching valid types from DB.

// search functionality
$searchTerm = trim($_GET['search'] ?? '');
$searchTerm = strlen($searchTerm) > 0 ? $searchTerm : '';

// sorting functionality
$sortBy    = $_GET['sort_by'] ?? 'created_at';
$sortOrder = strtoupper($_GET['sort_order'] ?? 'DESC');
$validSortColumns = ['title', 'company_name', 'type', 'status', 'created_at', 'app_count'];
$validSortOrders  = ['ASC', 'DESC'];

if (!in_array($sortBy, $validSortColumns, true)) {
    $sortBy = 'created_at';
}
if (!in_array($sortOrder, $validSortOrders, true)) {
    $sortOrder = 'DESC';
}

// ---- handle POST actions ----
if ($_SERVER['REQUEST_METHOD'] === 'POST') {

    if (!validateCsrfToken($_POST['csrf_token'] ?? '')) {
        http_response_code(403);
        exit('Invalid CSRF token.');
    }

    // ---- handle BULK actions ----
    if (isset($_POST['bulk_action']) && !empty($_POST['bulk_action'])) {
        $bulkAction = $_POST['bulk_action'];
        $jobIds = $_POST['job_ids'] ?? [];

        if (!empty($jobIds) && is_array($jobIds)) {
            $jobIds = array_map('intval', array_filter($jobIds, 'is_numeric'));

            if (!empty($jobIds)) {
                try {
                    $pdo = getConnection();
                    $placeholders = implode(',', array_fill(0, count($jobIds), '?'));
                    $sql = '';
                    if ($bulkAction === 'delete') {
                        $sql = "DELETE FROM job_listings WHERE job_id IN ($placeholders)";
                        $stmt = $pdo->prepare($sql);
                        $stmt->execute($jobIds);
                        log_audit_event('BULK_LISTING_DELETE', $_SESSION['user_id'], ['target_job_ids' => $jobIds]);
                        setFlash('success', "{$stmt->rowCount()} listing(s) deleted.");
                    } elseif (in_array($bulkAction, $validStatuses, true)) {
                        $sql = "UPDATE job_listings SET status = ? WHERE job_id IN ($placeholders)";
                        $stmt = $pdo->prepare($sql);
                        $stmt->execute(array_merge([$bulkAction], $jobIds));
                        log_audit_event('BULK_LISTING_STATUS_CHANGE', $_SESSION['user_id'], ['target_job_ids' => $jobIds, 'new_status' => $bulkAction]);
                        setFlash('success', "{$stmt->rowCount()} listing(s) updated.");
                    }
                } catch (PDOException $e) {
                    setFlash('error', 'Bulk action failed. Please try again.');
                }
            }
        } else {
            setFlash('error', 'No listings were selected.');
        }
    } else { // ---- handle SINGLE actions ----
        $action = $_POST['action'] ?? '';
        $jobId  = (int) ($_POST['job_id'] ?? 0);

        if ($jobId > 0) {
            try {
                $pdo = getConnection();
                if ($action === 'delete') {
                    $pdo->prepare("DELETE FROM job_listings WHERE job_id = :jid")->execute([':jid' => $jobId]);
                    log_audit_event('LISTING_DELETED', $_SESSION['user_id'], ['target_job_id' => $jobId]);
                    setFlash('success', 'Listing deleted.');
                } elseif ($action === 'set_status') {
                    $newStatus = trim($_POST['status'] ?? '');
                    if (in_array($newStatus, $validStatuses, true)) {
                        $pdo->prepare("UPDATE job_listings SET status = :status WHERE job_id = :jid")->execute([':status' => $newStatus, ':jid' => $jobId]);
                        log_audit_event('LISTING_STATUS_CHANGE', $_SESSION['user_id'], ['target_job_id' => $jobId, 'new_status' => $newStatus]);
                        setFlash('success', 'Listing status updated.');
                    }
                }
            } catch (PDOException $e) {
                setFlash('error', 'Action failed. Please try again.');
            }
        }
    }


    // after POST, preserve the filters if they were active
    $intendedFilterStatus = $_POST['intended_status'] ?? '';
    $intendedPage = (int)($_POST['intended_page'] ?? 1);
    $intendedSearch = trim($_POST['intended_search'] ?? '');
    $intendedPerPage = max(5, min(100, (int)($_POST['intended_per_page'] ?? 20)));
    if ($intendedPage < 1) $intendedPage = 1;
    $intendedSortBy = $_POST['intended_sort_by'] ?? 'created_at';
    $intendedSortOrder = strtoupper($_POST['intended_sort_order'] ?? 'DESC');
    
    if (!in_array($intendedFilterStatus, array_merge($validStatuses, ['']), true)) $intendedFilterStatus = '';
    if (!in_array($intendedSortBy, $validSortColumns, true)) $intendedSortBy = 'created_at';
    if (!in_array($intendedSortOrder, $validSortOrders, true)) $intendedSortOrder = 'DESC';
    
    $redirectUrl = '/admin/listings.php';
    $params = [];
    if ($intendedFilterStatus) {
        $params[] = 'status=' . urlencode($intendedFilterStatus);
    }
    if ($_POST['intended_type'] ?? '') {
        $params[] = 'type=' . urlencode($_POST['intended_type']);
    }
    if ($intendedPage > 1) {
        $params[] = 'page=' . $intendedPage;
    }
    if (strlen($intendedSearch) > 0) {
        $params[] = 'search=' . urlencode($intendedSearch);
    }
    if ($intendedPerPage != 20) {
        $params[] = 'per_page=' . $intendedPerPage;
    }
    if ($intendedSortBy != 'created_at' || $intendedSortOrder != 'DESC') {
        $params[] = 'sort_by=' . urlencode($intendedSortBy);
        $params[] = 'sort_order=' . urlencode($intendedSortOrder);
    }
    if ($params) {
        $redirectUrl .= '?' . implode('&', $params);
    }
    redirect($redirectUrl);
}

// ---- fetch paginated listings ----
try {
    $pdo = getConnection();

    // Fetch distinct job types for the filter dropdown
    $typeStmt = $pdo->query("SELECT DISTINCT type FROM job_listings WHERE type IS NOT NULL AND type != '' ORDER BY type ASC");
    $validTypes = $typeStmt->fetchAll(PDO::FETCH_COLUMN);
    if (!in_array($filterType, $validTypes)) $filterType = '';

    // build a parameterised where clause depending on the status filter and search term
    $whereConditions = [];
    $params = [];
    
    if ($filterStatus) {
        $whereConditions[] = 'jl.status = :status';
        $params[':status'] = $filterStatus;
    }

    if ($filterType) {
        $whereConditions[] = 'jl.type = :type';
        $params[':type'] = $filterType;
    }
    
    if (strlen($searchTerm) > 0) {
        $whereConditions[] = '(jl.title LIKE :search_title OR c.company_name LIKE :search_company)';
        $params[':search_title'] = '%' . $searchTerm . '%';
        $params[':search_company'] = '%' . $searchTerm . '%';
    }
    
    $whereClause = count($whereConditions) > 0 ? 'WHERE ' . implode(' AND ', $whereConditions) : '';

    // Count total listings
    $countStmt = $pdo->prepare("
        SELECT COUNT(*) FROM job_listings jl
        JOIN companies c ON c.company_id = jl.company_id
        $whereClause
    ");
    $countStmt->execute($params);
    $totalListings = (int) $countStmt->fetchColumn();
    $totalPages    = (int) ceil($totalListings / $perPage);
    
    // Clamp currentPage to valid range now that we know totalPages
    $pageNum = (int) max(1, min($pageNum, max(1, $totalPages)));
    $offset = ($pageNum - 1) * $perPage;

    // Fetch paginated listings
    $listStmt = $pdo->prepare("
        SELECT jl.job_id, jl.title, jl.type, jl.location, jl.status, jl.created_at,
               c.company_name,
               (SELECT COUNT(*) FROM applications WHERE job_id = jl.job_id) AS app_count
          FROM job_listings jl
          JOIN companies c ON c.company_id = jl.company_id
        $whereClause
         ORDER BY $sortBy $sortOrder
         LIMIT :limit OFFSET :offset
    ");
    $listStmt->bindValue(':limit',  $perPage, PDO::PARAM_INT);
    $listStmt->bindValue(':offset', $offset,  PDO::PARAM_INT);
    foreach ($params as $key => $value) {
        $listStmt->bindValue($key, $value);
    }
    $listStmt->execute();
    $listings = $listStmt->fetchAll();

} catch (PDOException $e) {
    error_log('Listings page PDOException: ' . $e->getMessage());
    $listings = [];
    $totalPages = 1;
    $totalListings = 0;
}

$flash     = getFlash();
$csrfToken = generateCsrfToken();

/**
 * Build a pagination URL preserving all active filters/sort state.
 */
function buildPaginationUrl(int $page, string $filterStatus, string $filterType, string $searchTerm, int $perPage, string $sortBy, string $sortOrder): string {
    $page    = (int) $page;
    $perPage = (int) $perPage;
    $params  = ['page=' . $page];
    if ($filterStatus)            $params[] = 'status='     . urlencode($filterStatus);
    if ($filterType)              $params[] = 'type='       . urlencode($filterType);
    if (strlen($searchTerm) > 0)  $params[] = 'search='     . urlencode($searchTerm);
    if ($perPage !== 20)          $params[] = 'per_page='   . $perPage;
    if ($sortBy !== 'created_at' || $sortOrder !== 'DESC') {
        $params[] = 'sort_by='    . urlencode($sortBy);
        $params[] = 'sort_order=' . urlencode($sortOrder);
    }
    $url = '/admin/listings.php?' . implode('&', $params);
    return $url;
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage Listings — TalentBridge Admin</title>
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
                <a href="/admin/dashboard.php" class="btn btn-outline-secondary btn-sm"><span aria-hidden="true">←</span> Dashboard</a>
            </div>

            <?php foreach ($flash as $type => $msg): ?>
                <div class="alert alert-<?= $type === 'error' ? 'danger' : sanitise($type) ?> tb-flash" role="alert">
                    <?= sanitise($msg) ?>
                </div>
            <?php endforeach; ?>

            <!-- status and type filter tabs -->
            <div id="listings-filter-container" class="realtime-update-container">
                <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3">
                    <nav aria-label="Filter listings by type" class="flex-shrink-0">
                        <ul class="nav nav-pills gap-1">
                            <?php
                            $typeTabs = array_merge(['' => 'All Types'], array_combine($validTypes, $validTypes));
                            foreach ($typeTabs as $typeVal => $typeLabel):
                                $href = '/admin/listings.php';
                                $params = [];
                                if ($filterStatus) { $params[] = 'status=' . urlencode($filterStatus); }
                                if ($typeVal) { $params[] = 'type=' . urlencode($typeVal); }
                                if (strlen($searchTerm) > 0) { $params[] = 'search=' . urlencode($searchTerm); }
                                if ($perPage != 20) { $params[] = 'per_page=' . $perPage; }
                                
                                // NEW: Preserve sorting state in filter links
                                $params[] = 'sort_by=' . urlencode($sortBy);
                                $params[] = 'sort_order=' . urlencode($sortOrder);

                                if ($params) { $href .= '?' . implode('&', $params); }
                            ?>
                                <li class="nav-item">
                                    <a class="nav-link <?= $filterType === $typeVal ? 'active' : '' ?>"
                                       href="<?= sanitise($href) ?>"
                                       <?= $filterType === $typeVal ? 'aria-current="true"' : '' ?>>
                                        <?= sanitise($typeLabel) ?>
                                    </a>
                                </li>
                            <?php endforeach; ?>
                        </ul>
                    </nav>
                    <nav aria-label="Filter listings by status" class="flex-shrink-0">
                        <ul class="nav nav-pills gap-1">
                            <?php
                            $statusTabs = ['' => 'All Statuses', 'active' => 'Active', 'closed' => 'Closed', 'draft' => 'Draft'];
                            foreach ($statusTabs as $s => $label):
                                $href = '/admin/listings.php';
                                $params = [];
                                if ($s) { $params[] = 'status=' . urlencode($s); }
                                if ($filterType) { $params[] = 'type=' . urlencode($filterType); }
                                if (strlen($searchTerm) > 0) { $params[] = 'search=' . urlencode($searchTerm); }
                                if ($perPage != 20) { $params[] = 'per_page=' . $perPage; }
                                
                                // NEW: Preserve sorting state in filter links
                                $params[] = 'sort_by=' . urlencode($sortBy);
                                $params[] = 'sort_order=' . urlencode($sortOrder);

                                if ($params) { $href .= '?' . implode('&', $params); }
                            ?>
                                <li class="nav-item">
                                    <a class="nav-link <?= $filterStatus === $s ? 'active' : '' ?>"
                                       href="<?= sanitise($href) ?>"
                                       <?= $filterStatus === $s ? 'aria-current="page"' : '' ?>>
                                        <?= sanitise($label) ?>
                                    </a>
                                </li>
                            <?php endforeach; ?>
                        </ul>
                    </nav>
                </div>
            </div>

            <!-- search & per-page controls -->
            <div class="d-flex gap-3 mb-4 flex-wrap align-items-center justify-content-between">
                <form method="get" action="/admin/listings.php" class="d-flex gap-2 flex-grow-1 realtime-search-form" style="max-width: 400px;">
                    <input type="hidden" name="status" value="<?= sanitise($filterStatus) ?>">
                    <input type="hidden" name="type" value="<?= sanitise($filterType) ?>">
                    <input type="hidden" name="per_page" value="<?= (int)$perPage ?>">
                    <input type="hidden" name="sort_by" value="<?= sanitise($sortBy) ?>">
                    <input type="hidden" name="sort_order" value="<?= sanitise($sortOrder) ?>">
                    <label for="listings-search" class="visually-hidden">Search listings</label>
                    <input type="text" name="search" id="listings-search" class="form-control form-control-sm"
                           placeholder="Search by job title or company..." value="<?= sanitise($searchTerm) ?>">
                    <a href="/admin/listings.php?status=<?= sanitise($filterStatus) ?>&type=<?= sanitise($filterType) ?>" class="btn btn-sm btn-outline-secondary btn-clear-search" style="display: <?= strlen($searchTerm) > 0 ? 'inline-block' : 'none' ?>;">Clear</a>
                </form>
                
                <select name="per_page" id="perPageSelect" class="form-select form-select-sm" style="max-width: 120px;" aria-label="Listings per page">
                    <option value="5" <?= $perPage == 5 ? 'selected' : '' ?>>5 per page</option>
                    <option value="10" <?= $perPage == 10 ? 'selected' : '' ?>>10 per page</option>
                    <option value="20" <?= $perPage == 20 ? 'selected' : '' ?>>20 per page</option>
                    <option value="50" <?= $perPage == 50 ? 'selected' : '' ?>>50 per page</option>
                    <option value="100" <?= $perPage == 100 ? 'selected' : '' ?>>100 per page</option>
                </select>
            </div>

            <script>
                document.getElementById('perPageSelect').addEventListener('change', function() {
                    const hiddenPerPage = document.querySelector('.realtime-search-form input[name="per_page"]');
                    if (hiddenPerPage) hiddenPerPage.value = this.value;

                    const url = new URL(window.location.href);
                    url.searchParams.set('per_page', this.value);
                    url.searchParams.set('page', '1'); 
                    window.location.href = url.toString();
                });
            </script>

            <form method="post" action="/admin/listings.php" id="bulk-actions-form">
                <input type="hidden" name="csrf_token" value="<?= sanitise($csrfToken) ?>">
                <input type="hidden" name="intended_status"   value="<?= sanitise($filterStatus) ?>">
                <input type="hidden" name="intended_page"     value="<?= (int)$pageNum ?>">
                <input type="hidden" name="intended_type"     value="<?= sanitise($filterType) ?>">
                <input type="hidden" name="intended_search"   value="<?= sanitise($searchTerm) ?>">
                <input type="hidden" name="intended_per_page" value="<?= (int)$perPage ?>">
                <input type="hidden" name="intended_sort_by"  value="<?= sanitise($sortBy) ?>">
                <input type="hidden" name="intended_sort_order" value="<?= sanitise($sortOrder) ?>">

                <div class="d-flex justify-content-between align-items-center mb-3">
                    <div id="listings-count-summary" class="realtime-update-container">
                        <p class="text-muted small mb-0">
                            Showing <?= count($listings) ?> of <?= $totalListings ?> listing<?= $totalListings !== 1 ? 's' : '' ?>
                            <?= $filterStatus ? '(' . sanitise(ucfirst($filterStatus)) . ' only)' : '' ?>
                            <?= $filterType ? '(' . sanitise($filterType) . ' only)' : '' ?>
                            <?= strlen($searchTerm) > 0 ? ' (filtered)' : '' ?>
                        </p>
                    </div>
                    <!-- bulk action controls -->
                    <div class="d-flex gap-2 align-items-center" id="bulk-actions-controls" style="display: none;">
                        <label for="bulk-action-select" class="visually-hidden">Bulk Actions</label>
                        <select name="bulk_action" id="bulk-action-select" class="form-select form-select-sm" style="width: auto;" required>
                            <option value="">Bulk Actions...</option>
                            <option value="active">Set to Active</option>
                            <option value="closed">Set to Closed</option>
                            <option value="draft">Set to Draft</option>
                            <option value="delete">Delete</option>
                        </select>
                        <button type="submit" class="btn btn-sm btn-primary">Apply</button>
                    </div>
                </div>

            <div id="listings-table-container" class="realtime-update-container">

                <?php if (empty($listings)): ?>
                    <div class="alert alert-info text-center py-4">No listings found.</div>

                <?php else: ?>

                <div class="tb-card p-0 overflow-hidden mb-4">
                    <div class="table-responsive">
                        <table class="table table-hover mb-0 align-middle">
                            <thead style="background:var(--tb-light-bg)">
                                <tr>
                                    <th class="ps-3" style="width: 1%;"><span class="visually-hidden">Select listing for bulk actions</span></th>
                                    <?php
                                    $sortableColumns = [
                                        'title' => 'Job Title',
                                        'company_name' => 'Company',
                                        'type' => 'Type',
                                        'status' => 'Status',
                                        'created_at' => 'Posted',
                                        'app_count' => 'Apps'
                                    ];
                                    
                                    foreach ($sortableColumns as $colName => $colLabel):
                                        $newSortOrder = 'ASC';
                                        $indicator = '';
                                        
                                        if ($sortBy === $colName) {
                                            $newSortOrder = $sortOrder === 'ASC' ? 'DESC' : 'ASC';
                                            $indicator = $sortOrder === 'ASC' ? ' ▲' : ' ▼';
                                        }
                                        
                                        $href = '/admin/listings.php?sort_by=' . urlencode($colName) . '&sort_order=' . $newSortOrder;
                                        if ($filterStatus) $href .= '&status=' . urlencode($filterStatus);
                                        if ($filterType) $href .= '&type=' . urlencode($filterType);
                                        if (strlen($searchTerm) > 0) $href .= '&search=' . urlencode($searchTerm);
                                        if ($perPage != 20) $href .= '&per_page=' . $perPage;
                                        // if ($pageNum > 1) $href .= '&page=' . (int)$pageNum;
                                        
                                        $thClass = ($colName === 'title' ? 'ps-4' : '');
                                        $ariaSortVal = 'none';
                                        if ($sortBy === $colName) {
                                            $ariaSortVal = $sortOrder === 'ASC' ? 'ascending' : 'descending';
                                        }
                                    ?>
                                        <th scope="col" class="<?= $thClass ?>" style="cursor: pointer;"
                                            aria-sort="<?= $ariaSortVal ?>">
                                            <a href="<?= sanitise($href) ?>" class="text-decoration-none" style="color: inherit;">
                                                <?= sanitise($colLabel) ?><?php if ($indicator): ?><span aria-hidden="true"><?= $indicator ?></span><?php endif; ?>
                                            </a>
                                        </th>
                                    <?php endforeach; ?>
                                    <th scope="col" class="pe-4">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <?php foreach ($listings as $listing): ?>
                                    <tr>
                                        <td class="ps-3">
                                            <input type="checkbox" name="job_ids[]" value="<?= (int)$listing['job_id'] ?>" class="bulk-checkbox" aria-label="Select listing <?= sanitise($listing['title']) ?>">
                                        </td>
                                        <td class="ps-4 fw-semibold">
                                            <a href="/job_detail.php?id=<?= (int)$listing['job_id'] ?>"
                                               class="text-decoration-none" style="color:var(--tb-primary)"
                                               target="_blank" rel="noopener noreferrer">
                                                <?= sanitise($listing['title']) ?>
                                            </a>
                                        </td>
                                        <td class="text-muted small"><?= sanitise($listing['company_name']) ?></td>
                                        <td class="small"><?= sanitise($listing['type']) ?></td>
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
                                        <td class="text-muted small">
                                            <?= date('d M Y', strtotime($listing['created_at'])) ?>
                                        </td>
                                        <td class="text-muted small"><?= (int)$listing['app_count'] ?></td>
                                        <td class="pe-4">
                                            <div class="d-flex gap-2 flex-nowrap">
                                                <!-- status change dropdown form -->
                                                <select name="status" class="form-select form-select-sm single-action-status-change"
                                                        style="width: 110px; height: 31px;"
                                                        aria-label="Change listing status"
                                                        data-job-id="<?= (int)$listing['job_id'] ?>">
                                                    <?php foreach ($validStatuses as $s): ?>
                                                        <option value="<?= sanitise($s) ?>"
                                                            <?= $listing['status'] === $s ? 'selected' : '' ?>>
                                                            <?= sanitise(ucfirst($s)) ?>
                                                        </option>
                                                    <?php endforeach; ?>
                                                </select>

                                                <!-- delete -->
                                                <button type="button" class="btn btn-sm btn-outline-danger single-action-delete-btn"
                                                        data-job-id="<?= (int)$listing['job_id'] ?>"
                                                        data-confirm-message="Delete this listing permanently?">
                                                    Delete
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                <?php endforeach; ?>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- pagination -->
                <?php if ($totalPages > 1): ?>
                <?php
                    
                    $prev = $pageNum - 1;
                    $next = $pageNum + 1;

                    // How many numbers to show on each side of the current page
                    $adjacents = 2; 
                    $slots = [];

                    // 1. Always show Page 1
                    $slots[] = ['type' => ($pageNum === 1 ? 'current' : 'link'), 'page' => 1];

                    // 2. Calculate the Window
                    // We want to show a range around the current page
                    $start = max(2, $pageNum - $adjacents);
                    $end   = min($totalPages - 1, $pageNum + $adjacents);

                    // 3. Handle Left Ellipsis
                    if ($start > 2) {
                        $slots[] = ['type' => 'ellipsis'];
                    }

                    // 4. Fill the Window
                    for ($i = $start; $i <= $end; $i++) {
                        $slots[] = [
                            'type' => ($i === $pageNum ? 'current' : 'link'),
                            'page' => $i
                        ];
                    }

                    // 5. Handle Right Ellipsis
                    if ($end < $totalPages - 1) {
                        $slots[] = ['type' => 'ellipsis'];
                    }

                    // 6. Always show Last Page (if it's not Page 1)
                    if ($totalPages > 1) {
                        $slots[] = ['type' => ($pageNum === $totalPages ? 'current' : 'link'), 'page' => $totalPages];
                    }
                ?>
                <nav aria-label="User list pagination" class="mt-2 mb-2">
                    <ul class="pagination justify-content-center gap-1 flex-wrap">

                        <li class="page-item <?= $pageNum <= 1 ? 'disabled' : '' ?>">
                            <?php if ($pageNum > 1): ?>
                                <a class="page-link rounded" href="<?= sanitise(buildPaginationUrl($prev, $filterStatus, $filterType, $searchTerm, $perPage, $sortBy, $sortOrder)) ?>" aria-label="Previous page">&#8592;</a>
                            <?php else: ?>
                                <span class="page-link rounded" aria-disabled="true" aria-label="Previous page">&#8592;</span>
                            <?php endif; ?>
                        </li>

                        <?php foreach ($slots as $slot): ?>
                            <?php if ($slot['type'] === 'ellipsis'): ?>
                                <li class="page-item disabled"><span class="page-link border-0">…</span></li>
                            <?php elseif ($slot['type'] === 'current'): ?>
                                <li class="page-item active" aria-current="page">
                                    <span class="page-link rounded fw-bold"><?= $slot['page'] ?></span>
                                </li>
                            <?php else: ?>
                                <li class="page-item">
                                    <a class="page-link rounded" href="<?= sanitise(buildPaginationUrl($slot['page'], $filterStatus, $filterType, $searchTerm, $perPage, $sortBy, $sortOrder)) ?>">
                                        <?= $slot['page'] ?>
                                    </a>
                                </li>
                            <?php endif; ?>
                        <?php endforeach; ?>

                        <li class="page-item <?= $pageNum >= $totalPages ? 'disabled' : '' ?>">
                            <?php if ($pageNum < $totalPages): ?>
                                <a class="page-link rounded" href="<?= sanitise(buildPaginationUrl($next, $filterStatus, $filterType, $searchTerm, $perPage, $sortBy, $sortOrder)) ?>" aria-label="Next page">&#8594;</a>
                            <?php else: ?>
                                <span class="page-link rounded" aria-disabled="true" aria-label="Next page">&#8594;</span>
                            <?php endif; ?>
                        </li>

                    </ul>
                    <p class="text-center text-muted small mt-1 mb-0">Page <?= $pageNum ?> of <?= $totalPages ?></p>
                </nav>
                <?php endif; ?>

                <?php endif; // closes if (empty($listings)) ?>
            </div>
            </form>
        </div>
    </section>
</main>

<footer class="tb-footer">
    <div class="container">
        <hr>
        <small>&copy; <?= date('Y') ?> TalentBridge Pte. Ltd. All rights reserved.</small>
    </div>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="../assets/js/admin_search.js?v=20260401"></script>
<script>
// Bulk actions script
document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('bulk-actions-form');
    if (!form) return;

    const checkboxes = form.querySelectorAll('.bulk-checkbox');
    const bulkControls = document.getElementById('bulk-actions-controls');

    if (!bulkControls || checkboxes.length === 0) return;

    const actionSelect = bulkControls.querySelector('select[name="bulk_action"]');

    function toggleControls() {
        const anyChecked = [...checkboxes].some(cb => cb.checked);
        bulkControls.style.display = anyChecked ? 'flex' : 'none';
    }

    checkboxes.forEach(cb => {
        cb.addEventListener('change', () => {
            toggleControls();
        });
    });

    form.addEventListener('submit', (e) => {
        if (actionSelect.value === 'delete' && !confirm('Are you sure you want to permanently delete the selected listings?')) {
            e.preventDefault();
        } else if (actionSelect.value === '') {
            e.preventDefault();
            alert('Please select a bulk action to apply.');
        }
    });
    toggleControls(); // Initial check
});

// Single action script (replaces nested forms)
// Single action script using Event Delegation (Future-proof for Listings Search)
(function() {
    const bulkForm = document.getElementById('bulk-actions-form');

    // Helper to create and submit a form preserving state
    function submitListingsAction(data) {
        if (!bulkForm) return;

        const tempForm = document.createElement('form');
        tempForm.method = 'post';
        tempForm.action = '/admin/listings.php';
        
        // 1. Start with the current search/filter/sort state
        let formHTML = `
            <input type="hidden" name="csrf_token" value="${bulkForm.querySelector('input[name="csrf_token"]').value}">
            <input type="hidden" name="intended_status" value="${bulkForm.querySelector('input[name="intended_status"]').value}">
            <input type="hidden" name="intended_type" value="${bulkForm.querySelector('input[name="intended_type"]').value}">
            <input type="hidden" name="intended_page" value="${bulkForm.querySelector('input[name="intended_page"]').value}">
            <input type="hidden" name="intended_search" value="${bulkForm.querySelector('input[name="intended_search"]').value}">
            <input type="hidden" name="intended_per_page" value="${bulkForm.querySelector('input[name="intended_per_page"]').value}">
            <input type="hidden" name="intended_sort_by" value="${bulkForm.querySelector('input[name="intended_sort_by"]').value}">
            <input type="hidden" name="intended_sort_order" value="${bulkForm.querySelector('input[name="intended_sort_order"]').value}">
        `;

        // 2. Add the specific action data (job_id, action type, new status, etc.)
        for (const key in data) {
            formHTML += `<input type="hidden" name="${key}" value="${data[key]}">`;
        }

        tempForm.innerHTML = formHTML;
        document.body.appendChild(tempForm);
        tempForm.submit();
    }

    // Listener, Handles the Status Dropdown change
    document.addEventListener('change', function(e) {
        const select = e.target.closest('.single-action-status-change');
        if (!select) return;

        submitListingsAction({
            action: 'set_status',
            job_id: select.dataset.jobId,
            status: select.value
        });
    });

    // Listener, Handles the Delete Button click
    document.addEventListener('click', function(e) {
        const button = e.target.closest('.single-action-delete-btn');
        if (!button) return;

        if (confirm(button.dataset.confirmMessage || 'Delete this listing?')) {
            submitListingsAction({
                action: 'delete',
                job_id: button.dataset.jobId
            });
        }
    });
})();
</script>
</body>
</html>
