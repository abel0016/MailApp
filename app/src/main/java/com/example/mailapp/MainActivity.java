package com.example.mailapp;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.navigation.NavigationView;
import com.example.mailapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private DrawerLayout drawerLayout;
    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (findViewById(R.id.nav_host_fragment) == null) {
            Log.e("MainActivity", "NavHostFragment no encontrado en el layout!");
            return;
        }

        // Configurar el Toolbar como ActionBar
        setSupportActionBar(binding.toolbar);

        NavigationView navigationView = binding.navView;
        drawerLayout = binding.drawerLayout;

        binding.getRoot().post(() -> {
            try {
                navController = Navigation.findNavController(this, R.id.nav_host_fragment);
                Log.d("MainActivity", "NavController inicializado correctamente: " + navController);

                appBarConfiguration = new AppBarConfiguration.Builder(
                        R.id.recibidosFragment, R.id.enviadosFragment, R.id.borradorFragment, R.id.busquedaFragment)
                        .setOpenableLayout(drawerLayout)
                        .build();
                NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
                NavigationUI.setupWithNavController(navigationView, navController);
            } catch (IllegalStateException e) {
                Log.e("MainActivity", "Error al obtener NavController incluso después de retrasar: " + e.getMessage());
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (navController == null) {
            return super.onSupportNavigateUp();
        }
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}