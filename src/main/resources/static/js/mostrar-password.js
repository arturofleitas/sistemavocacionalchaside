/*
 * Check "Mostrar contraseña".
 * Cada campo va dentro de un contenedor .campo-password con el <input data-password>
 * y el check <input type="checkbox" data-mostrar-pass>. Solo cambia el type del campo
 * en el navegador (password <-> text); no toca el envío del formulario.
 */
(function () {
    'use strict';

    function aplicar(chk) {
        var campo = chk.closest('.campo-password');
        var input = campo ? campo.querySelector('[data-password]') : null;
        if (input) {
            input.type = chk.checked ? 'text' : 'password';
        }
    }

    function restablecer(raiz) {
        raiz.querySelectorAll('[data-mostrar-pass]').forEach(function (chk) {
            chk.checked = false;
            aplicar(chk);
        });
    }

    document.addEventListener('change', function (e) {
        if (e.target && e.target.matches && e.target.matches('[data-mostrar-pass]')) {
            aplicar(e.target);
        }
    });

    // Las ventanas (modales) siempre se abren y se cierran con la contraseña oculta.
    document.addEventListener('show.bs.modal', function (e) { restablecer(e.target); });
    document.addEventListener('hidden.bs.modal', function (e) { restablecer(e.target); });

    // Al volver con el botón "atrás" del navegador, la contraseña vuelve a estar oculta.
    window.addEventListener('pageshow', function () { restablecer(document); });
})();
