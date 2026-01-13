package com.example.projet_tp.ui.payment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projet_tp.R;
import com.example.projet_tp.api.MealReservationAPI;
import com.example.projet_tp.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;

public class PaymentActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private SessionManager sessionManager;
    private double amount;
    private String paymentType;
    private String description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        sessionManager = new SessionManager(this);
        
        amount = getIntent().getDoubleExtra("amount", 15.0);
        paymentType = getIntent().getStringExtra("paymentType");
        description = getIntent().getStringExtra("description");
        
        if (paymentType == null) {
            paymentType = "subscription";
        }
        if (description == null) {
            description = paymentType.equals("subscription") ? "Abonnement mensuel" : "Réservation repas";
        }

        initViews();
        setupToolbar();
        setupWebView();
        loadPaymentPage();
    }

    private void initViews() {
        try {
            webView = findViewById(R.id.webViewPayment);
            progressBar = findViewById(R.id.progressBar);
            
            if (webView == null) {
                Log.e("PaymentActivity", "WebView not found in layout!");
                Toast.makeText(this, "Erreur: WebView non trouvé", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            if (progressBar == null) {
                Log.w("PaymentActivity", "ProgressBar not found in layout");
            }
        } catch (Exception e) {
            Log.e("PaymentActivity", "Erreur lors de l'initialisation des vues", e);
            Toast.makeText(this, "Erreur d'initialisation: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Paiement par Carte");
            }
            toolbar.setNavigationOnClickListener(v -> {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            });
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        if (webView == null) {
            Log.e("PaymentActivity", "WebView is null in setupWebView!");
            return;
        }
        
        try {
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setDomStorageEnabled(true);
            webView.getSettings().setLoadWithOverviewMode(true);
            webView.getSettings().setUseWideViewPort(true);
            webView.getSettings().setBuiltInZoomControls(false);
            webView.getSettings().setDisplayZoomControls(false);
        } catch (Exception e) {
            Log.e("PaymentActivity", "Erreur configuration WebView", e);
        }
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                Log.d("PaymentActivity", "Page started: " + url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                Log.d("PaymentActivity", "Page finished: " + url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                progressBar.setVisibility(View.GONE);
                Log.e("PaymentActivity", "WebView error: " + error.getDescription());
                Toast.makeText(PaymentActivity.this, "Erreur de chargement: " + error.getDescription(), Toast.LENGTH_LONG).show();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                Log.d("PaymentActivity", "URL loading: " + url);
                
                if (url.contains("payment_success") || url.contains("status=success") || url.contains("result=success")) {
                    handlePaymentSuccess();
                    return true;
                }
                
                if (url.contains("payment_cancel") || url.contains("status=cancel") || url.contains("result=cancel")) {
                    handlePaymentCancel();
                    return true;
                }
                

                if (url.startsWith("http://") || url.startsWith("https://")) {
                    view.loadUrl(url);
                    return true;
                }
                
                return true;
            }
        });
    }

    private void loadPaymentPage() {
        try {
            String userId = sessionManager.getUserId();
            String userEmail = sessionManager.getEmail();
            
            if (userId == null || userId.isEmpty()) {
                Toast.makeText(this, "Erreur: ID utilisateur manquant", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            String baseUrl = "http://10.0.2.2:3000";
            String emailEncoded = "";
            if (userEmail != null && !userEmail.isEmpty()) {
                try {
                    emailEncoded = java.net.URLEncoder.encode(userEmail, "UTF-8");
                } catch (java.io.UnsupportedEncodingException e) {
                    Log.e("PaymentActivity", "Erreur encodage email", e);
                    emailEncoded = userEmail.replace(" ", "%20").replace("@", "%40");
                }
            }
            
            String descriptionEncoded = "";
            if (description != null && !description.isEmpty()) {
                try {
                    descriptionEncoded = java.net.URLEncoder.encode(description, "UTF-8");
                } catch (java.io.UnsupportedEncodingException e) {
                    Log.e("PaymentActivity", "Erreur encodage description", e);
                    descriptionEncoded = description.replace(" ", "%20");
                }
            }
            
            String paymentUrl = baseUrl + "/payment-page" +
                    "?amount=" + amount +
                    "&userId=" + userId +
                    "&email=" + emailEncoded +
                    "&description=" + descriptionEncoded +
                    "&isSubscription=" + (paymentType != null && paymentType.equals("subscription") ? "true" : "false");

            Log.d("PaymentActivity", "Loading payment URL: " + paymentUrl);
            
            if (webView != null) {
                webView.loadUrl(paymentUrl);
            } else {
                Log.e("PaymentActivity", "WebView is null!");
                Toast.makeText(this, "Erreur: WebView non initialisé", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("PaymentActivity", "Erreur lors du chargement de la page de paiement", e);
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void handlePaymentSuccess() {
        Log.d("PaymentActivity", "Payment success detected");
        
        if (paymentType != null && paymentType.equals("subscription")) {
            String studentId = sessionManager.getUserId();
            if (studentId != null && !studentId.isEmpty()) {
                MealReservationAPI api = new MealReservationAPI(this);
                api.subscribe(studentId, amount, new MealReservationAPI.ReservationCallback() {
                    @Override
                    public void onSuccess(org.json.JSONObject response) {
                        Toast.makeText(PaymentActivity.this, 
                            "Paiement réussi! Votre compte a été crédité de " + amount + " DNT", 
                            Toast.LENGTH_LONG).show();
                        
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("paymentSuccess", true);
                        resultIntent.putExtra("amount", amount);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(PaymentActivity.this, 
                            "Paiement effectué mais erreur lors du crédit: " + error, 
                            Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
            }
        } else {
            Toast.makeText(this, "Paiement réussi!", Toast.LENGTH_SHORT).show();
            Intent resultIntent = new Intent();
            resultIntent.putExtra("paymentSuccess", true);
            setResult(RESULT_OK, resultIntent);
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }

    private void handlePaymentCancel() {
        Log.d("PaymentActivity", "Payment cancelled");
        Toast.makeText(this, "Paiement annulé", Toast.LENGTH_SHORT).show();
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }
}

