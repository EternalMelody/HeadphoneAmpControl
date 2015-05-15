package com.marioandhika.headphoneampcontrol;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * Preferences activity
 */
public class MainActivity extends Activity implements OnCheckedChangeListener, OnSeekBarChangeListener {

	private SharedPreferences sp;
	private SharedPreferences.Editor spe;

	private MainFragment mainFragment;

	// Views
	private CheckBox checkBoxToggleSafety;
	private CheckBox checkBoxVolumeButtonHack;
	private SeekBar seekBarMinLevel;
	private SeekBar seekBarMaxLevel;
	private SeekBar seekBarSafetyLevel;
	private TextView textViewMinLevel;
	private TextView textViewMaxLevel;
	private TextView textViewSafetyLevel;
	private SeekBar seekBarVolumeButtonHack;
	private TextView textViewVolumeButtonHack;
	private CheckBox checkBoxMusicHack;
	private CheckBox checkBoxVoiceCallHack;
	private CheckBox checkBoxRingHack;
	private CheckBox checkBoxToggleService;

	/**
	 * Minimum volume level accepted by the kernel
	 */
	public static final int MIN_LEVEL = 0;

	/**
	 * Maximum volume level accepted by the kernel
	 */
	public static final int MAX_LEVEL = 63;

	/**
	 * Preference keys
	 */
	public static final String CHECKBOX_SERVICE_CHECKED = "checkbox_service_checked";
	public static final String CHECKBOX_SAFETY_CHECKED = "checkbox_safety_checked";
	public static final String SEEKBAR_MIN_LEVEL = "seekbar_min_level";
	public static final String SEEKBAR_MAX_LEVEL = "seekbar_max_level";
	public static final String SEEKBAR_SAFETY_LEVEL = "seekbar_safety_level";
	public static final String CHECKBOX_BALANCE = "checkbox_balance";
	public static final String CHECKBOX_VOLUME_BUTTON_HACK = "checkbox_volume_button_hack";
	public static final String SEEKBAR_VOLUME_BUTTON_HACK = "seekbar_volume_button_hack";
	public static final String CHECKBOX_RING_HACK = "checkbox_ring_hack";
	public static final String CHECKBOX_VOICE_CALL_HACK = "checkbox_voice_call_hack";
	public static final String CHECKBOX_MUSIC_HACK = "checkbox_music_hack";

	/**
	 * Intent code to indicate toggling of volume hack to the main service
	 */
	public static final int TOGGLE_VOLUME_HACK = 9;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Get views and set view listeners
		checkBoxToggleService = (CheckBox) findViewById(R.id.checkBox_toggleService);
		checkBoxToggleSafety = (CheckBox) findViewById(R.id.checkBox_toggleSafetyLevel);
		checkBoxToggleService.setOnCheckedChangeListener(this);
		checkBoxToggleSafety.setOnCheckedChangeListener(this);
		checkBoxVolumeButtonHack = (CheckBox) findViewById(R.id.checkBox_VolumeButtonHack);
		checkBoxVolumeButtonHack.setOnCheckedChangeListener(this);
		checkBoxMusicHack = (CheckBox) findViewById(R.id.checkBox_musicHack);
		checkBoxVoiceCallHack = (CheckBox) findViewById(R.id.checkBox_voiceCallHack);
		checkBoxRingHack = (CheckBox) findViewById(R.id.checkBox_ringHack);
		checkBoxMusicHack.setOnCheckedChangeListener(this);
		checkBoxVoiceCallHack.setOnCheckedChangeListener(this);
		checkBoxRingHack.setOnCheckedChangeListener(this);

		seekBarMinLevel = (SeekBar) findViewById(R.id.seekBar_minLevel);
		seekBarMaxLevel = (SeekBar) findViewById(R.id.seekBar_maxLevel);
		seekBarSafetyLevel = (SeekBar) findViewById(R.id.seekBar_safetyLevel);
		seekBarVolumeButtonHack = (SeekBar) findViewById(R.id.seekBar_VolumeButtonHack);
		seekBarMinLevel.setOnSeekBarChangeListener(this);
		seekBarMaxLevel.setOnSeekBarChangeListener(this);
		seekBarSafetyLevel.setOnSeekBarChangeListener(this);
		seekBarVolumeButtonHack.setOnSeekBarChangeListener(this);

		textViewMinLevel = (TextView) findViewById(R.id.textView_minLevel);
		textViewMaxLevel = (TextView) findViewById(R.id.textView_maxLevel);
		textViewSafetyLevel = (TextView) findViewById(R.id.textView_safetyLevel);
		textViewVolumeButtonHack = (TextView) findViewById(R.id.textView_VolumeButtonHack);

