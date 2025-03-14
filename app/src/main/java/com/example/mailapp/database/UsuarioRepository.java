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
    public Task<Usuario> getUsuarioById(String usuarioId) {
        return db.collection("usuarios")
                .document(usuarioId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            return document.toObject(Usuario.class);
                        }
                    }
                    return null;
                });
    }

    public Task<Void> guardarUsuario(String id, String nombre, String fechaNacimiento, String email, String photoUrl) {
        Usuario usuario = new Usuario(id, nombre, fechaNacimiento, email, photoUrl);
        return db.collection("usuarios")
                .document(id)
                .set(usuario);
    }
}