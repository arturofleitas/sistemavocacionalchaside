package com.tesis.vocacional.controller;

import com.tesis.vocacional.model.Pregunta;
import com.tesis.vocacional.model.Respuesta;
import com.tesis.vocacional.model.SesionInvitado;
import com.tesis.vocacional.model.Test;
import com.tesis.vocacional.model.TestUsuario;
import com.tesis.vocacional.model.TestUsuarioPregunta;
import com.tesis.vocacional.model.Usuario;
import com.tesis.vocacional.services.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Acceso de invitado sin registro. El usuario comienza el test sin iniciar
 * sesión. La pertenencia de cada evaluación se controla mediante un token
 * secreto enviado en una cookie HttpOnly; en el servidor solo se guarda su
 * hash. Reutiliza el mismo servicio de cálculo que los usuarios registrados.
 */
@Controller
@RequestMapping("/test/publico")
public class TestPublicoController {

    private final PreguntaService preguntaService;
    private final TestService testService;
    private final TestUsuarioService testUsuarioService;
    private final TestUsuarioPreguntaService testUsuarioPreguntaService;
    private final RespuestaService respuestaService;
    private final CalculoTestService calculoTestService;
    private final SesionInvitadoService sesionInvitadoService;
    private final UsuarioService usuarioService;

    public TestPublicoController(PreguntaService preguntaService,
                                 TestService testService,
                                 TestUsuarioService testUsuarioService,
                                 TestUsuarioPreguntaService testUsuarioPreguntaService,
                                 RespuestaService respuestaService,
                                 CalculoTestService calculoTestService,
                                 SesionInvitadoService sesionInvitadoService,
                                 UsuarioService usuarioService) {
        this.preguntaService = preguntaService;
        this.testService = testService;
        this.testUsuarioService = testUsuarioService;
        this.testUsuarioPreguntaService = testUsuarioPreguntaService;
        this.respuestaService = respuestaService;
        this.calculoTestService = calculoTestService;
        this.sesionInvitadoService = sesionInvitadoService;
        this.usuarioService = usuarioService;
    }

    /** Página pública de presentación e instrucciones. */
    @GetMapping
    public String inicio(Model model) {
        model.addAttribute("totalPreguntas", preguntaService.listarTodas().size());
        return "publico-inicio";
    }

    /** Crea la sesión de invitado y la evaluación, y arranca el test. */
    @PostMapping("/iniciar")
    public String iniciar(HttpServletRequest request, HttpServletResponse response) {
        SesionInvitadoService.SesionCreada creada = sesionInvitadoService.crearSesion();

        Test test = obtenerTestCHASIDE();
        TestUsuario testUsuario = new TestUsuario();
        testUsuario.setUsuario(null);
        testUsuario.setSesionInvitado(creada.sesion());
        testUsuario.setTest(test);
        testUsuario.setEstado("EN_CURSO");
        testUsuarioService.guardar(testUsuario);

        request.getSession().removeAttribute("invitadoIndiceVisualizado");
        response.addCookie(crearCookie(creada.tokenEnClaro(), request.isSecure()));
        return "redirect:/test/publico/pregunta";
    }

    /** Reinicia el test desde la pregunta 1, reutilizando la misma sesión de invitado. */
    @PostMapping("/reiniciar")
    public String reiniciar(HttpServletRequest request, HttpServletResponse response) {
        SesionInvitado sesion = resolverSesion(request).orElse(null);
        if (sesion == null) {
            return iniciar(request, response);
        }

        Test test = obtenerTestCHASIDE();
        TestUsuario testUsuario = new TestUsuario();
        testUsuario.setUsuario(null);
        testUsuario.setSesionInvitado(sesion);
        testUsuario.setTest(test);
        testUsuario.setEstado("EN_CURSO");
        testUsuarioService.guardar(testUsuario);

        request.getSession().removeAttribute("invitadoIndiceVisualizado");
        return "redirect:/test/publico/pregunta";
    }

