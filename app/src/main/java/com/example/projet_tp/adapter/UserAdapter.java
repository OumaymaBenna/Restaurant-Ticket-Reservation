package com.example.projet_tp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet_tp.R;
import com.example.projet_tp.model.User;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private OnUserActionListener listener;

    public interface OnUserActionListener {
        void onUserClick(User user);
        void onBlockClick(User user);
    }

    public UserAdapter(List<User> userList, OnUserActionListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    public void updateUserList(List<User> newList) {
        this.userList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardUser;
        private TextView textViewUserName;
        private TextView textViewUserEmail;
        private TextView textViewUserRole;
        private TextView textViewUserBalance;
        private TextView textViewBlockedStatus;
        private MaterialButton buttonBlock;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            cardUser = itemView.findViewById(R.id.cardUser);
            textViewUserName = itemView.findViewById(R.id.textViewUserName);
            textViewUserEmail = itemView.findViewById(R.id.textViewUserEmail);
            textViewUserRole = itemView.findViewById(R.id.textViewUserRole);
            textViewUserBalance = itemView.findViewById(R.id.textViewUserBalance);
            textViewBlockedStatus = itemView.findViewById(R.id.textViewBlockedStatus);
            buttonBlock = itemView.findViewById(R.id.buttonBlock);
        }

        public void bind(User user) {
            textViewUserName.setText(user.getFullName() != null ? user.getFullName() : "Nom inconnu");
            textViewUserEmail.setText(user.getEmail() != null ? user.getEmail() : "Email inconnu");
            
            String role = user.getRole() != null && user.getRole().equals("admin") ? "Administrateur" : "Étudiant";
            textViewUserRole.setText(role);
            
            double balance = user.getSubscriptionBalance();
            textViewUserBalance.setText("Solde: " + String.format("%.3f", balance) + " DNT");

            if (user.getRole() != null && user.getRole().equals("admin")) {
                textViewUserRole.setTextColor(0xFFFF6B35); // Orange pour admin
            } else {
                textViewUserRole.setTextColor(0xFF4A8A93); // Bleu pour étudiant
            }

            cardUser.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUserClick(user);
                }
            });

            if (user.isBlocked()) {
                textViewBlockedStatus.setVisibility(View.VISIBLE);
                textViewBlockedStatus.setText("🔒 Bloqué");
                textViewBlockedStatus.setTextColor(0xFFFF6B35);
                buttonBlock.setText("Débloquer");
                buttonBlock.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF4CAF50));
            } else {
                textViewBlockedStatus.setVisibility(View.GONE);
                buttonBlock.setText("Bloquer");
                buttonBlock.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFF6B35));
            }

            buttonBlock.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBlockClick(user);
                }
            });
        }
    }
}

