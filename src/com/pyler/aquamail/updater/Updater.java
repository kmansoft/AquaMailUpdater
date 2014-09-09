package com.pyler.aquamail.updater;

import java.io.File;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

public class Updater extends Activity {
	public String URL_CHANGELOG = "http://aqua-mail.com/download/AquaMail-market-%s.apk-changes.txt";
	public String URL_VERSION = "http://aqua-mail.com/download/xversion-AquaMail-market.txt";
	public String URL_VERSION_BETA = "http://aqua-mail.com/download/xversion-AquaMail-market-beta.txt";
	public String URL_NEW_VERSION = "http://aqua-mail.com/download/AquaMail-market-%s.apk";
	public String AQUAMAIL_APK_FILE = "AquaMail-market-%s.apk";
	public String AQUAMAIL_VERSION = "AquaMail %s";
	public String AQUAMAIL_PKG = "org.kman.AquaMail";
	public Button downloadButton;
	public TextView installedVersion;
	public TextView latestVersion;
	public TextView downloadInfo;
	public ProgressBar progressBar;
	public String installedVersionName;
	public String latestVersionName;
	public String changelog;
	public String apkFile;
	public AlarmManager alarm;
	public Helper helper = new Helper(this);
	public Future<File> downloading;
	public AlertDialog.Builder changelogDialog;
	public ProgressDialog checkingUpdates;
	public ProgressDialog loadingChangelog;
	public SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_updater);
		installedVersion = (TextView) findViewById(R.id.installedVersion);
		latestVersion = (TextView) findViewById(R.id.latestVersion);
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
		if (!helper.isAquaMailInstalled()) {
			installedVersion
					.setText(getString(R.string.aquamail_not_installed));
			return;
		}
		deleteOldAquaMailInstallationApk();
		showAquaMailLatestVersion();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.updater, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.refresh:
			refreshApp();
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

	public void refreshApp() {
		this.recreate();
	}

	public void showAbout() {
		String title = getString(R.string.app_name);
		if (helper.getAquaMailUpdaterInstalledVersion() != null) {
			String version = String.format(" %s",
					helper.getAquaMailUpdaterInstalledVersion());
			title += version;
		}
		AlertDialog.Builder aboutDialog = new AlertDialog.Builder(this);
		aboutDialog.setTitle(title);
		aboutDialog.setMessage(R.string.about_info);
		aboutDialog.setCancelable(true);
		aboutDialog.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface aboutDialog, int id) {
						aboutDialog.dismiss();
					}
				});
		aboutDialog.create();
		aboutDialog.show();
	}

	public void showSettings() {
		Intent settings = new Intent(this, Settings.class);
		startActivity(settings);
	}

	public void showAquaMailLatestVersion() {
		String urlVersion = null;
		if (getVersionType() == 1) {
			urlVersion = helper.URL_VERSION_BETA;
		} else {
			urlVersion = helper.URL_VERSION;
		}
		installedVersionName = helper.getAquaMailInstalledVersion();
		installedVersion.setText(getString(R.string.installed_version,
				installedVersionName));
		checkingUpdates = new ProgressDialog(this);
		checkingUpdates.setMessage(getString(R.string.checking_for_updates));
		checkingUpdates.show();
		Ion.with(this).load(urlVersion).asString()
				.setCallback(new FutureCallback<String>() {
					@Override
					public void onCompleted(Exception e, String result) {
						checkingUpdates.dismiss();
						if (result != null) {
							latestVersionName = helper.getLatestVersion(result);
							latestVersion.setTextColor(Color.GREEN);
							latestVersion
									.setText(getString(R.string.latest_version,
											latestVersionName));
							if (!latestVersionName.equals(installedVersionName)) {
								downloadButton.setVisibility(View.VISIBLE);
								if (getChangelog() == 0) {
									showChangelog();
								}
							}
						} else {
							latestVersion.setTextColor(Color.RED);
							String unknownVersion = String.format(
									getString(R.string.latest_version),
									getString(R.string.unknown));
							latestVersion.setText(unknownVersion);
						}

					}
				});

	}

	public void downloadUpdate() {
		final String updateUrl = String.format(URL_NEW_VERSION,
				latestVersionName);
		final File updateFile = new File(getExternalFilesDir(null)
				+ File.separator
				+ String.format(AQUAMAIL_APK_FILE, latestVersionName));
		apkFile = updateFile.toString();
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
							showFileToast();
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
						changelogDialog.dismiss();
					}
				});
		changelogDialog.create();
		loadingChangelog = new ProgressDialog(this);
		loadingChangelog.setMessage(getString(R.string.loading_changelog));
		loadingChangelog.show();
		String urlChangelog = String.format(URL_CHANGELOG, latestVersionName);
		Ion.with(this).load(urlChangelog).asString()
				.setCallback(new FutureCallback<String>() {
					@Override
					public void onCompleted(Exception e, String result) {
						loadingChangelog.dismiss();
						if (result != null) {
							changelog = getString(R.string.version) + " "
									+ latestVersionName + "\n\n";
							changelog += helper.getChanges(result) + "\n";
							changelog += helper.getChangelog(result);
						} else {
							changelog = getString(R.string.cant_load_changelog);
						}
						changelogDialog.setMessage(changelog);
						changelogDialog.show();
					}
				});
	}

	public int getChangelog() {
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		int showChangelog = Integer.valueOf(prefs.getString(
				helper.show_changelog, "0"));
		return showChangelog;
	}

	public int getVersionType() {
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		int versionType = Integer.valueOf(prefs.getString(helper.version_type,
				"0"));
		return versionType;
	}

	public void deleteOldAquaMailInstallationApk() {
		File oldApkFile = new File(getExternalFilesDir(null)
				+ File.separator
				+ String.format(AQUAMAIL_APK_FILE,
						helper.getAquaMailInstalledVersion()));
		if (oldApkFile.exists()) {
			oldApkFile.delete();
		}
	}

	public void showFileToast() {
		Toast.makeText(this, apkFile, Toast.LENGTH_LONG).show();
	}
}
