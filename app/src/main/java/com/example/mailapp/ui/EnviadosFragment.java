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
import com.google.firebase.auth.FirebaseAuth;

public class EnviadosFragment extends Fragment {
    private static final String TAG = "EnviadosFragment";
    private FragmentEnviadosBinding binding;
    private CorreoAdapter adapter;
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

        navController = Navigation.findNavController(view);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CorreoAdapter(correo -> {
            Bundle args = new Bundle();
            args.putString("correoId", correo.getId());
            navController.navigate(R.id.action_enviadosFragment_to_detalleCorreoFragment, args);
        }, false);
        binding.recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(CorreoViewModel.class);
        loadEnviados();
    }

    private void loadEnviados() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) {
            Log.e(TAG, "Usuario no autenticado, userId es null");
            if (binding != null) {
                binding.tvMensaje.setVisibility(View.VISIBLE);
                binding.tvMensaje.setText(R.string.no_auth_error);
            }
            return;
        }
        Log.d(TAG, "Cargando correos enviados por emisor: " + userId);
        viewModel.getCorreosEnviadosLiveData(userId).observe(getViewLifecycleOwner(), correos -> {
            if (binding == null) return;
            adapter.setCorreoList(correos);
            if (correos == null || correos.isEmpty()) {
                binding.tvMensaje.setVisibility(View.VISIBLE);
                binding.tvMensaje.setText(R.string.no_enviados_message);
            } else {
                binding.tvMensaje.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}