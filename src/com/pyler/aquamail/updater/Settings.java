package com.pyler.aquamail.updater;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Settings extends PreferenceActivity implements
		SharedPreferences.OnSharedPreferenceChangeListener {
	Helper helper = new Helper(this);

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		if (helper.check_for_updates.equals(key)) {
			int checkForUpdates = Integer.valueOf(prefs.getString(
					helper.check_for_updates, "1"));
			AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			if (checkForUpdates != -1) {
				Intent serviceIntent = new Intent(this, Checker.class);
				serviceIntent.setAction(helper.ACTION_NAME);
				PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
						1, serviceIntent, 0);
				alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP,
						System.currentTimeMillis(), checkForUpdates
								* AlarmManager.INTERVAL_HOUR, pendingIntent);
				helper.enableCheck();
			}
		}

	}
}