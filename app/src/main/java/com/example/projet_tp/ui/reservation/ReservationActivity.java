package com.example.projet_tp.ui.reservation;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projet_tp.R;
import com.example.projet_tp.model.Reservation;
import com.example.projet_tp.utils.ReservationManager;
import com.example.projet_tp.utils.SessionManager;
import com.example.projet_tp.api.MealReservationAPI;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.graphics.Bitmap;
import android.graphics.Color;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import android.app.Dialog;
import android.widget.ImageView;
import android.view.ViewGroup;
import android.view.Window;

public class ReservationActivity extends AppCompatActivity {

    private MaterialButton buttonAddLunch, buttonAddDinner, buttonAddColdMeal;
    private MaterialCardView cardLunch, cardDinner, cardColdMeal;
    private TextView textViewTitle, textViewSubtitle, textViewTotalAmount, textViewTicketStatus;
    private LinearLayout containerReservedMeals;

    private boolean isTicketReserved = false;
    private double totalAmount = 0;
    private List<Meal> mealsList = new ArrayList<>();
    private SessionManager sessionManager;
    private ReservationManager reservationManager;

    private static class Meal {
        String name;
        double price;
        Meal(String name, double price) { this.name = name; this.price = price; }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        sessionManager = new SessionManager(this);
        reservationManager = new ReservationManager(this);
        
        reservationManager.cleanExpiredReservations();

        initViews();
        setupClickListeners();
        updateTicketStatus();

