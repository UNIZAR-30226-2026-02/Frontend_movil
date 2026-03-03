package com.example.secretpanda.ui.home;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.secretpanda.R;
import com.example.secretpanda.data.model.Jugador; // ¡Importante!

public class HomeActivity extends AppCompatActivity {

    // Guardamos el jugador a nivel de clase por si lo queremos usar en los popups
    private Jugador jugadorActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuración de pantalla completa
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.home);

        // =========================================================
        // 1. RECIBIR EL JUGADOR Y CAMBIAR EL SALUDO
        // =========================================================
        TextView textoSaludo = findViewById(R.id.texto_saludo_home); // Añade este ID en tu XML

        // Sacamos al jugador de la mochila
        jugadorActual = (Jugador) getIntent().getSerializableExtra("DATOS_JUGADOR");
        String nombreMostrar = "Espía Secreto";

        if (jugadorActual != null) {
            nombreMostrar = jugadorActual.getTag(); // Extraemos el tag (nombre)
        }

        // Si hemos encontrado el TextView, le ponemos el nombre real
        if (textoSaludo != null) {
            textoSaludo.setText("Hola, " + nombreMostrar);
        }

        // =========================================================
        // 2. CONFIGURAR BOTONES PRINCIPALES
        // =========================================================
        Button btnNuevaMision = findViewById(R.id.btn_nueva_mision);
        Button btnUneteMision = findViewById(R.id.btn_unete_mision);
        ImageView btnMenuOpciones = findViewById(R.id.btn_menu_opciones); // El botón de arriba a la derecha

        btnNuevaMision.setOnClickListener(v -> Toast.makeText(HomeActivity.this, "Iniciando Nueva Misión...", Toast.LENGTH_SHORT).show());
        btnUneteMision.setOnClickListener(v -> Toast.makeText(HomeActivity.this, "Buscando Misiones disponibles...", Toast.LENGTH_SHORT).show());

        // Evento del botón de menú superior
        btnMenuOpciones.setOnClickListener(v -> mostrarMenuPersonalizado(v));
    }

    // =========================================================
    // 3. LÓGICA DEL MENÚ DESPLEGABLE
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
    // 4. MÉTODOS DE LOS POPUPS (Aquí programarás lo de dentro)
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

        // ¡Magia! En un futuro aquí puedes acceder a: jugadorActual.getPartidasJugadas()

        dialog.show();
    }

    // =========================================================
    // 5. MÉTODO AYUDANTE PARA EL FONDO DE LOS POPUPS
    // =========================================================
    private AlertDialog crearDialogoBase(View dialogView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            // Esto quita el fondo blanco feo de Android y muestra tu fondo redondeado
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }
}