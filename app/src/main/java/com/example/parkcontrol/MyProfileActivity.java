package com.example.parkcontrol;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MyProfileActivity extends AppCompatActivity {

    // Para cambiar la foto de perfil
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ImageView imgProfileLogo;
    private DatabaseHelper dbHelper;

    private TextView tvName;
    private TextView tvEmail;
    private TextView tvPassword;
    private Button buttonEditProfile;

    // Guarda el user_id del usuario actualmente cargado en pantalla
    private int currentUserId = -1;

    // Controla si estamos en modo edición
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        ImageButton buttonGetBack = findViewById(R.id.my_profile_back_button);
        Button buttonChangeProfilePic = findViewById(R.id.edit_profile_pic_button);
        tvName = findViewById(R.id.profile_name);
        tvEmail = findViewById(R.id.profile_email);
        tvPassword = findViewById(R.id.tv_profile_password);
        ImageButton btnClosed = findViewById(R.id.btn_toggle_password_closed);
        ImageButton btnOpen = findViewById(R.id.btn_toggle_password_open);
        SwitchMaterial switchNotifications = findViewById(R.id.switch_notifications);
        buttonEditProfile = findViewById(R.id.edit_profile_button);

        // Botón regresar
        buttonGetBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainMenuActivity.class);
            startActivity(intent);
            finish();
        });

        // Boton para cambiar foto de perfil
        imgProfileLogo = findViewById(R.id.blank_profile_logo);

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            File savedFile = guardarImagenEnCarpetaInterna(selectedImageUri);
                            if (savedFile != null) {
                                Uri localUri = Uri.fromFile(savedFile);
                                imgProfileLogo.setImageURI(localUri);
                                guardarRutaFotoEnBD(localUri.toString());
                                Toast.makeText(this, "Foto de perfil guardada en la app", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "No se pudo copiar la imagen", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );

        buttonChangeProfilePic.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(galleryIntent);
        });

        // Botones ocultar/mostrar contraseña
        btnClosed.setOnClickListener(v -> {
            tvPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            btnClosed.setVisibility(android.view.View.GONE);
            btnOpen.setVisibility(android.view.View.VISIBLE);
        });

        btnOpen.setOnClickListener(v -> {
            tvPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            btnOpen.setVisibility(android.view.View.GONE);
            btnClosed.setVisibility(android.view.View.VISIBLE);
        });

        // Switch para recibir notificaciones
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(this, "Notificaciones de parqueo activadas (Simulación)", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notificaciones de parqueo desactivadas (Simulación)", Toast.LENGTH_SHORT).show();
            }
        });

        // Listeners para editar cada campo cuando isEditing sea true
        tvName.setOnClickListener(v -> {
            if (isEditing) {
                mostrarDialogoCampo("Editar nombre", tvName, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
            }
        });

        tvEmail.setOnClickListener(v -> {
            if (isEditing) {
                mostrarDialogoCampo("Editar correo", tvEmail, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            }
        });

        tvPassword.setOnClickListener(v -> {
            if (isEditing) {
                mostrarDialogoCampo("Editar contraseña", tvPassword, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
        });

        // Botón editar perfil / guardar cambios
        buttonEditProfile.setOnClickListener(v -> {
            if (!isEditing) {
                activarModoEdicion(true);
            } else {
                guardarCambiosPerfilEnBD();
            }
        });

        // Cargar datos del usuario desde SQLite
        cargarDatosUsuario();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dbHelper != null && !isEditing) {
            cargarDatosUsuario();
        }
    }

    private void activarModoEdicion(boolean activar) {
        isEditing = activar;

        if (activar) {
            buttonEditProfile.setText("GUARDAR CAMBIOS");
            int colorResaltado = Color.parseColor("#E3F2FD");
            tvName.setBackgroundColor(colorResaltado);
            tvEmail.setBackgroundColor(colorResaltado);
            tvPassword.setBackgroundColor(colorResaltado);
            Toast.makeText(this, "Toca el nombre, correo o contraseña para editarlos", Toast.LENGTH_SHORT).show();
        } else {
            buttonEditProfile.setText("EDITAR PERFIL");
            tvName.setBackgroundColor(Color.TRANSPARENT);
            tvEmail.setBackgroundColor(Color.TRANSPARENT);
            tvPassword.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void mostrarDialogoCampo(String titulo, TextView campoDestino, int inputType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titulo);

        final EditText input = new EditText(this);
        input.setInputType(inputType);
        input.setText(campoDestino.getText().toString());
        input.setSelection(input.getText().length());
        builder.setView(input);

        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            String nuevoValor = input.getText().toString().trim();
            if (nuevoValor.isEmpty()) {
                Toast.makeText(this, "El campo no puede estar vacío", Toast.LENGTH_SHORT).show();
                return;
            }
            campoDestino.setText(nuevoValor);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void guardarCambiosPerfilEnBD() {
        if (currentUserId <= 0) {
            Toast.makeText(this, "No se encontró un usuario válido para actualizar", Toast.LENGTH_SHORT).show();
            return;
        }

        String nuevoNombre = tvName.getText().toString().trim();
        String nuevoCorreo = tvEmail.getText().toString().trim();
        String nuevaPassword = tvPassword.getText().toString().trim();

        if (nuevoNombre.isEmpty() || nuevoCorreo.isEmpty() || nuevaPassword.isEmpty()) {
            Toast.makeText(this, "Nombre, correo y contraseña son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("name", nuevoNombre);
            values.put("email", nuevoCorreo);
            values.put("password", nuevaPassword);

            int filasActualizadas = db.update(
                    "User",
                    values,
                    "user_id = ?",
                    new String[]{String.valueOf(currentUserId)}
            );

            if (filasActualizadas > 0) {
                activarModoEdicion(false);
                Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No se pudo actualizar el perfil", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            android.util.Log.e("MyProfileActivity", "Error al actualizar perfil en BD", e);
            Toast.makeText(this, "Error al guardar: verifica que el correo no esté repetido", Toast.LENGTH_LONG).show();
        }
    }

    private void cargarDatosUsuario() {
        SharedPreferences sesion = getSharedPreferences("ParkControlSession", MODE_PRIVATE);
        int userId = sesion.getInt("user_id", -1);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            if (userId > 0) {
                cursor = db.rawQuery(
                        "SELECT user_id, name, email, password, profile_image_uri FROM User WHERE user_id = ?",
                        new String[]{String.valueOf(userId)}
                );
            }

            // Si no hay userId en sesión o no se encontró, toma el último usuario registrado
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                cursor = db.rawQuery(
                        "SELECT user_id, name, email, password, profile_image_uri FROM User ORDER BY user_id DESC LIMIT 1",
                        null
                );
            }

            if (cursor != null && cursor.moveToFirst()) {
                currentUserId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                String password = cursor.getString(cursor.getColumnIndexOrThrow("password"));
                String profileImageUri = cursor.getString(cursor.getColumnIndexOrThrow("profile_image_uri"));

                tvName.setText(name != null ? name : "Sin nombre");
                tvEmail.setText(email != null ? email : "Sin correo");
                tvPassword.setText(password != null ? password : "");

                if (profileImageUri != null && !profileImageUri.isEmpty()) {
                    File archivoLocal = new File(Uri.parse(profileImageUri).getPath());
                    if (archivoLocal.exists()) {
                        imgProfileLogo.setImageURI(Uri.fromFile(archivoLocal));
                    } else {
                        imgProfileLogo.setImageURI(Uri.parse(profileImageUri));
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("MyProfileActivity", "Error al cargar usuario desde BD", e);
            Toast.makeText(this, "Error al cargar datos del perfil", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private File guardarImagenEnCarpetaInterna(Uri sourceUri) {
        File carpetaPerfil = new File(getFilesDir(), "profile_images");
        if (!carpetaPerfil.exists() && !carpetaPerfil.mkdirs()) {
            return null;
        }

        String nombreArchivo = "user_" + (currentUserId > 0 ? currentUserId : "default") + "_profile.jpg";
        File destino = new File(carpetaPerfil, nombreArchivo);

        try (InputStream inputStream = getContentResolver().openInputStream(sourceUri);
             OutputStream outputStream = new FileOutputStream(destino)) {

            if (inputStream == null) {
                return null;
            }

            byte[] buffer = new byte[4096];
            int bytesLeidos;
            while ((bytesLeidos = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesLeidos);
            }
            outputStream.flush();
            return destino;

        } catch (Exception e) {
            android.util.Log.e("MyProfileActivity", "Error al copiar imagen a carpeta interna", e);
            return null;
        }
    }

    private void guardarRutaFotoEnBD(String uriString) {
        if (currentUserId <= 0) {
            return;
        }

        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("profile_image_uri", uriString);

            db.update(
                    "User",
                    values,
                    "user_id = ?",
                    new String[]{String.valueOf(currentUserId)}
            );
        } catch (Exception e) {
            android.util.Log.e("MyProfileActivity", "Error al guardar profile_image_uri en BD", e);
        }
    }
}