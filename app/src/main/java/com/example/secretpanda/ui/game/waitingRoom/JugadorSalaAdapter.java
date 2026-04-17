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

        // Cargamos la foto de perfil usando el GestorImagenes
        int resId = com.example.secretpanda.ui.home.GestorImagenes.obtenerImagenManual(jugador.getFotoPerfil());
        holder.ivPerfil.setImageResource(resId);

        if (jugador.isEsEquipoAzul()) {
            holder.cardJugador.setStrokeColor(android.graphics.Color.parseColor("#80A0D0"));
            holder.tvNombre.setShadowLayer(8f, 0f, 0f, android.graphics.Color.parseColor("#80A0D0"));
        } else {
            holder.cardJugador.setStrokeColor(android.graphics.Color.parseColor("#E08080"));
            holder.tvNombre.setShadowLayer(8f, 0f, 0f, android.graphics.Color.parseColor("#E08080"));
        }

        if (holder.btnExpulsar != null) {
            // El botón de expulsar (X) se elimina por completo de la vista por petición del usuario.
            holder.btnExpulsar.setVisibility(View.GONE);
            holder.btnExpulsar.setOnClickListener(null);
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
        android.widget.ImageView ivPerfil;

        public JugadorViewHolder(@NonNull View itemView) {
            super(itemView);
            cardJugador = itemView.findViewById(R.id.card_jugador);
            tvNombre = itemView.findViewById(R.id.tv_nombre_jugador);
            btnExpulsar = itemView.findViewById(R.id.btn_expulsar);
            ivPerfil = itemView.findViewById(R.id.iv_perfil_sala);
        }
    }
}
