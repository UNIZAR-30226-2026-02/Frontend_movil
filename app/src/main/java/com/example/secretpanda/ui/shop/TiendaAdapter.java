package com.example.secretpanda.ui.shop;

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

public class TiendaAdapter extends RecyclerView.Adapter<TiendaAdapter.ViewHolder> {

    private List<ItemPersonalizacion> listaItems;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ItemPersonalizacion item, int position);
    }

    public TiendaAdapter(List<ItemPersonalizacion> listaItems, OnItemClickListener listener) {
        this.listaItems = listaItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tienda, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemPersonalizacion item = listaItems.get(position);
        holder.txtNombre.setText(item.getNombre());
        holder.txtPrecio.setText(String.valueOf(item.getPrecio()));

        // Mostrar foto o cartas
        if (item.getTipo().equals("baraja")) {
            holder.vistaCartas.setVisibility(View.VISIBLE);
            holder.vistaImagen.setVisibility(View.GONE);
        } else {
            holder.vistaCartas.setVisibility(View.GONE);
            holder.vistaImagen.setVisibility(View.VISIBLE);
            if (item.getIconoResId() != 0) holder.vistaImagen.setImageResource(item.getIconoResId());
        }

        // ¿Lo tienes comprado o no?
        if (item.isBloqueado()) {
            // NO LO TIENES: Mostrar precio
            holder.layoutPrecio.setVisibility(View.VISIBLE);
            holder.layoutComprado.setVisibility(View.GONE);
            holder.fondo.setAlpha(1.0f); // Brillo normal

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item, holder.getAdapterPosition());
            });
        } else {
            // YA LO TIENES: Mostrar ¡En posesión! y desactivar clic
            holder.layoutPrecio.setVisibility(View.GONE);
            holder.layoutComprado.setVisibility(View.VISIBLE);
            holder.fondo.setAlpha(0.6f); // Lo oscurecemos un poco
            holder.itemView.setOnClickListener(null); // No se puede tocar
        }
    }

    @Override
    public int getItemCount() {
        return listaItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout fondo, layoutPrecio, layoutComprado;
        TextView txtNombre, txtPrecio;
        FrameLayout vistaCartas;
        ImageView vistaImagen;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fondo = itemView.findViewById(R.id.fondo_item_tienda);
            txtNombre = itemView.findViewById(R.id.txt_nombre_item_tienda);
            txtPrecio = itemView.findViewById(R.id.txt_precio_item_tienda);
            vistaCartas = itemView.findViewById(R.id.vista_cartas_tienda);
            vistaImagen = itemView.findViewById(R.id.vista_imagen_tienda);
            layoutPrecio = itemView.findViewById(R.id.layout_precio_tienda);
            layoutComprado = itemView.findViewById(R.id.layout_comprado_tienda);
        }
    }
}