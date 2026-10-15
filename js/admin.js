// Zenith '26 - Admin Operations Dashboard (admin.js)

document.addEventListener('DOMContentLoaded', () => {
  // DOM Elements - Metrics
  const adminLoading = document.getElementById('admin-loading');
  const adminError = document.getElementById('admin-error');
  const adminContent = document.getElementById('admin-content');
  const adminRetryBtn = document.getElementById('admin-retry-btn');
  const refreshDashboardBtn = document.getElementById('refresh-dashboard-btn');
  const refreshIcon = document.getElementById('refresh-icon');

  const statTotalEvents = document.getElementById('stat-total-events');
  const statTotalRegistrations = document.getElementById('stat-total-registrations');
  const statCapacitySubtext = document.getElementById('stat-capacity-subtext');
  const statUtilizationPct = document.getElementById('stat-utilization-pct');
  const statUtilizationBar = document.getElementById('stat-utilization-bar');
  const statUpcomingEvents = document.getElementById('stat-upcoming-events');
  const catDistributionContainer = document.getElementById('category-distribution-container');

  // DOM Elements - Tabs & Tables
  const tabEventsBtn = document.getElementById('tab-events-btn');
  const tabRegistrationsBtn = document.getElementById('tab-registrations-btn');
  const tabEventsHint = document.getElementById('tab-events-hint');
  const regFilterContainer = document.getElementById('reg-filter-container');
  const regEventFilter = document.getElementById('reg-event-filter');

  const viewEventsTable = document.getElementById('view-events-table');
  const viewRegistrationsTable = document.getElementById('view-registrations-table');
  const eventsTableBody = document.getElementById('events-table-body');
  const registrationsTableBody = document.getElementById('registrations-table-body');
  const registrationsEmptyState = document.getElementById('registrations-empty-state');

  // DOM Elements - Modals
  const createEventModal = document.getElementById('create-event-modal');
  const openCreateEventModalBtn = document.getElementById('open-create-event-modal');
  const closeCreateEventModalBtn = document.getElementById('close-create-event-modal');
  const cancelCreateEventBtn = document.getElementById('cancel-create-event-btn');
  const createEventForm = document.getElementById('create-event-form');
  const createEventError = document.getElementById('create-event-error');

  const deleteConfirmModal = document.getElementById('delete-confirm-modal');
  const deleteConfirmMessage = document.getElementById('delete-confirm-message');
  const deleteCancelBtn = document.getElementById('delete-cancel-btn');
  const deleteConfirmBtn = document.getElementById('delete-confirm-btn');

  // State
  let cachedEvents = [];
  let pendingDeleteRegId = null;

  // Category styling badge classes
  const categoryBadgeColors = {
    Technical: 'bg-blue-900/60 text-blue-300 border-blue-700/60',
    Cultural: 'bg-purple-900/60 text-purple-300 border-purple-700/60',
    Sports: 'bg-emerald-900/60 text-emerald-300 border-emerald-700/60',
    Gaming: 'bg-rose-900/60 text-rose-300 border-rose-700/60',
    Workshop: 'bg-amber-900/60 text-amber-300 border-amber-700/60',
  };

  /**
   * Main dashboard fetch function. Loads stats, events, and dropdown filters.
   */
  async function loadDashboardData() {
    adminLoading.classList.remove('hidden');
    adminError.classList.add('hidden');
    refreshIcon.classList.add('animate-spin');

    try {
      // 1. Fetch live fest stats (GET /api/stats)
      const statsRes = await fetch('/api/stats');
      if (!statsRes.ok) throw new Error(`Stats HTTP error ${statsRes.status}`);
      const stats = await statsRes.json();
      renderStats(stats);

      // 2. Fetch live events list (GET /api/events)
      const eventsRes = await fetch('/api/events');
      if (!eventsRes.ok) throw new Error(`Events HTTP error ${eventsRes.status}`);
      cachedEvents = await eventsRes.json();
      renderEventsTable(cachedEvents);
      populateEventFilterDropdown(cachedEvents);

      // 3. Fetch registrations if currently on the registrations tab
      if (!viewRegistrationsTable.classList.contains('hidden')) {
        await loadRegistrations();
      }

      adminLoading.classList.add('hidden');
      adminContent.classList.remove('hidden');
    } catch (err) {
      console.error('Error fetching dashboard metrics:', err);
      adminLoading.classList.add('hidden');
      adminError.classList.remove('hidden');
    } finally {
      refreshIcon.classList.remove('animate-spin');
    }
  }

  /**
   * Renders stats cards and category distribution.
   */
  function renderStats(stats) {
    statTotalEvents.textContent = stats.total_events;
    statTotalRegistrations.textContent = stats.total_registrations;
    statCapacitySubtext.textContent = `Out of ${stats.total_capacity} total capacity`;
    statUtilizationPct.textContent = `${stats.overall_utilization_pct}%`;
    statUtilizationBar.style.width = `${Math.min(100, stats.overall_utilization_pct)}%`;
    statUpcomingEvents.textContent = stats.upcoming_events_count;

    // Category distribution badges
    catDistributionContainer.innerHTML = '';
    const cats = stats.events_by_category || {};
    const catKeys = Object.keys(cats);

    if (catKeys.length === 0) {
      catDistributionContainer.innerHTML = '<p class="text-xs text-slate-500">No categories found.</p>';
      return;
    }

    catKeys.forEach(cat => {
      const count = cats[cat];
      const badgeStyle = categoryBadgeColors[cat] || 'bg-slate-800 text-slate-300 border-slate-700';
      const div = document.createElement('div');
      div.className = 'flex items-center justify-between p-3 rounded-xl bg-slate-950/70 border border-slate-800/80';
      div.innerHTML = `
        <span class="inline-block text-[11px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full border ${badgeStyle}">
          ${cat}
        </span>
        <span class="text-sm font-black text-white">${count}</span>
      `;
      catDistributionContainer.appendChild(div);
    });
  }

  /**
   * Renders the Events Table sorted by Utilization % descending.
   * Highlights events with > 90% utilization in warning colors!
   */
  function renderEventsTable(events) {
    eventsTableBody.innerHTML = '';

    if (!events || events.length === 0) {
      eventsTableBody.innerHTML = `
        <tr>
          <td colspan="6" class="py-8 text-center text-slate-400 text-xs">
            No events found in database.
          </td>
        </tr>
      `;
      return;
    }

    // Compute utilization per event and sort descending
    const enrichedEvents = events.map(ev => {
      const registeredCount = ev.capacity - ev.available_seats;
      const utilPct = ev.capacity > 0 ? Math.round((registeredCount / ev.capacity) * 100) : 0;
      return { ...ev, registeredCount, utilPct };
    });

    enrichedEvents.sort((a, b) => b.utilPct - a.utilPct);

    enrichedEvents.forEach(ev => {
      const tr = document.createElement('tr');
      const isOver90 = ev.utilPct >= 90;
      const isFull = ev.available_seats <= 0;
      const catStyle = categoryBadgeColors[ev.category] || 'bg-slate-800 text-slate-300 border-slate-700';

      // Highlight row if over 90% capacity
      if (isFull) {
        tr.className = 'bg-rose-950/20 hover:bg-rose-950/30 transition-colors border-l-4 border-l-rose-500';
      } else if (isOver90) {
        tr.className = 'bg-amber-950/20 hover:bg-amber-950/30 transition-colors border-l-4 border-l-amber-500';
      } else {
        tr.className = 'hover:bg-slate-800/40 transition-colors';
      }

      tr.innerHTML = `
        <td class="py-3.5 px-4 font-semibold text-white">
          <div class="flex items-center space-x-2">
            <span class="text-xl">${ev.icon || '🎉'}</span>
            <div>
              <div class="text-sm font-bold text-white">${ev.title}</div>
              <div class="text-[11px] text-slate-400">ID: #${ev.id} · Fee: ${ev.fee > 0 ? '₹' + ev.fee : 'Free'}</div>
            </div>
          </div>
        </td>
        <td class="py-3.5 px-3">
          <span class="inline-block text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full border ${catStyle}">
            ${ev.category}
          </span>
        </td>
        <td class="py-3.5 px-3 text-slate-300">
          <div class="font-medium">${ev.event_date} · ${ev.event_time}</div>
          <div class="text-[11px] text-slate-400">${ev.venue}</div>
        </td>
        <td class="py-3.5 px-3 text-center">
          <span class="font-mono font-bold text-white">${ev.registeredCount}</span>
          <span class="text-slate-500 font-mono"> / ${ev.capacity}</span>
        </td>
        <td class="py-3.5 px-3 text-center">
          <div class="inline-flex items-center space-x-1.5 font-bold font-mono ${
            isOver90 ? 'text-amber-400' : 'text-slate-200'
          }">
            <span>${ev.utilPct}%</span>
            ${isOver90 ? '<span title="Critical Capacity!">⚠️</span>' : ''}
          </div>
          <div class="w-20 mx-auto bg-slate-950 rounded-full h-1 overflow-hidden mt-1 border border-slate-800">
            <div class="h-full ${isOver90 ? 'bg-amber-500' : 'bg-purple-500'}" style="width: ${Math.min(100, ev.utilPct)}%"></div>
          </div>
        </td>
        <td class="py-3.5 px-4 text-center">
          ${
            isFull
              ? `<span class="inline-block text-[10px] font-black uppercase px-2.5 py-0.5 rounded-full bg-rose-500/20 text-rose-400 border border-rose-500/50">FULL (100%)</span>`
              : isOver90
              ? `<span class="inline-block text-[10px] font-bold uppercase px-2.5 py-0.5 rounded-full bg-amber-500/20 text-amber-300 border border-amber-500/50">Filling Fast</span>`
              : `<span class="inline-block text-[10px] font-semibold uppercase px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">${ev.available_seats} Open</span>`
          }
        </td>
      `;

      eventsTableBody.appendChild(tr);
    });
  }

  /**
   * Populate the event filter dropdown for registrations.
   */
  function populateEventFilterDropdown(events) {
    const selectedVal = regEventFilter.value;
    regEventFilter.innerHTML = '<option value="">All Events</option>';
    events.forEach(ev => {
      const opt = document.createElement('option');
      opt.value = ev.id;
      opt.textContent = `[#${ev.id}] ${ev.title}`;
      regEventFilter.appendChild(opt);
    });
    if (selectedVal) {
      regEventFilter.value = selectedVal;
    }
  }

  /**
   * Loads registrations from GET /api/registrations (optionally filtered by ?event_id=).
   */
  async function loadRegistrations() {
    registrationsTableBody.innerHTML = `
      <tr>
        <td colspan="7" class="py-6 text-center text-slate-400 text-xs">
          Loading student registrations...
        </td>
      </tr>
    `;
    registrationsEmptyState.classList.add('hidden');

    const selectedEventId = regEventFilter.value;
    const url = selectedEventId ? `/api/registrations?event_id=${selectedEventId}` : '/api/registrations';

    try {
      const res = await fetch(url);
      if (!res.ok) throw new Error(`Registrations error HTTP ${res.status}`);
      const regList = await res.json();
      renderRegistrationsTable(regList);
    } catch (err) {
      console.error('Failed to load registrations:', err);
      registrationsTableBody.innerHTML = `
        <tr>
          <td colspan="7" class="py-6 text-center text-rose-400 text-xs">
            Failed to load registrations: ${err.message}
          </td>
        </tr>
      `;
    }
  }

  /**
   * Render registrations table with delete cancellation capability (Stretch Goal 2).
   */
  function renderRegistrationsTable(regList) {
    registrationsTableBody.innerHTML = '';

    if (!regList || regList.length === 0) {
      registrationsEmptyState.classList.remove('hidden');
      return;
    }

    registrationsEmptyState.classList.add('hidden');

    regList.forEach(reg => {
      const tr = document.createElement('tr');
      tr.className = 'hover:bg-slate-800/40 transition-colors';
      tr.innerHTML = `
        <td class="py-3 px-4 font-mono text-purple-400 font-bold text-xs">
          #${reg.id}
        </td>
        <td class="py-3 px-3 text-white font-semibold">
          ${reg.student_name}
        </td>
        <td class="py-3 px-3 text-slate-300 font-mono text-xs">
          ${reg.roll_number}
        </td>
        <td class="py-3 px-3 text-slate-300 text-xs">
          <div>${reg.email}</div>
          ${reg.phone ? `<div class="text-[11px] text-slate-400">${reg.phone}</div>` : ''}
        </td>
        <td class="py-3 px-3 text-white text-xs">
          <span class="font-semibold text-purple-300">${reg.event_title}</span>
          <span class="text-slate-500 font-mono text-[10px]">(#${reg.event_id})</span>
        </td>
        <td class="py-3 px-3 text-slate-400 text-xs font-mono">
          ${reg.registered_at}
        </td>
        <td class="py-3 px-4 text-right">
          <button 
            type="button" 
            class="cancel-reg-btn text-rose-400 hover:text-rose-300 hover:bg-rose-950/60 border border-rose-900/60 px-2.5 py-1 rounded-lg text-xs font-semibold transition-all"
            data-reg-id="${reg.id}"
            data-reg-name="${reg.student_name}"
            data-reg-event="${reg.event_title}"
          >
            Cancel
          </button>
        </td>
      `;
      registrationsTableBody.appendChild(tr);
    });

    // Attach listeners to Cancel buttons
    document.querySelectorAll('.cancel-reg-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        const regId = btn.getAttribute('data-reg-id');
        const regName = btn.getAttribute('data-reg-name');
        const regEvent = btn.getAttribute('data-reg-event');
        openDeleteModal(regId, regName, regEvent);
      });
    });
  }

  /**
   * Delete Modal Management (Stretch Goal 2)
   */
  function openDeleteModal(regId, regName, regEvent) {
    pendingDeleteRegId = regId;
    deleteConfirmMessage.textContent = `Are you sure you want to cancel ${regName}'s registration for "${regEvent}"?`;
    deleteConfirmModal.classList.remove('hidden');
  }

  function closeDeleteModal() {
    deleteConfirmModal.classList.add('hidden');
    pendingDeleteRegId = null;
  }

  deleteCancelBtn.addEventListener('click', closeDeleteModal);

  deleteConfirmBtn.addEventListener('click', async () => {
    if (!pendingDeleteRegId) return;

    deleteConfirmBtn.disabled = true;
    deleteConfirmBtn.textContent = 'Cancelling...';

    try {
      const res = await fetch(`/api/registrations/${pendingDeleteRegId}`, {
        method: 'DELETE',
      });
      if (!res.ok) {
        const errData = await res.json();
        throw new Error(errData.detail || `Error HTTP ${res.status}`);
      }

      closeDeleteModal();
      // Reload entire dashboard so seat counts & stats update live
      await loadDashboardData();
    } catch (err) {
      alert(`Could not cancel registration: ${err.message}`);
    } finally {
      deleteConfirmBtn.disabled = false;
      deleteConfirmBtn.textContent = 'Yes, Cancel';
    }
  });

  /**
   * Stretch Goal 1: Create New Event Form Handling
   */
  openCreateEventModalBtn.addEventListener('click', () => {
    createEventError.classList.add('hidden');
    createEventForm.reset();
    document.getElementById('new-icon').value = '🎉';
    document.getElementById('new-date').value = '2026-11-14';
    document.getElementById('new-time').value = '10:00 AM';
    document.getElementById('new-capacity').value = '50';
    document.getElementById('new-fee').value = '0';
    createEventModal.classList.remove('hidden');
  });

  function closeCreateModal() {
    createEventModal.classList.add('hidden');
  }

  closeCreateEventModalBtn.addEventListener('click', closeCreateModal);
  cancelCreateEventBtn.addEventListener('click', closeCreateModal);

  createEventForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    createEventError.classList.add('hidden');

    const title = document.getElementById('new-title').value.trim();
    const category = document.getElementById('new-category').value;
    const icon = document.getElementById('new-icon').value.trim() || '🎉';
    const description = document.getElementById('new-desc').value.trim();
    const eventDate = document.getElementById('new-date').value.trim();
    const eventTime = document.getElementById('new-time').value.trim();
    const venue = document.getElementById('new-venue').value.trim();
    const capacity = parseInt(document.getElementById('new-capacity').value, 10);
    const fee = parseInt(document.getElementById('new-fee').value, 10) || 0;

    if (!title || !category || !eventDate || !eventTime || !venue) {
      createEventError.textContent = 'Please fill in all required fields.';
      createEventError.classList.remove('hidden');
      return;
    }

    try {
      const res = await fetch('/api/events', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          title,
          category,
          icon,
          description: description || null,
          event_date: eventDate,
          event_time: eventTime,
          venue,
          capacity,
          fee,
        }),
      });

      if (!res.ok) {
        const err = await res.json();
        throw new Error(err.detail || 'Failed to create event');
      }

      closeCreateModal();
      await loadDashboardData();
    } catch (err) {
      createEventError.textContent = err.message;
      createEventError.classList.remove('hidden');
    }
  });

  /**
   * Tabs Switching Logic
   */
  tabEventsBtn.addEventListener('click', () => {
    tabEventsBtn.classList.add('bg-purple-600', 'text-white', 'shadow-md', 'shadow-purple-600/30');
    tabEventsBtn.classList.remove('bg-slate-900', 'text-slate-300');

    tabRegistrationsBtn.classList.remove('bg-purple-600', 'text-white', 'shadow-md', 'shadow-purple-600/30');
    tabRegistrationsBtn.classList.add('bg-slate-900', 'text-slate-300');

    viewEventsTable.classList.remove('hidden');
    tabEventsHint.classList.remove('hidden');
    viewRegistrationsTable.classList.add('hidden');
    regFilterContainer.classList.add('hidden');
    regFilterContainer.classList.remove('flex');
  });

  tabRegistrationsBtn.addEventListener('click', async () => {
    tabRegistrationsBtn.classList.add('bg-purple-600', 'text-white', 'shadow-md', 'shadow-purple-600/30');
    tabRegistrationsBtn.classList.remove('bg-slate-900', 'text-slate-300');

    tabEventsBtn.classList.remove('bg-purple-600', 'text-white', 'shadow-md', 'shadow-purple-600/30');
    tabEventsBtn.classList.add('bg-slate-900', 'text-slate-300');

    viewEventsTable.classList.add('hidden');
    tabEventsHint.classList.add('hidden');
    viewRegistrationsTable.classList.remove('hidden');
    regFilterContainer.classList.remove('hidden');
    regFilterContainer.classList.add('flex');

    await loadRegistrations();
  });

  regEventFilter.addEventListener('change', () => {
    loadRegistrations();
  });

  refreshDashboardBtn.addEventListener('click', loadDashboardData);
  adminRetryBtn.addEventListener('click', loadDashboardData);

  // Initial load
  loadDashboardData();
});
