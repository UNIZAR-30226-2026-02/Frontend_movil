package com.example.secretpanda.ui.game.match;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.secretpanda.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompCommand;
import ua.naiksoftware.stomp.dto.StompHeader;
import ua.naiksoftware.stomp.dto.StompMessage;

public class PartidaActivity extends AppCompatActivity {

    private TextView btnAbandonar;
    private View btnAlerta;
    private View btnChat;
    private GridLayout gridTablero;
    private TextView notificacionChat;
    private TextView tvMiRol;
    private android.widget.ImageView iconoBtnAlerta;
    private boolean hayPista = false;
    private final String AGENTE_STRING = "Agente de Campo";
    private final String JEFE_STRING = "Jefe de Espionaje";
    private final String ROJO_STRING = "rojo";
    private final String AZUL_STRING = "azul";
    private String miRol = AGENTE_STRING;
    private android.widget.ImageView[] imagenesTablero;

    private String palabraPista = "";
    private int cantidadPista = 0;

    private List<JSONObject> historialChat = new ArrayList<>();
    private String palabraPistaJefe = "";
    private String numeroPistaJefe = "";
    private StompClient stompClient;
    private int idPartidaActual;
    private String miEquipo;
    private LinearLayout contenedorMensajesActual = null;
    private String miPropioIdGoogle = "";

    private int idTurnoActual = -1; // Para saber en qué turno estamos votando
    private FrameLayout cartaActualmenteSeleccionada = null;

    private TextView tvTimer;         // id: tv_timer
    private TextView tvFasePartida;   // id: tv_fase_partida
    private View circuloTurno;        // id: circulo_turno
    private TextView tvPuntosRojo;    // id: tv_puntos_rojo
    private TextView tvPuntosAzul;    // id: tv_puntos_azul

