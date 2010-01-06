package com.joshsera;

import android.content.*;
import android.view.*;

public class Settings {
	private static String ip;
	public static KeyCharacterMap charmap;
	// object so we can test vs. null in the getter.
	private static Boolean tapToClick;
	// cached value for performance
	private static boolean tapValue;
	// object so we can test vs. null in the getter.
	private static Integer clickTime;
	// cached value for performance
	private static int clickValue;
	// object so we can test vs. null in the getter.
	private static Boolean trackAsScroll;
	// cached value for performance
	private static boolean trackValue;
	// object so we can test vs. null in the getter.
	private static Integer sensitivity;
	// cached value for performance
	private static int sensitivityValue;
	//
	private static SharedPreferences prefs;
	//
	public static final String PREFS_IPKEY = "remoteip";
	public static final String PREFS_TRACKASSCROLL = "trackasscroll";
	public static final String PREFS_TAPTOCLICK = "tapclick";
	public static final String PREFS_TAPTIME = "taptime";
	public static final String PREFS_SENSITIVITY = "sensitivity";
	//
	private static final String PREFS_FILENAME = "RemoteDroid";
	
	public static void init(Context con) {
		if (prefs == null) {
			prefs = con.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE);
			charmap = KeyCharacterMap.load(KeyCharacterMap.BUILT_IN_KEYBOARD);
		}
	}

	public static void setIp(String ip) {
		SharedPreferences.Editor edit = prefs.edit();
		edit.putString(Settings.PREFS_IPKEY, ip);
		edit.commit();
		Settings.ip = ip;
	}

	public static String getIp() {
		if (ip == null) {
			ip = prefs.getString(PREFS_IPKEY, "127.0.0.1");
		}
		return ip;
	}

	public static void setTrackAsScroll(boolean trackAsScroll) {
		SharedPreferences.Editor edit = prefs.edit();
		edit.putBoolean(Settings.PREFS_TRACKASSCROLL, trackAsScroll);
		edit.commit();
		Settings.trackAsScroll = new Boolean(trackAsScroll);
		trackValue = Settings.trackAsScroll.booleanValue();
	}

	public static boolean getTrackAsScroll() {
		if (trackAsScroll == null) {
			trackAsScroll = new Boolean(prefs.getBoolean(PREFS_TRACKASSCROLL, false));
			trackValue = trackAsScroll.booleanValue();
		}
		return trackValue;
	}

	public static void setTapToClick(boolean tapToClick) {
		SharedPreferences.Editor edit = prefs.edit();
		edit.putBoolean(Settings.PREFS_TAPTOCLICK, tapToClick);
		edit.commit();
		Settings.tapToClick = new Boolean(tapToClick);
		Settings.tapValue = Settings.tapToClick.booleanValue();
	}

	public static boolean getTapToClick() {
		if (tapToClick == null) {
			tapToClick = new Boolean(prefs.getBoolean(PREFS_TAPTOCLICK, true));
			tapValue = tapToClick.booleanValue();
		}
		return tapValue;
	}

	public static void setClickTime(int clickTime) {
		SharedPreferences.Editor edit = prefs.edit();
		edit.putInt(Settings.PREFS_TAPTIME, clickTime);
		edit.commit();
		Settings.clickTime = clickTime;
		clickValue = Settings.clickTime.intValue();
	}

	public static int getClickTime() {
		if (clickTime == null) {
			clickTime = new Integer(prefs.getInt(Settings.PREFS_TAPTIME, 200));
			clickValue = clickTime.intValue();
		}
		return clickValue;
	}

	public static void setSensitivity(int sensitivity) {
		SharedPreferences.Editor edit = prefs.edit();
		edit.putInt(Settings.PREFS_SENSITIVITY, sensitivity);
		edit.commit();
		Settings.sensitivity = new Integer(sensitivity);
		sensitivityValue = Settings.sensitivity.intValue();
	}

	public static int getSensitivity() {
		if (sensitivity == null) {
			sensitivity = new Integer(prefs.getInt(Settings.PREFS_SENSITIVITY, 0));
			sensitivityValue = sensitivity.intValue();
		}
		return sensitivityValue;
	}
}
