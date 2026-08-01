/**
 * application.js — apply form toggle on job_detail.php.
 *
 * Shows and hides the job application form when the "Apply Now" button is
 * clicked. Updates aria-expanded on the toggle button and smoothly scrolls
 * the viewport to the form when it opens.
 *
 * @module application
 */

'use strict';

(function () {

    /**
     * Initialises the apply form toggle behaviour.
     *
     * Expects the following elements in the DOM:
     *   #applyToggleBtn  — the button that shows/hides the form
     *   #applyFormPanel  — the container wrapping the application form
     *
     * @returns {void}
     */
    function init() {
        const toggleBtn = document.getElementById('applyToggleBtn');
        const formPanel = document.getElementById('applyFormPanel');

        if (!toggleBtn || !formPanel) {
            return;
        }

        // set the initial collapsed state
        formPanel.hidden = true;
        toggleBtn.setAttribute('aria-expanded', 'false');

        toggleBtn.addEventListener('click', function () {
            const isCurrentlyHidden = formPanel.hidden;

            if (isCurrentlyHidden) {
                // open the form
                formPanel.hidden = false;
                toggleBtn.setAttribute('aria-expanded', 'true');
                toggleBtn.textContent = 'Cancel Application';
                toggleBtn.classList.replace('btn-primary', 'btn-outline-secondary');

                // smooth scroll to the form so the user sees it
                formPanel.scrollIntoView({ behavior: 'smooth', block: 'start' });

            } else {
                // close the form
                formPanel.hidden = true;
                toggleBtn.setAttribute('aria-expanded', 'false');
                toggleBtn.textContent = 'Apply Now';
                toggleBtn.classList.replace('btn-outline-secondary', 'btn-primary');
            }
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

}());