    private String equipoTurnoActual = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partida);

        // --- RECOGER DATOS DE LA PANTALLA ANTERIOR ---
        idPartidaActual = getIntent().getIntExtra("ID_PARTIDA", -1);
        miPropioIdGoogle = getIntent().getStringExtra("MI_NOMBRE_USUARIO");

        // Leer el equipo como String (Soluciona el error ClassCastException)
        miEquipo = getIntent().getStringExtra("MI_EQUIPO");
        if (miEquipo == null || miEquipo.isEmpty()) {
            miEquipo = "rojo"; // Por seguridad
        }
        obtenerMiRolDelServidor();


        // Si el ID es -1, significa que hubo un error al pasar los datos
        if (idPartidaActual == -1) {
            Toast.makeText(this, "Error: No se encontró la partida", Toast.LENGTH_SHORT).show();
            finish(); // Cerramos la pantalla porque está rota
            return;
        }

        /*String url = "ws://10.0.2.2:8080/ws/websocket"; // Ajusta a la URL real de tu backend
        stompClient = ua.naiksoftware.stomp.Stomp.over(ua.naiksoftware.stomp.Stomp.ConnectionProvider.OKHTTP, url);

        stompClient.connect();
        suscribirseAlEstadoDeLaPartida();
        obtenerEstadoCompletoDePartida();
        conectarWebSocket();
        suscribirseAlChat();
        btnAbandonar = findViewById(R.id.btn_abandonar);
        btnAlerta = findViewById(R.id.btn_alerta);
        btnChat = findViewById(R.id.btn_chat);
        gridTablero = findViewById(R.id.grid_tablero);
        notificacionChat = findViewById(R.id.notificacion_chat);
        // Enlazamos las nuevas variables
        tvMiRol = findViewById(R.id.tv_mi_rol);
        iconoBtnAlerta = findViewById(R.id.icono_btn_alerta);
        //mostrarDialogoResultadoVotacion("Carta más votada (1/3)", "La carta es : Buena.", "El turno continua");
        //mostrarDialogoResultadoVotacion("Carta más votada (1/3)", "La carta es : Mala.", "El turno se pierde");
        //mostrarDialogoResultadoVotacion("Carta más votada (1/3)", "La carta es : la Muerte.", "Perdistes");
        //mostrarDialogoFinPartida("Victoria", "Has encontrado al asesino", 10000, 20);
        configurarBotones();
        configurarTablero();*/

        conectarWebSocket();

        suscribirseAlEstadoDeLaPartida();
        suscribirseAlChat();
        suscribirseAPista();

        obtenerEstadoCompletoDePartida();

        btnAbandonar = findViewById(R.id.btn_abandonar);
        btnAlerta = findViewById(R.id.btn_alerta);
        btnChat = findViewById(R.id.btn_chat);
        gridTablero = findViewById(R.id.grid_tablero);
        notificacionChat = findViewById(R.id.notificacion_chat);
        tvMiRol = findViewById(R.id.tv_mi_rol);
        iconoBtnAlerta = findViewById(R.id.icono_btn_alerta);
        tvTimer = findViewById(R.id.tv_timer);
        tvFasePartida = findViewById(R.id.tv_fase_partida);
        circuloTurno = findViewById(R.id.circulo_turno);
        tvPuntosRojo = findViewById(R.id.tv_puntos_rojo);
        tvPuntosAzul = findViewById(R.id.tv_puntos_azul);
        suscribirseAlTemporizador();

        configurarBotones();
    }

    // Centralizamos el pintado para no repetir código
    private void pintarTablero(org.json.JSONArray cartasArray, org.json.JSONArray votosArray, int totalAgentes) {
        runOnUiThread(() -> {
            if (gridTablero == null) return;
            gridTablero.removeAllViews();
            gridTablero.setColumnCount(4);
            imagenesTablero = new android.widget.ImageView[cartasArray.length()];

            for (int i = 0; i < cartasArray.length(); i++) {
                try {
                    org.json.JSONObject cartaJson = cartasArray.getJSONObject(i);

                    int idCarta = cartaJson.optInt("id_carta_tablero");
                    String palabra = cartaJson.optString("palabra", "");
                    String estado = cartaJson.optString("estado", "oculta");
                    String tipo = cartaJson.optString("tipo", "");
                    boolean estaRevelada = !"oculta".equalsIgnoreCase(estado);


                    // CONTAR VOTOS Y SABER SI YO HE VOTADO AQUÍ

                    int contadorVotos = 0;
                    boolean heVotadoYo = false;

                    for (int v = 0; v < votosArray.length(); v++) {
                        org.json.JSONObject votoJson = votosArray.getJSONObject(v);
                        if (votoJson.optInt("id_carta_tablero", -1) == idCarta || votoJson.optInt("idCartaTablero", -1) == idCarta) {
                            contadorVotos++;

                            // Comprobamos si este voto es mío (el backend debe mandar el id_google o id_jugador en el voto)
                            String idVotante = votoJson.optString("id_google", votoJson.optString("id_jugador", ""));
                            if (miPropioIdGoogle.equals(idVotante)) {
                                heVotadoYo = true;
                            }
                        }
                    }


                    //CONFIGURAR EL CONTENEDOR (BORDES)

                    FrameLayout cartaContenedor = new FrameLayout(PartidaActivity.this);
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = 0;
                    params.height = dpToPx(85);
                    params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                    params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                    params.setMargins(dpToPx(3), dpToPx(3), dpToPx(3), dpToPx(3));
                    cartaContenedor.setLayoutParams(params);


                    if (!estaRevelada && contadorVotos > 0) {
                        // Si hay votos, borde grueso de 4dp
                        cartaContenedor.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
                        if (heVotadoYo) {
                            cartaContenedor.setBackgroundColor(Color.parseColor("#4CAF50")); // Borde VERDE (Mi voto)
                        } else {
                            cartaContenedor.setBackgroundColor(Color.parseColor("#FFC107")); // Borde AMARILLO (Voto de compañero)
                        }
                    } else {
                        // Si no hay votos (o está revelada), borde fino negro normal de 1dp
                        cartaContenedor.setPadding(dpToPx(1), dpToPx(1), dpToPx(1), dpToPx(1));
                        cartaContenedor.setBackgroundColor(Color.BLACK);
                    }

                    // FONDO E IMAGEN
                    FrameLayout fondoColor = new FrameLayout(PartidaActivity.this);
                    fondoColor.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                    android.widget.ImageView imagenCarta = new android.widget.ImageView(PartidaActivity.this);
                    imagenCarta.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    imagenCarta.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                    imagenCarta.setImageResource(R.drawable.fondo_carta_gruesa);

                    // TEXTO
                    TextView tvPalabra = new TextView(PartidaActivity.this);
                    tvPalabra.setText(palabra);
                    tvPalabra.setTextColor(Color.WHITE);
                    tvPalabra.setGravity(Gravity.CENTER);
                    tvPalabra.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    tvPalabra.setTypeface(null, Typeface.BOLD);
                    tvPalabra.setShadowLayer(4f, 0f, 0f, Color.BLACK);

                    // REVELACIÓN Y CLICS SEGuROS
                    if (estaRevelada) {
                        if ("rojo".equalsIgnoreCase(tipo)) fondoColor.setBackgroundColor(Color.parseColor("#D32F2F"));
                        else if ("azul".equalsIgnoreCase(tipo)) fondoColor.setBackgroundColor(Color.parseColor("#1976D2"));
                        else if ("asesino".equalsIgnoreCase(tipo)) fondoColor.setBackgroundColor(Color.BLACK);
                        else fondoColor.setBackgroundColor(Color.parseColor("#B0BEC5"));

                        imagenCarta.setAlpha(0.25f);
                        cartaContenedor.setEnabled(false);
                    } else {
                        fondoColor.setBackgroundColor(Color.parseColor("#546E7A"));
                        imagenCarta.setAlpha(0.8f);


                        cartaContenedor.setOnClickListener(v -> {

                            if (JEFE_STRING.equalsIgnoreCase(miRol) ||
                                    idTurnoActual == -1 ||
                                    !miEquipo.equalsIgnoreCase(equipoTurnoActual)) {

                                mostrarPreviewCarta(palabra);
                            } else {

                                enviarVoto(idCarta);
                            }
                        });
                    }

                    fondoColor.addView(imagenCarta);
                    fondoColor.addView(tvPalabra);

                    // BURBUJA DE VOTOS (Ej: 1/3)
                    if (contadorVotos > 0 && !estaRevelada) {
                        TextView tvVotos = new TextView(PartidaActivity.this);
                        // Ponemos formato "X/Y"
                        tvVotos.setText(contadorVotos + "/" + totalAgentes);
                        tvVotos.setTextColor(Color.WHITE);
                        tvVotos.setBackgroundColor(Color.parseColor("#AAFF9800"));
                        tvVotos.setGravity(Gravity.CENTER);
                        tvVotos.setTypeface(null, Typeface.BOLD);
                        tvVotos.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11); // Letra un poco más pequeña para que quepa


                        FrameLayout.LayoutParams votosParams = new FrameLayout.LayoutParams(dpToPx(35), dpToPx(24));
                        votosParams.gravity = Gravity.TOP | Gravity.END;
                        tvVotos.setLayoutParams(votosParams);
                        tvVotos.setBackgroundResource(android.R.drawable.presence_away);

                        fondoColor.addView(tvVotos);
                    }

                    cartaContenedor.addView(fondoColor);
                    imagenesTablero[i] = imagenCarta;
                    gridTablero.addView(cartaContenedor);

                } catch (Exception e) {
                    android.util.Log.e("WS_TABLERO", "Error pintando la carta " + i, e);
                }
            }
        });
    }

    private void suscribirseAlEstadoDeLaPartida() {
        String destinoTopic = "/user/queue/partidas/" + idPartidaActual + "/estado";
        android.util.Log.d("WS_TABLERO", "📡 Conectando radar al canal: " + destinoTopic);

        stompClient.topic(destinoTopic).subscribe(stompMessage -> {
            try {
                String jsonCrudo = stompMessage.getPayload();
                org.json.JSONObject json = new org.json.JSONObject(jsonCrudo);

                runOnUiThread(() -> {
                    String estado = json.optString("estado", "");
                    String equipoTurno = json.optString("equipo_turno_actual", "");
                    equipoTurnoActual = equipoTurno;
                    //  PINTAMOS EL CUADRADITO DEL TURNO (¡Corregido!)
                    if (circuloTurno != null) {
                        if (equipoTurno.equals(ROJO_STRING)) {
                            // Usamos Color.parseColor para evitar crasheos con el '#'
                            circuloTurno.setBackgroundColor(Color.parseColor("#9B3838"));
                        } else {
                            circuloTurno.setBackgroundColor(Color.parseColor("#38567A"));
                        }
                    }

                    //   COMPROBAMOS LA FASE ACTUAL Y LAS PISTAS
                    if (json.has("pista_actual") && !json.isNull("pista_actual")) {
                        try {
                            JSONObject pistaObj = json.getJSONObject("pista_actual");

                            // Guardamos el ID del turno para poder votar luego
                            idTurnoActual = pistaObj.optInt("id_turno", idTurnoActual);
                            palabraPista = pistaObj.optString("palabra", "");
                            cantidadPista = pistaObj.optInt("cantidad", pistaObj.optInt("numero", 0));

                            // ACTUALIZAMOS EL TEXTO DE LA FASE
                            if (tvFasePartida != null) tvFasePartida.setText("Fase: Agentes votando");

                            // Avisamos a los agentes con un Toast
                            android.widget.Toast.makeText(PartidaActivity.this,
                                    "📢Nueva pista: " + palabraPista + " (" + cantidadPista + ")",
                                    android.widget.Toast.LENGTH_LONG).show();

                        } catch (JSONException e) {
                            android.util.Log.e("WS_TABLERO", "Error leyendo la pista del JSON", e);
                        }
                    } else {
                        // Si la pista viene null, leemos el turno general (si existe)
                        idTurnoActual = json.optInt("id_turno", idTurnoActual);

                        // ACTUALIZAMOS EL TEXTO DE LA FASE
                        if (tvFasePartida != null) tvFasePartida.setText("Fase: El Jefe piensa...");
                    }

                    //  EXTRAEMOS LOS VOTOS
                    org.json.JSONArray votosArray = json.optJSONArray("votos_turno_actual");
                    if (votosArray == null) votosArray = new org.json.JSONArray();


                    int totalAgentes = json.optInt("total_agentes_equipo", 2);

                    // EXTRAEMOS EL TABLERO Y PINTAMOS
                    if (json.has("tablero")) {
                        try {
                            JSONObject tableroJson = json.getJSONObject("tablero");
                            if (tableroJson.has("cartas")) {
                                org.json.JSONArray tableroArray = tableroJson.getJSONArray("cartas");

                                // 🔥 Le pasamos el totalAgentes a la función
                                pintarTablero(tableroArray, votosArray, totalAgentes);
                            }
                        } catch (JSONException e) {
                            android.util.Log.e("WS_TABLERO", "Error leyendo el tablero", e);
                        }
                    }

                    if ("FINALIZADA".equalsIgnoreCase(estado)) {
                        android.util.Log.d("WS_TABLERO", "🏁 ¡La partida ha terminado!");
                        Toast.makeText(PartidaActivity.this, "¡Partida finalizada!", Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                android.util.Log.e("WS_TABLERO", "Error desencriptando el estado de la partida", e);
            }
        }, throwable -> {
            android.util.Log.e("WS_TABLERO", " Interferencia en el radar del tablero", throwable);
        });
    }

    private void obtenerEstadoCompletoDePartida() {
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        String url = "http://10.0.2.2:8080/api/partidas/" + idPartidaActual + "/estado";

        com.example.secretpanda.data.TokenManager tokenManager = new com.example.secretpanda.data.TokenManager(this);
        String token = tokenManager.getToken();

        okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder().url(url).get();
        if (token != null && !token.isEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer " + token);
        }

        client.newCall(requestBuilder.build()).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                android.util.Log.e("API_PARTIDA", " Error al obtener el estado de la partida", e);
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonCrudo = response.body().string();
                    try {
                        org.json.JSONObject json = new org.json.JSONObject(jsonCrudo);

                        runOnUiThread(() -> {

                            // GUARDAMOS EL TURNO Y PINTAMOS EL COLOR
                            String equipoTurno = json.optString("equipo_turno_actual", "");
                            equipoTurnoActual = equipoTurno;

                            if (circuloTurno != null) {
                                if (equipoTurno.equalsIgnoreCase("rojo")) {
                                    circuloTurno.setBackgroundColor(Color.parseColor("#9B3838"));
                                } else if (equipoTurno.equalsIgnoreCase("azul")) {
                                    circuloTurno.setBackgroundColor(Color.parseColor("#38567A"));
                                } else {
                                    circuloTurno.setBackgroundColor(Color.GRAY);
                                }
                            }


                            // FASE Y PISTA ACTUAL
                            if (json.has("pista_actual") && !json.isNull("pista_actual")) {
                                try {
                                    JSONObject pistaObj = json.getJSONObject("pista_actual");
                                    idTurnoActual = pistaObj.optInt("id_turno", idTurnoActual);
                                    palabraPista = pistaObj.optString("palabra", "");
                                    cantidadPista = pistaObj.optInt("cantidad", pistaObj.optInt("numero", 0));

                                    if (tvFasePartida != null) tvFasePartida.setText("Fase: Agentes votando");
                                } catch (JSONException e) { e.printStackTrace(); }
                            } else {
                                idTurnoActual = json.optInt("id_turno", idTurnoActual);
                                if (tvFasePartida != null) tvFasePartida.setText("Fase: El Jefe piensa...");
                            }


                            // RELOJ INICIAL (Por si el WS tarda en llegar)

                            int tiempoRestante = json.optInt("segundosRestantes", json.optInt("tiempo_restante", -1));
                            if (tiempoRestante != -1 && tvTimer != null) {
                                int minutos = tiempoRestante / 60;
                                int secs = tiempoRestante % 60;
                                tvTimer.setText(String.format(java.util.Locale.getDefault(), "%02d:%02d", minutos, secs));
                            }


                            // VOTOS Y TABLERO
                            org.json.JSONArray votosArray = json.optJSONArray("votos_turno_actual");
                            if (votosArray == null) votosArray = new org.json.JSONArray();
                            int totalAgentes = json.optInt("total_agentes_equipo", 2);

                            if (json.has("tablero")) {
                                try {
                                    JSONObject tableroJson = json.getJSONObject("tablero");
                                    if (tableroJson.has("cartas")) {
                                        org.json.JSONArray tableroArray = tableroJson.getJSONArray("cartas");
                                        pintarTablero(tableroArray, votosArray, totalAgentes);
                                    }
                                } catch (JSONException e) { e.printStackTrace(); }
                            }
                        });

                    } catch (Exception e) {
                        android.util.Log.e("API_PARTIDA", "Error procesando JSON", e);
                    }
                }
            }
        });
    }


    private void enviarVoto(int idCartaTablero) {
        // Seguridad: Lideres y Jefes no votan en el campo
        if (JEFE_STRING.equalsIgnoreCase(miRol)) {
            android.widget.Toast.makeText(this, "Solo los Agentes de campo pueden votar.", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        if (hayPista == false) {
            android.widget.Toast.makeText(this, "Aún no hay una pista activa. Espera a tu Jefe.", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Creamos el JSON exacto que espera tu VotarPayload
            org.json.JSONObject payload = new org.json.JSONObject();
            payload.put("idCartaTablero", idCartaTablero);
            // La ruta mapeada en tu @MessageMapping
            String destinoTopic = "/app/partidas/" + idPartidaActual + "/votar";
            //cabeceras JSON
            java.util.List<ua.naiksoftware.stomp.dto.StompHeader> headers = new java.util.ArrayList<>();
            headers.add(new ua.naiksoftware.stomp.dto.StompHeader(ua.naiksoftware.stomp.dto.StompHeader.DESTINATION, destinoTopic));
            headers.add(new ua.naiksoftware.stomp.dto.StompHeader("content-type", "application/json"));
            stompClient.send(new ua.naiksoftware.stomp.dto.StompMessage(
                    ua.naiksoftware.stomp.dto.StompCommand.SEND,
                    headers,
                    payload.toString()
            )).subscribe(() -> {
                android.util.Log.d("WS_VOTO", "Voto enviado correctamente a la carta: " + idCartaTablero);
            }, throwable -> {
                android.util.Log.e("WS_VOTO", "Error enviando el voto", throwable);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void conectarWebSocket() {
        // Si ya está conectado, no hacemos nada
        if (stompClient != null && stompClient.isConnected()) {
            return;
        }

        com.example.secretpanda.data.TokenManager tokenManager = new com.example.secretpanda.data.TokenManager(this);
        String token = tokenManager.getToken();

        java.util.List<ua.naiksoftware.stomp.dto.StompHeader> cabeceras = new java.util.ArrayList<>();
        if (token != null && !token.isEmpty()) {
            cabeceras.add(new ua.naiksoftware.stomp.dto.StompHeader("Authorization", "Bearer " + token));
        }

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://10.0.2.2:8080/ws/websocket");

        // Escuchamos los eventos de la conexión general
        stompClient.lifecycle().subscribe(lifecycleEvent -> {
            switch (lifecycleEvent.getType()) {
                case OPENED:
                    android.util.Log.d("WS_GENERAL", "¡Conexión principal establecida!");
                    break;
                case ERROR:
                    android.util.Log.e("WS_GENERAL", "Error en la conexión", lifecycleEvent.getException());
                    break;
                case CLOSED:
                    android.util.Log.d("WS_GENERAL", "Conexión cerrada");
                    break;
            }
        });

        stompClient.connect(cabeceras);
    }

    private void suscribirseAlChat() {
        if (miEquipo == null) return;

        String topicDestino = "/topic/partidas/" + idPartidaActual + "/chat/" + miEquipo.toLowerCase();

        stompClient.topic(topicDestino).subscribe(stompMessage -> {
            String jsonCrudo = stompMessage.getPayload();
            runOnUiThread(() -> {
                // Dentro de suscribirseAlChat -> runOnUiThread
                try {
                    JSONObject msgJson = new JSONObject(jsonCrudo);

                    // USA ESTAS CLAVES (coinciden con tu log)
                    String idJugadorMensaje = msgJson.getString("id_jugador"); // Antes quizás tenías idJugador
                    String textoDelMensaje = msgJson.getString("mensaje");
                    String autor = msgJson.getString("tag");

                    historialChat.add(msgJson);
                    // Filtro para no duplicar mi propio mensaje
                    if (miPropioIdGoogle != null && miPropioIdGoogle.equals(idJugadorMensaje)) {
                        return;
                    }

                    if (contenedorMensajesActual != null) {
                        agregarMensajeAlChat(contenedorMensajesActual, autor, textoDelMensaje, false);
                    }
                } catch (Exception e) {
                    Log.e("CHAT_ERROR", "Error al leer: " + jsonCrudo, e);
                }
            });
        }, throwable -> {
            Log.e("WS_CHAT", "Error en suscripción", throwable);
        });
    }

    // ==========================================
    // MÉTODO PARA APLICAR EL ROL
    // ==========================================
    private void aplicarRol() {
        if (miRol.equals(JEFE_STRING)) {
            // Si es el Jefe:
            tvMiRol.setText("Tu rol: " + JEFE_STRING);

            // Le ponemos un icono de "Lápiz/Editar" porque él escribe la pista
            iconoBtnAlerta.setImageResource(R.drawable.ic_anadir_pista);

            // Le damos permiso para abrir el cuadro de enviar pista
            btnAlerta.setOnClickListener(v -> mostrarDialogoPistaJefe());

        } else if (miRol.equals(AGENTE_STRING)) {
            // Si es un Agente de Campo:
            tvMiRol.setText("Tu rol: " + AGENTE_STRING);

            // Le ponemos un icono de "Información/Lupa" porque él busca las palabras
            iconoBtnAlerta.setImageResource(android.R.drawable.ic_menu_view); // O ic_dialog_info

            // El agente no envía pistas, así que le mostramos un mensaje distinto
            btnAlerta.setOnClickListener(v -> mostrarDialogoPistaJefe());
        }
    }
    private void configurarBotones() {
        // 1. ¡ACTUALIZADO! Ahora llama a nuestro nuevo diálogo personalizado
        btnAbandonar.setOnClickListener(v -> mostrarDialogoAbandonar());

        btnChat.setOnClickListener(v -> {
            notificacionChat.setVisibility(View.GONE);
            mostrarDialogoChat();
        });
    }

    // ==========================================
    // NUEVO MÉTODO PARA CONFIRMAR ABANDONO
    // ==========================================
    private void mostrarDialogoAbandonar() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_abandonar);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        dialog.findViewById(R.id.btn_cerrar_abandonar).setOnClickListener(v -> dialog.dismiss());

        dialog.findViewById(R.id.btn_confirmar_abandonar).setOnClickListener(v -> {
            dialog.dismiss();

            // ¡CONEXIÓN AL BACKEND! Llamamos a nuestro nuevo método
            abandonarPartidaEnBackend();
        });

        dialog.show();
    }

    private void abandonarPartidaEnBackend() {
        Toast.makeText(this, "Avisando al Cuartel General de la retirada...", Toast.LENGTH_SHORT).show();

        OkHttpClient client = new OkHttpClient();

        // ⚠️ ATENCIÓN: Asegúrate de que la ruta base es "/api/partidas" o "/api/partida" según tu Controlador
        String url = "http://10.0.2.2:8080/api/partidas/" + idPartidaActual + "/participantes";

        com.example.secretpanda.data.TokenManager tokenManager = new com.example.secretpanda.data.TokenManager(this);
        String jwt = tokenManager.getToken();

        // Construimos la petición DELETE
        Request request = new Request.Builder()
                .url(url)
                .delete() // ⬅️ Método DELETE para enlazar con tu @DeleteMapping
                .addHeader("Authorization", "Bearer " + jwt)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    android.util.Log.e("API_ABANDONAR", "Error de red", e);
                    // Aunque falle la red, sacamos al jugador de la pantalla visualmente
                    finish();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        // ¡Éxito! El servidor ejecutó el partidaService.abandonar(...)
                        Toast.makeText(PartidaActivity.this, "Te has retirado de la misión.", Toast.LENGTH_SHORT).show();
                    } else {
                        android.util.Log.e("API_ABANDONAR", "El servidor devolvió error: " + response.code());
                    }

                    // Independientemente de la respuesta, salimos de la pantalla (volvemos al menú)
                    finish();
                });
            }
        });
    }

    private void mostrarDialogoPistaJefe() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_anadir_pista);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        // Buscamos los elementos dentro del XML
        View btnCerrar = dialog.findViewById(R.id.btn_cerrar_pista);
        View btnEnviar = dialog.findViewById(R.id.btn_enviar_pista);
        android.widget.EditText inputPalabra = dialog.findViewById(R.id.input_palabra_pista);
        android.widget.EditText inputNumero = dialog.findViewById(R.id.input_numero_pista);

        TextView txtTitulo = dialog.findViewById(R.id.tv_titulo_pista);

        btnCerrar.setOnClickListener(v -> dialog.dismiss());

        if (miRol.equals(JEFE_STRING)&& miEquipo.equalsIgnoreCase(equipoTurnoActual)) {
            // VISTA: JEFE DE ESPIONAJE
            btnEnviar.setVisibility(View.VISIBLE);
            inputPalabra.setFocusableInTouchMode(true);
            inputNumero.setFocusableInTouchMode(true);

            if (txtTitulo != null) txtTitulo.setText("Añadir Pista");

            btnEnviar.setOnClickListener(v -> {
                String palabraEscrita = inputPalabra.getText().toString().trim();
                String numeroEscrito = inputNumero.getText().toString().trim();

                if (!palabraEscrita.isEmpty() && !numeroEscrito.isEmpty()) {
                    btnEnviar.setEnabled(false);
                    try {
                        JSONObject jsonPista = new JSONObject();
                        jsonPista.put("id_jugador_partida", 1);
                        jsonPista.put("palabraPista", palabraEscrita);
                        jsonPista.put("pistaNumero", Integer.parseInt(numeroEscrito));

                        String destino = "/app/partidas/" + idPartidaActual + "/pista";

                        List<StompHeader> headers = new ArrayList<>();
                        headers.add(new StompHeader(StompHeader.DESTINATION, destino));
                        headers.add(new StompHeader("content-type", "application/json"));

                        stompClient.send(new StompMessage(StompCommand.SEND, headers, jsonPista.toString()))
                                .subscribe(() -> {
                                    runOnUiThread(() -> {
                                        dialog.dismiss();
                                        Toast.makeText(PartidaActivity.this, "Pista enviada", Toast.LENGTH_SHORT).show();
                                    });
                                }, throwable -> {
                                    Log.e("WS_ERROR", "Error al enviar", throwable);
                                    runOnUiThread(() -> btnEnviar.setEnabled(true));
                                });

                    } catch (Exception e) {
                        e.printStackTrace();
                        btnEnviar.setEnabled(true);
                    }
                } else {
                    Toast.makeText(this, "Por favor, rellena la palabra y el número.", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            // VISTA: AGENTE DE CAMPO

            // Ocultar el botón azul de enviar
            btnEnviar.setVisibility(View.GONE);

            // Cambiar el texto del título principal
            if (txtTitulo != null) txtTitulo.setText("Pista Actual");

            // Bloquear los teclados
            inputPalabra.setFocusable(false);
            inputPalabra.setFocusableInTouchMode(false);
            inputPalabra.setClickable(false);
            inputPalabra.setCursorVisible(false);

            inputNumero.setFocusable(false);
            inputNumero.setFocusableInTouchMode(false);
            inputNumero.setClickable(false);
            inputNumero.setCursorVisible(false);

            //Mostrar la información recibida del Jefe
            if (cantidadPista > 0) {
                inputPalabra.setText(palabraPista);
                inputNumero.setText(String.valueOf(cantidadPista));
            } else {
                // Si abren la lupa y el jefe aún no ha mandado nada
                inputPalabra.setText("Esperando al jefe...");
                inputNumero.setText("-");
            }
        }

        dialog.show();
    }

    // NUEVO MÉTODO MÁGICO
    // NUEVO MÉTODO MÁGICO (ARREGLADO )
    private void mostrarDialogoChat() {
        // 1. Creamos el diálogo base
        android.app.Dialog dialog = new android.app.Dialog(this);

        // 2. Le asignamos el diseño XML (Esto crea UNA SOLA VISTA real)
        dialog.setContentView(R.layout.dialog_chat);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );

            // ¡EL TRUCO DEL TECLADO!
            dialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        // 3. Buscamos TODOS los elementos DENTRO del diálogo real
        // Fíjate que usamos 'dialog.findViewById' para todo
        this.contenedorMensajesActual = dialog.findViewById(R.id.contenedor_mensajes);
        View btnCerrar = dialog.findViewById(R.id.btn_cerrar_chat);
        android.widget.EditText inputMensaje = dialog.findViewById(R.id.input_mensaje);
        FrameLayout btnEnviar = dialog.findViewById(R.id.btn_enviar_mensaje);
        android.widget.ScrollView scrollChat = dialog.findViewById(R.id.scroll_chat);
        View zonaDeEscribir = (View) inputMensaje.getParent();

        // 4. Lógica de roles
        if (miRol.equals(JEFE_STRING)) {
            zonaDeEscribir.setVisibility(View.GONE);
        } else {
            zonaDeEscribir.setVisibility(View.VISIBLE);
        }

        for (int i = 0; i < historialChat.size(); i++) {
            try {
                android.util.Log.d("CHAT_DEBUG", "Dibujando mensaje número: " + i);
                org.json.JSONObject msgJson = historialChat.get(i);
                String idJugadorMensaje = msgJson.getString("id_jugador");
                String texto = msgJson.getString("mensaje");
                String autor = msgJson.optString("tag", "Jugador");

                boolean esMio = idJugadorMensaje.equals(miPropioIdGoogle);

                if (esMio) {
                    autor = "Yo";
                }
                agregarMensajeAlChat(contenedorMensajesActual, autor, texto, esMio);
            } catch (Exception e) { e.printStackTrace(); }
        }
        // 5. Botones y acciones
        btnCerrar.setOnClickListener(v -> dialog.dismiss());

        btnEnviar.setOnClickListener(v -> {
            String textoEscrito = inputMensaje.getText().toString().trim();

            if (!textoEscrito.isEmpty()) {
                // 1. Pintamos el mensaje en NUESTRA pantalla inmediatamente
                agregarMensajeAlChat(this.contenedorMensajesActual, "Yo", textoEscrito, true);
                inputMensaje.setText(""); // Vaciamos la caja de texto

                // 2. Disparamos al servidor CON CABECERAS JSON
                try {
                    JSONObject jsonMensaje = new JSONObject();
                    jsonMensaje.put("mensaje", textoEscrito);
                    //jsonMensaje.put("id_jugador_partida", 1);
                    String destino = "/app/partidas/" + idPartidaActual + "/chat";

                    java.util.List<ua.naiksoftware.stomp.dto.StompHeader> headers = new java.util.ArrayList<>();
                    headers.add(new ua.naiksoftware.stomp.dto.StompHeader(ua.naiksoftware.stomp.dto.StompHeader.DESTINATION, destino));
                    headers.add(new ua.naiksoftware.stomp.dto.StompHeader("content-type", "application/json"));

                    stompClient.send(new ua.naiksoftware.stomp.dto.StompMessage(
                            ua.naiksoftware.stomp.dto.StompCommand.SEND,
                            headers,
                            jsonMensaje.toString()
                    )).subscribe();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                // 3. Hacemos scroll hacia abajo
                scrollChat.post(() -> scrollChat.fullScroll(View.FOCUS_DOWN));
            }
        });

        // 6. ¡LLAMAMOS AL HISTORIAL!
        // Ahora contenedorMensajesActual apunta a la ventana visible de verdad.
        //cargarHistorialChat();

        // 7. Mostramos el diálogo (Y hemos borrado toda la morralla del AlertDialog)
        dialog.show();
    }



    /*private void agregarMensajeAlChat(LinearLayout contenedor, String remitente, String texto, boolean esMio) {

        // Creamos la caja del mensaje (El globo)
        LinearLayout globoMensaje = new LinearLayout(this);
        globoMensaje.setOrientation(LinearLayout.VERTICAL);
        // Ajustamos el padding para que parezca más un chat moderno
        globoMensaje.setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(10));

        // 🌟 MEJORA CLAVE: Usamos WRAP_CONTENT para que el globo "abrace" el texto
        // y no ocupe toda la pantalla de lado a lado.
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dpToPx(12);

        int colorNombre;
        int colorTexto;

        if (esMio) {
            // 🌟 ALINEAR A LA DERECHA (Gravedad END)
            params.gravity = Gravity.END;
            // Damos un margen izquierdo grande para que los textos largos no peguen con el otro lado
            params.setMarginStart(dpToPx(60));
            params.setMarginEnd(dpToPx(8));
            globoMensaje.setBackgroundResource(R.drawable.fondo_mensaje_mio);

            colorNombre = Color.parseColor("#004D40"); // Un verde muy oscuro y elegante para tu nombre
            colorTexto = Color.BLACK;
            remitente = "Tú"; // Forzamos que ponga "Tú" para que sea más inmersivo
        } else {
            // 🌟 ALINEAR A LA IZQUIERDA (Gravedad START)
            params.gravity = Gravity.START;
            params.setMarginStart(dpToPx(8));
            params.setMarginEnd(dpToPx(60)); // Damos margen derecho para que no cruce toda la pantalla
            globoMensaje.setBackgroundResource(R.drawable.fondo_mensaje_otro);

            colorNombre = Color.parseColor("#FFD54F"); // Un tono dorado/amarillo que resalta sobre fondo oscuro
            colorTexto = Color.WHITE;
        }

        // Aplicamos la configuración de diseño al globo
        globoMensaje.setLayoutParams(params);

        // 2. Estilo del Nombre del Remitente (Más pequeñito y en negrita)
        TextView tvRemitente = new TextView(this);
        tvRemitente.setText(remitente);
        tvRemitente.setTextColor(colorNombre);
        tvRemitente.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tvRemitente.setTypeface(null, Typeface.BOLD);
        tvRemitente.setPadding(0, 0, 0, dpToPx(2)); // Mini separación entre el nombre y el mensaje

        // 3. Estilo del Texto del mensaje (Más grande para leer bien)
        TextView tvTexto = new TextView(this);
        tvTexto.setText(texto);
        tvTexto.setTextColor(colorTexto);
        tvTexto.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        // Protegemos la vista: si alguien escribe un testamento, el texto saltará de línea
        tvTexto.setMaxWidth(dpToPx(250));

        // 4. Lo empaquetamos todo en el globo
        globoMensaje.addView(tvRemitente);
        globoMensaje.addView(tvTexto);

        // 5. Lo mandamos a la pantalla
        contenedor.addView(globoMensaje);
    }*/

    /**
     * Inyecta el XML 'item_mensaje_chat' en el contenedor y lo rellena con los datos.
     */
    private void agregarMensajeAlChat(LinearLayout contenedor, String remitente, String texto, boolean esMio) {


        View vistaMensaje = getLayoutInflater().inflate(R.layout.item_mensaje_chat, contenedor, false);

        //Buscamos las piezas por su ID dentro de esa vista
        LinearLayout globoMensaje = vistaMensaje.findViewById(R.id.globo_mensaje);
        TextView tvRemitente = vistaMensaje.findViewById(R.id.tv_remitente);
        TextView tvTexto = vistaMensaje.findViewById(R.id.tv_texto_mensaje);

        // Escribimos los textos
        tvRemitente.setText(remitente);
        tvTexto.setText(texto);

        // 4. Modificamos colores y posición dependiendo de si es nuestro o no
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) globoMensaje.getLayoutParams();

        if (esMio) {
            // ALINEAR A LA DERECHA
            params.gravity = Gravity.END;
            params.setMarginStart(dpToPx(60));
            params.setMarginEnd(dpToPx(8));
            globoMensaje.setBackgroundResource(R.drawable.fondo_mensaje_mio);

            tvRemitente.setTextColor(Color.parseColor("#004D40")); // Verde oscuro
            tvTexto.setTextColor(Color.BLACK);
            tvRemitente.setText("Tú"); // Forzamos el nombre "Tú"
        } else {
            // alinear a la izquierda
            params.gravity = Gravity.START;
            params.setMarginStart(dpToPx(8));
            params.setMarginEnd(dpToPx(60));
            globoMensaje.setBackgroundResource(R.drawable.fondo_mensaje_otro);

            tvRemitente.setTextColor(Color.parseColor("#FFD54F")); // Dorado/Amarillo
            tvTexto.setTextColor(Color.WHITE);
        }

        // Aplicamos la posición y lo metemos a la pantalla
        globoMensaje.setLayoutParams(params);
        contenedor.addView(vistaMensaje);
    }

    private void configurarTablero() {
        gridTablero.removeAllViews();
        int totalCartas = 20;

        imagenesTablero = new android.widget.ImageView[totalCartas];
        for (int i = 0; i < totalCartas; i++) {
            final int i2 = i;

            // 1. CREAR EL CONTENEDOR
            FrameLayout cartaContenedor = new FrameLayout(this);
            cartaContenedor.setClipToPadding(false);

            GridLayout.LayoutParams gridParams = new GridLayout.LayoutParams();
            gridParams.width = 0;
            gridParams.height = dpToPx(75);
            gridParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            gridParams.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
            cartaContenedor.setLayoutParams(gridParams);

            // 2. CREAR EL FONDO Y LA IMAGEN
            View cartaFondo = new View(this);
            FrameLayout.LayoutParams fondoParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            cartaFondo.setLayoutParams(fondoParams);
            cartaFondo.setBackgroundResource(R.drawable.fondo_blanco_redondeado);
            cartaFondo.setElevation(dpToPx(2));

            // ¡NUEVO! Creamos el ImageView para la foto
            android.widget.ImageView imagenVisual = new android.widget.ImageView(this);
            FrameLayout.LayoutParams imgParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            // Le damos un poco de margen para que se vea el borde blanco del fondo
            imgParams.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
            imagenVisual.setLayoutParams(imgParams);
            imagenVisual.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
            imagenVisual.setElevation(dpToPx(3)); // Un poco más alto que el fondo

            imagenesTablero[i] = imagenVisual;

            // 3. CREAR LA ETIQUETA "1/x"
            TextView etiquetaFraccion = new TextView(this);
            FrameLayout.LayoutParams etiquetaParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            etiquetaParams.gravity = Gravity.TOP | Gravity.END;
            etiquetaFraccion.setLayoutParams(etiquetaParams);

            etiquetaFraccion.setBackgroundResource(R.drawable.fondo_etiqueta_esquina);
            etiquetaFraccion.setElevation(dpToPx(4));
            etiquetaFraccion.setPadding(dpToPx(4), dpToPx(1), dpToPx(4), dpToPx(1));

            etiquetaFraccion.setText("1/x"); // Le pongo 1/1 por defecto, ya que solo hay 1 seleccionable
            etiquetaFraccion.setTextColor(Color.BLACK);
            etiquetaFraccion.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            etiquetaFraccion.setTypeface(null, Typeface.BOLD);
            etiquetaFraccion.setVisibility(View.INVISIBLE);

            // 4. METER EL FONDO Y LA ETIQUETA
            cartaContenedor.addView(cartaFondo);
            cartaContenedor.addView(imagenVisual); // <--- Metemos la imagen
            cartaContenedor.addView(etiquetaFraccion);

            // 5. DARLE EL CLIC A LA CARTA (LÓGICA ACTUALIZADA)
            cartaContenedor.setTag(false);


            cartaContenedor.setOnClickListener(v -> {
                boolean estaSeleccionada = (boolean) cartaContenedor.getTag();

                // 1. Bloqueo al Jefe
                if (miRol.equals(JEFE_STRING)) {
                    Toast.makeText(this, "Como Jefe, no puedes seleccionar cartas.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!estaSeleccionada) {
                    // 2. Apagar la carta anterior (¡ARREGLADO EL ERROR DEL ÍNDICE!)
                    if (cartaActualmenteSeleccionada != null) {
                        View fondoAnterior = cartaActualmenteSeleccionada.getChildAt(0);
                        // Ahora la etiqueta es el hijo 2 (0=Fondo, 1=Imagen, 2=Etiqueta)
                        TextView etiquetaAnterior = (TextView) cartaActualmenteSeleccionada.getChildAt(2);

                        fondoAnterior.setBackgroundResource(R.drawable.fondo_blanco_redondeado);
                        etiquetaAnterior.setVisibility(View.INVISIBLE);
                        cartaActualmenteSeleccionada.setTag(false);
                    }

                    // 3. Encender la nueva carta
                    cartaFondo.setBackgroundResource(R.drawable.fondo_carta_seleccionada);
                    etiquetaFraccion.setVisibility(View.VISIBLE);
                    cartaContenedor.setTag(true);

                    cartaActualmenteSeleccionada = cartaContenedor;

                    // ==========================================
                    // 4. ¡TRANSMITIR EL VOTO AL SERVIDOR!
                    // ==========================================
                    try {
                        JSONObject jsonVoto = new JSONObject();
                        // i es la posición de la carta en el tablero (0 a 19)
                        jsonVoto.put("posicionCarta", i2);

                        // Disparamos el voto por el WebSocket
                        String destinoVoto = "/app/partidas/" + idPartidaActual + "/votar";
                        stompClient.send(destinoVoto, jsonVoto.toString()).subscribe();

                    } catch (Exception e) {
                        android.util.Log.e("WS_VOTO", "Error enviando voto", e);
                    }

                } else {
                    // Deseleccionar la carta actual
                    cartaFondo.setBackgroundResource(R.drawable.fondo_blanco_redondeado);
                    etiquetaFraccion.setVisibility(View.INVISIBLE);
                    cartaContenedor.setTag(false);
                    cartaActualmenteSeleccionada = null;

                    // Aquí podrías enviar un mensaje al servidor avisando de que has quitado tu voto
                }
            });

            // 6. AÑADIR AL TABLERO
            gridTablero.addView(cartaContenedor);
        }
    }
    // Llama a este método cuando quieras mostrar el resultado, pasándole los textos
    public void mostrarDialogoResultadoVotacion(String estado, String resultado, String consecuencia) {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_resultado_votacion);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        // Buscamos los elementos de la interfaz
        View btnCerrar = dialog.findViewById(R.id.btn_cerrar_resultado);
        android.widget.TextView tvEstado = dialog.findViewById(R.id.tv_estado_votacion);
        android.widget.TextView tvResultado = dialog.findViewById(R.id.tv_resultado_carta);
        android.widget.TextView tvConsecuencia = dialog.findViewById(R.id.tv_consecuencia_turno);
        View vistaCarta = dialog.findViewById(R.id.vista_carta_votada);


        // Actualizamos los textos con los parámetros que hayamos recibido
        tvEstado.setText(estado);
        tvResultado.setText(resultado);
        tvConsecuencia.setText(consecuencia);

        /* NOTA PARA EL FUTURO:
           Si quieres que la carta cambie de color según el equipo,
           podrás hacerlo así:

        */

        // Lógica de cerrar
        btnCerrar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
    // Método para mostrar el fin de la partida
    public void mostrarDialogoFinPartida(String titulo, String explicacion, int puntosActuales, int puntosGanados) {
        // Usamos un estilo de pantalla completa para que ocupe todo y oscurezca el fondo
        android.app.Dialog dialog = new android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_fin_partida);

        if (dialog.getWindow() != null) {
            // Fondo negro semi-transparente
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.parseColor("#CC1A1A1A")));
        }

        // Buscamos los elementos
        android.widget.TextView tvTitulo = dialog.findViewById(R.id.tv_titulo_fin);
        android.widget.TextView tvExplicacion = dialog.findViewById(R.id.tv_explicacion_fin);
        android.widget.TextView tvPuntosBase = dialog.findViewById(R.id.tv_puntos_base);
        android.widget.TextView tvSumaPuntos = dialog.findViewById(R.id.tv_suma_puntos);
        android.widget.TextView tvPuntosTotal = dialog.findViewById(R.id.tv_puntos_total);
        android.view.View btnVolver = dialog.findViewById(R.id.btn_volver_menu);

        // Aplicamos los textos
        tvTitulo.setText(titulo);
        tvExplicacion.setText(explicacion);
        tvPuntosBase.setText(String.valueOf(puntosActuales));

        // Matemáticas automáticas para mostrar la suma o resta
        int total = puntosActuales + puntosGanados;
        tvPuntosTotal.setText(String.valueOf(total));

        if (puntosGanados >= 0) {
            tvSumaPuntos.setText("+ " + puntosGanados);
        } else {
            // Si es negativo (ej. -15), Math.abs quita el signo para que no salga "- -15"
            tvSumaPuntos.setText("- " + Math.abs(puntosGanados));
        }

        // Lógica del botón inferior
        btnVolver.setOnClickListener(v -> {
            dialog.dismiss();
            finish(); // Esto cerrará PartidaActivity y te devolverá a la pantalla anterior (Menú)
        });

        // Evitamos que el jugador pueda cerrar la ventana tocando fuera. ¡Tienen que usar el botón!
        dialog.setCancelable(false);
        dialog.show();
    }

    // Llama a este método en tu onCreate()
    private void obtenerMiRolDelServidor() {
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();

        // ⚠️ ALERTA DE RUTA: Asegúrate de que esta sea la URL correcta de tu backend.
        // Si la ruta en tu Spring Boot es distinta, cámbiala aquí.
        String url = "http://10.0.2.2:8080/api/partidas/" + idPartidaActual + "/participantes/rol";

        // Extraemos la credencial (token) del agente
        com.example.secretpanda.data.TokenManager tokenManager = new com.example.secretpanda.data.TokenManager(this);
        String token = tokenManager.getToken();

        okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder()
                .url(url)
                .get(); // Asumo que es una petición GET

        // Añadimos el token al escudo de la petición
        if (token != null && !token.isEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer " + token);
        }

        client.newCall(requestBuilder.build()).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                android.util.Log.e("API_ROL", "❌ Fallo en las comunicaciones solicitando el rol", e);
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String respuestaServidor = response.body().string();

                    try {
                        // 🕵️‍♂️ DEPENDIENDO DE TU BACKEND, ELIGE UNA DE ESTAS DOS OPCIONES:

                        // Opción A: Si tu backend devuelve un JSON ej: {"rol": "Lider"}
                        org.json.JSONObject json = new org.json.JSONObject(respuestaServidor);
                        final String rolAsignado = json.optString("rol", "Agente");

                        // Opción B: Si tu backend devuelve solo el texto ej: "Lider"
                        // final String rolAsignado = respuestaServidor;

                        android.util.Log.d("API_ROL", "✅ El Cuartel General ha confirmado tu rol: " + rolAsignado);

                        // Pasamos a la interfaz gráfica para actualizarla
                        runOnUiThread(() -> {
                            miRol = rolAsignado; // Actualizamos la variable global

                            // Actualizamos el texto en la pantalla si tienes el TextView
                            if (tvMiRol != null) {
                                if (miRol.equals("agente")) {
                                    miRol = AGENTE_STRING;
                                } else if (miRol.equals("lider")) {
                                    miRol = JEFE_STRING;
                                }
                                aplicarRol();
                                tvMiRol.setText("Tu rol: " + miRol);
                            }
                        });

                    } catch (Exception e) {
                        android.util.Log.e("API_ROL", "Error al descifrar el rol recibido del servidor", e);
                    }
                } else {
                    // 🕵️‍♂️ Vamos a extraer el mensaje de error que Spring Boot nos manda por defecto
                    String cuerpoError = "Cuerpo vacío";
                    try {
                        if (response.body() != null) {
                            cuerpoError = response.body().string();
                        }
                    } catch (Exception e) {
                        cuerpoError = "No se pudo leer el error";
                    }

                    android.util.Log.e("API_ROL", "⚠️ El servidor denegó la solicitud de rol. Código: " + response.code() + " - Detalles: " + cuerpoError);
                }
            }
        });
    }

    private void suscribirseAlTablero() {
        if (stompClient == null || !stompClient.isConnected()) return;

        // Ajusta esta ruta a la que use tu backend para difundir el estado del juego
        String topicTablero = "/topic/partidas/" + idPartidaActual + "/estado";

        stompClient.topic(topicTablero).subscribe(stompMessage -> {
            String jsonCrudo = stompMessage.getPayload();

            runOnUiThread(() -> {
                try {
                    JSONObject estadoJuego = new JSONObject(jsonCrudo);
                    org.json.JSONArray cartasArray = estadoJuego.getJSONArray("cartas");

                    // Recorremos las 20 cartas del tablero
                    for (int i = 0; i < cartasArray.length(); i++) {
                        JSONObject carta = cartasArray.getJSONObject(i);

                        // 1. Extraemos la nueva información (Ajusta los nombres a lo que envíe tu backend)
                        int votosActuales = carta.optInt("votos", 0);
                        int votosNecesarios = carta.optInt("votosNecesarios", 1);
                        boolean estaRevelada = carta.optBoolean("revelada", false);
                        String identidadCarta = carta.optString("identidad", "desconocida"); // Ej: "Rojo", "Azul", "Civil", "Asesino"

                        FrameLayout cartaContenedor = (FrameLayout) gridTablero.getChildAt(i);
                        View cartaFondo = cartaContenedor.getChildAt(0);
                        TextView etiquetaFraccion = (TextView) cartaContenedor.getChildAt(2);

                        // 2. Lógica de revelación inmediata
                        if (estaRevelada) {
                            // Como ya está revelada, escondemos la etiqueta de votos
                            etiquetaFraccion.setVisibility(View.INVISIBLE);

                            // ¡REVELAMOS LA IDENTIDAD CAMBIANDO EL COLOR!
                            if (identidadCarta.equalsIgnoreCase("Rojo")) {
                                cartaFondo.setBackgroundColor(Color.RED);
                            } else if (identidadCarta.equalsIgnoreCase("Azul")) {
                                cartaFondo.setBackgroundColor(Color.BLUE);
                            } else if (identidadCarta.equalsIgnoreCase("Asesino")) {
                                cartaFondo.setBackgroundColor(Color.BLACK);
                            } else if (identidadCarta.equalsIgnoreCase("Civil")) {
                                cartaFondo.setBackgroundColor(Color.LTGRAY);
                            }

                            // Opcional: Desactivamos el clic para que no la puedan volver a votar
                            cartaContenedor.setEnabled(false);

                        } else {
                            // 3. Lógica de votos normal (si aún no está revelada)
                            if (votosActuales > 0) {
                                etiquetaFraccion.setText(votosActuales + "/" + votosNecesarios);
                                etiquetaFraccion.setVisibility(View.VISIBLE);
                            } else {
                                etiquetaFraccion.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                } catch (Exception e) {
                    android.util.Log.e("WS_TABLERO", "Error actualizando los votos", e);
                }
            });
        }, throwable -> {
            android.util.Log.e("WS_TABLERO", "Error en la suscripción del tablero", throwable);
        });
    }
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private void suscribirseAPista() {
        String topicPista = "/topic/partidas/" + idPartidaActual + "/pista";

        stompClient.topic(topicPista).subscribe(stompMessage -> {
            String payload = stompMessage.getPayload();
            runOnUiThread(() -> {
                try {
                    JSONObject json = new JSONObject(payload);
                    palabraPista = json.optString("palabra_pista", json.optString("palabraPista", ""));
                    cantidadPista = json.optInt("pista_numero", json.optInt("pistaNumero", 0));

                    //  ACTUALIZAR LA FASE EN LA CABECERA
                    if (tvFasePartida != null) tvFasePartida.setText("Fase: Agentes votando");

                    // Mostrar alerta
                    android.widget.Toast.makeText(PartidaActivity.this,
                            "📢 Nueva pista: " + palabraPista + " (" + cantidadPista + ")",
                            android.widget.Toast.LENGTH_LONG).show();

                } catch (Exception e) {
                    Log.e("WS_PISTA", "Error al leer pista: " + payload, e);
                }
            });
        });
    }

    private void mostrarPreviewCarta(String palabra) {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_preview_carta);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        View btnCerrar = dialog.findViewById(R.id.btn_cerrar_preview);
        TextView tvPalabra = dialog.findViewById(R.id.tv_palabra_preview);

        if (tvPalabra != null) tvPalabra.setText(palabra);

        btnCerrar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
    private void suscribirseAlTemporizador() {
        if (stompClient == null) return;
        String topicTemporizador = "/topic/partidas/" + idPartidaActual + "/temporizador";

        stompClient.topic(topicTemporizador).subscribe(stompMessage -> {
            String payload = stompMessage.getPayload();

            runOnUiThread(() -> {
                try {
                    int segundos = 0;

                    if (payload.contains("{")) {
                        JSONObject json = new JSONObject(payload);
                        segundos = json.optInt("segundosRestantes", json.optInt("tiempo", 0));
                    } else {
                        String numeroLimpio = payload.replaceAll("[^0-9]", "");
                        if (!numeroLimpio.isEmpty()) segundos = Integer.parseInt(numeroLimpio);
                    }

                    // Pintamos el reloj
                    if (tvTimer != null) {
                        int minutos = segundos / 60;
                        int secs = segundos % 60;

                        String tiempoFormateado = String.format(java.util.Locale.getDefault(), "%02d:%02d", minutos, secs);
                        tvTimer.setText(tiempoFormateado);

                        if (segundos <= 10) {
                            tvTimer.setTextColor(Color.parseColor("#FF5252")); // Rojo claro
                        } else {
                            tvTimer.setTextColor(Color.WHITE);
                        }
                    }
                } catch (Exception e) {
                    android.util.Log.e("WS_TEMP", "Error procesando temporizador", e);
                }
            });
        });
    }
}
