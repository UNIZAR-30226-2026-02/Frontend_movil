package com.example.secretpanda.ui.game.createMatch;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.secretpanda.R;
import com.example.secretpanda.ui.game.join.TematicasDialogFragment;
import com.example.secretpanda.ui.game.waitingRoom.SalaEsperaActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ConfiguracionMisionActivity extends AppCompatActivity {

    private boolean esPrivada;
    private String tematicaSeleccionada = "";
    private int tiempoSeleccionado = 60;
    private int jugadoresSeleccionados = 8;
    private String nombreUsuario;

    private TextView txtTematica;
    private TextView[] botonesTiempo, botonesJugadores;
    private Map<String, Integer> misTematicasDisponibles = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_configuracion_mision);

        esPrivada = getIntent().getBooleanExtra("ES_PRIVADA", false);
        nombreUsuario = getIntent().getStringExtra("MI_NOMBRE_USUARIO");

        TextView txtTitulo = findViewById(R.id.txt_titulo_config);
        if (txtTitulo != null) txtTitulo.setText(esPrivada ? "Configurar misión privada" : "Configurar misión pública");

        findViewById(R.id.btn_volver_home_config).setOnClickListener(v -> finish());
        findViewById(R.id.btn_desplegable_tematica_crear).setOnClickListener(v -> mostrarDialogoTematicas());
        txtTematica = findViewById(R.id.txt_tematica_elegida_crear);

        botonesTiempo = new TextView[]{
                findViewById(R.id.btn_tiempo_30), findViewById(R.id.btn_tiempo_60),
                findViewById(R.id.btn_tiempo_90), findViewById(R.id.btn_tiempo_120)
        };
        configurarBotonesSeleccion(botonesTiempo, new int[]{30, 60, 90, 120}, true);
        seleccionarBoton(botonesTiempo[1]);

        botonesJugadores = new TextView[]{
                findViewById(R.id.btn_jug_4), findViewById(R.id.btn_jug_6),
                findViewById(R.id.btn_jug_8), findViewById(R.id.btn_jug_10)
        };
        configurarBotonesSeleccion(botonesJugadores, new int[]{4, 6, 8, 10}, false);
        seleccionarBoton(botonesJugadores[2]);

        findViewById(R.id.btn_crear_mision_final).setOnClickListener(v -> crearMision());
        obtenerMisTematicas();
    }

    private void crearMision() {
        if (tematicaSeleccionada.isEmpty()) {
            Toast.makeText(this, "Selecciona una temática", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer idTema = misTematicasDisponibles.get(tematicaSeleccionada);
        if (idTema == null) idTema = 1;

        try {
            JSONObject json = new JSONObject();
            // Nombres de campos sincronizados AL 100% con la versión Web (Pantalla12CrearPartida.jsx)
            json.put("es_publica", !esPrivada);
            json.put("tiempo_espera", tiempoSeleccionado);
            json.put("id_tema", idTema);
            json.put("max_jugadores", jugadoresSeleccionados);

            Log.d("CREAR", "Enviando Payload: " + json.toString());

            OkHttpClient client = new OkHttpClient();
            String token = new com.example.secretpanda.data.TokenManager(this).getToken();
            RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
            
            Request request = new Request.Builder()
                    .url("http://10.0.2.2:8080/api/partidas/")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + token)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> Toast.makeText(ConfiguracionMisionActivity.this, "Error de red", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String bodyStr = response.body() != null ? response.body().string() : "";
                    if (response.isSuccessful()) {
                        try {
                            JSONObject res = new JSONObject(bodyStr);
                            int idPartida = res.optInt("id_partida", -1);
                            String codigo = res.optString("codigo_partida", "");

                            Intent intent = new Intent(ConfiguracionMisionActivity.this, SalaEsperaActivity.class);
                            intent.putExtra("ID_PARTIDA", idPartida);
                            intent.putExtra("CODIGO_PARTIDA", codigo);
                            intent.putExtra("ES_LIDER", true);
                            intent.putExtra("MI_NOMBRE_USUARIO", nombreUsuario);
                            startActivity(intent);
                            finish();
                        } catch (Exception e) { 
                            Log.e("CREAR", "Error JSON: " + bodyStr, e); 
                        }
                    } else {
                        Log.e("CREAR", "Error Servidor (" + response.code() + "): " + bodyStr);
                        runOnUiThread(() -> Toast.makeText(ConfiguracionMisionActivity.this, "Error del servidor: " + response.code(), Toast.LENGTH_SHORT).show());
                    }
                }
            });
        } catch (Exception e) { Log.e("CREAR", "Error fatal", e); }
    }

    private void mostrarDialogoTematicas() {
        TematicasDialogFragment dialog = new TematicasDialogFragment();
        dialog.setMisTematicas(new ArrayList<>(misTematicasDisponibles.keySet()));
        dialog.setConfiguracionFiltros(true, false);
        dialog.setTematicaListener(tema -> {
            tematicaSeleccionada = tema;
            txtTematica.setText(tema);
        });
        dialog.show(getSupportFragmentManager(), "TematicasDialog");
    }

    private void configurarBotonesSeleccion(TextView[] botones, int[] valores, boolean esTiempo) {
        for (int i = 0; i < botones.length; i++) {
            final int idx = i;
            botones[i].setOnClickListener(v -> {
                for (TextView b : botones) b.setBackgroundResource(R.drawable.fondo_btn_unirse_pequeno);
                seleccionarBoton(botones[idx]);
                if (esTiempo) tiempoSeleccionado = valores[idx];
                else jugadoresSeleccionados = valores[idx];
            });
        }
    }

    private void seleccionarBoton(TextView b) {
        b.setBackgroundResource(R.drawable.fondo_verde_seleccion_personalizacion);
    }

    private void obtenerMisTematicas() {
        OkHttpClient client = new OkHttpClient();
        String token = new com.example.secretpanda.data.TokenManager(this).getToken();
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8080/api/jugadores/temas")
                .addHeader("Authorization", "Bearer " + token).build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) { }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONArray array = new JSONArray(response.body().string());
                        misTematicasDisponibles.clear();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject o = array.getJSONObject(i);
                            misTematicasDisponibles.put(o.getString("nombre"), o.getInt("id_tema"));
                        }
                        runOnUiThread(() -> {
                            if (!misTematicasDisponibles.isEmpty() && tematicaSeleccionada.isEmpty()) {
                                tematicaSeleccionada = misTematicasDisponibles.keySet().iterator().next();
                                txtTematica.setText(tematicaSeleccionada);
                            }
                        });
                    } catch (Exception e) { }
                }
            }
        });
    }
}
