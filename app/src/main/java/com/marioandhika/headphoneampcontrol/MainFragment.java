package com.marioandhika.headphoneampcontrol;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * Fragment that contains the main volume seekbars
 */
public class MainFragment extends Fragment implements OnSeekBarChangeListener, OnCheckedChangeListener {

	private HeadphoneAmpControl headphoneAmpControl = (HeadphoneAmpControl) getActivity().getApplication();

	// Views
	private TextView textViewLevel;
	private SeekBar seekBarMain;
	private SeekBar seekBarMainRight;
	private TextView textViewLevelRight;

	// User preferred minimum volume
	private int minLevel;

	// User preferred maximum volume
	private int maxLevel;

	// User preferred volume balance
	private boolean isBalanced;

	/**
	 * Minimum value for volume level set by the kernel
	 */
	public static final int MIN_LEVEL = 0;

	/**
	 * Maximum value for volume level set by the kernel
	 */
	public static final int MAX_LEVEL = 63;
	public static final int LEVEL_OFFSET = -57;

	// Handler and runnable to handle activity killing after certain time. Also see FINISH_DELAY
	private Handler idleKillTimer; // Handles idle kill timer
	private IdleTimer idleTimerRunnable; // Runnable to kill activity

	/**
	 * Longest user idle time before killing activity.
	 */
	public static final long FINISH_DELAY = 2000L;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {

		// Retrieve preferences
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
		isBalanced = sp.getBoolean(MainActivity.CHECKBOX_BALANCE, false);

		// Get and initialize views
		View v = inflater.inflate(R.layout.control, container, false);
		textViewLevel = (TextView) v.findViewById(R.id.textView_level);
		textViewLevelRight = (TextView) v.findViewById(R.id.textView_levelRight);
		seekBarMain = (SeekBar) v.findViewById(R.id.seekBar_main);
		seekBarMainRight = (SeekBar) v.findViewById(R.id.seekBar_mainRight);
		CheckBox checkBoxBalance = (CheckBox) v.findViewById(R.id.checkBox_Balance);
		checkBoxBalance.setChecked(isBalanced);

		// Set view listeners
		seekBarMain.setOnSeekBarChangeListener(this);
		seekBarMainRight.setOnSeekBarChangeListener(this);
		checkBoxBalance.setOnCheckedChangeListener(this);

		rebuildSeekbar();

		// Set up idle kill timer
		idleKillTimer = new Handler(); // Make your Main UIWorker Thread to execute this statement
		idleTimerRunnable = new IdleTimer();
		if (getActivity().getClass() == DialogActivity.class) {
			idleKillTimer.postDelayed(idleTimerRunnable, FINISH_DELAY);
		}

		return v;
	}

	/**
	 * Rebuild main seekbars with values from the system files and preferences
	 */
	public void rebuildSeekbar() {
		headphoneAmpControl.getLevelsFromFile();
		int currentLevelL = headphoneAmpControl.getCurrentLevelL();
		int currentLevelR = headphoneAmpControl.getCurrentLevelR();

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
		minLevel = sp.getInt(MainActivity.SEEKBAR_MIN_LEVEL, MIN_LEVEL);
		maxLevel = sp.getInt(MainActivity.SEEKBAR_MAX_LEVEL, MAX_LEVEL);

		seekBarMain.setMax(maxLevel - minLevel);
		seekBarMainRight.setMax(maxLevel - minLevel);

		// Enforce safety levels
		if (currentLevelL > maxLevel) {
			currentLevelL = maxLevel;
		} else if (currentLevelL < minLevel) {
			currentLevelL = minLevel;
		}
		if (currentLevelR > maxLevel) {
			currentLevelR = maxLevel;
		} else if (currentLevelR < minLevel) {
			currentLevelR = minLevel;
		}

		// Determine visibility of right level seekbar
		if (isBalanced) {
			seekBarMainRight.setVisibility(View.GONE);
			textViewLevelRight.setVisibility(View.GONE);

			currentLevelR = currentLevelL;
			headphoneAmpControl.setLevel(currentLevelL, currentLevelR);
			updateSeekbar(currentLevelL, currentLevelR);
			updateLabel(currentLevelL, currentLevelR);
		} else {
			seekBarMainRight.setVisibility(View.VISIBLE);
			textViewLevelRight.setVisibility(View.VISIBLE);

			headphoneAmpControl.setLevel(currentLevelL, currentLevelR);
			updateSeekbar(currentLevelL, currentLevelR);
			updateLabel(currentLevelL, currentLevelR);
		}

	}

