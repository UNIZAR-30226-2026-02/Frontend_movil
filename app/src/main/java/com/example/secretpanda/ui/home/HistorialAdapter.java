package com.example.secretpanda.ui.home; // ¡Asegúrate de poner tu paquete correcto!

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

// Asegúrate de importar tu clase PartidaHistorial
// import com.example.secretpanda.data.model.PartidaHistorial;

import java.util.List;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder> {

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
        Log.w("ADAPTER_PRUEBA", "Pintando la partida con código: " + partida.codigo_partida);

        holder.txtCodigo.setText("Sala: " + partida.codigo_partida);

        // 2. Mostrar Equipo y Rol
        holder.txtRolEquipo.setText("Equipo " + partida.equipo.toUpperCase() + " - " + partida.rol.toUpperCase());

        // 3. Lógica de Ganador/Perdedor mejorada (Hemos borrado la variable 'victoria' que causaba el fallo)
        if (partida.rojoGana == null) {
            holder.txtResultado.setText("EMPATE / FINALIZADA");
            holder.txtResultado.setTextColor(Color.GRAY);
        } else if (partida.rojoGana && partida.equipo.equalsIgnoreCase("rojo")) {
            holder.txtResultado.setText("VICTORIA");
            holder.txtResultado.setTextColor(Color.parseColor("#4CAF50"));
        } else if (!partida.rojoGana && partida.equipo.equalsIgnoreCase("azul")) {
            holder.txtResultado.setText("VICTORIA");
            holder.txtResultado.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            holder.txtResultado.setText("DERROTA");
            holder.txtResultado.setTextColor(Color.parseColor("#F44336"));
        }

        // 4. Lógica de Aciertos y Fallos (Solo para Agentes - RF-4)
        if (partida.rol != null && partida.rol.equalsIgnoreCase("agente")) {
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
}
