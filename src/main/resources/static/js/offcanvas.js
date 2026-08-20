// offcanvas.js - Control del menú móvil (sin Bootstrap)
function toggleOffcanvas() {
    var el = document.getElementById('offcanvasMenu');
    if (el) {
        el.classList.toggle('open');
    }
}

function closeOffcanvas() {
    var el = document.getElementById('offcanvasMenu');
    if (el) {
        el.classList.remove('open');
    }
}

// Cerrar al hacer clic fuera del offcanvas
document.addEventListener('click', function(e) {
    var offcanvas = document.getElementById('offcanvasMenu');
    var toggleBtn = document.querySelector('.menu-toggle-btn');
    if (offcanvas && toggleBtn && !offcanvas.contains(e.target) && !toggleBtn.contains(e.target)) {
        offcanvas.classList.remove('open');
    }
});

// Cerrar al hacer clic en un enlace del menú
document.addEventListener('click', function(e) {
    var link = e.target.closest('.offcanvas-body .nav-link');
    if (link) {
        closeOffcanvas();
    }
});