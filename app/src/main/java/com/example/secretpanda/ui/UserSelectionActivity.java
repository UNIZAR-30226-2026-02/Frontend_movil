package com.example.secretpanda.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.secretpanda.R;

public class UserSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eleccion_nombre_usuario);

        EditText inputUsuario = findViewById(R.id.inputUsuario);
        Button btnAceptar = findViewById(R.id.btnAceptar);

        btnAceptar.setOnClickListener(v -> {
            String username = inputUsuario.getText().toString().trim();

            // 1. Lógica: Mínimo 4 caracteres
            if (username.length() < 4) {
                Toast.makeText(this, "El nombre debe tener al menos 4 caracteres", Toast.LENGTH_SHORT).show();
                return; // Cortamos aquí, no avanza
            }

            // 2. Lógica: Usuario único (Simulación)
            // Si pone "panda" o "admin", le da error
            if (username.equalsIgnoreCase("panda") || username.equalsIgnoreCase("admin")) {
                Toast.makeText(this, "Ese usuario ya existe. ¡Elige otro!", Toast.LENGTH_SHORT).show();
                return; // Cortamos aquí, no avanza
            }

            // 3. Éxito: Volvemos a la pantalla de carga, pero destino HOME
            Intent intent = new Intent(UserSelectionActivity.this, LoadingActivity.class);
            intent.putExtra("DESTINO", "HOME");
            startActivity(intent);
            finish();
        });
    }
}