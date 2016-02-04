package de.mohmann.moretodo.util;

import android.content.Context;
import android.content.res.Resources;
import android.widget.Toast;

import de.mohmann.moretodo.R;

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
}
