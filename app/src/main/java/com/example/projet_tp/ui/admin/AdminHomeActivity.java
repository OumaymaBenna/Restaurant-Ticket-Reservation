package com.example.projet_tp.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projet_tp.R;
import com.example.projet_tp.ui.auth.LoginActivity;
import com.example.projet_tp.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class AdminHomeActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private TextView textViewAdminName;
    private MaterialCardView cardManageMenus, cardViewReservations, cardManageUsers, cardStatistics, cardOrderComments;
    private MaterialButton buttonLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Veuillez vous connecter", Toast.LENGTH_SHORT).show();
            redirectToLogin();
            return;
        }

        String role = sessionManager.getRole();
        String studentId = sessionManager.getUserId();
        
        boolean isAdmin = false;
        
        if (studentId != null && (studentId.startsWith("ADMIN_") || studentId.toUpperCase().contains("ADMIN"))) {
            isAdmin = true;
            android.util.Log.d("AdminHomeActivity", "Admin détecté via studentId: " + studentId);
        }
        
        if (!isAdmin && role != null && (role.equals("admin") || role.equalsIgnoreCase("admin"))) {
            isAdmin = true;
            android.util.Log.d("AdminHomeActivity", "Admin détecté via rôle: " + role);
        }
        
        if (!isAdmin && sessionManager.isAdmin()) {
            isAdmin = true;
            android.util.Log.d("AdminHomeActivity", "Admin détecté via sessionManager.isAdmin()");
        }
        
        if (!isAdmin) {
            android.util.Log.e("AdminHomeActivity", "Accès refusé - Rôle: " + role + ", StudentId: " + studentId);
            Toast.makeText(this, "Accès non autorisé. Réservé aux administrateurs.", Toast.LENGTH_LONG).show();
            redirectToLogin();
            return;
        }
        
        android.util.Log.d("AdminHomeActivity", "✅ Accès admin autorisé");

        initViews();
        setupToolbar();
        setupUserInfo();
        setupClickListeners();
    }

    private void initViews() {
        textViewAdminName = findViewById(R.id.textViewAdminName);
        cardManageMenus = findViewById(R.id.cardManageMenus);
        cardViewReservations = findViewById(R.id.cardViewReservations);
        cardManageUsers = findViewById(R.id.cardManageUsers);
        cardStatistics = findViewById(R.id.cardStatistics);
        cardOrderComments = findViewById(R.id.cardOrderComments);
        buttonLogout = findViewById(R.id.buttonLogout);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Panneau Administrateur");
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        }
    }

    private void setupUserInfo() {
        if (textViewAdminName != null) {
            String adminName = sessionManager.getFullName();
            if (adminName != null && !adminName.isEmpty()) {
                textViewAdminName.setText("Bienvenue, " + adminName);
            } else {
                textViewAdminName.setText("Bienvenue, Administrateur");
            }
        }
    }

    private void setupClickListeners() {
        if (cardManageMenus != null) {
            cardManageMenus.setOnClickListener(v -> {
                Intent intent = new Intent(AdminHomeActivity.this, ManageMenusActivity.class);
                startActivity(intent);
            });
        }

        if (cardViewReservations != null) {
            cardViewReservations.setOnClickListener(v -> {
                Intent intent = new Intent(AdminHomeActivity.this, AdminReservationsStatsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }

        if (cardManageUsers != null) {
            cardManageUsers.setOnClickListener(v -> {
                Intent intent = new Intent(AdminHomeActivity.this, ManageUsersActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }

        if (cardStatistics != null) {
            cardStatistics.setOnClickListener(v -> {
                Intent intent = new Intent(AdminHomeActivity.this, StatisticsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }

        if (cardOrderComments != null) {
            cardOrderComments.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(AdminHomeActivity.this, OrderCommentsActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } catch (Exception e) {
                    android.util.Log.e("AdminHomeActivity", "Erreur lors du lancement de OrderCommentsActivity", e);
                    Toast.makeText(AdminHomeActivity.this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }

        if (buttonLogout != null) {
            buttonLogout.setOnClickListener(v -> {
                sessionManager.logout();
                Toast.makeText(this, "Déconnexion réussie", Toast.LENGTH_SHORT).show();
                redirectToLogin();
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

