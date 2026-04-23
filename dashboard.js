// ============================================================
//  dashboard.js — Admin & User Dashboard Logic
//  Smart Lost & Found System
// ============================================================

const BASE = '/LostAndFoundSystem';  // context path — update if needed

// ── Section Navigation ─────────────────────────────────────
/**
 * Shows the target section and hides all others.
 * Also highlights the correct sidebar nav item.
 */
function toggleSection(sectionName) {
  // Hide all sections
  document.querySelectorAll('.content-section').forEach(s => {
    s.classList.add('hidden');
  });

  // Show target section
  const target = document.getElementById('section-' + sectionName);
  if (target) target.classList.remove('hidden');

  // Update active nav item
  document.querySelectorAll('.nav-item').forEach(item => {
    item.classList.remove('active');
    if (item.dataset.section === sectionName) {
      item.classList.add('active');
    }
  });

  // Update page title in topbar
  const titles = {
    dashboard:     'Dashboard',
    items:         'All Items',
    claims:        'Pending Claims',
    users:         'User Management',
    reports:       'Reports & Export',
    notifications: 'Notifications',
    report:        'Report an Item',
    search:        'Search Items',
    myclaims:      'My Claims',
    matches:       'Smart Matches',
  };

  const titleEl = document.getElementById('pageTitle');
  if (titleEl && titles[sectionName]) {
    titleEl.textContent = titles[sectionName];
  }
}

// Wire up sidebar nav clicks
document.querySelectorAll('.nav-item[data-section]').forEach(item => {
  item.addEventListener('click', function(e) {
    e.preventDefault();
    toggleSection(this.dataset.section);
  });
});

// ── Sidebar toggle (mobile) ────────────────────────────────
function toggleSidebar() {
  document.getElementById('sidebar').classList.toggle('open');
}

// ── Modal helpers ──────────────────────────────────────────
function openModal() {
  document.getElementById('claimModal').classList.remove('hidden');
}

function closeModal() {
  document.getElementById('claimModal').classList.add('hidden');
}

// ── Status Badge HTML ──────────────────────────────────────
function statusBadge(status) {
  const cls = {
    LOST:     'status-lost',
    FOUND:    'status-found',
    CLAIMED:  'status-claimed',
    EXPIRED:  'status-expired',
    PENDING:  'status-pending',
    APPROVED: 'status-approved',
    REJECTED: 'status-rejected',
  };
  return `<span class="status-badge ${cls[status] || ''}">
            ${status}
          </span>`;
}

// ── Format date string ─────────────────────────────────────
function formatDate(dateStr) {
  if (!dateStr) return '—';
  const d = new Date(dateStr);
  return d.toLocaleDateString('en-IN', {
    day: '2-digit', month: 'short', year: 'numeric'
  });
}

// ── Truncate long text ─────────────────────────────────────
function truncate(str, len = 40) {
  if (!str) return '—';
  return str.length > len ? str.substring(0, len) + '...' : str;
}

// ============================================================
//  ADMIN DASHBOARD
// ============================================================

/**
 * Fetches all admin dashboard data from the server
 * and populates every section of the admin UI.
 */
