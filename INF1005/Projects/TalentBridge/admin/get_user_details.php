<?php
/**
 * AJAX endpoint to fetch detailed user information for the admin panel.
 *
 * Responds with a JSON object containing basic user info and role-specific
 * details for seekers (profile, applications) or employers (company, listings).
 * This is a protected endpoint, accessible only by administrators.
 *
 * @package TalentBridge
 */

session_start();
require_once '../includes/helpers.php';
require_once '../includes/auth.php';
require_once '../includes/db.php';

// This is an admin-only endpoint.
requireRole('admin');

header('Content-Type: application/json');

$userId = (int)($_GET['id'] ?? 0);

if ($userId <= 0) {
    http_response_code(400);
    echo json_encode(['error' => 'Invalid user ID.']);
    exit;
}

try {
    $pdo = getConnection();
    
    // 1. Get basic user info
    $stmt = $pdo->prepare("SELECT user_id, name, email, role, created_at FROM users WHERE user_id = ?");
    $stmt->execute([$userId]);
    $user = $stmt->fetch();

    if (!$user) {
        http_response_code(404);
        echo json_encode(['error' => 'User not found.']);
        exit;
    }

    $response = ['user' => $user, 'role_details' => null];

    // 2. Get role-specific details
    if ($user['role'] === 'seeker') {
        // Get seeker profile
        $profileStmt = $pdo->prepare("SELECT headline, location FROM seeker_profiles WHERE user_id = ?");
        $profileStmt->execute([$userId]);
        $profile = $profileStmt->fetch() ?: ['headline' => null, 'location' => null];

        // Get recent applications
        $appsStmt = $pdo->prepare("
            SELECT jl.job_id, jl.title, c.company_name, a.status, a.applied_at
            FROM applications a
            JOIN job_listings jl ON a.job_id = jl.job_id
            JOIN companies c ON jl.company_id = c.company_id
            WHERE a.user_id = ?
            ORDER BY a.applied_at DESC
            LIMIT 5
        ");
        $appsStmt->execute([$userId]);
        $applications = $appsStmt->fetchAll();

        $response['role_details'] = [
            'profile' => ['professional_headline' => $profile['headline'], 'location' => $profile['location']],
            'applications' => $applications,
        ];

    } elseif ($user['role'] === 'employer') {
        // Get company profile
        $companyStmt = $pdo->prepare("SELECT company_id, company_name, industry, description FROM companies WHERE user_id = ?");
        $companyStmt->execute([$userId]);
        $company = $companyStmt->fetch();

        if ($company) {
            // Get recent listings
            $listingsStmt = $pdo->prepare("
                SELECT job_id, title, status, created_at FROM job_listings WHERE company_id = ? ORDER BY created_at DESC LIMIT 5
            ");
            $listingsStmt->execute([$company['company_id']]);
            $listings = $listingsStmt->fetchAll();

            // Get recent reviews (assuming 'reviews' table structure)
            // This block will gracefully handle if the 'reviews' table does not exist, thanks to the outer try-catch.
            $reviews = [];
            try {
                $reviewsStmt = $pdo->prepare("
                    SELECT r.rating, r.review_text, r.created_at, u.name as seeker_name
                    FROM reviews r
                    JOIN users u ON r.seeker_id = u.user_id
                    WHERE r.company_id = ?
                    ORDER BY r.created_at DESC
                    LIMIT 5
                ");
                $reviewsStmt->execute([$company['company_id']]);
                $reviews = $reviewsStmt->fetchAll();
            } catch (PDOException $e) {
                // If the reviews table doesn't exist, this will fail. We can ignore it and just return no reviews.
                error_log("Could not fetch reviews for company {$company['company_id']}: " . $e->getMessage());
            }

            $response['role_details'] = [
                'company' => $company,
                'listings' => $listings,
                'reviews' => $reviews,
            ];
        }
    }

    echo json_encode($response);

} catch (PDOException $e) {
    error_log("get_user_details.php PDOException: " . $e->getMessage());
    http_response_code(500);
    echo json_encode(['error' => 'A database error occurred while fetching user details.']);
}