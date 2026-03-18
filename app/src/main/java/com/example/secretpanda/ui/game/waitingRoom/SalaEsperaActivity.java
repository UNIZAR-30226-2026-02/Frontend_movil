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

    // NUEVO: Hacemos la lista global para poder usarla en cualquier método
    private List<Jugador> listaJugadores;

    // Variables para la lógica
    private boolean estoyEnEquipoAzul; // true = Azul, false = Rojo
    private TextView btnUnirseAzul;
    private TextView btnUnirseRojo;
    private PersonalizacionAdapter adapterPersonalizacionDialogo;
    // Tu jugador
    private Jugador jugadorLocal;
    private int posicionJugadorLocal = 0;
    private TextView tvContadorAzul, tvContadorRojo, tvContadorTotal, tvTiempoSala;

    private int maxJugadores = 8; // O el máximo que hayas configurado en los ajustes
    private boolean esLider = false; // NUEVO: Variable global de rol (por defecto false)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sala_espera);

        TextView btnAbandonar = findViewById(R.id.btn_abandonar);
        btnAbandonar.setOnClickListener(v -> mostrarDialogoAbandonar());

        // 1. Vinculamos los TextViews de tu XML
        tvContadorAzul = findViewById(R.id.tv_contador_azul);
        tvContadorRojo = findViewById(R.id.tv_contador_rojo);
        tvContadorTotal = findViewById(R.id.tv_jugadores_sala);
        btnUnirseAzul = findViewById(R.id.btn_unirse_azul);
        btnUnirseRojo = findViewById(R.id.btn_unirse_rojo);
        tvTiempoSala = findViewById(R.id.tv_tiempo_sala);

        // ==========================================
        // ASIGNACIÓN ALEATORIA AL ENTRAR (true o false)
        // ==========================================
        estoyEnEquipoAzul = new Random().nextBoolean();

        if (estoyEnEquipoAzul) {
            setModoDentro(btnUnirseAzul, "#0000FF");
            setModoUnirse(btnUnirseRojo, "#FF0000");
        } else {
            setModoDentro(btnUnirseRojo, "#FF0000");
            setModoUnirse(btnUnirseAzul, "#0000FF");
        }

        // Lógica al pulsar los botones
        btnUnirseAzul.setOnClickListener(v -> gestionarClicEquipo(true));
        btnUnirseRojo.setOnClickListener(v -> gestionarClicEquipo(false));

        // ==========================================
        // CONFIGURACIÓN DE LA LISTA
        // ==========================================
        rvJugadores = findViewById(R.id.rv_jugadores);
        rvJugadores.setLayoutManager(new LinearLayoutManager(this));

        // NUEVO: Usamos la lista global en lugar de una local
        listaJugadores = new ArrayList<>();

        // 1. CREAMOS TU JUGADOR con el equipo aleatorio
        jugadorLocal = new Jugador("TuNombreDeUsuario");
        jugadorLocal.setEsEquipoAzul(estoyEnEquipoAzul);
        listaJugadores.add(jugadorLocal);
        posicionJugadorLocal = 0;

        // 2. Añadimos otros jugadores de prueba
        Jugador j2 = new Jugador("GabriThePro");
        j2.setEsEquipoAzul(false); // Rojo
        listaJugadores.add(j2);

        Jugador j3 = new Jugador("PandaNinja");
        j3.setEsEquipoAzul(true); // Azul
        listaJugadores.add(j3);

        // 2. Calculamos los contadores por primera vez al abrir la pantalla
        actualizarContadores(listaJugadores);

        esLider = getIntent().getBooleanExtra("ES_LIDER", false); // Cámbialo a false para probar el rol normal
        TextView btnIniciarPartida = findViewById(R.id.btn_iniciar_partida_principal);
        View btnConfiguracion = findViewById(R.id.btn_configuracion); // Asegúrate de que el ID del layout de la tuerca es este

        if (!esLider) {
            // SI NO ERES LÍDER:

            // 1. Ocultamos la tuerca de ajustes
            if (btnConfiguracion != null) btnConfiguracion.setVisibility(View.GONE);

            // 2. Cambiamos el texto del botón y lo desactivamos
            if (btnIniciarPartida != null) {
                btnIniciarPartida.setText("Esperando\nal líder...");
                // Opcional: Le bajamos la opacidad para que parezca desactivado
                btnIniciarPartida.setAlpha(0.5f);
                btnIniciarPartida.setEnabled(false);
            }
        } else {
            // SI SÍ ERES LÍDER:

            // Configuramos los clics normales de líder
            if (btnConfiguracion != null) {
                btnConfiguracion.setOnClickListener(v -> mostrarDialogoAjustes());
            }

            if (btnIniciarPartida != null) {
                btnIniciarPartida.setOnClickListener(v -> {
                    if (listaJugadores != null && listaJugadores.size() < 4) {
                        mostrarDialogoErrorJugadores();
                    } else {
                        mostrarDialogoConfirmacion();
                    }
                });
            }
        }
        String miNombre = "TuNombreDeUsuario";

        // NUEVO: Le pasamos la variable 'esLider' al adapter al crearlo
        adapter = new JugadorSalaAdapter(listaJugadores, miNombre, esLider, nuevaLista -> {
            actualizarContadores(nuevaLista);
        });

        rvJugadores.setAdapter(adapter);

        rvJugadores.setAdapter(adapter);

        // Botón de la estrella (Temáticas)
        findViewById(R.id.btn_tematicas).setOnClickListener(v -> mostrarDialogoEstrella());

        // Botón de la tuerca (Configuración)
        findViewById(R.id.btn_configuracion).setOnClickListener(v -> mostrarDialogoAjustes());

    }

    private void mostrarDialogoErrorJugadores() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Usaremos un nuevo layout XML que vamos a crear en el siguiente paso
        dialog.setContentView(R.layout.dialog_error_jugadores);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Configuramos el botón de cerrar
        Button btnCerrar = dialog.findViewById(R.id.btn_cerrar_error);
        if (btnCerrar != null) {
            btnCerrar.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    /**
     * Función que cuenta cuántos jugadores hay en cada equipo
     * y actualiza los textos de la pantalla.
     */
    private void actualizarContadores(List<Jugador> lista) {
        int contadorAzul = 0;
        int contadorRojo = 0;

        // Recorremos la lista actual para contar
        for (Jugador jugador : lista) {
            if (jugador.isEsEquipoAzul()) {
                contadorAzul++;
            } else {
                contadorRojo++;
            }
        }

        // Actualizamos los textos en la pantalla
        if (tvContadorAzul != null) tvContadorAzul.setText("Azul: " + String.valueOf(contadorAzul) + "/" + maxJugadores/2);
        if (tvContadorRojo != null) tvContadorRojo.setText("Rojo: " + String.valueOf(contadorRojo) + "/" + maxJugadores/2);

        // Actualizamos el contador total (ej: "4/8")
        if (tvContadorTotal != null) tvContadorTotal.setText(lista.size() + "/" + maxJugadores);
    }

    // ==========================================
    // MÉTODOS PARA CONTROLAR LOS BOTONES Y LA LISTA
    // ==========================================

    private void gestionarClicEquipo(boolean pulsadoAzul) {
        // Si pulsas el botón del equipo en el que ya estás, no hacemos nada
        if (estoyEnEquipoAzul == pulsadoAzul) {
            return;
        }

        // Si pulsas el equipo contrario, te cambiamos
        estoyEnEquipoAzul = pulsadoAzul;

        if (estoyEnEquipoAzul) {
            // Me paso al AZUL
            setModoDentro(btnUnirseAzul, "#0000FF");
            setModoUnirse(btnUnirseRojo, "#FF0000");
        } else {
            // Me paso al ROJO
            setModoDentro(btnUnirseRojo, "#FF0000");
            setModoUnirse(btnUnirseAzul, "#0000FF");
        }

        // ¡MAGIA! Actualizamos tu tarjeta al nuevo equipo
        jugadorLocal.setEsEquipoAzul(estoyEnEquipoAzul);
        if (adapter != null) {
            adapter.notifyItemChanged(posicionJugadorLocal);
        }

        // NUEVO: Volvemos a contar los jugadores para actualizar los textos
        if (listaJugadores != null) {
            actualizarContadores(listaJugadores);
        }
    }

    // Pinta el botón con el borde de color y fondo transparente
    private void setModoDentro(TextView btn, String colorHex) {
        btn.setText("Dentro");
        btn.setBackgroundResource(R.drawable.fondo_boton_dentro);

        GradientDrawable fondo = (GradientDrawable) btn.getBackground().mutate();
        fondo.setStroke(5, Color.parseColor(colorHex));

        btn.setTextColor(Color.parseColor(colorHex));
        btn.setShadowLayer(8f, 0f, 0f, Color.parseColor("#FFFFFF"));
    }

    // Pinta el botón con el fondo sólido de color
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
    // DIÁLOGOS
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

    private void activarPestana(LinearLayout activo, TextView txtActivo,
                                LinearLayout inactivo, TextView txtInactivo) {
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
            if (item.getTipo().equals(categoria) && !item.isBloqueado()) {
                posesion.add(item);
            }
        }

        if (!posesion.isEmpty()) {
            txtTematica.setText(posesion.get(0).getNombre());
        } else {
            txtTematica.setText("Ninguna");
        }

        adapterPersonalizacionDialogo = new PersonalizacionAdapter(posesion, false, true, (item, position) -> {
            txtTematica.setText(item.getNombre());
            if (adapterPersonalizacionDialogo != null) {
                adapterPersonalizacionDialogo.setPosicionSeleccionada(position);
            }
        });

        recycler.setAdapter(adapterPersonalizacionDialogo);
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
        // Código para pasar al tablero de juego...
    }

    private void mostrarDialogoAjustes() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_ajustes_sala);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ImageButton btnCerrar = dialog.findViewById(R.id.btn_cerrar_ajustes);
        btnCerrar.setOnClickListener(v -> dialog.dismiss());

        TextView btnTiempo30 = dialog.findViewById(R.id.btn_tiempo_30);
        TextView btnTiempo60 = dialog.findViewById(R.id.btn_tiempo_60);
        TextView btnTiempo80 = dialog.findViewById(R.id.btn_tiempo_80);
        TextView btnTiempo120 = dialog.findViewById(R.id.btn_tiempo_120);

        TextView[] botonesTiempo = {btnTiempo30, btnTiempo60, btnTiempo80, btnTiempo120};

        // --- NUEVA LÓGICA PARA EL TIEMPO ---
        for (TextView btn : botonesTiempo) {
            btn.setOnClickListener(v -> {
                // 1. Cambiamos el color (esto ya lo tenías)
                seleccionarBoton(btn, botonesTiempo);

                // 2. Leemos el texto del botón (ej: "60s" o "60")
                String tiempoSeleccionado = btn.getText().toString();

                // 3. Actualizamos el texto de la pantalla principal al instante
                if (tvTiempoSala != null) {
                    tvTiempoSala.setText(tiempoSeleccionado);
                }
            });
        }

        TextView btnJugadores4 = dialog.findViewById(R.id.btn_jugadores_4);
        TextView btnJugadores6 = dialog.findViewById(R.id.btn_jugadores_6);
        TextView btnJugadores8 = dialog.findViewById(R.id.btn_jugadores_8);
        TextView btnJugadores10 = dialog.findViewById(R.id.btn_jugadores_10);

        TextView[] botonesJugadores = {btnJugadores4, btnJugadores6, btnJugadores8, btnJugadores10};

        // --- NUEVA LÓGICA PARA LOS JUGADORES ---
        for (TextView btn : botonesJugadores) {
            btn.setOnClickListener(v -> {
                // 1. Cambiamos el color del botón
                seleccionarBoton(btn, botonesJugadores);

                try {
                    // 2. Extraemos SOLO los números del texto del botón
                    // (Por si tu botón dice "8 Jug." esto saca solo el "8")
                    String numStr = btn.getText().toString().replaceAll("\\D+", "");

                    // 3. Actualizamos nuestra variable global de maxJugadores
                    maxJugadores = Integer.parseInt(numStr);

                    // 4. ¡LA MAGIA! Llamamos a tu método de contadores para que
                    // recalcule la pantalla (ej: pasará de "4/8" a "4/10")
                    if (listaJugadores != null) {
                        actualizarContadores(listaJugadores);
                    }
                } catch (Exception e) {
                    e.printStackTrace(); // Por si ocurre algún error al leer el número
                }
            });
        }

        dialog.show();
    }

    private void seleccionarBoton(TextView seleccionado, TextView[] grupo) {
        for (TextView btn : grupo) {
            if (btn == seleccionado) {
                btn.setBackgroundResource(R.drawable.fondo_carta_seleccionada);
            } else {
                btn.setBackgroundResource(R.drawable.fondo_boton_mision);
            }
        }
    }
}
