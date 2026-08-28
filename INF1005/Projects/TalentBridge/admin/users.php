<?php
/**
 * Admin users page — paginated user management.
 *
 * Allows the admin to approve/suspend accounts (toggle is_active) and
 * permanently delete users (CASCADE removes all related rows). All
 * state-changing actions use POST with CSRF. Admins cannot delete or
 * suspend their own account.
 *
 * @package TalentBridge
 */

session_start();
require_once '../includes/helpers.php';
require_once '../includes/auth.php';
require_once '../includes/db.php';
require_once '../includes/csrf.php';

requireRole('admin');

error_log("DEBUG: users.php - GET parameters: " . print_r($_GET, true));

$adminId = (int) $_SESSION['user_id'];

$perPage = (int) max(5, min(100, (int) ($_GET['per_page'] ?? 20)));
$pageNum = (int) max(1, (int) ($_GET['page'] ?? 1)); 
$offset  = (int) (($pageNum - 1) * $perPage);

// optional role filter
$filterRole = $_GET['role'] ?? '';
$validRoles = ['seeker', 'employer', 'admin', ''];

if (!in_array($filterRole, $validRoles, true)) {
    $filterRole = '';
}

// optional status filter
$filterIsActive = $_GET['is_active'] ?? '';
if (!in_array($filterIsActive, ['active', 'suspended', ''], true)) {
    $filterIsActive = '';
}

// search functionality
$searchTerm = trim($_GET['search'] ?? '');
$searchTerm = strlen($searchTerm) > 0 ? $searchTerm : '';

