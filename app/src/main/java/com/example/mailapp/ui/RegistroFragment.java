package com.example.mailapp.ui;

import static com.google.android.gms.common.util.IOUtils.toByteArray;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.example.mailapp.R;
import com.example.mailapp.databinding.FragmentRegistroBinding;
import com.example.mailapp.models.Usuario;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.*;

public class RegistroFragment extends Fragment {

    private static final String TAG = "RegistroFragment";
    private static final String SUPABASE_URL = "https://opvcrulzhtnetrajjctt.supabase.co/storage/v1/object";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9wdmNydWx6aHRuZXRyYWpqY3R0Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDEzMTA3OTQsImV4cCI6MjA1Njg4Njc5NH0.8B_eRnrWBf3YZP5lFtZunreQ5949SzD4NAZNNTDQhG4";
    private static final String BUCKET_NAME = "profile-photos";
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 100;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
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
                        .setTitle("Permiso requerido")
                        .setMessage("Necesitas conceder el permiso para seleccionar una foto.")
                        .setPositiveButton("Ir a configuración", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancelar", null)
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
                        binding.etFechaNacimiento.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    } else {
                        binding.etFechaNacimiento.setText("");
                        binding.layoutFechaNacimiento.setError("Fecha inválida (entre 12 y 100 años)");
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

    private void registrarUsuario() {
        String nombre = binding.etNombre.getText().toString().trim();
        String fechaNacimiento = binding.etFechaNacimiento.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(fechaNacimiento) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            binding.layoutEmail.setError("Completa todos los campos");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutEmail.setError("Correo inválido");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String userId = mAuth.getCurrentUser().getUid();
                if (fotoUri != null) {
                    subirFotoASupabase(fotoUri, userId, (photoUrl) -> guardarUsuarioEnSupabase(userId, nombre, fechaNacimiento, email, photoUrl));
                } else {
                    guardarUsuarioEnSupabase(userId, nombre, fechaNacimiento, email, "");
                }
            } else {
                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                    binding.layoutEmail.setError("Correo ya registrado");
                }
            }
        });
    }

    private void guardarUsuarioEnSupabase(String id, String nombre, String fechaNacimiento, String email, String photoUrl) {
        OkHttpClient client = new OkHttpClient();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
            jsonObject.put("nombre", nombre);
            jsonObject.put("fecha_nacimiento", fechaNacimiento);
            jsonObject.put("email", email);
            jsonObject.put("foto_perfil", photoUrl);
        } catch (JSONException e) {
            Log.e(TAG, "Error creando JSON", e);
            return;
        }

        RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/usuarios") // URL de la tabla usuarios
                .header("apikey", SUPABASE_KEY)
                .header("Authorization", "Bearer " + SUPABASE_KEY)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error al guardar usuario en Supabase: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                requireActivity().runOnUiThread(() -> navController.navigate(R.id.action_registroFragment_to_loginFragment));
            }
        });
    }
    private void subirFotoASupabase(Uri uri, String userId, UploadCallback callback) {
        executorService.execute(() -> {
            if (uri == null) {
                Log.e(TAG, "Error: fotoUri es nulo.");
                return;
            }

            OkHttpClient client = new OkHttpClient();

            // Generar un nombre único para la imagen
            String fileName = userId + "_" + UUID.randomUUID().toString() + ".jpg";
            String endpoint = "https://opvcrulzhtnetrajjctt.supabase.co/storage/v1/object/public/" + BUCKET_NAME + "/" + fileName;

            Log.d(TAG, "Subiendo imagen a: " + endpoint);

            try {
                InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                byte[] fileBytes = toByteArray(inputStream);

                RequestBody fileBody = RequestBody.create(fileBytes, MediaType.parse("image/jpeg"));

                Request request = new Request.Builder()
                        .url(endpoint)
                        .header("Content-Type", "image/jpeg")
                        .header("apikey", SUPABASE_KEY)
                        .header("Authorization", "Bearer " + SUPABASE_KEY)
                        .put(fileBody)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    Log.d(TAG, "Imagen subida con éxito");

                    // URL pública de la imagen
                    String publicUrl = "https://opvcrulzhtnetrajjctt.supabase.co/storage/v1/object/public/" + BUCKET_NAME + "/" + fileName;
                    Log.d(TAG, "URL pública de la imagen: " + publicUrl);

                    new Handler(Looper.getMainLooper()).post(() -> callback.onUploadComplete(publicUrl));
                } else {
                    Log.e(TAG, "Error al subir imagen: " + response.code() + " - " + response.message());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error en subirFotoASupabase: " + e.getMessage(), e);
            }
        });
    }

    private interface UploadCallback {
        void onUploadComplete(String photoUrl);
    }


}
