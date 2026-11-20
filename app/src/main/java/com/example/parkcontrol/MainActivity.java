package com.example.parkcontrol;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login); // Redirección al activity principal
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Creación de base de datos y preparación de usuario por defecto
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int defaultUserId = asegurarUsuarioPorDefecto(db);

        if (defaultUserId > 0) {
            SharedPreferences sesion = getSharedPreferences("ParkControlSession", MODE_PRIVATE);
            if (!sesion.contains("user_id") || sesion.getInt("user_id", -1) <= 0) {
                sesion.edit().putInt("user_id", defaultUserId).apply();
            }
        }

        // Redirección temporal al activity de cada uno
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private int asegurarUsuarioPorDefecto(SQLiteDatabase db) {
        try (Cursor cursor = db.rawQuery(
                "SELECT user_id FROM User WHERE user_id = 1 LIMIT 1",
                null
        )) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }
        // Usuario de prueba
        ContentValues values = new ContentValues();
        values.put("user_id", 1);
        values.put("name", "Usuario Prueba");
        values.put("email", "prueba@parkcontrol.com");
        values.put("password", "123456");

        long insertedId = db.insertWithOnConflict(
                "User",
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
        );

        if (insertedId > 0) {
            return (int) insertedId;
        }

        try (Cursor cursor = db.rawQuery(
                "SELECT user_id FROM User ORDER BY user_id ASC LIMIT 1",
                null
        )) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }

        return -1;
    }
}