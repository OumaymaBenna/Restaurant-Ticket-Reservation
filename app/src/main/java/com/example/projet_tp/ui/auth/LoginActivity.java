package com.example.projet_tp.ui.auth;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projet_tp.R;
import com.example.projet_tp.model.User;
import com.example.projet_tp.model.UserResponse;
import com.example.projet_tp.network.ApiService;
import com.example.projet_tp.network.RetrofitClient;
import com.example.projet_tp.ui.main.HomeActivity;
import com.example.projet_tp.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText editEmail, editPassword;
    private MaterialButton buttonLogin;
    private ProgressBar progressBar;
    private TextView textViewRegister;

    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        apiService = RetrofitClient.getRetrofit().create(ApiService.class);
        sessionManager = new SessionManager(this);

        initViews();
        setupListeners();
        startAnimations();
    }

    private void initViews() {
        try {
            editEmail = findViewById(R.id.editEmail);
            editPassword = findViewById(R.id.editPassword);
            buttonLogin = findViewById(R.id.buttonLogin);
            progressBar = findViewById(R.id.progressBar);
            textViewRegister = findViewById(R.id.textViewRegister);

            if (editEmail == null || editPassword == null || buttonLogin == null || textViewRegister == null) {
                Log.e(TAG, "Une ou plusieurs vues essentielles sont null");
                Toast.makeText(this, "Erreur d'initialisation de l'interface", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'initialisation des vues: " + e.getMessage(), e);
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupListeners() {
        if (buttonLogin != null) {
            buttonLogin.setOnClickListener(v -> validateInputs());
        } else {
            Log.e(TAG, "buttonLogin is null, impossible de définir le listener");
        }

        if (textViewRegister != null) {
            textViewRegister.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            });
        } else {
            Log.e(TAG, "textViewRegister est null, vérifiez votre layout");
        }
    }

    private void validateInputs() {
        if (editEmail == null || editPassword == null) {
            Toast.makeText(this, "Erreur d'initialisation", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editEmail.setError("Email requis");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editPassword.setError("Mot de passe requis");
            return;
        }

        performLogin(email, password);
    }

    private void performLogin(String email, String password) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (buttonLogin != null) buttonLogin.setEnabled(false);

        User user = new User();
        user.setEmail(email);
        user.setPassword(password);

        apiService.login(user).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (buttonLogin != null) buttonLogin.setEnabled(true);

                if (!response.isSuccessful()) {
                    String errorMessage = "Erreur de connexion";
                    
                    if (response.code() == 403) {
                        try {
                            if (response.errorBody() != null) {
                                String errorBody = response.errorBody().string();
                                Log.d(TAG, "Réponse erreur 403: " + errorBody);
                                
                                if (errorBody.contains("message")) {
                                    int messageStart = errorBody.indexOf("\"message\":\"") + 11;
                                    int messageEnd = errorBody.indexOf("\"", messageStart);
                                    if (messageStart > 10 && messageEnd > messageStart) {
                                        errorMessage = errorBody.substring(messageStart, messageEnd);
                                    } else {
                                        errorMessage = "Votre compte est bloqué. Veuillez contacter l'administrateur.";
                                    }
                                } else {
                                    errorMessage = "Votre compte est bloqué. Veuillez contacter l'administrateur.";
                                }
                            } else {
                                errorMessage = "Votre compte est bloqué. Veuillez contacter l'administrateur.";
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Erreur parsing message de blocage", e);
                            errorMessage = "Votre compte est bloqué. Veuillez contacter l'administrateur.";
                        }
                    } else if (response.code() == 401) {
                        errorMessage = "Email ou mot de passe incorrect";
                    } else if (response.code() == 404) {
                        errorMessage = "Compte non trouvé";
                    } else if (response.code() == 500) {
                        errorMessage = "Erreur serveur";
                    }
                    
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Erreur HTTP: " + response.code() + " - " + errorMessage);
                    return;
                }

                UserResponse userResponse = response.body();
                if (userResponse == null || userResponse.getUser() == null) {
                    Toast.makeText(LoginActivity.this, "Réponse serveur invalide", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "userResponse ou user est null");
                    return;
                }

                String token = userResponse.getToken() != null ? userResponse.getToken() : "no-token";
                
                User user = userResponse.getUser();
                
                if (user != null) {
                    Log.d(TAG, "=== DÉBUT LOGIN ===");
                    Log.d(TAG, "User object: " + user.toString());
                    Log.d(TAG, "User.getRole(): " + user.getRole());
                    Log.d(TAG, "User.getRole() == null: " + (user.getRole() == null));
                    if (user.getRole() != null) {
                        Log.d(TAG, "User.getRole().equals(\"admin\"): " + user.getRole().equals("admin"));
                        Log.d(TAG, "User.getRole().equalsIgnoreCase(\"admin\"): " + user.getRole().equalsIgnoreCase("admin"));
                    }
                }
                
                sessionManager.saveUserSession(user, token);

                if (sessionManager.isLoggedIn()) {
                    String studentId = user != null ? user.getStudentId() : null;
                    String roleFromResponse = user != null ? user.getRole() : null;
                    String roleFromSession = sessionManager.getRole();
                    
                    Log.d(TAG, "=== INFORMATIONS UTILISATEUR ===");
                    Log.d(TAG, "StudentId: " + studentId);
                    Log.d(TAG, "Rôle (réponse): " + roleFromResponse);
                    Log.d(TAG, "Rôle (session): " + roleFromSession);
                    
                    boolean isAdmin = false;
                    
                    if (studentId != null) {
                        if (studentId.startsWith("ADMIN_") || studentId.toUpperCase().contains("ADMIN")) {
                            isAdmin = true;
                            Log.d(TAG, "✅ Admin détecté via studentId: " + studentId);
                        }
                    }
                    
                    if (!isAdmin && roleFromResponse != null) {
                        if (roleFromResponse.equals("admin") || roleFromResponse.equalsIgnoreCase("admin")) {
                            isAdmin = true;
                            Log.d(TAG, "✅ Admin détecté via rôle dans la réponse: " + roleFromResponse);
                        }
                    }
                    
                    if (!isAdmin && roleFromSession != null) {
                        if (roleFromSession.equals("admin") || roleFromSession.equalsIgnoreCase("admin")) {
                            isAdmin = true;
                            Log.d(TAG, "✅ Admin détecté via rôle dans la session: " + roleFromSession);
                        }
                    }
                    
                    if (!isAdmin && sessionManager.isAdmin()) {
                        isAdmin = true;
                        Log.d(TAG, "✅ Admin détecté via sessionManager.isAdmin()");
                    }
                    
                    Log.d(TAG, "Résultat final - isAdmin: " + isAdmin);
                    
                    String debugInfo = "StudentId: " + (studentId != null ? studentId : "null") +
                                     "\nRôle réponse: " + (roleFromResponse != null ? roleFromResponse : "null") +
                                     "\nRôle session: " + (roleFromSession != null ? roleFromSession : "null") +
                                     "\nisAdmin: " + isAdmin;
                    Log.d(TAG, "DEBUG INFO: " + debugInfo);
                    
                    if (isAdmin) {
                        Toast.makeText(LoginActivity.this, "Connexion admin réussie - Redirection vers AdminHome", Toast.LENGTH_LONG).show();
                        Log.d(TAG, "🚀 REDIRECTION VERS AdminHomeActivity");
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            goToAdminHome();
                        }, 500);
                    } else {
                        Toast.makeText(LoginActivity.this, "Connexion réussie - Redirection vers Home", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "🚀 REDIRECTION VERS HomeActivity");
                        goToHome();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Erreur lors de la sauvegarde de la session", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "❌ Session non sauvegardée");
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (buttonLogin != null) buttonLogin.setEnabled(true);

                String errorMessage = "Erreur de connexion";
                if (t != null && t.getMessage() != null) {
                    if (t.getMessage().contains("Unable to resolve host") || t.getMessage().contains("Failed to connect")) {
                        errorMessage = "Problème de connexion internet";
                    } else {
                        errorMessage = "Erreur : " + t.getMessage();
                    }
                }
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Erreur réseau: " + (t != null ? t.getMessage() : "null"), t);
            }
        });
    }

    private void startAnimations() {
        View logoCard = findViewById(R.id.logoCard);
        View titleText = findViewById(R.id.titleText);
        View subtitleText = findViewById(R.id.subtitleText);
        View loginCard = findViewById(R.id.loginCard);

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

        if (titleText != null) {
            titleText.setAlpha(0f);
            titleText.setTranslationY(50f);
            titleText.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(600)
                    .setStartDelay(300)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }

        if (subtitleText != null) {
            // Animation du sous-titre
            subtitleText.setAlpha(0f);
            subtitleText.setTranslationY(30f);
            subtitleText.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(600)
                    .setStartDelay(500)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }

        if (loginCard != null) {
            loginCard.setAlpha(0f);
            loginCard.setTranslationY(100f);
            loginCard.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(700)
                    .setStartDelay(700)
                    .setInterpolator(new OvershootInterpolator())
                    .start();
        }
    }

    private void goToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void goToAdminHome() {
        Intent intent = new Intent(LoginActivity.this, com.example.projet_tp.ui.admin.AdminHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
