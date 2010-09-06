package com.joshsera;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Bitmap.Config;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPort;
import com.illposed.osc.OSCPortOut;
import com.joshsera.PadActivity.PanelConfig.PanelItem;

/**
 * 
 * @author jsera
 * 
 *         <pre>
 *         TODO:
 *         trackbutton + mouse click toggles the mouse button to enable click and drag
 *         add scroll wheel
 *         add port selection text box on front page
 *         add back button. Make it go back to the IP connect page
 * </pre>
 */

public class PadActivity extends Activity {
	//
	private static final int TAP_NONE = 0;
	private static final int TAP_FIRST = 1;
	private static final int TAP_SECOND = 2;
	private static final int TAP_DOUBLE = 3;
	private static final int TAP_DOUBLE_FINISH = 4;
	private static final int TAP_RIGHT = 5;
	private static final String TAG = "RemoteDroid";

	//
	private OSCPortOut sender;
	// thread and graphics stuff
	private Handler handler = new Handler();
	//
	private FrameLayout flLeftButton;
	private boolean leftToggle = false;
	private Runnable rLeftDown;
	private Runnable rLeftUp;
	//
	private FrameLayout flRightButton;
	private boolean rightToggle = false;
	private Runnable rRightDown;
	private Runnable rRightUp;
	//
	private FrameLayout flMidButton;
	private boolean softShown = false;
	private Runnable rMidDown;
	private Runnable rMidUp;
	//

	private EditText etAdvancedText;
	//
	private FrameLayout flAdvancedPanel;
	private int advancedPanelHeight = 72;
	private String advancedPanelConfig = "Play{icon:bitblt_halpha16('" +
			"0000000000FF00000000000000000000" +
			"0000000000FFFF000000000000000000" +
			"0000000000FFFFFF0000000000000000" +
			"0000000000FFFFFFFF00000000000000" +
			"0000000000FFFFFFFFFF000000000000" +
			"0000000000FFFFFFFFFFFF0000000000" +
			"0000000000FFFFFFFFFFFFFF00000000" +
			"0000000000FFFFFFFFFFFFFFFF000000" +
			"0000000000FFFFFFFFFFFFFF77000000" +
			"0000000000FFFFFFFFFFFF7733000000" +
			"0000000000FFFFFFFFFF773300000000" +
			"0000000000FFFFFFFF77330000000000" +
			"0000000000FFFFFF7733000000000000" +
			"0000000000FFFF773300000000000000" +
			"0000000000FF77330000000000000000" +
			"00000000007733000000000000000000');" +
			"command:key_code(162,162,162);" +
			"}\r\n" +
			"Next{icon:bitblt_halpha16('" +
			"0000BBFF0000FF000000000000000000" +
			"0000BBFF5500FFFF0000000000000000" +
			"0000BBFF5500FFFFFF00000000000000" +
			"0000BBFF5500FFFFFFFF000000000000" +
			"0000BBFF5500FFFFFFFFFF0000000000" +
			"0000BBFF5500FFFFFFFFFFFF00000000" +
			"0000BBFF5500FFFFFFFFFFFFFF000000" +
			"0000BBFF5500FFFFFFFFFFFFFFFF0000" +
			"0000BBFF5500FFFFFFFFFFFFFF770000" +
			"0000BBFF5500FFFFFFFFFFFF77330000" +
			"0000BBFF5500FFFFFFFFFF7733000000" +
			"0000BBFF5500FFFFFFFF773300000000" +
			"0000BBFF5500FFFFFF77330000000000" +
			"0000BBFF5500FFFF7733000000000000" +
			"0000BBFF5500FF773300000000000000" +
			"00000033550077330000000000000000');" +
			"command:key_code(162,162,162);" +
			"}" +
			"Ctrl{icon:bitblt_halpha16('" +
			"0000BBFF0000FF000000000000000000" +
			"0000BBFF5500FFFF0000000000000000" +
			"0000BBFF5500FFFFFF00000000000000" +
			"0000BBFF5500FFFFFFFF000000000000" +
			"0000BBFF5500FFFFFFFFFF0000000000" +
			"0000BBFF5500FFFFFFFFFFFF00000000" +
			"0000BBFF5500FFFFFFFFFFFFFF000000" +
			"0000BBFF5500FFFFFFFFFFFFFFFF0000" +
			"0000BBFF5500FFFFFFFFFFFFFF770000" +
			"0000BBFF5500FFFFFFFFFFFF77330000" +
			"0000BBFF5500FFFFFFFFFF7733000000" +
			"0000BBFF5500FFFFFFFF773300000000" +
			"0000BBFF5500FFFFFF77330000000000" +
			"0000BBFF5500FFFF7733000000000000" +
			"0000BBFF5500FF773300000000000000" +
			"00000033550077330000000000000000');" +
			"command:key_code(162,162,162);" +
			"}";
	
	public class PanelConfig
	{
		
		ArrayList<PanelItem> PanelItems = new ArrayList<PanelItem>();
		
		public PanelConfig(String config)
		{
			int level = 0;
			String current = "";
			for (int i = 0; i < config.length(); i++)
			{
				String chr = config.substring(i, i+1);

				current += chr;
				if (chr.equals("{"))
				{
					if (level == 0) current = current.trim();
					level++;
				}
				if (chr.equals("}"))
				{
					level--;

					if (level == 0)
					{
						PanelItems.add(new PanelItem(current));
						current = "";
					}
				}
			}
		}
	
		public class PanelItem
		{
			public String Name = "";
			Bitmap icon = null;
			PanelCommand command = null;
			public PanelItem(String config)
			{
				boolean isBefore = true;
				String current = "";
				String name = "";
				String value = "";
				for (int i = 0; i < config.length(); i++)
				{
					String chr = config.substring(i, i + 1);
					if (isBefore)
					{
						if (chr.equals("{"))
						{
							current = current.trim();
							Name = current;

							current = "";
							isBefore = false;
						}
						else
						{
							current += chr;
						}
					}
					else
					{
						if (chr.equals(":"))
						{
							name = current.trim().toLowerCase();
							current = "";
						}
						else if (chr.equals("}") || chr.equals(";"))
						{
							if (!name.equals(""))
							{
								value = current.trim();
								current = "";
								
								if(name.equals("icon"))
								{
									icon = parseBitmap(value);
								}
								if(name.equals("command"))
								{
									command = parseCommand(value);
								}
								if(name.equals("name"))
								{
									Name = parseString(value);
								}
								
								name = "";
							}
						}
						else
						{
							current += chr;
						}
					}
				}
			}
			
