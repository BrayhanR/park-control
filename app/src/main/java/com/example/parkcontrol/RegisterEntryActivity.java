package com.example.parkcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterEntryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_entry);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {

            Insets systemBars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
            );

            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );

            return insets;
        });

        // Flecha para volver a disponibilidad de espacios
        TextView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        // Recibir y mostrar el espacio seleccionado
        TextView txtSelectedSpace = findViewById(R.id.txtSelectedSpace);

        String espacioSeleccionado =
                getIntent().getStringExtra("selected_space");

        if (espacioSeleccionado != null) {
            txtSelectedSpace.setText(
                    "Piso 1 - Espacio " + espacioSeleccionado
            );
        }

        // Botón registrar y guardar ingreso

        Button registerEntry = findViewById(R.id.btnRegisterEntry);
        registerEntry.setOnClickListener(v -> {
            Toast.makeText(this, "Ingreso registrado (Simulación)", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, MainMenuActivity.class);
            startActivity(intent);
            finish();
        });

    }
}