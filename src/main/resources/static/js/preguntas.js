// preguntas.js - Filtros, modal de pregunta, scroll y confirmación de eliminar

(function() {
    'use strict';

    // Utilidades del modal de pregunta
    function obtenerModal() {
        const modal = document.getElementById('modalPregunta');
        const form = document.getElementById('formPregunta');
        const titulo = document.getElementById('tituloModalPregunta');
        const instancia = modal && typeof bootstrap !== 'undefined' ? bootstrap.Modal.getOrCreateInstance(modal) : null;
        return { modal, form, titulo, instancia };
    }

    // Cambia el título del modal según sea nueva o edición
    function setTitulo(editando) {
        const { titulo } = obtenerModal();
        if (!titulo) return;
        titulo.innerHTML = editando
            ? '<i class="fas fa-user-edit me-2"></i>Editar Pregunta'
            : '<i class="fas fa-plus me-2"></i>Nueva Pregunta';
    }

    function abrirModal(editando, resetear) {
        const { form, instancia } = obtenerModal();
        if (!instancia) return;
        setTitulo(editando);
        // Solo se restablece el formulario al crear una pregunta nueva desde el botón "+"
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
        set('numero', row.getAttribute('data-numero'));
        set('texto', row.getAttribute('data-texto'));
        set('categoria', row.getAttribute('data-categoria'));
        set('dimension', row.getAttribute('data-dimension'));

        // Selecciona el test; si no está en la lista (inactivo) se agrega como opción
        const testSel = form.elements['test'];
        if (testSel) {
            const testId = row.getAttribute('data-test-id') || '';
            const testNombre = row.getAttribute('data-test-nombre') || '';
            if (testId) {
                if (!Array.from(testSel.options).some(o => o.value === testId)) {
                    const opt = document.createElement('option');
                    opt.value = testId;
                    opt.textContent = testNombre || 'Test asignado';
                    testSel.appendChild(opt);
                }
                testSel.value = testId;
            } else {
                testSel.value = '';
            }
        }
    }

    function initModal() {
        const { modal, form } = obtenerModal();
        if (!modal || !form) return;

        // Limpia el formulario al cerrar el modal
        modal.addEventListener('hidden.bs.modal', function() {
            form.reset();
            setTitulo(false);
        });

        // Botón "+" para nueva pregunta
        const btnNuevo = document.getElementById('btnNuevoPregunta');
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

        // Reabre el modal ante un error del servidor o edición por navegación
        if (document.body.getAttribute('data-abrir-modal') === 'true') {
            const idInput = form.querySelector('input[name="id"]');
            const editando = idInput && parseInt(idInput.value, 10) > 0;
            abrirModal(editando, false);
        }
    }

    // Filtros de la tabla
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

    // Botón flotante para subir
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
            tablaContainer.scrollTo({ top: 0, behavior: 'smooth' });
        });
    }

    // Confirmación con SweetAlert2 para eliminar preguntas
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
                } else if (confirm(texto)) {
                    window.location.href = '/preguntas/eliminar/' + id;
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
        initEliminar();
        initTooltips();
    });

})();
