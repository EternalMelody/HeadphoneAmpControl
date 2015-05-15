package com.marioandhika.headphoneampcontrol;

import android.app.Application;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

/**
 * Created by marioandhika on 5/15/15.
 * Handles application-wide actions
 */
public class HeadphoneAmpControl extends Application {

	/**
	 * Path to system file that contains left volume level
	 */
	public static final String FILE_AMP_LEVEL_LEFT = "/sys/class/misc/wolfson_control/headphone_left";//"/sys/class/misc/scoobydoo_sound/headphone_amplifier_level";

	/**
	 * Path to system file that contains right volume level
	 */
	public static final String FILE_AMP_LEVEL_RIGHT = "/sys/class/misc/wolfson_control/headphone_right";

	/**
	 * Error code to return when device is not rooted
	 */
	private static final int ERROR_CODE_NOT_ROOTED = 0;

	/**
	 * Reference to a volume system file that contains right volume level
	 */
	private File fileR;

	/**
	 * Reference to a volume system file that contains left volume level
	 */
	private File fileL;

	/**
	 * Current left level in memory
	 */
	private int currentLevelL;

	/**
	 * Current right level in memory
	 */
	private int currentLevelR;

	/**
	 * @return Gets current left level
	 */
	public int getCurrentLevelL() {
		return currentLevelL;
	}

	/**
	 * @return Gets current right level
	 */
	public int getCurrentLevelR() {
		return currentLevelR;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		fileL = new File(HeadphoneAmpControl.FILE_AMP_LEVEL_LEFT);
		fileR = new File(HeadphoneAmpControl.FILE_AMP_LEVEL_RIGHT);
	}

	/**
	 * Write new volume levels to the system files
	 *
	 * @param newLevelL New volume of left channel.
	 * @param newLevelR New volume of right channel.
	 */
	public void setLevel(int newLevelL, int newLevelR) {
		File mFile = new File(FILE_AMP_LEVEL_LEFT);
		FileOutputStream mFos;
		try {
			mFos = new FileOutputStream(mFile);

			byte[] bytesToWrite = String.valueOf(newLevelL).getBytes();
			mFos.write(bytesToWrite);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		File mFileR = new File(FILE_AMP_LEVEL_RIGHT);
		FileOutputStream mFosR;
		try {
			mFosR = new FileOutputStream(mFileR);

			byte[] bytesToWrite = String.valueOf(newLevelR).getBytes();
			mFosR.write(bytesToWrite);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Fix file permissions for the volume level files
	 */
	private void fixPermissions() {
		// Define terminal commands
		String[] hin1 = {"su", "-c",
				"chmod o+w " + FILE_AMP_LEVEL_LEFT,
				"chmod o+w " + FILE_AMP_LEVEL_RIGHT};

		// Execute terminal commands
		try {
			Runtime.getRuntime().exec(hin1);
		} catch (IOException e) {
			// Quit application if device is not rooted
			Toast.makeText(this, "Root access required for HeadphoneAmpControl to work", Toast.LENGTH_LONG).show();
			System.exit(ERROR_CODE_NOT_ROOTED);
			e.printStackTrace();
		}
	}

	/**
	 * Get levels from the system files
	 */
	public void getLevelsFromFile() {

		// Check permissions
		if (!fileL.canWrite() || !fileR.canWrite()) {
			fixPermissions();
		}

		// Read files
		try {
			Scanner scannerL = new Scanner(fileL);
			Scanner scannerR = new Scanner(fileR);
			currentLevelL = scannerL.nextInt();
			currentLevelR = scannerR.nextInt();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
