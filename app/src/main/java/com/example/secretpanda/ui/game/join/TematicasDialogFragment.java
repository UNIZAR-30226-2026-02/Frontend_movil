package com.example.secretpanda.ui.game.join;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secretpanda.R;

import java.util.ArrayList;
import java.util.List;

public class TematicasDialogFragment extends DialogFragment {

    private boolean soloDesbloqueadas = true;
    private boolean mostrarBotonTodas = true;

    // 🌟 NUEVO: Variable para guardar la lista que nos pase la Activity
    private List<String> misTematicas = new ArrayList<>();

    public interface TematicaListener {
        void onTematicaSelected(String tematica);
    }

    private TematicaListener listener;

    public void setTematicaListener(TematicaListener listener) {
        this.listener = listener;
    }

    public void setConfiguracionFiltros(boolean soloDesbloqueadas, boolean mostrarBotonTodas) {
        this.soloDesbloqueadas = soloDesbloqueadas;
        this.mostrarBotonTodas = mostrarBotonTodas;
    }
    public void setMostrarOpcionTodas(boolean mostrar) {
        this.mostrarBotonTodas = mostrar;
    }

    // 🌟 NUEVO: Método para inyectar los datos a la velocidad de la luz
    public void setMisTematicas(List<String> tematicas) {
        this.misTematicas = tematicas;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_tematicas, container, false);

        View btnOpcionTodas = view.findViewById(R.id.opcion_todas);

        if (mostrarBotonTodas) {
            btnOpcionTodas.setVisibility(View.VISIBLE);
            btnOpcionTodas.setOnClickListener(v -> {
                if (listener != null) listener.onTematicaSelected("Todas las temáticas");
                dismiss();
            });
        } else {
            btnOpcionTodas.setVisibility(View.GONE);
        }

        // Cargamos el RecyclerView con la lista de temas inyectada
        RecyclerView rvTematicas = view.findViewById(R.id.rv_tematicas_grid);
        rvTematicas.setLayoutManager(new GridLayoutManager(getContext(), 2));

        TematicaAdapter adapter = new TematicaAdapter(misTematicas, listener, this);
        rvTematicas.setAdapter(adapter);

        LinearLayout opcionTodas = view.findViewById(R.id.opcion_todas);
        if (!mostrarBotonTodas) {
            opcionTodas.setVisibility(View.GONE);
        } else {
            opcionTodas.setOnClickListener(v -> {
                if (listener != null) listener.onTematicaSelected("Todas las temáticas");
                dismiss();
            });
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            int anchoEnPixeles = (int) (340 * getResources().getDisplayMetrics().density);
            dialog.getWindow().setLayout(
                    anchoEnPixeles,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
    }
}