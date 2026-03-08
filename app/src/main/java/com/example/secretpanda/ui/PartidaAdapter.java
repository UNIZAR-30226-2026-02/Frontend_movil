package com.example.secretpanda.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secretpanda.R;
import com.example.secretpanda.data.model.Partida;

import java.util.List;

public class PartidaAdapter extends RecyclerView.Adapter<PartidaAdapter.PartidaViewHolder> {

    private List<Partida> listaPartidas;

    public PartidaAdapter(List<Partida> listaPartidas) {
        this.listaPartidas = listaPartidas;
    }

    @NonNull
    @Override
    public PartidaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Recuerda renombrar tu XML de item_mision_publica a item_partida_publica
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mision_publica, parent, false);
        return new PartidaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PartidaViewHolder holder, int position) {
        Partida
 partida = listaPartidas.get(position);

        holder.tvNombre.setText(partida.getNombre());
        holder.tvCreador.setText(partida.getCreador());
        holder.tvTiempo.setText(partida.getTiempo());
        holder.tvJugadores.setText(partida.getJugadores());

        if (partida.isBloqueada()) {
            holder.contenedor.setBackgroundResource(R.drawable.fondo_item_mision_bloqueada);
            holder.tvNombre.setTextColor(0xFFB71C1C);
            holder.tvNombre.setText(partida.getNombre() + " (no desbloqueada)");
        } else {
            holder.contenedor.setBackgroundResource(R.drawable.fondo_item_mision_publica);
            holder.tvNombre.setTextColor(0xFF000000);
        }
    }

    @Override
    public int getItemCount() {
        return listaPartidas.size();
    }

    class PartidaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvCreador, tvTiempo, tvJugadores;
        LinearLayout contenedor;

        public PartidaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tv_nombre_mision); // Puedes dejar los IDs o cambiarlos también
            tvCreador = itemView.findViewById(R.id.tv_creador_mision);
            tvTiempo = itemView.findViewById(R.id.tv_tiempo_mision);
            tvJugadores = itemView.findViewById(R.id.tv_jugadores_mision);
            contenedor = itemView.findViewById(R.id.contenedor_item);
        }
    }
}
