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
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

public class StatisticsActivity extends AppCompatActivity {

    private TextView textViewDejeunerCount, textViewDinerCount, textViewRepasFroidCount, textViewTotal;
    private TextView textViewTotalUsers, textViewBlockedUsers, textViewActiveUsers;
    private TextView textViewUsersWithSubscription, textViewRevenueDay, textViewRevenueWeek, textViewRevenueMonth;
    private MaterialCardView cardDejeuner, cardDiner, cardRepasFroid, cardTotal;
    private MaterialCardView cardTotalUsers, cardBlockedUsers, cardActiveUsers;
    private MaterialCardView cardUsersWithSubscription, cardRevenueDay, cardRevenueWeek, cardRevenueMonth;
    private MaterialButton buttonPeriodDay, buttonPeriodWeek, buttonPeriodMonth;
    private ProgressBar progressBar;
    private MealReservationAPI api;
    private String currentPeriod = "day";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        api = new MealReservationAPI(this);

        initViews();
        setupToolbar();
        setupPeriodButtons();
        loadStatistics();
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
        
        textViewTotalUsers = findViewById(R.id.textViewTotalUsers);
        textViewBlockedUsers = findViewById(R.id.textViewBlockedUsers);
        textViewActiveUsers = findViewById(R.id.textViewActiveUsers);
        textViewUsersWithSubscription = findViewById(R.id.textViewUsersWithSubscription);
        cardTotalUsers = findViewById(R.id.cardTotalUsers);
        cardBlockedUsers = findViewById(R.id.cardBlockedUsers);
        cardActiveUsers = findViewById(R.id.cardActiveUsers);
        cardUsersWithSubscription = findViewById(R.id.cardUsersWithSubscription);
        
        textViewRevenueDay = findViewById(R.id.textViewRevenueDay);
        textViewRevenueWeek = findViewById(R.id.textViewRevenueWeek);
        textViewRevenueMonth = findViewById(R.id.textViewRevenueMonth);
        cardRevenueDay = findViewById(R.id.cardRevenueDay);
        cardRevenueWeek = findViewById(R.id.cardRevenueWeek);
        cardRevenueMonth = findViewById(R.id.cardRevenueMonth);
        
        buttonPeriodDay = findViewById(R.id.buttonPeriodDay);
        buttonPeriodWeek = findViewById(R.id.buttonPeriodWeek);
        buttonPeriodMonth = findViewById(R.id.buttonPeriodMonth);
        
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupPeriodButtons() {
        if (buttonPeriodDay != null) {
            buttonPeriodDay.setOnClickListener(v -> {
                currentPeriod = "day";
                updatePeriodButtons();
                loadStatistics();
            });
        }
        if (buttonPeriodWeek != null) {
            buttonPeriodWeek.setOnClickListener(v -> {
                currentPeriod = "week";
                updatePeriodButtons();
                loadStatistics();
            });
        }
        if (buttonPeriodMonth != null) {
            buttonPeriodMonth.setOnClickListener(v -> {
                currentPeriod = "month";
                updatePeriodButtons();
                loadStatistics();
            });
        }
        updatePeriodButtons();
    }

