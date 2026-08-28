/**
 * Admin Dashboard Charts — Chart.js visualization and interactions
 *
 * Handles rendering and filtering for:
 * - New users over time (stacked vs overlapping area chart)
 * - Industry popularity (bar chart with status & type filters)
 * - Job applications (dual donut charts - overall & by industry)
 * - Geographical popularity (grouped bar chart - listings vs seekers)
 *
 * @package TalentBridge
 */

let isStackedMode = true; // Track stacked vs overlapping mode
let currentNewUsersParams = { days: 30 }; // Track current date range for new users
let geoOrderBy = 'listings'; // Track geography chart ordering (listings or seekers)

// Initialise charts. This pattern ensures that the script runs correctly whether it's
// loaded normally or deferred/async.
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initCharts);
} else {
    initCharts();
}

function initCharts() {
    // attach global event listeners that don't depend on charts being loaded
    setupEventListeners();
    setupDownloadButtons();

    // set up the observer to lazy-load charts as they scroll into view
    lazyLoadCharts();
}

/**
 * Sets up an IntersectionObserver to load and render charts only when they
 * become visible in the viewport.
 */
function lazyLoadCharts() {
    const chartCards = document.querySelectorAll('[data-chart-init]');
    if (chartCards.length === 0) return;

    const observer = new IntersectionObserver((entries, obs) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const card = entry.target;
                const chartName = card.dataset.chartInit;

                // a map of chart names to their loading functions
                const chartLoaders = {
                    'new_users': () => loadChartData('new_users', { days: 30 }),
                    'industry_popularity': () => loadChartData('industry_popularity', {}),
                    'applications': () => {
                        // this card has two charts that should load together
                        return Promise.all([
                            loadChartData('job_applications', { days: 30 }),
                            loadChartData('applications_by_industry', { days: 30 })
                        ]);
                    },
                    'geographical_popularity': () => loadChartData('geographical_popularity')
                };

                if (chartLoaders[chartName]) {
                    // load the chart(s)
                    chartLoaders[chartName]().catch(err => {
                        console.error(`Failed to load chart '${chartName}':`, err);
                        showError(`Failed to load chart: ${chartName}`);
                    });
                }

                // stop observing this card so it only loads once
                obs.unobserve(card);
            }
        });
    }, { threshold: 0.08 }); // trigger when 50% of the card is visible

    chartCards.forEach(card => {
        observer.observe(card);
    });
}

