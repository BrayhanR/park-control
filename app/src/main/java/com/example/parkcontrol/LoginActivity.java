package com.example.parkcontrol;

import android.content.Intent;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private TextView textRegisterLink;
    private TextView textForgotPassword;
    private CheckBox checkRememberMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar vistas
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textRegisterLink = findViewById(R.id.textRegisterLink);
        textForgotPassword = findViewById(R.id.textForgotPassword);
        checkRememberMe = findViewById(R.id.checkRememberMe);

        // Pasar la validadcion de registro
        buttonLogin.setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this, "Verificación completa", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, MainMenuActivity.class
            );

            startActivity(intent);
        });

        // Enlace a registro
        textRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterUserActivity.class);
            startActivity(intent);
        });

        // Enlace a recuperación de contraseña
        textForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

    }
}

