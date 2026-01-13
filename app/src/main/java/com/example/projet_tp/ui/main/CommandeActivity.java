package com.example.projet_tp.ui.main;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projet_tp.R;
import com.example.projet_tp.api.RetrofitClient;
import com.example.projet_tp.model.ApiResponse;
import com.example.projet_tp.model.OrderComment;
import com.example.projet_tp.network.ApiService;
import com.example.projet_tp.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommandeActivity extends AppCompatActivity {

    private Spinner spinnerMealType;
    private TextInputEditText editTextComment;
    private MaterialButton buttonSubmit;
    private ProgressBar progressBar;
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commande);

        apiService = RetrofitClient.getApiService();
        sessionManager = new SessionManager(this);

        setupToolbar();
        initViews();
        setupSpinner();
        setupButton();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Nouvelle Commande");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initViews() {
        spinnerMealType = findViewById(R.id.spinnerMealType);
        editTextComment = findViewById(R.id.editTextComment);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupSpinner() {
        String[] mealTypes = {"Déjeuner", "Dîner", "Repas froid"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, mealTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMealType.setAdapter(adapter);
    }

    private void setupButton() {
        buttonSubmit.setOnClickListener(v -> submitOrder());
    }

    private void submitOrder() {
        String mealType = spinnerMealType.getSelectedItem().toString();
        String comment = editTextComment.getText().toString().trim();

        if (comment.isEmpty()) {
            Toast.makeText(this, "Veuillez saisir un commentaire", Toast.LENGTH_SHORT).show();
            return;
        }

        String studentId = sessionManager.getUserId();
        String userName = sessionManager.getFullName();

        if (studentId == null || studentId.isEmpty()) {
            Toast.makeText(this, "Erreur: Session expirée. Veuillez vous reconnecter.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Désactiver le bouton pendant l'envoi
        buttonSubmit.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        // Créer la commande avec commentaire
        OrderComment orderComment = new OrderComment(studentId, userName, mealType, comment);
        apiService.createOrderWithComment(orderComment)
            .enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    progressBar.setVisibility(View.GONE);
                    buttonSubmit.setEnabled(true);

                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse apiResponse = response.body();
                        if (apiResponse.isSuccess()) {
                            Toast.makeText(CommandeActivity.this, 
                                "Commande créée avec succès", Toast.LENGTH_SHORT).show();
                            // Vider le champ commentaire
                            editTextComment.setText("");
                            // Retourner à l'écran précédent
                            finish();
                        } else {
                            String errorMsg = apiResponse.getMessage() != null ? 
                                apiResponse.getMessage() : "Erreur lors de la création de la commande";
                            Toast.makeText(CommandeActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(CommandeActivity.this, 
                            "Erreur serveur (Code " + response.code() + ")", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    buttonSubmit.setEnabled(true);
                    android.util.Log.e("CommandeActivity", "Erreur de connexion", t);
                    Toast.makeText(CommandeActivity.this, 
                        "Erreur de connexion: " + (t != null ? t.getMessage() : "Erreur inconnue"), 
                        Toast.LENGTH_LONG).show();
                }
            });
    }
}

