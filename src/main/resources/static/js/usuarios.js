// usuarios.js - Lógica de filtros y alertas profesionales con SweetAlert2

(function() {
    'use strict';

    // Inicializa los filtros de la tabla y las acciones interactivas
    function initUsuarios() {
        const searchNombre   = document.getElementById('searchNombre');
        const searchUsername = document.getElementById('searchUsername');
        const filterRol      = document.getElementById('filterRol');
        const resetBtn       = document.getElementById('resetFilters');
        const tbody          = document.getElementById('usuariosBody');

        if (!tbody) return;

        const allRows = Array.from(tbody.querySelectorAll('tr')).filter(row => {
            return !(row.querySelectorAll('td').length === 1 && row.querySelector('.text-muted'));
        });

        // --- FILTROS ---
        function aplicarFiltros() {
            const textoNombre   = searchNombre.value.toLowerCase().trim();
            const textoUsername = searchUsername.value.toLowerCase().trim();
            const rolSeleccionado = filterRol.value;

            allRows.forEach(row => {
                const nombreCompleto = (row.getAttribute('data-nombre') || '').toLowerCase();
                const username       = (row.getAttribute('data-username') || '').toLowerCase();
                const rol            = row.getAttribute('data-rol') || '';

                let visible = true;

                if (textoNombre !== '' && !nombreCompleto.includes(textoNombre)) {
                    visible = false;
                }
                if (visible && textoUsername !== '' && !username.includes(textoUsername)) {
                    visible = false;
                }
                if (visible && rolSeleccionado !== '' && rol !== rolSeleccionado) {
                    visible = false;
                }

                row.style.display = visible ? '' : 'none';
            });
        }

        if (searchNombre) searchNombre.addEventListener('input', aplicarFiltros);
        if (searchUsername) searchUsername.addEventListener('input', aplicarFiltros);
        if (filterRol) filterRol.addEventListener('change', aplicarFiltros);

        if (resetBtn) {
            resetBtn.addEventListener('click', function() {
                searchNombre.value = '';
                searchUsername.value = '';
                filterRol.value = '';
                aplicarFiltros();
            });
        }

        // --- ALERTA INTELIGENTE PARA ACTIVAR / DESACTIVAR (SWEETALERT2) ---
        tbody.addEventListener('click', function(e) {
            const btnAlternar = e.target.closest('.btn-alternar-estado');
            if (!btnAlternar) return;

            const usuarioId = btnAlternar.getAttribute('data-id');
            const nombreUsuario = btnAlternar.getAttribute('data-nombre');
            
            // Evaluamos si el usuario está actualmente activo (true) o inactivo (false)
            const estaActivo = btnAlternar.getAttribute('data-estado') === 'true';

            // Configuramos los textos de la alerta dependiendo de la acción
            const titulo = estaActivo ? '¿Estás seguro de desactivar?' : '¿Estás seguro de activar?';
            const texto = estaActivo 
                ? `Se desactivará el acceso al usuario "${nombreUsuario}".` 
                : `Se volverá a habilitar el acceso al usuario "${nombreUsuario}".`;
            
            const icono = estaActivo ? 'warning' : 'question';
            const colorBotonConfirmar = estaActivo ? '#d33' : '#198754';
            const textoBotonConfirmar = estaActivo 
                ? '<i class="fas fa-ban me-1"></i> Sí, desactivar' 
                : '<i class="fas fa-check me-1"></i> Sí, activar';

            Swal.fire({
                title: titulo,
                text: texto,
                icon: icono,
                showCancelButton: true,
                confirmButtonColor: colorBotonConfirmar,
                cancelButtonColor: '#6c757d',
                confirmButtonText: textoBotonConfirmar,
                cancelButtonText: 'Cancelar',
                reverseButtons: true
            }).then((result) => {
                if (result.isConfirmed) {
                    // Redirige a la ruta del backend encargada de alternar el estado
                    window.location.href = `/usuarios/alternar-estado/${usuarioId}`;
                }
            });
        });
    }

    document.addEventListener('DOMContentLoaded', initUsuarios);
})();