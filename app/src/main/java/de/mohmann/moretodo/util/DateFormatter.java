package de.mohmann.moretodo.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by mohmann on 2/3/16.
 */
public class DateFormatter {

    final private static String SHORT_FORMAT = "yyyy-MM-dd  HH:mm";
    final private static String LONG_FORMAT = "EEE, MMMM dd yyyy  HH:mm";
    final private static SimpleDateFormat SHORT_FORMATTER = new SimpleDateFormat(SHORT_FORMAT, Locale.US);
    final private static SimpleDateFormat LONG_FORMATTER = new SimpleDateFormat(LONG_FORMAT, Locale.US);

    public static String getShortDate(final Date date) {
        return SHORT_FORMATTER.format(date);
    }

    public static String getShortDate(final long millis) {
        return getShortDate(new Date(millis));
    }

    public static String getFullDate(final Date date) {
        return LONG_FORMATTER.format(date);
    }

    public static String getFullDate(final long millis) {
        return getFullDate(new Date(millis));
    }

    private static String pluralSuffix(long number) {
        if (number != 1)
            return "s";
        return "";
    }

    public static String humanReadable(final Date date) {
        long diff = (System.currentTimeMillis() - date.getTime()) / 1000;

        String format = "%d %s%s ago";
        String humanDate;
        long adjusted;

        if (diff < 0) {
            diff = -diff;
            format = "in %d %s%s";
        }

        if (diff > 604800) {
             humanDate = getShortDate(date);
        } else if (diff > 86400) {
             adjusted = diff / 86400;
             humanDate = String.format(format, adjusted, "day", pluralSuffix(adjusted));
        } else if (diff > 3600) {
             adjusted = diff / 3600;
             humanDate = String.format(format, adjusted, "hour", pluralSuffix(adjusted));
        } else if (diff > 60) {
             adjusted = diff / 60;
             humanDate = String.format(format, adjusted, "minute", pluralSuffix(adjusted));
        } else {
             humanDate = "just now";
        }

        return humanDate;
    }

    public static String humanReadable(final long millis) {
        return humanReadable(new Date(millis));
    }
}