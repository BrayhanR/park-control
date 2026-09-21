package com.example.parkcontrol;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ParkControl.db";
    private static final int DATABASE_VERSION = 1;

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
                "password TEXT NOT NULL);";

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
}