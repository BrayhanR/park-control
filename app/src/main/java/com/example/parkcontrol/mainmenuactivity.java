package com.example.parkcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class mainmenuactivity extends AppCompatActivity {

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
                    mainmenuactivity.this,
                    RegistroVehiculoActivity.class
            );

            startActivity(intent);
        });

        // Botones que desarrollaremos posteriormente

        btnDisponibilidad.setOnClickListener(v -> {
            // Próximamente
        });

        btnIngreso.setOnClickListener(v -> {
            // Próximamente
        });

        btnParqueos.setOnClickListener(v -> {
            // Próximamente
        });

        btnHistorial.setOnClickListener(v -> {
            // Próximamente
        });

        btnPerfil.setOnClickListener(v -> {
            // Próximamente
        });
    }
}