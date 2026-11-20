package com.example.parkcontrol;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class RegisterEntryActivity extends AppCompatActivity {

    private int selectedVehicleId = -1;
    private ImageView imgVehiclePhotoSlot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_entry);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Flecha para volver a disponibilidad de espacios
        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Recibir y mostrar el espacio seleccionado
        TextView txtSelectedSpace = findViewById(R.id.txtSelectedSpace);
        String espacioSeleccionado = getIntent().getStringExtra("selected_space");

        if (espacioSeleccionado != null) {
            txtSelectedSpace.setText("Piso 1 - Espacio " + espacioSeleccionado);
        }

        SharedPreferences sesion = getSharedPreferences("ParkControlSession", MODE_PRIVATE);
        int userId = sesion.getInt("user_id", -1);

        DatabaseHelper dbHelper = new DatabaseHelper(this);

        TextView txtVehiclePlate = findViewById(R.id.txtVehiclePlate);
        TextView txtVehicleDetails = findViewById(R.id.txtVehicleDetails);
        TextView txtVehicleColor = findViewById(R.id.txtVehicleColor);
        TextView txtVehicleIcon = findViewById(R.id.txtVehicleIcon);

        // Preparamos el ImageView en el mismo espacio (55dp x 55dp) sin tocar el XML
        prepararSlotFotoVehiculo(txtVehicleIcon);

        Spinner selectorVehiculos = findViewById(R.id.spVehicleSelection);

        ArrayList<Integer> idsVehiculos = new ArrayList<>();
        ArrayList<String> placas = new ArrayList<>();
        ArrayList<String> detalles = new ArrayList<>();
        ArrayList<String> colores = new ArrayList<>();

        try (Cursor vehiculos = dbHelper.obtenerVehiculosDelUsuario(userId)) {
            while (vehiculos.moveToNext()) {
                int idVehiculo = vehiculos.getInt(vehiculos.getColumnIndexOrThrow("vehicle_id"));
                String placa = vehiculos.getString(vehiculos.getColumnIndexOrThrow("license_plate"));
                String marca = vehiculos.getString(vehiculos.getColumnIndexOrThrow("brand"));
                String tipo = vehiculos.getString(vehiculos.getColumnIndexOrThrow("vehicle_type"));
                String color = vehiculos.getString(vehiculos.getColumnIndexOrThrow("color"));

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
            mostrarFallbackEmoji(txtVehicleIcon, "🚗");

            selectorVehiculos.setEnabled(false);
            findViewById(R.id.btnRegisterEntry).setEnabled(false);
        } else {
            ArrayAdapter<String> adaptador = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    placas
            );
            adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            selectorVehiculos.setAdapter(adaptador);

            selectorVehiculos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedVehicleId = idsVehiculos.get(position);
                    txtVehiclePlate.setText(placas.get(position));
                    txtVehicleDetails.setText(detalles.get(position));
                    txtVehicleColor.setText(colores.get(position));

                    String detalleVehiculo = detalles.get(position).toLowerCase(Locale.ROOT);
                    String emojiFallback = detalleVehiculo.contains("moto") ? "🏍️" : "🚗";

                    mostrarFotoVehiculoEnSlot(dbHelper, selectedVehicleId, txtVehicleIcon, emojiFallback);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    selectedVehicleId = -1;
                    mostrarFallbackEmoji(txtVehicleIcon, "🚗");
                }
            });
        }

        TextView txtEntryDate = findViewById(R.id.txtEntryDate);
        TextView txtEntryTime = findViewById(R.id.txtEntryTime);

        Date ahora = new Date();
        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat formatoHora = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        txtEntryDate.setText(formatoFecha.format(ahora) + " 📅");
        txtEntryTime.setText(formatoHora.format(ahora) + " ◷");

        // Botón registrar y guardar ingreso
        Button registerEntry = findViewById(R.id.btnRegisterEntry);

        registerEntry.setOnClickListener(v -> {
            if (userId <= 0 || selectedVehicleId <= 0 || espacioSeleccionado == null) {
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

                Intent intent = new Intent(this, MainMenuActivity.class);
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

        // --- Validación: Mensaje para volver al menu y registrar un vehiculo ---
        Button btnGuardarFinal = findViewById(R.id.btnRegisterEntry);

        if (placas.isEmpty()) {
            btnGuardarFinal.setVisibility(View.GONE);
            txtVehicleDetails.setText("Debes registrar un vehículo. Toca aquí para volver al menú.");

            Toast.makeText(
                    this,
                    "Debes registrar un vehículo antes de registrar el ingreso",
                    Toast.LENGTH_LONG
            ).show();

            txtVehicleDetails.setOnClickListener(v -> {
                Intent intent = new Intent(RegisterEntryActivity.this, MainMenuActivity.class);
                startActivity(intent);
                finish();
            });
        } else {
            btnGuardarFinal.setVisibility(View.VISIBLE);
            btnGuardarFinal.setEnabled(true);
        }
    }

    private void prepararSlotFotoVehiculo(TextView txtVehicleIcon) {
        if (txtVehicleIcon != null && txtVehicleIcon.getParent() instanceof ViewGroup) {
            ViewGroup padre = (ViewGroup) txtVehicleIcon.getParent();
            int indice = padre.indexOfChild(txtVehicleIcon);

            imgVehiclePhotoSlot = new ImageView(this);
            int sizePx = (int) (55 * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizePx, sizePx);
            params.gravity = Gravity.CENTER_VERTICAL;
            imgVehiclePhotoSlot.setLayoutParams(params);
            imgVehiclePhotoSlot.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imgVehiclePhotoSlot.setVisibility(View.GONE);

            padre.addView(imgVehiclePhotoSlot, indice);
        }
    }

    private void mostrarFotoVehiculoEnSlot(
            DatabaseHelper dbHelper,
            int vehicleId,
            TextView txtVehicleIcon,
            String emojiFallback
    ) {
        String photoUriString = obtenerPhotoUriVehiculo(dbHelper, vehicleId);

        if (imgVehiclePhotoSlot != null && photoUriString != null && !photoUriString.isEmpty()) {
            Uri uri = Uri.parse(photoUriString);
            String ruta = uri.getPath();

            if (ruta != null) {
                File archivo = new File(ruta);
                if (archivo.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(archivo.getAbsolutePath());
                    if (bitmap != null) {
                        imgVehiclePhotoSlot.setImageBitmap(bitmap);
                        imgVehiclePhotoSlot.setVisibility(View.VISIBLE);
                        if (txtVehicleIcon != null) {
                            txtVehicleIcon.setVisibility(View.GONE);
                        }
                        return;
                    }
                }
            }

            try {
                imgVehiclePhotoSlot.setImageURI(uri);
                if (imgVehiclePhotoSlot.getDrawable() != null) {
                    imgVehiclePhotoSlot.setVisibility(View.VISIBLE);
                    if (txtVehicleIcon != null) {
                        txtVehicleIcon.setVisibility(View.GONE);
                    }
                    return;
                }
            } catch (Exception ignored) {
                // Si no se puede cargar el URI, cae al emoji
            }
        }

        mostrarFallbackEmoji(txtVehicleIcon, emojiFallback);
    }

    private void mostrarFallbackEmoji(TextView txtVehicleIcon, String emojiFallback) {
        if (imgVehiclePhotoSlot != null) {
            imgVehiclePhotoSlot.setVisibility(View.GONE);
        }
        if (txtVehicleIcon != null) {
            txtVehicleIcon.setText(emojiFallback);
            txtVehicleIcon.setVisibility(View.VISIBLE);
        }
    }

    private String obtenerPhotoUriVehiculo(DatabaseHelper dbHelper, int vehicleId) {
        if (vehicleId <= 0) {
            return null;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT photo_uri FROM Vehicle WHERE vehicle_id = ? LIMIT 1",
                new String[]{String.valueOf(vehicleId)}
        )) {
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                return cursor.getString(0);
            }
        } catch (Exception e) {
            android.util.Log.e("RegisterEntryActivity", "Error al consultar photo_uri del vehículo", e);
        }
        return null;
    }
}