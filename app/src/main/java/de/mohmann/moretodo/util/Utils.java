package de.mohmann.moretodo.util;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.commonsware.cwac.anddown.AndDown;

/**
 * Created by mohmann on 2/4/16.
 */
public class Utils {

    public static void toast(final Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void toast(final Context context, int id) {
        String message = context.getResources().getString(id);
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void toast(final Context context, String format, Object...params) {
        toast(context, String.format(format, params));
    }

    public static void toast(final Context context, int id, Object...params) {
        String format = context.getResources().getString(id);
        toast(context, format, params);
    }

    public static String shorten(String string, int len) {
        return shorten(string, len, true);
    }

    public static String shorten(String string, int len, boolean ellipsis) {
        if (string == null)
            return null;

        if (string.length() > len && len > 0) {
            if (ellipsis) {
                return string.substring(0, len) + "\u2026";
            }
            return string.substring(0, len);
        }
        return string;
    }

    public static String pluralize(long number, String singular, String plural) {
        if (number != 1)
            return plural;
        return singular;
    }

    public static String pluralize(final Context context, long number, int singularId,
                                   int pluralId) {
        return pluralize(number, context.getResources().getString(singularId),
                context.getResources().getString(pluralId));
    }

    public static CharSequence markdownToHtml(String str) {
        return trimTrailingWhitespace(
                Html.fromHtml(new AndDown().markdownToHtml(str), null, new ListTagHandler()));
    }

    public static boolean isNotEmpty(CharSequence str) {
        return !isEmpty(str);
    }

    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    public static void setMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(l + " cannot be cast to int " +
                    "without changing its value.");
        }
        return (int) l;
    }

    /**
     * taken from stackoverflow: http://stackoverflow.com/a/10187511
     *
     * Trims trailing whitespace. Removes any of these characters:
     * 0009, HORIZONTAL TABULATION
     * 000A, LINE FEED
     * 000B, VERTICAL TABULATION
     * 000C, FORM FEED
     * 000D, CARRIAGE RETURN
     * 001C, FILE SEPARATOR
     * 001D, GROUP SEPARATOR
     * 001E, RECORD SEPARATOR
     * 001F, UNIT SEPARATOR
     * @return "" if source is null, otherwise string with all trailing whitespace removed
     */
    public static CharSequence trimTrailingWhitespace(CharSequence source) {
        if(source == null)
            return "";

        int i = source.length();
        // loop back to the first non-whitespace character
        while (--i >= 0 && Character.isWhitespace(source.charAt(i)));
        return source.subSequence(0, i+1);
    }
}
