package com.example.mailapp.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;
    private FirebaseAuth mAuth;
    private NavController navController;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        navController = Navigation.findNavController(view);
        binding.btnBack.setOnClickListener(v -> navController.navigate(R.id.action_loginFragment_to_welcomeFragment));
        binding.btnLoginUser.setOnClickListener(v -> loginUsuario());
    }

    private void loginUsuario() {
        String email = binding.etEmailLogin.getText().toString().trim();
        String password = binding.etPasswordLogin.getText().toString().trim();
        limpiarErrores();
        boolean valido = true;

        if (TextUtils.isEmpty(email)) {
            binding.etEmailLogin.setError("Ingrese su correo electrónico");
            valido = false;
        }
        if (TextUtils.isEmpty(password)) {
            binding.etPasswordLogin.setError("Ingrese su contraseña");
            valido = false;
        }
        if (!valido) return;
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                navController.navigate(R.id.action_loginFragment_to_recibidosFragment);
            }else{
                try {
                    throw task.getException();
                }catch (Exception e) {
                    binding.etPasswordLogin.setError("Usuario o contraseña incorrectos");
                }
            }
            });
    }

    private void limpiarErrores() {
        binding.etEmailLogin.setError(null);
        binding.etPasswordLogin.setError(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
