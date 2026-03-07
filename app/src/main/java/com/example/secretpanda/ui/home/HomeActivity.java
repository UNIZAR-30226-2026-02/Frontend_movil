package com.example.secretpanda.ui.home;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.secretpanda.R;
import com.example.secretpanda.data.model.Jugador;
import com.example.secretpanda.ui.ClasificacionActivity; // Importamos la nueva pantalla

import com.example.secretpanda.ui.PerfilActivity;
import com.example.secretpanda.ui.PersonalizacionActivity;
import com.example.secretpanda.ui.TiendaActivity;


public class HomeActivity extends AppCompatActivity {

    private Jugador jugadorActual;
    private ActivityResultLauncher<Intent> perfilLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Quitar la barra de título superior por defecto de Android
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.home);

        // =========================================================
        // 1. RECIBIR EL JUGADOR Y CAMBIAR EL SALUDO
        // =========================================================
        TextView textoSaludo = findViewById(R.id.texto_saludo_home);

        jugadorActual = (Jugador) getIntent().getSerializableExtra("DATOS_JUGADOR");
        String nombreMostrar = "Espía Secreto";

        if (jugadorActual != null) {
            nombreMostrar = jugadorActual.getTag(); // Sacamos el nombre real
        }

        if (textoSaludo != null) {
            textoSaludo.setText("Hola, " + nombreMostrar);
        }

        // =========================================================
        // 2. CAPTURAR BOTONES PRINCIPALES
        // =========================================================
        Button btnNuevaMision = findViewById(R.id.btn_nueva_mision);
        Button btnUneteMision = findViewById(R.id.btn_unete_mision);

        // El botón de las 3 rayitas
        ImageView btnMenuOpciones = findViewById(R.id.btn_menu_opciones);

        // ¡NUEVO! El botón de la copa para la clasificación
        ImageView btnClasificacion = findViewById(R.id.btn_clasificacion);

        // ¡NUEVO! El botón de la copa para la clasificación
        ImageView btnPerfil = findViewById(R.id.btn_perfil);
        // Configurar el escuchador ANTES de usarlo
        perfilLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // Recogemos el texto nuevo
                        String nombreActualizado = result.getData().getStringExtra("NOMBRE_ACTUALIZADO");

                        if (nombreActualizado != null) {
                            // Lo guardamos en nuestro objeto
                            jugadorActual.setTag(nombreActualizado);
                            textoSaludo.setText("Hola, " + nombreActualizado);
                        }
                    }
                }
        );

        btnPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, PerfilActivity.class);

            // PASAMOS SOLO EL STRING (EL NOMBRE)
            intent.putExtra("NOMBRE_JUGADOR", jugadorActual.getTag());

            // Abrimos la pantalla
            perfilLauncher.launch(intent);
        });

        // =========================================================
        // 3. DARLES ACCIÓN A LOS BOTONES
        // =========================================================
        btnNuevaMision.setOnClickListener(v -> Toast.makeText(HomeActivity.this, "Iniciando Nueva Misión...", Toast.LENGTH_SHORT).show());
        btnUneteMision.setOnClickListener(v -> Toast.makeText(HomeActivity.this, "Buscando Misiones disponibles...", Toast.LENGTH_SHORT).show());

        // Al pulsar las rayitas, abrimos tu menú personalizado
        btnMenuOpciones.setOnClickListener(v -> mostrarMenuPersonalizado(v));

        // ¡Al pulsar la copa, viajamos a la pantalla de clasificación!
        if (btnClasificacion != null) {
            btnClasificacion.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, ClasificacionActivity.class);
                startActivity(intent);
            });
        }


        if (btnPerfil != null) {
            btnPerfil.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, PerfilActivity.class);
                startActivity(intent);
            });
        }
        // Buscar el botón de la tienda en la barra inferior
        View btnNavTienda = findViewById(R.id.nav_tienda);

        if (btnNavTienda != null) {
            btnNavTienda.setOnClickListener(v -> {
                // Abrir la pantalla de Tienda
                android.content.Intent intent = new android.content.Intent(HomeActivity.this, TiendaActivity.class);
                startActivity(intent);

                // Opcional: Anula la animación por defecto de Android para que parezca
                // que cambias de pestaña sin que la pantalla "vuele" desde abajo
                overridePendingTransition(0, 0);
            });
        }
        View btnNavPersonalizacion = findViewById(R.id.nav_personalizar);
        if (btnNavPersonalizacion != null) {
            btnNavPersonalizacion.setOnClickListener(v -> {
                // Abrir la pantalla de Tienda
                android.content.Intent intent = new android.content.Intent(HomeActivity.this, PersonalizacionActivity.class);
                startActivity(intent);

                // Opcional: Anula la animación por defecto de Android para que parezca
                // que cambias de pestaña sin que la pantalla "vuele" desde abajo
                overridePendingTransition(0, 0);
            });
        }


    }

    // =========================================================
    // LÓGICA DEL MENÚ DESPLEGABLE DE LA ESQUINA
    // =========================================================
    private void mostrarMenuPersonalizado(View vistaBoton) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_menu_personalizado, null);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        Button btnMusica = popupView.findViewById(R.id.btn_opcion_musica);
        Button btnComoJugar = popupView.findViewById(R.id.btn_opcion_como_jugar);
        Button btnHistorial = popupView.findViewById(R.id.btn_opcion_historial);

        btnMusica.setOnClickListener(v -> {
            popupWindow.dismiss();
            mostrarDialogoMusica();
        });

        btnComoJugar.setOnClickListener(v -> {
            popupWindow.dismiss();
            mostrarDialogoComoJugar();
        });

        btnHistorial.setOnClickListener(v -> {
            popupWindow.dismiss();
            mostrarDialogoHistorial();
        });

        popupWindow.showAsDropDown(vistaBoton, -350, 10);
    }

    // =========================================================
    // MÉTODOS DE LOS POPUPS
    // =========================================================

    private void mostrarDialogoMusica() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_musica, null);
        AlertDialog dialog = crearDialogoBase(dialogView);

        ImageView btnCerrar = dialogView.findViewById(R.id.btn_cerrar_popup);
        btnCerrar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void mostrarDialogoComoJugar() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_como_jugar, null);
        AlertDialog dialog = crearDialogoBase(dialogView);

        ImageView btnCerrar = dialogView.findViewById(R.id.btn_cerrar_popup);
        btnCerrar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void mostrarDialogoHistorial() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_historial, null);
        AlertDialog dialog = crearDialogoBase(dialogView);

        ImageView btnCerrar = dialogView.findViewById(R.id.btn_cerrar_popup);
        btnCerrar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // =========================================================
    // MÉTODO AYUDANTE PARA EL FONDO REDONDEADO DE LOS POPUPS
    // =========================================================
    private AlertDialog crearDialogoBase(View dialogView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }
}