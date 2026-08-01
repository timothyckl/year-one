<?php
session_start();
require_once 'includes/auth.php';
require_once 'includes/db.php';
require_once 'includes/helpers.php';

if (!isLoggedIn()) {
    header('Location: /login.php');
    exit;
}

$partnerId = isset($_GET['user_id']) ? (int) $_GET['user_id'] : 0;
if (!$partnerId) {
    header('Location: /users.php');
    exit;
}

$pdo = getConnection();
$partner = null;
$stmt = $pdo->prepare('SELECT unique_id, name, status FROM users WHERE unique_id = :uid LIMIT 1');
$stmt->execute([':uid' => $partnerId]);
$partner = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$partner) {
    header('Location: /users.php');
    exit;
}

$currentUserId = $_SESSION['user_id'];
$currentUser = null;
$stmt = $pdo->prepare('SELECT name, status FROM users WHERE user_id = :user_id LIMIT 1');
$stmt->execute([':user_id' => $currentUserId]);
$currentUser = $stmt->fetch(PDO::FETCH_ASSOC);
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chat with <?= sanitise($partner['name']) ?> — TalentBridge</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="assets/css/style.css" rel="stylesheet">
    <style>
      .chat-box { min-height: 400px; max-height: 400px; overflow-y: auto; border: 1px solid #dee2e6; border-radius: .5rem; background:#f8f9fa; padding: .75rem; }
      .chat-out { text-align:right; }
      .chat-out .bubble { display:inline-block; background: #0d6efd; color:white; border-radius:.75rem; padding:.5rem .75rem; max-width:80%; margin:.3rem 0; }
      .chat-in .bubble { display:inline-block; background:#fff; color:#212529; border:1px solid #dee2e6; border-radius:.75rem; padding:.5rem .75rem; max-width:80%; margin:.3rem 0; }
    </style>
</head>
<body>
<?php require_once 'includes/nav.php'; ?>
<main id="main-content" class="container my-4">
  <div class="card">
    <div class="card-header d-flex justify-content-between align-items-center">
      <div>
        <h2 class="h5 mb-1">Chat with <?= sanitise($partner['name']) ?></h2>
        <small>Status: <?= sanitise($partner['status'] ?? 'Offline now') ?></small>
      </div>
      <a href="/users.php" class="btn btn-sm btn-outline-secondary">Back to Users</a>
    </div>
    <div class="card-body">
      <div id="chatBox" class="chat-box" aria-live="polite" aria-atomic="false">
        <div class="text-muted">Loading messages...</div>
      </div>
      <form id="chatForm" class="mt-3 d-flex" action="#" method="POST" onsubmit="return false;">
        <input id="messageInput" type="text" class="form-control me-2" placeholder="Type a message" autocomplete="off" aria-label="Type a message">
        <button id="sendBtn" type="submit" class="btn btn-primary">Send</button>
      </form>
    </div>
  </div>
</main>
<script>
const partnerId = <?= (int)$partner['unique_id'] ?>;
const chatBox = document.getElementById('chatBox');
const messageInput = document.getElementById('messageInput');
const chatForm = document.getElementById('chatForm');
let polling = false;
let pollPending = false;

function queueFetch() {
    if (pollPending) return;
    pollPending = true;
    setTimeout(() => {
        pollPending = false;
        fetchMessages();
    }, 1000);
}

function fetchMessages() {
    if (polling) return; // avoid overlap
    polling = true;

    const body = new URLSearchParams({ incoming_id: partnerId });
    fetch('chat_fetch.php', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: body.toString(),
        credentials: 'same-origin'
    }).then(res => {
        if (!res.ok) throw new Error('HTTP ' + res.status);
        return res.text();
    })
      .then(html => {
          chatBox.innerHTML = html;
          chatBox.scrollTop = chatBox.scrollHeight;
      })
      .catch(err => {
          console.error('chat_fetch', err);
          chatBox.innerHTML = '<div class="text-danger">Unable to load messages.</div><div style="font-size:.7rem;color:#a00;">' + err.message + '</div>';
      })
      .finally(() => {
          polling = false;
          queueFetch();
      });
}

function sendMessage() {
    const message = messageInput.value.trim();
    if (!message) return;

    const body = new URLSearchParams({ incoming_id: partnerId, message: message });
    fetch('chat_send.php', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: body.toString(),
        credentials: 'same-origin'
    }).then(res => {
        if (!res.ok) throw new Error('HTTP ' + res.status);
        return res.text();
    })
      .then(() => { messageInput.value = ''; fetchMessages(); })
      .catch(err => { console.error('chat_send', err); });
}

chatForm.addEventListener('submit', function (event) {
    event.preventDefault();
    sendMessage();
});

// Start near real-time chat polling
fetchMessages();
</script>
</body>