package com.marioandhika.headphoneampcontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Detects whether or not headset is plugged in
 */
public class MainReceiver extends BroadcastReceiver {

	// Headset status constants
	public static final int UNPLUGGED = 0;
	public static final int PLUGGED = 1;
	public static final int UNALTERED = 2;

	@Override
	public void onReceive(Context context, Intent intent) {

		boolean isPlugged = intent.getIntExtra("state", UNPLUGGED) != 0;

		// Update service based on headset status
		Intent service = new Intent(context, MainService.class);
		if (isPlugged) {
			service.putExtra(MainService.HEADSET_STATUS, PLUGGED);
			context.startService(service);
		} else {
			service.putExtra(MainService.HEADSET_STATUS, UNPLUGGED);
			context.startService(service);
		}
	}

}
