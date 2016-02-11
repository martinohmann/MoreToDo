package de.mohmann.moretodo.util;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

/**
 * Created by mohmann on 2/11/16.
 */
final public class ServiceUtils {
    /**
     * Flag used for PendingIntents to identify running state of a service
     */
    final public static int FLAG_SERVICE_RUNNING = 0x539;

    /**
     * Checks if given service is running by checking if a pending intent exists. Works if service
     * is scheduled via AlarmManager.
     *
     * @param context
     * @param serviceClass
     * @return
     */
    public static boolean isServiceRunning(Context context, Class<? extends Service> serviceClass) {
        final Intent intent = new Intent(context, serviceClass);
        return (PendingIntent.getService(context, FLAG_SERVICE_RUNNING, intent,
                PendingIntent.FLAG_NO_CREATE) != null);
    }
}