// sorting functionality
$sortBy    = $_GET['sort_by'] ?? 'created_at';
$sortOrder = strtoupper($_GET['sort_order'] ?? 'DESC');
$validSortColumns = ['name', 'email', 'role', 'is_active', 'created_at'];
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
        $userIds = $_POST['user_ids'] ?? [];

        if (!empty($userIds) && is_array($userIds)) {
            $userIds = array_map('intval', $userIds);
            $originalCount = count($userIds);
            $userIds = array_filter($userIds, fn($id) => $id > 0 && $id !== $adminId);

            if (count($userIds) < $originalCount) {
                setFlash('warning', 'You cannot modify your own account. It was skipped.');
            }

            if (!empty($userIds)) {
                try {
                    $pdo = getConnection();
                    $placeholders = implode(',', array_fill(0, count($userIds), '?'));
                    $sql = '';
                    if ($bulkAction === 'suspend') {
                        $sql = "UPDATE users SET is_active = 0 WHERE user_id IN ($placeholders)";
                    } elseif ($bulkAction === 'activate') {
                        $sql = "UPDATE users SET is_active = 1 WHERE user_id IN ($placeholders)";
                    } elseif ($bulkAction === 'delete') {
                        $sql = "DELETE FROM users WHERE user_id IN ($placeholders)";
                    }

                    if ($sql) {
                        $stmt = $pdo->prepare($sql);
                        $stmt->execute(array_values($userIds));
                        log_audit_event('BULK_USER_' . strtoupper($bulkAction), $adminId, ['target_user_ids' => $userIds]);
                        setFlash('success', "{$stmt->rowCount()} user(s) updated.");
                    }
                } catch (PDOException $e) {
                    setFlash('error', 'Bulk action failed. Please try again.');
                }
            }
        } else {
            setFlash('error', 'No users were selected.');
        }
    } else { // ---- handle SINGLE actions ----
        $action = $_POST['action'] ?? '';
        $targetUserId = (int) ($_POST['user_id'] ?? 0);

        if ($targetUserId === $adminId) {
            setFlash('error', 'You cannot modify your own admin account.');
        } elseif ($targetUserId > 0) {
            try {
                $pdo = getConnection();
                if ($action === 'suspend') {
                    $pdo->prepare("UPDATE users SET is_active = 0 WHERE user_id = :uid")->execute([':uid' => $targetUserId]);
                    log_audit_event('USER_SUSPENDED', $adminId, ['target_user_id' => $targetUserId]);
                    setFlash('success', 'User suspended.');
                } elseif ($action === 'activate') {
                    $pdo->prepare("UPDATE users SET is_active = 1 WHERE user_id = :uid")->execute([':uid' => $targetUserId]);
                    log_audit_event('USER_ACTIVATED', $adminId, ['target_user_id' => $targetUserId]);
                    setFlash('success', 'User activated.');
                } elseif ($action === 'delete') {
                    $pdo->prepare("DELETE FROM users WHERE user_id = :uid")->execute([':uid' => $targetUserId]);
                    log_audit_event('USER_DELETED', $adminId, ['target_user_id' => $targetUserId]);
                    setFlash('success', 'User deleted.');
                }
            } catch (PDOException $e) {
                setFlash('error', 'Action failed. Please try again.');
            }
        }
    }
    
    // after POST, preserve the filters if they were active
    $intendedRole     = $_POST['intended_role'] ?? '';
    $intendedIsActive = $_POST['intended_is_active'] ?? '';
    $intendedPage     = (int) max(1, (int) ($_POST['intended_page'] ?? 1));
    $intendedSearch   = trim($_POST['intended_search'] ?? '');
    $intendedPerPage  = (int) max(5, min(100, (int) ($_POST['intended_per_page'] ?? 20)));
    $intendedSortBy   = $_POST['intended_sort_by'] ?? 'created_at';
    $intendedSortOrder = strtoupper($_POST['intended_sort_order'] ?? 'DESC');

    if (!in_array($intendedRole, $validRoles, true)) $intendedRole = '';
    if (!in_array($intendedIsActive, ['active', 'suspended', ''], true)) $intendedIsActive = '';
    if (!in_array($intendedSortBy, $validSortColumns, true)) $intendedSortBy = 'created_at';
    if (!in_array($intendedSortOrder, $validSortOrders, true)) $intendedSortOrder = 'DESC';

    $redirectUrl = '/admin/users.php';
    $params = [];
    if ($intendedRole) {
        $params[] = 'role=' . urlencode($intendedRole);
    }
    if ($intendedIsActive) {
        $params[] = 'is_active=' . urlencode($intendedIsActive);
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

// ---- fetch paginated users ----
try {
    $pdo = getConnection();

    // build a parameterised where clause depending on the role filter and search term
    $whereConditions = [];
    $params = [];
    
    if ($filterRole) {
        $whereConditions[] = 'role = :role';
        $params[':role'] = $filterRole;
    }

    if ($filterIsActive === 'active') {
        $whereConditions[] = 'is_active = 1';
    } elseif ($filterIsActive === 'suspended') {
        $whereConditions[] = 'is_active = 0';
    }
    
    if (strlen($searchTerm) > 0) {
        $whereConditions[] = '(name LIKE :search_name OR email LIKE :search_email)';
        $params[':search_name'] = '%' . $searchTerm . '%';
        $params[':search_email'] = '%' . $searchTerm . '%';
    }
    
    $whereClause = count($whereConditions) > 0 ? 'WHERE ' . implode(' AND ', $whereConditions) : '';

    $countStmt = $pdo->prepare("SELECT COUNT(*) FROM users $whereClause");
    $countStmt->execute($params);
    $totalUsers  = (int) $countStmt->fetchColumn();
    $totalPages = ($totalUsers > 0) ? (int) ceil($totalUsers / $perPage) : 1;

    // Clamp currentPage to valid range now that we know totalPages
    $pageNum = (int) max(1, min($pageNum, max(1, $totalPages)));
    $offset      = (int) (($pageNum - 1) * $perPage);

    $listStmt = $pdo->prepare("
        SELECT user_id, name, email, role, is_active, created_at
          FROM users
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
    $users = $listStmt->fetchAll();

} catch (PDOException $e) {
    error_log('Users page PDOException: ' . $e->getMessage());
    $users       = [];
    $totalPages  = 1;
    $totalUsers  = 0;
    $pageNum = 1; // ensure int — $_GET value may still be a string here
    $offset      = 0;
}

$flash     = getFlash();
$csrfToken = generateCsrfToken();

/**
 * Build a pagination URL preserving all active filters/sort state.
 */
function buildPaginationUrl(int $page, string $filterRole, string $filterIsActive, string $searchTerm, int $perPage, string $sortBy, string $sortOrder): string {
    $page    = (int) $page;
    $perPage = (int) $perPage;
    $params  = ['page=' . $page];
    if ($filterRole)              $params[] = 'role='       . urlencode($filterRole);
    if ($filterIsActive)          $params[] = 'is_active='  . urlencode($filterIsActive);
    if (strlen($searchTerm) > 0)  $params[] = 'search='     . urlencode($searchTerm);
    if ($perPage !== 20)          $params[] = 'per_page='   . $perPage;
    if ($sortBy !== 'created_at' || $sortOrder !== 'DESC') {
        $params[] = 'sort_by='    . urlencode($sortBy);
        $params[] = 'sort_order=' . urlencode($sortOrder);
    }
    $url = '/admin/users.php?' . implode('&', $params);
    error_log("DEBUG: buildPaginationUrl received page: " . $page . " and generated URL: " . $url);
    return $url;
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage Users — TalentBridge Admin</title>
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
                    <h1 class="tb-section-title mb-1">Manage Users</h1>
                    <div class="tb-divider"></div>
                </div>
                <a href="/admin/dashboard.php" class="btn btn-outline-primary btn-sm"><span aria-hidden="true">←</span> Dashboard</a>
            </div>

            <?php foreach ($flash as $type => $msg): ?>
                <div class="alert alert-<?= $type === 'error' ? 'danger' : sanitise($type) ?> tb-flash" role="alert">
                    <?= sanitise($msg) ?>
                </div>
            <?php endforeach; ?>

            <!-- role and status filter tabs -->
            <div id="users-filter-container" class="realtime-update-container">
                <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3">
                    <nav aria-label="Filter users by role" class="flex-shrink-0">
                        <ul class="nav nav-pills gap-1">
                            <?php
                            $roleTabs = ['' => 'All Roles', 'seeker' => 'Seekers', 'employer' => 'Employers', 'admin' => 'Admins'];
                            foreach ($roleTabs as $roleVal => $roleLabel):
                                $href = '/admin/users.php';
                                $params = [];
                                if ($roleVal) { $params[] = 'role=' . urlencode($roleVal); }
                                if ($filterIsActive) { $params[] = 'is_active=' . urlencode($filterIsActive); }
                                if (strlen($searchTerm) > 0) { $params[] = 'search=' . urlencode($searchTerm); }
                                if ($perPage != 20) { $params[] = 'per_page=' . $perPage; }
                                
                                // NEW: Keep the sorting state when clicking a filter tab
                                $params[] = 'sort_by=' . urlencode($sortBy);
                                $params[] = 'sort_order=' . urlencode($sortOrder);

                                if ($params) { $href .= '?' . implode('&', $params); }
                            ?>
                                <li class="nav-item">
                                    <a class="nav-link <?= $filterRole === $roleVal ? 'active' : '' ?>"
                                       href="<?= sanitise($href) ?>"
                                       <?= $filterRole === $roleVal ? 'aria-current="page"' : '' ?>>
                                        <?= sanitise($roleLabel) ?>
                                    </a>
                                </li>
                            <?php endforeach; ?>
                        </ul>
                    </nav>
                    <nav aria-label="Filter users by status" class="flex-shrink-0">
                        <ul class="nav nav-pills gap-1">
                            <?php
                            $statusTabs = ['' => 'All Statuses', 'active' => 'Active', 'suspended' => 'Suspended'];
                            foreach ($statusTabs as $statusVal => $statusLabel):
                                $href = '/admin/users.php';
                                $params = [];
                                if ($filterRole) { $params[] = 'role=' . urlencode($filterRole); }
                                if ($statusVal) { $params[] = 'is_active=' . urlencode($statusVal); }
                                if (strlen($searchTerm) > 0) { $params[] = 'search=' . urlencode($searchTerm); }
                                if ($perPage != 20) { $params[] = 'per_page=' . $perPage; }
                                
                                // NEW: Keep the sorting state when clicking a filter tab
                                $params[] = 'sort_by=' . urlencode($sortBy);
                                $params[] = 'sort_order=' . urlencode($sortOrder);

                                if ($params) { $href .= '?' . implode('&', $params); }
                            ?>
                                <li class="nav-item">
                                    <a class="nav-link <?= $filterIsActive === $statusVal ? 'active' : '' ?>"
                                       href="<?= sanitise($href) ?>"
                                       <?= $filterIsActive === $statusVal ? 'aria-current="true"' : '' ?>>
                                        <?= sanitise($statusLabel) ?>
                                    </a>
                                </li>
                            <?php endforeach; ?>
                        </ul>
                    </nav>
                </div>
            </div> 
            <!-- search & per-page controls -->
            <div class="d-flex gap-3 mb-4 flex-wrap align-items-center justify-content-between">
                <form method="get" action="/admin/users.php" class="d-flex gap-2 flex-grow-1 realtime-search-form" style="max-width: 400px;">
                    <input type="hidden" name="role" value="<?= sanitise($filterRole) ?>">
                    <input type="hidden" name="is_active" value="<?= sanitise($filterIsActive) ?>">
                    <input type="hidden" name="per_page" value="<?= (int)$perPage ?>">
                    <input type="hidden" name="sort_by" value="<?= sanitise($sortBy) ?>">
                    <input type="hidden" name="sort_order" value="<?= sanitise($sortOrder) ?>">
                    <label for="users-search" class="visually-hidden">Search users</label>
                    <input type="text" name="search" id="users-search" class="form-control form-control-sm"
                           placeholder="Search by name or email..." value="<?= sanitise($searchTerm) ?>">
                    <a href="/admin/users.php?role=<?= sanitise($filterRole) ?>&is_active=<?= sanitise($filterIsActive) ?>" class="btn btn-sm btn-outline-secondary btn-clear-search" style="display: <?= strlen($searchTerm) > 0 ? 'inline-block' : 'none' ?>;">Clear</a>
                </form>
                
                <select name="per_page" id="perPageSelect" class="form-select form-select-sm" style="max-width: 120px;"
                        aria-label="Results per page">
                    <option value="5" <?= $perPage == 5 ? 'selected' : '' ?>>5 per page</option>
                    <option value="10" <?= $perPage == 10 ? 'selected' : '' ?>>10 per page</option>
                    <option value="20" <?= $perPage == 20 ? 'selected' : '' ?>>20 per page</option>
                    <option value="50" <?= $perPage == 50 ? 'selected' : '' ?>>50 per page</option>
                    <option value="100" <?= $perPage == 100 ? 'selected' : '' ?>>100 per page</option>
                </select>
            </div>

            <script>
                // document.getElementById('perPageSelect').addEventListener('change', function() {
                //     const url = new URL(window.location.href);
                //     url.searchParams.set(this.name, this.value);
                //     window.location.href = url.toString();
                // });

                document.getElementById('perPageSelect').addEventListener('change', function() {
                    const url = new URL(window.location.href);
                    url.searchParams.set('per_page', this.value);
                    url.searchParams.set('page', '1'); // Always reset to page 1 on limit change
                    window.location.href = url.toString();
                });

                
            </script>

            <form method="post" action="/admin/users.php" id="bulk-actions-form">
                <input type="hidden" name="csrf_token" value="<?= sanitise($csrfToken) ?>">
                <input type="hidden" name="intended_role"   value="<?= sanitise($filterRole) ?>">
                <input type="hidden" name="intended_is_active" value="<?= sanitise($filterIsActive) ?>">
                <input type="hidden" name="intended_page"   value="<?= (int)$pageNum ?>">
                <input type="hidden" name="intended_search" value="<?= sanitise($searchTerm) ?>">
                <input type="hidden" name="intended_per_page" value="<?= (int)$perPage ?>">
                <input type="hidden" name="intended_sort_by" value="<?= sanitise($sortBy) ?>">
                <input type="hidden" name="intended_sort_order" value="<?= sanitise($sortOrder) ?>">

                <div class="d-flex justify-content-between align-items-center mb-3">
                    <div id="users-count-summary" class="realtime-update-container">
                        <p class="text-muted small mb-0">
                            Showing <?= count($users) ?> of <?= $totalUsers ?> user<?= $totalUsers !== 1 ? 's' : '' ?>
                            <?= $filterRole ? '(' . sanitise(ucfirst($filterRole)) . 's only)' : '' ?>
                            <?= $filterIsActive ? '(' . sanitise(ucfirst($filterIsActive)) . ' only)' : '' ?>
                            <?= strlen($searchTerm) > 0 ? ' (filtered)' : '' ?>
                        </p>
                    </div>
                    <!-- bulk action controls -->
                    <div class="d-flex gap-2 align-items-center" id="bulk-actions-controls" style="display: none;">
                        <label for="bulk-action-select" class="visually-hidden">Bulk Actions</label>
                        <select name="bulk_action" id="bulk-action-select" class="form-select form-select-sm" style="width: auto;" required>
                            <option value="">Bulk Actions...</option>
                            <option value="activate">Activate selected</option>
                            <option value="suspend">Suspend selected</option>
                            <option value="delete">Delete selected</option>
                        </select>
                        <button type="submit" class="btn btn-sm btn-primary">Apply</button>
                    </div>
                </div>

            <div id="users-table-container" class="realtime-update-container">

                <?php if (empty($users)): ?>
                    <div class="alert alert-info text-center py-4">No users found.</div>

                <?php else: ?>

                <div class="tb-card p-0 overflow-hidden mb-4">
                    <div class="table-responsive">
                        <table class="table table-hover mb-0 align-middle">
                            <thead style="background:var(--tb-light-bg)">
                                <tr>
                                    <th class="ps-3" style="width: 1%;"><span class="visually-hidden">Select user for bulk actions</span></th>
                                    <?php
                                    $sortableColumns = [
                                        'name' => 'Name',
                                        'email' => 'Email',
                                        'role' => 'Role',
                                        'is_active' => 'Status',
                                        'created_at' => 'Joined'
                                    ];
                                    
                                    foreach ($sortableColumns as $colName => $colLabel):
                                        $newSortOrder = 'ASC';
                                        $indicator = '';
                                        
                                        if ($sortBy === $colName) {
                                            $newSortOrder = $sortOrder === 'ASC' ? 'DESC' : 'ASC';
                                            $indicator = $sortOrder === 'ASC' ? ' ▲' : ' ▼';
                                        }
                                        
                                        $href = '/admin/users.php?sort_by=' . urlencode($colName) . '&sort_order=' . $newSortOrder;
                                        if ($filterRole) $href .= '&role=' . urlencode($filterRole);
                                        if ($filterIsActive) $href .= '&is_active=' . urlencode($filterIsActive);
                                        if (strlen($searchTerm) > 0) $href .= '&search=' . urlencode($searchTerm);
                                        if ($perPage != 20) $href .= '&per_page=' . $perPage;
                                        // if ($pageNum > 1) $href .= '&page=' . (int)$pageNum;
                                        
                                        $thClass = ($colName === 'name' ? 'ps-4' : '');
                                        $ariaSortVal = 'none';
                                        if ($sortBy === $colName) {
                                            $ariaSortVal = $sortOrder === 'ASC' ? 'ascending' : 'descending';
                                        }
                                    ?>
                                        <th scope="col" class="<?= $thClass ?>" style="cursor: pointer;"
                                            aria-sort="<?= sanitise($ariaSortVal) ?>">
                                            <a href="<?= sanitise($href) ?>" class="text-decoration-none" style="color: inherit;">
                                                <?= sanitise($colLabel) ?><?php if ($indicator): ?><span aria-hidden="true"><?= $indicator ?></span><?php endif; ?>
                                            </a>
                                        </th>
                                    <?php endforeach; ?>
                                    <th scope="col" class="pe-4">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <?php foreach ($users as $user): ?>
                                    <tr <?= !$user['is_active'] ? 'class="table-secondary"' : '' ?>>
                                        <td class="ps-3">
                                            <?php if ((int)$user['user_id'] !== $adminId): ?>
                                                <input type="checkbox" name="user_ids[]" value="<?= (int)$user['user_id'] ?>" class="bulk-checkbox" aria-label="Select user <?= sanitise($user['name']) ?>">
                                            <?php endif; ?>
                                        </td>
                                        <td class="ps-4 fw-semibold">
                                            <?php if (in_array($user['role'], ['seeker', 'employer'])): ?>
                                                <a href="#" class="text-decoration-none view-user-details"
                                                   data-user-id="<?= (int)$user['user_id'] ?>"
                                                   data-user-name="<?= sanitise($user['name']) ?>"
                                                   data-bs-toggle="modal" data-bs-target="#userDetailsModal">
                                                    <?= sanitise($user['name']) ?>
                                                </a>
                                            <?php else: ?>
                                                <?= sanitise($user['name']) ?>
                                            <?php endif; ?>
                                        </td>
                                        <td class="text-muted small"><?= sanitise($user['email']) ?></td>
                                        <td>
                                            <?php
                                            $roleBadge = [
                                                'admin'    => 'bg-danger',
                                                'employer' => 'bg-primary',
                                                'seeker'   => 'bg-success',
                                            ][$user['role']] ?? 'bg-secondary';
                                            ?>
                                            <span class="badge <?= $roleBadge ?>">
                                                <?= sanitise(ucfirst($user['role'])) ?>
                                            </span>
                                        </td>
                                        <td>
                                            <span class="badge <?= $user['is_active'] ? 'bg-success' : 'bg-secondary' ?>">
                                                <?= $user['is_active'] ? 'Active' : 'Suspended' ?>
                                            </span>
                                        </td>
                                        <td class="text-muted small">
                                            <?= date('d M Y', strtotime($user['created_at'])) ?>
                                        </td>
                                        <td class="pe-4">
                                            <?php if ((int)$user['user_id'] !== $adminId): ?>
                                                <div class="d-flex gap-2 flex-nowrap">                                                    <!-- suspend / activate toggle -->
                                                    <button type="button"
                                                            class="btn btn-sm single-action-btn <?= $user['is_active'] ? 'btn-outline-warning' : 'btn-outline-success' ?>"
                                                            data-user-id="<?= (int)$user['user_id'] ?>"
                                                            data-action="<?= $user['is_active'] ? 'suspend' : 'activate' ?>"
                                                            data-confirm-message="<?= $user['is_active'] ? 'Suspend' : 'Activate' ?> this user?">
                                                        <?= $user['is_active'] ? 'Suspend' : 'Activate' ?>
                                                    </button>

                                                    <!-- delete -->
                                                    <button type="button" class="btn btn-sm btn-outline-danger single-action-btn"
                                                            data-user-id="<?= (int)$user['user_id'] ?>"
                                                            data-action="delete"
                                                            data-confirm-message="Permanently delete <?= sanitise(addslashes($user['name'])) ?>? This cannot be undone.">
                                                        Delete
                                                    </button>
                                                </div>
                                            <?php else: ?>
                                                <span class="text-muted small">You</span>
                                            <?php endif; ?>
                                        </td>
                                    </tr>
                                <?php endforeach; ?>
                            </tbody>
                        </table>
                    </div>
                </div>
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

                        <!-- ← Prev arrow -->
                        <li class="page-item <?= $pageNum <= 1 ? 'disabled' : '' ?>">
                            <?php if ($pageNum > 1): ?>
                                <a class="page-link rounded" href="<?= sanitise(buildPaginationUrl($prev, $filterRole, $filterIsActive, $searchTerm, $perPage, $sortBy, $sortOrder)) ?>" aria-label="Previous page" rel="prev">←</a>
                            <?php else: ?>
                                <span class="page-link rounded" aria-disabled="true">←</span>
                            <?php endif; ?>
                        </li>

                        <!-- Numbered slots -->
                        <?php foreach ($slots as $slot): ?>
                            <?php if ($slot['type'] === 'ellipsis'): ?>
                                <li class="page-item disabled"><span class="page-link border-0">…</span></li>
                            <?php elseif ($slot['type'] === 'current'): ?>
                                <li class="page-item active" aria-current="page">
                                    <span class="page-link rounded fw-bold"><?= $slot['page'] ?></span>
                                </li>
                            <?php else: ?>
                                <li class="page-item">
                                    <a class="page-link rounded" href="<?= sanitise(buildPaginationUrl($slot['page'], $filterRole, $filterIsActive, $searchTerm, $perPage, $sortBy, $sortOrder)) ?>">
                                        <?= $slot['page'] ?>
                                    </a>
                                </li>
                            <?php endif; ?>
                        <?php endforeach; ?>

                        <!-- → Next arrow -->
                        <li class="page-item <?= $pageNum >= $totalPages ? 'disabled' : '' ?>">
                            <?php if ($pageNum < $totalPages): ?>
                                <a class="page-link rounded" href="<?= sanitise(buildPaginationUrl($next, $filterRole, $filterIsActive, $searchTerm, $perPage, $sortBy, $sortOrder)) ?>" aria-label="Next page" rel="next">→</a>
                            <?php else: ?>
                                <span class="page-link rounded" aria-disabled="true">→</span>
                            <?php endif; ?>
                        </li>

                    </ul>
                    <p class="text-center text-muted small mt-1 mb-0">Page <?= $pageNum ?> of <?= $totalPages ?></p>
                </nav>
                <?php endif; ?>

                <?php endif; // closes if (empty($users)) ?>
            </div>
            
            <!-- User Details Modal -->
            <div class="modal fade" id="userDetailsModal" tabindex="-1" aria-labelledby="userDetailsModalLabel" aria-hidden="true" role="dialog">
                <div class="modal-dialog modal-lg modal-dialog-scrollable" role="document">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h2 class="modal-title h5" id="userDetailsModalLabel">User Details</h2>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close user details modal"></button>
                        </div>
                        <div class="modal-body" id="userDetailsModalBody">
                            <!-- Details will be loaded here via AJAX -->
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                        </div>
                    </div>
                </div>
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
        if (actionSelect.value === 'delete' && !confirm('Are you sure you want to permanently delete the selected users? This cannot be undone.')) {
            e.preventDefault();
        } else if (actionSelect.value === '') {
            e.preventDefault();
            alert('Please select a bulk action to apply.');
        }
    });
    toggleControls(); // Initial check
});

