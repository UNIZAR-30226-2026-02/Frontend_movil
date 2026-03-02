package com.example.secretpanda.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.secretpanda.R;
import com.example.secretpanda.ui.LoadingActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        Button btnLogin = findViewById(R.id.button);

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, LoadingActivity.class);

            intent.putExtra("DESTINO", "ELEGIR_USUARIO");

            startActivity(intent);

            finish();
        });
    }
}