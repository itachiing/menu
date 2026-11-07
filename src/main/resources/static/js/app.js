/**
 * Marea - Casa de Cocina
 * Aplicación de gestión de restaurante
 */

document.addEventListener('DOMContentLoaded', () => {
  // 1. Alternar visualización de formulario nuevo (data-form-toggle)
  document.querySelectorAll('[data-form-toggle]').forEach((button) => {
    button.addEventListener('click', () => {
      const formId = button.dataset.formToggle;
      openForm(formId, false);
    });
  });

  // 2. Cerrar formulario por ID (data-form-close)
  document.querySelectorAll('[data-form-close]').forEach((button) => {
    button.addEventListener('click', () => {
      const formId = button.dataset.formClose;
      const form = document.getElementById(formId);
      if (form) {
        form.hidden = true;
      }
    });
  });

  // 3. Abrir y poblar formulario de edición (data-edit-form)
  document.querySelectorAll('[data-edit-form]').forEach((button) => {
    button.addEventListener('click', () => {
      const formId = button.dataset.editForm;
      const form = openForm(formId, true);
      if (!form) return;

      form.querySelectorAll('[name]').forEach((field) => {
        // Conversión kebab-case a camelCase (ej: categoria-id -> categoriaId)
        const camelCaseKey = field.name.replace(/-([a-z])/g, (_, letter) => letter.toUpperCase());
        const rawKey = field.name;

        let val = undefined;
        if (camelCaseKey in button.dataset) {
          val = button.dataset[camelCaseKey];
        } else if (rawKey in button.dataset) {
          val = button.dataset[rawKey];
        }

        if (val === undefined) return;

        if (field.type === 'checkbox') {
          field.checked = val === 'true' || val === true || val === '1';
        } else if (field.name !== 'password') {
          field.value = (val === 'null' || val === 'undefined') ? '' : val;
        }
      });
    });
  });

  // 4. Confirmación previa para formularios destructivos o de eliminación (data-confirm)
  document.querySelectorAll('form[data-confirm]').forEach((form) => {
    form.addEventListener('submit', (event) => {
      const message = form.dataset.confirm || '¿Confirmas realizar esta acción?';
      if (!window.confirm(message)) {
        event.preventDefault();
      }
    });
  });

  // 5. Alternar barra lateral en dispositivos móviles (data-menu-toggle)
  const menuToggle = document.querySelector('[data-menu-toggle]');
  const sidebar = document.querySelector('.sidebar');

  if (menuToggle && sidebar) {
    menuToggle.addEventListener('click', (event) => {
      event.stopPropagation();
      sidebar.classList.toggle('is-open');
    });
  }

  // 8. Cerrar barra lateral al hacer clic fuera de ella (en móviles)
  document.addEventListener('click', (event) => {
    if (sidebar && sidebar.classList.contains('is-open')) {
      const clickedInsideSidebar = sidebar.contains(event.target);
      const clickedMenuToggle = menuToggle && menuToggle.contains(event.target);

      if (!clickedInsideSidebar && !clickedMenuToggle) {
        sidebar.classList.remove('is-open');
      }
    }
  });

  // Soporte tecla Escape para cerrar barra lateral y formularios abiertos
  document.addEventListener('keydown', (event) => {
    if (event.key === 'Escape') {
      if (sidebar && sidebar.classList.contains('is-open')) {
        sidebar.classList.remove('is-open');
      }
    }
  });

  // 7. Auto-descartar mensajes de notificación (flash messages) tras 5 segundos con desvanecimiento
  const flashNotices = document.querySelectorAll('.flash-stack .notice, .login-form-wrap .notice');
  flashNotices.forEach((notice) => {
    setTimeout(() => {
      notice.style.transition = 'opacity 0.45s ease, transform 0.45s ease, max-height 0.45s ease, margin 0.45s ease, padding 0.45s ease';
      notice.style.overflow = 'hidden';
      notice.style.opacity = '0';
      notice.style.transform = 'translateY(-6px)';
      notice.style.maxHeight = '0';
      notice.style.paddingTop = '0';
      notice.style.paddingBottom = '0';
      notice.style.marginTop = '0';
      notice.style.marginBottom = '0';

      setTimeout(() => {
        notice.remove();
      }, 450);
    }, 5000);
  });

  /**
   * 6. Función auxiliar para inicializar y abrir formularios
   * @param {string} formId ID del elemento formulario
   * @param {boolean} editing Indica si es apertura en modo edición
   * @returns {HTMLFormElement|null}
   */
  function openForm(formId, editing) {
    const form = document.getElementById(formId);
    if (!form) return null;

    form.reset();
    form.hidden = false;

    // Actualizar encabezado del formulario ("Nuevo" vs "Editar")
    const title = form.querySelector('[data-form-title]');
    if (title) {
      if (!title.dataset.defaultTitle) {
        title.dataset.defaultTitle = title.textContent.trim();
      }
      const baseTitle = title.dataset.newTitle || title.dataset.defaultTitle;
      title.textContent = editing
        ? baseTitle.replace(/^(Nuevo|Nueva)\s+/i, 'Editar ')
        : baseTitle;
    }

    // Gestionar campo de contraseña (opcional en edición, obligatorio al crear)
    const passwordInput = form.querySelector('[data-password-input]');
    if (passwordInput) {
      passwordInput.required = !editing;
      passwordInput.value = '';

      const passwordLabel = form.querySelector('[data-password-label]');
      if (passwordLabel) {
        let textUpdated = false;
        for (const node of passwordLabel.childNodes) {
          if (node.nodeType === Node.TEXT_NODE && node.nodeValue.trim().length > 0) {
            node.nodeValue = editing ? 'Cambiar contraseña ' : 'Contraseña inicial ';
            textUpdated = true;
            break;
          }
        }
        if (!textUpdated && passwordLabel.firstChild) {
          passwordLabel.firstChild.textContent = editing ? 'Cambiar contraseña' : 'Contraseña inicial';
        }
      }
    }

    // Gestionar selectores inmutables durante la edición (ej: productoId en variantes)
    const immutableFields = form.querySelectorAll('[data-immutable-on-edit]');
    immutableFields.forEach((field) => {
      field.disabled = editing;
    });

    // Desplazamiento suave para visibilidad
    form.scrollIntoView({ behavior: 'smooth', block: 'center' });

    return form;
  }
});