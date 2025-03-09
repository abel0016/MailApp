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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginFragment extends Fragment {
    private static final String TAG = "LoginFragment";
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

        // Verificar el usuario actual al cargar el fragmento
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "Usuario actual al cargar LoginFragment: " + currentUser.getEmail() + " (UID: " + currentUser.getUid() + ")");
        } else {
            Log.d(TAG, "No hay usuario autenticado al cargar LoginFragment");
        }

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

        // Mostrar las credenciales que se están intentando usar
        Log.d(TAG, "Intentando iniciar sesión con email: " + email + ", contraseña: [oculta]");

        // Cerrar sesión del usuario anterior
        FirebaseUser userBeforeSignOut = mAuth.getCurrentUser();
        if (userBeforeSignOut != null) {
            Log.d(TAG, "Usuario antes de signOut: " + userBeforeSignOut.getEmail() + " (UID: " + userBeforeSignOut.getUid() + ")");
        } else {
            Log.d(TAG, "No hay usuario antes de signOut");
        }
        mAuth.signOut();

        // Verificar que signOut se realizó correctamente
        if (mAuth.getCurrentUser() == null) {
            Log.d(TAG, "signOut exitoso: No hay usuario autenticado");
        } else {
            Log.e(TAG, "signOut falló: Todavía hay un usuario autenticado: " + mAuth.getCurrentUser().getEmail());
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "Inicio de sesión exitoso: " + user.getEmail() + " (UID: " + user.getUid() + ")");
                            // Verificar que el usuario autenticado coincida con el correo ingresado
                            if (!user.getEmail().equalsIgnoreCase(email)) {
                                Log.e(TAG, "El usuario autenticado no coincide con el correo ingresado. Esperado: " + email + ", Obtenido: " + user.getEmail());
                                mAuth.signOut(); // Cerrar sesión si hay un mismatch
                                binding.etPasswordLogin.setError("Error: usuario incorrecto autenticado. Intente de nuevo.");
                                return;
                            }
                            navController.navigate(R.id.action_loginFragment_to_recibidosFragment);
                        } else {
                            Log.e(TAG, "Inicio de sesión exitoso pero getCurrentUser es null");
                            binding.etPasswordLogin.setError("Error al autenticar. Intente de nuevo.");
                        }
                    } else {
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthInvalidUserException e) {
                            Log.w(TAG, "Usuario no encontrado", e);
                            binding.etEmailLogin.setError("El usuario no existe. Regístrese primero.");
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            Log.w(TAG, "Credenciales inválidas", e);
                            binding.etPasswordLogin.setError("Correo o contraseña incorrectos.");
                        } catch (Exception e) {
                            Log.e(TAG, "Error inesperado en el login", e);
                            binding.etPasswordLogin.setError("Error al iniciar sesión: " + e.getMessage());
                        }
                    }
                });
    }

    private void limpiarErrores() {
        binding.etEmailLogin.setError(null);
        binding.etPasswordLogin.setError(null);
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