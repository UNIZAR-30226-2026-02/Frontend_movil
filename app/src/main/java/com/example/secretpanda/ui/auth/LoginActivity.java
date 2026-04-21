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
import com.example.secretpanda.data.NetworkConfig;
import com.example.secretpanda.ui.EfectosManager;
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
    private String nombreUsuario;

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Intent data = result.getData();
                if (data != null) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    manejarResultadoGoogle(task);
                } else {
                    Log.e("LOGIN_GOOGLE", "El intent ha devuelto null");
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EfectosManager.inicializar(this);
        setContentView(R.layout.activity_login);

        intentarAutoLogin();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("271645130319-2raursujnehhvpjcj6g015kpn9rqfnbs.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnLogin = findViewById(R.id.button);
        btnLogin.setOnClickListener(v -> {
            EfectosManager.reproducir(getApplicationContext(), R.raw.sonido_click);
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void manejarResultadoGoogle(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();
            String googleIdEstable = account.getId();
            enviarTokenAlBackend(idToken, googleIdEstable);
        } catch (ApiException e) {
            Log.e("LOGIN_GOOGLE", "Error en Google Sign-In: " + e.getStatusCode());
            Toast.makeText(this, "Fallo al iniciar sesión con Google", Toast.LENGTH_SHORT).show();
        }
    }

    private void enviarTokenAlBackend(String idToken, String googleIdEstable) {
        OkHttpClient client = new OkHttpClient();
        String url = NetworkConfig.BASE_URL + "/auth/login";

        String json = "{\"id_google\":\"" + idToken + "\"}";
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                com.example.secretpanda.data.ErrorUtils.showConnectionError(LoginActivity.this, e);
                runOnUiThread(() -> { if (btnLogin != null) btnLogin.setEnabled(true); });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonRespuesta = response.body().string();
                        org.json.JSONObject jsonObject = new org.json.JSONObject(jsonRespuesta);
                        boolean esNuevo = jsonObject.optBoolean("es_nuevo", false);
                        
                        runOnUiThread(() -> {
                            if (esNuevo) {
                                Intent intent = new Intent(LoginActivity.this, com.example.secretpanda.ui.auth.UserSelectionActivity.class);
                                intent.putExtra("MI_NOMBRE_USUARIO", nombreUsuario);
                                intent.putExtra("GOOGLE_ID_ESTABLE", googleIdEstable);
                                intent.putExtra("ID_GOOGLE", idToken);
                                startActivity(intent);
                                finish();
                            } else {
                                String tokenJwt = jsonObject.optString("token", "");
                                if (!tokenJwt.isEmpty()) {
                                    com.example.secretpanda.data.TokenManager tm = new com.example.secretpanda.data.TokenManager(LoginActivity.this);
                                    tm.saveToken(tokenJwt);
                                    tm.saveIdGoogle(googleIdEstable);

                                    try {
                                        org.json.JSONObject jug = jsonObject.getJSONObject("jugador");
                                        long pId = jug.optLong("partida_activa_id", 0);
                                        String tag = jug.optString("tag", "");
                                        if (pId > 0) {
                                            verificarYReconectar((int)pId, tag, googleIdEstable);
                                        } else {
                                            irAHome(tag, googleIdEstable);
                                        }
                                    } catch (JSONException e) {
                                        irAHome(nombreUsuario, googleIdEstable);
                                    }
                                } else {
                                    Toast.makeText(LoginActivity.this, "Error: Token no recibido", Toast.LENGTH_SHORT).show();
                                    if (btnLogin != null) btnLogin.setEnabled(true);
                                }
                            }
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> { if (btnLogin != null) btnLogin.setEnabled(true); });
                    }
                } else {
                    com.example.secretpanda.data.ErrorUtils.showErrorMessage(LoginActivity.this, response);
                    runOnUiThread(() -> { if (btnLogin != null) btnLogin.setEnabled(true); });
                }
            }
        });
    }

    private void intentarAutoLogin() {
        com.example.secretpanda.data.TokenManager tm = new com.example.secretpanda.data.TokenManager(this);
        String token = tm.getToken();
        String idG = tm.getIdGoogle();

        if (token != null && !token.isEmpty()) {
            OkHttpClient client = new OkHttpClient();
            Request req = new Request.Builder()
                    .url(NetworkConfig.BASE_URL + "/jugadores")
                    .addHeader("Authorization", "Bearer " + token)
                    .build();

            client.newCall(req).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) { }
                @Override
                public void onResponse(Call call, Response res) throws IOException {
                    if (res.isSuccessful() && res.body() != null) {
                        try {
                            org.json.JSONObject jug = new org.json.JSONObject(res.body().string());
                            long pId = jug.optLong("partida_activa_id", 0);
                            String tag = jug.optString("tag", "");
                            runOnUiThread(() -> {
                                if (pId > 0) verificarYReconectar((int)pId, tag, idG);
                                else irAHome(tag, idG);
                            });
                        } catch (Exception e) { }
                    } else {
                        tm.clearToken();
                    }
                }
            });
        }
    }

    private void verificarYReconectar(int idPartida, String tag, String idG) {
        String token = new com.example.secretpanda.data.TokenManager(this).getToken();
        OkHttpClient client = new OkHttpClient();
        Request req = new Request.Builder()
                .url(NetworkConfig.BASE_URL + "/partidas/" + idPartida + "/estado")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> irAHome(tag, idG));
            }
            @Override public void onResponse(Call call, Response res) throws IOException {
                if (res.isSuccessful() && res.body() != null) {
                    try {
                        org.json.JSONObject json = new org.json.JSONObject(res.body().string());
                        if ("en_curso".equalsIgnoreCase(json.optString("estado", ""))) {
                            runOnUiThread(() -> {
                                Intent intent = new Intent(LoginActivity.this, com.example.secretpanda.ui.game.match.PartidaActivity.class);
                                intent.putExtra("ID_PARTIDA", idPartida);
                                intent.putExtra("MI_NOMBRE_USUARIO", tag);
                                Toast.makeText(LoginActivity.this, "Reconectando...", Toast.LENGTH_SHORT).show();
                                startActivity(intent);
                                finish();
                            });
                        } else {
                            runOnUiThread(() -> irAHome(tag, idG));
                        }
                    } catch (Exception e) { runOnUiThread(() -> irAHome(tag, idG)); }
                } else { runOnUiThread(() -> irAHome(tag, idG)); }
            }
        });
    }

    private void irAHome(String tag, String idG) {
        Intent intent = new Intent(LoginActivity.this, com.example.secretpanda.ui.LoadingActivity.class);
        intent.putExtra("MI_NOMBRE_USUARIO", tag);
        intent.putExtra("GOOGLE_ID_ESTABLE", idG);
        intent.putExtra("DESTINO", "HOME");
        startActivity(intent);
        finish();
    }
}
