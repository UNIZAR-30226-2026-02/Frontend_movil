package com.example.secretpanda.ui.game.waitingRoom;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secretpanda.R;
import com.example.secretpanda.data.NetworkConfig;
import com.example.secretpanda.data.TokenManager;
import com.example.secretpanda.data.model.Jugador;
import com.example.secretpanda.ui.audio.EfectosManager;
import com.example.secretpanda.ui.game.match.PartidaActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;

public class SalaEsperaActivity extends AppCompatActivity {

    private RecyclerView rvJugadores;
    private JugadorSalaAdapter adapter;
    private List<Jugador> listaJugadores;

    private boolean estoyEnEquipoAzul;
    private TextView btnGestionarEquipo, btnConfig;
    private ImageView btnPersonalizarSala;
    private Jugador jugadorLocal;

    private int idPartida = -1;
    private String miPropioIdGoogle = "";

    private TextView tvContadorAzul, tvContadorRojo, tvContadorTotal, tvTiempoSala;

    private int maxJugadores = 8;
    private boolean esLider = false;
    private Dialog dialogoCarga;

    private int jugadoresAzul = 0;
    private int jugadoresRojo = 0;

    private StompClient stompClient;

    // Variables para la personalización de la sala
    private List<JSONObject> misBordes = new ArrayList<>();
    private List<JSONObject> misFondos = new ArrayList<>();
    private PersonalizacionSalaAdapter personalizacionAdapter;
    private boolean mostrandoBordes = true; // Recuerda en qué pestaña estamos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_sala_espera);

        esLider = getIntent().getBooleanExtra("ES_LIDER", false);
        idPartida = getIntent().getIntExtra("ID_PARTIDA", -1);
        miPropioIdGoogle = getIntent().getStringExtra("MI_NOMBRE_USUARIO");

        TextView btnAbandonar = findViewById(R.id.btn_abandonar);
        btnAbandonar.setOnClickListener(v -> mostrarDialogoAbandonar());

        tvContadorAzul = findViewById(R.id.tv_contador_azul);
        tvContadorRojo = findViewById(R.id.tv_contador_rojo);
        tvContadorTotal = findViewById(R.id.tv_jugadores_sala);
        tvTiempoSala = findViewById(R.id.tv_tiempo_sala);

        btnGestionarEquipo = findViewById(R.id.btn_gestionar_equipo);
        btnGestionarEquipo.setOnClickListener(v -> {
            EfectosManager.reproducir(getApplicationContext(), R.raw.sonido_click);
            mostrarDialogoCambiarEquipo();
        });

        btnConfig = findViewById(R.id.btn_config_sala);
        btnConfig.setOnClickListener(v -> {
            // Lógica de configuración...
        });

        // 🔥 BOTÓN DE PERSONALIZACIÓN 🔥
        btnPersonalizarSala = findViewById(R.id.btn_personalizar_sala);
        btnPersonalizarSala.setOnClickListener(v -> {
            EfectosManager.reproducir(getApplicationContext(), R.raw.sonido_click);
            mostrarDialogoPersonalizacion();
        });

        cargarLobbyInicial();

        rvJugadores = findViewById(R.id.rv_jugadores);
        rvJugadores.setLayoutManager(new LinearLayoutManager(this));
        listaJugadores = new ArrayList<>();

        TextView btnIniciarPartida = findViewById(R.id.btn_iniciar_partida_principal);

        if (!esLider) {
            if (btnIniciarPartida != null) {
                btnIniciarPartida.setText("Esperando\nal líder...");
                btnIniciarPartida.setAlpha(0.5f);
                btnIniciarPartida.setEnabled(false);
            }
        } else {
            if (btnIniciarPartida != null) {
                btnIniciarPartida.setText("Iniciar\npartida");
                btnIniciarPartida.setAlpha(1.0f);
                btnIniciarPartida.setEnabled(true);
                btnIniciarPartida.setOnClickListener(v -> validarAntesDeIniciar());
            }
        }

