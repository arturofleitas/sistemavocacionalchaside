package com.tesis.vocacional.repository;

import com.tesis.vocacional.model.SesionInvitado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SesionInvitadoRepository extends JpaRepository<SesionInvitado, UUID> {

    Optional<SesionInvitado> findByTokenHash(String tokenHash);
}
