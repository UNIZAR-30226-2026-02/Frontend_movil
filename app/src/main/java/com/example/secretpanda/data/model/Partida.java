package com.example.secretpanda.data.model;

public class Partida {
    private String nombre;
    private String creador;
    private String tiempo;
    private String jugadores;
    private boolean bloqueada;
    private String tematica; // NUEVO CAMPO

    public Partida(String nombre, String creador, String tiempo, String jugadores, boolean bloqueada, String tematica) {
        this.nombre = nombre;
        this.creador = creador;
        this.tiempo = tiempo;
        this.jugadores = jugadores;
        this.bloqueada = bloqueada;
        this.tematica = tematica;
    }

    public String getNombre() { return nombre; }
    public String getCreador() { return creador; }
    public String getTiempo() { return tiempo; }
    public String getJugadores() { return jugadores; }
    public boolean isBloqueada() { return bloqueada; }
    public String getTematica() { return tematica; }
}
