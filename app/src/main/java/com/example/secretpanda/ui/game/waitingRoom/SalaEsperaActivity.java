package com.example.secretpanda.ui.game.waitingRoom;

import android.app.Dialog;
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
import com.example.secretpanda.data.model.ItemPersonalizacion;
import com.example.secretpanda.data.model.InventarioGlobal;
import com.example.secretpanda.ui.customization.PersonalizacionAdapter;
import java.util.ArrayList;
import java.util.List;
import com.example.secretpanda.R;
import com.example.secretpanda.data.model.Jugador;
import java.util.Random;

public class SalaEsperaActivity extends AppCompatActivity {

    private RecyclerView rvJugadores;
    private JugadorSalaAdapter adapter;
    private List<Jugador> listaJugadores;

    private boolean estoyEnEquipoAzul;
    private TextView btnUnirseAzul;
    private TextView btnUnirseRojo;
    private PersonalizacionAdapter adapterPersonalizacionDialogo;
    private Jugador jugadorLocal;
    private int posicionJugadorLocal = 0;

    private TextView tvContadorAzul, tvContadorRojo, tvContadorTotal, tvTiempoSala;

    // Variables de configuración de la sala
    private int maxJugadores = 8;
    private boolean esLider = false;
    private boolean esPrivada = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sala_espera);

        // ==========================================
        //  RECUPERAR DATOS DEL INTENT
        // ==========================================
        esLider = getIntent().getBooleanExtra("ES_LIDER", false);
        esPrivada = getIntent().getBooleanExtra("ES_PRIVADA", false);
        maxJugadores = getIntent().getIntExtra("MAX_JUGADORES", 8);
        int tiempoTurno = getIntent().getIntExtra("TIEMPO_TURNO", 60);

        TextView btnAbandonar = findViewById(R.id.btn_abandonar);
        btnAbandonar.setOnClickListener(v -> mostrarDialogoAbandonar());

        tvContadorAzul = findViewById(R.id.tv_contador_azul);
        tvContadorRojo = findViewById(R.id.tv_contador_rojo);
        tvContadorTotal = findViewById(R.id.tv_jugadores_sala);
        btnUnirseAzul = findViewById(R.id.btn_unirse_azul);
        btnUnirseRojo = findViewById(R.id.btn_unirse_rojo);
        tvTiempoSala = findViewById(R.id.tv_tiempo_sala);

        if (tvTiempoSala != null) {
            tvTiempoSala.setText(tiempoTurno + "s");
        }

        // ==========================================
        // LÓGICA DEL CÓDIGO DE PARTIDA (PÚBLICA VS PRIVADA)
        // ==========================================
        TextView tvCodigoPartida = findViewById(R.id.tv_codigo_partida);
        // Pillamos el layout "padre" que envuelve el texto "Código partida :" y el propio código
        View layoutCodigoEntero = (View) tvCodigoPartida.getParent();

        if (!esPrivada) {
            // Si es pública, ocultamos el bloque del código entero
            layoutCodigoEntero.setVisibility(View.GONE);
        } else {
            // Si es privada, lo mostramos y decidimos qué código poner
            layoutCodigoEntero.setVisibility(View.VISIBLE);

            if (esLider) {
                // El líder crea la sala, así que generamos un código aleatorio nuevo
                tvCodigoPartida.setText(generarCodigoAleatorio());
            } else {
                // Si te unes, mostramos el código que hayas metido en la pantalla anterior
                String codigoRecibido = getIntent().getStringExtra("CODIGO_PARTIDA");
                tvCodigoPartida.setText(codigoRecibido != null ? codigoRecibido : "------");
            }
        }

        String equipoAsignado = getIntent().getStringExtra("MI_EQUIPO");

        // Decidimos el booleano en base al texto (por defecto rojo si algo falla)
        if (equipoAsignado != null && equipoAsignado.equalsIgnoreCase("azul")) {
            estoyEnEquipoAzul = true;
        } else {
            estoyEnEquipoAzul = false;
        }

        // Actualizamos los colores de los botones visualmente
        actualizarBotonesEquipo();

        // 3. ¡IMPORTANTE! Actualizar la interfaz visual
        // Si tienes un jugadorLocal creado en esta pantalla, actualiza su equipo:
        if (jugadorLocal != null) {
            jugadorLocal.setEsEquipoAzul(estoyEnEquipoAzul);
        }


        actualizarBotonesEquipo();

        btnUnirseAzul.setOnClickListener(v -> gestionarClicEquipo(true));
        btnUnirseRojo.setOnClickListener(v -> gestionarClicEquipo(false));

        // CONFIGURACIÓN DE LA LISTA DE JUGADORES
        rvJugadores = findViewById(R.id.rv_jugadores);
        rvJugadores.setLayoutManager(new LinearLayoutManager(this));
        listaJugadores = new ArrayList<>();

        jugadorLocal = new Jugador("TuNombreDeUsuario");
        jugadorLocal.setEsEquipoAzul(estoyEnEquipoAzul);
        listaJugadores.add(jugadorLocal);

        actualizarContadores(listaJugadores);

        // CONFIGURACIÓN DE INTERFAZ SEGÚN SI ES LÍDER O NO
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
                btnIniciarPartida.setOnClickListener(v -> intentarIniciarPartida());
            }
        }

        adapter = new JugadorSalaAdapter(listaJugadores, "TuNombreDeUsuario", esLider, nuevaLista -> {
            actualizarContadores(nuevaLista);
        });
        rvJugadores.setAdapter(adapter);

        findViewById(R.id.btn_tematicas).setOnClickListener(v -> mostrarDialogoEstrella());
    }

    // ==========================================
    // GENERADOR DE CÓDIGO ALEATORIO (6 Caracteres)
    // ==========================================
    private String generarCodigoAleatorio() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder codigo = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < 6; i++) {
            codigo.append(caracteres.charAt(rnd.nextInt(caracteres.length())));
        }
        return codigo.toString();
    }

    // ==========================================
    // LÓGICA DE INICIO DE PARTIDA (SOLO LÍDER)
    // ==========================================
    private void intentarIniciarPartida() {
        int contadorAzul = 0;
        int contadorRojo = 0;

        for (Jugador jugador : listaJugadores) {
            if (jugador.isEsEquipoAzul()) contadorAzul++;
            else contadorRojo++;
        }

        int totalJugadores = listaJugadores.size();

        if (totalJugadores < 4 || contadorAzul < 2 || contadorRojo < 2) {
            mostrarDialogoErrorJugadores();
        }
        else if (totalJugadores < maxJugadores) {
            mostrarDialogoConfirmacion();
        }
        else {
            iniciarJuegoReal();
        }
    }

    private void mostrarDialogoErrorJugadores() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_error_jugadores);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        Button btnCerrar = dialog.findViewById(R.id.btn_cerrar_error);
        if (btnCerrar != null) {
            btnCerrar.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private void mostrarDialogoConfirmacion() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirmar_inicio);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ImageButton btnCerrar = dialog.findViewById(R.id.btn_cerrar_dialogo);
        Button btnIniciar = dialog.findViewById(R.id.btn_iniciar_confirmado);

        btnCerrar.setOnClickListener(v -> dialog.dismiss());
        btnIniciar.setOnClickListener(v -> {
            dialog.dismiss();
            iniciarJuegoReal();
        });

        dialog.show();
    }

    private void iniciarJuegoReal() {
        Toast.makeText(this, "¡Iniciando partida!", Toast.LENGTH_SHORT).show();
    }

    // ==========================================
    // AJUSTES DE SALA (SOLO LÍDER)
    // ==========================================
    private void mostrarDialogoAjustes() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_ajustes_sala);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ImageButton btnCerrar = dialog.findViewById(R.id.btn_cerrar_ajustes);
        btnCerrar.setOnClickListener(v -> dialog.dismiss());

        // Configuración de Tiempo
        TextView[] botonesTiempo = {
                dialog.findViewById(R.id.btn_tiempo_30),
                dialog.findViewById(R.id.btn_tiempo_60),
                dialog.findViewById(R.id.btn_tiempo_80),
                dialog.findViewById(R.id.btn_tiempo_120)
        };

        String tiempoActualStr = tvTiempoSala.getText().toString();
        for (TextView btn : botonesTiempo) {
            if ((btn.getText().toString()).equals(tiempoActualStr)) {
                seleccionarBoton(btn, botonesTiempo);
            }
            btn.setOnClickListener(v -> {
                seleccionarBoton(btn, botonesTiempo);
                if (tvTiempoSala != null) tvTiempoSala.setText(btn.getText().toString());
            });
        }

        // Configuración de Jugadores
        TextView[] botonesJugadores = {
                dialog.findViewById(R.id.btn_jugadores_4),
                dialog.findViewById(R.id.btn_jugadores_6),
                dialog.findViewById(R.id.btn_jugadores_8),
                dialog.findViewById(R.id.btn_jugadores_10),
                dialog.findViewById(R.id.btn_jugadores_12),
                dialog.findViewById(R.id.btn_jugadores_14),
                dialog.findViewById(R.id.btn_jugadores_16)
        };

        TextView tvAdvertencia = dialog.findViewById(R.id.tv_advertencia_sala);
        tvAdvertencia.setVisibility(View.GONE);

        for (TextView btn : botonesJugadores) {
            if (btn.getText().toString().equals(String.valueOf(maxJugadores))) {
                seleccionarBoton(btn, botonesJugadores);
            }

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
                if (btn == seleccionado) {
                    btn.setBackgroundResource(R.drawable.fondo_carta_seleccionada);
                } else {
                    btn.setBackgroundResource(R.drawable.fondo_boton_mision);
                }
            }
        }
    }

    // ==========================================
    // LÓGICA DE EQUIPOS Y UI
    // ==========================================
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

    private void gestionarClicEquipo(boolean pulsadoAzul) {
        if (estoyEnEquipoAzul == pulsadoAzul) return;

        estoyEnEquipoAzul = pulsadoAzul;
        actualizarBotonesEquipo();

        jugadorLocal.setEsEquipoAzul(estoyEnEquipoAzul);
        if (adapter != null) adapter.notifyItemChanged(posicionJugadorLocal);

        if (listaJugadores != null) actualizarContadores(listaJugadores);
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

    // ==========================================
    // OTROS DIÁLOGOS (Abandonar y Personalización)
    // ==========================================
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
            finish();
        });
        dialog.show();
    }

    private void mostrarDialogoEstrella() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_tematicas_sala);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ImageButton btnCerrar = dialog.findViewById(R.id.btn_cerrar_personalizacion);
        TextView txtTematica = dialog.findViewById(R.id.txt_tematica_seleccionada_dialog);
        RecyclerView recycler = dialog.findViewById(R.id.recycler_tematicas_dialog);
        recycler.setLayoutManager(new GridLayoutManager(this, 3));

        LinearLayout tabBordes = dialog.findViewById(R.id.tab_bordes);
        LinearLayout tabFondos = dialog.findViewById(R.id.tab_fondos);
        TextView txtBordes = dialog.findViewById(R.id.txt_tab_bordes);
        TextView txtFondos = dialog.findViewById(R.id.txt_tab_fondos);

        btnCerrar.setOnClickListener(v -> dialog.dismiss());

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
}