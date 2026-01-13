package com.example.projet_tp.model;

import java.util.List;

public class MenuResponse {
    private boolean success;
    private List<Menu> menus;

    public MenuResponse() {}

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<Menu> getMenus() {
        return menus;
    }

    public void setMenus(List<Menu> menus) {
        this.menus = menus;
    }
}



