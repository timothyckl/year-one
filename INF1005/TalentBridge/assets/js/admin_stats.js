/**
 * admin_stats.js — animated counter for admin dashboard stat cards.
 *
 * Finds all elements with a data-stat-target attribute and animates their
 * displayed value from 0 up to the target number using requestAnimationFrame.
 * The animation duration scales with the target value so large numbers still
 * feel snappy.
 *
 * @module admin_stats
 */

'use strict';

(function () {

    /**
     * Animates a single counter element from 0 to its data-stat-target value.
     *
     * @param {HTMLElement} el - The element whose text content will be animated.
     * @returns {void}
     */
    function animateCounter(el) {
        const target   = parseInt(el.dataset.statTarget, 10);
        const suffix   = el.dataset.statSuffix || '';

        if (isNaN(target) || target < 0) {
            return;
        }

        // scale duration: small numbers animate quickly, large ones take longer
        const duration = Math.min(1500, Math.max(600, target * 2));
        const startTime = performance.now();

        /**
         * Single animation frame — recalculates the displayed value and
         * schedules the next frame until the target is reached.
         *
         * @param {number} now - The current timestamp from requestAnimationFrame.
         * @returns {void}
         */
        function step(now) {
            const elapsed  = now - startTime;
            const progress = Math.min(elapsed / duration, 1);

            // ease-out cubic — fast start, gentle finish
            const eased  = 1 - Math.pow(1 - progress, 3);
            const current = Math.round(eased * target);

            el.textContent = current.toLocaleString() + suffix;

            if (progress < 1) {
                requestAnimationFrame(step);
            }
        }

        requestAnimationFrame(step);
    }

    /**
     * Initialises all stat counters on the page.
     *
     * Uses an IntersectionObserver so counters only start animating when the
     * stat cards scroll into the viewport, rather than firing immediately on load.
     *
     * @returns {void}
     */
    function init() {
        const counters = document.querySelectorAll('[data-stat-target]');

        if (counters.length === 0) {
            return;
        }

        // animate immediately if IntersectionObserver is not available
        if (!('IntersectionObserver' in window)) {
            counters.forEach(animateCounter);
            return;
        }

        const observer = new IntersectionObserver(function (entries) {
            entries.forEach(function (entry) {
                if (entry.isIntersecting) {
                    animateCounter(entry.target);
                    // only animate once per page load
                    observer.unobserve(entry.target);
                }
            });
        }, { threshold: 0.3 });

        counters.forEach(function (el) {
            // set initial text to zero while waiting for the element to enter view
            el.textContent = '0' + (el.dataset.statSuffix || '');
            observer.observe(el);
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

}());