    /** Muestra la pregunta actual; reanuda desde donde quedó si es el caso. */
    @GetMapping("/pregunta")
    public String pregunta(HttpServletRequest request, Model model) {
        SesionInvitado sesion = resolverSesion(request).orElse(null);
        TestUsuario testUsuario = testUsuarioPorSesion(sesion);
        if (sesion == null || testUsuario == null) {
            return "redirect:/test/publico?error=sesion";
        }

        if ("COMPLETADO".equals(testUsuario.getEstado())) {
            return "redirect:/test/publico/test-resultado";
        }

        List<Pregunta> preguntas = preguntaService.listarTodas();
        List<String> respuestas = reconstruirRespuestas(testUsuario, preguntas);
        int indiceActual = respuestas.size();

        if (indiceActual >= preguntas.size()) {
            return "redirect:/test/publico/test-resultado";
        }

        HttpSession session = request.getSession(false);
        int indiceMostrar = resolverIndiceMostrado(session, indiceActual);

        Pregunta preguntaActual = preguntas.get(indiceMostrar);
        model.addAttribute("pregunta", preguntaActual);
        model.addAttribute("progreso", indiceMostrar + 1);
        model.addAttribute("total", preguntas.size());
        model.addAttribute("esUltimaPregunta", indiceMostrar == preguntas.size() - 1);

        // Si se está revisando una pregunta ya respondida (se volvió con
        // "Anterior"), se expone su valor guardado para que la vista lo
        // preseleccione en vez de mostrar "Sí" por defecto.
        Boolean valorGuardado = mapaRespuestasPorPreguntaId(testUsuario).get(preguntaActual.getId());
        model.addAttribute("respuestaGuardada", valorGuardado == null ? null : (valorGuardado ? "SI" : "NO"));

        // El aviso "Retomaste el test..." solo debe mostrarse cuando el usuario
        // vuelve tras haber estado ausente (cerró la ventana, perdió la conexión,
        // etc.), no cada vez que avanza pregunta a pregunta. Cuando se responde o
        // se retrocede se marca un indicador que suprime el aviso en la siguiente
        // pregunta.
        boolean retomando = indiceActual > 0;
        if (session != null && session.getAttribute("avisoRetomandoSuprimido") != null) {
            session.removeAttribute("avisoRetomandoSuprimido");
            retomando = false;
        }
        model.addAttribute("retomando", retomando);
        return "publico-test";
    }

    /** Guarda la respuesta y avanza a la siguiente pregunta. */
    @PostMapping("/responder")
    public String responder(@RequestParam String respuesta, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        SesionInvitado sesion = resolverSesion(request).orElse(null);
        TestUsuario testUsuario = testUsuarioPorSesion(sesion);
        if (sesion == null || testUsuario == null) {
            return "redirect:/test/publico?error=sesion";
        }

        // No se puede responder un test ya finalizado
        if ("COMPLETADO".equals(testUsuario.getEstado())) {
            return "redirect:/test/publico/test-resultado";
        }

        if (!"SI".equalsIgnoreCase(respuesta) && !"NO".equalsIgnoreCase(respuesta)) {
            redirectAttributes.addFlashAttribute("error", "Respuesta inválida. Seleccioná Sí o No para continuar.");
            return "redirect:/test/publico/pregunta";
        }

        List<Pregunta> preguntas = preguntaService.listarTodas();
        List<String> respuestas = reconstruirRespuestas(testUsuario, preguntas);
        int indiceActual = respuestas.size();

        if (indiceActual >= preguntas.size()) {
            return "redirect:/test/publico/test-resultado";
        }

        // Si se está reviendo/cambiando una pregunta anterior (por "Anterior"),
        // se guarda en esa posición, no en el frente natural de avance.
        HttpSession sesionHttp = request.getSession(false);
        int indiceAResponder = resolverIndiceMostrado(sesionHttp, indiceActual);
        Pregunta preguntaActual = preguntas.get(indiceAResponder);

        Optional<TestUsuarioPregunta> optTUP = testUsuarioPreguntaService
                .buscarPorTestUsuarioYPregunta(testUsuario, preguntaActual);
        TestUsuarioPregunta tup;
        if (optTUP.isPresent()) {
            tup = optTUP.get();
        } else {
            tup = new TestUsuarioPregunta();
            tup.setTestUsuario(testUsuario);
            tup.setPregunta(preguntaActual);
            tup = testUsuarioPreguntaService.guardar(tup);
        }

        // Evitar duplicados al re-responder
        respuestaService.eliminarPorTestUsuarioPregunta(tup);

        Respuesta respuestaEntity = new Respuesta();
        respuestaEntity.setPregunta(tup);
        respuestaEntity.setFechaRespuesta(LocalDate.now());
        respuestaEntity.setValor("SI".equalsIgnoreCase(respuesta));
        respuestaService.guardar(respuestaEntity);

        HttpSession session = request.getSession();
        session.removeAttribute("invitadoIndiceVisualizado");
        session.setAttribute("avisoRetomandoSuprimido", Boolean.TRUE);
        return "redirect:/test/publico/pregunta";
    }

