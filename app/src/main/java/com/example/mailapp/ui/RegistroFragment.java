package com.example.mailapp.ui;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.mailapp.MainActivity;
import com.example.mailapp.R;
import com.example.mailapp.database.UsuarioRepository;
import com.example.mailapp.databinding.FragmentRegistroBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.UUID;

import okhttp3.*;

public class RegistroFragment extends Fragment {

    private static final String TAG = "RegistroFragment";
    private static final String SUPABASE_URL = "https://opvcrulzhtnetrajjctt.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9wdmNydWx6aHRuZXRyYWpqY3R0Iiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc0MTMxMDc5NCwiZXhwIjoyMDU2ODg2Nzk0fQ.15tav7RUlZJvEsyXTxJHOSQT0YzYMFrE8-uI5YHG9ck";
    private static final String BUCKET_NAME = "imagenes";
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 100;

    private FragmentRegistroBinding binding;
    private FirebaseAuth mAuth;
    private NavController navController;
    private Uri fotoUri;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegistroBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "Fragmento creado");

        mAuth = FirebaseAuth.getInstance();
        navController = Navigation.findNavController(view);

        binding.btnBack.setOnClickListener(v -> navController.navigate(R.id.action_registroFragment_to_welcomeFragment));
        binding.etFechaNacimiento.setOnClickListener(v -> mostrarDatePicker());
        binding.btnRegistrar.setOnClickListener(v -> registrarUsuario());
        binding.btnSeleccionarFoto.setOnClickListener(v -> seleccionarFoto());
    }

    private void seleccionarFoto() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;
        int permissionCheck = ContextCompat.checkSelfPermission(requireContext(), permission);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{permission}, STORAGE_PERMISSION_REQUEST_CODE);
        } else {
            abrirGaleria();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirGaleria();
            } else {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.permission_required_title)
                        .setMessage(R.string.permission_required_message)
                        .setPositiveButton(R.string.go_to_settings, (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        }
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == getActivity().RESULT_OK && data != null) {
            fotoUri = data.getData();
            binding.imgPerfil.setImageURI(fotoUri);
            Log.d(TAG, "Foto seleccionada: " + fotoUri.toString());
        } else {
            Log.w(TAG, "No se seleccionó ninguna foto o hubo un error: requestCode=" + requestCode + ", resultCode=" + resultCode);
        }
    }

    private void mostrarDatePicker() {
        final Calendar calendario = Calendar.getInstance();
        int anno = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int dia = calendario.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    if (esFechaValida(year)) {
                        String fecha = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                        binding.etFechaNacimiento.setText(fecha);
                        binding.layoutFechaNacimiento.setError(null); // Limpiar error si la fecha es válida
                    } else {
                        binding.etFechaNacimiento.setText("");
                        binding.layoutFechaNacimiento.setError(getString(R.string.invalid_date_error));
                    }
                },
                anno, mes, dia
        );
        datePickerDialog.show();
    }

    private boolean esFechaValida(int year) {
        int edad = Calendar.getInstance().get(Calendar.YEAR) - year;
        return edad >= 12 && edad <= 100;
    }

    private void limpiarErrores() {
        binding.layoutNombre.setError(null);
        binding.layoutFechaNacimiento.setError(null);
        binding.layoutEmail.setError(null);
        binding.layoutPassword.setError(null);
        binding.layoutConfirmPassword.setError(null);
    }

    private boolean validarCampos() {
        boolean esValido = true;
        limpiarErrores();

        String nombre = binding.etNombre.getText().toString().trim();
        String fechaNacimiento = binding.etFechaNacimiento.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        // Validar nombre
        if (TextUtils.isEmpty(nombre)) {
            binding.layoutNombre.setError(getString(R.string.name_empty_error));
            esValido = false;
        }

        // Validar fecha de nacimiento
        if (TextUtils.isEmpty(fechaNacimiento)) {
            binding.layoutFechaNacimiento.setError(getString(R.string.birth_date_empty_error));
            esValido = false;
        }

        // Validar correo
        if (TextUtils.isEmpty(email)) {
            binding.layoutEmail.setError(getString(R.string.email_empty_error));
            esValido = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutEmail.setError(getString(R.string.email_invalid_error));
            esValido = false;
        }

        // Validar contraseña
        if (TextUtils.isEmpty(password)) {
            binding.layoutPassword.setError(getString(R.string.password_empty_error));
            esValido = false;
        }

        // Validar confirmación de contraseña
        if (TextUtils.isEmpty(confirmPassword)) {
            binding.layoutConfirmPassword.setError(getString(R.string.confirm_password_empty_error));
            esValido = false;
        } else if (!password.equals(confirmPassword)) {
            binding.layoutConfirmPassword.setError(getString(R.string.password_mismatch_error));
            esValido = false;
        }

        return esValido;
    }

    private void registrarUsuario() {
        // Validar todos los campos antes de proceder
        if (!validarCampos()) {
            return;
        }

        String nombre = binding.etNombre.getText().toString().trim();
        String fechaNacimiento = binding.etFechaNacimiento.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String userId = mAuth.getCurrentUser().getUid();
                Log.d(TAG, "Usuario registrado con éxito, userId: " + userId);
                if (fotoUri != null) {
                    Log.d(TAG, "Intentando subir foto para userId: " + userId);
                    subirFotoASupabase(fotoUri, userId, photoUrl -> {
                        Log.d(TAG, "Callback recibido con photoUrl: " + (photoUrl != null ? photoUrl : "null"));
                        if (photoUrl != null) {
                            guardarUsuarioEnRepository(userId, nombre, fechaNacimiento, email, photoUrl);
                        } else {
                            Log.w(TAG, "URL de foto nula, guardando sin foto");
                            guardarUsuarioEnRepository(userId, nombre, fechaNacimiento, email, "");
                        }
                    });
                } else {
                    Log.d(TAG, "Sin foto seleccionada, guardando usuario sin foto");
                    guardarUsuarioEnRepository(userId, nombre, fechaNacimiento, email, "");
                }
            } else {
                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                    binding.layoutEmail.setError(getString(R.string.email_already_registered_error));
                } else {
                    String errorMessage = String.format(getString(R.string.registration_error_message), task.getException().getMessage());
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle(R.string.registration_error_title)
                            .setMessage(errorMessage)
                            .setPositiveButton(R.string.ok_button, null)
                            .show();
                }
            }
        });
    }

    private void guardarUsuarioEnRepository(String id, String nombre, String fechaNacimiento, String email, String photoUrl) {
        Log.d(TAG, "Guardando usuario en Firestore con photoUrl: " + photoUrl);
        UsuarioRepository repository = UsuarioRepository.getInstance();
        repository.guardarUsuario(id, nombre, fechaNacimiento, email, photoUrl)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Usuario guardado en Firestore con éxito, foto_url: " + photoUrl);
                    requireActivity().runOnUiThread(() -> {
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle(R.string.registration_success_title)
                                .setMessage(R.string.registration_success_message)
                                .setPositiveButton(R.string.ok_button, (dialog, which) -> {
                                    // Forzar actualización del Drawer antes de navegar
                                    if (requireActivity() instanceof MainActivity) {
                                        ((MainActivity) requireActivity()).actualizarDrawer();
                                    }
                                    navController.navigate(R.id.action_registroFragment_to_loginFragment);
                                })
                                .show();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar usuario en Firestore: " + e.getMessage(), e);
                    requireActivity().runOnUiThread(() ->
                            new MaterialAlertDialogBuilder(requireContext())
                                    .setTitle(R.string.firestore_error_title)
                                    .setMessage(R.string.firestore_error_message)
                                    .setPositiveButton(R.string.ok_button, null)
                                    .show());
                });
    }

    private void subirFotoASupabase(Uri uri, String userId, UploadCallback callback) {
        Log.d(TAG, "Iniciando subida de foto, uri: " + uri.toString());
        OkHttpClient client = new OkHttpClient();
        String fileName = userId + "_" + UUID.randomUUID().toString() + ".jpg";
        String endpoint = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + fileName;
        Log.d(TAG, "Endpoint de subida: " + endpoint);

        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                Log.e(TAG, "Error: No se pudo abrir el InputStream de la imagen para uri: " + uri);
                callback.onUploadComplete(null);
                return;
            }

            byte[] fileBytes = toByteArray(inputStream);
            inputStream.close();
            Log.d(TAG, "Tamaño de la imagen: " + fileBytes.length + " bytes");

            RequestBody fileBody = RequestBody.create(fileBytes, MediaType.parse("image/jpeg"));
            Request request = new Request.Builder()
                    .url(endpoint)
                    .header("apikey", SUPABASE_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_KEY)
                    .header("Content-Type", "image/jpeg")
                    .post(fileBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "Fallo al subir la imagen: " + e.getMessage(), e);
                    callback.onUploadComplete(null);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "No body";
                    Log.d(TAG, "Respuesta de Supabase - Código: " + response.code() + ", Cuerpo: " + responseBody);
                    Log.d(TAG, "Headers: " + response.headers().toString());

                    if (response.isSuccessful()) {
                        String publicUrl = SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/" + fileName;
                        Log.d(TAG, "Imagen subida con éxito: " + publicUrl);
                        callback.onUploadComplete(publicUrl);
                    } else {
                        Log.e(TAG, "Error al subir imagen - Código: " + response.code() + ", Cuerpo: " + responseBody);
                        callback.onUploadComplete(null);
                    }
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "Excepción al subir la imagen: " + e.getMessage(), e);
            callback.onUploadComplete(null);
        }
    }

    private byte[] toByteArray(InputStream inputStream) throws IOException {
        java.io.ByteArrayOutputStream byteArrayOutputStream = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private interface UploadCallback {
        void onUploadComplete(String photoUrl);
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