package com.tesis.vocacional.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

@Entity
public class Usuario extends Persona {

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private Boolean estado;

    private String rol;

    @OneToMany(mappedBy = "usuario",
               cascade = CascadeType.ALL,
               orphanRemoval = true)
    private List<TestUsuario> testUsuarios = new ArrayList<>();

    public Usuario() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public List<TestUsuario> getTestUsuarios() {
        return testUsuarios;
    }

    public void setTestUsuarios(List<TestUsuario> testUsuarios) {
        this.testUsuarios = testUsuarios;
    }
}