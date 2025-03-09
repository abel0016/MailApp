package com.example.mailapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.mailapp.R;
import com.example.mailapp.databinding.FragmentWelcomeBinding;

public class WelcomeFragment extends Fragment {
    private FragmentWelcomeBinding binding;
    private NavController navController;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWelcomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        binding.btnLogin.setOnClickListener(v -> navController.navigate(R.id.action_welcomeFragment_to_loginFragment));
        binding.btnRegister.setOnClickListener(v -> navController.navigate(R.id.action_welcomeFragment_to_registroFragment));
    }

    @Override
    public void onStart() {
        super.onStart();
        // Ocultar el Toolbar cuando el fragmento se muestra
        requireActivity().findViewById(R.id.toolbar).setVisibility(View.GONE);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Restaurar la visibilidad del Toolbar cuando el fragmento se oculta
        View toolbar = requireActivity().findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}