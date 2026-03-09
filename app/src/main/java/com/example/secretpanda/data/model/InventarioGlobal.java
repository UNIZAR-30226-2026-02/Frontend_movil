package com.example.secretpanda.data.model;

import com.example.secretpanda.R;
import java.util.ArrayList;
import java.util.List;

public class InventarioGlobal {
    private static InventarioGlobal instance;
    private int misBalas = 1500;
    private List<ItemPersonalizacion> todosLosItems;

    private InventarioGlobal() {
        todosLosItems = new ArrayList<>();

        // BARAJAS
        todosLosItems.add(new ItemPersonalizacion("Clásica", false, "baraja", 0, 0));
        todosLosItems.add(new ItemPersonalizacion("Lápices", false, "baraja", 0, 0));
        todosLosItems.add(new ItemPersonalizacion("Neón", false, "baraja", 0, 0));
        // Estas van a la tienda (tienen precio y empiezan bloqueadas)
        todosLosItems.add(new ItemPersonalizacion("Flores", true, "baraja", 0, 500));
        todosLosItems.add(new ItemPersonalizacion("Fuego", true, "baraja", 0, 800));
        todosLosItems.add(new ItemPersonalizacion("Hielo", true, "baraja", 0, 1000));

        // BORDES
        todosLosItems.add(new ItemPersonalizacion("Madera", false, "borde", R.drawable.ic_menu, 0));
        todosLosItems.add(new ItemPersonalizacion("Metal", false, "borde", R.drawable.baseline_emoji_events_24, 0));
        todosLosItems.add(new ItemPersonalizacion("Oro", true, "borde", R.drawable.ic_costumize, 1200));
        todosLosItems.add(new ItemPersonalizacion("Diamante", true, "borde", R.drawable.ic_candado, 3000));

        // FONDOS
        todosLosItems.add(new ItemPersonalizacion("Océano", true, "fondo", R.drawable.ic_home, 600));
        todosLosItems.add(new ItemPersonalizacion("Selva", true, "fondo", R.drawable.ic_home, 750));
        todosLosItems.add(new ItemPersonalizacion("Volcán", true, "fondo", R.drawable.ic_home, 1500));
    }

    public static InventarioGlobal getInstance() {
        if (instance == null) {
            instance = new InventarioGlobal();
        }
        return instance;
    }

    public List<ItemPersonalizacion> getTodosLosItems() { return todosLosItems; }
    public int getMisBalas() { return misBalas; }
    public void restarBalas(int cantidad) { misBalas -= cantidad; }
}