function loadAdminDashboard() {
  fetch(BASE + '/admin/dashboard')
    .then(res => {
      if (res.status === 401 || res.status === 403) {
        window.location.href = 'login.html';
        return;
      }
      return res.json();
    })
    .then(data => {
      if (!data) return;

      // Hide spinner
      document.getElementById('loadingSpinner').style.display = 'none';
      document.getElementById('section-dashboard').classList.remove('hidden');

      // Populate greeting
      const el = document.getElementById('adminName');
      if (el) el.textContent = data.adminName || 'Admin';

      // ── Stat Cards ──────────────────────────────────────
      setText('totalLost',         data.totalLost    || 0);
      setText('totalFound',        data.totalFound   || 0);
      setText('totalClaimed',      data.totalClaimed || 0);
      setText('totalExpired',      data.totalExpired || 0);
      setText('totalUsers',        data.totalUsers   || 0);
      setText('pendingClaimsCount',data.pendingClaims|| 0);

      // Notification + claim badge
      setText('notifCount', data.unreadCount || 0);
      setText('claimBadge', data.pendingClaims || 0);

      // ── Recent Items Table ──────────────────────────────
      renderRecentItems(data.recentItems || []);

      // ── All Items Table ─────────────────────────────────
      renderAllItemsAdmin(data.recentItems || []);

      // ── Pending Claims Table ────────────────────────────
      renderAdminClaims(data.claims || []);

      // ── Users Table ─────────────────────────────────────
      renderUsersTable(data.users || []);
    })
    .catch(err => {
      console.error('Dashboard load error:', err);
    });
}

// Render recent activity table
function renderRecentItems(items) {
  const tbody = document.getElementById('recentBody');
  if (!items.length) {
    tbody.innerHTML =
      '<tr><td colspan="7" class="empty">No items reported yet.</td></tr>';
    return;
  }

  tbody.innerHTML = items.map(item => `
    <tr>
      <td><code>${item.refCode}</code></td>
      <td>${truncate(item.title, 30)}</td>
      <td>${statusBadge(item.type)}</td>
      <td>${item.categoryName || '—'}</td>
      <td>${truncate(item.location, 25)}</td>
      <td>${statusBadge(item.status)}</td>
      <td>${item.reporterName || '—'}</td>
    </tr>
  `).join('');
}

// Render all items in the Items section
function renderAllItemsAdmin(items) {
  const tbody = document.getElementById('allItemsBody');
  if (!tbody) return;

  const count = document.getElementById('itemCount');
  if (count) count.textContent = items.length + ' items';

  if (!items.length) {
    tbody.innerHTML =
      '<tr><td colspan="8" class="empty">No items found.</td></tr>';
    return;
  }

  tbody.innerHTML = items.map(item => `
    <tr>
      <td><code>${item.refCode}</code></td>
      <td>${truncate(item.title, 30)}</td>
      <td>${statusBadge(item.type)}</td>
      <td>${item.categoryName || '—'}</td>
      <td>${truncate(item.location, 25)}</td>
      <td>${statusBadge(item.status)}</td>
      <td>${item.reporterName || '—'}</td>
      <td>${formatDate(item.createdAt)}</td>
    </tr>
  `).join('');
}

// Render pending claims with approve/reject buttons
function renderAdminClaims(claims) {
  const tbody  = document.getElementById('claimsBody');
  const pCount = document.getElementById('pendingCount');
  if (pCount) pCount.textContent = claims.length + ' pending';

  if (!claims.length) {
    tbody.innerHTML =
      '<tr><td colspan="7" class="empty">No pending claims.</td></tr>';
    return;
  }

  tbody.innerHTML = claims.map(c => `
    <tr>
      <td>#${c.claimId}</td>
      <td>${truncate(c.itemTitle, 28)}</td>
      <td><code>${c.itemRefCode}</code></td>
      <td>${c.claimedByName}</td>
      <td title="${c.proofDescription}">${truncate(c.proofDescription, 35)}</td>
      <td>${formatDate(c.claimedAt)}</td>
      <td>
        <button class="btn-approve"
                onclick="handleClaim(${c.claimId}, ${c.itemId}, 'APPROVED')">
          ✅ Approve
        </button>
        <button class="btn-reject"
                onclick="handleClaim(${c.claimId}, ${c.itemId}, 'REJECTED')">
          ❌ Reject
        </button>
      </td>
    </tr>
  `).join('');
}

