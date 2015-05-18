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
	private Integer currentLevelL;

	/**
	 * Current right level in memory
	 */
	private Integer currentLevelR;

	/**
	 * @return Gets current left level
	 */
	public int getCurrentLevelL() {
		if (currentLevelL == null) {
			getLevelsFromFile();
		}

		return currentLevelL;
	}

	/**
	 * @return Gets current right level
	 */
	public int getCurrentLevelR() {
		if (currentLevelR == null) {
			getLevelsFromFile();
		}

		return currentLevelR;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		initFilesIfNecessary();
	}

	private void initFilesIfNecessary(){
		if (fileL == null || fileR == null) {
			fileL = new File(HeadphoneAmpControl.FILE_AMP_LEVEL_LEFT);
			fileR = new File(HeadphoneAmpControl.FILE_AMP_LEVEL_RIGHT);
		}
	}

	/**
	 * Write new volume levels to the system files
	 *
	 * @param newLevelL New volume of left channel.
	 * @param newLevelR New volume of right channel.
	 */
	public void setLevel(int newLevelL, int newLevelR) {
		// Check permissions
		if (!fileL.canWrite() || !fileR.canWrite()) {
			fixPermissions();
		}

		FileOutputStream mFos;
		try {
			mFos = new FileOutputStream(fileL);

			byte[] bytesToWrite = String.valueOf(newLevelL).getBytes();
			mFos.write(bytesToWrite);
			mFos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		FileOutputStream mFosR;
		try {
			mFosR = new FileOutputStream(fileR);

			byte[] bytesToWrite = String.valueOf(newLevelR).getBytes();
			mFosR.write(bytesToWrite);
			mFosR.close();
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
		initFilesIfNecessary();
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
