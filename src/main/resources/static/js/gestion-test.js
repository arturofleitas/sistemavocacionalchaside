// gestion-test.js - Lógica de filtros y botón flotante para la gestión de tests

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

        // Obtener todas las filas de la tabla (excepto la fila de "no hay datos")
        const allRows = Array.from(tablaBody.querySelectorAll('tr')).filter(row => {
            return !(row.querySelectorAll('td').length === 1 && row.querySelector('.text-muted'));
        });

        function aplicarFiltros() {
            const nombre = filtroNombre.value.toLowerCase().trim();
            const descripcion = filtroDescripcion.value.toLowerCase().trim();
            const estado = filtroEstado.value;

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

        // Eventos para filtros
        filtroNombre.addEventListener('input', aplicarFiltros);
        filtroDescripcion.addEventListener('input', aplicarFiltros);
        filtroEstado.addEventListener('change', aplicarFiltros);

        // Botón limpiar filtros
        limpiarBtn.addEventListener('click', function() {
            filtroNombre.value = '';
            filtroDescripcion.value = '';
            filtroEstado.value = '';
            aplicarFiltros();
        });
    }

    // Botón flotante para subir
    function initBtnSubir() {
        const btnSubir = document.getElementById('btnSubir');
        if (!btnSubir) return;

        // El contenedor con scroll es la tabla o su contenedor padre
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

        // Ejecutar una vez para verificar al cargar
        setTimeout(toggleBtnSubir, 300);
    }

    // Manejar desactivación/activación de tests
    function initDesactivar() {
        const botones = document.querySelectorAll('.btn-desactivar');
        botones.forEach(function(btn) {
            btn.addEventListener('click', function(e) {
                e.preventDefault();
                const id = this.getAttribute('data-id');
                const nombre = this.getAttribute('data-nombre');
                const estadoActual = this.getAttribute('data-estado') === 'true';
                const accion = estadoActual ? 'desactivar' : 'activar';
                const mensaje = estadoActual
                    ? `¿Estás seguro que quieres desactivar el test "${nombre}"?\nLos tests desactivados no se mostrarán en la lista de disponibles.`
                    : `¿Estás seguro que quieres activar el test "${nombre}"?`;

                if (confirm(mensaje)) {
                    // Redirigir al endpoint de desactivar/activar
                    window.location.href = '/gestion-test/desactivar/' + id;
                }
            });
        });
    }

    // Inicializar todo al cargar el DOM
    document.addEventListener('DOMContentLoaded', function() {
        initFiltros();
        initBtnSubir();
        initDesactivar();
    });

})();