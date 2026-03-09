package com.example.secretpanda.ui;

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
import com.example.secretpanda.ui.auth.LoginActivity;
import java.util.List;

public class PerfilActivity extends AppCompatActivity {

    private RecyclerView recyclerAmigos;
    private TextView tabAmigos, tabDatos;
    private LinearLayout layoutDatos;
    private FrameLayout layoutAmigos;
    private Button btnCerrarSesion;
    // Añade esto junto a tus otras variables
    private View layoutDetalleAmigo;
    private TextView textoNombreDetalleAmigo;
    private ImageView btnCerrarDetalleAmigo;
    private Jugador jugadorActual; // Añade esta línea
    private TextView textoNombreDatos; // Añade esta
    private LinearLayout layoutListaAmigosContenedor;
    private TextView btnGestionarSolicitudes;
    private AmigoAdapter adaptador;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Referencias a los contenedores
        // Enlazamos las nuevas vistas
        // Enlazar las vistas
        layoutListaAmigosContenedor = findViewById(R.id.layout_lista_amigos_contenedor);
        btnGestionarSolicitudes = findViewById(R.id.btn_gestionar_solicitudes);
        layoutDetalleAmigo = findViewById(R.id.layout_detalle_amigo);
        // Darle una acción al botón nuevo (de momento un aviso)
        btnGestionarSolicitudes.setOnClickListener(v -> {
            Intent intent = new Intent(PerfilActivity.this, SolicitudesActivity.class);
            startActivity(intent);
        });

        // ACTUAIZAR EL BOTÓN 'X' (Para volver del detalle)
        btnCerrarDetalleAmigo = findViewById(R.id.btn_cerrar_detalle_amigo);
        btnCerrarDetalleAmigo.setOnClickListener(v -> {
            layoutDetalleAmigo.setVisibility(View.GONE);
            // ¡Ojo aquí! Ahora mostramos el contenedor entero, no solo el recycler
            layoutListaAmigosContenedor.setVisibility(View.VISIBLE);
        });

        // Configurar la lista de amigos...
        recyclerAmigos = findViewById(R.id.recycler_amigos);
        recyclerAmigos.setLayoutManager(new LinearLayoutManager(this));

        // TUS AMIGOS FALSOS (el código que ya tenías)
        java.util.List<Jugador> misAmigos = new java.util.ArrayList<>();
        misAmigos.add(new Jugador("NinjaMaster"));
        misAmigos.add(new Jugador("NinjaMaster"));
        misAmigos.add(new Jugador("BambooHunter"));
        misAmigos.add(new Jugador("PandaLoco"));
        misAmigos.add(new Jugador("Espía Experto"));
        misAmigos.add(new Jugador("JugadorPro99"));
        misAmigos.add(new Jugador("SombrasNuevas"));
        misAmigos.add(new Jugador("Agente 007"));
        misAmigos.add(new Jugador("Sr. Misterio")); // Añadimos suficientes para que la pantalla haga scroll


        // ¡ATENCIÓN A ESTA PARTE! Pasamos el listener al adaptador
        // ¡ATENCIÓN A ESTA PARTE! Ahora lo guardamos en la variable global
        adaptador = new AmigoAdapter(misAmigos, amigoClickado -> {
            mostrarDetalleDe(amigoClickado);
        });
        recyclerAmigos.setAdapter(adaptador);

        layoutAmigos = findViewById(R.id.layout_amigos);
        layoutDatos = findViewById(R.id.layout_datos);

        // Referencias a las pestañas
        tabAmigos = findViewById(R.id.tab_amigos);
        tabDatos = findViewById(R.id.tab_datos);

        textoNombreDatos = findViewById(R.id.texto_nombre_datos);

        // 1. RECIBIMOS EL NOMBRE COMO STRING
        String nombreRecibido = getIntent().getStringExtra("NOMBRE_JUGADOR");

