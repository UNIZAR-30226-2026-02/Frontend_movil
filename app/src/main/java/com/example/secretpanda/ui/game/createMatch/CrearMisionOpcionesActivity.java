package com.example.secretpanda.ui.game.createMatch;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.secretpanda.R;
import com.example.secretpanda.ui.game.waitingRoom.ConfiguracionMisionActivity;

public class CrearMisionOpcionesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_crear_mision_opciones);

        FrameLayout btnVolver = findViewById(R.id.btn_volver_home_crear);
        LinearLayout btnPrivada = findViewById(R.id.tarjeta_crear_privada);
        LinearLayout btnPublica = findViewById(R.id.tarjeta_crear_publica);

        btnVolver.setOnClickListener(v -> finish());

        // Si es privada, le pasamos "esPrivada" = true a la siguiente pantalla
        btnPrivada.setOnClickListener(v -> {
            Intent intent = new Intent(this, ConfiguracionMisionActivity.class);
            intent.putExtra("ES_PRIVADA", true);
            startActivity(intent);
        });

        // Si es pública, le pasamos "esPrivada" = false
        btnPublica.setOnClickListener(v -> {
            Intent intent = new Intent(this, ConfiguracionMisionActivity.class);
            intent.putExtra("ES_PRIVADA", false);
            startActivity(intent);
        });
    }
}