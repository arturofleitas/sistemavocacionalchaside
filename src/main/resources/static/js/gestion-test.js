// gestion-test.js - Filtros, modal de test, botón flotante y confirmación de estado

(function() {
    'use strict';

    // Utilidades del modal de test
    function obtenerModal() {
        const modal = document.getElementById('modalTest');
        const form = document.getElementById('formTest');
        const titulo = document.getElementById('tituloModalTest');
        const instancia = modal && typeof bootstrap !== 'undefined' ? bootstrap.Modal.getOrCreateInstance(modal) : null;
        return { modal, form, titulo, instancia };
    }

    // Cambia el título del modal según sea nuevo o edición
    function setTitulo(editando) {
        const { titulo } = obtenerModal();
        if (!titulo) return;
        titulo.innerHTML = editando
            ? '<i class="fas fa-user-edit me-2"></i>Editar Test'
            : '<i class="fas fa-plus me-2"></i>Nuevo Test';
    }

    function abrirModal(editando, resetear) {
        const { form, instancia } = obtenerModal();
        if (!instancia) return;
        setTitulo(editando);
        // Solo se restablece el formulario al crear un test nuevo desde el botón "+"
        if (resetear && !editando && form) {
            form.reset();
            const idInput = form.querySelector('input[name="id"]');
            if (idInput) idInput.value = '0';
        }
        instancia.show();
    }

    // Rellena el formulario con los datos de la fila (edición)
    function rellenarFormulario(row) {
        const { form } = obtenerModal();
        if (!form) return;
        const set = (campo, valor) => {
            const el = form.elements[campo];
            if (el) el.value = (valor == null || valor === 'null') ? '' : valor;
        };
        set('id', row.getAttribute('data-id'));
        set('nombre', row.getAttribute('data-nombre'));
        set('descripcion', row.getAttribute('data-descripcion'));
        const estadoSel = form.elements['estado'];
        if (estadoSel) estadoSel.value = row.getAttribute('data-estado') || 'true';
    }

    function initModal() {
        const { modal, form } = obtenerModal();
        if (!modal || !form) return;

        // Limpia el formulario al cerrar el modal
        modal.addEventListener('hidden.bs.modal', function() {
            form.reset();
            setTitulo(false);
        });

        // Botón "+" para nuevo test
        const btnNuevo = document.getElementById('btnNuevoTest');
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

        // Reabre el modal ante una edición por navegación (/gestion-test/editar/x)
        if (document.body.getAttribute('data-abrir-modal') === 'true') {
            const idInput = form.querySelector('input[name="id"]');
            const editando = idInput && parseInt(idInput.value, 10) > 0;
            abrirModal(editando, false);
        }
    }

    // Filtros de la tabla
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
            tablaContainer.scrollTo({ top: 0, behavior: 'smooth' });
        });

        setTimeout(toggleBtnSubir, 300);
    }

    // Confirmación de activar/desactivar con SweetAlert2
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
                } else if (confirm(texto)) {
                    window.location.href = '/gestion-test/alternar-estado/' + id;
                }
            });
        });
    }

    // Tooltips de Bootstrap (botón "+")
    function initTooltips() {
        if (typeof bootstrap !== 'undefined' && bootstrap.Tooltip?.getOrCreateInstance) {
            document.querySelectorAll('[data-bs-toggle="tooltip"]').forEach(el => {
                bootstrap.Tooltip.getOrCreateInstance(el);
            });
        }
    }

    document.addEventListener('DOMContentLoaded', function() {
        initModal();
        initFiltros();
        initBtnSubir();
        initDesactivar();
        initTooltips();
    });

})();
