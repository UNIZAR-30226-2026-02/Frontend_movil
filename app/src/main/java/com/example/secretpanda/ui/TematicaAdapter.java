package com.example.secretpanda.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secretpanda.R;
import java.util.List;

public class TematicaAdapter extends RecyclerView.Adapter<TematicaAdapter.TematicaViewHolder> {

    private List<String> listaTematicas;
    private TematicasDialogFragment.TematicaListener listener;
    private TematicasDialogFragment dialog;

    public TematicaAdapter(List<String> listaTematicas, TematicasDialogFragment.TematicaListener listener, TematicasDialogFragment dialog) {
        this.listaTematicas = listaTematicas;
        this.listener = listener;
        this.dialog = dialog;
    }

    @NonNull
    @Override
    public TematicaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Usamos la tarjeta individual que creamos en los pasos anteriores
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tematica_tarjeta, parent, false);
        return new TematicaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TematicaViewHolder holder, int position) {
        String tematica = listaTematicas.get(position);
        holder.tvNombre.setText(tematica);

        // Al pulsar en una tarjeta, se avisa a la pantalla y se cierra el diálogo
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTematicaSelected(tematica);
            }
            if (dialog != null) {
                dialog.dismiss();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaTematicas.size();
    }

    class TematicaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;

        public TematicaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tv_nombre_tematica);
        }
    }
}
