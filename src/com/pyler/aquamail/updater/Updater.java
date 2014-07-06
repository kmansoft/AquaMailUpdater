package com.pyler.aquamail.updater;

import java.io.File;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

public class Updater extends Activity {
	public String URL_VERSION = "http://aqua-mail.com/download/xversion-AquaMail-market.txt";
	public String URL_CHANGELOG = "http://pyler.wen.ru/aquamail/changelog.txt";
	public String URL_NEW_VERSION = "http://aqua-mail.com/download/AquaMail-market-%s.apk";
	public String URL_NEW_FILE = "AquaMail-market-%s.apk";
	public String AQUAMAIL_VERSION = "AquaMail %s";
	public String AQUAMAIL_PKG = "org.kman.AquaMail";
	public Button downloadButton;
	public TextView installedVersion;
	public TextView newVersion;
	public TextView downloadInfo;
	public ProgressBar progressBar;
	public String installedVersionName;
	public String newVersionName;
	public String changelog;
	public AlarmManager alarm;
	public boolean isNewVersion;
	public Helper helper = new Helper();
	public int NOTIFICATION_ID = 1;
	public NotificationManager notificationManager;
	public Notification updateNotification;
	public Future<File> downloading;
	public AlertDialog.Builder changelogDialog;
	public SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_updater);
		installedVersion = (TextView) findViewById(R.id.installedVersion);
		newVersion = (TextView) findViewById(R.id.newVersion);
		downloadInfo = (TextView) findViewById(R.id.downloadInfo);
		progressBar = (ProgressBar) findViewById(R.id.downloadProgress);
		downloadButton = (Button) findViewById(R.id.downloadButton);
		downloadButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (downloading != null && !downloading.isCancelled()) {
					resetDownload();
					return;
				}
				downloadButton.setText(android.R.string.cancel);
				downloadUpdate();
			}
		});
		if (!isAquaMailInstalled()) {
			installedVersion
					.setText(getString(R.string.aquamail_not_installed));
			return;
		}

		showAquaMailNewVersion();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.updater, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.changelog:
			showChangelog();
			return true;
		case R.id.settings:
			showSettings();
			return true;
		case R.id.about:
			showAbout();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void showAbout() {
		String title = getString(R.string.app_name);
		if (helper.getAquaMailUpdaterInstalledVersion(this) != null) {
			String version = String.format(" %s",
					helper.getAquaMailUpdaterInstalledVersion(this));
			title += version;
		}
		AlertDialog.Builder aboutDialog = new AlertDialog.Builder(this);
		aboutDialog.setTitle(title);
		aboutDialog.setMessage(R.string.about_info);
		aboutDialog.setCancelable(true);
		aboutDialog.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface aboutDialog, int id) {
						aboutDialog.cancel();
					}
				});
		aboutDialog.create();
		aboutDialog.show();
	}

	public void showSettings() {
		Intent settings = new Intent(this, UpdaterPreferences.class);
		startActivity(settings);
	}

	public String getAquaMailInstalledVersion() {
		return helper.getAquaMailInstalledVersion(this);
	}

	public void showAquaMailNewVersion() {
		installedVersionName = getAquaMailInstalledVersion();
		installedVersion.setText(getString(R.string.installed_version,
				installedVersionName));
		Ion.with(this).load(URL_VERSION).asString()
				.setCallback(new FutureCallback<String>() {
					@Override
					public void onCompleted(Exception e, String result) {
						if (result != null) {
							int start = 16;
							int end = result.length() - 43;
							newVersionName = result.substring(start, end);
							newVersion.setTextColor(Color.GREEN);
							newVersion.setText(getString(R.string.new_version,
									newVersionName));
							if (!newVersionName.equals(installedVersionName)) {
								downloadButton.setVisibility(View.VISIBLE);
							}
							if (changelogEnabled()) {
								showChangelog();
							}
						} else {
							newVersion.setTextColor(Color.RED);
							String unknownVersion = String.format(
									getString(R.string.new_version),
									getString(R.string.unknown));
							newVersion.setText(unknownVersion);
						}

					}
				});

	}

	public boolean isAquaMailInstalled() {
		return helper.isAquaMailInstalled(this);
	}

	public void downloadUpdate() {
		final String updateUrl = String.format(URL_NEW_VERSION, newVersionName);
		final File updateFile = new File(
				Environment.getExternalStorageDirectory() + "/"
						+ String.format(URL_NEW_FILE, newVersionName));
		progressBar.setVisibility(View.VISIBLE);
		downloading = Ion.with(this).load(updateUrl).progressBar(progressBar)
				.progressHandler(new ProgressCallback() {
					@Override
					public void onProgress(long downloaded, long total) {
						String downloadedSize = helper.getFileSize(downloaded);
						String totalSize = helper.getFileSize(total);
						String downloadProgress = getString(
								R.string.download_info, downloadedSize,
								totalSize);
						downloadInfo.setText(downloadProgress);
					}
				}).write(updateFile).setCallback(new FutureCallback<File>() {
					@Override
					public void onCompleted(Exception e, File result) {
						resetDownload();
						if (result != null) {
							Intent intent = new Intent();
							intent.setAction(Intent.ACTION_VIEW);
							intent.setDataAndType(Uri.fromFile(updateFile),
									"application/vnd.android.package-archive");
							startActivity(intent);
						}
					}
				});
	}

	public void resetDownload() {
		downloading.cancel();
		downloading = null;
		downloadButton.setText(getString(R.string.download));
		downloadInfo.setText(null);
		progressBar.setProgress(0);
		progressBar.setVisibility(View.GONE);

	}

	public void showChangelog() {
		changelogDialog = new AlertDialog.Builder(this);
		changelogDialog.setTitle(getString(R.string.changelog));
		changelogDialog.setCancelable(true);
		changelogDialog.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface changelogDialog, int id) {
						changelogDialog.cancel();
					}
				});
		changelogDialog.create();
		Ion.with(this).load(URL_CHANGELOG).asString()
				.setCallback(new FutureCallback<String>() {
					@Override
					public void onCompleted(Exception e, String result) {
						if (result != null) {
							String version = "Version";
							changelog = result.replaceAll(version,
									getString(R.string.version));
							changelogDialog.setMessage(changelog);
							changelogDialog.show();
						} else {
							changelog = getString(R.string.cant_load_changelog);
							changelogDialog.setMessage(changelog);
							changelogDialog.show();
						}
					}
				});
	}

	public boolean changelogEnabled() {
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean changelogEnabled = prefs
				.getBoolean(helper.show_changelog, true);
		return changelogEnabled;
	}
}
