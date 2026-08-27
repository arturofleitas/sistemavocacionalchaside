/**
 * reportes.js
 * Lógica de filtros para la página de reportes.
 * Filtra por nombre del alumno (solo admin) y rango de fechas (ambos roles)
 * en la tabla unificada #tablaReportes.
 *
 * NOTA: antes este script asumía que #filtroNombre siempre existía en el DOM,
 * lo cual solo es cierto para el rol ADMIN. Ahora se valida su existencia
 * antes de leer su valor, para que también funcione sin errores en la vista
 * de ESTUDIANTE (donde ese input no se renderiza).
 */

(function() {
    'use strict';

    document.addEventListener('DOMContentLoaded', function() {
        // La tabla ahora es única para ambos roles.
        const tabla = document.getElementById('tablaReportes');
        if (!tabla) return; // Si no existe ninguna tabla en la página, no hacemos nada.

        // Elementos del filtro (filtroNombre puede no existir para el rol ESTUDIANTE).
        const filtroNombre = document.getElementById('filtroNombre');
        const filtroFechaDesde = document.getElementById('filtroFechaDesde');
        const filtroFechaHasta = document.getElementById('filtroFechaHasta');
        const limpiarBtn = document.getElementById('limpiarFiltros');

        const tbody = document.getElementById('tablaReportesBody');
        if (!tbody) return;

        // Filas de datos reales, excluyendo la fila de "sin datos".
        let allRows = Array.from(tbody.querySelectorAll('tr')).filter(row => {
            const celdas = row.querySelectorAll('td');
            const esFilaVacia = celdas.length === 1 && row.querySelector('.text-muted');
            return !esFilaVacia;
        });

        // Aplica los filtros de nombre y rango de fechas, mostrando/ocultando filas.
        function aplicarFiltros() {
            // Si el input de nombre no existe (rol ESTUDIANTE), simplemente no filtramos por nombre.
            const textoNombre = filtroNombre ? filtroNombre.value.toLowerCase().trim() : '';
            const fechaDesde = filtroFechaDesde ? filtroFechaDesde.value : ''; // YYYY-MM-DD
            const fechaHasta = filtroFechaHasta ? filtroFechaHasta.value : '';

            allRows.forEach(row => {
                const nombreAlumno = row.getAttribute('data-nombre')?.toLowerCase() || '';
                let fechaISO = row.getAttribute('data-fecha') || '';

                // La fecha puede venir con hora (LocalDateTime ISO); nos quedamos solo con la fecha.
                if (fechaISO.includes('T')) {
                    fechaISO = fechaISO.split('T')[0];
                }

                let visible = true;

                if (textoNombre && !nombreAlumno.includes(textoNombre)) visible = false;
                if (visible && fechaDesde && fechaISO < fechaDesde) visible = false;
                if (visible && fechaHasta && fechaISO > fechaHasta) visible = false;

                row.style.display = visible ? '' : 'none';
            });
        }

        // Asignar eventos solo a los elementos que efectivamente existen en el DOM.
        if (filtroNombre) filtroNombre.addEventListener('input', aplicarFiltros);
        if (filtroFechaDesde) filtroFechaDesde.addEventListener('change', aplicarFiltros);
        if (filtroFechaHasta) filtroFechaHasta.addEventListener('change', aplicarFiltros);
        if (limpiarBtn) {
            limpiarBtn.addEventListener('click', function() {
                if (filtroNombre) filtroNombre.value = '';
                if (filtroFechaDesde) filtroFechaDesde.value = '';
                if (filtroFechaHasta) filtroFechaHasta.value = '';
                aplicarFiltros();
            });
        }

        // Ejecutar al inicio por si hay valores por defecto.
        aplicarFiltros();

        // --- Botón "Imprimir Reporte" ---
        // Antes estaba como onclick="window.print()" en el HTML.
        // Se separa aquí para no mezclar JS dentro del HTML.
        const btnImprimir = document.getElementById('btnImprimir');
        if (btnImprimir) {
            btnImprimir.addEventListener('click', function() {
                window.print();
            });
        }

        // --- Botón flotante "Volver arriba" (solo visible en móvil) ---
        // Antes estaba como un <script> inline al final del HTML.
        const btnSubir = document.getElementById('btnSubir');
        if (btnSubir) {
            // Único contenedor con scroll de la tabla, compartido por ambos roles.
            const scrollContainer = document.getElementById('scrollableTable');

            if (!scrollContainer) {
                btnSubir.style.display = 'none';
            } else {
                const toggleBtnSubir = function() {
                    btnSubir.style.display = scrollContainer.scrollTop > 200 ? 'flex' : 'none';
                };

                scrollContainer.addEventListener('scroll', toggleBtnSubir);

                btnSubir.addEventListener('click', function() {
                    scrollContainer.scrollTo({ top: 0, behavior: 'smooth' });
                });

                // Estado inicial (por si la tabla ya viene con scroll aplicado).
                setTimeout(toggleBtnSubir, 300);
            }
        }
    });
})();