package com.example.secretpanda.ui.home.profile; // <-- Revisa que este sea tu paquete correcto

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

public class AmigoAdapter extends RecyclerView.Adapter<AmigoAdapter.AmigoViewHolder> {

    private List<Jugador> listaAmigos;
    private OnAmigoClickListener listener; // ¡NUEVO!

    public AmigoAdapter(List<Jugador> listaAmigos, OnAmigoClickListener listener) {
        this.listaAmigos = listaAmigos;
        this.listener = listener;
    }


    // ¡NUEVA INTERFAZ!
    public interface OnAmigoClickListener {
        void onAmigoClick(Jugador amigo);
    }
    @NonNull
    @Override
    public AmigoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_amigo, parent, false);
        return new AmigoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AmigoViewHolder holder, int position) {
        Jugador amigo = listaAmigos.get(position);
        holder.textoNombre.setText(amigo.getTag());

        // ¡NUEVO! Detectamos el clic en toda la fila
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAmigoClick(amigo);
            }
        });
    }
    public void setListaAmigos(java.util.List<Jugador> nuevaLista) {
        this.listaAmigos = nuevaLista;
    }

    @Override
    public int getItemCount() {
        return listaAmigos.size();
    }


    // --- CLASE VIEWHOLDER INTERNA ---
    public static class AmigoViewHolder extends RecyclerView.ViewHolder {
        ImageView iconoAmigo;
        TextView textoNombre;

        public AmigoViewHolder(@NonNull View itemView) {
            super(itemView);
            iconoAmigo = itemView.findViewById(R.id.icono_amigo);
            textoNombre = itemView.findViewById(R.id.nombre_amigo);
        }
    }
}