package com.example.secretpanda.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.secretpanda.R;
import com.example.secretpanda.data.model.InventarioGlobal;
import com.example.secretpanda.data.model.ItemPersonalizacion;

import java.util.ArrayList;
import java.util.List;

public class TematicasDialogFragment extends DialogFragment {

    // "Interruptores" de configuración del diálogo
    private boolean soloDesbloqueadas = false;
    private boolean mostrarBotonTodas = true;

    public interface TematicaListener {
        void onTematicaSelected(String tematica);
    }

    private TematicaListener listener;

    public void setTematicaListener(TematicaListener listener) {
        this.listener = listener;
    }

    // Nuevo método para configurarlo desde las Activities
    public void setConfiguracionFiltros(boolean soloDesbloqueadas, boolean mostrarBotonTodas) {
        this.soloDesbloqueadas = soloDesbloqueadas;
        this.mostrarBotonTodas = mostrarBotonTodas;
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
            btnOpcionTodas.setVisibility(View.GONE); // Lo ocultamos
        }

        List<ItemPersonalizacion> todosLosItems = InventarioGlobal.getInstance().getTodosLosItems();
        List<String> misTematicas = new ArrayList<>();

        for (ItemPersonalizacion item : todosLosItems) {
            if ("baraja".equals(item.getTipo())) {
                // Si piden "solo desbloqueadas", nos saltamos las bloqueadas
                if (soloDesbloqueadas && item.isBloqueado()) {
                    continue;
                }
                misTematicas.add(item.getNombre());
            }
        }

        androidx.recyclerview.widget.RecyclerView rvTematicas = view.findViewById(R.id.rv_tematicas_grid);
        rvTematicas.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(getContext(), 2));

        TematicaAdapter adapter = new TematicaAdapter(misTematicas, listener, this);
        rvTematicas.setAdapter(adapter);

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