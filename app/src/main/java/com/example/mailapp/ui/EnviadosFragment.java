package com.example.mailapp.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mailapp.R;
import com.example.mailapp.databinding.FragmentEnviadosBinding;
import com.example.mailapp.models.Correo;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class EnviadosFragment extends Fragment {

    private static final String TAG = "EnviadosFragment";
    private FragmentEnviadosBinding binding;
    private CorreoAdapter adapter;
    private FirebaseAuth mAuth;
    private NavController navController;
    private CorreoViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEnviadosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        navController = Navigation.findNavController(view);
        viewModel = new ViewModelProvider(this).get(CorreoViewModel.class);

        adapter = new CorreoAdapter(new ArrayList<>(), correo -> {
            Log.d(TAG, "Correo seleccionado: " + correo.getAsunto());
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);

        binding.btnNuevoCorreo.setOnClickListener(v -> {
            navController.navigate(R.id.action_enviadosFragment_to_crearCorreoFragment);
        });

        viewModel.getCorreosEnviadosLiveData().observe(getViewLifecycleOwner(), correos -> {
            if (correos != null) {
                if (correos.isEmpty()) {
                    binding.tvMensaje.setText("No tienes correos enviados");
                    binding.tvMensaje.setVisibility(View.VISIBLE);
                } else {
                    binding.tvMensaje.setVisibility(View.GONE);
                    adapter.actualizarLista(correos);
                }
            }
        });

        //Recargar correos al volver al fragmento
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.enviadosFragment) {
                cargarCorreosEnviados();
            }
        });

        cargarCorreosEnviados();
    }

    private void cargarCorreosEnviados() {
        if (mAuth.getCurrentUser() == null) {
            Log.e(TAG, "Usuario no autenticado");
            binding.tvMensaje.setText("Usuario no autenticado");
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        viewModel.cargarCorreosEnviados(userId);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}