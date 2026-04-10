package com.example.secretpanda.ui.home;
import com.example.secretpanda.data.model.PartidaHistorial;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secretpanda.R;
import com.example.secretpanda.data.TokenManager;
import com.example.secretpanda.data.model.Jugador;
import com.example.secretpanda.ui.home.classification.ClasificacionActivity; // Importamos la nueva pantalla

import com.example.secretpanda.ui.home.achivements.LogrosActivity;
import com.example.secretpanda.ui.home.profile.PerfilActivity;
import com.example.secretpanda.ui.customization.PersonalizacionActivity;
import com.example.secretpanda.ui.shop.TiendaActivity;
import com.example.secretpanda.ui.game.join.UnirseMisionActivity;
import com.example.secretpanda.ui.game.createMatch.CrearMisionOpcionesActivity;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;


public class HomeActivity extends AppCompatActivity {

    private Jugador jugadorActual;
    private ActivityResultLauncher<Intent> perfilLauncher;

    private String nombreUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        nombreUsuario = getIntent().getStringExtra("MI_NOMBRE_USUARIO");

        setContentView(R.layout.home);

        // RECIBIR EL JUGADOR Y CAMBIAR EL SALUDO
        TextView textoSaludo = findViewById(R.id.texto_saludo_home);


        if (textoSaludo != null) {
            textoSaludo.setText("Hola, " + nombreUsuario);
        }

        // CAPTURAR BOTONES PRINCIPALES
        Button btnNuevaMision = findViewById(R.id.btn_nueva_mision);
        Button btnUneteMision = findViewById(R.id.btn_unete_mision);

        // El botón de las 3 rayitas
        ImageView btnMenuOpciones = findViewById(R.id.btn_menu_opciones);

        ImageView btnClasificacion = findViewById(R.id.btn_clasificacion);

        ImageView btnPerfil = findViewById(R.id.btn_perfil);
        perfilLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // Recogemos el texto nuevo
                        String nombreActualizado = result.getData().getStringExtra("NOMBRE_ACTUALIZADO");

