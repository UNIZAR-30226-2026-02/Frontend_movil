package com.example.secretpanda.ui.game.waitingRoom;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.secretpanda.R;
import com.example.secretpanda.data.TokenManager;
import com.example.secretpanda.data.model.InventarioGlobal;
import com.example.secretpanda.data.model.ItemPersonalizacion;
import com.example.secretpanda.data.model.Jugador;
import com.example.secretpanda.ui.customization.PersonalizacionAdapter;
import com.example.secretpanda.ui.game.match.PartidaActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SalaEsperaActivity extends AppCompatActivity {

    private RecyclerView rvJugadores;
    private JugadorSalaAdapter adapter;
    private List<Jugador> listaJugadores;

    private boolean estoyEnEquipoAzul;
<<<<<<< HEAD
    private TextView btnUnirseAzul, btnAbandonar, btnEmpezarPartida;
=======
    private TextView btnUnirseAzul;
>>>>>>> b1648d95a10bb97a88257c4d33d8b0e75c37d2e0
    private TextView btnUnirseRojo;
    private PersonalizacionAdapter adapterPersonalizacionDialogo;
    private Jugador jugadorLocal;

    // ID real de la partida y Token del usuario
    private long idPartida = -1;
    private String miPropioIdGoogle = "";

    private TextView tvContadorAzul, tvContadorRojo, tvContadorTotal, tvTiempoSala;

    // Variables de configuración de la sala
    private int maxJugadores = 8;
    private boolean esLider = false;
    private boolean esPrivada = false;
<<<<<<< HEAD

    private ua.naiksoftware.stomp.StompClient stompClient;
    private boolean soyElCreador = false;
    private String miTagPropio = ""; // Guárdalo desde el Token o SharedPreferences para identificarte
=======
>>>>>>> b1648d95a10bb97a88257c4d33d8b0e75c37d2e0

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sala_espera);

        // ==========================================
        // RECUPERAR DATOS DEL INTENT
        // ==========================================
        esLider = getIntent().getBooleanExtra("ES_LIDER", false);
        esPrivada = getIntent().getBooleanExtra("ES_PRIVADA", false);
        maxJugadores = getIntent().getIntExtra("MAX_JUGADORES", 8);
        int tiempoTurno = getIntent().getIntExtra("TIEMPO_TURNO", 60);

        // Recuperamos el ID real de la partida que nos ha dado el Backend
        idPartida = getIntent().getLongExtra("ID_PARTIDA", -1);
<<<<<<< HEAD
=======

        // Leemos quién somos nosotros (para luego saber nuestro rol)
        miPropioIdGoogle = getIntent().getStringExtra("MI_NOMBRE_USUARIO");
        if(miPropioIdGoogle == null) miPropioIdGoogle = "TuNombreDeUsuario";
>>>>>>> b1648d95a10bb97a88257c4d33d8b0e75c37d2e0

        // Leemos quién somos nosotros (para luego saber nuestro rol)
        miPropioIdGoogle = getIntent().getStringExtra("MI_NOMBRE_USUARIO");
        if(miPropioIdGoogle == null) miPropioIdGoogle = "TuNombreDeUsuario";

        btnAbandonar = findViewById(R.id.btn_abandonar);
        btnAbandonar.setOnClickListener(v -> mostrarDialogoAbandonar());

        tvContadorAzul = findViewById(R.id.tv_contador_azul);
        tvContadorRojo = findViewById(R.id.tv_contador_rojo);
        tvContadorTotal = findViewById(R.id.tv_jugadores_sala);
        btnUnirseAzul = findViewById(R.id.btn_unirse_azul);
        btnUnirseRojo = findViewById(R.id.btn_unirse_rojo);
        tvTiempoSala = findViewById(R.id.tv_tiempo_sala);

        if (tvTiempoSala != null) tvTiempoSala.setText(tiempoTurno + "s");

        // ==========================================
        // LÓGICA DEL CÓDIGO DE PARTIDA
        // ==========================================
        TextView tvCodigoPartida = findViewById(R.id.tv_codigo_partida);
        View layoutCodigoEntero = (View) tvCodigoPartida.getParent();

        if (!esPrivada) {
            layoutCodigoEntero.setVisibility(View.GONE);
        } else {
            layoutCodigoEntero.setVisibility(View.VISIBLE);
            String codigoRecibido = getIntent().getStringExtra("CODIGO_PARTIDA");
            tvCodigoPartida.setText(codigoRecibido != null ? codigoRecibido : generarCodigoAleatorio());
        }


        // BOTONES DE CAMBIO DE EQUIPO (RF-21)


