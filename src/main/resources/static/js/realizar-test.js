// Validación del formulario de test

// Impide avanzar si no se ha seleccionado una respuesta y muestra un mensaje amigable

(function() {

    'use strict';

    console.log('realizar-test.js cargado');

    document.addEventListener('DOMContentLoaded', function() {

        console.log('DOMContentLoaded ejecutado');

        var form = document.getElementById('formTest');

        var radios = document.querySelectorAll('input[name="respuesta"]');

        var alerta = document.getElementById('alertaSeleccion');

        var btnSiguiente = document.getElementById('btnSiguiente');

        var btnFinalizar = document.getElementById('btnFinalizar');

        console.log('form:', form);

        console.log('radios:', radios);

        console.log('alerta:', alerta);

        console.log('btnSiguiente:', btnSiguiente);

        console.log('btnFinalizar:', btnFinalizar);

        if (!form) {

            console.error('Formulario no encontrado');

            return;

        }

        if (!alerta) {

            console.error('Alerta no encontrada');

            return;

        }

        /*Verifica si existe una respuesta seleccionada.*/

        function isRespuestaSeleccionada() {

            for (var i = 0; i < radios.length; i++) {

                if (radios[i].checked) {

                    return true;

                }

            }

            return false;

        }

        /*
         * Oculta la alerta de validación.
         */

        function ocultarAlerta() {

            alerta.style.display = 'none';

            console.log('Alerta ocultada');

        }

        /*
         * Muestra la alerta de validación.
         */

        function mostrarAlerta() {

            alerta.style.display = 'block';

            alerta.scrollIntoView({
                behavior: 'smooth',
                block: 'center'
            });

            console.log('Alerta mostrada');

        }

        /*
         * Ocultar alerta cuando el usuario
         * selecciona una respuesta.
         */

        radios.forEach(function(radio) {

            radio.addEventListener('change', ocultarAlerta);

        });

        /*BOTÓN SIGUIENTE*/

        if (btnSiguiente) {

            btnSiguiente.addEventListener('click', function(event) {

                console.log('Botón Siguiente presionado');

                /*
                 * Verificar si el usuario respondió
                 * la pregunta actual.
                 */

                if (!isRespuestaSeleccionada()) {

                    console.log('Respuesta no seleccionada');

                    /*
                     * Evitar cualquier comportamiento
                     * posterior del botón.
                     */

                    event.preventDefault();

                    /*
                     * Mostrar mensaje de validación.
                     */

                    mostrarAlerta();

                    return;

                }

                /*
                 * Si existe una respuesta,
                 * se puede continuar.
                 */

                console.log('Respuesta seleccionada, enviando');

                /*
                 * Evitar doble clic durante el envío.
                 */

                btnSiguiente.disabled = true;

                form.submit();

            });

        }

        /* BOTÓN FINALIZAR TEST
         * Es de tipo "button": se envía el formulario desde JS para
         * garantizar que llegue al backend y muestre el resultado. */

        if (btnFinalizar) {

            btnFinalizar.addEventListener('click', function(event) {

                if (!isRespuestaSeleccionada()) {

                    event.preventDefault();

                    mostrarAlerta();

                    return;

                }

                // Validado: enviar el formulario para ir al resultado
                event.preventDefault();

                btnFinalizar.disabled = true;

                form.submit();

            });

        }

        /* Navegación por teclado (simplificada):
         * - Flechas izquierda/derecha (y arriba/abajo): alternan entre Sí y No.
         * - Enter: confirma la opción seleccionada y envía el formulario. */

        document.addEventListener('keydown', function(event) {

            var mover = ['ArrowLeft', 'ArrowRight', 'ArrowUp', 'ArrowDown'].indexOf(event.key) !== -1;

            if (mover) {

                var actual = -1;

                for (var i = 0; i < radios.length; i++) {

                    if (radios[i].checked) { actual = i; break; }

                }

                // Si no hay selección se elige "Sí"; izquierda/arriba -> Sí, derecha/abajo -> No
                var destino = (actual === -1) ? 0 : ((event.key === 'ArrowLeft' || event.key === 'ArrowUp') ? 0 : 1);

                if (radios[destino]) {

                    radios[destino].checked = true;

                    radios[destino].focus();

                }

                event.preventDefault();

            } else if (event.key === 'Enter') {

                // Confirmar la opción elegida (envía el formulario)
                if (isRespuestaSeleccionada()) {

                    event.preventDefault();

                    form.submit();

                }

            }

        });

        /* En escritorio, el foco y la selección empiezan en "Sí" en cada pregunta */

        var contenedorOpciones = document.getElementById('opciones');

        if (contenedorOpciones && window.innerWidth >= 768 && radios[0] && !isRespuestaSeleccionada()) {

            radios[0].checked = true;

            radios[0].focus();

        }

        console.log('Validación inicializada correctamente');

    });

})();