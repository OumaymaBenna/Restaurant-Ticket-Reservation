package com.example.projet_tp.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projet_tp.R;
import com.example.projet_tp.api.MealReservationAPI;
import com.example.projet_tp.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONException;
import org.json.JSONObject;

public class SubscriptionActivity extends AppCompatActivity {

    private MaterialButton buttonSubscribe;
    private TextView textViewBalance, textViewDescription;
    private ProgressBar progressBar;
    private MaterialCardView cardBalance;
    private SessionManager sessionManager;
    private MealReservationAPI api;
    private double currentBalance = 0.0;
    
    private ActivityResultLauncher<Intent> paymentLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        sessionManager = new SessionManager(this);
        api = new MealReservationAPI(this);

        initViews();
        setupToolbar();
        setupPaymentLauncher();
        setupClickListeners();
        loadBalance();
    }
    
    private void setupPaymentLauncher() {
        paymentLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        boolean paymentSuccess = result.getData().getBooleanExtra("paymentSuccess", false);
                        if (paymentSuccess) {
                            loadBalance();
                            Toast.makeText(SubscriptionActivity.this, "Paiement effectué avec succès!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        );
    }

    private void initViews() {
        buttonSubscribe = findViewById(R.id.buttonSubscribe);
        textViewBalance = findViewById(R.id.textViewBalance);
        textViewDescription = findViewById(R.id.textViewDescription);
        progressBar = findViewById(R.id.progressBar);
        cardBalance = findViewById(R.id.cardBalance);
        
        if (buttonSubscribe == null) {
            android.util.Log.e("SubscriptionActivity", "buttonSubscribe est null après findViewById!");
            Toast.makeText(this, "Erreur: Bouton non trouvé dans le layout", Toast.LENGTH_LONG).show();
        } else {
            android.util.Log.d("SubscriptionActivity", "Bouton trouvé et initialisé");
            buttonSubscribe.setEnabled(true);
        }
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            });
        }
    }

    private void loadBalance() {
        String studentId = sessionManager.getUserId();
        if (studentId == null || studentId.isEmpty()) {
            Toast.makeText(this, "Erreur: ID utilisateur manquant", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showLoading(true);
        api.getSubscriptionBalance(studentId, new MealReservationAPI.ReservationCallback() {
            @Override
            public void onSuccess(org.json.JSONObject response) {
                showLoading(false);
                try {
                    if (response.has("subscriptionBalance")) {
                        currentBalance = response.getDouble("subscriptionBalance");
                        updateBalanceDisplay();
                    }
                } catch (JSONException e) {
                    android.util.Log.e("SubscriptionActivity", "Erreur parsing balance", e);
                }
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(SubscriptionActivity.this, "Erreur: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateBalanceDisplay() {
        if (textViewBalance != null) {
            textViewBalance.setText(String.format("%.3f DNT", currentBalance));
        }
    }

    private void setupClickListeners() {
        if (buttonSubscribe == null) {
            android.util.Log.e("SubscriptionActivity", "buttonSubscribe is null!");
            Toast.makeText(this, "Erreur: Bouton non initialisé", Toast.LENGTH_SHORT).show();
            return;
        }
        
        buttonSubscribe.setOnClickListener(v -> {
            android.util.Log.d("SubscriptionActivity", "Bouton Payer cliqué");
            try {
                if (paymentLauncher == null) {
                    android.util.Log.e("SubscriptionActivity", "PaymentLauncher is null!");
                    Toast.makeText(this, "Erreur: Launcher non initialisé", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                android.util.Log.d("SubscriptionActivity", "Création de l'intent de paiement...");
                Intent paymentIntent = new Intent(this, com.example.projet_tp.ui.payment.PaymentActivity.class);
                paymentIntent.putExtra("amount", 15.0);
                paymentIntent.putExtra("paymentType", "subscription");
                paymentIntent.putExtra("description", "Abonnement mensuel - 15 DNT");
                
                android.util.Log.d("SubscriptionActivity", "Lancement de PaymentActivity...");
                paymentLauncher.launch(paymentIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                android.util.Log.d("SubscriptionActivity", "PaymentActivity lancé avec succès");
            } catch (Exception e) {
                android.util.Log.e("SubscriptionActivity", "Erreur lors de l'ouverture du paiement", e);
                e.printStackTrace();
                Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        
        android.util.Log.d("SubscriptionActivity", "Listener du bouton configuré");
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (buttonSubscribe != null) {
            buttonSubscribe.setEnabled(!show);
            buttonSubscribe.setClickable(!show);
            android.util.Log.d("SubscriptionActivity", "Bouton " + (show ? "désactivé" : "activé"));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBalance();
    }
}

