package com.pyler.aquamail.updater;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Scanner;

public class Helper {
    public String AQUAMAIL_PKG = "org.kman.AquaMail";
    public String URL_VERSION_STABLE = "http://aqua-mail.com/download/xversion-AquaMail-market.txt";
    public String URL_VERSION_BETA = "http://aqua-mail.com/download/xversion-AquaMail-market-beta.txt";
    public String STABLE_VERSION_ID = "stable_version";
    public String BETA_VERSION_ID = "beta_version";
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

	private static final int SUFFIX_COMMIT_HASH_LEN = 12;
	private static final String SUFFIX_STABLE = "-stable";
	private static final String SUFFIX_DEV = "-dev";

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
		final int start = text.indexOf('\t');
		if (start != -1) {
			final int end = text.indexOf('\t', start+1);
			if (start + 1 < end) {
				return text.substring(start+1, end);
			}
		}
        return "";
    }

	public boolean isUpdate(String newVersion, String installedVersion) {
		if (!newVersion.equals(installedVersion)) {
			final String newVersionCleaned = cleanVersionNameForCompare(newVersion);
			final String installedVersionCleaned = cleanVersionNameForCompare(installedVersion);
			return !newVersionCleaned.equals(installedVersionCleaned);
		}
		return false;
	}

    public String getChangelog(String text) {
		final int startChangeLog = text.indexOf("--- changelog:");
		if (startChangeLog == -1) {
			// No scm / changelog, just use the whole thing
			return text;
		}

		int start = startChangeLog + 15;
        int end = text.length();
        String changelog = text.substring(start, end);
        changelog = changelog.replaceAll(VERSION_TAG,
                getContext().getString(R.string.version));
        return changelog;
    }

    public String getChanges(String text) {
		final int startScmLog = text.indexOf("--- scm log:");
		final int startChangeLog = text.indexOf("--- changelog:");

		if (startScmLog == -1 || startChangeLog == -1 || startScmLog > startChangeLog) {
			// No scm / changelog, just use the whole thing
			return text;
		}

        int start = startScmLog + 13;
        int end = startChangeLog - 1;
        String codeChanges = text.substring(start, end);
        Scanner txt = new Scanner(codeChanges);
        String changes = "";
        String line;
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
            if (line.startsWith("  ")) {
                int posStart = 2;
                int posEnd = line.length();
                line = "+ " + line.substring(posStart, posEnd);
                changes += line + "\n";
            }
        }
        txt.close();
        return changes;
    }

    public String cleanVersionName(String version) {
        return cleanVersionNameForCompare(version);
    }
	private String cleanVersionNameForCompare(String version) {
		version = removeVersionCommitHash(version);

		for (String suffix : new String[] {SUFFIX_STABLE, SUFFIX_DEV}) {
			if (version.endsWith(suffix)) {
				version = version.substring(0, version.length() - suffix.length());
				break;
			}
		}

		return version;
	}

	private String removeVersionCommitHash(String version) {
		// Remove commit hash, should be 12 characters
		for (int i = version.length() - 1; i >= 0; --i) {
			final char ch = version.charAt(i);
			if (ch == '-') {
				if (version.length() - i >= SUFFIX_COMMIT_HASH_LEN) {
					version = version.substring(0, i);
				}
				break;
			} else if (ch >= '0' && ch <= '9' || ch >= 'a' && ch <= 'f' || ch >= 'A' && ch <= 'F') {
				// Valid
			} else {
				// Not valid
				break;
			}
		}

		return version;
	}
}
