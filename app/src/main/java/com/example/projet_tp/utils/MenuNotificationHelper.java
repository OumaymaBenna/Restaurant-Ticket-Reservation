package com.example.projet_tp.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.projet_tp.R;
import com.example.projet_tp.model.Menu;
import com.example.projet_tp.model.NotificationItem;
import com.example.projet_tp.ui.main.MenuActivity;

public class MenuNotificationHelper {
    
    private static final String CHANNEL_ID = "menu_notifications";
    private static final String CHANNEL_NAME = "Notifications de Menus";
    private static final int NOTIFICATION_ID = 1001;

    public static void createNotificationChannel(Context context) {
        if (context == null) {
            Log.e("MenuNotificationHelper", "Context est null lors de la création du canal");
            return;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications pour les nouveaux menus disponibles");
            channel.enableVibration(true);
            channel.enableLights(true);
            channel.setShowBadge(true);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d("MenuNotificationHelper", "Canal de notification créé");
            } else {
                Log.e("MenuNotificationHelper", "NotificationManager est null");
            }
        }
    }

    public static void showNewMenuNotification(Context context, Menu menu) {
        if (context == null) {
            Log.e("MenuNotificationHelper", "Context est null, impossible d'envoyer la notification");
            return;
        }
        
        if (menu == null) {
            Log.e("MenuNotificationHelper", "Menu est null, impossible d'envoyer la notification");
            return;
        }
        
        try {
            Context appContext = context.getApplicationContext();
            createNotificationChannel(appContext);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                NotificationManager notificationManager = appContext.getSystemService(NotificationManager.class);
                if (notificationManager != null && !notificationManager.areNotificationsEnabled()) {
                    Log.w("MenuNotificationHelper", "Les notifications ne sont pas activées pour cette application");
                }
            }

            Intent intent = new Intent(appContext, MenuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }
            
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    appContext,
                    (int) System.currentTimeMillis(),
                    intent,
                    flags
            );

            String menuName = menu.getName() != null ? menu.getName() : "Nouveau menu";
            String message = "L'administrateur a ajouté le menu du jour : " + menuName;
            String bigText = "🍽️ Nouveau Menu du Jour\n\n" +
                    "L'administrateur vient d'ajouter un nouveau menu disponible.\n\n" +
                    "Menu: " + menuName + "\n\n" +
                    "Entrée: " + (menu.getAppetizer() != null ? menu.getAppetizer() : "Non spécifié") + "\n" +
                    "Plat: " + (menu.getMainCourse() != null ? menu.getMainCourse() : "Non spécifié") + "\n" +
                    "Dessert: " + (menu.getDessert() != null ? menu.getDessert() : "Non spécifié") + "\n\n" +
                    "Cliquez pour voir le menu complet.";

            NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("🍽️ Nouveau Menu du Jour")
                    .setContentText(message)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setVibrate(new long[]{0, 500, 250, 500})
                    .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_LIGHTS)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setShowWhen(true)
                    .setWhen(System.currentTimeMillis());

            NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (!notificationManager.areNotificationsEnabled()) {
                        Log.w("MenuNotificationHelper", "⚠️ Les notifications ne sont pas activées pour cette application");
                    }
                }
                
                int notificationId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE); // ID unique
                try {
                    notificationManager.notify(notificationId, builder.build());
                    Log.d("MenuNotificationHelper", "✅ Notification envoyée avec succès pour le menu: " + menuName + " (ID: " + notificationId + ")");
                    
                    NotificationItem notificationItem = new NotificationItem(
                            "🍽️ Nouveau Menu du Jour",
                            message,
                            menuName,
                            menu.getId() != null ? menu.getId() : ""
                    );
                    NotificationStorageHelper.saveNotification(appContext, notificationItem);
                    Log.d("MenuNotificationHelper", "✅ Notification sauvegardée dans le stockage local");
                } catch (SecurityException e) {
                    Log.e("MenuNotificationHelper", "❌ Erreur de sécurité: Permission refusée - " + e.getMessage());
                } catch (Exception e) {
                    Log.e("MenuNotificationHelper", "❌ Erreur lors de l'envoi: " + e.getMessage(), e);
                }
            } else {
                Log.e("MenuNotificationHelper", "❌ NotificationManager est null");
            }
        } catch (SecurityException e) {
            Log.e("MenuNotificationHelper", "❌ Erreur de sécurité lors de l'envoi de la notification: " + e.getMessage());
        } catch (Exception e) {
            Log.e("MenuNotificationHelper", "❌ Erreur lors de l'envoi de la notification", e);
            e.printStackTrace();
        }
    }
}

