package com.proyecto.carmarket.Objetos;

public class Mensaje {
    String id;
    String asunto;
    String emailEmisor;
    String emailReceptor;
    String fecha;
    boolean leido;
    String mensaje;
    boolean borradoEmisor;
    boolean borradoReceptor;

    public Mensaje() {

    }

    public Mensaje(String id, String asunto, String emailEmisor, String emailReceptor, String fecha, boolean leido, String mensaje, boolean borradoEmisor, boolean borradoReceptor) {
        this.id = id;
        this.asunto = asunto;
        this.emailEmisor = emailEmisor;
        this.emailReceptor = emailReceptor;
        this.fecha = fecha;
        this.leido = leido;
        this.mensaje = mensaje;
        this.borradoEmisor = borradoEmisor;
        this.borradoReceptor = borradoReceptor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getEmailEmisor() {
        return emailEmisor;
    }

    public void setEmailEmisor(String emailEmisor) {
        this.emailEmisor = emailEmisor;
    }

    public String getEmailReceptor() {
        return emailReceptor;
    }

    public void setEmailReceptor(String emailReceptor) {
        this.emailReceptor = emailReceptor;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fechaHora) {
        this.fecha = fechaHora;
    }

    public boolean isLeido() {
        return leido;
    }

    public void setLeido(boolean leido) {
        this.leido = leido;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public boolean isBorradoEmisor() {
        return borradoEmisor;
    }

    public void setBorradoEmisor(boolean borradoEmisor) {
        this.borradoEmisor = borradoEmisor;
    }

    public boolean isBorradoReceptor() {
        return borradoReceptor;
    }

    public void setBorradoReceptor(boolean borradoReceptor) {
        this.borradoReceptor = borradoReceptor;
    }
}
