package com.example.projet_tp.model;

import com.google.gson.annotations.SerializedName;

public class Menu {
    @SerializedName("_id")
    private String id;
    private String name;
    private String appetizer;
    private String mainCourse;
    private String dessert;
    private String drink;
    private double price;
    private String comment; // Commentaire de l'administrateur
    private boolean available = true;
    @SerializedName("date")
    private String date;

    public Menu() {}

    public Menu(String id, String name, String appetizer, String mainCourse,
                String dessert, String drink, double price) {
        this.id = id;
        this.name = name;
        this.appetizer = appetizer;
        this.mainCourse = mainCourse;
        this.dessert = dessert;
        this.drink = drink;
        this.price = price;
        this.available = true;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAppetizer() { return appetizer; }
    public void setAppetizer(String appetizer) { this.appetizer = appetizer; }

    public String getMainCourse() { return mainCourse; }
    public void setMainCourse(String mainCourse) { this.mainCourse = mainCourse; }

    public String getDessert() { return dessert; }
    public void setDessert(String dessert) { this.dessert = dessert; }

    public String getDrink() { return drink; }
    public void setDrink(String drink) { this.drink = drink; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}