function setupEventListeners() {
    // new users date range filter
    document.getElementById('newUsersDateRange')?.addEventListener('change', function (e) {
        const value = e.target.value;
        const customContainer = document.getElementById('newUsersCustomDateRange');
        if (value === 'custom') {
            customContainer.style.display = 'flex';
        } else {
            customContainer.style.display = 'none';
            currentNewUsersParams = { days: parseInt(value) };
            updateNewUsersChart(currentNewUsersParams);
        }
    });
    document.getElementById('applyNewUsersDate')?.addEventListener('click', function() {
        const startDate = document.getElementById('newUsersStartDate').value;
        const endDate = document.getElementById('newUsersEndDate').value;
        if (startDate && endDate) {
            currentNewUsersParams = { startDate, endDate };
            updateNewUsersChart(currentNewUsersParams);
        }
    });

    // new users stack/overlap toggle
    document.getElementById('toggleStackMode')?.addEventListener('click', function() {
        isStackedMode = !isStackedMode;
        const btnText = this.querySelector('span');
        btnText.textContent = isStackedMode ? 'Stacked' : 'Overlapping';
        this.classList.toggle('active');
        
        // Re-render chart if data exists
        if (window.lastNewUsersData) {
            renderNewUsersChart(window.lastNewUsersData);
        } else {
            // Data not loaded yet, reload chart
            loadChartData('new_users', currentNewUsersParams);
        }
    });

    // industry filters
    document.getElementById('includeInactiveListings')?.addEventListener('change', function() {
        updateIndustryChart();
    });

    document.getElementById('jobTypeFilter')?.addEventListener('change', function() {
        updateIndustryChart();
    });

    function setAllIndustries(checkedState) {
        document.querySelectorAll('.industry-checkbox').forEach(checkbox => {
            checkbox.checked = checkedState;
            const label = document.querySelector(`label[for="${checkbox.id}"]`);
            if (label) {
                if (checkedState) {
                    label.classList.remove('btn-outline-secondary');
                    label.classList.add('btn-primary');
                } else {
                    label.classList.remove('btn-primary');
                    label.classList.add('btn-outline-secondary');
                }
            }
        });
        updateIndustryChartDisplay(); // Update chart display without re-fetching
    }

    document.getElementById('selectAllIndustries')?.addEventListener('click', () => setAllIndustries(true));
    document.getElementById('deselectAllIndustries')?.addEventListener('click', () => setAllIndustries(false));

    // applications date range filter
    document.getElementById('applicationsDateRange')?.addEventListener('change', function (e) {
        const value = e.target.value;
        const customContainer = document.getElementById('applicationsCustomDateRange');
        if (value === 'custom') {
            customContainer.style.display = 'flex';
        } else {
            customContainer.style.display = 'none';
            const params = { days: parseInt(value) };
            updateApplicationsChart(params);
            updateApplicationsByIndustry(params);
        }
    });
    document.getElementById('applyApplicationsDate')?.addEventListener('click', function() {
        const startDate = document.getElementById('applicationsStartDate').value;
        const endDate = document.getElementById('applicationsEndDate').value;
        if (startDate && endDate) {
            const params = { startDate, endDate };
            updateApplicationsChart(params);
            updateApplicationsByIndustry(params);
        }
    });

    // applications industry filter
    document.getElementById('industryApplicationFilter')?.addEventListener('change', function() {
        updateApplicationsByIndustry();
    });

    // industry filter checkboxes
    document.querySelectorAll('.industry-checkbox').forEach(checkbox => {
        checkbox.addEventListener('change', updateIndustryChart);
    });

    // geography chart controls
    document.getElementById('geoOrderToggle')?.addEventListener('click', function() {
        geoOrderBy = geoOrderBy === 'listings' ? 'seekers' : 'listings';
        const label = document.getElementById('geoOrderLabel');
        label.textContent = geoOrderBy === 'listings' ? 'Job Listings ▼' : 'Job Seekers ▼';
        updateGeographicalChart();
    });

    document.getElementById('geoCountLimit')?.addEventListener('change', function() {
        updateGeographicalChart();
    });
}

function loadChartData(action, params = {}) {
    let url = `/admin/chart_data.php?action=${action}`;

    if (params.days) {
        url += `&days=${params.days}`;
    } else if (params.startDate && params.endDate) {
        url += `&startDate=${encodeURIComponent(params.startDate)}&endDate=${encodeURIComponent(params.endDate)}`;
    }

    // Add query parameters based on action
    if (action === 'industry_popularity') {
        const includeInactive = document.getElementById('includeInactiveListings')?.checked ? 1 : 0;
        const jobType = document.getElementById('jobTypeFilter')?.value || '';
        url += `&includeInactive=${includeInactive}`; // This is a parameter for this action specifically
        if (jobType) url += `&jobType=${encodeURIComponent(jobType)}`;
    } 

    if (action === 'applications_by_industry') {
        const industry = document.getElementById('industryApplicationFilter')?.value || '';
        if (industry) url += `&industry=${encodeURIComponent(industry)}`;
    }

    return fetch(url)
        .then(res => {
            if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);
            return res.json();
        })
        .then(data => {
            if (data.error) throw new Error(data.error);
            
            switch (action) {
                case 'new_users':
                    window.lastNewUsersData = data;
                    renderNewUsersChart(data);
                    break;
                case 'industry_popularity':
                    renderIndustryChart(data);
                    break;
                case 'job_applications':
                    renderApplicationsChart(data);
                    break;
                case 'applications_by_industry':
                    window.lastApplicationsByIndustryData = data;
                    populateIndustryApplicationFilter(data);
                    renderApplicationsByIndustryChart(data);
                    break;
                case 'geographical_popularity':
                    window.lastGeographicalData = data;
                    renderGeographicalChart(data);
                    break;
            }
        });
}

/**
 * Render area chart - New users over time
 */
let newUsersChart = null;

