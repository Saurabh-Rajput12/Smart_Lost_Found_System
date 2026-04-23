// ============================================================
//  profile.js — Profile page logic + session timeout warning
// ============================================================

const BASE = '/LostAndFoundSystem';

// ── Load Profile Data ──────────────────────────────────────
function loadProfile() {
  fetch(BASE + '/profile')
    .then(res => {
      if (res.status === 401) {
        window.location.href = 'login.html';
        return null;
      }
      return res.json();
    })
    .then(data => {
      if (!data) return;

      // Avatar initials from name
      const initials = data.name
        ? data.name.split(' ')
               .map(w => w[0])
               .join('')
               .substring(0, 2)
               .toUpperCase()
        : '?';

      setText('avatarInitials', initials);
      setText('profileName',   data.name  || '—');
      setText('profileEmail',  data.email || '—');
      setText('profileRole',   data.role  || '—');
      setText('topbarName',    data.name  || '—');

      if (data.createdAt) {
        const d = new Date(data.createdAt);
        setText('profileJoined', d.toLocaleDateString('en-IN', {
          month: 'long', year: 'numeric'
        }));
      }

      // Pre-fill edit form
      setVal('editName',  data.name  || '');
      setVal('editEmail', data.email || '');
      setVal('editPhone', data.phone || '');
    })
    .catch(err => console.error('Profile load error:', err));
}

// ── Form Submit ────────────────────────────────────────────
document.getElementById('profileForm').addEventListener('submit', function(e) {
  e.preventDefault();

  const alertBox = document.getElementById('profileAlert');
  const newPass  = document.getElementById('newPassword').value;
  const confirm  = document.getElementById('confirmPassword').value;

  // Confirm password match
  if (newPass && newPass !== confirm) {
    showAlert('New passwords do not match.', 'error');
    return;
  }

  const body = new URLSearchParams({
    name:        document.getElementById('editName').value.trim(),
    phone:       document.getElementById('editPhone').value.trim(),
    oldPassword: document.getElementById('oldPassword').value,
    newPassword: newPass,
  });

  const btn = this.querySelector('button[type="submit"]');
  btn.textContent = 'Saving...';
  btn.disabled    = true;

  fetch(BASE + '/profile/update', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: body.toString()
  })
  .then(res => res.json())
  .then(data => {
    btn.textContent = '💾 Save Changes';
    btn.disabled    = false;

    showAlert(data.message, data.success ? 'success' : 'error');

    if (data.success) {
      // Clear password fields
      ['oldPassword', 'newPassword', 'confirmPassword'].forEach(id => {
        setVal(id, '');
      });
      loadProfile(); // refresh displayed name
    }
  })
  .catch(() => {
    btn.textContent = '💾 Save Changes';
    btn.disabled    = false;
    showAlert('Network error. Please try again.', 'error');
  });
});

// ── Alert helper ───────────────────────────────────────────
function showAlert(message, type) {
  const box = document.getElementById('profileAlert');
  box.className     = 'alert ' + type;
  box.textContent   = (type === 'success' ? '✅ ' : '❌ ') + message;
  box.style.display = 'block';
  window.scrollTo({ top: 0, behavior: 'smooth' });
  if (type === 'success') {
    setTimeout(() => { box.style.display = 'none'; }, 4000);
  }
}

// ── Utilities ──────────────────────────────────────────────
function setText(id, val) {
  const el = document.getElementById(id);
  if (el) el.textContent = val;
}

function setVal(id, val) {
  const el = document.getElementById(id);
  if (el) el.value = val;
}

function toggleSidebar() {
  document.getElementById('sidebar').classList.toggle('open');
}

// ============================================================
//  SESSION TIMEOUT WARNING (works on ALL pages)
//  Add this block to dashboard.js too for global coverage
// ============================================================

(function sessionTimeoutWarning() {
  const WARNING_AT  = 29 * 60 * 1000; // warn at 29 min
  const SESSION_MAX = 30 * 60 * 1000; // session = 30 min

  let warningTimer, countdownTimer, countdownVal;

  function resetTimers() {
    clearTimeout(warningTimer);
    clearInterval(countdownTimer);
    hideModal();

    warningTimer = setTimeout(() => {
      countdownVal = 60;
      showModal();
      countdownTimer = setInterval(() => {
        countdownVal--;
        setText('countdown', countdownVal);
        if (countdownVal <= 0) {
          clearInterval(countdownTimer);
          window.location.href = BASE + '/logout';
        }
      }, 1000);
    }, WARNING_AT);
  }

  function showModal() {
    document.getElementById('sessionModal').classList.remove('hidden');
  }

  function hideModal() {
    const m = document.getElementById('sessionModal');
    if (m) m.classList.add('hidden');
  }

  // Reset timer on any user interaction
  ['click', 'keypress', 'scroll', 'mousemove'].forEach(evt => {
    document.addEventListener(evt, resetTimers, { passive: true });
  });

  resetTimers(); // start on page load
})();

// Extend session by pinging the server
function extendSession() {
  fetch(BASE + '/user/dashboard')
    .then(() => {
      document.getElementById('sessionModal').classList.add('hidden');
    });
}

// ── Init ───────────────────────────────────────────────────
loadProfile();