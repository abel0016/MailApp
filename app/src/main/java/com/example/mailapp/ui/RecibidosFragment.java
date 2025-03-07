package com.example.mailapp.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mailapp.databinding.FragmentRecibidosBinding;
import com.example.mailapp.models.Correo;
import com.google.firebase.auth.FirebaseAuth;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.*;

public class RecibidosFragment extends Fragment {

    private static final String TAG = "RecibidosFragment";
    private static final String SUPABASE_URL = "https://opvcrulzhtnetrajjctt.supabase.co/storage/v1/object";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9wdmNydWx6aHRuZXRyYWpqY3R0Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDEzMTA3OTQsImV4cCI6MjA1Njg4Njc5NH0.8B_eRnrWBf3YZP5lFtZunreQ5949SzD4NAZNNTDQhG4";

    private FragmentRecibidosBinding binding;
    private CorreoAdapter adapter;
    private FirebaseAuth mAuth;
    private final OkHttpClient client = new OkHttpClient();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRecibidosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        adapter = new CorreoAdapter(new ArrayList<>(), correo -> {
            Log.d("RecibidosFragment", "Correo seleccionado: " + correo.getAsunto());
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);

        obtenerCorreosDeSupabase();
    }


    private void obtenerCorreosDeSupabase() {
        if (mAuth.getCurrentUser() == null) {
            Log.e(TAG, "Usuario no autenticado");
            return;
        }

        String emailUsuario = mAuth.getCurrentUser().getEmail();
        Log.d(TAG, "Cargando correos para: " + emailUsuario);

        HttpUrl url = HttpUrl.parse(SUPABASE_URL)
                .newBuilder()
                .addQueryParameter("destinatario", "eq." + emailUsuario)
                .addQueryParameter("select", "id, asunto, remitente, fecha, mensaje") // Incluye mensaje
                .build();

        Request request = new Request.Builder()
                .url(url)
                .header("apikey", SUPABASE_KEY)
                .header("Authorization", "Bearer " + SUPABASE_KEY)
                .header("Accept", "application/json")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error al cargar correos: " + e.getMessage(), e);
                requireActivity().runOnUiThread(() -> binding.tvMensaje.setText("Error al cargar correos"));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Error en la respuesta: " + response.code() + " - " + response.message());
                    requireActivity().runOnUiThread(() -> binding.tvMensaje.setText("No se pudieron cargar los correos"));
                    return;
                }

                String jsonResponse = response.body().string();
                Log.d(TAG, "Correos recibidos: " + jsonResponse);
                List<Correo> listaCorreos = convertirJsonACorreos(jsonResponse);

                requireActivity().runOnUiThread(() -> {
                    if (listaCorreos.isEmpty()) {
                        binding.tvMensaje.setText("No tienes correos en la bandeja de entrada");
                    } else {
                        binding.tvMensaje.setVisibility(View.GONE);
                        adapter.actualizarLista(listaCorreos);
                    }
                });
            }
        });
    }

    private List<Correo> convertirJsonACorreos(String jsonResponse) {
        List<Correo> listaCorreos = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Correo correo = new Correo(
                        jsonObject.getString("id"),
                        jsonObject.getString("asunto"),
                        jsonObject.getString("remitente"),
                        jsonObject.getString("fecha"),
                        jsonObject.getString("mensaje")
                );
                listaCorreos.add(correo);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error al convertir JSON: " + e.getMessage());
        }
        return listaCorreos;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