        // 2. LO MOSTRAMOS EN PANTALLA
        if (nombreRecibido != null && !nombreRecibido.isEmpty()) {
            textoNombreDatos.setText(nombreRecibido);
        } else {
            // Por si acaso llega vacío
            textoNombreDatos.setText("Espía Secreto");
        }
        // Referencia a listas y botones
        recyclerAmigos = findViewById(R.id.recycler_amigos);
        ImageView btnCerrar = findViewById(R.id.btn_cerrar_perfil);
        btnCerrarSesion = findViewById(R.id.btn_cerrar_sesion);

        // Configuración inicial de RecyclerView (Pendiente de tu Adapter)
        recyclerAmigos.setLayoutManager(new LinearLayoutManager(this));

        // Cerrar el perfil
        btnCerrar.setOnClickListener(v -> finish());

        // Lógica: Clic en "Datos"
        tabDatos.setOnClickListener(v -> {
            // Cambiar vista
            layoutAmigos.setVisibility(View.GONE);
            layoutDatos.setVisibility(View.VISIBLE);

            // Cambiar estilos de pestañas (Fondos y Textos)
            tabDatos.setBackgroundResource(R.drawable.tab_selected);
            tabDatos.setTextColor(Color.parseColor("#555555")); // Gris oscuro

            tabAmigos.setBackgroundResource(R.drawable.tab_unselected);
            tabAmigos.setTextColor(Color.WHITE); // Blanco
        });

        // Lógica: Clic en "Amigos"
        tabAmigos.setOnClickListener(v -> {
            // Cambiar vista
            layoutDatos.setVisibility(View.GONE);
            layoutAmigos.setVisibility(View.VISIBLE);

            // Cambiar estilos de pestañas (Fondos y Textos)
            tabAmigos.setBackgroundResource(R.drawable.tab_selected);
            tabAmigos.setTextColor(Color.parseColor("#555555"));

            tabDatos.setBackgroundResource(R.drawable.tab_unselected);
            tabDatos.setTextColor(Color.WHITE);
        });

        // Lógica: Botón Cerrar Sesión
        btnCerrarSesion.setOnClickListener(v -> {
            // Te devuelve al Login limpiando el historial de pantallas
            Intent intent = new Intent(PerfilActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        // Justo donde tenías las referencias a tus otros botones:
        ImageView btnEditarPerfil = findViewById(R.id.btn_editar_perfil);

// Al pulsar el lápiz, abrimos el popup
        btnEditarPerfil.setOnClickListener(v -> mostrarDialogoEditar());

    }
    private void mostrarDialogoEditar() {
        // 1. Inflamos el diseño XML que acabamos de crear
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_editar_perfil, null);

        // 2. Construimos el AlertDialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setView(dialogView);
        android.app.AlertDialog dialog = builder.create();

        // 3. Fondo transparente para que se vean nuestras esquinas redondeadas
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // 4. Referencias a los botones DENTRO del diálogo
        ImageView btnCerrar = dialogView.findViewById(R.id.btn_cerrar_editar);
        Button btnGuardar = dialogView.findViewById(R.id.btn_guardar_cambios);
        ImageView btnCambiarFoto = dialogView.findViewById(R.id.btn_cambiar_foto);
        android.widget.EditText inputNombre = dialogView.findViewById(R.id.input_editar_nombre);

        // NUEVO: Que el cuadro de texto muestre el nombre que ya tienes puesto en la pantalla
        inputNombre.setText(textoNombreDatos.getText().toString());

        // 5. Lógica de los botones
        btnCerrar.setOnClickListener(v -> dialog.dismiss());

        // DENTRO DE mostrarDialogoEditar()
        btnCambiarFoto.setOnClickListener(v -> {
            // En vez del Toast, abrimos el nuevo diálogo y le pasamos la imagen a actualizar
            mostrarDialogoElegirImagen(btnCambiarFoto);
        });

        btnGuardar.setOnClickListener(v -> {
            String nuevoNombre = inputNombre.getText().toString().trim();

            if (!nuevoNombre.isEmpty()) {
                textoNombreDatos.setText(nuevoNombre); // Cambia en pantalla

                // DEVOLVEMOS EL STRING A HOMEACTIVITY
                Intent intentDeVuelta = new Intent();
                intentDeVuelta.putExtra("NOMBRE_ACTUALIZADO", nuevoNombre);
                setResult(RESULT_OK, intentDeVuelta);

                android.widget.Toast.makeText(this, "Perfil actualizado", android.widget.Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                inputNombre.setError("El nombre no puede estar vacío");
            }
        });

        // 6. ¡Lo mostramos!
        dialog.show();
    }
    private void mostrarDialogoElegirImagen(ImageView imagenPerfilActual) {
        // 1. Inflamos el nuevo diseño
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_elegir_imagen, null);

        // 2. Construimos el AlertDialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setView(dialogView);
        android.app.AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // 3. Referencias
        ImageView btnCerrar = dialogView.findViewById(R.id.btn_cerrar_elegir_imagen);
        RecyclerView recyclerImagenes = dialogView.findViewById(R.id.recycler_elegir_imagen);

        // 4. Configurar RecyclerView como Cuadrícula (Grid) de 3 columnas
        recyclerImagenes.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 3));

