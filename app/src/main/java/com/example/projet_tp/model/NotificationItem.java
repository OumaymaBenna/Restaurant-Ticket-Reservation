package com.example.projet_tp.model;

import java.io.Serializable;

public class NotificationItem implements Serializable {
    private String id;
    private String title;
    private String message;
    private String menuName;
    private String menuId;
    private long timestamp;
    private boolean read;

    public NotificationItem() {
        this.timestamp = System.currentTimeMillis();
        this.read = false;
    }

    public NotificationItem(String title, String message, String menuName, String menuId) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.title = title;
        this.message = message;
        this.menuName = menuName;
        this.menuId = menuId;
        this.timestamp = System.currentTimeMillis();
        this.read = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}



