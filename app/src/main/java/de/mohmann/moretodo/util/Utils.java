package de.mohmann.moretodo.util;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * Created by mohmann on 2/4/16.
 */
public class Utils {

    public static void toast(final Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void toast(final Context context, int id) {
        Resources res = context.getResources();
        String message = res.getString(id);
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static String shorten(String string, int len) {
        return shorten(string, len, false);
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
}
