package com.example.projet_tp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.example.projet_tp.model.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SessionManager {

    private static final String TAG = "SessionManager";
    private static final String PREF_NAME = "session";
    private static final String PROFILE_IMAGE_FILE = "profile_image.jpg";
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;
    private final Context context;

    public SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }


    public void saveUser(String id, String name, String email, String university, String phone) {
        editor.putString("id", id);
        editor.putString("name", name);
        editor.putString("email", email);
        editor.putString("university", university);
        editor.putString("phone", phone);
        editor.putBoolean("logged", true);
        editor.apply();
    }

    public void saveUser(String id, String name, String email, String university, String phone, String role) {
        editor.putString("id", id);
        editor.putString("name", name);
        editor.putString("email", email);
        editor.putString("university", university);
        editor.putString("phone", phone);
        editor.putString("role", role != null ? role : "etudiant");
        editor.putBoolean("logged", true);
        editor.apply();
    }


    public void saveUserSession(User user, String token) {
        editor.putString("token", token);
        editor.putString("id", user.getStudentId());
        editor.putString("name", user.getFullName());
        editor.putString("email", user.getEmail());
        String role = user.getRole() != null ? user.getRole() : "etudiant";
        editor.putString("role", role);
        editor.putBoolean("logged", true);
        editor.apply();
        Log.d(TAG, "Session sauvegardée - Rôle: " + role + ", isAdmin: " + "admin".equals(role));
    }

    public boolean saveProfileImage(Uri imageUri) {
        if (imageUri == null) {
            Log.e(TAG, "URI d'image est null");
            return false;
        }

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Log.e(TAG, "Impossible d'ouvrir l'input stream pour l'URI: " + imageUri);
                return false;
            }

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (bitmap == null) {
                Log.e(TAG, "Impossible de décoder l'image depuis l'URI: " + imageUri);
                return false;
            }

            File imageFile = new File(context.getFilesDir(), PROFILE_IMAGE_FILE);
            
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            boolean saved = bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            outputStream.flush();
            outputStream.close();

            if (saved) {
                editor.putString("profile_image_path", imageFile.getAbsolutePath());
                editor.putBoolean("has_profile_image", true);
                editor.apply();
                Log.d(TAG, "Image de profil sauvegardée avec succès: " + imageFile.getAbsolutePath());
                return true;
            } else {
                Log.e(TAG, "Échec de la compression de l'image");
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "Erreur lors de la sauvegarde de l'image de profil", e);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Erreur inattendue lors de la sauvegarde de l'image", e);
            return false;
        }
    }


    public String getProfileImagePath() {
        try {
            boolean hasImage = prefs.getBoolean("has_profile_image", false);
            if (!hasImage) {
                return null;
            }

            String imagePath = prefs.getString("profile_image_path", null);
            if (imagePath == null) {
                return null;
            }

            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                Log.w(TAG, "Le fichier d'image n'existe plus: " + imagePath);
                // Nettoyer les préférences
                editor.remove("profile_image_path");
                editor.putBoolean("has_profile_image", false);
                editor.apply();
                return null;
            }

            return imagePath;
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la récupération de l'image de profil", e);
            return null;
        }
    }

    @Deprecated
    public Uri getProfileImage() {
        String imagePath = getProfileImagePath();
        if (imagePath != null) {
            File imageFile = new File(imagePath);
            return Uri.fromFile(imageFile);
        }
        return null;
    }


    public boolean deleteProfileImage() {
        try {
            String imagePath = prefs.getString("profile_image_path", null);
            if (imagePath != null) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    boolean deleted = imageFile.delete();
                    if (deleted) {
                        editor.remove("profile_image_path");
                        editor.putBoolean("has_profile_image", false);
                        editor.apply();
                        Log.d(TAG, "Image de profil supprimée");
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la suppression de l'image de profil", e);
            return false;
        }
    }


    public boolean isLoggedIn() { return prefs.getBoolean("logged", false); }
    public String getUserId() { return prefs.getString("id", ""); }
    public String getFullName() { return prefs.getString("name", ""); }
    public String getEmail() { return prefs.getString("email", ""); }
    public String getUniversity() { return prefs.getString("university", ""); }
    public String getPhone() { return prefs.getString("phone", ""); }
    public String getToken() { return prefs.getString("token", ""); }
    public String getRole() { return prefs.getString("role", "etudiant"); }
    public boolean isAdmin() { return "admin".equals(getRole()); }


    public void setSubscriptionBalance(double balance) {
        editor.putFloat("subscription_balance", (float) balance);
        editor.apply();
        Log.d(TAG, "Solde d'abonnement mis à jour: " + balance + " TND");
    }


    public double getSubscriptionBalance() {
        return prefs.getFloat("subscription_balance", 0.0f);
    }


    public boolean deductFromSubscription(double amount) {
        double currentBalance = getSubscriptionBalance();
        if (currentBalance >= amount) {
            double newBalance = currentBalance - amount;
            setSubscriptionBalance(newBalance);
            Log.d(TAG, "Déduction de " + amount + " TND. Nouveau solde: " + newBalance + " TND");
            return true;
        }
        Log.w(TAG, "Solde insuffisant. Solde actuel: " + currentBalance + " TND, montant requis: " + amount + " TND");
        return false;
    }


    public void setSubscriptionStartDate(long timestamp) {
        editor.putLong("subscription_start_date", timestamp);
        editor.apply();
        Log.d(TAG, "Date de début d'abonnement mise à jour: " + timestamp);
    }


    public long getSubscriptionStartDate() {
        return prefs.getLong("subscription_start_date", 0);
    }


    public boolean hasActiveSubscription() {
        long startDate = getSubscriptionStartDate();
        if (startDate == 0) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        long daysSinceStart = (currentTime - startDate) / (1000 * 60 * 60 * 24);
        return daysSinceStart < 30 && getSubscriptionBalance() > 0;
    }


    public boolean hasEnoughBalanceForReservation() {
        return getSubscriptionBalance() >= 0.2;
    }


    public void logout() {
        editor.clear();
        editor.apply();
    }
}
