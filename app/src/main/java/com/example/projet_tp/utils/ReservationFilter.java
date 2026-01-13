package com.example.projet_tp.utils;

import android.util.Log;

import com.example.projet_tp.model.Reservation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class ReservationFilter {
    private static final String TAG = "ReservationFilter";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private static final SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final SimpleDateFormat serverDateFormat = new SimpleDateFormat("EEEE dd/MM/yyyy", Locale.getDefault());


    public static List<Reservation> filterValidMealReservations(List<Reservation> reservations) {
        List<Reservation> validReservations = new ArrayList<>();
        Calendar now = Calendar.getInstance();
        
        for (Reservation reservation : reservations) {
            try {
                Date createdAt = parseDate(reservation.getCreatedAt());
                if (createdAt == null) {
                    validReservations.add(reservation);
                    continue;
                }
                
                Calendar createdCal = Calendar.getInstance();
                createdCal.setTime(createdAt);
                
                createdCal.add(Calendar.HOUR_OF_DAY, 24);
                
                if (createdCal.after(now)) {
                    validReservations.add(reservation);
                } else {
                    Log.d(TAG, "Réservation expirée (cachée de l'affichage): " + reservation.getMenuName() + 
                        " - Créée le: " + reservation.getCreatedAt());
                }
            } catch (Exception e) {
                Log.e(TAG, "Erreur lors du filtrage de la réservation: " + reservation.getMenuName(), e);
                validReservations.add(reservation);
            }
        }
        
        return validReservations;
    }


    public static List<Reservation> filterValidColdMealReservations(List<Reservation> reservations) {
        List<Reservation> validReservations = new ArrayList<>();
        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);
        
        for (Reservation reservation : reservations) {
            try {
                Date reservationDate = parseReservationDate(reservation.getDate());
                if (reservationDate == null) {
                    validReservations.add(reservation);
                    continue;
                }
                
                Calendar reservationCal = Calendar.getInstance();
                reservationCal.setTime(reservationDate);
                reservationCal.set(Calendar.HOUR_OF_DAY, 0);
                reservationCal.set(Calendar.MINUTE, 0);
                reservationCal.set(Calendar.SECOND, 0);
                reservationCal.set(Calendar.MILLISECOND, 0);
                
                if (reservationCal.after(now) || reservationCal.equals(now)) {
                    validReservations.add(reservation);
                } else {
                    Log.d(TAG, "Réservation repas froid expirée (cachée de l'affichage, mais reste dans MongoDB): " + 
                        reservation.getMenuName() + " - Date: " + reservation.getDate());
                }
            } catch (Exception e) {
                Log.e(TAG, "Erreur lors du filtrage de la réservation repas froid: " + reservation.getMenuName(), e);
                validReservations.add(reservation);
            }
        }
        
        return validReservations;
    }


    private static Date parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        
        String[] formats = {
            "dd/MM/yyyy HH:mm",
            "EEEE dd/MM/yyyy",
            "dd/MM/yyyy",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd"
        };
        
        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
                return sdf.parse(dateString);
            } catch (ParseException e) {
            }
        }
        
        return null;
    }


    private static Date parseReservationDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        
        try {
            return serverDateFormat.parse(dateString);
        } catch (ParseException e) {
            try {
                return dateOnlyFormat.parse(dateString);
            } catch (ParseException e2) {
                return parseDate(dateString);
            }
        }
    }


    public static boolean isColdMealReservationValid(Reservation reservation) {
        try {
            Date reservationDate = parseReservationDate(reservation.getDate());
            if (reservationDate == null) return true;
            
            Calendar now = Calendar.getInstance();
            now.set(Calendar.HOUR_OF_DAY, 0);
            now.set(Calendar.MINUTE, 0);
            now.set(Calendar.SECOND, 0);
            now.set(Calendar.MILLISECOND, 0);
            
            Calendar reservationCal = Calendar.getInstance();
            reservationCal.setTime(reservationDate);
            reservationCal.set(Calendar.HOUR_OF_DAY, 0);
            reservationCal.set(Calendar.MINUTE, 0);
            reservationCal.set(Calendar.SECOND, 0);
            reservationCal.set(Calendar.MILLISECOND, 0);
            
            return reservationCal.after(now) || reservationCal.equals(now);
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la vérification de validité", e);
            return true;
        }
    }


    public static boolean isMealReservationValid(Reservation reservation) {
        try {
            Date createdAt = parseDate(reservation.getCreatedAt());
            if (createdAt == null) return true;
            
            Calendar now = Calendar.getInstance();
            Calendar createdCal = Calendar.getInstance();
            createdCal.setTime(createdAt);
            createdCal.add(Calendar.HOUR_OF_DAY, 24);
            
            return createdCal.after(now);
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la vérification de validité", e);
            return true;
        }
    }


    public static List<Reservation> filterReservationsByType(List<Reservation> reservations, String mealType) {
        if (reservations == null) return new ArrayList<>();
        
        if ("Repas Froid".equals(mealType)) {
            return filterValidColdMealReservations(reservations);
        } else {
            return filterValidMealReservations(reservations);
        }
    }
}

