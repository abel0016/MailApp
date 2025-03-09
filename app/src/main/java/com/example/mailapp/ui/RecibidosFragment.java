package com.example.mailapp.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.mailapp.R;
import com.example.mailapp.databinding.FragmentRecibidosBinding;
import com.example.mailapp.model.Correo;
import com.example.mailapp.database.CorreoRepository;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;

public class RecibidosFragment extends Fragment {

    private FragmentRecibidosBinding binding;
    private CorreoAdapter correoAdapter;
    private ArrayList<Correo> correoList;
    private CorreoRepository correoRepository;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRecibidosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        correoList = new ArrayList<>();
        correoAdapter = new CorreoAdapter(correoList, correo -> {
            Bundle bundle = new Bundle();
            bundle.putString("correoId", correo.getId());
            Navigation.findNavController(view).navigate(R.id.action_recibidosFragment_to_detalleCorreoFragment, bundle);
        });
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(correoAdapter);

        correoRepository = CorreoRepository.getInstance();
        auth = FirebaseAuth.getInstance();
        cargarCorreosRecibidos();

        // Añadir OnClickListener al botón de nuevo correo
        binding.btnNuevoCorreo.setOnClickListener(v -> {
            Log.d("RecibidosFragment", "Botón Nuevo Correo presionado");
            Navigation.findNavController(view).navigate(R.id.action_recibidosFragment_to_crearCorreoFragment);
        });
    }

    private void cargarCorreosRecibidos() {
        String emailUsuario = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : null;
        if (emailUsuario == null) {
            Log.e("RecibidosFragment", "No hay usuario autenticado");
            if (binding != null) {
                binding.tvMensaje.setVisibility(View.VISIBLE);
                binding.tvMensaje.setText(R.string.no_auth_error);
            }
            return;
        }

        Log.d("RecibidosFragment", "Cargando correos para destinatario: " + emailUsuario);

        correoRepository.getCorreosRecibidos(emailUsuario).observe(getViewLifecycleOwner(), correos -> {
            if (binding == null) return;

            correoList.clear();
            if (correos != null && !correos.isEmpty()) {
                correoList.addAll(correos);
                binding.tvMensaje.setVisibility(View.GONE);
                Log.d("RecibidosFragment", "Se encontraron " + correoList.size() + " correos recibidos");
            } else {
                binding.tvMensaje.setVisibility(View.VISIBLE);
                binding.tvMensaje.setText(R.string.no_recibidos_message);
                Log.d("RecibidosFragment", "No se encontraron correos recibidos para " + emailUsuario);
            }
            correoAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}