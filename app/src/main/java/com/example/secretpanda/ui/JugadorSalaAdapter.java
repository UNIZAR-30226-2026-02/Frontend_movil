package com.example.secretpanda.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secretpanda.R;
import com.example.secretpanda.data.model.Jugador;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class JugadorSalaAdapter extends RecyclerView.Adapter<JugadorSalaAdapter.JugadorViewHolder> {

    private List<Jugador> listaJugadores;

    public JugadorSalaAdapter(List<Jugador> listaJugadores) {
        this.listaJugadores = listaJugadores;

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

        // Verificamos el booleano: true es Azul, false es Rojo
        if (jugador.isEsEquipoAzul()) {
            // EQUIPO AZUL
            holder.cardJugador.setStrokeColor(android.graphics.Color.parseColor("#0000FF"));
            holder.tvNombre.setShadowLayer(8f, 0f, 0f, android.graphics.Color.parseColor("#0000FF"));
        } else {
            // EQUIPO ROJO
            holder.cardJugador.setStrokeColor(android.graphics.Color.parseColor("#FF0000"));
            holder.tvNombre.setShadowLayer(8f, 0f, 0f, android.graphics.Color.parseColor("#FF0000"));
        }
    }

    @Override
    public int getItemCount() {
        return listaJugadores.size();
    }

    class JugadorViewHolder extends RecyclerView.ViewHolder {
        com.google.android.material.card.MaterialCardView cardJugador;
        TextView tvNombre;

        public JugadorViewHolder(@NonNull View itemView) {
            super(itemView);
            // Referenciamos la tarjeta principal en lugar del LinearLayout
            cardJugador = itemView.findViewById(R.id.card_jugador);
            tvNombre = itemView.findViewById(R.id.tv_nombre_jugador);
        }
    }


}
