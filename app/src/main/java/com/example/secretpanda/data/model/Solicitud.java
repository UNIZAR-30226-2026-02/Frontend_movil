package com.example.secretpanda.data.model;

import java.io.Serializable;

public class Solicitud implements Serializable {
    private String id_solicitante;
    private String tag_solicitante;
    private String foto_perfil_solicitante;
    private String fecha_solicitud;
    private String estado;

    public Solicitud(String id_solicitante, String tag_solicitante, String foto_perfil_solicitante, String fecha_solicitud, String estado) {
        this.id_solicitante = id_solicitante;
        this.tag_solicitante = tag_solicitante;
        this.foto_perfil_solicitante = foto_perfil_solicitante;
        this.fecha_solicitud = fecha_solicitud;
        this.estado = estado;
    }

    public String getIdSolicitante() { return id_solicitante; }
    public String getTagSolicitante() { return tag_solicitante; }
    public String getFotoPerfilSolicitante() { return foto_perfil_solicitante; }
    public String getFechaSolicitud() { return fecha_solicitud; }
    public String getEstado() { return estado; }
}