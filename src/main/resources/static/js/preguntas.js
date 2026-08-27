// Módulo de interacción para la vista de preguntas
(function() {
    'use strict';

    // Inicializa la búsqueda y filtrado dinámico de la tabla
    function initFiltros() {
        const searchText = document.getElementById('searchText');
        const filterNumero = document.getElementById('filterNumero'); 
        const filterCategoria = document.getElementById('filterCategoria');
        const filterTest = document.getElementById('filterTest');
        const resetBtn = document.getElementById('resetFilters');
        const tbody = document.getElementById('preguntasBody');

        if (!tbody) return;

        // Filtra únicamente las filas que contienen datos válidos
        const allRows = Array.from(tbody.querySelectorAll('tr')).filter(row => {
            return !(row.querySelectorAll('td').length === 1 && row.querySelector('.text-muted'));
        });

        // Aplica los filtros seleccionados a las filas
        function aplicarFiltros() {
            const textoBuscar = searchText ? searchText.value.toLowerCase().trim() : '';
            const numeroBuscado = filterNumero ? parseInt(filterNumero.value) : NaN;
            const categoriaSelecc = filterCategoria ? filterCategoria.value : '';
            const testSeleccionado = filterTest ? filterTest.value : '';

            allRows.forEach(row => {
                const rowNumero = parseInt(row.getAttribute('data-numero')) || 0;
                const rowCategoria = row.getAttribute('data-categoria') || '';
                const rowTestId = row.getAttribute('data-test-id') || '';
                const preguntaTexto = row.querySelector('.pregunta-texto')?.innerText.toLowerCase() || '';

                let visible = true;

                if (textoBuscar !== '' && !preguntaTexto.includes(textoBuscar)) visible = false;
                if (visible && !isNaN(numeroBuscado) && rowNumero !== numeroBuscado) visible = false;
                if (visible && categoriaSelecc !== '' && rowCategoria !== categoriaSelecc) visible = false;
                if (visible && testSeleccionado !== '' && rowTestId !== testSeleccionado) visible = false;

                row.style.display = visible ? '' : 'none';
            });
        }

        if (searchText) searchText.addEventListener('input', aplicarFiltros);
        if (filterNumero) filterNumero.addEventListener('input', aplicarFiltros);
        if (filterCategoria) filterCategoria.addEventListener('change', aplicarFiltros);
        if (filterTest) filterTest.addEventListener('change', aplicarFiltros);

        if (resetBtn) {
            resetBtn.addEventListener('click', function() {
                if (searchText) searchText.value = '';
                if (filterNumero) filterNumero.value = '';
                if (filterCategoria) filterCategoria.value = '';
                if (filterTest) filterTest.value = '';
                aplicarFiltros();
            });
        }
    }

    // Gestiona el botón flotante para desplazarse al inicio
    function initBtnSubir() {
        const btnSubir = document.getElementById('btnSubir');
        const tablaContainer = document.querySelector('.contenedor-tabla-fisica');

        if (!btnSubir || !tablaContainer) return;

        function toggleBtnSubir() {
            if (tablaContainer.scrollTop > 200) {
                btnSubir.style.display = 'flex';
            } else {
                btnSubir.style.display = 'none';
            }
        }

        tablaContainer.addEventListener('scroll', toggleBtnSubir);

        btnSubir.addEventListener('click', function() {
            tablaContainer.scrollTo({
                top: 0,
                behavior: 'smooth'
            });
        });
    }

    // Agrega la confirmación con SweetAlert2 para eliminar preguntas
    function initEliminar() {
        const botonesEliminar = document.querySelectorAll('.btn-eliminar');

        botonesEliminar.forEach(btn => {
            btn.addEventListener('click', function(e) {
                e.preventDefault();
                const id = this.getAttribute('data-id');
                const numero = this.getAttribute('data-numero');

                const titulo = '¿Eliminar Pregunta?';
                const texto = `¿Estás seguro que deseas eliminar la pregunta N° ${numero}? Esta acción no se puede deshacer.`;

                if (typeof Swal !== 'undefined') {
                    Swal.fire({
                        title: titulo,
                        text: texto,
                        icon: 'warning',
                        showCancelButton: true,
                        confirmButtonColor: '#dc3545',
                        cancelButtonColor: '#6c757d',
                        confirmButtonText: 'Sí, eliminar',
                        cancelButtonText: 'Cancelar'
                    }).then((result) => {
                        if (result.isConfirmed) {
                            window.location.href = '/preguntas/eliminar/' + id;
                        }
                    });
                } else {
                    if (confirm(texto)) {
                        window.location.href = '/preguntas/eliminar/' + id;
                    }
                }
            });
        });
    }

    // Inicializa la lógica tras cargar el DOM
    document.addEventListener('DOMContentLoaded', function() {
        initFiltros();
        initBtnSubir();
        initEliminar();
    });

})();