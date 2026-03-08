package com.example.secretpanda.ui; // Asegúrate de que coincida con tu paquete real

import android.os.Bundle;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.secretpanda.R;

public class UnirsePrivadaActivity extends AppCompatActivity {

    private EditText etCodigoSala;
    private TextView btnUnirse;
    private FrameLayout btnHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unirse_privada);

        // 1. Inicializar las vistas
        etCodigoSala = findViewById(R.id.et_codigo_sala);
        btnUnirse = findViewById(R.id.btn_confirmar_union);
        btnHome = findViewById(R.id.btn_volver_home);

        // 2. Configurar los eventos de clic
        configurarListeners();
    }

    private void configurarListeners() {

        // Botón para volver atrás (Home)
        btnHome.setOnClickListener(v -> {
            finish(); // Cierra esta actividad y vuelve a la anterior (UnirseMisionActivity)
        });

        // Botón de confirmar unión
        btnUnirse.setOnClickListener(v -> {
            String codigo = etCodigoSala.getText().toString().trim();

            if (codigo.isEmpty()) {
                // Validación básica: si el campo está vacío
                etCodigoSala.setError("Debes introducir un código");
                Toast.makeText(this, "Por favor, escribe el código de la sala", Toast.LENGTH_SHORT).show();
            } else {
                // Aquí iría tu lógica de conexión al servidor para validar el código
                conectarASalaPrivada(codigo);
            }
        });
    }

    private void conectarASalaPrivada(String codigo) {
        // Por ahora, simulamos que estamos buscando la sala
        Toast.makeText(this, "Conectando a la sala: " + codigo, Toast.LENGTH_LONG).show();

        /* EN EL FUTURO:
           - Enviar el código a Firebase/Backend.
           - Si es correcto, navegar a la sala de espera (Lobby).
           - Si es incorrecto, mostrar error.
        */
    }
}
