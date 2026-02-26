package com.example.secretpanda;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Configuración de pantalla completa (Opcional, para estética de juego)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // 2. Establecer el layout (Asegúrate que el archivo se llame home.xml)
        setContentView(R.layout.home);

        // 3. Inicializar los botones principales
        Button btnNuevaMision = findViewById(R.id.btn_nueva_mision);
        Button btnUneteMision = findViewById(R.id.btn_unete_mision);

        // 4. Configurar eventos de clic
        btnNuevaMision.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí iría el Intent para cambiar de pantalla
                Toast.makeText(MainActivity.this, "Iniciando Nueva Misión...", Toast.LENGTH_SHORT).show();
            }
        });

        btnUneteMision.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lógica para unirse a partida
                Toast.makeText(MainActivity.this, "Buscando Misiones disponibles...", Toast.LENGTH_SHORT).show();
            }
        });

        // 5. Configurar los botones del Bottom Navigation (Opcional)
        // Si quieres que los botones de "Tienda" o "Personalizar" hagan algo,
        // deberás asignarles un ID en el XML y llamarlos aquí.
    }
}