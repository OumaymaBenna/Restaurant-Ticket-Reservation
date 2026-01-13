package com.example.projet_tp.utils;

import android.text.TextUtils;
import android.util.Patterns;

public class ValidationUtils {

    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password) && password.length() >= 6;
    }

    public static boolean isValidStudentId(String studentId) {
        return !TextUtils.isEmpty(studentId) && studentId.length() >= 5;
    }

    public static boolean isValidFullName(String fullName) {
        return !TextUtils.isEmpty(fullName) && fullName.trim().length() >= 3;
    }

    public static boolean isNotEmpty(String text) {
        return !TextUtils.isEmpty(text) && !text.trim().isEmpty();
    }
}