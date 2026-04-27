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
    private List<String> tematicasDesbloqueadas;

    public interface OnItemClickListener {
        void onItemClick(Partida partida);
    }

    public PartidaAdapter(List<Partida> listaPartidas, List<String> tematicasDesbloqueadas, OnItemClickListener listener) {
        this.listaPartidas = listaPartidas;
        this.tematicasDesbloqueadas = tematicasDesbloqueadas;
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

        // 🕵️‍♂️ LECTURA DE DATOS:
        // Según tu servidor, getTag() es el líder y getNombre() es la temática
        String lider = partida.getNombre() != null ? partida.getNombre() : "Agente Secreto";
        String tematica = partida.getTematica() != null ? partida.getTematica() : "Desconocida";
        holder.tvLider.setText(lider);
        // 1. Asignamos los textos a la tarjeta
        holder.tvNombre.setText(tematica);

        // Comprobamos si el recuadro de la temática existe en el XML para no sobrescribir nada
        if (holder.tvTematica != null) {
            holder.tvTematica.setText(tematica);
        }

        holder.tvSegundos.setText(String.valueOf(partida.getSegundos()));
        holder.tvJugadores.setText(partida.getJugadoresTexto());

        // 2. LÓGICA DE BLOQUEO
        boolean tengoLaTematica = "Clásico".equalsIgnoreCase(tematica) || "Clasico".equalsIgnoreCase(tematica);

        if (!tengoLaTematica && tematicasDesbloqueadas != null) {
            for (String miTematica : tematicasDesbloqueadas) {
                if (miTematica.equalsIgnoreCase(tematica)) {
                    tengoLaTematica = true;
                    break;
                }
            }
        }
        // 3. CAMBIAR LA INTERFAZ
        if (tengoLaTematica) {
            holder.itemView.setBackgroundResource(R.drawable.fondo_item_mision_publica);
            holder.itemView.setAlpha(1.0f);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(partida);
                }
            });
        } else {
            holder.itemView.setBackgroundResource(R.drawable.fondo_item_mision_bloqueada);
            holder.itemView.setAlpha(0.6f);
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
        TextView tvLider;
        TextView tvSegundos;

        public PartidaViewHolder(View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tv_nombre_mision);
            tvTematica = itemView.findViewById(R.id.tv_nombre_tematica);
            tvJugadores = itemView.findViewById(R.id.tv_jugadores_mision);
            tvLider = itemView.findViewById(R.id.tv_creador_mision);
            tvSegundos = itemView.findViewById(R.id.tv_tiempo_mision);
        }
    }
}