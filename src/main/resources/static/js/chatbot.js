/**
 * Asados al Carbón — Asistente Virtual (Chatbot con Google Gemini)
 * Encapsulado en IIFE para evitar colisiones con el JS inline del menú.
 */
(function () {
  'use strict';

  document.addEventListener('DOMContentLoaded', initChatbot);

  function initChatbot() {
    const wrapper = document.getElementById('chatbotWrapper');
    const launcher = document.getElementById('chatbotLauncher');
    const panel = document.getElementById('chatbotPanel');
    if (!wrapper || !launcher || !panel) return;

    const messagesContainer = document.getElementById('chatbotMessages');
    const form = document.getElementById('chatbotForm');
    const input = document.getElementById('chatbotInput');
    const sendBtn = document.getElementById('chatbotSendBtn');
    const typingIndicator = document.getElementById('chatbotTyping');
    const closeBtn = document.getElementById('chatbotCloseBtn');
    const resetBtn = document.getElementById('chatbotResetBtn');
    const chips = document.querySelectorAll('.chatbot-chip');

    const urlMensaje = panel.getAttribute('data-url-mensaje') || '/api/chatbot/mensaje';
    const urlHistorial = panel.getAttribute('data-url-historial') || '/api/chatbot/historial';

    let isOpen = false;
    let isSending = false;

    // --- Abrir / Cerrar el Panel ---
    function toggleChatbot() {
      if (isOpen) {
        cerrarChatbot();
      } else {
        abrirChatbot();
      }
    }

    function abrirChatbot() {
      isOpen = true;
      panel.style.display = 'flex';
      panel.setAttribute('aria-hidden', 'false');
      launcher.setAttribute('aria-expanded', 'true');
      wrapper.classList.add('is-open');

      // Foco en el input
      setTimeout(() => {
        if (input) input.focus();
      }, 100);

      scrollBottom();
    }

    function cerrarChatbot() {
      isOpen = false;
      panel.style.display = 'none';
      panel.setAttribute('aria-hidden', 'true');
      launcher.setAttribute('aria-expanded', 'false');
      wrapper.classList.remove('is-open');

      if (launcher) launcher.focus();
    }

    launcher.addEventListener('click', toggleChatbot);
    if (closeBtn) closeBtn.addEventListener('click', cerrarChatbot);

    // Cerrar con Escape
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape' && isOpen) {
        cerrarChatbot();
      }
    });

    // --- Manejo del Textarea y Envío ---
    function actualizarEstadoBoton() {
      const texto = input.value.trim();
      sendBtn.disabled = texto.length === 0 || isSending;
    }

    input.addEventListener('input', () => {
      actualizarEstadoBoton();
      // Autoajustar altura (hasta 120px)
      input.style.height = 'auto';
      input.style.height = Math.min(input.scrollHeight, 120) + 'px';
    });

    input.addEventListener('keydown', (e) => {
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        if (!sendBtn.disabled && !isSending) {
          enviarMensaje(input.value.trim());
        }
      }
    });

    form.addEventListener('submit', (e) => {
      e.preventDefault();
      if (!sendBtn.disabled && !isSending) {
        enviarMensaje(input.value.trim());
      }
    });

    // Chips de sugerencia rápida
    chips.forEach((chip) => {
      chip.addEventListener('click', () => {
        const pregunta = chip.getAttribute('data-question') || chip.textContent;
        if (pregunta && !isSending) {
          enviarMensaje(pregunta);
        }
      });
    });

    // --- Envío de Mensajes a la API ---
    async function enviarMensaje(texto) {
      if (!texto || isSending) return;

      isSending = true;
      sendBtn.disabled = true;
      input.disabled = true;

      // 1. Renderizar mensaje del usuario (SEGURO: solo textContent)
      agregarBurbuja(texto, 'user');
      input.value = '';
      input.style.height = 'auto';
      actualizarEstadoBoton();

      // 2. Mostrar indicador de tipeo
      mostrarTipeo(true);
      scrollBottom();

      try {
        const response = await fetch(urlMensaje, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
          },
          body: JSON.stringify({ mensaje: texto })
        });

        const data = await response.json().catch(() => ({}));

        if (!response.ok) {
          const mensajeError = data.error || 'Lo sentimos, el asistente no está disponible en este momento. Puedes comunicarte por WhatsApp.';
          agregarBurbuja(mensajeError, 'bot', true);
        } else {
          const respuestaTexto = data.respuesta || 'No pude obtener una respuesta.';
          agregarBurbuja(respuestaTexto, 'bot', false);
        }
      } catch (err) {
        agregarBurbuja('Error de conexión. Por favor verifica tu internet o realiza tu pedido directamente por WhatsApp.', 'bot', true);
      } finally {
        mostrarTipeo(false);
        isSending = false;
        input.disabled = false;
        actualizarEstadoBoton();
        setTimeout(() => input.focus(), 50);
        scrollBottom();
      }
    }

    // --- Reiniciar Conversación ---
    if (resetBtn) {
      resetBtn.addEventListener('click', async () => {
        if (isSending) return;
        try {
          await fetch(urlHistorial, { method: 'DELETE' });
        } catch (e) {
          // Ignorar error al limpiar sesión
        }

        // Limpiar mensajes y dejar el saludo inicial
        messagesContainer.innerHTML = '';
        const bienvenida = document.createElement('div');
        bienvenida.className = 'chatbot-bubble chatbot-bubble-bot';
        bienvenida.textContent = '¡Conversación reiniciada! ¿En qué te puedo orientar sobre nuestra carta a la brasa? 🔥';
        messagesContainer.appendChild(bienvenida);
        scrollBottom();
      });
    }

    // --- Utilidades de Renderizado y UI ---
    function agregarBurbuja(texto, tipo, esError = false) {
      const bubble = document.createElement('div');
      bubble.className = 'chatbot-bubble chatbot-bubble-' + tipo;
      if (esError) {
        bubble.classList.add('chatbot-bubble-error');
      }
      // REGLA FUNDAMENTAL DE SEGURIDAD: textContent para evitar XSS
      bubble.textContent = texto;
      messagesContainer.appendChild(bubble);
      scrollBottom();
    }

    function mostrarTipeo(mostrar) {
      if (!typingIndicator) return;
      typingIndicator.style.display = mostrar ? 'flex' : 'none';
      if (mostrar) scrollBottom();
    }

    function scrollBottom() {
      messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }

    // --- Sincronización con el Carrito Flotante ---
    // Si la barra del carrito (#floatingCart) se hace visible, subir el botón para no taparlo
    const floatingCart = document.getElementById('floatingCart');
    if (floatingCart) {
      const observer = new MutationObserver(() => {
        const visible = floatingCart.style.display !== 'none' && floatingCart.offsetHeight > 0;
        if (visible) {
          wrapper.classList.add('has-cart-bar');
        } else {
          wrapper.classList.remove('has-cart-bar');
        }
      });
      observer.observe(floatingCart, { attributes: true, attributeFilter: ['style', 'class'] });
    }
  }
})();
