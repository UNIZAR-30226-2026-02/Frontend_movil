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

    // Este es el "receptor" que espera a que el usuario elija su cuenta de Google
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    manejarResultadoGoogle(task);
                } else {
                    Toast.makeText(this, "Login cancelado", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 1. Configuramos las opciones de Google Sign-In
        // Aquí es donde usamos el client_id de tu archivo JSON web
        String clientId = "271645130319-f9agsfadvl8njoaoitevnaspuchj5fb9.apps.googleusercontent.com";

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientId) // Pedimos el ticket (idToken)
                .requestEmail()           // Pedimos el email
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // 2. Configuramos el botón
        Button btnLogin = findViewById(R.id.button);
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

        // ¡AQUÍ ESTÁ LA MAGIA! Construimos el JSON exactamente como lo pide tu AuthController
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
                        org.json.JSONObject jsonObject = new org.json.JSONObject(jsonRespuesta);

                        // Tu AuthController devuelve un AuthResponseDTO que tiene el "token"
                        String tokenJwt = jsonObject.getString("token");

                        // 3. ¡LO GUARDAMOS EN LA CAJA FUERTE (TokenManager)!
                        com.example.secretpanda.data.TokenManager tokenManager = new com.example.secretpanda.data.TokenManager(LoginActivity.this);
                        tokenManager.saveToken(tokenJwt);

                        Log.d("LOGIN_API", "¡Login exitoso en Spring Boot! Token guardado: " + tokenJwt);

                        // 4. Saltamos a la siguiente pantalla
                        runOnUiThread(() -> {
                            Intent intent = new Intent(LoginActivity.this, com.example.secretpanda.ui.LoadingActivity.class);
                            intent.putExtra("DESTINO", "ELEGIR_USUARIO");
                            startActivity(intent);
                            finish();
                        });

                    } catch (org.json.JSONException e) {
                        Log.e("LOGIN_API", "Error extrayendo el token del JSON", e);
                    }
                } else {
                    Log.e("LOGIN_API", "Error en el backend. Código: " + response.code());
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Error al validar con el servidor", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}