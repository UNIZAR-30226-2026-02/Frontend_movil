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


    private StompClient stompClient;
    private int idPartidaActual; // <--- Sin valor fijo
    private String miEquipo;     // <--- Sin valor fijo
    private LinearLayout contenedorMensajesActual = null;

    // ¡NUEVA VARIABLE MAGICA!
    // Aquí guardaremos la carta que está verde en cada momento
    private FrameLayout cartaActualmenteSeleccionada = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partida);

        // --- RECOGER DATOS DE LA PANTALLA ANTERIOR ---
        idPartidaActual = getIntent().getIntExtra("ID_PARTIDA", -1);
        miEquipo = getIntent().getStringExtra("MI_EQUIPO");

        // Por seguridad, si por algún motivo no llega el equipo, le ponemos uno por defecto
        if (miEquipo == null) {
            miEquipo = "rojo";
        }

        // Si el ID es -1, significa que hubo un error al pasar los datos
        if (idPartidaActual == -1) {
            Toast.makeText(this, "Error: No se encontró la partida", Toast.LENGTH_SHORT).show();
            finish(); // Cerramos la pantalla porque está rota
            return;
        }

        // 1. Conectar al WebSocket de Spring Boot (Asegúrate de que la URL /ws o /stomp es la tuya del backend)
        com.example.secretpanda.data.TokenManager tokenManager = new com.example.secretpanda.data.TokenManager(this);
        String token = tokenManager.getToken();

        java.util.List<ua.naiksoftware.stomp.dto.StompHeader> cabeceras = new java.util.ArrayList<>();
        if (token != null && !token.isEmpty()) {
            cabeceras.add(new ua.naiksoftware.stomp.dto.StompHeader("Authorization", "Bearer " + token));
        } else {
            android.util.Log.e("CHAT_DEBUG", "¡Ojo! El token es nulo o está vacío en Android.");
        }

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://10.0.2.2:8080/ws/websocket");
        stompClient.connect(cabeceras); // Pasamos las cabeceras aquí
        String topicDestino = "/topic/partidas/" + idPartidaActual + "/chat/" + miEquipo;

        // 2. Escuchar los mensajes que mandan mis compañeros de equipo
        stompClient.topic(topicDestino).subscribe(stompMessage -> {
            // Cuando llega un mensaje nuevo del servidor, lo pintamos en la pantalla
            runOnUiThread(() -> {
                // Aquí procesas el JSON que llega y usas tu método:
                // agregarMensajeAlChat(contenedorMensajes, "Compañero", textoDelMensaje, false);
            });
        });

        btnAbandonar = findViewById(R.id.btn_abandonar);
        btnAlerta = findViewById(R.id.btn_alerta);
        btnChat = findViewById(R.id.btn_chat);
        gridTablero = findViewById(R.id.grid_tablero);
        notificacionChat = findViewById(R.id.notificacion_chat);
        // Enlazamos las nuevas variables
        tvMiRol = findViewById(R.id.tv_mi_rol);
        iconoBtnAlerta = findViewById(R.id.icono_btn_alerta);
        mostrarDialogoResultadoVotacion("Carta más votada (1/3)", "La carta es : Buena.", "El turno continua");
        mostrarDialogoResultadoVotacion("Carta más votada (1/3)", "La carta es : Mala.", "El turno se pierde");
        mostrarDialogoResultadoVotacion("Carta más votada (1/3)", "La carta es : la Muerte.", "Perdistes");
        //mostrarDialogoFinPartida("Victoria", "Has encontrado al asesino", 10000, 20);
        configurarBotones();
        configurarTablero();
        // Ejecutamos la magia de los roles
        aplicarRol();
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
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        // Buscamos los botones dentro del XML del diálogo
        View btnCerrar = dialog.findViewById(R.id.btn_cerrar_abandonar);
        View btnConfirmar = dialog.findViewById(R.id.btn_confirmar_abandonar);

        // Si pulsa la 'X', simplemente cerramos el diálogo y vuelve a la partida
        btnCerrar.setOnClickListener(v -> dialog.dismiss());

        // Si pulsa el botón rojo de "Abandonar"...
        btnConfirmar.setOnClickListener(v -> {
            dialog.dismiss(); // Cerramos el diálogo por limpieza
            finish();         // Y ejecutamos 'finish()' para salir de la actividad de la partida
        });

        dialog.show();
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

                // Aquí en el futuro enviarás esto al servidor. Por ahora mostramos un mensajito:
                android.widget.Toast.makeText(this, "Pista enviada: " + palabraEscrita + " (" + numeroEscrito + ")", android.widget.Toast.LENGTH_SHORT).show();

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

                agregarMensajeAlChat(this.contenedorMensajesActual, "Yo", textoEscrito, true);

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

        for (int i = 0; i < totalCartas; i++) {

            // 1. CREAR EL CONTENEDOR
            FrameLayout cartaContenedor = new FrameLayout(this);
            cartaContenedor.setClipToPadding(false);

            GridLayout.LayoutParams gridParams = new GridLayout.LayoutParams();
            gridParams.width = 0;
            gridParams.height = dpToPx(75);
            gridParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            gridParams.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
            cartaContenedor.setLayoutParams(gridParams);

            // 2. CREAR EL FONDO
            View cartaFondo = new View(this);
            FrameLayout.LayoutParams fondoParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            cartaFondo.setLayoutParams(fondoParams);
            cartaFondo.setBackgroundResource(R.drawable.fondo_blanco_redondeado);
            cartaFondo.setElevation(dpToPx(2));

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
            cartaContenedor.addView(etiquetaFraccion);

            // 5. DARLE EL CLIC A LA CARTA (LÓGICA ACTUALIZADA)
            cartaContenedor.setTag(false);


            cartaContenedor.setOnClickListener(v -> {
                boolean estaSeleccionada = (boolean) cartaContenedor.getTag();
                // ¡NUEVO! Comprobamos si es el jefe
                if (miRol.equals("Jefe")) {
                    Toast.makeText(this, "Como Jefe, no puedes seleccionar cartas.", Toast.LENGTH_SHORT).show();
                    return; // "return" hace que el código se detenga aquí y no ejecute lo de abajo
                }
                if (!estaSeleccionada) {

                    // PASO A: ¿Había otra carta seleccionada antes? ¡Apágala!
                    if (cartaActualmenteSeleccionada != null) {
                        View fondoAnterior = cartaActualmenteSeleccionada.getChildAt(0);
                        TextView etiquetaAnterior = (TextView) cartaActualmenteSeleccionada.getChildAt(1);

                        fondoAnterior.setBackgroundResource(R.drawable.fondo_blanco_redondeado);
                        etiquetaAnterior.setVisibility(View.INVISIBLE);
                        cartaActualmenteSeleccionada.setTag(false);
                    }

                    // PASO B: Enciende la NUEVA carta que acabamos de tocar
                    cartaFondo.setBackgroundResource(R.drawable.fondo_carta_seleccionada);
                    etiquetaFraccion.setVisibility(View.VISIBLE);
                    cartaContenedor.setTag(true);

                    // PASO C: Guardamos esta nueva carta como la "Actualmente Seleccionada"
                    cartaActualmenteSeleccionada = cartaContenedor;

                } else {
                    // Si tocaste LA MISMA carta que ya estaba verde, la apagamos (Deseleccionar)
                    cartaFondo.setBackgroundResource(R.drawable.fondo_blanco_redondeado);
                    etiquetaFraccion.setVisibility(View.INVISIBLE);
                    cartaContenedor.setTag(false);

                    // Como ya no hay ninguna seleccionada, vaciamos la memoria
                    cartaActualmenteSeleccionada = null;
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
           vistaCarta.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#9B3838"))); // Rojo
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
                String urlStr = "http://10.0.2.2:8080/api/partidas/" + idPartidaActual + "/chat/" + miEquipo;
                java.net.URL url = new java.net.URL(urlStr);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();

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
                                    String miPropioId = "118253860678694957644"; // O sácalo de tu TokenManager

// Usamos msgJson para leer los datos
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

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}
