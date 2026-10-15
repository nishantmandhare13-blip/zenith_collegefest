// Zenith '26 Fest System - Student Event Portal (app.js)

document.addEventListener('DOMContentLoaded', () => {
  // DOM Elements
  const eventsGrid = document.getElementById('events-grid');
  const loadingState = document.getElementById('loading-state');
  const errorState = document.getElementById('error-state');
  const errorMessageText = document.getElementById('error-message-text');
  const emptyState = document.getElementById('empty-state');
  const retryBtn = document.getElementById('retry-btn');
  const resetFiltersBtn = document.getElementById('reset-filters-btn');
  const searchInput = document.getElementById('search-input');
  const categoryFilterBtns = document.querySelectorAll('#category-filters button');
  const eventsCountBadge = document.getElementById('events-count-badge');

  // Modal Elements
  const eventModal = document.getElementById('event-modal');
  const modalCloseBtn = document.getElementById('modal-close-btn');
  const modalCancelBtn = document.getElementById('modal-cancel-btn');
  const modalSubmitBtn = document.getElementById('modal-submit-btn');
  const btnText = document.getElementById('btn-text');
  const btnSpinner = document.getElementById('btn-spinner');
  const regForm = document.getElementById('registration-form');

  const modalIcon = document.getElementById('modal-event-icon');
  const modalCategory = document.getElementById('modal-event-category');
  const modalTitle = document.getElementById('modal-event-title');
  const modalDesc = document.getElementById('modal-event-desc');
  const modalDatetime = document.getElementById('modal-event-datetime');
  const modalVenue = document.getElementById('modal-event-venue');
  const modalSeats = document.getElementById('modal-event-seats');
  const modalFullBanner = document.getElementById('modal-full-banner');
  const modalSuccessBanner = document.getElementById('modal-success-banner');
  const modalSuccessText = document.getElementById('modal-success-text');
  const modalServerError = document.getElementById('modal-server-error');
  const modalServerErrorText = document.getElementById('modal-server-error-text');

  const regEventIdInput = document.getElementById('reg-event-id');
  const regNameInput = document.getElementById('reg-student-name');
  const regRollInput = document.getElementById('reg-roll-number');
  const regEmailInput = document.getElementById('reg-email');
  const regPhoneInput = document.getElementById('reg-phone');

  const errorName = document.getElementById('error-reg-name');
  const errorRoll = document.getElementById('error-reg-roll');
  const errorEmail = document.getElementById('error-reg-email');

  // State
  let currentCategory = 'All';
  let currentSearch = '';
  let activeEventData = null;
  let searchDebounceTimeout = null;

  // Category badge colors styling mapping
  const categoryStyles = {
    Technical: 'bg-blue-900/60 text-blue-300 border-blue-700/60',
    Cultural: 'bg-purple-900/60 text-purple-300 border-purple-700/60',
    Sports: 'bg-emerald-900/60 text-emerald-300 border-emerald-700/60',
    Gaming: 'bg-rose-900/60 text-rose-300 border-rose-700/60',
    Workshop: 'bg-amber-900/60 text-amber-300 border-amber-700/60',
  };

  /**
   * Fetch events from the server with current search & category query params.
   * Exercises the real backend API: GET /api/events?category=...&search=...
   */
  async function fetchEvents() {
    // Show loading state
    loadingState.classList.remove('hidden');
    eventsGrid.classList.add('hidden');
    errorState.classList.add('hidden');
    emptyState.classList.add('hidden');
    eventsCountBadge.textContent = 'Loading...';

    const params = new URLSearchParams();
    if (currentCategory && currentCategory !== 'All') {
      params.append('category', currentCategory);
    }
    if (currentSearch && currentSearch.trim() !== '') {
      params.append('search', currentSearch.trim());
    }

    const url = `/api/events${params.toString() ? '?' + params.toString() : ''}`;

    try {
      const response = await fetch(url);
      if (!response.ok) {
        throw new Error(`Server responded with HTTP ${response.status}: ${response.statusText}`);
      }
      const events = await response.json();
      renderEvents(events);
    } catch (err) {
      console.error('Failed to fetch events:', err);
      loadingState.classList.add('hidden');
      errorState.classList.remove('hidden');
      errorMessageText.textContent = `Could not load events (${err.message}). Make sure the backend server is running on port 8000.`;
      eventsCountBadge.textContent = 'Error';
    }
  }

  /**
   * Renders the fetched events into #events-grid.
   */
  function renderEvents(events) {
    loadingState.classList.add('hidden');

    if (!events || events.length === 0) {
      emptyState.classList.remove('hidden');
      eventsGrid.classList.add('hidden');
      eventsCountBadge.textContent = '0 events';
      return;
    }

    eventsCountBadge.textContent = `${events.length} event${events.length === 1 ? '' : 's'}`;
    eventsGrid.innerHTML = '';

    events.forEach((ev, index) => {
      const isFull = ev.available_seats <= 0;
      const catStyle = categoryStyles[ev.category] || 'bg-slate-800 text-slate-300 border-slate-700';
      const feeLabel = ev.fee > 0 ? `₹${ev.fee}` : 'Free';
      const seatPct = Math.round(((ev.capacity - ev.available_seats) / ev.capacity) * 100);

      const card = document.createElement('div');
      card.className = 'card-fade-in bg-slate-900/90 rounded-2xl border border-slate-800/90 p-5 sm:p-6 flex flex-col justify-between hover:border-purple-600/60 hover:shadow-xl hover:shadow-purple-950/20 transition-all duration-300 group';
      card.style.animationDelay = `${index * 40}ms`;

      card.innerHTML = `
        <div>
          <!-- Top Row: Icon, Category & Full Badge -->
          <div class="flex items-start justify-between gap-2 mb-3">
            <div class="flex items-center space-x-2.5">
              <span class="text-3xl p-2 rounded-xl bg-slate-950 border border-slate-800/80 shadow-sm group-hover:scale-110 transition-transform">
                ${ev.icon || '🎉'}
              </span>
              <div>
                <span class="inline-block text-[11px] font-bold uppercase tracking-wider px-2.5 py-0.5 rounded-full border ${catStyle}">
                  ${ev.category}
                </span>
              </div>
            </div>
            ${
              isFull
                ? `<span class="inline-flex items-center text-xs font-black px-2.5 py-1 rounded-full bg-rose-500/20 text-rose-400 border border-rose-500/40 tracking-wider">
                     FULL
                   </span>`
                : `<span class="inline-flex items-center text-xs font-semibold px-2.5 py-1 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">
                     ${feeLabel}
                   </span>`
            }
          </div>

          <!-- Title -->
          <h3 class="text-lg font-bold text-white group-hover:text-purple-300 transition-colors line-clamp-1 mb-1">
            ${ev.title}
          </h3>

          <!-- Date & Time & Venue -->
          <div class="space-y-1 text-xs text-slate-400 mb-3">
            <div class="flex items-center space-x-1.5">
              <span>🗓️</span>
              <span>${ev.event_date} · ${ev.event_time}</span>
            </div>
            <div class="flex items-center space-x-1.5">
              <span>📍</span>
              <span class="truncate">${ev.venue}</span>
            </div>
          </div>

          <!-- Description preview -->
          <p class="text-xs text-slate-300/80 line-clamp-2 leading-relaxed mb-4">
            ${ev.description || 'Join us for this exciting fest event!'}
          </p>
        </div>

        <!-- Card Footer: Capacity progress & View Details button -->
        <div class="pt-3 border-t border-slate-800/70">
          <div class="flex items-center justify-between text-xs mb-1.5">
            <span class="text-slate-400 font-medium">Availability</span>
            <span class="${isFull ? 'text-rose-400 font-bold' : 'text-slate-200 font-semibold'}">
              ${isFull ? '0 seats left' : `${ev.available_seats} of ${ev.capacity} left`}
            </span>
          </div>

          <!-- Mini Progress Bar -->
          <div class="w-full bg-slate-950 rounded-full h-1.5 overflow-hidden mb-4 border border-slate-800">
            <div class="h-full rounded-full transition-all duration-500 ${
              isFull ? 'bg-rose-500' : seatPct > 80 ? 'bg-amber-400' : 'bg-purple-500'
            }" style="width: ${seatPct}%"></div>
          </div>

          <button 
            type="button" 
            class="view-details-btn w-full py-2 px-3 text-xs sm:text-sm font-semibold rounded-xl transition-all duration-200 flex items-center justify-center space-x-1.5 ${
              isFull 
                ? 'bg-slate-800 text-slate-300 hover:bg-slate-700 hover:text-white' 
                : 'bg-purple-600/90 hover:bg-purple-600 text-white shadow-md shadow-purple-600/20 hover:shadow-purple-600/40 active:scale-[0.98]'
            }"
            data-event-id="${ev.id}"
          >
            <span>View Details</span>
            <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
            </svg>
          </button>
        </div>
      `;

      eventsGrid.appendChild(card);
    });

    eventsGrid.classList.remove('hidden');

    // Attach click listeners to "View Details" buttons
    document.querySelectorAll('.view-details-btn').forEach(btn => {
      btn.addEventListener('click', (e) => {
        const evId = parseInt(btn.getAttribute('data-event-id'), 10);
        openEventModal(evId);
      });
    });
  }

  /**
   * Open the Event Detail & Registration Modal for a specific event ID.
   */
  async function openEventModal(eventId) {
    // Reset modal state
    modalServerError.classList.add('hidden');
    modalSuccessBanner.classList.add('hidden');
    modalFullBanner.classList.add('hidden');
    clearValidationErrors();
    regForm.reset();

    try {
      const response = await fetch(`/api/events/${eventId}`);
      if (!response.ok) {
        throw new Error('Event not found or server error');
      }
      activeEventData = await response.json();
    } catch (err) {
      alert(`Could not fetch details for event: ${err.message}`);
      return;
    }

    const isFull = activeEventData.available_seats <= 0;
    const catStyle = categoryStyles[activeEventData.category] || 'bg-slate-800 text-slate-300 border-slate-700';

    regEventIdInput.value = activeEventData.id;
    modalIcon.textContent = activeEventData.icon || '🎉';
    modalTitle.textContent = activeEventData.title;
    modalCategory.textContent = activeEventData.category;
    modalCategory.className = `inline-block text-[11px] font-bold uppercase tracking-wider px-2.5 py-0.5 rounded-full border ${catStyle}`;
    modalDesc.textContent = activeEventData.description || 'No description provided.';
    modalDatetime.textContent = `${activeEventData.event_date} · ${activeEventData.event_time}`;
    modalVenue.textContent = activeEventData.venue;

    const feeText = activeEventData.fee > 0 ? `₹${activeEventData.fee}` : 'Free';
    if (isFull) {
      modalSeats.textContent = `0 seats left · ${feeText}`;
      modalSeats.className = 'font-bold text-rose-400';
      modalFullBanner.classList.remove('hidden');
      modalSubmitBtn.disabled = true;
      modalSubmitBtn.classList.add('opacity-50', 'cursor-not-allowed');
    } else {
      modalSeats.textContent = `${activeEventData.available_seats} of ${activeEventData.capacity} left · ${feeText}`;
      modalSeats.className = 'font-bold text-emerald-400';
      modalFullBanner.classList.add('hidden');
      modalSubmitBtn.disabled = false;
      modalSubmitBtn.classList.remove('opacity-50', 'cursor-not-allowed');
    }

    eventModal.classList.remove('hidden');
    document.body.style.overflow = 'hidden';
  }

  function closeModal() {
    eventModal.classList.add('hidden');
    document.body.style.overflow = '';
  }

  function clearValidationErrors() {
    errorName.classList.add('hidden');
    errorRoll.classList.add('hidden');
    errorEmail.classList.add('hidden');
    modalServerError.classList.add('hidden');
  }

  /**
   * Handle registration form submission.
   * Step 7: Client-side validation + server POST + 400/409 handling
   */
  regForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    clearValidationErrors();

    const eventId = parseInt(regEventIdInput.value, 10);
    const studentName = regNameInput.value.trim();
    const rollNumber = regRollInput.value.trim();
    const email = regEmailInput.value.trim();
    const phone = regPhoneInput.value.trim();

    // Client-side validation checks
    let hasClientError = false;

    if (!studentName) {
      errorName.classList.remove('hidden');
      hasClientError = true;
    }

    if (!rollNumber) {
      errorRoll.classList.remove('hidden');
      hasClientError = true;
    }

    if (!email || !email.includes('@') || email.startsWith('@') || email.endsWith('@')) {
      errorEmail.classList.remove('hidden');
      hasClientError = true;
    }

    if (hasClientError) {
      return;
    }

    // Submit payload to server
    btnText.textContent = 'Registering...';
    btnSpinner.classList.remove('hidden');
    modalSubmitBtn.disabled = true;

    try {
      const response = await fetch(`/api/events/${eventId}/register`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          student_name: studentName,
          roll_number: rollNumber,
          email: email,
          phone: phone || null,
        }),
      });

      const responseData = await response.json();

      if (response.status === 201) {
        // Registration success!
        modalSuccessBanner.classList.remove('hidden');
        modalSuccessText.textContent = `Success! ${studentName} (${rollNumber}) is registered for ${activeEventData.title}. Registration ID: #${responseData.id}.`;
        regForm.reset();

        // Refresh events in background to update available seats immediately
        await fetchEvents();

        // Auto close after 2.5 seconds
        setTimeout(() => {
          closeModal();
        }, 2500);
      } else {
        // Server validation error: 400 Bad Request or 409 Conflict
        const errorDetail = responseData.detail || 'Registration failed. Please check your information.';
        modalServerError.classList.remove('hidden');
        modalServerErrorText.textContent = `${errorDetail} (HTTP ${response.status})`;
      }
    } catch (networkErr) {
      console.error('Registration network error:', networkErr);
      modalServerError.classList.remove('hidden');
      modalServerErrorText.textContent = `Network error connecting to registration API: ${networkErr.message}`;
    } finally {
      btnText.textContent = 'Confirm Registration';
      btnSpinner.classList.add('hidden');
      if (activeEventData && activeEventData.available_seats > 0) {
        modalSubmitBtn.disabled = false;
      }
    }
  });

  // Modal event listeners
  modalCloseBtn.addEventListener('click', closeModal);
  modalCancelBtn.addEventListener('click', closeModal);
  eventModal.addEventListener('click', (e) => {
    if (e.target === eventModal) closeModal();
  });

  // Category filter buttons
  categoryFilterBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      categoryFilterBtns.forEach(b => {
        b.classList.remove('bg-purple-600', 'text-white', 'shadow-md', 'shadow-purple-600/30');
        b.classList.add('bg-slate-800', 'text-slate-300', 'hover:bg-slate-700', 'hover:text-white', 'border', 'border-slate-700/60');
      });
      btn.classList.add('bg-purple-600', 'text-white', 'shadow-md', 'shadow-purple-600/30');
      btn.classList.remove('bg-slate-800', 'text-slate-300', 'hover:bg-slate-700');

      currentCategory = btn.getAttribute('data-category');
      fetchEvents();
    });
  });

  // Live search input with 300ms debounce
  searchInput.addEventListener('input', (e) => {
    clearTimeout(searchDebounceTimeout);
    searchDebounceTimeout = setTimeout(() => {
      currentSearch = e.target.value;
      fetchEvents();
    }, 300);
  });

  // Retry and Reset Buttons
  retryBtn.addEventListener('click', fetchEvents);
  resetFiltersBtn.addEventListener('click', () => {
    searchInput.value = '';
    currentSearch = '';
    currentCategory = 'All';
    categoryFilterBtns.forEach(b => {
      if (b.getAttribute('data-category') === 'All') {
        b.click();
      }
    });
  });

  // Keyboard shortcut: Escape to close modal
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && !eventModal.classList.contains('hidden')) {
      closeModal();
    }
  });

  // Initial load
  fetchEvents();
});
