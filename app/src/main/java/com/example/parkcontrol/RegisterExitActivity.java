package com.example.parkcontrol;

import android.content.ContentValues;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Locale;

public class RegisterExitActivity extends AppCompatActivity {

    private static final long TARIFA_POR_MINUTO = 100L;

    private DatabaseHelper dbHelper;
    private TextView txtEmptyState;

    private TextView plateNumberTv;
    private TextView vehicleTv;
    private TextView colorTv;
    private ImageView vehicleImageIv;
    private TextView valEntryTimeTv;
    private TextView valExitTimeTv;
    private TextView valTotalTimeTv;
    private TextView valPaymentTv;
    private Button confirmButton;

    private ParqueoSalidaDatos parqueoActual;

    static class ParqueoSalidaDatos {
        int recordId = -1;
        int slotId = -1;
        String slotNumber = "";
        String placa = "";
        String vehiculo = "";
        String color = "";
        String photoUri = "";
        String entryTimestampRaw = "";
        String horaIngresoTexto = "";
        long entryMillis = 0L;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_exit);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        plateNumberTv = findViewById(R.id.plate_number);
        vehicleTv = findViewById(R.id.vehicle);
        colorTv = findViewById(R.id.color);
        vehicleImageIv = findViewById(R.id.vehicle_image);
        valEntryTimeTv = findViewById(R.id.val_entry_time);
        valExitTimeTv = findViewById(R.id.val_exit_time);
        valTotalTimeTv = findViewById(R.id.val_total_time);
        valPaymentTv = findViewById(R.id.val_payment);
        confirmButton = findViewById(R.id.confirm_button);

        prepararVistaSinParqueo();

