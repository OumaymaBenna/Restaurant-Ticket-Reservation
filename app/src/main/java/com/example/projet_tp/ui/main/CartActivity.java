package com.example.projet_tp.ui.main;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet_tp.R;
import com.example.projet_tp.adapter.CartAdapter;
import com.example.projet_tp.model.Order;
import com.example.projet_tp.model.OrderItem;
import com.example.projet_tp.utils.OrderManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.List;

public class CartActivity extends AppCompatActivity {
    private RecyclerView recyclerViewCart;
    private TextView textViewTotalPrice;
    private TextView textViewEmptyCart;
    private MaterialButton buttonConfirmOrder;
    private MaterialButton buttonClearCart;
    private ProgressBar progressBar;
    private CartAdapter cartAdapter;
    private OrderManager orderManager;
    private Order currentOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        orderManager = OrderManager.getInstance();
        currentOrder = orderManager.getCurrentOrder();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerViewCart = findViewById(R.id.recyclerViewCart);
        textViewTotalPrice = findViewById(R.id.textViewTotalPrice);
        textViewEmptyCart = findViewById(R.id.textViewEmptyCart);
        buttonConfirmOrder = findViewById(R.id.buttonConfirmOrder);
        buttonClearCart = findViewById(R.id.buttonClearCart);
        progressBar = findViewById(R.id.progressBar);

        recyclerViewCart.setLayoutManager(new LinearLayoutManager(this));

        setupCartAdapter();
        setupButtons();
        updateUI();
    }

    private void setupCartAdapter() {
        if (currentOrder != null) {
            cartAdapter = new CartAdapter(currentOrder.getItems(), new CartAdapter.OnItemQuantityChangedListener() {
                @Override
                public void onQuantityChanged(String menuId, int quantity) {
                    orderManager.updateItemQuantity(menuId, quantity);
                    updateUI();
                }

                @Override
                public void onItemRemoved(String menuId) {
                    orderManager.removeItemFromOrder(menuId);
                    updateUI();
                }
            });
            recyclerViewCart.setAdapter(cartAdapter);
        }
    }

    private void setupButtons() {
        buttonConfirmOrder.setOnClickListener(v -> {
            if (orderManager.hasItemsInCart()) {
                orderManager.confirmOrder();
                Toast.makeText(this, "Commande confirmée avec succès!", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, "Votre panier est vide", Toast.LENGTH_SHORT).show();
            }
        });

        buttonClearCart.setOnClickListener(v -> {
            if (orderManager.hasItemsInCart()) {
                orderManager.clearCurrentOrder();
                updateUI();
                Toast.makeText(this, "Panier vidé", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        if (currentOrder == null || currentOrder.getItems().isEmpty()) {
            // Afficher l'état vide
            recyclerViewCart.setVisibility(View.GONE);
            textViewEmptyCart.setVisibility(View.VISIBLE);
            textViewTotalPrice.setVisibility(View.GONE);
            buttonConfirmOrder.setEnabled(false);
            buttonClearCart.setEnabled(false);
        } else {
            // Afficher le contenu
            recyclerViewCart.setVisibility(View.VISIBLE);
            textViewEmptyCart.setVisibility(View.GONE);
            textViewTotalPrice.setVisibility(View.VISIBLE);
            buttonConfirmOrder.setEnabled(true);
            buttonClearCart.setEnabled(true);

            // Mettre à jour le total
            double total = orderManager.getCartTotalPrice();
            DecimalFormat df = new DecimalFormat("#.##");
            textViewTotalPrice.setText("Total: " + df.format(total) + " TND");

            // Rafraîchir l'adaptateur
            if (cartAdapter != null) {
                cartAdapter.updateItems(currentOrder.getItems());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentOrder = orderManager.getCurrentOrder();
        updateUI();
    }
}


