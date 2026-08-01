<?php

/**
 * Landing page — TalentBridge home page.
 *
 * Displays the hero section, platform value propositions, and the six
 * most recently posted active job listings. Featured jobs are made dynamic
 * in Phase 4; placeholder cards are shown until the database is populated.
 *
 * @package TalentBridge
 */

session_start();
require_once 'includes/helpers.php';
require_once 'includes/auth.php';
require_once 'includes/db.php';

// attempt to fetch the six most recent active listings
$featuredJobs = [];
try {
    $pdo  = getConnection();
    $stmt = $pdo->prepare("
        SELECT jl.job_id, jl.title, jl.location, jl.type,
               jl.salary_min, jl.salary_max, jl.salary_period,
               c.company_name, c.industry
          FROM job_listings jl
          JOIN companies c ON c.company_id = jl.company_id
         WHERE jl.status = 'active'
         ORDER BY jl.created_at DESC
         LIMIT 6
    ");
    $stmt->execute();
    $featuredJobs = $stmt->fetchAll();
} catch (PDOException $e) {
    // silently degrade — the page renders without listings if db is unavailable
    $featuredJobs = [];
}

$flash = getFlash();

// map job type to badge css class
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
    <title>Home — TalentBridge</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="assets/css/style.css" rel="stylesheet">
</head>

<body>

    <?php require_once 'includes/nav.php'; ?>

    <main id="main-content">

        <!-- hero section -->
        <section class="tb-hero" aria-label="Hero banner">
            <div class="container">
                <div class="row align-items-center g-5">
                    <div class="col-lg-7">
                        <h1>Find Your Next<br>Career Move</h1>
                        <p class="lead mt-3 mb-4">
                            TalentBridge connects ambitious professionals with Singapore's
                            fastest-growing companies. Browse hundreds of roles — full-time,
                            part-time, remote, and more.
                        </p>

                        <?php if (!isLoggedIn()): ?>
                            <!-- dual cta for unauthenticated visitors -->
                            <div class="d-flex flex-wrap gap-3">
                                <a href="register.php?role=seeker" class="btn-hero-primary btn">
                                    Find a Job
                                </a>
                                <a href="register.php?role=employer" class="btn-hero-secondary btn">
                                    Hire Talent
                                </a>
                            </div>
                        <?php else: ?>
                            <a href="jobs.php" class="btn-hero-primary btn">Browse All Jobs</a>
                        <?php endif; ?>
                    </div>

                    <div class="col-lg-5 d-none d-lg-flex justify-content-center">
                        <!-- decorative stat badges -->
                        <div class="d-flex flex-column gap-3">
                            <div class="bg-white rounded-3 shadow-sm px-4 py-3 d-flex align-items-center gap-3">
                                <div style="font-size:2rem;" aria-hidden="true">💼</div>
                                <div>
                                    <div class="fw-bold text-dark">500+ Active Jobs</div>
                                    <div class="text-muted small">Across all industries</div>
                                </div>
                            </div>
                            <div class="bg-white rounded-3 shadow-sm px-4 py-3 d-flex align-items-center gap-3">
                                <div style="font-size:2rem;" aria-hidden="true">🏢</div>
                                <div>
                                    <div class="fw-bold text-dark">200+ Companies</div>
                                    <div class="text-muted small">Hiring right now</div>
                                </div>
                            </div>
                            <div class="bg-white rounded-3 shadow-sm px-4 py-3 d-flex align-items-center gap-3">
                                <div style="font-size:2rem;" aria-hidden="true">🎯</div>
                                <div>
                                    <div class="fw-bold text-dark">10,000+ Placements</div>
                                    <div class="text-muted small">Since 2020</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- value propositions -->
        <section class="tb-section" aria-labelledby="why-heading">
            <div class="container">
                <div class="text-center mb-5">
                    <h2 id="why-heading" class="tb-section-title">Why TalentBridge?</h2>
                    <div class="tb-divider mx-auto"></div>
                    <p class="tb-section-subtitle">We make finding and filling roles simple, fast, and secure.</p>
                </div>

                <div class="row g-4">
                    <div class="col-md-4">
                        <div class="text-center p-4">
                            <div class="mb-3" style="font-size:2.5rem;" aria-hidden="true">🔍</div>
                            <h3 class="h5 fw-bold" style="color:var(--tb-primary)">Smart Job Search</h3>
                            <p class="text-muted">Filter by industry, location, type, and salary. Find exactly the role you're looking for.</p>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="text-center p-4">
                            <div class="mb-3" style="font-size:2.5rem;" aria-hidden="true">⚡</div>
                            <h3 class="h5 fw-bold" style="color:var(--tb-primary)">Apply in Minutes</h3>
                            <p class="text-muted">Upload your CV once, apply to multiple roles with a personalised cover letter.</p>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="text-center p-4">
                            <div class="mb-3" style="font-size:2.5rem;" aria-hidden="true">📊</div>
                            <h3 class="h5 fw-bold" style="color:var(--tb-primary)">Track Your Progress</h3>
                            <p class="text-muted">See the real-time status of every application — Pending, Reviewed, Shortlisted, or Rejected.</p>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- featured job listings -->
        <section class="tb-section tb-section-alt" aria-labelledby="featured-heading">
            <div class="container">
                <div class="d-flex justify-content-between align-items-end mb-4 flex-wrap gap-3">
                    <div>
                        <h2 id="featured-heading" class="tb-section-title mb-1">Featured Jobs</h2>
                        <div class="tb-divider"></div>
                    </div>
                    <a href="jobs.php" class="btn btn-outline-primary">View All Jobs</a>
                </div>

                <?php if (empty($featuredJobs)): ?>
                    <p class="text-muted">No active job listings at the moment. Check back soon.</p>
                <?php else: ?>
                    <!-- live listings from the database -->
                    <div class="row g-4">
                        <?php foreach ($featuredJobs as $job): ?>
                            <div class="col-md-6 col-lg-4">
                                <div class="card tb-job-card h-100">
                                    <div class="card-body p-4">
                                        <div class="d-flex justify-content-between align-items-start mb-2">
                                            <h3 class="card-title h6 mb-0"><?= sanitise($job['title']) ?></h3>
                                            <span class="badge badge-type <?= getTypeBadgeClass($job['type']) ?> ms-2">
                                                <?= sanitise($job['type']) ?>
                                            </span>
                                        </div>
                                        <p class="company-name mb-2"><?= sanitise($job['company_name']) ?></p>
                                        <div class="card-meta d-flex flex-column gap-1">
                                            <?php if ($job['location']): ?>
                                                <span><span aria-hidden="true">📍</span> <?= sanitise($job['location']) ?></span>
                                            <?php endif; ?>
                                            <?php if ($job['salary_min'] && $job['salary_max']): ?>
                                                <span><span aria-hidden="true">💰</span>
                                                    $<?= number_format($job['salary_min']) ?> – $<?= number_format($job['salary_max']) ?>
                                                    <?= sanitise($job['salary_period'] ?? '') ?>
                                                </span>
                                            <?php endif; ?>
                                        </div>
                                    </div>
                                    <div class="card-footer bg-transparent border-top-0 pt-0 px-4 pb-4">
                                        <a href="job_detail.php?id=<?= (int)$job['job_id'] ?>" class="btn btn-primary btn-sm w-100">
                                            View Job
                                        </a>
                                    </div>
                                </div>
                            </div>
                        <?php endforeach; ?>
                    </div>
                <?php endif; ?>
            </div>
        </section>

        <!-- employer cta section -->
        <section class="tb-section" aria-labelledby="employer-cta-heading">
            <div class="container">
                <div class="row align-items-center g-4">
                    <div class="col-lg-8">
                        <h2 id="employer-cta-heading" class="tb-section-title">Hiring? Post Your Role Today</h2>
                        <p class="tb-section-subtitle">
                            Reach thousands of qualified candidates across Singapore and beyond.
                            Posting is free — create your employer account in under two minutes.
                        </p>
                    </div>
                    <div class="col-lg-4 text-lg-end">
                        <a href="register.php?role=employer" class="btn btn-primary btn-lg">
                            Get Started as Employer
                        </a>
                    </div>
                </div>
            </div>
        </section>

    </main>

    <?php require_once 'includes/footer.php'; ?>

    <?php
    // floating chat widget for logged-in users
    require_once __DIR__ . '/includes/chat_widget.php';
    ?>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>

</html>