	// Update seekbar progress
	private void updateSeekbar(int currentLevel2, int currentRightLevel2) {
		seekBarMain.setProgress(currentLevel2 - minLevel);
		seekBarMainRight.setProgress(currentRightLevel2 - minLevel);
	}

	// Update textviews
	private void updateLabel(int currentLevel2, int currentRightLevel2) {
		textViewLevel.setText(String.valueOf(currentLevel2));
		textViewLevelRight.setText(String.valueOf(currentRightLevel2));
	}

	// Update foreground service notification
	private void updateNotification() {
		Intent service = new Intent(getActivity(), MainService.class);
		getActivity().startService(service);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
	                              boolean fromUser) {

		// Manually update volume levels and textviews if the user manually moved the seekbar
		if (fromUser) {
			if (isBalanced) {
				updateLabel(progress + minLevel, progress + minLevel);
				headphoneAmpControl.setLevel(progress + minLevel, progress + minLevel); // Comment for non-immediate level change. See onStopTrackingTouch().
			} else {
				switch (seekBar.getId()) {
					case R.id.seekBar_main:
						updateLabel(progress + minLevel, seekBarMainRight.getProgress() + minLevel);
						headphoneAmpControl.setLevel(progress + minLevel, seekBarMainRight.getProgress() + minLevel); // Comment for non-immediate level change. See onStopTrackingTouch().
						break;
					case R.id.seekBar_mainRight:
						updateLabel(seekBarMain.getProgress() + minLevel, progress + minLevel);
						headphoneAmpControl.setLevel(seekBarMain.getProgress() + minLevel, progress + minLevel); // Comment for non-immediate level change. See onStopTrackingTouch().
						break;
				}
			}
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// Disable kill timer if the user is using the seekbar
		idleKillTimer.removeCallbacks(idleTimerRunnable);
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
		boolean isServiceChecked = sp.getBoolean(MainActivity.CHECKBOX_SERVICE_CHECKED, false);

		// Update persistent notification if foreground service is enabled
		if (isServiceChecked) {
			updateNotification();
		}
		//setLevel(progress+minLevel); // Uncomment for non-immediate level change. See onProgressChanged().
		if (getActivity().getClass() == DialogActivity.class) {
			idleKillTimer.postDelayed(idleTimerRunnable, FINISH_DELAY);
		}
	}

	@Override
	public void onPause() {
		idleKillTimer.removeCallbacks(idleTimerRunnable);

		super.onPause();
	}

	// Runnable to kill activity after a specified delay time. Also see FINISH_DELAY
	private class IdleTimer implements Runnable {
		@Override
		public void run() {
			if (getActivity() != null)
				getActivity().finish();
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

		if (isChecked) {
			sp.edit().putBoolean(MainActivity.CHECKBOX_BALANCE, true).commit();
			isBalanced = true;

		} else {
			sp.edit().putBoolean(MainActivity.CHECKBOX_BALANCE, false).commit();
			isBalanced = false;
		}
		rebuildSeekbar();

		if (getActivity().getClass() == DialogActivity.class) {
			resetTimer();
		}

	}

	// Reset idle kill timer
	public void resetTimer() {
		idleKillTimer.removeCallbacks(idleTimerRunnable);
		idleKillTimer.postDelayed(idleTimerRunnable, FINISH_DELAY);
	}
}