        String mealType = getIntent().getStringExtra("meal_type");
        if (mealType != null) {
            if ("lunch".equals(mealType)) {
                if (cardDinner != null) cardDinner.setVisibility(android.view.View.GONE);
                if (cardLunch != null) {
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) cardLunch.getLayoutParams();
                    params.weight = 1.0f; params.setMargins(0,0,0,0);
                    cardLunch.setLayoutParams(params);
                }
                textViewTitle.setText("Réservation Déjeuner");
                textViewSubtitle.setText("Réservez votre déjeuner.");
            } else if ("dinner".equals(mealType)) {
                if (cardLunch != null) cardLunch.setVisibility(android.view.View.GONE);
                if (cardDinner != null) {
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) cardDinner.getLayoutParams();
                    params.weight = 1.0f; params.setMargins(0,0,0,0);
                    cardDinner.setLayoutParams(params);
                }
                textViewTitle.setText("Réservation Dîner");
                textViewSubtitle.setText("Réservez votre dîner.");
            } else if ("cold".equals(mealType)) {
                if (cardLunch != null) cardLunch.setVisibility(android.view.View.GONE);
                if (cardDinner != null) cardDinner.setVisibility(android.view.View.GONE);
                if (cardColdMeal != null) {
                    cardColdMeal.setVisibility(android.view.View.VISIBLE);
                }
                textViewTitle.setText("Réservation Repas Froid");
                textViewSubtitle.setText("Réservez votre repas froid pour le samedi soir.");
            }
        }
    }

    private void initViews() {
        buttonAddLunch = findViewById(R.id.buttonAddLunch);
        buttonAddDinner = findViewById(R.id.buttonAddDinner);
        buttonAddColdMeal = findViewById(R.id.buttonAddColdMeal);
        cardLunch = findViewById(R.id.cardLunch);
        cardDinner = findViewById(R.id.cardDinner);
        cardColdMeal = findViewById(R.id.cardColdMeal);
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewSubtitle = findViewById(R.id.textViewSubtitle);
        textViewTotalAmount = findViewById(R.id.textViewTotalAmount);
        textViewTicketStatus = findViewById(R.id.textViewTicketStatus);
        containerReservedMeals = findViewById(R.id.containerReservedMeals);
        
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            });
        }
    }

    private void setupClickListeners() {

        buttonAddLunch.setOnClickListener(v -> addMeal("Déjeuner", 0.2, buttonAddLunch));
        buttonAddDinner.setOnClickListener(v -> addMeal("Dîner", 0.2, buttonAddDinner));
        if (buttonAddColdMeal != null) {
            buttonAddColdMeal.setOnClickListener(v -> addMeal("Repas Froid", 0.2, buttonAddColdMeal));
        }
    }

    private void addMeal(String mealName, double price, MaterialButton button) {
        if (isTicketReserved) {
            Toast.makeText(this, "Impossible d'ajouter après réservation", Toast.LENGTH_SHORT).show();
            return;
        }
        for (Meal meal : mealsList) if (meal.name.equals(mealName)) {
            Toast.makeText(this, "Ce repas est déjà dans votre panier", Toast.LENGTH_SHORT).show();
            return;
        }

        mealsList.add(new Meal(mealName, price));
        totalAmount += price;
        updateTotalAmount();
        addMealToReservedList(mealName, price);

        if (button != null) {
            button.setEnabled(false);
            button.setText("Ajouté");
            button.setBackgroundTintList(getResources().getColorStateList(R.color.gray_medium, null));
        }

        Toast.makeText(this, "Repas \"" + mealName + "\" ajouté!", Toast.LENGTH_SHORT).show();
        updateTicketStatus();
    }

    private void updateTotalAmount() {
        textViewTotalAmount.setText(String.format("%.3f TND", totalAmount));
    }

    private void addMealToReservedList(String mealName, double price) {
        MaterialCardView mealCard = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0,0,0,16);
        mealCard.setLayoutParams(cardParams);
        mealCard.setRadius(16); mealCard.setCardElevation(4);
        mealCard.setTag(mealName);

        LinearLayout cardContent = new LinearLayout(this);
        cardContent.setOrientation(LinearLayout.VERTICAL);
        cardContent.setPadding(32,32,32,32);

        TextView mealNameText = new TextView(this);
        mealNameText.setText(mealName); mealNameText.setTextSize(18);
        mealNameText.setTypeface(null, android.graphics.Typeface.BOLD);
        mealNameText.setTextColor(getResources().getColor(R.color.primary_dark, null));

        TextView mealPriceText = new TextView(this);
        mealPriceText.setText("Prix : " + String.format("%.3f TND", price));
        mealPriceText.setTextSize(14);
        mealPriceText.setTextColor(getResources().getColor(R.color.secondary_text, null));
        LinearLayout.LayoutParams priceParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        priceParams.setMargins(0,8,0,16);
        mealPriceText.setLayoutParams(priceParams);

        MaterialButton reserveButton = new MaterialButton(this);
        reserveButton.setText("Réserver ce repas");
        reserveButton.setTextSize(14);
        reserveButton.setCornerRadius(8);
        reserveButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFF6B35));
        reserveButton.setTextColor(0xFFFFFFFF);
        LinearLayout.LayoutParams reserveParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        reserveParams.setMargins(0,8,0,0);
        reserveButton.setLayoutParams(reserveParams);

        reserveButton.setOnClickListener(v -> {
            if (!isTicketReserved) {
                reserveTicket();
                sendReservationToServer(mealName, price);
                reserveButton.setEnabled(false);
                reserveButton.setText("Réservé ✓");
                Toast.makeText(this, "Réservation confirmée!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Le ticket est déjà réservé", Toast.LENGTH_SHORT).show();
            }
        });

        MaterialButton removeButton = new MaterialButton(this);
        removeButton.setText("Supprimer");
        removeButton.setTextSize(14);
        removeButton.setCornerRadius(8);
        removeButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFF6B35));
        removeButton.setTextColor(0xFFFFFFFF);
        LinearLayout.LayoutParams removeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        removeParams.setMargins(0,8,0,0);
        removeButton.setLayoutParams(removeParams);

        removeButton.setOnClickListener(v -> {
            if (!isTicketReserved) removeMeal(mealName, price, mealCard);
            else Toast.makeText(this, "Impossible de supprimer après réservation", Toast.LENGTH_SHORT).show();
        });

        cardContent.addView(mealNameText);
        cardContent.addView(mealPriceText);
        cardContent.addView(reserveButton);
        cardContent.addView(removeButton);
        mealCard.addView(cardContent);

        containerReservedMeals.addView(mealCard);
    }

    private void removeMeal(String mealName, double price, MaterialCardView card) {
        mealsList.removeIf(meal -> meal.name.equals(mealName));
        totalAmount -= price;
        updateTotalAmount();
        containerReservedMeals.removeView(card);

        if (mealName.equals("Déjeuner")) {
            if (buttonAddLunch != null) {
                buttonAddLunch.setEnabled(true);
                buttonAddLunch.setText("Ajouter");
                buttonAddLunch.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFF6B35));
            }
        } else if (mealName.equals("Dîner")) {
            if (buttonAddDinner != null) {
                buttonAddDinner.setEnabled(true);
                buttonAddDinner.setText("Ajouter");
                buttonAddDinner.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFF6B35));
            }
        } else if (mealName.equals("Repas Froid")) {
            if (buttonAddColdMeal != null) {
                buttonAddColdMeal.setEnabled(true);
                buttonAddColdMeal.setText("Ajouter au panier");
                buttonAddColdMeal.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFF6B35));
            }
        }

        updateTicketStatus();
        Toast.makeText(this, "Repas \"" + mealName + "\" supprimé", Toast.LENGTH_SHORT).show();
    }

    private void updateTicketStatus() {
        if (isTicketReserved) {
            textViewTicketStatus.setText("Ticket réservé ✓");
            textViewTicketStatus.setTextColor(0xFFFF6B35);
            if (buttonAddLunch != null) buttonAddLunch.setEnabled(false);
            if (buttonAddDinner != null) buttonAddDinner.setEnabled(false);
            if (buttonAddColdMeal != null) buttonAddColdMeal.setEnabled(false);
            showQRCodesForReservedMeals();
        } else if (mealsList.isEmpty()) {
            textViewTicketStatus.setText("En attente");
            textViewTicketStatus.setTextColor(0xFFFF6B35);
        } else {
            textViewTicketStatus.setText("Prêt à réserver (" + mealsList.size() + " repas)");
            textViewTicketStatus.setTextColor(0xFFFF6B35);
        }
    }

    private void reserveTicket() {
        isTicketReserved = true;
        updateTicketStatus();
    }

    private void sendReservationToServer(String mealName, double price) {
        MealReservationAPI api = new MealReservationAPI(this);
        String studentId = sessionManager.getUserId();
        
        if (studentId != null && !studentId.isEmpty()) {
            api.getSubscriptionBalance(studentId, new MealReservationAPI.ReservationCallback() {
                @Override
                public void onSuccess(org.json.JSONObject response) {
                    try {
                        double balance = 0.0;
                        if (response.has("subscriptionBalance")) {
                            balance = response.getDouble("subscriptionBalance");
                        }
                        
                        if (balance < price) {
                            Toast.makeText(ReservationActivity.this, 
                                "Solde insuffisant! Solde actuel: " + String.format("%.3f DNT", balance) + 
                                "\nMontant requis: " + String.format("%.3f DNT", price) + 
                                "\nVeuillez recharger votre compte d'abonnement.", 
                                Toast.LENGTH_LONG).show();
                            return;
                        }
                        
                        proceedWithReservation(mealName, price);
                    } catch (org.json.JSONException e) {
                        android.util.Log.e("ReservationActivity", "Erreur parsing balance", e);
                        proceedWithReservation(mealName, price);
                    }
                }

                @Override
                public void onError(String error) {
                    android.util.Log.e("ReservationActivity", "Erreur vérification solde: " + error);
                    proceedWithReservation(mealName, price);
                }
            });
        } else {
            proceedWithReservation(mealName, price);
        }
    }
    
    private void proceedWithReservation(String mealName, double price) {
        MealReservationAPI api = new MealReservationAPI(this);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE dd/MM/yyyy", new Locale("fr"));
        
        Date reservationDateObj;
        if ("Repas Froid".equals(mealName)) {
            reservationDateObj = getNextSaturdayEvening();
        } else {
            reservationDateObj = new Date();
        }
        
        String reservationDate = dateFormat.format(reservationDateObj);

        String userId = sessionManager.getUserId();
        String userEmail = sessionManager.getEmail();
        String userName = sessionManager.getFullName();
        String studentId = sessionManager.getUserId();
        String qrCode = "Type: " + mealName + "\nDate: " + reservationDate;

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Erreur: ID utilisateur manquant. Veuillez vous reconnecter.", Toast.LENGTH_LONG).show();
            android.util.Log.e("ReservationActivity", "userId est vide");
            return;
        }
        if (studentId == null || studentId.isEmpty()) {
            Toast.makeText(this, "Erreur: ID étudiant manquant. Veuillez vous reconnecter.", Toast.LENGTH_LONG).show();
            android.util.Log.e("ReservationActivity", "studentId est vide");
            return;
        }
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "Erreur: Email manquant. Veuillez vous reconnecter.", Toast.LENGTH_LONG).show();
            android.util.Log.e("ReservationActivity", "userEmail est vide");
            return;
        }
        if (userName == null || userName.isEmpty()) {
            Toast.makeText(this, "Erreur: Nom utilisateur manquant. Veuillez vous reconnecter.", Toast.LENGTH_LONG).show();
            android.util.Log.e("ReservationActivity", "userName est vide");
            return;
        }

        android.util.Log.d("ReservationActivity", "Envoi de la réservation au serveur:");
        android.util.Log.d("ReservationActivity", "  - Type de repas: " + mealName);
        android.util.Log.d("ReservationActivity", "  - Prix: " + price);
        android.util.Log.d("ReservationActivity", "  - Date: " + reservationDate);
        android.util.Log.d("ReservationActivity", "  - ID Étudiant: " + studentId);
        android.util.Log.d("ReservationActivity", "  - Email: " + userEmail);
        android.util.Log.d("ReservationActivity", "  - Nom: " + userName);

        final String finalMealName = mealName;
        final String finalReservationDate = reservationDate;
        final double finalPrice = price;
        
        MealReservationAPI.ReservationCallback callback = new MealReservationAPI.ReservationCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                android.util.Log.d("ReservationActivity", "✅ Réservation créée avec succès dans MongoDB");
                try {
                    if (response.has("success") && response.getBoolean("success")) {
                        if (response.has("reservation")) {
                            JSONObject reservation = response.getJSONObject("reservation");
                            String collection = "Repas Froid".equals(finalMealName) ? "coldmealreservations" : "mealreservations";
                            android.util.Log.d("ReservationActivity", "ID Réservation MongoDB: " + reservation.optString("_id", "N/A") + " (Collection: " + collection + ")");
                        }
                        
                        saveReservationLocally(finalMealName, finalReservationDate, finalPrice);
                        
                        String balanceMessage = "";
                        try {
                            if (response.has("subscriptionBalance")) {
                                double newBalance = response.getDouble("subscriptionBalance");
                                balanceMessage = "\nNouveau solde: " + String.format("%.3f DNT", newBalance);
                            }
                        } catch (JSONException e) {
                        }
                        
                        String message = "Repas Froid".equals(finalMealName) 
                            ? "✅ Réservation repas froid enregistrée !" + balanceMessage
                            : "✅ Réservation enregistrée !" + balanceMessage;
                        Toast.makeText(ReservationActivity.this, message, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ReservationActivity.this, "Réservation envoyée au serveur", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    android.util.Log.e("ReservationActivity", "Erreur parsing réponse", e);
                    Toast.makeText(ReservationActivity.this, "Réservation envoyée au serveur", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("ReservationActivity", "❌ Erreur serveur: " + error);
                Toast.makeText(ReservationActivity.this, "Erreur: " + error, Toast.LENGTH_LONG).show();
            }
        };

        if ("Repas Froid".equals(mealName)) {
            android.util.Log.d("ReservationActivity", "📦 Utilisation de la collection séparée pour repas froid");
            api.createColdMealReservation(userId, userEmail, userName, studentId, mealName, price, reservationDate, qrCode, callback);
        } else {
            android.util.Log.d("ReservationActivity", "📦 Utilisation de la collection normale pour " + mealName);
            api.createReservation(userId, userEmail, userName, studentId, mealName, price, reservationDate, qrCode, callback);
        }
    }

    private void showQRCodesForReservedMeals() {
        for (int i = 0; i < containerReservedMeals.getChildCount(); i++) {
            MaterialCardView card = (MaterialCardView) containerReservedMeals.getChildAt(i);
            LinearLayout cardContent = (LinearLayout) card.getChildAt(0);
            boolean hasQRButton = false;
            for (int j = 0; j < cardContent.getChildCount(); j++) {
                if (cardContent.getChildAt(j) instanceof MaterialButton) {
                    MaterialButton btn = (MaterialButton) cardContent.getChildAt(j);
                    if (btn.getText().toString().contains("QR")) { hasQRButton = true; break; }
                }
            }
            if (!hasQRButton) {
                MaterialButton qrButton = new MaterialButton(this);
                qrButton.setText("Voir le QR Code");
                qrButton.setTextSize(14); qrButton.setCornerRadius(8);
                qrButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFF6B35));
                qrButton.setTextColor(0xFFFFFFFF);
                LinearLayout.LayoutParams qrParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                qrParams.setMargins(0,8,0,0); qrButton.setLayoutParams(qrParams);

                String mealName = (String) card.getTag();
                qrButton.setOnClickListener(v -> showQRCodeDialog(mealName));

                cardContent.addView(qrButton);
            }
        }
    }

    private void showQRCodeDialog(String mealName) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE dd/MM/yyyy", new Locale("fr"));
        Date dateToShow;
        if ("Repas Froid".equals(mealName)) {
            dateToShow = getNextSaturdayEvening();
        } else {
            dateToShow = new Date();
        }
        String currentDate = dateFormat.format(dateToShow);
        String qrContent = "Type de repas: " + mealName + "\nDate: " + currentDate;
        Bitmap qrBitmap = generateQRCode(qrContent);

        if (qrBitmap != null) {
            Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_qr_code);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            ImageView qrImageView = dialog.findViewById(R.id.imageViewQRCode);
            TextView textMealName = dialog.findViewById(R.id.textMealName);
            TextView textDate = dialog.findViewById(R.id.textDate);
            MaterialButton buttonClose = dialog.findViewById(R.id.buttonClose);

            qrImageView.setImageBitmap(qrBitmap);
            textMealName.setText("Repas: " + mealName);
            textDate.setText("Date: " + currentDate);

            buttonClose.setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        } else {
            Toast.makeText(this, "Erreur lors de la génération du QR code", Toast.LENGTH_SHORT).show();
        }
    }


    private Date getNextSaturdayEvening() {
        Calendar cal = Calendar.getInstance();
        int currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        
        int daysUntilSaturday;
        if (currentDayOfWeek == Calendar.SATURDAY) {
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            if (hour < 18) {
                daysUntilSaturday = 0;
            } else {
                daysUntilSaturday = 7;
            }
        } else {
            daysUntilSaturday = (Calendar.SATURDAY - currentDayOfWeek + 7) % 7;
            if (daysUntilSaturday == 0) daysUntilSaturday = 7;
        }
        
        cal.add(Calendar.DAY_OF_MONTH, daysUntilSaturday);
        cal.set(Calendar.HOUR_OF_DAY, 18);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        return cal.getTime();
    }

    private Bitmap generateQRCode(String content) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            com.google.zxing.common.BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth(); int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            // Utiliser gris clair au lieu de blanc pour le fond du QR code
            int backgroundColor = getResources().getColor(R.color.text_light, null);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : backgroundColor);
                }
            }
            return bitmap;
        } catch (WriterException e) { e.printStackTrace(); return null; }
    }


    private void saveReservationLocally(String mealName, String reservationDate, double price) {
        try {
            Reservation reservation = new Reservation();
            reservation.setUserId(sessionManager.getUserId());
            reservation.setUserEmail(sessionManager.getEmail());
            reservation.setUserName(sessionManager.getFullName());
            reservation.setMenuName(mealName);
            reservation.setDate(reservationDate);
            reservation.setTime("");
            reservation.setNumberOfTickets(1);
            reservation.setTotalPrice(price);
            reservation.setStatus("RESERVED");
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            reservation.setCreatedAt(dateFormat.format(new Date()));
            
            if ("Repas Froid".equals(mealName)) {
                reservationManager.saveColdMealReservation(reservation);
                android.util.Log.d("ReservationActivity", "Réservation repas froid sauvegardée localement");
            } else {
                reservationManager.saveReservation(reservation);
                android.util.Log.d("ReservationActivity", "Réservation sauvegardée localement");
            }
        } catch (Exception e) {
            android.util.Log.e("ReservationActivity", "Erreur lors de la sauvegarde locale: " + e.getMessage(), e);
        }
    }
}
