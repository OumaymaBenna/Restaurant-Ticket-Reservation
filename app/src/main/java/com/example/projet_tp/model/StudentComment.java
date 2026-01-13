package com.example.projet_tp.model;

import com.google.gson.annotations.SerializedName;

public class StudentComment {
    @SerializedName("_id")
    private String id;
    @SerializedName("menuId")
    private String menuId;
    @SerializedName("studentId")
    private String studentId;
    @SerializedName("userName")
    private String userName;
    @SerializedName("comment")
    private String comment;
    @SerializedName("createdAt")
    private String createdAt;

    public StudentComment() {}

    public StudentComment(String menuId, String studentId, String userName, String comment) {
        this.menuId = menuId;
        this.studentId = studentId;
        this.userName = userName;
        this.comment = comment;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
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
}


