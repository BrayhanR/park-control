package com.example.parkcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.SharedPreferences;
import android.database.Cursor;

import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.view.View;
import java.util.ArrayList;



public class RegisterEntryActivity extends AppCompatActivity {

    private int selectedVehicleId = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_entry);

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

        // Flecha para volver a disponibilidad de espacios
        TextView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        // Recibir y mostrar el espacio seleccionado
        TextView txtSelectedSpace = findViewById(R.id.txtSelectedSpace);

        String espacioSeleccionado =
                getIntent().getStringExtra("selected_space");

        if (espacioSeleccionado != null) {
            txtSelectedSpace.setText(
                    "Piso 1 - Espacio " + espacioSeleccionado
            );
        }

        SharedPreferences sesion = getSharedPreferences(
                "ParkControlSession",
                MODE_PRIVATE
        );

        int userId = sesion.getInt("user_id", -1);

        DatabaseHelper dbHelper = new DatabaseHelper(this);

        TextView txtVehiclePlate = findViewById(R.id.txtVehiclePlate);
        TextView txtVehicleDetails = findViewById(R.id.txtVehicleDetails);
        TextView txtVehicleColor = findViewById(R.id.txtVehicleColor);
        TextView txtVehicleIcon = findViewById(R.id.txtVehicleIcon);



        Spinner selectorVehiculos = findViewById(R.id.spVehicleSelection);

        ArrayList<Integer> idsVehiculos = new ArrayList<>();
        ArrayList<String> placas = new ArrayList<>();
        ArrayList<String> detalles = new ArrayList<>();
        ArrayList<String> colores = new ArrayList<>();

        try (Cursor vehiculos = dbHelper.obtenerVehiculosDelUsuario(userId)) {

            while (vehiculos.moveToNext()) {

                int idVehiculo = vehiculos.getInt(
                        vehiculos.getColumnIndexOrThrow("vehicle_id"));

                String placa = vehiculos.getString(
                        vehiculos.getColumnIndexOrThrow("license_plate"));

                String marca = vehiculos.getString(
                        vehiculos.getColumnIndexOrThrow("brand"));

                String tipo = vehiculos.getString(
                        vehiculos.getColumnIndexOrThrow("vehicle_type"));

                String color = vehiculos.getString(
                        vehiculos.getColumnIndexOrThrow("color"));

                idsVehiculos.add(idVehiculo);
                placas.add(placa);
                detalles.add((marca == null ? "" : marca + " - ") + tipo);
                colores.add(color == null ? "Sin color" : color);
            }
        }

        if (placas.isEmpty()) {

            selectedVehicleId = -1;
            txtVehiclePlate.setText("Sin vehículo");
            txtVehicleDetails.setText("No hay vehículos registrados");
            txtVehicleColor.setText("");

            selectorVehiculos.setEnabled(false);
            findViewById(R.id.btnRegisterEntry).setEnabled(false);

        } else {

            ArrayAdapter<String> adaptador = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    placas
            );

            adaptador.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item
            );

            selectorVehiculos.setAdapter(adaptador);

            selectorVehiculos.setOnItemSelectedListener(
                    new AdapterView.OnItemSelectedListener() {

                        @Override
                        public void onItemSelected(
                                AdapterView<?> parent,
                                View view,
                                int position,
                                long id) {

                            selectedVehicleId = idsVehiculos.get(position);
                            txtVehiclePlate.setText(placas.get(position));
                            txtVehicleDetails.setText(detalles.get(position));
                            txtVehicleColor.setText(colores.get(position));

                            String detalleVehiculo = detalles.get(position)
                                    .toLowerCase(java.util.Locale.ROOT);

                            if (detalleVehiculo.contains("moto")) {
                                txtVehicleIcon.setText("🏍️");
                            } else {
                                txtVehicleIcon.setText("🚗");
                            }

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            selectedVehicleId = -1;
                        }
                    }
            );
        }



        TextView txtEntryDate = findViewById(R.id.txtEntryDate);
        TextView txtEntryTime = findViewById(R.id.txtEntryTime);

        java.util.Date ahora = new java.util.Date();

        java.text.SimpleDateFormat formatoFecha =
                new java.text.SimpleDateFormat(
                        "dd/MM/yyyy", java.util.Locale.getDefault());

        java.text.SimpleDateFormat formatoHora =
                new java.text.SimpleDateFormat(
                        "hh:mm a", java.util.Locale.getDefault());

        txtEntryDate.setText(formatoFecha.format(ahora) + " 📅");
        txtEntryTime.setText(formatoHora.format(ahora) + " ◷");



        // Botón registrar y guardar ingreso
        Button registerEntry = findViewById(R.id.btnRegisterEntry);

        registerEntry.setOnClickListener(v -> {

            if (userId <= 0 || selectedVehicleId <= 0 ||
                    espacioSeleccionado == null) {

                Toast.makeText(
                        this,
                        "No se pudo identificar el usuario, vehículo o espacio",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }

            boolean registrado = dbHelper.registrarIngreso(
                    selectedVehicleId,
                    userId,
                    espacioSeleccionado
            );

            if (registrado) {

                Toast.makeText(
                        this,
                        "Ingreso registrado correctamente",
                        Toast.LENGTH_SHORT
                ).show();

                Intent intent = new Intent(
                        this,
                        MainMenuActivity.class
                );

                startActivity(intent);
                finish();

            } else {

                Toast.makeText(
                        this,
                        "No se pudo registrar el ingreso. Verifica el espacio y el vehículo.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });


    }

}