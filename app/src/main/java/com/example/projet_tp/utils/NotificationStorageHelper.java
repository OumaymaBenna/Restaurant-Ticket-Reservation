package com.example.projet_tp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.projet_tp.model.NotificationItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationStorageHelper {
    private static final String PREFS_NAME = "notifications_prefs";
    private static final String KEY_NOTIFICATIONS = "notifications_list";
    private static final int MAX_NOTIFICATIONS = 100;

    public static void saveNotification(Context context, NotificationItem notification) {
        if (context == null || notification == null) {
            Log.e("NotificationStorageHelper", "Context ou notification est null");
            return;
        }

        try {
            List<NotificationItem> notifications = getAllNotifications(context);
            
            notifications.add(0, notification);
            
            if (notifications.size() > MAX_NOTIFICATIONS) {
                notifications = notifications.subList(0, MAX_NOTIFICATIONS);
            }
            
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            
            Gson gson = new Gson();
            String json = gson.toJson(notifications);
            editor.putString(KEY_NOTIFICATIONS, json);
            editor.apply();
            
            Log.d("NotificationStorageHelper", "Notification sauvegardée: " + notification.getTitle());
        } catch (Exception e) {
            Log.e("NotificationStorageHelper", "Erreur lors de la sauvegarde de la notification", e);
        }
    }

    public static List<NotificationItem> getAllNotifications(Context context) {
        if (context == null) {
            Log.w("NotificationStorageHelper", "Context est null");
            return new ArrayList<>();
        }

        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            if (prefs == null) {
                Log.w("NotificationStorageHelper", "SharedPreferences est null");
                return new ArrayList<>();
            }
            
            String json = prefs.getString(KEY_NOTIFICATIONS, null);
            
            if (json == null || json.isEmpty()) {
                Log.d("NotificationStorageHelper", "Aucune notification sauvegardée");
                return new ArrayList<>();
            }
            
            Gson gson = new Gson();
            if (gson == null) {
                Log.e("NotificationStorageHelper", "Gson est null");
                return new ArrayList<>();
            }
            
            Type type = new TypeToken<List<NotificationItem>>(){}.getType();
            List<NotificationItem> notifications = gson.fromJson(json, type);
            
            if (notifications == null) {
                Log.w("NotificationStorageHelper", "Liste de notifications désérialisée est null");
                return new ArrayList<>();
            }
            
            try {
                Collections.sort(notifications, (n1, n2) -> {
                    if (n1 == null || n2 == null) return 0;
                    return Long.compare(n2.getTimestamp(), n1.getTimestamp());
                });
            } catch (Exception e) {
                Log.e("NotificationStorageHelper", "Erreur lors du tri", e);
            }
            
            Log.d("NotificationStorageHelper", "Notifications récupérées: " + notifications.size());
            return notifications;
        } catch (Exception e) {
            Log.e("NotificationStorageHelper", "Erreur lors de la récupération des notifications", e);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static int getUnreadCount(Context context) {
        List<NotificationItem> notifications = getAllNotifications(context);
        int count = 0;
        for (NotificationItem notification : notifications) {
            if (!notification.isRead()) {
                count++;
            }
        }
        return count;
    }

    public static void markAsRead(Context context, String notificationId) {
        if (context == null || notificationId == null) {
            return;
        }

        try {
            List<NotificationItem> notifications = getAllNotifications(context);
            for (NotificationItem notification : notifications) {
                if (notification.getId() != null && notification.getId().equals(notificationId)) {
                    notification.setRead(true);
                    break;
                }
            }
            
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            
            Gson gson = new Gson();
            String json = gson.toJson(notifications);
            editor.putString(KEY_NOTIFICATIONS, json);
            editor.apply();
        } catch (Exception e) {
            Log.e("NotificationStorageHelper", "Erreur lors du marquage comme lu", e);
        }
    }

    public static void markAllAsRead(Context context) {
        if (context == null) {
            return;
        }

        try {
            List<NotificationItem> notifications = getAllNotifications(context);
            for (NotificationItem notification : notifications) {
                notification.setRead(true);
            }
            
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            
            Gson gson = new Gson();
            String json = gson.toJson(notifications);
            editor.putString(KEY_NOTIFICATIONS, json);
            editor.apply();
        } catch (Exception e) {
            Log.e("NotificationStorageHelper", "Erreur lors du marquage de toutes comme lues", e);
        }
    }

    public static void deleteNotification(Context context, String notificationId) {
        if (context == null || notificationId == null) {
            return;
        }

        try {
            List<NotificationItem> notifications = getAllNotifications(context);
            notifications.removeIf(notification -> 
                notification.getId() != null && notification.getId().equals(notificationId)
            );

            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            
            Gson gson = new Gson();
            String json = gson.toJson(notifications);
            editor.putString(KEY_NOTIFICATIONS, json);
            editor.apply();
        } catch (Exception e) {
            Log.e("NotificationStorageHelper", "Erreur lors de la suppression de la notification", e);
        }
    }

    public static void clearAllNotifications(Context context) {
        if (context == null) {
            return;
        }

        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(KEY_NOTIFICATIONS);
            editor.apply();
            Log.d("NotificationStorageHelper", "Toutes les notifications ont été supprimées");
        } catch (Exception e) {
            Log.e("NotificationStorageHelper", "Erreur lors de la suppression de toutes les notifications", e);
        }
    }
}

