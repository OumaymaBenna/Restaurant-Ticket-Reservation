package com.example.projet_tp.ui.main;

import android.app.Dialog;
import android.content.Context;
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
import com.example.projet_tp.model.StudentComment;
import com.example.projet_tp.network.ApiService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddCommentDialogFragment extends DialogFragment {

    private TextInputEditText editTextComment;
    private MaterialButton buttonSubmit;
    private MaterialButton buttonCancel;
    private ApiService apiService;
    private String menuId;
    private String studentId;
    private String userName;
    private OnCommentAddedListener listener;

    public interface OnCommentAddedListener {
        void onCommentAdded();
    }

    public static AddCommentDialogFragment newInstance(String menuId, String studentId, String userName, OnCommentAddedListener listener) {
        AddCommentDialogFragment fragment = new AddCommentDialogFragment();
        Bundle args = new Bundle();
        args.putString("menuId", menuId);
        args.putString("studentId", studentId);
        args.putString("userName", userName);
        fragment.setArguments(args);
        fragment.listener = listener;
        android.util.Log.d("AddCommentDialog", "newInstance créé - menuId: " + menuId + ", studentId: " + studentId);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = RetrofitClient.getApiService();
        
        // Récupérer les paramètres depuis le Bundle
        Bundle args = getArguments();
        if (args != null) {
            menuId = args.getString("menuId");
            studentId = args.getString("studentId");
            userName = args.getString("userName");
            android.util.Log.d("AddCommentDialog", "onCreate - menuId: " + menuId + ", studentId: " + studentId);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        android.util.Log.d("AddCommentDialog", "onCreateDialog appelé");
        
        Context context = getContext();
        if (context == null) {
            context = getActivity();
        }
        if (context == null) {
            android.util.Log.e("AddCommentDialog", "❌ Context est null!");
            return super.onCreateDialog(savedInstanceState);
        }

        // Vérifier les paramètres
        if (menuId == null || studentId == null || userName == null) {
            android.util.Log.e("AddCommentDialog", "❌ Paramètres manquants - menuId: " + menuId + ", studentId: " + studentId + ", userName: " + userName);
        }

        // Utiliser AlertDialog.Builder pour plus de compatibilité
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_add_comment, null);

        if (view == null) {
            android.util.Log.e("AddCommentDialog", "❌ View est null après inflation!");
            return super.onCreateDialog(savedInstanceState);
        }

        editTextComment = view.findViewById(R.id.editTextComment);
        buttonSubmit = view.findViewById(R.id.buttonSubmit);
        buttonCancel = view.findViewById(R.id.buttonCancel);

        android.util.Log.d("AddCommentDialog", "Vues trouvées - editTextComment: " + (editTextComment != null) + 
                ", buttonSubmit: " + (buttonSubmit != null) + ", buttonCancel: " + (buttonCancel != null));

        builder.setView(view);
        Dialog dialog = builder.create();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        if (buttonSubmit == null) {
            android.util.Log.e("AddCommentDialog", "❌ buttonSubmit est null!");
        } else {
            android.util.Log.d("AddCommentDialog", "✅ buttonSubmit trouvé");
            buttonSubmit.setEnabled(true);
            buttonSubmit.setClickable(true);
            buttonSubmit.setFocusable(true);
            
            // Ajouter le listener directement
            buttonSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    android.util.Log.d("AddCommentDialog", "🔘🔘🔘 Bouton Publier cliqué! (OnClickListener)");
                    submitComment();
                }
            });
        }

        if (buttonCancel != null) {
            buttonCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    android.util.Log.d("AddCommentDialog", "🔘 Bouton Annuler cliqué!");
                    dialog.dismiss();
                }
            });
        }

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            // Demander le focus sur le champ de texte
            window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        
        // Demander le focus sur le champ de texte après que le dialog soit affiché
        if (editTextComment != null) {
            editTextComment.post(new Runnable() {
                @Override
                public void run() {
                    editTextComment.requestFocus();
                    android.util.Log.d("AddCommentDialog", "✅ Focus demandé sur editTextComment");
                }
            });
        }

        android.util.Log.d("AddCommentDialog", "✅ Dialog créé avec succès");
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        android.util.Log.d("AddCommentDialog", "onStart appelé - Dialog visible");
        
        // Donner le focus au champ de texte pour que l'étudiant puisse directement taper
        if (editTextComment != null) {
            editTextComment.post(new Runnable() {
                @Override
                public void run() {
                    editTextComment.requestFocus();
                    // Ouvrir le clavier virtuel
                    android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) 
                            getContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(editTextComment, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                    }
                    android.util.Log.d("AddCommentDialog", "✅ Focus et clavier demandés dans onStart");
                }
            });
        }
        
        // Vérifier à nouveau que le bouton est bien configuré
        if (buttonSubmit != null) {
            android.util.Log.d("AddCommentDialog", "✅ buttonSubmit vérifié dans onStart");
            buttonSubmit.setEnabled(true);
            buttonSubmit.setClickable(true);
        } else {
            android.util.Log.e("AddCommentDialog", "❌ buttonSubmit est null dans onStart!");
            // Essayer de le récupérer
            Dialog dialog = getDialog();
            if (dialog != null) {
                buttonSubmit = dialog.findViewById(R.id.buttonSubmit);
                if (buttonSubmit != null) {
                    android.util.Log.d("AddCommentDialog", "✅ buttonSubmit récupéré dans onStart");
                    buttonSubmit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            android.util.Log.d("AddCommentDialog", "🔘🔘🔘 Bouton Publier cliqué! (onStart)");
                            submitComment();
                        }
                    });
                }
            }
        }
    }

    private void submitComment() {
        android.util.Log.d("AddCommentDialog", "📝 Début de submitComment()");
        
        if (menuId == null || menuId.isEmpty()) {
            android.util.Log.e("AddCommentDialog", "❌ menuId est null ou vide!");
            Context context = getContext();
            if (context != null) {
                Toast.makeText(context, "Erreur: ID du menu manquant", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (studentId == null || studentId.isEmpty()) {
            android.util.Log.e("AddCommentDialog", "❌ studentId est null ou vide!");
            Context context = getContext();
            if (context != null) {
                Toast.makeText(context, "Erreur: ID étudiant manquant", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        String commentText = editTextComment.getText().toString().trim();

        if (commentText.isEmpty()) {
            android.util.Log.w("AddCommentDialog", "⚠️ Commentaire vide");
            Context context = getContext();
            if (context != null) {
                Toast.makeText(context, "Veuillez saisir un commentaire", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // Désactiver le bouton pendant l'envoi
        if (buttonSubmit != null) {
            buttonSubmit.setEnabled(false);
            buttonSubmit.setText("Publication...");
        }

        StudentComment comment = new StudentComment();
        comment.setMenuId(menuId);
        comment.setStudentId(studentId);
        comment.setUserName(userName);
        comment.setComment(commentText);

        android.util.Log.d("AddCommentDialog", "Création du commentaire: menuId=" + menuId + ", studentId=" + studentId + ", userName=" + userName);

        if (apiService == null) {
            Context context = getContext();
            if (context != null) {
                Toast.makeText(context, "Erreur: Service API non initialisé", Toast.LENGTH_LONG).show();
            }
            if (buttonSubmit != null) {
                buttonSubmit.setEnabled(true);
                buttonSubmit.setText("Publier");
            }
            return;
        }

        android.util.Log.d("AddCommentDialog", "Envoi de la requête API - menuId: " + menuId);
        apiService.createComment(menuId, comment).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                android.util.Log.d("AddCommentDialog", "Réponse reçue - Code: " + response.code() + ", Succès: " + response.isSuccessful());
                
                // Réactiver le bouton
                if (buttonSubmit != null) {
                    buttonSubmit.setEnabled(true);
                    buttonSubmit.setText("Publier");
                }
                
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        ApiResponse apiResponse = response.body();
                        android.util.Log.d("AddCommentDialog", "Réponse body - success: " + apiResponse.isSuccess() + ", message: " + apiResponse.getMessage());
                        
                        if (apiResponse.isSuccess()) {
                            Context context = getContext();
                            if (context == null) {
                                context = getActivity();
                            }
                            if (context != null) {
                                Toast.makeText(context, "Commentaire ajouté avec succès", Toast.LENGTH_SHORT).show();
                            }
                            if (listener != null) {
                                android.util.Log.d("AddCommentDialog", "Appel du callback onCommentAdded");
                                listener.onCommentAdded();
                            }
                            dismiss();
                        } else {
                            Context context = getContext();
                            if (context == null) {
                                context = getActivity();
                            }
                            if (context != null) {
                                String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Erreur lors de l'ajout du commentaire";
                                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        android.util.Log.e("AddCommentDialog", "Réponse body est null");
                        Context context = getContext();
                        if (context == null) {
                            context = getActivity();
                        }
                        if (context != null) {
                            Toast.makeText(context, "Erreur: Réponse serveur vide", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    android.util.Log.e("AddCommentDialog", "Réponse non réussie - Code: " + response.code());
                    String errorMsg = "Erreur serveur (Code " + response.code() + ")";
                    if (response.errorBody() != null) {
                        try {
                            String errorString = response.errorBody().string();
                            android.util.Log.e("AddCommentDialog", "Erreur serveur: " + errorString);
                            if (errorString.length() > 0) {
                                errorMsg = "Erreur: " + errorString.substring(0, Math.min(100, errorString.length()));
                            }
                        } catch (Exception e) {
                            android.util.Log.e("AddCommentDialog", "Erreur lors de la lecture du body d'erreur", e);
                        }
                    }
                    Context context = getContext();
                    if (context == null) {
                        context = getActivity();
                    }
                    if (context != null) {
                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                android.util.Log.e("AddCommentDialog", "Erreur de connexion", t);
                if (t != null) {
                    android.util.Log.e("AddCommentDialog", "Message d'erreur: " + t.getMessage());
                    android.util.Log.e("AddCommentDialog", "Stack trace: ", t);
                }
                
                // Réactiver le bouton
                if (buttonSubmit != null) {
                    buttonSubmit.setEnabled(true);
                    buttonSubmit.setText("Publier");
                }
                
                Context context = getContext();
                if (context == null) {
                    context = getActivity();
                }
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
}

