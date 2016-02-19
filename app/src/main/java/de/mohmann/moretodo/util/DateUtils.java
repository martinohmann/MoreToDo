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

    public static String getShortDate(final Context context, final Date date) {
        String format = Preferences.getShortDateFormat(context);
        return new SimpleDateFormat(format, Locale.US).format(date);
    }

    public static String getShortDate(final Context context, final long millis) {
        return getShortDate(context, new Date(millis));
    }

    public static String getFullDate(final Context context, final Date date) {
        String format = Preferences.getLongDateFormat(context);
        return new SimpleDateFormat(format, Locale.US).format(date);
    }

    public static String getFullDate(final Context context, final long millis) {
        return getFullDate(context, new Date(millis));
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
            return getShortDate(context, date);
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