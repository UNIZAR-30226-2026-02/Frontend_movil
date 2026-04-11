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
import com.example.secretpanda.data.model.GestorEstadisticas;
import com.example.secretpanda.ui.auth.LoginActivity;
import com.example.secretpanda.ui.home.GestorImagenes;

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
        cargarAmigosServidor();
        adaptador = new AmigoAdapter(misAmigos, amigoClickado -> {
            mostrarDetalleDe(amigoClickado);
        });
        recyclerAmigos.setAdapter(adaptador);

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
        Log.d("API_AMIGOS", "Iniciando petición a cargarAmigosServidor...");

        TokenManager tokenManager = new TokenManager(this);
        String jwt = tokenManager.getToken();

        // 1. Comprobamos si el token falla
        if (jwt == null || jwt.isEmpty()) {
            Log.e("API_AMIGOS", "Error: El token es nulo o está vacío. Abortando petición.");
            return;
        }

        OkHttpClient client = new OkHttpClient();
        String url = "http://10.0.2.2:8080/api/amigos/";

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + jwt)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                Log.e("API_AMIGOS", "Fallo catastrófico de red: " + e.getMessage());
                runOnUiThread(() -> android.widget.Toast.makeText(PerfilActivity.this, "Error de red al cargar amigos", android.widget.Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                // 2. Controlamos si el servidor devuelve error (401, 404, 500...)
                if (!response.isSuccessful()) {
                    Log.e("API_AMIGOS", "Respuesta del servidor fallida. Código: " + response.code());
                    return;
                }

                if (response.body() != null) {
                    // 3. LA REGLA DE ORO: Guardar el string en una variable
                    String jsonResponse = response.body().string();
                    Log.d("API_AMIGOS", "JSON recibido correctamente: " + jsonResponse);

                    try {
                        // Usamos la variable en lugar de volver a llamar a body().string()
                        org.json.JSONArray jsonArray = new org.json.JSONArray(jsonResponse);
                        java.util.List<Jugador> listaAmigosReales = new java.util.ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            org.json.JSONObject obj = jsonArray.getJSONObject(i);

                            String tag = obj.getString("tag");
                            String fotoPerfil = obj.optString("foto_perfil", "");
                            int victorias = obj.optInt("victorias", 0);
                            int numAciertos = obj.optInt("num_aciertos", 0);

                            Jugador amigo = new Jugador(tag);
                            amigo.setFotoPerfil(fotoPerfil);
                            amigo.setVictorias(victorias);
                            amigo.setNumAciertos(numAciertos);

                            listaAmigosReales.add(amigo);
                        }

                        runOnUiThread(() -> {
                            misAmigos = listaAmigosReales;
                            if (adaptador != null) {
                                adaptador.setListaAmigos(misAmigos);
                                adaptador.notifyDataSetChanged();
                            }
                        });

                    } catch (org.json.JSONException e) {
                        Log.e("API_AMIGOS", "Error parseando el JSON: " + jsonResponse, e);
                    }
                } else {
                    Log.w("API_AMIGOS", "La respuesta fue exitosa pero el cuerpo (body) estaba vacío.");
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

        inputNombre.setText(tagActual);
        btnCerrar.setOnClickListener(v -> dialog.dismiss());
        btnCambiarFoto.setOnClickListener(v -> mostrarDialogoElegirImagen(btnCambiarFoto));

        btnGuardar.setOnClickListener(v -> {
            String nuevoNombre = inputNombre.getText().toString().trim();
            if (!nuevoNombre.isEmpty()) {
                actualizarPerfilServidor(nuevoNombre, nombreImagen);
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

        // ESTADÍSTICAS MATEMÁTICAS DEL AMIGO

        int victoriasAmigo = amigo.getVictorias();

        // Enlazamos los textos
        TextView txtAmigoPartidas = findViewById(R.id.stat_amigo_partidas);
        TextView txtAmigoWinrate = findViewById(R.id.stat_amigo_winrate);
        TextView txtAmigoVictorias = findViewById(R.id.stat_amigo_victorias);
        TextView txtAmigoDerrotas = findViewById(R.id.stat_amigo_derrotas);
        TextView txtBalasAmigo = findViewById(R.id.stat_amigo_balas);

        // Ponemos el dato real
        if (txtAmigoVictorias != null) txtAmigoVictorias.setText(String.valueOf(victoriasAmigo));

        // Ponemos guiones en los datos que el servidor no nos proporciona
        if (txtBalasAmigo != null) txtBalasAmigo.setText("---");
        if (txtAmigoPartidas != null) txtAmigoPartidas.setText("---");
        if (txtAmigoDerrotas != null) txtAmigoDerrotas.setText("---");
        if (txtAmigoWinrate != null) txtAmigoWinrate.setText("---");


        // if (txtAmigoPartidas != null) txtAmigoPartidas.setText(String.valueOf(amigo.getNumAciertos()));

        // Mostramos la vista
        if (layoutListaAmigosContenedor != null && layoutDetalleAmigo != null) {
            layoutListaAmigosContenedor.setVisibility(View.GONE);
            layoutDetalleAmigo.setVisibility(View.VISIBLE);
        }
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

                        Log.d("API_PERFIL", "JSON recibido: " + jsonData);

                        // Extraemos los datos del JSON
                        tagActual = obj.getString("tag");
                        nombreImagen = obj.optString("foto_perfil", ""); // Nombre del recurso o URL
                        Log.d("API_PERFIL", "Nombre de la imagen: " + nombreImagen);
                        fotoPerfil.setBackgroundResource(GestorImagenes.obtenerImagenManual(nombreImagen));
                        int balas = obj.getInt("balas");
                        int victorias = obj.getInt("victorias");
                        int derrotas = obj.getInt("derrotas");
                        int aciertos = obj.getInt("num_aciertos");
                        int fallos = obj.getInt("num_fallos"); // Por si los necesitas luego


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
            jsonBody.put("foto_perfil", nombreImagen);
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
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        //Cambiamos la variable y el texto visual 
                        tagActual = nuevoTag;
                        if (textoNombreDatos != null) {
                            textoNombreDatos.setText(nuevoTag);
                        }

                        //Avisamos al Home
                        Intent intentDeVuelta = new Intent();
                        intentDeVuelta.putExtra("NOMBRE_ACTUALIZADO", nuevoTag);
                        setResult(RESULT_OK, intentDeVuelta);

                        // Recargamos estadísticas
                        cargarDatosPerfil();

                    } else {
                    }
                });
            }
        });
    }
}