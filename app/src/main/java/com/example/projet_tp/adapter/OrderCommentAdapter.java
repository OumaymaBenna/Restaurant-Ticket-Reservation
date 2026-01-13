package com.example.projet_tp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet_tp.R;
import com.example.projet_tp.model.OrderComment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderCommentAdapter extends RecyclerView.Adapter<OrderCommentAdapter.OrderCommentViewHolder> {

    private List<OrderComment> orders;
    private OnUserNameClickListener onUserNameClickListener;

    public interface OnUserNameClickListener {
        void onUserNameClick(String studentId, String userName);
    }

    public OrderCommentAdapter(List<OrderComment> orders) {
        this.orders = orders;
    }

    public void setOnUserNameClickListener(OnUserNameClickListener listener) {
        this.onUserNameClickListener = listener;
    }

    public void updateOrders(List<OrderComment> newOrders) {
        this.orders = newOrders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderCommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_comment, parent, false);
        if (view == null) {
            android.util.Log.e("OrderCommentAdapter", "View est null après inflation!");
            // Créer un layout simple comme fallback
            android.widget.LinearLayout fallbackView = new android.widget.LinearLayout(parent.getContext());
            fallbackView.setOrientation(android.widget.LinearLayout.VERTICAL);
            fallbackView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            TextView textView = new TextView(parent.getContext());
            textView.setText("Erreur de chargement du layout");
            textView.setPadding(16, 16, 16, 16);
            textView.setTextColor(0xFFFFFFFF);
            fallbackView.addView(textView);
            view = fallbackView;
        }
        return new OrderCommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderCommentViewHolder holder, int position) {
        OrderComment order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders != null ? orders.size() : 0;
    }

    class OrderCommentViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewUserName;
        private TextView textViewMealType;
        private TextView textViewDate;
        private TextView textViewComment;

        public OrderCommentViewHolder(@NonNull View itemView) {
            super(itemView);
            try {
                // Vérifier si c'est un LinearLayout (fallback) ou le layout normal
                if (itemView instanceof android.widget.LinearLayout && itemView.findViewById(R.id.textViewUserName) == null) {
                    // C'est le fallback, ne pas essayer de trouver les vues
                    android.util.Log.w("OrderCommentAdapter", "Utilisation du layout fallback");
                    return;
                }
                
                textViewUserName = itemView.findViewById(R.id.textViewUserName);
                textViewMealType = itemView.findViewById(R.id.textViewMealType);
                textViewDate = itemView.findViewById(R.id.textViewDate);
                textViewComment = itemView.findViewById(R.id.textViewComment);
                
                // Vérifier que toutes les vues sont trouvées
                if (textViewUserName == null) {
                    android.util.Log.e("OrderCommentAdapter", "textViewUserName est null!");
                }
                if (textViewMealType == null) {
                    android.util.Log.e("OrderCommentAdapter", "textViewMealType est null!");
                }
                if (textViewDate == null) {
                    android.util.Log.e("OrderCommentAdapter", "textViewDate est null!");
                }
                if (textViewComment == null) {
                    android.util.Log.e("OrderCommentAdapter", "textViewComment est null!");
                }
            } catch (Exception e) {
                android.util.Log.e("OrderCommentAdapter", "Erreur lors de l'initialisation du ViewHolder", e);
            }
        }

        public void bind(OrderComment order) {
            try {
                // Si c'est le layout fallback, ne rien faire
                if (textViewUserName == null && textViewMealType == null && textViewDate == null && textViewComment == null) {
                    return;
                }
                
                if (order == null) {
                    android.util.Log.e("OrderCommentAdapter", "OrderComment est null!");
                    return;
                }
                
                if (textViewUserName != null) {
                    textViewUserName.setText(order.getUserName() != null ? order.getUserName() : "Étudiant");
                    
                    // Rendre le nom cliquable
                    textViewUserName.setOnClickListener(v -> {
                        try {
                            if (onUserNameClickListener != null && order != null && order.getStudentId() != null && !order.getStudentId().isEmpty()) {
                                onUserNameClickListener.onUserNameClick(order.getStudentId(), order.getUserName());
                            } else {
                                android.util.Log.w("OrderCommentAdapter", "Impossible d'ouvrir les détails: studentId est null ou vide");
                            }
                        } catch (Exception e) {
                            android.util.Log.e("OrderCommentAdapter", "Erreur lors du clic sur le nom", e);
                        }
                    });
                    
                    // Style pour indiquer que c'est cliquable
                    textViewUserName.setClickable(true);
                    textViewUserName.setFocusable(true);
                    textViewUserName.setTextColor(0xFFFF6B35); // Couleur orange pour indiquer que c'est cliquable
                }
                if (textViewMealType != null) {
                    textViewMealType.setText(order.getMealType() != null ? order.getMealType() : "");
                }
                if (textViewComment != null) {
                    textViewComment.setText(order.getComment() != null ? order.getComment() : "");
                }

                // Formater la date
                if (textViewDate != null) {
                    if (order.getCreatedAt() != null && !order.getCreatedAt().isEmpty()) {
                        try {
                            String dateStr = order.getCreatedAt();
                            SimpleDateFormat inputFormat;
                            
                            if (dateStr.contains("T")) {
                                if (dateStr.contains(".")) {
                                    inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                                } else {
                                    inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                                }
                            } else {
                                inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            }
                            
                            Date date = inputFormat.parse(dateStr);
                            if (date != null) {
                                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                                textViewDate.setText(outputFormat.format(date));
                            } else {
                                textViewDate.setText(dateStr);
                            }
                        } catch (ParseException e) {
                            textViewDate.setText(order.getCreatedAt());
                        }
                    } else {
                        textViewDate.setText("");
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("OrderCommentAdapter", "Erreur lors du bind", e);
            }
        }
    }
}

