package com.example.projet_tp.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.projet_tp.R;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MotionEvent;

import com.example.projet_tp.network.ApiService;
import com.example.projet_tp.api.RetrofitClient;
import com.example.projet_tp.model.Menu;
import com.example.projet_tp.model.MenuResponse;
import com.example.projet_tp.model.StudentComment;
import com.example.projet_tp.model.CommentResponse;
import com.example.projet_tp.utils.SessionManager;
import com.example.projet_tp.utils.OrderManager;
import com.example.projet_tp.adapter.CommentAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMenu;
    private ProgressBar progressBar;
    private MenuAdapter menuAdapter;
    private List<Menu> menuList = new ArrayList<>();
    private ApiService apiService;
    private SessionManager sessionManager;
    private OrderManager orderManager;
    private Map<String, List<StudentComment>> commentsMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        apiService = RetrofitClient.getApiService();
        sessionManager = new SessionManager(this);
        orderManager = OrderManager.getInstance();
        
        // Initialiser la commande si elle n'existe pas
        if (orderManager.getCurrentOrder() == null) {
            String userId = sessionManager.getUserId();
            String userName = sessionManager.getFullName();
            if (userId != null && userName != null) {
                orderManager.createNewOrder(userId, userName);
            }
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        
        // Ajouter un bouton panier dans la toolbar
        toolbar.inflateMenu(R.menu.menu_cart);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_cart) {
                Intent cartIntent = new Intent(this, CartActivity.class);
                startActivity(cartIntent);
                return true;
            }
            return false;
        });

        recyclerViewMenu = findViewById(R.id.recyclerViewMenu);
        recyclerViewMenu.setLayoutManager(new LinearLayoutManager(this));
        // Désactiver l'interception des clics par le RecyclerView pour permettre aux boutons de fonctionner
        recyclerViewMenu.setNestedScrollingEnabled(false);
        // Important : permettre aux vues enfants (boutons) de recevoir les touches
        recyclerViewMenu.setHasFixedSize(false);
        // Permettre aux vues enfants d'intercepter les touches
        recyclerViewMenu.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

        progressBar = findViewById(R.id.progressBar);

        com.example.projet_tp.utils.MenuNotificationHelper.createNotificationChannel(this);

        loadMenus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMenus();
    }

    private void loadMenus() {
        progressBar.setVisibility(View.VISIBLE);

        apiService.getMenus().enqueue(new Callback<MenuResponse>() {
            @Override
            public void onResponse(Call<MenuResponse> call, Response<MenuResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    MenuResponse menuResponse = response.body();
                    menuList.clear();
                    
                    if (menuResponse.getMenus() != null) {
                        menuList.addAll(menuResponse.getMenus());
                    }

                    if (menuList.isEmpty()) {
                        showEmptyState();
                        Toast.makeText(MenuActivity.this,
                                "Aucun menu disponible pour aujourd'hui",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        hideEmptyState();
                        if (menuAdapter == null) {
                            menuAdapter = new MenuAdapter(menuList, sessionManager);
                            recyclerViewMenu.setAdapter(menuAdapter);
                        } else {
                            menuAdapter.updateMenus(menuList);
                        }
                        // Charger les commentaires pour chaque menu
                        loadCommentsForMenus();
                    }
                } else {
                    showEmptyState();
                    Toast.makeText(MenuActivity.this,
                            "Erreur de chargement des menus",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MenuResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showEmptyState();
                android.util.Log.e("MenuActivity", "Erreur réseau: " + t.getMessage(), t);
                Toast.makeText(MenuActivity.this,
                        "Erreur réseau : " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showEmptyState() {
        View emptyState = findViewById(R.id.emptyState);
        if (emptyState != null) {
            emptyState.setVisibility(View.VISIBLE);
        }
        if (recyclerViewMenu != null) {
            recyclerViewMenu.setVisibility(View.GONE);
        }
    }

    private void hideEmptyState() {
        View emptyState = findViewById(R.id.emptyState);
        if (emptyState != null) {
            emptyState.setVisibility(View.GONE);
        }
        if (recyclerViewMenu != null) {
            recyclerViewMenu.setVisibility(View.VISIBLE);
        }
    }



    private void publishComment(String menuId, String commentText, int position) {
        android.util.Log.d("MenuActivity", "📝 Publication de commentaire - menuId: " + menuId + ", Position: " + position);
        
        // Vérifier que sessionManager est disponible
        if (sessionManager == null) {
            android.util.Log.e("MenuActivity", "❌ sessionManager est null");
            Toast.makeText(this, "Erreur: Session non disponible. Veuillez vous reconnecter.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String studentId = sessionManager.getUserId();
        String userName = sessionManager.getFullName();
        
        android.util.Log.d("MenuActivity", "studentId: " + studentId + ", userName: " + userName + ", menuId: " + menuId);
        
        if (studentId == null || studentId.isEmpty()) {
            android.util.Log.e("MenuActivity", "❌ studentId est null ou vide");
            Toast.makeText(this, "Erreur: ID étudiant non disponible. Veuillez vous reconnecter.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (userName == null || userName.isEmpty()) {
            android.util.Log.e("MenuActivity", "❌ userName est null ou vide");
            Toast.makeText(this, "Erreur: Nom utilisateur non disponible. Veuillez vous reconnecter.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (menuId == null || menuId.isEmpty()) {
            android.util.Log.e("MenuActivity", "❌ menuId est null ou vide");
            Toast.makeText(this, "Erreur: ID menu non disponible", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (commentText == null || commentText.trim().isEmpty()) {
            Toast.makeText(this, "Veuillez saisir un commentaire", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Créer le commentaire
        StudentComment comment = new StudentComment();
        comment.setMenuId(menuId);
        comment.setStudentId(studentId);
        comment.setUserName(userName);
        comment.setComment(commentText.trim());
        
        android.util.Log.d("MenuActivity", "Envoi du commentaire au serveur...");
        
        // Désactiver le bouton pendant l'envoi
        final int finalPosition = position;
        if (menuAdapter != null && finalPosition != RecyclerView.NO_POSITION) {
            RecyclerView.ViewHolder holder = recyclerViewMenu.findViewHolderForAdapterPosition(finalPosition);
            if (holder instanceof MenuAdapter.MenuViewHolder) {
                MenuAdapter.MenuViewHolder menuHolder = (MenuAdapter.MenuViewHolder) holder;
                if (menuHolder.buttonPublishComment != null) {
                    menuHolder.buttonPublishComment.setEnabled(false);
                    menuHolder.buttonPublishComment.setText("Publication...");
                }
            }
        }
        
        // Envoyer le commentaire au serveur
        apiService.createComment(menuId, comment).enqueue(new Callback<com.example.projet_tp.model.ApiResponse>() {
            @Override
            public void onResponse(Call<com.example.projet_tp.model.ApiResponse> call, Response<com.example.projet_tp.model.ApiResponse> response) {
                // Réactiver le bouton
                if (menuAdapter != null && finalPosition != RecyclerView.NO_POSITION) {
                    RecyclerView.ViewHolder holder = recyclerViewMenu.findViewHolderForAdapterPosition(finalPosition);
                    if (holder instanceof MenuAdapter.MenuViewHolder) {
                        MenuAdapter.MenuViewHolder menuHolder = (MenuAdapter.MenuViewHolder) holder;
                        if (menuHolder.buttonPublishComment != null) {
                            menuHolder.buttonPublishComment.setEnabled(true);
                            menuHolder.buttonPublishComment.setText("Publier");
                        }
                        if (menuHolder.editTextComment != null) {
                            menuHolder.editTextComment.setText("");
                        }
                    }
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    com.example.projet_tp.model.ApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(MenuActivity.this, "Commentaire publié avec succès", Toast.LENGTH_SHORT).show();
                        // Recharger les commentaires après ajout
                        loadComments(menuId);
                        // Rafraîchir l'item du menu pour afficher le nouveau commentaire
                        if (finalPosition != RecyclerView.NO_POSITION && menuAdapter != null) {
                            android.util.Log.d("MenuActivity", "Rafraîchissement de l'item à la position: " + finalPosition);
                            menuAdapter.notifyItemChanged(finalPosition);
                        }
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Erreur lors de la publication du commentaire";
                        Toast.makeText(MenuActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                } else {
                    String errorMsg = "Erreur serveur (Code " + response.code() + ")";
                    Toast.makeText(MenuActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onFailure(Call<com.example.projet_tp.model.ApiResponse> call, Throwable t) {
                android.util.Log.e("MenuActivity", "Erreur lors de la publication du commentaire", t);
                
                // Réactiver le bouton
                if (menuAdapter != null && finalPosition != RecyclerView.NO_POSITION) {
                    RecyclerView.ViewHolder holder = recyclerViewMenu.findViewHolderForAdapterPosition(finalPosition);
                    if (holder instanceof MenuAdapter.MenuViewHolder) {
                        MenuAdapter.MenuViewHolder menuHolder = (MenuAdapter.MenuViewHolder) holder;
                        if (menuHolder.buttonPublishComment != null) {
                            menuHolder.buttonPublishComment.setEnabled(true);
                            menuHolder.buttonPublishComment.setText("Publier");
                        }
                    }
                }
                
                String errorMsg = "Erreur de connexion";
                if (t != null && t.getMessage() != null) {
                    errorMsg += ": " + t.getMessage();
                }
                Toast.makeText(MenuActivity.this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadCommentsForMenus() {
        for (Menu menu : menuList) {
            if (menu.getId() != null) {
                loadComments(menu.getId());
            }
        }
    }

    private void loadComments(String menuId) {
        android.util.Log.d("MenuActivity", "Chargement des commentaires pour menuId: " + menuId);
        apiService.getComments(menuId).enqueue(new Callback<CommentResponse>() {
            @Override
            public void onResponse(Call<CommentResponse> call, Response<CommentResponse> response) {
                android.util.Log.d("MenuActivity", "Réponse commentaires - Code: " + response.code() + ", Succès: " + response.isSuccessful());
                if (response.isSuccessful() && response.body() != null) {
                    CommentResponse commentResponse = response.body();
                    if (commentResponse.getComments() != null) {
                        android.util.Log.d("MenuActivity", "Commentaires reçus: " + commentResponse.getComments().size());
                        commentsMap.put(menuId, commentResponse.getComments());
                        // Rafraîchir l'adaptateur pour afficher les commentaires
                        if (menuAdapter != null) {
                            menuAdapter.notifyDataSetChanged();
                        }
                    } else {
                        android.util.Log.d("MenuActivity", "Aucun commentaire dans la réponse");
                        commentsMap.put(menuId, new ArrayList<>());
                        if (menuAdapter != null) {
                            menuAdapter.notifyDataSetChanged();
                        }
                    }
                } else {
                    android.util.Log.e("MenuActivity", "Erreur réponse - Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CommentResponse> call, Throwable t) {
                android.util.Log.e("MenuActivity", "Erreur lors du chargement des commentaires", t);
                if (t != null) {
                    android.util.Log.e("MenuActivity", "Message: " + t.getMessage());
                }
            }
        });
    }

    private class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {
        private List<Menu> menus;
        private SessionManager sessionManager;

        public MenuAdapter(List<Menu> menus, SessionManager sessionManager) {
            this.menus = menus;
            this.sessionManager = sessionManager;
        }

        public void updateMenus(List<Menu> newMenus) {
            this.menus = newMenus;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_menu, parent, false);
            return new MenuViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
            Menu menu = menus.get(position);
            holder.bind(menu, position, sessionManager);
        }

        @Override
        public int getItemCount() {
            return menus.size();
        }

        class MenuViewHolder extends RecyclerView.ViewHolder {
            private TextView textViewMenuName, textViewAppetizer, textViewMainCourse,
                    textViewDessert, textViewPrice, textViewDate, textViewComment;
            private com.google.android.material.card.MaterialCardView cardComment;
            private MaterialButton buttonSelect;
            public MaterialButton buttonPublishComment;
            public com.google.android.material.textfield.TextInputEditText editTextComment;
            private android.view.View layoutAddComment;
            private RecyclerView recyclerViewComments;
            private TextView textViewNoComments;
            private CommentAdapter commentAdapter;
            private List<StudentComment> comments = new ArrayList<>();

            public MenuViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewMenuName = itemView.findViewById(R.id.textViewMenuName);
                textViewAppetizer = itemView.findViewById(R.id.textViewAppetizer);
                textViewMainCourse = itemView.findViewById(R.id.textViewMainCourse);
                textViewDessert = itemView.findViewById(R.id.textViewDessert);
                textViewPrice = itemView.findViewById(R.id.textViewPrice);
                textViewDate = itemView.findViewById(R.id.textViewDate);
                textViewComment = itemView.findViewById(R.id.textViewComment);
                cardComment = itemView.findViewById(R.id.cardComment);
                buttonSelect = itemView.findViewById(R.id.buttonSelect);
                buttonPublishComment = itemView.findViewById(R.id.buttonPublishComment);
                editTextComment = itemView.findViewById(R.id.editTextComment);
                layoutAddComment = itemView.findViewById(R.id.layoutAddComment);
                recyclerViewComments = itemView.findViewById(R.id.recyclerViewComments);
                textViewNoComments = itemView.findViewById(R.id.textViewNoComments);

                android.util.Log.d("MenuActivity", "MenuViewHolder créé - buttonPublishComment trouvé: " + (buttonPublishComment != null));

                // Configurer le RecyclerView pour les commentaires
                if (recyclerViewComments != null) {
                    recyclerViewComments.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
                    recyclerViewComments.setNestedScrollingEnabled(false); // Important pour éviter les conflits
                    commentAdapter = new CommentAdapter(comments);
                    recyclerViewComments.setAdapter(commentAdapter);
                }
            }

            public void bind(Menu menu, int position, SessionManager sessionManager) {
                textViewMenuName.setText(menu.getName() != null ? menu.getName() : "Menu sans nom");
                textViewAppetizer.setText("Entrée: " + (menu.getAppetizer() != null ? menu.getAppetizer() : "Non spécifié"));
                textViewMainCourse.setText("Plat: " + (menu.getMainCourse() != null ? menu.getMainCourse() : "Non spécifié"));
                textViewDessert.setText("Dessert: " + (menu.getDessert() != null ? menu.getDessert() : "Non spécifié"));
                
                // Afficher le commentaire de l'administrateur s'il existe
                if (menu.getComment() != null && !menu.getComment().trim().isEmpty()) {
                    textViewComment.setText(menu.getComment());
                    if (cardComment != null) {
                        cardComment.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (cardComment != null) {
                        cardComment.setVisibility(View.GONE);
                    }
                }
                
                textViewPrice.setText("200 millimes");
                
                if (menu.getDate() != null && !menu.getDate().isEmpty()) {
                    try {
                        String dateStr = menu.getDate();
                        // Parser la date ISO (ex: "2024-01-15T10:30:00.000Z" ou "2024-01-15T10:30:00Z")
                        java.text.SimpleDateFormat inputFormat;
                        if (dateStr.contains(".")) {
                            inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault());
                        } else {
                            inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault());
                        }
                        java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                        java.util.Date date = inputFormat.parse(dateStr);
                        if (date != null) {
                            textViewDate.setText(outputFormat.format(date));
                        } else {
                            textViewDate.setText(dateStr);
                        }
                    } catch (Exception e) {
                        try {
                            java.text.SimpleDateFormat inputFormat2 = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                            java.text.SimpleDateFormat outputFormat2 = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                            java.util.Date date2 = inputFormat2.parse(menu.getDate());
                            if (date2 != null) {
                                textViewDate.setText(outputFormat2.format(date2));
                            } else {
                                textViewDate.setText(menu.getDate());
                            }
                        } catch (Exception e2) {
                            textViewDate.setText(menu.getDate());
                        }
                    }
                } else {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                    textViewDate.setText(sdf.format(new java.util.Date()));
                }

                // Charger et afficher les commentaires pour ce menu
                if (menu.getId() != null) {
                    List<StudentComment> menuComments = commentsMap.get(menu.getId());
                    if (menuComments != null && !menuComments.isEmpty()) {
                        comments.clear();
                        comments.addAll(menuComments);
                        if (commentAdapter != null) {
                            commentAdapter.updateComments(comments);
                        }
                        if (recyclerViewComments != null) {
                            recyclerViewComments.setVisibility(View.VISIBLE);
                        }
                        if (textViewNoComments != null) {
                            textViewNoComments.setVisibility(View.GONE);
                        }
                    } else {
                        comments.clear();
                        if (commentAdapter != null) {
                            commentAdapter.updateComments(comments);
                        }
                        if (recyclerViewComments != null) {
                            recyclerViewComments.setVisibility(View.GONE);
                        }
                        if (textViewNoComments != null) {
                            textViewNoComments.setVisibility(View.VISIBLE);
                        }
                    }
                }

                // Section pour ajouter un commentaire (uniquement pour les étudiants, pas pour les admins)
                if (layoutAddComment != null) {
                    android.util.Log.d("MenuActivity", "✅ layoutAddComment trouvé pour menu: " + (menu != null ? menu.getName() : "null"));
                    
                    // Vérifier si l'utilisateur est un admin - si oui, cacher la section
                    boolean isAdmin = sessionManager != null && sessionManager.isAdmin();
                    android.util.Log.d("MenuActivity", "Utilisateur admin: " + isAdmin);
                    
                    if (isAdmin) {
                        layoutAddComment.setVisibility(View.GONE);
                        android.util.Log.d("MenuActivity", "🔒 Section commentaire cachée (utilisateur admin)");
                    } else {
                        final String finalMenuId = menu != null ? menu.getId() : null;
                        final int finalPosition = position;
                        
                        android.util.Log.d("MenuActivity", "MenuId: " + finalMenuId + ", Position: " + finalPosition);
                        
                        // Vérifier que le menuId est valide avant de configurer la section
                        if (finalMenuId == null || finalMenuId.isEmpty()) {
                            android.util.Log.e("MenuActivity", "❌ menuId invalide pour le menu: " + (menu != null ? menu.getName() : "null"));
                            layoutAddComment.setVisibility(View.GONE);
                        } else {
                            // Afficher la section
                            layoutAddComment.setVisibility(View.VISIBLE);
                            android.util.Log.d("MenuActivity", "✅ Section commentaire visible pour étudiant");
                            
                            // Réinitialiser le champ de texte
                            if (editTextComment != null) {
                                editTextComment.setText("");
                                editTextComment.setEnabled(true);
                                editTextComment.setClickable(true);
                                editTextComment.setFocusable(true);
                                editTextComment.setFocusableInTouchMode(true);
                                android.util.Log.d("MenuActivity", "✅ editTextComment configuré - Enabled: " + editTextComment.isEnabled() + ", Focusable: " + editTextComment.isFocusable());
                                
                                // Empêcher le RecyclerView d'intercepter les touches
                                editTextComment.setOnTouchListener(new View.OnTouchListener() {
                                    @Override
                                    public boolean onTouch(View v, MotionEvent event) {
                                        android.util.Log.d("MenuActivity", "🔘 Touch sur editTextComment - Action: " + event.getAction());
                                        // Empêcher le parent RecyclerView d'intercepter les touches
                                        ViewParent parent = v.getParent();
                                        while (parent != null) {
                                            if (parent instanceof RecyclerView) {
                                                parent.requestDisallowInterceptTouchEvent(true);
                                                android.util.Log.d("MenuActivity", "✅ RecyclerView intercept désactivé");
                                                break;
                                            }
                                            parent = parent.getParent();
                                        }
                                        return false; // Laisser le champ gérer normalement
                                    }
                                });
                            } else {
                                android.util.Log.e("MenuActivity", "❌ editTextComment est null!");
                            }
                            
                            // Configurer le bouton Publier
                            if (buttonPublishComment != null) {
                                buttonPublishComment.setEnabled(true);
                                buttonPublishComment.setClickable(true);
                                buttonPublishComment.setFocusable(true);
                                buttonPublishComment.setFocusableInTouchMode(true);
                                android.util.Log.d("MenuActivity", "✅ buttonPublishComment configuré - Enabled: " + buttonPublishComment.isEnabled() + ", Clickable: " + buttonPublishComment.isClickable());
                                
                                // Supprimer tous les anciens listeners
                                buttonPublishComment.setOnClickListener(null);
                                buttonPublishComment.setOnTouchListener(null);
                                
                                // Obtenir l'activité directement depuis le contexte
                                android.content.Context context = itemView.getContext();
                                MenuActivity activity = null;
                                if (context instanceof MenuActivity) {
                                    activity = (MenuActivity) context;
                                } else if (context instanceof android.content.ContextWrapper) {
                                    android.content.Context baseContext = ((android.content.ContextWrapper) context).getBaseContext();
                                    if (baseContext instanceof MenuActivity) {
                                        activity = (MenuActivity) baseContext;
                                    }
                                }
                                
                                final MenuActivity finalActivity = activity;
                                
                                View.OnClickListener publishListener = new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        android.util.Log.d("MenuActivity", "🔘🔘🔘 BOUTON PUBLIER CLIQUE! menuId: " + finalMenuId + ", Position: " + finalPosition);
                                        
                                        // Vérifier que le menuId est valide
                                        if (finalMenuId == null || finalMenuId.isEmpty()) {
                                            android.util.Log.e("MenuActivity", "❌ menuId invalide ou vide");
                                            Toast.makeText(v.getContext(), "Erreur: ID du menu invalide", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        
                                        // Récupérer le texte du commentaire
                                        String commentText = "";
                                        if (editTextComment != null) {
                                            commentText = editTextComment.getText().toString().trim();
                                        }
                                        
                                        if (commentText.isEmpty()) {
                                            Toast.makeText(v.getContext(), "Veuillez saisir un commentaire", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        
                                        // Publier le commentaire directement
                                        if (finalActivity != null) {
                                            finalActivity.publishComment(finalMenuId, commentText, finalPosition);
                                        } else {
                                            android.util.Log.e("MenuActivity", "❌ Impossible d'obtenir l'activité");
                                            Toast.makeText(v.getContext(), "Erreur: Impossible de publier le commentaire", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                };
                                
                                // Attacher le listener de clic
                                buttonPublishComment.setOnClickListener(publishListener);
                                
                                // Ajouter un OnTouchListener pour capturer les touches même si le RecyclerView les intercepte
                                buttonPublishComment.setOnTouchListener(new View.OnTouchListener() {
                                    @Override
                                    public boolean onTouch(View v, MotionEvent event) {
                                        // Empêcher le parent RecyclerView d'intercepter les touches
                                        ViewParent parent = v.getParent();
                                        while (parent != null) {
                                            if (parent instanceof RecyclerView) {
                                                parent.requestDisallowInterceptTouchEvent(true);
                                                break;
                                            }
                                            parent = parent.getParent();
                                        }
                                        
                                        // Si c'est un ACTION_UP (relâchement), déclencher le clic
                                        if (event.getAction() == MotionEvent.ACTION_UP) {
                                            android.util.Log.d("MenuActivity", "🔘 Touch ACTION_UP sur bouton Publier");
                                            v.performClick();
                                            return true;
                                        }
                                        
                                        return false;
                                    }
                                });
                                
                                android.util.Log.d("MenuActivity", "✅ Bouton Publier configuré pour menu: " + menu.getName());
                            }
                        }
                    }
                } else {
                    android.util.Log.e("MenuActivity", "❌ layoutAddComment est null!");
                }

                buttonSelect.setOnClickListener(v -> {
                    // Ajouter le menu au panier
                    if (orderManager != null && menu != null) {
                        orderManager.addItemToOrder(
                            menu.getId(),
                            menu.getName(),
                            menu.getPrice(),
                            1,
                            menu.getDate() != null ? menu.getDate() : ""
                        );
                        Toast.makeText(v.getContext(), 
                            menu.getName() + " ajouté au panier", 
                            Toast.LENGTH_SHORT).show();
                        android.util.Log.d("MenuActivity", "Menu ajouté au panier: " + menu.getName());
                    }
                });
            }
        }
    }
}