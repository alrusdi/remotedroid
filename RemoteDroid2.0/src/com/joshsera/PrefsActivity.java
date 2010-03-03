package com.joshsera;

import android.app.Activity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.SeekBar;

public class PrefsActivity extends Activity {
	//
	private CheckBox cbTap;
	private SeekBar sbTap;
	private SeekBar sbSensitivity;
	private SeekBar sbScrollSensitivity;
	private CheckBox cbScrollInverted;
	
	public PrefsActivity() {
		super();
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//
		Settings.init(this.getApplicationContext());
		//
		this.setContentView(R.layout.prefs);
		this.setTitle("Preferences");
		// set refs to important UI things.
		this.cbTap = (CheckBox)this.findViewById(R.id.cbTap);
		this.sbTap = (SeekBar)this.findViewById(R.id.sbClick);
		this.sbSensitivity = (SeekBar)this.findViewById(R.id.sbSensitivity);
		this.sbScrollSensitivity = (SeekBar)this.findViewById(R.id.sbScrollSensitivity);
		this.cbScrollInverted = (CheckBox)this.findViewById(R.id.cbScrollInverted);
		// set UI to Settings
		this.cbTap.setChecked(Settings.tapToClick);
		this.sbTap.setProgress(Settings.clickTime);
		this.sbSensitivity.setProgress(Settings.sensitivity);
		this.sbScrollSensitivity.setProgress(Settings.scrollSensitivity);
		this.cbScrollInverted.setChecked(Settings.scrollInverted);
//		//
//		Button but = (Button)this.findViewById(R.id.btnSavePrefs);
//		but.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				savePrefs();
//			}
//		});
	}
	
	public void savePrefs() {
		// save stuff
		Settings.setTapToClick(this.cbTap.isChecked());
		Settings.setClickTime(this.sbTap.getProgress());
//		Settings.setTrackAsScroll(this.cbWheel.isChecked());
		Settings.setSensitivity(this.sbSensitivity.getProgress());
		Settings.setScrollSensitivity(this.sbScrollSensitivity.getProgress());
		Settings.setScrollInverted(this.cbScrollInverted.isChecked());
		// go back to home screen
//		Intent i = new Intent(this, RemoteDroid.class);
//		this.startActivity(i);
//		this.finish();
	}
	
	@Override
	public void onBackPressed() {
		savePrefs();
		super.onBackPressed();
	}
}