    /**
     * Retrocede a la pregunta anterior para revisarla o cambiarla. Ya NO
     * elimina la respuesta guardada: solo mueve el puntero de revisión de
     * sesión un lugar atrás. La respuesta sigue intacta en la base de datos
     * hasta que el usuario la reenvía (responder() reemplaza el valor previo
     * sin duplicar el par evaluación-ítem).
     */
    @PostMapping("/anterior")
    public String anterior(HttpServletRequest request) {
        SesionInvitado sesion = resolverSesion(request).orElse(null);
        TestUsuario testUsuario = testUsuarioPorSesion(sesion);
        if (sesion == null || testUsuario == null) {
            return "redirect:/test/publico?error=sesion";
        }

        List<Pregunta> preguntas = preguntaService.listarTodas();
        List<String> respuestas = reconstruirRespuestas(testUsuario, preguntas);
        int indiceActual = respuestas.size();
        if (indiceActual <= 0) {
            return "redirect:/test/publico/pregunta";
        }

        HttpSession session = request.getSession();
        int indiceMostradoActual = resolverIndiceMostrado(session, indiceActual);
        int nuevoIndice = Math.max(0, indiceMostradoActual - 1);
        session.setAttribute("invitadoIndiceVisualizado", nuevoIndice);
        session.setAttribute("avisoRetomandoSuprimido", Boolean.TRUE);
        return "redirect:/test/publico/pregunta";
    }

    /** Calcula y muestra el resultado. Persiste el resultado solo la primera vez. */
    @GetMapping("/test-resultado")
    public String testResultado(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        SesionInvitado sesion = resolverSesion(request).orElse(null);
        TestUsuario testUsuario = testUsuarioPorSesion(sesion);
        if (sesion == null || testUsuario == null) {
            return "redirect:/test/publico?error=sesion";
        }

        List<Pregunta> preguntas = preguntaService.listarTodas();
        List<String> respuestas = reconstruirRespuestas(testUsuario, preguntas);

        if (respuestas.size() != preguntas.size()) {
            redirectAttributes.addFlashAttribute("error", "Aún no respondiste todas las preguntas.");
            return "redirect:/test/publico/pregunta";
        }

        // Idempotencia: si ya está COMPLETADO, solo se recalcula (no se duplica el resultado)
        boolean yaCompletado = "COMPLETADO".equals(testUsuario.getEstado());
        ResultadoCalculo calculo = yaCompletado
                ? calculoTestService.calcular(preguntas, respuestas)
                : calculoTestService.calcularYGuardar(preguntas, respuestas, testUsuario);

        model.addAttribute("interesesPredominantes", calculo.getInteresesPredominantes());
        model.addAttribute("aptitudesPredominantes", calculo.getAptitudesPredominantes());
        model.addAttribute("topIntereses", calculoTestService.topConEmpates(calculo.getPuntajesInteres()));
        model.addAttribute("topAptitudes", calculoTestService.topConEmpates(calculo.getPuntajesAptitud()));
        model.addAttribute("puntajesInteres", calculo.getPuntajesInteres());
        model.addAttribute("puntajesAptitud", calculo.getPuntajesAptitud());
        model.addAttribute("maxPuntaje", calculo.getMaxInteres());
        model.addAttribute("maxPuntajeAptitud", calculo.getMaxAptitud());
        model.addAttribute("interesCero", calculo.isInteresCero());
        model.addAttribute("aptitudCero", calculo.isAptitudCero());
        model.addAttribute("totalPreguntas", calculo.getTotalPreguntas());
        model.addAttribute("nombresCategoria", calculo.getNombresCategoria());
        model.addAttribute("inconsistencias", calculo.getInconsistencias());

        return "publico-resultado";
    }

