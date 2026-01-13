package com.example.projet_tp.model;

public class OrderItem {
    private String menuId;
    private String menuName;
    private double price;
    private int quantity;
    private String date;

    public OrderItem() {}

    public OrderItem(String menuId, String menuName, double price, int quantity, String date) {
        this.menuId = menuId;
        this.menuName = menuName;
        this.price = price;
        this.quantity = quantity;
        this.date = date;
    }

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getTotalPrice() {
        return price * quantity;
    }
}


