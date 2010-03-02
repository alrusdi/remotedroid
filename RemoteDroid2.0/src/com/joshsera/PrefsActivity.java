package com.joshsera;

import com.joshsera.R;
import com.joshsera.R.id;
import com.joshsera.R.layout;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;

public class PrefsActivity extends Activity {
	//
	private CheckBox cbTap;
	private SeekBar sbTap;
	private CheckBox cbWheel;
	private SeekBar sbSensitivity;
	
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
		this.cbWheel = (CheckBox)this.findViewById(R.id.cbTrackAsScroll);
		this.sbSensitivity = (SeekBar)this.findViewById(R.id.sbSensitivity);
		// set UI to Settings
		this.cbTap.setChecked(Settings.getTapToClick());
		this.sbTap.setProgress(Settings.getClickTime());
		this.cbWheel.setChecked(Settings.getTrackAsScroll());
		this.sbSensitivity.setProgress(Settings.getSensitivity());
		//
		Button but = (Button)this.findViewById(R.id.btnSavePrefs);
		but.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				savePrefs();
			}
		});
	}
	
	public void savePrefs() {
		// save stuff
		Settings.setTapToClick(this.cbTap.isChecked());
		Settings.setClickTime(this.sbTap.getProgress());
		Settings.setTrackAsScroll(this.cbWheel.isChecked());
		Settings.setSensitivity(this.sbSensitivity.getProgress());
		// go back to home screen
//		Intent i = new Intent(this, RemoteDroid.class);
//		this.startActivity(i);
//		this.finish();
	}
}
