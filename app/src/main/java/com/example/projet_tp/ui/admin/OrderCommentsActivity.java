package com.example.projet_tp.ui.admin;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet_tp.R;
import com.example.projet_tp.adapter.OrderCommentAdapter;
import com.example.projet_tp.api.RetrofitClient;
import com.example.projet_tp.model.OrderComment;
import com.example.projet_tp.network.ApiService;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderCommentsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewOrders;
    private ProgressBar progressBar;
    private android.view.View textViewEmpty;
    private OrderCommentAdapter orderAdapter;
    private List<OrderComment> orderList;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_order_comments);

            apiService = RetrofitClient.getApiService();
            orderList = new ArrayList<>();

            setupToolbar();
            initViews();
            setupRecyclerView();
            loadOrders();
        } catch (Exception e) {
            android.util.Log.e("OrderCommentsActivity", "Erreur lors de l'initialisation", e);
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Commandes avec Commentaires");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    private void initViews() {
        try {
            recyclerViewOrders = findViewById(R.id.recyclerViewOrders);
            progressBar = findViewById(R.id.progressBar);
            textViewEmpty = findViewById(R.id.textViewEmpty);
            
            if (recyclerViewOrders == null) {
                android.util.Log.e("OrderCommentsActivity", "recyclerViewOrders est null!");
            }
            if (progressBar == null) {
                android.util.Log.e("OrderCommentsActivity", "progressBar est null!");
            }
            if (textViewEmpty == null) {
                android.util.Log.e("OrderCommentsActivity", "textViewEmpty est null!");
            }
        } catch (Exception e) {
            android.util.Log.e("OrderCommentsActivity", "Erreur lors de l'initialisation des vues", e);
            Toast.makeText(this, "Erreur d'initialisation: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupRecyclerView() {
        try {
            if (recyclerViewOrders != null) {
                orderAdapter = new OrderCommentAdapter(orderList);
                
                // Ajouter le listener pour le clic sur le nom de l'étudiant
                orderAdapter.setOnUserNameClickListener((studentId, userName) -> {
                    try {
                        if (studentId == null || studentId.isEmpty()) {
                            Toast.makeText(OrderCommentsActivity.this, "ID étudiant invalide", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        // Ouvrir ManageUsersActivity avec le studentId pour filtrer cet utilisateur
                        android.content.Intent intent = new android.content.Intent(OrderCommentsActivity.this, ManageUsersActivity.class);
                        intent.putExtra("STUDENT_ID", studentId);
                        if (userName != null) {
                            intent.putExtra("USER_NAME", userName);
                        }
                        startActivity(intent);
                        
                        // Animation de transition (avec vérification)
                        try {
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        } catch (Exception e) {
                            android.util.Log.w("OrderCommentsActivity", "Animation non disponible, utilisation de l'animation par défaut", e);
                            // Utiliser l'animation par défaut si les animations personnalisées n'existent pas
                        }
                    } catch (Exception e) {
                        android.util.Log.e("OrderCommentsActivity", "Erreur lors de l'ouverture de ManageUsersActivity", e);
                        Toast.makeText(OrderCommentsActivity.this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
                
                recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
                recyclerViewOrders.setAdapter(orderAdapter);
            } else {
                android.util.Log.e("OrderCommentsActivity", "recyclerViewOrders est null dans setupRecyclerView!");
            }
        } catch (Exception e) {
            android.util.Log.e("OrderCommentsActivity", "Erreur lors de la configuration du RecyclerView", e);
            Toast.makeText(this, "Erreur RecyclerView: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadOrders() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (textViewEmpty != null) {
            textViewEmpty.setVisibility(View.GONE);
        }
        if (recyclerViewOrders != null) {
            recyclerViewOrders.setVisibility(View.GONE);
        }

        if (apiService == null) {
            showError("Erreur de connexion");
            return;
        }

        // Appel à l'API pour récupérer toutes les commandes avec commentaires
        try {
            if (apiService == null) {
                android.util.Log.e("OrderCommentsActivity", "apiService est null!");
                showError("Erreur: Service API non disponible");
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                return;
            }
            
            android.util.Log.d("OrderCommentsActivity", "Appel de getAllOrderComments()");
            android.util.Log.d("OrderCommentsActivity", "URL de base: http://10.0.2.2:3000/");
            
            apiService.getAllOrderComments().enqueue(new Callback<com.example.projet_tp.model.OrderCommentResponse>() {
                @Override
                public void onResponse(Call<com.example.projet_tp.model.OrderCommentResponse> call, 
                        Response<com.example.projet_tp.model.OrderCommentResponse> response) {
                    try {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }

                        if (response.isSuccessful() && response.body() != null) {
                            com.example.projet_tp.model.OrderCommentResponse orderResponse = response.body();
                            if (orderResponse != null && orderResponse.getOrders() != null) {
                                if (!orderResponse.getOrders().isEmpty()) {
                                    orderList.clear();
                                    orderList.addAll(orderResponse.getOrders());
                                    if (orderAdapter != null) {
                                        orderAdapter.updateOrders(orderList);
                                    }
                                    updateEmptyState();
                                } else {
                                    updateEmptyState();
                                }
                            } else {
                                android.util.Log.w("OrderCommentsActivity", "Réponse vide ou null");
                                updateEmptyState();
                            }
                        } else {
                            String errorMsg = "Erreur serveur (Code " + response.code() + ")";
                            android.util.Log.e("OrderCommentsActivity", "Réponse non réussie - Code: " + response.code());
                            
                            if (response.errorBody() != null) {
                                try {
                                    String errorString = response.errorBody().string();
                                    android.util.Log.e("OrderCommentsActivity", "Erreur serveur: " + errorString);
                                    
                                    // Si c'est une erreur 404, donner un message plus clair
                                    if (response.code() == 404) {
                                        errorMsg = "Route non trouvée. Vérifiez que le serveur est démarré et redémarré après les modifications.";
                                    }
                                } catch (Exception e) {
                                    android.util.Log.e("OrderCommentsActivity", "Erreur lecture errorBody", e);
                                }
                            }
                            
                            // Si c'est une erreur 404, afficher un message plus utile
                            if (response.code() == 404) {
                                android.util.Log.e("OrderCommentsActivity", "❌ Erreur 404 - La route /orders/comments n'est pas trouvée");
                                android.util.Log.e("OrderCommentsActivity", "   Vérifiez que le serveur est démarré et redémarré");
                                errorMsg = "Route non trouvée (404). Redémarrez le serveur Node.js.";
                            }
                            
                            showError(errorMsg);
                            updateEmptyState();
                        }
                    } catch (Exception e) {
                        android.util.Log.e("OrderCommentsActivity", "Erreur dans onResponse", e);
                        showError("Erreur: " + e.getMessage());
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        updateEmptyState();
                    }
                }

                @Override
                public void onFailure(Call<com.example.projet_tp.model.OrderCommentResponse> call, Throwable t) {
                    try {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        android.util.Log.e("OrderCommentsActivity", "Erreur de connexion", t);
                        if (t != null) {
                            android.util.Log.e("OrderCommentsActivity", "Message: " + t.getMessage());
                            android.util.Log.e("OrderCommentsActivity", "Cause: " + (t.getCause() != null ? t.getCause().getMessage() : "null"));
                        }
                        showError("Erreur de connexion: " + (t != null ? t.getMessage() : "Erreur inconnue"));
                        updateEmptyState();
                    } catch (Exception e) {
                        android.util.Log.e("OrderCommentsActivity", "Erreur dans onFailure", e);
                    }
                }
            });
        } catch (Exception e) {
            android.util.Log.e("OrderCommentsActivity", "Erreur lors de l'appel API", e);
            showError("Erreur: " + e.getMessage());
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            updateEmptyState();
        }
    }

    private void updateEmptyState() {
        if (orderList.isEmpty()) {
            if (textViewEmpty != null) {
                textViewEmpty.setVisibility(View.VISIBLE);
            }
            if (recyclerViewOrders != null) {
                recyclerViewOrders.setVisibility(View.GONE);
            }
        } else {
            if (textViewEmpty != null) {
                textViewEmpty.setVisibility(View.GONE);
            }
            if (recyclerViewOrders != null) {
                recyclerViewOrders.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }
}

