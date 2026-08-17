/**
 *  preguntas.js - Lógica de filtros para la tabla de preguntas
 * 
 *  Funcionalidades:
 *   - Filtrado por texto libre (busca en el texto de la pregunta).
 *   - Filtrado por número de pregunta (coincidencia exacta).
 *   - Filtrado por categoría (C, H, A, S, I, D, E).
 *   - Filtrado por test (ID del test).
 *   - Botón para limpiar todos los filtros. 
 */

(function() {
    'use strict';

    /**
     * Inicializa los filtros de la tabla de preguntas.
     * Se ejecuta automáticamente cuando el DOM esté completamente cargado.
     */
    function initFiltros() {
        //  1. OBTENER REFERENCIAS A LOS ELEMENTOS DEL DOM 
        const searchText      = document.getElementById('searchText');
        const filterNumero   = document.getElementById('filterNumero'); 
        const filterCategoria = document.getElementById('filterCategoria');
        const filterTest     = document.getElementById('filterTest');
        const resetBtn       = document.getElementById('resetFilters');
        const tbody          = document.getElementById('preguntasBody');

        // Si no existe el tbody, no hay tabla que filtrar → salir
        if (!tbody) return;

        /**
         * Obtiene todas las filas de la tabla, excluyendo la fila de "No hay datos".
         * Se filtran las filas que tienen solo una celda y contienen la clase 'text-muted',
         * que corresponde al mensaje de tabla vacía.
         */
        const allRows = Array.from(tbody.querySelectorAll('tr')).filter(row => {
            return !(row.querySelectorAll('td').length === 1 && row.querySelector('.text-muted'));
        });

        // ========== 2. FUNCIÓN PRINCIPAL DE FILTRADO ==========
        /**
         * Aplica todos los filtros activos y oculta/muestra las filas correspondientes.
         * Se ejecuta en cada cambio de los filtros.
         */
        function aplicarFiltros() {
            // Leer valores de los filtros (normalizados)
            const textoBuscar      = searchText.value.toLowerCase().trim();
            const numeroBuscado   = parseInt(filterNumero.value);  // NUEVO
            const categoriaSelecc = filterCategoria.value;
            const testSeleccionado = filterTest.value;

            // Recorrer todas las filas de la tabla
            allRows.forEach(row => {
                // Obtener datos de la fila desde los atributos data-*
                const rowNumero     = parseInt(row.getAttribute('data-numero')) || 0;     // NUEVO
                const rowCategoria  = row.getAttribute('data-categoria') || '';
                const rowTestId     = row.getAttribute('data-test-id') || '';
                const preguntaTexto = row.querySelector('.pregunta-texto')?.innerText.toLowerCase() || '';

                let visible = true;

                // ===== FILTRO POR TEXTO LIBRE =====
                if (textoBuscar !== '' && !preguntaTexto.includes(textoBuscar)) {
                    visible = false;
                }

                // ===== FILTRO POR NÚMERO DE PREGUNTA (coincidencia exacta) =====
                if (visible && !isNaN(numeroBuscado) && rowNumero !== numeroBuscado) {
                    visible = false;
                }

                // ===== FILTRO POR CATEGORÍA =====
                if (visible && categoriaSelecc !== '' && rowCategoria !== categoriaSelecc) {
                    visible = false;
                }

                // ===== FILTRO POR TEST =====
                if (visible && testSeleccionado !== '' && rowTestId !== testSeleccionado) {
                    visible = false;
                }

                // Aplicar visibilidad ('' = mostrar, 'none' = ocultar)
                row.style.display = visible ? '' : 'none';
            });
        }

        // ========== 3. REGISTRAR EVENTOS ==========
        // Cada vez que se escriba en el campo de texto
        searchText.addEventListener('input', aplicarFiltros);

        // Cada vez que se escriba en el campo de número (filtro por número)
        filterNumero.addEventListener('input', aplicarFiltros);   // NUEVO

        // Cada vez que se cambie el select de categoría
        filterCategoria.addEventListener('change', aplicarFiltros);

        // Cada vez que se cambie el select de test
        filterTest.addEventListener('change', aplicarFiltros);

        // Botón para limpiar todos los filtros
        resetBtn.addEventListener('click', function() {
            searchText.value = '';
            filterNumero.value = '';    // NUEVO
            filterCategoria.value = '';
            filterTest.value = '';
            aplicarFiltros();           // Re-aplicar filtros (ahora todos vacíos)
        });
    }

    // ========== 4. INICIALIZACIÓN ==========
    // Esperar a que el DOM esté listo antes de ejecutar la lógica de filtros
    document.addEventListener('DOMContentLoaded', initFiltros);
})();