package com.tesis.vocacional.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Sesión de acceso de invitado (sin registro).
 *
 * Separa el identificador interno de la credencial secreta:
 *  - id: identificador interno de la sesión (UUID). Conocerlo no otorga acceso.
 *  - tokenHash: huella (hash) del token secreto enviado en la cookie del
 *    navegador. El token en claro nunca se guarda.
 */
@Entity
public class SesionInvitado {

    @Id
    private UUID id;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "expira_en", nullable = false)
    private LocalDateTime expiraEn;

    @Column(name = "revocado_en")
    private LocalDateTime revocadoEn;

    public SesionInvitado() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public LocalDateTime getExpiraEn() {
        return expiraEn;
    }

    public void setExpiraEn(LocalDateTime expiraEn) {
        this.expiraEn = expiraEn;
    }

    public LocalDateTime getRevocadoEn() {
        return revocadoEn;
    }

    public void setRevocadoEn(LocalDateTime revocadoEn) {
        this.revocadoEn = revocadoEn;
    }

    public boolean estaVigente() {
        if (revocadoEn != null) {
            return false;
        }
        return expiraEn != null && LocalDateTime.now().isBefore(expiraEn);
    }
}
