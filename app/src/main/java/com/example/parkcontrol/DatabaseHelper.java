package com.example.parkcontrol;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Nombre de la base de datos
    private static final String DATABASE_NAME = "parkcontrol.db";

    // Versión de la base de datos
    private static final int DATABASE_VERSION = 1;

    // Nombre de la tabla
    public static final String TABLE_VEHICLE = "VEHICLE";

    // Campos de la tabla
    public static final String COLUMN_VEHICLE_ID = "vehicle_id";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_LICENSE_PLATE = "license_plate";
    public static final String COLUMN_VEHICLE_TYPE = "vehicle_type";
    public static final String COLUMN_BRAND = "Brand";
    public static final String COLUMN_COLOR = "color";

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Crear las tablas
    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_VEHICULE_TABLE = "CREATE TABLE "
                + TABLE_VEHICLE + " ("
                + COLUMN_VEHICLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USER_ID + " INTEGER NOT NULL, "
                + COLUMN_LICENSE_PLATE + " TEXT NOT NULL UNIQUE, "
                + COLUMN_VEHICLE_TYPE + " TEXT NOT NULL, "
                + COLUMN_BRAND + " TEXT NOT NULL, "
                + COLUMN_COLOR + " TEXT NOT NULL"
                + ")";

        db.execSQL(CREATE_VEHICULE_TABLE);
    }

    // Actualizar la estructura de la base de datos
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VEHICLE);

        onCreate(db);
    }

    // Insertar vehículo
    public long insertarVehiculo(
            int userId,
            String licensePlate,
            String vehicleType,
            String brand,
            String color) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_LICENSE_PLATE, licensePlate);
        values.put(COLUMN_VEHICLE_TYPE, vehicleType);
        values.put(COLUMN_BRAND, brand);
        values.put(COLUMN_COLOR, color);

        long resultado = db.insert(
                TABLE_VEHICLE,
                null,
                values
        );

        db.close();

        return resultado;
    }
}