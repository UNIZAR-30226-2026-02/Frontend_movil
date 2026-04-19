package com.example.secretpanda.ui.game.match;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
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
import com.example.secretpanda.ui.EfectosManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;

public class PartidaActivity extends AppCompatActivity {

    private final String AGENTE_STRING = "Agente de Campo";
    private final String JEFE_STRING = "Jefe de Espionaje";

    private TextView btnAbandonar, tvMiRol, tvTimer, tvFasePartida, tvPuntosRojo, tvPuntosAzul, notificacionChat;
    private View btnAlerta, btnChat, circuloTurno, circuloMiEquipo;
    private android.widget.ImageView iconoBtnAlerta;
    private GridLayout gridTablero;

    private StompClient stompClient;
    private int idPartidaActual;
    private String miEquipo, miRol, miPropioIdGoogle;
    
    private String equipoTurnoActual = "";
    private String faseTurno = ""; 
    private boolean miVotoEnviado = false;
    private boolean hayPistaActiva = false;
    private String palabraPistaActual = "";
    private int numeroPistaActual = 0;

    private List<JSONObject> historialChat = new ArrayList<>();
    private LinearLayout contenedorMensajesActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partida);

        idPartidaActual = getIntent().getIntExtra("ID_PARTIDA", -1);
        miEquipo = getIntent().getStringExtra("MI_EQUIPO");
        
        // Obtenemos el ID real guardado durante el login
        miPropioIdGoogle = new com.example.secretpanda.data.TokenManager(this).getIdGoogle();
        
        if (idPartidaActual == -1) {
            Toast.makeText(this, "Error: Partida no encontrada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        inicializarVistas();
        configurarBotones();
        conectarWebSocket();
        
        obtenerMiRolDelServidor();
        obtenerEstadoCompletoDePartida();
    }

    private void inicializarVistas() {
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
        circuloMiEquipo = findViewById(R.id.circulo_mi_equipo);
    }

    private void conectarWebSocket() {
        if (stompClient != null && stompClient.isConnected()) return;
        String token = new com.example.secretpanda.data.TokenManager(this).getToken();
        List<StompHeader> headers = new ArrayList<>();
        if (token != null) headers.add(new StompHeader("Authorization", "Bearer " + token));

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://10.0.2.2:8080/ws/websocket");
        stompClient.lifecycle().subscribe(lifecycleEvent -> {
            if (lifecycleEvent.getType() == ua.naiksoftware.stomp.dto.LifecycleEvent.Type.OPENED) {
                runOnUiThread(() -> {
                    suscribirseAlEstadoPublico();
                    suscribirseAlEstadoPrivado();
                    suscribirseAlChat();
                    suscribirseAlTemporizador();
                });
            }
        });
        stompClient.connect(headers);
    }

    private void suscribirseAlEstadoPublico() {
        stompClient.topic("/topic/partidas/" + idPartidaActual + "/estado").subscribe(msg -> {
            String payload = msg.getPayload();
            if ("FINALIZADA".equalsIgnoreCase(payload)) {
                runOnUiThread(this::navegarAFinPartida);
                return;
            }
            try {
                JSONObject json = new JSONObject(payload);
                runOnUiThread(() -> aplicarEstadoTotal(json));
            } catch (Exception e) { Log.e("WS", "Error estado publico", e); }
        });
    }

    private void suscribirseAlEstadoPrivado() {
        stompClient.topic("/user/queue/partidas/" + idPartidaActual + "/estado").subscribe(msg -> {
            String payload = msg.getPayload();
            if ("FINALIZADA".equalsIgnoreCase(payload)) {
                runOnUiThread(this::navegarAFinPartida);
                return;
            }
            try {
                JSONObject json = new JSONObject(payload);
                runOnUiThread(() -> aplicarEstadoTotal(json));
            } catch (Exception e) { Log.e("WS", "Error estado privado", e); }
        });
    }

    private void navegarAFinPartida() {
        Intent intent = new Intent(this, com.example.secretpanda.ui.game.endMatch.FinPartidaActivity.class);
        intent.putExtra("ID_PARTIDA", idPartidaActual);
        startActivity(intent);
        finish();
    }

    private void aplicarEstadoTotal(JSONObject json) {
        try {
            String estadoPartida = json.optString("estado", "");
            if ("finalizada".equalsIgnoreCase(estadoPartida)) {
                navegarAFinPartida();
                return;
            }

            equipoTurnoActual = json.optString("equipo_turno_actual", equipoTurnoActual);
            String miEquipoReal = json.optString("mi_equipo", miEquipo);
            if (!miEquipoReal.isEmpty()) miEquipo = miEquipoReal;

            faseTurno = json.optString("fase_turno", "JEFE_PISTA");

            tvPuntosRojo.setText(String.valueOf(json.optInt("puntos_rojo", 0)));
            tvPuntosAzul.setText(String.valueOf(json.optInt("puntos_azul", 0)));

            if (json.has("pista_actual") && !json.isNull("pista_actual")) {
                JSONObject pista = json.getJSONObject("pista_actual");
                palabraPistaActual = pista.optString("palabra_pista", "");
                numeroPistaActual = pista.optInt("pista_numero", 0);
                hayPistaActiva = !palabraPistaActual.isEmpty();
            } else {
                hayPistaActiva = false;
                palabraPistaActual = "";
                numeroPistaActual = 0;
            }

            JSONArray votos = json.optJSONArray("votos_turno_actual");
            if (votos == null || votos.length() == 0) {
                miVotoEnviado = false;
            } else {
                miVotoEnviado = false;
                for (int i = 0; i < votos.length(); i++) {
                    String idVotante = votos.getJSONObject(i).optString("id_google", "");
                    if (miPropioIdGoogle.equals(idVotante)) miVotoEnviado = true;
                }
            }

            actualizarInterfazGlobal();
            pintarTablero(json);

        } catch (Exception e) { Log.e("UI", "Error aplicando estado", e); }
    }

    private void actualizarInterfazGlobal() {
        if (circuloMiEquipo != null) {
            circuloMiEquipo.setBackgroundColor(miEquipo.equalsIgnoreCase("ROJO") ? Color.RED : Color.BLUE);
        }
        if (circuloTurno != null) {
            circuloTurno.setBackgroundColor(equipoTurnoActual.equalsIgnoreCase("ROJO") ? Color.RED : Color.BLUE);
        }
        if (hayPistaActiva) {
            tvFasePartida.setText("PISTA: " + palabraPistaActual.toUpperCase() + " (" + numeroPistaActual + ")");
        } else {
            String msg = equipoTurnoActual.equalsIgnoreCase(miEquipo) ? "TU TURNO: DA UNA PISTA" : "ESPERANDO AL JEFE " + equipoTurnoActual.toUpperCase();
            tvFasePartida.setText(msg);
        }
        tvMiRol.setText("ROL: " + miRol);
        
        boolean puedoDarPista = JEFE_STRING.equalsIgnoreCase(miRol) && miEquipo.equalsIgnoreCase(equipoTurnoActual) && !hayPistaActiva;
        iconoBtnAlerta.setImageResource(puedoDarPista ? R.drawable.ic_anadir_pista : android.R.drawable.ic_menu_view);
    }

    private void pintarTablero(JSONObject estado) {
        try {
            JSONObject tableroObj = estado.optJSONObject("tablero");
            if (tableroObj == null) return;
            JSONArray cartasArray = tableroObj.getJSONArray("cartas");
            JSONArray votos = estado.optJSONArray("votos_turno_actual");
            if (votos == null) votos = new JSONArray();

            List<JSONObject> listaCartas = new ArrayList<>();
            for (int i = 0; i < cartasArray.length(); i++) listaCartas.add(cartasArray.getJSONObject(i));
            
            // Estabilidad del tablero: Ordenar siempre por ID
            Collections.sort(listaCartas, (a, b) -> Integer.compare(a.optInt("id_carta_tablero"), b.optInt("id_carta_tablero")));

            gridTablero.removeAllViews();
            gridTablero.setColumnCount(4);

            for (JSONObject cartaJson : listaCartas) {
                int idCarta = cartaJson.optInt("id_carta_tablero");
                String palabra = cartaJson.optString("palabra", "");
                String est = cartaJson.optString("estado", "oculta");
                String tipo = cartaJson.optString("tipo", "");
                boolean revelada = !"oculta".equalsIgnoreCase(est);

                int numVotos = 0;
                boolean yoVotoAqui = false;
                for (int v = 0; v < votos.length(); v++) {
                    JSONObject voto = votos.getJSONObject(v);
                    if (voto.optInt("id_carta_tablero") == idCarta) {
                        numVotos++;
                        if (miPropioIdGoogle.equals(voto.optString("id_google"))) yoVotoAqui = true;
                    }
                }

                FrameLayout contenedor = new FrameLayout(this);
                GridLayout.LayoutParams p = new GridLayout.LayoutParams();
                p.width = 0; p.height = dpToPx(85);
                p.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                p.setMargins(dpToPx(3), dpToPx(3), dpToPx(3), dpToPx(3));
                contenedor.setLayoutParams(p);

                if (!revelada && numVotos > 0) {
                    contenedor.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
                    contenedor.setBackgroundColor(yoVotoAqui ? Color.parseColor("#4CAF50") : Color.parseColor("#FFC107"));
                } else {
                    contenedor.setPadding(dpToPx(1), dpToPx(1), dpToPx(1), dpToPx(1));
                    contenedor.setBackgroundColor(Color.DKGRAY);
                }

                FrameLayout fondo = new FrameLayout(this);
                fondo.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
                if (revelada) {
                    fondo.setBackgroundColor(obtenerColorTipo(tipo));
                } else {
                    fondo.setBackgroundColor(Color.parseColor("#2C3E50"));
                    if (JEFE_STRING.equalsIgnoreCase(miRol)) {
                        View line = new View(this);
                        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-1, dpToPx(6));
                        lp.gravity = Gravity.BOTTOM;
                        line.setLayoutParams(lp);
                        line.setBackgroundColor(obtenerColorTipo(tipo));
                        fondo.addView(line);
                    }
                }

                TextView tv = new TextView(this);
                tv.setText(palabra.toUpperCase());
                tv.setTextColor(Color.WHITE);
                tv.setGravity(Gravity.CENTER);
                tv.setTypeface(null, Typeface.BOLD);
                tv.setTextSize(12);
                fondo.addView(tv);

                contenedor.addView(fondo);
                contenedor.setOnClickListener(v -> manejarClickCarta(idCarta, palabra, revelada));
                gridTablero.addView(contenedor);
            }
        } catch (Exception e) { Log.e("TABLERO", "Error", e); }
    }

    private void manejarClickCarta(int idCarta, String palabra, boolean revelada) {
        if (revelada) return;
        boolean esMiTurno = miEquipo.equalsIgnoreCase(equipoTurnoActual);
        boolean soyAgente = AGENTE_STRING.equalsIgnoreCase(miRol);
        if (soyAgente && esMiTurno && hayPistaActiva && !miVotoEnviado) {
            enviarVoto(idCarta);
        } else {
            mostrarPreviewCarta(palabra);
        }
    }

    private void enviarVoto(int idCarta) {
        try {
            JSONObject json = new JSONObject();
            json.put("id_carta_tablero", idCarta);
            stompClient.send("/app/partidas/" + idPartidaActual + "/votar", json.toString()).subscribe();
        } catch (Exception e) { }
    }

    private void configurarBotones() {
        btnAbandonar.setOnClickListener(v -> mostrarDialogoAbandonar());
        btnChat.setOnClickListener(v -> {
            notificacionChat.setVisibility(View.GONE);
            mostrarDialogoChat();
        });
        btnAlerta.setOnClickListener(v -> {
            if (JEFE_STRING.equalsIgnoreCase(miRol) && miEquipo.equalsIgnoreCase(equipoTurnoActual) && !hayPistaActiva) {
                mostrarDialogoAnadirPista();
            } else {
                mostrarDialogoVerPista();
            }
        });
    }

    private void mostrarDialogoAnadirPista() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_anadir_pista);
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0));
        android.widget.EditText inputP = dialog.findViewById(R.id.input_palabra_pista);
        android.widget.Spinner spinnerN = dialog.findViewById(R.id.spinner_numero_pista);
        List<Integer> nums = new ArrayList<>();
        for (int i = 1; i <= 9; i++) nums.add(i);
        android.widget.ArrayAdapter<Integer> adapter = new android.widget.ArrayAdapter<>(this, R.layout.spinner_item_pista, nums);
        adapter.setDropDownViewResource(R.layout.spinner_item_pista);
        spinnerN.setAdapter(adapter);
        dialog.findViewById(R.id.btn_enviar_pista).setOnClickListener(v -> {
            String p = inputP.getText().toString().trim();
            int n = (int) spinnerN.getSelectedItem();
            if (!p.isEmpty()) {
                enviarPistaServidor(p, n);
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.btn_cerrar_pista).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void enviarPistaServidor(String palabra, int numero) {
        try {
            JSONObject json = new JSONObject();
            json.put("palabra_pista", palabra);
            json.put("pista_numero", numero);
            stompClient.send("/app/partidas/" + idPartidaActual + "/pista", json.toString()).subscribe();
        } catch (Exception e) { }
    }

    private void mostrarDialogoVerPista() {
        if (!hayPistaActiva) {
            Toast.makeText(this, "Aún no hay una pista activa", Toast.LENGTH_SHORT).show();
            return;
        }
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_pista_jefe);
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0));
        
        TextView tvPalabra = dialog.findViewById(R.id.input_palabra_jefe);
        TextView tvNumero = dialog.findViewById(R.id.input_numero_jefe);
        
        if (tvPalabra != null) tvPalabra.setText(palabraPistaActual.toUpperCase());
        if (tvNumero != null) tvNumero.setText(String.valueOf(numeroPistaActual));
        
        dialog.findViewById(R.id.btn_cerrar_pista).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void obtenerMiRolDelServidor() {
        OkHttpClient client = new OkHttpClient();
        String token = new com.example.secretpanda.data.TokenManager(this).getToken();
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8080/api/partidas/" + idPartidaActual + "/participantes/rol")
                .addHeader("Authorization", "Bearer " + token).build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { }
            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        String r = json.optString("rol", "agente");
                        miRol = r.equalsIgnoreCase("lider") ? JEFE_STRING : AGENTE_STRING;
                        miEquipo = json.optString("equipo", miEquipo);
                        runOnUiThread(() -> actualizarInterfazGlobal());
                    } catch (Exception e) { }
                }
            }
        });
    }

    private void obtenerEstadoCompletoDePartida() {
        OkHttpClient client = new OkHttpClient();
        String token = new com.example.secretpanda.data.TokenManager(this).getToken();
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8080/api/partidas/" + idPartidaActual + "/estado")
                .addHeader("Authorization", "Bearer " + token).build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { }
            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        runOnUiThread(() -> aplicarEstadoTotal(json));
                    } catch (Exception e) { }
                }
            }
        });
    }

    private void suscribirseAlTemporizador() {
        stompClient.topic("/topic/partidas/" + idPartidaActual + "/temporizador").subscribe(msg -> {
            try {
                String p = msg.getPayload();
                int s = p.contains("{") ? new JSONObject(p).optInt("segundos_restantes", 0) : Integer.parseInt(p.replaceAll("[^0-9]", ""));
                runOnUiThread(() -> tvTimer.setText(String.format("%02d:%02d", s/60, s%60)));
            } catch (Exception e) { }
        });
    }

    private void suscribirseAlChat() {
        stompClient.topic("/topic/partidas/" + idPartidaActual + "/chat/" + miEquipo.toLowerCase()).subscribe(msg -> {
            try {
                JSONObject json = new JSONObject(msg.getPayload());
                String idEnviado = json.optString("id_google", ""); 
                if (idEnviado.isEmpty()) idEnviado = json.optString("id_jugador", "");
                
                final String finalId = idEnviado;
                runOnUiThread(() -> {
                    historialChat.add(json);
                    if (contenedorMensajesActual != null) {
                        boolean esMio = miPropioIdGoogle != null && miPropioIdGoogle.equals(finalId);
                        boolean esValido = json.optBoolean("es_valido", true);
                        agregarMensajeAlChat(contenedorMensajesActual, json.optString("tag"), json.optString("mensaje"), esMio, esValido);
                    }
                });
            } catch (Exception e) { 
                Log.e("CHAT", "Error procesando mensaje", e); 
            }
        });
    }

    private void agregarMensajeAlChat(LinearLayout c, String r, String t, boolean esMio, boolean esValido) {
        if (c == null) return;
        View v = getLayoutInflater().inflate(R.layout.item_mensaje_chat, c, false);
        
        TextView tvRemitente = v.findViewById(R.id.tv_remitente);
        TextView tvMensaje = v.findViewById(R.id.tv_texto_mensaje);
        View globo = v.findViewById(R.id.globo_mensaje);
        
        tvRemitente.setText(esMio ? "YO" : r.toUpperCase());
        
        if (!esValido) {
            tvMensaje.setText("[Mensaje bloqueado por lenguaje inapropiado]");
            tvMensaje.setTextColor(Color.GRAY);
            tvMensaje.setTypeface(null, Typeface.ITALIC);
        } else {
            tvMensaje.setText(t);
            tvMensaje.setTypeface(null, Typeface.NORMAL);
        }
        
        // Alineación y color según el emisor
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) globo.getLayoutParams();
        if (esMio) {
            lp.gravity = Gravity.END;
            lp.setMargins(dpToPx(40), 0, 0, dpToPx(12));
            globo.setBackgroundResource(R.drawable.fondo_input_codigo); // Fondo claro (Manila)
            tvRemitente.setTextColor(getResources().getColor(R.color.agent_green));
            if (esValido) tvMensaje.setTextColor(Color.BLACK);
        } else {
            lp.gravity = Gravity.START;
            lp.setMargins(0, 0, dpToPx(40), dpToPx(12));
            globo.setBackgroundResource(R.drawable.fondo_boton_mision); // Fondo oscuro (Tinta)
            tvRemitente.setTextColor(getResources().getColor(R.color.agent_blue));
            if (esValido) tvMensaje.setTextColor(Color.WHITE); // Texto claro sobre fondo oscuro
        }
        globo.setLayoutParams(lp);
        
        c.addView(v);
        
        // Auto-scroll al final
        View parent = (View) c.getParent();
        if (parent instanceof android.widget.ScrollView) {
            parent.post(() -> ((android.widget.ScrollView)parent).fullScroll(View.FOCUS_DOWN));
        }
    }

    private void mostrarDialogoChat() {
        Dialog d = new Dialog(this);
        d.setContentView(R.layout.dialog_chat);
        d.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0));
        d.getWindow().setLayout(-1, -1);
        contenedorMensajesActual = d.findViewById(R.id.contenedor_mensajes);
        android.widget.EditText in = d.findViewById(R.id.input_mensaje);
        if (JEFE_STRING.equalsIgnoreCase(miRol)) d.findViewById(R.id.zona_escribir).setVisibility(View.GONE);
        for (JSONObject m : historialChat) {
            boolean esMio = miPropioIdGoogle != null && miPropioIdGoogle.equals(m.optString("id_jugador"));
            boolean esValido = m.optBoolean("es_valido", true);
            agregarMensajeAlChat(contenedorMensajesActual, m.optString("tag"), m.optString("mensaje"), esMio, esValido);
        }
        d.findViewById(R.id.btn_enviar_mensaje).setOnClickListener(v -> {
            String txt = in.getText().toString().trim();
            if (!txt.isEmpty()) {
                enviarChatServidor(txt);
                in.setText("");
            }
        });
        d.findViewById(R.id.btn_cerrar_chat).setOnClickListener(v -> d.dismiss());
        d.show();
    }

    private void enviarChatServidor(String m) {
        try {
            JSONObject j = new JSONObject();
            j.put("mensaje", m);
            stompClient.send("/app/partidas/" + idPartidaActual + "/chat", j.toString()).subscribe();
            // Eliminamos agregarMensajeAlChat de aquí para evitar duplicados, 
            // el WebSocket se encargará de recibirlo y pintarlo.
        } catch (Exception e) { }
    }

    private void mostrarDialogoAbandonar() {
        Dialog d = new Dialog(this);
        d.setContentView(R.layout.dialog_abandonar);
        d.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0));
        d.findViewById(R.id.btn_confirmar_abandonar).setOnClickListener(v -> {
            d.dismiss();
            abandonarPartidaBackend();
        });
        d.findViewById(R.id.btn_cerrar_abandonar).setOnClickListener(v -> {
            EfectosManager.reproducir(getApplicationContext(), R.raw.sonido_cancelar);
            d.dismiss();
        });
        d.show();
    }

    private void abandonarPartidaBackend() {
        OkHttpClient client = new OkHttpClient();
        String jwt = new com.example.secretpanda.data.TokenManager(this).getToken();
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8080/api/partidas/" + idPartidaActual + "/participantes")
                .delete().addHeader("Authorization", "Bearer " + jwt).build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { finish(); }
            @Override public void onResponse(Call call, Response response) { finish(); }
        });
    }

    private void mostrarPreviewCarta(String p) {
        Dialog d = new Dialog(this);
        d.setContentView(R.layout.dialog_preview_carta);
        d.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0));
        ((TextView)d.findViewById(R.id.tv_palabra_preview)).setText(p);
        d.findViewById(R.id.btn_cerrar_preview).setOnClickListener(v -> d.dismiss());
        d.show();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private int obtenerColorTipo(String tipo) {
        switch (tipo.toLowerCase()) {
            case "rojo": return Color.parseColor("#C0392B");
            case "azul": return Color.parseColor("#2980B9");
            case "asesino": return Color.BLACK;
            default: return Color.parseColor("#95A5A6");
        }
    }
}
