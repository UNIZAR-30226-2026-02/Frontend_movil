package com.example.secretpanda.ui.home.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secretpanda.R;
import java.util.List;

public class ImagenPerfilAdapter extends RecyclerView.Adapter<ImagenPerfilAdapter.ViewHolder> {

    private List<Integer> imagenes; // Lista de R.drawable...
    private OnImagenClickListener listener;

    public interface OnImagenClickListener {
        void onImagenClick(int recursoImagen);
    }

    public ImagenPerfilAdapter(List<Integer> imagenes, OnImagenClickListener listener) {
        this.imagenes = imagenes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_imagen_perfil, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int recursoImagen = imagenes.get(position);
        holder.imagen.setImageResource(recursoImagen);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImagenClick(recursoImagen);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imagenes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imagen;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imagen = itemView.findViewById(R.id.imagen_opcion_perfil);
        }
    }
}
