package com.example.secretpanda.data.model;

import java.io.Serializable;

public class Jugador implements Serializable {

    // ==========================================
    // CAMPOS DE LA BASE DE DATOS (Espejo del SQL)
    // ==========================================
    private String idGoogle;
    private String tag;              // Tu nombre de usuario único
    private String fotoPerfil;       // Texto/URL de la imagen
    private int balas;
    private String fechaRegistro;    // Se suele guardar como String al pasarlo por red

    // Estadísticas globales
    private int partidasJugadas;
    private int victorias;
    private int numAciertos;
    private int numFallos;

    // ==========================================
    // CAMPOS TEMPORALES DE PARTIDA (No van a la BD)
    // ==========================================
    private transient boolean esEquipoAzul; // <--- AÑADE ESTO

    // ==========================================
    // CAMPOS TEMPORALES DE PARTIDA (No van a la BD)
    // ==========================================
    // "transient" significa: "ignora esto cuando guardes el jugador en la base de datos de internet"
    private transient boolean esEspia;
    private transient int puntosPartidaActual;

    private transient int idEquipo;

    // Constructor inicial (Cuando solo sabemos su Tag / Nombre)
    public Jugador(String tag) {
        this.tag = tag;
        this.balas = 0;
        this.partidasJugadas = 0;
        this.victorias = 0;
        this.numAciertos = 0;
        this.numFallos = 0;

        // Variables de partida por defecto
        this.esEspia = false;
        this.puntosPartidaActual = 0;
    }

    // ==========================================
    // GETTERS Y SETTERS
    // ==========================================

    public String getIdGoogle() { return idGoogle; }
    public void setIdGoogle(String idGoogle) { this.idGoogle = idGoogle; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public String getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }

    public int getBalas() { return balas; }
    public void setBalas(int balas) { this.balas = balas; }

    public String getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(String fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public int getPartidasJugadas() { return partidasJugadas; }
    public void sumarPartidaJugada() { this.partidasJugadas++; }

    public int getVictorias() { return victorias; }
    public void sumarVictoria() { this.victorias++; }

    public int getNumAciertos() { return numAciertos; }
    public void sumarAcierto() { this.numAciertos++; }

    public int getNumFallos() { return numFallos; }
    public void sumarFallo() { this.numFallos++; }

    public boolean isEsEspia() { return esEspia; }
    public void setEsEspia(boolean esEspia) { this.esEspia = esEspia; }

    public int getPuntosPartidaActual() { return puntosPartidaActual; }
    public void sumarPuntosPartida(int puntos) { this.puntosPartidaActual += puntos; }
    public boolean isEsEquipoAzul() { return esEquipoAzul; }
    public void setEsEquipoAzul(boolean esEquipoAzul) { this.esEquipoAzul = esEquipoAzul; }
}