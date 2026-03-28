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
    private PersonalizacionAdapter adapterPersonalizacionDialogo;
    private Jugador jugadorLocal;

    // ID real de la partida y Token del usuario
    private int idPartida = -1;
    private String miPropioIdGoogle = "";

    private TextView tvContadorAzul, tvContadorRojo, tvContadorTotal, tvTiempoSala;

    // Variables de configuración de la sala
    private int maxJugadores = 8;
    private boolean esLider = false;
    private boolean esPrivada = false;

    private Dialog dialogoCarga;

    private StompClient stompClient;

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
        String tiempoTurno = getIntent().getStringExtra("TIEMPO_TURNO");
        if (tiempoTurno == null) {
            tiempoTurno = "60"; // Valor por defecto por si falla
        }

        // Recuperamos el ID real de la partida que nos ha dado el Backend
        idPartida = getIntent().getIntExtra("ID_PARTIDA", -1);
        Log.d("API_LOBBY", "🔍 Verificando ID na Sala de Espera: " + idPartida);
        // Leemos quién somos nosotros (para luego saber nuestro rol)
        miPropioIdGoogle = getIntent().getStringExtra("MI_NOMBRE_USUARIO");
        if(miPropioIdGoogle == null) miPropioIdGoogle = "TuNombreDeUsuario";

        TextView btnAbandonar = findViewById(R.id.btn_abandonar);
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


        // Ejemplo para el botón del Equipo Azul
        btnUnirseAzul.setOnClickListener(v -> {
            if (!estoyEnEquipoAzul) { // Solo si no está ya en el azul
                estoyEnEquipoAzul = true;
                actualizarBotonesEquipo();

                // ¡LA CONEXIÓN AL BACKEND!
                cambiarEquipoEnBackend("azul");
            }
        });

        // Ejemplo para el botón del Equipo Rojo
        btnUnirseRojo.setOnClickListener(v -> {
            if (estoyEnEquipoAzul) { // Solo si estaba en el azul y quiere pasar al rojo
                estoyEnEquipoAzul = false;
                actualizarBotonesEquipo();

                // ¡LA CONEXIÓN AL BACKEND!
                cambiarEquipoEnBackend("rojo");
            }
        });

        // CONFIGURACIÓN DE LA LISTA DE JUGADORES
        rvJugadores = findViewById(R.id.rv_jugadores);
        rvJugadores.setLayoutManager(new LinearLayoutManager(this));
        listaJugadores = new ArrayList<>();



        // BOTÓN INICIAR PARTIDA (RF-13)
        TextView btnIniciarPartida = findViewById(R.id.btn_iniciar_partida_principal);
        View btnConfiguracion = findViewById(R.id.btn_configuracion);

        if (!esLider) {
            if (btnConfiguracion != null) btnConfiguracion.setVisibility(View.GONE);
            if (btnIniciarPartida != null) {
                btnIniciarPartida.setText("Esperando\nal líder...");
                btnIniciarPartida.setAlpha(0.5f);
                btnIniciarPartida.setEnabled(false);
            }
        } else {
            if (btnConfiguracion != null) {
                btnConfiguracion.setVisibility(View.VISIBLE);
                btnConfiguracion.setOnClickListener(v -> mostrarDialogoAjustes());
            }
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
        cargarLobbyInicial();

        findViewById(R.id.btn_tematicas).setOnClickListener(v -> mostrarDialogoEstrella());
    }


    private void suscribirseAlCanalDelLobby() {
        // IMPORTANTE: Asegúrate de que esta ruta coincide con el canal (@SendTo) que use tu backend
        String destinoTopic = "/topic/partidas/" + idPartida + "/lobby";

        stompClient.topic(destinoTopic).subscribe(stompMessage -> {
            try {
                String jsonCrudo = stompMessage.getPayload();
                org.json.JSONObject json = new org.json.JSONObject(jsonCrudo);

                // 1. SI LA PARTIDA HA EMPEZADO: Todos saltan al tablero
                if ("en_curso".equalsIgnoreCase(json.optString("estado", ""))) {
                    runOnUiThread(() -> {
                        android.content.Intent intent = new android.content.Intent(SalaEsperaActivity.this, PartidaActivity.class);
                        intent.putExtra("ID_PARTIDA", idPartida);
                        intent.putExtra("MI_NOMBRE_USUARIO", miPropioIdGoogle);
                        intent.putExtra("MI_EQUIPO", estoyEnEquipoAzul ? "azul" : "rojo");
                        startActivity(intent);
                        finish();
                    });
                }
                // 2. SI HAY CAMBIOS EN EL LOBBY: Actualizar la lista de jugadores
                else if (json.has("jugadores")) {
                    org.json.JSONArray jugadoresArray = json.getJSONArray("jugadores");

                    // Procesamos los nuevos datos en el hilo principal para que la pantalla no explote
                    runOnUiThread(() -> {
                        listaJugadores.clear(); // Vaciamos la lista vieja

                        for (int i = 0; i < jugadoresArray.length(); i++) {
                            try {
                                org.json.JSONObject jp = jugadoresArray.getJSONObject(i);

                                // Extraer el nombre (tag)
                                String tag = "Anónimo";
                                if (jp.has("jugador")) {
                                    tag = jp.getJSONObject("jugador").optString("tag", "Anónimo");
                                } else if (jp.has("tag")) {
                                    tag = jp.getString("tag");
                                }

                                // Extraer el equipo
                                String equipo = jp.optString("equipo", "ROJO");

                                // Crear y añadir el jugador actualizado
                                Jugador jugadorActualizado = new Jugador(tag);
                                jugadorActualizado.setEsEquipoAzul("AZUL".equalsIgnoreCase(equipo));
                                listaJugadores.add(jugadorActualizado);

                                // Si somos nosotros, actualizamos nuestros botones de color
                                if (tag.equals(miPropioIdGoogle)) {
                                    estoyEnEquipoAzul = "AZUL".equalsIgnoreCase(equipo);
                                    jugadorLocal = jugadorActualizado;
                                    actualizarBotonesEquipo();
                                }

                            } catch (Exception e) {
                                android.util.Log.e("WS_LOBBY", "Error leyendo a un jugador del WebSocket", e);
                            }
                        }

                        // ¡AVISAR AL ADAPTADOR DE QUE HAY CAMBIOS!
                        adapter.notifyDataSetChanged();
                        actualizarContadores(listaJugadores);
                    });
                }

            } catch (Exception e) {
                android.util.Log.e("WS_LOBBY", "Error desencriptando el mensaje del lobby", e);
            }
        }, throwable -> {
            android.util.Log.e("WS_LOBBY", "Error de interferencia al suscribirse", throwable);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 🧹 Limpieza táctica: Desconectamos radares si siguen activos
        if (stompClient != null && stompClient.isConnected()) {
            stompClient.disconnect();
        }

        // 🛡️ El Escudo de Nulos: PRIMERO comprobamos que no sea null
        if (dialogoCarga != null) {
            // LUEGO comprobamos si se está mostrando
            if (dialogoCarga.isShowing()) {
                dialogoCarga.dismiss();
            }
        }
    }
    private void cambiarEquipoEnBackend(String nuevoEquipo) {
        if (stompClient != null && stompClient.isConnected()) {
            try {
                // 1. Empaquetamos la decisión.
                // Tu backend usa CambiarEquipoPayload que seguramente espera el campo "equipo"
                org.json.JSONObject payload = new org.json.JSONObject();
                payload.put("equipo", nuevoEquipo); // "ROJO" o "AZUL"

                // 2. Ruta exacta hacia tu backend (con el prefijo /app)
                String destino = "/app/partida/" + idPartida + "/participantes/equipo";

                // 3. Disparamos la señal
                stompClient.send(destino, payload.toString()).subscribe(() -> {
                    android.util.Log.d("WS_EQUIPO", "Señal de cambio a equipo " + nuevoEquipo + " enviada con éxito.");
                }, throwable -> {
                    android.util.Log.e("WS_EQUIPO", "Error de interferencia al cambiar de equipo", throwable);
                    runOnUiThread(() -> Toast.makeText(SalaEsperaActivity.this, "Error al comunicar el cambio de equipo", Toast.LENGTH_SHORT).show());
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Sin conexión por radio con el servidor.", Toast.LENGTH_SHORT).show();
        }
    }
    private void conectarWebSocketLobby() {
        // 1. URL de tu WebSocket (Basado en tu IP del emulador)
        // Nota: Revisa en tu backend (WebSocketConfig) si la ruta de conexión es /ws, /stomp, etc.
        String wsUrl = "ws://10.0.2.2:8080/ws/websocket";

        // 2. Inicializar el cliente
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, wsUrl);

        // 3. Recuperar tu credencial (Token JWT)
        TokenManager tokenManager = new TokenManager(this);
        String jwt = tokenManager.getToken();

        // 4. Preparar las cabeceras de seguridad
        List<StompHeader> headers = new ArrayList<>();
        if (jwt != null && !jwt.isEmpty()) {
            headers.add(new StompHeader("Authorization", "Bearer " + jwt));
        }

        // 5. Escuchar el estado del radar (Para saber si conectó bien)
        stompClient.lifecycle().subscribe(lifecycleEvent -> {
            switch (lifecycleEvent.getType()) {
                case OPENED:
                    android.util.Log.d("WS_LOBBY", "¡Radar WebSocket Conectado!");
                    suscribirseAlCanalDelLobby();
                    break;
                case ERROR:
                    android.util.Log.e("WS_LOBBY", "Error en la señal del radar", lifecycleEvent.getException());
                    break;
                case CLOSED:
                    android.util.Log.d("WS_LOBBY", "Radar desconectado.");
                    break;
            }
        });

        // 6. ¡Encender la antena!
        stompClient.connect(headers);
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

    private void mandarOrdenDeInicio() {
        Toast.makeText(this, "Arrancando motores...", Toast.LENGTH_SHORT).show();

        OkHttpClient client = new OkHttpClient();
        // 1. URL CORREGIDA a "/iniciar"
        String url = "http://10.0.2.2:8080/api/partida/" + idPartida + "/iniciar";

        TokenManager tokenManager = new TokenManager(this);
        String jwt = tokenManager.getToken();

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
                    android.util.Log.e("API_INICIAR", " HTTP " + response.code());
                    runOnUiThread(() -> Toast.makeText(SalaEsperaActivity.this, "El servidor rechazó iniciar la partida", Toast.LENGTH_SHORT).show());
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
        // Usamos la variable global
        dialogoCarga = new Dialog(this);
        dialogoCarga.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogoCarga.setContentView(R.layout.dialog_confirmar_inicio);
        if (dialogoCarga.getWindow() != null) dialogoCarga.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialogoCarga.findViewById(R.id.btn_cerrar_dialogo).setOnClickListener(v -> dialogoCarga.dismiss());

        Button btnIniciarPartida = dialogoCarga.findViewById(R.id.btn_iniciar_confirmado);
        btnIniciarPartida.setOnClickListener(v -> {
            btnIniciarPartida.setEnabled(false);
            btnIniciarPartida.setText("Iniciando...");
            dialogoCarga.setCancelable(false); // Para que no lo cierren tocando fuera
            iniciarPartidaHTTP();
        });

        dialogoCarga.show();
    }

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
                seleccionarBoton(btn, botonesTiempo);
                String nuevoTiempoStr = btn.getText().toString();

                if (tvTiempoSala != null) tvTiempoSala.setText(nuevoTiempoStr);

                // Limpiamos la cadena por si el botón dice "60s" en vez de "60"
                int nuevoTiempoNum = Integer.parseInt(nuevoTiempoStr.replace("s", "").trim());

                // ¡AQUÍ ESTÁ LA CONEXIÓN! Avisamos al backend
                cambiarTiempoTurnoEnBackend(nuevoTiempoNum);
            });
        }
        dialog.show();
    }

    private void cambiarTiempoTurnoEnBackend(int nuevoTiempo) {
        if (stompClient != null && stompClient.isConnected()) {
            try {
                // 1. Preparamos el JSON (Asegúrate de que la clase CambiarTiempoPayload de tu backend espera el campo "tiempoEspera")
                org.json.JSONObject payload = new org.json.JSONObject();
                payload.put("tiempoEspera", nuevoTiempo);

                // 2. Ruta de destino del @MessageMapping (recuerda que Spring Boot suele usar /app)
                String destino = "/app/partida/" + idPartida + "/tiempoTurno";

                // 3. Disparamos el cambio por radio
                stompClient.send(destino, payload.toString()).subscribe(() -> {
                    android.util.Log.d("WS_TIEMPO", "Tiempo actualizado a " + nuevoTiempo + " segundos.");
                }, throwable -> {
                    android.util.Log.e("WS_TIEMPO", "Error al enviar el nuevo tiempo", throwable);
                    runOnUiThread(() -> Toast.makeText(SalaEsperaActivity.this, "Error de red al cambiar el tiempo", Toast.LENGTH_SHORT).show());
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "El radar WebSocket no está conectado", Toast.LENGTH_SHORT).show();
        }
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

            // ¡NUEVO! Avisamos al servidor antes de irnos
            salirDeLaSala();
        });

        dialog.show();
    }
    private void salirDeLaSala() {
        // 1. Recuperamos el token para identificarnos
        TokenManager tokenManager = new TokenManager(this);
        String jwt = tokenManager.getToken();

        if (jwt == null || idPartida == 0) {
            finish(); // Si no hay datos, cerramos la pantalla y listo
            return;
        }

        // 2. Preparamos la URL EXACTA que espera tu backend
        // Asumiendo que tu PartidaController tiene @RequestMapping("/api/partidas")
        String url = "http://10.0.2.2:8080/api/partidas/" + idPartida + "/participantes";

        OkHttpClient client = new OkHttpClient();

        // 3. Hacemos una petición DELETE (no POST) con el Token
        Request request = new Request.Builder()
                .url(url)
                .delete() // 👈 ¡Cambiado a DELETE para coincidir con tu @DeleteMapping!
                .addHeader("Authorization", "Bearer " + jwt)
                .build();

        // 4. Hacemos la llamada al servidor
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@androidx.annotation.NonNull Call call, @androidx.annotation.NonNull IOException e) {
                android.util.Log.e("API_ABANDONAR", "Error de red al abandonar", e);
                // Aunque falle la red, cerramos la pantalla para no dejar al usuario atrapado
                runOnUiThread(() -> finish());
            }

            @Override
            public void onResponse(@androidx.annotation.NonNull Call call, @androidx.annotation.NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    android.util.Log.e("API_ABANDONAR", "El servidor falló al sacarnos: " + response.code());
                } else {
                    android.util.Log.i("API_ABANDONAR", "¡Abandono exitoso!");
                }
                // Haya ido bien o mal, sacamos al jugador de la sala de espera visualmente
                runOnUiThread(() -> finish());
            }
        });
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
        String url = "http://10.0.2.2:8080/api/partidas/" + idPartida + "/lobby";

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
                    // 🕵️‍♂️ EL INTERROGATORIO: Vamos a leer la excusa exacta que nos da el servidor
                    try {
                        String errorBody = response.body() != null ? response.body().string() : "Cuerpo vacío";
                        android.util.Log.e("API_LOBBY", "¡ATRAPADO! Código: " + response.code() + ". Motivo: " + errorBody);
                    } catch (Exception e) {
                        android.util.Log.e("API_LOBBY", "No se pudo leer el cuerpo del error");
                    }

                    runOnUiThread(() -> Toast.makeText(SalaEsperaActivity.this, "No se pudo cargar la sala. Código: " + response.code(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
    private void iniciarPartidaHTTP() {
        android.util.Log.d("API_LOBBY", "🚀 Solicitando al servidor iniciar la partida...");

        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        // Apuntamos al endpoint @PutMapping("/api/partida/{id_partida}/iniciar")
        String url = "http://10.0.2.2:8080/api/partida/" + idPartida + "/iniciar";

        com.example.secretpanda.data.TokenManager tokenManager = new com.example.secretpanda.data.TokenManager(this);
        String token = tokenManager.getToken();

        // Como es un PUT, la librería OkHttp nos exige un "cuerpo" (body), aunque esté vacío.
        okhttp3.RequestBody body = new okhttp3.FormBody.Builder().build();
        okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder()
                .url(url)
                .put(body);

        if (token != null && !token.isEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer " + token);
        }

        okhttp3.Request request = requestBuilder.build();

        // Hacemos la llamada en segundo plano
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                android.util.Log.e("API_LOBBY", "❌ Error de red al intentar iniciar la partida", e);
                runOnUiThread(() -> android.widget.Toast.makeText(SalaEsperaActivity.this, "Error de conexión", android.widget.Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful()) {
                    android.util.Log.d("API_LOBBY", "✅ ¡Partida iniciada en el servidor!");

                    // 🚀 PREPARAMOS EL INTENT HACIA EL TABLERO
                    runOnUiThread(() -> {
                        android.content.Intent intent = new android.content.Intent(SalaEsperaActivity.this, PartidaActivity.class);
                        // Pasamos toda la munición necesaria a la siguiente pantalla
                        intent.putExtra("ID_PARTIDA", idPartida);
                        intent.putExtra("MI_NOMBRE_USUARIO", miPropioIdGoogle);
                        intent.putExtra("MI_EQUIPO", estoyEnEquipoAzul ? "AZUL" : "ROJO");
                        intent.putExtra("ES_LIDER", true); // ¡Somos el líder, esto es vital!

                        startActivity(intent);
                        finish(); // Cerramos la sala de espera para que no pueda volver atrás con el botón de retroceso
                    });
                } else {
                    // 🕵️‍♂️ Extraemos el cuerpo del error para ver qué dice el servidor
                    String motivo = "Motivo desconocido";
                    try {
                        motivo = response.body() != null ? response.body().string() : "Cuerpo vacío";
                    } catch (Exception e) {
                        motivo = "No se pudo leer el cuerpo";
                    }

                    android.util.Log.e("API_LOBBY", "⚠️ El servidor denegó el inicio. Código: " + response.code() + " - Detalles: " + motivo);

                    final String mensajeFinal = motivo;
                    runOnUiThread(() -> android.widget.Toast.makeText(SalaEsperaActivity.this, "Error: " + mensajeFinal, android.widget.Toast.LENGTH_LONG).show());
                }
            }
        });
    }
}