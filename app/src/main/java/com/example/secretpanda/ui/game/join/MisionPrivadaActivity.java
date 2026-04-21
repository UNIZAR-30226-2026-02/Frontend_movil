package com.example.secretpanda.ui.game.join; // Asegúrate de que coincida con tu paquete real

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.secretpanda.R;
import com.example.secretpanda.data.NetworkConfig;
import com.example.secretpanda.data.TokenManager;
import com.example.secretpanda.ui.EfectosManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MisionPrivadaActivity extends AppCompatActivity {

    private EditText etCodigoSala;
    private TextView btnUnirse;
    private FrameLayout btnHome;

    private String nombreUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unirse_privada);

        nombreUsuario = getIntent().getStringExtra("MI_NOMBRE_USUARIO");
        // 1. Inicializar las vistas
        etCodigoSala = findViewById(R.id.et_codigo_sala);
        btnUnirse = findViewById(R.id.btn_confirmar_union);
        btnHome = findViewById(R.id.btn_volver_home);

        // 2. Configurar los eventos de clic
        configurarListeners();
    }

    private void configurarListeners() {

        // Botón para volver atrás (Home)
        btnHome.setOnClickListener(v -> {
            EfectosManager.reproducir(getApplicationContext(), R.raw.sonido_click);
            finish(); // Cierra esta actividad y vuelve a la anterior (UnirseMisionActivity)
        });

        // Botón de confirmar unión
        btnUnirse.setOnClickListener(v -> {
            EfectosManager.reproducir(getApplicationContext(), R.raw.sonido_click);
            String codigo = etCodigoSala.getText().toString().trim();

            if (codigo.isEmpty()) {
                // Validación básica: si el campo está vacío
                etCodigoSala.setError("Debes introducir un código");
                Toast.makeText(this, "Por favor, escribe el código de la sala", Toast.LENGTH_SHORT).show();
            } else {
                // Aquí iría tu lógica de conexión al servidor para validar el código
                conectarASalaPrivada(codigo);
            }
        });
    }

    private void conectarASalaPrivada(String codigo) {
        // Pon tu IP o dominio de tu servidor backend aquí
        String url = NetworkConfig.BASE_URL + "/partidas/" + codigo.trim() + "/unirse/privada";

        TokenManager tokenManager = new TokenManager(this);
        String jwt = tokenManager.getToken();
        // Obtenemos el token del jugador (asumiendo que usas tu TokenManager)

        if (jwt == null || jwt.isEmpty()) {
            Toast.makeText(this, "Error de sesión: No hay token", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();

        // Como el endpoint es un POST pero no recibe Body, enviamos un body vacío
        okhttp3.RequestBody body = new okhttp3.FormBody.Builder().build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", "Bearer " + jwt)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() -> Toast.makeText(MisionPrivadaActivity.this,
                        "Error de red al intentar unirse", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful()) {
                    // 1. Leemos la respuesta y le quitamos espacios/saltos de línea (.trim())
                    String idPartidaStr = response.body() != null ? response.body().string().trim() : "";

                    // 2. Comprobamos que el servidor realmente nos ha enviado algo
                    if (idPartidaStr.isEmpty()) {
                        runOnUiThread(() -> Toast.makeText(MisionPrivadaActivity.this,
                                "Error interno: El servidor no devolvió el ID de la partida", Toast.LENGTH_LONG).show());
                        return; // Cortamos la ejecución aquí para no crashear
                    }

                    try {
                        // 3. Intentamos convertirlo a número de forma segura
                        int idPartidaReal = Integer.parseInt(idPartidaStr);

                        runOnUiThread(() -> {
                            Toast.makeText(MisionPrivadaActivity.this, "¡Unido con éxito!", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(MisionPrivadaActivity.this, com.example.secretpanda.ui.game.waitingRoom.SalaEsperaActivity.class);
                            intent.putExtra("ID_PARTIDA", idPartidaReal);
                            intent.putExtra("CODIGO_PARTIDA", codigo);
                            intent.putExtra("MI_NOMBRE_USUARIO", nombreUsuario);
                            startActivity(intent);
                            finish();
                        });
                    } catch (NumberFormatException e) {
                        // Si el servidor devuelve texto raro (ej: "OK" en vez de "42"), no crashea
                        runOnUiThread(() -> Toast.makeText(MisionPrivadaActivity.this,
                                "Error: Respuesta del servidor inválida (" + idPartidaStr + ")", Toast.LENGTH_LONG).show());
                    }

                } else {
                    // ... (Tu código para cuando la sala no existe o está llena se queda igual)
                    String errorBody = response.body() != null ? response.body().string() : "Error desconocido";
                    runOnUiThread(() -> {
                        etCodigoSala.setError("Código no válido o sala llena");
                        Toast.makeText(MisionPrivadaActivity.this, "Error: " + errorBody, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }
}
