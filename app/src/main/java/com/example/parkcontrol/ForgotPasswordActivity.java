package com.example.parkcontrol;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private Button buttonRecover;
//    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        editTextEmail = findViewById(R.id.editTextEmail);
        buttonRecover = findViewById(R.id.buttonRecover);
//        dbHelper = new DBHelper(this);

        buttonRecover.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString();

            if (email.isEmpty()) {
                Toast.makeText(ForgotPasswordActivity.this, "Ingresa tu correo electrónico", Toast.LENGTH_SHORT).show();
                return;
            }

//            Cursor cursor = dbHelper.getReadableDatabase()
//                    .rawQuery("SELECT * FROM User WHERE email = ?", new String[]{email});

//            if (cursor.moveToFirst()) {
//                // Aquí podrías integrar envío de correo real con Firebase/Auth o tu servidor
//                Toast.makeText(ForgotPasswordActivity.this, "Se enviaron instrucciones a tu correo", Toast.LENGTH_LONG).show();
//            } else {
//                Toast.makeText(ForgotPasswordActivity.this, "Correo no registrado", Toast.LENGTH_SHORT).show();
//            }

//            cursor.close();
        });
    }
}
