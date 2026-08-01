/**
 * filter.js — live client-side job card filtering.
 *
 * Reads data-industry, data-location, data-type, and data-keywords
 * attributes from each job card and hides/shows cards in real time as the
 * user types in the keyword box or changes the dropdown filters.
 * Shows a "no results" message when zero cards match.
 *
 * @module filter
 */

'use strict';

(function () {

    /**
     * Normalises a string to lowercase with leading/trailing whitespace removed,
     * for consistent comparison against data attributes.
     *
     * @param {string} value - The raw string to normalise.
     * @returns {string} The lowercase trimmed string.
     */
    function normalise(value) {
        return String(value || '').toLowerCase().trim();
    }

    /**
     * Reads the current filter values from all controls and filters the job
     * card list accordingly. Called on every input/change event.
     *
     * @returns {void}
     */
    function applyFilters() {
        const keyword  = normalise(document.getElementById('filterKeyword')?.value  || '');
        const industry = normalise(document.getElementById('filterIndustry')?.value || '');
        const location = normalise(document.getElementById('filterLocation')?.value || '');
        const type     = normalise(document.getElementById('filterType')?.value     || '');

        const cards       = document.querySelectorAll('[data-job-card]');
        const noResults   = document.getElementById('noResultsMsg');
        let   visibleCount = 0;

        cards.forEach(function (card) {
            const cardIndustry = normalise(card.dataset.industry || '');
            const cardLocation = normalise(card.dataset.location || '');
            const cardType     = normalise(card.dataset.type     || '');
            const cardKeywords = normalise(card.dataset.keywords || '');

            // each active filter must match; empty filter string matches everything
            const matchesKeyword  = !keyword  || cardKeywords.includes(keyword);
            const matchesIndustry = !industry || cardIndustry === industry;
            const matchesLocation = !location || cardLocation.includes(location);
            const matchesType     = !type     || cardType === type;

            const isVisible = matchesKeyword && matchesIndustry && matchesLocation && matchesType;

            // show or hide the outer column wrapper
            const col = card.closest('[data-job-col]') || card;
            col.style.display = isVisible ? '' : 'none';

            if (isVisible) {
                visibleCount++;
            }
        });

        // show the "no results" message when nothing matches
        if (noResults) {
            noResults.style.display = visibleCount === 0 ? 'block' : 'none';
        }
    }

    /**
     * Wires up event listeners once the DOM is ready.
     *
     * @returns {void}
     */
    function init() {
        const keyword  = document.getElementById('filterKeyword');
        const industry = document.getElementById('filterIndustry');
        const location = document.getElementById('filterLocation');
        const type     = document.getElementById('filterType');

        // keyword field triggers on every keystroke
        keyword?.addEventListener('input', applyFilters);

        // dropdowns trigger on selection change
        industry?.addEventListener('change', applyFilters);
        location?.addEventListener('change', applyFilters);
        type?.addEventListener('change',     applyFilters);

        // run once on load to handle pre-filled filter values (e.g. browser back)
        applyFilters();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

}());
