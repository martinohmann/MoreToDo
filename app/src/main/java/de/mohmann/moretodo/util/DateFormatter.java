package de.mohmann.moretodo.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by mohmann on 2/3/16.
 */
public class DateFormatter {

    final private static String FORMAT = "yyyy-MM-dd HH:mm";
    final private static SimpleDateFormat FORMATTER = new SimpleDateFormat(FORMAT, Locale.US);

    public static String getDateTime(final Date date) {
        return FORMATTER.format(date);
    }

    public static String getDateTime(final long millis) {
        return getDateTime(new Date(millis));
    }

    public static String humanReadable(final Date date) {
        long diff = (System.currentTimeMillis() - date.getTime()) / 1000;

        String humanDate;

        if (diff < 60) {
            humanDate = "just now";
        } else if (diff < 3600) {
            humanDate = String.format("%d minutes ago", diff / 60);
        } else if (diff < 86400) {
            humanDate = String.format("%d hours ago", diff / 3600);
        } else if (diff < 604800) {
            humanDate = String.format("%d days ago", diff / 604800);
        } else {
            humanDate = getDateTime(date);
        }

        return humanDate;
    }

    public static String humanReadable(final long millis) {
        return humanReadable(new Date(millis));
    }
}