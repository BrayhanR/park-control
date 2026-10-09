package com.example.parkcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.ImageButton; // Botón regresar

public class RegisterExitActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_exit);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Boton regresar
        ImageButton parked_vehicle_out_back_btn = findViewById(R.id.parked_vehicle_out_back_button);
        parked_vehicle_out_back_btn.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterExitActivity.this, ParkedVehicleActivity.class);
            startActivity(intent);
            finish();
        });

        // Botón para confirmar salida
        Button confirmButton = findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(v -> {
            Toast.makeText(this, "Salida registrada (Simulación)", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, MainMenuActivity.class);
            startActivity(intent);
            finish();
        });

    }
}