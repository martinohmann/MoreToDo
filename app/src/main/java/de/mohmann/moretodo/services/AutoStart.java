package de.mohmann.moretodo.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by mohmann on 2/8/16.
 */
public class AutoStart extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // start notification service on boot
        context.startService(new Intent(context, BackgroundService.class));
    }
}
