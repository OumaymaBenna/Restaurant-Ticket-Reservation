package com.example.projet_tp.model;

public class User {
    private String id;
    private String fullName;
    private String email;
    private String studentId;
    private String password;
    private String phone;
    private String university;
    private String role;
    private String adminCode;
    private boolean isBlocked;
    private String blockedUntil;
    private double subscriptionBalance;

    public User() {}

    public User(String fullName, String email, String studentId, String password) {
        this.fullName = fullName;
        this.email = email;
        this.studentId = studentId;
        this.password = password;
        this.role = "etudiant";
    }

    public User(String fullName, String email, String studentId, String password, String role) {
        this.fullName = fullName;
        this.email = email;
        this.studentId = studentId;
        this.password = password;
        this.role = role != null ? role : "etudiant";
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getUniversity() { return university; }
    public void setUniversity(String university) { this.university = university; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAdminCode() { return adminCode; }
    public void setAdminCode(String adminCode) { this.adminCode = adminCode; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public double getSubscriptionBalance() { return subscriptionBalance; }
    public void setSubscriptionBalance(double subscriptionBalance) { this.subscriptionBalance = subscriptionBalance; }

    public boolean isBlocked() { return isBlocked; }
    public void setBlocked(boolean blocked) { this.isBlocked = blocked; }

    public String getBlockedUntil() { return blockedUntil; }
    public void setBlockedUntil(String blockedUntil) { this.blockedUntil = blockedUntil; }

    @Override
    public String toString() {
        return "User{" +
                "fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", studentId='" + studentId + '\'' +
                ", role='" + role + '\'' +
                ", university='" + university + '\'' +
                '}';
    }
}
