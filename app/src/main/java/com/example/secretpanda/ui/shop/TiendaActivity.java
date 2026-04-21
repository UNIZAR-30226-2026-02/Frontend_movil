package com.example.secretpanda.ui.shop;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secretpanda.R;
import com.example.secretpanda.data.NetworkConfig;
import com.example.secretpanda.data.TokenManager;
import com.example.secretpanda.data.model.GestorEstadisticas;
import com.example.secretpanda.data.model.ItemPersonalizacion;
import com.example.secretpanda.data.model.Jugador;
import com.example.secretpanda.ui.EfectosManager;
import com.example.secretpanda.ui.customization.PersonalizacionActivity;
import com.example.secretpanda.ui.home.HomeActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class TiendaActivity extends AppCompatActivity {

    private RecyclerView recyclerBarajas, recyclerBordes, recyclerFondos;
    private TiendaAdapter adapterBarajas, adapterBordes, adapterFondos;
    private TextView txtSaldo;
    private List<ItemPersonalizacion> barajasTienda = new ArrayList<>();
    private List<ItemPersonalizacion> bordesTienda = new ArrayList<>();
    private List<ItemPersonalizacion> fondosTienda = new ArrayList<>();

    private String nombreUsuario;
    private String idGoogle;
    private String idGoogleEstable;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_tienda);

        idGoogle = getIntent().getStringExtra("ID_GOOGLE");
        idGoogleEstable = getIntent().getStringExtra("GOOGLE_ID_ESTABLE");

        txtSaldo = findViewById(R.id.txt_saldo_balas);
        configurarNavegacionInferior();

        recyclerBarajas = findViewById(R.id.recycler_tienda_barajas);
        recyclerBordes = findViewById(R.id.recycler_tienda_bordes);
        recyclerFondos = findViewById(R.id.recycler_tienda_fondos);

        adapterBarajas = new TiendaAdapter(barajasTienda, (item, position) -> mostrarPreviewCompra(item, position, adapterBarajas));
        adapterBordes = new TiendaAdapter(bordesTienda, (item, position) -> mostrarPreviewCompra(item, position, adapterBordes));
        adapterFondos = new TiendaAdapter(fondosTienda, (item, position) -> mostrarPreviewCompra(item, position, adapterFondos));

        recyclerBarajas.setAdapter(adapterBarajas);
        recyclerBordes.setAdapter(adapterBordes);
        recyclerFondos.setAdapter(adapterFondos);

        nombreUsuario = getIntent().getStringExtra("MI_NOMBRE_USUARIO");

        //actualizarTextoSaldo();
        cargarBalasServidor();
        cargarTemasTienda();
        cargarBordesYFondos();
    }


    private void actualizarTextoSaldo() {
        // LEEMOS LAS BALAS DEL JUGADOR REAL
        Jugador jugador = GestorEstadisticas.getInstance().getJugadorActual();
        if (txtSaldo != null && jugador != null) {
            txtSaldo.setText(String.valueOf(jugador.getBalas()));
        }
    }

    private void cargarTemasTienda() {
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        String url = NetworkConfig.BASE_URL + "/temas/activos";

        com.example.secretpanda.data.TokenManager tokenManager = new com.example.secretpanda.data.TokenManager(this);
        String jwt = tokenManager.getToken();
        if (jwt == null || jwt.isEmpty()) return;

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + jwt)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() -> android.widget.Toast.makeText(TiendaActivity.this, "Error de red al cargar la tienda", android.widget.Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonRespuesta = response.body().string();
                        org.json.JSONArray temasArray = new org.json.JSONArray(jsonRespuesta);

                        runOnUiThread(() -> {
                            barajasTienda.clear();
                            for (int i = 0; i < temasArray.length(); i++) {
                                try {
                                    org.json.JSONObject temaJson = temasArray.getJSONObject(i);
                                    int idTema = temaJson.optInt("id_tema", temaJson.optInt("id_tema", -1));
                                    String nombre = temaJson.optString("nombre", "Tema Desconocido");
                                    int precio = temaJson.optInt("precio_balas", temaJson.optInt("precio_balas", 0));
                                    boolean comprado = temaJson.optBoolean("comprado", false);
                                    String tipo = temaJson.optString("tipo", "baraja");

                                    com.example.secretpanda.data.model.ItemPersonalizacion item = new com.example.secretpanda.data.model.ItemPersonalizacion(
                                            nombre,
                                            !comprado,
                                            tipo,
                                            0,
                                            precio,
                                            "0" // Esto fuerza que tanto en la lista como en el popup se vea fondo_carta_gruesa
                                    );
                                    item.setId(idTema);
                                    barajasTienda.add(item);

                                } catch (Exception e) {
                                    android.util.Log.e("API_TIENDA", "Error procesando item", e);
                                }
                            }
                            adapterBarajas.notifyDataSetChanged();
                        });
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
        });
    }
    private void cargarBordesYFondos() {

        OkHttpClient client = new OkHttpClient();

        // URL del endpoint (ajusta la IP si es necesario)
        String url = NetworkConfig.BASE_URL + "/personalizaciones/activas";

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
                Log.d("API_TIENDA-2- Error", "Error de red al cargar la tienda");
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonRespuesta = response.body().string();
                        Log.d("API_TIENDA-2", "Respuesta JSON: " + jsonRespuesta);
                        org.json.JSONArray temasArray = new org.json.JSONArray(jsonRespuesta);

                        runOnUiThread(() -> {
                            bordesTienda.clear();
                            fondosTienda.clear();

                            for (int i = 0; i < temasArray.length(); i++) {
                                try {
                                    org.json.JSONObject temaJson = temasArray.getJSONObject(i);

                                    Log.d("API_TIENDA-2", "Tema JSON: " + temaJson.toString());

                                    int idTema = temaJson.optInt("id_personalizacion", temaJson.optInt("id_tema", -1));
                                    String nombre = temaJson.optString("nombre", "Tema Desconocido");
                                    int posicionGuion = nombre.indexOf("_");
                                    String nombrePersonalizacion = nombre.substring(0, posicionGuion);
                                    int precio = temaJson.optInt("precio_bala", 0);
                                    boolean comprado = temaJson.optBoolean("comprado", false);
                                    String tipo = temaJson.optString("tipo", "baraja");
                                    String valor = temaJson.optString("valor_visual", "0");

                                        ItemPersonalizacion item = new ItemPersonalizacion(
                                                nombrePersonalizacion,
                                                !comprado,        // bloqueado por defecto en la tienda
                                                tipo ,    // tipo
                                                0,           // icono (0 para que ponga la carta por defecto)
                                                precio,       // precio en balas
                                                valor
                                        );
                                        item.setId(idTema);

                                        if (tipo.equalsIgnoreCase("carta")) bordesTienda.add(item);
                                        else fondosTienda.add(item);


                                } catch (Exception e) {
                                    android.util.Log.e("API_TIENDA", "Error procesando item", e);
                                }
                            }
                            adapterBordes.notifyDataSetChanged();
                            adapterFondos.notifyDataSetChanged();
                        });
                    } catch (Exception e) {
                        android.util.Log.e("API_TIENDA", "Error procesando JSON", e);
                    }
                }
            }
        });
    }

    private void mostrarPreviewCompra(ItemPersonalizacion item, int position, TiendaAdapter adaptadorOrigen) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_preview_tienda, null);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setView(dialogView);
        android.app.AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView txtTitulo = dialogView.findViewById(R.id.txt_preview_titulo_tienda);
        ImageView btnCerrar = dialogView.findViewById(R.id.btn_cerrar_preview_tienda);

        // Nuevas referencias al XML
        ImageView imgPreview = dialogView.findViewById(R.id.img_preview_item_tienda);
        FrameLayout vistaCartasPreview = dialogView.findViewById(R.id.vista_cartas_preview);
        ImageView imgCartaFrontalPreview = dialogView.findViewById(R.id.img_carta_frontal_preview);

        LinearLayout btnComprar = dialogView.findViewById(R.id.btn_comprar_preview);
        TextView txtPrecioBotonArriba = dialogView.findViewById(R.id.txt_precio_boton_compra_arriba);
        TextView txtPrecioBoton = dialogView.findViewById(R.id.txt_precio_boton_compra);
        TextView txtFeedback = dialogView.findViewById(R.id.txt_feedback_tienda);

        txtTitulo.setText(item.getNombre());
        txtPrecioBoton.setText(String.valueOf(item.getPrecio()));

        if (item.getTipo().equals("baraja")) {
            if (vistaCartasPreview != null) vistaCartasPreview.setVisibility(View.VISIBLE);
            if (imgPreview != null) imgPreview.setVisibility(View.GONE);

            // Forzamos la carta normal para la baraja
            if (imgCartaFrontalPreview != null) {
                imgCartaFrontalPreview.setImageResource(R.drawable.fondo_carta_gruesa);
            }
        } else {
            if (vistaCartasPreview != null) vistaCartasPreview.setVisibility(View.GONE);
            if (imgPreview != null) {
                imgPreview.setVisibility(View.VISIBLE);
                if (item.getIconoResId() != 0) {
                    imgPreview.setImageResource(item.getIconoResId());
                } else if (!Objects.equals(item.getValor(), "0")) {
                    try {
                        imgPreview.setBackgroundColor(Color.parseColor("#" + item.getValor()));
                    } catch (Exception e) {
                        imgPreview.setImageResource(R.drawable.fondo_carta_gruesa);
                    }
                } else {
                    imgPreview.setImageResource(R.drawable.fondo_carta_gruesa);
                }
            }
        }

        btnComprar.setOnClickListener(v -> {
            okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
            String url = NetworkConfig.BASE_URL + "/tienda/comprar/" + idGoogleEstable;

            TokenManager tokenManager = new TokenManager(this);
            String jwt = tokenManager.getToken();

            org.json.JSONObject jsonBody = new org.json.JSONObject();
            try {
                if(item.getTipo().equals("baraja"))
                    jsonBody.put("id_tema", item.getId());
                else
                    jsonBody.put("id_personalizacion", item.getId());
            } catch (Exception e) { e.printStackTrace(); }

            okhttp3.RequestBody body = okhttp3.RequestBody.create(jsonBody.toString(), okhttp3.MediaType.parse("application/json; charset=utf-8"));

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + jwt)
                    .build();

            btnComprar.setEnabled(false);
            txtPrecioBotonArriba.setText("");
            txtPrecioBoton.setText("Comprando...");

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, java.io.IOException e) {
                    runOnUiThread(() -> {
                        txtFeedback.setVisibility(View.VISIBLE);
                        txtFeedback.setText("Error de red");
                        txtFeedback.setTextColor(Color.RED);
                        btnComprar.setEnabled(true);
                    });
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                    String respuestaCuerpo = response.body() != null ? response.body().string() : "";

                    runOnUiThread(() -> {
                        txtFeedback.setVisibility(View.VISIBLE);
                        if (response.isSuccessful()) {
                            EfectosManager.reproducir(getApplicationContext(), R.raw.sonido_disparo);
                            try {
                                org.json.JSONObject resJson = new org.json.JSONObject(respuestaCuerpo);
                                int balasRestantes = resJson.getInt("balas");

                                // Actualizar saldo y UI
                                Jugador jugador = GestorEstadisticas.getInstance().getJugadorActual();
                                if (jugador != null) jugador.setBalas(balasRestantes);
                                actualizarTextoSaldo();
                                item.setBloqueado(false);
                                adaptadorOrigen.notifyItemChanged(position);

                                // Feedback de éxito
                                txtFeedback.setText("✔ ¡Compra realizada!");
                                txtFeedback.setTextColor(Color.parseColor("#4CAF50")); // Verde
                                btnComprar.setVisibility(View.GONE); // Ocultamos el botón tras comprar

                                new android.os.Handler().postDelayed(dialog::dismiss, 1500);

                            } catch (Exception e) { e.printStackTrace(); }
                        } else {
                            btnComprar.setEnabled(true);
                            txtFeedback.setTextColor(Color.RED);
                            if (response.code() == 400) {
                                txtFeedback.setText("✘ No tienes suficientes balas");
                            } else {
                                txtFeedback.setText("Error: " + response.code());
                            }
                        }
                    });
                }
            });
        });

        btnCerrar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
    private void configurarNavegacionInferior() {
        LinearLayout btnNavTienda = findViewById(R.id.nav_tienda);
        if (btnNavTienda != null) btnNavTienda.setSelected(true);

        LinearLayout btnNavInicio = findViewById(R.id.nav_inicio);
        if (btnNavInicio != null) btnNavInicio.setOnClickListener(v -> {
            startActivity(new Intent(TiendaActivity.this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            overridePendingTransition(0, 0);
        });

        LinearLayout btnNavPersonalizacion = findViewById(R.id.nav_personalizar);
        if (btnNavPersonalizacion != null) btnNavPersonalizacion.setOnClickListener(v -> {
            startActivity(new Intent(TiendaActivity.this, PersonalizacionActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            overridePendingTransition(0, 0);
        });
    }

    private void cargarBalasServidor() {
        TokenManager tokenManager = new TokenManager(this);
        String jwt = tokenManager.getToken();
        if (jwt == null) return;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(NetworkConfig.BASE_URL + "/jugadores")
                .get()
                .addHeader("Authorization", "Bearer " + jwt)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {}

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        org.json.JSONObject obj = new org.json.JSONObject(response.body().string());
                        int balasReales = obj.optInt("balas", 0);

                        runOnUiThread(() -> {
                            if (txtSaldo != null) {
                                txtSaldo.setText(String.valueOf(balasReales));
                            }
                            // Actualizamos el singleton local por coherencia
                            Jugador jugador = GestorEstadisticas.getInstance().getJugadorActual();
                            if (jugador != null) jugador.setBalas(balasReales);
                        });
                    } catch (org.json.JSONException e) { e.printStackTrace(); }
                }
            }
        });
    }
}