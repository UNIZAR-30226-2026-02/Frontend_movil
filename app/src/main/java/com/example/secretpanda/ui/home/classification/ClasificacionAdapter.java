package com.example.secretpanda.ui.home.classification; // ¡Ojo! Asegúrate de que el paquete coincide con el tuyo

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secretpanda.R;
import com.example.secretpanda.data.model.Jugador;
import java.util.List;

public class ClasificacionAdapter extends RecyclerView.Adapter<ClasificacionAdapter.JugadorViewHolder> {

    private List<Jugador> listaJugadores;

    public ClasificacionAdapter(List<Jugador> listaJugadores) {
        this.listaJugadores = listaJugadores;
    }

    public void setListaJugadores(List<Jugador> nuevaLista) {
        this.listaJugadores = nuevaLista;
        notifyDataSetChanged(); // Avisamos a la lista de que hay datos nuevos
    }

    @NonNull
    @Override
    public JugadorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_jugador_clasificacion, parent, false);
        return new JugadorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JugadorViewHolder holder, int position) {
        Jugador jugador = listaJugadores.get(position);
        int rankingReal = position + 1; // La posición en la lista empieza en 0, le sumamos 1

        holder.textoPosicion.setText(String.valueOf(rankingReal));
        holder.nombreJugador.setText(jugador.getTag());
        holder.textoVictorias.setText(String.valueOf(jugador.getVictorias()));

        String nombreImagen = jugador.getFotoPerfil();
        if (nombreImagen != null && !nombreImagen.isEmpty()) {
            // Buscamos el recurso por su nombre (ej. "avatar_1")
            int resId = holder.itemView.getContext().getResources().getIdentifier(
                    nombreImagen, "drawable", holder.itemView.getContext().getPackageName());

            if (resId != 0) {
                holder.fotoPerfil.setImageResource(resId);
            } else {
                holder.fotoPerfil.setImageResource(R.mipmap.ic_launcher); // Imagen por defecto
            }
        } else {
            holder.fotoPerfil.setImageResource(R.mipmap.ic_launcher); // Imagen por defecto
        }
        // ==========================================
        // LÓGICA DE ORO, PLATA Y BRONCE
        // ==========================================
        GradientDrawable fondoPosicion = (GradientDrawable) holder.textoPosicion.getBackground().mutate();

        if (rankingReal == 1) {
            fondoPosicion.setColor(Color.parseColor("#FFD700")); // ORO
            holder.textoPosicion.setTextColor(Color.BLACK);
        } else if (rankingReal == 2) {
            fondoPosicion.setColor(Color.parseColor("#C0C0C0")); // PLATA
            holder.textoPosicion.setTextColor(Color.BLACK);
        } else if (rankingReal == 3) {
            fondoPosicion.setColor(Color.parseColor("#CD7F32")); // BRONCE
            holder.textoPosicion.setTextColor(Color.WHITE);
        } else {
            fondoPosicion.setColor(Color.parseColor("#555555")); // GRIS NORMAL
            holder.textoPosicion.setTextColor(Color.WHITE);
        }
    }

    @Override
    public int getItemCount() {
        return listaJugadores.size();
    }

    public static class JugadorViewHolder extends RecyclerView.ViewHolder {
        TextView textoPosicion, nombreJugador, textoVictorias;
        ImageView fotoPerfil;

        public JugadorViewHolder(@NonNull View itemView) {
            super(itemView);
            textoPosicion = itemView.findViewById(R.id.texto_posicion);
            nombreJugador = itemView.findViewById(R.id.nombre_jugador);
            textoVictorias = itemView.findViewById(R.id.texto_victorias);
            fotoPerfil = itemView.findViewById(R.id.foto_perfil);
        }
    }
}