package com.example.secretpanda.ui.game.join; // Asegúrate de cambiar esto por el nombre de tu paquete

import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.secretpanda.R;

public class UnirseMisionActivity extends AppCompatActivity {

    private LinearLayout tarjetaPrivada, tarjetaPublica;
    private FrameLayout btnHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unirse_mision);

        // 1. Inicializar los componentes
        btnHome = findViewById(R.id.btn_volver_home);
        tarjetaPrivada = findViewById(R.id.tarjeta_mision_privada);
        tarjetaPublica = findViewById(R.id.tarjeta_mision_publica);

        // 2. Configurar los clics
        configurarListeners();
    }

    private void configurarListeners() {

        // Clic en el botón Home (Volver atrás)
        btnHome.setOnClickListener(v -> {
            // Cierra esta actividad y vuelve a la anterior
            finish();
        });

        // Clic en Misión Privada
        tarjetaPrivada.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(UnirseMisionActivity.this, UnirsePrivadaActivity.class);
            startActivity(intent);

            // Opcional: Anula la animación por defecto de Android para que parezca
            // que cambias de pestaña sin que la pantalla "vuele" desde abajo
            overridePendingTransition(0, 0);

        });

        // Clic en Misión Pública
        tarjetaPublica.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(UnirseMisionActivity.this, MisionPublicaActivity.class);
            startActivity(intent);

            // Opcional: Anula la animación por defecto de Android para que parezca
            // que cambias de pestaña sin que la pantalla "vuele" desde abajo
            overridePendingTransition(0, 0);

        });
    }
}
