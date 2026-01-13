package com.example.projet_tp.model;

public class Session {
    private static final long SESSION_DURATION = 3600000; // 1 heure

    private String userId;
    private String token;
    private User user;
    private long loginTime;

    public Session() {}

    public Session(String userId, String token, User user, long loginTime) {
        this.userId = userId;
        this.token = token;
        this.user = user;
        this.loginTime = loginTime;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public long getLoginTime() { return loginTime; }
    public void setLoginTime(long loginTime) { this.loginTime = loginTime; }

    public boolean isSessionExpired() {
        return System.currentTimeMillis() - loginTime > SESSION_DURATION;
    }

    @Override
    public String toString() {
        return "Session{" +
                "userId='" + userId + '\'' +
                ", token='" + token + '\'' +
                ", user=" + user +
                ", loginTime=" + loginTime +
                '}';
    }
}