// Render users management table
function renderUsersTable(users) {
  const tbody = document.getElementById('usersBody');
  const count = document.getElementById('userCount');
  if (count) count.textContent = users.length + ' users';

  if (!users.length) {
    tbody.innerHTML =
      '<tr><td colspan="8" class="empty">No users found.</td></tr>';
    return;
  }

  tbody.innerHTML = users.map(u => `
    <tr>
      <td>#${u.userId}</td>
      <td>${u.name}</td>
      <td>${u.email}</td>
      <td>${u.phone || '—'}</td>
      <td>${statusBadge(u.role)}</td>
      <td>${statusBadge(u.status)}</td>
      <td>${formatDate(u.createdAt)}</td>
      <td>
        ${u.role !== 'ADMIN' ? `
          <button class="${u.status === 'ACTIVE' ? 'btn-reject' : 'btn-approve'}"
                  onclick="toggleUser(${u.userId},
                  '${u.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'}')">
            ${u.status === 'ACTIVE' ? '🔒 Deactivate' : '✅ Activate'}
          </button>
        ` : '<span style="color:#9ca3af">—</span>'}
      </td>
    </tr>
  `).join('');
}

// ── Admin: Approve or Reject a claim ──────────────────────
function handleClaim(claimId, itemId, action) {
  if (!confirm(`Are you sure you want to ${action} this claim?`)) return;

  fetch(BASE + '/claim/action', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: `claimId=${claimId}&itemId=${itemId}&action=${action}`
  })
  .then(res => res.json())
  .then(data => {
    if (data.success) {
      alert(`Claim ${action} successfully!`);
      loadAdminDashboard();  // refresh all data
    } else {
      alert('Error: ' + (data.message || 'Action failed.'));
    }
  })
  .catch(() => alert('Network error. Please try again.'));
}

// ── Admin: Toggle user active/inactive ────────────────────
function toggleUser(userId, newStatus) {
  if (!confirm(`Are you sure you want to set user status to ${newStatus}?`))
    return;

  fetch(BASE + '/user/status', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: `userId=${userId}&status=${newStatus}`
  })
  .then(res => res.json())
  .then(data => {
    if (data.success) {
      loadAdminDashboard();
    } else {
      alert('Failed to update user status.');
    }
  });
}

// ============================================================
//  USER DASHBOARD
// ============================================================

/**
 * Fetches all user-specific data and populates
 * every section of the user dashboard.
 */
function loadUserDashboard() {
  fetch(BASE + '/user/dashboard')
    .then(res => {
      if (res.status === 401) {
        window.location.href = 'login.html';
        return;
      }
      return res.json();
    })
    .then(data => {
      if (!data) return;

      // Hide spinner, show dashboard
      document.getElementById('loadingSpinner').style.display = 'none';
      document.getElementById('section-dashboard').classList.remove('hidden');

      // Greeting
      setText('userName',    data.userName || 'User');
      setText('welcomeMsg',  'Welcome back, ' + (data.userName || 'User') + '! 👋');

      // Stat cards
      setText('myLostCount',       data.myLostCount        || 0);
      setText('myFoundCount',      data.myFoundCount       || 0);
      setText('pendingClaimsCount',data.pendingClaimsCount || 0);
      setText('matchCount',        data.matchCount         || 0);

      // Badges
      const unread = data.unreadCount || 0;
      setText('notifCount', unread);
      setText('notifBadge', unread);
      setText('matchBadge', data.matchCount || 0);

      // My items table
      renderMyItems(data.myItems || []);

      // My claims table
      renderMyClaims(data.myClaims || []);

      // Smart matches
      renderMatches(data.myMatches || []);

      // Notifications
      renderNotifications(data.notifications || []);

      // Load categories into dropdowns
      loadCategories();
    })
    .catch(err => console.error('User dashboard error:', err));
}

