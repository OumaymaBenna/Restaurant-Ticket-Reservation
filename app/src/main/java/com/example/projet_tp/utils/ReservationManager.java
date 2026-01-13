package com.example.projet_tp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.projet_tp.model.Reservation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReservationManager {
    private static final String TAG = "ReservationManager";
    private static final String PREF_NAME = "reservations";
    private static final String KEY_RESERVATIONS = "reservations_list";
    private static final String KEY_COLD_MEAL_RESERVATIONS = "cold_meal_reservations_list";
    
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private final SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public ReservationManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }


    public void saveReservation(Reservation reservation) {
        try {
            List<Reservation> reservations = loadReservationsFromPrefs(KEY_RESERVATIONS);
            reservations.add(reservation);
            saveReservationsList(reservations);
            Log.d(TAG, "Réservation sauvegardée: " + reservation.getMenuName());
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la sauvegarde de la réservation", e);
        }
    }


    public void saveColdMealReservation(Reservation reservation) {
        try {
            List<Reservation> reservations = loadReservationsFromPrefs(KEY_COLD_MEAL_RESERVATIONS);
            reservations.add(reservation);
            saveColdMealReservationsList(reservations);
            Log.d(TAG, "Réservation repas froid sauvegardée: " + reservation.getMenuName());
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la sauvegarde de la réservation repas froid", e);
        }
    }


    public List<Reservation> getReservations() {
        List<Reservation> allReservations = loadReservationsFromPrefs(KEY_RESERVATIONS);
        List<Reservation> validReservations = ReservationFilter.filterValidMealReservations(allReservations);
        

        if (validReservations.size() != allReservations.size()) {
            saveReservationsList(validReservations);
        }
        
        return validReservations;
    }


    public List<Reservation> getColdMealReservations() {
        List<Reservation> allReservations = loadReservationsFromPrefs(KEY_COLD_MEAL_RESERVATIONS);
        return ReservationFilter.filterValidColdMealReservations(allReservations);
    }
    

    public List<Reservation> getAllColdMealReservations() {
        return loadReservationsFromPrefs(KEY_COLD_MEAL_RESERVATIONS);
    }


    public void cleanExpiredReservations() {
        Log.d(TAG, "Nettoyage des réservations expirées...");
        
        getReservations();
        

        List<Reservation> coldMealReservations = getColdMealReservations();
        Log.d(TAG, "Repas froids valides affichés: " + coldMealReservations.size());
        Log.d(TAG, "Note: Les repas froids expirés restent dans MongoDB pour l'historique");
        
        Log.d(TAG, "Nettoyage terminé");
    }


    public void clearAllReservations() {
        editor.remove(KEY_RESERVATIONS);
        editor.remove(KEY_COLD_MEAL_RESERVATIONS);
        editor.apply();
        Log.d(TAG, "Toutes les réservations ont été supprimées");
    }


    private List<Reservation> loadReservationsFromPrefs(String key) {
        String json = prefs.getString(key, "[]");
        List<Reservation> reservations = new ArrayList<>();
        
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Reservation reservation = reservationFromJson(jsonObject);
                if (reservation != null) {
                    reservations.add(reservation);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Erreur lors du chargement des réservations", e);
        }
        
        return reservations;
    }

    private void saveReservationsList(List<Reservation> reservations) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (Reservation reservation : reservations) {
                jsonArray.put(reservationToJson(reservation));
            }
            editor.putString(KEY_RESERVATIONS, jsonArray.toString());
            editor.apply();
        } catch (JSONException e) {
            Log.e(TAG, "Erreur lors de la sauvegarde des réservations", e);
        }
    }

    private void saveColdMealReservationsList(List<Reservation> reservations) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (Reservation reservation : reservations) {
                jsonArray.put(reservationToJson(reservation));
            }
            editor.putString(KEY_COLD_MEAL_RESERVATIONS, jsonArray.toString());
            editor.apply();
        } catch (JSONException e) {
            Log.e(TAG, "Erreur lors de la sauvegarde des réservations repas froid", e);
        }
    }

    private JSONObject reservationToJson(Reservation reservation) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", reservation.getId() != null ? reservation.getId() : "");
        json.put("userId", reservation.getUserId() != null ? reservation.getUserId() : "");
        json.put("userEmail", reservation.getUserEmail() != null ? reservation.getUserEmail() : "");
        json.put("userName", reservation.getUserName() != null ? reservation.getUserName() : "");
        json.put("menuId", reservation.getMenuId() != null ? reservation.getMenuId() : "");
        json.put("menuName", reservation.getMenuName() != null ? reservation.getMenuName() : "");
        json.put("date", reservation.getDate() != null ? reservation.getDate() : "");
        json.put("time", reservation.getTime() != null ? reservation.getTime() : "");
        json.put("numberOfTickets", reservation.getNumberOfTickets());
        json.put("totalPrice", reservation.getTotalPrice());
        json.put("status", reservation.getStatus() != null ? reservation.getStatus() : "PENDING");
        json.put("createdAt", reservation.getCreatedAt() != null ? reservation.getCreatedAt() : dateFormat.format(new Date()));
        return json;
    }

    private Reservation reservationFromJson(JSONObject json) {
        try {
            Reservation reservation = new Reservation();
            reservation.setId(json.optString("id", ""));
            reservation.setUserId(json.optString("userId", ""));
            reservation.setUserEmail(json.optString("userEmail", ""));
            reservation.setUserName(json.optString("userName", ""));
            reservation.setMenuId(json.optString("menuId", ""));
            reservation.setMenuName(json.optString("menuName", ""));
            reservation.setDate(json.optString("date", ""));
            reservation.setTime(json.optString("time", ""));
            reservation.setNumberOfTickets(json.optInt("numberOfTickets", 1));
            reservation.setTotalPrice(json.optDouble("totalPrice", 0.0));
            reservation.setStatus(json.optString("status", "PENDING"));
            reservation.setCreatedAt(json.optString("createdAt", dateFormat.format(new Date())));
            return reservation;
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la conversion JSON vers Reservation", e);
            return null;
        }
    }
}

