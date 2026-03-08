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

        // Configuramos los textos de la rejilla
        setupCard(view.findViewById(R.id.card_coches), "Coches");
        setupCard(view.findViewById(R.id.card_motos), "Motos");
        setupCard(view.findViewById(R.id.card_perros), "Perros");
        setupCard(view.findViewById(R.id.card_gatos), "Gatos");

        view.findViewById(R.id.opcion_todas).setOnClickListener(v -> {
            if (listener != null) listener.onTematicaSelected("Todas las temáticas");
            dismiss();
        });

        return view;
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
