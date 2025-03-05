package com.example.mailapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.mailapp.R;
import com.example.mailapp.models.Correo;
import com.example.mailapp.databinding.FragmentRecibidosBinding;
import java.util.ArrayList;
import java.util.List;

public class RecibidosFragment extends Fragment {

    private FragmentRecibidosBinding binding;
    private CorreoAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRecibidosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Configurar RecyclerView con View Binding
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Datos temporales (hardcoded) para pruebas
        List<Correo> correos = new ArrayList<>();
        correos.add(new Correo("1", "Reunión mañana", "juan@example.com", "2025-03-05"));
        correos.add(new Correo("2", "Proyecto final", "maria@example.com", "2025-03-04"));
        correos.add(new Correo("3", "Recordatorio", "pedro@example.com", "2025-03-03"));

        adapter = new CorreoAdapter(correos);
        binding.recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}