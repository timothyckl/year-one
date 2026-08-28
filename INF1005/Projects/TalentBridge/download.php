<?php
/**
 * CV download handler — auth-gated file delivery.
 *
 * Streams a seeker's CV to the browser only when the requester is:
 *   (a) the seeker who owns the file,
 *   (b) an employer who has received an application from this seeker, or
 *   (c) an admin.
 *
 * Files are served from outside the web root via readfile(). Path traversal
 * attempts are rejected before any filesystem access.
 *
 * @package TalentBridge
 */

session_start();
require_once 'includes/helpers.php';
require_once 'includes/auth.php';
require_once 'includes/db.php';

// must be authenticated to download anything
if (!isLoggedIn()) {
    redirect('/login.php');
}

$requestedUserId = (int) ($_GET['user_id'] ?? 0);
$currentUserId   = (int) $_SESSION['user_id'];
$currentRole     = getUserRole();

if ($requestedUserId <= 0) {
    http_response_code(400);
    exit('Invalid request.');
}

try {
    $pdo = getConnection();

    // fetch the cv path for the requested seeker
    $stmt = $pdo->prepare("
        SELECT sp.cv_path, u.name
          FROM seeker_profiles sp
          JOIN users u ON u.user_id = sp.user_id
         WHERE sp.user_id = :uid
    ");
    $stmt->execute([':uid' => $requestedUserId]);
    $seeker = $stmt->fetch();

} catch (PDOException $e) {
    http_response_code(500);
    exit('Server error.');
}

if (!$seeker || empty($seeker['cv_path'])) {
    http_response_code(404);
    exit('CV not found.');
}

$cvPath = $seeker['cv_path'];

// ---- authorisation check ----
$authorised = false;

if ($currentRole === 'admin') {
    // admins can download any cv
    $authorised = true;

} elseif ($currentRole === 'seeker' && $currentUserId === $requestedUserId) {
    // seekers can download their own cv
    $authorised = true;

} elseif ($currentRole === 'employer') {
    // employers may download the cv only if this seeker has applied to one of their jobs
    try {
        $checkStmt = $pdo->prepare("
            SELECT 1
              FROM applications a
              JOIN job_listings jl ON jl.job_id = a.job_id
              JOIN companies c     ON c.company_id = jl.company_id
             WHERE a.user_id  = :seeker_id
               AND c.user_id  = :employer_id
             LIMIT 1
        ");
        $checkStmt->execute([
            ':seeker_id'   => $requestedUserId,
            ':employer_id' => $currentUserId,
        ]);
        $authorised = (bool) $checkStmt->fetch();
    } catch (PDOException $e) {
        $authorised = false;
    }
}

if (!$authorised) {
    log_audit_event('CV_DOWNLOAD_FORBIDDEN', $currentUserId, ['requested_user_id' => $requestedUserId]);
    http_response_code(403);
    exit('You do not have permission to download this file.');
}

// log successful authorisation to download
log_audit_event('CV_DOWNLOAD_SUCCESS', $currentUserId, ['requested_user_id' => $requestedUserId]);

// ---- path traversal protection ----
// resolve the real path and confirm it is within the allowed uploads directory
$allowedDir  = '/var/uploads/cvs/';
$resolvedPath = realpath($cvPath);

if ($resolvedPath === false || !str_starts_with($resolvedPath, realpath($allowedDir))) {
    http_response_code(403);
    exit('Invalid file path.');
}

if (!file_exists($resolvedPath) || !is_file($resolvedPath)) {
    http_response_code(404);
    exit('File not found on server.');
}

// ---- determine content-type from the file extension ----
$ext = strtolower(pathinfo($resolvedPath, PATHINFO_EXTENSION));
$mimeTypes = [
    'pdf'  => 'application/pdf',
    'doc'  => 'application/msword',
    'docx' => 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
];
$contentType = $mimeTypes[$ext] ?? 'application/octet-stream';

// build a safe download filename for the browser using the seeker's name
$safeSeekername = preg_replace('/[^a-zA-Z0-9_\-]/', '_', $seeker['name']);
$downloadName   = 'CV_' . $safeSeekername . '.' . $ext;

// ---- stream the file ----
header('Content-Type: '        . $contentType);
header('Content-Disposition: attachment; filename="' . $downloadName . '"');
header('Content-Length: '      . filesize($resolvedPath));
header('Cache-Control: private, no-cache, no-store, must-revalidate');
header('Pragma: no-cache');
header('X-Content-Type-Options: nosniff');

// flush any buffered output before streaming
if (ob_get_level()) {
    ob_end_clean();
}

readfile($resolvedPath);
exit;
