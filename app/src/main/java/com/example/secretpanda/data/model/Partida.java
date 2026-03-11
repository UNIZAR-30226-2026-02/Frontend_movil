package com.example.secretpanda.data.model;

public class Partida {
    private String nombre;
    private String creador;
    private String tiempo;

    private int jugadoresActuales;
    private int maxJugadores;

    private boolean bloqueada;
    private String tematica;

    public Partida(String nombre, String creador, String tiempo, int jugadoresActuales, int maxJugadores, boolean bloqueada, String tematica) {
        this.nombre = nombre;
        this.creador = creador;
        this.tiempo = tiempo;
        this.jugadoresActuales = jugadoresActuales;
        this.maxJugadores = maxJugadores;
        this.bloqueada = bloqueada;
        this.tematica = tematica;
    }

    public String getNombre() { return nombre; }
    public String getCreador() { return creador; }
    public String getTiempo() { return tiempo; }
    public boolean isBloqueada() { return bloqueada; }
    public String getTematica() { return tematica; }

    public int getJugadoresActuales() { return jugadoresActuales; }
    public int getMaxJugadores() { return maxJugadores; }


    public String getJugadoresTexto() {
        return jugadoresActuales + "/" + maxJugadores;
    }

    public boolean isLlena() {
        return jugadoresActuales >= maxJugadores;
    }
}