		// Initialize views based on preferences
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		spe = sp.edit();

		boolean isToggleServiceChecked = sp.getBoolean(CHECKBOX_SERVICE_CHECKED, false);
		boolean isToggleSafetyChecked = sp.getBoolean(CHECKBOX_SAFETY_CHECKED, false);
		boolean isVolumeButtonHackChecked = sp.getBoolean(CHECKBOX_VOLUME_BUTTON_HACK, false);
		int minLevel = sp.getInt(SEEKBAR_MIN_LEVEL, MIN_LEVEL);
		int maxLevel = sp.getInt(SEEKBAR_MAX_LEVEL, MAX_LEVEL);
		int safetyLevel = sp.getInt(SEEKBAR_SAFETY_LEVEL, MIN_LEVEL);
		int hackLevelJump = sp.getInt(SEEKBAR_VOLUME_BUTTON_HACK, 1);
		boolean isMusicHackChecked = sp.getBoolean(CHECKBOX_MUSIC_HACK, false);
		boolean isVoiceCallHackChecked = sp.getBoolean(CHECKBOX_VOICE_CALL_HACK, false);
		boolean isRingHackChecked = sp.getBoolean(CHECKBOX_RING_HACK, false);

		checkBoxMusicHack.setChecked(isMusicHackChecked);
		checkBoxVoiceCallHack.setChecked(isVoiceCallHackChecked);
		checkBoxRingHack.setChecked(isRingHackChecked);
		checkBoxVolumeButtonHack.setChecked(isVolumeButtonHackChecked);
		checkBoxVolumeButtonHack.setEnabled(isToggleServiceChecked);
		checkBoxToggleService.setChecked(isToggleServiceChecked);
		checkBoxToggleSafety.setChecked(isToggleSafetyChecked);
		checkBoxToggleSafety.setEnabled(isToggleServiceChecked);

		checkBoxMusicHack.setEnabled(checkBoxToggleService.isChecked() && checkBoxVolumeButtonHack.isChecked());
		checkBoxVoiceCallHack.setEnabled(checkBoxToggleService.isChecked() && checkBoxVolumeButtonHack.isChecked());
		checkBoxRingHack.setEnabled(checkBoxToggleService.isChecked() && checkBoxVolumeButtonHack.isChecked());

		seekBarVolumeButtonHack.setProgress(hackLevelJump);
		seekBarMaxLevel.setProgress(maxLevel);
		seekBarMinLevel.setProgress(minLevel);
		seekBarSafetyLevel.setProgress(safetyLevel);
		textViewMinLevel.setText("Min level: " + minLevel);
		textViewMaxLevel.setText("Max level: " + maxLevel);
		textViewSafetyLevel.setText("Safety level: " + safetyLevel);
		textViewVolumeButtonHack.setText("Amp level jump: " + (hackLevelJump + 1));

		// Get main fragment
		FragmentManager fm = getFragmentManager();
		mainFragment = (MainFragment) fm.findFragmentById(R.id.main_fragment);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// Toggle activation of foreground service
	private void toggleService(boolean isChecked) {
		// Store preference
		spe.putBoolean(CHECKBOX_SERVICE_CHECKED, isChecked);
		spe.commit();

		Intent service = new Intent(this, MainService.class);
		if (isChecked) {
			// Start service
			if (!isMyServiceRunning()) {
				startService(service);
			}
		} else {
			// Stop service
			stopService(service);
		}

		// Update views
		checkBoxVolumeButtonHack.setEnabled(isChecked);
		checkBoxMusicHack.setEnabled(isChecked && checkBoxVolumeButtonHack.isChecked());
		checkBoxVoiceCallHack.setEnabled(isChecked && checkBoxVolumeButtonHack.isChecked());
		checkBoxRingHack.setEnabled(isChecked && checkBoxVolumeButtonHack.isChecked());
		checkBoxToggleSafety.setEnabled(isChecked);
	}

