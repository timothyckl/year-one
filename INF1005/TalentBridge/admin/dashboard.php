<?php
/**
 * Admin dashboard — platform-wide statistics at a glance.
 *
 * Displays four animated stat cards: total users, active listings,
 * total applications, and unread contact messages. Counters are animated
 * by admin_stats.js using requestAnimationFrame.
 *
 * @package TalentBridge
 */

session_start();
require_once '../includes/helpers.php';
require_once '../includes/auth.php';
require_once '../includes/db.php';

requireRole('admin');

// fetch platform statistics in a single query each
$stats = [
    'users'        => 0,
    'listings'     => 0,
    'applications' => 0,
    'unread_msgs'  => 0,
];

try {
    $pdo = getConnection();

    // use prepare/execute for convention consistency — no user input but avoids ->query() footgun
    $statsStmt = $pdo->prepare("
        SELECT
            (SELECT COUNT(*) FROM users)                              AS total_users,
            (SELECT COUNT(*) FROM job_listings WHERE status='active') AS active_listings,
            (SELECT COUNT(*) FROM applications)                      AS total_applications,
            (SELECT COUNT(*) FROM contact_messages WHERE is_read=0)  AS unread_messages
    ");
    $statsStmt->execute([]);
    $row = $statsStmt->fetch();

    if ($row) {
        $stats['users']        = (int) $row['total_users'];
        $stats['listings']     = (int) $row['active_listings'];
        $stats['applications'] = (int) $row['total_applications'];
        $stats['unread_msgs']  = (int) $row['unread_messages'];
    }

} catch (PDOException $e) {
    // stats remain at zero if the query fails
}

// fetch the five most recently registered users for a quick overview
$recentUsers = [];
try {
    $recentStmt = $pdo->prepare("
        SELECT user_id, name, email, role, is_active, created_at
          FROM users
         ORDER BY created_at DESC
         LIMIT 5
    ");
    $recentStmt->execute([]);
    $recentUsers = $recentStmt->fetchAll();
} catch (PDOException $e) {
    $recentUsers = [];
}

$flash = getFlash();
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard — TalentBridge</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="../assets/css/style.css" rel="stylesheet">
</head>
<body>

<?php require_once '../includes/nav.php'; ?>

<main id="main-content">
    <section class="tb-section">
        <div class="container">

            <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-3">
                <div>
                    <h1 class="tb-section-title mb-1">Admin Dashboard</h1>
                    <div class="tb-divider"></div>
                </div>
                <span class="text-muted small">Logged in as <?= sanitise($_SESSION['name']) ?></span>
            </div>

            <?php foreach ($flash as $type => $msg): ?>
                <div class="alert alert-<?= $type === 'error' ? 'danger' : sanitise($type) ?> tb-flash" role="alert">
                    <?= sanitise($msg) ?>
                </div>
            <?php endforeach; ?>

            <!-- stat cards -->
            <div class="row g-4 mb-5">

                <div class="col-sm-6 col-xl-3">
                    <div class="stat-card stat-card-blue">
                        <div class="stat-number" role="img"
                             data-stat-target="<?= $stats['users'] ?>"
                             aria-label="<?= $stats['users'] ?> total users">
                            <?= $stats['users'] ?>
                        </div>
                        <div class="stat-label">Total Users</div>
                        <a href="/admin/users.php"
                           class="stretched-link text-white-50 small mt-2 d-block text-decoration-none">
                            Manage users →
                        </a>
                    </div>
                </div>

                <div class="col-sm-6 col-xl-3">
                    <div class="stat-card stat-card-green">
                        <div class="stat-number" role="img"
                             data-stat-target="<?= $stats['listings'] ?>"
                             aria-label="<?= $stats['listings'] ?> active listings">
                            <?= $stats['listings'] ?>
                        </div>
                        <div class="stat-label">Active Listings</div>
                        <a href="/admin/listings.php"
                           class="stretched-link text-white-50 small mt-2 d-block text-decoration-none">
                            Manage listings →
                        </a>
                    </div>
                </div>

                <div class="col-sm-6 col-xl-3">
                    <div class="stat-card stat-card-purple">
                        <div class="stat-number" role="img"
                             data-stat-target="<?= $stats['applications'] ?>"
                             aria-label="<?= $stats['applications'] ?> total applications">
                            <?= $stats['applications'] ?>
                        </div>
                        <div class="stat-label">Total Applications</div>
                    </div>
                </div>

                <div class="col-sm-6 col-xl-3">
                    <div class="stat-card stat-card-orange">
                        <div class="stat-number" role="img"
                             data-stat-target="<?= $stats['unread_msgs'] ?>"
                             aria-label="<?= $stats['unread_msgs'] ?> unread messages">
                            <?= $stats['unread_msgs'] ?>
                        </div>
                        <div class="stat-label">Unread Messages</div>
                        <a href="/admin/messages.php"
                           class="stretched-link text-white-50 small mt-2 d-block text-decoration-none">
                            View messages →
                        </a>
                    </div>
                </div>

            </div>

            <!-- quick nav cards -->
            <div class="row g-4 mb-5">
                <div class="col-md-3">
                    <a href="/admin/users.php" class="text-decoration-none">
                        <div class="tb-card h-100 text-center py-4 tb-job-card">
                            <div style="font-size:2.5rem;margin-bottom:.5rem" aria-hidden="true">👥</div>
                            <h2 class="h6 fw-bold" style="color:var(--tb-primary)">Manage Users</h2>
                            <p class="text-muted small mb-0">Approve, suspend, or delete user accounts.</p>
                        </div>
                    </a>
                </div>
                <div class="col-md-3">
                    <a href="/admin/listings.php" class="text-decoration-none">
                        <div class="tb-card h-100 text-center py-4 tb-job-card">
                            <div style="font-size:2.5rem;margin-bottom:.5rem" aria-hidden="true">📋</div>
                            <h2 class="h6 fw-bold" style="color:var(--tb-primary)">Manage Listings</h2>
                            <p class="text-muted small mb-0">Change status or remove job listings platform-wide.</p>
                        </div>
                    </a>
                </div>
                <div class="col-md-3">
                    <a href="/admin/messages.php" class="text-decoration-none">
                        <div class="tb-card h-100 text-center py-4 tb-job-card">
                            <div style="font-size:2.5rem;margin-bottom:.5rem" aria-hidden="true">
                                ✉️<?php if ($stats['unread_msgs'] > 0): ?>
                                    <sup class="badge bg-danger" style="font-size:.5rem"><?= $stats['unread_msgs'] ?></sup>
                                <?php endif; ?>
                            </div>
                            <h2 class="h6 fw-bold" style="color:var(--tb-primary)">Contact Messages</h2>
                            <p class="text-muted small mb-0">Read, mark as read, and delete enquiries.</p>
                        </div>
                    </a>
                </div>
                <div class="col-md-3">
                    <a href="/admin/audit_log.php" class="text-decoration-none">
                        <div class="tb-card h-100 text-center py-4 tb-job-card">
                            <div style="font-size:2.5rem;margin-bottom:.5rem" aria-hidden="true">🛡️</div>
                            <h2 class="h6 fw-bold" style="color:var(--tb-primary)">Security Audit Log</h2>
                            <p class="text-muted small mb-0">Review important security-related events.</p>
                        </div>
                    </a>
                </div>
            </div>

            <!-- data visualisations section -->
            <div id="chartsSection" class="mb-5">

                <!-- New Users Chart Card -->
                <div class="card" data-chart-init="new_users">
                    <div class="card-header d-flex flex-wrap justify-content-between align-items-center">
                        <h2 class="h5 mb-2 mb-md-0">New User Registrations</h2>
                        <div class="d-flex flex-wrap align-items-center gap-2">
                            <!-- Custom Legend Container -->
                            <div id="newUsersLegendContainer" class="d-flex gap-2 me-2"></div>

                            <!-- Stack/Overlap Toggle -->
                            <button id="toggleStackMode" class="btn btn-outline-secondary btn-sm">
                                <i class="bi bi-stack"></i> <span>Stacked</span>
                            </button>

                            <!-- Date Range Selector -->
                            <div class="d-flex align-items-center gap-2">
                                <select id="newUsersDateRange" class="form-select form-select-sm" style="width: auto;" aria-label="Select date range for new user registrations">
                                    <option value="7">Last 7 Days</option>
                                    <option value="30" selected>Last 30 Days</option>
                                    <option value="90">Last 90 Days</option>
                                    <option value="365">Last Year</option>
                                    <option value="custom">Custom Range...</option>
                                </select>
                                <div id="newUsersCustomDateRange" class="d-flex gap-2 align-items-center" style="display: none;">
                                    <label for="newUsersStartDate" class="visually-hidden">New users start date</label>
                                    <input type="date" id="newUsersStartDate" class="form-control form-control-sm">
                                    <label for="newUsersEndDate" class="visually-hidden">New users end date</label>
                                    <input type="date" id="newUsersEndDate" class="form-control form-control-sm">
                                    <button id="applyNewUsersDate" class="btn btn-primary btn-sm">Apply</button>
                                </div>
                                <div class="ms-2">
                                    <button class="btn btn-outline-secondary btn-sm btn-download-chart" data-chart-id="newUsersChart" data-chart-name="new-user-registrations" title="Save Chart as Image"><i class="bi bi-download"></i></button>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="card-body">
                        <div id="newUsersChartContainer" role="img" aria-label="New user registrations chart" style="height: 350px;">
                            <canvas id="newUsersChart"></canvas>
                        </div>
                    </div>
                </div>

                <!-- Industry Popularity Chart Card -->
                <div class="card mt-4" data-chart-init="industry_popularity">
                    <div class="card-header d-flex flex-wrap justify-content-between align-items-center">
                        <h2 class="h5 mb-2 mb-md-0">Industry Popularity</h2>
                        <div class="d-flex flex-wrap align-items-center gap-3">
                            <div class="form-check form-switch">
                                <input class="form-check-input" type="checkbox" id="includeInactiveListings" checked>
                                <label class="form-check-label" for="includeInactiveListings">Include Closed Listings</label>
                            </div>
                            <div class="d-flex align-items-center gap-2">
                                <label for="jobTypeFilter" class="form-label mb-0">Job Type:</label>
                                <select id="jobTypeFilter" class="form-select form-select-sm" style="width: auto;">
                                    <option value="">All Types</option>
                                </select>
                            </div>
                            <div class="btn-group btn-group-sm">
                                <button type="button" class="btn btn-outline-secondary" id="selectAllIndustries">Select All</button>
                                <button type="button" class="btn btn-outline-secondary" id="deselectAllIndustries">Deselect All</button>
                            </div>
                            <button class="btn btn-outline-secondary btn-sm btn-download-chart" data-chart-id="industryChart" data-chart-name="industry-popularity" title="Save Chart as Image">
                                <i class="bi bi-download"></i>
                            </button>
                        </div>
                    </div>
                    <div class="card-body">
                        <div id="industryCheckboxes" class="mb-3 d-flex flex-wrap gap-2">
                            <!-- Industry filter chips will be rendered here by JS -->
                        </div>
                        <div id="industryChartContainer" role="img" aria-label="Industry popularity chart" style="height: 400px;">
                            <canvas id="industryChart"></canvas>
                        </div>
                    </div>
                </div>

                <!-- Applications Overview Card -->
                <div class="card mt-4" data-chart-init="applications">
                    <div class="card-header d-flex flex-wrap justify-content-between align-items-center">
                        <h2 class="h5 mb-2 mb-md-0">Applications Overview</h2>
                        <div class="d-flex align-items-center gap-2">
                            <select id="applicationsDateRange" class="form-select form-select-sm" style="width: auto;" aria-label="Select date range for applications overview">
                                <option value="7">Last 7 Days</option>
                                <option value="30" selected>Last 30 Days</option>
                                <option value="90">Last 90 Days</option>
                                <option value="365">Last Year</option>
                                <option value="custom">Custom Range...</option>
                            </select>
                            <div id="applicationsCustomDateRange" class="d-flex gap-2 align-items-center" style="display: none;">
                                <label for="applicationsStartDate" class="visually-hidden">Applications start date</label>
                                <input type="date" id="applicationsStartDate" class="form-control form-control-sm">
                                <label for="applicationsEndDate" class="visually-hidden">Applications end date</label>
                                <input type="date" id="applicationsEndDate" class="form-control form-control-sm">
                                <button id="applyApplicationsDate" class="btn btn-primary btn-sm">Apply</button>
                            </div>
                            <div class="btn-group btn-group-sm ms-2" role="group" aria-label="Download chart images">
                                <button class="btn btn-outline-secondary btn-download-chart" data-chart-id="applicationsChart" data-chart-name="applications-overall-status" title="Save Overall Status Chart">
                                    <i class="bi bi-pie-chart-fill"></i> <span class="d-none d-lg-inline ms-1">Overall</span>
                                </button>
                                <button class="btn btn-outline-secondary btn-download-chart" data-chart-id="applicationsIndustryChart" data-chart-name="applications-industry-status" title="Save Industry Status Chart">
                                    <i class="bi bi-pie-chart"></i> <span class="d-none d-lg-inline ms-1">By Industry</span>
                                </button>
                            </div>
                        </div>
                    </div>
                    <div class="row g-0">
                        <div class="col-md-6 border-end">
                            <div class="card-body">
                                <h3 class="h6 card-subtitle mb-2 text-muted text-center">By Status (Overall)</h3>
                                <div id="applicationsChartContainer" role="img" aria-label="Applications overview chart" style="height: 300px;">
                                    <canvas id="applicationsChart"></canvas>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="card-body">
                                <div class="d-flex justify-content-center align-items-center gap-2 mb-2">
                                    <h3 class="h6 card-subtitle text-muted text-center mb-0">By Status (Industry):</h3>
                                    <select id="industryApplicationFilter" class="form-select form-select-sm" style="width: auto;" aria-label="Filter applications by industry">
                                        <option value="">All Industries</option>
                                    </select>
                                </div>
                                <div id="applicationsIndustryChartContainer" role="img" aria-label="Applications by industry chart" style="height: 300px;">
                                    <canvas id="applicationsIndustryChart"></canvas>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Geographical Popularity Card -->
                <div class="card mt-4" data-chart-init="geographical_popularity">
                    <div class="card-header d-flex flex-wrap justify-content-between align-items-center">
                        <h2 class="h5 mb-2 mb-md-0">Geographic Activity</h2>
                        <div class="d-flex flex-wrap align-items-center gap-3">
                            <!-- Custom Legend Container -->
                            <div id="geoLegendContainer" class="d-flex gap-2"></div>

                            <!-- Order Toggle -->
                            <div class="btn-group btn-group-sm">
                                <button id="geoOrderToggle" class="btn btn-outline-secondary">
                                    Order by: <span id="geoOrderLabel">Job Listings ▼</span>
                                </button>
                            </div>

                            <!-- Count Limit -->
                            <div class="d-flex align-items-center gap-2">
                                <label for="geoCountLimit" class="form-label mb-0">Show Top:</label>
                                <select id="geoCountLimit" class="form-select form-select-sm" style="width: auto;">
                                    <option value="5">5</option>
                                    <option value="10" selected>10</option>
                                    <option value="15">15</option>
                                    <option value="20">20</option>
                                </select>
                            </div>
                            <button class="btn btn-outline-secondary btn-sm btn-download-chart" data-chart-id="geographicalChart" data-chart-name="geographical-activity" title="Save Chart as Image">
                                <i class="bi bi-download"></i>
                            </button>
                        </div>
                    </div>
                    <div class="card-body">
                        <div id="geographicalChartContainer" role="img" aria-label="Geographic activity chart" style="height: 450px;">
                            <canvas id="geographicalChart"></canvas>
                        </div>
                    </div>
                </div>
            </div>

            <!-- recent users table -->
            <?php if (!empty($recentUsers)): ?>
                <div class="tb-card p-0 overflow-hidden">
                    <div class="px-4 pt-3 pb-2 d-flex justify-content-between align-items-center"
                         style="background:var(--tb-light-bg)">
                        <h2 class="h6 fw-bold mb-0" style="color:var(--tb-primary)">Recently Registered Users</h2>
                        <a href="/admin/users.php" class="btn btn-outline-primary btn-sm">View All</a>
                    </div>
                    <div class="table-responsive">
                        <table class="table table-hover mb-0 align-middle">
                            <thead style="background:var(--tb-light-bg)">
                                <tr>
                                    <th scope="col" class="ps-4">Name</th>
                                    <th scope="col">Email</th>
                                    <th scope="col">Role</th>
                                    <th scope="col">Status</th>
                                    <th scope="col" class="pe-4">Joined</th>
                                </tr>
                            </thead>
                            <tbody>
                                <?php foreach ($recentUsers as $u): ?>
                                    <tr>
                                        <td class="ps-4 fw-semibold"><?= sanitise($u['name']) ?></td>
                                        <td class="text-muted small"><?= sanitise($u['email']) ?></td>
                                        <td>
                                            <span class="badge <?= $u['role'] === 'admin' ? 'bg-danger' : ($u['role'] === 'employer' ? 'bg-primary' : 'bg-success') ?>">
                                                <?= sanitise(ucfirst($u['role'])) ?>
                                            </span>
                                        </td>
                                        <td>
                                            <span class="badge <?= $u['is_active'] ? 'bg-success' : 'bg-secondary' ?>">
                                                <?= $u['is_active'] ? 'Active' : 'Suspended' ?>
                                            </span>
                                        </td>
                                        <td class="pe-4 text-muted small">
                                            <?= date('d M Y', strtotime($u['created_at'])) ?>
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

<footer class="tb-footer">
    <div class="container">
        <hr>
        <small>&copy; <?= date('Y') ?> TalentBridge Pte. Ltd. All rights reserved.</small>
    </div>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
<script src="../assets/js/admin_stats.js"></script>
<script src="../assets/js/charts_dashboard.js"></script>

</body>
</html>
