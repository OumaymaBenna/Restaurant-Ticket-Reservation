package com.example.projet_tp.ui.admin;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet_tp.R;
import com.example.projet_tp.adapter.MenuAdminAdapter;
import com.example.projet_tp.api.RetrofitClient;
import com.example.projet_tp.model.Menu;
import com.example.projet_tp.model.MenuResponse;
import com.example.projet_tp.network.ApiService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageMenusActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMenus;
    private MenuAdminAdapter adapter;
    private List<Menu> menuList;
    private ProgressBar progressBar;
    private android.widget.LinearLayout textViewEmpty;
    private ExtendedFloatingActionButton fabAddMenu;
    private ApiService apiService;
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_manage_menus);

            apiService = RetrofitClient.getApiService();
            menuList = new ArrayList<>();

            requestNotificationPermission();

            initViews();
            setupToolbar();
            setupRecyclerView();
            setupClickListeners();
            loadMenus();
        } catch (Exception e) {
            android.util.Log.e("ManageMenusActivity", "Erreur dans onCreate", e);
            e.printStackTrace();
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE
                );
            } else {
                com.example.projet_tp.utils.MenuNotificationHelper.createNotificationChannel(this);
            }
        } else {
            com.example.projet_tp.utils.MenuNotificationHelper.createNotificationChannel(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                android.util.Log.d("ManageMenusActivity", "Permission de notification accordée");
                com.example.projet_tp.utils.MenuNotificationHelper.createNotificationChannel(this);
            } else {
                android.util.Log.w("ManageMenusActivity", "Permission de notification refusée");
                Toast.makeText(this, "Les notifications ne fonctionneront pas sans cette permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initViews() {
        recyclerViewMenus = findViewById(R.id.recyclerViewMenus);
        progressBar = findViewById(R.id.progressBar);
        textViewEmpty = findViewById(R.id.textViewEmpty);
        fabAddMenu = findViewById(R.id.fabAddMenu);
        
        if (textViewEmpty != null) {
            com.google.android.material.button.MaterialButton buttonCreateMenu = 
                textViewEmpty.findViewById(R.id.buttonCreateMenu);
            if (buttonCreateMenu != null) {
                buttonCreateMenu.setOnClickListener(v -> showAddMenuDialog());
            }
        }
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Gérer les Menus");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    private void setupRecyclerView() {
        if (recyclerViewMenus != null) {
            try {
                adapter = new MenuAdminAdapter(menuList, this);
                recyclerViewMenus.setLayoutManager(new LinearLayoutManager(this));
                recyclerViewMenus.setAdapter(adapter);
            } catch (Exception e) {
                android.util.Log.e("ManageMenusActivity", "Erreur lors de la configuration du RecyclerView", e);
            }
        }
    }

    private void setupClickListeners() {
        if (fabAddMenu != null) {
            fabAddMenu.setOnClickListener(v -> showAddMenuDialog());
        }
    }

    private void loadMenus() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (textViewEmpty != null) {
            textViewEmpty.setVisibility(View.GONE);
        }

        if (apiService == null) {
            showError("Erreur de connexion");
            return;
        }

        apiService.getMenus().enqueue(new Callback<MenuResponse>() {
            @Override
            public void onResponse(Call<MenuResponse> call, Response<MenuResponse> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

                if (response.isSuccessful() && response.body() != null) {
                    MenuResponse menuResponse = response.body();
                    if (menuResponse.isSuccess() && menuResponse.getMenus() != null) {
                        menuList.clear();
                        menuList.addAll(menuResponse.getMenus());
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                        updateEmptyState();
                    } else {
                        showError("Aucun menu trouvé");
                        updateEmptyState();
                    }
                } else {
                    showError("Erreur lors du chargement des menus");
                    updateEmptyState();
                }
            }

            @Override
            public void onFailure(Call<MenuResponse> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                android.util.Log.e("ManageMenusActivity", "Erreur de connexion", t);
                showError("Erreur de connexion: " + (t != null ? t.getMessage() : "Erreur inconnue"));
                updateEmptyState();
            }
        });
    }

    private void updateEmptyState() {
        try {
            if (menuList.isEmpty()) {
                if (textViewEmpty != null) {
                    textViewEmpty.setVisibility(View.VISIBLE);
                }
                if (recyclerViewMenus != null) {
                    recyclerViewMenus.setVisibility(View.GONE);
                }
            } else {
                if (textViewEmpty != null) {
                    textViewEmpty.setVisibility(View.GONE);
                }
                if (recyclerViewMenus != null) {
                    recyclerViewMenus.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("ManageMenusActivity", "Erreur lors de la mise à jour de l'état vide", e);
        }
    }

    private void showAddMenuDialog() {
        try {
            if (!isFinishing() && !isDestroyed()) {
                MenuDialogFragment dialog = MenuDialogFragment.newInstance(null, this);
                dialog.show(getSupportFragmentManager(), "MenuDialog");
            }
        } catch (Exception e) {
            android.util.Log.e("ManageMenusActivity", "Erreur lors de l'affichage du dialog", e);
            Toast.makeText(this, "Erreur lors de l'ouverture du formulaire", Toast.LENGTH_SHORT).show();
        }
    }

    public void showEditMenuDialog(Menu menu) {
        try {
            if (!isFinishing() && !isDestroyed() && menu != null) {
                MenuDialogFragment dialog = MenuDialogFragment.newInstance(menu, this);
                dialog.show(getSupportFragmentManager(), "MenuDialog");
            }
        } catch (Exception e) {
            android.util.Log.e("ManageMenusActivity", "Erreur lors de l'affichage du dialog d'édition", e);
            Toast.makeText(this, "Erreur lors de l'ouverture du formulaire", Toast.LENGTH_SHORT).show();
        }
    }

    public void showDeleteConfirmationDialog(Menu menu) {
        try {
            if (!isFinishing() && !isDestroyed() && menu != null) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Supprimer le menu")
                        .setMessage("Êtes-vous sûr de vouloir supprimer le menu \"" + (menu.getName() != null ? menu.getName() : "ce menu") + "\" ?")
                        .setPositiveButton("Supprimer", (dialog, which) -> deleteMenu(menu))
                        .setNegativeButton("Annuler", null)
                        .show();
            }
        } catch (Exception e) {
            android.util.Log.e("ManageMenusActivity", "Erreur lors de l'affichage du dialog de confirmation", e);
            Toast.makeText(this, "Erreur lors de l'ouverture du dialog", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteMenu(Menu menu) {
        if (menu == null || menu.getId() == null) {
            showError("Menu invalide");
            return;
        }
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (apiService == null) {
            showError("Erreur de connexion");
            return;
        }
        apiService.deleteMenu(menu.getId()).enqueue(new Callback<com.example.projet_tp.model.ApiResponse>() {
            @Override
            public void onResponse(Call<com.example.projet_tp.model.ApiResponse> call, Response<com.example.projet_tp.model.ApiResponse> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    onMenuDeleted(menu.getId());
                } else {
                    showError("Erreur lors de la suppression du menu");
                }
            }

            @Override
            public void onFailure(Call<com.example.projet_tp.model.ApiResponse> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                android.util.Log.e("ManageMenusActivity", "Erreur lors de la suppression", t);
                showError("Erreur de connexion: " + (t != null ? t.getMessage() : "Erreur inconnue"));
            }
        });
    }

    public void onMenuAdded(Menu menu) {
        if (menu != null) {
            menuList.add(menu);
            if (adapter != null) {
                adapter.notifyItemInserted(menuList.size() - 1);
            }
            updateEmptyState();
            Toast.makeText(this, "Menu ajouté avec succès", Toast.LENGTH_SHORT).show();
        }
    }

    public void onMenuUpdated(Menu menu) {
        if (menu != null && menu.getId() != null) {
            int position = findMenuPosition(menu.getId());
            if (position != -1) {
                menuList.set(position, menu);
                if (adapter != null) {
                    adapter.notifyItemChanged(position);
                }
                Toast.makeText(this, "Menu modifié avec succès", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onMenuDeleted(String menuId) {
        if (menuId != null) {
            int position = findMenuPosition(menuId);
            if (position != -1) {
                menuList.remove(position);
                if (adapter != null) {
                    adapter.notifyItemRemoved(position);
                }
                updateEmptyState();
                Toast.makeText(this, "Menu supprimé avec succès", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private int findMenuPosition(String menuId) {
        for (int i = 0; i < menuList.size(); i++) {
            if (menuList.get(i).getId() != null && menuList.get(i).getId().equals(menuId)) {
                return i;
            }
        }
        return -1;
    }

    public void refreshMenus() {
        loadMenus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recharger les menus pour afficher les nouveaux commentaires
        loadMenus();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

