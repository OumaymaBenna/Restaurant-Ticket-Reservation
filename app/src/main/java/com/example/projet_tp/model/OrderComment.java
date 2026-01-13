package com.example.projet_tp.model;

import com.google.gson.annotations.SerializedName;

public class OrderComment {
    @SerializedName("studentId")
    private String studentId;
    
    @SerializedName("userName")
    private String userName;
    
    @SerializedName("mealType")
    private String mealType; // "Déjeuner", "Dîner", "Repas froid"
    
    @SerializedName("comment")
    private String comment;
    
    @SerializedName("createdAt")
    private String createdAt;
    
    @SerializedName("_id")
    private String id;

    public OrderComment() {}

    public OrderComment(String studentId, String userName, String mealType, String comment) {
        this.studentId = studentId;
        this.userName = userName;
        this.mealType = mealType;
        this.comment = comment;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

