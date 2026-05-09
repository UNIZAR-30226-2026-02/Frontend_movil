package com.example.secretpanda.ui.game.match;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.secretpanda.R;
import com.example.secretpanda.data.NetworkConfig;
import com.example.secretpanda.ui.audio.EfectosManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
    private com.example.secretpanda.data.CustomizationManager customizationManager;

    private StompClient stompClient;
    private int idPartidaActual;
    private int cartas_rojas_restantes, cartas_azules_restantes;
    private int max_Jugadores;
    private int jugadores_Rojo, jugadores_Azul;

    private static final int NUM_CARTAS = 8;
    private String miEquipo = "", miRol = "", miPropioIdGoogle = "", miTag = "";
    
    private String equipoTurnoActual = "";
    private String equipoInicioPartida = "";

    private String faseTurno = ""; 
    private boolean miVotoEnviado = false;
    private boolean hayPistaActiva = false;
    private String palabraPistaActual = "";
    private int numeroPistaActual = 0;
    private int idCartaVotada;
    private int cartas_rojas_restantes_voto;
    private int cartas_azules_restantes_voto;


    private List<JSONObject> historialChat = new ArrayList<>();
    private LinearLayout contenedorMensajesActual;
    private List<Integer> ordenInicialCartas = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partida);

        idPartidaActual = getIntent().getIntExtra("ID_PARTIDA", -1);
        max_Jugadores = getIntent().getIntExtra("JUGADORES_TOTAL", 0);
        jugadores_Rojo = getIntent().getIntExtra("JUGADORES_ROJO", 0);
        jugadores_Azul = getIntent().getIntExtra("JUGADORES_AZUL", 0);

        String equipoExtra = getIntent().getStringExtra("MI_EQUIPO");
        if (equipoExtra != null) miEquipo = equipoExtra;

        String tagExtra = getIntent().getStringExtra("MI_NOMBRE_USUARIO");
        if (tagExtra != null) miTag = tagExtra;
        
        // Obtenemos el ID real guardado durante el login
        miPropioIdGoogle = new com.example.secretpanda.data.TokenManager(this).getIdGoogle();
        
        customizationManager = new com.example.secretpanda.data.CustomizationManager(this);
        cargarPersonalizaciones();

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

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, NetworkConfig.WS_URL);
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
                Log.d("WS", "Estado público: " + json.toString());
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
                Log.d("WS", "Estado público: " + json.toString());
                runOnUiThread(() -> aplicarEstadoTotal(json));
            } catch (Exception e) { Log.e("WS", "Error estado privado", e); }
        });
    }

    private void navegarAFinPartida() {
        Intent intent = new Intent(this, com.example.secretpanda.ui.game.endMatch.FinPartidaActivity.class);
        intent.putExtra("ID_PARTIDA", idPartidaActual);
        intent.putExtra("MI_NOMBRE_USUARIO", miTag);
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

            equipoTurnoActual = json.optString("equipo_turno_actual", "");
            cartas_rojas_restantes = json.optInt("cartas_rojas_restantes", 0);
            cartas_azules_restantes = json.optInt("cartas_azules_restantes", 0);


            if(equipoInicioPartida.equals("azul")){
                tvPuntosAzul.setText(String.valueOf(NUM_CARTAS + 1 - cartas_azules_restantes));
                tvPuntosRojo.setText(String.valueOf(NUM_CARTAS - cartas_rojas_restantes));
            }else{
                tvPuntosAzul.setText(String.valueOf(NUM_CARTAS - cartas_azules_restantes));
                tvPuntosRojo.setText(String.valueOf(NUM_CARTAS + 1 - cartas_rojas_restantes));
            }
            // Solo actualizamos miEquipo si el servidor nos lo envía explícitamente
            if (json.has("mi_equipo") && !json.isNull("mi_equipo")) {
                String nuevoEquipo = json.optString("mi_equipo");
                if (!nuevoEquipo.isEmpty()) {
                    miEquipo = nuevoEquipo;
                }
            }

            faseTurno = json.optString("fase_turno", "JEFE_PISTA");


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
                    String tagVotante = votos.getJSONObject(i).optString("tag", "");
                    if (miTag != null && miTag.equalsIgnoreCase(tagVotante)) miVotoEnviado = true;
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
        if (hayPistaActiva || "votando".equalsIgnoreCase(faseTurno)) {
            tvFasePartida.setText("PISTA: " + palabraPistaActual.toUpperCase() + " (" + numeroPistaActual + ")");
        } else {
            String msg;
            if (equipoTurnoActual.equalsIgnoreCase(miEquipo)) {
                if (JEFE_STRING.equalsIgnoreCase(miRol)) {
                    msg = "TU TURNO: DA UNA PISTA";
                } else {
                    msg = "ESPERANDO PISTA DEL JEFE";
                }
            } else {
                msg = "ESPERANDO AL JEFE " + equipoTurnoActual.toUpperCase();
            }
            tvFasePartida.setText(msg);
        }
        tvMiRol.setText("ROL: " + miRol);

        boolean puedoDarPista = JEFE_STRING.equalsIgnoreCase(miRol) && miEquipo.equalsIgnoreCase(equipoTurnoActual) && !"votando".equalsIgnoreCase(faseTurno);
        iconoBtnAlerta.setImageResource(puedoDarPista ? R.drawable.ic_anadir_pista : android.R.drawable.ic_menu_view);
    }

    private void pintarTablero(JSONObject estado) {
        try {
            JSONObject tableroObj = estado.optJSONObject("tablero");
            if (tableroObj == null) return;
            JSONArray cartasArray = tableroObj.getJSONArray("cartas");
            JSONArray votos = estado.optJSONArray("votos_turno_actual");
            if (votos == null) votos = new JSONArray();

            if (ordenInicialCartas == null && cartasArray.length() > 0) {
                ordenInicialCartas = new ArrayList<>();
                for (int i = 0; i < cartasArray.length(); i++) {
                    ordenInicialCartas.add(cartasArray.getJSONObject(i).optInt("id_carta_tablero"));
                }
            }

            // Reordenar las cartas recibidas basándonos estrictamente en el orden inicial
            List<JSONObject> listaCartas = new ArrayList<>();
            if (ordenInicialCartas != null) {
                for (Integer id : ordenInicialCartas) {
                    for (int i = 0; i < cartasArray.length(); i++) {
                        JSONObject carta = cartasArray.getJSONObject(i);
                        if (carta.optInt("id_carta_tablero") == id) {
                            listaCartas.add(carta);
                            break;
                        }
                    }
                }
            } else {
                // Por si falla algo, las metemos tal cual
                for (int i = 0; i < cartasArray.length(); i++) {
                    listaCartas.add(cartasArray.getJSONObject(i));
                }
            }

            gridTablero.removeAllViews();
            gridTablero.setColumnCount(5);

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
                        String tagVotante = voto.optString("tag", "");
                        if (miTag != null && miTag.equals(tagVotante)) yoVotoAqui = true;
                    }
                }

                FrameLayout contenedor = new FrameLayout(this);
                GridLayout.LayoutParams p = new GridLayout.LayoutParams();
                p.width = 0; p.height = dpToPx(85);
                p.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                p.setMargins(dpToPx(3), dpToPx(3), dpToPx(3), dpToPx(3));
                contenedor.setLayoutParams(p);

                int marcoColor = customizationManager.getBorderColor();
                if (!revelada && numVotos > 0) {
                    contenedor.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
                    contenedor.setBackgroundColor(yoVotoAqui ? Color.parseColor("#4CAF50") : Color.parseColor("#FFC107"));
                } else {
                    contenedor.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
                    contenedor.setBackgroundColor(marcoColor);
                }

                FrameLayout fondo = new FrameLayout(this);
                fondo.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));

                // 1. CAPA DE FONDO (Color base o Imagen)
                boolean esImagen = palabra.startsWith("http") || palabra.contains("/") || palabra.endsWith(".png") || palabra.endsWith(".jpg");
                
                if (revelada) {
                    if (esImagen) {
                        if(idCarta == idCartaVotada){
                            if(miEquipo.equals("azul") && cartas_azules_restantes < cartas_azules_restantes_voto){
                                mostrarPopupResultadoCarta(true, "aliado");
                            }else if(miEquipo.equals("azul") &&
                                    cartas_azules_restantes_voto == cartas_azules_restantes &&
                                    cartas_rojas_restantes_voto == cartas_rojas_restantes){
                                mostrarPopupResultadoCarta(true, "civil");
                            }else if(miEquipo.equals("azul")){
                                mostrarPopupResultadoCarta(false, "");
                            }
                            if(miEquipo.equals("rojo") && cartas_rojas_restantes < cartas_rojas_restantes_voto){
                                mostrarPopupResultadoCarta(true, "aliado");
                            }else if(miEquipo.equals("rojo") &&
                                    cartas_azules_restantes_voto == cartas_azules_restantes &&
                                    cartas_rojas_restantes_voto == cartas_rojas_restantes){
                                mostrarPopupResultadoCarta(true, "civil");
                            }else if(miEquipo.equals("rojo")){
                                mostrarPopupResultadoCarta(false, "");
                            }
                        }
                        // Si es imagen revelada, ponemos la imagen y luego un filtro encima
                        android.widget.ImageView iv = new android.widget.ImageView(this);
                        iv.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
                        iv.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                        Glide.with(this)
                            .load(palabra)
                            .transform(new CenterCrop(), new RoundedCorners(dpToPx(4)))
                            .placeholder(android.R.color.darker_gray)
                            .error(android.R.color.darker_gray)
                            .into(iv);
                        fondo.addView(iv);

                        // Filtro semitransparente (60% de opacidad del color del equipo)
                        View filtro = new View(this);
                        filtro.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
                        int colorTipo = obtenerColorTipo(tipo);
                        int colorConAlpha = Color.argb(150, Color.red(colorTipo), Color.green(colorTipo), Color.blue(colorTipo));
                        filtro.setBackgroundColor(colorConAlpha);
                        fondo.addView(filtro);
                    } else {
                        fondo.setBackgroundColor(obtenerColorTipo(tipo));
                    }
                } else {
                    fondo.setBackgroundColor(Color.parseColor("#2C3E50"));
                    
                    if (esImagen) {
                        if(idCarta == idCartaVotada){
                            mostrarPopupResultadoCarta(false, String.valueOf(idCarta));
                        }
                        android.widget.ImageView iv = new android.widget.ImageView(this);
                        iv.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
                        iv.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                        Glide.with(this)
                            .load(palabra)
                            .transform(new CenterCrop(), new RoundedCorners(dpToPx(4)))
                            .placeholder(android.R.color.darker_gray)
                            .error(android.R.color.darker_gray)
                            .into(iv);
                        fondo.addView(iv);
                    }
                }

                // 2. CAPA DE INFORMACIÓN PARA EL JEFE (Línea de color si no está revelada)
                if (!revelada && JEFE_STRING.equalsIgnoreCase(miRol)) {
                    View line = new View(this);
                    FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-1, dpToPx(8));
                    lp.gravity = Gravity.BOTTOM;
                    line.setLayoutParams(lp);
                    line.setBackgroundColor(obtenerColorTipo(tipo));
                    fondo.addView(line);
                }

                // 3. CAPA DE TEXTO (Solo si NO es imagen o si es Jefe para identificar)
                if (!esImagen) {
                    TextView tv = new TextView(this);
                    tv.setText(palabra.toUpperCase());
                    tv.setTextColor(Color.WHITE);
                    tv.setGravity(Gravity.CENTER);
                    tv.setTypeface(null, Typeface.BOLD);
                    tv.setTextSize(12);
                    fondo.addView(tv);
                }

                contenedor.addView(fondo);
                contenedor.setOnClickListener(v -> manejarClickCarta(idCarta, palabra, revelada));
                gridTablero.addView(contenedor);
            }
        } catch (Exception e) { Log.e("TABLERO", "Error", e); }
    }
    private void mostrarPopupResultadoCarta(boolean correcta, String rol) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_resultado_revelacion);

        // Hacer el fondo del diálogo original transparente para que se vea nuestro diseño manila
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView txtTitulo = dialog.findViewById(R.id.txt_titulo_revelacion);
        TextView txtDetalle = dialog.findViewById(R.id.txt_detalle_carta);
        ImageView imgIcon = dialog.findViewById(R.id.img_resultado_icon);
        TextView btnCerrar = dialog.findViewById(R.id.btn_cerrar_reporte);

        if (correcta) {
            txtTitulo.setText("OPERACIÓN REALIZADA");
            txtTitulo.setTextColor(getResources().getColor(R.color.agent_green));
            imgIcon.setImageResource(R.drawable.ic_check_tick);
            if(rol.equals("aliado")){
                txtDetalle.setText("CARTA: AGENTE ALIADO");
            }else{
                txtDetalle.setText("CARTA: AGENTE CIVIL");
            }
            imgIcon.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.agent_green)));
            EfectosManager.reproducir(this, R.raw.sonido_aceptar);
        } else {
            txtTitulo.setText("FALLO DE INTELIGENCIA");
            txtTitulo.setTextColor(getResources().getColor(R.color.ink_muted));
            txtDetalle.setText("CARTA: AGENTE ENEMIGO");
            imgIcon.setImageResource(R.drawable.ic_cerrar_x);
            imgIcon.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.fbi_red)));
            EfectosManager.reproducir(this, R.raw.sonido_fiasco);
        }

        btnCerrar.setOnClickListener(v -> {
            EfectosManager.reproducir(this, R.raw.sonido_click);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void manejarClickCarta(int idCarta, String palabra, boolean revelada) {
        boolean puedeVotarEnEsteMomento = false;

        if (!revelada) {
            // NORMALIZACIÓN DE STRINGS para evitar errores de mayúsculas o espacios
            String equipoNormalizado = (miEquipo != null) ? miEquipo.trim().toLowerCase() : "";
            String turnoNormalizado = (equipoTurnoActual != null) ? equipoTurnoActual.trim().toLowerCase() : "";
            String faseNormalizada = (faseTurno != null) ? faseTurno.trim().toLowerCase() : "";
            
            boolean esMiTurno = equipoNormalizado.equals(turnoNormalizado);
            boolean soyJefe = JEFE_STRING.equalsIgnoreCase(miRol) || "lider".equalsIgnoreCase(miRol);
            boolean soyAgente = !soyJefe; // Por defecto, si no eres jefe, eres agente
            
            // El botón aparece si es fase de votación (o hay pista) y es mi turno
            boolean faseVotacion = faseNormalizada.contains("votan") || hayPistaActiva;
            puedeVotarEnEsteMomento = soyAgente && esMiTurno && faseVotacion && !miVotoEnviado;
        }

        mostrarPreviewCarta(idCarta, palabra, puedeVotarEnEsteMomento);
    }

    private void enviarVoto(int idCarta) {
        try {
            JSONObject json = new JSONObject();
            json.put("id_carta_tablero", idCarta);
            idCartaVotada = idCarta;
            cartas_rojas_restantes_voto = cartas_rojas_restantes;
            cartas_azules_restantes_voto = cartas_azules_restantes;
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

    private int numeroPistaSeleccionado = 1;

    private void mostrarDialogoAnadirPista() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_anadir_pista);
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0));
        android.widget.EditText inputP = dialog.findViewById(R.id.input_palabra_pista);
        
        numeroPistaSeleccionado = 1;
        for (int i = 1; i <= 8; i++) {
            final int val = i;
            int id = getResources().getIdentifier("btn_num_" + i, "id", getPackageName());
            View b = dialog.findViewById(id);
            if (b != null) {
                b.setOnClickListener(v -> {
                    numeroPistaSeleccionado = val;
                    for (int j = 1; j <= 8; j++) {
                        int idOther = getResources().getIdentifier("btn_num_" + j, "id", getPackageName());
                        View bOther = dialog.findViewById(idOther);
                        if (bOther != null) bOther.setSelected(j == val);
                    }
                });
                if (i == 1) b.setSelected(true);
            }
        }

        dialog.findViewById(R.id.btn_enviar_pista).setOnClickListener(v -> {
            String p = inputP.getText().toString().trim();
            if (!p.isEmpty()) {
                enviarPistaServidor(p, numeroPistaSeleccionado);
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
                .url(NetworkConfig.BASE_URL + "/partidas/" + idPartidaActual + "/participantes/rol")
                .addHeader("Authorization", "Bearer " + token).build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                com.example.secretpanda.data.ErrorUtils.showConnectionError(PartidaActivity.this, e);
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        String r = json.optString("rol", "agente");
                        miRol = r.equalsIgnoreCase("lider") ? JEFE_STRING : AGENTE_STRING;
                        miEquipo = json.optString("equipo", miEquipo);
                        runOnUiThread(() -> actualizarInterfazGlobal());
                    } catch (Exception e) { }
                } else {
                    com.example.secretpanda.data.ErrorUtils.showErrorMessage(PartidaActivity.this, response);
                }
            }
        });
    }

    private void obtenerEstadoCompletoDePartida() {
        OkHttpClient client = new OkHttpClient();
        String token = new com.example.secretpanda.data.TokenManager(this).getToken();
        Request request = new Request.Builder()
                .url(NetworkConfig.BASE_URL + "/partidas/" + idPartidaActual + "/estado")
                .addHeader("Authorization", "Bearer " + token).build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                com.example.secretpanda.data.ErrorUtils.showConnectionError(PartidaActivity.this, e);
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        runOnUiThread(() -> aplicarEstadoTotal(json));
                    } catch (Exception e) { }
                } else {
                    com.example.secretpanda.data.ErrorUtils.showErrorMessage(PartidaActivity.this, response);
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

                runOnUiThread(() -> {
                    historialChat.add(json);
                    if (contenedorMensajesActual != null) {
                        boolean esMio = miTag.equals(json.optString("tag", ""));
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
            boolean esMio = Objects.equals(miTag, m.optString("tag", ""));
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
                .url(NetworkConfig.BASE_URL + "/partidas/" + idPartidaActual + "/participantes")
                .delete().addHeader("Authorization", "Bearer " + jwt).build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { finish(); }
            @Override public void onResponse(Call call, Response response) { finish(); }
        });
    }

    private void mostrarPreviewCarta(int idCarta, String palabra, boolean mostrarBotonVotar) {
        Dialog d = new Dialog(this);
        d.setContentView(R.layout.dialog_preview_carta);

        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0));
            d.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvPalabra = d.findViewById(R.id.tv_palabra_preview);
        android.widget.ImageView ivCarta = d.findViewById(R.id.iv_carta_preview);

        boolean esImagen = palabra.startsWith("http") || palabra.contains("/") || palabra.endsWith(".png") || palabra.endsWith(".jpg");

        if (esImagen) {
            if (tvPalabra != null) tvPalabra.setVisibility(View.GONE);
            if (ivCarta != null) {
                ivCarta.setVisibility(View.VISIBLE);
                Glide.with(this)
                    .load(palabra)
                    .placeholder(android.R.color.darker_gray)
                    .error(android.R.color.darker_gray)
                    .into(ivCarta);
            }
        } else {
            if (tvPalabra != null) {
                tvPalabra.setVisibility(View.VISIBLE);
                tvPalabra.setText(palabra.toUpperCase());
            }
            if (ivCarta != null) ivCarta.setVisibility(View.GONE);
        }

        TextView btnVotar = d.findViewById(R.id.btn_votar_preview);
        if (btnVotar != null) {
            if (mostrarBotonVotar) {
                btnVotar.setVisibility(View.VISIBLE);
                btnVotar.setOnClickListener(v -> {
                    EfectosManager.reproducir(getApplicationContext(), R.raw.sonido_click);
                    enviarVoto(idCarta);
                    d.dismiss();
                });
            } else {
                btnVotar.setVisibility(View.GONE);
            }
        }

        View btnCerrar = d.findViewById(R.id.btn_cerrar_preview);
        if (btnCerrar != null) {
            btnCerrar.setOnClickListener(v -> d.dismiss());
        }
        
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

    private void cargarPersonalizaciones() {
        OkHttpClient client = new OkHttpClient();
        String url = NetworkConfig.BASE_URL + "/jugadores";
        String jwt = new com.example.secretpanda.data.TokenManager(this).getToken();
        
        if (jwt == null) return;

        Request request = new Request.Builder()
                .url(url).get().addHeader("Authorization", "Bearer " + jwt).build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {}
            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject obj = new JSONObject(response.body().string());
                        String marco = obj.optString("marco_carta_equipado", "");
                        String fondo = obj.optString("fondo_tablero_equipado", "");
                        customizationManager.saveCustomizations(marco, fondo);
                        runOnUiThread(() -> aplicarTematizacionVisual());
                    } catch (Exception e) {}
                }
            }
        });
    }

    private void aplicarTematizacionVisual() {
        // Aplicamos el color de personalización al contenedor del tablero (la carpeta)
        View tableroContenedor = findViewById(R.id.tablero_contenedor);
        if (tableroContenedor instanceof CardView) {
            ((CardView) tableroContenedor).setCardBackgroundColor(customizationManager.getBoardColor());
        }

        // También a la pestaña para que coincida
        View tabCarpeta = findViewById(R.id.tab_carpeta);
        if (tabCarpeta != null && tabCarpeta.getBackground() != null) {
            tabCarpeta.getBackground().setColorFilter(customizationManager.getBoardColor(), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        // Refrescamos el tablero si ya se ha pintado
        obtenerEstadoCompletoDePartida();
    }
}

