package com.example.secretpanda.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.secretpanda.R;
import com.example.secretpanda.ui.LoadingActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private Button btnLogin;
    // Este es el "receptor" que espera a que el usuario elija su cuenta de Google
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Le pasamos los datos a tu método manejarResultadoGoogle SIEMPRE,
                // haya ido bien o haya ido mal, para que su try/catch atrape el código de error
                Intent data = result.getData();
                if (data != null) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    manejarResultadoGoogle(task);
                } else {
                    Log.e("LOGIN_GOOGLE", "El intent ha devuelto null (se cerró la ventana pulsando fuera)");
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("271645130319-f9agsfadvl8njoaoitevnaspuchj5fb9.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // 2. Configuramos el botón
        btnLogin = findViewById(R.id.button);
        btnLogin.setOnClickListener(v -> {
            // Al pulsar, abrimos la ventana de cuentas de Google
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void manejarResultadoGoogle(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // ¡BINGO! Aquí tenemos el ticket que Google nos da para este usuario
            String idToken = account.getIdToken();
            String email = account.getEmail();

            Log.d("LOGIN_GOOGLE", "¡Login en Android OK! Email: " + email);
            Log.d("LOGIN_GOOGLE", "Ticket (idToken) para enviar al servidor: " + idToken);

            // TODO: Aquí haremos la llamada OkHttp a tu Spring Boot (Paso 2)
            enviarTokenAlBackend(idToken);

        } catch (ApiException e) {
            Log.e("LOGIN_GOOGLE", "Error en Google Sign-In. Código de estado: " + e.getStatusCode());
            Toast.makeText(this, "Fallo al iniciar sesión con Google", Toast.LENGTH_SHORT).show();
        }
    }

    private void enviarTokenAlBackend(String idToken) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://10.0.2.2:8080/api/auth/login";

        // Construimos el JSON exactamente como lo pide tu AuthController
        String json = "{\"id_google\":\"" + idToken + "\"}";
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("LOGIN_API", "Error de conexión con el backend", e);
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Error conectando al servidor", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonRespuesta = response.body().string();
                        Log.w("LOGIN_API", "JSON del servidor: " + jsonRespuesta);

                        org.json.JSONObject jsonObject = new org.json.JSONObject(jsonRespuesta);

                        // 1. ¿Es nuevo?
                        boolean esNuevo = jsonObject.optBoolean("es_nuevo", false);

                        final String tokenFinal = idToken;

                        runOnUiThread(() -> {
                            if (esNuevo) {
                                // ES NUEVO -> A elegir nombre (Registro)
                                Intent intent = new Intent(LoginActivity.this, com.example.secretpanda.ui.auth.UserSelectionActivity.class);
                                intent.putExtra("ID_GOOGLE", tokenFinal);
                                startActivity(intent);
                                finish();
                            } else {
                                // YA EXISTE -> Guardamos JWT y miramos si estaba jugando
                                String tokenJwt = jsonObject.optString("token", "");
                                if (!tokenJwt.isEmpty()) {
                                    com.example.secretpanda.data.TokenManager tokenManager = new com.example.secretpanda.data.TokenManager(LoginActivity.this);
                                    tokenManager.saveToken(tokenJwt);

                                    try {
                                        // 🕵️‍♂️ NUEVO: Extraemos el jugador y miramos si tiene partida activa
                                        org.json.JSONObject jugadorJson = jsonObject.getJSONObject("jugador");
                                        // optLong devuelve 0 si no existe el campo o es null
                                        long partidaActivaId = jugadorJson.optLong("partidaActivaId", 0);

                                        Intent intent;
                                        if (partidaActivaId > 0) {
                                            // 🔥 ¡Estaba en una partida! Lo reconectamos
                                            intent = new Intent(LoginActivity.this, com.example.secretpanda.ui.game.match.PartidaActivity.class);
                                            intent.putExtra("MI_NOMBRE_USUARIO", jugadorJson.optString("tag", ""));
                                            intent.putExtra("ID_PARTIDA", partidaActivaId);
                                            Toast.makeText(LoginActivity.this, "Reconectando a la partida...", Toast.LENGTH_SHORT).show();
                                        } else {
                                            // No estaba jugando -> A la Home
                                            intent = new Intent(LoginActivity.this, com.example.secretpanda.ui.LoadingActivity.class);
                                            intent.putExtra("MI_NOMBRE_USUARIO", jugadorJson.optString("tag", ""));
                                            intent.putExtra("DESTINO", "HOME");
                                        }
                                        startActivity(intent);
                                        finish();

                                    } catch (org.json.JSONException e) {
                                        // Si por lo que sea el backend no mandó el objeto Jugador, vamos a la Home por defecto
                                        Intent intent = new Intent(LoginActivity.this, com.example.secretpanda.ui.LoadingActivity.class);
                                        intent.putExtra("DESTINO", "HOME");
                                        startActivity(intent);
                                        finish();
                                    }

                                } else {
                                    Toast.makeText(LoginActivity.this, "Error: El servidor no envió el Token", Toast.LENGTH_SHORT).show();
                                    if (btnLogin != null) btnLogin.setEnabled(true);
                                }
                            }
                        });

                    } catch (Exception e) {
                        Log.e("LOGIN_API", "Error procesando el JSON", e);
                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this, "Error leyendo datos del servidor", Toast.LENGTH_SHORT).show();
                            if (btnLogin != null) btnLogin.setEnabled(true);
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, "Error de credenciales", Toast.LENGTH_SHORT).show();
                        if (btnLogin != null) btnLogin.setEnabled(true);
                    });
                }
            }
        });
    }
}