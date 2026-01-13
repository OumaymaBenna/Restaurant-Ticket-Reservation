package com.example.projet_tp.model;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private String id;
    private String userId;
    private String userName;
    private List<OrderItem> items;
    private double totalPrice;
    private String date;
    private String status; // "PENDING", "CONFIRMED", "CANCELLED"

    public Order() {
        this.items = new ArrayList<>();
        this.status = "PENDING";
    }

    public Order(String userId, String userName) {
        this.userId = userId;
        this.userName = userName;
        this.items = new ArrayList<>();
        this.status = "PENDING";
        this.totalPrice = 0.0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
        calculateTotalPrice();
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void addItem(OrderItem item) {
        // Vérifier si l'item existe déjà
        for (OrderItem existingItem : items) {
            if (existingItem.getMenuId().equals(item.getMenuId())) {
                // Augmenter la quantité
                existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                calculateTotalPrice();
                return;
            }
        }
        // Ajouter un nouvel item
        items.add(item);
        calculateTotalPrice();
    }

    public void removeItem(String menuId) {
        items.removeIf(item -> item.getMenuId().equals(menuId));
        calculateTotalPrice();
    }

    public void updateItemQuantity(String menuId, int quantity) {
        for (OrderItem item : items) {
            if (item.getMenuId().equals(menuId)) {
                if (quantity <= 0) {
                    removeItem(menuId);
                } else {
                    item.setQuantity(quantity);
                }
                calculateTotalPrice();
                return;
            }
        }
    }

    public int getTotalItems() {
        int total = 0;
        for (OrderItem item : items) {
            total += item.getQuantity();
        }
        return total;
    }

    private void calculateTotalPrice() {
        totalPrice = 0.0;
        for (OrderItem item : items) {
            totalPrice += item.getTotalPrice();
        }
    }

    public void clear() {
        items.clear();
        totalPrice = 0.0;
    }
}


