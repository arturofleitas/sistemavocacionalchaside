package com.tesis.vocacional.services;

import com.tesis.vocacional.model.Usuario;
import com.tesis.vocacional.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Guarda un usuario en el sistema.
     * Si el ID existe, realiza una actualización. Si el ID no existe, realiza un nuevo registro.
     *
     * @param usuario Objeto usuario recibido desde el formulario.
     * @return Usuario guardado o actualizado.
     */
    public Usuario guardarUsuario(Usuario usuario) {
        try {
            // Validar fecha de nacimiento
            if (usuario.getFecha_nacimiento() == null) {
                throw new RuntimeException("La fecha de nacimiento es obligatoria.");
            }

            LocalDate fechaNac = usuario.getFecha_nacimiento().toLocalDate();
            LocalDate hoy = LocalDate.now();

            // Verificar que no sea futura
            if (fechaNac.isAfter(hoy)) {
                throw new RuntimeException("La fecha de nacimiento no puede ser futura.");
            }

            // Calcular edad
            int edad = Period.between(fechaNac, hoy).getYears();

            // Verificar edad mínima (14 años)
            if (edad < 14) {
                throw new RuntimeException("Debes tener al menos 14 años para registrarte.");
            }

            // MODO ACTUALIZACIÓN
            if (usuario.getId() != 0 && usuario.getId() > 0) {
                Optional<Usuario> opt = usuarioRepository.findById(usuario.getId());
                if (opt.isPresent()) {
                    Usuario existente = opt.get();

                    // Validar username duplicado únicamente si fue modificado
                    if (!usuario.getUsername().equals(existente.getUsername())
                            && usuarioRepository.existsByUsername(usuario.getUsername())) {
                        throw new RuntimeException("El nombre de usuario ya está en uso.");
                    }

                    // Actualización de datos
                    existente.setNombre(usuario.getNombre());
                    existente.setApellido(usuario.getApellido());
                    existente.setFecha_nacimiento(usuario.getFecha_nacimiento());
                    existente.setUsername(usuario.getUsername());
                    existente.setPassword(usuario.getPassword());
                    existente.setRol(usuario.getRol());
                    existente.setEstado(usuario.getEstado());

                    return usuarioRepository.save(existente);
                }
            }

            // MODO CREACIÓN
            // Validar username duplicado
            if (usuarioRepository.existsByUsername(usuario.getUsername())) {
                throw new RuntimeException("El nombre de usuario ya está en uso.");
            }

            return usuarioRepository.save(usuario);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Ocurrió un error inesperado al procesar la solicitud.");
        }
    }

    public Usuario buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username).orElse(null);
    }

    public Usuario buscarPorId(int id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public void eliminarUsuario(int id) {
        usuarioRepository.deleteById(id);
    }
    //Retorna el número total de usuarios registrados en el sistema.
    public long count() {
        return usuarioRepository.count();
    }
}