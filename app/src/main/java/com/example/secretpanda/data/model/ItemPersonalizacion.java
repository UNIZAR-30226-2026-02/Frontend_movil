package com.example.secretpanda.data.model;

public class ItemPersonalizacion {
    private int id;
    private String nombre;
    private boolean bloqueado;
    private String tipo;
    private int iconoResId;
    private int precio;
    private String valor;

    public ItemPersonalizacion(String nombre, boolean bloqueado, String tipo, int iconoResId, int precio, String valor) {
        this.nombre = nombre;
        this.bloqueado = bloqueado;
        this.tipo = tipo;
        this.iconoResId = iconoResId;
        this.precio = precio;
        this.valor = valor;
    }

    // Constructor de compatibilidad
    public ItemPersonalizacion(String nombre, boolean bloqueado, String tipo, int iconoResId, int precio) {
        this(nombre, bloqueado, tipo, iconoResId, precio, "0");
    }

    public String getNombre() { return nombre; }
    public boolean isBloqueado() { return bloqueado; }

    // ¡NUEVO! Permite cambiar el estado al comprarlo
    public void setBloqueado(boolean bloqueado) { this.bloqueado = bloqueado; }

    public String getTipo() { return tipo; }
    public int getIconoResId() { return iconoResId; }
    public int getPrecio() { return precio; }

    public void setTipo(String tipo){ this.tipo = tipo;}

    public void setPrecio(int precio){ this.precio = precio;}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }
}