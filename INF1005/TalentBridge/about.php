<?php

/**
 * About page — TalentBridge company information.
 *
 * Describes the mission, values, and team of TalentBridge Pte. Ltd.
 *
 * @package TalentBridge
 */

session_start();
require_once 'includes/helpers.php';
require_once 'includes/auth.php';
require_once 'includes/db.php';
?>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>About Us — TalentBridge</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="assets/css/style.css" rel="stylesheet">
</head>

<body>

    <?php require_once 'includes/nav.php'; ?>

    <main id="main-content">

        <!-- page header -->
        <section class="tb-hero py-5" aria-label="About page header">
            <div class="container text-center">
                <h1 class="fw-bold mb-3">About TalentBridge</h1>
                <p class="lead mb-0" style="max-width:600px;margin:0 auto;">
                    Bridging the gap between ambitious talent and forward-thinking companies since 2020.
                </p>
            </div>
        </section>

        <!-- mission section -->
        <section class="tb-section" aria-labelledby="mission-heading">
            <div class="container">
                <div class="row align-items-center g-5">
                    <div class="col-lg-6">
                        <h2 id="mission-heading" class="tb-section-title">Our Mission</h2>
                        <div class="tb-divider"></div>
                        <p>
                            At TalentBridge, we believe that finding the right role — or the right
                            candidate — should never be a matter of luck or connections. Our mission is
                            to democratise access to opportunity by building a transparent, efficient,
                            and human-centred recruitment platform for Singapore and the wider region.
                        </p>
                        <p>
                            We partner with companies of every size, from ambitious start-ups to
                            established multinationals, giving job seekers a single trusted place to
                            discover and apply for roles that genuinely match their skills and
                            aspirations.
                        </p>
                    </div>
                    <div class="col-lg-6">
                        <div class="tb-card p-4 p-lg-5" style="background:var(--tb-light-bg)">
                            <blockquote class="mb-0">
                                <p class="fs-5 fst-italic" style="color:var(--tb-primary)">
                                    "We founded TalentBridge because we experienced first-hand how broken
                                    the hiring process was — for both sides. We knew there had to be a
                                    better way."
                                </p>
                                <footer class="blockquote-footer mt-3" style="color:var(--tb-text-muted)">
                                    Sarah Tan, <cite>Co-Founder &amp; CEO</cite>
                                </footer>
                            </blockquote>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- values section -->
        <section class="tb-section tb-section-alt" aria-labelledby="values-heading">
            <div class="container">
                <div class="text-center mb-5">
                    <h2 id="values-heading" class="tb-section-title">Our Values</h2>
                    <div class="tb-divider mx-auto"></div>
                    <p class="tb-section-subtitle">The principles that guide everything we build.</p>
                </div>

                <div class="row g-4">
                    <div class="col-sm-6 col-lg-3">
                        <div class="tb-card text-center h-100">
                            <div class="mb-3" style="font-size:2.5rem;" aria-hidden="true">🔒</div>
                            <h3 class="h5 fw-bold" style="color:var(--tb-primary)">Trust &amp; Safety</h3>
                            <p class="text-muted small mb-0">
                                Every company is verified. Every piece of data is protected. We take
                                privacy and security seriously.
                            </p>
                        </div>
                    </div>
                    <div class="col-sm-6 col-lg-3">
                        <div class="tb-card text-center h-100">
                            <div class="mb-3" style="font-size:2.5rem;" aria-hidden="true">⚖️</div>
                            <h3 class="h5 fw-bold" style="color:var(--tb-primary)">Fairness</h3>
                            <p class="text-muted small mb-0">
                                Equal opportunity for every applicant. Our platform never discriminates
                                on the basis of age, gender, or background.
                            </p>
                        </div>
                    </div>
                    <div class="col-sm-6 col-lg-3">
                        <div class="tb-card text-center h-100">
                            <div class="mb-3" style="font-size:2.5rem;" aria-hidden="true">💡</div>
                            <h3 class="h5 fw-bold" style="color:var(--tb-primary)">Transparency</h3>
                            <p class="text-muted small mb-0">
                                Applicants can see the real-time status of every application. No black
                                holes, no guesswork.
                            </p>
                        </div>
                    </div>
                    <div class="col-sm-6 col-lg-3">
                        <div class="tb-card text-center h-100">
                            <div class="mb-3" style="font-size:2.5rem;" aria-hidden="true">🚀</div>
                            <h3 class="h5 fw-bold" style="color:var(--tb-primary)">Innovation</h3>
                            <p class="text-muted small mb-0">
                                We continuously improve our platform based on feedback from seekers
                                and employers alike.
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- team section -->
        <section class="tb-section" aria-labelledby="team-heading">
            <div class="container">
                <div class="text-center mb-5">
                    <h2 id="team-heading" class="tb-section-title">Meet the Team</h2>
                    <div class="tb-divider mx-auto"></div>
                    <p class="tb-section-subtitle">The people behind TalentBridge.</p>
                </div>

                <div class="row g-4 justify-content-center">
                    <?php
                    // team member data — easy to update for report purposes
                    $teamMembers = [
                        ['name' => 'Olivia Ee Chew Fong',    'role' => 'Web Developer', 'initials' => 'OE'],
                        ['name' => 'Timothy Chia Kai Lun',   'role' => 'Web Developer', 'initials' => 'TC'],
                        ['name' => 'Matthias Chua Jia Jun',  'role' => 'Web Developer', 'initials' => 'MC'],
                        ['name' => 'Chia Yixuan',             'role' => 'Web Developer', 'initials' => 'CY'],
                        ['name' => 'Tan Zhi Kai',             'role' => 'Web Developer', 'initials' => 'TZ'],
                    ];
                    foreach ($teamMembers as $member):
                    ?>
                        <div class="col-6 col-sm-4 col-lg-2">
                            <div class="team-card">
                                <div class="team-avatar" aria-hidden="true">
                                    <?= sanitise($member['initials']) ?>
                                </div>
                                <h3 class="h6 mb-1"><?= sanitise($member['name']) ?></h3>
                                <p class="team-role mb-0"><?= sanitise($member['role']) ?></p>
                            </div>
                        </div>
                    <?php endforeach; ?>
                </div>
            </div>
        </section>

        <!-- stats section -->
        <section class="tb-section tb-section-alt" aria-labelledby="stats-heading">
            <div class="container">
                <h2 id="stats-heading" class="visually-hidden">TalentBridge by the numbers</h2>
                <div class="row g-4 text-center">
                    <div class="col-6 col-md-3">
                        <div class="display-5 fw-bold stat-number" data-target="500" style="color:var(--tb-accent)">500+</div>
                        <p class="text-muted mb-0">Active Job Listings</p>
                    </div>
                    <div class="col-6 col-md-3">
                        <div class="display-5 fw-bold stat-number" data-target="200" style="color:var(--tb-accent)">200+</div>
                        <p class="text-muted mb-0">Partner Companies</p>
                    </div>
                    <div class="col-6 col-md-3">
                        <div class="display-5 fw-bold stat-number" data-target="10000" data-display="10K+" style="color:var(--tb-accent)">10K+</div>
                        <p class="text-muted mb-0">Successful Placements</p>
                    </div>
                    <div class="col-6 col-md-3">
                        <div class="display-5 fw-bold" style="color:var(--tb-accent)">4.8<span aria-hidden="true">★</span><span class="visually-hidden"> out of 5 stars</span></div>
                        <p class="text-muted mb-0">Average Employer Rating</p>
                    </div>
                </div>
            </div>
        </section>

    </main>

    <?php require_once 'includes/footer.php'; ?>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        const counters = document.querySelectorAll('.stat-number');
        if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
            // skip animation and display final values immediately
            counters.forEach(el => {
                const target = +el.dataset.target;
                const suffix = el.dataset.suffix || '+';
                el.textContent = el.dataset.display || (target + suffix);
            });
        } else {
            counters.forEach(el => {
                const target = +el.dataset.target;
                const suffix = el.dataset.suffix || '+';
                let count = 0;
                const interval = setInterval(() => {
                    count += Math.ceil(target / 50);
                    if (count >= target) {
                        el.textContent = el.dataset.display || (target + suffix);
                        clearInterval(interval);
                    } else {
                        // format large numbers
                        el.textContent = count >= 1000 ? Math.floor(count / 1000) + 'K+' : count + '+';
                    }
                }, 30);
            });
        }
    </script>
</body>

</html>