<<<<<<< HEAD
        btnUnirseAzul.setOnClickListener(v -> solicitarCambioEquipo("azul"));
        btnUnirseRojo.setOnClickListener(v -> solicitarCambioEquipo("rojo"));
=======
        btnUnirseAzul.setOnClickListener(v -> cambiarEquipoEnBackend("AZUL"));
        btnUnirseRojo.setOnClickListener(v -> cambiarEquipoEnBackend("ROJO"));
>>>>>>> b1648d95a10bb97a88257c4d33d8b0e75c37d2e0

        // CONFIGURACIÓN DE LA LISTA DE JUGADORES
        rvJugadores = findViewById(R.id.rv_jugadores);
        rvJugadores.setLayoutManager(new LinearLayoutManager(this));
        listaJugadores = new ArrayList<>();



        // BOTÓN INICIAR PARTIDA (RF-13)
<<<<<<< HEAD
        btnEmpezarPartida = findViewById(R.id.btn_iniciar_partida_principal);
=======
        TextView btnIniciarPartida = findViewById(R.id.btn_iniciar_partida_principal);
>>>>>>> b1648d95a10bb97a88257c4d33d8b0e75c37d2e0
        View btnConfiguracion = findViewById(R.id.btn_configuracion);

        if (!soyElCreador) {
            if (btnConfiguracion != null) btnConfiguracion.setVisibility(View.GONE);
            if (btnEmpezarPartida != null) {
                btnEmpezarPartida.setText("Esperando\nal líder...");
                btnEmpezarPartida.setAlpha(0.5f);
                btnEmpezarPartida.setEnabled(false);
            }
        } else {
            if (btnConfiguracion != null) {
                btnConfiguracion.setVisibility(View.VISIBLE);
                btnConfiguracion.setOnClickListener(v -> mostrarDialogoAjustes());
            }
<<<<<<< HEAD
            if (btnEmpezarPartida != null) {
                btnEmpezarPartida.setText("Iniciar\npartida");
                btnEmpezarPartida.setAlpha(1.0f);
                btnEmpezarPartida.setEnabled(true);
                btnEmpezarPartida.setOnClickListener(v -> iniciarPartida());
=======
            if (btnIniciarPartida != null) {
                btnIniciarPartida.setText("Iniciar\npartida");
                btnIniciarPartida.setAlpha(1.0f);
                btnIniciarPartida.setEnabled(true);
                btnIniciarPartida.setOnClickListener(v -> validarAntesDeIniciar());
>>>>>>> b1648d95a10bb97a88257c4d33d8b0e75c37d2e0
            }
        }

        adapter = new JugadorSalaAdapter(listaJugadores, miPropioIdGoogle, esLider, nuevaLista -> {
            actualizarContadores(nuevaLista);
        });
        rvJugadores.setAdapter(adapter);
<<<<<<< HEAD
        conectarWebSocketLobby();
=======
        cargarLobbyInicial();
>>>>>>> b1648d95a10bb97a88257c4d33d8b0e75c37d2e0

        findViewById(R.id.btn_tematicas).setOnClickListener(v -> mostrarDialogoEstrella());
    }


    // API: PUT CAMBIAR EQUIPO (RF-21)
    private void cambiarEquipoEnBackend(String equipoElegido) {
        if ((equipoElegido.equals("AZUL") && estoyEnEquipoAzul) ||
                (equipoElegido.equals("ROJO") && !estoyEnEquipoAzul)) {
            return; // Ya estoy en ese equipo
        }

        // Reflejo visual instantáneo para el usuario
        estoyEnEquipoAzul = equipoElegido.equals("AZUL");
        jugadorLocal.setEsEquipoAzul(estoyEnEquipoAzul);
        actualizarBotonesEquipo();
        adapter.notifyDataSetChanged();
        actualizarContadores(listaJugadores);

        btnUnirseRojo.setEnabled(false);
        btnUnirseAzul.setEnabled(false);

        OkHttpClient client = new OkHttpClient();
        String url = "http://10.0.2.2:8080/api/partida/" + idPartida + "/equipo";

        TokenManager tokenManager = new TokenManager(this);
        String jwt = tokenManager.getToken();

        RequestBody body = RequestBody.create(equipoElegido, MediaType.parse("text/plain; charset=utf-8"));

        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .addHeader("Authorization", "Bearer " + jwt)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(SalaEsperaActivity.this, "Error de red al cambiar equipo", Toast.LENGTH_SHORT).show();
                    btnUnirseRojo.setEnabled(true);
                    btnUnirseAzul.setEnabled(true);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Leemos el mensaje real de error que manda el servidor
                String cuerpoError = response.body() != null ? response.body().string() : "Vacío";
                int codigoHTTP = response.code();

                runOnUiThread(() -> {
                    btnUnirseRojo.setEnabled(true);
                    btnUnirseAzul.setEnabled(true);

                    if (!response.isSuccessful()) {
                        // Imprimimos el error exacto en el Logcat (en rojo)
                        android.util.Log.e("API_EQUIPO", " URL LLAMADA: " + request.url());
                        android.util.Log.e("API_EQUIPO", " CÓDIGO HTTP: " + codigoHTTP);
                        android.util.Log.e("API_EQUIPO", " MOTIVO DEL BACKEND: " + cuerpoError);

                        Toast.makeText(SalaEsperaActivity.this, "Error " + codigoHTTP + ". Mira el Logcat", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(SalaEsperaActivity.this, "¡Cambiado con éxito!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // API: POST INICIAR PARTIDA (RF-13)
    private void validarAntesDeIniciar() {
        int contadorAzul = 0;
        int contadorRojo = 0;
        for (Jugador jugador : listaJugadores) {
            if (jugador.isEsEquipoAzul()) contadorAzul++;
            else contadorRojo++;
        }
        int totalJugadores = listaJugadores.size();

        if (totalJugadores < 4 || contadorAzul < 2 || contadorRojo < 2) {
            mostrarDialogoErrorJugadores();
        } else if (totalJugadores < maxJugadores) {
            mostrarDialogoConfirmacion();
        } else {
            mandarOrdenDeInicio();
        }
    }

<<<<<<< HEAD

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (stompClient != null && stompClient.isConnected()) {
            stompClient.disconnect();
            android.util.Log.d("WS_LOBBY", "Desconectado del lobby");
        }
    }

    private void conectarWebSocketLobby() {
        // 1. Configurar la conexión con Token
        String wsUrl = "ws://10.0.2.2:8080/ws-endpoint"; // Ajusta tu URL

        com.example.secretpanda.data.TokenManager tokenManager = new com.example.secretpanda.data.TokenManager(this);
        String token = tokenManager.getToken();

        java.util.List<ua.naiksoftware.stomp.dto.StompHeader> headers = new java.util.ArrayList<>();
        if (token != null && !token.isEmpty()) {
            headers.add(new ua.naiksoftware.stomp.dto.StompHeader("Authorization", "Bearer " + token));
        }

        stompClient = ua.naiksoftware.stomp.Stomp.over(ua.naiksoftware.stomp.Stomp.ConnectionProvider.OKHTTP, wsUrl);
        stompClient.connect(headers);

        // 2. Suscribirnos al canal de esta partida en concreto
        String topicUrl = "/topic/partidas/" + idPartida + "/lobby";

        stompClient.topic(topicUrl).subscribe(topicMessage -> {
            String payload = topicMessage.getPayload();
            android.util.Log.d("WS_LOBBY", "Actualización del lobby: " + payload);

            runOnUiThread(() -> {
                try {
                    org.json.JSONObject lobbyJson = new org.json.JSONObject(payload);
                    String estado = lobbyJson.getString("estado");

                    // ==========================================
                    // LÓGICA 1: REDIRECCIONES SEGÚN EL ESTADO
                    // ==========================================
                    if ("finalizado".equalsIgnoreCase(estado)) {
                        android.widget.Toast.makeText(SalaEsperaActivity.this, "El líder abortó la misión.", android.widget.Toast.LENGTH_LONG).show();
                        finish(); // Echamos a todos a la pantalla anterior
                        return;
                    } else if ("en_curso".equalsIgnoreCase(estado)) {
                        // El creador le dio a empezar. ¡Nos vamos a jugar!
                        android.content.Intent intent = new android.content.Intent(SalaEsperaActivity.this, PartidaActivity.class);
                        intent.putExtra("ID_PARTIDA", idPartida);
                        intent.putExtra("ID_JUGADOR", miPropioIdGoogle);
                        // Tu equipo (basado en la variable booleana que usas para saber de qué color estás)
                        intent.putExtra("MI_EQUIPO", estoyEnEquipoAzul ? "azul" : "rojo");

                        // Tu rol (lo leemos de la maleta con la que llegaste a esta pantalla)
                        String miRol = getIntent().getStringExtra("MI_ROL");
                        intent.putExtra("MI_ROL", miRol != null ? miRol : "agente");
                        startActivity(intent);
                        finish();
                        return;
                    }

                    // ==========================================
                    // LÓGICA 2: ACTUALIZACIÓN DE DATOS DE LA SALA
                    // ==========================================
                    boolean hayMinimo = lobbyJson.getBoolean("hayMinimo");
                    String tagCreador = lobbyJson.getString("tag_creador");

                    // Verificamos si nosotros somos el creador según el backend
                    soyElCreador = tagCreador.equals(miTagPropio);

                    btnEmpezarPartida.setEnabled(soyElCreador && hayMinimo);

                    // Y para que sea evidente visualmente (opcional):
                    btnEmpezarPartida.setAlpha((soyElCreador && hayMinimo) ? 1.0f : 0.5f);

                    // ==========================================
                    // LÓGICA 3: ACTUALIZAR LA LISTA DE JUGADORES
                    // ==========================================
                    org.json.JSONArray jugadoresArray = lobbyJson.getJSONArray("jugadores");

                    // Limpiamos la lista actual y la rellenamos con lo nuevo
                    listaJugadores.clear();

                    for (int i = 0; i < jugadoresArray.length(); i++) {
                        org.json.JSONObject jugJson = jugadoresArray.getJSONObject(i);

                        Jugador j = new Jugador(jugJson.getString("tag"));
                        j.setTag(jugJson.getString("tag")); // Cuidado si en tu modelo se llama de otra forma
                        j.setEsEquipoAzul(Boolean.parseBoolean(jugJson.getString("equipo")));
                        // j.setFotoPerfil(jugJson.optString("fotoPerfil", "")); // Si usas fotos de perfil

                        listaJugadores.add(j);
                    }

                    // Refrescamos el RecyclerView
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }

                } catch (Exception e) {
                    android.util.Log.e("WS_LOBBY", "Error parseando JSON del lobby", e);
                }
            });
        }, throwable -> {
            android.util.Log.e("WS_LOBBY", "Error de conexión en el lobby", throwable);
        });
    }

=======
>>>>>>> b1648d95a10bb97a88257c4d33d8b0e75c37d2e0
    private void mandarOrdenDeInicio() {
        Toast.makeText(this, "Arrancando motores...", Toast.LENGTH_SHORT).show();

        OkHttpClient client = new OkHttpClient();
        //  /api/partida (singular)  /crear
        String url = "http://10.0.2.2:8080/api/partida/" + idPartida + "/crear";

        TokenManager tokenManager = new TokenManager(this);
        String jwt = tokenManager.getToken();

        // Es un PUT, así que mandamos un cuerpo JSON vacío por si acaso el backend lo requiere
        RequestBody body = RequestBody.create("{}", MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .addHeader("Authorization", "Bearer " + jwt)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(SalaEsperaActivity.this, "Error de red al intentar iniciar", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    // Si falla, imprimimos el motivo exacto en el Logcat para poder decirselo al Backend
                    String errorBody = response.body() != null ? response.body().string() : "Vacío";
                    android.util.Log.e("API_INICIAR", " HTTP " + response.code() + " | Motivo: " + errorBody);

                    runOnUiThread(() -> Toast.makeText(SalaEsperaActivity.this, "El servidor rechazó iniciar la partida", Toast.LENGTH_SHORT).show());
                } else {
                    //  PARCHE TEMPORAL (Por no tener WebSockets)

                    runOnUiThread(() -> {
                        Intent intent = new Intent(SalaEsperaActivity.this, PartidaActivity.class);
                        intent.putExtra("ID_PARTIDA", idPartida);
                        intent.putExtra("MI_ROL", "JEFE");
                        startActivity(intent);
                        finish();
                    });
                }
            }
        });
    }

    // MÉTODOS VISUALES ORIGINALES

    private String generarCodigoAleatorio() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder codigo = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < 6; i++) {
            codigo.append(caracteres.charAt(rnd.nextInt(caracteres.length())));
        }
        return codigo.toString();
    }
    private void solicitarCambioEquipo(String nuevoEquipo) {
        // Comprobamos que el radar (WebSocket) está encendido
        if (stompClient != null && stompClient.isConnected()) {
            try {
                // 1. Preparamos el mensaje JSON: {"equipo": "rojo"}
                org.json.JSONObject payload = new org.json.JSONObject();
                payload.put("equipo", nuevoEquipo);

<<<<<<< HEAD
                // 2. La ruta de publicación que me has dado
                String destination = "/app/partida/" + idPartida + "/participantes/equipo";

                // 3. Enviamos el mensaje al servidor
                stompClient.send(destination, payload.toString()).subscribe(() -> {
                    android.util.Log.d("WS_EQUIPO", "Petición de cambio a equipo " + nuevoEquipo + " enviada.");
                }, throwable -> {
                    android.util.Log.e("WS_EQUIPO", "Error al cambiar de equipo", throwable);
                    runOnUiThread(() -> android.widget.Toast.makeText(SalaEsperaActivity.this, "Error de conexión al cambiar de equipo", android.widget.Toast.LENGTH_SHORT).show());
                });

            } catch (Exception e) {
                android.util.Log.e("WS_EQUIPO", "Error creando el JSON para el equipo", e);
            }
        } else {
            android.widget.Toast.makeText(this, "Conectando con la base... espere un momento.", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
=======
>>>>>>> b1648d95a10bb97a88257c4d33d8b0e75c37d2e0
    private void actualizarContadores(List<Jugador> lista) {
        int contadorAzul = 0;
        int contadorRojo = 0;
        for (Jugador jugador : lista) {
            if (jugador.isEsEquipoAzul()) contadorAzul++;
            else contadorRojo++;
        }

        if (tvContadorAzul != null) tvContadorAzul.setText("Azul: " + contadorAzul + "/" + (maxJugadores/2));
        if (tvContadorRojo != null) tvContadorRojo.setText("Rojo: " + contadorRojo + "/" + (maxJugadores/2));
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
        GradientDrawable fondo = (GradientDrawable) btn.getBackground().mutate();
        fondo.setStroke(5, Color.parseColor(colorHex));
        btn.setTextColor(Color.parseColor(colorHex));
        btn.setShadowLayer(8f, 0f, 0f, Color.parseColor("#FFFFFF"));
    }

    private void setModoUnirse(TextView btn, String colorHex) {
        btn.setText("Unirse");
        btn.setBackgroundResource(R.drawable.fondo_boton_unirse);
        GradientDrawable fondo = (GradientDrawable) btn.getBackground().mutate();
        fondo.setColor(Color.parseColor(colorHex));
        fondo.setStroke(0, Color.TRANSPARENT);
        btn.setTextColor(Color.parseColor("#FFFFFF"));
        btn.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT);
    }

    private void mostrarDialogoErrorJugadores() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_error_jugadores);
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button btnCerrar = dialog.findViewById(R.id.btn_cerrar_error);
        if (btnCerrar != null) btnCerrar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void mostrarDialogoConfirmacion() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirmar_inicio);
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.findViewById(R.id.btn_cerrar_dialogo).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btn_iniciar_confirmado).setOnClickListener(v -> {
            dialog.dismiss();
            mandarOrdenDeInicio();
        });
        dialog.show();
    }

