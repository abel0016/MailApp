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
import com.example.mailapp.R;
import com.example.mailapp.databinding.FragmentCrearCorreoBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

public class CrearCorreoFragment extends Fragment {

    private static final String TAG = "CrearCorreoFragment";
    private FragmentCrearCorreoBinding binding;
    private FirebaseAuth mAuth;
    private NavController navController;
    private CorreoViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCrearCorreoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        navController = Navigation.findNavController(view);
        viewModel = new ViewModelProvider(this).get(CorreoViewModel.class);

        binding.btnEnviarCorreo.setOnClickListener(v -> {
            sendCorreo();
        });
    }

    private void sendCorreo() {
        String destinatarioEmail = binding.etDestinatario.getText().toString().trim();
        String asunto = binding.etAsunto.getText().toString().trim();
        String contenido = binding.etMensaje.getText().toString().trim();

        if (destinatarioEmail.isEmpty() || asunto.isEmpty() || contenido.isEmpty()) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.email_fields_empty_title)
                    .setMessage(R.string.email_fields_empty_message)
                    .setPositiveButton(R.string.ok_button, null)
                    .show();
            return;
        }

        if (mAuth.getCurrentUser() == null) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.auth_error_title)
                    .setMessage(R.string.auth_error_message)
                    .setPositiveButton(R.string.ok_button, null)
                    .show();
            return;
        }

        String remitenteId = mAuth.getCurrentUser().getUid();
        if (remitenteId == null) {
            Log.e(TAG, "El userId del usuario autenticado es null");
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.user_id_error_title)
                    .setMessage(R.string.user_id_error_message)
                    .setPositiveButton(R.string.ok_button, null)
                    .show();
            return;
        }

        viewModel.sendCorreo(remitenteId, destinatarioEmail, asunto, contenido).observe(getViewLifecycleOwner(), success -> {
            if (success) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.email_sent_success_title)
                        .setMessage(R.string.email_sent_success_message)
                        .setPositiveButton(R.string.ok_button, (dialog, which) -> {
                            navController.navigate(R.id.action_crearCorreoFragment_to_recibidosFragment);
                        })
                        .show();
            } else {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.email_send_error_title)
                        .setMessage(R.string.email_send_error_message)
                        .setPositiveButton(R.string.ok_button, null)
                        .show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}