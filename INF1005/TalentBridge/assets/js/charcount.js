/**
 * charcount.js — live character counter for textareas.
 *
 * Finds all textareas with a data-charcount attribute, reads their
 * data-maxlength value, and renders a live "X / N characters remaining"
 * counter below them. The counter is colour-coded:
 *   - green  when usage is below 80%
 *   - amber  when usage is between 80% and 100%
 *   - red    when the limit is exceeded (form submission is blocked)
 *
 * Usage: add data-charcount="true" and data-maxlength="N" to any textarea.
 *
 * @module charcount
 */

'use strict';

(function () {

    /**
     * Creates and manages a character counter for a single textarea element.
     *
     * @param {HTMLTextAreaElement} textarea - The textarea to attach the counter to.
     * @returns {void}
     */
    function attachCounter(textarea) {
        const maxLen = parseInt(textarea.dataset.maxlength, 10);

        if (isNaN(maxLen) || maxLen <= 0) {
            return;
        }

        // create the counter display element
        const counter = document.createElement('div');
        counter.className = 'char-count';
        counter.setAttribute('aria-live', 'polite');
        counter.setAttribute('aria-atomic', 'true');

        // insert the counter immediately after the textarea
        textarea.insertAdjacentElement('afterend', counter);

        /**
         * Refreshes the counter text and colour class based on current length.
         *
         * @returns {void}
         */
        function updateCounter() {
            const used      = textarea.value.length;
            const remaining = maxLen - used;

            counter.textContent = remaining + ' / ' + maxLen + ' characters remaining';

            // colour-code by proximity to the limit
            counter.className = 'char-count ' + (
                remaining < 0              ? 'count-danger' :
                used / maxLen >= 0.8       ? 'count-warn'   :
                                             'count-ok'
            );
        }

        textarea.addEventListener('input', updateCounter);
        updateCounter();
    }

    /**
     * Initialises counters on all qualifying textareas in the document.
     *
     * @returns {void}
     */
    function init() {
        document.querySelectorAll('textarea[data-charcount]').forEach(attachCounter);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

}());
