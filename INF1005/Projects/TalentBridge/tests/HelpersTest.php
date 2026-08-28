<?php
/**
 * Unit tests for includes/helpers.php.
 *
 * Covers sanitise(), setFlash(), and getFlash(). The redirect() function is
 * excluded because it calls exit(), which would terminate the test runner.
 *
 * @package TalentBridge
 */

use PHPUnit\Framework\TestCase;

class HelpersTest extends TestCase
{
    /**
     * Reset the session before each test to prevent state bleed between cases.
     */
    protected function setUp(): void
    {
        $_SESSION = [];
    }

    // -- sanitise() ----------------------------------------------------------

    public function testSanitisesScriptTag(): void
    {
        $this->assertSame(
            '&lt;script&gt;alert(1)&lt;/script&gt;',
            sanitise('<script>alert(1)</script>')
        );
    }

    public function testSanitisesDoubleQuotes(): void
    {
        $this->assertSame('&quot;hello&quot;', sanitise('"hello"'));
    }

    public function testSanitisesSingleQuotes(): void
    {
        $this->assertSame('&#039;hello&#039;', sanitise("'hello'"));
    }

    public function testSanitisesNull(): void
    {
        $this->assertSame('', sanitise(null));
    }

    public function testSanitisesPlainStringUnchanged(): void
    {
        $this->assertSame('Hello, World!', sanitise('Hello, World!'));
    }

    // -- setFlash() / getFlash() ---------------------------------------------

    public function testFlashMessagesRoundTrip(): void
    {
        setFlash('success', 'Profile saved.');
        $this->assertSame(['success' => 'Profile saved.'], getFlash());
    }

    public function testGetFlashClearsMessagesAfterRead(): void
    {
        setFlash('error', 'Something went wrong.');
        getFlash(); // consume
        $this->assertSame([], getFlash());
    }

    public function testMultipleFlashTypesCoexist(): void
    {
        setFlash('success', 'Saved.');
        setFlash('warning', 'Check your input.');

        $messages = getFlash();

        $this->assertSame('Saved.', $messages['success']);
        $this->assertSame('Check your input.', $messages['warning']);
    }

    public function testGetFlashReturnsEmptyArrayWithNoMessages(): void
    {
        $this->assertSame([], getFlash());
    }
}
