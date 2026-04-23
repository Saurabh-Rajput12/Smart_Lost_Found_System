// ============================================================
//  auth.js — Shared JS for login and register pages
//  Smart Lost & Found System
// ============================================================

/**
 * Shows the alert box at the top of the form.
 * @param {string} message  - text to display
 * @param {string} type     - 'error' or 'success'
 */
function showAlert(message, type) {
  const box = document.getElementById('alertBox');
  if (!box) return;

  box.textContent   = message;
  box.className     = 'alert ' + type;  // applies CSS color
  box.style.display = 'block';

  // Auto-hide success messages after 5 seconds
  if (type === 'success') {
    setTimeout(() => { box.style.display = 'none'; }, 5000);
  }
}

/**
 * Shows a red error message below a specific field.
 * @param {string} elementId - ID of the <span> error element
 * @param {string} message   - error text to show
 */
function showFieldError(elementId, message) {
  const el = document.getElementById(elementId);
  if (el) el.textContent = message;
}

/**
 * Clears all field-level error messages.
 */
function clearErrors() {
  document.querySelectorAll('.field-error').forEach(el => {
    el.textContent = '';
  });
}

/**
 * Validates a basic email format.
 * @param {string} email
 * @returns {boolean}
 */
function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

/**
 * Toggles a password field between text and password type.
 * @param {string} fieldId - ID of the password input element
 */
function togglePassword(fieldId) {
  const field = document.getElementById(fieldId);
  if (!field) return;
  field.type = (field.type === 'password') ? 'text' : 'password';
}

/**
 * Updates the password strength bar on the register page.
 * Scores the password and sets bar width + color accordingly.
 * @param {string} password
 */
function updateStrength(password) {
  const fill = document.getElementById('strengthFill');
  const text = document.getElementById('strengthText');
  if (!fill || !text) return;

  let score = 0;

  if (password.length >= 6)  score++;   // minimum length
  if (password.length >= 10) score++;   // good length
  if (/[A-Z]/.test(password)) score++; // has uppercase
  if (/[0-9]/.test(password)) score++; // has number
  if (/[^A-Za-z0-9]/.test(password)) score++; // has symbol

  // Map score to visual feedback
  const levels = [
    { width: '0%',   color: '#e5e7eb', label: '' },
    { width: '25%',  color: '#ef4444', label: 'Weak' },
    { width: '50%',  color: '#f97316', label: 'Fair' },
    { width: '75%',  color: '#eab308', label: 'Good' },
    { width: '90%',  color: '#22c55e', label: 'Strong' },
    { width: '100%', color: '#16a34a', label: 'Very Strong' },
  ];

  const level = levels[Math.min(score, levels.length - 1)];
  fill.style.width      = level.width;
  fill.style.background = level.color;
  text.textContent      = level.label;
  text.style.color      = level.color;
}