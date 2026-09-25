package com.example.parkcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainMenuActivity extends AppCompatActivity {

    private Button btnVehiculos;
    private Button btnDisponibilidad;
    private Button btnIngreso;
    private Button btnParqueos;
    private Button btnHistorial;
    private Button btnPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.mainmenuactivity);

        // Referencias a los botones
        btnVehiculos = findViewById(R.id.btnVehiculos);
        btnDisponibilidad = findViewById(R.id.btnDisponibilidad);
        btnIngreso = findViewById(R.id.btnIngreso);
        btnParqueos = findViewById(R.id.btnParqueos);
        btnHistorial = findViewById(R.id.btnHistorial);
        btnPerfil = findViewById(R.id.btnPerfil);

        // Botón Mis vehículos
        btnVehiculos.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainMenuActivity.this,
                    RegisterVehicleActivity.class
            );

            startActivity(intent);
        });

        // Botones que desarrollaremos posteriormente

        btnDisponibilidad.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainMenuActivity.this,
                    SpacesAvailabilityActivity.class
            );

            startActivity(intent);
        });

        btnIngreso.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainMenuActivity.this,
                    RegisterEntryActivity.class
            );

            startActivity(intent);
        });

        btnParqueos.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainMenuActivity.this,
                    ParkedVehicleActivity.class
            );

            startActivity(intent);
        });

        btnHistorial.setOnClickListener(v -> {
            Toast.makeText(this, "Proximamente se registrará esta Activity", Toast.LENGTH_SHORT).show();
        });

        btnPerfil.setOnClickListener(v -> {
            Toast.makeText(this, "Proximamente se registrará esta Activity", Toast.LENGTH_SHORT).show();
        });
    }
}