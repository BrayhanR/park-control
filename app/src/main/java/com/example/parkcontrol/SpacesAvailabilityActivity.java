package com.example.parkcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SpacesAvailabilityActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_spaces_availability);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
            );

            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );

            return insets;
        });

        configurarEspacio(R.id.slot01, "01");
        configurarEspacio(R.id.slot02, "02");
        configurarEspacio(R.id.slot04, "04");
        configurarEspacio(R.id.slot05, "05");
        configurarEspacio(R.id.slot06, "06");
        configurarEspacio(R.id.slot07, "07");
        configurarEspacio(R.id.slot09, "09");
        configurarEspacio(R.id.slot11, "11");
        configurarEspacio(R.id.slot12, "12");
    }

    private void configurarEspacio(int idVista, String numeroEspacio) {

        View espacio = findViewById(idVista);

        espacio.setOnClickListener(v -> {

            Intent intent = new Intent(
                    SpacesAvailabilityActivity.this,
                    RegisterEntryActivity.class
            );

            intent.putExtra("selected_space", numeroEspacio);

            startActivity(intent);
        });
    }
}