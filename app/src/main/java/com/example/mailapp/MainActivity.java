package com.example.mailapp;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.mailapp.database.UsuarioRepository;
import com.example.mailapp.databinding.ActivityMainBinding;
import com.example.mailapp.databinding.NavHeaderBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private NavController navController;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseAuth.AuthStateListener authStateListener;
    private NavHeaderBinding headerBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applySavedLocale();
        applySavedTheme();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
            getSupportActionBar().setTitle(R.string.app_name);
        }

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        } else {
            Log.e(TAG, "NavHostFragment no encontrado!");
            return;
        }

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.recibidosFragment, R.id.enviadosFragment, R.id.settingsFragment
        )
                .setOpenableLayout(binding.drawerLayout)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navigationView, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.recibidosFragment ||
                    destination.getId() == R.id.enviadosFragment ||
                    destination.getId() == R.id.settingsFragment) {
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
                Log.d(TAG, "Destino con Drawer habilitado: " + destination.getLabel());
            } else {
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
                Log.d(TAG, "Destino sin Drawer: " + destination.getLabel());
            }
        });

        binding.navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Log.d(TAG, "Opción seleccionada: " + itemId);
            if (itemId == R.id.nav_recibidos) {
                navController.navigate(R.id.recibidosFragment);
            } else if (itemId == R.id.nav_enviados) {
                navController.navigate(R.id.enviadosFragment);
            } else if (itemId == R.id.nav_busqueda) {
                navController.navigate(R.id.busquedaFragment);
            } else if (itemId == R.id.nav_configuracion) {
                navController.navigate(R.id.settingsFragment);
            } else if (itemId == R.id.nav_logout) {
                Log.d(TAG, "Cerrando sesión...");
                mAuth.signOut();
                navController.navigate(R.id.action_global_loginFragment);
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Inicializar el headerBinding una sola vez
        View headerView = binding.navigationView.getHeaderView(0);
        if (headerView == null) {
            Log.e(TAG, "HeaderView no encontrado! Asegúrate de que el layout activity_main.xml incluye un NavigationView con un header.");
            return;
        }
        try {
            headerBinding = NavHeaderBinding.bind(headerView);
        } catch (Exception e) {
            Log.e(TAG, "Error al bindear NavHeaderBinding: " + e.getMessage());
            return;
        }

        setupAuthStateListener();
    }

    private void setupAuthStateListener() {
        authStateListener = firebaseAuth -> {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            Log.d(TAG, "Estado de autenticación cambiado. Usuario: " +
                    (currentUser != null ? currentUser.getUid() + ", Email: " + currentUser.getEmail() : "null"));
            updateNavigationHeader(currentUser);
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(authStateListener);
    }

    private void updateNavigationHeader(FirebaseUser currentUser) {
        if (headerBinding == null) {
            Log.e(TAG, "headerBinding no inicializado!");
            return;
        }

        if (currentUser == null) {
            Log.w(TAG, "No hay usuario autenticado. Verifica el flujo de autenticación.");
            headerBinding.navHeaderEmail.setText("Usuario");
            headerBinding.navHeaderImage.setImageResource(R.drawable.ic_profile_placeholder);
            return;
        }

        Log.d(TAG, "Usuario autenticado - UID: " + currentUser.getUid() + ", Email: " + currentUser.getEmail());
        String email = currentUser.getEmail();
        if (email != null && !email.isEmpty()) {
            headerBinding.navHeaderEmail.setText(email);
            Log.d(TAG, "Email de FirebaseAuth asignado al header: " + email);
        } else {
            headerBinding.navHeaderEmail.setText("Usuario");
            Log.w(TAG, "El email de FirebaseAuth es null o vacío.");
        }

        // Usar UsuarioRepository para obtener los datos
        UsuarioRepository.getInstance().getUsuarioById(currentUser.getUid())
                .addOnSuccessListener(usuario -> {
                    if (usuario != null) {
                        // No usamos exists() ni toObject() aquí porque usuario ya es un objeto de tipo Usuario
                        Log.d(TAG, "Datos desde Firestore - Email: " + usuario.getEmail() + ", Foto URL: " + usuario.getFotoUrl());
                        if (usuario.getEmail() != null && !usuario.getEmail().isEmpty()) {
                            headerBinding.navHeaderEmail.setText(usuario.getEmail());
                            Log.d(TAG, "Email de Firestore asignado al header: " + usuario.getEmail());
                        }

                        String photoUrl = usuario.getFotoUrl();
                        if (photoUrl != null && !photoUrl.isEmpty()) {
                            Log.d(TAG, "Intentando cargar imagen desde URL: " + photoUrl);
                            new DownloadImageTask(headerBinding).execute(photoUrl);
                        } else {
                            Log.w(TAG, "Foto URL es null o vacía en Firestore.");
                            headerBinding.navHeaderImage.setImageResource(R.drawable.ic_profile_placeholder);
                        }
                    } else {
                        Log.w(TAG, "Usuario no encontrado en Firestore para UID: " + currentUser.getUid());
                        headerBinding.navHeaderImage.setImageResource(R.drawable.ic_profile_placeholder);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar datos de Firestore: " + e.getMessage());
                    headerBinding.navHeaderImage.setImageResource(R.drawable.ic_profile_placeholder);
                });
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private final NavHeaderBinding headerBinding;
        private boolean activityDestroyed = false;

        public DownloadImageTask(NavHeaderBinding headerBinding) {
            this.headerBinding = headerBinding;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (isFinishing() || isDestroyed()) {
                activityDestroyed = true;
                cancel(true);
            }
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String imageUrl = urls[0];
            Log.d(TAG, "Descargando imagen desde URL: " + imageUrl); // Depuración
            Bitmap bitmap = null;
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Código de respuesta HTTP: " + responseCode); // Depuración
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream input = connection.getInputStream();
                    bitmap = BitmapFactory.decodeStream(input);
                    input.close();
                } else {
                    Log.w(TAG, "Respuesta no exitosa al descargar imagen: " + responseCode);
                }
                connection.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error al descargar imagen: " + e.getMessage(), e);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (activityDestroyed) {
                Log.w(TAG, "Actividad destruida, no se puede establecer la imagen.");
                return;
            }
            if (result != null) {
                Bitmap circularBitmap = getCircularBitmap(result);
                headerBinding.navHeaderImage.setImageBitmap(circularBitmap);
                Log.d(TAG, "Imagen cargada exitosamente en el DrawerMenu");
            } else {
                Log.w(TAG, "No se pudo descargar la imagen, usando placeholder.");
                headerBinding.navHeaderImage.setImageResource(R.drawable.ic_profile_placeholder);
            }
        }

        private Bitmap getCircularBitmap(Bitmap bitmap) {
            int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
            Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            android.graphics.Canvas canvas = new android.graphics.Canvas(output);
            android.graphics.Paint paint = new android.graphics.Paint();
            android.graphics.Rect rect = new android.graphics.Rect(0, 0, size, size);
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(android.graphics.Color.WHITE);
            float radius = size / 2f;
            canvas.drawCircle(radius, radius, radius, paint);
            paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);
            return output;
        }
    }

    // Método público para actualizar el Drawer
    public void actualizarDrawer() {
        // Añadir un pequeño retraso para permitir que Firestore sincronice
        new android.os.Handler().postDelayed(() -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            updateNavigationHeader(currentUser);
        }, 2000); // Retraso de 2 segundo
    }

    private void applySavedTheme() {
        SharedPreferences preferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        int themeMode = preferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(themeMode);
    }

    private void applySavedLocale() {
        SharedPreferences preferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        String language = preferences.getString("language", "es");
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.d(TAG, "Navegación hacia arriba invocada");
        int currentDestinationId = navController.getCurrentDestination() != null ?
                navController.getCurrentDestination().getId() : -1;
        if (currentDestinationId == R.id.recibidosFragment ||
                currentDestinationId == R.id.enviadosFragment ||
                currentDestinationId == R.id.settingsFragment) {
            Log.d(TAG, "Abriendo el Drawer desde destino: " + currentDestinationId);
            binding.drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return NavigationUI.navigateUp(navController, binding.drawerLayout) || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}