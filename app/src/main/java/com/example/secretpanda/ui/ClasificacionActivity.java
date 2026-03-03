package com.example.secretpanda.ui; // Asegúrate de que tu package está bien

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secretpanda.R;
import com.example.secretpanda.data.model.Jugador;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClasificacionActivity extends AppCompatActivity {

    private TextView tabAmigos, tabGlobal;
    private RecyclerView listaClasificacion;
    private ClasificacionAdapter adapter;

    // Nuestras dos listas separadas
    private List<Jugador> listaGlobalFalsa = new ArrayList<>();
    private List<Jugador> listaAmigosFalsa = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Pantalla sin título superior
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_clasificacion);

        tabAmigos = findViewById(R.id.tab_amigos);
        tabGlobal = findViewById(R.id.tab_global);
        listaClasificacion = findViewById(R.id.lista_clasificacion);
        ImageView btnCerrar = findViewById(R.id.btn_cerrar_clasificacion);

        // 1. Configurar la lista (RecyclerView)
        listaClasificacion.setLayoutManager(new LinearLayoutManager(this));

        // Generamos los datos falsos y los cargamos
        generarDatosFalsos();
        adapter = new ClasificacionAdapter(listaAmigosFalsa); // Empezamos en Amigos
        listaClasificacion.setAdapter(adapter);

        // 2. Comportamiento de los botones
        tabAmigos.setOnClickListener(v -> cambiarPestana(tabAmigos, tabGlobal, listaAmigosFalsa));
        tabGlobal.setOnClickListener(v -> cambiarPestana(tabGlobal, tabAmigos, listaGlobalFalsa));

        // 3. Botón para salir
        btnCerrar.setOnClickListener(v -> finish()); // Cierra esta pantalla y vuelve al Home
    }

    // ==========================================
    // LÓGICA DE ANIMACIÓN DE LOS BOTONES
    // ==========================================
    private void cambiarPestana(TextView activa, TextView inactiva, List<Jugador> listaMostrar) {
        // Actualizamos los datos de la lista instantáneamente
        adapter.setListaJugadores(listaMostrar);

        // Cambiamos colores (Verde para la activa, Gris oscuro para la inactiva)
        activa.setBackgroundResource(R.drawable.bg_tab_activo);
        activa.setTextColor(Color.WHITE);

        inactiva.setBackgroundResource(R.drawable.bg_tab_inactivo);
        inactiva.setTextColor(Color.parseColor("#BBBBBB"));

        // ¡ANIMACIÓN! Modificamos la altura fluidamente en 200 milisegundos
        animarAltura(activa, 40, 55);
        animarAltura(inactiva, 55, 40);
    }

    private void animarAltura(TextView vista, int altoInicialDp, int altoFinalDp) {
        // Convertimos DP a Pixeles porque Java trabaja en pixeles
        float density = getResources().getDisplayMetrics().density;
        int inicialPx = (int) (altoInicialDp * density);
        int finalPx = (int) (altoFinalDp * density);

        ValueAnimator animator = ValueAnimator.ofInt(inicialPx, finalPx);
        animator.setDuration(200); // 0.2 segundos
        animator.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = vista.getLayoutParams();
            params.height = (int) animation.getAnimatedValue();
            vista.setLayoutParams(params);
        });
        animator.start();
    }

    // ==========================================
    // GENERAR 10 JUGADORES HARDCODEADOS
    // ==========================================
    private void generarDatosFalsos() {
        // Creamos 10 jugadores con datos aleatorios
        Jugador j1 = crearJugador("NinjaMaster", 250);
        Jugador j2 = crearJugador("PandaRex", 210);
        Jugador j3 = crearJugador("ElEspiaSupremo", 185);
        Jugador j4 = crearJugador("TuUsuario", 150); // Simula el usuario actual
        Jugador j5 = crearJugador("PeleteLover", 120);
        Jugador j6 = crearJugador("GatoFurtivo", 95);
        Jugador j7 = crearJugador("ShadowKiller", 70);
        Jugador j8 = crearJugador("BambooHunter", 55);
        Jugador j9 = crearJugador("NoobPlayer123", 20);
        Jugador j10 = crearJugador("BuscandoPistas", 5);

        // Los metemos todos en la global
        listaGlobalFalsa.add(j1); listaGlobalFalsa.add(j2); listaGlobalFalsa.add(j3);
        listaGlobalFalsa.add(j4); listaGlobalFalsa.add(j5); listaGlobalFalsa.add(j6);
        listaGlobalFalsa.add(j7); listaGlobalFalsa.add(j8); listaGlobalFalsa.add(j9);
        listaGlobalFalsa.add(j10);

        // Elegimos unos pocos para que sean tus "amigos" (Tú tienes que estar)
        listaAmigosFalsa.add(j1); // NinjaMaster
        listaAmigosFalsa.add(j4); // Tú
        listaAmigosFalsa.add(j5); // PeleteLover
        listaAmigosFalsa.add(j8); // BambooHunter

        // Ordenamos ambas listas de mayor a menor victorias (por si acaso)
        listaGlobalFalsa.sort((a, b) -> Integer.compare(b.getVictorias(), a.getVictorias()));
        listaAmigosFalsa.sort((a, b) -> Integer.compare(b.getVictorias(), a.getVictorias()));
    }

    // Pequeño ayudante para no escribir tanto
    private Jugador crearJugador(String nombre, int victorias) {
        Jugador j = new Jugador(nombre);
        // Simulamos sumarle las victorias (Como tu clase Jugador no tiene setVictorias, usamos un bucle o creamos el setter.
        // ¡Ojo! Si no tienes setVictorias en Jugador.java, lo añadiremos luego. Por ahora asumo que tienes un método así:
        for(int i=0; i<victorias; i++) j.sumarVictoria();
        return j;
    }
}