// Single action script using Event Delegation
document.addEventListener('click', function(e) {
    // Check if the clicked element (or its parent) is a single-action-btn
    const button = e.target.closest('.single-action-btn');
    
    if (!button) return; 

    e.preventDefault();

    const userId = button.dataset.userId;
    const action = button.dataset.action;
    const confirmMessage = button.dataset.confirmMessage;

    if (confirmMessage && !confirm(confirmMessage)) {
        return; // User cancelled
    }

    // Get CSRF token and parameters from the main bulk form
    const bulkForm = document.getElementById('bulk-actions-form');
    const csrfToken = bulkForm.querySelector('input[name="csrf_token"]').value;
    
    // Create a temporary form for submission
    const tempForm = document.createElement('form');
    tempForm.method = 'post';
    tempForm.action = '/admin/users.php';
    
    // Pass along the search/filter state so the admin stays on the same results after the action
    const fields = [
        ['csrf_token', csrfToken],
        ['user_id', userId],
        ['action', action],
        ['intended_role', bulkForm.querySelector('input[name="intended_role"]').value],
        ['intended_is_active', bulkForm.querySelector('input[name="intended_is_active"]').value],
        ['intended_page', bulkForm.querySelector('input[name="intended_page"]').value],
        ['intended_search', bulkForm.querySelector('input[name="intended_search"]').value],
        ['intended_per_page', bulkForm.querySelector('input[name="intended_per_page"]').value],
        ['intended_sort_by', bulkForm.querySelector('input[name="intended_sort_by"]').value],
        ['intended_sort_order', bulkForm.querySelector('input[name="intended_sort_order"]').value]
    ];

    fields.forEach(([name, value]) => {
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = name;
        input.value = value;
        tempForm.appendChild(input);
    });

    document.body.appendChild(tempForm);
    tempForm.submit();
});

