package com.example.mailapp.models;

public class Correo {
    private String id;
    private String asunto;
    private String remitente;
    private String fecha;
    private String mensaje;

    public Correo() {
    }

    public Correo(String id, String asunto, String remitente, String fecha) {
        this.id = id;
        this.asunto = asunto;
        this.remitente = remitente;
        this.fecha = fecha;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }
    public String getRemitente() { return remitente; }
    public void setRemitente(String remitente) { this.remitente = remitente; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public String getContent() { return mensaje; }
    public void setContent(String content) { this.mensaje = content; }
}