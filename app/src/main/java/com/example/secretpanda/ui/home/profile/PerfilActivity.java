package com.example.secretpanda.ui.home.profile;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import com.example.secretpanda.data.model.GestorEstadisticas;
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
    private TextView textoNombreDetalleAmigo;
    private ImageView btnCerrarDetalleAmigo;
    private Jugador jugadorActual;
    private TextView textoNombreDatos;
    private LinearLayout layoutListaAmigosContenedor;
    private TextView btnGestionarSolicitudes;
    private AmigoAdapter adaptador;

    private List<Jugador> misAmigos;
    private String nombreImagen;
    private String tagActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        layoutListaAmigosContenedor = findViewById(R.id.layout_lista_amigos_contenedor);
        btnGestionarSolicitudes = findViewById(R.id.btn_gestionar_solicitudes);
        layoutDetalleAmigo = findViewById(R.id.layout_detalle_amigo);

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

        // TUS AMIGOS FALSOS INICIALES
        misAmigos = new java.util.ArrayList<>();
        cargarAmigosServidor();
        adaptador = new AmigoAdapter(misAmigos, amigoClickado -> {
            mostrarDetalleDe(amigoClickado);
        });
        recyclerAmigos.setAdapter(adaptador);

        layoutAmigos = findViewById(R.id.layout_amigos);
        layoutDatos = findViewById(R.id.layout_datos);

        tabAmigos = findViewById(R.id.tab_amigos);
        tabDatos = findViewById(R.id.tab_datos);

        textoNombreDatos = findViewById(R.id.texto_nombre_datos);

        String nombreRecibido = getIntent().getStringExtra("NOMBRE_JUGADOR");
        if (nombreRecibido != null && !nombreRecibido.isEmpty()) {
            textoNombreDatos.setText(nombreRecibido);
        } else {
            textoNombreDatos.setText("Espía Secreto");
        }

        ImageView btnCerrar = findViewById(R.id.btn_cerrar_perfil);
        btnCerrarSesion = findViewById(R.id.btn_cerrar_sesion);
        cargarDatosPerfil();
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
            adaptador = new AmigoAdapter(misAmigos, amigoClickado -> {
                mostrarDetalleDe(amigoClickado);
            });

        });

        btnCerrarSesion.setOnClickListener(v -> {
            //  DESCONECTAR DE GOOGLE SIGN-IN
            com.google.android.gms.auth.api.signin.GoogleSignInOptions gso =
                    new com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN).build();
            com.google.android.gms.auth.api.signin.GoogleSignInClient googleClient =
                    com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this, gso);

            googleClient.signOut().addOnCompleteListener(this, task -> {

                // BORRAR EL JWT LOCAL (TokenManager)
                com.example.secretpanda.data.TokenManager tokenManager = new com.example.secretpanda.data.TokenManager(this);
                tokenManager.clearToken();

                //  REDIRIGIR AL LOGIN LIMPIANDO EL HISTORIAL DE PANTALLAS
                android.content.Intent intent = new android.content.Intent(PerfilActivity.this, com.example.secretpanda.ui.auth.LoginActivity.class);
                intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        });

        ImageView btnEditarPerfil = findViewById(R.id.btn_editar_perfil);
        btnEditarPerfil.setOnClickListener(v -> mostrarDialogoEditar());
    }

    private void cargarAmigosServidor() {
        OkHttpClient client = new OkHttpClient();
        // Sustituye por la IP de tu servidor si es necesario
        String url = "http://10.0.2.2:8080/api/amigos/";

        // Recuperamos el token JWT
        TokenManager tokenManager = new TokenManager(this);
        String jwt = tokenManager.getToken();
        if (jwt == null || jwt.isEmpty()) {
            android.util.Log.e("API_AMIGOS", "No hay token disponible");
            return;
        }

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + jwt)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() -> {
                    android.widget.Toast.makeText(PerfilActivity.this, "Error de red al cargar amigos", android.widget.Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonData = response.body().string();
                    try {
                        org.json.JSONArray jsonArray = new org.json.JSONArray(jsonData);

                        // IMPORTANTE: Asegúrate de usar la clase correcta aquí (Jugador o Amigo)
                        // según lo que espere tu AmigoAdapter.
                        List<Jugador> listaAmigosReales = new java.util.ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            org.json.JSONObject obj = jsonArray.getJSONObject(i);

                            String tag = obj.getString("tag");
                            String fotoPerfil = obj.optString("foto_perfil", "");
                            int victorias = obj.getInt("victorias");
                            int numAciertos = obj.getInt("num_aciertos");

                            // Instanciamos el objeto con los datos del RF-24 y RF-25
                            Jugador amigo = new Jugador(tag);
                            amigo.setFotoPerfil(fotoPerfil);
                            amigo.setVictorias(victorias);
                            amigo.setNumAciertos(numAciertos);

                            listaAmigosReales.add(amigo);
                        }
                        misAmigos = listaAmigosReales;

                    } catch (org.json.JSONException e) {
                        android.util.Log.e("API_AMIGOS", "Error parseando el JSON de amigos", e);
                    }
                } else {
                    runOnUiThread(() -> {
                        android.widget.Toast.makeText(PerfilActivity.this, "Error del servidor: " + response.code(), android.widget.Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
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

        inputNombre.setText(textoNombreDatos.getText().toString());

        btnCerrar.setOnClickListener(v -> dialog.dismiss());
        btnCambiarFoto.setOnClickListener(v -> mostrarDialogoElegirImagen(btnCambiarFoto));

        btnGuardar.setOnClickListener(v -> {
            String nuevoNombre = inputNombre.getText().toString().trim();
            if (!nuevoNombre.isEmpty()) {
                tagActual = nuevoNombre;
                actualizarPerfilServidor(tagActual, nombreImagen);
                textoNombreDatos.setText(nuevoNombre);
                Intent intentDeVuelta = new Intent();
                intentDeVuelta.putExtra("NOMBRE_ACTUALIZADO", nuevoNombre);
                setResult(RESULT_OK, intentDeVuelta);

                android.widget.Toast.makeText(this, "Perfil actualizado", android.widget.Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                inputNombre.setError("El nombre no puede estar vacío");
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

        ImagenPerfilAdapter adaptador = new ImagenPerfilAdapter(misImagenes, recursoImagen -> {
            // 1. Cambiamos la imagen visualmente
            imagenPerfilActual.setImageResource(recursoImagen);

            // 2. Obtenemos el nombre del recurso (ej: de R.drawable.avatar_1 a "avatar_1")
            nombreImagen = getResources().getResourceEntryName(recursoImagen);

            // 4. ¡ENVIAMOS AL BACKEND!
            actualizarPerfilServidor(tagActual, nombreImagen);

            dialog.dismiss();
        });
        recyclerImagenes.setAdapter(adaptador);
        btnCerrar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void mostrarDetalleDe(Jugador amigo) {
        TextView textoNombreAmigo = findViewById(R.id.texto_nombre_detalle_amigo);
        if (textoNombreAmigo != null && amigo != null) {
            textoNombreAmigo.setText(amigo.getTag());
        }

        // ===============================================
        // MAGIA: ESTADÍSTICAS MATEMÁTICAS DEL AMIGO
        // ===============================================

        int partidasAmigo = 45;
        int victoriasAmigo = 25;
        int derrotasAmigo = partidasAmigo - victoriasAmigo;
        int balasAmigo = (int)(Math.random() * 2000);
        float winrateAmigo = partidasAmigo > 0 ? ((float) victoriasAmigo / partidasAmigo) * 100 : 0f;

        TextView txtAmigoPartidas = findViewById(R.id.stat_amigo_partidas);
        TextView txtAmigoWinrate = findViewById(R.id.stat_amigo_winrate);
        TextView txtAmigoVictorias = findViewById(R.id.stat_amigo_victorias);
        TextView txtAmigoDerrotas = findViewById(R.id.stat_amigo_derrotas);
        TextView txtBalasAmigo = findViewById(R.id.stat_amigo_balas);
        if (txtBalasAmigo != null) txtBalasAmigo.setText(String.valueOf(balasAmigo));

        if (txtAmigoPartidas != null) txtAmigoPartidas.setText(String.valueOf(partidasAmigo));
        if (txtAmigoVictorias != null) txtAmigoVictorias.setText(String.valueOf(victoriasAmigo));
        if (txtAmigoDerrotas != null) txtAmigoDerrotas.setText(String.valueOf(derrotasAmigo));
        if (txtAmigoWinrate != null) txtAmigoWinrate.setText(String.format("%.1f%%", winrateAmigo));

        if (layoutListaAmigosContenedor != null && layoutDetalleAmigo != null) {
            layoutListaAmigosContenedor.setVisibility(View.GONE);
            layoutDetalleAmigo.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        GestorEstadisticas stats = GestorEstadisticas.getInstance();

        int partidas = stats.getPartidasJugadas();
        int victorias = stats.getVictorias();
        int derrotas = partidas - victorias;
        float winrate = partidas > 0 ? ((float) victorias / partidas) * 100 : 0f;

        TextView txtMioPartidas = findViewById(R.id.stat_mio_partidas);
        TextView txtMioWinrate = findViewById(R.id.stat_mio_winrate);
        TextView txtMioVictorias = findViewById(R.id.stat_mio_victorias);
        TextView txtMioDerrotas = findViewById(R.id.stat_mio_derrotas);

        if (txtMioPartidas != null) txtMioPartidas.setText(String.valueOf(partidas));
        if (txtMioVictorias != null) txtMioVictorias.setText(String.valueOf(victorias));
        if (txtMioDerrotas != null) txtMioDerrotas.setText(String.valueOf(derrotas));
        if (txtMioWinrate != null) txtMioWinrate.setText(String.format("%.1f%%", winrate));

    }

    private void cargarDatosPerfil() {
        OkHttpClient client = new OkHttpClient();

        // URL del endpoint (ajusta la IP si es necesario)
        String url = "http://10.0.2.2:8080/api/jugadores";

        // Obtenemos el token para la autenticación
        TokenManager tokenManager = new TokenManager(this);
        String jwt = tokenManager.getToken();

        if (jwt == null || jwt.isEmpty()) {
            return; // O redirigir al Login
        }

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + jwt)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() -> {
                    android.util.Log.e("API_PERFIL", "Error: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonData = response.body().string();
                    try {
                        org.json.JSONObject obj = new org.json.JSONObject(jsonData);

                        // Extraemos los datos del JSON
                        tagActual = obj.getString("tag");
                        nombreImagen = obj.optString("fotoPerfil", ""); // Nombre del recurso o URL
                        int balas = obj.getInt("balas");
                        int victorias = obj.getInt("victorias");
                        int derrotas = obj.getInt("derrotas");
                        int aciertos = obj.getInt("numAciertos");
                        int fallos = obj.getInt("numFallos"); // Por si los necesitas luego


                        // Calculamos datos derivados
                        int totalPartidas = victorias + derrotas;
                        float winrate = totalPartidas > 0 ? ((float) victorias / totalPartidas) * 100 : 0f;

                        // Actualizamos la UI en el hilo principal
                        runOnUiThread(() -> {
                            // 1. Nombre y Balas
                            if (textoNombreDatos != null) textoNombreDatos.setText(tagActual);

                            // Buscamos los TextViews de estadísticas (usando los IDs de tu XML)
                            TextView txtPartidas = findViewById(R.id.stat_mio_partidas);
                            TextView txtVictorias = findViewById(R.id.stat_mio_victorias);
                            TextView txtDerrotas = findViewById(R.id.stat_mio_derrotas);
                            TextView txtWinrate = findViewById(R.id.stat_mio_winrate);
                            TextView txtBalas = findViewById(R.id.texto_mis_balas); // Asegúrate de que este ID existe

                            if (txtPartidas != null) txtPartidas.setText(String.valueOf(totalPartidas));
                            if (txtVictorias != null) txtVictorias.setText(String.valueOf(victorias));
                            if (txtDerrotas != null) txtDerrotas.setText(String.valueOf(derrotas));
                            if (txtWinrate != null) txtWinrate.setText(String.format("%.1f%%", winrate));
                            if (txtBalas != null) txtBalas.setText(String.valueOf(balas));

                            // 2. Actualizar la foto de perfil si el servidor devuelve un nombre de recurso
                            if (!nombreImagen.isEmpty()) {
                                int resId = getResources().getIdentifier(nombreImagen, "drawable", getPackageName());
                                if (resId != 0) {
                                    ImageView imgPerfil = findViewById(R.id.icono_perfil_datos);
                                    if (imgPerfil != null) imgPerfil.setImageResource(resId);
                                }
                            }
                        });

                    } catch (org.json.JSONException e) {
                        android.util.Log.e("API_PERFIL", "Error parseando perfil", e);
                    }
                }
            }
        });
    }

    private void actualizarPerfilServidor(String nuevoTag, String nombreImagen) {
        OkHttpClient client = new OkHttpClient();
        final String url = "http://10.0.2.2:8080/api/jugadores";

        TokenManager tokenManager = new TokenManager(this);
        String jwt = tokenManager.getToken();

        // 1. Creamos el cuerpo de la petición en formato JSON
        org.json.JSONObject jsonBody = new org.json.JSONObject();
        try {
            jsonBody.put("tag", nuevoTag);
            jsonBody.put("fotoPerfil", nombreImagen);
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }

        okhttp3.RequestBody body = okhttp3.RequestBody.create(
                jsonBody.toString(),
                okhttp3.MediaType.parse("application/json; charset=utf-8")
        );

        // 2. Construimos la petición PUT
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .addHeader("Authorization", "Bearer " + jwt)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() ->
                        android.widget.Toast.makeText(PerfilActivity.this, "Error de red al guardar", android.widget.Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() ->
                            android.widget.Toast.makeText(PerfilActivity.this, "Perfil actualizado correctamente", android.widget.Toast.LENGTH_SHORT).show()
                    );
                } else {
                    // Si el código es 400, probablemente el "tag" ya existe o es inválido
                    runOnUiThread(() ->
                            android.widget.Toast.makeText(PerfilActivity.this, "Error: El nombre ya está en uso", android.widget.Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
}