// User details modal script
document.addEventListener('DOMContentLoaded', () => {
    const userDetailsModal = document.getElementById('userDetailsModal');
    if (userDetailsModal) {
        userDetailsModal.addEventListener('show.bs.modal', function (event) {
            const button = event.relatedTarget;
            const userId = button.getAttribute('data-user-id');
            const userName = button.getAttribute('data-user-name');

            const modalTitle = userDetailsModal.querySelector('.modal-title');
            const modalBody = userDetailsModal.querySelector('.modal-body');

            modalTitle.textContent = `Details for ${userName}`;
            // Show spinner while loading
            modalBody.innerHTML = `<div class="text-center p-4"><div class="spinner-border text-primary" role="status"><span class="visually-hidden">Loading...</span></div></div>`;

            fetch(`/admin/get_user_details.php?id=${userId}`)
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`Network response was not ok (${response.statusText})`);
                    }
                    return response.json();
                })
                .then(data => {
                    if (data.error) {
                        throw new Error(data.error);
                    }
                    
                    let html = `<p><strong>Role:</strong> <span class="badge bg-primary">${data.user.role}</span></p>`;
                    html += `<p><strong>Email:</strong> ${data.user.email}</p>`;
                    html += `<p><strong>Joined:</strong> ${new Date(data.user.created_at).toLocaleDateString()}</p><hr>`;

                    if (data.role_details) {
                        if (data.user.role === 'seeker') {
                            const details = data.role_details;
                            html += `<h4>Seeker Profile</h4>`;
                            html += `<p class="mb-1"><strong>Headline:</strong> ${details.profile.professional_headline || '<em>Not set</em>'}</p>`;
                            html += `<p><strong>Location:</strong> ${details.profile.location || '<em>Not set</em>'}</p>`;
                            html += `<h5 class="mt-3">Recent Applications (${details.applications.length})</h5>`;
                            if (details.applications.length > 0) {
                                html += '<ul class="list-group list-group-flush">';
                                details.applications.forEach(app => {
                                    html += `<li class="list-group-item d-flex justify-content-between align-items-center">
                                                <div>Applied for <strong>${app.title}</strong> at ${app.company_name} <span class="badge bg-secondary">${app.status}</span></div>
                                                <a href="/job_detail.php?id=${app.job_id}" class="btn btn-sm btn-outline-primary" target="_blank" rel="noopener noreferrer">View</a>
                                             </li>`;
                                });
                                html += '</ul>';
                            } else {
                                html += '<p>No applications found.</p>';
                            }
                        } else if (data.user.role === 'employer') {
                            const details = data.role_details;
                            html += `<h4>Company Profile</h4>`;
                            html += `<p class="mb-1"><strong>Company:</strong> ${details.company.company_name || '<em>Not set</em>'}</p>`;
                            html += `<p><strong>Industry:</strong> ${details.company.industry || '<em>Not set</em>'}</p>`;
                            html += `<div><strong>Description:</strong> <div class="card card-body bg-light p-2 mt-1 small">${details.company.description || '<em>Not set</em>'}</div></div>`;

                            html += `<h5 class="mt-3">Recent Listings (${details.listings.length})</h5>`;
                            if (details.listings.length > 0) {
                                html += '<ul class="list-group list-group-flush">';
                                details.listings.forEach(listing => {
                                    html += `<li class="list-group-item d-flex justify-content-between align-items-center">
                                                <div><strong>${listing.title}</strong> <span class="badge bg-secondary">${listing.status}</span></div>
                                                <a href="/job_detail.php?id=${listing.job_id}" class="btn btn-sm btn-outline-primary" target="_blank" rel="noopener noreferrer">View</a>
                                             </li>`;
                                });
                                html += '</ul>';
                            } else {
                                html += '<p>No listings found.</p>';
                            }
                        }
                    }
                    modalBody.innerHTML = html;
                })
                .catch(error => {
                    modalBody.innerHTML = `<div class="alert alert-danger">Error loading user details: ${error.message}</div>`;
                });
        });
    }
});
</script>
</body>
</html>