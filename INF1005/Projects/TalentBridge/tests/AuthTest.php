<?php
/**
 * Unit tests for includes/auth.php.
 *
 * Covers isLoggedIn() and getUserRole(). The requireRole() function is
 * excluded because it calls exit(), which would terminate the test runner.
 *
 * @package TalentBridge
 */

use PHPUnit\Framework\TestCase;

class AuthTest extends TestCase
{
    /**
     * Reset the session before each test to prevent state bleed between cases.
     */
    protected function setUp(): void
    {
        $_SESSION = [];
    }

    // -- isLoggedIn() --------------------------------------------------------

    public function testIsLoggedInReturnsFalseWithEmptySession(): void
    {
        $this->assertFalse(isLoggedIn());
    }

    public function testIsLoggedInReturnsTrueWhenUserIdIsSet(): void
    {
        $_SESSION['user_id'] = 42;
        $this->assertTrue(isLoggedIn());
    }

    public function testIsLoggedInReturnsFalseWhenUserIdIsZero(): void
    {
        // user_id of 0 should not be treated as a valid session
        $_SESSION['user_id'] = 0;
        $this->assertFalse(isLoggedIn());
    }

    public function testIsLoggedInReturnsFalseWhenUserIdIsEmptyString(): void
    {
        $_SESSION['user_id'] = '';
        $this->assertFalse(isLoggedIn());
    }

    // -- getUserRole() -------------------------------------------------------

    public function testGetUserRoleReturnsNullWhenNotLoggedIn(): void
    {
        $this->assertNull(getUserRole());
    }

    public function testGetUserRoleReturnsSeeker(): void
    {
        $_SESSION['role'] = 'seeker';
        $this->assertSame('seeker', getUserRole());
    }

    public function testGetUserRoleReturnsEmployer(): void
    {
        $_SESSION['role'] = 'employer';
        $this->assertSame('employer', getUserRole());
    }

    public function testGetUserRoleReturnsAdmin(): void
    {
        $_SESSION['role'] = 'admin';
        $this->assertSame('admin', getUserRole());
    }
}
