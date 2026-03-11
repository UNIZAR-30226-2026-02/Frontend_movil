package com.example.secretpanda.data.model;

import java.util.ArrayList;
import java.util.List;

public class GestorEstadisticas {
    private static GestorEstadisticas instance;

    private int partidasJugadas = 12;
    private int victorias = 4;
    private int numAciertos = 35;
    private int numFallos = 8;

    private List<Logro> todosLosLogros;
    private Jugador jugadorActual;
    private List<String> todasLasTematicas;
    private List<String> tematicasDesbloqueadas;
    private GestorEstadisticas() {
        jugadorActual = new Jugador("MiUsuarioOculto");
        jugadorActual.setBalas(5000);
        todasLasTematicas = new ArrayList<>();
        todasLasTematicas.add("Español - Básico");
        todasLasTematicas.add("Inglés - Básico");
        todasLasTematicas.add("Deportes");
        todasLasTematicas.add("Cine y Series");
        todasLasTematicas.add("Videojuegos");

        tematicasDesbloqueadas = new ArrayList<>();
        tematicasDesbloqueadas.add("Español - Básico"); // Tienes esta
        tematicasDesbloqueadas.add("Videojuegos");      // Y esta

        todosLosLogros = new ArrayList<>();
        todosLosLogros.add(new Logro(1, "Primera Sangre", "Gana tu primera partida", "medalla", "victorias", 1, 100));
        todosLosLogros.add(new Logro(2, "Veterano", "Juega 50 partidas", "medalla", "partidas_jugadas", 50, 500));
        todosLosLogros.add(new Logro(3, "Imparable", "Gana 10 partidas", "medalla", "victorias", 10, 800));
        todosLosLogros.add(new Logro(4, "Francotirador", "Acierta 50 palabras", "logro", "num_aciertos", 50, 200));
        todosLosLogros.add(new Logro(5, "Panda Torpe", "Comete 10 fallos", "logro", "num_fallos", 10, 50));
        todosLosLogros.add(new Logro(6, "Aprendiz", "Juega 15 partidas", "logro", "partidas_jugadas", 15, 150));

        recalcularProgresos();
    }

    public static GestorEstadisticas getInstance() {
        if (instance == null) instance = new GestorEstadisticas();
        return instance;
    }

    public Jugador getJugadorActual() { return jugadorActual; }

    public void recalcularProgresos() {
        for (Logro l : todosLosLogros) {
            int progresoUsuario = 0;
            switch (l.getEstadisticaClave()) {
                case "victorias": progresoUsuario = victorias; break;
                case "partidas_jugadas": progresoUsuario = partidasJugadas; break;
                case "num_aciertos": progresoUsuario = numAciertos; break;
                case "num_fallos": progresoUsuario = numFallos; break;
            }
            if (progresoUsuario >= l.getValorObjetivo()) {
                l.setProgresoActual(l.getValorObjetivo());
                l.setCompletado(true);
            } else {
                l.setProgresoActual(progresoUsuario);
                l.setCompletado(false);
            }
        }
    }

    public List<Logro> getTodosLosLogros() { return todosLosLogros; }
    public int getPartidasJugadas() { return partidasJugadas; }
    public int getVictorias() { return victorias; }
    public int getNumAciertos() { return numAciertos; }
    public int getNumFallos() { return numFallos; }
    public List<String> getTodasLasTematicas() { return todasLasTematicas; }

        public boolean isTematicaDesbloqueada(String tema) {
        return tematicasDesbloqueadas.contains(tema);
    }
}