<?php
/**
 * Unit tests for includes/csrf.php.
 *
 * Covers generateCsrfToken() and validateCsrfToken(). Tests manipulate
 * $_SESSION directly to control token state without a real session.
 *
 * @package TalentBridge
 */

use PHPUnit\Framework\TestCase;

class CsrfTest extends TestCase
{
    /**
     * Reset the session before each test to prevent state bleed between cases.
     */
    protected function setUp(): void
    {
        $_SESSION = [];
    }

    // -- generateCsrfToken() -------------------------------------------------

    public function testTokenIs64CharacterHexString(): void
    {
        $token = generateCsrfToken();
        $this->assertMatchesRegularExpression('/^[0-9a-f]{64}$/', $token);
    }

    public function testSameTokenReturnedOnSubsequentCalls(): void
    {
        $first  = generateCsrfToken();
        $second = generateCsrfToken();
        $this->assertSame($first, $second);
    }

    public function testTokenStoredInSession(): void
    {
        $token = generateCsrfToken();
        $this->assertSame($token, $_SESSION['csrf_token']);
    }

    // -- validateCsrfToken() -------------------------------------------------

    public function testValidatesMatchingToken(): void
    {
        $_SESSION['csrf_token'] = 'abc123def456';
        $this->assertTrue(validateCsrfToken('abc123def456'));
    }

    public function testRejectsMismatchedToken(): void
    {
        $_SESSION['csrf_token'] = 'abc123def456';
        $this->assertFalse(validateCsrfToken('xyz789'));
    }

    public function testRejectsEmptySubmittedToken(): void
    {
        $_SESSION['csrf_token'] = 'abc123def456';
        $this->assertFalse(validateCsrfToken(''));
    }

    public function testRejectsWhenNoSessionTokenExists(): void
    {
        // no token set in session
        $this->assertFalse(validateCsrfToken('abc123def456'));
    }

    public function testGenerateAndValidateRoundTrip(): void
    {
        $token = generateCsrfToken();
        $this->assertTrue(validateCsrfToken($token));
    }
}