// Render user's own reported items
function renderMyItems(items) {
  const tbody = document.getElementById('myItemsBody');
  if (!items.length) {
    tbody.innerHTML =
      '<tr><td colspan="7" class="empty">You haven\'t reported any items yet.</td></tr>';
    return;
  }

  tbody.innerHTML = items.map(item => `
    <tr>
      <td><code>${item.refCode}</code></td>
      <td>${truncate(item.title, 30)}</td>
      <td>${statusBadge(item.type)}</td>
      <td>${item.categoryName || '—'}</td>
      <td>${truncate(item.location, 25)}</td>
      <td>${statusBadge(item.status)}</td>
      <td>${formatDate(item.createdAt)}</td>
    </tr>
  `).join('');
}

// Render user's claims
function renderMyClaims(claims) {
  const tbody = document.getElementById('myClaimsBody');
  if (!claims.length) {
    tbody.innerHTML =
      '<tr><td colspan="6" class="empty">You haven\'t submitted any claims yet.</td></tr>';
    return;
  }

  tbody.innerHTML = claims.map(c => `
    <tr>
      <td>#${c.claimId}</td>
      <td>${truncate(c.itemTitle, 28)}</td>
      <td><code>${c.itemRefCode}</code></td>
      <td>${truncate(c.proofDescription, 35)}</td>
      <td>${formatDate(c.claimedAt)}</td>
      <td>${statusBadge(c.status)}</td>
    </tr>
  `).join('');
}

// Render smart match cards
function renderMatches(matches) {
  const grid = document.getElementById('matchesList');
  if (!matches.length) {
    grid.innerHTML = '<p class="empty">No smart matches found yet.</p>';
    return;
  }

  grid.innerHTML = matches.map(m => `
    <div class="match-card match-highlight">
      <h4>✨ Possible Match Found!</h4>
      <p>📋 <strong>Lost:</strong> ${m.lostItemTitle}
         <code>${m.lostItemRefCode}</code></p>
      <p>✅ <strong>Found:</strong> ${m.foundItemTitle}
         <code>${m.foundItemRefCode}</code></p>
      <p>📍 Lost at: ${m.lostItemLocation || '—'}</p>
      <p>📍 Found at: ${m.foundItemLocation || '—'}</p>
      <p style="font-size:0.75rem;color:#9ca3af;margin-top:0.5rem">
        Matched on ${formatDate(m.matchDate)}
      </p>
    </div>
  `).join('');
}

// Render notification list
function renderNotifications(notifications) {
  const list = document.getElementById('notifList');
  if (!notifications.length) {
    list.innerHTML = '<li class="empty">No notifications yet.</li>';
    return;
  }

  list.innerHTML = notifications.map(n => `
    <li class="${!n.read ? 'unread' : ''}">
      ${!n.read ? '<span class="notif-dot"></span>' : ''}
      <div>
        <div>${n.message}</div>
        <div style="font-size:0.75rem;color:#9ca3af;margin-top:3px">
          ${formatDate(n.createdAt)}
        </div>
      </div>
    </li>
  `).join('');
}

// ── Load categories into select dropdowns ─────────────────
function loadCategories() {
  fetch(BASE + '/categories')
    .then(res => {
      console.log('[Categories] Status:', res.status);
      if (!res.ok) throw new Error('HTTP ' + res.status);
      return res.json();
    })
    .then(cats => {
      console.log('[Categories] Loaded:', cats.length, 'categories');
      const options = cats.map(c =>
        `<option value="${c.categoryId}">${c.categoryName}</option>`
      ).join('');

      const rptSel = document.getElementById('categorySelect');
      if (rptSel) {
        rptSel.innerHTML =
          '<option value="">Select category</option>' + options;
      }

      const srchSel = document.getElementById('srchCategory');
      if (srchSel) {
        srchSel.innerHTML =
          '<option value="">All Categories</option>' + options;
      }
    })
    .catch(err => {
      console.error('[Categories] Load failed:', err);
      const rptSel = document.getElementById('categorySelect');
      if (rptSel) {
        rptSel.innerHTML =
          '<option value="">⚠ Failed to load — refresh page</option>';
      }
    });
}

