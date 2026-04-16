<<<<<<< HEAD
package com.example.secretpanda.ui.customization;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secretpanda.R;
import com.example.secretpanda.data.TokenManager;
import com.example.secretpanda.ui.home.HomeActivity;
import com.example.secretpanda.data.model.ItemPersonalizacion;
import com.example.secretpanda.data.model.InventarioGlobal;
import com.example.secretpanda.ui.shop.TiendaActivity;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_personalizacion);

        configurarNavegacionInferior();

        tabBarajas = findViewById(R.id.tab_barajas);
        tabBordes = findViewById(R.id.tab_bordes);
        tabFondos = findViewById(R.id.tab_fondos);
        txtTabBarajas = findViewById(R.id.txt_tab_barajas);
        txtTabBordes = findViewById(R.id.txt_tab_bordes);
        txtTabFondos = findViewById(R.id.txt_tab_fondos);

        txtSeccionActual = findViewById(R.id.txt_seccion_actual);
        txtTematicaSeleccionada = findViewById(R.id.txt_tematica_seleccionada);
        txtVacioPosesion = findViewById(R.id.txt_vacio_posesion);
        layoutTematicaSeleccionada = findViewById(R.id.layout_tematica_seleccionada);

        recyclerPosesion = findViewById(R.id.recycler_posesion);
        recyclerBloqueados = findViewById(R.id.recycler_bloqueados);

        recyclerPosesion.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerBloqueados.setLayoutManager(new GridLayoutManager(this, 3));

        tabBarajas.setOnClickListener(v -> seleccionarPestana(
                tabBarajas, txtTabBarajas, tabBordes, txtTabBordes, tabFondos, txtTabFondos, "Temática barajas"));

        tabBordes.setOnClickListener(v -> seleccionarPestana(
                tabBordes, txtTabBordes, tabBarajas, txtTabBarajas, tabFondos, txtTabFondos, "Temática borde"));

        tabFondos.setOnClickListener(v -> seleccionarPestana(
                tabFondos, txtTabFondos, tabBarajas, txtTabBarajas, tabBordes, txtTabBordes, "Temática fondo"));

        // EMPEZAMOS EN BARAJAS (¡Ahora en singular!)
        //cargarDatos("baraja");
        cargarInventarioServidor("baraja");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (txtSeccionActual != null) {
            String tituloActual = txtSeccionActual.getText().toString().toLowerCase();

            if (tituloActual.contains("barajas")) cargarInventarioServidor("baraja");
            else if (tituloActual.contains("borde")) cargarInventarioServidor("borde");
            else cargarInventarioServidor("fondo");
        }
    }

    private void seleccionarPestana(LinearLayout activa, TextView txtActivo,
                                    LinearLayout inactiva1, TextView txtInactivo1,
                                    LinearLayout inactiva2, TextView txtInactivo2,
                                    String titulo) {

        txtSeccionActual.setText(titulo);
        txtActivo.setVisibility(View.VISIBLE);
        txtInactivo1.setVisibility(View.GONE);
        txtInactivo2.setVisibility(View.GONE);

        activa.setBackgroundResource(R.drawable.fondo_tab_activo);
        inactiva1.setBackgroundResource(R.drawable.fondo_tab_inactivo);
        inactiva2.setBackgroundResource(R.drawable.fondo_tab_inactivo);

        animarAltura(activa, 80);
        animarAltura(inactiva1, 50);
        animarAltura(inactiva2, 50);

        String tituloMin = titulo.toLowerCase();

        if (tituloMin.contains("barajas")) cargarInventarioServidor("baraja");
        else if (tituloMin.contains("borde")) cargarInventarioServidor("borde");
        else cargarInventarioServidor("fondo");
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
        ImageView imgPreview = dialogView.findViewById(R.id.img_preview_item);
        android.widget.Button btnSeleccionar = dialogView.findViewById(R.id.btn_seleccionar_preview);

        txtTitulo.setText(item.getNombre());
        if (item.getIconoResId() != 0) imgPreview.setImageResource(item.getIconoResId());
        else imgPreview.setImageResource(R.drawable.fondo_carta_gruesa);

        if (!permiteSeleccion || item.getTipo().equals("baraja")) {
            btnSeleccionar.setVisibility(View.GONE);
        } else {
            btnSeleccionar.setVisibility(View.VISIBLE);
            btnSeleccionar.setOnClickListener(v -> {

                equiparItemServidor(item.getId(), item.getNombre(), position, dialog);
            });
        }

        btnCerrar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void configurarNavegacionInferior() {
        LinearLayout btnNavInicio = findViewById(R.id.nav_inicio);
        if (btnNavInicio != null) {
            btnNavInicio.setOnClickListener(v -> {
                Intent intent = new Intent(PersonalizacionActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
        LinearLayout btnNavTienda = findViewById(R.id.nav_tienda);
        if (btnNavTienda != null) {
            btnNavTienda.setOnClickListener(v -> {
                Intent intent = new Intent(PersonalizacionActivity.this, TiendaActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
    }

    private void cargarInventarioServidor(String categoria) {
        TokenManager tokenManager = new TokenManager(this);
        String jwt = tokenManager.getToken();
        if (jwt == null || jwt.isEmpty()) return;

        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();

        //  LEER EL INVENTARIO
        String urlInventario = "http://10.0.2.2:8080/api/jugadores/personalizaciones";
        okhttp3.Request requestInv = new okhttp3.Request.Builder()
                .url(urlInventario).get().addHeader("Authorization", "Bearer " + jwt).build();

        client.newCall(requestInv).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {}

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response responseInv) throws java.io.IOException {
                if (!responseInv.isSuccessful()) return;

                List<ItemPersonalizacion> comprados = new ArrayList<>();
                List<Integer> idsComprados = new ArrayList<>();
                String[] itemEquipadoNombre = {"Ninguno"};
                int[] posEquipada = {-1};

                try {
                    org.json.JSONArray arrInv = new org.json.JSONArray(responseInv.body().string());
                    for (int i = 0; i < arrInv.length(); i++) {
                        org.json.JSONObject obj = arrInv.getJSONObject(i);
                        String tipo = obj.optString("tipo", "baraja");

                        if (tipo.equalsIgnoreCase(categoria)) {
                            int id = obj.optInt("id_tema", -1); // Según API es id_tema
                            String nombre = obj.optString("nombre", "Desconocido");
                            boolean equipado = obj.optBoolean("equipado", false);

                            ItemPersonalizacion item = new ItemPersonalizacion(nombre, false, tipo, 0, 0);
                            item.setId(id);
                            comprados.add(item);
                            idsComprados.add(id);

                            if (equipado) {
                                itemEquipadoNombre[0] = nombre;
                                posEquipada[0] = comprados.size() - 1;
                            }
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); }

                //  LEER LA TIENDA (Para sacar lo bloqueado)
                String urlTienda = "http://10.0.2.2:8080/api/temas/activos";
                okhttp3.Request requestTienda = new okhttp3.Request.Builder()
                        .url(urlTienda).get().addHeader("Authorization", "Bearer " + jwt).build();

                client.newCall(requestTienda).enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(okhttp3.Call call, java.io.IOException e) {}

                    @Override
                    public void onResponse(okhttp3.Call call, okhttp3.Response responseTienda) throws java.io.IOException {
                        List<ItemPersonalizacion> bloqueadosLocal = new ArrayList<>();

                        if (responseTienda.isSuccessful()) {
                            try {
                                org.json.JSONArray arrTienda = new org.json.JSONArray(responseTienda.body().string());
                                for (int i = 0; i < arrTienda.length(); i++) {
                                    org.json.JSONObject obj = arrTienda.getJSONObject(i);
                                    int id = obj.optInt("id_tema", -1);
                                    String nombre = obj.optString("nombre", "Desconocido");
                                    String tipo = obj.optString("tipo", "baraja");

                                    // Si es de esta pestaña y no lo tenemos comprado
                                    if (tipo.equalsIgnoreCase(categoria) && !idsComprados.contains(id)) {
                                        ItemPersonalizacion item = new ItemPersonalizacion(nombre, true, tipo, 0, 0);
                                        item.setId(id);
                                        bloqueadosLocal.add(item);
                                    }
                                }
                            } catch (Exception e) { e.printStackTrace(); }
                        }

                        //  DIBUJAR TODO EN PANTALLA
                        runOnUiThread(() -> {
                            posesion.clear(); posesion.addAll(comprados);
                            PersonalizacionActivity.this.bloqueados.clear();
                            PersonalizacionActivity.this.bloqueados.addAll(bloqueadosLocal);

                            txtTematicaSeleccionada.setText(itemEquipadoNombre[0]);
                            if (posesion.isEmpty()) {
                                txtVacioPosesion.setVisibility(View.VISIBLE);
                                recyclerPosesion.setVisibility(View.GONE);
                            } else {
                                txtVacioPosesion.setVisibility(View.GONE);
                                recyclerPosesion.setVisibility(View.VISIBLE);
                                if (posEquipada[0] == -1) txtTematicaSeleccionada.setText(posesion.get(0).getNombre());
                            }

                            boolean permiteSeleccion = !categoria.equals("baraja");
                            adapterPosesion = new PersonalizacionAdapter(posesion, false, permiteSeleccion, (item, pos) -> mostrarPreview(item, pos, permiteSeleccion));
                            adapterBloqueados = new PersonalizacionAdapter(PersonalizacionActivity.this.bloqueados, true, false, (item, pos) -> mostrarPreview(item, pos, false));

                            if (posEquipada[0] != -1) adapterPosesion.setPosicionSeleccionada(posEquipada[0]);

                            recyclerPosesion.setAdapter(adapterPosesion);
                            recyclerBloqueados.setAdapter(adapterBloqueados);
                        });
                    }
                });
            }
        });
    }
    private void equiparItemServidor(int idPersonalizacion, String nombreItem, int position, android.app.AlertDialog dialog) {
        TokenManager tokenManager = new TokenManager(this);
        String jwt = tokenManager.getToken();
        if (jwt == null) return;

        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        String url = "http://10.0.2.2:8080/api/personalizaciones/equipar";

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
                        if(adapterPosesion != null) adapterPosesion.setPosicionSeleccionada(position);
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
=======
package com.example.secretpanda.ui.customization;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secretpanda.R;
import com.example.secretpanda.data.TokenManager;
import com.example.secretpanda.ui.home.HomeActivity;
import com.example.secretpanda.data.model.ItemPersonalizacion;
import com.example.secretpanda.data.model.InventarioGlobal;
import com.example.secretpanda.ui.shop.TiendaActivity;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_personalizacion);

        configurarNavegacionInferior();

        tabBarajas = findViewById(R.id.tab_barajas);
        tabBordes = findViewById(R.id.tab_bordes);
        tabFondos = findViewById(R.id.tab_fondos);
        txtTabBarajas = findViewById(R.id.txt_tab_barajas);
        txtTabBordes = findViewById(R.id.txt_tab_bordes);
        txtTabFondos = findViewById(R.id.txt_tab_fondos);

        txtSeccionActual = findViewById(R.id.txt_seccion_actual);
        txtTematicaSeleccionada = findViewById(R.id.txt_tematica_seleccionada);
        txtVacioPosesion = findViewById(R.id.txt_vacio_posesion);
        layoutTematicaSeleccionada = findViewById(R.id.layout_tematica_seleccionada);

        recyclerPosesion = findViewById(R.id.recycler_posesion);
        recyclerBloqueados = findViewById(R.id.recycler_bloqueados);

        recyclerPosesion.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerBloqueados.setLayoutManager(new GridLayoutManager(this, 3));

        tabBarajas.setOnClickListener(v -> seleccionarPestana(
                tabBarajas, txtTabBarajas, tabBordes, txtTabBordes, tabFondos, txtTabFondos, "Temática barajas"));

        tabBordes.setOnClickListener(v -> seleccionarPestana(
                tabBordes, txtTabBordes, tabBarajas, txtTabBarajas, tabFondos, txtTabFondos, "Temática borde"));

        tabFondos.setOnClickListener(v -> seleccionarPestana(
                tabFondos, txtTabFondos, tabBarajas, txtTabBarajas, tabBordes, txtTabBordes, "Temática fondo"));

        // EMPEZAMOS EN BARAJAS (¡Ahora en singular!)
        //cargarDatos("baraja");
        cargarInventarioServidor("baraja");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (txtSeccionActual != null) {
            String tituloActual = txtSeccionActual.getText().toString().toLowerCase();

            if (tituloActual.contains("barajas")) cargarInventarioServidor("baraja");
            else if (tituloActual.contains("borde")) cargarInventarioServidor("borde");
            else cargarInventarioServidor("fondo");
        }
    }

    private void seleccionarPestana(LinearLayout activa, TextView txtActivo,
                                    LinearLayout inactiva1, TextView txtInactivo1,
                                    LinearLayout inactiva2, TextView txtInactivo2,
                                    String titulo) {

        txtSeccionActual.setText(titulo);
        txtActivo.setVisibility(View.VISIBLE);
        txtInactivo1.setVisibility(View.GONE);
        txtInactivo2.setVisibility(View.GONE);

        activa.setBackgroundResource(R.drawable.fondo_tab_activo);
        inactiva1.setBackgroundResource(R.drawable.fondo_tab_inactivo);
        inactiva2.setBackgroundResource(R.drawable.fondo_tab_inactivo);

        animarAltura(activa, 80);
        animarAltura(inactiva1, 50);
        animarAltura(inactiva2, 50);

        String tituloMin = titulo.toLowerCase();

        if (tituloMin.contains("barajas")) cargarInventarioServidor("baraja");
        else if (tituloMin.contains("borde")) cargarInventarioServidor("borde");
        else cargarInventarioServidor("fondo");
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
        ImageView imgPreview = dialogView.findViewById(R.id.img_preview_item);
        android.widget.Button btnSeleccionar = dialogView.findViewById(R.id.btn_seleccionar_preview);

        txtTitulo.setText(item.getNombre());
        if (item.getIconoResId() != 0) imgPreview.setImageResource(item.getIconoResId());
        else imgPreview.setImageResource(R.drawable.fondo_carta_gruesa);

        if (!permiteSeleccion || item.getTipo().equals("baraja")) {
            btnSeleccionar.setVisibility(View.GONE);
        } else {
            btnSeleccionar.setVisibility(View.VISIBLE);
            btnSeleccionar.setOnClickListener(v -> {

                equiparItemServidor(item.getId(), item.getNombre(), position, dialog);
            });
        }

        btnCerrar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void configurarNavegacionInferior() {
        LinearLayout btnNavInicio = findViewById(R.id.nav_inicio);
        if (btnNavInicio != null) {
            btnNavInicio.setOnClickListener(v -> {
                Intent intent = new Intent(PersonalizacionActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
        LinearLayout btnNavTienda = findViewById(R.id.nav_tienda);
        if (btnNavTienda != null) {
            btnNavTienda.setOnClickListener(v -> {
                Intent intent = new Intent(PersonalizacionActivity.this, TiendaActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
    }

    private void cargarInventarioServidor(String categoria) {
        TokenManager tokenManager = new TokenManager(this);
        String jwt = tokenManager.getToken();
        if (jwt == null || jwt.isEmpty()) return;

        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();

        //  LEER EL INVENTARIO
        String urlInventario = "http://10.0.2.2:8080/api/jugadores/personalizaciones";
        okhttp3.Request requestInv = new okhttp3.Request.Builder()
                .url(urlInventario).get().addHeader("Authorization", "Bearer " + jwt).build();

        client.newCall(requestInv).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {}

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response responseInv) throws java.io.IOException {
                if (!responseInv.isSuccessful()) return;

                List<ItemPersonalizacion> comprados = new ArrayList<>();
                List<Integer> idsComprados = new ArrayList<>();
                String[] itemEquipadoNombre = {"Ninguno"};
                int[] posEquipada = {-1};

                try {
                    org.json.JSONArray arrInv = new org.json.JSONArray(responseInv.body().string());
                    for (int i = 0; i < arrInv.length(); i++) {
                        org.json.JSONObject obj = arrInv.getJSONObject(i);
                        String tipo = obj.optString("tipo", "baraja");

                        if (tipo.equalsIgnoreCase(categoria)) {
                            int id = obj.optInt("id_tema", -1); // Según API es id_tema
                            String nombre = obj.optString("nombre", "Desconocido");
                            boolean equipado = obj.optBoolean("equipado", false);

                            ItemPersonalizacion item = new ItemPersonalizacion(nombre, false, tipo, 0, 0);
                            item.setId(id);
                            comprados.add(item);
                            idsComprados.add(id);

                            if (equipado) {
                                itemEquipadoNombre[0] = nombre;
                                posEquipada[0] = comprados.size() - 1;
                            }
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); }

                //  LEER LA TIENDA (Para sacar lo bloqueado)
                String urlTienda = "http://10.0.2.2:8080/api/temas/activos";
                okhttp3.Request requestTienda = new okhttp3.Request.Builder()
                        .url(urlTienda).get().addHeader("Authorization", "Bearer " + jwt).build();

                client.newCall(requestTienda).enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(okhttp3.Call call, java.io.IOException e) {}

                    @Override
                    public void onResponse(okhttp3.Call call, okhttp3.Response responseTienda) throws java.io.IOException {
                        List<ItemPersonalizacion> bloqueadosLocal = new ArrayList<>();

                        if (responseTienda.isSuccessful()) {
                            try {
                                org.json.JSONArray arrTienda = new org.json.JSONArray(responseTienda.body().string());
                                for (int i = 0; i < arrTienda.length(); i++) {
                                    org.json.JSONObject obj = arrTienda.getJSONObject(i);
                                    int id = obj.optInt("id_tema", -1);
                                    String nombre = obj.optString("nombre", "Desconocido");
                                    String tipo = obj.optString("tipo", "baraja");


                                    // Si el nombre es "Básico", no lo añadimos a bloqueados aunque no esté en idsComprados
                                    if (tipo.equalsIgnoreCase(categoria) && !idsComprados.contains(id) && !nombre.equalsIgnoreCase("Basico")) {
                                        ItemPersonalizacion item = new ItemPersonalizacion(nombre, true, tipo, 0, 0);
                                        item.setId(id);
                                        bloqueadosLocal.add(item);
                                    }
                                }
                            } catch (Exception e) { e.printStackTrace(); }
                        }

                        //  DIBUJAR TODO EN PANTALLA
                        runOnUiThread(() -> {
                            posesion.clear(); posesion.addAll(comprados);
                            PersonalizacionActivity.this.bloqueados.clear();
                            PersonalizacionActivity.this.bloqueados.addAll(bloqueadosLocal);

                            txtTematicaSeleccionada.setText(itemEquipadoNombre[0]);
                            if (posesion.isEmpty()) {
                                txtVacioPosesion.setVisibility(View.VISIBLE);
                                recyclerPosesion.setVisibility(View.GONE);
                            } else {
                                txtVacioPosesion.setVisibility(View.GONE);
                                recyclerPosesion.setVisibility(View.VISIBLE);
                                if (posEquipada[0] == -1) txtTematicaSeleccionada.setText(posesion.get(0).getNombre());
                            }

                            boolean permiteSeleccion = !categoria.equals("baraja");
                            adapterPosesion = new PersonalizacionAdapter(posesion, false, permiteSeleccion, (item, pos) -> mostrarPreview(item, pos, permiteSeleccion));
                            adapterBloqueados = new PersonalizacionAdapter(PersonalizacionActivity.this.bloqueados, true, false, (item, pos) -> mostrarPreview(item, pos, false));

                            if (posEquipada[0] != -1) adapterPosesion.setPosicionSeleccionada(posEquipada[0]);

                            recyclerPosesion.setAdapter(adapterPosesion);
                            recyclerBloqueados.setAdapter(adapterBloqueados);
                        });
                    }
                });
            }
        });
    }
    private void equiparItemServidor(int idPersonalizacion, String nombreItem, int position, android.app.AlertDialog dialog) {
        TokenManager tokenManager = new TokenManager(this);
        String jwt = tokenManager.getToken();
        if (jwt == null) return;

        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        String url = "http://10.0.2.2:8080/api/personalizaciones/equipar";

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
                        if(adapterPosesion != null) adapterPosesion.setPosicionSeleccionada(position);
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
>>>>>>> eee3289f2a3cab5b6c7d883011302786ea012647
}