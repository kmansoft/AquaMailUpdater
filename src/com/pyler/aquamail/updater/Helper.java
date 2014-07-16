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
	public String URL_CHANGELOG = "http://aquamailupdater.wen.ru/changelog.txt";
	public String URL_VERSION = "http://aqua-mail.com/download/xversion-AquaMail-market.txt";
	public String URL_VERSION_BETA = "http://aqua-mail.com/download/xversion-AquaMail-market-beta.txt";
	public String AQUAMAIL_VERSION = "AquaMail %s";
	public String TAG = "AquaMail Updater";
	public String installedVersionName;
	public String newVersionName;
	public String check_for_updates = "check_for_updates";
	public String show_changelog = "show_changelog";
	public String release_type = "release_type";
	public String ACTION_NAME = "com.pyler.aquamail.updater.ENABLE_CHECK";
    public Context context;
	public Helper(Context ctx) {
		this.context = ctx;
	}
	public Context getContext() {
		return this.context;
	}
	public boolean isAquaMailInstalled() {
		PackageManager packageManager = getContext().getPackageManager();
		boolean isInstalled = false;
		try {
			packageManager.getPackageInfo(AQUAMAIL_PKG,
					PackageManager.GET_ACTIVITIES);
			isInstalled = true;
		} catch (PackageManager.NameNotFoundException e) {
		}
		return isInstalled;
	}

	public String getAquaMailInstalledVersion() {
		String versionName = getContext().getString(R.string.unknown);
		try {
			PackageInfo pInfo = getContext().getPackageManager().getPackageInfo(
					AQUAMAIL_PKG, 0);
			versionName = pInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionName;
	}

	public String getAquaMailUpdaterInstalledVersion() {
		String versionName = null;
		try {
			PackageInfo pInfo = getContext().getPackageManager().getPackageInfo(
					getContext().getPackageName(), 0);
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

	public void debugToast(String msg) {
		Toast.makeText(getContext(), TAG + ": " + msg, Toast.LENGTH_SHORT).show();
	}

	public void enableCheck() {
		Intent enableCheck = new Intent(ACTION_NAME);
		getContext().sendBroadcast(enableCheck);
	}
	
	public String getNewVersion(String text) {
		int start = 16;
		int end = text.length() - 43;
		String newVersion = text.substring(start, end);
		return newVersion;
	}
}
