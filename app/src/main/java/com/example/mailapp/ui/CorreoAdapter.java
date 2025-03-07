package com.example.mailapp.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mailapp.databinding.ItemCorreoBinding;
import com.example.mailapp.models.Correo;
import java.util.List;

public class CorreoAdapter extends RecyclerView.Adapter<CorreoAdapter.ViewHolder> {

    public interface OnCorreoClickListener {
        void onCorreoClick(Correo correo);
    }

    private List<Correo> correos;
    private OnCorreoClickListener listener;

    public CorreoAdapter(List<Correo> correos, OnCorreoClickListener listener) {
        this.correos = correos;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemCorreoBinding binding;

        public ViewHolder(@NonNull ItemCorreoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Correo correo, OnCorreoClickListener listener) {
            binding.asunto.setText(correo.getAsunto());
            binding.remitente.setText(correo.getRemitenteId());
            binding.fecha.setText(correo.getFechaEnvio());

            itemView.setOnClickListener(v -> listener.onCorreoClick(correo));
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCorreoBinding binding = ItemCorreoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Correo correo = correos.get(position);
        holder.bind(correo, listener);
    }

    @Override
    public int getItemCount() {
        return correos.size();
    }

    public void actualizarLista(List<Correo> nuevaLista) {
        this.correos = nuevaLista;
        notifyDataSetChanged();
    }
}