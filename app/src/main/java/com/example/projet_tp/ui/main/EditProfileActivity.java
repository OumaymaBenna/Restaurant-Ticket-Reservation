package com.example.projet_tp.ui.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projet_tp.R;
import com.example.projet_tp.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private MaterialToolbar toolbar;
    private CircleImageView imageViewProfile;
    private TextInputEditText editTextFullName, editTextEmail, editTextStudentId, editTextPhone, editTextUniversity;
    private MaterialButton buttonSave, buttonChangePhoto;
    private SessionManager sessionManager;
    private Uri selectedImageUri;
    private boolean imageSelected = false;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        sessionManager = new SessionManager(this);

        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    handleImageSelection(result.getData());
                }
            }
        );

        initViews();
        setupToolbar();
        loadUserData();
        setupClickListeners();
    }

    private void initViews() {
        try {
            toolbar = findViewById(R.id.toolbar);
            imageViewProfile = findViewById(R.id.imageViewProfile);
            editTextFullName = findViewById(R.id.editTextFullName);
            editTextEmail = findViewById(R.id.editTextEmail);
            editTextStudentId = findViewById(R.id.editTextStudentId);
            editTextPhone = findViewById(R.id.editTextPhone);
            editTextUniversity = findViewById(R.id.editTextUniversity);
            buttonSave = findViewById(R.id.buttonSave);
            buttonChangePhoto = findViewById(R.id.buttonChangePhoto);

            if (toolbar == null || imageViewProfile == null || editTextFullName == null ||
                editTextEmail == null || editTextStudentId == null ||
                editTextPhone == null || editTextUniversity == null ||
                buttonSave == null || buttonChangePhoto == null) {
                android.util.Log.e("EditProfileActivity", "Un ou plusieurs éléments du layout sont manquants");
                Toast.makeText(this, "Erreur: Layout incomplet", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } catch (Exception e) {
            android.util.Log.e("EditProfileActivity", "Erreur lors de l'initialisation des vues: " + e.getMessage(), e);
            Toast.makeText(this, "Erreur lors du chargement de l'éditeur de profil", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupToolbar() {
        try {
            if (toolbar == null) {
                android.util.Log.e("EditProfileActivity", "Toolbar est null");
                return;
            }
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Modifier le profil");
            }
            toolbar.setNavigationOnClickListener(v -> {
                getOnBackPressedDispatcher().onBackPressed();
            });
        } catch (Exception e) {
            android.util.Log.e("EditProfileActivity", "Erreur lors de la configuration de la toolbar: " + e.getMessage(), e);
        }
    }

    private void loadUserData() {
        try {
            String fullName = sessionManager.getFullName();
            String email = sessionManager.getEmail();
            String userId = sessionManager.getUserId();
            String phone = sessionManager.getPhone();
            String university = sessionManager.getUniversity();

            if (editTextFullName != null) {
                editTextFullName.setText(fullName != null && !fullName.isEmpty() ? fullName : "");
            }
            if (editTextEmail != null) {
                editTextEmail.setText(email != null && !email.isEmpty() ? email : "");
            }
            if (editTextStudentId != null) {
                editTextStudentId.setText(userId != null && !userId.isEmpty() ? userId : "");
                editTextStudentId.setEnabled(false);
            }
            if (editTextPhone != null) {
                editTextPhone.setText(phone != null && !phone.isEmpty() ? phone : "");
            }
            if (editTextUniversity != null) {
                editTextUniversity.setText(university != null && !university.isEmpty() ? university : "");
            }

            if (imageViewProfile != null) {
                String imagePath = sessionManager.getProfileImagePath();
                if (imagePath != null) {
                    try {
                        android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(imagePath);
                        if (bitmap != null) {
                            imageViewProfile.setImageBitmap(bitmap);
                        } else {
                            imageViewProfile.setImageResource(R.drawable.ic_profile_placeholder);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("EditProfileActivity", "Erreur lors du chargement de l'image: " + e.getMessage(), e);
                        imageViewProfile.setImageResource(R.drawable.ic_profile_placeholder);
                    }
                } else {
                    imageViewProfile.setImageResource(R.drawable.ic_profile_placeholder);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("EditProfileActivity", "Erreur lors du chargement des données utilisateur: " + e.getMessage(), e);
            Toast.makeText(this, "Erreur lors du chargement des données", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        try {
            if (buttonChangePhoto != null) {
                buttonChangePhoto.setOnClickListener(v -> {
                    try {
                        openImagePicker();
                    } catch (Exception e) {
                        android.util.Log.e("EditProfileActivity", "Erreur lors de l'ouverture du sélecteur d'image: " + e.getMessage(), e);
                        Toast.makeText(this, "Erreur lors de l'ouverture de la galerie", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (imageViewProfile != null) {
                imageViewProfile.setOnClickListener(v -> {
                    try {
                        openImagePicker();
                    } catch (Exception e) {
                        android.util.Log.e("EditProfileActivity", "Erreur lors de l'ouverture du sélecteur d'image: " + e.getMessage(), e);
                        Toast.makeText(this, "Erreur lors de l'ouverture de la galerie", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (buttonSave != null) {
                buttonSave.setOnClickListener(v -> {
                    try {
                        saveProfile();
                    } catch (Exception e) {
                        android.util.Log.e("EditProfileActivity", "Erreur lors de la sauvegarde: " + e.getMessage(), e);
                        Toast.makeText(this, "Erreur lors de la sauvegarde", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            android.util.Log.e("EditProfileActivity", "Erreur lors de la configuration des listeners: " + e.getMessage(), e);
        }
    }

    private void openImagePicker() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");

            if (imagePickerLauncher != null) {
                imagePickerLauncher.launch(Intent.createChooser(intent, "Sélectionner une image"));
            } else {
                startActivityForResult(Intent.createChooser(intent, "Sélectionner une image"), PICK_IMAGE_REQUEST);
            }

        } catch (Exception e) {
            android.util.Log.e("EditProfileActivity", "Erreur lors de l'ouverture du sélecteur d'image: " + e.getMessage(), e);
            Toast.makeText(this, "Erreur lors de l'ouverture de la galerie", Toast.LENGTH_SHORT).show();
        }
    }



    private void handleImageSelection(Intent data) {
        if (data == null || data.getData() == null) {
            return;
        }

        try {
            selectedImageUri = data.getData();
            imageSelected = true;

            if (imageViewProfile != null) {
                imageViewProfile.setImageURI(selectedImageUri);
            }

            boolean saved = sessionManager.saveProfileImage(selectedImageUri);
            if (saved) {
                Toast.makeText(this, "Image sélectionnée et sauvegardée", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Image sélectionnée mais erreur lors de la sauvegarde", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            android.util.Log.e("EditProfileActivity", "Erreur lors du traitement de l'image: " + e.getMessage(), e);
            Toast.makeText(this, "Erreur lors du chargement de l'image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            handleImageSelection(data);
        }
    }

    private void saveProfile() {
        try {
            if (editTextFullName == null || editTextEmail == null || editTextPhone == null ||
                editTextUniversity == null || editTextStudentId == null) {
                Toast.makeText(this, "Erreur: Champs manquants", Toast.LENGTH_SHORT).show();
                return;
            }

            String fullName = editTextFullName.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();
            String phone = editTextPhone.getText().toString().trim();
            String university = editTextUniversity.getText().toString().trim();
            String studentId = editTextStudentId.getText().toString().trim();

            if (fullName.isEmpty()) {
                editTextFullName.requestFocus();
                Toast.makeText(this, "Le nom complet est requis", Toast.LENGTH_SHORT).show();
                return;
            }

            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                editTextEmail.requestFocus();
                Toast.makeText(this, "Email invalide", Toast.LENGTH_SHORT).show();
                return;
            }

            sessionManager.saveUser(studentId, fullName, email, university, phone);

            if (imageSelected && selectedImageUri != null) {
                boolean imageSaved = sessionManager.saveProfileImage(selectedImageUri);
                if (!imageSaved) {
                    Toast.makeText(this, "Profil mis à jour mais erreur lors de la sauvegarde de l'image", Toast.LENGTH_LONG).show();
                    return;
                }
            }

            Toast.makeText(this, "Profil mis à jour avec succès", Toast.LENGTH_SHORT).show();

            Intent resultIntent = new Intent();
            resultIntent.putExtra("profile_updated", true);
            if (selectedImageUri != null) {
                resultIntent.putExtra("image_updated", true);
                resultIntent.setData(selectedImageUri);
            }
            setResult(RESULT_OK, resultIntent);
            finish();
        } catch (Exception e) {
            android.util.Log.e("EditProfileActivity", "Erreur lors de la sauvegarde du profil: " + e.getMessage(), e);
            Toast.makeText(this, "Erreur lors de la sauvegarde", Toast.LENGTH_SHORT).show();
        }
    }
}
