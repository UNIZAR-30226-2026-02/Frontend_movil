package com.example.secretpanda.ui.game.join;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secretpanda.R;
import com.example.secretpanda.data.NetworkConfig;
import com.example.secretpanda.data.model.Partida;
import com.example.secretpanda.data.TokenManager; // Asegúrate de que este import apunte a tu TokenManager correcto
import com.example.secretpanda.ui.audio.EfectosManager;
import com.example.secretpanda.ui.game.match.PartidaAdapter;
import com.example.secretpanda.ui.game.waitingRoom.SalaEsperaActivity;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ua.naiksoftware.stomp.StompClient;

public class MisionPublicaActivity extends AppCompatActivity {

    private RecyclerView recyclerMisiones;
    private FrameLayout btnCerrar;
    private LinearLayout btnSelectorTematicas;
    private TextView tvTematicaActual;

    private PartidaAdapter adapter;
    private List<Partida> listaPartidasTodas;
    private List<Partida> listaPartidasFiltradas;
    private StompClient mStompClient;
    private io.reactivex.disposables.CompositeDisposable compositeDisposable = new io.reactivex.disposables.CompositeDisposable();
    private String tematicaFiltroActual = "Todas las temáticas";
    private List<String> misTemasAdquiridos = new ArrayList<>();

    private String nombreUsuario;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        // Cuidado aquí: asegúrate de que tu XML se llama así. Antes pusiste "activity_misiones_publicas"
        setContentView(R.layout.activity_misiones_publicas);

        recyclerMisiones = findViewById(R.id.rv_misiones);
        btnCerrar = findViewById(R.id.btn_volver_home);
        btnSelectorTematicas = findViewById(R.id.btn_selector_tematicas);
        tvTematicaActual = findViewById(R.id.tv_tematica_actual);

        nombreUsuario = getIntent().getStringExtra("MI_NOMBRE_USUARIO");

        if (btnCerrar != null) btnCerrar.setOnClickListener(v -> {
            EfectosManager.reproducir(getApplicationContext(), R.raw.sonido_click);
            finish();
        });
        if (btnSelectorTematicas != null) btnSelectorTematicas.setOnClickListener(v -> {
            EfectosManager.reproducir(getApplicationContext(), R.raw.sonido_click);
            mostrarDialogoFiltro();
        });

