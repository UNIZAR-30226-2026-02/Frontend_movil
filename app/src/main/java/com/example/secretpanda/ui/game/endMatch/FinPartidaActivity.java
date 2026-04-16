package com.example.secretpanda.ui.game.match;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.secretpanda.R;
import com.example.secretpanda.data.TokenManager;
import com.example.secretpanda.ui.home.HomeActivity;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FinPartidaActivity extends AppCompatActivity {

    private int idPartida;
    private OkHttpClient client;
    private String token;

    // Vistas del XML que creamos antes
    private TextView tvCargando;
    private LinearLayout layoutContenido;
    private CardView cardBannerResultado;
    private TextView tvSubtituloVictoria, tvTituloVictoria, tvDetalleVictoria;
    private TextView tvAciertosRojo, tvAciertosAzul;
    private Button btnVolverEscritorio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fin_partida);

        idPartida = getIntent().getIntExtra("ID_PARTIDA", -1);
        client = new OkHttpClient();
        token = new TokenManager(this).getToken();

        vincularVistas();

        // NUEVA NAVEGACIÓN PURA AL HOME
        btnVolverEscritorio.setOnClickListener(v -> {
            Intent intent = new Intent(FinPartidaActivity.this, HomeActivity.class);
            
            // Estas dos flags son CRÍTICAS: Borran todo el historial de la partida anterior
            // para que el Home vuelva a ser la única pantalla activa (como si acabaras de abrir la app).
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            
            startActivity(intent);
            finish();
        });

        if (idPartida != -1) {
            fetchResultadosWeb(); // <-- Llamamos al método equivalente al de React
        } else {
            Toast.makeText(this, "Error crítico: ID de partida perdido.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void vincularVistas() {
        tvCargando = findViewById(R.id.tv_cargando);
        layoutContenido = findViewById(R.id.layout_contenido);
        cardBannerResultado = findViewById(R.id.card_banner_resultado);
        tvSubtituloVictoria = findViewById(R.id.tv_subtitulo_victoria);
        tvTituloVictoria = findViewById(R.id.tv_titulo_victoria);
        tvDetalleVictoria = findViewById(R.id.tv_detalle_victoria);
        tvAciertosRojo = findViewById(R.id.tv_aciertos_rojo);
        tvAciertosAzul = findViewById(R.id.tv_aciertos_azul);
        btnVolverEscritorio = findViewById(R.id.btn_volver_escritorio);
    }

    // Método exactamente igual al fetchResultados() de Pantalla15FinPartida.jsx
    private void fetchResultadosWeb() {
        // LLAMADA 1: Obtener estadísticas finales de la partida (SINGULAR: /partida/{id}/fin)
        String urlFin = "http://10.0.2.2:8080/api/partida/" + idPartida + "/fin";
        
        Request reqFin = new Request.Builder()
                .url(urlFin)
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        client.newCall(reqFin).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mostrarError("No se ha podido recuperar el informe de la misión.");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    mostrarError("Error al recuperar el informe: " + response.code());
                    return;
                }

                try {
                    String jsonFinString = response.body().string();
                    JSONObject datosFinales = new JSONObject(jsonFinString);

                    // LLAMADA 2: Obtener de qué equipo soy yo (PLURAL: /partidas/{id}/participantes/rol)
                    String urlRol = "http://10.0.2.2:8080/api/partidas/" + idPartida + "/participantes/rol";
                    Request reqRol = new Request.Builder()
                            .url(urlRol)
                            .addHeader("Authorization", "Bearer " + token)
                            .get()
                            .build();

                    client.newCall(reqRol).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            mostrarError("No se ha podido desencriptar tu identidad.");
                        }

                        @Override
                        public void onResponse(Call call, Response responseRol) throws IOException {
                            if (!responseRol.isSuccessful() || responseRol.body() == null) {
                                mostrarError("Error de identidad: " + responseRol.code());
                                return;
                            }

                            try {
                                JSONObject dataRol = new JSONObject(responseRol.body().string());
                                String miEquipo = dataRol.optString("equipo", "rojo");

                                // Ya tenemos las dos cosas, calculamos y mostramos la pantalla
                                calcularVariablesCalculadas(datosFinales, miEquipo);

                            } catch (Exception e) {
                                mostrarError("Error parseando identidad.");
                            }
                        }
                    });

                } catch (Exception e) {
                    mostrarError("Error parseando resultados de la misión.");
                }
            }
        });
    }

    // Exactamente la misma lógica matemática que en React
    private void calcularVariablesCalculadas(JSONObject datosFinales, String miEquipo) {
        String equipoGanador = datosFinales.optString("equipo_ganador", "Rojo");
        int aciertosRojo = datosFinales.optInt("aciertos_rojo", 0);
        int aciertosAzul = datosFinales.optInt("aciertos_azul", 0);

        boolean esRojoGanador = equipoGanador.equalsIgnoreCase("rojo");
        boolean soyRojo = miEquipo != null && miEquipo.equalsIgnoreCase("rojo");
        boolean heGanado = miEquipo != null && miEquipo.equalsIgnoreCase(equipoGanador);

        // Volvemos al hilo principal para actualizar la interfaz
        runOnUiThread(() -> {
            // Quitamos el texto de carga y mostramos el contenido
            tvCargando.setVisibility(View.GONE);
            layoutContenido.setVisibility(View.VISIBLE);

            // NUEVO: El color de la bandera AHORA SIEMPRE ES EL DE TU EQUIPO
            if (soyRojo) {
                cardBannerResultado.setCardBackgroundColor(Color.parseColor("#8B2020")); // bg-[#8b2020]
            } else {
                cardBannerResultado.setCardBackgroundColor(Color.parseColor("#80A0D0")); // bg-[#80a0d0]
            }

            // Cartel Dinámico
            if (heGanado) {
                tvSubtituloVictoria.setText("¡Enhorabuena agente!");
                tvTituloVictoria.setText("HAS GANADO");
                tvDetalleVictoria.setText("(Tu equipo ha desmantelado la red rival)");
            } else {
                tvSubtituloVictoria.setText("¡Misión fallida!");
                tvTituloVictoria.setText("HAS PERDIDO");
                tvDetalleVictoria.setText("(Victoria para el Equipo " + equipoGanador + ")");
            }

            // Marcadores Inferiores
            tvAciertosRojo.setText(String.valueOf(aciertosRojo));
            tvAciertosAzul.setText(String.valueOf(aciertosAzul));
        });
    }

    private void mostrarError(String mensaje) {
        runOnUiThread(() -> {
            tvCargando.setText(mensaje);
            tvCargando.setTextColor(Color.parseColor("#CC3333")); // text-[#cc3333] de React
        });
    }
}