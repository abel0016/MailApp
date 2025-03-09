package com.example.mailapp.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mailapp.database.UsuarioRepository;
import com.example.mailapp.databinding.ItemCorreoBinding;
import com.example.mailapp.model.Correo;
import com.example.mailapp.model.Usuario;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class CorreoAdapter extends RecyclerView.Adapter<CorreoAdapter.CorreoViewHolder> {

    private List<Correo> correoList;
    private OnCorreoClickListener listener;
    private boolean isRecibidos;
    private final UsuarioRepository usuarioRepository;

    public CorreoAdapter(OnCorreoClickListener listener, boolean isRecibidos) {
        this.correoList = new ArrayList<>();
        this.listener = listener;
        this.isRecibidos = isRecibidos;
        this.usuarioRepository = UsuarioRepository.getInstance(); // Usar getInstance()
    }

    public CorreoAdapter(List<Correo> correoList, OnCorreoClickListener listener) {
        this.correoList = correoList != null ? correoList : new ArrayList<>();
        this.listener = listener;
        this.isRecibidos = true;
        this.usuarioRepository = UsuarioRepository.getInstance(); // Usar getInstance()
    }

    public void setCorreoList(List<Correo> correos) {
        this.correoList = correos != null ? correos : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public CorreoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemCorreoBinding binding = ItemCorreoBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CorreoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(CorreoViewHolder holder, int position) {
        Correo correo = correoList.get(position);
        holder.bind(correo);
    }

    @Override
    public int getItemCount() {
        return correoList.size();
    }

    class CorreoViewHolder extends RecyclerView.ViewHolder {
        private final ItemCorreoBinding binding;

        CorreoViewHolder(ItemCorreoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onCorreoClick(correoList.get(position));
                }
            });
        }

        void bind(Correo correo) {
            if (isRecibidos) {
                usuarioRepository.getUsuarioById(correo.getRemitenteId())
                        .addOnSuccessListener(userDoc -> {
                            if (userDoc.exists()) {
                                Usuario usuario = userDoc.toObject(Usuario.class);
                                if (usuario != null && usuario.getEmail() != null) {
                                    binding.remitente.setText("De: " + usuario.getEmail());
                                } else {
                                    binding.remitente.setText("De: Desconocido");
                                }
                            } else {
                                binding.remitente.setText("De: Usuario no encontrado");
                            }
                        })
                        .addOnFailureListener(e -> {
                            binding.remitente.setText("De: Error al cargar");
                        });
            } else {
                binding.remitente.setText("Para: " + correo.getDestinatarioEmail());
            }

            binding.asunto.setText(correo.getAsunto());
            if (correo.getFechaEnvio() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                binding.fecha.setText(sdf.format(correo.getFechaEnvio().toDate()));
            } else {
                binding.fecha.setText("Fecha no disponible");
            }
        }
    }

    public interface OnCorreoClickListener {
        void onCorreoClick(Correo correo);
    }
}