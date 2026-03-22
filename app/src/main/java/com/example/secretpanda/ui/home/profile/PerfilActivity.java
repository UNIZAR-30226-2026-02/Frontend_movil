package com.example.secretpanda.ui.home.profile;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secretpanda.R;
import com.example.secretpanda.data.model.Jugador;
import com.example.secretpanda.data.model.GestorEstadisticas;
import com.example.secretpanda.ui.auth.LoginActivity;
import java.util.List;

public class PerfilActivity extends AppCompatActivity {

    private RecyclerView recyclerAmigos;
    private TextView tabAmigos, tabDatos;
    private LinearLayout layoutDatos;
    private FrameLayout layoutAmigos;
    private Button btnCerrarSesion;
    private View layoutDetalleAmigo;
    private TextView textoNombreDetalleAmigo;
    private ImageView btnCerrarDetalleAmigo;
    private Jugador jugadorActual;
    private TextView textoNombreDatos;
    private LinearLayout layoutListaAmigosContenedor;
    private TextView btnGestionarSolicitudes;
    private AmigoAdapter adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        layoutListaAmigosContenedor = findViewById(R.id.layout_lista_amigos_contenedor);
        btnGestionarSolicitudes = findViewById(R.id.btn_gestionar_solicitudes);
        layoutDetalleAmigo = findViewById(R.id.layout_detalle_amigo);

        btnGestionarSolicitudes.setOnClickListener(v -> {
            Intent intent = new Intent(PerfilActivity.this, SolicitudesActivity.class);
            startActivity(intent);
        });

        btnCerrarDetalleAmigo = findViewById(R.id.btn_cerrar_detalle_amigo);
        btnCerrarDetalleAmigo.setOnClickListener(v -> {
            layoutDetalleAmigo.setVisibility(View.GONE);
            layoutListaAmigosContenedor.setVisibility(View.VISIBLE);
        });

        recyclerAmigos = findViewById(R.id.recycler_amigos);
        recyclerAmigos.setLayoutManager(new LinearLayoutManager(this));

        // TUS AMIGOS FALSOS INICIALES
        java.util.List<Jugador> misAmigos = new java.util.ArrayList<>();
        misAmigos.add(new Jugador("NinjaMaster"));
        misAmigos.add(new Jugador("BambooHunter"));

        adaptador = new AmigoAdapter(misAmigos, amigoClickado -> {
            mostrarDetalleDe(amigoClickado);
        });
        recyclerAmigos.setAdapter(adaptador);

        layoutAmigos = findViewById(R.id.layout_amigos);
        layoutDatos = findViewById(R.id.layout_datos);

        tabAmigos = findViewById(R.id.tab_amigos);
        tabDatos = findViewById(R.id.tab_datos);

        textoNombreDatos = findViewById(R.id.texto_nombre_datos);

        String nombreRecibido = getIntent().getStringExtra("NOMBRE_JUGADOR");
        if (nombreRecibido != null && !nombreRecibido.isEmpty()) {
            textoNombreDatos.setText(nombreRecibido);
        } else {
            textoNombreDatos.setText("Espía Secreto");
        }

        ImageView btnCerrar = findViewById(R.id.btn_cerrar_perfil);
        btnCerrarSesion = findViewById(R.id.btn_cerrar_sesion);

        btnCerrar.setOnClickListener(v -> finish());

        tabDatos.setOnClickListener(v -> {
            layoutAmigos.setVisibility(View.GONE);
            layoutDatos.setVisibility(View.VISIBLE);
            tabDatos.setBackgroundResource(R.drawable.tab_selected);
            tabDatos.setTextColor(Color.parseColor("#555555"));
            tabAmigos.setBackgroundResource(R.drawable.tab_unselected);
            tabAmigos.setTextColor(Color.WHITE);
        });

