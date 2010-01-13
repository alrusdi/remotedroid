package com.joshsera;

import java.util.LinkedList;
import android.content.*;
import android.util.Log;
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
	private static final String PREFS_RECENT_IP_PREFIX = "recenthost";
	//
	private static final String PREFS_FILENAME = "RemoteDroid";
	//number of hosts to save in the history
	public static final int MAX_SAVED_HOSTS = 5;

	/**
	 * this is the working data set of the saved hosts in the history
	 * this mirrors the data in the settings
	 */
	private static LinkedList<String> recentIPs;	

	public static void init(Context con) {
		if (prefs == null) {
			prefs = con.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE);
			charmap = KeyCharacterMap.load(KeyCharacterMap.BUILT_IN_KEYBOARD);
			// set up the stack for the saved hosts
			recentIPs = new LinkedList<String>();
			populateRecentIPs();

		}
	}

	public static void setIp(String ip) throws Exception{
		SharedPreferences.Editor edit = prefs.edit();
		testIPValid(ip);
		edit.putString(Settings.PREFS_IPKEY, ip);
		edit.commit();
		Settings.ip = ip;

		// save ip into a list of most used hosts here:
		// push IP onto recent stack
		if (!recentIPs.contains(ip)){
			if (recentIPs.size() < MAX_SAVED_HOSTS) {
				recentIPs.addFirst(ip);
			} else {
				recentIPs.removeLast();
				recentIPs.addFirst(ip);
			}
		} else {
			while (recentIPs.contains(ip)){
				recentIPs.remove(ip);
			}
			recentIPs.addFirst(ip);
		}
		// save recent ips to settings
		writeRecentIPsToSettings();


	}

	private static void testIPValid(String ip) throws Exception{
		try {		
			String[] octets = ip.split("\\.");
			for (String s:octets){
				int i = Integer.parseInt(s);
				if (i > 255 || i < 0){
					throw new NumberFormatException();
				}
			}
		} catch (NumberFormatException e) {
			throw new Exception("Illegal IP address!");
		}


	}

	public static LinkedList<String> getSavedHosts(){
		return recentIPs;
	}

	private static void writeRecentIPsToSettings(){
		SharedPreferences.Editor edit = prefs.edit();
		String s;
		for (int i=0; i<MAX_SAVED_HOSTS ; ++i){
			try {
				s = recentIPs.get(i);
			} catch (IndexOutOfBoundsException e) {
				s = null;
			}
			edit.putString(PREFS_RECENT_IP_PREFIX+((Integer)i).toString(), s);
		}
		edit.commit();
	}

	private static void populateRecentIPs(){
		recentIPs.clear();
		for (int i=0;i<MAX_SAVED_HOSTS; ++i){
			String host = prefs.getString(PREFS_RECENT_IP_PREFIX+((Integer)i).toString(), null);
			if (host != null) {
				recentIPs.add(host);
			}
		}
	}

	// deletes a saved host from the list of saved hosts, by string
	public static void removeSavedHost(CharSequence ip) throws Exception{

		// remove ip from list
		if (recentIPs.remove(ip.toString())){
			// rewrite settings
			writeRecentIPsToSettings();

		} else {
			throw new Exception("did not find "+ip.toString()+" in saved host list");
		}
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

