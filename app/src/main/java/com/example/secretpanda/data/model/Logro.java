package com.example.secretpanda.data.model;

public class Logro {
    private int idLogro;
    private String nombre;
    private String descripcion;
    private String tipo;             // 'medalla' o 'logro'
    private String estadisticaClave; // 'victorias', 'partidas_jugadas', 'num_aciertos', 'num_fallos'
    private int valorObjetivo;
    private int balasRecompensa;

    // Campos de la tabla jugador_logro
    private int progresoActual;
    private boolean completado;

    // Constructor vacío para poder crearlo desde el JSON paso a paso
    public Logro() {
    }

    // Constructor completo original
    public Logro(int idLogro, String nombre, String descripcion, String tipo, String estadisticaClave, int valorObjetivo, int balasRecompensa) {
        this.idLogro = idLogro;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.estadisticaClave = estadisticaClave;
        this.valorObjetivo = valorObjetivo;
        this.balasRecompensa = balasRecompensa;
        this.progresoActual = 0;
        this.completado = false;
    }


    // GETTERS
    public int getIdLogro() { return idLogro; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getTipo() { return tipo; }
    public String getEstadisticaClave() { return estadisticaClave; }
    public int getValorObjetivo() { return valorObjetivo; }
    public int getBalasRecompensa() { return balasRecompensa; }
    public int getProgresoActual() { return progresoActual; }
    public boolean isCompletado() { return completado; }

    // SETTERS (
    public void setIdLogro(int idLogro) { this.idLogro = idLogro; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setEstadisticaClave(String estadisticaClave) { this.estadisticaClave = estadisticaClave; }
    public void setValorObjetivo(int valorObjetivo) { this.valorObjetivo = valorObjetivo; }
    public void setBalasRecompensa(int balasRecompensa) { this.balasRecompensa = balasRecompensa; }
    public void setProgresoActual(int progresoActual) { this.progresoActual = progresoActual; }
    public void setCompletado(boolean completado) { this.completado = completado; }
}