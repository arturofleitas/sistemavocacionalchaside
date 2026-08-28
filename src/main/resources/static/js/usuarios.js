// usuarios.js - Gestión de usuarios con modal, filtros y alertas (SweetAlert2)

(function() {
    'use strict';

    const PASSWORD_MIN_LENGTH = 5;

    // Utilidades del modal de usuario
    function obtenerModal() {
        const modal = document.getElementById('modalUsuario');
        const form = document.getElementById('formUsuario');
        const titulo = document.getElementById('tituloModalUsuario');
        const instancia = modal && typeof bootstrap !== 'undefined' ? bootstrap.Modal.getOrCreateInstance(modal) : null;
        return { modal, form, titulo, instancia };
    }

    function setTitulo(editando) {
        const { titulo } = obtenerModal();
        if (!titulo) return;
        titulo.innerHTML = editando
            ? '<i class="fas fa-user-edit me-2"></i>Editar Usuario'
            : '<i class="fas fa-user-plus me-2"></i>Nuevo Usuario';
    }

    function abrirModal(editando, resetear) {
        const { form, instancia } = obtenerModal();
        if (!instancia) return;

        setTitulo(editando);

        // resetear = true solo para "nuevo usuario" limpio (botón "+").
        if (resetear && !editando) {
            if (form) form.reset();
            const idInput = form ? form.querySelector('input[name="id"]') : null;
            if (idInput) idInput.value = '0';
        }
        instancia.show();
    }

    // Rellena el formulario con los datos de la fila seleccionada (edición)
    function rellenarFormulario(row) {
        const { form } = obtenerModal();
        if (!form) return;

        const set = (campo, valor) => {
            const el = form.elements[campo];
            if (el) el.value = (valor == null || valor === 'null') ? '' : valor;
        };

        set('id', row.getAttribute('data-id'));
        set('nombre', row.getAttribute('data-nombre'));
        set('apellido', row.getAttribute('data-apellido'));
        set('fecha_nacimiento', row.getAttribute('data-fecha') || '');
        set('username', row.getAttribute('data-username'));

        const rolSel = form.elements['rol'];
        if (rolSel) rolSel.value = row.getAttribute('data-rol') || 'ESTUDIANTE';

        const estadoSel = form.elements['estado'];
        if (estadoSel) estadoSel.value = row.getAttribute('data-estado') || 'true';

        // Vacío = conservar la contraseña actual en edición
        set('password', '');
    }

    // Inicializa el modal (botón "+", edición y validación de contraseña)
    function initModal() {
        const { modal, form } = obtenerModal();
        if (!modal || !form) return;

        // Al cerrar, se restablece el formulario
        modal.addEventListener('hidden.bs.modal', function() {
            form.reset();
            setTitulo(false);
        });

        // Botón "+" para nuevo usuario
        const btnNuevo = document.getElementById('btnNuevoUsuario');
        if (btnNuevo) {
            btnNuevo.addEventListener('click', function() {
                abrirModal(false, true);
            });
        }

        // Botones de editar en la tabla
        document.querySelectorAll('.btn-editar').forEach(function(btn) {
            btn.addEventListener('click', function(e) {
                e.preventDefault();
                const fila = this.closest('tr');
                if (!fila) return;
                rellenarFormulario(fila);
                abrirModal(true, false);
            });
        });

        // Validación de contraseña mínima antes de enviar
        const passwordInput = form.elements['password'];
        if (passwordInput) {
            form.addEventListener('submit', function(e) {
                const valor = passwordInput.value || '';
                if (valor.length > 0 && valor.length < PASSWORD_MIN_LENGTH) {
                    e.preventDefault();
                    if (typeof Swal !== 'undefined') {
                        Swal.fire({
                            title: 'Contraseña muy corta',
                            text: `La contraseña debe tener al menos ${PASSWORD_MIN_LENGTH} caracteres.`,
                            icon: 'warning',
                            confirmButtonColor: '#0d6efd',
                            confirmButtonText: 'Entendido'
                        });
                    }
                }
            });
        }

        // Si el servidor devolvió la vista con error (o es una edición por navegación),
        // se reabre el modal con los datos ya rellenados por Thymeleaf (sin resetear).
        if (document.body.getAttribute('data-abrir-modal') === 'true') {
            const idInput = form.querySelector('input[name="id"]');
            const editando = idInput && parseInt(idInput.value, 10) > 0;
            abrirModal(editando, false);
        }
    }

    // Inicializa los filtros de la tabla
    function initFiltros() {
        const searchNombre = document.getElementById('searchNombre');
        const searchUsername = document.getElementById('searchUsername');
        const filterRol = document.getElementById('filterRol');
        const filterEstado = document.getElementById('filterEstado');
        const resetBtn = document.getElementById('resetFilters');
        const tbody = document.getElementById('usuariosBody');

        if (!tbody) return;

        const allRows = Array.from(tbody.querySelectorAll('tr')).filter(row => {
            return !(row.querySelectorAll('td').length === 1 && row.querySelector('.text-muted'));
        });

        function aplicarFiltros() {
            const textoNombre = searchNombre ? searchNombre.value.toLowerCase().trim() : '';
            const textoUsername = searchUsername ? searchUsername.value.toLowerCase().trim() : '';
            const rolSeleccionado = filterRol ? filterRol.value : '';
            const estadoSeleccionado = filterEstado ? filterEstado.value : '';

            allRows.forEach(row => {
                const nombreCompleto = (row.getAttribute('data-nombre') || '').toLowerCase() + ' ' + (row.getAttribute('data-apellido') || '').toLowerCase();
                const username = (row.getAttribute('data-username') || '').toLowerCase();
                const rol = row.getAttribute('data-rol') || '';
                const estado = row.getAttribute('data-estado') || '';

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

                if (visible && estadoSeleccionado !== '' && estado !== estadoSeleccionado) {
                    visible = false;
                }

                row.style.display = visible ? '' : 'none';
            });
        }

        if (searchNombre) searchNombre.addEventListener('input', aplicarFiltros);
        if (searchUsername) searchUsername.addEventListener('input', aplicarFiltros);
        if (filterRol) filterRol.addEventListener('change', aplicarFiltros);
        if (filterEstado) filterEstado.addEventListener('change', aplicarFiltros);

        if (resetBtn) {
            resetBtn.addEventListener('click', function() {
                if (searchNombre) searchNombre.value = '';
                if (searchUsername) searchUsername.value = '';
                if (filterRol) filterRol.value = '';
                if (filterEstado) filterEstado.value = '';
                aplicarFiltros();
            });
        }
    }

    // Inicializa las acciones de la tabla (alternar estado con SweetAlert2)
    function initAcciones() {
        const tbody = document.getElementById('usuariosBody');
        if (!tbody) return;

        tbody.addEventListener('click', function(e) {
            const btnAlternar = e.target.closest('.btn-alternar-estado');
            if (!btnAlternar) return;

            const usuarioId = btnAlternar.getAttribute('data-id');
            const nombreUsuario = btnAlternar.getAttribute('data-nombre');
            const estaActivo = btnAlternar.getAttribute('data-estado') === 'true';

            const titulo = estaActivo
                ? '¿Estás seguro de desactivar?'
                : '¿Estás seguro de activar?';

            const texto = estaActivo
                ? `Se desactivará el acceso al usuario "${nombreUsuario}".`
                : `Se volverá a habilitar el acceso al usuario "${nombreUsuario}".`;

            const icono = estaActivo ? 'warning' : 'question';
            const colorBotonConfirmar = estaActivo ? '#d33' : '#198754';
            const textoBotonConfirmar = estaActivo
                ? '<i class="fas fa-ban me-1"></i> Sí, desactivar'
                : '<i class="fas fa-check me-1"></i> Sí, activar';

            if (typeof Swal !== 'undefined') {
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
                        window.location.href = `/usuarios/alternar-estado/${usuarioId}`;
                    }
                });
            } else if (confirm(texto)) {
                window.location.href = `/usuarios/alternar-estado/${usuarioId}`;
            }
        });
    }

    // Inicializa el botón flotante para volver arriba (listas extensas)
    function initBtnSubir() {
        const btnSubir = document.getElementById('btnSubir');
        const contenedor = document.querySelector('.scrollable-table');
        if (!btnSubir || !contenedor) return;

        function alternar() {
            // Muestra el botón cuando la tabla se desplaza bastante
            btnSubir.style.display = contenedor.scrollTop > 200 ? 'flex' : 'none';
        }

        contenedor.addEventListener('scroll', alternar);
        btnSubir.addEventListener('click', function() {
            contenedor.scrollTo({ top: 0, behavior: 'smooth' });
        });
    }

    // Inicializa los tooltips de Bootstrap (botón "+")
    function initTooltips() {
        if (typeof bootstrap !== 'undefined' && bootstrap.Tooltip && document.querySelectorAll('[data-bs-toggle="tooltip"]').length) {
            if (typeof bootstrap.Tooltip.getOrCreateInstance !== 'undefined') {
                document.querySelectorAll('[data-bs-toggle="tooltip"]').forEach(el => {
                    bootstrap.Tooltip.getOrCreateInstance(el);
                });
            }
        }
    }

    // Inicialización principal
    document.addEventListener('DOMContentLoaded', function() {
        initModal();
        initFiltros();
        initAcciones();
        initBtnSubir();
        initTooltips();
    });

})();
