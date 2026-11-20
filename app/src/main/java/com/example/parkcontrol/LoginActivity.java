package com.example.parkcontrol;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private TextView textRegisterLink;
    private TextView textForgotPassword;
    private CheckBox checkRememberMe;
    private CheckBox checkShowPassword;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar vistas
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textRegisterLink = findViewById(R.id.textRegisterLink);
        textForgotPassword = findViewById(R.id.textForgotPassword);
        checkRememberMe = findViewById(R.id.checkRememberMe);

        // Agregar "Mostrar contraseña" arriba de "Recuérdame" sin tocar el XML
        agregarCheckboxMostrarContrasena();

        // Inicializar base de datos
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        db = dbHelper.getReadableDatabase();

        // Acción de login
        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString();

            android.database.Cursor cursor = db.rawQuery(
                    "SELECT * FROM User WHERE email=? AND password=?",
                    new String[]{email, password}
            );

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                int userId = cursor.getInt(
                        cursor.getColumnIndexOrThrow("user_id")
                );

                getSharedPreferences("ParkControlSession", MODE_PRIVATE)
                        .edit()
                        .putInt("user_id", userId)
                        .apply();
                Toast.makeText(LoginActivity.this, "Login exitoso", Toast.LENGTH_SHORT).show();
                cursor.close();

                Intent intent = new Intent(LoginActivity.this, MainMenuActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Credenciales inválidas", Toast.LENGTH_SHORT).show();
                cursor.close();
            }
        });

        // Enlace a registro
        textRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterUserActivity.class);
            startActivity(intent);
        });

        // Enlace a recuperación de contraseña
        textForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        // Botón temporal (Borrar despues)
        TextView btnTemporal = findViewById(R.id.temporalBtn);

        btnTemporal.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LoginActivity.this,
                    MainMenuActivity.class
            );

            startActivity(intent);
        });
    }

    private void agregarCheckboxMostrarContrasena() {
        View root = findViewById(R.id.main);
        if (!(root instanceof ConstraintLayout) || checkRememberMe == null || editTextPassword == null) {
            return;
        }

        ConstraintLayout parent = (ConstraintLayout) root;

        checkShowPassword = new CheckBox(this);
        checkShowPassword.setId(View.generateViewId());
        checkShowPassword.setText("Mostrar contraseña");
        checkShowPassword.setTextColor(getColor(R.color.clear_grey));

        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        params.topToBottom = R.id.editTextPassword;
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        params.leftMargin = (int) (32 * getResources().getDisplayMetrics().density);
        params.topMargin = (int) (8 * getResources().getDisplayMetrics().density);

        parent.addView(checkShowPassword, params);

        if (checkRememberMe.getLayoutParams() instanceof ConstraintLayout.LayoutParams) {
            ConstraintLayout.LayoutParams rememberParams =
                    (ConstraintLayout.LayoutParams) checkRememberMe.getLayoutParams();
            rememberParams.topToBottom = checkShowPassword.getId();
            rememberParams.topMargin = (int) (4 * getResources().getDisplayMetrics().density);
            checkRememberMe.setLayoutParams(rememberParams);
        }

        checkShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                editTextPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                editTextPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            editTextPassword.setSelection(editTextPassword.getText().length());
        });
    }
}