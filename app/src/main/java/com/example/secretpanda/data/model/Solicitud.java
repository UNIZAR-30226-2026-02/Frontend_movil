package com.example.secretpanda.data.model;

public class Solicitud {
    public int id_solicitante;
    public String tag_solicitante;
    public String foto_perfil;
    public String fecha_solicitud;
    public String estado;

    public Solicitud(int idSolicitante, String nombre, String fotoPerfil, String fechaSolicitud, String estado) {
        this.id_solicitante = idSolicitante;
        this.tag_solicitante = nombre;
        this.foto_perfil = fotoPerfil;
        this.fecha_solicitud = fechaSolicitud;
        this.estado = estado;
    }
    public String getFotoPerfil() {
        return foto_perfil;
    }

    public String getFechaSolicitud() {
        return fecha_solicitud;
    }

    public String getEstado() {
        return estado;
    }
    public int getIdSolicitante() {
        return id_solicitante;
    }

    public String getNombre() {
        return tag_solicitante;
    }
}
