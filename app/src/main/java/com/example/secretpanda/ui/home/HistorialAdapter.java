package com.example.secretpanda.ui.home;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secretpanda.R;
import com.example.secretpanda.data.model.PartidaHistorial;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder> {

    private List<PartidaHistorial> listaPartidas;

    public HistorialAdapter(List<PartidaHistorial> listaPartidas) {
        this.listaPartidas = listaPartidas;
    }

    public void setLista(List<PartidaHistorial> nuevaLista) {
        this.listaPartidas = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historial, parent, false);
        return new HistorialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistorialViewHolder holder, int position) {
        PartidaHistorial partida = listaPartidas.get(position);

        // Código
        holder.txtCodigo.setText("SALA: " + partida.codigo_partida);

        String fechaMostrada = "---";
        if (partida.fechaFin != null && !partida.fechaFin.equals("null") && !partida.fechaFin.isEmpty()) {
            fechaMostrada = obtenerTiempoTranscurrido(partida.fechaFin);
        }
        holder.txtFecha.setText(fechaMostrada);
        // Mostrar Equipo y Rol
        String equipoStr = partida.equipo != null ? partida.equipo.toUpperCase() : "DESCONOCIDO";
        String rolStr = partida.rol != null ? partida.rol.toUpperCase() : "AGENTE";
        holder.txtRolEquipo.setText("EQUIPO " + equipoStr + " - " + rolStr);

        // Lógica de Victoria / Derrota
        if (partida.rojoGana == null) {
            holder.txtResultado.setText("FINALIZADA");
            holder.txtResultado.setTextColor(Color.GRAY);
        } else if ((partida.rojoGana && "rojo".equalsIgnoreCase(partida.equipo)) ||
                (!partida.rojoGana && "azul".equalsIgnoreCase(partida.equipo))) {
            holder.txtResultado.setText("VICTORIA");
            holder.txtResultado.setTextColor(Color.parseColor("#4CAF50")); // Verde
        } else {
            holder.txtResultado.setText("DERROTA");
            holder.txtResultado.setTextColor(Color.parseColor("#F44336")); // Rojo
        }

        // Estadísticas del líder
        if (partida.rol != null && (partida.rol.equalsIgnoreCase("lider") || partida.rol.equalsIgnoreCase("jefe"))) {
            holder.txtAciertos.setVisibility(View.VISIBLE);
            holder.txtAciertos.setText("ACIERTOS: " + partida.numAciertos + " | FALLOS: " + partida.numFallos);
        } else {
            holder.txtAciertos.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return listaPartidas == null ? 0 : listaPartidas.size();
    }

    private String obtenerTiempoTranscurrido(String fechaString) {
        if (fechaString == null || fechaString.isEmpty()) return "";
        try {
            String fechaLimpia = fechaString.replace(" ", "T"); // Unifica el separador
            if (fechaLimpia.contains(".")) {
                fechaLimpia = fechaLimpia.substring(0, fechaLimpia.indexOf(".")); // Quita milisegundos
            }
            fechaLimpia = fechaLimpia.replace("Z", ""); // Quita la Z de UTC si la trae

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date fechaPasada = sdf.parse(fechaLimpia);
            if (fechaPasada == null) return fechaString;

            long tiempoPasado = fechaPasada.getTime();
            long tiempoActual = System.currentTimeMillis();

            long diffSegundos = (tiempoActual - tiempoPasado) / 1000;

            if (diffSegundos < 0) return "Hace un momento"; // Por posibles desajustes de reloj
            if (diffSegundos < 60) return "Hace " + diffSegundos + " seg";

            long diffMinutos = diffSegundos / 60;
            if (diffMinutos < 60) return "Hace " + diffMinutos + " min";

            long diffHoras = diffMinutos / 60;
            if (diffHoras < 24) return "Hace " + diffHoras + " h";

            long diffDias = diffHoras / 24;
            if (diffDias < 30) return "Hace " + diffDias + " d";

            long diffMeses = diffDias / 30;
            if (diffMeses < 12) return "Hace " + diffMeses + " mes" + (diffMeses == 1 ? "" : "es");

            long diffAnios = diffMeses / 12;
            return "Hace " + diffAnios + " año" + (diffAnios == 1 ? "" : "s");

        } catch (Exception e) {
            e.printStackTrace();
            return fechaString; // Si hay error procesando, devolvemos la fecha original
        }
    }

    public static class HistorialViewHolder extends RecyclerView.ViewHolder {
        TextView txtResultado, txtFecha, txtCodigo, txtRolEquipo, txtAciertos;

        public HistorialViewHolder(@NonNull View itemView) {
            super(itemView);
            txtResultado = itemView.findViewById(R.id.item_historial_resultado);
            txtFecha = itemView.findViewById(R.id.item_historial_fecha);
            txtCodigo = itemView.findViewById(R.id.item_historial_codigo);
            txtRolEquipo = itemView.findViewById(R.id.item_historial_rol);
            txtAciertos = itemView.findViewById(R.id.item_historial_aciertos);
        }
    }
}