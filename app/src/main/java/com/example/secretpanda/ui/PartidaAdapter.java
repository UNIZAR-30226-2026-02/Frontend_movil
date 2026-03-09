package com.example.secretpanda.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secretpanda.R;
import com.example.secretpanda.data.model.Partida;

import java.util.List;

public class PartidaAdapter extends RecyclerView.Adapter<PartidaAdapter.PartidaViewHolder> {

    private List<Partida> listaPartidas;

    public PartidaAdapter(List<Partida> listaPartidas) {
        this.listaPartidas = listaPartidas;
    }

    @NonNull
    @Override
    public PartidaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Recuerda renombrar tu XML de item_mision_publica a item_partida_publica
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mision_publica, parent, false);
        return new PartidaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PartidaViewHolder holder, int position) {
        Partida
 partida = listaPartidas.get(position);

        holder.tvNombre.setText(partida.getNombre());
        holder.tvCreador.setText(partida.getCreador());
        holder.tvTiempo.setText(partida.getTiempo());
        holder.tvJugadores.setText(partida.getJugadores());

        if (partida.isBloqueada()) {
            holder.contenedor.setBackgroundResource(R.drawable.fondo_item_mision_bloqueada);
            holder.tvNombre.setTextColor(0xFFB71C1C);
            holder.tvNombre.setText(partida.getNombre() + " (no desbloqueada)");
        } else {
            holder.contenedor.setBackgroundResource(R.drawable.fondo_item_mision_publica);
            holder.tvNombre.setTextColor(0xFF000000);
        }
        holder.itemView.setOnClickListener(v -> {

            // 1. Comprobamos si la partida está llena
            boolean estaLlena = false;
            if (partida.getJugadores() != null) {
                String[] cantidadJugadores = partida.getJugadores().split("/");
                if (cantidadJugadores.length == 2 && cantidadJugadores[0].equals(cantidadJugadores[1])) {
                    estaLlena = true;
                }
            }

            // 2. Si está bloqueada o llena, mostramos el diálogo
            if (partida.isBloqueada() || estaLlena) {
                android.app.Dialog dialogError = new android.app.Dialog(v.getContext());
                dialogError.setContentView(R.layout.dialog_error_mision);

                if (dialogError.getWindow() != null) {
                    dialogError.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                }

                dialogError.findViewById(R.id.btn_cerrar_dialogo).setOnClickListener(viewCerrar -> {
                    dialogError.dismiss();
                });

                dialogError.show();

            } else {
                // 3. ¡LA MAGIA AQUÍ! Si está libre, viajamos a la Sala de Espera
                android.content.Intent intent = new android.content.Intent(v.getContext(), SalaEsperaActivity.class);

                // (Opcional) Podemos enviarle datos a la nueva pantalla, por ejemplo, el nombre de la temática
                intent.putExtra("TEMATICA_PARTIDA", partida.getTematica());

                // Arrancamos la nueva Activity
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaPartidas.size();
    }

    class PartidaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvCreador, tvTiempo, tvJugadores;
        LinearLayout contenedor;

        public PartidaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tv_nombre_mision); // Puedes dejar los IDs o cambiarlos también
            tvCreador = itemView.findViewById(R.id.tv_creador_mision);
            tvTiempo = itemView.findViewById(R.id.tv_tiempo_mision);
            tvJugadores = itemView.findViewById(R.id.tv_jugadores_mision);
            contenedor = itemView.findViewById(R.id.contenedor_item);
        }
    }
}
