package com.example.projet_tp.api;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MealReservationAPI {

    private static final String BASE_URL = "http://10.0.2.2:3000"; // Pour émulateur Android
    // Si vous testez sur un appareil physique, utilisez l'adresse IP de votre PC
    // private static final String BASE_URL = "http://192.168.1.X:3000";
    
    // Méthode utilitaire pour construire l'URL correctement
    private static String buildUrl(String endpoint) {
        String base = BASE_URL.trim();
        // S'assurer qu'il n'y a pas de slash à la fin de BASE_URL
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        // S'assurer que endpoint commence par /
        if (!endpoint.startsWith("/")) {
            endpoint = "/" + endpoint;
        }
        return base + endpoint;
    }

    private static final String TAG = "MealReservationAPI";

    private final RequestQueue requestQueue;

    public MealReservationAPI(Context context) {
        this.requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    // Interface pour les callbacks
    public interface ReservationCallback {
        void onSuccess(JSONObject response);
        void onError(String error);
    }

    /**
     * Créer une réservation de repas
     */
    public void createReservation(
            String userId,
            String userEmail,
            String userName,
            String studentId,
            String mealType,
            double price,
            String reservationDate,
            String qrCode,
            ReservationCallback callback
    ) {
        String url = buildUrl("/meal-reservations");

        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "userId est vide ou null");
            callback.onError("ID utilisateur manquant");
            return;
        }
        if (studentId == null || studentId.isEmpty()) {
            Log.e(TAG, "studentId est vide ou null");
            callback.onError("ID étudiant manquant");
            return;
        }
        if (userEmail == null || userEmail.isEmpty()) {
            Log.e(TAG, "userEmail est vide ou null");
            callback.onError("Email utilisateur manquant");
            return;
        }
        if (userName == null || userName.isEmpty()) {
            Log.e(TAG, "userName est vide ou null");
            callback.onError("Nom utilisateur manquant");
            return;
        }
        if (mealType == null || mealType.isEmpty()) {
            Log.e(TAG, "mealType est vide ou null");
            callback.onError("Type de repas manquant");
            return;
        }
        if (reservationDate == null || reservationDate.isEmpty()) {
            Log.e(TAG, "reservationDate est vide ou null");
            callback.onError("Date de réservation manquante");
            return;
        }

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("userId", userId);
            requestBody.put("userEmail", userEmail);
            requestBody.put("userName", userName);
            requestBody.put("studentId", studentId);
            requestBody.put("mealType", mealType);
            requestBody.put("price", price);
            requestBody.put("reservationDate", reservationDate);
            requestBody.put("qrCode", qrCode != null ? qrCode : JSONObject.NULL);

            Log.d(TAG, "Envoi de la réservation:");
            Log.d(TAG, "  URL: " + url);
            Log.d(TAG, "  userId: " + userId);
            Log.d(TAG, "  studentId: " + studentId);
            Log.d(TAG, "  userEmail: " + userEmail);
            Log.d(TAG, "  userName: " + userName);
            Log.d(TAG, "  mealType: " + mealType);
            Log.d(TAG, "  price: " + price);
            Log.d(TAG, "  reservationDate: " + reservationDate);
            Log.d(TAG, "  Body JSON: " + requestBody.toString());

        } catch (JSONException e) {
            Log.e(TAG, "Erreur JSON: " + e.getMessage(), e);
            callback.onError("Erreur lors de la préparation des données JSON: " + e.getMessage());
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                response -> {
                    Log.d(TAG, "✅ Réservation créée avec succès: " + response.toString());
                    try {
                        if (response.has("success") && response.getBoolean("success")) {
                            Log.d(TAG, "Réservation stockée dans MongoDB avec succès");
                        }
                    } catch (JSONException e) {
                        Log.w(TAG, "Impossible de parser la réponse success", e);
                    }
                    callback.onSuccess(response);
                },
                error -> {
                    String errorMsg = "Erreur lors de la création de la réservation";
                    if (error.networkResponse != null) {
                        if (error.networkResponse.data != null) {
                            try {
                                String errorBody = new String(error.networkResponse.data);
                                Log.e(TAG, "Erreur serveur (code " + error.networkResponse.statusCode + "): " + errorBody);
                                errorMsg = errorBody;
                            } catch (Exception e) {
                                Log.e(TAG, "Erreur lors de la lecture de la réponse d'erreur", e);
                            }
                        }
                        Log.e(TAG, "Code HTTP: " + error.networkResponse.statusCode);
                    } else {
                        if (error.getMessage() != null) {
                            Log.e(TAG, "Erreur réseau: " + error.getMessage());
                            if (error.getMessage().contains("Unable to resolve host") || 
                                error.getMessage().contains("Failed to connect")) {
                                errorMsg = "Impossible de se connecter au serveur. Vérifiez que le serveur est démarré.";
                            } else {
                                errorMsg = "Erreur réseau: " + error.getMessage();
                            }
                        }
                    }
                    callback.onError(errorMsg);
                }
        );

        requestQueue.add(request);
    }


    public void getUserReservations(String studentId, ReservationCallback callback) {
        String url = buildUrl("/meal-reservations/user/" + studentId);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d(TAG, "Réservations récupérées: " + response.toString());
                    callback.onSuccess(response);
                },
                error -> {
                    String errorMsg = "Erreur lors de la récupération des réservations";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        errorMsg = new String(error.networkResponse.data);
                    }
                    Log.e(TAG, "Erreur: " + errorMsg);
                    callback.onError(errorMsg);
                }
        );

        requestQueue.add(request);
    }


    public void createColdMealReservation(
            String userId,
            String userEmail,
            String userName,
            String studentId,
            String mealType,
            double price,
            String reservationDate,
            String qrCode,
            ReservationCallback callback
    ) {
        String url = buildUrl("/cold-meal-reservations");

        // Validation des données requises
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "userId est vide ou null");
            callback.onError("ID utilisateur manquant");
            return;
        }
        if (studentId == null || studentId.isEmpty()) {
            Log.e(TAG, "studentId est vide ou null");
            callback.onError("ID étudiant manquant");
            return;
        }
        if (userEmail == null || userEmail.isEmpty()) {
            Log.e(TAG, "userEmail est vide ou null");
            callback.onError("Email utilisateur manquant");
            return;
        }
        if (userName == null || userName.isEmpty()) {
            Log.e(TAG, "userName est vide ou null");
            callback.onError("Nom utilisateur manquant");
            return;
        }
        if (reservationDate == null || reservationDate.isEmpty()) {
            Log.e(TAG, "reservationDate est vide ou null");
            callback.onError("Date de réservation manquante");
            return;
        }

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("userId", userId);
            requestBody.put("userEmail", userEmail);
            requestBody.put("userName", userName);
            requestBody.put("studentId", studentId);
            requestBody.put("mealType", mealType != null ? mealType : "Repas Froid");
            requestBody.put("price", price);
            requestBody.put("reservationDate", reservationDate);
            requestBody.put("qrCode", qrCode != null ? qrCode : JSONObject.NULL);

            Log.d(TAG, "Envoi de la réservation repas froid:");
            Log.d(TAG, "  URL: " + url);
            Log.d(TAG, "  userId: " + userId);
            Log.d(TAG, "  studentId: " + studentId);
            Log.d(TAG, "  userEmail: " + userEmail);
            Log.d(TAG, "  userName: " + userName);
            Log.d(TAG, "  mealType: " + mealType);
            Log.d(TAG, "  price: " + price);
            Log.d(TAG, "  reservationDate: " + reservationDate);
            Log.d(TAG, "  Body JSON: " + requestBody.toString());

        } catch (JSONException e) {
            Log.e(TAG, "Erreur JSON: " + e.getMessage(), e);
            callback.onError("Erreur lors de la préparation des données JSON: " + e.getMessage());
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                response -> {
                    Log.d(TAG, "✅ Réservation repas froid créée avec succès: " + response.toString());
                    try {
                        if (response.has("success") && response.getBoolean("success")) {
                            Log.d(TAG, "Réservation repas froid stockée dans la collection séparée MongoDB");
                        }
                    } catch (JSONException e) {
                        Log.w(TAG, "Impossible de parser la réponse success", e);
                    }
                    callback.onSuccess(response);
                },
                error -> {
                    String errorMsg = "Erreur lors de la création de la réservation repas froid";
                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        if (error.networkResponse.data != null) {
                            try {
                                String errorBody = new String(error.networkResponse.data);
                                Log.e(TAG, "Erreur serveur (code " + statusCode + "): " + errorBody);
                                
                                try {
                                    JSONObject errorJson = new JSONObject(errorBody);
                                    if (errorJson.has("message")) {
                                        errorMsg = errorJson.getString("message");
                                    } else if (errorJson.has("error")) {
                                        errorMsg = errorJson.getString("error");
                                    }
                                } catch (JSONException e) {
                                    if (errorBody.contains("<!DOCTYPE") || errorBody.contains("<html") || statusCode == 404) {
                                        errorMsg = "Erreur serveur: Route non trouvée (404).\n\n" +
                                                "Vérifiez que:\n" +
                                                "1. Le serveur Node.js est démarré (cd server && npm start)\n" +
                                                "2. MongoDB est en cours d'exécution\n" +
                                                "3. La route /cold-meal-reservations existe sur le serveur";
                                    } else {
                                        errorMsg = errorBody.length() > 200 ? errorBody.substring(0, 200) + "..." : errorBody;
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Erreur lors de la lecture de la réponse d'erreur", e);
                                if (statusCode == 404) {
                                    errorMsg = "Route non trouvée (404). Vérifiez que le serveur est démarré.";
                                }
                            }
                        } else if (statusCode == 404) {
                            errorMsg = "Route non trouvée (404). Vérifiez que le serveur est démarré et que la route /cold-meal-reservations existe.";
                        }
                        Log.e(TAG, "Code HTTP: " + statusCode);
                    } else {
                        if (error.getMessage() != null) {
                            Log.e(TAG, "Erreur réseau: " + error.getMessage());
                            if (error.getMessage().contains("Unable to resolve host") || 
                                error.getMessage().contains("Failed to connect") ||
                                error.getMessage().contains("Connection refused")) {
                                errorMsg = "Impossible de se connecter au serveur.\n\n" +
                                        "Vérifiez que:\n" +
                                        "1. Le serveur Node.js est démarré (cd server && npm start)\n" +
                                        "2. Le serveur écoute sur http://localhost:3000\n" +
                                        "3. MongoDB est en cours d'exécution";
                            } else {
                                errorMsg = "Erreur réseau: " + error.getMessage();
                            }
                        }
                    }
                    callback.onError(errorMsg);
                }
        );

        requestQueue.add(request);
    }


    public void getUserColdMealReservations(String studentId, ReservationCallback callback) {
        String url = buildUrl("/cold-meal-reservations/user/" + studentId);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d(TAG, "Réservations repas froid récupérées: " + response.toString());
                    callback.onSuccess(response);
                },
                error -> {
                    String errorMsg = "Erreur lors de la récupération des réservations repas froid";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        errorMsg = new String(error.networkResponse.data);
                    }
                    Log.e(TAG, "Erreur: " + errorMsg);
                    callback.onError(errorMsg);
                }
        );

        requestQueue.add(request);
    }


    public void cancelReservation(String reservationId, ReservationCallback callback) {
        String url = buildUrl("/meal-reservations/" + reservationId + "/cancel");

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                null,
                response -> {
                    Log.d(TAG, "Réservation annulée: " + response.toString());
                    callback.onSuccess(response);
                },
                error -> {
                    String errorMsg = "Erreur lors de l'annulation de la réservation";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        errorMsg = new String(error.networkResponse.data);
                    }
                    Log.e(TAG, "Erreur: " + errorMsg);
                    callback.onError(errorMsg);
                }
        );

        requestQueue.add(request);
    }


    public void subscribe(String studentId, double amount, ReservationCallback callback) {
        String url = buildUrl("/subscribe");

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("studentId", studentId);
            requestBody.put("amount", amount);

            Log.d(TAG, "Paiement d'abonnement:");
            Log.d(TAG, "  URL: " + url);
            Log.d(TAG, "  studentId: " + studentId);
            Log.d(TAG, "  amount: " + amount);
        } catch (JSONException e) {
            Log.e(TAG, "Erreur JSON: " + e.getMessage(), e);
            callback.onError("Erreur lors de la préparation des données JSON: " + e.getMessage());
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                response -> {
                    Log.d(TAG, "✅ Abonnement payé avec succès: " + response.toString());
                    callback.onSuccess(response);
                },
                error -> {
                    String errorMsg = "Erreur lors du paiement de l'abonnement";
                    if (error.networkResponse != null) {
                        if (error.networkResponse.data != null) {
                            try {
                                String errorBody = new String(error.networkResponse.data);
                                Log.e(TAG, "Erreur serveur (code " + error.networkResponse.statusCode + "): " + errorBody);
                                
                                try {
                                    JSONObject errorJson = new JSONObject(errorBody);
                                    if (errorJson.has("message")) {
                                        errorMsg = errorJson.getString("message");
                                    }
                                } catch (JSONException e) {
                                    errorMsg = errorBody.length() > 200 ? errorBody.substring(0, 200) + "..." : errorBody;
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Erreur lors de la lecture de la réponse d'erreur", e);
                            }
                        }
                    } else {
                        if (error.getMessage() != null) {
                            Log.e(TAG, "Erreur réseau: " + error.getMessage());
                            errorMsg = "Erreur réseau: " + error.getMessage();
                        }
                    }
                    callback.onError(errorMsg);
                }
        );

        requestQueue.add(request);
    }


    public void getSubscriptionBalance(String studentId, ReservationCallback callback) {
        String url = buildUrl("/user/" + studentId + "/balance");

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d(TAG, "Solde récupéré: " + response.toString());
                    callback.onSuccess(response);
                },
                error -> {
                    String errorMsg = "Erreur lors de la récupération du solde";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        errorMsg = new String(error.networkResponse.data);
                    }
                    Log.e(TAG, "Erreur: " + errorMsg);
                    callback.onError(errorMsg);
                }
        );

        requestQueue.add(request);
    }


    public void getReservationsStats(ReservationCallback callback) {
        getReservationsStatsByPeriod("day", callback);
    }


    public void getReservationsStatsByPeriod(String period, ReservationCallback callback) {
        String url = buildUrl("/admin/reservations/stats?period=" + period);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d(TAG, "Statistiques récupérées pour " + period + ": " + response.toString());
                    callback.onSuccess(response);
                },
                error -> {
                    String errorMsg = "Erreur lors de la récupération des statistiques";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        errorMsg = new String(error.networkResponse.data);
                    }
                    Log.e(TAG, "Erreur: " + errorMsg);
                    callback.onError(errorMsg);
                }
        );

        requestQueue.add(request);
    }

    public void getUsersStats(ReservationCallback callback) {
        String url = buildUrl("/admin/users/stats");

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d(TAG, "Statistiques utilisateurs récupérées: " + response.toString());
                    callback.onSuccess(response);
                },
                error -> {
                    String errorMsg = "Erreur lors de la récupération des statistiques utilisateurs";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        errorMsg = new String(error.networkResponse.data);
                    }
                    Log.e(TAG, "Erreur: " + errorMsg);
                    callback.onError(errorMsg);
                }
        );

        requestQueue.add(request);
    }

    public void getAllUsers(ReservationCallback callback) {
        String url = buildUrl("/admin/users");

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d(TAG, "Utilisateurs récupérés: " + response.toString());
                    callback.onSuccess(response);
                },
                error -> {
                    String errorMsg = "Erreur lors de la récupération des utilisateurs";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        errorMsg = new String(error.networkResponse.data);
                    }
                    Log.e(TAG, "Erreur: " + errorMsg);
                    callback.onError(errorMsg);
                }
        );

        requestQueue.add(request);
    }


    public void blockUser(String userId, boolean block, ReservationCallback callback) {
        String url = buildUrl("/admin/users/" + userId + "/block");

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("block", block);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PUT,
                    url,
                    requestBody,
                    response -> {
                        Log.d(TAG, "Utilisateur " + (block ? "bloqué" : "débloqué") + ": " + response.toString());
                        callback.onSuccess(response);
                    },
                    error -> {
                        String errorMsg = "Erreur lors du blocage/déblocage de l'utilisateur";
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            errorMsg = new String(error.networkResponse.data);
                        }
                        Log.e(TAG, "Erreur: " + errorMsg);
                        callback.onError(errorMsg);
                    }
            );

            requestQueue.add(request);
        } catch (JSONException e) {
            Log.e(TAG, "Erreur création JSON", e);
            callback.onError("Erreur lors de la création de la requête");
        }
    }
}