function renderNewUsersChart(data) {
    const container = document.getElementById('newUsersChartContainer');
    if (!container) return;

    if (!data.dates || data.dates.length === 0) {
        container.innerHTML = '<div class="text-muted text-center py-5">No data available</div>';
        if (newUsersChart) {
            newUsersChart.destroy();
            newUsersChart = null;
        }
        return;
    }

    // Ensure canvas exists if it was removed by the "no data" message
    if (!container.querySelector('canvas')) {
        container.innerHTML = '<canvas id="newUsersChart"></canvas>';
    }

    const ctx = document.getElementById('newUsersChart');
    // destroy previous chart instance
    if (newUsersChart) {
        newUsersChart.destroy();
    }

    // Calculate appropriate max value with padding
    // When stacked, sum the values at each point and find the max sum
    // When overlapping, find the individual max
    let yMax;
    if (isStackedMode) {
        // Calculate sum at each point
        const sums = data.seekers.map((val, idx) => val + (data.employers[idx] || 0));
        const maxSum = Math.max(...sums, 0);
        yMax = Math.ceil(maxSum * 1.15);
    } else {
        // For overlapping, use the individual max
        const allValues = [...data.seekers, ...data.employers];
        const maxValue = Math.max(...allValues, 0);
        yMax = Math.ceil(maxValue * 1.15);
    }

    const datasets = [
        {
            label: 'Job Seekers',
            data: data.seekers,
            borderColor: '#28A745',
            backgroundColor: isStackedMode ? 'rgba(40, 167, 69, 0.2)' : 'rgba(40, 167, 69, 0.1)',
            borderWidth: 2,
            fill: true,
            tension: 0.4,
            pointRadius: 4,
            pointBackgroundColor: '#28A745',
            pointBorderColor: '#fff',
            pointBorderWidth: 2 }, {
            label: 'Employers',
            data: data.employers,
            borderColor: '#007BFF',
            backgroundColor: isStackedMode ? 'rgba(0, 123, 255, 0.2)' : 'rgba(0, 123, 255, 0.1)',
            borderWidth: 2,
            fill: true,
            tension: 0.4,
            pointRadius: 4,
            pointBackgroundColor: '#007BFF',
            pointBorderColor: '#fff',
            pointBorderWidth: 2 }
    ];

    newUsersChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: data.dates,
            datasets: datasets
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            interaction: {
                mode: 'index',
                intersect: false
            },
            plugins: {
                legend: {
                    display: false // Use custom HTML legend
                },
                filler: {
                    propagate: true
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    max: yMax,
                    stacked: isStackedMode,
                    ticks: {
                        stepSize: Math.ceil(yMax / 5)
                    }
                },
                x: {
                    stacked: isStackedMode
                }
            }
        }
    });

    // Render custom legend
    createCustomLegend(newUsersChart, 'newUsersLegendContainer');
}

function updateNewUsersChart(params) {
    loadChartData('new_users', params);
}

/**
 * Render bar chart - Industry popularity
 */
let industryChart = null;
let industryChartData = null;

function renderIndustryChart(data) {
    industryChartData = data;

    if (!data.industries || data.industries.length === 0) {
        document.getElementById('industryChartContainer').innerHTML =
            '<div class="text-muted text-center py-5">No data available</div>';
        return;
    }

    // Populate job type filter
    if (data.jobTypes && data.jobTypes.length > 0) {
        const typeSelect = document.getElementById('jobTypeFilter');
        const currentValue = typeSelect?.value || '';
        if (typeSelect && typeSelect.children.length === 1) {
            data.jobTypes.forEach(type => {
                const option = document.createElement('option');
                option.value = type;
                option.textContent = type;
                typeSelect.appendChild(option);
            });
            typeSelect.value = currentValue;
        }
    }

    // populate industry checkboxes
    const checkboxContainer = document.getElementById('industryCheckboxes');
    if (checkboxContainer) {
        checkboxContainer.innerHTML = '';

        // Sort industries alphabetically, keeping "Other" at the end if it exists
        const industriesToSort = [...data.industries];
        const otherIndustry = 'Other';
        const otherIndex = industriesToSort.findIndex(i => i.toLowerCase() === otherIndustry.toLowerCase());
        let otherValue = null;
        if (otherIndex > -1) {
            otherValue = industriesToSort.splice(otherIndex, 1)[0];
        }
        industriesToSort.sort((a, b) => a.localeCompare(b));
        if (otherValue) {
            industriesToSort.push(otherValue);
        }

        industriesToSort.forEach(industry => {
            const id = `industry_${industry.replace(/\s+/g, '-')}`;
            const wrapper = document.createElement('div');
            wrapper.className = 'form-check';
            wrapper.innerHTML = `
                <input class="form-check-input industry-checkbox d-none" type="checkbox" 
                       id="${id}" value="${industry}" checked>
                <label class="btn btn-sm btn-primary rounded-pill" for="${id}">
                    ${sanitizeHtml(industry)}
                </label>
            `;
            checkboxContainer.appendChild(wrapper);

            const checkbox = wrapper.querySelector('.industry-checkbox');
            const label = wrapper.querySelector('label');

            checkbox.addEventListener('change', function() {
                if (this.checked) {
                    label.classList.remove('btn-outline-secondary');
                    label.classList.add('btn-primary');
                } else {
                    label.classList.remove('btn-primary');
                    label.classList.add('btn-outline-secondary');
                }
                updateIndustryChart();
            });
        });
    }

    updateIndustryChartDisplay();
}