        // 5. Crear MUCHAS imágenes de prueba para forzar el scroll
        java.util.List<Integer> misImagenes = new java.util.ArrayList<>();

        // Vamos a generar 60 imágenes de prueba con un bucle
        for (int i = 0; i < 60; i++) {
            misImagenes.add(R.mipmap.ic_launcher); // Cambia esto por tus R.drawable... cuando tengas las reales
        }

        // 6. Configurar el adaptador
        ImagenPerfilAdapter adaptador = new ImagenPerfilAdapter(misImagenes, recursoImagen -> {
            // Cuando hacen click en una imagen de la cuadrícula:
            // a) Cambiamos la foto en el diálogo de edición (fondo o recurso)
            imagenPerfilActual.setImageResource(recursoImagen);
            // b) Cerramos el diálogo de selección
            dialog.dismiss();
            android.widget.Toast.makeText(this, "Imagen seleccionada", android.widget.Toast.LENGTH_SHORT).show();
        });
        recyclerImagenes.setAdapter(adaptador);

        // 7. Cerrar con la X
        btnCerrar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
    private void mostrarDetalleDe(Jugador amigo) {
        // 1. Buscamos el TextView del nombre con su NUEVO ID
        TextView textoNombreAmigo = findViewById(R.id.texto_nombre_detalle_amigo);

        // 2. Le ponemos el nombre del amigo (asegurándonos de que no sea nulo)
        if (textoNombreAmigo != null && amigo != null) {
            textoNombreAmigo.setText(amigo.getTag());
        }

        // (Opcional) Si quieres poner las victorias u otros datos en las cajas nuevas,
        // tendrías que enlazarlos aquí también, por ejemplo:
        // TextView statVictorias = findViewById(R.id.stat_victorias_1);
        // statVictorias.setText(String.valueOf(amigo.getVictorias()));

        // 3. Ocultamos el contenedor de la lista y mostramos el detalle
        if (layoutListaAmigosContenedor != null && layoutDetalleAmigo != null) {
            layoutListaAmigosContenedor.setVisibility(View.GONE);
            layoutDetalleAmigo.setVisibility(View.VISIBLE);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

        // Pedimos la lista fresca a la "Base de Datos" (ahora devuelve Strings)
        List<String> nombresNuevos = com.example.secretpanda.data.model.SocialGlobal.getInstance().getMisAmigos();

        // Convertimos esos Strings en objetos Jugador para que tu adaptador no se queje
        java.util.List<Jugador> amigosConvertidos = new java.util.ArrayList<>();
        for(String nombre : nombresNuevos) {
            amigosConvertidos.add(new Jugador(nombre));
        }

        if (adaptador != null) {
            adaptador.setListaAmigos(amigosConvertidos);
            adaptador.notifyDataSetChanged();
        }
    }
}