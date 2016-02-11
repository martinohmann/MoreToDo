package de.mohmann.moretodo.util;

import android.content.Context;
import android.content.res.Resources;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.mohmann.moretodo.R;

/**
 * Created by mohmann on 2/3/16.
 */
final public class DateUtils {

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

    public static String humanReadable(final Context context, final Date date) {
        long diff = (System.currentTimeMillis() - date.getTime()) / 1000;
        Resources res = context.getResources();
        String format = res.getString(R.string.date_format_human_past);

        if (diff < 0) {
            diff = -diff;
            format = res.getString(R.string.date_format_human_future);
        }

        if (diff > 604800) {
            return getShortDate(date);
        } else if (diff > 86400) {
            diff /= 86400;
            return String.format(format, diff,
                    Utils.pluralize(context, diff, R.string.date_day, R.string.date_days));
        } else if (diff > 3600) {
            diff /= 3600;
            return String.format(format, diff,
                    Utils.pluralize(context, diff, R.string.date_hour, R.string.date_hours));
        } else if (diff > 60) {
            diff /= 60;
            return String.format(format, diff,
                    Utils.pluralize(context, diff, R.string.date_minute, R.string.date_minutes));
        }
        return res.getString(R.string.date_just_now);
    }

    public static String humanReadable(final Context context, final long millis) {
        return humanReadable(context, new Date(millis));
    }
}