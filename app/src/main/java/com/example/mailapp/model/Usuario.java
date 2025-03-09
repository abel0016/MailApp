package com.example.mailapp.model;

public class Usuario {
    private String id;
    private String nombre;
    private String fechaNacimiento;
    private String email;
    private String fotoUrl;

    public Usuario() {
    }

    public Usuario(String id,String nombre, String fechaNacimiento, String email,String fotoUrl) {
        this.id = id;
        this.nombre = nombre;
        this.fechaNacimiento = fechaNacimiento;
        this.email = email;
        this.fotoUrl = fotoUrl;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
}
