/*
 * Acordeón de "Ver carreras recomendadas" del ranking de resultado.
 * Delegación de eventos: funciona en estudiante, invitado y detalle.
 * En la impresión el CSS (@media print) fuerza todo expandido, no depende de esto.
 */
(function () {
    'use strict';

    var TEXTO_ABRIR = 'Ver carreras recomendadas';
    var TEXTO_CERRAR = 'Ocultar carreras recomendadas';

    document.addEventListener('click', function (e) {
        var boton = e.target.closest('[data-carreras-toggle]');
        if (!boton) {
            return;
        }
        var cuerpo = document.getElementById(boton.getAttribute('aria-controls'));
        if (!cuerpo) {
            return;
        }
        var abierto = cuerpo.classList.toggle('abierto');
        boton.setAttribute('aria-expanded', abierto ? 'true' : 'false');
        var texto = boton.querySelector('.ver-carreras-texto');
        if (texto) {
            texto.textContent = abierto ? TEXTO_CERRAR : TEXTO_ABRIR;
        }
    });
})();
