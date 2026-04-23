package com.example.secretpanda.ui.game.join;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secretpanda.R;
import com.example.secretpanda.ui.audio.EfectosManager;

import java.util.List;

public class TematicaAdapter extends RecyclerView.Adapter<TematicaAdapter.TematicaViewHolder> {

    private List<String> listaTematicas;
    private TematicasDialogFragment.TematicaListener listener;
    private TematicasDialogFragment dialog;

    public TematicaAdapter(List<String> listaTematicas, TematicasDialogFragment.TematicaListener listener, TematicasDialogFragment dialog) {
        this.listaTematicas = listaTematicas;
        this.listener = listener;
        this.dialog = dialog;
    }

    @NonNull
    @Override
    public TematicaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tematica_tarjeta, parent, false);
        return new TematicaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TematicaViewHolder holder, int position) {
        String tematica = listaTematicas.get(position);

        holder.tvNombre.setText(tematica);

        holder.txtEmoji.setText(emojiPaquete(tematica));

        holder.itemView.setOnClickListener(v -> {
            EfectosManager.reproducir(v.getContext(), R.raw.sonido_click);
            if (listener != null) {
                listener.onTematicaSelected(tematica);
            }
            if (dialog != null) {
                dialog.dismiss();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaTematicas.size();
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

        if (n.contains("Todas")) return "🌍";

        return "🎴"; // Por defecto
    }

    class TematicaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView txtEmoji;

        public TematicaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tv_nombre_tematica);
            txtEmoji = itemView.findViewById(R.id.txt_emoji_tematica); // 🔥 Lo enlazamos
        }
    }
}