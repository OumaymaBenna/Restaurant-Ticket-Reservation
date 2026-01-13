package com.example.projet_tp.model;

public class Reservation {
    private String id;
    private String userId;
    private String userEmail;
    private String userName;
    private String menuId;
    private String menuName;
    private String date;
    private String time;
    private int numberOfTickets;
    private double totalPrice;
    private String status;
    private String createdAt;

    public Reservation() {}

    public Reservation(String userId, String userEmail, String userName,
                       String menuId, String menuName, String date, String time,
                       int numberOfTickets, double totalPrice) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.menuId = menuId;
        this.menuName = menuName;
        this.date = date;
        this.time = time;
        this.numberOfTickets = numberOfTickets;
        this.totalPrice = totalPrice;
        this.status = "PENDING";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getMenuId() { return menuId; }
    public void setMenuId(String menuId) { this.menuId = menuId; }

    public String getMenuName() { return menuName; }
    public void setMenuName(String menuName) { this.menuName = menuName; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public int getNumberOfTickets() { return numberOfTickets; }
    public void setNumberOfTickets(int numberOfTickets) { this.numberOfTickets = numberOfTickets; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
