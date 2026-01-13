package com.example.projet_tp.ui.main;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.projet_tp.R;
import com.example.projet_tp.ui.auth.LoginActivity;
import com.example.projet_tp.ui.admin.AdminHomeActivity;
import com.example.projet_tp.ui.reservation.ReservationActivity;
import com.example.projet_tp.ui.reservation.ReservationListActivity;
import com.example.projet_tp.ui.main.ProfileActivity;
import com.example.projet_tp.ui.main.MenuActivity;
import com.example.projet_tp.ui.main.NotificationsActivity;
import com.example.projet_tp.utils.ReservationManager;
import com.example.projet_tp.utils.SessionManager;
import com.example.projet_tp.model.NotificationItem;
import com.example.projet_tp.utils.NotificationStorageHelper;
import android.widget.PopupMenu;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class HomeActivity extends AppCompatActivity {

    private MaterialButton buttonReserve, buttonReserveColdMeal, buttonAddLunch, buttonAddDinner, buttonCommande;
    private MaterialCardView cardLunch, cardDinner, cardLunchDinner, cardColdMeal, cardCommande;
    private ExtendedFloatingActionButton fabHelp;
    private SessionManager sessionManager;
    private ReservationManager reservationManager;
    private ImageView iconNotifications, iconSubscription, iconLogout, profileImage;
    private TextView textViewUserName;
    private LinearLayout navHome, navMenu, navReservations, navProfile, navLogout;
    private RecyclerView recyclerViewCategories;
    
    private ActivityResultLauncher<Intent> profileLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sessionManager = new SessionManager(this);
        reservationManager = new ReservationManager(this);

        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        String role = sessionManager.getRole();
        String studentId = sessionManager.getUserId();
        
        boolean isAdmin = (studentId != null && studentId.startsWith("ADMIN_"))
                         || (role != null && role.equals("admin"))
                         || sessionManager.isAdmin();
        
        if (isAdmin) {
            android.util.Log.d("HomeActivity", "Admin détecté - Redirection vers AdminHomeActivity");
            redirectToAdminHome();
            return;
        }

        reservationManager.cleanExpiredReservations();

        initViews();
        setupProfileLauncher();
        setupClickListeners();
        setupCardAnimations();
        setupBottomNavigation();
        setupUserInfo();
        startEntranceAnimations();
    }
    
    private void setupProfileLauncher() {
        profileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    refreshUserInfo();
                }
            }
        );
    }

    private void initViews() {
        try {
            cardLunch = findViewById(R.id.cardLunch);
            cardDinner = findViewById(R.id.cardDinner);
            cardColdMeal = findViewById(R.id.cardColdMeal);

            buttonReserveColdMeal = findViewById(R.id.buttonReserveColdMeal);
            buttonAddLunch = findViewById(R.id.buttonAddLunch);
            buttonAddDinner = findViewById(R.id.buttonAddDinner);
            buttonCommande = findViewById(R.id.buttonCommande);
            cardCommande = findViewById(R.id.cardCommande);

        iconNotifications = findViewById(R.id.iconNotifications);
        iconSubscription = findViewById(R.id.iconSubscription);
        iconLogout = findViewById(R.id.iconLogout);
        profileImage = findViewById(R.id.profileImage);
        textViewUserName = findViewById(R.id.textViewUserName);
            
            navHome = findViewById(R.id.navHome);
            navMenu = findViewById(R.id.navMenu);
            navReservations = findViewById(R.id.navReservations);
            navProfile = findViewById(R.id.navProfile);
            navLogout = findViewById(R.id.navLogout);
            
            recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        } catch (Exception e) {
            android.util.Log.e("HomeActivity", "Erreur lors de l'initialisation des vues: " + e.getMessage(), e);
        }
    }
    
    private void setupUserInfo() {
        refreshUserInfo();
    }
    
    private void refreshUserInfo() {
        try {
            if (sessionManager != null && textViewUserName != null) {
                String fullName = sessionManager.getFullName();
                if (fullName != null && !fullName.isEmpty()) {
                    textViewUserName.setText(fullName);
                } else {
                    String email = sessionManager.getEmail();
                    if (email != null && !email.isEmpty()) {
                        textViewUserName.setText(email);
                    }
                }
            }
            
            if (profileImage != null && sessionManager != null) {
                loadProfileImage();
            }
        } catch (Exception e) {
            android.util.Log.e("HomeActivity", "Erreur lors du rafraîchissement des informations utilisateur", e);
        }
    }
    
    private void loadProfileImage() {
        try {
            if (profileImage == null || sessionManager == null) {
                return;
            }
            
            String imagePath = sessionManager.getProfileImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                try {
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(imagePath);
                    if (bitmap != null) {
                        profileImage.setImageBitmap(bitmap);
                        return;
                    }
                } catch (Exception e) {
                    android.util.Log.e("HomeActivity", "Erreur lors du chargement de l'image: " + e.getMessage(), e);
                }
            }
            
            profileImage.setImageResource(R.drawable.ic_profile_placeholder);
        } catch (Exception e) {
            android.util.Log.e("HomeActivity", "Erreur lors du chargement de l'image de profil", e);
            if (profileImage != null) {
                profileImage.setImageResource(R.drawable.ic_profile_placeholder);
            }
        }
    }
    
    private void setupBottomNavigation() {
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
            });
        }
        
        if (navMenu != null) {
            navMenu.setOnClickListener(v -> {
                Intent intent = new Intent(this, MenuActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }
        
        if (navReservations != null) {
            navReservations.setOnClickListener(v -> {
                Intent intent = new Intent(this, ReservationListActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }
        
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProfileActivity.class);
                profileLauncher.launch(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }
        
        if (navLogout != null) {
            navLogout.setOnClickListener(v -> {
                v.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                        showLogoutDialog();
                    })
                    .start();
            });
        }
    }

    private void setupClickListeners() {
        try {
            if (buttonAddLunch != null) {
                buttonAddLunch.setOnClickListener(v -> startReservationActivity("lunch"));
            }

            if (buttonAddDinner != null) {
                buttonAddDinner.setOnClickListener(v -> startReservationActivity("dinner"));
            }

            if (cardLunch != null) {
                cardLunch.setOnClickListener(v -> startReservationActivity("lunch"));
            }

            if (cardDinner != null) {
                cardDinner.setOnClickListener(v -> startReservationActivity("dinner"));
            }

            if (buttonReserveColdMeal != null) {
                buttonReserveColdMeal.setOnClickListener(v -> startReservationActivity("cold"));
            }

            if (cardColdMeal != null) {
                cardColdMeal.setOnClickListener(v -> startReservationActivity("cold"));
            }
            
            if (buttonCommande != null) {
                buttonCommande.setOnClickListener(v -> {
                    Intent intent = new Intent(this, CommandeActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                });
            }
            
            if (cardCommande != null) {
                cardCommande.setOnClickListener(v -> {
                    Intent intent = new Intent(this, CommandeActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                });
            }
            
            if (iconNotifications != null) {
                iconNotifications.setOnClickListener(v -> showNotificationsPopupMenu(v));
            }
            
            if (iconSubscription != null) {
                iconSubscription.setOnClickListener(v -> {
                    Intent intent = new Intent(this, SubscriptionActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                });
            }
            
            if (profileImage != null) {
                profileImage.setOnClickListener(v -> {
                    Intent intent = new Intent(this, ProfileActivity.class);
                    profileLauncher.launch(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                });
            }
            
            if (iconLogout != null) {
                iconLogout.setOnClickListener(v -> {
                    v.animate()
                        .scaleX(0.9f)
                        .scaleY(0.9f)
                        .setDuration(100)
                        .withEndAction(() -> {
                            v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start();
                            showLogoutDialog();
                        })
                        .start();
                });
            }
        } catch (Exception e) {
            android.util.Log.e("HomeActivity", "Erreur lors de la configuration des listeners: " + e.getMessage(), e);
        }
    }

    private void showMealTypeDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Choisir un repas à réserver")
                .setMessage("Sélectionnez le type de repas que vous souhaitez réserver :\n\n" +
                        "🍽️ Déjeuner : Formule complète (12h30 - 13h30)\n" +
                        "🌙 Dîner : Menu gastronomique (18h00 - 19h30)")
                .setPositiveButton("🍽️ Déjeuner", (dialog, which) -> startReservationActivity("lunch"))
                .setNegativeButton("🌙 Dîner", (dialog, which) -> startReservationActivity("dinner"))
                .setNeutralButton("Annuler", null)
                .show();
    }

    private void startEntranceAnimations() {
        if (cardLunch != null) {
            cardLunch.setAlpha(0f);
            cardLunch.setTranslationY(50f);
            cardLunch.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(600)
                    .setStartDelay(100)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }

        if (cardDinner != null) {
            cardDinner.setAlpha(0f);
            cardDinner.setTranslationY(50f);
            cardDinner.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(600)
                    .setStartDelay(200)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }

        if (cardColdMeal != null) {
            cardColdMeal.setAlpha(0f);
            cardColdMeal.setTranslationY(50f);
            cardColdMeal.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(600)
                    .setStartDelay(300)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    }

    private void setupCardAnimations() {
        if (cardLunch != null) setupCardAnimation(cardLunch);
        if (cardDinner != null) setupCardAnimation(cardDinner);
        if (cardColdMeal != null) setupCardAnimation(cardColdMeal);
    }

    private void setupCardAnimation(MaterialCardView card) {
        if (card == null) return;
        card.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    card.animate()
                            .scaleX(0.96f)
                            .scaleY(0.96f)
                            .setDuration(150)
                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            .start();
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    card.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(200)
                            .setInterpolator(new OvershootInterpolator())
                            .start();
                    break;
            }
            return false;
        });
    }

    private void startReservationActivity(String mealType) {
        Intent intent = new Intent(this, ReservationActivity.class);
        intent.putExtra("meal_type", mealType);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void showHelpDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Aide Réservation")
                .setMessage("• Déjeuner: 12h30 - 13h30\n• Dîner: 18h00 - 19h30")
                .setPositiveButton("Compris", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            profileLauncher.launch(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            return true;
        }

        if (id == R.id.menu_notifications) {
            Intent intent = new Intent(this, NotificationsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            return true;
        }

        if (id == R.id.menu_comments) {
            Toast.makeText(this, "Commentaires clicked", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (id == R.id.action_logout) {
            showLogoutDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onResume() {
        super.onResume();

        refreshUserInfo();
    }

    private void showLogoutDialog() {
        try {
            android.app.Dialog dialog = new android.app.Dialog(this);
            dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_logout);
            
            com.google.android.material.button.MaterialButton buttonCancel =
                dialog.findViewById(R.id.buttonCancelLogout);
            com.google.android.material.button.MaterialButton buttonConfirm = 
                dialog.findViewById(R.id.buttonConfirmLogout);
            
            if (buttonCancel != null) {
                buttonCancel.setOnClickListener(v -> dialog.dismiss());
            }
            
            if (buttonConfirm != null) {
                buttonConfirm.setOnClickListener(v -> {
                    dialog.dismiss();
                    performLogout();
                });
            }
            
            android.view.Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
                               android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
                window.setBackgroundDrawableResource(android.R.color.transparent);
                window.setGravity(android.view.Gravity.CENTER);
                
                window.getAttributes().windowAnimations = R.style.DialogAnimation;
            }
            
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        } catch (Exception e) {
            android.util.Log.e("HomeActivity", "Erreur lors de l'affichage du dialog de déconnexion", e);
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Déconnexion")
                    .setMessage("Voulez-vous vous déconnecter ?")
                    .setPositiveButton("Oui", (d, which) -> performLogout())
                    .setNegativeButton("Annuler", null)
                    .show();
        }
    }

    private void performLogout() {
        sessionManager.logout();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void redirectToAdminHome() {
        Intent intent = new Intent(HomeActivity.this, AdminHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showNotificationsPopupMenu(View anchor) {
        try {
            List<NotificationItem> notifications = NotificationStorageHelper.getAllNotifications(this);
            
            if (notifications == null || notifications.isEmpty()) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("🔔 Notifications")
                        .setMessage("Vous n'avez pas encore de notifications")
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }

            android.app.Dialog dialog = new android.app.Dialog(this);
            dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_notifications_list);
            
            RecyclerView recyclerView = dialog.findViewById(R.id.recyclerViewNotificationsDialog);
            TextView textViewTitle = dialog.findViewById(R.id.textViewDialogTitle);
            View buttonClose = dialog.findViewById(R.id.buttonCloseDialog);
            MaterialButton buttonViewAll = dialog.findViewById(R.id.buttonViewAll);
            
            if (textViewTitle != null) {
                int unreadCount = NotificationStorageHelper.getUnreadCount(this);
                textViewTitle.setText("🔔 Notifications (" + notifications.size() + ")" + 
                    (unreadCount > 0 ? " • " + unreadCount + " non lues" : ""));
            }
            
            if (buttonClose != null) {
                buttonClose.setOnClickListener(v -> dialog.dismiss());
            }
            
            if (buttonViewAll != null) {
                buttonViewAll.setOnClickListener(v -> {
                    dialog.dismiss();
                    Intent intent = new Intent(this, NotificationsActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                });
            }
            
            if (recyclerView != null) {
                final List<NotificationItem> displayNotifications = notifications.size() > 5
                    ? notifications.subList(0, 5) 
                    : notifications;
                
                com.example.projet_tp.adapter.NotificationAdapter adapter = 
                    new com.example.projet_tp.adapter.NotificationAdapter(displayNotifications, null);
                recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
                recyclerView.setAdapter(adapter);
                
                recyclerView.addOnItemTouchListener(new androidx.recyclerview.widget.RecyclerView.SimpleOnItemTouchListener() {
                    @Override
                    public boolean onInterceptTouchEvent(@NonNull androidx.recyclerview.widget.RecyclerView rv, @NonNull android.view.MotionEvent e) {
                        if (e.getAction() == android.view.MotionEvent.ACTION_UP) {
                            View child = rv.findChildViewUnder(e.getX(), e.getY());
                            if (child != null) {
                                int position = rv.getChildAdapterPosition(child);
                                if (position >= 0 && position < displayNotifications.size()) {
                                    NotificationItem notification = displayNotifications.get(position);
                                    NotificationStorageHelper.markAsRead(HomeActivity.this, notification.getId());
                                    if (notification.getMenuId() != null && !notification.getMenuId().isEmpty()) {
                                        Intent intent = new Intent(HomeActivity.this, MenuActivity.class);
                                        startActivity(intent);
                                    }
                                    dialog.dismiss();
                                    return true;
                                }
                            }
                        }
                        return false;
                    }
                });
            }
            
            android.view.Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
                               android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
                window.setBackgroundDrawableResource(android.R.color.transparent);
                window.setGravity(android.view.Gravity.TOP);
            }
            
            dialog.show();
        } catch (Exception e) {
            android.util.Log.e("HomeActivity", "Erreur lors de l'affichage des notifications", e);
            e.printStackTrace();
            Intent intent = new Intent(this, NotificationsActivity.class);
            startActivity(intent);
        }
    }

}
