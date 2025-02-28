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
                //Si el login es correcto naveamos al RecibidosFragment que actúa como home
                navController.navigate(R.id.action_loginFragment_to_welcomeFragment);
            }else{
                try {
                    throw task.getException();
                } catch (FirebaseAuthInvalidUserException e) {
                    //Mensaje para cuando el correo no está registrado
                    binding.etEmailLogin.setError("El correo electrónico introducido no está registrado o es incorrecto");
                } catch (FirebaseAuthInvalidCredentialsException e) {
                    //Mensaje para cuando la contraseña no coincide con la del correo
                    binding.etPasswordLogin.setError("La contraseña introducida no es correcta");
                } catch (Exception e) {
                    //Mensaje para otro tipo de error
                    //He puesto el Log.e para ver el error en el logcat, siempre me da el mismo, por lo que sea FireBase no me devuelve el error especifico
                    //Siempre me dice lo mismo IVALID_LOGIN_CREDENTIALS
                    //Se puede hacer que cuando se de este error simplemente en el setError de ambos campos podemos poner usuario o contraseña incorrectos
                    //pero prefiero especificar mas el error. Preguntar a Jose Ángel
                    Log.e("LoginError", "Error de autenticación", e);
                    binding.etPasswordLogin.setError("Error: " + e.getMessage());
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
        binding = null; // Evitar memory leaks
    }
}
