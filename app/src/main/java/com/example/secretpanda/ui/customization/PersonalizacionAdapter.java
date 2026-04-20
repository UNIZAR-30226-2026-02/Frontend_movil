package com.example.secretpanda.ui.customization;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secretpanda.R;
import com.example.secretpanda.data.model.ItemPersonalizacion;

import java.util.List;

public class PersonalizacionAdapter extends RecyclerView.Adapter<PersonalizacionAdapter.ViewHolder> {

    private List<ItemPersonalizacion> listaItems;
    private int posicionSeleccionada = 0;
    private boolean esListaBloqueados;
    private boolean permiteSeleccion;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ItemPersonalizacion item, int position);
    }

    public PersonalizacionAdapter(List<ItemPersonalizacion> listaItems, boolean esListaBloqueados, boolean permiteSeleccion, OnItemClickListener listener) {
        this.listaItems = listaItems;
        this.esListaBloqueados = esListaBloqueados;
        this.permiteSeleccion = permiteSeleccion;
        this.listener = listener;
    }

    public void setPosicionSeleccionada(int position) {
        int seleccionAnterior = posicionSeleccionada;
        posicionSeleccionada = position;
        notifyItemChanged(seleccionAnterior);
        notifyItemChanged(posicionSeleccionada);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_personalizacion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemPersonalizacion item = listaItems.get(position);
        holder.txtNombre.setText(item.getNombre());

        // Lógica de imágenes (cartas o fondo/borde)
        if (item.getTipo().equals("baraja")) {
            holder.vistaCartas.setVisibility(View.VISIBLE);
            holder.vistaImagen.setVisibility(View.GONE);
            holder.txtEmoji.setText(emojiPaquete(item.getNombre()));
            holder.txtEmoji.setVisibility(View.VISIBLE);
        } else {
            holder.vistaCartas.setVisibility(View.GONE);
            holder.vistaImagen.setVisibility(View.VISIBLE);
            holder.txtEmoji.setVisibility(View.GONE);
            if (item.getIconoResId() != 0) {
                holder.vistaImagen.setImageResource(item.getIconoResId());
            } else if (item.getValor() != null && !item.getValor().equals("0")) {
                try {
                    holder.vistaImagen.setBackgroundColor(Color.parseColor("#" + item.getValor()));
                } catch (Exception e) {
                    holder.vistaImagen.setImageResource(R.drawable.fondo_carta_gruesa);
                }
            } else {
                holder.vistaImagen.setImageResource(R.drawable.fondo_carta_gruesa);
            }
        }

        if (item.isBloqueado()) {
            holder.iconoCandado.setVisibility(View.VISIBLE);
            holder.fondo.setBackgroundResource(R.drawable.bg_dark_card);
            holder.fondo.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#3e3224"))); // Un poco más oscuro si está bloqueado
            holder.txtNombre.setTextColor(Color.parseColor("#d4b878")); // Letra en oro viejo para bloqueados

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item, holder.getAdapterPosition());
            });
        } else {
            holder.iconoCandado.setVisibility(View.GONE);
            holder.txtNombre.setTextColor(Color.parseColor("#e8dcc8")); // Blanco/crema para adquiridos

            if (permiteSeleccion && position == posicionSeleccionada && !esListaBloqueados) {
                holder.fondo.setBackgroundResource(R.drawable.borde_verde_seleccion_personalizacion); // Solo el borde si está seleccionado
                holder.fondo.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#2a2218")));
            } else {
                holder.fondo.setBackgroundResource(R.drawable.bg_dark_card);
                holder.fondo.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#2a2218")));
            }

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item, holder.getAdapterPosition());
            });
        }
    }

    @Override
    public int getItemCount() {
        return listaItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout fondo;
        TextView txtNombre;
        ImageView iconoCandado;
        FrameLayout vistaCartas;
        ImageView vistaImagen;
        TextView txtEmoji;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fondo = itemView.findViewById(R.id.fondo_item_personalizacion);
            txtNombre = itemView.findViewById(R.id.txt_nombre_item);
            iconoCandado = itemView.findViewById(R.id.icono_candado);
            vistaCartas = itemView.findViewById(R.id.vista_cartas);
            vistaImagen = itemView.findViewById(R.id.vista_imagen);
            txtEmoji = itemView.findViewById(R.id.txt_emoji_item);

            ImageView imgCartaFrontal = itemView.findViewById(R.id.img_carta_frontal_preview);
        }
    }
    private String emojiPaquete(String nombre) {
        if (nombre == null) return "🎴";
        String n = nombre.toLowerCase();
        if (n.contains("básico") || n.contains("basico")) return "🃏";
        if (n.contains("magia")) return "🪄";
        if (n.contains("histórico") || n.contains("historico")) return "📜";
        if (n.contains("submarina") || n.contains("profundo")) return "🐙";
        if (n.contains("cyber") || n.contains("futuro") || n.contains("punk")) return "🌆";
        if (n.contains("naturaleza") || n.contains("bambu")) return "🌿";
        return "🎴";
    }
}