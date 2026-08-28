<?php
/**
 * Admin messages page — manage contact form submissions.
 *
 * Displays all contact_messages; unread messages are visually highlighted.
 * Supports marking as read (UPDATE is_read=1) and permanent deletion,
 * completing the CRUD cycle for contact_messages. All actions use POST
 * with CSRF.
 *
 * @package TalentBridge
 */

session_start();
require_once '../includes/helpers.php';
require_once '../includes/auth.php';
require_once '../includes/db.php';
require_once '../includes/csrf.php';

requireRole('admin');

// ---- handle POST actions ----
if ($_SERVER['REQUEST_METHOD'] === 'POST') {

    if (!validateCsrfToken($_POST['csrf_token'] ?? '')) {
        http_response_code(403);
        exit('Invalid CSRF token.');
    }

    $action    = $_POST['action']     ?? '';
    $messageId = (int) ($_POST['message_id'] ?? 0);

    if ($messageId > 0) {
        try {
            $pdo = getConnection();

            if ($action === 'mark_read') {
                $pdo->prepare("UPDATE contact_messages SET is_read = 1 WHERE message_id = :mid")
                    ->execute([':mid' => $messageId]);
                setFlash('success', 'Message marked as read.');

            } elseif ($action === 'delete') {
                $pdo->prepare("DELETE FROM contact_messages WHERE message_id = :mid")
                    ->execute([':mid' => $messageId]);
                setFlash('success', 'Message deleted.');
            }

        } catch (PDOException $e) {
            setFlash('error', 'Action failed. Please try again.');
        }
    }

    redirect('/admin/messages.php');
}

// optional read/unread filter
$filterRead = $_GET['filter'] ?? '';

