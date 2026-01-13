package com.example.projet_tp.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projet_tp.R;
import com.example.projet_tp.api.MealReservationAPI;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONException;
import org.json.JSONObject;

public class AdminReservationsStatsActivity extends AppCompatActivity {

    private TextView textViewDejeunerCount, textViewDinerCount, textViewRepasFroidCount, textViewTotal;
    private MaterialCardView cardDejeuner, cardDiner, cardRepasFroid, cardTotal;
    private ProgressBar progressBar;
    private MealReservationAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reservations_stats);

        api = new MealReservationAPI(this);

        initViews();
        setupToolbar();
        loadStats();
    }

    private void initViews() {
        textViewDejeunerCount = findViewById(R.id.textViewDejeunerCount);
        textViewDinerCount = findViewById(R.id.textViewDinerCount);
        textViewRepasFroidCount = findViewById(R.id.textViewRepasFroidCount);
        textViewTotal = findViewById(R.id.textViewTotal);
        cardDejeuner = findViewById(R.id.cardDejeuner);
        cardDiner = findViewById(R.id.cardDiner);
        cardRepasFroid = findViewById(R.id.cardRepasFroid);
        cardTotal = findViewById(R.id.cardTotal);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Réservations du Jour");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            });
        }
    }

    private void loadStats() {
        showLoading(true);
        
        api.getReservationsStats(new MealReservationAPI.ReservationCallback() {
            @Override
            public void onSuccess(org.json.JSONObject response) {
                showLoading(false);
                try {
                    if (response.has("stats")) {
                        JSONObject stats = response.getJSONObject("stats");
                        
                        int dejeuner = stats.optInt("dejeuner", 0);
                        int diner = stats.optInt("diner", 0);
                        int repasFroid = stats.optInt("repasFroid", 0);
                        int total = stats.optInt("total", 0);
                        
                        updateStats(dejeuner, diner, repasFroid, total);
                    } else {
                        Toast.makeText(AdminReservationsStatsActivity.this, 
                            "Format de réponse invalide", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    android.util.Log.e("AdminReservationsStats", "Erreur parsing stats", e);
                    Toast.makeText(AdminReservationsStatsActivity.this, 
                        "Erreur lors du parsing des données", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(AdminReservationsStatsActivity.this, 
                    "Erreur: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateStats(int dejeuner, int diner, int repasFroid, int total) {
        if (textViewDejeunerCount != null) {
            textViewDejeunerCount.setText(String.valueOf(dejeuner));
        }
        if (textViewDinerCount != null) {
            textViewDinerCount.setText(String.valueOf(diner));
        }
        if (textViewRepasFroidCount != null) {
            textViewRepasFroidCount.setText(String.valueOf(repasFroid));
        }
        if (textViewTotal != null) {
            textViewTotal.setText(String.valueOf(total));
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
    }
}



