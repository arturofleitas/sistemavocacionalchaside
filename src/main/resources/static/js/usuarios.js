// usuarios.js - Lógica de filtros para la tabla de usuarios
(function() {
    'use strict';

    function initFiltros() {
        // Elementos del DOM
        const searchNombre   = document.getElementById('searchNombre');
        const searchUsername = document.getElementById('searchUsername');
        const filterRol      = document.getElementById('filterRol');
        const resetBtn       = document.getElementById('resetFilters');
        const tbody          = document.getElementById('usuariosBody');

        if (!tbody) return;

        // Obtener todas las filas de usuarios (excluyendo el mensaje de "sin datos")
        const allRows = Array.from(tbody.querySelectorAll('tr')).filter(row => {
            return !(row.querySelectorAll('td').length === 1 && row.querySelector('.text-muted'));
        });

        function aplicarFiltros() {
            const textoNombre   = searchNombre.value.toLowerCase().trim();
            const textoUsername = searchUsername.value.toLowerCase().trim();
            const rolSeleccionado = filterRol.value;

            allRows.forEach(row => {
                // Obtener datos desde los atributos data-*
                const nombreCompleto = row.getAttribute('data-nombre')?.toLowerCase() || '';
                const username       = row.getAttribute('data-username')?.toLowerCase() || '';
                const rol            = row.getAttribute('data-rol') || '';

                let visible = true;

                if (textoNombre !== '' && !nombreCompleto.includes(textoNombre)) visible = false;
                if (visible && textoUsername !== '' && !username.includes(textoUsername)) visible = false;
                if (visible && rolSeleccionado !== '' && rol !== rolSeleccionado) visible = false;

                row.style.display = visible ? '' : 'none';
            });
        }

        // Eventos
        searchNombre.addEventListener('input', aplicarFiltros);
        searchUsername.addEventListener('input', aplicarFiltros);
        filterRol.addEventListener('change', aplicarFiltros);
        resetBtn.addEventListener('click', () => {
            searchNombre.value = '';
            searchUsername.value = '';
            filterRol.value = '';
            aplicarFiltros();
        });
    }

    document.addEventListener('DOMContentLoaded', initFiltros);
})();