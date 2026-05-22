// preguntas.js
(function() {
    'use strict';

    function initFiltros() {
        const searchText = document.getElementById('searchText');
        const filterCategoria = document.getElementById('filterCategoria');
        const filterTest = document.getElementById('filterTest');
        const resetBtn = document.getElementById('resetFilters');
        const tbody = document.getElementById('preguntasBody');
        if (!tbody) return;
        const allRows = Array.from(tbody.querySelectorAll('tr')).filter(row => {
            // Excluir la fila de "no hay preguntas" si existe
            return !(row.querySelectorAll('td').length === 1 && row.querySelector('.text-muted'));
        });

        function aplicarFiltros() {
            const textoBuscar = searchText.value.toLowerCase().trim();
            const categoriaSeleccionada = filterCategoria.value;
            const testSeleccionado = filterTest.value;

            allRows.forEach(row => {
                const rowCategoria = row.getAttribute('data-categoria') || '';
                const rowTestId = row.getAttribute('data-test-id') || '';
                const preguntaTexto = row.querySelector('.pregunta-texto')?.innerText.toLowerCase() || '';

                let visible = true;
                if (textoBuscar !== '' && !preguntaTexto.includes(textoBuscar)) visible = false;
                if (visible && categoriaSeleccionada !== '' && rowCategoria !== categoriaSeleccionada) visible = false;
                if (visible && testSeleccionado !== '' && rowTestId !== testSeleccionado) visible = false;

                row.style.display = visible ? '' : 'none';
            });
        }

        searchText.addEventListener('input', aplicarFiltros);
        filterCategoria.addEventListener('change', aplicarFiltros);
        filterTest.addEventListener('change', aplicarFiltros);
        resetBtn.addEventListener('click', () => {
            searchText.value = '';
            filterCategoria.value = '';
            filterTest.value = '';
            aplicarFiltros();
        });
    }

    document.addEventListener('DOMContentLoaded', initFiltros);
})();