package com.example.parkcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainMenuActivity extends AppCompatActivity {

    private Button btnVehiculos;
    private Button btnDisponibilidad;
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

        // Botón Disponibilidad de espacios
        btnDisponibilidad.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainMenuActivity.this,
                    SpacesAvailabilityActivity.class
            );
            startActivity(intent);
        });

        // Botón Mis parqueos
        btnParqueos.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainMenuActivity.this,
                    ParkedVehicleActivity.class
            );
            startActivity(intent);
        });

        // Botón Historial
        btnHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainMenuActivity.this,
                    VehicleHistoryActivity.class
            );
            startActivity(intent);
        });

        // Botón Mi perfil
        btnPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainMenuActivity.this,
                    MyProfileActivity.class
            );
            startActivity(intent);
        });
    }
}