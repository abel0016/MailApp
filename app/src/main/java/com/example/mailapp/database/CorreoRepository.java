package com.example.mailapp.database;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.mailapp.model.Correo;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.ArrayList;
import java.util.List;

public class CorreoRepository {

    private static final String TAG = "CorreoRepository";
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;
    private static CorreoRepository instance;

    private CorreoRepository() {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public static synchronized CorreoRepository getInstance() {
        if (instance == null) {
            instance = new CorreoRepository();
        }
        return instance;
    }

    public LiveData<List<Correo>> getCorreosRecibidos(String userEmail) {
        MutableLiveData<List<Correo>> correosLiveData = new MutableLiveData<>();
        db.collection("correos")
                .whereEqualTo("destinatarioEmail", userEmail)
                .whereEqualTo("estado", "enviado")
                .orderBy("fechaEnvio", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Correo> correos = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Correo correo = doc.toObject(Correo.class);
                        if (correo != null) {
                            correo.setId(doc.getId());
                            correos.add(correo);
                            Log.d(TAG, "Correo recibido cargado - ID: " + correo.getId() +
                                    ", Asunto: " + correo.getAsunto() +
                                    ", Remitente: " + correo.getRemitenteId() +
                                    ", Destinatario: " + correo.getDestinatarioEmail());
                        }
                    }
                    correosLiveData.setValue(correos);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar correos recibidos", e);
                    correosLiveData.setValue(new ArrayList<>());
                });
        return correosLiveData;
    }

    public LiveData<List<Correo>> getCorreosEnviados(String userId) {
        MutableLiveData<List<Correo>> correosLiveData = new MutableLiveData<>();
        db.collection("correos")
                .whereEqualTo("remitenteId", userId)
                .whereEqualTo("estado", "enviado")
                .orderBy("fechaEnvio", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Correo> correos = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Correo correo = doc.toObject(Correo.class);
                        if (correo != null) {
                            correo.setId(doc.getId());
                            correos.add(correo);
                            Log.d(TAG, "Correo enviado cargado - ID: " + correo.getId());
                        }
                    }
                    correosLiveData.setValue(correos);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar correos enviados", e);
                    correosLiveData.setValue(new ArrayList<>());
                });
        return correosLiveData;
    }

    public LiveData<Boolean> sendCorreo(String remitenteId, String destinatarioEmail, String asunto, String contenido) {
        MutableLiveData<Boolean> resultLiveData = new MutableLiveData<>();
        Correo correo = new Correo(remitenteId, destinatarioEmail, asunto, contenido, "enviado", com.google.firebase.Timestamp.now());
        db.collection("correos")
                .add(correo)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Correo enviado con ID: " + documentReference.getId());
                    resultLiveData.setValue(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al enviar correo: " + e.getMessage(), e);
                    resultLiveData.setValue(false);
                });
        return resultLiveData;
    }

    public void getCorreoById(String correoId, OnCorreoLoadedListener listener) {
        db.collection("correos").document(correoId).get()
                .addOnCompleteListener(task -> listener.onCorreoLoaded(task));
    }

    public interface OnCorreoLoadedListener {
        void onCorreoLoaded(Task<DocumentSnapshot> task);
    }

    public void subirImagen(android.net.Uri imagenUri, String nombreArchivo, com.google.android.gms.tasks.OnSuccessListener<android.net.Uri> onSuccess) {
        StorageReference storageRef = storage.getReference().child("imagenes/" + nombreArchivo);
        UploadTask uploadTask = storageRef.putFile(imagenUri);
        uploadTask.addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(onSuccess))
                .addOnFailureListener(e -> Log.e(TAG, "Error al subir imagen: " + e.getMessage()));
    }
}