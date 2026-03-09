package com.example.secretpanda.ui;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.secretpanda.R;

public class TematicasDialogFragment extends DialogFragment {

    public interface TematicaListener {
        void onTematicaSelected(String tematica);
    }

    private TematicaListener listener;

    public void setTematicaListener(TematicaListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_tematicas, container, false);

        // 1. Configurar botón de "Todas las temáticas"
        view.findViewById(R.id.opcion_todas).setOnClickListener(v -> {
            if (listener != null) listener.onTematicaSelected("Todas las temáticas");
            dismiss();
        });

        // 2. HARDCODEAMOS LAS TEMÁTICAS PARA EL FUTURO
        java.util.List<String> misTematicas = java.util.Arrays.asList(
                "Coches",
                "Motos",
                "Perros",
                "Gatos",
                "Cine",      // <--- Puedes añadir todas las que quieras
                "Deportes"   // <--- Se irán colocando solas en la cuadrícula
        );

        // 3. Configuramos el RecyclerView en modo Cuadrícula (Grid) de 2 columnas
        androidx.recyclerview.widget.RecyclerView rvTematicas = view.findViewById(R.id.rv_tematicas_grid);
        rvTematicas.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(getContext(), 2));

        // 4. Asignamos el adaptador
        TematicaAdapter adapter = new TematicaAdapter(misTematicas, listener, this);
        rvTematicas.setAdapter(adapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {

            // 1. Calculamos 340dp y los convertimos a píxeles exactos para tu pantalla
            int anchoEnPixeles = (int) (340 * getResources().getDisplayMetrics().density);

            // 2. Le forzamos el ANCHO FIJO y el alto que envuelva el contenido
            dialog.getWindow().setLayout(
                    anchoEnPixeles,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );

            // 3. Fondo transparente para los bordes redondeados
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
    }

    private void setupCard(View view, String nombre) {
        TextView tv = view.findViewById(R.id.tv_nombre_tematica);
        tv.setText(nombre);
        view.setOnClickListener(v -> {
            if (listener != null) listener.onTematicaSelected(nombre);
            dismiss();
        });
    }

    private void configurarItem(View view, String nombre) {
        TextView tv = view.findViewById(R.id.tv_nombre_tematica);
        tv.setText(nombre);
        view.setOnClickListener(v -> seleccionar(nombre));
    }

    private void seleccionar(String nombre) {
        if (listener != null) listener.onTematicaSelected(nombre);
        dismiss(); // Se cierra al seleccionar
    }
}
