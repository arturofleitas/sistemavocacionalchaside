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

            // Verificar que no sea una fecha futura
            if (fechaNac.isAfter(hoy)) {
                throw new RuntimeException("La fecha de nacimiento no puede ser futura.");
            }

            // Verificar que la fecha no sea demasiado antigua (año 1900 como límite)
            if (fechaNac.isBefore(LocalDate.of(1900, 1, 1))) {
                throw new RuntimeException("La fecha de nacimiento no puede ser anterior al año 1900.");
            }

            // Calcular edad actual
            int edad = Period.between(fechaNac, hoy).getYears();

            // Verificar edad mínima (14 años)
            if (edad < 14) {
                throw new RuntimeException("Debes tener al menos 14 años para registrarte.");
            }

            // Verificar edad máxima (120 años)
            if (edad > 120) {
                throw new RuntimeException("La edad no puede superar los 120 años.");
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

                    // Actualización de datos permitidos
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

    /**
     * Cambia el estado del usuario al valor contrario al que tiene actualmente.
     * 
     * @param id ID del usuario.
     * @return El nuevo estado resultante (true si quedó activo, false si quedó inactivo).
     */
    public boolean alternarEstado(int id) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario != null) {
            // Invierte el estado actual (si era true pasa a false, si era false o null pasa a true)
            boolean estadoActual = usuario.getEstado() != null ? usuario.getEstado() : false;
            boolean nuevoEstado = !estadoActual;
            
            usuario.setEstado(nuevoEstado);
            usuarioRepository.save(usuario);
            return nuevoEstado;
        } else {
            throw new RuntimeException("No se encontró el usuario especificado.");
        }
    }

    /**
     * Busca y retorna un usuario por su nombre de usuario (username).
     */
    public Usuario buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username).orElse(null);
    }

    /**
     * Busca y retorna un usuario por su ID único.
     */
    public Usuario buscarPorId(int id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    /**
     * Retorna una lista con todos los usuarios registrados en el sistema.
     */
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    /**
     * Elimina físicamente un usuario por su ID.
     */
    public void eliminarUsuario(int id) {
        usuarioRepository.deleteById(id);
    }

    /**
     * Retorna el número total de usuarios registrados en el sistema.
     */
    public long count() {
        return usuarioRepository.count();
    }
}