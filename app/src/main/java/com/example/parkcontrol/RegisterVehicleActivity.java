package com.example.parkcontrol;



import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AppCompatActivity;



public class RegisterVehicleActivity extends AppCompatActivity {

    private EditText txtPlaca;
    private EditText txtMarca;
    private EditText txtColor;

    private Spinner spinnerTipo;

    private Button btnGuardar;
    private Button btnCamara;
    private Button btnRegresar;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permisoCamaraLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                permitido -> {
                    if (permitido) {
                        abrirCamara();
                    } else {
                        Toast.makeText(
                                this,
                                "Se necesita permiso para utilizar la cámara",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
        camaraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                imagen -> {
                    if (imagen != null) {

                        Toast.makeText(
                                this,
                                "Fotografía capturada correctamente",
                                Toast.LENGTH_SHORT
                        ).show();

                        // Posteriormente utilizaremos esta imagen
                        // para reconocer la placa.
                    }
                }
        );

        setContentView(R.layout.registervehicleactivity);
        databaseHelper = new DatabaseHelper(this);

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
        btnCamara.setOnClickListener(v -> {
            abrirCamara();
        });

        // Guardar
        btnGuardar.setOnClickListener(v -> {

            guardarVehiculo();

        });
    }

    private void abrirCamara() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED) {

            camaraLauncher.launch(null);

        } else {

            permisoCamaraLauncher.launch(
                    Manifest.permission.CAMERA
            );
        }
    }

    private ActivityResultLauncher<String> permisoCamaraLauncher;

    private ActivityResultLauncher<Void> camaraLauncher;

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

        // Usuario de prueba
        int userId = 1;

        // Insertar vehículo en SQLite
//        long resultado = databaseHelper.insertarVehiculo(
//                userId,
//                placa,
//                tipo,
//                marca,
//                color
//        );

//        if (resultado != -1) {
//
//            Toast.makeText(
//                    this,
//                    "Vehículo registrado correctamente",
//                    Toast.LENGTH_LONG
//            ).show();
//
//            finish();
//
//        } else {
//
//            Toast.makeText(
//                    this,
//                    "No fue posible registrar el vehículo",
//                    Toast.LENGTH_LONG
//            ).show();
//        }


        finish();
    }
}