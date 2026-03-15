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

import java.util.List;

public class SolicitudAdapter extends RecyclerView.Adapter<SolicitudAdapter.ViewHolder> {

    private List<String> listaSolicitudes;
    private OnAccionSolicitudListener listener;

    public interface OnAccionSolicitudListener {
        void onAceptar(int position, String nombre);
        void onRechazar(int position, String nombre);
    }

    public SolicitudAdapter(List<String> listaSolicitudes, OnAccionSolicitudListener listener) {
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
        String nombre = listaSolicitudes.get(position);
        holder.txtNombre.setText(nombre);

        holder.btnAceptar.setOnClickListener(v -> {
            if (listener != null) listener.onAceptar(holder.getAdapterPosition(), nombre);
        });

        holder.btnRechazar.setOnClickListener(v -> {
            if (listener != null) listener.onRechazar(holder.getAdapterPosition(), nombre);
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