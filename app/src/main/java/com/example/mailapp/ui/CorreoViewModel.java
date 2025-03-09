package com.example.mailapp.ui;
import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.mailapp.database.CorreoRepository;
import com.example.mailapp.model.Correo;
import com.google.android.gms.tasks.OnSuccessListener;
import java.util.List;

public class CorreoViewModel extends ViewModel {

    private static final String TAG = "CorreoViewModel";
    private final CorreoRepository repository;
    private LiveData<List<Correo>> correosLiveData;
    private LiveData<List<Correo>> correosEnviadosLiveData;

    public CorreoViewModel() {
        repository = CorreoRepository.getInstance();
    }

    public LiveData<List<Correo>> getCorreosLiveData(String emailUsuario) {
        if (correosLiveData == null) {
            correosLiveData = repository.getCorreosRecibidos(emailUsuario);
        }
        return correosLiveData;
    }

    public LiveData<List<Correo>> getCorreosEnviadosLiveData(String userId) {
        if (correosEnviadosLiveData == null) {
            correosEnviadosLiveData = repository.getCorreosEnviados(userId);
        }
        return correosEnviadosLiveData;
    }
    public LiveData<Boolean> sendCorreo(String remitenteId, String destinatarioEmail, String asunto, String contenido) {
        return repository.sendCorreo(remitenteId, destinatarioEmail, asunto, contenido);
    }
    public void subirImagen(Uri imagenUri, String nombreArchivo, OnSuccessListener<Uri> onSuccess) {
        repository.subirImagen(imagenUri, nombreArchivo, onSuccess);
    }
}