        // 1. ¡SUPER IMPORTANTE! Inicializamos las listas ANTES de pedir datos al servidor
        listaPartidasTodas = new ArrayList<>();
        listaPartidasFiltradas = new ArrayList<>();
        obtenerTemasDelJugador();
        // 2. Inicializamos el Adapter configurando qué pasa al hacer clic en una partida
        adapter = new PartidaAdapter(listaPartidasFiltradas, misTemasAdquiridos, partida -> {
            if (partida.isLlena()) {
                mostrarDialogoError(partida);
            } else {
                int idPartidaClicada = partida.getIdPartida();

                // 1. PREPARAMOS LA LLAMADA AL SERVIDOR PARA UNIRNOS
                OkHttpClient client = new OkHttpClient();
                String url = NetworkConfig.BASE_URL + "/partidas/" + idPartidaClicada + "/unirse/publica";

                TokenManager tokenManager = new TokenManager(this);
                String token = tokenManager.getToken();

                // Mandamos un JSON vacío porque tu backend acepta dto nulo o vacío para partidas públicas
                okhttp3.RequestBody body = okhttp3.RequestBody.create("{}", okhttp3.MediaType.parse("application/json"));

                Request.Builder requestBuilder = new Request.Builder()
                        .url(url)
                        .post(body);

                if (token != null && !token.isEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer " + token);
                }

                Request request = requestBuilder.build();

                // 2. HACEMOS LA PETICIÓN EN SEGUNDO PLANO
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("API_ERROR", "Error de red al intentar unirse", e);
                        runOnUiThread(() -> android.widget.Toast.makeText(MisionPublicaActivity.this, "Error de conexión", android.widget.Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        // IMPORTANTE: Guardamos el cuerpo de la respuesta. (Solo se puede leer .string() una vez)
                        String jsonRespuesta = response.body() != null ? response.body().string() : "";

                        // Si da 200 (éxito) O da 409 (ya estabas dentro), te dejamos pasar
                        if (response.isSuccessful() || response.code() == 409) {

                            // 🕵️‍♂️ Intentamos leer qué equipo nos asignó el backend
                            String equipoAsignado = "rojo"; // Valor de emergencia por defecto
                            try {
                                if (!jsonRespuesta.isEmpty()) {
                                    org.json.JSONObject jsonObject = new org.json.JSONObject(jsonRespuesta);
                                    // OJO: Pon aquí exactamente cómo se llama la variable de equipo que te devuelve tu backend
                                    if (jsonObject.has("equipo")) {
                                        equipoAsignado = jsonObject.getString("equipo");
                                    }
                                }
                            } catch (Exception e) {
                                Log.e("WS_PARTIDAS", "No se pudo extraer el equipo del JSON. Usando defecto.", e);
                            }

                            // Congelamos la variable para poder usarla dentro del hilo principal
                            final String miEquipoFinal = equipoAsignado;

                            runOnUiThread(() -> {
                                Intent intent = new Intent(MisionPublicaActivity.this, SalaEsperaActivity.class);
                                intent.putExtra("ID_PARTIDA", idPartidaClicada);
                                intent.putExtra("MI_EQUIPO", miEquipoFinal); // ¡Usamos el equipo real del servidor!
                                intent.putExtra("ES_LIDER", false); // Como nos unimos a partida ajena, no somos líder
                                intent.putExtra("ES_PRIVADA", false);
                                intent.putExtra("MAX_JUGADORES", partida.getMaxJugadores());
                                intent.putExtra("TIEMPO_TURNO", partida.getTiempo());
                                intent.putExtra("MI_NOMBRE_USUARIO", nombreUsuario);
                                startActivity(intent);
                            });
                        } else {
                            // Otros errores
                            Log.e("API_ERROR", "El servidor rechazó la entrada. Código: " + response.code() + " Cuerpo: " + jsonRespuesta);
                            runOnUiThread(() -> android.widget.Toast.makeText(MisionPublicaActivity.this, "No se pudo entrar a la sala", android.widget.Toast.LENGTH_SHORT).show());
                        }
                    }
                });
            }
        });

        // 3. Enganchamos el Adapter al RecyclerView
        if (recyclerMisiones != null) {
            recyclerMisiones.setLayoutManager(new LinearLayoutManager(this));
            recyclerMisiones.setAdapter(adapter);
        }


        obtenerPartidasHTTP();
        conectarWebSocketPartidas();
    }

    // --- LÓGICA DE INTERFAZ Y DIÁLOGOS ---

    private void mostrarDialogoError(Partida partida) {
        Dialog dialogError = new Dialog(MisionPublicaActivity.this);
        dialogError.setContentView(R.layout.dialog_error_mision);

        if (dialogError.getWindow() != null) {
            dialogError.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        }

        TextView txtTitulo = dialogError.findViewById(R.id.txt_titulo_error);
        TextView txtMensaje = dialogError.findViewById(R.id.txt_mensaje_error);

        if(partida.isLlena()) {
            if (txtTitulo != null) txtTitulo.setText("Sala Llena");
            if (txtMensaje != null) txtMensaje.setText("La partida ya tiene " + partida.getJugadoresTexto() + " jugadores.");
        }

        dialogError.findViewById(R.id.btn_cerrar_dialogo).setOnClickListener(viewCerrar -> dialogError.dismiss());
        dialogError.show();
    }

    private void mostrarDialogoFiltro() {
        TematicasDialogFragment dialog = new TematicasDialogFragment();

        // 🚩 ¡LA LÍNEA MÁGICA! Le pasamos la lista de temas al diálogo antes de abrirlo.
        // Asegúrate de añadir "Todas las temáticas" al principio para que puedan quitar el filtro
        List<String> temasParaElDialogo = new ArrayList<>();
        temasParaElDialogo.add("Todas las temáticas");
        temasParaElDialogo.addAll(misTemasAdquiridos);

        // *NOTA: Necesitas tener un método en tu TematicasDialogFragment para recibir esto
        dialog.setMisTematicas(temasParaElDialogo);

        dialog.setTematicaListener(tematica -> {
            EfectosManager.reproducir(getApplicationContext(), R.raw.sonido_click);
            tematicaFiltroActual = tematica;
            if (tvTematicaActual != null) {
                tvTematicaActual.setText(tematicaFiltroActual);
            }
            // Llamamos al filtro unificado
            filtrarMisionesPorTematica(tematicaFiltroActual);
        });
        dialog.show(getSupportFragmentManager(), "TematicasDialog");
    }

    // --- LÓGICA DE DATOS Y RED ---

    private void filtrarMisionesPorTematica(String tematica) {
        listaPartidasFiltradas.clear();

        // 1. Si el filtro es nulo o es "Todas", metemos todas las partidas
        if (tematica == null || tematica.equals("Todas las temáticas") || tematica.equals("TODAS")) {
            listaPartidasFiltradas.addAll(listaPartidasTodas);
        } else {
            // 2. Si hay una temática específica, buscamos las que coincidan
            for (Partida partida : listaPartidasTodas) {
                // Cuidado con los nulos aquí
                if (partida.getTematica() != null && partida.getTematica().equalsIgnoreCase(tematica)) {
                    listaPartidasFiltradas.add(partida);
                }
            }
        }

        // 3. Avisamos al adaptador
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        // 🚩 CHIVATO PARA EL LOGCAT: Nos dirá si el filtro funciona
        Log.d("WS_PARTIDAS", "Filtro: " + tematica + " | Partidas a mostrar: " + listaPartidasFiltradas.size());
    }

    private void conectarWebSocketPartidas() {
        // 1. Configurar el cliente Stomp
        // Importante: "/ws/websocket" debe coincidir con tu registerStompEndpoints en Spring Boot
        mStompClient = ua.naiksoftware.stomp.Stomp.over(
                ua.naiksoftware.stomp.Stomp.ConnectionProvider.OKHTTP,
                NetworkConfig.WS_URL
        );

        // 2. Obtener el Token usando la Opción B (la que te funcionó)
        String token = new com.example.secretpanda.data.TokenManager(this).getToken();

        // 3. Preparar las cabeceras de seguridad
        java.util.List<ua.naiksoftware.stomp.dto.StompHeader> headers = new java.util.ArrayList<>();
        headers.add(new ua.naiksoftware.stomp.dto.StompHeader("Authorization", "Bearer " + token));

        // 4. Conectar al servidor
        mStompClient.connect(headers);

        // 5. Suscribirse al canal de partidas
        io.reactivex.disposables.Disposable disposable = mStompClient.topic("/topic/partidas/publicas")
                .subscribeOn(io.reactivex.schedulers.Schedulers.io())
                .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe(topicMessage -> {
                    Log.d("WS_PARTIDAS", "Datos recibidos por el radar: " + topicMessage.getPayload());

                    // Usamos GSON para convertir el mensaje en una lista de nuestras Partidas
                    Gson gson = new Gson();
                    java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<Partida>>(){}.getType();
                    List<Partida> nuevasPartidas = gson.fromJson(topicMessage.getPayload(), listType);

                    // Actualizar la interfaz
                    if (nuevasPartidas != null) {
                        listaPartidasTodas.clear();
                        listaPartidasTodas.addAll(nuevasPartidas);
                        filtrarMisionesPorTematica(tematicaFiltroActual);
                    }

                }, throwable -> {
                    Log.e("WS_PARTIDAS", "Error en la recepción de datos", throwable);
                });

        compositeDisposable.add(disposable);
    }

    private void obtenerTemasDelJugador() {
        // 🚩 LOG DE CONTROL: Para saber si el método llega a arrancar
        Log.d("API_TEMAS", "🚀 Misión iniciada: Intentando obtener temas del servidor...");

        OkHttpClient client = new OkHttpClient();

        // 🔑 Usamos la Opción B que te funcionó:
        String token = new com.example.secretpanda.data.TokenManager(this).getToken();

        Request request = new Request.Builder()
                .url(NetworkConfig.BASE_URL + "/jugadores/temas") // Verifica que esta ruta sea correcta
                .header("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("API_TEMAS", "❌ FALLO TOTAL de red: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("API_TEMAS", "📡 Respuesta recibida. Código: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    String jsonRespuesta = response.body().string();
                    Log.d("API_TEMAS", "📦 JSON de temas: " + jsonRespuesta);

                    try {
                        org.json.JSONArray jsonArray = new org.json.JSONArray(jsonRespuesta);
                        misTemasAdquiridos.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            org.json.JSONObject temaJson = jsonArray.getJSONObject(i);
                            misTemasAdquiridos.add(temaJson.getString("nombre"));
                        }

                        // IMPORTANTE: Refrescar la interfaz en el hilo principal
                        runOnUiThread(() -> {
                            Log.d("API_TEMAS", "✅ Temas cargados: " + misTemasAdquiridos.size());
                            // Aquí podrías llamar a un método para actualizar tu Spinner o Selector
                        });

                    } catch (Exception e) {
                        Log.e("API_TEMAS", "🤯 Error procesando el JSON", e);
                    }
                } else {
                    Log.e("API_TEMAS", "⚠️ El servidor respondió pero con error: " + response.code());
                }
            }
        });
    }

    private void obtenerPartidasHTTP() {
        Log.d("WS_PARTIDAS", "📡 Iniciando escaneo HTTP de partidas existentes...");
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        String token = new com.example.secretpanda.data.TokenManager(this).getToken();

        // Llama al @GetMapping("/publicas") que creamos ayer en el servidor
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(NetworkConfig.BASE_URL + "/partidas/publicas")
                .header("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                Log.e("WS_PARTIDAS", "❌ Fallo en el escaneo inicial: " + e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonRespuesta = response.body().string();
                    Log.d("WS_PARTIDAS", "📦 JSON Partidas Iniciales: " + jsonRespuesta);

                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<Partida>>(){}.getType();
                    List<Partida> partidasIniciales = gson.fromJson(jsonRespuesta, listType);

                    // Volvemos a la interfaz para enseñarlas
                    runOnUiThread(() -> {
                        if (partidasIniciales != null) {
                            listaPartidasTodas.clear();
                            listaPartidasTodas.addAll(partidasIniciales);
                            // ¡Y llamamos al filtro para que las dibuje!
                            filtrarMisionesPorTematica(tematicaFiltroActual);
                        }
                    });
                } else {
                    Log.e("WS_PARTIDAS", "⚠️ Error del servidor en el escaneo inicial: " + response.code());
                }
            }
        });
    }
    @Override
    protected void onDestroy() {
        if (mStompClient != null) mStompClient.disconnect();
        if (compositeDisposable != null) compositeDisposable.dispose();
        super.onDestroy();
    }
}