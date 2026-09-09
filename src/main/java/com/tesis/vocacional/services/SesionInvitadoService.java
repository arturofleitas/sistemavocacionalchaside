package com.tesis.vocacional.services;

import com.tesis.vocacional.model.SesionInvitado;
import com.tesis.vocacional.repository.SesionInvitadoRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * Gestiona las sesiones de acceso de invitado (sin registro).
 *
 * Separa el identificador interno (UUID) de la credencial secreta:
 *  - El token en claro solo se envía al navegador en una cookie HttpOnly.
 *  - En la base de datos solo se guarda su hash (SHA-256), nunca el token.
 */
@Service
public class SesionInvitadoService {

    public static final String COOKIE_NAME = "invitadoToken";
    public static final int DURACION_DIAS = 7;
    private static final int TOKEN_BYTES = 32;

    private final SesionInvitadoRepository sesionInvitadoRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public SesionInvitadoService(SesionInvitadoRepository sesionInvitadoRepository) {
        this.sesionInvitadoRepository = sesionInvitadoRepository;
    }

    /** Resultado de la creación de una sesión: la entidad y el token en claro para la cookie. */
    public record SesionCreada(SesionInvitado sesion, String tokenEnClaro) {
    }

    public SesionCreada crearSesion() {
        String tokenEnClaro = generarToken();
        SesionInvitado sesion = new SesionInvitado();
        sesion.setId(UUID.randomUUID());
        sesion.setTokenHash(hashToken(tokenEnClaro));
        sesion.setCreadoEn(LocalDateTime.now());
        sesion.setExpiraEn(LocalDateTime.now().plusDays(DURACION_DIAS));
        sesion.setRevocadoEn(null);
        sesionInvitadoRepository.save(sesion);
        return new SesionCreada(sesion, tokenEnClaro);
    }

    /** Resuelve una sesión a partir del token presentado, solo si sigue vigente. */
    public Optional<SesionInvitado> resolverPorToken(String tokenEnClaro) {
        if (tokenEnClaro == null || tokenEnClaro.isBlank()) {
            return Optional.empty();
        }
        return sesionInvitadoRepository.findByTokenHash(hashToken(tokenEnClaro))
                .filter(SesionInvitado::estaVigente);
    }

    public void revocar(SesionInvitado sesion) {
        sesion.setRevocadoEn(LocalDateTime.now());
        sesionInvitadoRepository.save(sesion);
    }

    public String hashToken(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    private String generarToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
