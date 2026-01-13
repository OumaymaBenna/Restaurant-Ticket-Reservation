package com.example.projet_tp.utils;

import com.example.projet_tp.model.Order;
import com.example.projet_tp.model.OrderItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderManager {
    private static OrderManager instance;
    private Order currentOrder;
    private List<Order> orderHistory;

    private OrderManager() {
        this.orderHistory = new ArrayList<>();
    }

    public static OrderManager getInstance() {
        if (instance == null) {
            instance = new OrderManager();
        }
        return instance;
    }

    public Order getCurrentOrder() {
        return currentOrder;
    }

    public void createNewOrder(String userId, String userName) {
        this.currentOrder = new Order(userId, userName);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        this.currentOrder.setDate(sdf.format(new Date()));
    }

    public void addItemToOrder(String menuId, String menuName, double price, int quantity, String date) {
        if (currentOrder == null) {
            return;
        }
        OrderItem item = new OrderItem(menuId, menuName, price, quantity, date);
        currentOrder.addItem(item);
    }

    public void removeItemFromOrder(String menuId) {
        if (currentOrder != null) {
            currentOrder.removeItem(menuId);
        }
    }

    public void updateItemQuantity(String menuId, int quantity) {
        if (currentOrder != null) {
            currentOrder.updateItemQuantity(menuId, quantity);
        }
    }

    public void confirmOrder() {
        if (currentOrder != null && !currentOrder.getItems().isEmpty()) {
            currentOrder.setStatus("CONFIRMED");
            orderHistory.add(currentOrder);
            // Créer une nouvelle commande pour les prochains ajouts
            String userId = currentOrder.getUserId();
            String userName = currentOrder.getUserName();
            createNewOrder(userId, userName);
        }
    }

    public void cancelOrder() {
        if (currentOrder != null) {
            currentOrder.setStatus("CANCELLED");
            orderHistory.add(currentOrder);
            // Créer une nouvelle commande
            String userId = currentOrder.getUserId();
            String userName = currentOrder.getUserName();
            createNewOrder(userId, userName);
        }
    }

    public void clearCurrentOrder() {
        if (currentOrder != null) {
            currentOrder.clear();
        }
    }

    public List<Order> getOrderHistory() {
        return orderHistory;
    }

    public int getCartItemCount() {
        if (currentOrder == null) {
            return 0;
        }
        return currentOrder.getTotalItems();
    }

    public double getCartTotalPrice() {
        if (currentOrder == null) {
            return 0.0;
        }
        return currentOrder.getTotalPrice();
    }

    public boolean hasItemsInCart() {
        return currentOrder != null && !currentOrder.getItems().isEmpty();
    }
}


