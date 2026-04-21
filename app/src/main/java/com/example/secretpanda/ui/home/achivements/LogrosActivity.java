package com.example.secretpanda.ui.home.achivements;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secretpanda.R;
import com.example.secretpanda.data.NetworkConfig;
import com.example.secretpanda.data.TokenManager;
import com.example.secretpanda.data.model.Logro;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LogrosActivity extends AppCompatActivity {

    private TextView tabLogros, tabMedallas;
    private RecyclerView recyclerLogros, recyclerMedallas;
    private ImageView btnCerrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_logros);

        tabLogros = findViewById(R.id.tab_logros);
        tabMedallas = findViewById(R.id.tab_medallas);
        recyclerLogros = findViewById(R.id.recycler_lista_logros);
        recyclerMedallas = findViewById(R.id.recycler_lista_medallas);
        btnCerrar = findViewById(R.id.btn_cerrar_logros);

        recyclerLogros.setLayoutManager(new LinearLayoutManager(this));
        recyclerMedallas.setLayoutManager(new LinearLayoutManager(this));

        btnCerrar.setOnClickListener(v -> finish());

        tabLogros.setOnClickListener(v -> {
            recyclerMedallas.setVisibility(View.GONE);
            recyclerLogros.setVisibility(View.VISIBLE);
            animarAltura(tabLogros, tabLogros.getHeight(), 55);
            animarAltura(tabMedallas, tabMedallas.getHeight(), 40);
        });

        tabMedallas.setOnClickListener(v -> {
            recyclerLogros.setVisibility(View.GONE);
            recyclerMedallas.setVisibility(View.VISIBLE);
            animarAltura(tabMedallas, tabMedallas.getHeight(), 55);
            animarAltura(tabLogros, tabLogros.getHeight(), 40);
        });

        // Llamamos al servidor al iniciar
        cargarDatosReales();
    }

    // ==========================================
    // CONEXIÓN CON LA BASE DE DATOS (Real)
    // ==========================================
    private void cargarDatosReales() {
        OkHttpClient client = new OkHttpClient();
        String url = NetworkConfig.BASE_URL + "/jugadores/logros";
        String token = new TokenManager(this).getToken();

        if (token == null) {
            Toast.makeText(this, "Error de sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    Log.e("API_LOGROS", "Fallo red: " + e.getMessage());
                    Toast.makeText(LogrosActivity.this, "Error conectando al servidor", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonData = response.body().string();
                        JSONArray array = new JSONArray(jsonData);

                        List<Logro> listaLogros = new ArrayList<>();
                        List<Logro> listaMedallas = new ArrayList<>();

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);

                            // Mapeamos los datos del JSON al objeto Logro
                            // Asegúrate de que tu modelo Logro tenga estos constructores o setters
                            Logro item = new Logro();
                            item.setNombre(obj.optString("nombre", ""));
                            item.setDescripcion(obj.optString("descripcion", ""));
                            item.setProgresoActual(obj.optInt("progreso_actual", 0));
                            item.setValorObjetivo(obj.optInt("progreso_max", 1));
                            item.setCompletado(obj.optBoolean("completado", false));
                            item.setBalasRecompensa(obj.optInt("balas_recompensa", 0));

                            // Separamos en la lista que toca según el boolean "es_logro"
                            boolean esLogro = obj.optBoolean("es_logro", true);

                            if (esLogro) {
                                item.setTipo("logro");
                                listaLogros.add(item);
                            } else {
                                item.setTipo("medalla");
                                listaMedallas.add(item);
                            }
                        }

                        // Actualizamos los adaptadores en el hilo principal
                        runOnUiThread(() -> {
                            recyclerLogros.setAdapter(new LogrosAdapter(listaLogros));
                            recyclerMedallas.setAdapter(new MedallasAdapter(listaMedallas));
                        });

                    } catch (Exception e) {
                        Log.e("API_LOGROS", "Error parseando el JSON: ", e);
                    }
                }
            }
        });
    }

    private void animarAltura(TextView vista, int altoInicialPx, int altoFinalDp) {
        float density = getResources().getDisplayMetrics().density;
        int finalPx = (int) (altoFinalDp * density);
        if (vista.getHeight() == finalPx) return;

        ValueAnimator animator = ValueAnimator.ofInt(vista.getHeight(), finalPx);
        animator.setDuration(200);
        animator.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = vista.getLayoutParams();
            params.height = (int) animation.getAnimatedValue();
            vista.setLayoutParams(params);
        });
        animator.start();
    }


    // ADAPTADORES

    private class LogrosAdapter extends RecyclerView.Adapter<LogrosAdapter.ViewHolder> {
        private List<Logro> items;
        LogrosAdapter(List<Logro> items) { this.items = items; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_logro, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Logro logro = items.get(position);
            holder.txtTitulo.setText(logro.getNombre());
            holder.txtMonedas.setText("+" + logro.getBalasRecompensa());

            // Lógica de progreso Real
            holder.barra.setMax(logro.getValorObjetivo());
            holder.barra.setProgress(logro.getProgresoActual());
            holder.txtDescripcion.setText(logro.getDescripcion());


            if (logro.isCompletado()) {
                holder.txtProgreso.setText("¡Completado!");
                holder.txtProgreso.setTextColor(Color.parseColor("#4CAF50")); // Verde
            } else {
                holder.txtProgreso.setText(logro.getProgresoActual() + "/" + logro.getValorObjetivo());
                holder.txtProgreso.setTextColor(Color.parseColor("#AAAAAA")); // Gris
            }
        }
        @Override public int getItemCount() { return items.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtTitulo, txtProgreso, txtMonedas, txtDescripcion; ProgressBar barra;
            ViewHolder(View v) {
                super(v);
                txtTitulo = v.findViewById(R.id.txt_titulo_logro);
                txtProgreso = v.findViewById(R.id.txt_progreso_logro);
                txtMonedas = v.findViewById(R.id.txt_recompensa_logro);
                txtDescripcion = v.findViewById(R.id.txt_descripcion_logro);
                barra = v.findViewById(R.id.barra_progreso_logro);
            }
        }
    }

    private class MedallasAdapter extends RecyclerView.Adapter<MedallasAdapter.ViewHolder> {
        private List<Logro> items;
        MedallasAdapter(List<Logro> items) { this.items = items; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medalla, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Logro medalla = items.get(position);
            holder.txtTitulo.setText(medalla.getNombre());

            // Lógica de progreso Real
            holder.barra.setMax(medalla.getValorObjetivo());
            holder.barra.setProgress(medalla.getProgresoActual());
            holder.txtDescripcion.setText(medalla.getDescripcion());

            if (medalla.isCompletado()) {
                holder.iconoCheck.setVisibility(View.VISIBLE);
                holder.txtProgreso.setText(medalla.getValorObjetivo() + "/" + medalla.getValorObjetivo());
                holder.txtProgreso.setTextColor(Color.parseColor("#FFC107")); // Dorado
            } else {
                holder.iconoCheck.setVisibility(View.GONE);
                holder.txtProgreso.setText(medalla.getProgresoActual() + "/" + medalla.getValorObjetivo());
                holder.txtProgreso.setTextColor(Color.parseColor("#AAAAAA"));
            }
        }
        @Override public int getItemCount() { return items.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtTitulo, txtProgreso, txtDescripcion; ProgressBar barra; ImageView iconoCheck;
            ViewHolder(View v) {
                super(v);
                txtTitulo = v.findViewById(R.id.txt_titulo_medalla);
                txtProgreso = v.findViewById(R.id.txt_progreso_medalla);
                txtDescripcion = v.findViewById(R.id.txt_descripcion_medalla);
                barra = v.findViewById(R.id.barra_progreso_medalla);
                iconoCheck = v.findViewById(R.id.icono_completado_medalla);
            }
        }
    }
}