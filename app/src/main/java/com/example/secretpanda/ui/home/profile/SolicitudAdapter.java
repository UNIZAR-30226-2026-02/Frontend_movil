package com.example.secretpanda.ui.home.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secretpanda.R;
import com.example.secretpanda.data.model.Solicitud;
import com.example.secretpanda.ui.home.GestorImagenes;

import java.util.List;

public class SolicitudAdapter extends RecyclerView.Adapter<SolicitudAdapter.SolicitudViewHolder> {

    private List<Solicitud> listaSolicitudes;
    private OnSolicitudActionListener listener;

    public interface OnSolicitudActionListener {
        void onAceptar(Solicitud solicitud);
        void onRechazar(Solicitud solicitud);
    }

    public SolicitudAdapter(List<Solicitud> listaSolicitudes, OnSolicitudActionListener listener) {
        this.listaSolicitudes = listaSolicitudes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SolicitudViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_solicitud, parent, false);
        return new SolicitudViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SolicitudViewHolder holder, int position) {
        Solicitud solicitud = listaSolicitudes.get(position);
        
        holder.tvNombre.setText(solicitud.getTagSolicitante());

        int resId = GestorImagenes.obtenerImagenManual(solicitud.getFotoPerfilSolicitante());
        if (resId != 0) {
            holder.ivFoto.setImageResource(resId);
        } else {
            holder.ivFoto.setImageResource(R.mipmap.ic_launcher);
        }

        holder.btnAceptar.setOnClickListener(v -> {
            if (listener != null) listener.onAceptar(solicitud);
        });

        holder.btnRechazar.setOnClickListener(v -> {
            if (listener != null) listener.onRechazar(solicitud);
        });
    }

    @Override
    public int getItemCount() {
        return listaSolicitudes.size();
    }

    public static class SolicitudViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoto, btnAceptar, btnRechazar;
        TextView tvNombre;

        public SolicitudViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoto = itemView.findViewById(R.id.icono_solicitante);
            tvNombre = itemView.findViewById(R.id.nombre_solicitante);
            btnAceptar = itemView.findViewById(R.id.btn_aceptar_solicitud);
            btnRechazar = itemView.findViewById(R.id.btn_rechazar_solicitud);
        }
    }
}