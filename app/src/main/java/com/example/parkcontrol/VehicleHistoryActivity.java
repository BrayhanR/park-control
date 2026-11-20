package com.example.parkcontrol;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VehicleHistoryActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    private TextView tvHistoryDate;
    private TextView tvHistoryTicket;
    private ImageView imgVehicleHistory;
    private TextView tvParkingName;
    private TextView tvVehicleInfo;
    private TextView tvParkingPrice;
    private TextView tvTotalPaidVal;

    private Spinner spinnerHistoryRecords;
    private TextView tvEmptyHistory;

    private final List<HistorialSalida> historial = new ArrayList<>();

    static class HistorialSalida {
        int recordId;
        String slotNumber = "";
        String parkingAddress = "";
        String exitTimestampRaw = "";
        double totalFee = 0.0;
        String licensePlate = "";
        String brand = "";
        String vehicleType = "";
        String photoUri = "";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_vehicle_history);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        tvHistoryDate = findViewById(R.id.tv_history_date);
        tvHistoryTicket = findViewById(R.id.tv_history_ticket);
        imgVehicleHistory = findViewById(R.id.img_vehicle_history);
        tvParkingName = findViewById(R.id.tv_parking_name);
        tvVehicleInfo = findViewById(R.id.tv_vehicle_info);
        tvParkingPrice = findViewById(R.id.tv_parking_price);
        tvTotalPaidVal = findViewById(R.id.tv_total_paid_val);

        ImageButton buttonGetBack = findViewById(R.id.vehicle_history_back_button);
        buttonGetBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainMenuActivity.class);
            startActivity(intent);
            finish();
        });

        prepararSelectorYMensajeVacio();
        cargarHistorialSalidas();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarHistorialSalidas();
    }

    private void prepararSelectorYMensajeVacio() {
        View root = findViewById(R.id.main);
        if (!(root instanceof ConstraintLayout)) {
            return;
        }
        ConstraintLayout parent = (ConstraintLayout) root;

        int pad16 = (int) (16 * getResources().getDisplayMetrics().density);
        int pad20 = (int) (20 * getResources().getDisplayMetrics().density);
        int margin8 = (int) (8 * getResources().getDisplayMetrics().density);
        int spinnerHeight = (int) (44 * getResources().getDisplayMetrics().density);

        spinnerHistoryRecords = new Spinner(this);
        spinnerHistoryRecords.setId(View.generateViewId());
        spinnerHistoryRecords.setVisibility(View.GONE);
        spinnerHistoryRecords.setBackgroundColor(Color.parseColor("#F1F3F5"));

        ConstraintLayout.LayoutParams spParams = new ConstraintLayout.LayoutParams(
                0,
                spinnerHeight
        );
        spParams.topToBottom = R.id.vehicle_history_header;
        spParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        spParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        spParams.topMargin = pad16;
        spParams.leftMargin = pad20;
        spParams.rightMargin = pad20;
        parent.addView(spinnerHistoryRecords, spParams);

        if (tvHistoryDate != null && tvHistoryDate.getLayoutParams() instanceof ConstraintLayout.LayoutParams) {
            ConstraintLayout.LayoutParams dateParams = (ConstraintLayout.LayoutParams) tvHistoryDate.getLayoutParams();
            dateParams.topToBottom = spinnerHistoryRecords.getId();
            dateParams.topMargin = margin8;
            tvHistoryDate.setLayoutParams(dateParams);
        }

        spinnerHistoryRecords.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (position >= 0 && position < historial.size()) {
                    mostrarRegistro(historial.get(position));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        tvEmptyHistory = new TextView(this);
        tvEmptyHistory.setId(View.generateViewId());
        tvEmptyHistory.setText("Aún no tienes salidas registradas.");
        tvEmptyHistory.setTextColor(Color.parseColor("#666666"));
        tvEmptyHistory.setTextSize(16f);
        tvEmptyHistory.setGravity(Gravity.CENTER);
        tvEmptyHistory.setPadding(pad20, pad20, pad20, pad20);
        tvEmptyHistory.setVisibility(View.GONE);

        ConstraintLayout.LayoutParams emptyParams = new ConstraintLayout.LayoutParams(
                0,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        emptyParams.topToBottom = R.id.vehicle_history_header;
        emptyParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        emptyParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        emptyParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        parent.addView(tvEmptyHistory, emptyParams);
    }

    private void mostrarEstadoVacio(boolean vacio) {
        int[] idsContenido = {
                R.id.tv_history_date,
                R.id.tv_history_ticket,
                R.id.card_history_record
        };
        for (int id : idsContenido) {
            View v = findViewById(id);
            if (v != null) {
                v.setVisibility(vacio ? View.GONE : View.VISIBLE);
            }
        }
        if (spinnerHistoryRecords != null && vacio) {
            spinnerHistoryRecords.setVisibility(View.GONE);
        }
        if (tvEmptyHistory != null) {
            tvEmptyHistory.setVisibility(vacio ? View.VISIBLE : View.GONE);
        }
    }

    private void cargarHistorialSalidas() {
        SharedPreferences sesion = getSharedPreferences("ParkControlSession", MODE_PRIVATE);
        int userId = sesion.getInt("user_id", -1);

        historial.clear();
        historial.addAll(consultarSalidas(userId));

        if (historial.isEmpty()) {
            mostrarEstadoVacio(true);
            return;
        }

        mostrarEstadoVacio(false);

        if (historial.size() > 1 && spinnerHistoryRecords != null) {
            List<String> opciones = new ArrayList<>();
            for (HistorialSalida item : historial) {
                String placa = item.licensePlate.isEmpty() ? "Vehículo" : item.licensePlate;
                opciones.add("Ticket #PK-" + item.recordId + " • " + placa);
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    opciones
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerHistoryRecords.setAdapter(adapter);
            spinnerHistoryRecords.setSelection(0, false);
            spinnerHistoryRecords.setVisibility(View.VISIBLE);
        } else if (spinnerHistoryRecords != null) {
            spinnerHistoryRecords.setVisibility(View.GONE);
        }

        mostrarRegistro(historial.get(0));
    }

    private void mostrarRegistro(HistorialSalida item) {
        long exitMillis = ParkedVehicleActivity.parsearFechaMillis(item.exitTimestampRaw);
        String fechaTexto = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(new Date(exitMillis));
        tvHistoryDate.setText(fechaTexto);
        tvHistoryTicket.setText("TICKET #PK-" + item.recordId);

        String tituloEspacio;
        if (!item.parkingAddress.isEmpty()) {
            tituloEspacio = item.parkingAddress;
        } else if (!item.slotNumber.isEmpty()) {
            tituloEspacio = "Espacio #" + item.slotNumber;
        } else {
            tituloEspacio = "Parqueadero ParkControl";
        }
        tvParkingName.setText(tituloEspacio);

        StringBuilder detalleVehiculo = new StringBuilder();
        if (!item.brand.isEmpty()) {
            detalleVehiculo.append(item.brand);
        }
        if (!item.vehicleType.isEmpty()) {
            if (detalleVehiculo.length() > 0) {
                detalleVehiculo.append(" ");
            }
            detalleVehiculo.append(item.vehicleType);
        }
        if (!item.licensePlate.isEmpty()) {
            if (detalleVehiculo.length() > 0) {
                detalleVehiculo.append(" • ");
            }
            detalleVehiculo.append("Placa ").append(item.licensePlate);
        }
        tvVehicleInfo.setText(detalleVehiculo.length() > 0 ? detalleVehiculo.toString() : "Vehículo registrado");

        String precioFormateado = formatearPesos(Math.round(item.totalFee));
        tvParkingPrice.setText(precioFormateado);
        tvTotalPaidVal.setText(precioFormateado);

        cargarFotoVehiculo(item.photoUri);
    }

    private void cargarFotoVehiculo(String photoUriString) {
        if (photoUriString != null && !photoUriString.trim().isEmpty()) {
            Uri uri = Uri.parse(photoUriString);
            String path = uri.getPath();
            if (path != null) {
                File f = new File(path);
                if (f.exists()) {
                    Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
                    if (bmp != null) {
                        imgVehicleHistory.setImageBitmap(bmp);
                        return;
                    }
                }
            }
            try {
                imgVehicleHistory.setImageURI(uri);
                if (imgVehicleHistory.getDrawable() != null) {
                    return;
                }
            } catch (Exception ignored) {
            }
        }
        imgVehicleHistory.setImageResource(R.drawable.toyota_corola);
    }

    private List<HistorialSalida> consultarSalidas(int userId) {
        List<HistorialSalida> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String whereUser = userId > 0 ? " AND r.user_id = ?" : "";
        String[] args = userId > 0 ? new String[]{String.valueOf(userId)} : new String[0];

        String sql = "SELECT r.record_id, s.slot_number, r.parking_address, r.exit_timestamp, r.total_fee, " +
                "v.license_plate, v.brand, v.vehicle_type, v.photo_uri " +
                "FROM parkingRecord r " +
                "LEFT JOIN parkingSlot s ON r.slot_id = s.slot_id " +
                "LEFT JOIN Vehicle v ON r.vehicle_id = v.vehicle_id " +
                "WHERE r.exit_timestamp IS NOT NULL" + whereUser +
                " ORDER BY r.record_id DESC";

        try (Cursor c = db.rawQuery(sql, args)) {
            while (c.moveToNext()) {
                HistorialSalida item = new HistorialSalida();
                item.recordId = c.getInt(0);
                item.slotNumber = c.isNull(1) ? "" : c.getString(1).trim();
                item.parkingAddress = c.isNull(2) ? "" : c.getString(2).trim();
                item.exitTimestampRaw = c.isNull(3) ? "" : c.getString(3).trim();
                item.totalFee = c.isNull(4) ? 0.0 : c.getDouble(4);
                item.licensePlate = c.isNull(5) ? "" : c.getString(5).trim();
                item.brand = c.isNull(6) ? "" : c.getString(6).trim();
                item.vehicleType = c.isNull(7) ? "" : c.getString(7).trim();
                item.photoUri = c.isNull(8) ? "" : c.getString(8).trim();
                lista.add(item);
            }
        } catch (Exception e) {
            android.util.Log.e("VehicleHistoryActivity", "Error consultando historial de salidas", e);
        }
        return lista;
    }

    private String formatearPesos(long valor) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMANY);
        return "$ " + nf.format(Math.max(0L, valor));
    }
}