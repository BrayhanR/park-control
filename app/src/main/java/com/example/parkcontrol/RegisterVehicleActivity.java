package com.example.parkcontrol;




import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;



public class RegisterVehicleActivity extends AppCompatActivity {

    private EditText txtPlaca;
    private EditText txtMarca;
    private EditText txtColor;

    private Spinner spinnerTipo;

    private Button btnGuardar;
    private Button btnCamara;
    private Button btnRegresar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.registervehicleactivity);

        // Referencias
        txtPlaca = findViewById(R.id.txtPlaca);
        txtMarca = findViewById(R.id.txtMarca);
        txtColor = findViewById(R.id.txtColor);

        spinnerTipo = findViewById(R.id.spinnerTipo);

        btnGuardar = findViewById(R.id.btnGuardar);
        btnCamara = findViewById(R.id.btnCamara);
        btnRegresar = findViewById(R.id.btnRegresar);

        // Tipos de vehículos
        String[] tiposVehiculo = {
                "Seleccione el tipo",
                "Automóvil",
                "Motocicleta",
                "Camioneta",
                "Campero"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                tiposVehiculo
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerTipo.setAdapter(adapter);

        // Regresar
        btnRegresar.setOnClickListener(v -> {
            finish();
        });

        // Cámara
        // Cámara simulada para Fase 2
        btnCamara.setOnClickListener(v -> {
            Toast.makeText(
                    RegisterVehicleActivity.this,
                    "Botón Agregar foto diseñado para futura implementación",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // Guardar
        btnGuardar.setOnClickListener(v -> {

            guardarVehiculo();

            Toast.makeText(RegisterVehicleActivity.this, "Vehivulo registado correctamente (Simulación)", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(RegisterVehicleActivity.this, MainMenuActivity.class);
            startActivity(intent);
            finish();

        });
    }







    private void guardarVehiculo() {

        String placa = txtPlaca.getText().toString().trim();
        String marca = txtMarca.getText().toString().trim();
        String color = txtColor.getText().toString().trim();

        String tipo = spinnerTipo.getSelectedItem().toString();

        // Validar placa
        if (placa.isEmpty()) {

            txtPlaca.setError("Ingrese la placa");
            txtPlaca.requestFocus();

            return;
        }

        // Validar tipo
        if (tipo.equals("Seleccione el tipo")) {

            Toast.makeText(
                    this,
                    "Seleccione el tipo de vehículo",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Validar marca
        if (marca.isEmpty()) {

            txtMarca.setError("Ingrese la marca");
            txtMarca.requestFocus();

            return;
        }

        // Validar color
        if (color.isEmpty()) {

            txtColor.setError("Ingrese el color");
            txtColor.requestFocus();

            return;
        }

        finish();
    }
}