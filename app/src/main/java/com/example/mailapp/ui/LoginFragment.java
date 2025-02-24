package com.example.mailapp.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.mailapp.R;
import com.example.mailapp.databinding.FragmentLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.textfield.TextInputLayout;

public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;
    private FirebaseAuth mAuth;
    private NavController navController;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Usar ViewBinding
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar Firebase y NavController
        mAuth = FirebaseAuth.getInstance();
        navController = Navigation.findNavController(view);

        // Botón de volver
        binding.btnBack.setOnClickListener(v -> navController.navigate(R.id.action_loginFragment_to_welcomeFragment));

        // Botón de inicio de sesión
        binding.btnLoginUser.setOnClickListener(v -> loginUsuario());
    }

    private void loginUsuario() {
        String email = binding.etEmailLogin.getText().toString().trim();
        String password = binding.etPasswordLogin.getText().toString().trim();

        // Limpiar errores previos
        limpiarErrores();

        boolean valido = true;

        if (TextUtils.isEmpty(email)) {
            binding.textInputLayoutEmailLogin.setError("Ingrese su correo electrónico");
            valido = false;
        }

        if (TextUtils.isEmpty(password)) {
            binding.textInputLayoutPasswordLogin.setError("Ingrese su contraseña");
            valido = false;
        }

        if (!valido) return;

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                navController.navigate(R.id.action_loginFragment_to_welcomeFragment);
            } else {
                binding.textInputLayoutEmailLogin.setError("Error: " + task.getException().getMessage());
            }
        });
    }

    private void limpiarErrores() {
        binding.textInputLayoutEmailLogin.setError(null);
        binding.textInputLayoutPasswordLogin.setError(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Evitar memory leaks
    }
}