        tabAmigos.setOnClickListener(v -> {
            layoutDatos.setVisibility(View.GONE);
            layoutAmigos.setVisibility(View.VISIBLE);
            tabAmigos.setBackgroundResource(R.drawable.tab_selected);
            tabAmigos.setTextColor(Color.parseColor("#555555"));
            tabDatos.setBackgroundResource(R.drawable.tab_unselected);
            tabDatos.setTextColor(Color.WHITE);
        });

        btnCerrarSesion.setOnClickListener(v -> {
            //  DESCONECTAR DE GOOGLE SIGN-IN
            com.google.android.gms.auth.api.signin.GoogleSignInOptions gso =
                    new com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN).build();
            com.google.android.gms.auth.api.signin.GoogleSignInClient googleClient =
                    com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this, gso);

            googleClient.signOut().addOnCompleteListener(this, task -> {

                // BORRAR EL JWT LOCAL (TokenManager)
                com.example.secretpanda.data.TokenManager tokenManager = new com.example.secretpanda.data.TokenManager(this);
                tokenManager.clearToken();

                //  REDIRIGIR AL LOGIN LIMPIANDO EL HISTORIAL DE PANTALLAS
                android.content.Intent intent = new android.content.Intent(PerfilActivity.this, com.example.secretpanda.ui.auth.LoginActivity.class);
                intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        });

