package com.example.secretpanda.ui.home.classification; // Asegúrate de que tu package está bien

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secretpanda.R;
import com.example.secretpanda.data.model.Jugador;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import ua.naiksoftware.stomp.StompClient;

public class ClasificacionActivity extends AppCompatActivity {

    private TextView tabAmigos, tabGlobal;
    private RecyclerView listaClasificacion;
    private ClasificacionAdapter adapter;

    // Nuestras dos listas separadas
    private List<Jugador> listaGlobalFalsa = new ArrayList<>();
    private List<Jugador> listaAmigosFalsa = new ArrayList<>();

    private StompClient stompClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Pantalla sin título superior
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_clasificacion);

        tabAmigos = findViewById(R.id.tab_amigos);
        tabGlobal = findViewById(R.id.tab_global);
        listaClasificacion = findViewById(R.id.lista_clasificacion);
        ImageView btnCerrar = findViewById(R.id.btn_cerrar_clasificacion);

        // 1. Configurar la lista (RecyclerView)
        listaClasificacion.setLayoutManager(new LinearLayoutManager(this));

        suscribirseRankingGlobalWS();
        suscribirseActualizacionesAmigos();
        // Generamos los datos falsos y los cargamos
        cargarRankingAmigos();
        adapter = new ClasificacionAdapter(listaAmigosFalsa); // Empezamos en Amigos
        listaClasificacion.setAdapter(adapter);

        // 2. Comportamiento de los botones
        tabAmigos.setOnClickListener(v -> {cargarRankingAmigos(); cambiarPestana(tabAmigos, tabGlobal, listaAmigosFalsa);});
        tabGlobal.setOnClickListener(v -> {cargarRankingGlobal(); cambiarPestana(tabGlobal, tabAmigos, listaGlobalFalsa);});

        // 3. Botón para salir
        btnCerrar.setOnClickListener(v -> finish()); // Cierra esta pantalla y vuelve al Home
    }

    // ==========================================
    // LÓGICA DE ANIMACIÓN DE LOS BOTONES
    // ==========================================
    private void cambiarPestana(TextView activa, TextView inactiva, List<Jugador> listaMostrar) {
        // Actualizamos los datos de la lista instantáneamente
        adapter.setListaJugadores(listaMostrar);

        // ¡ANIMACIÓN! Modificamos la altura fluidamente en 200 milisegundos
        animarAltura(activa, 40, 55);
        animarAltura(inactiva, 55, 40);
    }

    private void animarAltura(TextView vista, int altoInicialDp, int altoFinalDp) {
        // Convertimos DP a Pixeles porque Java trabaja en pixeles
        float density = getResources().getDisplayMetrics().density;
        int inicialPx = (int) (altoInicialDp * density);
        int finalPx = (int) (altoFinalDp * density);

        ValueAnimator animator = ValueAnimator.ofInt(inicialPx, finalPx);
        animator.setDuration(200); // 0.2 segundos
        animator.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = vista.getLayoutParams();
            params.height = (int) animation.getAnimatedValue();
            vista.setLayoutParams(params);
        });
        animator.start();
    }

    // ==========================================
    // GENERAR 10 JUGADORES HARDCODEADOS
    // ==========================================
    private void generarDatosFalsos() {
        // Creamos 10 jugadores con datos aleatorios
        Jugador j1 = crearJugador("NinjaMaster", 250);
        Jugador j2 = crearJugador("PandaRex", 210);
        Jugador j3 = crearJugador("ElEspiaSupremo", 185);
        Jugador j4 = crearJugador("TuUsuario", 150); // Simula el usuario actual
        Jugador j5 = crearJugador("PeleteLover", 120);
        Jugador j6 = crearJugador("GatoFurtivo", 95);
        Jugador j7 = crearJugador("ShadowKiller", 70);
        Jugador j8 = crearJugador("BambooHunter", 55);
        Jugador j9 = crearJugador("NoobPlayer123", 20);
        Jugador j10 = crearJugador("BuscandoPistas", 5);

        // Los metemos todos en la global
        listaGlobalFalsa.add(j1); listaGlobalFalsa.add(j2); listaGlobalFalsa.add(j3);
        listaGlobalFalsa.add(j4); listaGlobalFalsa.add(j5); listaGlobalFalsa.add(j6);
        listaGlobalFalsa.add(j7); listaGlobalFalsa.add(j8); listaGlobalFalsa.add(j9);
        listaGlobalFalsa.add(j10);

        // Elegimos unos pocos para que sean tus "amigos" (Tú tienes que estar)
        listaAmigosFalsa.add(j1); // NinjaMaster
        listaAmigosFalsa.add(j4); // Tú
        listaAmigosFalsa.add(j5); // PeleteLover
        listaAmigosFalsa.add(j8); // BambooHunter

        // Ordenamos ambas listas de mayor a menor victorias (por si acaso)
        listaGlobalFalsa.sort((a, b) -> Integer.compare(b.getVictorias(), a.getVictorias()));
        listaAmigosFalsa.sort((a, b) -> Integer.compare(b.getVictorias(), a.getVictorias()));
    }
    private void cargarRankingGlobal() {
        OkHttpClient client = new OkHttpClient();

        // Sustituye con tu IP de servidor (ej: http://10.0.2.2:8080 si usas emulador)
        String url = "http://10.0.2.2:8080/api/leaderboard/global";

        Request request = new Request.Builder()
                .url(url)
                .get() // Es una petición GET
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() -> {
                    android.util.Log.e("API_RANKING", "Error conectando: " + e.getMessage());
                    android.widget.Toast.makeText(ClasificacionActivity.this, "Error de red", android.widget.Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonData = response.body().string();
                    try {
                        org.json.JSONArray jsonArray = new org.json.JSONArray(jsonData);
                        List<Jugador> listaNueva = new java.util.ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            org.json.JSONObject obj = jsonArray.getJSONObject(i);

                            // Extraemos los datos según tu especificación
                            String tag = obj.getString("tag");
                            String foto = obj.optString("foto_perfil", "");
                            int victorias = obj.getInt("victorias");
                            // int aciertos = obj.getInt("num_aciertos"); // Por si lo usas luego

                            // Creamos el objeto Jugador (asumiendo que tiene estos campos o usando tu helper)
                            Jugador j = new Jugador(tag);
                            // IMPORTANTE: Asegúrate de que tu clase Jugador tenga estos setters
                            j.setVictorias(victorias);
                            j.setFotoPerfil(foto);

                            listaNueva.add(j);
                        }

                        // Actualizamos la interfaz en el hilo principal
                        runOnUiThread(() -> {
                            listaNueva.sort((a, b) -> Integer.compare(b.getVictorias(), a.getVictorias()));
                            listaGlobalFalsa = listaNueva; // Guardamos los datos reales
                            // Si estamos en la pestaña global, actualizamos el adapter
                            adapter.setListaJugadores(listaGlobalFalsa);
                        });

                    } catch (org.json.JSONException e) {
                        android.util.Log.e("API_RANKING", "Error parseando JSON", e);
                    }
                }
            }
        });
    }

    private void cargarRankingAmigos() {
        OkHttpClient client = new OkHttpClient();

        // Sustituye con tu IP de servidor (ej: http://10.0.2.2:8080 si usas emulador)
        String url = "http://10.0.2.2:8080/api/leaderboard/amigos";

        Request request = new Request.Builder()
                .url(url)
                .get() // Es una petición GET
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() -> {
                    android.util.Log.e("API_RANKING", "Error conectando: " + e.getMessage());
                    android.widget.Toast.makeText(ClasificacionActivity.this, "Error de red", android.widget.Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonData = response.body().string();
                    try {
                        org.json.JSONArray jsonArray = new org.json.JSONArray(jsonData);
                        List<Jugador> listaNueva = new java.util.ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            org.json.JSONObject obj = jsonArray.getJSONObject(i);

                            // Extraemos los datos según tu especificación
                            String tag = obj.getString("tag");
                            String foto = obj.optString("foto_perfil", "");
                            int victorias = obj.getInt("victorias");
                            // int aciertos = obj.getInt("num_aciertos"); // Por si lo usas luego

                            // Creamos el objeto Jugador (asumiendo que tiene estos campos o usando tu helper)
                            Jugador j = new Jugador(tag);
                            // IMPORTANTE: Asegúrate de que tu clase Jugador tenga estos setters
                            j.setVictorias(victorias);
                            j.setFotoPerfil(foto);

                            listaNueva.add(j);
                        }

                        // Actualizamos la interfaz en el hilo principal
                        runOnUiThread(() -> {
                            listaNueva.sort((a, b) -> Integer.compare(b.getVictorias(), a.getVictorias()));
                            listaAmigosFalsa = listaNueva; // Guardamos los datos reales
                            // Si estamos en la pestaña global, actualizamos el adapter
                            adapter.setListaJugadores(listaAmigosFalsa);
                        });

                    } catch (org.json.JSONException e) {
                        android.util.Log.e("API_RANKING", "Error parseando JSON", e);
                    }
                }
            }
        });
    }

    private void suscribirseActualizacionesAmigos() {
        // Verificamos que el cliente WebSocket esté listo
        if (stompClient == null || !stompClient.isConnected()) {
            android.util.Log.e("WS_RANKING", "El StompClient no está conectado");
            return;
        }

        // Nos suscribimos al canal personal del usuario
        stompClient.topic("/user/queue/leaderboard/amigos").subscribe(topicMessage -> {
            String jsonPayload = topicMessage.getPayload();

            try {
                org.json.JSONArray jsonArray = new org.json.JSONArray(jsonPayload);
                List<Jugador> listaActualizada = new java.util.ArrayList<>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    org.json.JSONObject obj = jsonArray.getJSONObject(i);

                    String tag = obj.getString("tag");
                    String foto = obj.optString("foto_perfil", "");
                    int victorias = obj.getInt("victorias");
                    int numAciertos = obj.getInt("num_aciertos");

                    Jugador j = new Jugador(tag);
                    j.setVictorias(victorias);
                    j.setFotoPerfil(foto);
                    j.setNumAciertos(numAciertos);

                    listaActualizada.add(j);
                }

                // Actualizamos la interfaz en el hilo principal
                runOnUiThread(() -> {
                    // Actualizamos nuestra variable de datos
                    listaActualizada.sort((a, b) -> Integer.compare(b.getVictorias(), a.getVictorias()));
                    listaAmigosFalsa = listaActualizada;
                    adapter.setListaJugadores(listaAmigosFalsa);
                });

            } catch (org.json.JSONException e) {
                android.util.Log.e("WS_RANKING", "Error parseando la actualización en tiempo real", e);
            }
        }, throwable -> {
            android.util.Log.e("WS_RANKING", "Error en la suscripción WebSocket de amigos", throwable);
        });
    }

    private void suscribirseRankingGlobalWS() {
        if (stompClient == null || !stompClient.isConnected()) {
            Log.e("WS_GLOBAL", "StompClient no conectado, no se puede suscribir al ranking global");
            return;
        }

        // Suscripción al tópico público de la clasificación global
        stompClient.topic("/topic/leaderboard/global").subscribe(topicMessage -> {
            String jsonPayload = topicMessage.getPayload();

            try {
                org.json.JSONArray jsonArray = new org.json.JSONArray(jsonPayload);
                List<Jugador> listaNuevaGlobal = new java.util.ArrayList<>();

                // Procesamos el Top 10 que envía el servidor
                for (int i = 0; i < jsonArray.length(); i++) {
                    org.json.JSONObject obj = jsonArray.getJSONObject(i);

                    String tag = obj.getString("tag");
                    String foto = obj.optString("foto_perfil", "");
                    int victorias = obj.getInt("victorias");
                    int numAciertos = obj.getInt("num_aciertos");

                    Jugador j = new Jugador(tag);
                    j.setVictorias(victorias);
                    j.setFotoPerfil(foto);
                    j.setNumAciertos(numAciertos);

                    listaNuevaGlobal.add(j);
                }

                // Actualizamos la UI
                runOnUiThread(() -> {
                    listaNuevaGlobal.sort((a, b) -> Integer.compare(b.getVictorias(), a.getVictorias()));
                    listaGlobalFalsa = listaNuevaGlobal;
                    adapter.setListaJugadores(listaGlobalFalsa);

                });

            } catch (org.json.JSONException e) {
                Log.e("WS_GLOBAL", "Error al parsear el ranking global", e);
            }
        }, throwable -> {
            Log.e("WS_GLOBAL", "Error en la suscripción al ranking global", throwable);
        });
    }
    // Pequeño ayudante para no escribir tanto
    private Jugador crearJugador(String nombre, int victorias) {
        Jugador j = new Jugador(nombre);
        // Simulamos sumarle las victorias (Como tu clase Jugador no tiene setVictorias, usamos un bucle o creamos el setter.
        // ¡Ojo! Si no tienes setVictorias en Jugador.java, lo añadiremos luego. Por ahora asumo que tienes un método así:
        for(int i=0; i<victorias; i++) j.sumarVictoria();
        return j;
    }


    public static class SolicitudAdapter {
    }
}