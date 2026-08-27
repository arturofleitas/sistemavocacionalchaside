// gestion-test.js - Lógica de filtros, botón flotante y confirmación de estado

(function() {
    'use strict';

    // Funciones de filtrado
    function initFiltros() {
        const filtroNombre = document.getElementById('filtroNombre');
        const filtroDescripcion = document.getElementById('filtroDescripcion');
        const filtroEstado = document.getElementById('filtroEstado');
        const limpiarBtn = document.getElementById('limpiarFiltros');
        const tablaBody = document.getElementById('cuerpoTests');

        if (!tablaBody) return;

        const allRows = Array.from(tablaBody.querySelectorAll('tr')).filter(row => {
            return !(row.querySelectorAll('td').length === 1 && row.querySelector('.text-muted'));
        });

        function aplicarFiltros() {
            const nombre = filtroNombre ? filtroNombre.value.toLowerCase().trim() : '';
            const descripcion = filtroDescripcion ? filtroDescripcion.value.toLowerCase().trim() : '';
            const estado = filtroEstado ? filtroEstado.value : '';

            allRows.forEach(row => {
                const rowNombre = row.getAttribute('data-nombre')?.toLowerCase() || '';
                const rowDescripcion = row.getAttribute('data-descripcion')?.toLowerCase() || '';
                const rowEstado = row.getAttribute('data-estado') || '';

                let visible = true;

                if (nombre && !rowNombre.includes(nombre)) visible = false;
                if (visible && descripcion && !rowDescripcion.includes(descripcion)) visible = false;
                if (visible && estado !== '' && rowEstado !== estado) visible = false;

                row.style.display = visible ? '' : 'none';
            });
        }

        if (filtroNombre) filtroNombre.addEventListener('input', aplicarFiltros);
        if (filtroDescripcion) filtroDescripcion.addEventListener('input', aplicarFiltros);
        if (filtroEstado) filtroEstado.addEventListener('change', aplicarFiltros);

        if (limpiarBtn) {
            limpiarBtn.addEventListener('click', function() {
                if (filtroNombre) filtroNombre.value = '';
                if (filtroDescripcion) filtroDescripcion.value = '';
                if (filtroEstado) filtroEstado.value = '';
                aplicarFiltros();
            });
        }
    }

    // Botón flotante para subir
    function initBtnSubir() {
        const btnSubir = document.getElementById('btnSubir');
        if (!btnSubir) return;

        const tablaContainer = document.querySelector('.table-responsive');
        if (!tablaContainer) {
            btnSubir.style.display = 'none';
            return;
        }

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

        setTimeout(toggleBtnSubir, 300);
    }

    // Manejar desactivación/activación de tests usando SweetAlert2
    function initDesactivar() {
        const botones = document.querySelectorAll('.btn-desactivar');
        botones.forEach(function(btn) {
            btn.addEventListener('click', function(e) {
                e.preventDefault();
                const id = this.getAttribute('data-id');
                const nombre = this.getAttribute('data-nombre');
                const estadoActual = this.getAttribute('data-estado') === 'true';

                const titulo = estadoActual ? '¿Desactivar Test?' : '¿Activar Test?';
                const texto = estadoActual
                    ? `¿Estás seguro que deseas desactivar el test "${nombre}"? No estará disponible en la evaluación.`
                    : `¿Estás seguro que deseas activar el test "${nombre}"?`;
                const confirmButtonText = estadoActual ? 'Sí, desactivar' : 'Sí, activar';
                const confirmButtonColor = estadoActual ? '#dc3545' : '#198754';

                if (typeof Swal !== 'undefined') {
                    Swal.fire({
                        title: titulo,
                        text: texto,
                        icon: estadoActual ? 'warning' : 'question',
                        showCancelButton: true,
                        confirmButtonColor: confirmButtonColor,
                        cancelButtonColor: '#6c757d',
                        confirmButtonText: confirmButtonText,
                        cancelButtonText: 'Cancelar'
                    }).then((result) => {
                        if (result.isConfirmed) {
                            window.location.href = '/gestion-test/alternar-estado/' + id;
                        }
                    });
                } else {
                    if (confirm(texto)) {
                        window.location.href = '/gestion-test/alternar-estado/' + id;
                    }
                }
            });
        });
    }

    document.addEventListener('DOMContentLoaded', function() {
        initFiltros();
        initBtnSubir();
        initDesactivar();
    });

})();