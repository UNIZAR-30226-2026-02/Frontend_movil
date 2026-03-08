package com.example.secretpanda.ui;

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

    // AHORA LE PASAMOS TAMBIÉN LA POSICIÓN AL HACER CLIC
    public interface OnItemClickListener {
        void onItemClick(ItemPersonalizacion item, int position);
    }

    public PersonalizacionAdapter(List<ItemPersonalizacion> listaItems, boolean esListaBloqueados, boolean permiteSeleccion, OnItemClickListener listener) {
        this.listaItems = listaItems;
        this.esListaBloqueados = esListaBloqueados;
        this.permiteSeleccion = permiteSeleccion;
        this.listener = listener;
    }

    // MÉTODO NUEVO: Para que el Pop-up pueda decirle al Adapter "Pinta de verde el nº 3"
    public void setPosicionSeleccionada(int position) {
        int seleccionAnterior = posicionSeleccionada;
        posicionSeleccionada = position;
        notifyItemChanged(seleccionAnterior);      // Quita el verde al viejo
        notifyItemChanged(posicionSeleccionada);   // Le pone el verde al nuevo
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

        if (item.getTipo().equals("baraja")) {
            holder.vistaCartas.setVisibility(View.VISIBLE);
            holder.vistaImagen.setVisibility(View.GONE);
        } else {
            holder.vistaCartas.setVisibility(View.GONE);
            holder.vistaImagen.setVisibility(View.VISIBLE);
            if (item.getIconoResId() != 0) holder.vistaImagen.setImageResource(item.getIconoResId());
        }

        if (item.isBloqueado()) {
            holder.iconoCandado.setVisibility(View.VISIBLE);
            holder.fondo.setBackgroundResource(R.drawable.fondo_carta_bloqueado);
            holder.txtNombre.setTextColor(Color.parseColor("#B71C1C"));
            holder.itemView.setOnClickListener(null); // Bloqueados no hacen nada por ahora
        } else {
            holder.iconoCandado.setVisibility(View.GONE);
            holder.txtNombre.setTextColor(Color.BLACK);

            if (permiteSeleccion && position == posicionSeleccionada && !esListaBloqueados) {
                holder.fondo.setBackgroundResource(R.drawable.borde_verde_seleccion_personalizacion);
            } else {
                holder.fondo.setBackgroundResource(R.drawable.fondo_gris_redondeado);
            }

            // Al hacer clic, YA NO CAMBIAMOS EL COLOR AQUÍ. Solo abrimos el pop-up.
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fondo = itemView.findViewById(R.id.fondo_item_personalizacion);
            txtNombre = itemView.findViewById(R.id.txt_nombre_item);
            iconoCandado = itemView.findViewById(R.id.icono_candado);
            vistaCartas = itemView.findViewById(R.id.vista_cartas);
            vistaImagen = itemView.findViewById(R.id.vista_imagen);
        }
    }
}