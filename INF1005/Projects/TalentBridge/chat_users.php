<?php
session_start();
require_once 'includes/auth.php';
require_once 'includes/db.php';

header('Content-Type: application/json; charset=utf-8');

if (!isLoggedIn()) {
    http_response_code(401);
    echo json_encode([]);
    exit;
}

try {
    $pdo = getConnection();
    $q = trim($_GET['q'] ?? '');
    if ($q !== '') {
        $stmt = $pdo->prepare("SELECT unique_id, name, status FROM users WHERE user_id != :user_id AND is_active = 1 AND name LIKE :q ORDER BY name ASC");
        $stmt->execute([':user_id' => $_SESSION['user_id'], ':q' => '%' . $q . '%']);
    } else {
        $stmt = $pdo->prepare("SELECT unique_id, name, status FROM users WHERE user_id != :user_id AND is_active = 1 ORDER BY name ASC");
        $stmt->execute([':user_id' => $_SESSION['user_id']]);
    }
    $users = $stmt->fetchAll(PDO::FETCH_ASSOC);
    echo json_encode($users);

} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(['error' => $e->getMessage()]);
    exit;
}
