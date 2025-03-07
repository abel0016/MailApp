package com.example.mailapp.models;

public class Correo {
    private String id;
    private String asunto;
    private String remitenteId; // Cambiado de remitente a remitenteId
    private String fechaEnvio;  // Cambiado de fecha a fechaEnvio
    private String mensaje;
    private String destinatarioEmail; // Nuevo campo
    private String estado;      // Nuevo campo

    public Correo() {
    }

    public Correo(String id, String asunto, String remitenteId, String mensaje, String fechaEnvio, String destinatarioEmail, String estado) {
        this.id = id;
        this.asunto = asunto;
        this.remitenteId = remitenteId;
        this.fechaEnvio = fechaEnvio;
        this.mensaje = mensaje;
        this.destinatarioEmail = destinatarioEmail;
        this.estado = estado;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }
    public String getRemitenteId() { return remitenteId; }
    public void setRemitenteId(String remitenteId) { this.remitenteId = remitenteId; }
    public String getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(String fechaEnvio) { this.fechaEnvio = fechaEnvio; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public String getDestinatarioEmail() { return destinatarioEmail; }
    public void setDestinatarioEmail(String destinatarioEmail) { this.destinatarioEmail = destinatarioEmail; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    // Eliminar getContent() y setContent() ya que son redundantes con getMensaje() y setMensaje()
}