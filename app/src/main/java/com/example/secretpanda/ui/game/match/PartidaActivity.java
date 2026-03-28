package com.example.secretpanda.ui.game.match;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
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

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class PartidaActivity extends AppCompatActivity {

    private TextView btnAbandonar;
    private View btnAlerta;
    private View btnChat;
    private GridLayout gridTablero;
    private TextView notificacionChat;
    private TextView tvMiRol;
    private android.widget.ImageView iconoBtnAlerta;
    private String miRol = "Agente";

    private android.widget.ImageView[] imagenesTablero;

    private StompClient stompClient;
    private int idPartidaActual;
    private String miEquipo;
    private LinearLayout contenedorMensajesActual = null;
    private String miPropioIdGoogle = "";

    private FrameLayout cartaActualmenteSeleccionada = null;

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
        //suscribirseAlEstadoDeLaPartida();
        //obtenerEstadoCompletoDePartida();
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
        configurarTablero();

        aplicarRol();
    }

    private void suscribirseAlEstadoDeLaPartida() {
        String destinoTopic = "/topic/partidas/" + idPartidaActual + "/estado";
        android.util.Log.d("WS_TABLERO", "📡 Conectando radar al canal: " + destinoTopic);

        stompClient.topic(destinoTopic).subscribe(stompMessage -> {
            try {
                String jsonCrudo = stompMessage.getPayload();
                org.json.JSONObject json = new org.json.JSONObject(jsonCrudo);

                // Pasamos los datos al hilo principal para poder cambiar la pantalla
                runOnUiThread(() -> {
                    // 1. LEER EL ESTADO GENERAL
                    String estado = json.optString("estado", "");
                    String turnoActual = json.optString("turno_actual", "");
                    // boolean rojoGana = json.optBoolean("rojoGana", false); // Si lo necesitas luego

                    // Aquí podrías cambiar un TextView que diga de quién es el turno
                    // tvTurno.setText("Turno del equipo: " + turnoActual);

                    // 2. LEER EL TABLERO (Comprueba cómo se llama tu array en el JSON real, yo usaré "cartas")
                    if (json.has("cartas")) { // ⚠️ CAMBIA "cartas" por el nombre real de tu array en el JSON
                        try {
                            org.json.JSONArray tableroArray = json.getJSONArray("cartas");

                            // Aquí iría tu lógica para pintar las cartas en el gridTablero
                            // for (int i = 0; i < tableroArray.length(); i++) {
                            //     JSONObject carta = tableroArray.getJSONObject(i);
                            //     String tipo = carta.optString("tipo", "oculta");
                            //     // pintarCarta(carta, i);
                            // }
                        } catch (Exception e) {
                            android.util.Log.e("WS_TABLERO", "Error leyendo el array del tablero", e);
                        }
                    }

                    // 3. DETECTAR EL FINAL DE LA MISIÓN
                    if ("FINALIZADA".equalsIgnoreCase(estado)) {
                        android.util.Log.d("WS_TABLERO", "🏁 ¡La partida ha terminado!");
                        // Mostrar diálogo de victoria/derrota
                        // Toast.makeText(PartidaActivity.this, "¡Partida finalizada!", Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                android.util.Log.e("WS_TABLERO", "Error desencriptando el estado de la partida", e);
            }
        }, throwable -> {
            android.util.Log.e("WS_TABLERO", "❌ Interferencia en el radar del tablero", throwable);
        });
    }
    private void obtenerEstadoCompletoDePartida() {
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        // Reemplaza esto por tu endpoint real que devuelva los datos de la partida
        String url = "http://10.0.2.2:8080/api/partidas/" + idPartidaActual;

        com.example.secretpanda.data.TokenManager tokenManager = new com.example.secretpanda.data.TokenManager(this);
        String token = tokenManager.getToken();

        okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder().url(url).get();
        if (token != null && !token.isEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer " + token);
        }

        client.newCall(requestBuilder.build()).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                android.util.Log.e("API_PARTIDA", "❌ Error al obtener el estado de la partida", e);
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonCrudo = response.body().string();
                    try {
                        org.json.JSONObject json = new org.json.JSONObject(jsonCrudo);
                        // Aquí puedes extraer el array de "jugadores", de quién es el "turno_actual", etc.
                        android.util.Log.d("API_PARTIDA", "✅ Datos completos recuperados: " + json.toString());

                        // Si extraes datos para actualizar la interfaz, recuerda usar:
                        // runOnUiThread(() -> { ... });

                    } catch (Exception e) {
                        android.util.Log.e("API_PARTIDA", "Error procesando JSON de la partida", e);
                    }
                }
            }
        });
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
        // Validamos que tengamos la info necesaria
        if (stompClient == null || !stompClient.isConnected() || miEquipo == null) {
            android.util.Log.e("WS_CHAT", "No se puede suscribir al chat aún.");
            return;
        }

        String topicDestino = "/topic/partidas/" + idPartidaActual + "/chat/" + miEquipo.toLowerCase();

        stompClient.topic(topicDestino).subscribe(stompMessage -> {
            String jsonCrudo = stompMessage.getPayload();
            runOnUiThread(() -> {
                try {
                    org.json.JSONObject msgJson = new org.json.JSONObject(jsonCrudo);
                    String textoDelMensaje = msgJson.getString("mensaje");
                    String idJugadorMensaje = msgJson.optString("idJugador", msgJson.optString("id_jugador", ""));
                    String autor = msgJson.optString("tag", "Agente");

                    boolean esMio = (miPropioIdGoogle != null && miPropioIdGoogle.equals(idJugadorMensaje));
                    if (esMio) autor = "Yo";

                    if (contenedorMensajesActual != null) {
                        agregarMensajeAlChat(contenedorMensajesActual, autor, textoDelMensaje, esMio);
                        contenedorMensajesActual.requestLayout();
                        contenedorMensajesActual.invalidate();
                    }
                } catch (Exception e) {
                    android.util.Log.e("WS_CHAT", "Error desencriptando", e);
                }
            });
        }, throwable -> {
            android.util.Log.e("WS_CHAT", "Error suscribiéndose al chat", throwable);
        });
    }

    // ==========================================
    // MÉTODO PARA APLICAR EL ROL
    // ==========================================
    private void aplicarRol() {
        if (miRol.equals("Jefe")) {
            // Si es el Jefe:
            tvMiRol.setText("Tu rol: Jefe de espionaje");

            // Le ponemos un icono de "Lápiz/Editar" porque él escribe la pista
            iconoBtnAlerta.setImageResource(R.drawable.ic_anadir_pista);

            // Le damos permiso para abrir el cuadro de enviar pista
            btnAlerta.setOnClickListener(v -> mostrarDialogoPistaJefe());

        } else if (miRol.equals("Agente")) {
            // Si es un Agente de Campo:
            tvMiRol.setText("Tu rol: Agente de campo");

            // Le ponemos un icono de "Información/Lupa" porque él busca las palabras
            iconoBtnAlerta.setImageResource(android.R.drawable.ic_menu_view); // O ic_dialog_info

            // El agente no envía pistas, así que le mostramos un mensaje distinto
            btnAlerta.setOnClickListener(v -> android.widget.Toast.makeText(this, "Esperando a que el Jefe dé una pista...", android.widget.Toast.LENGTH_SHORT).show());
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
        // 1. Cargamos nuestro nuevo diseño
        dialog.setContentView(R.layout.dialog_anadir_pista);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        // 2. Buscamos los elementos dentro del XML
        View btnCerrar = dialog.findViewById(R.id.btn_cerrar_pista);
        View btnEnviar = dialog.findViewById(R.id.btn_enviar_pista);
        android.widget.EditText inputPalabra = dialog.findViewById(R.id.input_palabra_pista);
        android.widget.EditText inputNumero = dialog.findViewById(R.id.input_numero_pista);

        // 3. Lógica de cerrar la ventana
        btnCerrar.setOnClickListener(v -> dialog.dismiss());

        // 4. Lógica de enviar la pista
        btnEnviar.setOnClickListener(v -> {
            String palabraEscrita = inputPalabra.getText().toString().trim();
            String numeroEscrito = inputNumero.getText().toString().trim();

            // Comprobamos que no haya dejado ningún hueco en blanco
            if (!palabraEscrita.isEmpty() && !numeroEscrito.isEmpty()) {
                    try {
                        // 1. Empaquetamos la pista en un JSON como espera el servidor
                        JSONObject jsonPista = new JSONObject();
                        jsonPista.put("palabra", palabraEscrita);
                        jsonPista.put("numero", Integer.parseInt(numeroEscrito)); // Convertimos a número real

                        // 2. Disparamos la pista por el canal de STOMP
                        // (Asegúrate de que la ruta /app/partidas/... coincide con el @MessageMapping de tu backend)
                        String destino = "/app/partidas/" + idPartidaActual + "/pista";
                        stompClient.send(destino, jsonPista.toString()).subscribe(() -> {

                            // Si se envía bien, cerramos la ventana y avisamos
                            runOnUiThread(() -> {
                                dialog.dismiss();
                                Toast.makeText(PartidaActivity.this, "¡Pista transmitida con éxito!", Toast.LENGTH_SHORT).show();
                            });

                        }, throwable -> {
                            android.util.Log.e("WS_PISTA", "Error enviando la pista", throwable);
                            runOnUiThread(() -> Toast.makeText(PartidaActivity.this, "Error de radio. La pista no llegó.", Toast.LENGTH_SHORT).show());
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error formateando la pista", Toast.LENGTH_SHORT).show();
                    }

                dialog.dismiss(); // Cerramos el cuadro al enviar
            } else {
                android.widget.Toast.makeText(this, "Por favor, rellena la palabra y el número.", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    // NUEVO MÉTODO MÁGICO
    // NUEVO MÉTODO MÁGICO (ARREGLADO 🐼🔧)
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
        if (miRol.equals("Jefe")) {
            zonaDeEscribir.setVisibility(View.GONE);
        } else {
            zonaDeEscribir.setVisibility(View.VISIBLE);
        }

        // 5. Botones y acciones
        btnCerrar.setOnClickListener(v -> dialog.dismiss());

        btnEnviar.setOnClickListener(v -> {
            String textoEscrito = inputMensaje.getText().toString().trim();

            if (!textoEscrito.isEmpty()) {
                // Lo pintamos en MI pantalla

                //agregarMensajeAlChat(this.contenedorMensajesActual, "Yo", textoEscrito, true);

                // Disparamos al servidor
                try {
                    JSONObject jsonMensaje = new JSONObject();
                    jsonMensaje.put("mensaje", textoEscrito);
                    stompClient.send("/app/partidas/" + idPartidaActual + "/chat", jsonMensaje.toString()).subscribe();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                inputMensaje.setText("");
                scrollChat.post(() -> scrollChat.fullScroll(View.FOCUS_DOWN));
            }
        });

        // 6. ¡LLAMAMOS AL HISTORIAL!
        // Ahora contenedorMensajesActual apunta a la ventana visible de verdad.
        cargarHistorialChat();

        // 7. Mostramos el diálogo (Y hemos borrado toda la morralla del AlertDialog)
        dialog.show();
    }

    /**
     * Este método "fabrica" un bloque de mensaje como si fuera un Lego
     * y lo mete en el chat. Dependiendo de si es tuyo o no, cambia los colores y márgenes.
     */
    private void agregarMensajeAlChat(LinearLayout contenedor, String remitente, String texto, boolean esMio) {


        // Creamos la caja del mensaje
        LinearLayout globoMensaje = new LinearLayout(this);
        globoMensaje.setOrientation(LinearLayout.VERTICAL);
        globoMensaje.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));

        // Configuramos sus márgenes
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dpToPx(16);

        int colorTexto;

        if (esMio) {
            // Si es mío: margen a la izquierda y color clarito
            params.setMarginStart(dpToPx(40));
            params.setMarginEnd(0);
            globoMensaje.setBackgroundResource(R.drawable.fondo_mensaje_mio);
            colorTexto = Color.BLACK;
        } else {
            // Si es de otro: margen a la derecha y color oscuro
            params.setMarginStart(0);
            params.setMarginEnd(dpToPx(40));
            globoMensaje.setBackgroundResource(R.drawable.fondo_mensaje_otro);
            colorTexto = Color.WHITE;
        }

        globoMensaje.setLayoutParams(params);

        // Creamos el nombre del remitente
        TextView tvRemitente = new TextView(this);
        tvRemitente.setText(remitente);
        tvRemitente.setTextColor(colorTexto);
        tvRemitente.setTypeface(Typeface.SERIF, Typeface.BOLD_ITALIC);

        // Creamos el texto del mensaje
        TextView tvTexto = new TextView(this);
        tvTexto.setText(texto);
        tvTexto.setTextColor(colorTexto);
        tvTexto.setTypeface(Typeface.SERIF, Typeface.ITALIC);

        // Lo empaquetamos todo
        globoMensaje.addView(tvRemitente);
        globoMensaje.addView(tvTexto);

        // Lo mandamos a la pantalla
        contenedor.addView(globoMensaje);
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
                if (miRol.equals("Jefe")) {
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

    private void cargarHistorialChat() {
        // Verificamos qué valores tienen las variables antes de disparar
        android.util.Log.d("CHAT_DEBUG", "Cargando historial para Partida: " + idPartidaActual + " Equipo: " + miEquipo);

        new Thread(() -> {
            try {
                String urlStr = "http://10.0.2.2:8080/app/partidas/" + idPartidaActual + "/chat";
                java.net.URL url = new java.net.URL(urlStr);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                com.example.secretpanda.data.TokenManager tm = new com.example.secretpanda.data.TokenManager(this);
                conn.setRequestProperty("Authorization", "Bearer " + tm.getToken());

                int responseCode = conn.getResponseCode();
                android.util.Log.d("CHAT_DEBUG", "Código respuesta servidor: " + responseCode);

                if (responseCode == 200) {
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) response.append(line);

                    android.util.Log.d("CHAT_DEBUG", "JSON recibido: " + response.toString());

                    org.json.JSONArray historialArray = new org.json.JSONArray(response.toString());

                    runOnUiThread(() -> {
                        if (contenedorMensajesActual != null) {
                            contenedorMensajesActual.removeAllViews();
                            for (int i = 0; i < historialArray.length(); i++) {
                                try {
                                    android.util.Log.d("CHAT_DEBUG", "Dibujando mensaje número: " + i);
                                    org.json.JSONObject msgJson = historialArray.getJSONObject(i);
                                    String miPropioId = "118253860678694957644"; // Usamos msgJson para leer los datos
                                    String idJugadorMensaje = msgJson.getString("id_jugador");
                                    String texto = msgJson.getString("mensaje");
                                    String autor = msgJson.optString("tag", "Jugador");

                                    boolean esMio = idJugadorMensaje.equals(miPropioId);

                                    if (esMio) {
                                        autor = "Yo";
                                    }
                                    agregarMensajeAlChat(contenedorMensajesActual, autor, texto, esMio);
                                } catch (Exception e) { e.printStackTrace(); }
                            }
                        } else {
                            android.util.Log.e("CHAT_DEBUG", "¡ERROR! contenedorMensajesActual es NULL");
                        }
                        contenedorMensajesActual.requestLayout();
                        contenedorMensajesActual.invalidate();
                        android.util.Log.d("CHAT_DEBUG", "Refresco de vista solicitado");
                    });
                }
            } catch (Exception e) {
                android.util.Log.e("CHAT_DEBUG", "Error fatal: " + e.getMessage());
            }
        }).start();
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
                                    miRol = "Agente de Campo";
                                } else if (miRol.equals("jefe")) {
                                    miRol = "Jefe de Espionaje";
                                }
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
}
