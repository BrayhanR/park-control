package com.example.parkcontrol;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ParkControl.db";
    private static final int DATABASE_VERSION = 2; // Incrementamos la versión para forzar la actualización

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true); // Habilita el soporte de llaves foráneas en SQLite
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // TABLAS INDEPENDIENTES (Sin llaves foráneas)

        // Tabla: User
        String CREATE_USER_TABLE = "CREATE TABLE User (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "email TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "profile_image_uri TEXT);";

        // Tabla: parkingSlot
        String CREATE_PARKING_SLOT_TABLE = "CREATE TABLE parkingSlot (" +
                "slot_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "slot_number TEXT NOT NULL, " +
                "status TEXT NOT NULL);";

        // TABLA DEPENDIENTE (Depende de User)

        // Tabla: Vehicle
        String CREATE_VEHICLE_TABLE = "CREATE TABLE Vehicle (" +
                "vehicle_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "license_plate TEXT UNIQUE NOT NULL, " +
                "vehicle_type TEXT NOT NULL, " +
                "brand TEXT, " +
                "color TEXT, " +
                "photo_uri TEXT, " +
                "FOREIGN KEY(user_id) REFERENCES User(user_id) ON DELETE CASCADE);";

        // TABLA DEPENDIENTE DE NIVEL 2 (Intermedia / Transaccional)

        // Tabla: parkingRecord
        String CREATE_PARKING_RECORD_TABLE = "CREATE TABLE parkingRecord (" +
                "record_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "vehicle_id INTEGER NOT NULL, " +
                "slot_id INTEGER NOT NULL, " +
                "user_id INTEGER NOT NULL, " +
                "entry_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "exit_timestamp DATETIME, " +
                "total_fee REAL, " +
                "parking_address TEXT, " +
                "FOREIGN KEY(vehicle_id) REFERENCES Vehicle(vehicle_id), " +
                "FOREIGN KEY(slot_id) REFERENCES parkingSlot(slot_id), " +
                "FOREIGN KEY(user_id) REFERENCES User(user_id));";

        db.execSQL(CREATE_USER_TABLE);
        db.execSQL(CREATE_PARKING_SLOT_TABLE);
        db.execSQL(CREATE_VEHICLE_TABLE);
        db.execSQL(CREATE_PARKING_RECORD_TABLE);
    }

    // En caso de actualizar la BD, se eliminan en orden inverso a sus dependencias
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS parkingRecord;");
        db.execSQL("DROP TABLE IF EXISTS Vehicle;");
        db.execSQL("DROP TABLE IF EXISTS parkingSlot;");
        db.execSQL("DROP TABLE IF EXISTS User;");
        onCreate(db);
    }

    public void inicializarEspacios() {
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            for (int i = 1; i <= 12; i++) {
                String numero = i < 10 ? "0" + i : String.valueOf(i);

                db.execSQL(
                        "INSERT INTO parkingSlot (slot_number, status) " +
                                "SELECT ?, ? WHERE NOT EXISTS " +
                                "(SELECT 1 FROM parkingSlot WHERE slot_number = ?)",
                        new Object[]{numero, "disponible", numero}
                );
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public String obtenerEstadoEspacio(String numeroEspacio) {
        SQLiteDatabase db = getReadableDatabase();

        try (android.database.Cursor cursor = db.rawQuery(
                "SELECT status FROM parkingSlot WHERE slot_number = ?",
                new String[]{numeroEspacio}
        )) {
            if (cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        }

        return "no_encontrado";
    }

    public int contarEspacios(String estado) {
        SQLiteDatabase db = getReadableDatabase();

        String consulta;
        String[] parametros;

        if (estado == null) {
            consulta = "SELECT COUNT(*) FROM parkingSlot";
            parametros = null;
        } else {
            consulta = "SELECT COUNT(*) FROM parkingSlot WHERE status = ?";
            parametros = new String[]{estado};
        }

        try (android.database.Cursor cursor =
                     db.rawQuery(consulta, parametros)) {

            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }

        return 0;
    }

    public android.database.Cursor obtenerVehiculosDelUsuario(int userId) {
        SQLiteDatabase db = getReadableDatabase();

        return db.rawQuery(
                "SELECT vehicle_id, license_plate, vehicle_type, brand, color " +
                        "FROM Vehicle WHERE user_id = ? ORDER BY vehicle_id DESC",
                new String[]{String.valueOf(userId)}
        );
    }

    public boolean registrarIngreso(int vehicleId, int userId, String numeroEspacio) {

        if (vehicleId <= 0 || userId <= 0 || numeroEspacio == null) {
            return false;
        }

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            int slotId;

            // Verificar que el espacio esté disponible
            try (android.database.Cursor cursor = db.rawQuery(
                    "SELECT slot_id FROM parkingSlot " +
                            "WHERE slot_number = ? AND status = ?",
                    new String[]{numeroEspacio, "disponible"})) {

                if (!cursor.moveToFirst()) {
                    return false;
                }

                slotId = cursor.getInt(0);
            }

            // Verificar que el vehículo pertenezca al usuario
            try (android.database.Cursor cursor = db.rawQuery(
                    "SELECT vehicle_id FROM Vehicle " +
                            "WHERE vehicle_id = ? AND user_id = ?",
                    new String[]{
                            String.valueOf(vehicleId),
                            String.valueOf(userId)
                    })) {

                if (!cursor.moveToFirst()) {
                    return false;
                }
            }

            // Evitar que el mismo vehículo tenga dos ingresos activos
            try (android.database.Cursor cursor = db.rawQuery(
                    "SELECT record_id FROM parkingRecord " +
                            "WHERE vehicle_id = ? AND exit_timestamp IS NULL",
                    new String[]{String.valueOf(vehicleId)})) {

                if (cursor.moveToFirst()) {
                    return false;
                }
            }

            // Guardar el ingreso
            android.content.ContentValues datos =
                    new android.content.ContentValues();

            datos.put("vehicle_id", vehicleId);
            datos.put("slot_id", slotId);
            datos.put("user_id", userId);

            long registro = db.insert("parkingRecord", null, datos);

            if (registro == -1) {
                return false;
            }

            // Marcar el espacio como ocupado
            android.content.ContentValues estado =
                    new android.content.ContentValues();

            estado.put("status", "ocupado");

            int actualizados = db.update(
                    "parkingSlot",
                    estado,
                    "slot_id = ? AND status = ?",
                    new String[]{String.valueOf(slotId), "disponible"}
            );

            if (actualizados != 1) {
                return false;
            }

            db.setTransactionSuccessful();
            return true;

        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper",
                    "Error al registrar ingreso", e);
            return false;

        } finally {
            db.endTransaction();
        }
    }

}