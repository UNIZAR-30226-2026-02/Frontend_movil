package com.example.secretpanda.ui.shop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secretpanda.R;
import com.example.secretpanda.data.TokenManager;
import com.example.secretpanda.data.model.GestorEstadisticas;
import com.example.secretpanda.data.model.InventarioGlobal;
import com.example.secretpanda.data.model.ItemPersonalizacion;
import com.example.secretpanda.data.model.Jugador;
import com.example.secretpanda.ui.customization.PersonalizacionActivity;
import com.example.secretpanda.ui.home.HomeActivity;

import java.util.ArrayList;
import java.util.List;

public class TiendaActivity extends AppCompatActivity {

    private RecyclerView recyclerBarajas, recyclerBordes, recyclerFondos;
    private TiendaAdapter adapterBarajas, adapterBordes, adapterFondos;
    private TextView txtSaldo;
    private List<ItemPersonalizacion> barajasTienda = new ArrayList<>();
    private List<ItemPersonalizacion> bordesTienda = new ArrayList<>();
    private List<ItemPersonalizacion> fondosTienda = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_tienda);

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizarTextoSaldo();
        cargarDatosTienda();
    }

    private void actualizarTextoSaldo() {
        // LEEMOS LAS BALAS DEL JUGADOR REAL
        Jugador jugador = GestorEstadisticas.getInstance().getJugadorActual();
        if (txtSaldo != null && jugador != null) {
            txtSaldo.setText(String.valueOf(jugador.getBalas()));
        }
    }

    private void cargarDatosTienda() {
        /*
        //Esto es hardcodeado
        List<ItemPersonalizacion> todos = InventarioGlobal.getInstance().getTodosLosItems();

        List<ItemPersonalizacion> barajasTienda = new ArrayList<>();
        List<ItemPersonalizacion> bordesTienda = new ArrayList<>();
        List<ItemPersonalizacion> fondosTienda = new ArrayList<>();

        for (ItemPersonalizacion item : todos) {
            if (item.getPrecio() > 0) {
                if (item.getTipo().equals("baraja")) barajasTienda.add(item);
                else if (item.getTipo().equals("borde")) bordesTienda.add(item);
                else if (item.getTipo().equals("fondo")) fondosTienda.add(item);
            }
        }

        adapterBarajas = new TiendaAdapter(barajasTienda, (item, position) -> mostrarPreviewCompra(item, position, adapterBarajas));
        adapterBordes = new TiendaAdapter(bordesTienda, (item, position) -> mostrarPreviewCompra(item, position, adapterBordes));
        adapterFondos = new TiendaAdapter(fondosTienda, (item, position) -> mostrarPreviewCompra(item, position, adapterFondos));

        recyclerBarajas.setAdapter(adapterBarajas);
        recyclerBordes.setAdapter(adapterBordes);
        recyclerFondos.setAdapter(adapterFondos);*/

        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        String url = "http://10.0.2.2:8080/api/temas/activos";

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() -> Toast.makeText(TiendaActivity.this, "Error de red al cargar la tienda", Toast.LENGTH_SHORT).show());
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

                                    int idTema = temaJson.optInt("id_tema", temaJson.optInt("idTema", -1));
                                    String nombre = temaJson.optString("nombre", "Tema Desconocido");
                                    int precio = temaJson.optInt("precioBalas", temaJson.optInt("precio_balas", 0));

                                    ItemPersonalizacion item = new ItemPersonalizacion(
                                            nombre,
                                            true,        // bloqueado por defecto en la tienda
                                            "baraja",    // tipo
                                            0,           // icono (0 para que ponga la carta por defecto)
                                            precio       // precio en balas
                                    );
                                    item.setId(idTema);

                                    barajasTienda.add(item);
                                } catch (Exception e) {
                                    android.util.Log.e("API_TIENDA", "Error procesando item", e);
                                }
                            }
                            adapterBarajas.notifyDataSetChanged();
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
        ImageView imgPreview = dialogView.findViewById(R.id.img_preview_item_tienda);
        LinearLayout btnComprar = dialogView.findViewById(R.id.btn_comprar_preview);
        TextView txtPrecioBoton = dialogView.findViewById(R.id.txt_precio_boton_compra);

        txtTitulo.setText(item.getNombre());
        txtPrecioBoton.setText(String.valueOf(item.getPrecio()));

        if (item.getIconoResId() != 0) imgPreview.setImageResource(item.getIconoResId());
        else imgPreview.setImageResource(R.drawable.fondo_carta_gruesa);

        btnComprar.setOnClickListener(v -> {
            /*
            //hardcode
            Jugador jugador = GestorEstadisticas.getInstance().getJugadorActual();

            if (jugador.getBalas() >= item.getPrecio()) {
                // 1. Restamos balas al Jugador y actualizamos marcador
                jugador.setBalas(jugador.getBalas() - item.getPrecio());
                actualizarTextoSaldo();

                // 2. Desbloqueamos el ítem
                item.setBloqueado(false);

                // 3. Avisamos al adaptador
                adaptadorOrigen.notifyItemChanged(position);

                Toast.makeText(this, "¡Has adquirido " + item.getNombre() + "!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "No tienes suficientes balas.", Toast.LENGTH_SHORT).show();
            }*/
            String miIdGoogle = "MiNombreDeUsuario"; // ⚠️ Pon aquí tu variable real

            okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
            String url = "http://10.0.2.2:8080/api/tienda/comprar/" + miIdGoogle;

            TokenManager tokenManager = new TokenManager(this);
            String jwt = tokenManager.getToken();

            org.json.JSONObject jsonBody = new org.json.JSONObject();
            try {
                jsonBody.put("idTema", item.getId()); // O "id_tema" según tu backend
            } catch (Exception e) { e.printStackTrace(); }

            okhttp3.RequestBody body = okhttp3.RequestBody.create(jsonBody.toString(), okhttp3.MediaType.parse("application/json; charset=utf-8"));

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + jwt)
                    .build();

            btnComprar.setEnabled(false);
            txtPrecioBoton.setText("Comprando...");

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, java.io.IOException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(TiendaActivity.this, "Error de red al comprar", Toast.LENGTH_SHORT).show();
                        btnComprar.setEnabled(true);
                        txtPrecioBoton.setText(String.valueOf(item.getPrecio()));
                    });
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                    String respuestaCuerpo = response.body() != null ? response.body().string() : "";

                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            try {
                                org.json.JSONObject resJson = new org.json.JSONObject(respuestaCuerpo);
                                int balasRestantes = resJson.optInt("balas", -1);

                                if (balasRestantes != -1) {
                                    Jugador jugador = GestorEstadisticas.getInstance().getJugadorActual();
                                    jugador.setBalas(balasRestantes);
                                    actualizarTextoSaldo();

                                    item.setBloqueado(false);
                                    adaptadorOrigen.notifyItemChanged(position);

                                    Toast.makeText(TiendaActivity.this, "¡Has adquirido " + item.getNombre() + "!", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                            } catch (Exception e) {}
                        } else {
                            Toast.makeText(TiendaActivity.this, "Error al comprar. ¿Tienes suficientes balas?", Toast.LENGTH_LONG).show();
                            btnComprar.setEnabled(true);
                            txtPrecioBoton.setText(String.valueOf(item.getPrecio()));
                        }
                    });
                }
            });
        });

        btnCerrar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void configurarNavegacionInferior() {
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
}