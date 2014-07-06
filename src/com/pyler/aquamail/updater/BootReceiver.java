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
	Helper helper = new Helper();

	@Override
	public void onReceive(Context context, Intent intent) {
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		int checkUpdates = Integer.valueOf(prefs.getString(
				helper.checkForUpdates, "-1"));
		String action = intent.getAction();
		if (Intent.ACTION_BOOT_COMPLETED.equals(action) && checkUpdates != -1) {
			Intent serviceIntent = new Intent(context, UpdateReceiver.class);
			serviceIntent.setAction(helper.action_name);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
					1, serviceIntent, 0);
			alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP,
					System.currentTimeMillis(), checkUpdates
							* AlarmManager.INTERVAL_HOUR, pendingIntent);
			helper.enableCheck(context);

		}
	}
}