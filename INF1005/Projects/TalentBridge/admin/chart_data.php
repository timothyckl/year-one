<?php
/**
 * Admin Dashboard Chart Data — AJAX endpoints for Chart.js visualizations
 *
 * Serves chart data for:
 * - New users over time (stacked area: employers vs seekers)
 * - Industry popularity (bar chart: top 5 industries)
 * - Job application statistics (donut chart: by status)
 * - Geographical popularity (bar chart: top locations)
 *
 * @package TalentBridge
 */

session_start();
require_once '../includes/auth.php';
require_once '../includes/db.php';

requireRole('admin');

header('Content-Type: application/json');

// determine the action
$action = $_GET['action'] ?? '';

try {
    $pdo = getConnection();

    switch ($action) {
        case 'new_users':
            echo json_encode(getNewUsersChartData($pdo));
            break;

        case 'industry_popularity':
            echo json_encode(getIndustryPopularityData($pdo));
            break;

        case 'job_applications':
            echo json_encode(getJobApplicationsChartData($pdo));
            break;

        case 'applications_by_industry':
            echo json_encode(getApplicationsByIndustryData($pdo));
            break;

        case 'geographical_popularity':
            echo json_encode(getGeographicalPopularityData($pdo));
            break;

        case 'seekers_by_location':
            echo json_encode(getSeekersByLocationData($pdo));
            break;

        default:
            http_response_code(400);
            echo json_encode(['error' => 'Invalid action']);
    }

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['error' => $e->getMessage()]);
}

/**
 * Fetch new users over time (stacked area chart)
 * Groups by date and role (employer/seeker), excludes admins
 *
 * @param PDO $pdo
 * @return array
 */
