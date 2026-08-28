/**
 * admin_search.js — Reusable real-time search for admin panels.
 * Provides a seamless SPA-like experience by hot-swapping DOM containers.
 */
document.addEventListener('DOMContentLoaded', () => {
    const searchForm = document.querySelector('.realtime-search-form');
    if (!searchForm) return;

    const searchInput = searchForm.querySelector('input[type="search"], input[type="text"]');
    const clearBtn = searchForm.querySelector('.btn-clear-search');
    
    if (!searchInput) return;

    let debounceTimeout;

    // Trigger search as user types
    searchInput.addEventListener('input', () => {
        clearTimeout(debounceTimeout);
        debounceTimeout = setTimeout(() => {
            performSearch(searchInput.value);
        }, 300); 
    });

    // Handle Enter key
    searchForm.addEventListener('submit', (e) => {
        e.preventDefault();
        clearTimeout(debounceTimeout);
        performSearch(searchInput.value);
    });

    // Handle Clear button
    if (clearBtn) {
        clearBtn.addEventListener('click', (e) => {
            e.preventDefault();
            searchInput.value = '';
            performSearch('');
            searchInput.focus();
        });
    }

    function performSearch(query) {
        // 1. Re-scan for containers every time (Fixes the "Not Updating" issue)
        const updateContainers = document.querySelectorAll('.realtime-update-container');
        
        // 2. Build URL using the current window state to preserve sort/filter/limit
        const url = new URL(searchForm.action || window.location.href, window.location.origin);

        // 3. Scrape ALL form inputs (Hidden and Visible, including selects)
        const allInputs = searchForm.querySelectorAll('input, select');
        allInputs.forEach(input => {
            if (input.name && input.value.trim() !== '') {
                url.searchParams.set(input.name, input.value);
            }
        });

        // 4. Force current search and reset page
        url.searchParams.set(searchInput.name, query);
        url.searchParams.delete('page');

        // Update URL bar
        window.history.replaceState({}, '', url);

        // UI Visual Feedback
        updateContainers.forEach(c => c.style.opacity = '0.5');
        if (clearBtn) clearBtn.style.display = query.trim() !== '' ? 'inline-block' : 'none';

        // 5. Fetch and Swap
        fetch(url, { headers: { 'X-Requested-With': 'XMLHttpRequest' } })
            .then(response => response.text())
            .then(html => {
                const parser = new DOMParser();
                const doc = parser.parseFromString(html, 'text/html');
                
                updateContainers.forEach(container => {
                    const id = container.id;
                    if (!id) return;
                    
                    const newContent = doc.getElementById(id);
                    if (newContent) {
                        container.innerHTML = newContent.innerHTML;
                    }
                });
            })
            .catch(err => console.error('Real-time search error:', err))
            .finally(() => {
                updateContainers.forEach(c => c.style.opacity = '1');
            });
    }
});