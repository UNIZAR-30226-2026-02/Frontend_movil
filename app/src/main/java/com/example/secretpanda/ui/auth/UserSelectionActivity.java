package com.example.secretpanda.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.secretpanda.R;
import com.example.secretpanda.data.model.Jugador; // ¡Importante importar el modelo!
import com.example.secretpanda.ui.EfectosManager;
import com.example.secretpanda.ui.home.HomeActivity;

public class UserSelectionActivity extends AppCompatActivity {

    private String nombreUsuario;

    private String idGoogleEstable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eleccion_nombre_usuario);

        // Recuperamos el ID de Google que nos envió LoginActivity
        idGoogleEstable = getIntent().getStringExtra("GOOGLE_ID_ESTABLE");


        EditText inputUsuario = findViewById(R.id.inputUsuario);
        Button btnAceptar = findViewById(R.id.btnAceptar);


        btnAceptar.setOnClickListener(v -> {
            EfectosManager.reproducir(getApplicationContext(), R.raw.sonido_click);
            String username = inputUsuario.getText().toString().trim();
            nombreUsuario = username;
            if (username.length() < 4) {
                Toast.makeText(this, "El nombre debe tener al menos 4 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }

            // Recuperamos el ID de Google que nos envió LoginActivity
            String idGoogle = getIntent().getStringExtra("ID_GOOGLE");
            if (idGoogle == null) {
                Toast.makeText(this, "Error crítico: Falta el ID de Google", Toast.LENGTH_SHORT).show();
                return;
            }

            // Bloqueamos el botón para que no mande 20 registros a la vez
            btnAceptar.setEnabled(false);

            // PREPARAMOS LA LLAMADA AL BACKEND PARA REGISTRAR
            okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
            String url = "http://10.0.2.2:8080/api/auth/registro";

            // Construimos el JSON con los dos datos que exige el backend
            String json = "{\"id_google\":\"" + idGoogle + "\", \"tag\":\"" + username + "\"}";
            okhttp3.RequestBody body = okhttp3.RequestBody.create(json, okhttp3.MediaType.parse("application/json; charset=utf-8"));

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, java.io.IOException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(UserSelectionActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                        btnAceptar.setEnabled(true);
                    });
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String jsonRespuesta = response.body().string();
                            org.json.JSONObject jsonObject = new org.json.JSONObject(jsonRespuesta);

                            // Extraemos el JWT definitivo
                            String tokenJwt = jsonObject.getString("token");

                            //  Lo guardamos en la app
                            com.example.secretpanda.data.TokenManager tokenManager = new com.example.secretpanda.data.TokenManager(UserSelectionActivity.this);
                            tokenManager.saveToken(tokenJwt);

                            // Saltamos a la Home
                            runOnUiThread(() -> {
                                android.content.Intent intent = new android.content.Intent(UserSelectionActivity.this, com.example.secretpanda.ui.LoadingActivity.class);
                                intent.putExtra("MI_NOMBRE_USUARIO", nombreUsuario);
                                intent.putExtra("DESTINO", "HOME");
                                intent.putExtra("GOOGLE_ID_ESTABLE", idGoogleEstable);
                                startActivity(intent);
                                finish();
                            });
                        } catch (Exception e) {
                            runOnUiThread(() -> {
                                Toast.makeText(UserSelectionActivity.this, "Error procesando registro", Toast.LENGTH_SHORT).show();
                                btnAceptar.setEnabled(true);
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            // Si falla, suele ser porque el 'tag' ya está en uso en la BD
                            Toast.makeText(UserSelectionActivity.this, "Error en registro. Quizás el nombre ya existe.", Toast.LENGTH_SHORT).show();
                            btnAceptar.setEnabled(true);
                        });
                    }
                }
            });
        });
    }
}