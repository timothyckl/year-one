<?php
/**
 * Shared navigation partial.
 *
 * Renders a Bootstrap 5 navbar whose links adapt to the visitor's
 * authentication state and role. Must be included after session_start().
 * Logout is a CSRF-protected POST form to prevent forced-logout attacks.
 *
 * @package TalentBridge
 */

require_once __DIR__ . '/csrf.php';

// determine the current page for active-link highlighting
$currentPage = basename($_SERVER['PHP_SELF']);
$base = '/';

// pre-generate the csrf token once for the logout form
$navCsrfToken = generateCsrfToken();
?>
<a class="visually-hidden-focusable" href="#main-content">Skip to main content</a>
<nav class="navbar navbar-expand-lg tb-navbar" aria-label="main navigation">
    <div class="container">

        <!-- brand logo -->
        <a class="navbar-brand" href="<?= $base ?>index.php">
            Talent<span>Bridge</span>
        </a>

        <!-- mobile toggle -->
        <button
            class="navbar-toggler border-0"
            type="button"
            data-bs-toggle="collapse"
            data-bs-target="#tbNavbar"
            aria-controls="tbNavbar"
            aria-expanded="false"
            aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" id="tbNavbar">
            <!-- left-side links — always visible -->
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                <li class="nav-item">
                    <a class="nav-link <?= $currentPage === 'index.php' ? 'active' : '' ?>"
                       href="<?= $base ?>index.php"
                       <?= $currentPage === 'index.php' ? 'aria-current="page"' : '' ?>>
                        Home
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link <?= $currentPage === 'jobs.php' ? 'active' : '' ?>"
                       href="<?= $base ?>jobs.php"
                       <?= $currentPage === 'jobs.php' ? 'aria-current="page"' : '' ?>>
                        Jobs
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link <?= $currentPage === 'about.php' ? 'active' : '' ?>"
                       href="<?= $base ?>about.php"
                       <?= $currentPage === 'about.php' ? 'aria-current="page"' : '' ?>>
                        About
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link <?= $currentPage === 'contact.php' ? 'active' : '' ?>"
                       href="<?= $base ?>contact.php"
                       <?= $currentPage === 'contact.php' ? 'aria-current="page"' : '' ?>>
                        Contact
                    </a>
                </li>
            </ul>

            <!-- right-side links — depend on authentication state and role -->
            <ul class="navbar-nav ms-auto mb-2 mb-lg-0 align-items-lg-center gap-1">

                <?php if (!isLoggedIn()): ?>
                    <!-- unauthenticated visitor -->
                    <li class="nav-item">
                        <a class="nav-link <?= $currentPage === 'login.php' ? 'active' : '' ?>"
                           href="<?= $base ?>login.php"
                           <?= $currentPage === 'login.php' ? 'aria-current="page"' : '' ?>>
                            Login
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="btn-nav-cta nav-link px-3 ms-lg-2 rounded"
                           href="<?= $base ?>register.php">
                            Register
                        </a>
                    </li>

                <?php elseif (getUserRole() === 'seeker'): ?>
                    <!-- job seeker dashboard links -->
                    <li class="nav-item">
                        <a class="nav-link <?= $currentPage === 'applications.php' ? 'active' : '' ?>"
                           href="<?= $base ?>seeker/applications.php">
                            My Applications
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link <?= $currentPage === 'saved_jobs.php' ? 'active' : '' ?>"
                           href="<?= $base ?>seeker/saved_jobs.php">
                            Saved Jobs
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link <?= $currentPage === 'profile.php' ? 'active' : '' ?>"
                           href="<?= $base ?>seeker/profile.php">
                            Profile
                        </a>
                    </li>
                    <li class="nav-item ms-lg-2">
                        <!-- logout is a csrf-protected post to prevent forced-logout attacks -->
                        <form method="post" action="<?= $base ?>logout.php" class="d-inline">
                            <input type="hidden" name="csrf_token" value="<?= sanitise($navCsrfToken) ?>">
                            <button type="submit" class="btn-nav-cta nav-link px-3 rounded border-0 bg-transparent">
                                Logout
                            </button>
                        </form>
                    </li>

                <?php elseif (getUserRole() === 'employer'): ?>
                    <!-- employer dashboard links -->
                    <li class="nav-item">
                        <a class="nav-link <?= $currentPage === 'post_job.php' ? 'active' : '' ?>"
                           href="<?= $base ?>employer/post_job.php">
                            Post a Job
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link <?= $currentPage === 'manage_listings.php' ? 'active' : '' ?>"
                           href="<?= $base ?>employer/manage_listings.php">
                            My Listings
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link <?= $currentPage === 'company_profile.php' ? 'active' : '' ?>"
                           href="<?= $base ?>employer/company_profile.php">
                            Company Profile
                        </a>
                    </li>
                    <li class="nav-item ms-lg-2">
                        <form method="post" action="<?= $base ?>logout.php" class="d-inline">
                            <input type="hidden" name="csrf_token" value="<?= sanitise($navCsrfToken) ?>">
                            <button type="submit" class="btn-nav-cta nav-link px-3 rounded border-0 bg-transparent">
                                Logout
                            </button>
                        </form>
                    </li>

                <?php elseif (getUserRole() === 'admin'): ?>
                    <!-- admin panel link -->
                    <li class="nav-item">
                        <a class="nav-link <?= in_array($currentPage, ['dashboard.php','users.php','listings.php','messages.php']) ? 'active' : '' ?>"
                           href="<?= $base ?>admin/dashboard.php">
                            Admin Panel
                        </a>
                    </li>
                    <li class="nav-item ms-lg-2">
                        <form method="post" action="<?= $base ?>logout.php" class="d-inline">
                            <input type="hidden" name="csrf_token" value="<?= sanitise($navCsrfToken) ?>">
                            <button type="submit" class="btn-nav-cta nav-link px-3 rounded border-0 bg-transparent">
                                Logout
                            </button>
                        </form>
                    </li>

                <?php endif; ?>
            </ul>
        </div>
    </div>
</nav>


