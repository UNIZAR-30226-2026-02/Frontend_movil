
package com.example.secretpanda.ui.customization;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secretpanda.R;
import com.example.secretpanda.data.NetworkConfig;
import com.example.secretpanda.data.TokenManager;
import com.example.secretpanda.ui.audio.EfectosManager;
import com.example.secretpanda.ui.home.HomeActivity;
import com.example.secretpanda.data.model.ItemPersonalizacion;
import com.example.secretpanda.data.model.InventarioGlobal;
import com.example.secretpanda.ui.shop.TiendaActivity;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class PersonalizacionActivity extends AppCompatActivity {

    private LinearLayout tabBarajas, tabBordes, tabFondos;
    private TextView txtTabBarajas, txtTabBordes, txtTabFondos;

    private TextView txtSeccionActual, txtTematicaSeleccionada, txtVacioPosesion;
    private LinearLayout layoutTematicaSeleccionada;

    private RecyclerView recyclerPosesion, recyclerBloqueados;

    private List<ItemPersonalizacion> posesion = new ArrayList<>();
    private List<ItemPersonalizacion> bloqueados = new ArrayList<>();

    private PersonalizacionAdapter adapterPosesion;
    private PersonalizacionAdapter adapterBloqueados;

    private String idGoogleEstable;
    private TextView txtVacioBloqueados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_personalizacion);

        idGoogleEstable = getIntent().getStringExtra("GOOGLE_ID_ESTABLE");

        // 1. PRIMERO inicializamos todos los elementos del diseño (IDs)
        txtSeccionActual = findViewById(R.id.txt_seccion_actual);
        txtTematicaSeleccionada = findViewById(R.id.txt_tematica_seleccionada);
        txtVacioPosesion = findViewById(R.id.txt_vacio_posesion);
        txtVacioBloqueados = findViewById(R.id.txt_vacio_bloqueados);
        layoutTematicaSeleccionada = findViewById(R.id.layout_tematica_seleccionada);

        tabBarajas = findViewById(R.id.tab_barajas);
        tabBordes = findViewById(R.id.tab_bordes);
        tabFondos = findViewById(R.id.tab_fondos);
        txtTabBarajas = findViewById(R.id.txt_tab_barajas);
        txtTabBordes = findViewById(R.id.txt_tab_bordes);
        txtTabFondos = findViewById(R.id.txt_tab_fondos);

        recyclerPosesion = findViewById(R.id.recycler_posesion);
        recyclerBloqueados = findViewById(R.id.recycler_bloqueados);

        // 2. DESPUÉS configuramos el estado inicial
        configurarNavegacionInferior();

        txtTabBarajas.setVisibility(View.VISIBLE);
        txtTabBordes.setVisibility(View.VISIBLE);
        txtTabFondos.setVisibility(View.VISIBLE);

        txtTabBarajas.setTextColor(Color.parseColor("#d4b878")); // Oro
        txtTabBordes.setTextColor(Color.parseColor("#8a7a60"));  // Marrón
        txtTabFondos.setTextColor(Color.parseColor("#8a7a60"));  // Marrón

        txtSeccionActual.setText("Temática barajas");
        tabBarajas.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#3e3224")));

        recyclerPosesion.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerBloqueados.setLayoutManager(new GridLayoutManager(this, 3));

        // Listeners
        tabBarajas.setOnClickListener(v -> {
            EfectosManager.reproducir(getApplicationContext(), R.raw.sonido_click);
            seleccionarPestana(tabBarajas, txtTabBarajas, tabBordes, txtTabBordes, tabFondos, txtTabFondos, "Temática barajas");
        });
        tabBordes.setOnClickListener(v -> {
            EfectosManager.reproducir(getApplicationContext(), R.raw.sonido_click);
            seleccionarPestana(tabBordes, txtTabBordes, tabBarajas, txtTabBarajas, tabFondos, txtTabFondos, "Temática borde");
        });
        tabFondos.setOnClickListener(v -> {
            EfectosManager.reproducir(getApplicationContext(), R.raw.sonido_click);
            seleccionarPestana(tabFondos, txtTabFondos, tabBarajas, txtTabBarajas, tabBordes, txtTabBordes, "Temática fondo");
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (txtSeccionActual != null) {
            String tituloActual = txtSeccionActual.getText().toString().toLowerCase();

            if (tituloActual.contains("barajas")) cargarTemasServidor();
            else if (tituloActual.contains("borde")) cargarInventarioServidor("carta");
            else cargarInventarioServidor("tablero");
        }
    }
    private void seleccionarPestana(LinearLayout activa, TextView txtActivo,
                                    LinearLayout inactiva1, TextView txtInactivo1,
                                    LinearLayout inactiva2, TextView txtInactivo2,
                                    String titulo) {

        txtSeccionActual.setText(titulo);

        txtActivo.setVisibility(View.VISIBLE);
        txtInactivo1.setVisibility(View.VISIBLE);
        txtInactivo2.setVisibility(View.VISIBLE);

        txtActivo.setTextColor(android.graphics.Color.parseColor("#d4b878"));
        txtActivo.setTextSize(12);

        txtInactivo1.setTextColor(android.graphics.Color.parseColor("#8a7a60"));
        txtInactivo1.setTextSize(10);
        txtInactivo2.setTextColor(android.graphics.Color.parseColor("#8a7a60"));
        txtInactivo2.setTextSize(10);

        activa.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#3e3224")));
        inactiva1.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1e1810")));
        inactiva2.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1e1810")));

        animarAltura(activa, 80);
        animarAltura(inactiva1, 65);
        animarAltura(inactiva2, 65);

        String tituloMin = titulo.toLowerCase();

        if (tituloMin.contains("barajas")) cargarTemasServidor();
        else if (tituloMin.contains("borde")) cargarInventarioServidor("carta");
        else cargarInventarioServidor("tablero");
    }
private void animarAltura(View vista, int altoFinalDp) {
        float density = getResources().getDisplayMetrics().density;
        int finalPx = (int) (altoFinalDp * density);

        if (vista.getHeight() == finalPx) return;

        ValueAnimator animator = ValueAnimator.ofInt(vista.getHeight(), finalPx);
        animator.setDuration(200);
        animator.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = vista.getLayoutParams();
            params.height = (int) animation.getAnimatedValue();
            vista.setLayoutParams(params);
        });
        animator.start();
    }

    private void cargarDatos(String categoria) {
        List<ItemPersonalizacion> todos = InventarioGlobal.getInstance().getTodosLosItems();

        List<ItemPersonalizacion> posesion = new ArrayList<>();
        List<ItemPersonalizacion> bloqueados = new ArrayList<>();

        // CUIDADO: La comprobación también debe ir en singular
        boolean permiteSeleccion = !categoria.equals("baraja");

        if (categoria.equals("baraja")) {
            layoutTematicaSeleccionada.setVisibility(View.GONE);
        } else {
            layoutTematicaSeleccionada.setVisibility(View.VISIBLE);
        }

        // Buscar en la Base de Datos
        for (ItemPersonalizacion item : todos) {
            if (item.getTipo().equals(categoria)) {
                if (item.isBloqueado()) {
                    bloqueados.add(item);
                } else {
                    posesion.add(item);
                }
            }
        }

        if (posesion.isEmpty()) {
            txtVacioPosesion.setVisibility(View.VISIBLE);
            recyclerPosesion.setVisibility(View.GONE);
            txtTematicaSeleccionada.setText("Ninguna");
        } else {
            txtVacioPosesion.setVisibility(View.GONE);
            recyclerPosesion.setVisibility(View.VISIBLE);
            if (permiteSeleccion) txtTematicaSeleccionada.setText(posesion.get(0).getNombre());
        }

        boolean finalPermiteSeleccion = permiteSeleccion;

        this.adapterPosesion = new PersonalizacionAdapter(posesion, false, permiteSeleccion, (item, position) -> {
            mostrarPreview(item, position, finalPermiteSeleccion);
        });

        this.adapterBloqueados = new PersonalizacionAdapter(bloqueados, true, false, null);

        recyclerPosesion.setAdapter(adapterPosesion);
        recyclerBloqueados.setAdapter(adapterBloqueados);
    }

    private void mostrarPreview(ItemPersonalizacion item, int position, boolean permiteSeleccion) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_preview_personalizacion, null);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setView(dialogView);
        android.app.AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView txtTitulo = dialogView.findViewById(R.id.txt_preview_titulo);
        ImageView btnCerrar = dialogView.findViewById(R.id.btn_cerrar_preview);

        // Nuevas vistas
        ImageView imgPreview = dialogView.findViewById(R.id.img_preview_item);
        android.widget.FrameLayout vistaCartasPreview = dialogView.findViewById(R.id.vista_cartas_preview);
        ImageView imgCartaFrontalPreview = dialogView.findViewById(R.id.img_carta_frontal_preview);
        TextView txtEmojiPreview = dialogView.findViewById(R.id.txt_emoji_preview);
        android.widget.Button btnSeleccionar = dialogView.findViewById(R.id.btn_seleccionar_preview);

        txtTitulo.setText(item.getNombre());

        if (item.getTipo().equals("baraja")) {
            vistaCartasPreview.setVisibility(View.VISIBLE);
            imgPreview.setVisibility(View.GONE);
            if (txtEmojiPreview != null) {
                txtEmojiPreview.setText(emojiPaquete(item.getNombre()));
                txtEmojiPreview.setVisibility(View.VISIBLE);
            }
            if (vistaCartasPreview != null) vistaCartasPreview.setVisibility(View.VISIBLE);
            if (imgPreview != null) imgPreview.setVisibility(View.GONE);

            if (imgCartaFrontalPreview != null) {
                imgCartaFrontalPreview.setImageResource(R.drawable.fondo_carta_gruesa);
            }
        } else {
            vistaCartasPreview.setVisibility(View.GONE);
            if (txtEmojiPreview != null) txtEmojiPreview.setVisibility(View.GONE);
            imgPreview.setVisibility(View.VISIBLE);
            if (vistaCartasPreview != null) vistaCartasPreview.setVisibility(View.GONE);
            if (imgPreview != null) {
                imgPreview.setVisibility(View.VISIBLE);
                if (item.getIconoResId() != 0) {
                    imgPreview.setImageResource(item.getIconoResId());
                } else if (item.getValor() != null && !item.getValor().equals("0")) {
                    try {
                        imgPreview.setBackgroundColor(android.graphics.Color.parseColor("#" + item.getValor()));
                    } catch (Exception e) {
                        imgPreview.setImageResource(R.drawable.fondo_carta_gruesa);
                    }
                } else {
                    imgPreview.setImageResource(R.drawable.fondo_carta_gruesa);
                }
            }
        }

        if (!permiteSeleccion || item.getTipo().equals("baraja")) {
            btnSeleccionar.setVisibility(View.GONE);
        } else {
            btnSeleccionar.setVisibility(View.VISIBLE);
            btnSeleccionar.setOnClickListener(v -> {
                EfectosManager.reproducir(getApplicationContext(), R.raw.sonido_click);
                equiparItemServidor(item.getId(), item.getNombre(), position, dialog);
            });
        }

        btnCerrar.setOnClickListener(v -> {
            EfectosManager.reproducir(getApplicationContext(), R.raw.sonido_click);
            dialog.dismiss();
        });
        dialog.show();
    }
    private void configurarNavegacionInferior() {
        LinearLayout btnNavPersonalizar = findViewById(R.id.nav_personalizar);
        if (btnNavPersonalizar != null) btnNavPersonalizar.setSelected(true);

        LinearLayout btnNavInicio = findViewById(R.id.nav_inicio);
        if (btnNavInicio != null) {
            btnNavInicio.setOnClickListener(v -> {
                EfectosManager.reproducir(getApplicationContext(), R.raw.sonido_click);
                Intent intent = new Intent(PersonalizacionActivity.this, HomeActivity.class);
                intent.putExtra("GOOGLE_ID_ESTABLE", idGoogleEstable);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
        LinearLayout btnNavTienda = findViewById(R.id.nav_tienda);
        if (btnNavTienda != null) {
            btnNavTienda.setOnClickListener(v -> {
                EfectosManager.reproducir(getApplicationContext(), R.raw.sonido_click);
                Intent intent = new Intent(PersonalizacionActivity.this, TiendaActivity.class);
                intent.putExtra("GOOGLE_ID_ESTABLE", idGoogleEstable);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
    }
    private void cargarInventarioServidor(String categoria) {
        runOnUiThread(() -> layoutTematicaSeleccionada.setVisibility(View.VISIBLE));

        List<ItemPersonalizacion> comprados = new ArrayList<>();
        List<Integer> idsComprados = new ArrayList<>();
        String[] itemEquipadoNombre = {"Negro"};
        int[] posEquipada = {-1};

        OkHttpClient client = new OkHttpClient();
        TokenManager tokenManager = new TokenManager(this);
        String jwt = tokenManager.getToken();
        if (jwt == null || jwt.isEmpty()) return;

        String urlInventario = NetworkConfig.BASE_URL + "/jugadores/personalizaciones";
        Request requestInv = new Request.Builder()
                .url(urlInventario).get().addHeader("Authorization", "Bearer " + jwt).build();

        client.newCall(requestInv).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() -> Toast.makeText(PersonalizacionActivity.this, "Error de red", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        org.json.JSONArray invArray = new org.json.JSONArray(response.body().string());
                        for (int i = 0; i < invArray.length(); i++) {
                            org.json.JSONObject temaJson = invArray.getJSONObject(i);

                            boolean equipado = temaJson.optBoolean("equipado", false);
                            if (!equipado) equipado = temaJson.optInt("equipado", 0) == 1;

                            String tipo = temaJson.optString("tipo", "baraja");

                            if(tipo.equals(categoria)){
                                int idTema = temaJson.optInt("id_personalizacion", -1);
                                String nombre = temaJson.optString("nombre", "Tema Desconocido");
                                int posicionGuion = nombre.indexOf("_");
                                if (posicionGuion == -1) posicionGuion = nombre.length();
                                String nombrePersonalizacion = nombre.substring(0, posicionGuion);
                                String valor = temaJson.optString("valor_visual", "0");

                                ItemPersonalizacion item = new ItemPersonalizacion(nombrePersonalizacion, false, tipo, 0, 0, valor);
                                item.setId(idTema);

                                comprados.add(item);
                                idsComprados.add(idTema);

                                if (equipado) {
                                    itemEquipadoNombre[0] = nombrePersonalizacion;
                                    posEquipada[0] = comprados.size() - 1;
                                }
                            }
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                }

                cargarRestoPersonalizacionesTienda(client, jwt, categoria, comprados, idsComprados, itemEquipadoNombre, posEquipada);
            }
        });
    }

    private void cargarRestoPersonalizacionesTienda(OkHttpClient client, String jwt, String categoria, List<ItemPersonalizacion> comprados, List<Integer> idsComprados, String[] itemEquipadoNombre, int[] posEquipada) {
        String urlTienda = NetworkConfig.BASE_URL + "/personalizaciones/activas";
        Request requestTienda = new Request.Builder()
                .url(urlTienda).get().addHeader("Authorization", "Bearer " + jwt).build();

        client.newCall(requestTienda).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {}

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        org.json.JSONArray temasArray = new org.json.JSONArray(response.body().string());
                        List<ItemPersonalizacion> bloqueadosLocal = new ArrayList<>();

                        for (int i = 0; i < temasArray.length(); i++) {
                            org.json.JSONObject temaJson = temasArray.getJSONObject(i);
                            int idTema = temaJson.optInt("id_personalizacion", -1);

                            // Solo lo añadimos si NO lo teníamos ya en el inventario privado
                            if (!idsComprados.contains(idTema)) {
                                String tipo = temaJson.optString("tipo", "baraja");
                                if(tipo.equals(categoria)){
                                    String nombre = temaJson.optString("nombre", "Tema Desconocido");
                                    int posicionGuion = nombre.indexOf("_");
                                    if (posicionGuion == -1) posicionGuion = nombre.length();
                                    String nombrePersonalizacion = nombre.substring(0, posicionGuion);
                                    String valor = temaJson.optString("valor_visual", "0");
                                    int precio = temaJson.optInt("precio_bala", 0);

                                    ItemPersonalizacion item = new ItemPersonalizacion(nombrePersonalizacion, true, tipo, 0, precio, valor);
                                    item.setId(idTema);
                                    bloqueadosLocal.add(item);
                                }
                            }
                        }

                        runOnUiThread(() -> {
                            posesion.clear();
                            posesion.addAll(comprados);

                            PersonalizacionActivity.this.bloqueados.clear();
                            PersonalizacionActivity.this.bloqueados.addAll(bloqueadosLocal);

                            String tipoTexto = categoria.equals("carta") ? "borde" : "fondo";
                            String tipoTextoPlural = categoria.equals("carta") ? "bordes" : "fondos";

                            if (posesion.isEmpty()) {
                                txtVacioPosesion.setVisibility(View.VISIBLE);
                                txtVacioPosesion.setText("No tienes ningún " + tipoTexto + " en posesión. ¡Ve a la tienda!");
                                recyclerPosesion.setVisibility(View.GONE);
                                txtTematicaSeleccionada.setText("Negro");
                            } else {
                                txtVacioPosesion.setVisibility(View.GONE);
                                recyclerPosesion.setVisibility(View.VISIBLE);

                                if (posEquipada[0] == -1) txtTematicaSeleccionada.setText("Negro");
                                else txtTematicaSeleccionada.setText(itemEquipadoNombre[0]);
                            }

                            if (PersonalizacionActivity.this.bloqueados.isEmpty()) {
                                if (txtVacioBloqueados != null) {
                                    txtVacioBloqueados.setVisibility(View.VISIBLE);
                                    txtVacioBloqueados.setText("¡Felicidades! Ya tienes todos los " + tipoTextoPlural + " adquiridos.");
                                }
                                recyclerBloqueados.setVisibility(View.GONE);
                            } else {
                                if (txtVacioBloqueados != null) txtVacioBloqueados.setVisibility(View.GONE);
                                recyclerBloqueados.setVisibility(View.VISIBLE);
                            }

                            adapterPosesion = new PersonalizacionAdapter(posesion, false, true, (item, pos) -> mostrarPreview(item, pos, true));
                            adapterBloqueados = new PersonalizacionAdapter(PersonalizacionActivity.this.bloqueados, true, false, (item, pos) -> mostrarPreview(item, pos, false));

                            adapterPosesion.setPosicionSeleccionada(posEquipada[0]);

                            recyclerPosesion.setAdapter(adapterPosesion);
                            recyclerBloqueados.setAdapter(adapterBloqueados);
                        });

                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
        });
    }
    private void cargarTemasServidor() {
        runOnUiThread(() -> layoutTematicaSeleccionada.setVisibility(View.GONE));

        OkHttpClient client = new OkHttpClient();
        TokenManager tokenManager = new TokenManager(this);
        String jwt = tokenManager.getToken();
        if (jwt == null || jwt.isEmpty()) return;

        posesion.clear();
        bloqueados.clear();

        String urlInventario = NetworkConfig.BASE_URL + "/jugadores/temas";
        Request requestInv = new Request.Builder()
                .url(urlInventario).get().addHeader("Authorization", "Bearer " + jwt).build();

        client.newCall(requestInv).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() -> Toast.makeText(PersonalizacionActivity.this, "Error de red", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        org.json.JSONArray invArray = new org.json.JSONArray(response.body().string());
                        for (int i = 0; i < invArray.length(); i++) {
                            org.json.JSONObject temaJson = invArray.getJSONObject(i);
                            int idTema = temaJson.optInt("id_tema", -1);
                            String nombre = temaJson.optString("nombre", "Tema Desconocido");

                            ItemPersonalizacion item = new ItemPersonalizacion(nombre, false, "baraja", 0, 0, mapearImagenTema(nombre));
                            item.setId(idTema);
                            synchronized (posesion) { posesion.add(item); }
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                }
                cargarBarajasNoAdquiridas(client, jwt);
            }
        });
    }

    private void cargarBarajasNoAdquiridas(OkHttpClient client, String jwt) {
        String urlTienda = NetworkConfig.BASE_URL + "/temas/activos";
        Request requestTienda = new Request.Builder()
                .url(urlTienda).get().addHeader("Authorization", "Bearer " + jwt).build();

        client.newCall(requestTienda).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {}

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        org.json.JSONArray temasArray = new org.json.JSONArray(response.body().string());
                        for (int i = 0; i < temasArray.length(); i++) {
                            org.json.JSONObject temaJson = temasArray.getJSONObject(i);
                            int idTema = temaJson.optInt("id_tema", -1);

                            boolean yaLaTengo = false;
                            for (ItemPersonalizacion p : posesion) {
                                if (p.getId() == idTema) { yaLaTengo = true; break; }
                            }

                            if (!yaLaTengo) {
                                String nombre = temaJson.optString("nombre", "Tema Desconocido");
                                int precio = temaJson.optInt("precio_balas", 0);
                                ItemPersonalizacion item = new ItemPersonalizacion(nombre, true, "baraja", 0, precio, mapearImagenTema(nombre));
                                item.setId(idTema);
                                synchronized (bloqueados) { bloqueados.add(item); }
                            }
                        }

                        runOnUiThread(() -> {
                            if (posesion.isEmpty()) {
                                txtVacioPosesion.setVisibility(View.VISIBLE);
                                txtVacioPosesion.setText("No tienes ninguna baraja adquirida. ¡Ve a la tienda!");
                                recyclerPosesion.setVisibility(View.GONE);
                            } else {
                                txtVacioPosesion.setVisibility(View.GONE);
                                recyclerPosesion.setVisibility(View.VISIBLE);
                            }

                            if (bloqueados.isEmpty()) {
                                if (txtVacioBloqueados != null) {
                                    txtVacioBloqueados.setVisibility(View.VISIBLE);
                                    txtVacioBloqueados.setText("¡Genial! Ya tienes todas las barajas de la tienda.");
                                }
                                recyclerBloqueados.setVisibility(View.GONE);
                            } else {
                                if (txtVacioBloqueados != null) txtVacioBloqueados.setVisibility(View.GONE);
                                recyclerBloqueados.setVisibility(View.VISIBLE);
                            }

                            adapterPosesion = new PersonalizacionAdapter(posesion, false, false, (item, pos) -> mostrarPreview(item, pos, false));
                            adapterBloqueados = new PersonalizacionAdapter(bloqueados, true, false, (item, pos) -> mostrarPreview(item, pos, false));

                            recyclerPosesion.setAdapter(adapterPosesion);
                            recyclerBloqueados.setAdapter(adapterBloqueados);
                        });
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
        });
    }

    private String mapearImagenTema(String nombreTema) {
        String nombre = nombreTema.toLowerCase();
        if (nombre.contains("magia")) return "panda_mago";
        if (nombre.contains("histórico") || nombre.contains("historico")) return "panda_explorador";
        if (nombre.contains("submarina") || nombre.contains("profundo")) return "panda_buceador";
        if (nombre.contains("cyber") || nombre.contains("futuro")) return "panda_futurista";
        if (nombre.contains("naturaleza") || nombre.contains("bambu")) return "panda_bambu";
        return "0";
    }
    private void equiparItemServidor(int idPersonalizacion, String nombreItem, int position, android.app.AlertDialog dialog) {
        TokenManager tokenManager = new TokenManager(this);
        String jwt = tokenManager.getToken();
        if (jwt == null) return;

        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        String url = NetworkConfig.BASE_URL + "/jugadores/equipar";
        org.json.JSONObject jsonBody = new org.json.JSONObject();
        try {
            jsonBody.put("id_personalizacion", idPersonalizacion);
            jsonBody.put("equipado", true);
        } catch (Exception e) { e.printStackTrace(); }

        okhttp3.RequestBody body = okhttp3.RequestBody.create(
                jsonBody.toString(), okhttp3.MediaType.parse("application/json; charset=utf-8")
        );

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .put(body)
                .addHeader("Authorization", "Bearer " + jwt)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() -> android.widget.Toast.makeText(PersonalizacionActivity.this, "Error de red al equipar", android.widget.Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        if(adapterPosesion != null) {
                            adapterPosesion.setPosicionSeleccionada(position);
                            adapterPosesion.notifyDataSetChanged();
                        }
                        txtTematicaSeleccionada.setText(nombreItem);
                        android.widget.Toast.makeText(PersonalizacionActivity.this, "¡Equipado!", android.widget.Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        android.widget.Toast.makeText(PersonalizacionActivity.this, "Error al equipar: " + response.code(), android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private String emojiPaquete(String nombre) {
        if (nombre == null) return "🎴";
        String n = nombre.toLowerCase();
        if (n.contains("básico") || n.contains("basico")) return "🃏";
        if (n.contains("magia")) return "🪄";
        if (n.contains("histórico") || n.contains("historico")) return "📜";
        if (n.contains("submarina") || n.contains("profundo")) return "🐙";
        if (n.contains("cyber") || n.contains("futuro") || n.contains("punk")) return "🌆";
        if (n.contains("naturaleza") || n.contains("bambu")) return "🌿";
        return "🎴";
    }
}