        adapter = new JugadorSalaAdapter(listaJugadores, miPropioIdGoogle, esLider, nuevaLista -> {
            actualizarContadores(nuevaLista);
        });
        rvJugadores.setAdapter(adapter);
        conectarWebSocketLobby();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                mostrarDialogoAbandonar();
            }
        });
    }

    private void suscribirseAlCanalDelLobby() {
        String destinoTopic = "/topic/partidas/" + idPartida + "/lobby";
        stompClient.topic(destinoTopic).subscribe(stompMessage -> {
            String payload = stompMessage.getPayload();
            try {
                JSONObject json = new JSONObject(payload);
                String estado = json.optString("estado", "");

                if ("finalizada".equalsIgnoreCase(estado)) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "El líder ha abandonado. Partida cancelada.", Toast.LENGTH_LONG).show();
                        finish();
                    });
                    return;
                }

                if ("en_curso".equalsIgnoreCase(estado)) {
                    runOnUiThread(() -> {
                        Intent intent = new Intent(SalaEsperaActivity.this, PartidaActivity.class);
                        intent.putExtra("ID_PARTIDA", idPartida);
                        intent.putExtra("MI_NOMBRE_USUARIO", jugadorLocal != null ? jugadorLocal.getTag() : miPropioIdGoogle);
                        intent.putExtra("MI_EQUIPO", estoyEnEquipoAzul ? "azul" : "rojo");
                        startActivity(intent);
                        finish();
                    });
                    return;
                }

                runOnUiThread(() -> {
                    try {
                        if (json.has("es_publica")) {
                            boolean esPub = json.optBoolean("es_publica", true);
                            TextView tvCodigo = findViewById(R.id.tv_codigo_partida);
                            if (tvCodigo != null && tvCodigo.getParent() instanceof View) {
                                ((View) tvCodigo.getParent()).setVisibility(esPub ? View.GONE : View.VISIBLE);
                                if (!esPub) tvCodigo.setText(json.optString("codigo_partida", ""));
                            }
                        } else {
                            btnConfig.setVisibility(View.VISIBLE);
                        }

                        int nuevoTiempo = json.optInt("tiempo_espera", -1);
                        if (nuevoTiempo != -1 && tvTiempoSala != null) tvTiempoSala.setText(nuevoTiempo + "s");

                        if (json.has("max_jugadores")) {
                            maxJugadores = json.getInt("max_jugadores");
                        }

                        if (json.has("jugadores")) {
                            procesarListaJugadores(json.getJSONArray("jugadores"));
                            adapter.notifyDataSetChanged();
                            actualizarContadores(listaJugadores);
                        }
                    } catch (Exception e) { Log.e("WS_LOBBY", "Error UI", e); }
                });
            } catch (Exception e) { Log.e("WS_LOBBY", "Error JSON", e); }
        }, throwable -> Log.e("WS_LOBBY", "Error sub", throwable));
    }

    private void procesarListaJugadores(JSONArray jugadoresArray) {
        try {
            listaJugadores.clear();
            for (int i = 0; i < jugadoresArray.length(); i++) {
                JSONObject jp = jugadoresArray.getJSONObject(i);
                JSONObject jInfo = jp.optJSONObject("jugador");

                String tag = "Anónimo";
                String foto = "1";

                if (jInfo != null) {
                    tag = jInfo.optString("tag", "Anónimo");
                    foto = jInfo.optString("foto_perfil", "1");
                } else {
                    tag = jp.optString("tag", "Anónimo");
                    foto = jp.optString("foto_perfil", "1");
                }

                String equipo = jp.optString("equipo", "ROJO");
                Jugador j = new Jugador(tag);
                j.setFotoPerfil(foto);
                j.setEsEquipoAzul("AZUL".equalsIgnoreCase(equipo));
                listaJugadores.add(j);

                if (tag.equals(miPropioIdGoogle)) {
                    estoyEnEquipoAzul = "azul".equalsIgnoreCase(equipo);
                    jugadorLocal = j;
                }
            }
        } catch (Exception e) {
            Log.e("LOBBY", "Error procesando lista", e);
        }
    }

    private void cargarLobbyInicial() {
        OkHttpClient client = new OkHttpClient();
        String jwt = new TokenManager(this).getToken();
        Request request = new Request.Builder()
                .url(NetworkConfig.BASE_URL + "/partidas/" + idPartida + "/lobby")
                .get()
                .addHeader("Authorization", "Bearer " + jwt)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {}
            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseData);
                        boolean esPublica = json.optBoolean("es_publica", true);
                        String codigo = json.optString("codigo_partida", "");
                        int tiempo = json.optInt("tiempo_espera", -1);
                        int maxJug = json.optInt("max_jugadores", 8);
                        JSONArray jugadoresArr = json.optJSONArray("jugadores");

                        runOnUiThread(() -> {
                            TextView tvCodigoPartida = findViewById(R.id.tv_codigo_partida);
                            if (tvCodigoPartida != null && tvCodigoPartida.getParent() instanceof View) {
                                View layoutCodigoEntero = (View) tvCodigoPartida.getParent();
                                if (esPublica) {
                                    layoutCodigoEntero.setVisibility(View.GONE);
                                } else {
                                    layoutCodigoEntero.setVisibility(View.VISIBLE);
                                    tvCodigoPartida.setText(codigo);
                                }
                            }

                            if (tiempo != -1 && tvTiempoSala != null) tvTiempoSala.setText(tiempo + "s");
                            maxJugadores = maxJug;

                            if (jugadoresArr != null) {
                                procesarListaJugadores(jugadoresArr);
                                adapter.notifyDataSetChanged();
                                actualizarContadores(listaJugadores);
                            }
                        });
                    } catch (Exception e) {}
                }
            }
        });
    }

    private void cambiarEquipoEnBackend(String nuevoEquipo) {
        if (stompClient != null && stompClient.isConnected()) {
            try {
                JSONObject payload = new JSONObject();
                payload.put("equipo", nuevoEquipo);
                stompClient.send("/app/partida/" + idPartida + "/participantes/equipo", payload.toString()).subscribe();
            } catch (Exception e) {}
        }
    }

    private void conectarWebSocketLobby() {
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, NetworkConfig.WS_URL);
        String jwt = new TokenManager(this).getToken();
        List<StompHeader> headers = new ArrayList<>();
        if (jwt != null) headers.add(new StompHeader("Authorization", "Bearer " + jwt));

        stompClient.lifecycle().subscribe(ev -> {
            if (ev.getType() == ua.naiksoftware.stomp.dto.LifecycleEvent.Type.OPENED) suscribirseAlCanalDelLobby();
        });
        stompClient.connect(headers);
    }

    private void validarAntesDeIniciar() {
        int azul = 0, rojo = 0;
        for (Jugador j : listaJugadores) { if (j.isEsEquipoAzul()) azul++; else rojo++; }
        if (listaJugadores.size() < 4 || azul < 2 || rojo < 2) {
            mostrarDialogoErrorJugadores();
        } else if (listaJugadores.size() < maxJugadores) {
            mostrarDialogoConfirmacion();
        } else {
            iniciarPartidaHTTP();
        }
    }

    private void actualizarContadores(List<Jugador> lista) {
        int azul = 0, rojo = 0;
        for (Jugador j : lista) {
            if (j.isEsEquipoAzul()) azul++;
            else rojo++;
        }

        jugadoresAzul = azul;
        jugadoresRojo = rojo;
        maxJugadores = lista.size();

        int maxPorEquipo = maxJugadores - 2;
        if (maxPorEquipo < 2) maxPorEquipo = 2; // Seguro de fallos

        if (tvContadorAzul != null) {
            tvContadorAzul.setText("AZUL: " + azul + "/" + maxPorEquipo);
            if (azul < 2 || azul > maxPorEquipo) {
                tvContadorAzul.setTextColor(Color.parseColor("#FF5252"));
            } else {
                tvContadorAzul.setTextColor(Color.parseColor("#3366CC"));
            }
        }

        if (tvContadorRojo != null) {
            tvContadorRojo.setText("ROJO: " + rojo + "/" + maxPorEquipo);
            if (rojo < 2 || rojo > maxPorEquipo) {
                tvContadorRojo.setTextColor(Color.parseColor("#FF5252"));
            } else {
                tvContadorRojo.setTextColor(Color.parseColor("#8B2020"));
            }
        }

        if (tvContadorTotal != null) {
            String texto = lista.size() + "/" + maxJugadores;
            tvContadorTotal.setText(texto);
            if (lista.size() < 4) {
                tvContadorTotal.setTextColor(Color.parseColor("#FF5252"));
            } else {
                tvContadorTotal.setTextColor(Color.WHITE);
            }
        }
    }

    private void mostrarDialogoErrorJugadores() {
        Dialog d = new Dialog(this);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.dialog_error_jugadores);
        if (d.getWindow() != null) d.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        d.findViewById(R.id.btn_cerrar_error).setOnClickListener(v -> {
            EfectosManager.reproducir(getApplicationContext(), R.raw.sonido_click);
            d.dismiss();
        });
        d.show();
    }

    private void mostrarDialogoConfirmacion() {
        dialogoCarga = new Dialog(this);
        dialogoCarga.setContentView(R.layout.dialog_confirmar_inicio);
        if (dialogoCarga.getWindow() != null) dialogoCarga.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialogoCarga.findViewById(R.id.btn_cerrar_dialogo).setOnClickListener(v -> {
            EfectosManager.reproducir(getApplicationContext(), R.raw.sonido_click);
            dialogoCarga.dismiss();
        });
        Button btn = dialogoCarga.findViewById(R.id.btn_iniciar_confirmado);
        btn.setOnClickListener(v -> {
            EfectosManager.reproducir(getApplicationContext(), R.raw.sonido_aceptar);
            btn.setEnabled(false);
            iniciarPartidaHTTP(); });
        dialogoCarga.show();
    }

    private void mostrarDialogoAbandonar() {
        Dialog d = new Dialog(this);
        d.setContentView(R.layout.dialog_abandonar);
        if (d.getWindow() != null) d.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        d.findViewById(R.id.btn_cerrar_abandonar).setOnClickListener(v -> {
            EfectosManager.reproducir(getApplicationContext(), R.raw.sonido_click);
            d.dismiss();
        });
        d.findViewById(R.id.btn_confirmar_abandonar).setOnClickListener(v -> {
            EfectosManager.reproducir(getApplicationContext(), R.raw.sonido_cancelar);
            d.dismiss();
            salirDeLaSala(); });
        d.show();
    }

    private void salirDeLaSala() {
        if (stompClient != null && stompClient.isConnected()) {
            stompClient.send("/app/partida/" + idPartida + "/abandonarLobby", "").subscribe();
            stompClient.disconnect();
        }
        finish();
    }

    private void iniciarPartidaHTTP() {
        OkHttpClient client = new OkHttpClient();
        String jwt = new TokenManager(this).getToken();
        Request request = new Request.Builder()
                .url(NetworkConfig.BASE_URL + "/partida/" + idPartida + "/iniciar")
                .put(RequestBody.create(new byte[0]))
                .addHeader("Authorization", "Bearer " + jwt)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { }
            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Intent intent = new Intent(SalaEsperaActivity.this, PartidaActivity.class);
                        intent.putExtra("ID_PARTIDA", idPartida);
                        intent.putExtra("MI_NOMBRE_USUARIO", jugadorLocal != null ? jugadorLocal.getTag() : miPropioIdGoogle);
                        intent.putExtra("MI_EQUIPO", estoyEnEquipoAzul ? "AZUL" : "ROJO");
                        intent.putExtra("JUGADORES_AZUL", jugadoresAzul);
                        intent.putExtra("JUGADORES_ROJO", jugadoresRojo);
                        intent.putExtra("JUGADORES_TOTAL", maxJugadores);
                        intent.putExtra("ES_LIDER", true);
                        startActivity(intent);
                        finish();
                    });
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (stompClient != null && stompClient.isConnected()) stompClient.disconnect();
        if (dialogoCarga != null && dialogoCarga.isShowing()) dialogoCarga.dismiss();
    }

    private void mostrarDialogoCambiarEquipo() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_cambiar_equipo);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0));
        }

        TextView btnAzul = dialog.findViewById(R.id.btn_unirse_azul_popup);
        TextView btnRojo = dialog.findViewById(R.id.btn_unirse_rojo_popup);
        TextView btnCerrar = dialog.findViewById(R.id.btn_cerrar_cambio);

        if (estoyEnEquipoAzul) {
            btnAzul.setAlpha(0.5f);
            btnAzul.setText("YA ESTÁS EN EL\nEQUIPO AZUL");
        } else {
            btnRojo.setAlpha(0.5f);
            btnRojo.setText("YA ESTÁS EN EL\nEQUIPO ROJO");
        }

        btnAzul.setOnClickListener(v -> {
            if (!estoyEnEquipoAzul) {
                estoyEnEquipoAzul = true;
                cambiarEquipoEnBackend("azul");
                dialog.dismiss();
            }
        });

        btnRojo.setOnClickListener(v -> {
            if (estoyEnEquipoAzul) {
                estoyEnEquipoAzul = false;
                cambiarEquipoEnBackend("rojo");
                dialog.dismiss();
            }
        });

        btnCerrar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // =========================================================================
    // 🔥 LÓGICA DE PERSONALIZACIÓN VISUAL 🔥
    // =========================================================================

    private void mostrarDialogoPersonalizacion() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_tematicas_sala);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        View tabBordes = dialog.findViewById(R.id.tab_bordes);
        View tabFondos = dialog.findViewById(R.id.tab_fondos);
        TextView txtBordes = dialog.findViewById(R.id.txt_tab_bordes);
        TextView txtFondos = dialog.findViewById(R.id.txt_tab_fondos);
        RecyclerView recycler = dialog.findViewById(R.id.recycler_tematicas_dialog);

        recycler.setLayoutManager(new GridLayoutManager(this, 3));

        personalizacionAdapter = new PersonalizacionSalaAdapter(new ArrayList<>(), item -> equiparItemHTTP(item));
        recycler.setAdapter(personalizacionAdapter);

        mostrandoBordes = true; // Valor por defecto

        tabBordes.setOnClickListener(v -> {
            EfectosManager.reproducir(this, R.raw.sonido_click);
            tabBordes.setBackgroundResource(R.drawable.fondo_tab_activo);
            tabFondos.setBackgroundResource(R.drawable.fondo_tab_inactivo);
            txtBordes.setVisibility(View.VISIBLE);
            txtFondos.setVisibility(View.GONE);
            mostrandoBordes = true;
            personalizacionAdapter.setItems(misBordes);
        });

        tabFondos.setOnClickListener(v -> {
            EfectosManager.reproducir(this, R.raw.sonido_click);
            tabFondos.setBackgroundResource(R.drawable.fondo_tab_activo);
            tabBordes.setBackgroundResource(R.drawable.fondo_tab_inactivo);
            txtFondos.setVisibility(View.VISIBLE);
            txtBordes.setVisibility(View.GONE);
            mostrandoBordes = false;
            personalizacionAdapter.setItems(misFondos);
        });

        dialog.findViewById(R.id.btn_cerrar_personalizacion).setOnClickListener(v -> {
            EfectosManager.reproducir(this, R.raw.sonido_click);
            dialog.dismiss();
        });

        // Llamamos a la API para traer tu inventario
        cargarPersonalizacionesDesdeAPI();

        dialog.show();
    }

    private void cargarPersonalizacionesDesdeAPI() {
        OkHttpClient client = new OkHttpClient();
        String token = new TokenManager(this).getToken();
        if (token == null) return;

        // 🔥 OJO: Endpoint corregido al INVENTARIO del jugador 🔥
        Request request = new Request.Builder()
                .url(NetworkConfig.BASE_URL + "/jugadores/personalizaciones")
                .get()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(SalaEsperaActivity.this, "Error de red al cargar opciones", Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONArray array = new JSONArray(response.body().string());
                        misBordes.clear();
                        misFondos.clear();

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            String tipo = obj.optString("tipo", "");

                            // Ya no pedimos "comprado" porque todo lo que llega aquí ya es tuyo
                            if ("carta".equalsIgnoreCase(tipo)) {
                                misBordes.add(obj);
                            } else if ("tablero".equalsIgnoreCase(tipo)) {
                                misFondos.add(obj);
                            }
                        }

                        // Refrescamos la pestaña actual
                        runOnUiThread(() -> {
                            if (mostrandoBordes) {
                                personalizacionAdapter.setItems(misBordes);
                            } else {
                                personalizacionAdapter.setItems(misFondos);
                            }
                        });

                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
        });
    }

    private void equiparItemHTTP(JSONObject item) {
        OkHttpClient client = new OkHttpClient();
        String token = new TokenManager(this).getToken();
        if (token == null) return;

        try {
            int id = item.getInt("id_personalizacion");
            boolean equipado = item.optBoolean("equipado", false);
            boolean nuevoEstado = !equipado; // Hacemos toggle (si está equipado lo quita, si no, lo pone)

            JSONObject payload = new JSONObject();
            payload.put("id_personalizacion", id);
            payload.put("equipado", nuevoEstado);

            Request request = new Request.Builder()
                    .url(NetworkConfig.BASE_URL + "/jugadores/equipar")
                    .put(RequestBody.create(payload.toString(), MediaType.parse("application/json")))
                    .addHeader("Authorization", "Bearer " + token)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {}
                @Override public void onResponse(Call call, Response response) {
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> {
                            EfectosManager.reproducir(SalaEsperaActivity.this, R.raw.sonido_aceptar);
                            // Recargamos el inventario para actualizar qué está equipado
                            cargarPersonalizacionesDesdeAPI();
                        });
                    }
                }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    // =========================================================================
    // 🔥 INTERFAZ Y ADAPTADOR AISLADOS 🔥
    // =========================================================================

    public interface OnItemClickListener {
        void onItemClick(JSONObject item);
    }

    private class PersonalizacionSalaAdapter extends RecyclerView.Adapter<PersonalizacionSalaAdapter.ViewHolder> {
        private List<JSONObject> items;
        private OnItemClickListener listener;

        public PersonalizacionSalaAdapter(List<JSONObject> items, OnItemClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        public void setItems(List<JSONObject> nuevosItems) {
            this.items = nuevosItems;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_personalizacion, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            JSONObject item = items.get(position);

            holder.txtNombre.setText(item.optString("nombre", "Tema").toUpperCase());
            String colorHex = item.optString("valor_visual", "#888888");
            String tipo = item.optString("tipo", "");

            int parsedColor = Color.GRAY;
            try {
                if (!colorHex.startsWith("#")) colorHex = "#" + colorHex;
                parsedColor = Color.parseColor(colorHex);
            } catch (Exception e) {}

            // LÓGICA VISUAL: Mostrar cartas para "Borde" o cuadrado liso para "Fondo"
            if ("carta".equalsIgnoreCase(tipo)) {
                holder.vistaCartas.setVisibility(View.VISIBLE);
                holder.vistaImagen.setVisibility(View.GONE);
                holder.imgCartaFrontal.setColorFilter(parsedColor, android.graphics.PorterDuff.Mode.SRC_IN);
            } else {
                holder.vistaCartas.setVisibility(View.GONE);
                holder.vistaImagen.setVisibility(View.VISIBLE);

                // Le damos forma redondeada a la imagen para simular el tablero
                GradientDrawable gd = new GradientDrawable();
                gd.setShape(GradientDrawable.RECTANGLE);
                gd.setCornerRadius(16f); // Esquinas suaves
                gd.setColor(parsedColor);
                holder.vistaImagen.setImageDrawable(gd);
            }

            // Ocultamos el candado
            holder.iconoCandado.setVisibility(View.GONE);

            // Indicador visual de "EQUIPADO"
            if (item.optBoolean("equipado", false)) {
                holder.txtNombre.setTextColor(Color.parseColor("#d4b878")); // Dorado
                holder.fondoItem.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#3a3228")));
            } else {
                holder.txtNombre.setTextColor(Color.WHITE);
                holder.fondoItem.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#2a2218")));
            }

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtNombre;
            ImageView imgCartaFrontal;
            ImageView iconoCandado;
            View fondoItem;
            View vistaCartas;
            ImageView vistaImagen;

            public ViewHolder(View itemView) {
                super(itemView);
                txtNombre = itemView.findViewById(R.id.txt_nombre_item);
                imgCartaFrontal = itemView.findViewById(R.id.img_carta_frontal);
                iconoCandado = itemView.findViewById(R.id.icono_candado);
                fondoItem = itemView.findViewById(R.id.fondo_item_personalizacion);
                vistaCartas = itemView.findViewById(R.id.vista_cartas);
                vistaImagen = itemView.findViewById(R.id.vista_imagen);
            }
        }
    }
}