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
}