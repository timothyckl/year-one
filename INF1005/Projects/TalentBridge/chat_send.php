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
$message = trim($_POST['message'] ?? '');
$outgoingId = (int) ($_SESSION['unique_id'] ?? 0);

if (!$incomingId || !$outgoingId || $incomingId === $outgoingId || $message === '') {
    http_response_code(400);
    echo 'Invalid parameters.';
    exit;
}

try {
    $pdo = getConnection();

    // ensure messages table exists
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

    $stmt = $pdo->prepare(
        'INSERT INTO messages (incoming_id, outgoing_id, message)
         VALUES (:incoming_id, :outgoing_id, :message)'
    );
    $stmt->execute([
        ':incoming_id' => $incomingId,
        ':outgoing_id' => $outgoingId,
        ':message'     => $message,
    ]);
    echo 'OK';
} catch (PDOException $e) {
    http_response_code(500);
    error_log('chat_send PDOException: ' . $e->getMessage());
    echo 'Unable to send message. ' . htmlspecialchars($e->getMessage(), ENT_QUOTES | ENT_SUBSTITUTE, 'UTF-8');
}
