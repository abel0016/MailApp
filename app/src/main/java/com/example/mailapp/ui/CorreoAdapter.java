package com.example.mailapp.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mailapp.R;
import com.example.mailapp.models.Correo;
import com.example.mailapp.databinding.ItemCorreoBinding;
import java.util.List;

public class CorreoAdapter extends RecyclerView.Adapter<CorreoAdapter.ViewHolder> {

    private List<Correo> correos;

    public CorreoAdapter(List<Correo> correos) {
        this.correos = correos;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemCorreoBinding binding;

        public ViewHolder(@NonNull ItemCorreoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflar el layout usando View Binding
        ItemCorreoBinding binding = ItemCorreoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Correo correo = correos.get(position);
        holder.binding.asunto.setText(correo.getAsunto());
        holder.binding.remitente.setText(correo.getRemitente());
        holder.binding.fecha.setText(correo.getFecha());
    }

    @Override
    public int getItemCount() {
        return correos.size();
    }
}