// ---- fetch messages ----
try {
    $pdo = getConnection();

    $whereClause = '';
    $params      = [];

    if ($filterRead === 'unread') {
        $whereClause = 'WHERE is_read = 0';
    } elseif ($filterRead === 'read') {
        $whereClause = 'WHERE is_read = 1';
    }

    $stmt = $pdo->prepare("
        SELECT message_id, name, email, subject, body, submitted_at, is_read
          FROM contact_messages
        $whereClause
         ORDER BY submitted_at DESC
    ");
    $stmt->execute($params);
    $messages = $stmt->fetchAll();

    $unreadStmt = $pdo->prepare("SELECT COUNT(*) FROM contact_messages WHERE is_read = 0");
    $unreadStmt->execute([]);
    $unreadCount = (int) $unreadStmt->fetchColumn();

} catch (PDOException $e) {
    $messages    = [];
    $unreadCount = 0;
}

$flash     = getFlash();
$csrfToken = generateCsrfToken();
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Contact Messages — TalentBridge Admin</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="../assets/css/style.css" rel="stylesheet">
    <style>
        /* highlight unread message rows */
        .message-unread {
            background-color: #eaf4fd;
            border-left: 4px solid var(--tb-accent);
        }
        .message-read {
            border-left: 4px solid transparent;
        }
    </style>
</head>
<body>

<?php require_once '../includes/nav.php'; ?>

<main id="main-content">
    <section class="tb-section">
        <div class="container">

            <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-3">
                <div>
                    <h1 class="tb-section-title mb-1">
                        Contact Messages
                        <?php if ($unreadCount > 0): ?>
                            <span class="badge bg-danger ms-2" style="font-size:.7rem">
                                <?= $unreadCount ?> unread
                            </span>
                        <?php endif; ?>
                    </h1>
                    <div class="tb-divider"></div>
                </div>
                <a href="/admin/dashboard.php" class="btn btn-outline-primary btn-sm"><span aria-hidden="true">←</span> Dashboard</a>
            </div>

            <?php foreach ($flash as $type => $msg): ?>
                <div class="alert alert-<?= $type === 'error' ? 'danger' : sanitise($type) ?> tb-flash" role="alert">
                    <?= sanitise($msg) ?>
                </div>
            <?php endforeach; ?>

            <!-- filter tabs -->
            <div class="mb-4">
                <nav aria-label="Filter messages by read status">
                    <ul class="nav nav-pills gap-1">
                        <li class="nav-item">
                            <a class="nav-link <?= $filterRead === '' ? 'active' : '' ?>"
                               href="/admin/messages.php"
                               <?= $filterRead === '' ? 'aria-current="page"' : '' ?>>All</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link <?= $filterRead === 'unread' ? 'active' : '' ?>"
                               href="/admin/messages.php?filter=unread"
                               <?= $filterRead === 'unread' ? 'aria-current="page"' : '' ?>>Unread</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link <?= $filterRead === 'read' ? 'active' : '' ?>"
                               href="/admin/messages.php?filter=read"
                               <?= $filterRead === 'read' ? 'aria-current="page"' : '' ?>>Read</a>
                        </li>
                    </ul>
                </nav>
            </div>

            <p class="text-muted small mb-3">
                <?= count($messages) ?> message<?= count($messages) !== 1 ? 's' : '' ?>
            </p>

            <?php if (empty($messages)): ?>
                <div class="alert alert-info text-center py-4">No messages found.</div>

            <?php else: ?>
                <div class="d-flex flex-column gap-3">
                    <?php foreach ($messages as $msg): ?>
                        <?php $isUnread = !$msg['is_read']; ?>
                        <div class="tb-card p-0 overflow-hidden <?= $isUnread ? 'message-unread' : 'message-read' ?>"
                             <?= $isUnread ? 'aria-label="Unread message"' : '' ?>>

                            <div class="p-4">
                                <div class="d-flex justify-content-between align-items-start flex-wrap gap-2 mb-2">
                                    <div>
                                        <h2 class="h6 fw-bold mb-0" style="color:var(--tb-primary)">
                                            <?= sanitise($msg['subject']) ?>
                                            <?php if ($isUnread): ?>
                                                <span class="badge bg-primary ms-2"
                                                      style="font-size:.65rem;vertical-align:middle">New</span>
                                            <?php endif; ?>
                                        </h2>
                                        <p class="text-muted small mb-0">
                                            From
                                            <strong><?= sanitise($msg['name']) ?></strong>
                                            &lt;<a href="mailto:<?= sanitise($msg['email']) ?>"><?= sanitise($msg['email']) ?></a>&gt;
                                            &middot; 
                                            <?php
                                            $submittedAt = new DateTime($msg['submitted_at'], new DateTimeZone('UTC')); 
                                            // $submittedAt->sub(new DateInterval('PT8H')); 
                                            $submittedAt->setTimezone(new DateTimeZone('Asia/Singapore')); // UTC+8
                                            echo $submittedAt->format('d M Y, H:i');
                                            ?>
                                        </p>
                                    </div>
                                    <div class="d-flex gap-2 flex-shrink-0">

                                        <?php if ($isUnread): ?>
                                            <!-- mark as read -->
                                            <form method="post" action="/admin/messages.php">
                                                <input type="hidden" name="csrf_token"  value="<?= sanitise($csrfToken) ?>">
                                                <input type="hidden" name="action"      value="mark_read">
                                                <input type="hidden" name="message_id"  value="<?= (int)$msg['message_id'] ?>">
                                                <button type="submit" class="btn btn-sm btn-outline-primary">
                                                    Mark as Read
                                                </button>
                                            </form>
                                        <?php endif; ?>

                                        <!-- delete -->
                                        <form method="post" action="/admin/messages.php">
                                            <input type="hidden" name="csrf_token"  value="<?= sanitise($csrfToken) ?>">
                                            <input type="hidden" name="action"      value="delete">
                                            <input type="hidden" name="message_id"  value="<?= (int)$msg['message_id'] ?>">
                                            <button type="submit" class="btn btn-sm btn-outline-danger"
                                                onclick="return confirm('Delete this message permanently?')">
                                                Delete
                                            </button>
                                        </form>

                                    </div>
                                </div>

                                <!-- message body -->
                                <div class="mt-3 p-3 rounded small"
                                     style="background:rgba(255,255,255,0.7);line-height:1.6;">
                                    <?= nl2br(sanitise($msg['body'])) ?>
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
