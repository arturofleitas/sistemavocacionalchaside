// sidebar.js - Toggle del menú lateral (ocultar/mostrar) común a todas las páginas

(function() {
    'use strict';

    function contenedor(btn) {
        return btn.closest('.app-container') || btn.closest('.main-container');
    }

    function aplicar(collapsed) {
        document.querySelectorAll('.app-container, .main-container').forEach(function(c) {
            c.classList.toggle('sidebar-collapsed', collapsed);
        });
    }

    document.addEventListener('DOMContentLoaded', function() {
        document.querySelectorAll('.btn-toggle-sidebar').forEach(function(btn) {
            btn.addEventListener('click', function() {
                var cont = contenedor(btn);
                if (!cont) return;
                var collapsed = cont.classList.toggle('sidebar-collapsed');
                localStorage.setItem('sidebarCollapsed', collapsed);
            });
        });

        // Restaurar estado guardado
        if (localStorage.getItem('sidebarCollapsed') === 'true') {
            aplicar(true);
        }
    });

})();
