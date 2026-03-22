package com.example.secretpanda.ui.game.createMatch;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.secretpanda.R;
import com.example.secretpanda.ui.game.join.TematicasDialogFragment;
import com.example.secretpanda.ui.game.waitingRoom.SalaEsperaActivity;

public class ConfiguracionMisionActivity extends AppCompatActivity {

    private boolean esPrivada;

    // Variables para guardar lo que ha seleccionado el usuario
    private String tematicaSeleccionada = "";
    private int tiempoSeleccionado = 60; // Por defecto
    private int jugadoresSeleccionados = 8; // Por defecto

    private TextView txtTematica;
    private TextView[] botonesTiempo;
    private TextView[] botonesJugadores;

    private java.util.Map<String, Integer> misTematicasDisponibles = new java.util.HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_configuracion_mision);

        esPrivada = getIntent().getBooleanExtra("ES_PRIVADA", false);

        // Si es privada, le ponemos el texto al título
        TextView txtTitulo = findViewById(R.id.txt_titulo_config);
        if (esPrivada) txtTitulo.setText("Configurar misión privada");
        else txtTitulo.setText("Configurar misión pública");

        FrameLayout btnVolver = findViewById(R.id.btn_volver_home_config);
        btnVolver.setOnClickListener(v -> finish());

        LinearLayout btnTematica = findViewById(R.id.btn_desplegable_tematica_crear);
        txtTematica = findViewById(R.id.txt_tematica_elegida_crear);
        btnTematica.setOnClickListener(v -> mostrarDialogoTematicas());

        botonesTiempo = new TextView[]{
                findViewById(R.id.btn_tiempo_30),
                findViewById(R.id.btn_tiempo_60),
                findViewById(R.id.btn_tiempo_90),
                findViewById(R.id.btn_tiempo_120)
        };
        configurarBotonesSeleccion(botonesTiempo, new int[]{30, 60, 90, 120}, true);
        seleccionarBoton(botonesTiempo[1]); // Selecciona 60s por defecto

        botonesJugadores = new TextView[]{
                findViewById(R.id.btn_jug_4),
                findViewById(R.id.btn_jug_6),
                findViewById(R.id.btn_jug_8),
                findViewById(R.id.btn_jug_10)
        };
        configurarBotonesSeleccion(botonesJugadores, new int[]{4, 6, 8, 10}, false);
        seleccionarBoton(botonesJugadores[2]); // Selecciona 8 por defecto

        TextView btnCrear = findViewById(R.id.btn_crear_mision_final);
        btnCrear.setOnClickListener(v -> {
            // 1. Validar que haya elegido una temática
            if (tematicaSeleccionada == null || tematicaSeleccionada.isEmpty()) {
                Toast.makeText(this, "Por favor, selecciona una temática primero", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. Mapear el nombre de la temática a su ID de la Base de Datos
            Integer idTema = misTematicasDisponibles.get(tematicaSeleccionada);
            if (idTema == null) {
                idTema = 1; // Un valor por defecto por si ocurre algún error
            }
            String miTagReal = "MiNombreDeUsuario";

            // 3. Crear el JSON basándonos en tu CrearPartidaDTO
            org.json.JSONObject jsonBody = new org.json.JSONObject();
            try {
                jsonBody.put("id_tema", idTema);
                jsonBody.put("tiempo_espera", tiempoSeleccionado);
                jsonBody.put("max_jugadores", jugadoresSeleccionados);
                jsonBody.put("es_publica", !esPrivada);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 4. Preparar la petición OkHttp
            okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
            String url = "http://10.0.2.2:8080/api/partidas"; // Endpoint de tu backend

            com.example.secretpanda.data.TokenManager tokenManager = new com.example.secretpanda.data.TokenManager(this);
            String token = tokenManager.getToken();

            okhttp3.RequestBody body = okhttp3.RequestBody.create(
                    jsonBody.toString(),
                    okhttp3.MediaType.parse("application/json")
            );

            okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder()
                    .url(url)
                    .post(body);

            // IMPORTANTE: Enviar el Token JWT para que el servidor sepa quién la crea
            if (token != null && !token.isEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer " + token);
            }

            okhttp3.Request request = requestBuilder.build();

            // 5. Hacer la llamada al servidor
            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, java.io.IOException e) {
                    runOnUiThread(() -> Toast.makeText(ConfiguracionMisionActivity.this, "Error de red al crear partida", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                    // Leemos el cuerpo de la respuesta (tanto si es éxito como si es error)
                    String cuerpoRespuesta = response.body() != null ? response.body().string() : "Sin cuerpo";

                    if (response.isSuccessful()) {
                        // ¡EL SERVIDOR HA GUARDADO LA PARTIDA!
                        runOnUiThread(() -> {
                            try {
                                org.json.JSONObject res = new org.json.JSONObject(cuerpoRespuesta);

                                int idPartidaCreada = res.optInt("id_partida", res.optInt("idPartida", -1));
                                String codigoPartida = res.optString("codigo_partida", res.optString("codigoPartida", ""));

                                Intent intent = new Intent(ConfiguracionMisionActivity.this, com.example.secretpanda.ui.game.waitingRoom.SalaEsperaActivity.class);
                                intent.putExtra("ES_PRIVADA", esPrivada);
                                intent.putExtra("TEMATICA", tematicaSeleccionada);
                                intent.putExtra("TIEMPO", tiempoSeleccionado);
                                intent.putExtra("JUGADORES", jugadoresSeleccionados);
                                intent.putExtra("ID_PARTIDA", idPartidaCreada);
                                intent.putExtra("CODIGO_PARTIDA", codigoPartida);
                                intent.putExtra("ES_LIDER", true);
                                intent.putExtra("MI_NOMBRE_USUARIO", miTagReal);
                                startActivity(intent);
                                finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(ConfiguracionMisionActivity.this, "Error procesando la respuesta", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {

                        android.util.Log.e("API_ERROR_500", "Código: " + response.code());
                        android.util.Log.e("API_ERROR_500", "Motivo exacto del backend: " + cuerpoRespuesta);

                        runOnUiThread(() -> Toast.makeText(ConfiguracionMisionActivity.this, "Error del servidor. Mira el Logcat", Toast.LENGTH_LONG).show());
                    }
                }
            });
        });
        obtenerMisTematicas();
    }

    private void mostrarDialogoTematicas() {
        TematicasDialogFragment dialog = new TematicasDialogFragment();

        // Le decimos al diálogo que NO muestre el botón de "Todas" porque estamos creando
        dialog.setMostrarOpcionTodas(false);


        dialog.setTematicaListener(tematica -> {
            tematicaSeleccionada = tematica;
            txtTematica.setText(tematicaSeleccionada);
        });

        dialog.show(getSupportFragmentManager(), "TematicasDialogConfig");
    }

    // Método ayudante para que los botones actúen como "Radio Buttons" visuales
    private void configurarBotonesSeleccion(TextView[] botones, int[] valores, boolean esTiempo) {
        for (int i = 0; i < botones.length; i++) {
            final int index = i;
            botones[i].setOnClickListener(v -> {
                for (TextView btn : botones) {
                    btn.setBackgroundResource(R.drawable.fondo_btn_unirse_pequeno); // Tu fondo gris
                }
                seleccionarBoton(botones[index]);

                if (esTiempo) tiempoSeleccionado = valores[index];
                else jugadoresSeleccionados = valores[index];

            });
        }
    }

    private void seleccionarBoton(TextView boton) {
        boton.setBackgroundResource(R.drawable.fondo_verde_seleccion_personalizacion);
    }


    // API: GET TEMAS DEL JUGADOR
    private void obtenerMisTematicas() {
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        String url = "http://10.0.2.2:8080/api/jugadores/temas"; // URL de la API

        com.example.secretpanda.data.TokenManager tokenManager = new com.example.secretpanda.data.TokenManager(this);
        String token = tokenManager.getToken();

        okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder()
                .url(url)
                .get();

        if (token != null && !token.isEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer " + token);
        }

        client.newCall(requestBuilder.build()).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() -> Toast.makeText(ConfiguracionMisionActivity.this, "Error de red al cargar temas", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonRespuesta = response.body().string();
                        org.json.JSONArray temasArray = new org.json.JSONArray(jsonRespuesta);

                        misTematicasDisponibles.clear();

                        // Leemos el array del backend y lo metemos en nuestro mapa
                        for (int i = 0; i < temasArray.length(); i++) {
                            org.json.JSONObject temaJson = temasArray.getJSONObject(i);

                            // Aceptamos camelCase o snake_case por si acaso
                            int idTema = temaJson.optInt("id_tema", temaJson.optInt("idTema", -1));
                            String nombre = temaJson.optString("nombre", "");

                            if (idTema != -1 && !nombre.isEmpty()) {
                                misTematicasDisponibles.put(nombre, idTema);
                            }
                        }

                        // Auto-seleccionar el primer tema que nos llegue
                        runOnUiThread(() -> {
                            if (!misTematicasDisponibles.isEmpty() && tematicaSeleccionada.isEmpty()) {
                                tematicaSeleccionada = misTematicasDisponibles.keySet().iterator().next();
                                txtTematica.setText(tematicaSeleccionada);
                            }
                        });

                    } catch (Exception e) {
                        android.util.Log.e("API_TEMAS", "Error procesando JSON de temas", e);
                    }
                } else {
                    android.util.Log.e("API_TEMAS", "Error del backend: " + response.code());
                }
            }
        });
    }
}