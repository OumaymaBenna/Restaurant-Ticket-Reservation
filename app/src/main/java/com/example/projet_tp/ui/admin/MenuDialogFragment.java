package com.example.projet_tp.ui.admin;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.projet_tp.R;
import com.example.projet_tp.api.RetrofitClient;
import com.example.projet_tp.model.ApiResponse;
import com.example.projet_tp.model.Menu;
import com.example.projet_tp.network.ApiService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuDialogFragment extends DialogFragment {

    private TextInputEditText editTextName;
    private TextInputEditText editTextAppetizer;
    private TextInputEditText editTextMainCourse;
    private TextInputEditText editTextDessert;
    private TextInputEditText editTextComment;
    private MaterialButton buttonSave;
    private MaterialButton buttonCancel;

    private Menu menu;
    private ManageMenusActivity activity;
    private ApiService apiService;
    private boolean isEditMode = false;

    public static MenuDialogFragment newInstance(Menu menu, ManageMenusActivity activity) {
        MenuDialogFragment fragment = new MenuDialogFragment();
        fragment.menu = menu;
        fragment.activity = activity;
        fragment.isEditMode = (menu != null);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = RetrofitClient.getApiService();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        android.content.Context context = getContext();
        if (context == null) {
            context = getActivity();
        }
        if (context == null) {
            return super.onCreateDialog(savedInstanceState);
        }
        
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_menu_form, null);
        
        editTextName = view.findViewById(R.id.editTextName);
        editTextAppetizer = view.findViewById(R.id.editTextAppetizer);
        editTextMainCourse = view.findViewById(R.id.editTextMainCourse);
        editTextDessert = view.findViewById(R.id.editTextDessert);
        editTextComment = view.findViewById(R.id.editTextComment);
        
        buttonSave = view.findViewById(R.id.buttonSave);
        buttonCancel = view.findViewById(R.id.buttonCancel);
        
        if (isEditMode && menu != null) {
            editTextName.setText(menu.getName());
            editTextAppetizer.setText(menu.getAppetizer());
            editTextMainCourse.setText(menu.getMainCourse());
            editTextDessert.setText(menu.getDessert());
            if (menu.getComment() != null) {
                editTextComment.setText(menu.getComment());
            }
        }

        if (buttonSave != null) {
            buttonSave.setOnClickListener(v -> saveMenu());
        }
        if (buttonCancel != null) {
            buttonCancel.setOnClickListener(v -> dismiss());
        }
        
        dialog.setContentView(view);
        
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        
        return dialog;
    }

    private void saveMenu() {
        String name = editTextName.getText().toString().trim();
        String appetizer = editTextAppetizer.getText().toString().trim();
        String mainCourse = editTextMainCourse.getText().toString().trim();
        String dessert = editTextDessert.getText().toString().trim();
        String comment = editTextComment.getText().toString().trim();

        if (name.isEmpty() || appetizer.isEmpty() || mainCourse.isEmpty() || dessert.isEmpty()) {
            android.content.Context context = getContext();
            if (context != null) {
                Toast.makeText(context, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        Menu menuToSave = new Menu();
        if (isEditMode && menu != null) {
            menuToSave.setId(menu.getId());
        }
        menuToSave.setName(name);
        menuToSave.setAppetizer(appetizer);
        menuToSave.setMainCourse(mainCourse);
        menuToSave.setDessert(dessert);
        menuToSave.setComment(comment);
        menuToSave.setDrink("");
        menuToSave.setPrice(0.0);
        menuToSave.setAvailable(true);

        if (isEditMode) {
            updateMenu(menuToSave);
        } else {
            createMenu(menuToSave);
        }
    }

    private void createMenu(Menu menu) {
        android.util.Log.d("MenuDialogFragment", "Création du menu: " + menu.getName());
        android.util.Log.d("MenuDialogFragment", "Données du menu: name=" + menu.getName() + 
                ", appetizer=" + menu.getAppetizer() + 
                ", mainCourse=" + menu.getMainCourse() + 
                ", dessert=" + menu.getDessert());
        android.util.Log.d("MenuDialogFragment", "URL de base: http://10.0.2.2:3000");
        android.util.Log.d("MenuDialogFragment", "Route: POST /menus");
        
        if (apiService == null) {
            android.util.Log.e("MenuDialogFragment", "apiService est null!");
            android.content.Context context = getContext();
            if (context != null) {
                Toast.makeText(context, "Erreur: Service API non initialisé", Toast.LENGTH_LONG).show();
            }
            return;
        }
        
        android.util.Log.d("MenuDialogFragment", "Appel API en cours...");
        apiService.createMenu(menu).enqueue(new Callback<Menu>() {
            @Override
            public void onResponse(Call<Menu> call, Response<Menu> response) {
                android.util.Log.d("MenuDialogFragment", "Réponse reçue - Code: " + response.code() + ", Succès: " + response.isSuccessful());
                
                if (response.isSuccessful() && response.body() != null) {
                    android.util.Log.d("MenuDialogFragment", "Menu créé avec succès: " + response.body().getName());
                    if (activity != null) {
                        activity.onMenuAdded(response.body());
                    }
                    
                    // Envoyer une notification aux étudiants - Utiliser l'activité parente
                    android.content.Context context = null;
                    if (activity != null) {
                        context = activity.getApplicationContext();
                    } else if (getActivity() != null) {
                        context = getActivity().getApplicationContext();
                    } else if (getContext() != null) {
                        context = getContext().getApplicationContext();
                    }
                    
                    if (context != null) {
                        android.util.Log.d("MenuDialogFragment", "📢 Envoi de la notification pour le menu: " + response.body().getName());
                        try {
                            com.example.projet_tp.utils.MenuNotificationHelper.showNewMenuNotification(context, response.body());
                            android.util.Log.d("MenuDialogFragment", "✅ Notification envoyée avec succès");
                        } catch (Exception e) {
                            android.util.Log.e("MenuDialogFragment", "❌ Erreur lors de l'envoi de la notification", e);
                        }
                    } else {
                        android.util.Log.e("MenuDialogFragment", "❌ Impossible d'envoyer la notification: tous les contextes sont null");
                    }
                    
                    dismiss();
                } else {
                    String errorMsg = "Erreur lors de la création du menu";
                    if (response.errorBody() != null) {
                        try {
                            String errorString = response.errorBody().string();
                            android.util.Log.e("MenuDialogFragment", "Erreur serveur (Code " + response.code() + "): " + errorString);
                            
                            if (errorString.contains("Route non trouvée") || errorString.contains("Route non")) {
                                errorMsg = "⚠️ Serveur non accessible!\nVérifiez que le serveur Node.js est démarré.\nURL: http://10.0.2.2:3000";
                            } else if (errorString.contains("\"message\"")) {
                                try {
                                    int messageStart = errorString.indexOf("\"message\"");
                                    if (messageStart >= 0) {
                                        int messageValueStart = errorString.indexOf(":", messageStart) + 1;
                                        while (messageValueStart < errorString.length() &&
                                               (errorString.charAt(messageValueStart) == ' ' || 
                                                errorString.charAt(messageValueStart) == '"')) {
                                            messageValueStart++;
                                        }
                                        int messageValueEnd = messageValueStart;
                                        if (messageValueStart < errorString.length() && errorString.charAt(messageValueStart - 1) == '"') {
                                            messageValueEnd = errorString.indexOf("\"", messageValueStart);
                                        } else {
                                            messageValueEnd = errorString.indexOf(",", messageValueStart);
                                            if (messageValueEnd < 0) messageValueEnd = errorString.indexOf("}", messageValueStart);
                                        }
                                        if (messageValueEnd > messageValueStart && messageValueEnd < errorString.length()) {
                                            errorMsg = errorString.substring(messageValueStart, messageValueEnd);
                                        } else {
                                            errorMsg = "Erreur serveur";
                                        }
                                    } else {
                                        errorMsg = "Erreur serveur: " + errorString.substring(0, Math.min(150, errorString.length()));
                                    }
                                } catch (Exception e) {
                                    android.util.Log.e("MenuDialogFragment", "Erreur lors du parsing JSON", e);
                                    errorMsg = "Erreur: " + errorString.substring(0, Math.min(150, errorString.length()));
                                }
                            } else {
                                errorMsg = "Erreur: " + errorString.substring(0, Math.min(150, errorString.length()));
                            }
                        } catch (Exception e) {
                            android.util.Log.e("MenuDialogFragment", "Erreur lors de la lecture du body d'erreur", e);
                            errorMsg = "Erreur serveur (Code " + response.code() + ")";
                        }
                    } else {
                        errorMsg = "Erreur serveur (Code " + response.code() + ")";
                    }
                    android.content.Context context = getContext();
                    if (context != null) {
                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Menu> call, Throwable t) {
                android.util.Log.e("MenuDialogFragment", "Erreur de connexion", t);
                android.content.Context context = getContext();
                if (context != null) {
                    String errorMsg = "Erreur de connexion";
                    if (t != null && t.getMessage() != null) {
                        errorMsg += ": " + t.getMessage();
                    }
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void updateMenu(Menu menu) {
        apiService.updateMenu(menu.getId(), menu).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    if (activity != null) {
                        activity.onMenuUpdated(menu);
                    }
                    dismiss();
                } else {
                    android.content.Context context = getContext();
                    if (context != null) {
                        Toast.makeText(context, "Erreur lors de la modification du menu", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                android.content.Context context = getContext();
                if (context != null) {
                    Toast.makeText(context, "Erreur de connexion: " + (t != null ? t.getMessage() : "Erreur inconnue"), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