function updateIndustryChart() {
    const includeInactive = document.getElementById('includeInactiveListings')?.checked ? 1 : 0;
    const jobType = document.getElementById('jobTypeFilter')?.value || '';
    
    let url = `/admin/chart_data.php?action=industry_popularity&includeInactive=${includeInactive}`;
    if (jobType) url += `&jobType=${encodeURIComponent(jobType)}`;
    
    fetch(url)
        .then(res => res.json())
        .then(data => {
            industryChartData = data;
            updateIndustryChartDisplay();
        })
        .catch(err => console.error('Failed to update industry chart:', err));
}

function updateIndustryChartDisplay() {
    if (!industryChartData) return;

    // get selected industries
    const checkedBoxes = document.querySelectorAll('.industry-checkbox:checked');
    const selectedIndustries = Array.from(checkedBoxes).map(cb => cb.value);

    const container = document.getElementById('industryChartContainer');
    if (!container) return;

    if (selectedIndustries.length === 0) {
        container.innerHTML = '<div class="text-muted text-center py-5">Select industries to display</div>';
        if (industryChart) {
            industryChart.destroy();
            industryChart = null;
        }
        return;
    }

    // Ensure canvas exists if it was removed by the "no data" message
    if (!container.querySelector('canvas')) {
        container.innerHTML = '<canvas id="industryChart"></canvas>';
    }

    // filter data
    const filteredIndices = industryChartData.industries
        .map((ind, idx) => selectedIndustries.includes(ind) ? idx : -1)
        .filter(idx => idx !== -1);

    const filteredIndustries = filteredIndices.map(idx => industryChartData.industries[idx]);
    const filteredCounts = filteredIndices.map(idx => industryChartData.counts[idx]);

    const ctx = document.getElementById('industryChart');
    if (!ctx) return;

    // destroy previous chart
    if (industryChart) {
        industryChart.destroy();
    }

    industryChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: filteredIndustries,
            datasets: [
                {
                    label: 'Job Listings',
                    data: filteredCounts,
                    backgroundColor: '#007BFF',
                    borderColor: '#0056B3',
                    borderWidth: 1
                }
            ]
        },
        options: {
            indexAxis: 'y',
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false,
                    position: 'top'
                }
            },
            scales: {
                x: {
                    beginAtZero: true
                }
            }
        }
    });
}

/**
 * Render donut chart - Job applications by status
 */
let applicationsChart = null;

function renderApplicationsChart(data) {
    if (!data.data || data.data.length === 0) {
        document.getElementById('applicationsChartContainer').innerHTML =
            '<div class="text-muted text-center py-5">No data available</div>';
        return;
    }

    const ctx = document.getElementById('applicationsChart');
    if (!ctx) return;

    // destroy previous chart
    if (applicationsChart) {
        applicationsChart.destroy();
    }

    const labels = data.data.map(d => d.status);
    const counts = data.data.map(d => d.count);
    const colors = data.data.map(d => d.color);

    applicationsChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [
                {
                    data: counts,
                    backgroundColor: colors,
                    borderColor: '#fff',
                    borderWidth: 2
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: true,
                    position: 'bottom',
                    labels: {
                        padding: 15
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            return context.label + ': ' + context.parsed + ' applications';
                        }
                    }
                }
            }
        },
        plugins: [
            {
                id: 'textCenter',
                beforeDatasetsDraw(chart) {
                    const { width, height, ctx } = chart;
                    ctx.restore();

                    const labelFontSize = (height / 400).toFixed(2); // Smaller for the label
                    const valueFontSize = (height / 180).toFixed(2); // Larger for the number
        
                    ctx.textBaseline = 'middle';
                    ctx.textAlign = 'center';

                    const centerX = width / 2;
                    const centerY = height / 2;

                    ctx.font = `bold ${labelFontSize}em sans-serif`;
                    ctx.fillStyle = 'rgba(0, 0, 0, 0.4)';
                    ctx.fillText('TOTAL APPS:', centerX, centerY - 15); // Shifted up slightly

                    ctx.font = `bold ${valueFontSize}em sans-serif`;
                    ctx.fillStyle = '#000';
                    ctx.fillText(data.total, centerX, centerY + 15); // Shifted down slightl

                    ctx.save();
                }
            }
        ]
    });
}