			public String parseString(String value)
			{
				return value.trim().replace("'", "");
			}

			public Bitmap parseBitmap(String value)
			{
				value = value.trim();
				
				if(value.startsWith("bitblt_halpha16("))
				{
					String bb = "";
					for (int i = 16; i < value.length(); i++)
					{
						String chr = value.substring(i, i + 1);
						if ("0123456789ABCDEFabcdef".contains(chr)) bb += chr;
					}
					
					if(bb.length()<16*16*2) return Bitmap.createBitmap(16, 16, Config.ARGB_8888);
					
					Bitmap bmp = Bitmap.createBitmap(16, 16, Config.ARGB_8888);
					int ix=0;
					for (int y = 0; y < 16; y++)
					{
						for (int x = 0; x < 16; x++)
						{
							String hex_pair = bb.substring(ix,ix+2); ix+=2;
							bmp.setPixel(x, y, Color.argb(Integer.valueOf(hex_pair, 16).intValue(), 0, 0, 0));
						}
					}
					return bmp;					
				}if(value.startsWith("bitblt_halpha32("))
				{
					String bb = "";
					for (int i = 16; i < value.length(); i++)
					{
						String chr = value.substring(i, i + 1);
						if ("0123456789ABCDEFabcdef".contains(chr)) bb += chr;
					}
					
					if(bb.length()<32*32*2) return Bitmap.createBitmap(16, 16, Config.ARGB_8888);
					Bitmap bmp = Bitmap.createBitmap(32, 32, Config.ARGB_8888);
					int ix=0;
					for (int y = 0; y < 32; y++)
					{
						for (int x = 0; x < 32; x++)
						{
							String hex_pair = bb.substring(ix,ix+2); ix+=2;
							bmp.setPixel(x, y, Color.argb(Integer.valueOf(hex_pair, 16).intValue(), 0, 0, 0));
						}
					}
					return bmp;					
				}
				if(value.startsWith("bitblt_halpha48("))
				{
					String bb = "";
					for (int i = 16; i < value.length(); i++)
					{
						String chr = value.substring(i, i + 1);
						if ("0123456789ABCDEFabcdef".contains(chr)) bb += chr;
					}
					
					if(bb.length()<48*48*2) return Bitmap.createBitmap(16, 16, Config.ARGB_8888);
					Bitmap bmp = Bitmap.createBitmap(48, 48, Config.ARGB_8888);
					int ix=0;
					for (int y = 0; y < 48; y++)
					{
						for (int x = 0; x < 48; x++)
						{
							String hex_pair = bb.substring(ix,ix+2); ix+=2;
							bmp.setPixel(x, y, Color.argb(Integer.valueOf(hex_pair, 16).intValue(), 0, 0, 0));
						}
					}
					return bmp;					
				}
				if(value.startsWith("bitblt_halpha64("))
				{
					String bb = "";
					for (int i = 16; i < value.length(); i++)
					{
						String chr = value.substring(i, i + 1);
						if ("0123456789ABCDEFabcdef".contains(chr)) bb += chr;
					}
					
					if(bb.length()<48*48*2) return Bitmap.createBitmap(16, 16, Config.ARGB_8888);
					Bitmap bmp = Bitmap.createBitmap(64, 64, Config.ARGB_8888);
					int ix=0;
					for (int y = 0; y < 64; y++)
					{
						for (int x = 0; x < 64; x++)
						{
							String hex_pair = bb.substring(ix,ix+2); ix+=2;
							bmp.setPixel(x, y, Color.argb(Integer.valueOf(hex_pair, 16).intValue(), 0, 0, 0));
						}
					}
					return bmp;					
				}
				return Bitmap.createBitmap(16, 16, Config.ARGB_8888);
			}

			public class PanelCommand
			{
				public String Windows = "";
				public String Linux = "";
				public String OSX = "";
			}
			public PanelCommand parseCommand(String value)
			{
				return null;
			}
		}
	}
	//
	private float xHistory;
	private float yHistory;
	//
	private int lastPointerCount = 0;
	// power lock
	private PowerManager.WakeLock lock;
	// sensors
	private SensorManager mSensorManager;
	private SensorEventListener mSensorListener;
	private Sensor mSensorAccelerometer;
	private Sensor mSensorMagnetic;
	// sensor tolerance
	private boolean useOrientation = false;
	//
	private Point3D accel;
	private boolean accelSet = false;
	private Point3D mag;
	private boolean magSet = false;
	//
	private CoordinateSpace lastSpace;
	private CoordinateSpace currSpace;
	// toggles
	private boolean toggleButton = false;
	// tap to click
	private long lastTap = 0;
	private int tapState = TAP_NONE;
	private Timer tapTimer;
	// multitouch scroll
	// private float scrollX = 0f;
	private float scrollY = 0f;

	/**
	 * Mouse sensitivity power
	 */
	private double mMouseSensitivityPower;

	private static final float sScrollStepMax = 6f;
	private static final float sScrollStepMin = 45f;
	private static final float sScrollMaxSettingsValue = 100f;

	private float mScrollStep;// = 12f;
	private static final float sTrackMultiplier = 6f;

	/**
	 * Cached multitouch information
	 */
	private boolean mIsMultitouchEnabled;

	public PadActivity() {
		super();
	}

