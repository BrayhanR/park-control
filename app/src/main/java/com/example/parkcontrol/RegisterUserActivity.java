package com.example.parkcontrol;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterUserActivity extends AppCompatActivity {

    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private EditText editTextPhone;
    private Button buttonRegister;
    private TextView textLoginLink;
//    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        // Inicializar vistas
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextPhone = findViewById(R.id.editTextPhone);
        buttonRegister = findViewById(R.id.buttonRegister);
        textLoginLink = findViewById(R.id.textLoginLink);

        // Inicializar base de datos
        DatabaseHelper dbHelper = new DatabaseHelper(this);

        // Acción de registro
        buttonRegister.setOnClickListener(v -> {
            String name = editTextName.getText().toString();
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();
            String confirmPassword = editTextConfirmPassword.getText().toString();
            String phone = editTextPhone.getText().toString();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(RegisterUserActivity.this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(RegisterUserActivity.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("name", name);
            values.put("email", email);
            values.put("password", password);
            // Teléfono opcional, se puede guardar en otro campo si se requiere

            long result = db.insert("User", null, values);
            if (result == -1L) {
                Toast.makeText(RegisterUserActivity.this, "Error: el correo ya existe", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(RegisterUserActivity.this, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show();
                finish(); // Vuelve al LoginActivity
            }
        });

        // Enlace para volver al login
        textLoginLink.setOnClickListener(v -> finish());
    }
}
