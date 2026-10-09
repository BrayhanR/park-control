package com.example.parkcontrol;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class MyProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton buttonGetBack = findViewById(R.id.my_profile_back_button);
        Button buttonChangeProfilePic = findViewById(R.id.edit_profile_pic_button);
        TextView tvPassword = findViewById(R.id.tv_profile_password);
        ImageButton btnClosed = findViewById(R.id.btn_toggle_password_closed);
        ImageButton btnOpen = findViewById(R.id.btn_toggle_password_open);
        SwitchMaterial switchNotifications = findViewById(R.id.switch_notifications);

        // Botón regresar
        buttonGetBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainMenuActivity.class);
            startActivity(intent);
            finish();
        });

        // Botones ocultar/mostrar contraseña
        btnClosed.setOnClickListener(v -> {
            tvPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            btnClosed.setVisibility(View.GONE);
            btnOpen.setVisibility(View.VISIBLE);
        });

        btnOpen.setOnClickListener(v -> {
            tvPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            btnOpen.setVisibility(View.GONE);
            btnClosed.setVisibility(View.VISIBLE);
        });

        // Switch para recibir notificaciones
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Muestra un mensaje flotante nativo (Toast) o lanza un Intent
                Toast.makeText(this, "Notificaciones activadas", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notificaciones desactivadas", Toast.LENGTH_SHORT).show();
            }
        });

    }
}