	private void enableSensors() {
		if (mSensorManager == null) {
			mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
		}

		if (mSensorAccelerometer == null) {
			mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			Log.d(TAG, "Accelerometer Sensor: " + mSensorAccelerometer);
		}

		if (mSensorMagnetic == null) {
			mSensorMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
			Log.d(TAG, "Magnetic Sensor: " + mSensorMagnetic);
		}

		this.mSensorManager.registerListener(this.mSensorListener, mSensorAccelerometer,
				SensorManager.SENSOR_DELAY_GAME);
		this.mSensorManager.registerListener(this.mSensorListener, mSensorMagnetic,
				SensorManager.SENSOR_DELAY_GAME);
	}

	private void disableSensors() {
		if (mSensorManager != null) {
			this.mSensorManager.unregisterListener(this.mSensorListener);
			this.mSensorManager = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Settings.init(this.getApplicationContext());
		// Hide the title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		if (this.lock == null) {
			Context appContext = this.getApplicationContext();
			// get wake lock
			PowerManager manager = (PowerManager) appContext
					.getSystemService(Context.POWER_SERVICE);
			this.lock = manager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, this
					.getString(R.string.app_name));
			// prepare sensor Listener
			this.mSensorListener = new SensorEventListener() {
				// @Override
				public void onSensorChanged(SensorEvent event) {
					Sensor sensor = event.sensor;
					int type = sensor.getType();
					switch (type) {
					case Sensor.TYPE_ACCELEROMETER:
						onAccelerometer(event.values);
						break;
					case Sensor.TYPE_MAGNETIC_FIELD:
						onMagnetic(event.values);
						break;
					// case Sensor.TYPE_ORIENTATION:
					// break;
					}
				}

				// @Override
				public void onAccuracyChanged(Sensor sensor, int accuracy) {
					// no use for this
				}
			};

			if (useOrientation) {
				// enable Sensors
				enableSensors();
			}

			/**
			 * Caches information and forces WrappedMotionEvent class to load at
			 * Activity startup (avoid initial lag on touchpad).
			 */
			this.mIsMultitouchEnabled = WrappedMotionEvent.isMutitouchCapable();

			// Setup accelerations
			mMouseSensitivityPower = 1 + ((double) Settings.sensitivity) / 100d;
			mScrollStep = (sScrollStepMin - sScrollStepMax)
					* (sScrollMaxSettingsValue - Settings.scrollSensitivity) / sScrollMaxSettingsValue
					+ sScrollStepMax;

			Log.d(TAG, "mScrollStep=" + mScrollStep);
			Log.d(TAG, "Settings.sensitivity=" + Settings.scrollSensitivity);
			//
			this.accel = new Point3D();
			this.mag = new Point3D();
			this.lastSpace = new CoordinateSpace();
			this.currSpace = new CoordinateSpace();
			// UI runnables
			this.rLeftDown = new Runnable() {
				public void run() {
					drawButtonOn(flLeftButton);
				}
			};
			this.rLeftUp = new Runnable() {
				public void run() {
					drawButtonOff(flLeftButton);
				}
			};
			this.rRightDown = new Runnable() {
				public void run() {
					drawButtonOn(flRightButton);
				}
			};
			this.rRightUp = new Runnable() {
				public void run() {
					drawButtonOff(flRightButton);
				}
			};
			this.rMidDown = new Runnable() {
				public void run() {
					drawSoftOn();
				}
			};
			this.rMidUp = new Runnable() {
				public void run() {
					drawSoftOff();
				}
			};
			// window manager stuff
			this.getWindow().setFlags(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN,
					WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		}
		//
		try {
			//
			setContentView(R.layout.pad_layout);
			DisplayMetrics dm = new DisplayMetrics();
			this.getWindowManager().getDefaultDisplay().getMetrics(dm);
			//
			this.sender = new OSCPortOut(InetAddress.getByName(Settings.ip), OSCPort
					.defaultSCOSCPort());
			//
			this.initTouchpad();
			this.initLeftButton();
			this.initRightButton();
			this.initMidButton();
			this.initAdvancedPanel();
			this.initAdvancedText();
		} catch (Exception ex) {
			Log.d(TAG, ex.toString());
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		//
		menu.add(0, 1, 0, R.string.txt_advanced).setShortcut('0', 'c').setIcon(R.drawable.icon_advanced);
		menu.add(0, 2, 0, R.string.txt_keyboard).setShortcut('1', 'k').setIcon(R.drawable.icon_keyboard);
		menu.add(0, 3, 0, R.string.txt_help).setShortcut('2', 'h').setIcon(R.drawable.icon_help);
		//
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		//
		switch (item.getItemId()) {
		case 1:
			//
			onAdvancedToggle();
			break;
		case 2:
			//
			midButtonDown();
			break;
		case 3:
			//
			//onPrefs();
			break;
		}
		//
		return super.onOptionsItemSelected(item);
	}
	
	private void onAdvancedToggle() {
		
		if(flAdvancedPanel.getHeight()<10){
			android.view.ViewGroup.LayoutParams lp = flAdvancedPanel
					.getLayoutParams();
			lp.height = advancedPanelHeight;
			flAdvancedPanel.setLayoutParams(lp);

			
			
			LinearLayout ll = (LinearLayout) this.findViewById(R.id.llAdvancedGroup);

			ll.removeAllViewsInLayout();
			
			PanelConfig pc = new PanelConfig(advancedPanelConfig);
			for (PanelItem item : pc.PanelItems)
			{
				Bitmap bd = Bitmap.createScaledBitmap(item.icon, 48, 48, true);
				ImageButton ib = new ImageButton(getApplicationContext());
				ib.setImageBitmap(bd);
				ll.addView(ib,72,72);
			}

		}else{
			android.view.ViewGroup.LayoutParams lp = flAdvancedPanel.getLayoutParams();
			lp.height=0;
			flAdvancedPanel.setLayoutParams(lp);
		}
	}

	@SuppressWarnings("unused")
	private void onPrefs() {
		Intent i = new Intent(PadActivity.this, PrefsActivity.class);
		this.startActivity(i);
	}
	
	
	
	private void initAdvancedPanel() {
		FrameLayout fl = (FrameLayout) this.findViewById(R.id.flAdvancedPanel);
		//advancedPanelHeight = fl.getMeasuredHeight();
		//advancedPanelHeight = fl.getHeight();
		LayoutParams lp = fl.getLayoutParams();
		//advancedPanelHeight = lp.height*1;
		lp.height=0;
		fl.setLayoutParams(lp);
		// listener
		//fl.setOnTouchListener(new View.OnTouchListener() {
		//	public boolean onTouch(View v, MotionEvent ev) {
		//		return onAdvancedTouch(ev);
		//	}
		//});
		this.flAdvancedPanel = fl;
	}

	private void sendKey(int keycode)
	{
		
		Log.d("SEND_KEY", keycode + " '" + new Character(
				Character.toChars(Settings.charmap.get(keycode, 0))[0]).toString()+"'");
		try
		{
			{
				Object[] args = new Object[3];
				args[0] = 0; /* key down */
				args[1] = keycode;// (int)c;
				args[2] = new Character(
						Character.toChars(Settings.charmap.get(keycode, 0))[0]).toString();
				OSCMessage msg = new OSCMessage("/keyboard", args);

				this.sender.send(msg);

			}
			{
				Object[] args = new Object[3];
				args[0] = 1; /* key down */
				args[1] = keycode;// (int)c;
				args[2] = new Character(
						Character.toChars(Settings.charmap.get(keycode, 0))[0]).toString();
				OSCMessage msg = new OSCMessage("/keyboard", args);

				this.sender.send(msg);

			}
		}
		catch (Exception ex)
		{
			Log.d(TAG, ex.toString());
		}
	}
	
	
	
	int find = 0;
	
	private void sendKeys(String keys)
	{
		if(keys.equals("a  ")) return;
		/*
		 	shift,ctrl,alt
			
			
			"(",16,true,false,false
			")",7,true,false,false
			
			"{",71,true,false,false
		!!! "}",38,true,false,false
			
			"#",18
			"*",17
			"\n",66
			" ",62
			"+",81
			"-",69
			"&",14,true,false,false
			",",55
			";",74
			";",74,true,false,false
			"/",76
			"@",77
			"'",75
			"\"",75,true,false,false
			"!",8,true,false,false
			"?",72,false,false,false
			
			"~",126,true,false,false
			"_",95,true,false,false
			"^",94,true,false,false
			"%",12,true,false,false
			"=",70
			"$",11,true,false,false
		*/
		
		for (int i = 0; i < keys.length(); i++)
		{
			String c = keys.substring(i, i + 1);

			boolean isShift = false;
			boolean isCtrl = false;
			
			if(!c.toLowerCase().equals(c))
			{
				isShift = true;
				c = c.toLowerCase();
			}
			
			int key = 0;

			if(c.equals(" "))
				key = 62;
			if(c.equals("\n"))
				key = 66;
			if(c.equals("\t"))
			{
				key = 45;
				isCtrl = true;
			}
			

			if (c.equals("_"))
			{
				key = 95;
				isShift = true;
			}
			if (c.equals("\""))
			{
				key = 75;
				isShift = true;
			}
			if (c.equals("^"))
			{
				key = 94;
				isShift = true;
			}
			if (c.equals("~"))
			{
				key = 126;
				isShift = true;
			}
			if (c.equals("`"))
			{
				key = 68;
				isShift = true;
			}
			if (c.equals(":"))
			{
				key = 74;
				isShift = true;
			}
			if (c.equals("="))
				key = 70;
			if (c.equals("+"))
			{
				key = 70;
				isShift=true;
			}
			if (c.equals("%"))
			{
				key = 12;
				isShift=true;
			}
			if (c.equals("&"))
			{
				key = 14;
				isShift=true;
			}
			if (c.equals("^"))
			{
				key = 13;
				isShift=true;
			}
			if (c.equals("|"))
			{
				key = 73;
				isShift=true;
			}
			if (c.equals("_"))
			{
				key = 69;
				isShift=true;
			}
			if (c.equals("?"))
			{
				key = 76;
				isShift=true;
			}
			
			if (c.equals("!"))
			{
				key = 8;
				isShift=true;				
			}
			if (c.equals("$"))
			{
				key = 11;
				isShift=true;				
			}
			
			if (c.equals("~"))
			{
				key = 68;
				isShift=true;				
			}

			if (c.equals("<"))
			{
				key = 55;
				isShift=true;				
			}
			if (c.equals(">"))
			{
				key = 56;
				isShift=true;				
			}
			

			if (c.equals(""))
			{
				key = 56;
				isCtrl=true;				
			}
			
			/* for testing key codes ONLY
			if (c.equals("z"))
			{
				key = find++;
				isCtrl=true;
				isShift = true;
				Log.d("KEY_TEST", "'" + c + "' " + key + (isShift?" [59]":"") + (isCtrl?" [57]":""));
			}
			if (c.equals("x"))
			{
				key = find--;
				isCtrl=true;
				isShift = true;
				Log.d("KEY_TEST", "'" + c + "' " + key + (isShift?" [59]":"") + (isCtrl?" [57]":""));
			}
			*/
			
			
			if (c.equals("("))
			{
				key = 16;
				isShift = true;
			}
			if (c.equals(")"))
			{
				key = 7;
				isShift = true;
			}
			
			if (c.equals("{"))
			{
				key = 71;
				isShift = true;
			}
			if (c.equals("}"))
			{
				key = 72;
				isShift = true;
			}
			if (c.equals("["))
				key = 71;
			if (c.equals("]"))
				key = 72;

			if (key == 0)
				for (int z = 0; z < 1024; z++)
				{
					if (Settings.charmap.isPrintingKey(z))
					{
						if (new Character(Character.toChars(Settings.charmap
								.get(z, 0))[0]).toString().equals(c))
						{
							key = z;
							break;
						}
					}
				}

			try
			{	
				if(isCtrl){
					Object[] args = new Object[3];
					args[0] = 0; /* key down */
					args[1] = 57;// (int)c;
					args[2] = new Character((char)0).toString();
					OSCMessage msg = new OSCMessage("/keyboard", args);

					this.sender.send(msg);
				}
				
				if(isShift){
					Object[] args = new Object[3];
					args[0] = 0; /* key down */
					args[1] = 59;// (int)c;
					args[2] = new Character((char)0).toString();
					OSCMessage msg = new OSCMessage("/keyboard", args);

					this.sender.send(msg);
				}
				
				{
					Object[] args = new Object[3];
					args[0] = 0; /* key down */
					args[1] = key;// (int)c;
					args[2] = c;
					OSCMessage msg = new OSCMessage("/keyboard", args);

					this.sender.send(msg);

				}
				{
					Object[] args = new Object[3];
					args[0] = 1; /* key up */
					args[1] = key;// (int)c;
					args[2] = c;
					OSCMessage msg = new OSCMessage("/keyboard", args);

					this.sender.send(msg);

				}
				if(isShift){
					Object[] args = new Object[3];
					args[0] = 1; /* key up */
					args[1] = 59;// (int)c;
					args[2] = new Character((char)0).toString();
					OSCMessage msg = new OSCMessage("/keyboard", args);

					this.sender.send(msg);
				}
				
				if(isCtrl){
					Object[] args = new Object[3];
					args[0] = 1; /* key up */
					args[1] = 57;// (int)c;
					args[2] = new Character((char)0).toString();
					OSCMessage msg = new OSCMessage("/keyboard", args);

					this.sender.send(msg);
				}
			}
			catch (Exception ex)
			{
				Log.d(TAG, ex.toString());
			}
		}
		
	}
	
	String changed = "";
	private void initAdvancedText() {
		EditText et = (EditText) this.findViewById(R.id.etAdvancedText);
		this.etAdvancedText = et;
		
		et.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);//make sure keyboard doesnt go fullscreen in landscape mode

		changed = "a  ";
		etAdvancedText.setText(changed);
		
		// listener
		et.setOnKeyListener(new OnKeyListener(){

				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event)
				{
					Log.d("KEY_CHANGED", "'" + event.getCharacters() + "' "
							+ keyCode);
					changed = "a  ";
					etAdvancedText.setText(changed);
					return false;
				}

			});
		et.addTextChangedListener(new TextWatcher(){
			public void onTextChanged(CharSequence s, int start, int before,
					int count)
			{
					if (s.toString().equals(changed))
					{

						etAdvancedText.requestFocus();
						etAdvancedText.setSelection(2);
						return;
					}
					changed = null;

					// onAdvancedTextChanged(s, start, count);
					Log.d("TEXT_CHANGED",
							"'" + s.toString().substring(start, start + count)
									+ "' " + start + "|" + count);
					String change = s.toString().substring(start, start + count);
					
					if (count != 0)
					{
						if (change.equals(" "))
						{
							sendKey(62);
						}
						else
						{
							sendKeys(change);
						}
					}
					else
					{
						sendKey(67);
					}

					changed = "a  ";
					etAdvancedText.setText(changed);
			}
			
			public void afterTextChanged(Editable s)
			{
				
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
				
			}
		});
	}
	
	
	
	
	private void initTouchpad() {
		FrameLayout fl = (FrameLayout) this.findViewById(R.id.flTouchPad);

		// let's set up a touch listener
		fl.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent ev) {
				return onMouseMove(ev);
			}
		});
	}

	private void initLeftButton() {
		FrameLayout fl = (FrameLayout) this.findViewById(R.id.flLeftButton);
		android.view.ViewGroup.LayoutParams lp = fl.getLayoutParams();
		if(!Settings.hideMouseButtons) lp.height=0;
		fl.setLayoutParams(lp);
		// listener
		fl.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent ev) {
				return onLeftTouch(ev);
			}
		});
		this.flLeftButton = fl;
	}

	private void initRightButton() {
		FrameLayout iv = (FrameLayout) this.findViewById(R.id.flRightButton);
		android.view.ViewGroup.LayoutParams lp = iv.getLayoutParams();
		if(!Settings.hideMouseButtons) lp.height=0;
		iv.setLayoutParams(lp);
		// listener
		iv.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent ev) {
				return onRightTouch(ev);
			}
		});
		this.flRightButton = iv;
	}

	private void initMidButton() {
		FrameLayout fl = (FrameLayout) this.findViewById(R.id.flKeyboardButton);
		android.view.ViewGroup.LayoutParams lp = fl.getLayoutParams();
		if(!Settings.hideMouseButtons) lp.height=0;
		fl.setLayoutParams(lp);
		// listener
		fl.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent ev) {
				return onMidTouch(ev);
			}
		});
		this.flMidButton = fl;
	}

	public void onStart() {
		super.onStart();
	}

	public void onResume() {
		super.onResume();
		// acquire screen lock
		this.lock.acquire();
		// set sensor
		if (this.useOrientation) {
			enableSensors();
		}
	}

	public void onPause() {
		super.onPause();
		// this'd be a great time to disconnect from the server, and clean
		// up anything that needs to be cleaned up.
		// release screen lock
		this.lock.release();
		// release sensor
		disableSensors();
	}

	public void onStop() {
		super.onStop();
	}

	public void onDestroy() {
		super.onDestroy();
		this.sender.close();
	}

	// keyboard

	public boolean onKeyDown(int keycode, KeyEvent ev) {
		//allow menu to show
		//if (keycode == KeyEvent.KEYCODE_MENU)
		//	return super.onKeyDown(keycode, ev);
		if (keycode == 58) { // right alt
			this.toggleButton = true;
			return false;
		}
		//
		// Log.d(TAG, "keydown "+String.valueOf(keycode));
		Object[] args = new Object[3];
		args[0] = 0; /* key down */
		args[1] = keycode;
		args[2] = new Character(
				Character.toChars(Settings.charmap.get(keycode, ev.getMetaState()))[0]).toString();
		OSCMessage msg = new OSCMessage("/keyboard", args);
		try {
			this.sender.send(msg);
		} catch (Exception ex) {
			Log.d(TAG, ex.toString());
		}
		//
		return true;
	}

	public boolean onKeyUp(int keycode, KeyEvent ev) {
		//allow menu to show
		//if (keycode == KeyEvent.KEYCODE_MENU)
		//	return super.onKeyDown(keycode, ev);
		if (keycode == KeyEvent.KEYCODE_BACK) {
			if (!this.softShown) {
				Intent i = new Intent(this, RemoteDroid.class);
				this.startActivity(i);
				this.finish();
			} else {
				this.softShown = false;
			}
		} else if (keycode == 58) { // right alt
			this.toggleButton = false;
			return false;
		}
		//
		// Log.d(TAG, "keyup "+String.valueOf(keycode));
		Object[] args = new Object[3];
		args[0] = 1; /* key up */
		args[1] = keycode;
		args[2] = new Character(
				Character.toChars(Settings.charmap.get(keycode, ev.getMetaState()))[0]).toString();
		OSCMessage msg = new OSCMessage("/keyboard", args);
		try {
			this.sender.send(msg);
		} catch (Exception ex) {
			Log.d(TAG, ex.toString());
		}
		//
		return true;
	}

	// trackball

	public boolean onTrackballEvent(MotionEvent ev) {
		//
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			// send CTRL down
			Log.d(TAG, "track down");
			Object[] args = new Object[3];
			args[0] = 0; /* key down */
			args[1] = 60;
			args[2] = "CTRL";
			OSCMessage msg = new OSCMessage("/keyboard", args);
			try {
				this.sender.send(msg);
			} catch (Exception ex) {
				Log.d(TAG, ex.toString());
			}
		} else if (ev.getAction() == MotionEvent.ACTION_UP) {
			// send CTRL up
			Log.d(TAG, "track up");
			Object[] args = new Object[3];
			args[0] = 1; /* key up */
			args[1] = 60;
			args[2] = "CTRL";
			OSCMessage msg = new OSCMessage("/keyboard", args);
			try {
				this.sender.send(msg);
			} catch (Exception ex) {
				Log.d(TAG, ex.toString());
			}
		}
		// if (Settings.getTrackAsScroll() == false) {
		// use as mouse
		float dir = ev.getRawX();
		dir = dir == 0 ? 1 : dir / Math.abs(dir);
		float xDir = (float) Math.pow(sTrackMultiplier * ev.getRawX(), 3);
		//
		dir = ev.getRawY();
		dir = dir == 0 ? 1 : dir / Math.abs(dir);
		float yDir = (float) Math.pow(sTrackMultiplier * ev.getRawY(), 3);
		this.sendMouseEvent(2, xDir, yDir);
		//
		// } else {
		// // use as scroll
		// float dir = ev.getRawY();
		// if (dir != 0) {
		// dir = -(dir / Math.abs(dir));
		// this.sendScrollEvent((int) dir);
		// }
		// }
		//
		return true;
	}

	// mouse events
	boolean scrollTag = false;
	int scrollCount = 0;
	int rightClickAllowance = 1; //scroll iterations before skipping Right Click and doing scroll instead in two touch right click mode
	private boolean onMouseMove(MotionEvent ev) {
		int type = 0;
		float xMove = 0f;
		float yMove = 0f;

		int pointerCount = 1;
		if (mIsMultitouchEnabled) {
			pointerCount = WrappedMotionEvent.getPointerCount(ev);
		}

		// for (int i = 0; i < pointerCount; i++) {
		// int pointerId = ev.getPointerId(i);
		//
		// Log.v(TAG, "[Id=" + i + " - Index=" + i + "] X=" + ev.getX(pointerId)
		// + " Y="
		// + ev.getY(pointerId) + " Pressure=" + ev.getPressure(pointerId));
		// }

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			changed = "a  ";
			etAdvancedText.setText(changed);//reset text, so special keyboards wont try to insert a " " before next word (a bit of a hack, should be in a method, maybe cancelKeyboard()

			// scrollX = 0;
			scrollY = 0;
			//
			if (Settings.tapToClick && (pointerCount == 1)) {
				if (this.tapState == TAP_NONE) {
					// first tap
					this.lastTap = System.currentTimeMillis();
					//
				} else if (this.tapState == TAP_FIRST) {
					// second tap - check if we've fired the button up
					if (this.tapTimer != null) {
						// up has not been fired
						this.tapTimer.cancel();
						this.tapTimer = null;
						this.tapState = TAP_SECOND;
						this.lastTap = System.currentTimeMillis();
					}
				}
			}
			//
			type = 0;
			xMove = 0;
			yMove = 0;
			//
			this.xHistory = ev.getX();
			this.yHistory = ev.getY();
			//
			break;
		case MotionEvent.ACTION_UP:
			if (Settings.tapToClick && (pointerCount == 1)) {
				// it's a tap!
				long now = System.currentTimeMillis();
				long elapsed = now - this.lastTap;
				if (elapsed <= Settings.clickTime) {
					if (this.tapState == TAP_NONE) {
						// send the mouse down event
						if(scrollTag && Settings.twoTouchRightClick && scrollCount <= rightClickAllowance) //make sure scrolling has not happened
						{
							this.lastTap = now;
							//
							this.tapTimer = new Timer();
							this.tapTimer.scheduleAtFixedRate(new TimerTask() {
								public void run() {
									firstRightTapUp();
								}
							}, 0, Settings.clickTime);
						}
						else
						{
							this.lastTap = now;
							//
							this.tapTimer = new Timer();
							this.tapTimer.scheduleAtFixedRate(new TimerTask() {
								public void run() {
									firstTapUp();
								}
							}, 0, Settings.clickTime);
						}

					} else if (this.tapState == TAP_SECOND) {
						// double-click
						this.tapTimer = new Timer();
						this.tapTimer.scheduleAtFixedRate(new TimerTask() {
							public void run() {
								secondTapUp();
							}
						}, 0, 10);
					}

				} else {
					// too long
					this.lastTap = 0;
					if (this.tapState == TAP_SECOND) {
						// release the button
						this.tapState = TAP_NONE;
						this.lastTap = 0;
						this.leftButtonUp();
					}
				}
			}
			//
			type = 1;
			xMove = 0;
			yMove = 0;
			//scrollX= 0;
			scrollY = 0;
			scrollTag = false; //clear multi-touch event
			break;
		case MotionEvent.ACTION_MOVE:
			if (pointerCount == 1) {
				// move
				type = 2;
				if (lastPointerCount == 1) {
					xMove = ev.getX() - this.xHistory;
					yMove = ev.getY() - this.yHistory;
				}
				this.xHistory = ev.getX();
				this.yHistory = ev.getY();
			} else if (pointerCount == 2) {
				// multi-touch scroll
				type = -1;

				int pointer0 = WrappedMotionEvent.getPointerId(ev, 0);
				int pointer1 = WrappedMotionEvent.getPointerId(ev, 1);

				// float posX = WrappedMotionEvent.getX(ev, pointer0);
				float posY = WrappedMotionEvent.getY(ev, pointer0);

				// only consider the second pointer if I had a previous history
				if (lastPointerCount == 2) {
					// posX += WrappedMotionEvent.getX(ev, pointer1);
					// posX /= 2;
					posY += WrappedMotionEvent.getY(ev, pointer1);
					posY /= 2;

					// xMove = posX - this.xHistory;
					yMove = posY - this.yHistory;
				} else {
					// xMove = posX - this.xHistory;
					yMove = posY - this.yHistory;

					// posX += WrappedMotionEvent.getX(ev, pointer1);
					// posX /= 2;
					posY += WrappedMotionEvent.getY(ev, pointer1);
					posY /= 2;
				}

				// this.xHistory = posX;
				this.yHistory = posY;
			}
			break;
		}
		if (type == -1) {
			// scrollX += xMove;
			scrollY += yMove;
			int dir = 0;
			// if (Math.abs(scrollX) > SCROLL_STEP) {
			// // can't deal with X scrolling yet
			// scrollX = 0f;
			// }
			if (Math.abs(scrollY) > mScrollStep) {
				if (scrollY > 0f) {
					dir = 1;
				} else {
					dir = -1;
				}
				
				if (Settings.scrollInverted) {
					dir = -dir;
				}
				
				scrollY = 0f;
			}
			if(scrollTag==true) scrollCount++;
			else scrollCount = 0;
			scrollTag = true; //flag multi touch state for next up event
			if(Settings.twoTouchRightClick == true){ //if two finger right click is enabled we need to delay scrolling (1 iterations)
				if(dir!=0 && scrollCount > rightClickAllowance) { this.sendScrollEvent(dir); }//lets only send scroll events if there is distance to scroll
			} else {
				if(dir!=0) this.sendScrollEvent(dir); //lets only send scroll events if there is distance to scroll
			}
		} else if (type == 2) {
			// if type is 0 or 1, the server will not do anything with it, so we
			// only send type 2 events
			this.sendMouseEvent(type, xMove, yMove);
		}
		lastPointerCount = pointerCount;
		return true;
	}

	//
	
	private void firstRightTapUp() {
		this.leftToggle = false;
		if (this.tapState == TAP_NONE) {
			// single click
			// counts as a tap
			this.tapState = TAP_RIGHT;
			this.rightButtonDown();
		} else if (this.tapState == TAP_RIGHT) {
			this.rightButtonUp();
			this.tapState = TAP_NONE;
			this.lastTap = 0;
			this.tapTimer.cancel();
			this.tapTimer = null;
		}
	}
	
	private void firstTapUp() {
		this.leftToggle = false;
		if (this.tapState == TAP_NONE) {
			// single click
			// counts as a tap
			this.tapState = TAP_FIRST;
			this.leftButtonDown();
		} else if (this.tapState == TAP_FIRST) {
			this.leftButtonUp();
			this.tapState = TAP_NONE;
			this.lastTap = 0;
			this.tapTimer.cancel();
			this.tapTimer = null;
		}else if (this.tapState == TAP_RIGHT) {
			this.rightButtonUp();
			this.tapState = TAP_NONE;
			this.lastTap = 0;
			this.tapTimer.cancel();
			this.tapTimer = null;
		}
	}

	private void secondTapUp() {
		this.leftToggle = false;
		if (this.tapState == TAP_SECOND) {
			// mouse up
			this.leftButtonUp();
			this.lastTap = 0;
			this.tapState = TAP_DOUBLE;
		} else if (this.tapState == TAP_DOUBLE) {
			this.leftButtonDown();
			this.tapState = TAP_DOUBLE_FINISH;
		} else if (this.tapState == TAP_DOUBLE_FINISH) {
			this.leftButtonUp();
			this.tapState = TAP_NONE;
			this.tapTimer.cancel();
			this.tapTimer = null;
		}
	}

	// orientation event

	private void onAccelerometer(float[] values) {
		Point3D.copy(values, this.accel);
		this.accelSet = true;
		if (this.accelSet && this.magSet) {
			this.moveMouseFromSensors();
		}
	}

	private void onMagnetic(float[] values) {
		Point3D.copy(values, this.mag);
		this.magSet = true;
		if (this.accelSet && this.magSet) {
			this.moveMouseFromSensors();
		}
	}

	private void moveMouseFromSensors() {
		this.accelSet = false;
		this.magSet = false;
		//
		this.currSpace.setSpace(this.accel, this.mag);
		// get some dot products
		double dotX = Point3D.dot(this.currSpace.y, this.lastSpace.x);
		double dotY = Point3D.dot(this.currSpace.y, this.lastSpace.y);
		double angleX = Math.acos(dotX) / Math.PI - 0.5;
		double angleY = Math.acos(dotY) / Math.PI;
		Log.d(TAG, String.valueOf(angleX * 400) + ", " + String.valueOf(angleY * 400));
		//
		this.sendMouseEvent(2, (float) (angleX * 400), (float) (0 * 400));
		this.lastSpace.copy(this.currSpace);
	}

	// abstract mouse event

	private void sendMouseEvent(int type, float x, float y) {
		//
		float xDir = x == 0 ? 1 : x / Math.abs(x);
		float yDir = y == 0 ? 1 : y / Math.abs(y);
		//
		Object[] args = new Object[3];
		args[0] = type;
		args[1] = (float) (Math.pow(Math.abs(x), mMouseSensitivityPower)) * xDir;
		args[2] = (float) (Math.pow(Math.abs(y), mMouseSensitivityPower)) * yDir;
		// Log.d(TAG, String.valueOf(Settings.getSensitivity()));
		//
		OSCMessage msg = new OSCMessage("/mouse", args);
		try {
			this.sender.send(msg);
		} catch (Exception ex) {
			Log.d(TAG, ex.toString());
		}
	}

	private void sendScrollEvent(int dir) {
		Object[] args = new Object[1];
		args[0] = dir;
		//
		OSCMessage msg = new OSCMessage("/wheel", args);
		try {
			this.sender.send(msg);
		} catch (Exception ex) {
			Log.d(TAG, ex.toString());
		}
	}

	private boolean onLeftTouch(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			//
			if (this.toggleButton == false) {
				if (this.leftToggle) {
					this.leftButtonUp();
					this.leftToggle = false;
				}
				this.leftButtonDown();
			}
			break;
		case MotionEvent.ACTION_UP:
			//
			if (this.toggleButton == false) {
				this.leftButtonUp();
			} else {
				if (this.leftToggle) {
					this.leftButtonUp();
				} else {
					this.leftButtonDown();
				}
				this.leftToggle = !this.leftToggle;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			moveMouseWithSecondFinger(ev);
			break;
		}
		//
		return true;
	}

	/**
	 * Used to move the mouse with the second finger when one of the mouse
	 * buttons are pressed on the UI.
	 * 
	 * @param ev
	 */
	private void moveMouseWithSecondFinger(MotionEvent ev) {
		if (!mIsMultitouchEnabled) {
			return;
		}
		int pointerCount = WrappedMotionEvent.getPointerCount(ev);
		// if it is a multitouch move event
		if (pointerCount == 2) {
			// int pointer0 = ev.getPointerId(0);
			int pointer1 = WrappedMotionEvent.getPointerId(ev, 1);

			float x = WrappedMotionEvent.getX(ev, pointer1);
			float y = WrappedMotionEvent.getY(ev, pointer1);

			if (lastPointerCount == 2) {
				float xMove = x - this.xHistory;
				float yMove = y - this.yHistory;

				this.sendMouseEvent(2, xMove, yMove);
			}
			this.xHistory = x;
			this.yHistory = y;
		}
		lastPointerCount = pointerCount;
	}

	private synchronized void leftButtonDown() {
		Object[] args = new Object[1];
		args[0] = 0;
		OSCMessage msg = new OSCMessage("/leftbutton", args);
		try {
			this.sender.send(msg);
		} catch (Exception ex) {
			Log.d(TAG, ex.toString());
		}
		// graphical feedback
		this.handler.post(this.rLeftDown);
	}

	private synchronized void leftButtonUp() {
		Object[] args = new Object[1];
		args[0] = 1;
		OSCMessage msg = new OSCMessage("/leftbutton", args);
		try {
			this.sender.send(msg);
		} catch (Exception ex) {
			Log.d(TAG, ex.toString());
		}
		// graphical feedback
		this.handler.post(this.rLeftUp);
	}

	private boolean onRightTouch(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			//
			if (this.toggleButton == false) {
				if (this.rightToggle) {
					this.rightButtonUp();
					this.rightToggle = false;
				}
				this.rightToggle = false;
				this.rightButtonDown();
			}
			break;
		case MotionEvent.ACTION_UP:
			//
			if (this.toggleButton == false) {
				this.rightButtonUp();
			} else {
				// toggle magic!
				if (this.rightToggle) {
					this.rightButtonUp();
				} else {
					this.rightButtonDown();
				}
				this.rightToggle = !this.rightToggle;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			moveMouseWithSecondFinger(ev);
			break;
		}
		//
		return true;
	}

	private void rightButtonDown() {
		Object[] args = new Object[1];
		args[0] = 0;
		OSCMessage msg = new OSCMessage("/rightbutton", args);
		try {
			this.sender.send(msg);
		} catch (Exception ex) {
			Log.d(TAG, ex.toString());
		}
		// graphical feedback
		this.handler.post(this.rRightDown);
	}

	private void rightButtonUp() {
		Object[] args = new Object[1];
		args[0] = 1;
		OSCMessage msg = new OSCMessage("/rightbutton", args);
		try {
			this.sender.send(msg);
		} catch (Exception ex) {
			Log.d(TAG, ex.toString());
		}
		// graphical feedback
		this.handler.post(this.rRightUp);
	}

	private boolean onMidTouch(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			//
			this.handler.post(this.rMidDown);
			break;
		case MotionEvent.ACTION_UP:
			//
			this.midButtonDown();
			this.handler.post(this.rMidUp);
			break;
		}
		this.softShown = true;
		//
		return true;
	}

	private void midButtonDown() {
		InputMethodManager man = (InputMethodManager) this.getApplicationContext()
				.getSystemService(INPUT_METHOD_SERVICE);
		// boolean result = man.showSoftInput(this.findViewById(R.id.ivBtnSoft),
		// InputMethodManager.SHOW_IMPLICIT, new
		// SoftResultReceiver(this.handler));
		man.toggleSoftInputFromWindow(this.flMidButton.getWindowToken(),
				InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
		// Log.d(TAG, "show keyboard result: "+String.valueOf(result));
		//

	}

	// private void midButtonUp() {
	// InputMethodManager man = (InputMethodManager)
	// this.getApplicationContext()
	// .getSystemService(INPUT_METHOD_SERVICE);
	// //
	// man.toggleSoftInputFromWindow(this.ivMidButton.getWindowToken(),
	// InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
	//
	// }

	private void drawButtonOn(FrameLayout fl) {
		fl.setBackgroundResource(R.drawable.left_button_on);
	}

	private void drawButtonOff(FrameLayout fl) {
		fl.setBackgroundResource(R.drawable.left_button_off);
	}

	private void drawSoftOn() {
		this.flMidButton.setBackgroundResource(R.drawable.keyboard_on);
	}

	private void drawSoftOff() {
		this.flMidButton.setBackgroundResource(R.drawable.keyboard_off);
	}
}
