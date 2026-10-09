package com.example.parkcontrol;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private Button buttonRecover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        editTextEmail = findViewById(R.id.editTextEmail);
        buttonRecover = findViewById(R.id.buttonRecover);

        buttonRecover.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString();

            if (email.isEmpty()) {
                Toast.makeText(ForgotPasswordActivity.this, "Ingresa tu correo electrónico", Toast.LENGTH_SHORT).show();
                return;
            }

            // Como pasó la validación anterior, el correo contiene texto
            Toast.makeText(ForgotPasswordActivity.this, "Se enviaron instrucciones a tu correo (Simulación)", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();

        });
    }
}
