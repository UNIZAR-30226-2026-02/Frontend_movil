package com.example.secretpanda.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.secretpanda.R;
import com.example.secretpanda.data.model.Jugador; // ¡Importante importar el modelo!
import com.example.secretpanda.ui.home.HomeActivity;

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
            if (username.equalsIgnoreCase("panda") || username.equalsIgnoreCase("admin")) {
                Toast.makeText(this, "Ese usuario ya existe. ¡Elige otro!", Toast.LENGTH_SHORT).show();
                return; // Cortamos aquí, no avanza
            }

            // 3. CREAMOS EL JUGADOR con el "tag" que ha escrito
            Jugador nuevoJugador = new Jugador(username);

            // 4. Vamos a HomeActivity y le pasamos el objeto entero
            Intent intent = new Intent(UserSelectionActivity.this, HomeActivity.class);
            intent.putExtra("DATOS_JUGADOR", nuevoJugador);
            startActivity(intent);
            finish();
        });
    }
}