function updateApplicationsChart(params) {
    loadChartData('job_applications', params);
}

/**
 * Populate industry filter for applications by industry
 */
function populateIndustryApplicationFilter(data) {
    if (!data.industries) return;
    
    const select = document.getElementById('industryApplicationFilter');
    if (!select) return;

    const currentValue = select.value;
    // Clear existing options except the first one
    while (select.children.length > 1) {
        select.removeChild(select.lastChild);
    }

    data.industries.forEach(industry => {
        const option = document.createElement('option');
        option.value = industry;
        option.textContent = industry;
        select.appendChild(option);
    });

    select.value = currentValue;
}

/**
 * Render donut chart - Applications by industry
 */
let applicationsIndustryChart = null;

function renderApplicationsByIndustryChart(data) {
    if (!data.data || data.data.length === 0) {
        document.getElementById('applicationsIndustryChartContainer').innerHTML =
            '<div class="text-muted text-center py-5">No data available</div>';
        return;
    }

    const ctx = document.getElementById('applicationsIndustryChart');
    if (!ctx) return;

    // destroy previous chart
    if (applicationsIndustryChart) {
        applicationsIndustryChart.destroy();
    }

    const labels = data.data.map(d => d.status);
    const counts = data.data.map(d => d.count);
    const colors = data.data.map(d => d.color);

    applicationsIndustryChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [
                {
                    data: counts,
                    backgroundColor: colors,
                    borderColor: '#fff',
                    borderWidth: 2
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: true,
                    position: 'bottom',
                    labels: {
                        padding: 15
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            return context.label + ': ' + context.parsed + ' applications';
                        }
                    }
                }
            }
        },
        plugins: [
            {
                id: 'textCenter',
                beforeDatasetsDraw(chart) {
                    const { width, height, ctx } = chart;
                    ctx.restore();

                    const labelFontSize = (height / 400).toFixed(2); // Smaller for the label
                    const valueFontSize = (height / 180).toFixed(2); // Larger for the number
        
                    ctx.textBaseline = 'middle';
                    ctx.textAlign = 'center';

                    const centerX = width / 2;
                    const centerY = height / 2;

                    ctx.font = `bold ${labelFontSize}em sans-serif`;
                    ctx.fillStyle = 'rgba(0, 0, 0, 0.4)';
                    ctx.fillText('TOTAL APPS:', centerX, centerY - 15); // Shifted up slightly

                    ctx.font = `bold ${valueFontSize}em sans-serif`;
                    ctx.fillStyle = '#000';
                    ctx.fillText(data.total, centerX, centerY + 15); // Shifted down slightl

                    ctx.save();
                }
            }
        ]
    });
}

function updateApplicationsByIndustry(params = {}) {
    // If date info is not already in params, get it from the UI.
    if (!params.days && !params.startDate) {
        const dateRangeSelect = document.getElementById('applicationsDateRange');
        const rangeValue = dateRangeSelect?.value;

        if (rangeValue === 'custom') {
            const startDate = document.getElementById('applicationsStartDate').value;
            const endDate = document.getElementById('applicationsEndDate').value;
            if (startDate && endDate) {
                params.startDate = startDate;
                params.endDate = endDate;
            } else {
                // Fallback to default 30 days if custom range is selected but incomplete
                params.days = 30;
            }
        } else if (rangeValue) {
            params.days = parseInt(rangeValue);
        } else {
            params.days = 30; // Fallback
        }
    }

    loadChartData('applications_by_industry', params);
}

/**
 * Render grouped bar chart - Geographical popularity (listings vs seekers)
 */
let geographicalChart = null;

