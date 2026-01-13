package com.example.projet_tp.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projet_tp.R;
import com.example.projet_tp.ui.auth.LoginActivity;
import com.example.projet_tp.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class AdminActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn() || !sessionManager.isAdmin()) {
            Toast.makeText(this, "Accès non autorisé", Toast.LENGTH_SHORT).show();
            redirectToLogin();
            return;
        }

        setupToolbar();
        setupLogoutButton();
        setupClickListeners();
    }

    private void setupLogoutButton() {
        MaterialButton buttonLogout = findViewById(R.id.buttonLogout);
        if (buttonLogout != null) {
            buttonLogout.setOnClickListener(v -> {
                sessionManager.logout();
                Toast.makeText(this, "Déconnexion réussie", Toast.LENGTH_SHORT).show();
                redirectToLogin();
            });
        }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupClickListeners() {
        // Carte Gérer les Menus
        com.google.android.material.card.MaterialCardView cardManageMenus = findViewById(R.id.cardManageMenus);
        if (cardManageMenus != null) {
            cardManageMenus.setOnClickListener(v -> {
                Intent intent = new Intent(this, ManageMenusActivity.class);
                startActivity(intent);
            });
        }

        // Carte Voir les Commentaires
        com.google.android.material.card.MaterialCardView cardViewComments = findViewById(R.id.cardViewComments);
        if (cardViewComments != null) {
            cardViewComments.setOnClickListener(v -> {
                Intent intent = new Intent(this, CommentsActivity.class);
                startActivity(intent);
            });
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

