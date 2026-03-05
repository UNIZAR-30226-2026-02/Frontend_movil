package com.example.secretpanda.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secretpanda.R;
import java.util.List;

public class SolicitudPendienteAdapter extends RecyclerView.Adapter<SolicitudPendienteAdapter.ViewHolder> {

    private List<String> nombres;

    public SolicitudPendienteAdapter(List<String> nombres) {
        this.nombres = nombres;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_solicitud_pendiente, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textoNombre.setText(nombres.get(position));
    }

    @Override
    public int getItemCount() {
        return nombres.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textoNombre;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textoNombre = itemView.findViewById(R.id.texto_nombre_solicitud_pendiente);
        }
    }
}
