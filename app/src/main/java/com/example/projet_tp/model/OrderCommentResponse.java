package com.example.projet_tp.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OrderCommentResponse {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("orders")
    private List<OrderComment> orders;
    
    @SerializedName("count")
    private int count;
    
    @SerializedName("message")
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<OrderComment> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderComment> orders) {
        this.orders = orders;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

