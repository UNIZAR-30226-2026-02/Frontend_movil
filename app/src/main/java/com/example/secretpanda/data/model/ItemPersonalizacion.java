package com.example.secretpanda.data.model;

public class ItemPersonalizacion {
    private String nombre;
    private boolean bloqueado;
    private String tipo;
    private int iconoResId;
    private int precio;

    public ItemPersonalizacion(String nombre, boolean bloqueado, String tipo, int iconoResId, int precio) {
        this.nombre = nombre;
        this.bloqueado = bloqueado;
        this.tipo = tipo;
        this.iconoResId = iconoResId;
        this.precio = precio;
    }

    public String getNombre() { return nombre; }
    public boolean isBloqueado() { return bloqueado; }

    // ¡NUEVO! Permite cambiar el estado al comprarlo
    public void setBloqueado(boolean bloqueado) { this.bloqueado = bloqueado; }

    public String getTipo() { return tipo; }
    public int getIconoResId() { return iconoResId; }
    public int getPrecio() { return precio; }
}