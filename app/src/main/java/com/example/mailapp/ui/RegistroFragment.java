package com.example.mailapp.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.mailapp.R;
import com.example.mailapp.databinding.FragmentRegistroBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Calendar;
import java.util.HashMap;

public class RegistroFragment extends Fragment {
    private FragmentRegistroBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private NavController navController;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegistroBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Inicializar Firebase y NavController
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Usuarios");
        navController = Navigation.findNavController(view);

        binding.btnBack.setOnClickListener(v -> navController.navigate(R.id.action_registerFragment_to_welcomeFragment));
        binding.etFechaNacimiento.setOnClickListener(v -> mostrarDatePicker());
        binding.btnRegistrar.setOnClickListener(v -> registrarUsuario());
    }

    private void mostrarDatePicker() {
        // Obtener la fecha actual
        final Calendar calendario = Calendar.getInstance();
        int anno = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int dia = calendario.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    //Verificar si la fecha es válida
                    if (esFechaValida(year)) {
                        String fechaSeleccionada = dayOfMonth + "/" + (month + 1) + "/" + year;
                        binding.etFechaNacimiento.setText(fechaSeleccionada);
                        binding.layoutFechaNacimiento.setError(null);
                    } else {
                        binding.layoutFechaNacimiento.setError("Selecciona una fecha válida (entre " + 12 + " y " + 100 + " años)");
                        binding.etFechaNacimiento.setText("");
                    }
                },
                anno, mes, dia
        );
        datePickerDialog.show();
    }
    //validación para la fecha de nacimiento
    private boolean esFechaValida(int year) {
        int annoActual = Calendar.getInstance().get(Calendar.YEAR);
        int edad = annoActual - year;
        return edad >= 12 && edad <= 100;
    }

    private void registrarUsuario() {
        String nombre = binding.etNombre.getText().toString().trim();
        String fechaNacimiento = binding.etFechaNacimiento.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        limpiarErrores();

        //Validaciones
        boolean valido = true;
        if (TextUtils.isEmpty(nombre)) {
            binding.layoutNombre.setError("Ingrese su nombre y apellidos");
            valido = false;
        }
        if (TextUtils.isEmpty(fechaNacimiento)) {
            binding.layoutFechaNacimiento.setError("Seleccione su fecha de nacimiento");
            valido = false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutEmail.setError("Ingrese un correo válido");
            valido = false;
        }
        if (password.length() < 6) {
            binding.layoutPassword.setError("La contraseña debe tener al menos 6 caracteres");
            valido = false;
        }
        if (!password.equals(confirmPassword)) {
            binding.layoutConfirmPassword.setError("Las contraseñas no coinciden");
            valido = false;
        }
        if (!valido) return;

        //Registrar usuario en Firebase
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String userId = mAuth.getCurrentUser().getUid();

                //Guardar datos en Firebase Database
                HashMap<String, Object> usuario = new HashMap<>();
                usuario.put("nombre", nombre);
                usuario.put("fechaNacimiento", fechaNacimiento);
                usuario.put("email", email);

                mDatabase.child(userId).setValue(usuario).addOnCompleteListener(dbTask -> {
                    if (dbTask.isSuccessful()) {
                        navController.navigate(R.id.action_registerFragment_to_welcomeFragment);
                    } else {
                        binding.layoutEmail.setError("Error al guardar los datos");
                    }
                });

            } else {
                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                    binding.layoutEmail.setError("Este correo ya está registrado");
                } else {
                    binding.layoutEmail.setError("Error: " + task.getException().getMessage());
                }
            }
        });
    }

    private void limpiarErrores() {
        binding.layoutNombre.setError(null);
        binding.layoutFechaNacimiento.setError(null);
        binding.layoutEmail.setError(null);
        binding.layoutPassword.setError(null);
        binding.layoutConfirmPassword.setError(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