// ── Search Items ───────────────────────────────────────────
function searchItems() {
  const keyword  = document.getElementById('srchKeyword').value;
  const category = document.getElementById('srchCategory').value;
  const type     = document.getElementById('srchType').value;
  const location = document.getElementById('srchLocation').value;
  const dateFrom = document.getElementById('srchFrom').value;
  const dateTo   = document.getElementById('srchTo').value;

  const params = new URLSearchParams({
    keyword, categoryId: category, type,
    location, dateFrom, dateTo
  });

  fetch(BASE + '/item/search?' + params.toString())
    .then(res => res.json())
    .then(items => renderSearchResults(items))
    .catch(err => console.error('Search error:', err));
}

function renderSearchResults(items) {
  const tbody = document.getElementById('searchResults');

  if (!items.length) {
    tbody.innerHTML =
      '<tr><td colspan="8" class="empty">No items match your search.</td></tr>';
    return;
  }

  tbody.innerHTML = items.map(item => `
    <tr>
      <td><code>${item.refCode}</code></td>
      <td>${truncate(item.title, 30)}</td>
      <td>${statusBadge(item.type)}</td>
      <td>${item.categoryName || '—'}</td>
      <td>${truncate(item.location, 25)}</td>
      <td>${statusBadge(item.status)}</td>
      <td>${formatDate(item.dateOccurred)}</td>
      <td>
        ${item.status === 'FOUND' ? `
          <button class="btn-claim"
                  onclick="openClaimModal(${item.itemId})">
            Claim
          </button>
        ` : '—'}
      </td>
    </tr>
  `).join('');
}

function clearSearch() {
  ['srchKeyword','srchCategory','srchType',
   'srchLocation','srchFrom','srchTo'].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.value = '';
  });
  document.getElementById('searchResults').innerHTML =
    '<tr><td colspan="8" class="empty">Use filters above and click Search.</td></tr>';
}

// ── Claim Modal (User) ─────────────────────────────────────
function openClaimModal(itemId) {
  document.getElementById('claimItemId').value = itemId;
  document.getElementById('claimProof').value  = '';
  openModal();
}

function submitClaim() {
  const itemId = document.getElementById('claimItemId').value;
  const proof  = document.getElementById('claimProof').value.trim();

  if (!proof) {
    alert('Please provide a proof description.');
    return;
  }

  fetch(BASE + '/claim/submit', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: `itemId=${itemId}&proofDescription=${encodeURIComponent(proof)}`
  })
  .then(res => res.json())
  .then(data => {
    closeModal();
    if (data.success) {
      alert('Claim submitted successfully! Admin will review it shortly.');
      loadUserDashboard();
    } else {
      alert(data.message || 'Failed to submit claim.');
    }
  })
  .catch(() => alert('Network error. Please try again.'));
}

// ── Mark All Notifications Read ────────────────────────────
function markAllRead() {
  fetch(BASE + '/notifications/read', { method: 'POST' })
    .then(() => {
      // Reset badges
      setText('notifCount', 0);
      setText('notifBadge', 0);
      // Remove unread styling
      document.querySelectorAll('.notif-list li.unread').forEach(li => {
        li.classList.remove('unread');
      });
      document.querySelectorAll('.notif-dot').forEach(d => d.remove());
    });
}

// ── Image Preview ──────────────────────────────────────────
function previewImage(input) {
  const preview = document.getElementById('imagePreview');
  if (input.files && input.files[0]) {
    const reader = new FileReader();
    reader.onload = e => {
      preview.src = e.target.result;
      preview.classList.remove('hidden');
    };
    reader.readAsDataURL(input.files[0]);
  }
}

function clearPreview() {
  const preview = document.getElementById('imagePreview');
  if (preview) {
    preview.src = '';
    preview.classList.add('hidden');
  }
}

// ── Export CSV ─────────────────────────────────────────────
function exportCSV() {
  const from   = document.getElementById('rptFrom').value;
  const to     = document.getElementById('rptTo').value;
  const status = document.getElementById('rptStatus').value;

  const params = new URLSearchParams({ from, to, status });
  window.location.href = BASE + '/report/export?' + params.toString();
}

