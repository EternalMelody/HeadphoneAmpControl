package com.marioandhika.headphoneampcontrol;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;

/**
 * Dialog activity that starts when the user changes volume, opens shortcut from notification, or launched it from shortcut launcher.
 * Contains a main fragment.
 */
public class DialogActivity extends Activity {

	/**
	 * Value to determine what volume change action launched the activity.
	 */
	public static final int INCREASE = 1;

	/**
	 * Value to determine what volume change action launched the activity.
	 */
	public static final int DECREASE = 2;

	/**
	 * Value to determine what volume change action launched the activity.
	 */
	public static final int NEITHER = 0;

	/**
	 * Intent key to determine what volume change action launched the activity.
	 */
	public static final String DECREASE_OR_INCREASE = "decrease_or_increase";

	/**
	 * Intent action to intercept refreshing of headphone level seekbar
	 */
	public static final String ACTION_REFRESH_HEADPHONE_LEVEL_SEEKBAR = "action_refresh_headphone_level_seekbar";

	private MainFragment mf;
	private VolumeChangeReceiver receiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Initialize window
		setContentView(R.layout.activity_dialog);
		ColorDrawable dialogColor = new ColorDrawable(Color.BLACK);
		dialogColor.setAlpha(192);
		getWindow().setBackgroundDrawable(dialogColor);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(getWindow().getAttributes());
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		lp.gravity = Gravity.BOTTOM;
		getWindow().setAttributes(lp);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

		// Get main fragment
		FragmentManager fm = getFragmentManager();
		mf = (MainFragment) fm.findFragmentById(R.id.main_fragment);

		// Check what action started this activity
		int decreaseOrIncrease = getIntent().getIntExtra(DECREASE_OR_INCREASE, NEITHER);

		// Register volume change receiver if started from volume button
		if (decreaseOrIncrease != NEITHER) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(ACTION_REFRESH_HEADPHONE_LEVEL_SEEKBAR);
			receiver = new VolumeChangeReceiver();
			registerReceiver(receiver, filter);
		}
	}
	
	public void onDestroy(){
		if (receiver != null){
			unregisterReceiver(receiver);
		}
		super.onDestroy();
	}

	/**
	 * Detects volume change, and informs main fragment about the change
	 */
	public class VolumeChangeReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			mf.rebuildSeekbar();
			mf.resetTimer();
		}
		
	}
}