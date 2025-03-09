package com.example.mailapp.database;

import com.example.mailapp.model.Usuario;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UsuarioRepository {

    private static UsuarioRepository instance;
    private final FirebaseFirestore db;

    private UsuarioRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized UsuarioRepository getInstance() {
        if (instance == null) {
            instance = new UsuarioRepository();
        }
        return instance;
    }

    public Task<DocumentSnapshot> getUsuarioById(String usuarioId) {
        return db.collection("usuarios").document(usuarioId).get();
    }
}