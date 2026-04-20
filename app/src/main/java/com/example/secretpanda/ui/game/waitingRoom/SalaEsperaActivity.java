package com.example.secretpanda.ui.game.waitingRoom;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secretpanda.R;
import com.example.secretpanda.data.TokenManager;
import com.example.secretpanda.data.model.Jugador;
import com.example.secretpanda.ui.EfectosManager;
import com.example.secretpanda.ui.game.match.PartidaActivity;

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
    private TextView btnUnirseAzul;
    private TextView btnUnirseRojo;
    private Jugador jugadorLocal;

    private int idPartida = -1;
    private String miPropioIdGoogle = "";

    private TextView tvContadorAzul, tvContadorRojo, tvContadorTotal, tvTiempoSala;

    private int maxJugadores = 8;
    private boolean esLider = false;
    private boolean esPrivada = false;
    private int jugadoresAzul = 0;
    private int jugadoresRojo = 0;
    private Dialog dialogoCarga;

    private StompClient stompClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sala_espera);

        esLider = getIntent().getBooleanExtra("ES_LIDER", false);

        idPartida = getIntent().getIntExtra("ID_PARTIDA", -1);
        miPropioIdGoogle = getIntent().getStringExtra("MI_NOMBRE_USUARIO");

        TextView btnAbandonar = findViewById(R.id.btn_abandonar);
        btnAbandonar.setOnClickListener(v -> mostrarDialogoAbandonar());

        tvContadorAzul = findViewById(R.id.tv_contador_azul);
        tvContadorRojo = findViewById(R.id.tv_contador_rojo);
        tvContadorTotal = findViewById(R.id.tv_jugadores_sala);
        btnUnirseAzul = findViewById(R.id.btn_unirse_azul);
        btnUnirseRojo = findViewById(R.id.btn_unirse_rojo);
        tvTiempoSala = findViewById(R.id.tv_tiempo_sala);

        cargarLobbyInicial();

        btnUnirseAzul.setOnClickListener(v -> {
            if (!estoyEnEquipoAzul) {
                estoyEnEquipoAzul = true;
                actualizarBotonesEquipo();
                cambiarEquipoEnBackend("azul");
            }
        });

        btnUnirseRojo.setOnClickListener(v -> {
            if (estoyEnEquipoAzul) {
                estoyEnEquipoAzul = false;
                actualizarBotonesEquipo();
                cambiarEquipoEnBackend("rojo");
            }
        });

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
            Log.d("WS_LOBBY", "Payload: " + payload);
            if ("FINALIZADA".equalsIgnoreCase(payload)) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "El líder ha abandonado. Partida cancelada.", Toast.LENGTH_LONG).show();
                    finish();
                });
                return;
            }
            try {
                JSONObject json = new JSONObject(payload);
                String estado = json.optString("estado", "");

                if ("en_curso".equalsIgnoreCase(estado)) {
                    runOnUiThread(() -> {
                        Intent intent = new Intent(SalaEsperaActivity.this, PartidaActivity.class);
                        intent.putExtra("ID_PARTIDA", idPartida);
                        intent.putExtra("MI_NOMBRE_USUARIO", miPropioIdGoogle);
                        intent.putExtra("MI_EQUIPO", estoyEnEquipoAzul ? "azul" : "rojo");
                        startActivity(intent);
                        finish();
                    });
                    return;
                }
                
                if ("finalizada".equalsIgnoreCase(estado)) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "La sala ha sido cerrada por el líder.", Toast.LENGTH_LONG).show();
                        finish();
                    });
                    return;
                }

                runOnUiThread(() -> {
                    try {
                        int nuevoTiempo = json.optInt("tiempo_espera", -1);
                        if (nuevoTiempo != -1 && tvTiempoSala != null) tvTiempoSala.setText(nuevoTiempo + "s");

                        if (json.has("max_jugadores")) {
                            maxJugadores = json.getInt("max_jugadores");
                        }

                        if (json.has("jugadores")) {
                            org.json.JSONArray jugadoresArray = json.getJSONArray("jugadores");
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
                                    actualizarBotonesEquipo();
                                }
                            }
                            adapter.notifyDataSetChanged();
                            actualizarContadores(listaJugadores);
                        }
                    } catch (Exception e) { Log.e("WS_LOBBY", "Error UI", e); }
                });
            } catch (Exception e) { Log.e("WS_LOBBY", "Error JSON", e); }
        }, throwable -> Log.e("WS_LOBBY", "Error sub", throwable));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (stompClient != null && stompClient.isConnected()) stompClient.disconnect();
        if (dialogoCarga != null && dialogoCarga.isShowing()) dialogoCarga.dismiss();
    }

    private void cambiarEquipoEnBackend(String nuevoEquipo) {
        if (stompClient != null && stompClient.isConnected()) {
            try {
                JSONObject payload = new JSONObject();
                payload.put("equipo", nuevoEquipo);
                stompClient.send("/app/partida/" + idPartida + "/participantes/equipo", payload.toString()).subscribe();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void conectarWebSocketLobby() {
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://10.0.2.2:8080/ws/websocket");
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
        for (Jugador j : lista) { if (j.isEsEquipoAzul()) azul++; else rojo++; }
        jugadoresAzul = azul; jugadoresRojo = rojo;
        if (tvContadorAzul != null) tvContadorAzul.setText("Azul: " + azul + "/" + (maxJugadores/2));
        if (tvContadorRojo != null) tvContadorRojo.setText("Rojo: " + rojo + "/" + (maxJugadores/2));
        if (tvContadorTotal != null) tvContadorTotal.setText(lista.size() + "/" + maxJugadores);
    }

    private void actualizarBotonesEquipo() {
        if (estoyEnEquipoAzul) {
            setModoDentro(btnUnirseAzul, "#0000FF");
            setModoUnirse(btnUnirseRojo, "#FF0000");
        } else {
            setModoDentro(btnUnirseRojo, "#FF0000");
            setModoUnirse(btnUnirseAzul, "#0000FF");
        }
    }

    private void setModoDentro(TextView btn, String colorHex) {
        btn.setText("Dentro");
        btn.setBackgroundResource(R.drawable.fondo_boton_dentro);
        GradientDrawable f = (GradientDrawable) btn.getBackground().mutate();
        f.setStroke(5, Color.parseColor(colorHex));
        btn.setTextColor(Color.parseColor(colorHex));
    }

    private void setModoUnirse(TextView btn, String colorHex) {
        btn.setText("Unirse");
        btn.setBackgroundResource(R.drawable.fondo_boton_unirse);
        GradientDrawable f = (GradientDrawable) btn.getBackground().mutate();
        f.setColor(Color.parseColor(colorHex));
        btn.setTextColor(Color.WHITE);
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

    private void cargarLobbyInicial() {
        OkHttpClient client = new OkHttpClient();
        String jwt = new TokenManager(this).getToken();
        Request request = new Request.Builder().url("http://10.0.2.2:8080/api/partidas/" + idPartida + "/lobby")
                .get().addHeader("Authorization", "Bearer " + jwt).build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                Log.e("WS_LOBBY", "Error HTTP", e);
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    Log.d("WS_LOBBY", "Response: " + responseData);
                    try {
                        Log.d("WS_LOBBY", "Prueba");

                        JSONObject json = new JSONObject(responseData);
                        TextView tvCodigoPartida = findViewById(R.id.tv_codigo_partida);
                        View layoutCodigoEntero = (View) tvCodigoPartida.getParent();

                        Log.d("WS_LOBBY", "Es privada: " + json.getBoolean("es_publica"));

                        if (json.getBoolean("es_publica")) {
                            layoutCodigoEntero.setVisibility(View.GONE);
                        } else {
                            layoutCodigoEntero.setVisibility(View.VISIBLE);
                            tvCodigoPartida.setText(json.optString("codigo_partida", ""));
                        }
                        int nuevoTiempo = json.optInt("tiempo_espera", -1);
                        if (nuevoTiempo != -1 && tvTiempoSala != null) tvTiempoSala.setText(nuevoTiempo + "s");
                        if (json.has("max_jugadores")) {
                            maxJugadores = json.getInt("max_jugadores");
                        }
                        org.json.JSONArray array = json.optJSONArray("jugadores");
                        if (array != null) {
                            runOnUiThread(() -> {
                                listaJugadores.clear();
                                for (int i = 0; i < array.length(); i++) {
                                    try {
                                        JSONObject jp = array.getJSONObject(i);
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
                                        
                                        Jugador j = new Jugador(tag);
                                        j.setFotoPerfil(foto);
                                        String equipo = jp.optString("equipo", "ROJO");
                                        j.setEsEquipoAzul(equipo.equalsIgnoreCase("AZUL"));
                                        listaJugadores.add(j);
                                        
                                        if (tag.equals(miPropioIdGoogle)) {
                                            estoyEnEquipoAzul = equipo.equalsIgnoreCase("AZUL");
                                            actualizarBotonesEquipo();
                                        }
                                    } catch (Exception e) {
                                        Log.e("WS_LOBBY", "Error JSON", e);
                                    }
                                }
                                adapter.notifyDataSetChanged();
                                actualizarContadores(listaJugadores);
                            });
                        }
                    } catch (Exception e) {
                        Log.e("WS_LOBBY", "Error JSON", e);
                    }
                }
            }
        });
    }

    private void iniciarPartidaHTTP() {
        OkHttpClient client = new OkHttpClient();
        String jwt = new TokenManager(this).getToken();
        Request request = new Request.Builder().url("http://10.0.2.2:8080/api/partida/" + idPartida + "/iniciar")
                .put(RequestBody.create(new byte[0])).addHeader("Authorization", "Bearer " + jwt).build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { }
            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Intent intent = new Intent(SalaEsperaActivity.this, PartidaActivity.class);
                        intent.putExtra("ID_PARTIDA", idPartida);
                        intent.putExtra("MI_NOMBRE_USUARIO", miPropioIdGoogle);
                        intent.putExtra("MI_EQUIPO", estoyEnEquipoAzul ? "AZUL" : "ROJO");
                        intent.putExtra("ES_LIDER", true);
                        startActivity(intent);
                        finish();
                    });
                }
            }
        });
    }
}
