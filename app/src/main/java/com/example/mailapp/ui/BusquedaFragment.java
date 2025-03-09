package com.example.mailapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.SearchView;
import com.example.mailapp.R;
import com.example.mailapp.databinding.FragmentBusquedaBinding;
import com.example.mailapp.model.Correo;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

public class BusquedaFragment extends Fragment {

    private FragmentBusquedaBinding binding;
    private CorreoAdapter adapter;
    private CorreoViewModel viewModel;
    private NavController navController;
    private List<Correo> allCorreos;
    private String userId;
    private String userEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBusquedaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        allCorreos = new ArrayList<>();

        //Inicializar Firebase Auth y obtener userId y userEmail
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            binding.tvMensaje.setVisibility(View.VISIBLE);
            binding.tvMensaje.setText(R.string.no_auth_error);
            return;
        }
        userId = auth.getCurrentUser().getUid();
        userEmail = auth.getCurrentUser().getEmail();

        //Configurar RecyclerView y Adapter
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CorreoAdapter(correo -> {
            Bundle args = new Bundle();
            args.putString("correoId", correo.getId());
            navController.navigate(R.id.action_busquedaFragment_to_detalleCorreoFragment, args);
        }, true); // Pasamos true porque queremos mostrar el remitente (como en Recibidos)
        binding.recyclerView.setAdapter(adapter);

        //Inicializar ViewModel y cargar correos
        viewModel = new ViewModelProvider(this).get(CorreoViewModel.class);
        loadAllCorreos();

        //Configurar SearchView para filtrar
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterCorreos(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterCorreos(newText);
                return true;
            }
        });
    }

    private void loadAllCorreos() {
        //Cargar correos recibidos
        viewModel.getCorreosLiveData(userEmail).observe(getViewLifecycleOwner(), recibidos -> {
            if (recibidos != null) {
                allCorreos.addAll(recibidos);
                updateList();
            }
        });

        //Cargar correos enviados
        viewModel.getCorreosEnviadosLiveData(userId).observe(getViewLifecycleOwner(), enviados -> {
            if (enviados != null) {
                allCorreos.addAll(enviados);
                updateList();
            }
        });
    }

    private void updateList() {
        if (binding == null) return;

        if (allCorreos.isEmpty()) {
            binding.tvMensaje.setVisibility(View.VISIBLE);
            binding.tvMensaje.setText(R.string.no_correos_message);
        } else {
            binding.tvMensaje.setVisibility(View.GONE);
            adapter.setCorreoList(new ArrayList<>(allCorreos));
        }
    }

    private void filterCorreos(String query) {
        List<Correo> filteredList = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(allCorreos);
        } else {
            String filterPattern = query.toLowerCase().trim();
            for (Correo correo : allCorreos) {
                if ((correo.getAsunto() != null && correo.getAsunto().toLowerCase().contains(filterPattern)) ||
                        (correo.getContenido() != null && correo.getContenido().toLowerCase().contains(filterPattern)) ||
                        (correo.getRemitenteId() != null && correo.getRemitenteId().toLowerCase().contains(filterPattern)) ||
                        (correo.getDestinatarioEmail() != null && correo.getDestinatarioEmail().toLowerCase().contains(filterPattern))) {
                    filteredList.add(correo);
                }
            }
        }

        if (filteredList.isEmpty()) {
            binding.tvMensaje.setVisibility(View.VISIBLE);
            binding.tvMensaje.setText(R.string.no_results_message);
        } else {
            binding.tvMensaje.setVisibility(View.GONE);
        }
        adapter.setCorreoList(filteredList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}