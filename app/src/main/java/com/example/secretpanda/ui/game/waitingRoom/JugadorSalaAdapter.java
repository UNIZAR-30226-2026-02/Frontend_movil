package com.example.secretpanda.ui.game.waitingRoom;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secretpanda.R;
import com.example.secretpanda.data.model.Jugador;

import java.util.List;

public class JugadorSalaAdapter extends RecyclerView.Adapter<JugadorSalaAdapter.JugadorViewHolder> {

    private List<Jugador> listaJugadores;
    private String miNombreJugador;
    private boolean esLider; // NUEVO: Variable para saber si eres el creador

    public interface OnJugadoresCambioListener {
        void onJugadoresCambiados(List<Jugador> listaActualizada);
    }
    private OnJugadoresCambioListener listener;

    // NUEVO: Añadimos 'esLider' al constructor
    public JugadorSalaAdapter(List<Jugador> listaJugadores, String miNombreJugador, boolean esLider, OnJugadoresCambioListener listener) {
        this.listaJugadores = listaJugadores;
        this.miNombreJugador = miNombreJugador;
        this.esLider = esLider;
        this.listener = listener;
    }

    @NonNull
    @Override
    public JugadorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_jugador_sala, parent, false);
        return new JugadorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JugadorViewHolder holder, int position) {
        Jugador jugador = listaJugadores.get(position);

        holder.tvNombre.setText(jugador.getTag());

        if (jugador.isEsEquipoAzul()) {
            holder.cardJugador.setStrokeColor(android.graphics.Color.parseColor("#0000FF"));
            holder.tvNombre.setShadowLayer(8f, 0f, 0f, android.graphics.Color.parseColor("#0000FF"));
        } else {
            holder.cardJugador.setStrokeColor(android.graphics.Color.parseColor("#FF0000"));
            holder.tvNombre.setShadowLayer(8f, 0f, 0f, android.graphics.Color.parseColor("#FF0000"));
        }

        if (holder.btnExpulsar != null) {
            if (jugador.getTag().equals(miNombreJugador)) {
                holder.btnExpulsar.setVisibility(View.GONE);
            } else {
                holder.btnExpulsar.setVisibility(View.VISIBLE);
            }

            // --- NUEVA LÓGICA PARA EL BOTÓN DE EXPULSAR ---
            if (holder.btnExpulsar != null) {

                // Si NO eres líder, o el jugador de esta carta eres TÚ mismo -> Ocultamos la X
                if (!esLider || jugador.getTag().equals(miNombreJugador)) {
                    holder.btnExpulsar.setVisibility(View.GONE);
                } else {
                    // Si eres líder y es otro jugador -> Mostramos la X
                    holder.btnExpulsar.setVisibility(View.VISIBLE);
                }

                // El listener se queda igual (total, si no se ve, no se puede clicar)
                holder.btnExpulsar.setOnClickListener(v -> {
                    int posicionActual = holder.getAdapterPosition();
                    if (posicionActual != RecyclerView.NO_POSITION) {
                        listaJugadores.remove(posicionActual);
                        notifyItemRemoved(posicionActual);
                        notifyItemRangeChanged(posicionActual, listaJugadores.size());
                        if (listener != null) listener.onJugadoresCambiados(listaJugadores);
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return listaJugadores.size();
    }

    class JugadorViewHolder extends RecyclerView.ViewHolder {
        com.google.android.material.card.MaterialCardView cardJugador;
        TextView tvNombre;
        View btnExpulsar;

        public JugadorViewHolder(@NonNull View itemView) {
            super(itemView);
            cardJugador = itemView.findViewById(R.id.card_jugador);
            tvNombre = itemView.findViewById(R.id.tv_nombre_jugador);
            btnExpulsar = itemView.findViewById(R.id.btn_expulsar);
        }
    }
}
