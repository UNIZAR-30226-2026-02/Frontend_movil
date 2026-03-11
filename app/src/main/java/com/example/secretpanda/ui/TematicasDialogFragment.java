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

        //  Configurar botón principal de "Todas las temáticas"
        view.findViewById(R.id.opcion_todas).setOnClickListener(v -> {
            if (listener != null) listener.onTematicaSelected("Todas las temáticas");
            dismiss();
        });

        //  Extraemos las tematicas del sistema
        List<ItemPersonalizacion> todosLosItems = InventarioGlobal.getInstance().getTodosLosItems();
        List<String> misTematicas = new ArrayList<>();

        for (ItemPersonalizacion item : todosLosItems) {
            // Solo cogemos las que son de tipo "baraja"
            if ("baraja".equals(item.getTipo())) {
                misTematicas.add(item.getNombre());
            }
        }

        //  Configuramos el RecyclerView en modo Cuadrícula (Grid) de 2 columnas
        androidx.recyclerview.widget.RecyclerView rvTematicas = view.findViewById(R.id.rv_tematicas_grid);
        rvTematicas.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(getContext(), 2));

        //  Asignamos el adaptador con las temáticas dinámicas
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

            // Fondo transparente para los bordes redondeados
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
    }
}