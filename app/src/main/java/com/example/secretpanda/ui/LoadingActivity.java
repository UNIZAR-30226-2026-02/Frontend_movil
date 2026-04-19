package com.example.secretpanda.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.secretpanda.R;
import com.example.secretpanda.ui.auth.UserSelectionActivity;
import com.example.secretpanda.ui.home.HomeActivity;

public class LoadingActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private int progressStatus = 0;

    private String idGoogleEstable;


    private String nombreUsuario;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pantalla_de_carga);
        nombreUsuario = getIntent().getStringExtra("MI_NOMBRE_USUARIO");

        progressBar = findViewById(R.id.progressBar);

        idGoogleEstable = getIntent().getStringExtra("GOOGLE_ID_ESTABLE");

        String destino = getIntent().getStringExtra("DESTINO");

        new Thread(() -> {
            while (progressStatus < 100) {
                progressStatus += 2;
                runOnUiThread(() -> progressBar.setProgress(progressStatus));
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Intent intent;
            if ("HOME".equals(destino)) {
                intent = new Intent(LoadingActivity.this, HomeActivity.class);
                intent.putExtra("GOOGLE_ID_ESTABLE", idGoogleEstable);
                intent.putExtra("MI_NOMBRE_USUARIO", nombreUsuario);

            } else {
                intent = new Intent(LoadingActivity.this, UserSelectionActivity.class);
                intent.putExtra("GOOGLE_ID_ESTABLE", idGoogleEstable);
                intent.putExtra("MI_NOMBRE_USUARIO", nombreUsuario);
            }
            startActivity(intent);
            finish();
        }).start();
    }
}