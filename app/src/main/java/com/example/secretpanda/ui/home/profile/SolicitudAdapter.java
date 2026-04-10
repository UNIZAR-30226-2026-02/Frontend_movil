package com.example.secretpanda.ui.home.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secretpanda.R;
import com.example.secretpanda.data.model.Solicitud; // Asegúrate de importar el modelo

import java.util.List;

public class SolicitudAdapter extends RecyclerView.Adapter<SolicitudAdapter.ViewHolder> {

    // 1. Cambiamos String por Solicitud
    private List<Solicitud> listaSolicitudes;
    private OnAccionSolicitudListener listener;

    public interface OnAccionSolicitudListener {
        // 2. Añadimos el int idSolicitante
        void onAceptar(int position, String nombre, int idSolicitante);
        void onRechazar(int position, String nombre, int idSolicitante);
    }

    public SolicitudAdapter(List<Solicitud> listaSolicitudes, OnAccionSolicitudListener listener) {
        this.listaSolicitudes = listaSolicitudes;
        this.listener = listener;
    }

    public void removeItem(int position) {
        listaSolicitudes.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, listaSolicitudes.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_solicitud, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 3. Extraemos los datos del objeto
        Solicitud solicitud = listaSolicitudes.get(position);
        String nombre = solicitud.getNombre();
        int idSolicitante = solicitud.getIdSolicitante();

        holder.txtNombre.setText(nombre);

        holder.btnAceptar.setOnClickListener(v -> {
            if (listener != null) listener.onAceptar(holder.getAdapterPosition(), nombre, idSolicitante);
        });

        holder.btnRechazar.setOnClickListener(v -> {
            if (listener != null) listener.onRechazar(holder.getAdapterPosition(), nombre, idSolicitante);
        });
    }

    @Override
    public int getItemCount() {
        return listaSolicitudes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre;
        ImageView btnRechazar;
        FrameLayout btnAceptar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txt_nombre_recibida);
            btnAceptar = itemView.findViewById(R.id.btn_aceptar_recibida);
            btnRechazar = itemView.findViewById(R.id.btn_rechazar_recibida);
        }
    }
}