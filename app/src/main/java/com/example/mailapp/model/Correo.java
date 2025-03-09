package com.example.mailapp.model;

import com.google.firebase.Timestamp;

import java.util.Date;

public class Correo {
    private String id;
    private String remitenteId;
    private String destinatarioEmail;
    private String asunto;
    private String contenido;
    private String estado; // "recibido" o "enviado"
    private Timestamp fechaEnvio;

    // Constructor vacío requerido por Firestore
    public Correo() {
    }

    public Correo(String remitenteId, String destinatarioEmail, String asunto, String contenido, String estado, Timestamp fechaEnvio) {
        this.remitenteId = remitenteId;
        this.destinatarioEmail = destinatarioEmail;
        this.asunto = asunto;
        this.contenido = contenido;
        this.estado = estado;
        this.fechaEnvio = fechaEnvio;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRemitenteId() {
        return remitenteId;
    }

    public void setRemitenteId(String remitenteId) {
        this.remitenteId = remitenteId;
    }

    public String getDestinatarioEmail() {
        return destinatarioEmail;
    }

    public void setDestinatarioEmail(String destinatarioEmail) {
        this.destinatarioEmail = destinatarioEmail;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Timestamp getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(Timestamp fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }
}