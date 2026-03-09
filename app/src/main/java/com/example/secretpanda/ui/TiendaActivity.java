package com.example.secretpanda.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secretpanda.R;
import com.example.secretpanda.data.model.InventarioGlobal;
import com.example.secretpanda.data.model.ItemPersonalizacion;
import com.example.secretpanda.ui.home.HomeActivity;

import java.util.ArrayList;
import java.util.List;

public class TiendaActivity extends AppCompatActivity {

    private RecyclerView recyclerBarajas, recyclerBordes, recyclerFondos;
    private TiendaAdapter adapterBarajas, adapterBordes, adapterFondos;
    private TextView txtSaldo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_tienda);

        txtSaldo = findViewById(R.id.txt_saldo_balas);
        configurarNavegacionInferior();

        recyclerBarajas = findViewById(R.id.recycler_tienda_barajas);
        recyclerBordes = findViewById(R.id.recycler_tienda_bordes);
        recyclerFondos = findViewById(R.id.recycler_tienda_fondos);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cargamos los datos cada vez que abrimos la pantalla
        actualizarTextoSaldo();
        cargarDatosTienda();
    }

    private void actualizarTextoSaldo() {
        txtSaldo.setText(String.valueOf(InventarioGlobal.getInstance().getMisBalas()));
    }

    private void cargarDatosTienda() {
        List<ItemPersonalizacion> todos = InventarioGlobal.getInstance().getTodosLosItems();

        List<ItemPersonalizacion> barajasTienda = new ArrayList<>();
        List<ItemPersonalizacion> bordesTienda = new ArrayList<>();
        List<ItemPersonalizacion> fondosTienda = new ArrayList<>();

        // Filtramos para la tienda (solo los que tienen precio > 0)
        for (ItemPersonalizacion item : todos) {
            if (item.getPrecio() > 0) {
                if (item.getTipo().equals("baraja")) barajasTienda.add(item);
                else if (item.getTipo().equals("borde")) bordesTienda.add(item);
                else if (item.getTipo().equals("fondo")) fondosTienda.add(item);
            }
        }

        adapterBarajas = new TiendaAdapter(barajasTienda, (item, position) -> mostrarPreviewCompra(item, position, adapterBarajas));
        adapterBordes = new TiendaAdapter(bordesTienda, (item, position) -> mostrarPreviewCompra(item, position, adapterBordes));
        adapterFondos = new TiendaAdapter(fondosTienda, (item, position) -> mostrarPreviewCompra(item, position, adapterFondos));

        recyclerBarajas.setAdapter(adapterBarajas);
        recyclerBordes.setAdapter(adapterBordes);
        recyclerFondos.setAdapter(adapterFondos);
    }

    private void mostrarPreviewCompra(ItemPersonalizacion item, int position, TiendaAdapter adaptadorOrigen) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_preview_tienda, null);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setView(dialogView);
        android.app.AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView txtTitulo = dialogView.findViewById(R.id.txt_preview_titulo_tienda);
        ImageView btnCerrar = dialogView.findViewById(R.id.btn_cerrar_preview_tienda);
        ImageView imgPreview = dialogView.findViewById(R.id.img_preview_item_tienda);
        LinearLayout btnComprar = dialogView.findViewById(R.id.btn_comprar_preview);
        TextView txtPrecioBoton = dialogView.findViewById(R.id.txt_precio_boton_compra);

        txtTitulo.setText(item.getNombre());
        txtPrecioBoton.setText(String.valueOf(item.getPrecio()));

        if (item.getIconoResId() != 0) imgPreview.setImageResource(item.getIconoResId());
        else imgPreview.setImageResource(R.drawable.fondo_carta_gruesa);

        btnComprar.setOnClickListener(v -> {
            if (InventarioGlobal.getInstance().getMisBalas() >= item.getPrecio()) {
                // 1. Restamos balas y actualizamos marcador
                InventarioGlobal.getInstance().restarBalas(item.getPrecio());
                actualizarTextoSaldo();

                // 2. ¡Desbloqueamos el ítem! (Magia para la Base de Datos)
                item.setBloqueado(false);

                // 3. Avisamos al adaptador para que dibuje el Tick verde
                adaptadorOrigen.notifyItemChanged(position);

                Toast.makeText(this, "¡Has adquirido " + item.getNombre() + "!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "No tienes suficientes balas.", Toast.LENGTH_SHORT).show();
            }
        });

        btnCerrar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void configurarNavegacionInferior() {
        LinearLayout btnNavInicio = findViewById(R.id.nav_inicio);
        if (btnNavInicio != null) btnNavInicio.setOnClickListener(v -> {
            startActivity(new Intent(TiendaActivity.this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            overridePendingTransition(0, 0);
        });

        // ¡REVISA QUE EL ID AQUÍ SEA EL CORRECTO PARA TU BOTON COLECCIÓN!
        LinearLayout btnNavPersonalizacion = findViewById(R.id.nav_personalizar);
        if (btnNavPersonalizacion != null) btnNavPersonalizacion.setOnClickListener(v -> {
            startActivity(new Intent(TiendaActivity.this, PersonalizacionActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            overridePendingTransition(0, 0);
        });
    }
}