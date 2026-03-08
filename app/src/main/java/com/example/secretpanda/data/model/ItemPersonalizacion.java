package com.example.secretpanda.data.model; // Cambia la ruta si lo pones en otra carpeta

public class ItemPersonalizacion {
    private String nombre;
    private boolean bloqueado;

    public ItemPersonalizacion(String nombre, boolean bloqueado) {
        this.nombre = nombre;
        this.bloqueado = bloqueado;
    }

    public String getNombre() { return nombre; }
    public boolean isBloqueado() { return bloqueado; }
}