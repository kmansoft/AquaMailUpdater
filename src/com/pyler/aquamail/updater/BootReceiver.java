package com.pyler.aquamail.updater;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class BootReceiver extends BroadcastReceiver {
    public SharedPreferences prefs;
    public AlarmManager alarm;
    public Helper helper;

    @Override
    public void onReceive(Context context, Intent intent) {
        helper = new Helper(context);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int checkForUpdates = Integer.valueOf(prefs.getString(
                helper.check_for_updates, "1"));
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                && checkForUpdates != -1) {
            Intent serviceIntent = new Intent(context, Checker.class);
            serviceIntent.setAction(helper.ACTION_NAME);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    1, serviceIntent, 0);
            alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis(), checkForUpdates
                            * AlarmManager.INTERVAL_HOUR, pendingIntent);
            helper.enableCheck();

        }
    }
}