        ImageView btnEditarPerfil = findViewById(R.id.btn_editar_perfil);
        btnEditarPerfil.setOnClickListener(v -> mostrarDialogoEditar());
    }

    private void mostrarDialogoEditar() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_editar_perfil, null);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setView(dialogView);
        android.app.AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        ImageView btnCerrar = dialogView.findViewById(R.id.btn_cerrar_editar);
        Button btnGuardar = dialogView.findViewById(R.id.btn_guardar_cambios);
        ImageView btnCambiarFoto = dialogView.findViewById(R.id.btn_cambiar_foto);
        android.widget.EditText inputNombre = dialogView.findViewById(R.id.input_editar_nombre);

        inputNombre.setText(textoNombreDatos.getText().toString());

        btnCerrar.setOnClickListener(v -> dialog.dismiss());
        btnCambiarFoto.setOnClickListener(v -> mostrarDialogoElegirImagen(btnCambiarFoto));

        btnGuardar.setOnClickListener(v -> {
            String nuevoNombre = inputNombre.getText().toString().trim();
            if (!nuevoNombre.isEmpty()) {
                textoNombreDatos.setText(nuevoNombre);
                Intent intentDeVuelta = new Intent();
                intentDeVuelta.putExtra("NOMBRE_ACTUALIZADO", nuevoNombre);
                setResult(RESULT_OK, intentDeVuelta);

                android.widget.Toast.makeText(this, "Perfil actualizado", android.widget.Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                inputNombre.setError("El nombre no puede estar vacío");
            }
        });
        dialog.show();
    }

    private void mostrarDialogoElegirImagen(ImageView imagenPerfilActual) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_elegir_imagen, null);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setView(dialogView);
        android.app.AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        ImageView btnCerrar = dialogView.findViewById(R.id.btn_cerrar_elegir_imagen);
        RecyclerView recyclerImagenes = dialogView.findViewById(R.id.recycler_elegir_imagen);
        recyclerImagenes.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 3));

        java.util.List<Integer> misImagenes = new java.util.ArrayList<>();
        for (int i = 0; i < 60; i++) misImagenes.add(R.mipmap.ic_launcher);

        ImagenPerfilAdapter adaptador = new ImagenPerfilAdapter(misImagenes, recursoImagen -> {
            imagenPerfilActual.setImageResource(recursoImagen);
            dialog.dismiss();
            android.widget.Toast.makeText(this, "Imagen seleccionada", android.widget.Toast.LENGTH_SHORT).show();
        });
        recyclerImagenes.setAdapter(adaptador);
        btnCerrar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void mostrarDetalleDe(Jugador amigo) {
        TextView textoNombreAmigo = findViewById(R.id.texto_nombre_detalle_amigo);
        if (textoNombreAmigo != null && amigo != null) {
            textoNombreAmigo.setText(amigo.getTag());
        }

        // ===============================================
        // MAGIA: ESTADÍSTICAS MATEMÁTICAS DEL AMIGO
        // ===============================================

        int partidasAmigo = 45;
        int victoriasAmigo = 25;
        int derrotasAmigo = partidasAmigo - victoriasAmigo;
        int balasAmigo = (int)(Math.random() * 2000);
        float winrateAmigo = partidasAmigo > 0 ? ((float) victoriasAmigo / partidasAmigo) * 100 : 0f;

        TextView txtAmigoPartidas = findViewById(R.id.stat_amigo_partidas);
        TextView txtAmigoWinrate = findViewById(R.id.stat_amigo_winrate);
        TextView txtAmigoVictorias = findViewById(R.id.stat_amigo_victorias);
        TextView txtAmigoDerrotas = findViewById(R.id.stat_amigo_derrotas);
        TextView txtBalasAmigo = findViewById(R.id.stat_amigo_balas);
        if (txtBalasAmigo != null) txtBalasAmigo.setText(String.valueOf(balasAmigo));

        if (txtAmigoPartidas != null) txtAmigoPartidas.setText(String.valueOf(partidasAmigo));
        if (txtAmigoVictorias != null) txtAmigoVictorias.setText(String.valueOf(victoriasAmigo));
        if (txtAmigoDerrotas != null) txtAmigoDerrotas.setText(String.valueOf(derrotasAmigo));
        if (txtAmigoWinrate != null) txtAmigoWinrate.setText(String.format("%.1f%%", winrateAmigo));

        if (layoutListaAmigosContenedor != null && layoutDetalleAmigo != null) {
            layoutListaAmigosContenedor.setVisibility(View.GONE);
            layoutDetalleAmigo.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 1. ACTUALIZAR LISTA DE AMIGOS
        List<String> nombresNuevos = com.example.secretpanda.data.model.SocialGlobal.getInstance().getMisAmigos();
        java.util.List<Jugador> amigosConvertidos = new java.util.ArrayList<>();
        for(String nombre : nombresNuevos) {
            amigosConvertidos.add(new Jugador(nombre));
        }

        if (adaptador != null) {
            adaptador.setListaAmigos(amigosConvertidos);
            adaptador.notifyDataSetChanged();
        }

        // ===============================================
        // 2. MAGIA: TUS ESTADÍSTICAS MATEMÁTICAS REALES
        // ===============================================
        GestorEstadisticas stats = GestorEstadisticas.getInstance();

        int partidas = stats.getPartidasJugadas();
        int victorias = stats.getVictorias();
        int derrotas = partidas - victorias;
        float winrate = partidas > 0 ? ((float) victorias / partidas) * 100 : 0f;

        TextView txtMioPartidas = findViewById(R.id.stat_mio_partidas);
        TextView txtMioWinrate = findViewById(R.id.stat_mio_winrate);
        TextView txtMioVictorias = findViewById(R.id.stat_mio_victorias);
        TextView txtMioDerrotas = findViewById(R.id.stat_mio_derrotas);

        if (txtMioPartidas != null) txtMioPartidas.setText(String.valueOf(partidas));
        if (txtMioVictorias != null) txtMioVictorias.setText(String.valueOf(victorias));
        if (txtMioDerrotas != null) txtMioDerrotas.setText(String.valueOf(derrotas));
        if (txtMioWinrate != null) txtMioWinrate.setText(String.format("%.1f%%", winrate));
        int misBalas = com.example.secretpanda.data.model.GestorEstadisticas.getInstance().getJugadorActual().getBalas();

        TextView txtMisBalas = findViewById(R.id.texto_mis_balas);
        if (txtMisBalas != null) {
            txtMisBalas.setText(String.valueOf(misBalas));
        }
    }
}