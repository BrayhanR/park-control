
package com.example.parkcontrol;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VehiculosAdapter extends RecyclerView.Adapter<VehiculosAdapter.VehiculoViewHolder> {

    private final List<String[]> vehiculos;

    public VehiculosAdapter(List<String[]> vehiculos) {
        this.vehiculos = vehiculos;
    }

    @NonNull
    @Override
    public VehiculoViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vehiculo, parent, false);

        return new VehiculoViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(
            @NonNull VehiculoViewHolder holder, int position) {

        String[] vehiculo = vehiculos.get(position);

        holder.tvId.setText(vehiculo[0]);
        holder.tvPlaca.setText(vehiculo[1]);
        holder.tvTipo.setText(vehiculo[2]);
        holder.tvMarca.setText(vehiculo[3]);
        holder.tvColor.setText(vehiculo[4]);
    }

    @Override
    public int getItemCount() {
        return vehiculos.size();
    }

    static class VehiculoViewHolder extends RecyclerView.ViewHolder {

        TextView tvId, tvPlaca, tvTipo, tvMarca, tvColor;

        public VehiculoViewHolder(@NonNull View itemView) {
            super(itemView);

            tvId = itemView.findViewById(R.id.tvId);
            tvPlaca = itemView.findViewById(R.id.tvPlaca);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            tvMarca = itemView.findViewById(R.id.tvMarca);
            tvColor = itemView.findViewById(R.id.tvColor);
        }
    }
}
