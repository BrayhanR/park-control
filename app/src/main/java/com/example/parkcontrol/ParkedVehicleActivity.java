package com.example.parkcontrol;


import android.content.Intent;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ParkedVehicleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_parked_vehicle);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Boton regresar
        ImageButton parked_vehicle_out_back_btn = findViewById(R.id.parked_vehicle_back_button);
        parked_vehicle_out_back_btn.setOnClickListener(v -> {
            Intent intent = new Intent(ParkedVehicleActivity.this, MainMenuActivity.class);
            startActivity(intent);
            finish();
        });

            // Cambiar TextView por Button
            Button btnLocation = findViewById(R.id.location_button);

            btnLocation.setOnClickListener(v -> {
                Toast.makeText(this, "Ubicación en GPS (Simulación)", Toast.LENGTH_SHORT).show();


            });

        // Botón para cambiar al activity de RegisterExitActivity
        Button btnExitView = findViewById(R.id.exit_view_button);

        btnExitView.setOnClickListener(v -> {
            Intent intent = new Intent(
                    ParkedVehicleActivity.this,
                    RegisterExitActivity.class
            );

            startActivity(intent);
        });

    }
}