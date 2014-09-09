package com.pyler.aquamail.updater;

import java.util.Scanner;

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
	public String URL_VERSION_BETA = "http://aqua-mail.com/download/xversion-AquaMail-market-beta.txt";
	public String AQUAMAIL_VERSION = "AquaMail %s";
	public String AQUAMAIL_CHANGELOG_TAG = "aqm";
	public String VERSION_TAG = "Version";
	public String TAG = "AquaMail Updater";
	public String installedVersionName;
	public String newVersionName;
	public String check_for_updates = "check_for_updates";
	public String show_changelog = "show_changelog";
	public String version_type = "version_type";
	public String ACTION_NAME = "com.pyler.aquamail.updater.CHECK";
	public Context context;

	public Helper(Context ctx) {
		context = ctx;
	}

	public Context getContext() {
		return context;
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
			PackageInfo pInfo = getContext().getPackageManager()
					.getPackageInfo(AQUAMAIL_PKG, 0);
			versionName = pInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionName;
	}

	public String getAquaMailUpdaterInstalledVersion() {
		String versionName = null;
		try {
			PackageInfo pInfo = getContext().getPackageManager()
					.getPackageInfo(getContext().getPackageName(), 0);
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

	public void debugLog(String message) {
		Log.d(TAG, message);
	}

	public void debugToast(String message) {
		Toast.makeText(getContext(), TAG + ": " + message, Toast.LENGTH_SHORT)
				.show();
	}

	public void enableCheck() {
		Intent enableCheck = new Intent(ACTION_NAME);
		getContext().sendBroadcast(enableCheck);
	}

	public String getLatestVersion(String text) {
		int start = 16;
		int end = text.length() - 43;
		String latestVersion = text.substring(start, end);
		return latestVersion;
	}

	public String getChangelog(String text) {
		int start = text.indexOf("--- changelog:") + 15;
		int end = text.length();
		String changelog = text.substring(start, end);
		changelog = changelog.replaceAll(VERSION_TAG,
				getContext().getString(R.string.version));
		return changelog;
	}

	public String getChanges(String text) {
		int start = text.indexOf("--- scm log:") + 13;
		int end = text.indexOf("--- changelog:") - 1;
		String codeChanges = text.substring(start, end);
		Scanner txt = new Scanner(codeChanges);
		String changes = "";
		String line = "";
		while (txt.hasNextLine()) {
			line = txt.nextLine();
			if (line.contains(AQUAMAIL_CHANGELOG_TAG)) {
				int posStart = line.indexOf(AQUAMAIL_CHANGELOG_TAG) + 4;
				int posEnd = line.length();
				line = line.substring(posStart, posEnd);
				if (line.startsWith("-")) {
					line = "+" + line.substring(1, line.length());
				}
				changes += line + "\n";
			}
		}
		txt.close();
		return changes;
	}

}
