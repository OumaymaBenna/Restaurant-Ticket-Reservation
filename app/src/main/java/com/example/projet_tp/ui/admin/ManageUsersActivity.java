package com.example.projet_tp.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet_tp.R;
import com.example.projet_tp.adapter.UserAdapter;
import com.example.projet_tp.api.MealReservationAPI;
import com.example.projet_tp.model.User;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ManageUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerViewUsers;
    private UserAdapter adapter;
    private List<User> userList;
    private List<User> filteredUserList;
    private ProgressBar progressBar;
    private android.widget.LinearLayout emptyStateView;
    private TextInputEditText editTextSearch;
    private MealReservationAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        api = new MealReservationAPI(this);
        userList = new ArrayList<>();
        filteredUserList = new ArrayList<>();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSearchBar();
        
        // Vérifier si un studentId a été passé en paramètre
        try {
            String studentId = null;
            String userName = null;
            
            if (getIntent() != null) {
                studentId = getIntent().getStringExtra("STUDENT_ID");
                userName = getIntent().getStringExtra("USER_NAME");
            }
            
            if (studentId != null && !studentId.isEmpty()) {
                // Si un studentId est fourni, charger les utilisateurs puis filtrer
                loadUsersAndFilter(studentId, userName);
            } else {
                loadUsers();
            }
        } catch (Exception e) {
            android.util.Log.e("ManageUsersActivity", "Erreur lors de la récupération des paramètres", e);
            loadUsers(); // Charger tous les utilisateurs en cas d'erreur
        }
    }

    private void initViews() {
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        progressBar = findViewById(R.id.progressBar);
        emptyStateView = findViewById(R.id.emptyStateView);
        editTextSearch = findViewById(R.id.editTextSearch);
    }

    private void setupSearchBar() {
        if (editTextSearch != null) {
            editTextSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterUsers(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void filterUsers(String query) {
        filteredUserList.clear();
        
        if (query == null || query.trim().isEmpty()) {
            filteredUserList.addAll(userList);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (User user : userList) {
                if (user.getFullName() != null && user.getFullName().toLowerCase().contains(lowerQuery) ||
                    user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerQuery) ||
                    user.getStudentId() != null && user.getStudentId().toLowerCase().contains(lowerQuery)) {
                    filteredUserList.add(user);
                }
            }
        }
        
        adapter.updateUserList(filteredUserList);
        updateEmptyState();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Gestion des Utilisateurs");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            });
        }
    }

    private void setupRecyclerView() {
        adapter = new UserAdapter(filteredUserList, new UserAdapter.OnUserActionListener() {
            @Override
            public void onUserClick(User user) {
                showUserDetails(user);
            }

            @Override
            public void onBlockClick(User user) {
                blockUser(user);
            }
        });
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUsers.setAdapter(adapter);
    }

    private void loadUsers() {
        loadUsersAndFilter(null, null);
    }

    private void loadUsersAndFilter(String targetStudentId, String targetUserName) {
        try {
            showLoading(true);
            hideEmptyState();

            if (api == null) {
                android.util.Log.e("ManageUsersActivity", "API est null!");
                showLoading(false);
                Toast.makeText(this, "Erreur: API non disponible", Toast.LENGTH_LONG).show();
                return;
            }

            api.getAllUsers(new MealReservationAPI.ReservationCallback() {
            @Override
            public void onSuccess(org.json.JSONObject response) {
                showLoading(false);
                try {
                    if (response.has("users")) {
                        JSONArray usersArray = response.getJSONArray("users");
                        userList.clear();

                        for (int i = 0; i < usersArray.length(); i++) {
                            JSONObject userObj = usersArray.getJSONObject(i);
                            User user = parseUser(userObj);
                            if (user != null) {
                                userList.add(user);
                            }
                        }

                        // Si un studentId cible est fourni, filtrer et pré-remplir la recherche
                        if (targetStudentId != null && !targetStudentId.isEmpty()) {
                            if (editTextSearch != null) {
                                // Pré-remplir avec le nom ou l'ID de l'étudiant
                                String searchText = targetUserName != null && !targetUserName.isEmpty() 
                                    ? targetUserName 
                                    : targetStudentId;
                                editTextSearch.setText(searchText);
                            }
                            // Filtrer pour afficher uniquement cet utilisateur
                            filterUsersByStudentId(targetStudentId);
                        } else {
                            String searchQuery = editTextSearch != null ? editTextSearch.getText().toString() : "";
                            filterUsers(searchQuery);
                        }
                        updateEmptyState();
                    } else {
                        Toast.makeText(ManageUsersActivity.this, 
                            "Format de réponse invalide", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    android.util.Log.e("ManageUsersActivity", "Erreur parsing users", e);
                    Toast.makeText(ManageUsersActivity.this, 
                        "Erreur lors du parsing des données", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(ManageUsersActivity.this, 
                    "Erreur: " + error, Toast.LENGTH_LONG).show();
                updateEmptyState();
            }
        });
        } catch (Exception e) {
            android.util.Log.e("ManageUsersActivity", "Erreur lors du chargement des utilisateurs", e);
            showLoading(false);
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
            updateEmptyState();
        }
    }

    private void filterUsersByStudentId(String studentId) {
        try {
            filteredUserList.clear();
            
            if (studentId == null || studentId.trim().isEmpty()) {
                filteredUserList.addAll(userList);
            } else {
                String lowerStudentId = studentId.toLowerCase().trim();
                for (User user : userList) {
                    if (user != null && user.getStudentId() != null && user.getStudentId().toLowerCase().equals(lowerStudentId)) {
                        filteredUserList.add(user);
                        break; // On a trouvé l'utilisateur, pas besoin de continuer
                    }
                }
            }
            
            if (adapter != null) {
                adapter.updateUserList(filteredUserList);
            }
            updateEmptyState();
            
            // Si aucun utilisateur trouvé, afficher un message
            if (filteredUserList.isEmpty() && studentId != null) {
                Toast.makeText(this, "Utilisateur non trouvé avec l'ID: " + studentId, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            android.util.Log.e("ManageUsersActivity", "Erreur lors du filtrage par studentId", e);
            Toast.makeText(this, "Erreur lors du filtrage: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private User parseUser(JSONObject userObj) {
        try {
            User user = new User();
            user.setId(userObj.optString("id", userObj.optString("_id", "")));
            user.setFullName(userObj.optString("fullName", ""));
            user.setEmail(userObj.optString("email", ""));
            user.setStudentId(userObj.optString("studentId", ""));
            user.setPhone(userObj.optString("phone", ""));
            user.setUniversity(userObj.optString("university", "ISET Tataouine"));
            user.setRole(userObj.optString("role", "etudiant"));
            user.setSubscriptionBalance(userObj.optDouble("subscriptionBalance", 0.0));
            user.setBlocked(userObj.optBoolean("isBlocked", false));
            
            String blockedUntilStr = userObj.optString("blockedUntil", null);
            if (blockedUntilStr != null && !blockedUntilStr.isEmpty() && !blockedUntilStr.equals("null")) {
                try {
                    user.setBlockedUntil(blockedUntilStr);
                } catch (Exception e) {
                    android.util.Log.w("ManageUsersActivity", "Erreur parsing blockedUntil", e);
                }
            }
            
            return user;
        } catch (Exception e) {
            android.util.Log.e("ManageUsersActivity", "Erreur parsing user", e);
            return null;
        }
    }

    private void showUserDetails(User user) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Détails de l'utilisateur");
        
        String details = "Nom: " + user.getFullName() + "\n\n" +
                "Email: " + user.getEmail() + "\n\n" +
                "ID Étudiant: " + user.getStudentId() + "\n\n" +
                "Téléphone: " + (user.getPhone().isEmpty() ? "Non renseigné" : user.getPhone()) + "\n\n" +
                "Université: " + user.getUniversity() + "\n\n" +
                "Rôle: " + (user.getRole().equals("admin") ? "Administrateur" : "Étudiant") + "\n\n" +
                "Solde: " + String.format("%.3f", user.getSubscriptionBalance()) + " DNT";
        
        builder.setMessage(details);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void blockUser(User user) {
        boolean isCurrentlyBlocked = user.isBlocked();
        String action = isCurrentlyBlocked ? "débloquer" : "bloquer";
        String message = isCurrentlyBlocked 
            ? "Voulez-vous débloquer " + user.getFullName() + " ?"
            : "Voulez-vous bloquer " + user.getFullName() + " pour un mois ?";
        
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(action.substring(0, 1).toUpperCase() + action.substring(1) + " l'utilisateur");
        builder.setMessage(message);
        builder.setPositiveButton(action.substring(0, 1).toUpperCase() + action.substring(1), (dialog, which) -> {
            api.blockUser(user.getId(), !isCurrentlyBlocked, new MealReservationAPI.ReservationCallback() {
                @Override
                public void onSuccess(org.json.JSONObject response) {
                    String message = isCurrentlyBlocked 
                        ? "Utilisateur débloqué avec succès" 
                        : "Utilisateur bloqué pour un mois";
                    Toast.makeText(ManageUsersActivity.this, message, Toast.LENGTH_SHORT).show();
                    loadUsers();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(ManageUsersActivity.this, 
                        "Erreur: " + error, Toast.LENGTH_LONG).show();
                }
            });
        });
        builder.setNegativeButton("Annuler", null);
        builder.show();
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showEmptyState() {
        if (emptyStateView != null) {
            emptyStateView.setVisibility(View.VISIBLE);
        }
        if (recyclerViewUsers != null) {
            recyclerViewUsers.setVisibility(View.GONE);
        }
    }

    private void hideEmptyState() {
        if (emptyStateView != null) {
            emptyStateView.setVisibility(View.GONE);
        }
        if (recyclerViewUsers != null) {
            recyclerViewUsers.setVisibility(View.VISIBLE);
        }
    }

    private void updateEmptyState() {
        if (filteredUserList.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
    }
}