                        if (nombreActualizado != null) {
                            // Lo guardamos en nuestro objeto
                            nombreUsuario = nombreActualizado;
                            textoSaludo.setText("Hola, " + nombreActualizado);
                        }
                    }
                }
        );

        btnPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, PerfilActivity.class);

            // PASAMOS SOLO EL STRING (EL NOMBRE)
            intent.putExtra("NOMBRE_JUGADOR", nombreUsuario);
            // Abrimos la pantalla
            perfilLauncher.launch(intent);
        });

        btnNuevaMision.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CrearMisionOpcionesActivity.class);
            intent.putExtra("MI_NOMBRE_USUARIO", nombreUsuario);
            startActivity(intent);
            overridePendingTransition(0, 0); // Evitamos la animación por defecto
        });
        if (btnUneteMision != null) {
            btnUneteMision.setOnClickListener(v -> {
                // Abrir la pantalla de Tienda
                android.content.Intent intent = new android.content.Intent(HomeActivity.this, UnirseMisionActivity.class);
                intent.putExtra("MI_NOMBRE_USUARIO", nombreUsuario);
                startActivity(intent);

                overridePendingTransition(0, 0);
            });
        }

        btnMenuOpciones.setOnClickListener(v -> mostrarMenuPersonalizado(v));

        if (btnClasificacion != null) {
            btnClasificacion.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, ClasificacionActivity.class);
                startActivity(intent);
            });
        }



        // Buscar el botón de la tienda en la barra inferior
        View btnNavTienda = findViewById(R.id.nav_tienda);

        if (btnNavTienda != null) {
            btnNavTienda.setOnClickListener(v -> {
                // Abrir la pantalla de Tienda
                android.content.Intent intent = new android.content.Intent(HomeActivity.this, TiendaActivity.class);
                startActivity(intent);

                overridePendingTransition(0, 0);
            });
        }
        View btnNavPersonalizacion = findViewById(R.id.nav_personalizar);
        if (btnNavPersonalizacion != null) {
            btnNavPersonalizacion.setOnClickListener(v -> {
                // Abrir la pantalla de Tienda
                android.content.Intent intent = new android.content.Intent(HomeActivity.this, PersonalizacionActivity.class);
                startActivity(intent);

                overridePendingTransition(0, 0);
            });
        }
        View btnLogros = findViewById(R.id.btn_logros);
        if (btnLogros != null) {
            btnLogros.setOnClickListener(v -> {
                // Abrir la pantalla de Tienda
                android.content.Intent intent = new android.content.Intent(HomeActivity.this, LogrosActivity.class);
                startActivity(intent);

                overridePendingTransition(0, 0);
            });
        }


    }

    // LÓGICA DEL MENÚ DESPLEGABLE DE LA ESQUINA
    private void mostrarMenuPersonalizado(View vistaBoton) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_menu_personalizado, null);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        Button btnMusica = popupView.findViewById(R.id.btn_opcion_musica);
        Button btnComoJugar = popupView.findViewById(R.id.btn_opcion_como_jugar);
        Button btnHistorial = popupView.findViewById(R.id.btn_opcion_historial);

        btnMusica.setOnClickListener(v -> {
            popupWindow.dismiss();
            mostrarDialogoMusica();
        });

        btnComoJugar.setOnClickListener(v -> {
            popupWindow.dismiss();
            mostrarDialogoComoJugar();
        });

        btnHistorial.setOnClickListener(v -> {
            popupWindow.dismiss();
            mostrarDialogoHistorial();
        });

        popupWindow.showAsDropDown(vistaBoton, -350, 10);
    }

    // MÉTODOS DE LOS POPUPS

    private void mostrarDialogoMusica() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_musica, null);
        AlertDialog dialog = crearDialogoBase(dialogView);

        ImageView btnCerrar = dialogView.findViewById(R.id.btn_cerrar_popup);
        btnCerrar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void mostrarDialogoComoJugar() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_como_jugar, null);
        AlertDialog dialog = crearDialogoBase(dialogView);

        ImageView btnCerrar = dialogView.findViewById(R.id.btn_cerrar_popup);
        btnCerrar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void mostrarDialogoHistorial() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_historial, null);
        AlertDialog dialog = crearDialogoBase(dialogView);

        // 1. Configurar RecyclerView (Asegúrate de tener un RecyclerView con este ID en dialog_historial.xml)
        RecyclerView recycler = dialogView.findViewById(R.id.recycler_historial);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        // Creamos el adaptador (necesitarás crear esta clase HistorialAdapter similar a las anteriores)
        HistorialAdapter adapter = new HistorialAdapter(new ArrayList<>());
        recycler.setAdapter(adapter);

        // 2. CARGA AUTOMÁTICA: Llamamos al servidor nada más abrir
        cargarHistorialServidor(adapter);

        ImageView btnCerrar = dialogView.findViewById(R.id.btn_cerrar_popup);
        btnCerrar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private AlertDialog crearDialogoBase(View dialogView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }
    @Override
    protected void onResume() {
        super.onResume();

        cargarBalasReales();
    }
    private void cargarBalasReales() {
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        String token = new com.example.secretpanda.data.TokenManager(this).getToken();
        if (token == null) return;

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("http://10.0.2.2:8080/api/jugadores")
                .get()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {}

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        org.json.JSONObject obj = new org.json.JSONObject(response.body().string());
                        int balas = obj.optInt("balas", 0);

                        runOnUiThread(() -> {
                            android.widget.TextView txtBalas = findViewById(R.id.txt_balas_home);
                            if (txtBalas != null) txtBalas.setText(String.valueOf(balas));
                        });
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
        });
    }
    private void cargarHistorialServidor(HistorialAdapter adapter) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://10.0.2.2:8080/api/jugadores/historial";

        TokenManager tokenManager = new TokenManager(this);
        String jwt = tokenManager.getToken();
        if (jwt == null) return;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + jwt)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() -> Log.e("API_HISTORIAL", "Error al cargar historial"));
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonData = response.body().string();
                    try {
                        org.json.JSONArray array = new org.json.JSONArray(jsonData);
                        List<PartidaHistorial> lista = new java.util.ArrayList<>();

                        for (int i = 0; i < array.length(); i++) {
                            org.json.JSONObject obj = array.getJSONObject(i);
                            PartidaHistorial ph = new PartidaHistorial();
                            ph.id_partida = obj.getInt("id_partida");
                            ph.codigo_partida = obj.getString("codigo_partida");
                            ph.fechaFin = obj.optString("fecha_fin", "---");
                            ph.equipo = obj.getString("equipo");
                            ph.rol = obj.getString("rol");
                            ph.rojoGana = obj.getBoolean("rojo_gana");
                            ph.abandono = obj.getBoolean("abandono");

                            // Solo procesamos aciertos/fallos si es agente (RF-4)
                            if (ph.rol.equalsIgnoreCase("agente")) {
                                ph.numAciertos = obj.optInt("num_aciertos", 0);
                                ph.numFallos = obj.optInt("num_fallos", 0);
                            }
                            lista.add(ph);
                        }

                        runOnUiThread(() -> adapter.setLista(lista));

                    } catch (org.json.JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

}