	// Checks whether or not main service is currently running
	private boolean isMyServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (MainService.class.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	// Toggles volume safety feature
	private void toggleSafety(boolean isChecked) {

		// Update views
		if (seekBarSafetyLevel.getProgress() > seekBarMaxLevel.getProgress()) {
			seekBarSafetyLevel.setProgress(seekBarMaxLevel.getProgress());
		} else if (seekBarSafetyLevel.getProgress() < seekBarMinLevel.getProgress()) {
			seekBarSafetyLevel.setProgress(seekBarMinLevel.getProgress());
		} else {
			textViewSafetyLevel.setText("Safety level: " + seekBarSafetyLevel.getProgress());
		}

		// Store preference
		spe.putBoolean(CHECKBOX_SAFETY_CHECKED, isChecked);
		spe.apply();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

		switch (buttonView.getId()) {
			case R.id.checkBox_toggleService:
				toggleService(isChecked);
				break;
			case R.id.checkBox_toggleSafetyLevel:
				toggleSafety(isChecked);
				break;
			case R.id.checkBox_VolumeButtonHack:
				toggleVolumeButtonHack(isChecked);
				break;
			case R.id.checkBox_musicHack:
				spe.putBoolean(CHECKBOX_MUSIC_HACK, isChecked);
				spe.commit();
				break;
			case R.id.checkBox_voiceCallHack:
				spe.putBoolean(CHECKBOX_VOICE_CALL_HACK, isChecked);
				spe.commit();
				break;
			case R.id.checkBox_ringHack:
				spe.putBoolean(CHECKBOX_RING_HACK, isChecked);
				spe.commit();
				break;
		}
	}

	// Toggles volume button hack
	private void toggleVolumeButtonHack(boolean isChecked) {
		// Store preference
		spe.putBoolean(CHECKBOX_VOLUME_BUTTON_HACK, isChecked);
		spe.apply();

		// Update views
		checkBoxMusicHack.setEnabled(isChecked);
		checkBoxVoiceCallHack.setEnabled(isChecked);
		checkBoxRingHack.setEnabled(isChecked);

		// Update service
		Intent service = new Intent(this, MainService.class);
		service.putExtra(MainService.HEADSET_STATUS, MainActivity.TOGGLE_VOLUME_HACK);
		startService(service);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
	                              boolean fromUser) {

		// Handles seekbars' progress changes
		switch (seekBar.getId()) {
			case R.id.seekBar_minLevel:
				// Prevent min level from being higher than max level
				if (progress > seekBarMaxLevel.getProgress()) {
					seekBar.setProgress(seekBarMaxLevel.getProgress());
				} else if (progress > seekBarSafetyLevel.getProgress() && checkBoxToggleSafety.isChecked()) {
					seekBar.setProgress(seekBarSafetyLevel.getProgress());
				} else {
					textViewMinLevel.setText("Min level: " + progress);
				}
				break;
			case R.id.seekBar_maxLevel:
				// Prevent max level from being lower than min level
				if (progress < seekBarMinLevel.getProgress()) {
					seekBar.setProgress(seekBarMinLevel.getProgress());
				} else if (progress < seekBarSafetyLevel.getProgress() && checkBoxToggleSafety.isChecked()) {
					seekBar.setProgress(seekBarSafetyLevel.getProgress());
				} else {
					textViewMaxLevel.setText("Max level: " + progress);
				}
				break;
			case R.id.seekBar_safetyLevel:
				// Prevent safety level from being lower than min level, and higher than max level
				if (progress > seekBarMaxLevel.getProgress()) {
					seekBar.setProgress(seekBarMaxLevel.getProgress());
				} else if (progress < seekBarMinLevel.getProgress()) {
					seekBar.setProgress(seekBarMinLevel.getProgress());
				} else {
					textViewSafetyLevel.setText("Safety level: " + progress);
				}
				break;
			case R.id.seekBar_VolumeButtonHack:
				textViewVolumeButtonHack.setText("Amp level jump: " + (progress + 1));
				break;
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		switch (seekBar.getId()) {
			// Update preferences when seekbars are no longer dragged
			case R.id.seekBar_minLevel:
				spe.putInt(SEEKBAR_MIN_LEVEL, seekBar.getProgress());
				spe.commit();
				mainFragment.rebuildSeekbar();
				break;
			case R.id.seekBar_maxLevel:
				spe.putInt(SEEKBAR_MAX_LEVEL, seekBar.getProgress());
				spe.commit();
				mainFragment.rebuildSeekbar();
				break;
			case R.id.seekBar_safetyLevel:
				spe.putInt(SEEKBAR_SAFETY_LEVEL, seekBar.getProgress());
				spe.commit();
				mainFragment.rebuildSeekbar();
				break;
			case R.id.seekBar_VolumeButtonHack:
				spe.putInt(SEEKBAR_VOLUME_BUTTON_HACK, seekBar.getProgress());
				spe.commit();
				// Inform service of volume hack change
				Intent service = new Intent(this, MainService.class);
				service.putExtra(MainService.HEADSET_STATUS, MainActivity.TOGGLE_VOLUME_HACK);
				startService(service);
				break;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mainFragment.rebuildSeekbar();
	}
}
