<?php
/**
 * Job listings page — public browse page with live client-side filtering.
 *
 * Fetches all active listings joined with company data, renders Bootstrap
 * cards with data- attributes, and wires filter controls to filter.js.
 *
 * @package TalentBridge
 */

session_start();
require_once 'includes/helpers.php';
require_once 'includes/auth.php';
require_once 'includes/db.php';

// fetch all active job listings with their company details
$jobs = [];
$industries = [];
$locations  = [];

try {
    $pdo = getConnection();

    $stmt = $pdo->prepare("
        SELECT jl.job_id, jl.title, jl.location, jl.type,
               jl.salary_min, jl.salary_max, jl.salary_period,
               jl.description, jl.created_at,
               c.company_name, c.industry
          FROM job_listings jl
          JOIN companies c ON c.company_id = jl.company_id
         WHERE jl.status = 'active'
         ORDER BY jl.created_at DESC
    ");
    $stmt->execute();
    $jobs = $stmt->fetchAll();

    // build unique filter option lists from the live data
    foreach ($jobs as $job) {
        if (!empty($job['industry'])) {
            $industries[$job['industry']] = $job['industry'];
        }
        if (!empty($job['location'])) {
            $locations[$job['location']] = $job['location'];
        }
    }
    sort($industries);
    sort($locations);

} catch (PDOException $e) {
    $jobs = [];
}

// job type options for the filter dropdown
$typeOptions = ['Full-time', 'Part-time', 'Contract', 'Internship', 'Remote'];

/**
 * Returns the CSS badge class for a given job type string.
 *
 * @param string $type - The job type value from the database.
 * @return string  The corresponding CSS class name.
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
    <title>Browse Jobs — TalentBridge</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="assets/css/style.css" rel="stylesheet">
</head>
<body>

<?php require_once 'includes/nav.php'; ?>

<main id="main-content">

    <!-- page header -->
    <section class="tb-hero py-4" aria-label="Jobs page header">
        <div class="container">
            <h1 class="fw-bold mb-1">Browse Jobs</h1>
            <p class="lead mb-0">
                <?= count($jobs) ?> active listing<?= count($jobs) !== 1 ? 's' : '' ?> available
            </p>
        </div>
    </section>

    <section class="tb-section" aria-labelledby="jobs-heading">
        <div class="container">
            <h2 id="jobs-heading" class="visually-hidden">Job listings</h2>

            <!-- filter bar -->
            <div class="tb-filter-bar" role="search" aria-label="Filter job listings">
                <div class="row g-3 align-items-end">

                    <!-- keyword search -->
                    <div class="col-md-4">
                        <label for="filterKeyword" class="form-label fw-semibold mb-1">Keyword</label>
                        <input type="search" id="filterKeyword" class="form-control"
                            placeholder="Job title, company, skill…">
                    </div>

                    <!-- industry filter -->
                    <div class="col-md-3 col-sm-6">
                        <label for="filterIndustry" class="form-label fw-semibold mb-1">Industry</label>
                        <select id="filterIndustry" class="form-select">
                            <option value="">All Industries</option>
                            <?php foreach ($industries as $ind): ?>
                                <option value="<?= sanitise(strtolower($ind)) ?>"><?= sanitise($ind) ?></option>
                            <?php endforeach; ?>
                        </select>
                    </div>

                    <!-- location filter -->
                    <div class="col-md-3 col-sm-6">
                        <label for="filterLocation" class="form-label fw-semibold mb-1">Location</label>
                        <select id="filterLocation" class="form-select">
                            <option value="">All Locations</option>
                            <?php foreach ($locations as $loc): ?>
                                <option value="<?= sanitise(strtolower($loc)) ?>"><?= sanitise($loc) ?></option>
                            <?php endforeach; ?>
                        </select>
                    </div>

                    <!-- job type filter -->
                    <div class="col-md-2 col-sm-6">
                        <label for="filterType" class="form-label fw-semibold mb-1">Type</label>
                        <select id="filterType" class="form-select">
                            <option value="">All Types</option>
                            <?php foreach ($typeOptions as $t): ?>
                                <option value="<?= sanitise(strtolower($t)) ?>"><?= sanitise($t) ?></option>
                            <?php endforeach; ?>
                        </select>
                    </div>

                </div>
            </div>

            <!-- no results message — hidden by default, shown by filter.js -->
            <div id="noResultsMsg" class="alert alert-info text-center py-4" style="display:none" role="status">
                <strong>No jobs match your search.</strong> Try adjusting the filters.
            </div>

            <?php if (empty($jobs)): ?>
                <div class="alert alert-info text-center py-4">
                    <strong>No active job listings at the moment.</strong>
                    Check back soon or <a href="register.php?role=employer">post a role</a> yourself.
                </div>

            <?php else: ?>
                <div class="row g-4" id="jobCardGrid">
                    <?php foreach ($jobs as $job):
                        // build the keywords string for filter.js to search against
                        $keywords = implode(' ', [
                            $job['title'],
                            $job['company_name'],
                            $job['industry'] ?? '',
                            $job['location'] ?? '',
                            // strip html tags from description for plain-text keyword search
                            strip_tags($job['description']),
                        ]);
                    ?>
                    <div class="col-md-6 col-lg-4" data-job-col>
                        <div class="card tb-job-card h-100"
                             data-job-card
                             data-industry="<?= sanitise(strtolower($job['industry'] ?? '')) ?>"
                             data-location="<?= sanitise(strtolower($job['location'] ?? '')) ?>"
                             data-type="<?= sanitise(strtolower($job['type'])) ?>"
                             data-keywords="<?= sanitise(strtolower($keywords)) ?>">

                            <div class="card-body p-4">
                                <div class="d-flex justify-content-between align-items-start mb-2">
                                    <h3 class="card-title h6 mb-0"><?= sanitise($job['title']) ?></h3>
                                    <span class="badge badge-type <?= getTypeBadgeClass($job['type']) ?> ms-2 flex-shrink-0">
                                        <?= sanitise($job['type']) ?>
                                    </span>
                                </div>

                                <p class="company-name mb-2"><?= sanitise($job['company_name']) ?></p>

                                <div class="card-meta d-flex flex-column gap-1 mb-3">
                                    <?php if ($job['location']): ?>
                                        <span><span aria-hidden="true">📍</span> <?= sanitise($job['location']) ?></span>
                                    <?php endif; ?>
                                    <?php if ($job['salary_min']): ?>
                                        <span><span aria-hidden="true">💰</span> <?= sanitise(formatSalary($job['salary_min'], $job['salary_max'], $job['salary_period'])) ?></span>
                                    <?php endif; ?>
                                    <?php if ($job['industry']): ?>
                                        <span><span aria-hidden="true">🏢</span> <?= sanitise($job['industry']) ?></span>
                                    <?php endif; ?>
                                </div>

                                <!-- short description excerpt -->
                                <p class="text-muted small mb-0" style="display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden;">
                                    <?= sanitise(strip_tags($job['description'])) ?>
                                </p>
                            </div>

                            <div class="card-footer bg-transparent border-top-0 pt-0 px-4 pb-4">
                                <div class="d-flex justify-content-between align-items-center">
                                    <small class="text-muted">
                                        <?= date('d M Y', strtotime($job['created_at'])) ?>
                                    </small>
                                    <a href="job_detail.php?id=<?= (int)$job['job_id'] ?>"
                                       class="btn btn-primary btn-sm">
                                        View &amp; Apply
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                    <?php endforeach; ?>
                </div>
            <?php endif; ?>

        </div>
    </section>

</main>

<?php require_once 'includes/footer.php'; ?>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="assets/js/filter.js"></script>
</body>
</html>
