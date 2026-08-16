/**
 * reportes.js
 * Lógica de filtros para la página de reportes (solo para administrador)
 * Filtra por nombre del alumno y rango de fechas en la tabla.
 */

(function() {
    'use strict';

    // Esperamos a que el DOM esté cargado
    document.addEventListener('DOMContentLoaded', function() {
        // Verificamos si la tabla de administrador existe (solo se aplica en esa vista)
        const tabla = document.getElementById('tablaReportes');
        if (!tabla) return; // si no existe (vista estudiante), no hacemos nada

        // Elementos del filtro
        const filtroNombre = document.getElementById('filtroNombre');
        const filtroFechaDesde = document.getElementById('filtroFechaDesde');
        const filtroFechaHasta = document.getElementById('filtroFechaHasta');
        const limpiarBtn = document.getElementById('limpiarFiltros');

        // Cuerpo de la tabla y todas sus filas (excluyendo la fila de "sin datos")
        const tbody = document.getElementById('tablaReportesBody');
        if (!tbody) return;

        let allRows = Array.from(tbody.querySelectorAll('tr')).filter(row => {
            // Si la fila tiene un solo td con clase text-muted, es el mensaje vacío
            return !(row.querySelectorAll('td').length === 1 && row.querySelector('.text-muted'));
        });

        // Función que aplica los filtros y oculta/muestra filas
        function aplicarFiltros() {
            const textoNombre = filtroNombre.value.toLowerCase().trim();
            const fechaDesde = filtroFechaDesde.value;   // formato YYYY-MM-DD
            const fechaHasta = filtroFechaHasta.value;

            allRows.forEach(row => {
                // Obtener atributos data-* de la fila y normalizar a minúsculas
                const nombreAlumno = row.getAttribute('data-nombre')?.toLowerCase() || '';
                let fechaISO = row.getAttribute('data-fecha') || '';
                
                // La fecha puede venir con hora (formato LocalDateTime ISO), tomamos solo la parte de fecha (antes de la T)
                if (fechaISO.includes('T')) {
                    fechaISO = fechaISO.split('T')[0];
                }

                let visible = true;

                // Validación por nombre
                if (textoNombre && !nombreAlumno.includes(textoNombre)) visible = false;
                
                // Validación por rango de fechas (comparación de cadenas YYYY-MM-DD)
                if (visible && fechaDesde && fechaISO < fechaDesde) visible = false;
                if (visible && fechaHasta && fechaISO > fechaHasta) visible = false;

                row.style.display = visible ? '' : 'none';
            });
        }

        // Asignar eventos
        if (filtroNombre) filtroNombre.addEventListener('input', aplicarFiltros);
        if (filtroFechaDesde) filtroFechaDesde.addEventListener('change', aplicarFiltros);
        if (filtroFechaHasta) filtroFechaHasta.addEventListener('change', aplicarFiltros);
        if (limpiarBtn) {
            limpiarBtn.addEventListener('click', function() {
                filtroNombre.value = '';
                filtroFechaDesde.value = '';
                filtroFechaHasta.value = '';
                aplicarFiltros();
            });
        }

        // Ejecutar al inicio por si hay valores por defecto
        aplicarFiltros();
    });
})();
