package com.example.projet_tp.ui.main;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
// RecyclerView imports retirés car non utilisés actuellement
// import androidx.recyclerview.widget.LinearLayoutManager;
// import androidx.recyclerview.widget.RecyclerView;

import com.example.projet_tp.R;
// import com.example.projet_tp.adapter.ReservationAdapter;
// import com.example.projet_tp.model.Reservation;
import com.example.projet_tp.ui.auth.LoginActivity;
import com.example.projet_tp.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;


// List imports retirés car non utilisés actuellement
// import java.util.ArrayList;
// import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private CircleImageView imageViewProfile;
    private TextView textViewName, textViewEmail, textViewStudentId, textViewUniversity, textViewPhone;
    private MaterialButton buttonEditProfile, buttonLogout;

    private SessionManager sessionManager;

    private ActivityResultLauncher<Intent> editProfileLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);

        initViews();
        setupToolbar();
        setupEditProfileLauncher();
        setupRecyclerView();
        loadUserData();
        setupClickListeners();
    }
    
    private void setupEditProfileLauncher() {
        editProfileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        boolean profileUpdated = result.getData().getBooleanExtra("profile_updated", false);
                        if (profileUpdated) {
                            loadUserData();
                            Toast.makeText(ProfileActivity.this, "Profil mis à jour", Toast.LENGTH_SHORT).show();
                            
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("profile_updated", true);
                            setResult(RESULT_OK, resultIntent);
                        }
                    }
                }
            }
        );
    }

    private void initViews() {
        try {
            toolbar = findViewById(R.id.toolbar);
            imageViewProfile = findViewById(R.id.imageViewProfile);
            textViewName = findViewById(R.id.textViewName);
            textViewEmail = findViewById(R.id.textViewEmail);
            textViewStudentId = findViewById(R.id.textViewStudentId);
            textViewUniversity = findViewById(R.id.textViewUniversity);
            textViewPhone = findViewById(R.id.textViewPhone);
            buttonEditProfile = findViewById(R.id.buttonEditProfile);
            buttonLogout = findViewById(R.id.buttonLogout);
            
            if (toolbar == null || imageViewProfile == null || textViewName == null ||
                textViewEmail == null || textViewStudentId == null || 
                textViewUniversity == null || textViewPhone == null ||
                buttonEditProfile == null || buttonLogout == null) {
                android.util.Log.e("ProfileActivity", "Un ou plusieurs éléments du layout sont manquants");
                Toast.makeText(this, "Erreur: Layout incomplet", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } catch (Exception e) {
            android.util.Log.e("ProfileActivity", "Erreur lors de l'initialisation des vues: " + e.getMessage(), e);
            Toast.makeText(this, "Erreur lors du chargement du profil", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupToolbar() {
        try {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> {
                getOnBackPressedDispatcher().onBackPressed();
            });
        } catch (Exception e) {
            android.util.Log.e("ProfileActivity", "Erreur lors de la configuration de la toolbar: " + e.getMessage(), e);
        }
    }

    private void setupRecyclerView() {

    }

    private void loadUserData() {
        try {
            String fullName = sessionManager.getFullName();
            String email = sessionManager.getEmail();
            String userId = sessionManager.getUserId();
            String university = sessionManager.getUniversity();
            String phone = sessionManager.getPhone();
            
            textViewName.setText(fullName != null && !fullName.isEmpty() ? fullName : "Non défini");
            textViewEmail.setText(email != null && !email.isEmpty() ? email : "Non défini");
            textViewStudentId.setText(userId != null && !userId.isEmpty() ? userId : "Non renseigné");
            textViewUniversity.setText(university != null && !university.isEmpty() ? university : "Non renseigné");
            textViewPhone.setText(phone != null && !phone.isEmpty() ? phone : "Non renseigné");
            
            loadProfileImage();
        } catch (Exception e) {
            android.util.Log.e("ProfileActivity", "Erreur lors du chargement des données utilisateur: " + e.getMessage(), e);
            Toast.makeText(this, "Erreur lors du chargement des données", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        try {
            if (buttonEditProfile != null) {
                buttonEditProfile.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                        editProfileLauncher.launch(intent);
                    } catch (Exception e) {
                        android.util.Log.e("ProfileActivity", "Erreur lors de l'ouverture d'EditProfileActivity: " + e.getMessage(), e);
                        Toast.makeText(this, "Erreur lors de l'ouverture de l'éditeur de profil", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            
            if (buttonLogout != null) {
                buttonLogout.setOnClickListener(v -> showLogoutDialog());
            }

        } catch (Exception e) {
            android.util.Log.e("ProfileActivity", "Erreur lors de la configuration des listeners: " + e.getMessage(), e);
        }
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Déconnexion")
                .setMessage("Êtes-vous sûr de vouloir vous déconnecter ?")
                .setPositiveButton("Déconnexion", (dialog, which) -> performLogout())
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void performLogout() {
        sessionManager.logout();
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (imageViewProfile != null) {
            loadProfileImage();
        }
    }


    private void loadProfileImage() {
        try {
            if (imageViewProfile == null) {
                android.util.Log.e("ProfileActivity", "imageViewProfile est null");
                return;
            }
            

            String imagePath = sessionManager.getProfileImagePath();
            if (imagePath != null) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                    if (bitmap != null) {
                        imageViewProfile.setImageBitmap(bitmap);
                    } else {
                        android.util.Log.w("ProfileActivity", "Impossible de décoder l'image depuis: " + imagePath);
                        imageViewProfile.setImageResource(R.drawable.ic_profile_placeholder);
                    }
                } catch (Exception e) {
                    android.util.Log.e("ProfileActivity", "Erreur lors du chargement de l'image: " + e.getMessage(), e);
                    imageViewProfile.setImageResource(R.drawable.ic_profile_placeholder);
                }
            } else {
                imageViewProfile.setImageResource(R.drawable.ic_profile_placeholder);
            }
        } catch (Exception e) {
            android.util.Log.e("ProfileActivity", "Erreur lors du chargement de l'image de profil: " + e.getMessage(), e);
            if (imageViewProfile != null) {
                imageViewProfile.setImageResource(R.drawable.ic_profile_placeholder);
            }
        }
    }
}