<<<<<<< HEAD
    private void cambiarTiempoTurno(int nuevoTiempo) {
        // 1. Verificación de seguridad: ¿Es este usuario el líder?
        if (!soyElCreador) {
            android.widget.Toast.makeText(this, "Solo el creador puede cambiar los ajustes de la misión.", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Comprobamos que el radar sigue activo
        if (stompClient != null && stompClient.isConnected()) {
            try {
                // Preparamos el paquete JSON: {"tiempoEspera": 60}
                org.json.JSONObject payload = new org.json.JSONObject();
                payload.put("tiempoEspera", nuevoTiempo);

                // La ruta de publicación para el tiempo
                String destination = "/app/partida/" + idPartida + "/tiempoTurno";

                // Disparamos el mensaje
                stompClient.send(destination, payload.toString()).subscribe(() -> {
                    android.util.Log.d("WS_TIEMPO", "Petición de cambio de tiempo enviada: " + nuevoTiempo + "s");
                }, throwable -> {
                    android.util.Log.e("WS_TIEMPO", "Error al cambiar el tiempo", throwable);
                    runOnUiThread(() -> android.widget.Toast.makeText(SalaEsperaActivity.this, "Error de conexión al cambiar tiempo", android.widget.Toast.LENGTH_SHORT).show());
                });

            } catch (Exception e) {
                android.util.Log.e("WS_TIEMPO", "Error creando el JSON del tiempo", e);
            }
        } else {
            android.widget.Toast.makeText(this, "Sin conexión con la base.", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

=======
>>>>>>> b1648d95a10bb97a88257c4d33d8b0e75c37d2e0
    private void mostrarDialogoAjustes() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_ajustes_sala);
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.findViewById(R.id.btn_cerrar_ajustes).setOnClickListener(v -> dialog.dismiss());

        TextView[] botonesTiempo = {
                dialog.findViewById(R.id.btn_tiempo_30), dialog.findViewById(R.id.btn_tiempo_60),
                dialog.findViewById(R.id.btn_tiempo_80), dialog.findViewById(R.id.btn_tiempo_120)
        };
        String tiempoActualStr = tvTiempoSala.getText().toString();
        for (TextView btn : botonesTiempo) {
            if ((btn.getText().toString()).equals(tiempoActualStr)) seleccionarBoton(btn, botonesTiempo);
            btn.setOnClickListener(v -> {
                seleccionarBoton(btn, botonesTiempo);
<<<<<<< HEAD
                cambiarTiempoTurno(Integer.parseInt(btn.getText().toString()));
=======
>>>>>>> b1648d95a10bb97a88257c4d33d8b0e75c37d2e0
                if (tvTiempoSala != null) tvTiempoSala.setText(btn.getText().toString());
            });
        }

        TextView[] botonesJugadores = {
                dialog.findViewById(R.id.btn_jugadores_4), dialog.findViewById(R.id.btn_jugadores_6),
                dialog.findViewById(R.id.btn_jugadores_8), dialog.findViewById(R.id.btn_jugadores_10),
                dialog.findViewById(R.id.btn_jugadores_12), dialog.findViewById(R.id.btn_jugadores_14),
                dialog.findViewById(R.id.btn_jugadores_16)
        };
        TextView tvAdvertencia = dialog.findViewById(R.id.tv_advertencia_sala);
        tvAdvertencia.setVisibility(View.GONE);

        for (TextView btn : botonesJugadores) {
            if (btn.getText().toString().equals(String.valueOf(maxJugadores))) seleccionarBoton(btn, botonesJugadores);
            btn.setOnClickListener(v -> {
                int intentoMaxJugadores = Integer.parseInt(btn.getText().toString());
                if (intentoMaxJugadores < listaJugadores.size()) {
                    tvAdvertencia.setVisibility(View.VISIBLE);
                } else {
                    tvAdvertencia.setVisibility(View.GONE);
                    maxJugadores = intentoMaxJugadores;
                    seleccionarBoton(btn, botonesJugadores);
                    actualizarContadores(listaJugadores);
                }
            });
        }
        dialog.show();
    }

    private void seleccionarBoton(TextView seleccionado, TextView[] grupo) {
        for (TextView btn : grupo) {
            if (btn != null) {
                if (btn == seleccionado) btn.setBackgroundResource(R.drawable.fondo_carta_seleccionada);
                else btn.setBackgroundResource(R.drawable.fondo_boton_mision);
            }
        }
    }

    private void mostrarDialogoAbandonar() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_abandonar);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        dialog.findViewById(R.id.btn_cerrar_abandonar).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btn_confirmar_abandonar).setOnClickListener(v -> {
            dialog.dismiss();
            abandonarLobby();
            finish();
        });
        dialog.show();
    }

    private void mostrarDialogoEstrella() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_tematicas_sala);
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.findViewById(R.id.btn_cerrar_personalizacion).setOnClickListener(v -> dialog.dismiss());
        TextView txtTematica = dialog.findViewById(R.id.txt_tematica_seleccionada_dialog);
        RecyclerView recycler = dialog.findViewById(R.id.recycler_tematicas_dialog);
        recycler.setLayoutManager(new GridLayoutManager(this, 3));

        LinearLayout tabBordes = dialog.findViewById(R.id.tab_bordes);
        LinearLayout tabFondos = dialog.findViewById(R.id.tab_fondos);
        TextView txtBordes = dialog.findViewById(R.id.txt_tab_bordes);
        TextView txtFondos = dialog.findViewById(R.id.txt_tab_fondos);

        tabBordes.setOnClickListener(v -> {
            activarPestana(tabBordes, txtBordes, tabFondos, txtFondos);
            cargarDatosEnDialogo("borde", recycler, txtTematica);
        });

        tabFondos.setOnClickListener(v -> {
            activarPestana(tabFondos, txtFondos, tabBordes, txtBordes);
            cargarDatosEnDialogo("fondo", recycler, txtTematica);
        });

        tabBordes.performClick();
        dialog.show();
    }

    private void activarPestana(LinearLayout activo, TextView txtActivo, LinearLayout inactivo, TextView txtInactivo) {
        activo.setBackgroundResource(R.drawable.fondo_tab_activo);
        ViewGroup.LayoutParams paramsActivo = activo.getLayoutParams();
        paramsActivo.height = (int) (80 * getResources().getDisplayMetrics().density);
        paramsActivo.width = (int) (110 * getResources().getDisplayMetrics().density);
        activo.setLayoutParams(paramsActivo);
        txtActivo.setVisibility(View.VISIBLE);

        inactivo.setBackgroundResource(R.drawable.fondo_tab_inactivo);
        ViewGroup.LayoutParams paramsInactivo = inactivo.getLayoutParams();
        paramsInactivo.height = (int) (50 * getResources().getDisplayMetrics().density);
        paramsInactivo.width = (int) (90 * getResources().getDisplayMetrics().density);
        inactivo.setLayoutParams(paramsInactivo);
        txtInactivo.setVisibility(View.GONE);
    }

    private void cargarDatosEnDialogo(String categoria, RecyclerView recycler, TextView txtTematica) {
        List<ItemPersonalizacion> todos = InventarioGlobal.getInstance().getTodosLosItems();
        List<ItemPersonalizacion> posesion = new ArrayList<>();
        for (ItemPersonalizacion item : todos) {
            if (item.getTipo().equals(categoria) && !item.isBloqueado()) posesion.add(item);
        }
        if (!posesion.isEmpty()) txtTematica.setText(posesion.get(0).getNombre());
        else txtTematica.setText("Ninguna");

        adapterPersonalizacionDialogo = new PersonalizacionAdapter(posesion, false, true, (item, position) -> {
            txtTematica.setText(item.getNombre());
            if (adapterPersonalizacionDialogo != null) adapterPersonalizacionDialogo.setPosicionSeleccionada(position);
        });
        recycler.setAdapter(adapterPersonalizacionDialogo);
    }

    // API: GET ESTADO INICIAL DE LA SALA
    private void cargarLobbyInicial() {
        OkHttpClient client = new OkHttpClient();
        String url = "http://10.0.2.2:8080/api/partidas/" + idPartida;

        TokenManager tokenManager = new TokenManager(this);
        String jwt = tokenManager.getToken();

        Request request = new Request.Builder()
                .url(url)
                .get() // ⬅ Es un método GET
                .addHeader("Authorization", "Bearer " + jwt)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(SalaEsperaActivity.this, "Error de red al cargar el lobby", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonRespuesta = response.body().string();
                        org.json.JSONObject lobbyJson = new org.json.JSONObject(jsonRespuesta);
                        org.json.JSONArray jugadoresArray = lobbyJson.optJSONArray("jugadores");

                        if (jugadoresArray != null) {
                            runOnUiThread(() -> {
                                //  Vaciamos la lista porque vamos a rellenarla con datos reales
                                listaJugadores.clear();

                                for (int i = 0; i < jugadoresArray.length(); i++) {
                                    try {
                                        org.json.JSONObject jp = jugadoresArray.getJSONObject(i);

                                        // Dependiendo de si el Backend manda el jugador plano o anidado
                                        String tag = "Anónimo";
                                        if (jp.has("jugador")) {
                                            tag = jp.getJSONObject("jugador").optString("tag", "Anónimo");
                                        } else {
                                            tag = jp.optString("tag", "Anónimo");
                                        }

                                        String equipo = jp.optString("equipo", "ROJO");

                                        // Creamos el jugador y lo metemos en su equipo
                                        Jugador jugadorActualizado = new Jugador(tag);
                                        jugadorActualizado.setEsEquipoAzul(equipo.equalsIgnoreCase("AZUL"));
                                        listaJugadores.add(jugadorActualizado);

                                        // Si el jugador de la lista somos nosotros, actualizamos nuestra interfaz
                                        if (tag.equals(miPropioIdGoogle)) {
                                            estoyEnEquipoAzul = equipo.equalsIgnoreCase("AZUL");
                                            jugadorLocal = jugadorActualizado;
                                            actualizarBotonesEquipo();
                                        }

                                    } catch (Exception e) {
                                        android.util.Log.e("API_LOBBY", "Error leyendo a un jugador", e);
                                    }
                                }

                                //  Avisamos al RecyclerView de que repinte todo
                                adapter.notifyDataSetChanged();
                                actualizarContadores(listaJugadores);
                            });
                        }
                    } catch (Exception e) {
                        android.util.Log.e("API_LOBBY", "Error procesando el JSON del Lobby", e);
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(SalaEsperaActivity.this, "No se pudo cargar la sala. Código: " + response.code(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
<<<<<<< HEAD
    private void iniciarPartida() {
        // 1. Doble comprobación de seguridad: solo el líder da la orden
        if (!soyElCreador) {
            android.widget.Toast.makeText(this, "Solo el líder puede iniciar la misión.", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Preparamos el cliente HTTP y la URL
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        String url = "http://10.0.2.2:8080/api/partida/" + idPartida + "/iniciar"; // Ajusta tu IP si es necesario

        com.example.secretpanda.data.TokenManager tokenManager = new com.example.secretpanda.data.TokenManager(this);
        String token = tokenManager.getToken();

        // 3. En peticiones PUT es obligatorio mandar un Body. Mandamos uno vacío.
        okhttp3.RequestBody body = okhttp3.RequestBody.create(null, "");

        okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder()
                .url(url)
                .put(body);

        if (token != null && !token.isEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer " + token);
        }

        okhttp3.Request request = requestBuilder.build();

        // 4. Disparamos la orden
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                android.util.Log.e("API_INICIAR", "Error de red al intentar iniciar la partida", e);
                runOnUiThread(() -> android.widget.Toast.makeText(SalaEsperaActivity.this, "Error de comunicación con la base.", android.widget.Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful()) {
                    android.util.Log.d("API_INICIAR", "¡Orden de inicio recibida por el servidor!");

                } else {
                    android.util.Log.e("API_INICIAR", "Error del servidor: " + response.code());
                    runOnUiThread(() -> android.widget.Toast.makeText(SalaEsperaActivity.this, "No se pudo iniciar. ¿Están todos listos?", android.widget.Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
    private void abandonarLobby() {
        if (stompClient != null && stompClient.isConnected()) {
            try {
                // La ruta para avisar de que nos vamos
                String destination = "/app/partida/" + idPartida + "/abandonarLobby";

                // Mandamos un JSON vacío o un texto simple, ya que el backend
                // sabe quiénes somos por la sesión del WebSocket / Token.
                stompClient.send(destination, "{}").subscribe(() -> {
                    android.util.Log.d("WS_SALIR", "Aviso de abandono enviado al servidor.");

                    // Desconectamos el radar de forma segura
                    stompClient.disconnect();
                }, throwable -> {
                    android.util.Log.e("WS_SALIR", "Error al enviar el aviso de abandono", throwable);
                });
            } catch (Exception e) {
                android.util.Log.e("WS_SALIR", "Error creando mensaje de abandono", e);
            }
        }

        // Finalmente, cerramos la pantalla
        finish();
    }


=======
>>>>>>> b1648d95a10bb97a88257c4d33d8b0e75c37d2e0
}