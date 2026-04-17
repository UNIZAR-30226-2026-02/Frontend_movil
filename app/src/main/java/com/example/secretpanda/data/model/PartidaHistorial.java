package com.example.secretpanda.data.model;

public class PartidaHistorial {
    public int id_partida;
    public String codigo_partida;
    public String fechaFin;
    public String estado;
    public Boolean rojoGana;
    public String equipo; // "rojo" o "azul"
    public String rol;    // "espia" o "agente"
    public boolean abandono;
    public int numAciertos;
    public int numFallos;
}
