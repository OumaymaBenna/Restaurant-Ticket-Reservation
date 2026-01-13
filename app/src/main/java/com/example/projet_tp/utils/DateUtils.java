package com.example.projet_tp.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    private static final String DATE_FORMAT = "dd/MM/yyyy";
    private static final String TIME_FORMAT = "HH:mm";
    private static final String DATETIME_FORMAT = "dd/MM/yyyy HH:mm";

    public static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.FRANCE);
        return sdf.format(date);
    }

    public static String formatTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, Locale.FRANCE);
        return sdf.format(date);
    }

    public static String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT, Locale.FRANCE);
        return sdf.format(new Date());
    }

    public static String getCurrentDate() {
        return formatDate(new Date());
    }

    public static String getCurrentTime() {
        return formatTime(new Date());
    }
}