        ImageButton parked_vehicle_out_back_btn = findViewById(R.id.parked_vehicle_out_back_button);
        parked_vehicle_out_back_btn.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterExitActivity.this, ParkedVehicleActivity.class);
            startActivity(intent);
            finish();
        });

        cargarResumenSalida();

        confirmButton.setOnClickListener(v -> {
            if (parqueoActual == null || parqueoActual.recordId <= 0) {
                Toast.makeText(this, "No hay un parqueo activo para registrar salida", Toast.LENGTH_SHORT).show();
                return;
            }

            long ahoraMillis = System.currentTimeMillis();
            long elapsedMillis = Math.max(0L, ahoraMillis - parqueoActual.entryMillis);
            long minutos = Math.max(1L, (elapsedMillis + 59_999L) / 60_000L);
            long totalPagar = minutos * TARIFA_POR_MINUTO;

            String exitTimestampSql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(new Date(ahoraMillis));

            boolean guardado = confirmarSalidaEnBd(parqueoActual, exitTimestampSql, totalPagar);
            if (!guardado) {
                Toast.makeText(this, "No se pudo registrar la salida en la base de datos", Toast.LENGTH_LONG).show();
                return;
            }

            Toast.makeText(
                    this,
                    "Salida registrada (" + formatearPesos(totalPagar) + ")",
                    Toast.LENGTH_SHORT
            ).show();

            Intent intent = new Intent(this, MainMenuActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void prepararVistaSinParqueo() {
        View root = findViewById(R.id.main);
        if (!(root instanceof ConstraintLayout)) {
            return;
        }
        ConstraintLayout parent = (ConstraintLayout) root;

        LinearLayout boxVacio = new LinearLayout(this);
        boxVacio.setId(View.generateViewId());
        boxVacio.setOrientation(LinearLayout.VERTICAL);
        boxVacio.setGravity(Gravity.CENTER);
        int pad = (int) (24 * getResources().getDisplayMetrics().density);
        boxVacio.setPadding(pad, pad, pad, pad);
        boxVacio.setVisibility(View.GONE);

        txtEmptyState = new TextView(this);
        txtEmptyState.setText("Aún no has registrado un parqueo.\nVe a disponibilidad de espacios para registrar un ingreso.");
        txtEmptyState.setTextColor(Color.parseColor("#37474F"));
        txtEmptyState.setTextSize(16f);
        txtEmptyState.setGravity(Gravity.CENTER);
        boxVacio.addView(txtEmptyState);

        Button btnGoAvailability = new Button(this);
        btnGoAvailability.setText("IR A DISPONIBILIDAD DE ESPACIOS");
        btnGoAvailability.setTextColor(Color.WHITE);
        btnGoAvailability.setBackgroundColor(getColor(R.color.primary_blue));
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        btnParams.topMargin = (int) (18 * getResources().getDisplayMetrics().density);
        btnGoAvailability.setLayoutParams(btnParams);
        btnGoAvailability.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterExitActivity.this, SpacesAvailabilityActivity.class);
            startActivity(intent);
            finish();
        });
        boxVacio.addView(btnGoAvailability);

        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(
                0,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        lp.topToBottom = R.id.parked_vehicle_header;
        lp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        lp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        lp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        parent.addView(boxVacio, lp);

        txtEmptyState.setTag(boxVacio);
    }

    private void mostrarEstadoVacio(boolean vacio) {
        int[] idsContenido = {
                R.id.sumary_info,
                R.id.card_vehicle,
                R.id.card_parking_summary,
                R.id.confirm_button
        };
        for (int id : idsContenido) {
            View v = findViewById(id);
            if (v != null) {
                v.setVisibility(vacio ? View.GONE : View.VISIBLE);
            }
        }
        if (txtEmptyState != null && txtEmptyState.getTag() instanceof View) {
            ((View) txtEmptyState.getTag()).setVisibility(vacio ? View.VISIBLE : View.GONE);
        }
    }

    private void cargarResumenSalida() {
        SharedPreferences sesion = getSharedPreferences("ParkControlSession", MODE_PRIVATE);
        int userId = sesion.getInt("user_id", -1);
        int selectedRecordId = getIntent().getIntExtra("selected_record_id", -1);
        int selectedVehicleId = getIntent().getIntExtra("selected_vehicle_id", -1);

        parqueoActual = consultarParqueoActivoParaSalida(userId, selectedRecordId, selectedVehicleId);
        if (parqueoActual == null) {
            mostrarEstadoVacio(true);
            return;
        }

        mostrarEstadoVacio(false);

        plateNumberTv.setText(parqueoActual.placa.isEmpty() ? "SIN PLACA" : parqueoActual.placa);
        vehicleTv.setText(parqueoActual.vehiculo.isEmpty() ? "Vehículo" : parqueoActual.vehiculo);
        colorTv.setText(parqueoActual.color.isEmpty() ? "Sin color" : parqueoActual.color);
        cargarFotoVehiculo(parqueoActual.photoUri);

        long ahoraMillis = System.currentTimeMillis();
        long elapsedMillis = Math.max(0L, ahoraMillis - parqueoActual.entryMillis);
        long minutos = Math.max(1L, (elapsedMillis + 59_999L) / 60_000L);
        long totalPagar = minutos * TARIFA_POR_MINUTO;

        valEntryTimeTv.setText(parqueoActual.horaIngresoTexto);
        valExitTimeTv.setText(new SimpleDateFormat("dd/MM/yyyy - hh:mm a", Locale.getDefault())
                .format(new Date(ahoraMillis)));
        valTotalTimeTv.setText(ParkedVehicleActivity.formatearDuracion(elapsedMillis));
        valPaymentTv.setText(formatearPesos(totalPagar));
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
                        vehicleImageIv.setImageBitmap(bmp);
                        return;
                    }
                }
            }
            try {
                vehicleImageIv.setImageURI(uri);
                if (vehicleImageIv.getDrawable() != null) {
                    return;
                }
            } catch (Exception ignored) {
            }
        }
        vehicleImageIv.setImageResource(R.drawable.toyota_corola);
    }

    private ParqueoSalidaDatos consultarParqueoActivoParaSalida(
            int userId,
            int selectedRecordId,
            int selectedVehicleId
    ) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        StringBuilder where = new StringBuilder("r.exit_timestamp IS NULL");
        ArrayList<String> args = new ArrayList<>();

        if (selectedRecordId > 0) {
            where.append(" AND r.record_id = ?");
            args.add(String.valueOf(selectedRecordId));
        } else if (selectedVehicleId > 0) {
            where.append(" AND r.vehicle_id = ?");
            args.add(String.valueOf(selectedVehicleId));
        } else if (userId > 0) {
            where.append(" AND r.user_id = ?");
            args.add(String.valueOf(userId));
        }

        String sql = "SELECT r.record_id, r.slot_id, s.slot_number, r.entry_timestamp, " +
                "v.license_plate, v.brand, v.vehicle_type, v.color, v.photo_uri " +
                "FROM parkingRecord r " +
                "LEFT JOIN parkingSlot s ON r.slot_id = s.slot_id " +
                "LEFT JOIN Vehicle v ON r.vehicle_id = v.vehicle_id " +
                "WHERE " + where +
                " ORDER BY r.record_id DESC LIMIT 1";

        try (Cursor c = db.rawQuery(sql, args.toArray(new String[0]))) {
            if (!c.moveToFirst()) {
                return null;
            }

            ParqueoSalidaDatos d = new ParqueoSalidaDatos();
            d.recordId = c.getInt(0);
            d.slotId = c.isNull(1) ? -1 : c.getInt(1);
            d.slotNumber = c.isNull(2) ? "" : c.getString(2).trim();
            d.entryTimestampRaw = c.isNull(3) ? "" : c.getString(3).trim();
            d.placa = c.isNull(4) ? "" : c.getString(4).trim();
            String marca = c.isNull(5) ? "" : c.getString(5).trim();
            String tipo = c.isNull(6) ? "" : c.getString(6).trim();
            d.color = c.isNull(7) ? "" : c.getString(7).trim();
            d.photoUri = c.isNull(8) ? "" : c.getString(8).trim();

            StringBuilder desc = new StringBuilder();
            if (!marca.isEmpty()) {
                desc.append(marca);
            }
            if (!tipo.isEmpty()) {
                if (desc.length() > 0) desc.append(" - ");
                desc.append(tipo);
            }
            d.vehiculo = desc.length() > 0 ? desc.toString() : "Vehículo";

            d.entryMillis = ParkedVehicleActivity.parsearFechaMillis(d.entryTimestampRaw);
            d.horaIngresoTexto = new SimpleDateFormat("dd/MM/yyyy - hh:mm a", Locale.getDefault())
                    .format(new Date(d.entryMillis));

            return d;
        } catch (Exception e) {
            android.util.Log.e("RegisterExitActivity", "Error consultando parqueo para salida", e);
            return null;
        }
    }

    private boolean confirmarSalidaEnBd(
            ParqueoSalidaDatos datos,
            String exitTimestampSql,
            long totalPagar
    ) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("exit_timestamp", exitTimestampSql);
            values.put("total_fee", (double) totalPagar);

            int actualizados = db.update(
                    "parkingRecord",
                    values,
                    "record_id = ? AND exit_timestamp IS NULL",
                    new String[]{String.valueOf(datos.recordId)}
            );

            if (actualizados <= 0) {
                return false;
            }

            if (datos.slotId > 0) {
                ContentValues slotValues = new ContentValues();
                slotValues.put("status", "disponible");
                db.update(
                        "parkingSlot",
                        slotValues,
                        "slot_id = ?",
                        new String[]{String.valueOf(datos.slotId)}
                );
            }

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            android.util.Log.e("RegisterExitActivity", "Error al confirmar salida en BD", e);
            return false;
        } finally {
            db.endTransaction();
        }
    }

    private String formatearPesos(long valor) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMANY);
        return "$ " + nf.format(valor);
    }
}