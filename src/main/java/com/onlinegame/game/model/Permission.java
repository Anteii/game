package com.onlinegame.game.model;

public enum Permission {
    BASIC_PERMISSION("basic"),
    MODERATION("moderation");

    private String permission;

    Permission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}
