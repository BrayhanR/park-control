package com.example.parkcontrol;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
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

            // Cambiar TextView por Button
            Button btnLocation = findViewById(R.id.location_button);

            btnLocation.setOnClickListener(v -> {
                Toast.makeText(this, "Ubicación en GPS (Simulación)", Toast.LENGTH_SHORT).show();

                // Opción A: Abrir el mapa usando una ubicación o coordenadas específicas
                String geoUri = "geo:0,0?q=Parqueadero";

                Intent mapIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(geoUri));

                // Opcional: Especificar el paquete para forzar que abra en Google Maps si está disponible
                mapIntent.setPackage("com.google.android.apps.maps");

                // Verificar si hay alguna app de mapas instalada antes de lanzar el Intent
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else { // Si no tiene Google Maps instalado, se abre en cualquier app de navegación disponible
                    Intent fallbackIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(geoUri));
                    startActivity(fallbackIntent);
                }
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

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();


    }
}