package com.example.projet_tp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet_tp.R;
import com.example.projet_tp.model.NotificationItem;
import com.example.projet_tp.ui.main.NotificationsActivity;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<NotificationItem> notifications;
    private NotificationsActivity activity;

    public NotificationAdapter(List<NotificationItem> notifications, NotificationsActivity activity) {
        this.notifications = notifications;
        this.activity = activity;
    }

    public void updateNotifications(List<NotificationItem> newNotifications) {
        if (newNotifications == null) {
            this.notifications = new ArrayList<>();
        } else {
            this.notifications = newNotifications;
        }
        android.util.Log.d("NotificationAdapter", "Mise à jour de l'adaptateur avec " + this.notifications.size() + " notifications");
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification, parent, false);
            return new NotificationViewHolder(view, this);
        } catch (Exception e) {
            android.util.Log.e("NotificationAdapter", "Erreur lors de la création du ViewHolder", e);
            e.printStackTrace();
            View view = new View(parent.getContext());
            return new NotificationViewHolder(view, this);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem notification = notifications.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications != null ? notifications.size() : 0;
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardNotification;
        private TextView textViewTitle;
        private TextView textViewMessage;
        private TextView textViewTime;
        private View viewUnreadIndicator;
        private NotificationAdapter adapter;

        public NotificationViewHolder(@NonNull View itemView, NotificationAdapter adapter) {
            super(itemView);
            this.adapter = adapter;
            try {
                if (itemView instanceof MaterialCardView) {
                    cardNotification = (MaterialCardView) itemView;
                } else {
                    cardNotification = itemView.findViewById(R.id.cardNotification);
                    if (cardNotification == null) {
                        android.util.Log.w("NotificationAdapter", "MaterialCardView non trouvé, utilisation de itemView");
                    }
                }
                textViewTitle = itemView.findViewById(R.id.textViewTitle);
                textViewMessage = itemView.findViewById(R.id.textViewMessage);
                textViewTime = itemView.findViewById(R.id.textViewTime);
                viewUnreadIndicator = itemView.findViewById(R.id.viewUnreadIndicator);
            } catch (Exception e) {
                android.util.Log.e("NotificationAdapter", "Erreur dans NotificationViewHolder constructor", e);
                e.printStackTrace();
            }
        }

        public void bind(NotificationItem notification) {
            try {
                if (textViewTitle != null) {
                    textViewTitle.setText(notification.getTitle() != null ? notification.getTitle() : "Notification");
                }
                if (textViewMessage != null) {
                    textViewMessage.setText(notification.getMessage() != null ? notification.getMessage() : "");
                }
                
                if (textViewTime != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    String timeString = sdf.format(new Date(notification.getTimestamp()));
                    textViewTime.setText(timeString);
                }
                
                if (viewUnreadIndicator != null) {
                    if (notification.isRead()) {
                        viewUnreadIndicator.setVisibility(View.GONE);
                    } else {
                        viewUnreadIndicator.setVisibility(View.VISIBLE);
                    }
                }
                
                View cardView = cardNotification != null ? cardNotification : itemView;
                if (cardView != null) {
                    if (notification.isRead()) {
                        cardView.setAlpha(0.7f);
                        if (cardView instanceof com.google.android.material.card.MaterialCardView) {
                            ((com.google.android.material.card.MaterialCardView) cardView).setCardBackgroundColor(0xFF1A1A1A);
                            ((com.google.android.material.card.MaterialCardView) cardView).setStrokeColor(0xFF333333);
                        }
                    } else {
                        cardView.setAlpha(1.0f);
                        if (cardView instanceof com.google.android.material.card.MaterialCardView) {
                            ((com.google.android.material.card.MaterialCardView) cardView).setCardBackgroundColor(0xFF1A1A1A);
                            ((com.google.android.material.card.MaterialCardView) cardView).setStrokeColor(0xFFFF6B35);
                        }
                    }
                    
                    if (adapter != null && adapter.activity != null) {
                        cardView.setOnClickListener(v -> {
                            adapter.activity.onNotificationClick(notification);
                        });
                        
                        cardView.setOnLongClickListener(v -> {
                            adapter.activity.onNotificationDelete(notification);
                            return true;
                        });
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("NotificationAdapter", "Erreur dans bind", e);
                e.printStackTrace();
            }
        }
    }
}

