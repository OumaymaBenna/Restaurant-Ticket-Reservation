package com.example.projet_tp.adapter;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet_tp.R;
import com.example.projet_tp.api.RetrofitClient;
import com.example.projet_tp.model.Menu;
import com.example.projet_tp.model.StudentComment;
import com.example.projet_tp.model.CommentResponse;
import com.example.projet_tp.ui.admin.ManageMenusActivity;
import com.example.projet_tp.network.ApiService;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuAdminAdapter extends RecyclerView.Adapter<MenuAdminAdapter.MenuViewHolder> {

    private List<Menu> menuList;
    private ManageMenusActivity activity;
    private ApiService apiService;

    public MenuAdminAdapter(List<Menu> menuList, ManageMenusActivity activity) {
        this.menuList = menuList;
        this.activity = activity;
        this.apiService = RetrofitClient.getApiService();
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_menu_admin, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        Menu menu = menuList.get(position);
        holder.bind(menu);
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    class MenuViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewMenuName;
        private TextView textViewAppetizer;
        private TextView textViewMainCourse;
        private TextView textViewDessert;
        private TextView textViewAdminComment;
        private TextView textViewStatus;
        private TextView textViewNoComments;
        private RecyclerView recyclerViewComments;
        private CommentAdapter commentAdapter;
        private List<StudentComment> comments = new ArrayList<>();
        private MaterialButton buttonEdit;
        private MaterialButton buttonDelete;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMenuName = itemView.findViewById(R.id.textViewMenuName);
            textViewAppetizer = itemView.findViewById(R.id.textViewAppetizer);
            textViewMainCourse = itemView.findViewById(R.id.textViewMainCourse);
            textViewDessert = itemView.findViewById(R.id.textViewDessert);
            textViewAdminComment = itemView.findViewById(R.id.textViewAdminComment);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            textViewNoComments = itemView.findViewById(R.id.textViewNoComments);
            recyclerViewComments = itemView.findViewById(R.id.recyclerViewComments);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);

            // Configurer le RecyclerView pour les commentaires
            if (recyclerViewComments != null) {
                recyclerViewComments.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
                commentAdapter = new CommentAdapter(comments);
                recyclerViewComments.setAdapter(commentAdapter);
            }
        }

        public void bind(Menu menu) {
            textViewMenuName.setText(menu.getName() != null ? menu.getName() : "Menu sans nom");
            
            textViewAppetizer.setText("Entrée: " + (menu.getAppetizer() != null ? menu.getAppetizer() : "Non spécifié"));
            textViewMainCourse.setText("Plat: " + (menu.getMainCourse() != null ? menu.getMainCourse() : "Non spécifié"));
            textViewDessert.setText("Dessert: " + (menu.getDessert() != null ? menu.getDessert() : "Non spécifié"));
            
            // Afficher le commentaire de l'administrateur s'il existe
            if (menu.getComment() != null && !menu.getComment().trim().isEmpty()) {
                if (textViewAdminComment != null) {
                    textViewAdminComment.setText("💬 Commentaire admin: " + menu.getComment());
                    textViewAdminComment.setVisibility(View.VISIBLE);
                }
            } else {
                if (textViewAdminComment != null) {
                    textViewAdminComment.setVisibility(View.GONE);
                }
            }
            
            // Charger les commentaires des étudiants
            if (menu.getId() != null) {
                loadComments(menu.getId());
            }
            
            if (menu.isAvailable()) {
                textViewStatus.setText("Disponible");
                textViewStatus.setTextColor(0xFFFFFFFF); // Blanc pour meilleure visibilité
            } else {
                textViewStatus.setText("Indisponible");
                textViewStatus.setTextColor(0xFFFF5252); // Rouge clair
            }

            buttonEdit.setVisibility(View.GONE);
            buttonDelete.setVisibility(View.GONE);

            itemView.setOnClickListener(v -> {
                showContextMenu(v, menu);
            });
        }

        private void loadComments(String menuId) {
            if (apiService == null || recyclerViewComments == null) {
                return;
            }

            apiService.getComments(menuId).enqueue(new Callback<CommentResponse>() {
                @Override
                public void onResponse(Call<CommentResponse> call, Response<CommentResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        CommentResponse commentResponse = response.body();
                        if (commentResponse.getComments() != null && !commentResponse.getComments().isEmpty()) {
                            comments.clear();
                            comments.addAll(commentResponse.getComments());
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
                }

                @Override
                public void onFailure(Call<CommentResponse> call, Throwable t) {
                    android.util.Log.e("MenuAdminAdapter", "Erreur lors du chargement des commentaires: " + t.getMessage());
                }
            });
        }

        private void showContextMenu(View view, Menu menu) {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            popupMenu.getMenuInflater().inflate(R.menu.menu_context_menu, popupMenu.getMenu());
            
            try {
                android.view.MenuItem editItem = popupMenu.getMenu().findItem(R.id.action_edit);
                android.view.MenuItem deleteItem = popupMenu.getMenu().findItem(R.id.action_delete);
                
                if (editItem != null) {
                    editItem.setTitle("✏️ Modifier");
                }
                if (deleteItem != null) {
                    deleteItem.setTitle("🗑️ Supprimer");
                }
            } catch (Exception e) {
                android.util.Log.e("MenuAdminAdapter", "Erreur lors de la personnalisation du menu", e);
            }
            
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_edit) {
                    if (activity != null) {
                        activity.showEditMenuDialog(menu);
                    }
                    return true;
                } else if (item.getItemId() == R.id.action_delete) {
                    if (activity != null) {
                        activity.showDeleteConfirmationDialog(menu);
                    }
                    return true;
                }
                return false;
            });
            
            popupMenu.show();
        }
    }
}

