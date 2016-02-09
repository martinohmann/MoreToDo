package de.mohmann.moretodo.util;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
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

    public static String shorten(final String string, int len, boolean ellipsis) {
        if (string == null)
            return null;

        if (string.length() > len && len > 0) {
            if (ellipsis) {
                if (len > 3) {
                    return string.substring(0, len - 3) + "...";
                }
                return string.substring(0, len) + "...";
            }
            return string.substring(0, len);
        }
        return string;
    }

    public static Spanned markdownToHtml(String str) {
        return Html.fromHtml(new AndDown().markdownToHtml(str));
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
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }
}
