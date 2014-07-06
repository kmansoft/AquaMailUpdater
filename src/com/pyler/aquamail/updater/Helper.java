package com.pyler.aquamail.updater;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

public class Helper {
	public String AQUAMAIL_PKG = "org.kman.AquaMail";
	public String URL_VERSION = "http://aqua-mail.com/download/xversion-AquaMail-market.txt";
	public String AQUAMAIL_VERSION = "AquaMail %s";
	public String TAG = "AquaMail Updater";
	public String installedVersionName;
	public String newVersionName;
	public boolean isNewVersion;
	public String checkForUpdates = "check_for_updates";
	public String show_changelog = "show_changelog";
	public String action_name = "com.pyler.aquamail.updater.ENABLE_CHECK";

	public boolean isAquaMailInstalled(Context ctx) {
		PackageManager packageManager = ctx.getPackageManager();
		boolean isInstalled = false;
		try {
			packageManager.getPackageInfo(AQUAMAIL_PKG,
					PackageManager.GET_ACTIVITIES);
			isInstalled = true;
		} catch (PackageManager.NameNotFoundException e) {
		}
		return isInstalled;
	}

	public String getAquaMailInstalledVersion(Context ctx) {
		String versionName = ctx.getString(R.string.unknown);
		try {
			PackageInfo pInfo = ctx.getPackageManager().getPackageInfo(
					AQUAMAIL_PKG, 0);
			versionName = pInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionName;
	}

	public String getAquaMailUpdaterInstalledVersion(Context ctx) {
		String versionName = null;
		try {
			PackageInfo pInfo = ctx.getPackageManager().getPackageInfo(
					ctx.getPackageName(), 0);
			versionName = pInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionName;
	}

	@SuppressLint("DefaultLocale")
	public String getFileSize(long bytes) {
		int unit = 1024;
		if (bytes < unit) {
			return bytes + " B";
		}
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = "KMGTPE".charAt(exp - 1) + "";
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	public void debugLog(String msg) {
		Log.d(TAG, msg);
	}

	public void debugToast(Context ctx, String msg) {
		Toast.makeText(ctx, TAG + ": " + msg, Toast.LENGTH_SHORT).show();
	}

	public void enableCheck(Context ctx) {
		Intent enableCheck = new Intent(action_name);
		ctx.sendBroadcast(enableCheck);
	}
}