function getNewUsersChartData($pdo) {
    $params = [];
    $whereDate = '';

    if (isset($_GET['startDate']) && isset($_GET['endDate'])) {
        $whereDate = "AND DATE(created_at) BETWEEN :startDate AND :endDate";
        $params[':startDate'] = $_GET['startDate'];
        $params[':endDate'] = $_GET['endDate'];
    } else {
        $days = (int) ($_GET['days'] ?? 30);
        $whereDate = "AND created_at >= DATE_SUB(NOW(), INTERVAL :days DAY)";
        $params[':days'] = $days;
    }

    $stmt = $pdo->prepare("
        SELECT
            DATE(created_at) as date,
            role,
            COUNT(*) as count
        FROM users
        WHERE role IN ('seeker', 'employer') $whereDate
        GROUP BY DATE(created_at), role
        ORDER BY date ASC, role ASC
    ");
    $stmt->execute($params);
    $rows = $stmt->fetchAll(PDO::FETCH_ASSOC);

    // transform to chart format
    $dates = [];
    $seekerData = [];
    $employerData = [];

    foreach ($rows as $row) {
        $date = $row['date'];
        if (!in_array($date, $dates)) {
            $dates[] = $date;
            $seekerData[$date] = 0;
            $employerData[$date] = 0;
        }

        if ($row['role'] === 'seeker') {
            $seekerData[$date] = (int) $row['count'];
        } elseif ($row['role'] === 'employer') {
            $employerData[$date] = (int) $row['count'];
        }
    }

    // calculate max value for proper scaling
    $allValues = array_merge(array_values($seekerData), array_values($employerData));
    $maxValue = !empty($allValues) ? max($allValues) : 0;

    return [
        'dates' => $dates,
        'seekers' => array_values(array_map(function($d) use ($seekerData) { return $seekerData[$d] ?? 0; }, $dates)),
        'employers' => array_values(array_map(function($d) use ($employerData) { return $employerData[$d] ?? 0; }, $dates)),
        'maxValue' => $maxValue
    ];
}

/**
 * Fetch industry popularity data (top industries by active job listings)
 * Can filter by listing status and job type
 *
 * @param PDO $pdo
 * @return array
 */
function getIndustryPopularityData($pdo) {
    $includeInactive = isset($_GET['includeInactive']) && $_GET['includeInactive'] === '1';
    $jobType = isset($_GET['jobType']) && $_GET['jobType'] !== '' ? $_GET['jobType'] : null;

    $whereClause = "AND jl.status != 'draft'";
    if (!$includeInactive) {
        $whereClause = "AND jl.status = 'active'";
    } else {
        $whereClause = "AND jl.status IN ('active', 'closed')";
    }

    // Add job type filter if specified
    $typeFilter = '';
    if ($jobType) {
        $typeFilter = "AND jl.type = :jobType";
    }

    $query = "
        SELECT
            c.industry,
            COUNT(jl.job_id) as count
        FROM companies c
        LEFT JOIN job_listings jl ON c.company_id = jl.company_id
            $whereClause
            $typeFilter
        WHERE c.industry IS NOT NULL
            AND c.industry != ''
        GROUP BY c.industry
        ORDER BY count DESC
        LIMIT 10
    ";

    $stmt = $pdo->prepare($query);
    
    if ($jobType) {
        $stmt->bindValue(':jobType', $jobType, PDO::PARAM_STR);
    }
    
    $stmt->execute();
    $rows = $stmt->fetchAll(PDO::FETCH_ASSOC);

    $industries = [];
    $counts = [];

    foreach ($rows as $row) {
        $industries[] = $row['industry'] ?: 'Other';
        $counts[] = (int) $row['count'];
    }

    // Get available job types for UI
    $typesStmt = $pdo->prepare("SELECT DISTINCT type FROM job_listings WHERE type IS NOT NULL ORDER BY type");
    $typesStmt->execute();
    $jobTypes = array_column($typesStmt->fetchAll(PDO::FETCH_ASSOC), 'type');

    return [
        'industries' => $industries,
        'counts' => $counts,
        'jobTypes' => $jobTypes
    ];
}

/**
 * Fetch job application statistics (by status)
 *
 * @param PDO $pdo
 * @return array
 */
function getJobApplicationsChartData($pdo) {
    $params = [];
    $whereDate = '';

    if (isset($_GET['startDate']) && isset($_GET['endDate'])) {
        $whereDate = "WHERE DATE(applied_at) BETWEEN :startDate AND :endDate";
        $params[':startDate'] = $_GET['startDate'];
        $params[':endDate'] = $_GET['endDate'];
    } else {
        $days = (int) ($_GET['days'] ?? 30);
        $whereDate = "WHERE applied_at >= DATE_SUB(NOW(), INTERVAL :days DAY)";
        $params[':days'] = $days;
    }

    $stmt = $pdo->prepare("
        SELECT
            status,
            COUNT(*) as count
        FROM applications $whereDate
        GROUP BY status
    ");
    $stmt->execute($params);
    $rows = $stmt->fetchAll(PDO::FETCH_ASSOC);

    $statuses = ['Pending', 'Reviewed', 'Shortlisted', 'Rejected'];
    $statusColors = [
        'Pending' => '#FFC107',
        'Reviewed' => '#17A2B8',
        'Shortlisted' => '#28A745',
        'Rejected' => '#DC3545'
    ];

    $data = [];
    $total = 0;

    foreach ($statuses as $status) {
        $count = 0;
        foreach ($rows as $row) {
            if ($row['status'] === $status) {
                $count = (int) $row['count'];
                break;
            }
        }
        $data[] = [
            'status' => $status,
            'count' => $count,
            'color' => $statusColors[$status]
        ];
        $total += $count;
    }

    return [
        'data' => $data,
        'total' => $total
    ];
}

/**
 * Fetch geographical popularity data (top locations by active job listings)
 *
 * @param PDO $pdo
 * @return array
 */
function getGeographicalPopularityData($pdo) {
    // Get job listings by location
    $listingsStmt = $pdo->prepare("
        SELECT
            location,
            COUNT(job_id) as listing_count
        FROM job_listings
        WHERE status = 'active'
            AND location IS NOT NULL
            AND location != ''
        GROUP BY location
        ORDER BY listing_count DESC
        LIMIT 15
    ");
    $listingsStmt->execute();
    $listingsData = $listingsStmt->fetchAll(PDO::FETCH_ASSOC);

    // Get seekers by location
    $seekersStmt = $pdo->prepare("
        SELECT
            sp.location,
            COUNT(u.user_id) as seeker_count
        FROM seeker_profiles sp
        INNER JOIN users u ON sp.user_id = u.user_id
        WHERE u.role = 'seeker'
            AND sp.location IS NOT NULL
            AND sp.location != ''
        GROUP BY sp.location
        ORDER BY seeker_count DESC
        LIMIT 15
    ");
    $seekersStmt->execute();
    $seekersData = $seekersStmt->fetchAll(PDO::FETCH_ASSOC);

    // Merge data by location
    $locationMap = [];
    
    // Process listings
    foreach ($listingsData as $row) {
        $loc = $row['location'];
        if (!isset($locationMap[$loc])) {
            $locationMap[$loc] = ['listings' => 0, 'seekers' => 0];
        }
        $locationMap[$loc]['listings'] = (int) $row['listing_count'];
    }

    // Process seekers
    foreach ($seekersData as $row) {
        $loc = $row['location'];
        if (!isset($locationMap[$loc])) {
            $locationMap[$loc] = ['listings' => 0, 'seekers' => 0];
        }
        $locationMap[$loc]['seekers'] = (int) $row['seeker_count'];
    }

    // Sort by total activity (listings + seekers)
    uasort($locationMap, function($a, $b) {
        $totalA = $a['listings'] + $a['seekers'];
        $totalB = $b['listings'] + $b['seekers'];
        return $totalB <=> $totalA;
    });

    // Limit to top 15
    $locationMap = array_slice($locationMap, 0, 15);

    $locations = [];
    $listingCounts = [];
    $seekerCounts = [];

    foreach ($locationMap as $loc => $data) {
        $locations[] = $loc;
        $listingCounts[] = $data['listings'];
        $seekerCounts[] = $data['seekers'];
    }

    return [
        'locations' => $locations,
        'jobListings' => $listingCounts,
        'seekers' => $seekerCounts
    ];
}

/**
 * Fetch applications by industry data
 *
 * @param PDO $pdo
 * @return array
 */
function getApplicationsByIndustryData($pdo) {
    $industry = isset($_GET['industry']) && $_GET['industry'] !== '' ? $_GET['industry'] : null;

    // Get list of all industries for UI
    $industriesStmt = $pdo->prepare("
        SELECT DISTINCT industry
        FROM companies
        WHERE industry IS NOT NULL AND industry != ''
        ORDER BY industry
    ");
    $industriesStmt->execute();
    $availableIndustries = array_column($industriesStmt->fetchAll(PDO::FETCH_ASSOC), 'industry');

    $params = [];
    $whereClauses = [];

    // Date filter
    if (isset($_GET['startDate']) && isset($_GET['endDate'])) {
        $whereClauses[] = "DATE(a.applied_at) BETWEEN :startDate AND :endDate";
        $params[':startDate'] = $_GET['startDate'];
        $params[':endDate'] = $_GET['endDate'];
    } else {
        $days = (int) ($_GET['days'] ?? 30);
        $whereClauses[] = "a.applied_at >= DATE_SUB(NOW(), INTERVAL :days DAY)";
        $params[':days'] = $days;
    }

    $query = "SELECT a.status, COUNT(*) as count FROM applications a";

    if ($industry && in_array($industry, $availableIndustries)) {
        $query .= "
            INNER JOIN job_listings jl ON a.job_id = jl.job_id
            INNER JOIN companies c ON jl.company_id = c.company_id
        ";
        $whereClauses[] = "c.industry = :industry";
        $params[':industry'] = $industry;
    }

    $query .= " WHERE " . implode(' AND ', $whereClauses) . " GROUP BY a.status";

    $stmt = $pdo->prepare($query);
    $stmt->execute($params);
    $rows = $stmt->fetchAll(PDO::FETCH_ASSOC);

    $statuses = ['Pending', 'Reviewed', 'Shortlisted', 'Rejected'];
    $statusColors = [
        'Pending' => '#FFC107',
        'Reviewed' => '#17A2B8',
        'Shortlisted' => '#28A745',
        'Rejected' => '#DC3545'
    ];

    $data = [];
    $total = 0;

    foreach ($statuses as $status) {
        $count = 0;
        foreach ($rows as $row) {
            if ($row['status'] === $status) {
                $count = (int) $row['count'];
                break;
            }
        }
        $data[] = [
            'status' => $status,
            'count' => $count,
            'color' => $statusColors[$status]
        ];
        $total += $count;
    }

    return [
        'data' => $data,
        'total' => $total,
        'industries' => $availableIndustries,
        'selectedIndustry' => $industry
    ];
}

/**
 * Fetch seekers by location data
 *
 * @param PDO $pdo
 * @return array
 */
function getSeekersByLocationData($pdo) {
    // Get job listings by location
    $listingsStmt = $pdo->prepare("
        SELECT
            location,
            COUNT(job_id) as listing_count
        FROM job_listings
        WHERE status = 'active'
            AND location IS NOT NULL
            AND location != ''
        GROUP BY location
        ORDER BY listing_count DESC
        LIMIT 15
    ");
    $listingsStmt->execute();
    $listingsData = $listingsStmt->fetchAll(PDO::FETCH_ASSOC);

    // Get seekers by location
    $seekersStmt = $pdo->prepare("
        SELECT
            location,
            COUNT(u.user_id) as seeker_count
        FROM seeker_profiles sp
        INNER JOIN users u ON sp.user_id = u.user_id
        WHERE u.role = 'seeker'
            AND sp.location IS NOT NULL
            AND sp.location != ''
        GROUP BY sp.location
        ORDER BY seeker_count DESC
        LIMIT 15
    ");
    $seekersStmt->execute();
    $seekersData = $seekersStmt->fetchAll(PDO::FETCH_ASSOC);

    // Merge data by location
    $locations = [];
    $listingCounts = [];
    $seekerCounts = [];

    $locationMap = [];
    
    // Process listings
    foreach ($listingsData as $row) {
        $loc = $row['location'];
        if (!isset($locationMap[$loc])) {
            $locationMap[$loc] = ['listings' => 0, 'seekers' => 0];
        }
        $locationMap[$loc]['listings'] = (int) $row['listing_count'];
    }

    // Process seekers
    foreach ($seekersData as $row) {
        $loc = $row['location'];
        if (!isset($locationMap[$loc])) {
            $locationMap[$loc] = ['listings' => 0, 'seekers' => 0];
        }
        $locationMap[$loc]['seekers'] = (int) $row['seeker_count'];
    }

    // Sort by total activity (listings + seekers)
    uasort($locationMap, function($a, $b) {
        $totalA = $a['listings'] + $a['seekers'];
        $totalB = $b['listings'] + $b['seekers'];
        return $totalB <=> $totalA;
    });

    // Limit to top 15
    $locationMap = array_slice($locationMap, 0, 15);

    foreach ($locationMap as $loc => $data) {
        $locations[] = $loc;
        $listingCounts[] = $data['listings'];
        $seekerCounts[] = $data['seekers'];
    }

    return [
        'locations' => $locations,
        'jobListings' => $listingCounts,
        'seekers' => $seekerCounts
    ];
}
