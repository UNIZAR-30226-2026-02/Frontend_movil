package com.example.secretpanda.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Partida {

    @SerializedName("id_partida")
    private int idPartida;

    // El servidor lo llama codigo_partida, nosotros lo mostramos como nombre
    @SerializedName("codigo_partida")
    private String codigo_partida;

    @com.google.gson.annotations.SerializedName("tag")
    private String nombre;
    @SerializedName("max_jugadores")
    private int maxJugadores;

    @SerializedName("es_publica")
    private boolean esPublica;

    @com.google.gson.annotations.SerializedName("tiempo_espera")
    private int segundos;

    public int getSegundos() {
        return segundos;
    }

    // Le decimos a Android: "Cuando el JSON diga 'nombre', guárdalo en temática"
    @com.google.gson.annotations.SerializedName("nombre")
    private String tematica;

    @SerializedName("estado")
    private String estado;

    // El servidor nos envía una lista con los jugadores dentro de la sala
    @SerializedName("jugadores")
    private List<Object> jugadores;

    // Constructor vacío obligatorio para Gson
    public Partida() {}

    // Getters básicos
    public int getIdPartida() { return idPartida; }
    public String getNombre() { return nombre; }
    public int getMaxJugadores() { return maxJugadores; }
    public String getTematica() { return tematica; }
    public String getEstado() { return estado; }

    // Como el servidor no nos envía creador ni tiempo en el LobbyStatusDTO,
    // devolvemos un valor por defecto para que la interfaz no falle.
    public String getCreador() { return "Sistema"; }
    public String getTiempo() { return "Sin límite"; }

    // ¡TRUCO! Calculamos los jugadores actuales contando el tamaño de la lista
    public int getJugadoresActuales() {
        if (jugadores == null) return 0;
        return jugadores.size();
    }

    // Lógica para saber si está bloqueada (si no es pública)

    public String getJugadoresTexto() {
        return getJugadoresActuales() + "/" + maxJugadores;
    }

    public boolean isLlena() {
        return getJugadoresActuales() >= maxJugadores;
    }
}