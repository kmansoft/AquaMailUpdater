package com.pyler.aquamail.updater;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class UpdaterPreferences extends PreferenceActivity implements
		SharedPreferences.OnSharedPreferenceChangeListener {
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		Helper helper = new Helper();
		if (helper.checkForUpdates.equals(key)) {
			int checkUpdates = Integer.valueOf(prefs.getString(
					helper.checkForUpdates, "-1"));
			AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			if (checkUpdates != -1) {
				Intent serviceIntent = new Intent(this,
						UpdateReceiver.class);
				serviceIntent.setAction(helper.action_name);
				PendingIntent pendingIntent = PendingIntent.getBroadcast(
						this, 1, serviceIntent, 0);
				alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP,
						System.currentTimeMillis(), checkUpdates
								* AlarmManager.INTERVAL_HOUR, pendingIntent);
				helper.enableCheck(this);
			}
		}
	}
}