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
         * Este botón sí es "submit", pero se valida
         * directamente antes de permitir el envío.*/

        if (btnFinalizar) {

            btnFinalizar.addEventListener('click', function(event) {

                console.log('Botón Finalizar presionado');

                /*
                 * Comprobar si existe una respuesta.
                 */

                if (!isRespuestaSeleccionada()) {

                    console.log('Respuesta no seleccionada');

                    /*
                     * Cancelar el envío.
                     */

                    event.preventDefault();

                    /*
                     * Mostrar alerta.
                     */

                    mostrarAlerta();

                    return;

                }

                /*
                 * Si hay respuesta, permitir el envío.
                 */

                console.log('Respuesta seleccionada, finalizando test');

                /*
                 * Evitar múltiples envíos.
                 */

                btnFinalizar.disabled = true;

            });

        }

        console.log('Validación inicializada correctamente');

    });

})();