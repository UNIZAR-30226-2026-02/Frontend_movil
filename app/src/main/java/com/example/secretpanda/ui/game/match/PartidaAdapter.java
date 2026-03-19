package com.example.secretpanda.ui.game.match;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secretpanda.R;
import com.example.secretpanda.data.model.InventarioGlobal;
import com.example.secretpanda.data.model.ItemPersonalizacion;
import com.example.secretpanda.data.model.Partida;

import java.util.List;

public class PartidaAdapter extends RecyclerView.Adapter<PartidaAdapter.PartidaViewHolder> {

    private List<Partida> listaPartidas;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Partida partida);
    }

    public PartidaAdapter(List<Partida> listaPartidas, OnItemClickListener listener) {
        this.listaPartidas = listaPartidas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PartidaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mision_publica, parent, false);
        return new PartidaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PartidaViewHolder holder, int position) {
        Partida partida = listaPartidas.get(position);

        // 1. Asignamos los textos a la tarjeta
        holder.tvNombre.setText(partida.getNombre());
        holder.tvTematica.setText(partida.getTematica());
        holder.tvJugadores.setText(partida.getJugadoresActuales() + "/" + partida.getMaxJugadores());

        // 2. LÓGICA DE BLOQUEO: Comprobamos si tiene la temática
        boolean tengoLaTematica = partida.getTematica().equalsIgnoreCase("Clásico") ||
                partida.getTematica().equalsIgnoreCase("Clasico");

        // Recorremos el inventario global para ver si la tiene comprada
        List<ItemPersonalizacion> misItems = InventarioGlobal.getInstance().getTodosLosItems();
        if (misItems != null && !tengoLaTematica) {
            for (ItemPersonalizacion item : misItems) {
                // Si encontramos la temática y no está bloqueada, significa que la tenemos
                if (item.getNombre().equalsIgnoreCase(partida.getTematica()) && !item.isBloqueado()) {
                    tengoLaTematica = true;
                    break;
                }
            }
        }

        // 3. CAMBIAR LA INTERFAZ SEGÚN SI LA TENEMOS O NO
        if (tengoLaTematica) {
            holder.itemView.setBackgroundResource(R.drawable.fondo_item_mision_publica);
            holder.itemView.setAlpha(1.0f); // Opacidad normal

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(partida);
                }
            });
        } else {
            holder.itemView.setBackgroundResource(R.drawable.fondo_item_mision_bloqueada);
            holder.itemView.setAlpha(0.6f); // La difuminamos un poco para que parezca desactivada

            // Quitamos el listener para que no haga nada al pulsar
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return listaPartidas != null ? listaPartidas.size() : 0;
    }

    public static class PartidaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvTematica;
        TextView tvJugadores;

        public PartidaViewHolder( View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tv_nombre_mision);
            tvTematica = itemView.findViewById(R.id.tv_nombre_tematica);
            if(tvTematica==null){
                tvTematica=tvNombre;
            }
            tvJugadores = itemView.findViewById(R.id.tv_jugadores_mision);
        }
    }
}