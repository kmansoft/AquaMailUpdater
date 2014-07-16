package com.pyler.aquamail.updater;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class UpdateReceiver extends BroadcastReceiver {
	public int NOTIFICATION_ID = 1;
	public NotificationManager notificationManager;
	public Notification updateNotification;
	public String newVersionName;
	public String installedVersionName;
	public Helper helper;
	public SharedPreferences prefs;
	public int releaseType;
	@Override
	public void onReceive(final Context context, Intent intent) {
		helper = new Helper(context);
		String action = intent.getAction();
		if (helper.ACTION_NAME.equals(action)) {
		    prefs = PreferenceManager.getDefaultSharedPreferences(context);
			releaseType = Integer.valueOf(prefs
				.getString(helper.release_type, "0"));
			String URL;
			if (releaseType==1) {
			   URL = helper.URL_VERSION_BETA;
		    }
			else {
			   URL = helper.URL_VERSION;
			}
			installedVersionName = helper.getAquaMailInstalledVersion();
			Ion.with(context).load(URL).asString()
					.setCallback(new FutureCallback<String>() {
						@Override
						public void onCompleted(Exception e, String result) {
							if (result != null) {
								newVersionName = helper.getNewVersion(result);
								if (!newVersionName
										.equals(installedVersionName)) {
									Intent openIntent = new Intent(context,
											Updater.class);
									PendingIntent pendingIntent = PendingIntent
											.getActivity(
													context,
													0,
													openIntent,
													Intent.FLAG_ACTIVITY_NEW_TASK);
									updateNotification = new NotificationCompat.Builder(
											context)
											.setContentTitle(
													String.format(
															helper.AQUAMAIL_VERSION,
															newVersionName))
											.setContentText(
													context.getString(R.string.new_version_available))
											.setWhen(System.currentTimeMillis())
											.setContentIntent(pendingIntent)
											.setSmallIcon(android.R.drawable.stat_sys_download_done)
											.build();

									notificationManager = (NotificationManager) context
											.getSystemService(Context.NOTIFICATION_SERVICE);
									notificationManager.notify(NOTIFICATION_ID,
											updateNotification);
								}

							}

						}
					});
		}
	}
}