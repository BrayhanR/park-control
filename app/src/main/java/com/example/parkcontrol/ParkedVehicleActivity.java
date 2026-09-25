package com.example.parkcontrol;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;

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

        // Botón para cambiar al activity de SpacesAvailabilityActivity
        TextView btnLocation = findViewById(R.id.location_button);

        btnLocation.setOnClickListener(v -> {
            Intent intent = new Intent(
                    ParkedVehicleActivity.this,
                    SpacesAvailabilityActivity.class
            );

            startActivity(intent);
        });

        // Botón para cambiar al activity de RegisterExitActivity
        TextView btnExitView = findViewById(R.id.exit_view_button);

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