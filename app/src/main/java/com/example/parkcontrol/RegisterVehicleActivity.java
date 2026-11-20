package com.example.parkcontrol;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RegisterVehicleActivity extends AppCompatActivity {

    private EditText txtPlaca;
    private EditText txtMarca;
    private EditText txtColor;

    private ImageView imgPlaca;
    private Spinner spinnerTipo;

    private Button btnGuardar;
    private Button btnEditarVehiculo;
    private Button btnCamara;
    private ImageButton btnRegresar;

    private DatabaseHelper databaseHelper;
    private RecyclerView rvVehiculos;
    private TextView tvSinVehiculos;

    private Bitmap ultimaFotoCapturada = null;
    private boolean modoEdicion = false;
    private boolean gestosConfigurados = false;

    private final List<Integer> idsVehiculos = new ArrayList<>();
    private final List<String[]> filasVehiculos = new ArrayList<>();

    private final String[] tiposVehiculo = {
            "Seleccione el tipo",
            "Automóvil",
            "Motocicleta",
            "Camioneta",
            "Campero"
    };

    private ActivityResultLauncher<String> permisoCamaraLauncher;
    private ActivityResultLauncher<Void> camaraLauncher;

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
                                "Se necesita permiso para utilizar la cámara (Simulación)",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );

        camaraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                imagen -> {
                    if (imagen != null) {
                        ultimaFotoCapturada = imagen;
                        imgPlaca.setImageBitmap(imagen);
                        imgPlaca.setVisibility(View.VISIBLE);
                        imgPlaca.setScaleType(ImageView.ScaleType.FIT_CENTER);

                        Toast.makeText(
                                this,
                                "Fotografía capturada correctamente",
                                Toast.LENGTH_SHORT
                        ).show();
                    } else {
                        Toast.makeText(
                                this,
                                "No se recibió ninguna fotografía",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

        setContentView(R.layout.registervehicleactivity);

        imgPlaca = findViewById(R.id.imgPlaca);
        rvVehiculos = findViewById(R.id.rvVehiculos);
        rvVehiculos.setLayoutManager(new LinearLayoutManager(this));

        databaseHelper = new DatabaseHelper(this);

        // Referencias
        txtPlaca = findViewById(R.id.txtPlaca);
        txtMarca = findViewById(R.id.txtMarca);
        txtColor = findViewById(R.id.txtColor);
        spinnerTipo = findViewById(R.id.spinnerTipo);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnEditarVehiculo = findViewById(R.id.btnEditarVehiculo);
        btnCamara = findViewById(R.id.btnCamara);
        btnRegresar = findViewById(R.id.btnRegresar);

        configurarTablaSinIdYMensajeVacio();
        configurarGestosLista();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                tiposVehiculo
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapter);

        cargarVehiculos();

        // Regresar
        btnRegresar.setOnClickListener(v -> finish());

        // Cámara
        btnCamara.setOnClickListener(v -> {
            Toast.makeText(this, "Abriendo cámara...", Toast.LENGTH_SHORT).show();
            abrirCamara();
        });

        // Guardar (Create)
        btnGuardar.setOnClickListener(v -> guardarVehiculo());

        // Alternar modo edición / eliminación (Update / Delete)
        btnEditarVehiculo.setOnClickListener(v -> {
            if (idsVehiculos.isEmpty()) {
                Toast.makeText(this, "No hay vehículos registrados para editar o eliminar", Toast.LENGTH_SHORT).show();
                return;
            }
            modoEdicion = !modoEdicion;
            if (modoEdicion) {
                btnEditarVehiculo.setText("SALIR DE MODO EDICIÓN");
                btnEditarVehiculo.setBackgroundColor(Color.parseColor("#C62828"));
                rvVehiculos.setBackgroundColor(Color.parseColor("#E3F2FD"));
                Toast.makeText(this, "Toca un vehículo para editar/eliminar o mantén presionado para eliminar", Toast.LENGTH_LONG).show();
            } else {
                salirDeModoEdicion();
            }
        });
    }

    private void salirDeModoEdicion() {
        modoEdicion = false;
        btnEditarVehiculo.setText("EDITAR / ELIMINAR VEHÍCULO");
        btnEditarVehiculo.setBackgroundColor(Color.parseColor("#37474F"));
        rvVehiculos.setBackgroundColor(Color.WHITE);
    }

    private void configurarTablaSinIdYMensajeVacio() {
        if (rvVehiculos.getParent() instanceof ViewGroup) {
            ViewGroup contenedorTabla = (ViewGroup) rvVehiculos.getParent();

            if (contenedorTabla.getChildCount() > 0 && contenedorTabla.getChildAt(0) instanceof LinearLayout) {
                LinearLayout encabezado = (LinearLayout) contenedorTabla.getChildAt(0);
                if (encabezado.getChildCount() >= 5) {
                    encabezado.getChildAt(0).setVisibility(View.GONE);
                    float[] pesos = {1.3f, 1.3f, 1.3f, 1.1f};
                    for (int i = 1; i <= 4; i++) {
                        View col = encabezado.getChildAt(i);
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) col.getLayoutParams();
                        params.weight = pesos[i - 1];
                        col.setLayoutParams(params);
                    }
                }
            }

            if (tvSinVehiculos == null) {
                tvSinVehiculos = new TextView(this);
                tvSinVehiculos.setText("No hay vehículos registrados. Registra un vehículo.");
                tvSinVehiculos.setTextColor(Color.parseColor("#555555"));
                tvSinVehiculos.setTextSize(14f);
                tvSinVehiculos.setGravity(Gravity.CENTER);
                tvSinVehiculos.setPadding(16, 32, 16, 32);
                tvSinVehiculos.setBackgroundColor(Color.WHITE);
                tvSinVehiculos.setVisibility(View.GONE);

                int indexRecycler = contenedorTabla.indexOfChild(rvVehiculos);
                contenedorTabla.addView(tvSinVehiculos, indexRecycler);
            }
        }
    }

    private void abrirCamara() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED) {
            camaraLauncher.launch(null);
        } else {
            permisoCamaraLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private File guardarFotoVehiculoEnCarpetaInterna(Bitmap bitmap, String placa) {
        if (bitmap == null) {
            return null;
        }

        File carpetaVehiculos = new File(getFilesDir(), "vehicle_images");
        if (!carpetaVehiculos.exists() && !carpetaVehiculos.mkdirs()) {
            return null;
        }

        String nombreLimpio = placa.replaceAll("[^a-zA-Z0-9_-]", "_");
        File archivoDestino = new File(carpetaVehiculos, "vehicle_" + nombreLimpio + ".jpg");

        try (OutputStream out = new FileOutputStream(archivoDestino)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            return archivoDestino;
        } catch (Exception e) {
            android.util.Log.e("RegisterVehicleActivity", "Error al guardar foto del vehículo", e);
            return null;
        }
    }

    private void guardarVehiculo() {
        String placa = txtPlaca.getText().toString().trim().toUpperCase(Locale.ROOT);
        String marca = txtMarca.getText().toString().trim();
        String color = txtColor.getText().toString().trim();
        String tipo = spinnerTipo.getSelectedItem().toString();

        if (placa.isEmpty()) {
            txtPlaca.setError("Ingrese la placa");
            txtPlaca.requestFocus();
            return;
        }

        if (tipo.equals("Seleccione el tipo")) {
            Toast.makeText(this, "Seleccione el tipo de vehículo", Toast.LENGTH_SHORT).show();
            return;
        }

        if (marca.isEmpty()) {
            txtMarca.setError("Ingrese la marca");
            txtMarca.requestFocus();
            return;
        }

        if (color.isEmpty()) {
            txtColor.setError("Ingrese el color");
            txtColor.requestFocus();
            return;
        }

        int userId = getSharedPreferences("ParkControlSession", MODE_PRIVATE).getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Debes iniciar sesión para registrar un vehículo", Toast.LENGTH_LONG).show();
            return;
        }

        String photoUriString = null;
        if (ultimaFotoCapturada != null) {
            File archivoFoto = guardarFotoVehiculoEnCarpetaInterna(ultimaFotoCapturada, placa);
            if (archivoFoto != null) {
                photoUriString = Uri.fromFile(archivoFoto).toString();
            }
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("license_plate", placa);
        values.put("vehicle_type", tipo);
        values.put("brand", marca);
        values.put("color", color);
        if (photoUriString != null) {
            values.put("photo_uri", photoUriString);
        }

        long resultado = db.insert("Vehicle", null, values);

        if (resultado != -1) {
            Toast.makeText(this, "Vehículo registrado correctamente", Toast.LENGTH_LONG).show();
            txtPlaca.setText("");
            txtMarca.setText("");
            txtColor.setText("");
            spinnerTipo.setSelection(0);
            ultimaFotoCapturada = null;
            imgPlaca.setVisibility(View.GONE);
            cargarVehiculos();
        } else {
            Toast.makeText(
                    this,
                    "No fue posible registrar el vehículo (verifica que la placa no esté repetida)",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void cargarVehiculos() {
        int userId = getSharedPreferences("ParkControlSession", MODE_PRIVATE).getInt("user_id", -1);
        idsVehiculos.clear();
        filasVehiculos.clear();

        if (userId != -1) {
            Cursor cursor = databaseHelper.getReadableDatabase().rawQuery(
                    "SELECT vehicle_id, license_plate, vehicle_type, brand, color " +
                            "FROM Vehicle WHERE user_id = ? ORDER BY vehicle_id DESC",
                    new String[]{String.valueOf(userId)}
            );

            try {
                while (cursor.moveToNext()) {
                    idsVehiculos.add(cursor.getInt(0));
                    filasVehiculos.add(new String[]{
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.isNull(3) ? "" : cursor.getString(3),
                            cursor.isNull(4) ? "" : cursor.getString(4)
                    });
                }
            } finally {
                cursor.close();
            }
        }

        rvVehiculos.setAdapter(new VehiculosAdapter(filasVehiculos));

        if (filasVehiculos.isEmpty()) {
            rvVehiculos.setVisibility(View.GONE);
            if (tvSinVehiculos != null) {
                tvSinVehiculos.setVisibility(View.VISIBLE);
            }
        } else {
            rvVehiculos.setVisibility(View.VISIBLE);
            if (tvSinVehiculos != null) {
                tvSinVehiculos.setVisibility(View.GONE);
            }
        }
    }

    private void configurarGestosLista() {
        if (gestosConfigurados) {
            return;
        }
        gestosConfigurados = true;

        GestureDetector detector = new GestureDetector(
                this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        View child = rvVehiculos.findChildViewUnder(e.getX(), e.getY());
                        if (child != null && modoEdicion) {
                            int pos = rvVehiculos.getChildAdapterPosition(child);
                            if (pos != RecyclerView.NO_POSITION && pos < idsVehiculos.size()) {
                                mostrarDialogoEditarVehiculo(pos);
                                return true;
                            }
                        }
                        return false;
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {
                        View child = rvVehiculos.findChildViewUnder(e.getX(), e.getY());
                        if (child != null) {
                            int pos = rvVehiculos.getChildAdapterPosition(child);
                            if (pos != RecyclerView.NO_POSITION && pos < idsVehiculos.size()) {
                                confirmarEliminarVehiculo(pos, null);
                            }
                        }
                    }
                }
        );

        rvVehiculos.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                detector.onTouchEvent(e);
                return false;
            }
        });
    }

    private void mostrarDialogoEditarVehiculo(int position) {
        if (position < 0 || position >= idsVehiculos.size()) {
            return;
        }

        int vehicleId = idsVehiculos.get(position);
        String[] datosActuales = filasVehiculos.get(position);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);

        final EditText inputPlaca = new EditText(this);
        inputPlaca.setHint("Placa");
        inputPlaca.setText(datosActuales[0]);
        layout.addView(inputPlaca);

        final Spinner spinnerDialogTipo = new Spinner(this);
        ArrayAdapter<String> adapterDialog = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                tiposVehiculo
        );
        adapterDialog.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDialogTipo.setAdapter(adapterDialog);
        for (int i = 0; i < tiposVehiculo.length; i++) {
            if (tiposVehiculo[i].equalsIgnoreCase(datosActuales[1])) {
                spinnerDialogTipo.setSelection(i);
                break;
            }
        }
        layout.addView(spinnerDialogTipo);

        final EditText inputMarca = new EditText(this);
        inputMarca.setHint("Marca");
        inputMarca.setText(datosActuales[2]);
        layout.addView(inputMarca);

        final EditText inputColor = new EditText(this);
        inputColor.setHint("Color");
        inputColor.setText(datosActuales[3]);
        layout.addView(inputColor);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar vehículo");
        builder.setView(layout);

        builder.setPositiveButton("Guardar", null);
        builder.setNeutralButton("Eliminar", null);
        builder.setNegativeButton("Cancelar", null);

        AlertDialog editDialog = builder.create();
        editDialog.show();

        editDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> editDialog.dismiss());

        editDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v ->
                confirmarEliminarVehiculo(position, editDialog)
        );

        editDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String nuevaPlaca = inputPlaca.getText().toString().trim().toUpperCase(Locale.ROOT);
            String nuevoTipo = spinnerDialogTipo.getSelectedItem().toString();
            String nuevaMarca = inputMarca.getText().toString().trim();
            String nuevoColor = inputColor.getText().toString().trim();

            if (nuevaPlaca.isEmpty() || nuevoTipo.equals("Seleccione el tipo") || nuevaMarca.isEmpty() || nuevoColor.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos válidos", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                SQLiteDatabase db = databaseHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("license_plate", nuevaPlaca);
                values.put("vehicle_type", nuevoTipo);
                values.put("brand", nuevaMarca);
                values.put("color", nuevoColor);

                int filas = db.update(
                        "Vehicle",
                        values,
                        "vehicle_id = ?",
                        new String[]{String.valueOf(vehicleId)}
                );

                if (filas > 0) {
                    editDialog.dismiss();
                    salirDeModoEdicion();
                    cargarVehiculos();
                    Toast.makeText(this, "Vehículo actualizado", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "No se pudo actualizar el vehículo", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error al actualizar (revisa que la placa no esté repetida)", Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean tieneParqueoActivo(SQLiteDatabase db, int vehicleId) {
        try (Cursor cursor = db.rawQuery(
                "SELECT record_id FROM parkingRecord WHERE vehicle_id = ? AND exit_timestamp IS NULL LIMIT 1",
                new String[]{String.valueOf(vehicleId)}
        )) {
            return cursor.moveToFirst();
        }
    }

    private void confirmarEliminarVehiculo(int position, AlertDialog editDialogPadre) {
        if (position < 0 || position >= idsVehiculos.size()) {
            return;
        }

        int vehicleId = idsVehiculos.get(position);
        String placa = filasVehiculos.get(position)[0];

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Eliminar vehículo");
        builder.setMessage("¿Deseas eliminar el vehículo con placa " + placa + "?");
        builder.setPositiveButton("Eliminar", null);
        builder.setNegativeButton("Cancelar", null);

        AlertDialog deleteDialog = builder.create();
        deleteDialog.show();

        deleteDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> deleteDialog.dismiss());

        deleteDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            if (tieneParqueoActivo(db, vehicleId)) {
                deleteDialog.dismiss();
                Toast.makeText(
                        this,
                        "El vehículo tiene un parqueo activo; registra primero su salida para eliminarlo",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }

            db.beginTransaction();
            try {
                db.delete(
                        "parkingRecord",
                        "vehicle_id = ?",
                        new String[]{String.valueOf(vehicleId)}
                );

                int eliminadas = db.delete(
                        "Vehicle",
                        "vehicle_id = ?",
                        new String[]{String.valueOf(vehicleId)}
                );

                if (eliminadas > 0) {
                    db.setTransactionSuccessful();
                    deleteDialog.dismiss();
                    if (editDialogPadre != null && editDialogPadre.isShowing()) {
                        editDialogPadre.dismiss();
                    }
                    salirDeModoEdicion();
                    cargarVehiculos();
                    Toast.makeText(this, "Vehículo eliminado", Toast.LENGTH_SHORT).show();
                } else {
                    deleteDialog.dismiss();
                    Toast.makeText(this, "No se pudo eliminar el vehículo", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                android.util.Log.e("RegisterVehicleActivity", "Error al eliminar vehículo", e);
                deleteDialog.dismiss();
                Toast.makeText(this, "No se pudo eliminar el vehículo", Toast.LENGTH_LONG).show();
            } finally {
                db.endTransaction();
            }
        });
    }
}