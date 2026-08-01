/**
 * validation.js — reusable client-side form validation module.
 *
 * Provides functions for validating individual fields, displaying inline
 * error messages linked via aria-describedby, and clearing previous errors.
 * Imported by all forms throughout the TalentBridge application.
 *
 * @module validation
 */

'use strict';

/**
 * Validates that an email address matches the standard RFC 5321 pattern.
 *
 * @param {string} email - The raw email string to test.
 * @returns {boolean} True if the email format is valid.
 */
function validateEmail(email) {
    // standard email pattern — covers the vast majority of real addresses
    const pattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return pattern.test(String(email).trim());
}

/**
 * Validates that a value is not empty after trimming whitespace.
 *
 * @param {string} value - The field value to test.
 * @returns {boolean} True if the value contains at least one non-whitespace character.
 */
function validateRequired(value) {
    return String(value).trim().length > 0;
}

/**
 * Validates that a string meets a minimum character length after trimming.
 *
 * @param {string} value   - The field value to test.
 * @param {number} minLen  - The minimum required length.
 * @returns {boolean} True if the trimmed length is at least minLen.
 */
function validateMinLength(value, minLen) {
    return String(value).trim().length >= minLen;
}

/**
 * Validates that a string does not exceed a maximum character length.
 *
 * @param {string} value   - The field value to test.
 * @param {number} maxLen  - The maximum permitted length.
 * @returns {boolean} True if the length is within the limit.
 */
function validateMaxLength(value, maxLen) {
    return String(value).trim().length <= maxLen;
}

/**
 * Displays an inline error message beneath a form field.
 *
 * Creates or updates a <div> with the error text and links it to the field
 * via aria-describedby for screen reader accessibility (wcag 1.3.1).
 *
 * @param {HTMLElement} field   - The input or textarea element with the error.
 * @param {string}      message - The human-readable error message to display.
 * @returns {void}
 */
function showError(field, message) {
    // mark the field as invalid for assistive technology
    field.setAttribute('aria-invalid', 'true');
    field.classList.add('is-invalid');
    field.classList.remove('is-valid');

    // find or create the error container element
    const errorId = field.id + '_error';
    let errorDiv = document.getElementById(errorId);

    if (!errorDiv) {
        errorDiv = document.createElement('div');
        errorDiv.id        = errorId;
        errorDiv.className = 'field-error invalid-feedback d-block';
        errorDiv.setAttribute('role', 'alert');
        field.insertAdjacentElement('afterend', errorDiv);
    }

    errorDiv.textContent = message;

    // link the field to its error message for screen readers
    field.setAttribute('aria-describedby', errorId);
}

/**
 * Clears all validation errors from a form or a single field.
 *
 * Removes invalid-feedback elements, aria attributes, and Bootstrap
 * validation classes.
 *
 * @param {HTMLElement} scope - The form or individual field element to clear.
 * @returns {void}
 */
function clearErrors(scope) {
    // handle both a single field and a container with multiple fields
    const fields = scope.querySelectorAll
        ? scope.querySelectorAll('input, textarea, select')
        : [scope];

    fields.forEach(function (field) {
        field.removeAttribute('aria-invalid');
        field.removeAttribute('aria-describedby');
        field.classList.remove('is-invalid', 'is-valid');

        const errorEl = document.getElementById(field.id + '_error');
        if (errorEl) {
            errorEl.remove();
        }
    });
}

/**
 * Marks a field as valid with a visual indicator.
 *
 * @param {HTMLElement} field - The input element to mark as valid.
 * @returns {void}
 */
function markValid(field) {
    field.removeAttribute('aria-invalid');
    field.classList.remove('is-invalid');
    field.classList.add('is-valid');

    const errorEl = document.getElementById(field.id + '_error');
    if (errorEl) {
        errorEl.remove();
    }
}

/**
 * Validates a complete form and returns whether it passed all checks.
 *
 * Iterates over all required fields and email fields, calling showError()
 * on failures. Returns true only if every field is valid.
 *
 * @param {HTMLFormElement} form - The form element to validate.
 * @returns {boolean} True if the form is fully valid.
 */
function validateForm(form) {
    clearErrors(form);
    let isValid = true;

    // validate all fields marked as required
    form.querySelectorAll('[required]').forEach(function (field) {
        if (!validateRequired(field.value)) {
            showError(field, 'This field is required.');
            isValid = false;
        }
    });

    // validate email fields
    form.querySelectorAll('input[type="email"]').forEach(function (field) {
        if (field.value.trim() && !validateEmail(field.value)) {
            showError(field, 'Please enter a valid email address.');
            isValid = false;
        }
    });

    // validate fields with data-minlength attribute
    form.querySelectorAll('[data-minlength]').forEach(function (field) {
        const min = parseInt(field.dataset.minlength, 10);
        if (field.value.trim() && !validateMinLength(field.value, min)) {
            showError(field, 'Must be at least ' + min + ' characters.');
            isValid = false;
        }
    });

    // validate fields with data-maxlength attribute
    form.querySelectorAll('[data-maxlength]').forEach(function (field) {
        const max = parseInt(field.dataset.maxlength, 10);
        if (!validateMaxLength(field.value, max)) {
            showError(field, 'Must not exceed ' + max + ' characters.');
            isValid = false;
        }
    });

    // validate cross-field salary rules — only when a salary min field is present
    const minInput = form.querySelector('[data-salary-min]');
    if (minInput) {
        const maxInput    = form.querySelector('#salary_max');
        const periodInput = form.querySelector('#salary_period');
        const minVal      = minInput.value.trim();
        const maxVal      = maxInput    ? maxInput.value.trim()    : '';
        const periodVal   = periodInput ? periodInput.value.trim() : '';

        if (minVal !== '') {
            // min present — must be positive, and period must be selected
            if (parseFloat(minVal) <= 0) {
                showError(minInput, 'Please enter a valid minimum salary greater than 0.');
                isValid = false;
            } else if (periodInput && periodVal === '') {
                showError(periodInput, 'Please select a salary period.');
                isValid = false;
            }
        }

        if (maxVal !== '') {
            if (parseFloat(maxVal) <= 0) {
                // max present but not a positive number
                showError(maxInput, 'Please enter a valid maximum salary greater than 0.');
                isValid = false;
            } else if (minVal === '') {
                // max provided without a min
                showError(minInput, 'Please enter a minimum salary.');
                isValid = false;
            } else if (parseFloat(maxVal) < parseFloat(minVal)) {
                // max is less than min
                showError(maxInput, 'Maximum salary must be at least the minimum salary.');
                isValid = false;
            }
        }

        // period selected with no min value
        if (periodVal !== '' && minVal === '') {
            showError(minInput, 'Please enter a minimum salary.');
            isValid = false;
        }
    }

    return isValid;
}
