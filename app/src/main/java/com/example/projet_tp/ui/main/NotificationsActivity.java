package com.example.projet_tp.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet_tp.R;
import com.example.projet_tp.adapter.NotificationAdapter;
import com.example.projet_tp.model.NotificationItem;
import com.example.projet_tp.utils.NotificationStorageHelper;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewNotifications;
    private android.widget.LinearLayout textViewEmpty;
    private NotificationAdapter adapter;
    private List<NotificationItem> notificationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_notifications);

            setupToolbar();
            initViews();
            setupRecyclerView();
            loadNotifications();
        } catch (Exception e) {
            android.util.Log.e("NotificationsActivity", "Erreur dans onCreate", e);
            e.printStackTrace();
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Notifications");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    private void initViews() {
        recyclerViewNotifications = findViewById(R.id.recyclerViewNotifications);
        textViewEmpty = findViewById(R.id.textViewEmpty);
    }

    private void setupRecyclerView() {
        try {
            if (recyclerViewNotifications != null) {
                adapter = new NotificationAdapter(notificationList, this);
                LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                recyclerViewNotifications.setLayoutManager(layoutManager);
                recyclerViewNotifications.setAdapter(adapter);
                recyclerViewNotifications.setHasFixedSize(false);
                android.util.Log.d("NotificationsActivity", "RecyclerView configuré avec " + notificationList.size() + " notifications");
            }
        } catch (Exception e) {
            android.util.Log.e("NotificationsActivity", "Erreur lors de la configuration du RecyclerView", e);
            e.printStackTrace();
        }
    }

    private void loadNotifications() {
        try {
            notificationList.clear();
            List<NotificationItem> notifications = NotificationStorageHelper.getAllNotifications(this);
            if (notifications != null) {
                notificationList.addAll(notifications);
                android.util.Log.d("NotificationsActivity", "Notifications chargées: " + notificationList.size());
            } else {
                android.util.Log.d("NotificationsActivity", "Aucune notification trouvée");
            }
            
            if (notificationList.isEmpty()) {
                showEmptyState();
                android.util.Log.d("NotificationsActivity", "Affichage de l'état vide");
            } else {
                hideEmptyState();
                if (adapter != null) {
                    adapter.updateNotifications(notificationList);
                    android.util.Log.d("NotificationsActivity", "Adapter mis à jour avec " + notificationList.size() + " notifications");
                } else {
                    android.util.Log.w("NotificationsActivity", "Adapter est null, recréation...");
                    setupRecyclerView();
                    if (adapter != null) {
                        adapter.updateNotifications(notificationList);
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("NotificationsActivity", "Erreur lors du chargement des notifications", e);
            e.printStackTrace();
            showEmptyState();
            Toast.makeText(this, "Erreur lors du chargement des notifications", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEmptyState() {
        if (textViewEmpty != null) {
            textViewEmpty.setVisibility(View.VISIBLE);
        }
        if (recyclerViewNotifications != null) {
            recyclerViewNotifications.setVisibility(View.GONE);
        }
    }

    private void hideEmptyState() {
        if (textViewEmpty != null) {
            textViewEmpty.setVisibility(View.GONE);
        }
        if (recyclerViewNotifications != null) {
            recyclerViewNotifications.setVisibility(View.VISIBLE);
        }
    }

    public void onNotificationClick(NotificationItem notification) {
        NotificationStorageHelper.markAsRead(this, notification.getId());
        
        if (notification.getMenuId() != null && !notification.getMenuId().isEmpty()) {
            Intent intent = new Intent(this, MenuActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, notification.getTitle(), Toast.LENGTH_SHORT).show();
        }
        
        loadNotifications();
    }

    public void onNotificationDelete(NotificationItem notification) {
        NotificationStorageHelper.deleteNotification(this, notification.getId());
        loadNotifications();
        Toast.makeText(this, "Notification supprimée", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications();
    }
}

