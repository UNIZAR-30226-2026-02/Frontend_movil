package com.example.secretpanda.ui.home.classification;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secretpanda.R;
import com.example.secretpanda.data.TokenManager;
import com.example.secretpanda.data.model.Jugador;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;

public class ClasificacionActivity extends AppCompatActivity {

    private TextView tabAmigos, tabGlobal;
    private RecyclerView listaClasificacion;
    private ClasificacionAdapter adapter;

    // Listas para guardar los datos recibidos del servidor
    private List<Jugador> listaGlobalActual = new ArrayList<>();
    private List<Jugador> listaAmigosActual = new ArrayList<>();

    private StompClient stompClient;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_clasificacion);

        tokenManager = new TokenManager(this);

        tabAmigos = findViewById(R.id.tab_amigos);
        tabGlobal = findViewById(R.id.tab_global);
        listaClasificacion = findViewById(R.id.lista_clasificacion);
        ImageView btnCerrar = findViewById(R.id.btn_cerrar_clasificacion);

        listaClasificacion.setLayoutManager(new LinearLayoutManager(this));

        // Empezamos con una lista vacía
        adapter = new ClasificacionAdapter(new ArrayList<>());
        listaClasificacion.setAdapter(adapter);

        // Inicializamos los WebSockets
        conectarWebSocket();

        // Cargamos los datos iniciales por REST (Cargamos global por defecto)
        cargarRankingAmigos(); // Refrescamos datos al pulsar

        // Configuramos la UI para que 'Global' parezca activa por defecto si queremos
        // cambiarPestana(tabGlobal, tabAmigos, listaGlobalActual);

        // Eventos de las pestañas
        tabAmigos.setOnClickListener(v -> {
            cargarRankingAmigos(); // Refrescamos datos al pulsar
            cambiarPestana(tabAmigos, tabGlobal, listaAmigosActual);
        });

        tabGlobal.setOnClickListener(v -> {
            cargarRankingGlobal(); // Refrescamos datos al pulsar
            cambiarPestana(tabGlobal, tabAmigos, listaGlobalActual);
        });

        btnCerrar.setOnClickListener(v -> finish());
    }


    // LÓGICA DE WEBSOCKETS

    private void conectarWebSocket() {
        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) return;

        List<StompHeader> cabeceras = new ArrayList<>();
        cabeceras.add(new StompHeader("Authorization", "Bearer " + token));

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://10.0.2.2:8080/ws/websocket");

        stompClient.lifecycle().subscribe(lifecycleEvent -> {
            switch (lifecycleEvent.getType()) {
                case OPENED:
                    Log.d("WS_RANKING", "Conexión STOMP establecida");
                    // Una vez conectados, nos suscribimos a los canales
                    suscribirseRankingGlobalWS();
                    suscribirseActualizacionesAmigos();
                    break;
                case ERROR:
                    Log.e("WS_RANKING", "Error conexión STOMP", lifecycleEvent.getException());
                    break;
                case CLOSED:
                    Log.d("WS_RANKING", "Conexión STOMP cerrada");
                    break;
            }
        });

        stompClient.connect(cabeceras);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (stompClient != null && stompClient.isConnected()) {
            stompClient.disconnect();
        }
    }



    // LÓGICA DE ANIMACIÓN

    private void cambiarPestana(TextView activa, TextView inactiva, List<Jugador> listaMostrar) {
        adapter.setListaJugadores(listaMostrar);
        animarAltura(activa, 40, 55);
        animarAltura(inactiva, 55, 40);
    }

    private void animarAltura(TextView vista, int altoInicialDp, int altoFinalDp) {
        float density = getResources().getDisplayMetrics().density;
        int inicialPx = (int) (altoInicialDp * density);
        int finalPx = (int) (altoFinalDp * density);

        ValueAnimator animator = ValueAnimator.ofInt(inicialPx, finalPx);
        animator.setDuration(200);
        animator.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = vista.getLayoutParams();
            params.height = (int) animation.getAnimatedValue();
            vista.setLayoutParams(params);
        });
        animator.start();
    }

    // LLAMADAS REST AL BACKEND
    private void cargarRankingGlobal() {
        OkHttpClient client = new OkHttpClient();
        String url = "http://10.0.2.2:8080/api/leaderboard/global";

        String token = tokenManager.getToken();
        if (token == null) return;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Log.e("API_RANKING", "Error conectando global: " + e.getMessage());
                    Toast.makeText(ClasificacionActivity.this, "Error cargando ranking global", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonData = response.body().string();
                    try {
                        JSONArray jsonArray = new JSONArray(jsonData);
                        List<Jugador> listaNueva = procesarJsonRanking(jsonArray);

                        runOnUiThread(() -> {
                            listaGlobalActual = listaNueva;
                            // Actualizamos el adapter solo si estamos en esta pestaña (si el botón Global es más alto)
                            adapter.setListaJugadores(listaGlobalActual);
                        });

                    } catch (JSONException e) {
                        Log.e("API_RANKING", "Error parseando JSON global", e);
                    }
                }
            }
        });
    }

    private void cargarRankingAmigos() {
        OkHttpClient client = new OkHttpClient();
        String url = "http://10.0.2.2:8080/api/leaderboard/amigos";

        String token = tokenManager.getToken();
        if (token == null) return;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Log.e("API_RANKING", "Error conectando amigos: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonData = response.body().string();
                    try {
                        JSONArray jsonArray = new JSONArray(jsonData);
                        List<Jugador> listaNueva = procesarJsonRanking(jsonArray);

                        runOnUiThread(() -> {
                            listaAmigosActual = listaNueva;
                            adapter.setListaJugadores(listaAmigosActual);
                        });

                    } catch (JSONException e) {
                        Log.e("API_RANKING", "Error parseando JSON amigos", e);
                    }
                }
            }
        });
    }

    // Método auxiliar para no repetir código de parseo
    private List<Jugador> procesarJsonRanking(JSONArray jsonArray) throws JSONException {
        List<Jugador> lista = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            String tag = obj.getString("tag");
            String foto = obj.optString("foto_perfil", "");
            int victorias = obj.optInt("victorias", 0);
            int aciertos = obj.optInt("num_aciertos", 0);

            Jugador j = new Jugador(tag);
            j.setVictorias(victorias);
            j.setFotoPerfil(foto);
            j.setNumAciertos(aciertos);
            lista.add(j);
        }
        // Ordenamos por victorias descendente
        lista.sort((a, b) -> Integer.compare(b.getVictorias(), a.getVictorias()));
        return lista;
    }


    // SUSCRIPCIONES WEBSOCKET
    private void suscribirseRankingGlobalWS() {
        stompClient.topic("/topic/leaderboard/global").subscribe(topicMessage -> {
            String jsonPayload = topicMessage.getPayload();
            try {
                JSONArray jsonArray = new JSONArray(jsonPayload);
                List<Jugador> listaActualizada = procesarJsonRanking(jsonArray);

                runOnUiThread(() -> {
                    listaGlobalActual = listaActualizada;
                    // Solo actualizamos la vista si estamos en la pestaña correcta

                    if (tabGlobal.getLayoutParams().height > 100) { // usa un valor seguro o un boolean state
                        adapter.setListaJugadores(listaGlobalActual);
                    }
                });

            } catch (JSONException e) {
                Log.e("WS_GLOBAL", "Error parseo global", e);
            }
        }, throwable -> Log.e("WS_GLOBAL", "Error sub global", throwable));
    }

    private void suscribirseActualizacionesAmigos() {
        stompClient.topic("/user/queue/leaderboard/amigos").subscribe(topicMessage -> {
            String jsonPayload = topicMessage.getPayload();
            try {
                JSONArray jsonArray = new JSONArray(jsonPayload);
                List<Jugador> listaActualizada = procesarJsonRanking(jsonArray);

                runOnUiThread(() -> {
                    listaAmigosActual = listaActualizada;
                    // Solo actualizamos la vista si estamos en la pestaña correcta
                    if (tabAmigos.getLayoutParams().height > 100) {
                        adapter.setListaJugadores(listaAmigosActual);
                    }
                });

            } catch (JSONException e) {
                Log.e("WS_AMIGOS", "Error parseo amigos WS", e);
            }
        }, throwable -> Log.e("WS_AMIGOS", "Error sub amigos", throwable));
    }
}