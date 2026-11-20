package com.example.parkcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SpacesAvailabilityActivity extends AppCompatActivity {

    private static final String[][] SLOTS = {
            {"01"}, {"02"}, {"03"}, {"04"},
            {"05"}, {"06"}, {"07"}, {"08"},
            {"09"}, {"10"}, {"11"}, {"12"}
    };

    private static final int[] SLOT_VIEW_IDS = {
            R.id.slot01, R.id.slot02, R.id.slot03, R.id.slot04,
            R.id.slot05, R.id.slot06, R.id.slot07, R.id.slot08,
            R.id.slot09, R.id.slot10, R.id.slot11, R.id.slot12
    };

    private DatabaseHelper dbHelper;
    private TextView txtDisponibles;
    private TextView txtOcupados;
    private TextView txtTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_spaces_availability);

        dbHelper = new DatabaseHelper(this);
        dbHelper.inicializarEspacios();

        txtDisponibles = findViewById(R.id.txtDisponibles);
        txtOcupados = findViewById(R.id.txtOcupados);
        txtTotal = findViewById(R.id.txtTotal);

        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        configurarClicksEspacios();
        actualizarPantalla();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dbHelper != null) {
            actualizarPantalla();
        }
    }

    private void configurarClicksEspacios() {
        for (int i = 0; i < SLOT_VIEW_IDS.length; i++) {
            final String numeroEspacio = SLOTS[i][0];
            TextView espacio = findViewById(SLOT_VIEW_IDS[i]);

            espacio.setOnClickListener(v -> {
                String estadoActual = dbHelper.obtenerEstadoEspacio(numeroEspacio);

                if (!"disponible".equalsIgnoreCase(estadoActual)) {
                    Toast.makeText(
                            this,
                            "Este espacio no está disponible",
                            Toast.LENGTH_SHORT
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

    private void actualizarPantalla() {
        int disponibles = dbHelper.contarEspacios("disponible");
        int ocupados = dbHelper.contarEspacios("ocupado");
        int total = dbHelper.contarEspacios(null);

        txtDisponibles.setText(String.valueOf(disponibles));
        txtOcupados.setText(String.valueOf(ocupados));
        txtTotal.setText(String.valueOf(total));

        for (int i = 0; i < SLOT_VIEW_IDS.length; i++) {
            String numeroEspacio = SLOTS[i][0];
            TextView espacio = findViewById(SLOT_VIEW_IDS[i]);
            actualizarAparienciaEspacio(espacio, numeroEspacio);
        }
    }

    private void actualizarAparienciaEspacio(TextView espacio, String numeroEspacio) {
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
    }
}