// ── Utility: set text content safely ──────────────────────
function setText(id, value) {
  const el = document.getElementById(id);
  if (el) el.textContent = value;
}

// ============================================================
//  REPORT FORM SUBMISSION (add to bottom of dashboard.js)
// ============================================================

// Wire up the report form to submit via fetch (AJAX)
document.addEventListener('DOMContentLoaded', function () {

  const reportForm = document.getElementById('reportForm');
  if (!reportForm) return;

  reportForm.addEventListener('submit', function (e) {
    e.preventDefault();  // prevent full page reload

    const alertBox = document.getElementById('reportAlert');
    const submitBtn = reportForm.querySelector('button[type="submit"]');

    // Show loading state
    submitBtn.textContent = 'Submitting...';
    submitBtn.disabled = true;

    // Use FormData to include the image file
    const formData = new FormData(reportForm);

    fetch(BASE + '/item/report', {
      method: 'POST',
      body: formData   // DO NOT set Content-Type header — browser sets it
    })
    .then(res => res.json())
    .then(data => {
      submitBtn.textContent = 'Submit Report';
      submitBtn.disabled = false;

      if (data.success) {
        // Show success message
        alertBox.className = 'alert success';
        alertBox.textContent = '✅ ' + data.message;
        alertBox.style.display = 'block';

        // Reset form and image preview
        reportForm.reset();
        clearPreview();

        // Refresh dashboard data after 1.5 seconds
        setTimeout(() => {
          loadUserDashboard();
          alertBox.style.display = 'none';
        }, 1500);

      } else {
        alertBox.className = 'alert error';
        alertBox.textContent = '❌ ' + (data.message || 'Submission failed.');
        alertBox.style.display = 'block';
      }
    })
    .catch(() => {
      submitBtn.textContent = 'Submit Report';
      submitBtn.disabled = false;
      alertBox.className = 'alert error';
      alertBox.textContent = '❌ Network error. Please try again.';
      alertBox.style.display = 'block';
    });
  });
});

// ============================================================
//  SESSION TIMEOUT WARNING (add to bottom of dashboard.js)
// ============================================================

(function sessionTimeoutWarning() {
  const WARNING_AT = 29 * 60 * 1000;

  let warningTimer, countdownTimer, countdownVal;

  function resetTimers() {
    clearTimeout(warningTimer);
    clearInterval(countdownTimer);

    const modal = document.getElementById('sessionModal');
    if (modal) modal.classList.add('hidden');

    warningTimer = setTimeout(() => {
      countdownVal = 60;

      // Inject modal if not present on this page
      if (!document.getElementById('sessionModal')) {
        document.body.insertAdjacentHTML('beforeend', `
          <div id="sessionModal" class="modal-overlay">
            <div class="modal-box" style="text-align:center">
              <div style="font-size:3rem;margin-bottom:1rem">⏳</div>
              <h3>Session Expiring Soon</h3>
              <p>You will be logged out in
                <strong><span id="countdown">60</span> seconds</strong>
                due to inactivity.</p>
              <div class="modal-actions"
                   style="justify-content:center;margin-top:1.5rem">
                <button class="btn-primary" onclick="extendSession()">
                  Stay Logged In
                </button>
                <a href="../logout" class="btn-outline">Logout Now</a>
              </div>
            </div>
          </div>
        `);
      } else {
        document.getElementById('sessionModal').classList.remove('hidden');
      }

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

  ['click', 'keypress', 'scroll', 'mousemove'].forEach(evt => {
    document.addEventListener(evt, resetTimers, { passive: true });
  });

  resetTimers();
})();

function extendSession() {
  fetch(BASE + '/user/dashboard')
    .then(() => {
      const m = document.getElementById('sessionModal');
      if (m) m.classList.add('hidden');
    });
}