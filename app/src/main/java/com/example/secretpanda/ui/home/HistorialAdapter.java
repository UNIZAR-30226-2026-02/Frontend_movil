package com.example.secretpanda.ui.home;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secretpanda.R;
import com.example.secretpanda.data.model.PartidaHistorial;
import java.util.List;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder> {

    private List<PartidaHistorial> listaPartidas;

    public HistorialAdapter(List<PartidaHistorial> listaPartidas) {
        this.listaPartidas = listaPartidas;
    }

    public void setLista(List<PartidaHistorial> nuevaLista) {
        this.listaPartidas = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Usamos exactamente tu XML item_historial.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historial, parent, false);
        return new HistorialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistorialViewHolder holder, int position) {
        PartidaHistorial partida = listaPartidas.get(position);

        // 1. Código y Fecha
        holder.txtCodigo.setText("SALA: " + partida.codigo_partida);
        holder.txtFecha.setText(partida.fechaFin != null ? partida.fechaFin : "");

        // 2. Mostrar Equipo y Rol
        holder.txtRolEquipo.setText("EQUIPO " + partida.equipo.toUpperCase() + " - " + partida.rol.toUpperCase());

        // 3. Lógica de Victoria / Derrota
        if (partida.rojoGana == null) {
            holder.txtResultado.setText("FINALIZADA");
            holder.txtResultado.setTextColor(Color.GRAY);
        } else if ((partida.rojoGana && partida.equipo.equalsIgnoreCase("rojo")) ||
                (!partida.rojoGana && partida.equipo.equalsIgnoreCase("azul"))) {
            holder.txtResultado.setText("VICTORIA");
            holder.txtResultado.setTextColor(Color.parseColor("#4CAF50")); // Verde
        } else {
            holder.txtResultado.setText("DERROTA");
            holder.txtResultado.setTextColor(Color.parseColor("#F44336")); // Rojo
        }

        if (partida.rol != null && (partida.rol.equalsIgnoreCase("lider") || partida.rol.equalsIgnoreCase("jefe"))) {
            holder.txtAciertos.setVisibility(View.VISIBLE);
            holder.txtAciertos.setText("ACIERTOS: " + partida.numAciertos + " | FALLOS: " + partida.numFallos);
        } else {
            holder.txtAciertos.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return listaPartidas == null ? 0 : listaPartidas.size();
    }

    public static class HistorialViewHolder extends RecyclerView.ViewHolder {
        TextView txtResultado, txtFecha, txtCodigo, txtRolEquipo, txtAciertos;

        public HistorialViewHolder(@NonNull View itemView) {
            super(itemView);
            // Referencias exactas a tus IDs de item_historial.xml
            txtResultado = itemView.findViewById(R.id.item_historial_resultado);
            txtFecha = itemView.findViewById(R.id.item_historial_fecha);
            txtCodigo = itemView.findViewById(R.id.item_historial_codigo);
            txtRolEquipo = itemView.findViewById(R.id.item_historial_rol);
            txtAciertos = itemView.findViewById(R.id.item_historial_aciertos);
        }
    }
}