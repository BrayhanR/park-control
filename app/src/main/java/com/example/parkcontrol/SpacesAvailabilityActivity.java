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
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_spaces_availability);

        dbHelper = new DatabaseHelper(this);
        dbHelper.inicializarEspacios();

        TextView txtDisponibles = findViewById(R.id.txtDisponibles);
        TextView txtOcupados = findViewById(R.id.txtOcupados);
        TextView txtTotal = findViewById(R.id.txtTotal);

        int disponibles = dbHelper.contarEspacios("disponible");
        int ocupados = dbHelper.contarEspacios("ocupado");
        int total = dbHelper.contarEspacios(null);

        txtDisponibles.setText(String.valueOf(disponibles));
        txtOcupados.setText(String.valueOf(ocupados));
        txtTotal.setText(String.valueOf(total));



        // Flecha para volver a Menu
        TextView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

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
        configurarEspacio(R.id.slot03, "03");
        configurarEspacio(R.id.slot04, "04");
        configurarEspacio(R.id.slot05, "05");
        configurarEspacio(R.id.slot06, "06");
        configurarEspacio(R.id.slot07, "07");
        configurarEspacio(R.id.slot08, "08");
        configurarEspacio(R.id.slot09, "09");
        configurarEspacio(R.id.slot10, "10");
        configurarEspacio(R.id.slot11, "11");
        configurarEspacio(R.id.slot12, "12");
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (dbHelper == null) {
            return;
        }

        TextView disponibles = findViewById(R.id.txtDisponibles);
        TextView ocupados = findViewById(R.id.txtOcupados);
        TextView total = findViewById(R.id.txtTotal);

        disponibles.setText(String.valueOf(
                dbHelper.contarEspacios("disponible")));

        ocupados.setText(String.valueOf(
                dbHelper.contarEspacios("ocupado")));

        total.setText(String.valueOf(
                dbHelper.contarEspacios(null)));

        configurarEspacio(R.id.slot01, "01");
        configurarEspacio(R.id.slot02, "02");
        configurarEspacio(R.id.slot03, "03");
        configurarEspacio(R.id.slot04, "04");
        configurarEspacio(R.id.slot05, "05");
        configurarEspacio(R.id.slot06, "06");
        configurarEspacio(R.id.slot07, "07");
        configurarEspacio(R.id.slot08, "08");
        configurarEspacio(R.id.slot09, "09");
        configurarEspacio(R.id.slot10, "10");
        configurarEspacio(R.id.slot11, "11");
        configurarEspacio(R.id.slot12, "12");
    }

    private void configurarEspacio(int idVista, String numeroEspacio) {

        TextView espacio = findViewById(idVista);

        String estado = dbHelper.obtenerEstadoEspacio(numeroEspacio);
        boolean disponible = "disponible".equalsIgnoreCase(estado);

        if (disponible) {
            espacio.setText(numeroEspacio);
            espacio.setBackgroundResource(R.color.green_selection);
            espacio.setTextColor(getColor(R.color.green_selection_text));
        } else {
            espacio.setText(numeroEspacio + "\nOcupado");
            espacio.setBackgroundResource(R.color.grey_selection);
            espacio.setTextColor(getColor(R.color.grey_selection_text));
        }

        espacio.setOnClickListener(v -> {

            String estadoActual = dbHelper.obtenerEstadoEspacio(numeroEspacio);

            if (!"disponible".equalsIgnoreCase(estadoActual)) {
                android.widget.Toast.makeText(
                        this,
                        "Este espacio no está disponible",
                        android.widget.Toast.LENGTH_SHORT
                ).show();
                return;
            }

            Intent intent = new Intent(
                    SpacesAvailabilityActivity.this,
                    RegisterEntryActivity.class
            );

            intent.putExtra("selected_space", numeroEspacio);
            startActivity(intent);
        });
    }

}