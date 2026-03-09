package com.example.secretpanda.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secretpanda.R;

import java.util.List;

public class SolicitudPendienteAdapter extends RecyclerView.Adapter<SolicitudPendienteAdapter.ViewHolder> {

    private List<String> listaPendientes;
    private OnCancelarListener listener;

    // Interfaz para el botón de cancelar
    public interface OnCancelarListener {
        void onCancelar(int position, String nombre);
    }

    public SolicitudPendienteAdapter(List<String> listaPendientes, OnCancelarListener listener) {
        this.listaPendientes = listaPendientes;
        this.listener = listener;
    }

    public void addItem(String nombre) {
        listaPendientes.add(nombre);
        notifyItemInserted(listaPendientes.size() - 1);
    }

    // Borrado con animación
    public void removeItem(int position) {
        listaPendientes.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, listaPendientes.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_solicitud_pendiente, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String nombre = listaPendientes.get(position);
        holder.txtNombre.setText(nombre);

        // ¡AQUÍ ESTÁ LA MAGIA DEL BOTÓN X!
        holder.btnCancelar.setOnClickListener(v -> {
            if (listener != null) listener.onCancelar(holder.getAdapterPosition(), nombre);
        });
    }

    @Override
    public int getItemCount() {
        return listaPendientes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre;
        ImageView btnCancelar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txt_nombre_pendiente);
            btnCancelar = itemView.findViewById(R.id.btn_cancelar_pendiente);
        }
    }
}