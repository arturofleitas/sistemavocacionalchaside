package com.tesis.vocacional.services;

import com.tesis.vocacional.model.Usuario;
import com.tesis.vocacional.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Usuario guardarUsuario(Usuario usuario) {
        System.out.println("=== GUARDANDO USUARIO ===");
        System.out.println("ID recibido: " + usuario.getId());
        System.out.println("Nombre: " + usuario.getNombre() + " " + usuario.getApellido());
        System.out.println("Username: " + usuario.getUsername());

        if (usuario.getId() != 0 && usuario.getId() > 0) {
            System.out.println("→ Modo ACTUALIZACIÓN");

            // Busca el usuario en la BD
            Optional<Usuario> opt = usuarioRepository.findById(usuario.getId());
            if (opt.isPresent()) {
                Usuario existente = opt.get();

                existente.setNombre(usuario.getNombre());
                existente.setApellido(usuario.getApellido());
                existente.setCedula(usuario.getCedula());
                existente.setEmail(usuario.getEmail());
                existente.setEdad(usuario.getEdad());
                existente.setGenero(usuario.getGenero());
                existente.setUsername(usuario.getUsername());
                existente.setPassword(usuario.getPassword());
                existente.setRol(usuario.getRol());
                existente.setEstado(usuario.getEstado());

                Usuario actualizado = usuarioRepository.save(existente);
                System.out.println("→ Actualizado correctamente con ID: " + actualizado.getId());
                return actualizado;
            }
        }

        // Modo creación
        System.out.println("→ Modo CREACIÓN");
        if (usuarioRepository.existsByUsername(usuario.getUsername())) {
            throw new RuntimeException("El username ya está en uso");
        }
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }
        if (usuario.getCedula() != 0 && usuarioRepository.existsByCedula(usuario.getCedula())) {
            throw new RuntimeException("La cédula ya está registrada");
        }

        Usuario nuevo = usuarioRepository.save(usuario);
        System.out.println("→ Creado correctamente con ID: " + nuevo.getId());
        return nuevo;
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
}