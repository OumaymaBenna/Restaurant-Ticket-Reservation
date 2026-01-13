package com.example.projet_tp.ui.reservation;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet_tp.R;
import com.example.projet_tp.adapter.ReservationAdapter;
import com.example.projet_tp.api.MealReservationAPI;
import com.example.projet_tp.model.Reservation;
import com.example.projet_tp.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ReservationListActivity extends AppCompatActivity {

    private static final String TAG = "ReservationListActivity";
    private RecyclerView recyclerViewReservations;
    private LinearLayout emptyState;
    private ReservationAdapter adapter;
    private List<Reservation> reservationList;
    private SessionManager sessionManager;
    private MealReservationAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_list);

        sessionManager = new SessionManager(this);
        api = new MealReservationAPI(this);
        reservationList = new ArrayList<>();

        initViews();
        setupToolbar();
        
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Veuillez vous connecter", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadReservations();
    }

    private void initViews() {
        recyclerViewReservations = findViewById(R.id.recyclerViewReservations);
        emptyState = findViewById(R.id.emptyState);
        
        recyclerViewReservations.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReservationAdapter(reservationList);
        recyclerViewReservations.setAdapter(adapter);
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void loadReservations() {
        String studentId = sessionManager.getUserId();
        
        if (studentId == null || studentId.isEmpty()) {
            Log.e(TAG, "studentId est vide");
            Toast.makeText(this, "Erreur: ID étudiant manquant", Toast.LENGTH_SHORT).show();
            showEmptyState();
            return;
        }

        Log.d(TAG, "🔄 Chargement des réservations pour studentId: " + studentId);
        Log.d(TAG, "   Email utilisateur: " + sessionManager.getEmail());

        // Charger les réservations normales
        api.getUserReservations(studentId, new MealReservationAPI.ReservationCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    Log.d(TAG, "✅ Réponse API réservations normales: " + response.toString());
                    List<Reservation> normalReservations = parseReservations(response);
                    Log.d(TAG, "📋 Réservations normales parsées: " + normalReservations.size());
                    List<Reservation> todayReservations = filterTodayReservations(normalReservations);
                    Log.d(TAG, "📅 Réservations d'aujourd'hui: " + todayReservations.size());
                    reservationList.addAll(todayReservations);
                    
                    api.getUserColdMealReservations(studentId, new MealReservationAPI.ReservationCallback() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            try {
                                Log.d(TAG, "✅ Réponse API réservations repas froid: " + response.toString());
                                List<Reservation> coldMealReservations = parseReservations(response);
                                Log.d(TAG, "📋 Réservations repas froid parsées: " + coldMealReservations.size());
                                
                                // Afficher toutes les réservations de repas froid (pas de filtrage par date)
                                // car ils sont généralement réservés pour le samedi
                                for (Reservation r : coldMealReservations) {
                                    Log.d(TAG, "   - Repas froid brut: " + r.getMenuName() + " | Date: " + r.getDate() + " | Status: " + r.getStatus() + " | ID: " + r.getId());
                                }
                                
                                // Filtrer uniquement les repas froids valides (non utilisés, non annulés)
                                List<Reservation> validColdReservations = new ArrayList<>();
                                for (Reservation r : coldMealReservations) {
                                    String status = r.getStatus();
                                    if (status == null || status.isEmpty()) {
                                        status = "RESERVED";
                                    }
                                    
                                    // Ajouter tous les repas froids sauf ceux utilisés ou annulés
                                    if (!status.equalsIgnoreCase("USED") && 
                                        !status.equalsIgnoreCase("CANCELLED") && 
                                        !status.equalsIgnoreCase("CANCELED") &&
                                        !status.equalsIgnoreCase("EXPIRED")) {
                                        validColdReservations.add(r);
                                        Log.d(TAG, "✅ Repas froid valide ajouté: " + r.getMenuName());
                                    } else {
                                        Log.d(TAG, "❌ Repas froid ignoré (status: " + status + "): " + r.getMenuName());
                                    }
                                }
                                
                                Log.d(TAG, "📅 Réservations repas froid valides: " + validColdReservations.size());
                                
                                // Ajouter directement à la liste
                                reservationList.addAll(validColdReservations);
                                Log.d(TAG, "📊 Total réservations dans la liste (avant déduplication): " + reservationList.size());
                                
                                // Supprimer les doublons (mais être plus permissif pour les repas froids)
                                removeDuplicates();
                                Log.d(TAG, "📊 Total réservations après déduplication: " + reservationList.size());
                                
                                // Vérifier combien de repas froids sont dans la liste finale
                                int finalColdCount = 0;
                                for (Reservation r : reservationList) {
                                    if (r.getMenuName() != null && 
                                        (r.getMenuName().toLowerCase().contains("froid") || 
                                         r.getMenuName().toLowerCase().contains("cold"))) {
                                        finalColdCount++;
                                        Log.d(TAG, "🍽️ Repas froid dans liste finale: " + r.getMenuName() + " | Date: " + r.getDate());
                                    }
                                }
                                Log.d(TAG, "🍽️ Nombre total de repas froids dans la liste finale: " + finalColdCount);
                                
                                updateUI();
                            } catch (Exception e) {
                                Log.e(TAG, "❌ Erreur parsing réservations repas froid", e);
                                updateUI();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "❌ Erreur chargement réservations repas froid: " + error);
                            updateUI();
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "❌ Erreur parsing réservations normales", e);
                    e.printStackTrace();
                    showEmptyState();
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Erreur chargement réservations: " + error);
                Toast.makeText(ReservationListActivity.this, 
                    "Erreur: " + error, Toast.LENGTH_LONG).show();
                showEmptyState();
            }
        });
    }

    private List<Reservation> parseReservations(JSONObject response) throws JSONException {
        List<Reservation> reservations = new ArrayList<>();
        
        Log.d(TAG, "🔍 Parsing des réservations...");
        Log.d(TAG, "   Clés disponibles: " + response.keys().toString());
        
        if (response.has("reservations") && response.get("reservations") instanceof JSONArray) {
            JSONArray reservationsArray = response.getJSONArray("reservations");
            Log.d(TAG, "   Format: reservations array, taille: " + reservationsArray.length());
            
            for (int i = 0; i < reservationsArray.length(); i++) {
                JSONObject reservationJson = reservationsArray.getJSONObject(i);
                Reservation reservation = parseReservation(reservationJson);
                if (reservation != null) {
                    reservations.add(reservation);
                    Log.d(TAG, "   ✅ Réservation ajoutée: " + reservation.getMenuName() + " - " + reservation.getDate());
                }
            }
        } else if (response.has("reservation") && response.get("reservation") instanceof JSONObject) {
            Log.d(TAG, "   Format: reservation object");
            Reservation reservation = parseReservation(response.getJSONObject("reservation"));
            if (reservation != null) {
                reservations.add(reservation);
            }
        } else if (response.has("data") && response.get("data") instanceof JSONArray) {
            JSONArray reservationsArray = response.getJSONArray("data");
            Log.d(TAG, "   Format: data array, taille: " + reservationsArray.length());
            for (int i = 0; i < reservationsArray.length(); i++) {
                JSONObject reservationJson = reservationsArray.getJSONObject(i);
                Reservation reservation = parseReservation(reservationJson);
                if (reservation != null) {
                    reservations.add(reservation);
                }
            }
        } else {
            Log.w(TAG, "   ⚠️ Format de réponse non reconnu");
            Log.w(TAG, "   Réponse complète: " + response.toString());
        }
        
        Log.d(TAG, "   Total réservations parsées: " + reservations.size());
        return reservations;
    }

    private Reservation parseReservation(JSONObject json) {
        try {
            Reservation reservation = new Reservation();
            
            // Parser l'ID (priorité à _id puis id)
            if (json.has("_id")) {
                String id = json.getString("_id");
                reservation.setId(id);
                Log.d(TAG, "   Parsing réservation ID (_id): " + id);
            } else if (json.has("id")) {
                String id = json.getString("id");
                reservation.setId(id);
                Log.d(TAG, "   Parsing réservation ID (id): " + id);
            } else {
                Log.w(TAG, "   ⚠️ Aucun ID trouvé pour la réservation");
            }
            
            if (json.has("userId")) reservation.setUserId(json.getString("userId"));
            if (json.has("userEmail")) reservation.setUserEmail(json.getString("userEmail"));
            if (json.has("userName")) reservation.setUserName(json.getString("userName"));
            
            if (json.has("menuId")) reservation.setMenuId(json.getString("menuId"));
            
            // Parser le type de repas (mealType ou menuName)
            if (json.has("mealType")) {
                String mealType = json.getString("mealType");
                reservation.setMenuName(mealType);
                Log.d(TAG, "   Type de repas (mealType): " + mealType);
            } else if (json.has("menuName")) {
                String menuName = json.getString("menuName");
                reservation.setMenuName(menuName);
                Log.d(TAG, "   Type de repas (menuName): " + menuName);
            } else {
                Log.w(TAG, "   ⚠️ Aucun type de repas trouvé");
            }
            
            // Parser la date (reservationDate ou date)
            if (json.has("reservationDate")) {
                reservation.setDate(json.getString("reservationDate"));
            } else if (json.has("date")) {
                reservation.setDate(json.getString("date"));
            }
            
            if (json.has("time")) reservation.setTime(json.getString("time"));
            
            if (json.has("price")) {
                reservation.setTotalPrice(json.getDouble("price"));
            } else if (json.has("totalPrice")) {
                reservation.setTotalPrice(json.getDouble("totalPrice"));
            }
            
            if (json.has("numberOfTickets")) {
                reservation.setNumberOfTickets(json.getInt("numberOfTickets"));
            } else {
                reservation.setNumberOfTickets(1);
            }
            
            if (json.has("status")) {
                reservation.setStatus(json.getString("status"));
            } else {
                reservation.setStatus("RESERVED");
            }
            
            if (json.has("createdAt")) reservation.setCreatedAt(json.getString("createdAt"));
            
            String mealType = reservation.getMenuName();
            boolean isColdMeal = mealType != null && 
                (mealType.toLowerCase().contains("froid") || 
                 mealType.toLowerCase().contains("cold"));
            
            Log.d(TAG, "   ✅ Réservation parsée: " + mealType + 
                      " | Date: " + reservation.getDate() + 
                      " | Statut: " + reservation.getStatus() +
                      " | ID: " + reservation.getId() +
                      (isColdMeal ? " [REPAS FROID]" : ""));
            
            return reservation;
        } catch (JSONException e) {
            Log.e(TAG, "❌ Erreur parsing réservation: " + e.getMessage(), e);
            Log.e(TAG, "   JSON: " + json.toString());
            return null;
        }
    }

    private void updateUI() {
        runOnUiThread(() -> {
            // Compter les types de réservations
            int normalCount = 0;
            int coldMealCount = 0;
            for (Reservation r : reservationList) {
                if (r.getMenuName() != null) {
                    String mealType = r.getMenuName();
                    if (mealType.contains("Froid") || mealType.contains("froid")) {
                        coldMealCount++;
                    } else {
                        normalCount++;
                    }
                }
            }
            Log.d(TAG, "📊 Mise à jour UI - Déjeuner/Dîner: " + normalCount + " | Repas Froid: " + coldMealCount + " | Total: " + reservationList.size());
            
            if (reservationList.isEmpty()) {
                showEmptyState();
            } else {
                hideEmptyState();
                adapter.notifyDataSetChanged();
                Log.d(TAG, "✅ Affichage de " + reservationList.size() + " réservations dans le RecyclerView");
            }
        });
    }

    private void showEmptyState() {
        if (recyclerViewReservations != null) {
            recyclerViewReservations.setVisibility(View.GONE);
        }
        if (emptyState != null) {
            emptyState.setVisibility(View.VISIBLE);
        }
    }

    private void hideEmptyState() {
        if (recyclerViewReservations != null) {
            recyclerViewReservations.setVisibility(View.VISIBLE);
        }
        if (emptyState != null) {
            emptyState.setVisibility(View.GONE);
        }
    }


    private List<Reservation> filterTodayReservations(List<Reservation> reservations) {
        List<Reservation> todayReservations = new ArrayList<>();
        java.util.Calendar today = java.util.Calendar.getInstance();
        today.set(java.util.Calendar.HOUR_OF_DAY, 0);
        today.set(java.util.Calendar.MINUTE, 0);
        today.set(java.util.Calendar.SECOND, 0);
        today.set(java.util.Calendar.MILLISECOND, 0);
        
        java.text.SimpleDateFormat[] formats = {
            new java.text.SimpleDateFormat("EEEE dd/MM/yyyy", java.util.Locale.FRENCH),
            new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()),
            new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()),
            new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        };
        
        for (Reservation reservation : reservations) {
            if (reservation.getDate() != null && !reservation.getDate().isEmpty()) {
                try {
                    java.util.Date reservationDate = null;
                    String reservationDateStr = reservation.getDate();
                    
                    for (java.text.SimpleDateFormat format : formats) {
                        try {
                            reservationDate = format.parse(reservationDateStr);
                            break;
                        } catch (java.text.ParseException e) {
                        }
                    }
                    
                    if (reservationDate != null) {
                        java.util.Calendar reservationCal = java.util.Calendar.getInstance();
                        reservationCal.setTime(reservationDate);
                        reservationCal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                        reservationCal.set(java.util.Calendar.MINUTE, 0);
                        reservationCal.set(java.util.Calendar.SECOND, 0);
                        reservationCal.set(java.util.Calendar.MILLISECOND, 0);
                        
                        if (reservationCal.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR) &&
                            reservationCal.get(java.util.Calendar.DAY_OF_YEAR) == today.get(java.util.Calendar.DAY_OF_YEAR)) {
                            todayReservations.add(reservation);
                            Log.d(TAG, "✅ Réservation d'aujourd'hui: " + reservation.getMenuName() + " - " + reservation.getDate());
                        } else {
                            Log.d(TAG, "❌ Réservation passée ignorée: " + reservation.getMenuName() + " - " + reservation.getDate());
                        }
                    } else {
                        Log.w(TAG, "⚠️ Impossible de parser la date: " + reservation.getDate());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erreur lors du filtrage de la réservation: " + reservation.getDate(), e);
                }
            }
        }
        
        return todayReservations;
    }

    /**
     * Filtre les réservations de repas froid valides (non utilisées et non annulées)
     * Les repas froids sont généralement réservés pour le samedi, donc on affiche toutes les réservations valides
     */
    private List<Reservation> filterValidColdMealReservations(List<Reservation> reservations) {
        List<Reservation> validReservations = new ArrayList<>();
        
        Log.d(TAG, "🔍 Filtrage de " + reservations.size() + " réservations repas froid");
        
        for (Reservation reservation : reservations) {
            if (reservation == null) {
                Log.w(TAG, "⚠️ Réservation null ignorée");
                continue;
            }
            
            // Vérifier que c'est bien un repas froid
            String mealType = reservation.getMenuName();
            if (mealType == null || mealType.isEmpty()) {
                mealType = reservation.getMenuName();
            }
            
            Log.d(TAG, "   Vérification: " + mealType + " | Date: " + reservation.getDate() + " | ID: " + reservation.getId());
            
            // Vérifier que la réservation n'est pas utilisée ou annulée
            String status = reservation.getStatus();
            if (status == null || status.isEmpty()) {
                status = "RESERVED"; // Par défaut, considérer comme réservée
            }
            
            // Afficher les réservations qui sont RESERVED, PENDING, ou ACTIVE
            // Ne pas afficher celles qui sont USED, CANCELLED, ou EXPIRED
            if (!status.equalsIgnoreCase("USED") && 
                !status.equalsIgnoreCase("CANCELLED") && 
                !status.equalsIgnoreCase("CANCELED") &&
                !status.equalsIgnoreCase("EXPIRED")) {
                validReservations.add(reservation);
                Log.d(TAG, "✅ Réservation repas froid valide ajoutée: " + mealType + " - " + reservation.getDate() + " (Status: " + status + ")");
            } else {
                Log.d(TAG, "❌ Réservation repas froid ignorée (status: " + status + "): " + mealType);
            }
        }
        
        Log.d(TAG, "✅ Total réservations repas froid valides: " + validReservations.size());
        return validReservations;
    }

    /**
     * Supprime les doublons de réservations basés sur l'ID ou la combinaison date + type de repas
     */
    private void removeDuplicates() {
        List<Reservation> uniqueReservations = new ArrayList<>();
        java.util.Set<String> seenIds = new java.util.HashSet<>();
        java.util.Set<String> seenDateType = new java.util.HashSet<>();
        
        Log.d(TAG, "🔍 Déduplication de " + reservationList.size() + " réservations");
        
        for (Reservation reservation : reservationList) {
            if (reservation == null) {
                continue;
            }
            
            String id = reservation.getId();
            String date = reservation.getDate();
            String mealType = reservation.getMenuName(); // mealType est stocké dans menuName
            
            Log.d(TAG, "   Vérification: " + mealType + " | Date: " + date + " | ID: " + id);
            
            // Vérifier d'abord par ID si disponible
            if (id != null && !id.isEmpty()) {
                if (!seenIds.contains(id)) {
                    seenIds.add(id);
                    uniqueReservations.add(reservation);
                    Log.d(TAG, "✅ Réservation unique ajoutée (ID): " + id + " - " + mealType);
                } else {
                    Log.d(TAG, "❌ Doublon détecté et supprimé (ID): " + id + " - " + mealType);
                }
            } else {
                // Si pas d'ID, vérifier par combinaison date + type de repas
                // Pour les repas froids, utiliser seulement l'ID ou un identifiant unique
                String dateTypeKey = (date != null ? date : "") + "_" + (mealType != null ? mealType : "");
                
                // Pour les repas froids, être plus permissif pour éviter de supprimer des réservations valides
                boolean isColdMeal = mealType != null && 
                    (mealType.toLowerCase().contains("froid") || 
                     mealType.toLowerCase().contains("cold") ||
                     mealType.toLowerCase().contains("repas froid"));
                
                if (isColdMeal) {
                    // Pour les repas froids, utiliser l'ID si disponible, sinon createdAt, sinon une clé unique
                    String uniqueKey;
                    if (id != null && !id.isEmpty()) {
                        uniqueKey = id; // Utiliser l'ID si disponible (même si vide dans le if précédent, on réessaie)
                    } else if (reservation.getCreatedAt() != null && !reservation.getCreatedAt().isEmpty()) {
                        uniqueKey = reservation.getCreatedAt() + "_" + mealType;
                    } else {
                        // Clé unique avec timestamp pour éviter les collisions
                        uniqueKey = dateTypeKey + "_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
                    }
                    
                    if (!seenDateType.contains(uniqueKey)) {
                        seenDateType.add(uniqueKey);
                        uniqueReservations.add(reservation);
                        Log.d(TAG, "✅ Réservation repas froid unique ajoutée: " + mealType + " - " + date + " (Key: " + uniqueKey.substring(0, Math.min(50, uniqueKey.length())) + ")");
                    } else {
                        Log.d(TAG, "❌ Doublon repas froid détecté et ignoré: " + uniqueKey.substring(0, Math.min(50, uniqueKey.length())));
                    }
                } else {
                    // Pour les autres repas, utiliser la logique normale
                    if (!seenDateType.contains(dateTypeKey)) {
                        seenDateType.add(dateTypeKey);
                        uniqueReservations.add(reservation);
                        Log.d(TAG, "✅ Réservation unique ajoutée (Date+Type): " + dateTypeKey);
                    } else {
                        Log.d(TAG, "❌ Doublon détecté et supprimé (Date+Type): " + dateTypeKey);
                    }
                }
            }
        }
        
        int duplicatesRemoved = reservationList.size() - uniqueReservations.size();
        if (duplicatesRemoved > 0) {
            Log.d(TAG, "🗑️ " + duplicatesRemoved + " doublon(s) supprimé(s)");
        }
        
        // Compter les repas froids dans la liste finale
        int coldMealCount = 0;
        for (Reservation r : uniqueReservations) {
            if (r.getMenuName() != null && (r.getMenuName().contains("Froid") || r.getMenuName().contains("froid"))) {
                coldMealCount++;
            }
        }
        Log.d(TAG, "🍽️ Nombre de repas froids dans la liste finale: " + coldMealCount);
        
        reservationList.clear();
        reservationList.addAll(uniqueReservations);
        Log.d(TAG, "📊 Total réservations uniques: " + reservationList.size());
    }

    @Override
    protected void onResume() {
        super.onResume();
        reservationList.clear();
        loadReservations();
    }
}
