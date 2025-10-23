package com.example.precional.api;

public class N8nRequest {
    private String nombre_usuario;
    private String nombre_contacto_emergencia;
    private String numero_contacto_emergencia;
    private String email_contacto_emergencia;
    private int presion_sistolica;
    private int presion_diastolica;
    private String diagnostico;

    public N8nRequest(String nombreUsuario,
                      String nombreContactoEmergencia, String numeroContactoEmergencia, String emailContactoEmergencia,
                      int presionSistolica, int presionDiastolica, String diagnostico) {
        this.nombre_usuario = nombreUsuario;
        this.nombre_contacto_emergencia = nombreContactoEmergencia;
        this.numero_contacto_emergencia = numeroContactoEmergencia;
        this.email_contacto_emergencia = emailContactoEmergencia;
        this.presion_sistolica = presionSistolica;
        this.presion_diastolica = presionDiastolica;
        this.diagnostico = diagnostico;
    }

    // Getters
    public String getNombre_usuario() { return nombre_usuario; }
    public String getNombre_contacto_emergencia() { return nombre_contacto_emergencia; }
    public String getNumero_contacto_emergencia() { return numero_contacto_emergencia; }
    public String getEmail_contacto_emergencia() { return email_contacto_emergencia; }
    public int getPresion_sistolica() { return presion_sistolica; }
    public int getPresion_diastolica() { return presion_diastolica; }
    public String getDiagnostico() { return diagnostico; }
}

