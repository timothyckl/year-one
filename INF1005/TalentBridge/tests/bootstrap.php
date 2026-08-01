<?php
/**
 * PHPUnit bootstrap file.
 *
 * Initialises a bare session array so that $_SESSION-dependent functions in
 * the includes can be exercised without a running web server, then loads the
 * shared include files under test.
 *
 * @package TalentBridge
 */

// provide a plain array in place of a real php session
$_SESSION = [];

require_once __DIR__ . '/../includes/helpers.php';
require_once __DIR__ . '/../includes/csrf.php';
require_once __DIR__ . '/../includes/auth.php';
