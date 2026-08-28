<?php
session_start();
require_once 'includes/auth.php';
require_once 'includes/db.php';

if (!isLoggedIn()) {
    http_response_code(401);
    echo 'You must be logged in.';
    exit;
}

$incomingId = isset($_POST['incoming_id']) ? (int) $_POST['incoming_id'] : 0;
$outgoingId = (int) ($_SESSION['unique_id'] ?? 0);

if (!$incomingId || !$outgoingId) {
    http_response_code(400);
    echo '<div class="tb-chat-empty">Invalid conversation.</div>';
    exit;
}

try {
    $pdo = getConnection();

    // ensure messages table exists (auto-create if necessary)
    $pdo->exec("CREATE TABLE IF NOT EXISTS messages (
        message_id   INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
        incoming_id  INT UNSIGNED NOT NULL,
        outgoing_id  INT UNSIGNED NOT NULL,
        message      TEXT NOT NULL,
        created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
        INDEX idx_messages_incoming (incoming_id),
        INDEX idx_messages_outgoing (outgoing_id),
        INDEX idx_messages_conv (incoming_id, outgoing_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

    // Backward compatibility with both the old and new schema
    $hasIncoming = (bool)$pdo->query("SHOW COLUMNS FROM messages LIKE 'incoming_id'")->fetch();
    $hasOutgoing = (bool)$pdo->query("SHOW COLUMNS FROM messages LIKE 'outgoing_id'")->fetch();
    $hasMessage = (bool)$pdo->query("SHOW COLUMNS FROM messages LIKE 'message'")->fetch();

    $incomingCol = $hasIncoming ? 'incoming_id' : 'incoming_msg_id';
    $outgoingCol = $hasOutgoing ? 'outgoing_id' : 'outgoing_msg_id';
    $messageCol = $hasMessage ? 'message' : 'msg';

    $sql = "SELECT {$incomingCol} AS incoming_id, {$outgoingCol} AS outgoing_id, {$messageCol} AS message, created_at"
         . " FROM messages"
         . " WHERE ({$incomingCol} = :incomingA AND {$outgoingCol} = :outgoingA)"
         . " OR ({$incomingCol} = :incomingB AND {$outgoingCol} = :outgoingB)"
         . " ORDER BY created_at ASC";

    $stmt = $pdo->prepare($sql);
    $stmt->execute([
        ':incomingA' => $incomingId,
        ':outgoingA' => $outgoingId,
        ':incomingB' => $outgoingId,
        ':outgoingB' => $incomingId,
    ]);
    $messages = $stmt->fetchAll(PDO::FETCH_ASSOC);

    if (!$messages) {
        echo '<div class="tb-chat-empty">No messages are available. Start the conversation.</div>';
        exit;
    }

    foreach ($messages as $msg) {
        $isOut = ((int)$msg['outgoing_id'] === $outgoingId);
        $css = $isOut ? 'tb-chat-msg tb-chat-msg--out' : 'tb-chat-msg tb-chat-msg--in';
        $author = $isOut ? 'You' : 'Them';
        $body = htmlspecialchars($msg['message'], ENT_QUOTES | ENT_SUBSTITUTE, 'UTF-8');
        echo '<div class="' . $css . '">';
        echo '<div class="tb-chat-msg-author">' . $author . ' <span class="text-muted" style="font-size:.65rem;">' . date('H:i', strtotime($msg['created_at'])) . '</span></div>';
        echo '<div class="tb-chat-msg-text">' . $body . '</div>';
        echo '</div>';
    }
} catch (PDOException $e) {
    http_response_code(500);
    error_log('chat_fetch PDOException: ' . $e->getMessage());
    echo '<div class="tb-chat-empty">Unable to load messages right now.</div>';
    echo '<pre style="font-size:.7rem;color:#a00;white-space:pre-wrap;">' . htmlspecialchars($e->getMessage(), ENT_QUOTES | ENT_SUBSTITUTE, 'UTF-8') . '</pre>';
}
