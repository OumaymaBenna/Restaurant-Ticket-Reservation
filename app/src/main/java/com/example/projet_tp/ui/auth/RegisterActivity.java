package com.example.projet_tp.ui.auth;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;
import com.example.projet_tp.R;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projet_tp.network.ApiService;
import com.example.projet_tp.api.RetrofitClient;
import com.example.projet_tp.model.User;
import com.example.projet_tp.utils.ValidationUtils;
import com.example.projet_tp.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText editFullName, editEmail, editStudentId, editPassword, editConfirmPassword, editTextRole, editTextAdminCode;
    private TextInputLayout studentIdLayout, adminCodeLayout;
    private MaterialButton buttonRegister;
    private ApiService apiService;
    private RadioGroup radioGroupRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        apiService = RetrofitClient.getApiService();

        editFullName = findViewById(R.id.editTextFullName);
        editEmail = findViewById(R.id.editTextEmail);
        editStudentId = findViewById(R.id.editTextStudentId);
        editPassword = findViewById(R.id.editTextPassword);
        editConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextRole = findViewById(R.id.editTextRole);
        editTextAdminCode = findViewById(R.id.editTextAdminCode);
        studentIdLayout = findViewById(R.id.studentIdLayout);
        adminCodeLayout = findViewById(R.id.adminCodeLayout);
        radioGroupRole = findViewById(R.id.radioGroupRole);
        buttonRegister = findViewById(R.id.buttonRegister);

        radioGroupRole.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioEtudiant) {
                editTextRole.setText("Étudiant");
                if (studentIdLayout != null) {
                    studentIdLayout.setVisibility(View.VISIBLE);
                }
                if (adminCodeLayout != null) {
                    adminCodeLayout.setVisibility(View.GONE);
                }
                if (editTextAdminCode != null) {
                    editTextAdminCode.setText("");
                }
            } else if (checkedId == R.id.radioAdmin) {
                editTextRole.setText("Administrateur");
                if (studentIdLayout != null) {
                    studentIdLayout.setVisibility(View.GONE);
                }
                if (adminCodeLayout != null) {
                    adminCodeLayout.setVisibility(View.VISIBLE);
                }
                if (editStudentId != null) {
                    editStudentId.setText("");
                }
            }
        });

        editTextRole.setOnClickListener(v -> {
            if (radioGroupRole.getCheckedRadioButtonId() == R.id.radioEtudiant) {
                radioGroupRole.check(R.id.radioAdmin);
            } else {
                radioGroupRole.check(R.id.radioEtudiant);
            }
        });

        buttonRegister.setOnClickListener(v -> registerUser());
        
        startAnimations();
    }

    private void startAnimations() {
        View logoCard = findViewById(R.id.logoCard);
        View registerCard = findViewById(R.id.registerCard);

        if (logoCard != null) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(logoCard, "scaleX", 0f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(logoCard, "scaleY", 0f, 1f);
            ObjectAnimator rotation = ObjectAnimator.ofFloat(logoCard, "rotation", -180f, 0f);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(logoCard, "alpha", 0f, 1f);

            AnimatorSet logoSet = new AnimatorSet();
            logoSet.playTogether(scaleX, scaleY, rotation, alpha);
            logoSet.setDuration(800);
            logoSet.setInterpolator(new OvershootInterpolator());
            logoSet.start();
        }

        if (registerCard != null) {
            registerCard.setAlpha(0f);
            registerCard.setTranslationY(100f);
            registerCard.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(700)
                    .setStartDelay(300)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    }

    private void registerUser() {
        String fullName = editFullName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String studentId = editStudentId.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();
        String adminCode = editTextAdminCode != null ? editTextAdminCode.getText().toString().trim() : "";

        String role = "etudiant";
        int selectedId = radioGroupRole.getCheckedRadioButtonId();
        boolean isAdmin = (selectedId == R.id.radioAdmin);
        if (isAdmin) {
            role = "admin";
        }

        if (!ValidationUtils.isValidFullName(fullName)) {
            editFullName.setError("Nom complet requis (min 3 caractères)");
            editFullName.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            editEmail.setError("Email invalide");
            editEmail.requestFocus();
            return;
        }

        if (isAdmin) {
            if (adminCode == null || adminCode.isEmpty()) {
                if (editTextAdminCode != null) {
                    editTextAdminCode.setError("Code administrateur requis");
                    editTextAdminCode.requestFocus();
                }
                return;
            }

            studentId = "ADMIN_" + email.replace("@", "_").replace(".", "_");
        } else {
            if (!ValidationUtils.isValidStudentId(studentId)) {
                editStudentId.setError("ID étudiant invalide (min 5 caractères)");
                editStudentId.requestFocus();
                return;
            }
        }

        if (!ValidationUtils.isValidPassword(password)) {
            editPassword.setError("Mot de passe trop court (min 6 caractères)");
            editPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            editConfirmPassword.setError("Les mots de passe ne correspondent pas");
            editConfirmPassword.requestFocus();
            return;
        }

        buttonRegister.setEnabled(false);
        buttonRegister.setText("Inscription...");

        User newUser = new User(fullName, email, studentId, password, role);
        if (isAdmin && adminCode != null && !adminCode.isEmpty()) {
            newUser.setAdminCode(adminCode);
        }

        apiService.registerUser(newUser).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                buttonRegister.setEnabled(true);
                buttonRegister.setText("Créer un compte");

                if (response.isSuccessful() && response.body() != null) {
                    User registeredUser = response.body();
                    Toast.makeText(RegisterActivity.this,
                            "✅ Compte créé avec succès",
                            Toast.LENGTH_SHORT).show();

                    if (isAdmin && registeredUser.getRole() != null && registeredUser.getRole().equals("admin")) {
                        SessionManager sessionManager = new SessionManager(RegisterActivity.this);
                        String tempToken = "temp_" + System.currentTimeMillis();
                        sessionManager.saveUserSession(registeredUser, tempToken);
                        
                        Intent intent = new Intent(RegisterActivity.this, com.example.projet_tp.ui.admin.AdminHomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
                    }

                } else {
                    String errorMessage = "Erreur serveur";
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                    }

                    if (response.code() == 403) {
                        errorMessage = "Code administrateur invalide. Accès refusé.";
                    } else if (response.code() == 409) {
                        errorMessage = "Email ou ID étudiant déjà utilisé";
                    } else if (response.code() == 400) {
                        errorMessage = "Tous les champs sont requis";
                    } else if (response.code() == 404) {
                        errorMessage = "Route non trouvée (404).\n\nVérifications:\n" +
                                "1. Le serveur est démarré sur http://localhost:3000\n" +
                                "2. MongoDB est démarré\n" +
                                "3. URL serveur: http://10.0.2.2:3000";
                        if (!errorBody.isEmpty()) {
                            errorMessage += "\n\nDétails: " + errorBody;
                        }
                    } else if (response.code() == 500) {
                        errorMessage = "Erreur serveur interne";
                        if (!errorBody.isEmpty()) {
                            errorMessage += "\n\n" + errorBody;
                        }
                    } else {
                        errorMessage = "Erreur serveur : " + response.code();
                        if (!errorBody.isEmpty()) {
                            errorMessage += "\n\n" + errorBody;
                        }
                    }
                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    android.util.Log.e("RegisterActivity", "Erreur HTTP " + response.code() + ": " + errorBody);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                buttonRegister.setEnabled(true);
                buttonRegister.setText("Créer un compte");

                String errorMessage = "Erreur de connexion";
                if (t != null && t.getMessage() != null) {
                    String msg = t.getMessage();
                    if (msg.contains("Unable to resolve host") || 
                        msg.contains("Failed to resolve host")) {
                        errorMessage = "Impossible de résoudre l'adresse du serveur.\n\n" +
                                "Vérifiez:\n" +
                                "1. Le serveur est démarré\n" +
                                "2. Vous utilisez l'émulateur Android (10.0.2.2)\n" +
                                "3. URL: http://10.0.2.2:3000";
                    } else if (msg.contains("Failed to connect") ||
                               msg.contains("Connection refused") ||
                               msg.contains("ECONNREFUSED")) {
                        errorMessage = "Connexion refusée.\n\n" +
                                "Le serveur n'est probablement pas démarré.\n\n" +
                                "Démarrez le serveur:\n" +
                                "cd server\n" +
                                "node server.js";
                    } else if (msg.contains("timeout") || msg.contains("Timeout")) {
                        errorMessage = "Timeout de connexion.\n\n" +
                                "Le serveur met trop de temps à répondre.";
                    } else {
                        errorMessage = "Erreur de connexion:\n" + msg;
                    }
                    android.util.Log.e("RegisterActivity", "Erreur réseau: " + msg, t);
                }
                Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}