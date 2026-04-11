package com.example.secretpanda.ui.home.profile;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secretpanda.R;
import com.example.secretpanda.data.TokenManager;
import com.example.secretpanda.data.model.Jugador;
import com.example.secretpanda.ui.home.GestorImagenes;

import com.example.secretpanda.ui.auth.LoginActivity;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class PerfilActivity extends AppCompatActivity {

    private RecyclerView recyclerAmigos;
    private TextView tabAmigos, tabDatos;
    private LinearLayout layoutDatos;
    private FrameLayout layoutAmigos;
    private Button btnCerrarSesion;
    private View layoutDetalleAmigo;
    private ImageView btnCerrarDetalleAmigo;
    private TextView textoNombreDatos;
    private LinearLayout layoutListaAmigosContenedor;
    private TextView btnGestionarSolicitudes;
    private AmigoAdapter adaptador;

    private List<Jugador> misAmigos;
    private String nombreImagen="";
    private ImageView fotoPerfil;
    private String tagActual= "Espía Secreto";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        layoutListaAmigosContenedor = findViewById(R.id.layout_lista_amigos_contenedor);
        btnGestionarSolicitudes = findViewById(R.id.btn_gestionar_solicitudes);
        layoutDetalleAmigo = findViewById(R.id.layout_detalle_amigo);
        textoNombreDatos = findViewById(R.id.texto_nombre_datos);
        fotoPerfil = findViewById(R.id.icono_perfil_datos);

        cargarDatosPerfil();

        btnGestionarSolicitudes.setOnClickListener(v -> {
            Intent intent = new Intent(PerfilActivity.this, SolicitudesActivity.class);
            startActivity(intent);
        });

        btnCerrarDetalleAmigo = findViewById(R.id.btn_cerrar_detalle_amigo);
        btnCerrarDetalleAmigo.setOnClickListener(v -> {
            layoutDetalleAmigo.setVisibility(View.GONE);
            layoutListaAmigosContenedor.setVisibility(View.VISIBLE);
        });

        recyclerAmigos = findViewById(R.id.recycler_amigos);
        recyclerAmigos.setLayoutManager(new LinearLayoutManager(this));

        misAmigos = new java.util.ArrayList<>();
        adaptador = new AmigoAdapter(misAmigos, amigo -> mostrarDetalleDe(amigo));
        recyclerAmigos.setAdapter(adaptador);

        cargarAmigosServidor();

        layoutAmigos = findViewById(R.id.layout_amigos);
        layoutDatos = findViewById(R.id.layout_datos);
        tabAmigos = findViewById(R.id.tab_amigos);
        tabDatos = findViewById(R.id.tab_datos);

        String nombreRecibido = getIntent().getStringExtra("NOMBRE_JUGADOR");
        if (nombreRecibido != null && !nombreRecibido.isEmpty()) {
            textoNombreDatos.setText(nombreRecibido);
        } else {
            textoNombreDatos.setText("Espía Secreto");
        }

        ImageView btnCerrar = findViewById(R.id.btn_cerrar_perfil);
        btnCerrarSesion = findViewById(R.id.btn_cerrar_sesion);
        btnCerrar.setOnClickListener(v -> finish());

        tabDatos.setOnClickListener(v -> {
            cargarDatosPerfil();
            layoutAmigos.setVisibility(View.GONE);
            layoutDatos.setVisibility(View.VISIBLE);
            tabDatos.setBackgroundResource(R.drawable.tab_selected);
            tabDatos.setTextColor(Color.parseColor("#555555"));
            tabAmigos.setBackgroundResource(R.drawable.tab_unselected);
            tabAmigos.setTextColor(Color.WHITE);
        });

        tabAmigos.setOnClickListener(v -> {
            layoutDatos.setVisibility(View.GONE);
            layoutAmigos.setVisibility(View.VISIBLE);
            tabAmigos.setBackgroundResource(R.drawable.tab_selected);
            tabAmigos.setTextColor(Color.parseColor("#555555"));
            tabDatos.setBackgroundResource(R.drawable.tab_unselected);
            tabDatos.setTextColor(Color.WHITE);
            cargarAmigosServidor();
        });

        btnCerrarSesion.setOnClickListener(v -> {
            com.google.android.gms.auth.api.signin.GoogleSignInOptions gso =
                    new com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN).build();
            com.google.android.gms.auth.api.signin.GoogleSignInClient googleClient =
                    com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this, gso);

            googleClient.signOut().addOnCompleteListener(this, task -> {
                TokenManager tokenManager = new TokenManager(this);
                tokenManager.clearToken();
                Intent intent = new Intent(PerfilActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        });

        ImageView btnEditarPerfil = findViewById(R.id.btn_editar_perfil);
        btnEditarPerfil.setOnClickListener(v -> mostrarDialogoEditar());
    }

    private void cargarAmigosServidor() {
        TokenManager tokenManager = new TokenManager(this);
        String jwt = tokenManager.getToken();

        if (jwt == null || jwt.isEmpty()) return;

        OkHttpClient client = new OkHttpClient();
        String url = "http://10.0.2.2:8080/api/amigos";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + jwt)
                .addHeader("Cookie", "jwt=" + jwt)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() -> Log.e("API_AMIGOS", "Error red: " + e.getMessage()));
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (!response.isSuccessful()) return;

                if (response.body() != null) {
                    String jsonResponse = response.body().string();
                    try {
                        org.json.JSONArray jsonArray = new org.json.JSONArray(jsonResponse);
                        java.util.List<Jugador> listaAmigosReales = new java.util.ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            org.json.JSONObject obj = jsonArray.getJSONObject(i);
                            String tag = obj.has("tag") ? obj.getString("tag") : obj.optString("nombre", "Desconocido");
                            String fotoPerfil = obj.optString("foto_perfil", "");
                            int victorias = obj.optInt("victorias", 0);

                            Jugador amigo = new Jugador(tag);
                            amigo.setFotoPerfil(fotoPerfil);
                            amigo.setVictorias(victorias);
                            listaAmigosReales.add(amigo);
                        }

                        runOnUiThread(() -> {
                            misAmigos.clear();
                            misAmigos.addAll(listaAmigosReales);
                            if (adaptador != null) adaptador.notifyDataSetChanged();
                        });

                    } catch (org.json.JSONException e) {
                        Log.e("API_AMIGOS", "Error parseo: " + e.getMessage());
                    }
                }
            }
        });
    }

    private void mostrarDetalleDe(Jugador amigo) {
        TextView textoNombre = findViewById(R.id.texto_nombre_detalle_amigo);
        TextView txtVictorias = findViewById(R.id.stat_amigo_victorias);
        ImageView iconoAmigo = findViewById(R.id.icono_detalle_amigo);

        if (amigo != null) {
            if (textoNombre != null) textoNombre.setText(amigo.getTag());
            if (txtVictorias != null) txtVictorias.setText(String.valueOf(amigo.getVictorias()));
            
            if (iconoAmigo != null) {
                int resId = GestorImagenes.obtenerImagenManual(amigo.getFotoPerfil());
                if (resId != 0) {
                    iconoAmigo.setImageResource(resId);
                } else {
                    iconoAmigo.setImageResource(R.mipmap.ic_launcher);
                }
            }
        }

        if (layoutListaAmigosContenedor != null && layoutDetalleAmigo != null) {
            layoutListaAmigosContenedor.setVisibility(View.GONE);
            layoutDetalleAmigo.setVisibility(View.VISIBLE);
        }
    }

    private void mostrarDialogoEditar() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_editar_perfil, null);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setView(dialogView);
        android.app.AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        ImageView btnCerrar = dialogView.findViewById(R.id.btn_cerrar_editar);
        Button btnGuardar = dialogView.findViewById(R.id.btn_guardar_cambios);
        ImageView btnCambiarFoto = dialogView.findViewById(R.id.btn_cambiar_foto);
        android.widget.EditText inputNombre = dialogView.findViewById(R.id.input_editar_nombre);

        inputNombre.setText(tagActual);
        btnCerrar.setOnClickListener(v -> dialog.dismiss());
        btnCambiarFoto.setOnClickListener(v -> mostrarDialogoElegirImagen(btnCambiarFoto));

        btnGuardar.setOnClickListener(v -> {
            String nuevoNombre = inputNombre.getText().toString().trim();
            if (!nuevoNombre.isEmpty()) {
                actualizarPerfilServidor(nuevoNombre, nombreImagen);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void mostrarDialogoElegirImagen(ImageView imagenPerfilActual) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_elegir_imagen, null);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setView(dialogView);
        android.app.AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        ImageView btnCerrar = dialogView.findViewById(R.id.btn_cerrar_elegir_imagen);
        RecyclerView recyclerImagenes = dialogView.findViewById(R.id.recycler_elegir_imagen);
        recyclerImagenes.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 3));

        java.util.List<Integer> misImagenes = new java.util.ArrayList<>();
        for (int i = 0; i < 60; i++) misImagenes.add(R.mipmap.ic_launcher);

        ImagenPerfilAdapter adapter = new ImagenPerfilAdapter(misImagenes, recurso -> {
            imagenPerfilActual.setImageResource(recurso);
            nombreImagen = getResources().getResourceEntryName(recurso);
            actualizarPerfilServidor(tagActual, nombreImagen);
            dialog.dismiss();
        });
        recyclerImagenes.setAdapter(adapter);
        btnCerrar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void cargarDatosPerfil() {
        OkHttpClient client = new OkHttpClient();
        String url = "http://10.0.2.2:8080/api/jugadores";

        TokenManager tokenManager = new TokenManager(this);
        String jwt = tokenManager.getToken();
        if (jwt == null || jwt.isEmpty()) return;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + jwt)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                Log.e("API_PERFIL", "Error: " + e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonData = response.body().string();
                    try {
                        org.json.JSONObject obj = new org.json.JSONObject(jsonData);
                        tagActual = obj.getString("tag");
                        nombreImagen = obj.optString("foto_perfil", "");
                        int balas = obj.getInt("balas");
                        int victorias = obj.getInt("victorias");
                        int derrotas = obj.getInt("derrotas");

                        int totalPartidas = victorias + derrotas;
                        float winrate = totalPartidas > 0 ? ((float) victorias / totalPartidas) * 100 : 0f;

                        runOnUiThread(() -> {
                            if (textoNombreDatos != null) textoNombreDatos.setText(tagActual);
                            TextView txtPartidas = findViewById(R.id.stat_mio_partidas);
                            TextView txtVictorias = findViewById(R.id.stat_mio_victorias);
                            TextView txtDerrotas = findViewById(R.id.stat_mio_derrotas);
                            TextView txtWinrate = findViewById(R.id.stat_mio_winrate);
                            TextView txtBalas = findViewById(R.id.texto_mis_balas);

                            if (txtPartidas != null) txtPartidas.setText(String.valueOf(totalPartidas));
                            if (txtVictorias != null) txtVictorias.setText(String.valueOf(victorias));
                            if (txtDerrotas != null) txtDerrotas.setText(String.valueOf(derrotas));
                            if (txtWinrate != null) txtWinrate.setText(String.format("%.1f%%", winrate));
                            if (txtBalas != null) txtBalas.setText(String.valueOf(balas));

                            if (!nombreImagen.isEmpty()) {
                                int resId = getResources().getIdentifier(nombreImagen, "drawable", getPackageName());
                                if (resId != 0 && fotoPerfil != null) fotoPerfil.setImageResource(resId);
                            }
                        });
                    } catch (org.json.JSONException e) {
                        Log.e("API_PERFIL", "Error parseo perfil", e);
                    }
                }
            }
        });
    }

    private void actualizarPerfilServidor(String nuevoTag, String nombreImagen) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://10.0.2.2:8080/api/jugadores";
        TokenManager tokenManager = new TokenManager(this);
        String jwt = tokenManager.getToken();

        org.json.JSONObject jsonBody = new org.json.JSONObject();
        try {
            jsonBody.put("tag", nuevoTag);
            jsonBody.put("foto_perfil", nombreImagen);
        } catch (org.json.JSONException e) {}

        okhttp3.RequestBody body = okhttp3.RequestBody.create(
                jsonBody.toString(),
                okhttp3.MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .addHeader("Authorization", "Bearer " + jwt)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {}

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        tagActual = nuevoTag;
                        if (textoNombreDatos != null) textoNombreDatos.setText(nuevoTag);
                        Intent intent = new Intent();
                        intent.putExtra("NOMBRE_ACTUALIZADO", nuevoTag);
                        setResult(RESULT_OK, intent);
                        cargarDatosPerfil();
                    }
                });
            }
        });
    }
}