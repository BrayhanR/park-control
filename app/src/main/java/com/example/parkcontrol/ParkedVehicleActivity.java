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
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

public class ParkedVehicleActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextView txtEmptyState;
    private Spinner spinnerActiveVehicles;

    private TextView plateNumberTv;
    private TextView vehicleTv;
    private TextView colorTv;
    private ImageView vehicleImageIv;
    private TextView slotNumberTv;
    private TextView valEntryTimeTv;
    private TextView valElapsedTimeTv;
    private Button btnLocation;
    private Button btnExitView;

    private final List<ParqueoActivo> parqueosActivos = new ArrayList<>();
    private int indiceSeleccionado = 0;
    private long entryTimeMillis = -1L;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (entryTimeMillis > 0 && valElapsedTimeTv != null) {
                long elapsedMillis = Math.max(0L, System.currentTimeMillis() - entryTimeMillis);
                valElapsedTimeTv.setText(formatearDuracion(elapsedMillis));
                timerHandler.postDelayed(this, 1000L);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_parked_vehicle);

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
        slotNumberTv = findViewById(R.id.slot_number);
        valEntryTimeTv = findViewById(R.id.val_entry_time);
        valElapsedTimeTv = findViewById(R.id.val_elapsed_time);
        btnLocation = findViewById(R.id.location_button);
        btnExitView = findViewById(R.id.exit_view_button);

        prepararSelectorVehiculosYVistaVacia();

        ImageButton parked_vehicle_out_back_btn = findViewById(R.id.parked_vehicle_back_button);
        parked_vehicle_out_back_btn.setOnClickListener(v -> {
            Intent intent = new Intent(ParkedVehicleActivity.this, MainMenuActivity.class);
            startActivity(intent);
            finish();
        });

        btnLocation.setOnClickListener(v -> {
            Toast.makeText(this, "Ubicación en GPS (Simulación)", Toast.LENGTH_SHORT).show();
            String geoUri = "geo:0,0?q=Parqueadero";
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri)));
            }
        });

        btnExitView.setOnClickListener(v -> {
            Intent intent = new Intent(ParkedVehicleActivity.this, RegisterExitActivity.class);
            if (!parqueosActivos.isEmpty() && indiceSeleccionado < parqueosActivos.size()) {
                ParqueoActivo actual = parqueosActivos.get(indiceSeleccionado);
                if (actual.registroId > 0) {
                    intent.putExtra("selected_record_id", actual.registroId);
                }
                if (actual.vehicleId > 0) {
                    intent.putExtra("selected_vehicle_id", actual.vehicleId);
                }
            }
            startActivity(intent);
        });

        cargarParqueosActivos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarParqueosActivos();
    }

    @Override
    protected void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
    }

    private void prepararSelectorVehiculosYVistaVacia() {
        View root = findViewById(R.id.main);
        if (!(root instanceof ConstraintLayout)) {
            return;
        }
        ConstraintLayout parent = (ConstraintLayout) root;

        int margin16 = (int) (16 * getResources().getDisplayMetrics().density);
        int margin8 = (int) (8 * getResources().getDisplayMetrics().density);
        int spinnerHeight = (int) (46 * getResources().getDisplayMetrics().density);

        spinnerActiveVehicles = new Spinner(this);
        spinnerActiveVehicles.setId(View.generateViewId());
        spinnerActiveVehicles.setVisibility(View.GONE);
        spinnerActiveVehicles.setBackgroundColor(Color.parseColor("#F1F3F5"));

        ConstraintLayout.LayoutParams spParams = new ConstraintLayout.LayoutParams(
                0,
                spinnerHeight
        );
        spParams.topToBottom = R.id.current_info;
        spParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        spParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        spParams.topMargin = margin8;
        spParams.leftMargin = margin16;
        spParams.rightMargin = margin16;
        parent.addView(spinnerActiveVehicles, spParams);

        View cardVehicle = findViewById(R.id.card_vehicle);
        if (cardVehicle != null && cardVehicle.getLayoutParams() instanceof ConstraintLayout.LayoutParams) {
            ConstraintLayout.LayoutParams cardParams = (ConstraintLayout.LayoutParams) cardVehicle.getLayoutParams();
            cardParams.topToBottom = spinnerActiveVehicles.getId();
            cardParams.topMargin = margin8;
            cardVehicle.setLayoutParams(cardParams);
        }

        spinnerActiveVehicles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (position >= 0 && position < parqueosActivos.size()) {
                    indiceSeleccionado = position;
                    mostrarParqueo(parqueosActivos.get(position));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

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
            Intent intent = new Intent(ParkedVehicleActivity.this, SpacesAvailabilityActivity.class);
            startActivity(intent);
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
                R.id.current_info,
                R.id.card_vehicle,
                R.id.card_parking,
                R.id.location_button,
                R.id.exit_view_button
        };
        for (int id : idsContenido) {
            View v = findViewById(id);
            if (v != null) {
                v.setVisibility(vacio ? View.GONE : View.VISIBLE);
            }
        }
        if (spinnerActiveVehicles != null && vacio) {
            spinnerActiveVehicles.setVisibility(View.GONE);
        }
        if (txtEmptyState != null && txtEmptyState.getTag() instanceof View) {
            ((View) txtEmptyState.getTag()).setVisibility(vacio ? View.VISIBLE : View.GONE);
        }
    }

    private void cargarParqueosActivos() {
        timerHandler.removeCallbacks(timerRunnable);
        entryTimeMillis = -1L;

        SharedPreferences sesion = getSharedPreferences("ParkControlSession", MODE_PRIVATE);
        int userId = sesion.getInt("user_id", -1);

        int registroPrevioId = (!parqueosActivos.isEmpty() && indiceSeleccionado < parqueosActivos.size())
                ? parqueosActivos.get(indiceSeleccionado).registroId
                : -1;

        parqueosActivos.clear();
        parqueosActivos.addAll(consultarParqueosActivos(userId));

        if (parqueosActivos.isEmpty()) {
            indiceSeleccionado = 0;
            mostrarEstadoVacio(true);
            return;
        }

        mostrarEstadoVacio(false);

        int nuevoIndice = 0;
        if (registroPrevioId > 0) {
            for (int i = 0; i < parqueosActivos.size(); i++) {
                if (parqueosActivos.get(i).registroId == registroPrevioId) {
                    nuevoIndice = i;
                    break;
                }
            }
        }
        indiceSeleccionado = nuevoIndice;

        if (parqueosActivos.size() > 1 && spinnerActiveVehicles != null) {
            List<String> opciones = new ArrayList<>();
            for (ParqueoActivo p : parqueosActivos) {
                String labelEspacio = p.espacio.isEmpty() ? "Sin espacio" : "Espacio " + p.espacio;
                String labelPlaca = p.placa.isEmpty() ? "Vehículo" : p.placa;
                opciones.add(labelPlaca + " (" + labelEspacio + ")");
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    opciones
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerActiveVehicles.setAdapter(adapter);
            spinnerActiveVehicles.setSelection(indiceSeleccionado, false);
            spinnerActiveVehicles.setVisibility(View.VISIBLE);
        } else if (spinnerActiveVehicles != null) {
            spinnerActiveVehicles.setVisibility(View.GONE);
        }

        mostrarParqueo(parqueosActivos.get(indiceSeleccionado));
    }

    private void mostrarParqueo(ParqueoActivo datos) {
        timerHandler.removeCallbacks(timerRunnable);

        plateNumberTv.setText(datos.placa.isEmpty() ? "SIN PLACA" : datos.placa);
        vehicleTv.setText(datos.vehiculo.isEmpty() ? "Vehículo" : datos.vehiculo);
        colorTv.setText(datos.color.isEmpty() ? "Sin color" : datos.color);
        slotNumberTv.setText("Espacio # " + (datos.espacio.isEmpty() ? "0000" : datos.espacio));
        valEntryTimeTv.setText(datos.horaIngresoTexto);

        cargarFotoVehiculo(datos.photoUri);

        entryTimeMillis = datos.entryMillis > 0 ? datos.entryMillis : System.currentTimeMillis();
        valElapsedTimeTv.setText(formatearDuracion(Math.max(0L, System.currentTimeMillis() - entryTimeMillis)));
        timerHandler.postDelayed(timerRunnable, 1000L);
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

    static class ParqueoActivo {
        int registroId = -1;
        int vehicleId = -1;
        String placa = "";
        String vehiculo = "";
        String color = "";
        String photoUri = "";
        String espacio = "";
        String horaIngresoTexto = "";
        long entryMillis = 0L;
    }

    private List<ParqueoActivo> consultarParqueosActivos(int userId) {
        List<ParqueoActivo> resultados = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String whereUser = userId > 0 ? " AND r.user_id = ?" : "";
        String[] args = userId > 0 ? new String[]{String.valueOf(userId)} : new String[0];

        String sql = "SELECT r.record_id, r.vehicle_id, s.slot_number, r.entry_timestamp, " +
                "v.license_plate, v.brand, v.vehicle_type, v.color, v.photo_uri " +
                "FROM parkingRecord r " +
                "LEFT JOIN parkingSlot s ON r.slot_id = s.slot_id " +
                "LEFT JOIN Vehicle v ON r.vehicle_id = v.vehicle_id " +
                "WHERE r.exit_timestamp IS NULL" + whereUser +
                " ORDER BY r.record_id DESC";

        try (Cursor c = db.rawQuery(sql, args)) {
            SimpleDateFormat formatoLocal = new SimpleDateFormat("dd/MM/yyyy - hh:mm a", Locale.getDefault());
            while (c.moveToNext()) {
                ParqueoActivo p = new ParqueoActivo();
                p.registroId = c.getInt(0);
                p.vehicleId = c.getInt(1);
                p.espacio = c.isNull(2) ? "" : c.getString(2).trim();
                String ingresoRaw = c.isNull(3) ? "" : c.getString(3).trim();
                p.placa = c.isNull(4) ? "" : c.getString(4).trim();
                String marca = c.isNull(5) ? "" : c.getString(5).trim();
                String tipo = c.isNull(6) ? "" : c.getString(6).trim();
                p.color = c.isNull(7) ? "" : c.getString(7).trim();
                p.photoUri = c.isNull(8) ? "" : c.getString(8).trim();

                StringBuilder desc = new StringBuilder();
                if (!marca.isEmpty()) {
                    desc.append(marca);
                }
                if (!tipo.isEmpty()) {
                    if (desc.length() > 0) desc.append(" - ");
                    desc.append(tipo);
                }
                p.vehiculo = desc.length() > 0 ? desc.toString() : "Vehículo";

                p.entryMillis = parsearFechaMillis(ingresoRaw);
                p.horaIngresoTexto = formatoLocal.format(new Date(p.entryMillis));

                resultados.add(p);
            }
        } catch (Exception e) {
            android.util.Log.e("ParkedVehicleActivity", "Error consultando parqueos activos", e);
        }
        return resultados;
    }

    static String buscarTablaExistente(SQLiteDatabase db, String... candidatos) {
        for (String nombre : candidatos) {
            try (Cursor c = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND LOWER(name)=LOWER(?) LIMIT 1",
                    new String[]{nombre}
            )) {
                if (c.moveToFirst()) {
                    return c.getString(0);
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    static Set<String> obtenerColumnas(SQLiteDatabase db, String tabla) {
        Set<String> cols = new HashSet<>();
        try (Cursor c = db.rawQuery("PRAGMA table_info(" + tabla + ")", null)) {
            int idx = c.getColumnIndex("name");
            while (c.moveToNext() && idx >= 0) {
                cols.add(c.getString(idx).toLowerCase(Locale.ROOT));
            }
        } catch (Exception ignored) {
        }
        return cols;
    }

    static String primeraColumna(Set<String> cols, String... candidatos) {
        for (String cand : candidatos) {
            if (cols.contains(cand.toLowerCase(Locale.ROOT))) {
                return cand;
            }
        }
        return null;
    }

    static long parsearFechaMillis(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return System.currentTimeMillis();
        }
        String limpio = raw.trim();
        try {
            long val = Long.parseLong(limpio);
            if (val > 0 && val < 10_000_000_000L) {
                return val * 1000L;
            }
            return val;
        } catch (NumberFormatException ignored) {
        }

        String[] formatos = {
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd HH:mm",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "dd/MM/yyyy - hh:mm a",
                "dd/MM/yyyy HH:mm:ss",
                "dd/MM/yyyy HH:mm",
                "yyyy/MM/dd HH:mm:ss"
        };
        for (String fmt : formatos) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(fmt, Locale.getDefault());
                if (fmt.startsWith("yyyy-MM-dd") && !fmt.contains("a")) {
                    sdf.setTimeZone(TimeZone.getTimeZone("America/Bogota"));
                }
                Date d = sdf.parse(limpio);
                if (d != null) {
                    return d.getTime();
                }
            } catch (ParseException ignored) {
            }
        }
        return System.currentTimeMillis();
    }

    static String formatearDuracion(long millis) {
        long totalSegundos = Math.max(0L, millis / 1000L);
        long horas = totalSegundos / 3600L;
        long minutos = (totalSegundos % 3600L) / 60L;
        long segundos = totalSegundos % 60L;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", horas, minutos, segundos);
    }
}