    private void updatePeriodButtons() {
        if (buttonPeriodDay != null) {
            if (currentPeriod.equals("day")) {
                buttonPeriodDay.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFF6B35));
                buttonPeriodDay.setTextColor(0xFFFFFFFF);
            } else {
                buttonPeriodDay.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF333333));
                buttonPeriodDay.setTextColor(0xFFB0B0B0);
            }
        }
        if (buttonPeriodWeek != null) {
            if (currentPeriod.equals("week")) {
                buttonPeriodWeek.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFF6B35));
                buttonPeriodWeek.setTextColor(0xFFFFFFFF);
            } else {
                buttonPeriodWeek.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF333333));
                buttonPeriodWeek.setTextColor(0xFFB0B0B0);
            }
        }
        if (buttonPeriodMonth != null) {
            if (currentPeriod.equals("month")) {
                buttonPeriodMonth.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFF6B35));
                buttonPeriodMonth.setTextColor(0xFFFFFFFF);
            } else {
                buttonPeriodMonth.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF333333));
                buttonPeriodMonth.setTextColor(0xFFB0B0B0);
            }
        }
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Statistiques");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            });
        }
    }

    private void loadStatistics() {
        showLoading(true);
        
        api.getReservationsStatsByPeriod(currentPeriod, new MealReservationAPI.ReservationCallback() {
            @Override
            public void onSuccess(org.json.JSONObject response) {
                try {
                    if (response.has("stats")) {
                        JSONObject stats = response.getJSONObject("stats");
                        
                        int dejeuner = stats.optInt("dejeuner", 0);
                        int diner = stats.optInt("diner", 0);
                        int repasFroid = stats.optInt("repasFroid", 0);
                        int total = stats.optInt("total", 0);
                        double revenue = stats.optDouble("revenue", 0.0);
                        
                        updateReservationStats(dejeuner, diner, repasFroid, total, revenue);
                    }
                } catch (JSONException e) {
                    android.util.Log.e("StatisticsActivity", "Erreur parsing stats", e);
                }
                
                loadUserStatistics();
                
                loadAllRevenues();
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("StatisticsActivity", "Erreur: " + error);
                loadUserStatistics();
                loadAllRevenues();
            }
        });
    }

    private void loadAllRevenues() {
        loadRevenueForPeriod("day", textViewRevenueDay);
        loadRevenueForPeriod("week", textViewRevenueWeek);
        loadRevenueForPeriod("month", textViewRevenueMonth);
    }

    private void loadRevenueForPeriod(String period, TextView textView) {
        api.getReservationsStatsByPeriod(period, new MealReservationAPI.ReservationCallback() {
            @Override
            public void onSuccess(org.json.JSONObject response) {
                try {
                    if (response.has("stats")) {
                        JSONObject stats = response.getJSONObject("stats");
                        double revenue = stats.optDouble("revenue", 0.0);
                        
                        if (textView != null) {
                            textView.setText(String.format("%.3f DNT", revenue));
                        }
                    }
                } catch (JSONException e) {
                    android.util.Log.e("StatisticsActivity", "Erreur parsing revenue", e);
                }
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("StatisticsActivity", "Erreur revenue " + period + ": " + error);
            }
        });
    }

    private void loadUserStatistics() {
        api.getUsersStats(new MealReservationAPI.ReservationCallback() {
            @Override
            public void onSuccess(org.json.JSONObject response) {
                try {
                    if (response.has("stats")) {
                        JSONObject stats = response.getJSONObject("stats");
                        
                        int totalUsers = stats.optInt("totalStudents", 0);
                        int usersWithSubscription = stats.optInt("usersWithSubscription", 0);
                        
                        loadUserListForBlockedStats(totalUsers, usersWithSubscription);
                    } else {
                        showLoading(false);
                    }
                } catch (JSONException e) {
                    android.util.Log.e("StatisticsActivity", "Erreur parsing user stats", e);
                    showLoading(false);
                }
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("StatisticsActivity", "Erreur user stats: " + error);
                showLoading(false);
            }
        });
    }

    private void loadUserListForBlockedStats(int totalUsers, int usersWithSubscription) {
        api.getAllUsers(new MealReservationAPI.ReservationCallback() {
            @Override
            public void onSuccess(org.json.JSONObject response) {
                showLoading(false);
                try {
                    if (response.has("users")) {
                        org.json.JSONArray usersArray = response.getJSONArray("users");
                        
                        int blockedUsers = 0;
                        int activeUsers = 0;
                        
                        for (int i = 0; i < usersArray.length(); i++) {
                            JSONObject user = usersArray.getJSONObject(i);
                            boolean isBlocked = user.optBoolean("isBlocked", false);
                            
                            if (isBlocked) {
                                String blockedUntilStr = user.optString("blockedUntil", null);
                                if (blockedUntilStr != null && !blockedUntilStr.isEmpty() && !blockedUntilStr.equals("null")) {
                                    try {
                                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault());
                                        java.util.Date blockedUntil = sdf.parse(blockedUntilStr);
                                        if (blockedUntil != null && blockedUntil.after(new java.util.Date())) {
                                            blockedUsers++;
                                        } else {
                                            activeUsers++;
                                        }
                                    } catch (Exception e) {
                                        activeUsers++;
                                    }
                                } else {
                                    blockedUsers++;
                                }
                            } else {
                                activeUsers++;
                            }
                        }
                        
                        updateUserStats(totalUsers, blockedUsers, activeUsers, usersWithSubscription);
                    }
                } catch (JSONException e) {
                    android.util.Log.e("StatisticsActivity", "Erreur parsing users", e);
                    showLoading(false);
                }
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                android.util.Log.e("StatisticsActivity", "Erreur: " + error);
            }
        });
    }

    private void updateReservationStats(int dejeuner, int diner, int repasFroid, int total, double revenue) {
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

    private void updateUserStats(int totalUsers, int blockedUsers, int activeUsers, int usersWithSubscription) {
        if (textViewTotalUsers != null) {
            textViewTotalUsers.setText(String.valueOf(totalUsers));
        }
        if (textViewBlockedUsers != null) {
            textViewBlockedUsers.setText(String.valueOf(blockedUsers));
        }
        if (textViewActiveUsers != null) {
            textViewActiveUsers.setText(String.valueOf(activeUsers));
        }
        if (textViewUsersWithSubscription != null) {
            textViewUsersWithSubscription.setText(String.valueOf(usersWithSubscription));
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
        loadStatistics();
    }
}

