<?php
/**
 * Review widget partial — displays reviews and a submit form.
 *
 * TWO functions are provided:
 *
 *   handleReviewPost($targetType, $targetId)
 *     — Call this at the TOP of the page, before any HTML output,
 *       alongside the other POST handlers. It processes the form
 *       submission and redirects, so headers can still be sent.
 *
 *   renderReviews($targetType, $targetId, $pathPrefix)
 *     — Call this inside the HTML body where you want the section
 *       to appear. It only reads from the DB and renders HTML.
 *
 * @package TalentBridge
 */

/**
 * Handles the review POST submission.
 * MUST be called before any HTML output.
 *
 * @param string   $targetType  One of 'company', 'seeker', 'platform'.
 * @param int|null $targetId    The company_id / user_id being reviewed.
 * @return void
 */
function handleReviewPost(string $targetType, ?int $targetId): void
{
    if (
        $_SERVER['REQUEST_METHOD'] !== 'POST' ||
        ($_POST['action'] ?? '') !== 'review_submit'
    ) {
        return;
    }

    // CSRF guard
    if (!validateCsrfToken($_POST['csrf_token'] ?? '')) {
        http_response_code(403);
        exit('Invalid CSRF token.');
    }

    // Must be logged in
    if (!isLoggedIn()) {
        $_SESSION['review_flash'] = ['type' => 'error', 'msg' => 'You must be logged in to leave a review.'];
        header('Location: ' . strtok($_SERVER['REQUEST_URI'], '#') . '#reviews');
        exit;
    }

    $reviewerId = (int) $_SESSION['user_id'];
    $rating     = (int) ($_POST['rating'] ?? 0);
    $body       = trim($_POST['review_body'] ?? '');

    // Validate
    $reviewErrors = [];
    if ($rating < 1 || $rating > 5) {
        $reviewErrors[] = 'Please select a rating between 1 and 5 stars.';
    }
    if (strlen($body) < 20) {
        $reviewErrors[] = 'Your review must be at least 20 characters.';
    }
    if (strlen($body) > 2000) {
        $reviewErrors[] = 'Your review must not exceed 2,000 characters.';
    }

    if (!empty($reviewErrors)) {
        $_SESSION['review_flash'] = ['type' => 'error', 'msg' => implode(' ', $reviewErrors)];
        header('Location: ' . strtok($_SERVER['REQUEST_URI'], '#') . '#reviews');
        exit;
    }

    try {
        $pdo = getConnection();

        // Prevent duplicate reviews
        $dupStmt = $pdo->prepare("
            SELECT review_id FROM reviews
             WHERE reviewer_id = :rid
               AND target_type = :tt
               AND target_id   = :ti
        ");
        $dupStmt->execute([
            ':rid' => $reviewerId,
            ':tt'  => $targetType,
            ':ti'  => $targetId,
        ]);

        if ($dupStmt->fetch()) {
            $_SESSION['review_flash'] = ['type' => 'warning', 'msg' => 'You have already submitted a review for this ' . $targetType . '.'];
        } else {
            $stmt = $pdo->prepare("
                INSERT INTO reviews
                       (reviewer_id, target_type, target_id, rating, body, is_approved)
                VALUES (:reviewer_id, :target_type, :target_id, :rating, :body, 1)
            ");
            $stmt->execute([
                ':reviewer_id' => $reviewerId,
                ':target_type' => $targetType,
                ':target_id'   => $targetId,
                ':rating'      => $rating,
                ':body'        => $body,
            ]);
            $_SESSION['review_flash'] = ['type' => 'success', 'msg' => 'Thank you! Your review has been submitted successfully.'];

            if (function_exists('log_audit_event')) {
                log_audit_event('REVIEW_SUBMIT', $reviewerId, [
                    'target_type' => $targetType,
                    'target_id'   => $targetId,
                ]);
            }
        }

    } catch (PDOException $e) {
        $_SESSION['review_flash'] = ['type' => 'error', 'msg' => 'Could not save your review. Please try again.'];
        error_log('Review insert failed: ' . $e->getMessage());
    }

    // Always redirect after POST — prevents re-submission on refresh
    header('Location: ' . strtok($_SERVER['REQUEST_URI'], '#') . '#reviews');
    exit;
}

/**
 * Renders the review list and submit form.
 * Call this inside the HTML body.
 *
 * @param string   $targetType  One of 'company', 'seeker', 'platform'.
 * @param int|null $targetId    The company_id / user_id being reviewed.
 * @param string   $pathPrefix  '' for root pages, '../' for /employer/ subfolder.
 * @return void
 */
function renderReviews(string $targetType, ?int $targetId, string $pathPrefix = ''): void
{
    // ------------------------------------------------------------------
    // 1. Fetch existing approved reviews for this target
    // ------------------------------------------------------------------
    $reviews    = [];
    $avgRating  = 0;
    $totalCount = 0;
    $pdo        = getConnection();

    try {

        $countStmt = $pdo->prepare("
            SELECT COUNT(*) AS cnt, ROUND(AVG(rating), 1) AS avg_rating
              FROM reviews
             WHERE target_type = :tt
               AND target_id   = :ti
               AND is_approved = 1
        ");
        $countStmt->execute([':tt' => $targetType, ':ti' => $targetId]);
        $stats = $countStmt->fetch();

        $totalCount = (int)   ($stats['cnt']        ?? 0);
        $avgRating  = (float) ($stats['avg_rating']  ?? 0);

        if ($totalCount > 0) {
            $listStmt = $pdo->prepare("
                SELECT r.review_id, r.rating, r.body, r.created_at,
                       u.name AS reviewer_name, u.role AS reviewer_role
                  FROM reviews r
                  JOIN users u ON u.user_id = r.reviewer_id
                 WHERE r.target_type = :tt
                   AND r.target_id   = :ti
                   AND r.is_approved = 1
                 ORDER BY r.created_at DESC
                 LIMIT 20
            ");
            $listStmt->execute([':tt' => $targetType, ':ti' => $targetId]);
            $reviews = $listStmt->fetchAll();
        }

    } catch (PDOException $e) {
        error_log('Review fetch failed: ' . $e->getMessage());
    }

    // ------------------------------------------------------------------
    // 2. Check whether the current user has already reviewed this target
    // ------------------------------------------------------------------
    $currentUserHasReviewed = false;
    $currentUserId          = isLoggedIn() ? (int) $_SESSION['user_id'] : 0;

    if ($currentUserId > 0) {
        try {
            $myStmt = $pdo->prepare("
                SELECT 1 FROM reviews
                 WHERE reviewer_id = :rid
                   AND target_type = :tt
                   AND target_id   = :ti
            ");
            $myStmt->execute([
                ':rid' => $currentUserId,
                ':tt'  => $targetType,
                ':ti'  => $targetId,
            ]);
            $currentUserHasReviewed = (bool) $myStmt->fetch();
        } catch (PDOException $e) {
            // non-critical — degrade silently
        }
    }

    // ------------------------------------------------------------------
    // 3. Retrieve flash message set by handleReviewPost()
    // ------------------------------------------------------------------
    $reviewFlash = $_SESSION['review_flash'] ?? null;
    unset($_SESSION['review_flash']);

    $csrfToken = generateCsrfToken();

    // ------------------------------------------------------------------
    // 4. Render
    // ------------------------------------------------------------------
    ?>

    <!-- ================================================================
         REVIEW SECTION
         ================================================================ -->
    <section id="reviews" class="mt-5">
        <h2 class="h5 fw-bold mb-1" style="color:var(--tb-primary)">
            Reviews
            <?php if ($totalCount > 0): ?>
                <span class="text-muted fw-normal fs-6">(<?= $totalCount ?>)</span>
            <?php endif; ?>
        </h2>
        <div class="tb-divider mb-4"></div>

        <!-- flash message scoped to the review widget -->
        <?php if ($reviewFlash): ?>
            <div class="alert alert-<?= $reviewFlash['type'] === 'error' ? 'danger' : sanitise($reviewFlash['type']) ?> tb-flash" role="alert">
                <?= sanitise($reviewFlash['msg']) ?>
            </div>
        <?php endif; ?>

        <!-- overall rating summary -->
        <?php if ($totalCount > 0): ?>
            <div class="d-flex align-items-center gap-3 mb-4 p-3 rounded"
                 style="background:var(--tb-light-bg);border:1px solid var(--tb-border)">
                <div class="text-center">
                    <div class="fw-bold" style="font-size:2rem;color:var(--tb-primary);line-height:1">
                        <?= number_format($avgRating, 1) ?>
                    </div>
                    <div class="text-muted small">out of 5</div>
                </div>
                <div>
                    <div aria-label="Average rating: <?= number_format($avgRating, 1) ?> out of 5">
                        <?= renderStars($avgRating, 'lg') ?>
                    </div>
                    <div class="text-muted small mt-1">
                        Based on <?= $totalCount ?> <?= $totalCount === 1 ? 'review' : 'reviews' ?>
                    </div>
                </div>
            </div>
        <?php endif; ?>

        <!-- review list -->
        <?php if (empty($reviews)): ?>
            <p class="text-muted small mb-4">No reviews yet. Be the first to share your experience!</p>
        <?php else: ?>
            <div class="mb-4">
                <?php foreach ($reviews as $review): ?>
                    <div class="tb-card mb-3"
                         style="border-left:3px solid var(--tb-primary)">
                        <div class="d-flex justify-content-between align-items-start flex-wrap gap-2 mb-2">
                            <div>
                                <span class="fw-bold small"><?= sanitise($review['reviewer_name']) ?></span>
                                <span class="badge ms-2 text-bg-secondary" style="font-size:0.7rem;font-weight:400">
                                    <?= sanitise(ucfirst($review['reviewer_role'])) ?>
                                </span>
                            </div>
                            <div class="d-flex align-items-center gap-2">
                                <?= renderStars((float) $review['rating']) ?>
                                <span class="text-muted small">
                                    <?= date('d M Y', strtotime($review['created_at'])) ?>
                                </span>
                            </div>
                        </div>
                        <p class="mb-0 small" style="line-height:1.65">
                            <?= nl2br(sanitise($review['body'])) ?>
                        </p>
                    </div>
                <?php endforeach; ?>
            </div>
        <?php endif; ?>

        <!-- submit form -->
        <?php if (!isLoggedIn()): ?>
            <div class="alert alert-info">
                <a href="<?= sanitise($pathPrefix) ?>login.php?redirect=<?= urlencode($_SERVER['REQUEST_URI']) ?>">Log in</a>
                or
                <a href="<?= sanitise($pathPrefix) ?>register.php">register</a>
                to leave a review.
            </div>

        <?php elseif ($currentUserHasReviewed): ?>
            <div class="alert alert-success">
                You have already submitted a review for this <?= sanitise($targetType) ?>. Thank you!
            </div>

        <?php else: ?>
            <div class="tb-card" style="background:var(--tb-light-bg);border-color:var(--tb-border)">
                <h3 class="h6 fw-bold mb-3" style="color:var(--tb-primary)">Write a Review</h3>

                <form method="post" action="<?= sanitise(strtok($_SERVER['REQUEST_URI'], '#')) ?>" novalidate>
                    <input type="hidden" name="csrf_token"  value="<?= sanitise($csrfToken) ?>">
                    <input type="hidden" name="action"      value="review_submit">
                    <input type="hidden" name="target_type" value="<?= sanitise($targetType) ?>">
                    <input type="hidden" name="target_id"   value="<?= (int) $targetId ?>">

                    <!-- star rating picker -->
                    <div class="mb-3">
                        <label class="form-label">Your Rating <span class="text-danger" aria-hidden="true">*</span></label>
                        <div class="d-flex gap-2" role="group" aria-label="Star rating">
                            <?php for ($s = 1; $s <= 5; $s++): ?>
                                <div class="form-check form-check-inline">
                                    <input class="form-check-input visually-hidden"
                                           type="radio"
                                           name="rating"
                                           id="star_<?= $s ?>"
                                           value="<?= $s ?>"
                                           required>
                                    <label class="form-check-label review-star-label"
                                           for="star_<?= $s ?>"
                                           title="<?= $s ?> star<?= $s > 1 ? 's' : '' ?>"
                                           style="cursor:pointer;font-size:1.5rem;color:#767676;transition:color .15s">
                                        ★
                                    </label>
                                </div>
                            <?php endfor; ?>
                        </div>
                    </div>

                    <!-- review body -->
                    <div class="mb-3">
                        <label for="review_body" class="form-label">
                            Your Review <span class="text-danger" aria-hidden="true">*</span>
                            <span class="text-muted small fw-normal">(20–2,000 characters)</span>
                        </label>
                        <textarea id="review_body" name="review_body"
                                  class="form-control" rows="5"
                                  minlength="20" maxlength="2000" required
                                  placeholder="Share your experience…"></textarea>
                        <div class="text-muted small mt-1" id="reviewCharCount" aria-live="polite">
                            2,000 characters remaining
                        </div>
                    </div>

                    <button type="submit" class="btn btn-primary px-4">Submit Review</button>
                </form>
            </div>

            <!-- inline JS — star hover effect + character counter -->
            <script>
            (function () {
                var labels = document.querySelectorAll('.review-star-label');
                labels.forEach(function (lbl, idx) {
                    lbl.addEventListener('mouseenter', function () {
                        labels.forEach(function (l, i) {
                            l.style.color = i <= idx ? '#f4a000' : '#767676';
                        });
                    });
                    lbl.addEventListener('click', function () {
                        labels.forEach(function (l, i) {
                            l.style.color = i <= idx ? '#f4a000' : '#767676';
                        });
                    });
                });
                var radioGroup = document.querySelector('[role="group"]');
                if (radioGroup) {
                    radioGroup.addEventListener('mouseleave', function () {
                        var checked = document.querySelector('input[name="rating"]:checked');
                        var checkedIdx = checked ? parseInt(checked.value, 10) - 1 : -1;
                        labels.forEach(function (l, i) {
                            l.style.color = i <= checkedIdx ? '#f4a000' : '#767676';
                        });
                    });
                }

                // keyboard focus/blur feedback for screen reader and keyboard users
                var radios = document.querySelectorAll('input[name="rating"]');
                radios.forEach(function (radio, idx) {
                    radio.addEventListener('focus', function () {
                        labels.forEach(function (l, i) {
                            l.style.color = i <= idx ? '#f4a000' : '#767676';
                        });
                    });
                    radio.addEventListener('change', function () {
                        labels.forEach(function (l, i) {
                            l.style.color = i <= idx ? '#f4a000' : '#767676';
                        });
                    });
                    radio.addEventListener('blur', function () {
                        var checked = document.querySelector('input[name="rating"]:checked');
                        var checkedIdx = checked ? parseInt(checked.value, 10) - 1 : -1;
                        labels.forEach(function (l, i) {
                            l.style.color = i <= checkedIdx ? '#f4a000' : '#767676';
                        });
                    });
                });
                var textarea = document.getElementById('review_body');
                var countEl  = document.getElementById('reviewCharCount');
                textarea.addEventListener('input', function () {
                    var remaining = 2000 - textarea.value.length;
                    countEl.textContent = remaining.toLocaleString() + ' characters remaining';
                    countEl.style.color = remaining < 100 ? '#c0392b' : '';
                });

                // Auto-scroll to reviews section if #reviews is in the URL
                if (window.location.hash === '#reviews') {
                    var el = document.getElementById('reviews');
                    if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' });
                }
            }());
            </script>
        <?php endif; ?>
    </section>
    <?php
}

/**
 * Returns an HTML string of filled/empty star glyphs for a given rating.
 *
 * @param float  $rating  The rating value (0-5).
 * @param string $size    'sm' (default) or 'lg'.
 * @return string  HTML star glyphs.
 */
function renderStars(float $rating, string $size = 'sm'): string
{
    $fontSize = $size === 'lg' ? '1.4rem' : '1rem';
    $rounded  = round($rating);
    $html     = '<span aria-hidden="true" style="font-size:' . $fontSize . ';line-height:1">';
    for ($i = 1; $i <= 5; $i++) {
        $html .= $i <= $rounded
            ? '<span style="color:#f4a000">★</span>'
            : '<span style="color:#ccc">★</span>';
    }
    $html .= '</span>';
    return $html;
}