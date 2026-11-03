(() => {
  const sidebar = document.getElementById('sidebar');
  const backdrop = document.getElementById('sidebarBackdrop');
  const menuToggle = document.getElementById('menuToggle');

  function closeSidebar() {
    sidebar?.classList.remove('open');
    backdrop?.classList.remove('open');
  }

  function openSidebar() {
    sidebar?.classList.add('open');
    backdrop?.classList.add('open');
  }

  menuToggle?.addEventListener('click', openSidebar);
  backdrop?.addEventListener('click', closeSidebar);

  document.querySelectorAll('[data-toggle-password]').forEach((button) => {
    button.addEventListener('click', () => {
      const input = document.querySelector(button.getAttribute('data-toggle-password'));
      if (!input) return;
      const showing = input.getAttribute('type') === 'text';
      input.setAttribute('type', showing ? 'password' : 'text');
      button.setAttribute('aria-label', showing ? 'Show password' : 'Hide password');
    });
  });

  const modal = document.getElementById('reasonModal');
  const reasonForm = document.getElementById('reasonForm');
  const reasonInput = document.getElementById('reasonInput');
  const reasonTitle = document.getElementById('reasonTitle');
  const reasonConfirm = document.getElementById('reasonConfirm');
  const statusPreserve = document.getElementById('reasonStatus');

  function closeModal() {
    modal?.classList.remove('open');
    if (reasonInput) reasonInput.value = '';
  }

  document.querySelectorAll('[data-reason-action]').forEach((button) => {
    button.addEventListener('click', () => {
      if (!reasonForm || !modal) return;
      const action = button.getAttribute('data-reason-action');
      const shopId = button.getAttribute('data-shop-id');
      const status = button.getAttribute('data-status') || '';
      const label = action === 'reject' ? 'Reject' : 'Suspend';
      const title =
        action === 'reject' ? 'Reject shop registration' : 'Suspend shop';

      reasonForm.action = `/admin/shops/${shopId}/${action}`;
      reasonTitle.textContent = title;
      reasonConfirm.textContent = label;
      reasonConfirm.className = 'btn btn-danger-solid';
      if (statusPreserve) {
        if (status) {
          statusPreserve.value = status;
          statusPreserve.disabled = false;
        } else {
          statusPreserve.value = '';
          statusPreserve.disabled = true;
        }
      }
      modal.classList.add('open');
      reasonInput?.focus();
    });
  });

  document.querySelectorAll('[data-close-modal]').forEach((el) => {
    el.addEventListener('click', closeModal);
  });

  modal?.addEventListener('click', (event) => {
    if (event.target === modal) closeModal();
  });

  const toast = document.getElementById('toast');
  if (toast) {
    setTimeout(() => toast.remove(), 3000);
  }
})();
