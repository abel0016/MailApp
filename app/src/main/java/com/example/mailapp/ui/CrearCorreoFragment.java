package com.example.mailapp.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.mailapp.databinding.FragmentCrearCorreoBinding;
import com.example.mailapp.models.Correo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import okhttp3.*;

public class CrearCorreoFragment extends Fragment {

    private static final String TAG = "CrearCorreoFragment";
    private static final String SUPABASE_URL = "https://opvcrulzhtnetrajjctt.supabase.co/rest/v1/correos";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9wdmNydWx6aHRuZXRyYWpqY3R0Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDEzMTA3OTQsImV4cCI6MjA1Njg4Njc5NH0.8B_eRnrWBf3YZP5lFtZunreQ5949SzD4NAZNNTDQhG4";
    private FragmentCrearCorreoBinding binding;
    private FirebaseAuth mAuth;
    private NavController navController;

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

        binding.btnEnviarCorreo.setOnClickListener(v -> {
            String asunto = binding.etAsunto.getText().toString().trim();
            String mensaje = binding.etMensaje.getText().toString().trim();
            String destinatarioEmail = binding.etDestinatario.getText().toString().trim();

            if (asunto.isEmpty() || mensaje.isEmpty() || destinatarioEmail.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
                return;
            }

            String remitenteId = user.getUid();
            String fechaEnvio = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String idEnviado = UUID.randomUUID().toString();
            String idRecibido = UUID.randomUUID().toString();

            // Correo para el remitente (enviado)
            Correo correoEnviado = new Correo(idEnviado, asunto, remitenteId, mensaje, fechaEnvio, destinatarioEmail, "enviado");
            // Correo para el destinatario (recibido)
            Correo correoRecibido = new Correo(idRecibido, asunto, remitenteId, mensaje, fechaEnvio, destinatarioEmail, "recibido");

            guardarCorreoEnSupabase(correoEnviado, user, () -> {
                guardarCorreoEnSupabase(correoRecibido, user, () -> {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Correo enviado", Toast.LENGTH_SHORT).show();
                        navController.popBackStack();
                    });
                });
            });
        });
    }

    private void guardarCorreoEnSupabase(Correo correo, FirebaseUser user, Runnable onSuccess) {
        OkHttpClient client = new OkHttpClient();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", correo.getId());
            jsonObject.put("asunto", correo.getAsunto());
            jsonObject.put("remitente_id", correo.getRemitenteId());
            jsonObject.put("fecha_envio", correo.getFechaEnvio());
            jsonObject.put("mensaje", correo.getMensaje());
            jsonObject.put("destinatario_email", correo.getDestinatarioEmail());
            jsonObject.put("estado", correo.getEstado());
        } catch (JSONException e) {
            Log.e(TAG, "Error creando JSON: " + e.getMessage(), e);
            return;
        }

        user.getIdToken(true).addOnSuccessListener(idToken -> {
            Log.d(TAG, "Token JWT obtenido: " + idToken.getToken().substring(0, 20) + "..."); // Depuración parcial del token
            RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.get("application/json"));
            Request request = new Request.Builder()
                    .url(SUPABASE_URL)
                    .header("apikey", SUPABASE_KEY)
                    .header("Authorization", "Bearer " + idToken.getToken())
                    .header("Content-Type", "application/json")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "Error al guardar correo: " + e.getMessage(), e);
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Error al enviar el correo", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Correo guardado con éxito: " + correo.getEstado() + " - Response: " + response.body().string());
                        if (onSuccess != null) {
                            onSuccess.run();
                        }
                    } else {
                        Log.e(TAG, "Error: " + response.code() + " - " + response.message() + " - Body: " + response.body().string());
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Error al enviar el correo: " + response.message(), Toast.LENGTH_LONG).show());
                    }
                }
            });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error al obtener el token: " + e.getMessage(), e);
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), "Error de autenticación", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}