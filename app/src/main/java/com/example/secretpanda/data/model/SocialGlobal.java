package com.example.secretpanda.data.model;

import java.util.ArrayList;
import java.util.List;

public class SocialGlobal {
    private static SocialGlobal instance;

    // Listas que simulan tu Base de Datos
    private List<String> usuariosRegistrados;
    private List<String> misAmigos;
    private List<String> solicitudesRecibidas;
    private List<String> solicitudesPendientes;

    private SocialGlobal() {
        usuariosRegistrados = new ArrayList<>();
        misAmigos = new ArrayList<>();
        solicitudesRecibidas = new ArrayList<>();
        solicitudesPendientes = new ArrayList<>();

        // 1. Usuarios que existen en la BD (para probar el buscador)
        usuariosRegistrados.add("NinjaPanda");
        usuariosRegistrados.add("PeleteLover");
        usuariosRegistrados.add("GamerPro");
        usuariosRegistrados.add("Maria123");
        usuariosRegistrados.add("CarlosRex");

        // 2. Usuarios que YA son tus amigos
        misAmigos.add("NinjaPanda");

        // 3. Solicitudes que otros te han enviado a ti
        solicitudesRecibidas.add("Maria123");
        solicitudesRecibidas.add("CarlosRex");

        // 4. Empiezas sin haber enviado ninguna
    }

    public static SocialGlobal getInstance() {
        if (instance == null) instance = new SocialGlobal();
        return instance;
    }

    public List<String> getSolicitudesRecibidas() { return solicitudesRecibidas; }
    public List<String> getSolicitudesPendientes() { return solicitudesPendientes; }

    // Métodos de validación
    public boolean existeUsuario(String nombre) { return usuariosRegistrados.contains(nombre); }
    public boolean esMiAmigo(String nombre) { return misAmigos.contains(nombre); }
    public boolean yaEnvieSolicitud(String nombre) { return solicitudesPendientes.contains(nombre); }

    // Acciones
    public void enviarSolicitud(String nombre) { solicitudesPendientes.add(nombre); }
    public void aceptarSolicitud(String nombre) {
        solicitudesRecibidas.remove(nombre);
        misAmigos.add(nombre);
    }
    public void rechazarSolicitud(String nombre) { solicitudesRecibidas.remove(nombre); }
    public List<String> getMisAmigos() { return misAmigos; }
}