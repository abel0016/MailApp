package com.example.mailapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.example.mailapp.R;
import com.example.mailapp.databinding.FragmentDetalleCorreoBinding;
import com.example.mailapp.model.Correo;
import com.example.mailapp.model.Usuario;
import com.example.mailapp.database.CorreoRepository;
import com.example.mailapp.database.UsuarioRepository;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DetalleCorreoFragment extends Fragment {

    private FragmentDetalleCorreoBinding binding;
    private CorreoRepository correoRepository;
    private UsuarioRepository usuarioRepository;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDetalleCorreoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        correoRepository = CorreoRepository.getInstance();
        usuarioRepository = UsuarioRepository.getInstance();

        loadCorreoDetails();
    }

    private void loadCorreoDetails() {
        String correoId = getArguments() != null ? getArguments().getString("correoId") : null;
        if (correoId == null) {
            binding.remitente.setText(R.string.no_correo_specified_error);
            binding.asunto.setText("");
            binding.fecha.setText("");
            binding.cuerpo.setText("");
            return;
        }

        correoRepository.getCorreoById(correoId, task -> {
            if (binding == null) return;

            if (task.isSuccessful() && task.getResult().exists()) {
                Correo correo = task.getResult().toObject(Correo.class);
                if (correo != null) {
                    usuarioRepository.getUsuarioById(correo.getRemitenteId())
                            .addOnSuccessListener(usuario -> {
                                if (binding == null) return;
                                if (usuario != null && usuario.getEmail() != null) {
                                    binding.remitente.setText("Remitente: " + usuario.getEmail());
                                } else {
                                    binding.remitente.setText(R.string.unknown_sender);
                                }
                            })
                            .addOnFailureListener(e -> {
                                if (binding == null) return;
                                binding.remitente.setText(R.string.load_error);
                            });

                    binding.asunto.setText("Asunto: " + correo.getAsunto());
                    binding.cuerpo.setText("Mensaje: " + correo.getContenido());
                    if (correo.getFechaEnvio() != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                        Date fecha = correo.getFechaEnvio().toDate();
                        binding.fecha.setText("Fecha: " + sdf.format(fecha));
                    } else {
                        binding.fecha.setText("Fecha: No disponible");
                    }
                } else {
                    binding.remitente.setText(R.string.correo_not_found_error);
                    binding.asunto.setText("");
                    binding.fecha.setText("");
                    binding.cuerpo.setText("");
                }
            } else {
                binding.remitente.setText(R.string.correo_load_error);
                binding.asunto.setText("");
                binding.fecha.setText("");
                binding.cuerpo.setText("");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}