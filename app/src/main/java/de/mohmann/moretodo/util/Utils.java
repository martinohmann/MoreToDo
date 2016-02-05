package de.mohmann.moretodo.util;

import android.content.Context;
import android.content.res.Resources;
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
}
