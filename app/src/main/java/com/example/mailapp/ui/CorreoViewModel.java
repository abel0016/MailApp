package com.example.mailapp.ui;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.mailapp.models.Correo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.*;

public class CorreoViewModel extends ViewModel {

    private static final String TAG = "CorreoViewModel";
    private static final String SUPABASE_URL = "https://opvcrulzhtnetrajjctt.supabase.co/storage/v1/object";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9wdmNydWx6aHRuZXRyYWpqY3R0Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDEzMTA3OTQsImV4cCI6MjA1Njg4Njc5NH0.8B_eRnrWBf3YZP5lFtZunreQ5949SzD4NAZNNTDQhG4";
    private final MutableLiveData<List<Correo>> correosLiveData = new MutableLiveData<>();
    private final OkHttpClient client = new OkHttpClient();

    public LiveData<List<Correo>> getCorreosLiveData() {
        return correosLiveData;
    }

    public void cargarCorreos(String emailUsuario) {
        Log.d(TAG, "Cargando correos para: " + emailUsuario);

        HttpUrl url = HttpUrl.parse(SUPABASE_URL)
                .newBuilder()
                .addQueryParameter("destinatario", "eq." + emailUsuario)
                .addQueryParameter("select", "id, asunto, remitente, fecha, mensaje")
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
                correosLiveData.postValue(new ArrayList<>()); // Devolver lista vacía en caso de error
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Error en la respuesta: " + response.code() + " - " + response.message());
                    correosLiveData.postValue(new ArrayList<>());
                    return;
                }

                String jsonResponse = response.body().string();
                Log.d(TAG, "Correos recibidos: " + jsonResponse);

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
                    Log.e(TAG, "Error procesando JSON: " + e.getMessage(), e);
                }

                correosLiveData.postValue(listaCorreos);
            }
        });
    }
}
