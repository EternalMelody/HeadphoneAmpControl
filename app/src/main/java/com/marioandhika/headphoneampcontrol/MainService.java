package com.marioandhika.headphoneampcontrol;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class MainService extends Service {

	/**
	 * Code to signal ongoing notification
	 */
	public static final int ONGOING_NOTIFICATION = 9999;

	/**
	 * Intent key for indicating headset status
	 */
	public static final String HEADSET_STATUS = "HeadsetStatus";

	private SharedPreferences sp;
	private AudioManager audioManager;
	private HeadphoneAmpControl headphoneAmpControl = (HeadphoneAmpControl) getApplication();

	// Headset status broadcast receiver
	private MainReceiver receiver;

	// Current headset status
	private int headsetStatus;

	// Intent filter and receiver to intercept volume change
	private VolumeChangeReceiver volumeChangeReceiver;
	private IntentFilter volumeChangeIntentFilter;

	// Amount of volume change to be applied on volume button press
	private Integer hackLevelJump;

	// Whether or not the service is in the foreground
	private boolean isForeground;

	@Override
	public void onCreate() {
		// Get current volumes
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		isForeground = false;

		// Get shared preferences
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);

		// Set up main receiver
		receiver = new MainReceiver();
		registerReceiver(receiver, filter);

		volumeChangeIntentFilter = new IntentFilter();
		volumeChangeReceiver = new VolumeChangeReceiver();
		volumeChangeIntentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int intentHeadsetStatus;

		if (intent != null) {
			intentHeadsetStatus = intent.getIntExtra(HEADSET_STATUS, MainReceiver.UNALTERED);

			if (intentHeadsetStatus != MainReceiver.UNALTERED) {
				headsetStatus = intentHeadsetStatus;
			}
		} else {
			intentHeadsetStatus = MainReceiver.UNALTERED;
		}

		switch (intentHeadsetStatus) {
			case MainReceiver.UNPLUGGED:
				deactivateNotification();
				break;
			case MainReceiver.PLUGGED:
				activateNotification();
				break;
			case MainReceiver.UNALTERED:
				updateNotification();
				break;
			case MainActivity.TOGGLE_VOLUME_HACK:
				toggleVolumeHack();
				break;
		}

		return Service.START_STICKY;
	}

	/**
	 * Handles toggling of volume button hack
	 */
	private void toggleVolumeHack() {
		if (isForeground) {
			// Initialize volume levels to max-1 if hack is enabled, otherwise unregister volume change receiver
			if (sp.getBoolean(MainActivity.CHECKBOX_VOLUME_BUTTON_HACK, false)) {
				registerReceiver(volumeChangeReceiver, volumeChangeIntentFilter);
				hackLevelJump = sp.getInt(MainActivity.SEEKBAR_VOLUME_BUTTON_HACK, 1) + 1;

				if (sp.getBoolean(MainActivity.CHECKBOX_MUSIC_HACK, false)) {
					audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) - 1, AudioManager.FLAG_SHOW_UI);
				}
				if (sp.getBoolean(MainActivity.CHECKBOX_VOICE_CALL_HACK, false)) {
					audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL) - 1, AudioManager.FLAG_SHOW_UI);
				}
				if (sp.getBoolean(MainActivity.CHECKBOX_RING_HACK, false)) {
					audioManager.setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamMaxVolume(AudioManager.STREAM_RING) - 1, AudioManager.FLAG_SHOW_UI);
				}
			} else {
				if (volumeChangeReceiver != null) {
					try {
						unregisterReceiver(volumeChangeReceiver);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

	}

	// Updates notification
	private void updateNotification() {
		if (headsetStatus == MainReceiver.PLUGGED) {
			headphoneAmpControl.getLevelsFromFile();
			buildNotification(false);
		}
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(receiver);
		if (volumeChangeReceiver != null) {
			try {
				unregisterReceiver(volumeChangeReceiver);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// Build notification
	private void buildNotification(boolean isForeground) {
		Intent notificationIntent = new Intent(this, DialogActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification noti = new Notification.Builder(this)
				.setContentTitle("Headphone Amp")
				.setContentText("Amp level: L:" + headphoneAmpControl.getCurrentLevelL() + " R:" + headphoneAmpControl.getCurrentLevelR())
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentIntent(pendingIntent)
				.setContentInfo("")
				.setOngoing(true)
				.setWhen(0)
				.build();

		if (isForeground) {
			startForeground(ONGOING_NOTIFICATION, noti);
		} else {
			NotificationManager nm =
					(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

			nm.notify(ONGOING_NOTIFICATION, noti);
		}

	}

	// Activate persistent notification
	private void activateNotification() {
		headphoneAmpControl.getLevelsFromFile();

		int currentLevelL = headphoneAmpControl.getCurrentLevelL();
		int currentLevelR = headphoneAmpControl.getCurrentLevelR();

		boolean isSafetyChecked = sp.getBoolean(MainActivity.CHECKBOX_SAFETY_CHECKED, false);

		// Enforces safety levels
		if (isSafetyChecked) {
			int safetyLevel = sp.getInt(MainActivity.SEEKBAR_SAFETY_LEVEL, MainActivity.MIN_LEVEL);

			if (currentLevelL > safetyLevel || currentLevelR > safetyLevel) {

				if (currentLevelL > safetyLevel) {
					currentLevelL = safetyLevel;
				}
				if (currentLevelR > safetyLevel) {
					currentLevelR = safetyLevel;
				}
				Toast.makeText(this, "HP Amp set to safety levels L&R: " + safetyLevel, Toast.LENGTH_SHORT).show();

				headphoneAmpControl.setLevel(currentLevelL, currentLevelR);
			}
		}

		buildNotification(true);

		// Register volume change listener
		if (sp.getBoolean(MainActivity.CHECKBOX_VOLUME_BUTTON_HACK, false)) {
			registerReceiver(volumeChangeReceiver, volumeChangeIntentFilter);
			hackLevelJump = sp.getInt(MainActivity.SEEKBAR_VOLUME_BUTTON_HACK, 1) + 1;

			if (sp.getBoolean(MainActivity.CHECKBOX_MUSIC_HACK, false)) {
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) - 1, AudioManager.FLAG_SHOW_UI);
			}
			if (sp.getBoolean(MainActivity.CHECKBOX_VOICE_CALL_HACK, false)) {
				audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL) - 1, AudioManager.FLAG_SHOW_UI);
			}
			if (sp.getBoolean(MainActivity.CHECKBOX_RING_HACK, false)) {
				audioManager.setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamMaxVolume(AudioManager.STREAM_RING) - 1, AudioManager.FLAG_SHOW_UI);
			}
		}

		isForeground = true;
	}

	// Deactivates persistent notification
	private void deactivateNotification() {
		stopForeground(true);
		if (volumeChangeReceiver != null) {
			try {
				unregisterReceiver(volumeChangeReceiver);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		isForeground = false;
	}

	private void launchDialogActivity(int decreaseOrIncrease) {
		// Launch dialog activity
		Intent intent = new Intent(getApplicationContext(), DialogActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		intent.putExtra(DialogActivity.DECREASE_OR_INCREASE, decreaseOrIncrease);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		startActivity(intent);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	// Decrease volume level
	private void decreaseLevel() {
		headphoneAmpControl.setLevel(headphoneAmpControl.getCurrentLevelL() - hackLevelJump, headphoneAmpControl.getCurrentLevelR() - hackLevelJump);
		updateNotification();
		Intent intent = new Intent(DialogActivity.ACTION_REFRESH_HEADPHONE_LEVEL_SEEKBAR);
		sendBroadcast(intent);
	}

	// Increase volume level
	private void increaseLevel() {
		headphoneAmpControl.setLevel(headphoneAmpControl.getCurrentLevelL() + hackLevelJump, headphoneAmpControl.getCurrentLevelR() + hackLevelJump);
		updateNotification();
		Intent intent = new Intent(DialogActivity.ACTION_REFRESH_HEADPHONE_LEVEL_SEEKBAR);
		sendBroadcast(intent);
	}

	/**
	 * Listens to digital volume changes, and intercepts it
	 */
	public class VolumeChangeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			// Intercept music volume if hack is enabled
			if (sp.getBoolean(MainActivity.CHECKBOX_MUSIC_HACK, false)) {
				interceptVolumeChange(AudioManager.STREAM_MUSIC);
			}

			// Intercept voice call volume if hack is enabled
			if (sp.getBoolean(MainActivity.CHECKBOX_VOICE_CALL_HACK, false)) {
				interceptVolumeChange(AudioManager.STREAM_VOICE_CALL);
			}

			// Intercept ring volume if hack is enabled
			if (sp.getBoolean(MainActivity.CHECKBOX_RING_HACK, false)) {
				interceptVolumeChange(AudioManager.STREAM_RING);
			}

		}

		// Ensures that digital volume is not adjusted, and adjust analog volume instead
		private void interceptVolumeChange(int streamType) {
			int currentVolume = audioManager.getStreamVolume(streamType);

			if (audioManager.getStreamVolume(streamType) != currentVolume) {

				if (audioManager.getStreamVolume(streamType) >= currentVolume) {
					audioManager.setStreamVolume(streamType, audioManager.getStreamMaxVolume(streamType) - 1, AudioManager.FLAG_SHOW_UI);
					launchDialogActivity(DialogActivity.INCREASE);
					increaseLevel();
				} else if (audioManager.getStreamVolume(streamType) < currentVolume) {

					int currentLevelL = headphoneAmpControl.getCurrentLevelL();
					int currentLevelR = headphoneAmpControl.getCurrentLevelR();
					int minLevel = sp.getInt(MainActivity.SEEKBAR_MIN_LEVEL, 0);

					if (currentLevelL > minLevel && currentLevelR > minLevel) {
						audioManager.setStreamVolume(streamType, audioManager.getStreamMaxVolume(streamType) - 1, AudioManager.FLAG_SHOW_UI);
						launchDialogActivity(DialogActivity.DECREASE);
						decreaseLevel();
					}
				}
			}
		}
	}
}
