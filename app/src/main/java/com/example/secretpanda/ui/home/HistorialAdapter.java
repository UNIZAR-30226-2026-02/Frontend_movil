package com.example.secretpanda.ui.home; // ¡Asegúrate de poner tu paquete correcto!

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secretpanda.R;
import com.example.secretpanda.data.model.PartidaHistorial;

// Asegúrate de importar tu clase PartidaHistorial
// import com.example.secretpanda.data.model.PartidaHistorial;

import java.util.List;

/*public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder> {

    private List<PartidaHistorial> listaPartidas;

    public HistorialAdapter(List<PartidaHistorial> listaPartidas) {
        this.listaPartidas = listaPartidas;
    }

    public void setLista(List<PartidaHistorial> nuevaLista) {
        this.listaPartidas = nuevaLista;
        notifyDataSetChanged(); // Refresca la lista visualmente
    }

    @NonNull
    @Override
    public HistorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Asume que vas a crear un XML llamado item_historial.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historial, parent, false);
        return new HistorialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistorialViewHolder holder, int position) {
        PartidaHistorial partida = listaPartidas.get(position);

        // 1. Mostrar la Fecha y el Código de partida
        holder.txtFecha.setText(partida.fechaFin.split("T")); // Muestra solo la fecha si viene con hora
        holder.txtCodigo.setText("Sala: " + partida.codigo_partida);

        // 2. Mostrar Equipo y Rol
        holder.txtRolEquipo.setText("Equipo " + partida.equipo.toUpperCase() + " - " + partida.rol.toUpperCase());

        // 3. Lógica de Victoria / Derrota (RF-4)
        boolean victoria = (partida.equipo.equalsIgnoreCase("rojo") && partida.rojoGana) ||
                (partida.equipo.equalsIgnoreCase("azul") && !partida.rojoGana);

        if (partida.abandono) {
            holder.txtResultado.setText("ABANDONADA");
            holder.txtResultado.setTextColor(Color.parseColor("#808080")); // Gris
        } else if (victoria) {
            holder.txtResultado.setText("VICTORIA");
            holder.txtResultado.setTextColor(Color.parseColor("#4CAF50")); // Verde
        } else {
            holder.txtResultado.setText("DERROTA");
            holder.txtResultado.setTextColor(Color.parseColor("#F44336")); // Rojo
        }

        // 4. Lógica de Aciertos y Fallos (Solo para Agentes - RF-4)
        if (partida.rol.equalsIgnoreCase("agente")) {
            holder.txtAciertos.setVisibility(View.VISIBLE);
            holder.txtAciertos.setText("Aciertos: " + partida.numAciertos + " | Fallos: " + partida.numFallos);
        } else {
            // Si es espía, ocultamos este TextView para que no ocupe espacio
            holder.txtAciertos.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return listaPartidas == null ? 0 : listaPartidas.size();
    }

    // --- ViewHolder ---
    public static class HistorialViewHolder extends RecyclerView.ViewHolder {
        TextView txtResultado, txtFecha, txtCodigo, txtRolEquipo, txtAciertos;

        public HistorialViewHolder(@NonNull View itemView) {
            super(itemView);
            // Estos IDs tendrán que coincidir con tu archivo item_historial.xml
            txtResultado = itemView.findViewById(R.id.item_historial_resultado);
            txtFecha = itemView.findViewById(R.id.item_historial_fecha);
            txtCodigo = itemView.findViewById(R.id.item_historial_codigo);
            txtRolEquipo = itemView.findViewById(R.id.item_historial_rol);
            txtAciertos = itemView.findViewById(R.id.item_historial_aciertos);
        }
    }
}*/
