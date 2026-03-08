package com.example.secretpanda.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private boolean permiteSeleccion; // ¡La clave para que barajas no se elijan!
    private OnItemClickListener listener;

    // Interfaz para avisar a la pantalla de qué hemos tocado
    public interface OnItemClickListener {
        void onItemClick(ItemPersonalizacion item);
    }

    public PersonalizacionAdapter(List<ItemPersonalizacion> listaItems, boolean esListaBloqueados, boolean permiteSeleccion, OnItemClickListener listener) {
        this.listaItems = listaItems;
        this.esListaBloqueados = esListaBloqueados;
        this.permiteSeleccion = permiteSeleccion;
        this.listener = listener;
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

        if (item.isBloqueado()) {
            holder.iconoCandado.setVisibility(View.VISIBLE);
            holder.fondo.setBackgroundResource(R.drawable.fondo_carta_bloqueado);
            holder.txtNombre.setTextColor(Color.parseColor("#B71C1C"));
            holder.itemView.setOnClickListener(null); // No se puede tocar
        } else {
            holder.iconoCandado.setVisibility(View.GONE);
            holder.txtNombre.setTextColor(Color.BLACK);

            // Borde verde SOLO si está seleccionado Y además lo permitimos (Bordes/Fondos)
            if (permiteSeleccion && position == posicionSeleccionada && !esListaBloqueados) {
                holder.fondo.setBackgroundResource(R.drawable.borde_verde_seleccion_personalizacion);
            } else {
                holder.fondo.setBackgroundResource(R.drawable.fondo_gris_redondeado);
            }

            // Click SOLO si lo permitimos
            if (permiteSeleccion && !esListaBloqueados) {
                holder.itemView.setOnClickListener(v -> {
                    int seleccionAnterior = posicionSeleccionada;
                    posicionSeleccionada = holder.getAdapterPosition();
                    notifyItemChanged(seleccionAnterior);
                    notifyItemChanged(posicionSeleccionada);

                    // Avisamos a la pantalla
                    if (listener != null) listener.onItemClick(item);
                });
            } else {
                holder.itemView.setOnClickListener(null); // Barajas no hacen nada
            }
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fondo = itemView.findViewById(R.id.fondo_item_personalizacion);
            txtNombre = itemView.findViewById(R.id.txt_nombre_item);
            iconoCandado = itemView.findViewById(R.id.icono_candado);
        }
    }
}