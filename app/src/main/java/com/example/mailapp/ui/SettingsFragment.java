package com.example.mailapp.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.mailapp.R;
import com.example.mailapp.databinding.FragmentSettingsBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

public class SettingsFragment extends Fragment {

    private static final String TAG = "Ajustes";
    private FragmentSettingsBinding binding;
    private NavController navController;
    private SharedPreferences preferences;
    private static final String PREFS_NAME = "AppSettings";
    private static final String KEY_THEME = "theme_mode";
    private static final String KEY_LANGUAGE = "language";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        preferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        binding.tvTitle.setText(R.string.settings_title);
        binding.tvThemeLabel.setText(R.string.theme_label);
        binding.rbDayMode.setText(R.string.day_mode);
        binding.rbNightMode.setText(R.string.night_mode);
        binding.tvLanguageLabel.setText(R.string.language_label);
        binding.rbSpanish.setText(R.string.spanish);
        binding.rbEnglish.setText(R.string.english);
        binding.btnSave.setText(R.string.save);
        loadPreferences();

        binding.btnSave.setOnClickListener(v -> savePreferences());
    }

    private void loadPreferences() {
        int themeMode = preferences.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        String language = preferences.getString(KEY_LANGUAGE, "es");
        if (themeMode == AppCompatDelegate.MODE_NIGHT_NO) {
            binding.rbDayMode.setChecked(true);
        } else if (themeMode == AppCompatDelegate.MODE_NIGHT_YES) {
            binding.rbNightMode.setChecked(true);
        }
        if (language.equals("es")) {
            binding.rbSpanish.setChecked(true);
        } else if (language.equals("en")) {
            binding.rbEnglish.setChecked(true);
        }
    }

    private void savePreferences() {
        int themeMode;
        String language;

        //Obtener selección de tema
        int selectedThemeId = binding.rgTheme.getCheckedRadioButtonId();
        if (selectedThemeId == R.id.rbDayMode) {
            themeMode = AppCompatDelegate.MODE_NIGHT_NO;
        } else if (selectedThemeId == R.id.rbNightMode) {
            themeMode = AppCompatDelegate.MODE_NIGHT_YES;
        } else {
            themeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }

        //Obtener selección de idioma
        int selectedLanguageId = binding.rgLanguage.getCheckedRadioButtonId();
        if (selectedLanguageId == R.id.rbSpanish) {
            language = "es";
        } else if (selectedLanguageId == R.id.rbEnglish) {
            language = "en";
        } else {
            language = "es";
        }

        //Guardar en SharedPreferences
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_THEME, themeMode);
        editor.putString(KEY_LANGUAGE, language);
        editor.apply();

        //Mostrar AlertDialog y manejar cambios después de cerrarlo
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.settings_title)
                .setMessage(R.string.settings_saved_message)
                .setPositiveButton(R.string.ok_button, (dialog, which) -> {
                    //Aplicar tema y navegación solo después de aceptar
                    applyTheme(themeMode);
                    setLocale(language);
                    navController.navigateUp();
                })
                .setOnDismissListener(dialog -> {
                })
                .show();
    }

    private void applyTheme(int themeMode) {
        AppCompatDelegate.setDefaultNightMode(themeMode);
    }

    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        requireActivity().getResources().updateConfiguration(config, requireActivity().getResources().getDisplayMetrics());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}