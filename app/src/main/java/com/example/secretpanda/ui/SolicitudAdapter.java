package com.example.secretpanda.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secretpanda.R;
import java.util.List;

public class SolicitudAdapter extends RecyclerView.Adapter<SolicitudAdapter.SolicitudViewHolder> {

    private List<String> nombres;
    private OnAccionSolicitudListener listener; // <-- Nuestro puente

    // 1. Creamos la interfaz para comunicarnos con la Activity
    public interface OnAccionSolicitudListener {
        void onAceptar(String nombre);
        void onRechazar(String nombre);
    }

    // 2. Modificamos el constructor para recibir el listener
    public SolicitudAdapter(List<String> nombres, OnAccionSolicitudListener listener) {
        this.nombres = nombres;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SolicitudViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_solicitud, parent, false);
        return new SolicitudViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SolicitudViewHolder holder, int position) {
        String nombre = nombres.get(position);
        holder.textoNombre.setText(nombre);

        // 3. En vez de Toasts, usamos el listener
        holder.btnAceptar.setOnClickListener(v -> {
            if (listener != null) listener.onAceptar(nombre);
        });

        holder.btnRechazar.setOnClickListener(v -> {
            if (listener != null) listener.onRechazar(nombre);
        });
    }

    @Override
    public int getItemCount() {
        return nombres.size();
    }

    static class SolicitudViewHolder extends RecyclerView.ViewHolder {
        TextView textoNombre, btnAceptar, btnRechazar;

        public SolicitudViewHolder(@NonNull View itemView) {
            super(itemView);
            textoNombre = itemView.findViewById(R.id.texto_nombre_solicitud);
            btnAceptar = itemView.findViewById(R.id.btn_aceptar);
            btnRechazar = itemView.findViewById(R.id.btn_rechazar);
        }
    }
}