function renderGeographicalChart(data) {
    if (!data.locations || data.locations.length === 0) {
        document.getElementById('geographicalChartContainer').innerHTML =
            '<div class="text-muted text-center py-5">No data available</div>';
        return;
    }

    const ctx = document.getElementById('geographicalChart');
    if (!ctx) return;

    // destroy previous chart
    if (geographicalChart) {
        geographicalChart.destroy();
    }

    // Get user preferences
    const countLimit = parseInt(document.getElementById('geoCountLimit')?.value || 10);
    
    // Create array of objects for sorting
    let dataArray = data.locations.map((location, idx) => ({
        location: location,
        jobListings: data.jobListings[idx],
        seekers: data.seekers[idx]
    }));
    
    // Sort by selected criterion (descending)
    const sortKey = geoOrderBy === 'seekers' ? 'seekers' : 'jobListings';
    dataArray.sort((a, b) => b[sortKey] - a[sortKey]);
    
    // Limit to top N countries
    dataArray = dataArray.slice(0, countLimit);
    
    // Extract sorted data back into arrays
    const sortedLocations = dataArray.map(d => d.location);
    const sortedJobListings = dataArray.map(d => d.jobListings);
    const sortedSeekers = dataArray.map(d => d.seekers);

    geographicalChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: sortedLocations,
            datasets: [
                {
                    label: 'Job Listings',
                    data: sortedJobListings,
                    backgroundColor: '#007BFF',
                    borderColor: '#0056B3',
                    borderWidth: 1
                },
                {
                    label: 'Job Seekers',
                    data: sortedSeekers,
                    backgroundColor: '#28A745',
                    borderColor: '#1E7E34',
                    borderWidth: 1
                }
            ]
        },
        options: {
            indexAxis: 'y',
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false // Use custom HTML legend
                }
            },
            scales: {
                x: {
                    beginAtZero: true
                }
            }
        }
    });

    // Render custom legend
    createCustomLegend(geographicalChart, 'geoLegendContainer');
}

function updateGeographicalChart() {
    if (window.lastGeographicalData) {
        renderGeographicalChart(window.lastGeographicalData);
    } else {
        loadChartData('geographical_popularity');
    }
}

/**
 * Sets up a single, delegated event listener for all chart download buttons.
 */
function setupDownloadButtons() {
    document.body.addEventListener('click', function(e) {
        const downloadBtn = e.target.closest('.btn-download-chart');
        if (!downloadBtn) return;

        e.preventDefault();

        const chartId = downloadBtn.dataset.chartId;
        const chartName = downloadBtn.dataset.chartName || 'chart';
        const canvas = document.getElementById(chartId);

        if (!canvas) {
            console.error(`Canvas with id "${chartId}" not found.`);
            return;
        }

        const chartInstance = Chart.getChart(canvas);

        if (!chartInstance) {
            console.error(`Chart instance for canvas "${chartId}" not found.`);
            showError(`Could not save chart "${chartName}". Instance not found.`);
            return;
        }

        try {
            const image = chartInstance.toBase64Image('image/png', 1);
            const link = document.createElement('a');
            link.href = image;
            link.download = `${chartName}-${new Date().toISOString().slice(0, 10)}.png`;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        } catch (err) {
            console.error('Failed to save chart image:', err);
            showError('An error occurred while saving the chart image.');
        }
    });
}

/**
 * Utility functions
 */

function showError(message) {
    const alert = document.createElement('div');
    alert.className = 'alert alert-danger alert-dismissible fade show';
    alert.innerHTML = `
        ${sanitizeHtml(message)}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    const mainContent = document.querySelector('main');
    if (mainContent) {
        mainContent.insertBefore(alert, mainContent.firstChild);
    }
}

function sanitizeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

/**
 * Creates a custom HTML legend for a chart.
 * @param {Chart} chart - The Chart.js instance.
 * @param {string} containerId - The ID of the container element for the legend.
 */
function createCustomLegend(chart, containerId) {
    const legendContainer = document.getElementById(containerId);
    if (!legendContainer) return;

    legendContainer.innerHTML = ''; // Clear old legend
    legendContainer.classList.add('custom-legend-container');

    chart.data.datasets.forEach((dataset, i) => {
        const legendItem = document.createElement('div');
        legendItem.className = 'custom-legend-item';

        const colorBox = document.createElement('span');
        colorBox.className = 'custom-legend-color';
        colorBox.style.backgroundColor = dataset.borderColor;

        const label = document.createElement('span');
        label.textContent = dataset.label;

        legendItem.append(colorBox, label);

        if (!chart.isDatasetVisible(i)) legendItem.classList.add('disabled');

        legendItem.onclick = () => {
            chart.setDatasetVisibility(i, !chart.isDatasetVisible(i));
            legendItem.classList.toggle('disabled');
            chart.update();
        };

        legendContainer.appendChild(legendItem);
    });
}