    /** Vincula la evaluación de invitado a una cuenta registrada y revoca el acceso. */
    @PostMapping("/guardar")
    public String guardarEnCuenta(HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuarioActual = (auth != null && auth.isAuthenticated())
                ? usuarioService.buscarPorUsername(auth.getName())
                : null;
        if (usuarioActual == null) {
            return "redirect:/login?error=sesion";
        }

        SesionInvitado sesion = resolverSesion(request).orElse(null);
        TestUsuario testUsuario = testUsuarioPorSesion(sesion);
        if (sesion == null || testUsuario == null) {
            redirectAttributes.addFlashAttribute("error", "No hay una evaluación de invitado para guardar.");
            return "redirect:/test/publico";
        }

        if ("COMPLETADO".equals(testUsuario.getEstado())) {
            testUsuario.setUsuario(usuarioActual);
            testUsuario.setSesionInvitado(null);
            testUsuarioService.guardar(testUsuario);

            sesionInvitadoService.revocar(sesion);
            response.addCookie(borrarCookie());

            redirectAttributes.addFlashAttribute("success", "La evaluación se guardó en tu historial.");
            return "redirect:/home";
        }

        redirectAttributes.addFlashAttribute("error", "La evaluación aún no está finalizada.");
        return "redirect:/test/publico/pregunta";
    }

    // ---------- Helpers ----------

    private Test obtenerTestCHASIDE() {
        Test test = testService.buscarPorNombre("CHASIDE");
        if (test == null) {
            test = new Test();
            test.setNombre("CHASIDE");
            test.setDescripcion("Test vocacional basado en categorías C-H-A-S-I-D-E");
            test.setEstado(true);
            test.setFechaCreacion(LocalDate.now());
            test = testService.guardar(test);
        }
        return test;
    }

    private Optional<SesionInvitado> resolverSesion(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        for (Cookie c : cookies) {
            if (SesionInvitadoService.COOKIE_NAME.equals(c.getName())) {
                return sesionInvitadoService.resolverPorToken(c.getValue());
            }
        }
        return Optional.empty();
    }

    private TestUsuario testUsuarioPorSesion(SesionInvitado sesion) {
        if (sesion == null) {
            return null;
        }
        return testUsuarioService.buscarPorSesionInvitado(sesion).orElse(null);
    }

    private List<String> reconstruirRespuestas(TestUsuario testUsuario, List<Pregunta> preguntas) {
        List<String> respuestas = new ArrayList<>();
        Map<Integer, Boolean> respuestasPorPregunta = mapaRespuestasPorPreguntaId(testUsuario);

        for (Pregunta p : preguntas) {
            Boolean valor = respuestasPorPregunta.get(p.getId());
            if (valor == null) {
                break;
            }
            respuestas.add(Boolean.TRUE.equals(valor) ? "SI" : "NO");
        }
        return respuestas;
    }

    /** Mapa preguntaId -> valor de respuesta guardado para esta evaluación de invitado. */
    private Map<Integer, Boolean> mapaRespuestasPorPreguntaId(TestUsuario testUsuario) {
        Map<Integer, Boolean> respuestasPorPregunta = new HashMap<>();
        if (testUsuario == null || testUsuario.getId() == 0) {
            return respuestasPorPregunta;
        }
        for (Respuesta r : respuestaService.obtenerPorTestUsuarioId(testUsuario.getId())) {
            if (r.getPregunta() != null && r.getPregunta().getPregunta() != null) {
                respuestasPorPregunta.put(r.getPregunta().getPregunta().getId(), r.getValor());
            }
        }
        return respuestasPorPregunta;
    }

    /**
     * Resuelve qué índice mostrar/responder: el de revisión guardado en
     * sesión (cuando el usuario volvió con "Anterior" y todavía es válido,
     * es decir menor al frente natural de avance), o si no el frente natural
     * ({@code indiceActual}, la primera pregunta sin respuesta contigua).
     */
    private int resolverIndiceMostrado(HttpSession session, int indiceActual) {
        if (session == null) {
            return indiceActual;
        }
        Integer indiceVisualizado = (Integer) session.getAttribute("invitadoIndiceVisualizado");
        if (indiceVisualizado != null && indiceVisualizado >= 0 && indiceVisualizado < indiceActual) {
            return indiceVisualizado;
        }
        if (indiceVisualizado != null) {
            session.removeAttribute("invitadoIndiceVisualizado");
        }
        return indiceActual;
    }

    private Cookie crearCookie(String token, boolean secure) {
        Cookie cookie = new Cookie(SesionInvitadoService.COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(SesionInvitadoService.DURACION_DIAS * 24 * 60 * 60);
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }

    private Cookie borrarCookie() {
        Cookie cookie = new Cookie(SesionInvitadoService.COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }
}
