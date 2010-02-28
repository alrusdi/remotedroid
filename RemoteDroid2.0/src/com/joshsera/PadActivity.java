package com.joshsera;

import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPort;
import com.illposed.osc.OSCPortOut;

/**
 * 
 * @author jsera
 *
 * to-do: trackbutton + mouse click toggles the mouse button to enable click and drag
 * add scroll wheel
 * add port selection text box on front page
 * add back button. Make it go back to the IP connect page
 */

public class PadActivity extends Activity {
	//
	private static final int TAP_NONE = 0;
	private static final int TAP_FIRST = 1;
	private static final int TAP_SECOND = 2;
	private static final int TAP_DOUBLE = 3;
	private static final int TAP_DOUBLE_FINISH = 4;
	private static final String TAG = "RemoteDroid";
	
	//
	private OSCPortOut sender;
	//
	private Paint black;
	// thread and graphics stuff
	private Handler handler = new Handler();
	//
	private ImageView ivLeftButton;
	private Bitmap bmLeftButton;
	private Canvas caLeftButton;
	private boolean leftToggle = false;
	private Runnable rLeftDown;
	private Runnable rLeftUp;
	//
	private ImageView ivRightButton;
	private Bitmap bmRightButton;
	private Canvas caRightButton;
	private boolean rightToggle = false;
	private Runnable rRightDown;
	private Runnable rRightUp;
	//
	private ImageView ivMidButton;
	private Bitmap bmMidButton;
	private Bitmap bmSoftOff;
	private Bitmap bmSoftOn;
	private Canvas caMidButton;
	private boolean softShown = false;
	private Runnable rMidDown;
	private Runnable rMidUp;
	//
	private Paint greenStroke;
	private Paint greenFill;
	//
	private int buttonWidth;
	private int buttonHeight;
	//
	int portraitHeight;
	int portraitWidth;
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
	private float scrollX = 0f;
	private float scrollY = 0f;
	
	static final float SCROLL_STEP = 10f;
	
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
		
		this.mSensorManager.registerListener(this.mSensorListener, mSensorAccelerometer, SensorManager.SENSOR_DELAY_GAME);
		this.mSensorManager.registerListener(this.mSensorListener, mSensorMagnetic, SensorManager.SENSOR_DELAY_GAME);
	}
	
	private void disableSensors() {
		if (mSensorManager != null) {
			this.mSensorManager.unregisterListener(this.mSensorListener);
			this.mSensorManager = null;
		}
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Settings.init(this.getApplicationContext());
		//Hide the title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); 
		if (this.lock == null) {
			Context appContext = this.getApplicationContext();
			// get wake lock
			PowerManager manager = (PowerManager)appContext.getSystemService(Context.POWER_SERVICE);
			this.lock = manager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, this.getString(R.string.app_name));
			// prepare sensor Listener
			this.mSensorListener = new SensorEventListener() {
				@Override
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
//					case Sensor.TYPE_ORIENTATION:
//						break;
					}
				}
				
				@Override
				public void onAccuracyChanged(Sensor sensor, int accuracy) {
					// no use for this
				}
			};
			
			if (useOrientation) {
				// enable Sensors
				enableSensors();
			}

			//
			this.accel = new Point3D();
			this.mag = new Point3D();
			this.lastSpace = new CoordinateSpace();
			this.currSpace = new CoordinateSpace();
			// UI runnables
			this.rLeftDown = new Runnable() {
				public void run() {
					drawButtonOn(caLeftButton, bmLeftButton, ivLeftButton);
				}
			};
			this.rLeftUp = new Runnable() {
				public void run() {
					drawButtonOff(caLeftButton, bmLeftButton, ivLeftButton);
				}
			};
			this.rRightDown = new Runnable() {
				public void run() {
					drawButtonOn(caRightButton, bmRightButton, ivRightButton);
				}
			};
			this.rRightUp = new Runnable() {
				public void run() {
					drawButtonOff(caRightButton, bmRightButton, ivRightButton);
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
			this.getWindow().setFlags(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN, WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		}
		//
		try {
			//
			setContentView(R.layout.pad_layout);
			DisplayMetrics dm = new DisplayMetrics();
			this.getWindowManager().getDefaultDisplay().getMetrics(dm);
			int width = dm.widthPixels;
			int height = dm.heightPixels;
			//
			this.sender = new OSCPortOut(InetAddress.getByName(Settings.getIp()), OSCPort.defaultSCOSCPort());
			//
			this.black = new Paint();
			this.black.setARGB(255, 0, 0, 0);
			this.greenFill = new Paint();
			this.greenFill.setARGB(255, 0, 255, 0);
			this.greenStroke = new Paint();
			this.greenStroke.setARGB(255, 0, 255, 0);
			this.greenStroke.setStyle(Paint.Style.STROKE);
			this.buttonWidth = (int)(((double)width) * 0.5d) - 30;
			this.buttonHeight = height/3;
			initTouchpad(width, height);
			this.initLeftButton(width, height);
			this.initRightButton(width, height);
			this.initMidButton(width, height);
		} catch (Exception ex) {
			Log.d(TAG, ex.toString());
		}
	}

	private void initTouchpad(int width, int height) {
		int squWidth = width-2;
		int squHeight = height/3 * 2;
		ImageView iv = (ImageView)this.findViewById(R.id.ivRect);
		Canvas ca = new Canvas();
		Paint red = new Paint();
		red.setStyle(Paint.Style.STROKE);
		Bitmap bm = Bitmap.createBitmap(width, squHeight, Bitmap.Config.RGB_565);
		red.setARGB(255, 255, 0, 0);
		squHeight -= 2;
		ca.setBitmap(bm);
		this.drawSquare(ca, squWidth, squHeight, red);
		iv.setImageBitmap(bm);
		// let's set up a touch thinger
		iv.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent ev) {
				return onMouseMove(ev);
			}
		});
	}
	
	private void initLeftButton(int width, int height) {
		ImageView iv = (ImageView)this.findViewById(R.id.ivBtnLeft);
		Canvas ca = new Canvas();
		Bitmap bm = Bitmap.createBitmap(this.buttonWidth, this.buttonHeight, Bitmap.Config.RGB_565);
		ca.setBitmap(bm);
		this.drawSquare(ca, this.buttonWidth-2, this.buttonHeight-2, this.greenStroke);
		iv.setImageBitmap(bm);
		// listener
		iv.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent ev) {
				return onLeftTouch(ev);
			}
		});
		// position the button
		AbsoluteLayout.LayoutParams params = (AbsoluteLayout.LayoutParams)iv.getLayoutParams();
		params.y = this.buttonHeight * 2;
		// reference some stuff
		this.ivLeftButton = iv;
		this.caLeftButton = ca;
		this.bmLeftButton = bm;
	}
	
	private void initRightButton(int width, int height) {
		ImageView iv = (ImageView)this.findViewById(R.id.ivBtnRight);
		Canvas ca = new Canvas();
		Bitmap bm = Bitmap.createBitmap(this.buttonWidth, this.buttonHeight, Bitmap.Config.RGB_565);
		ca.setBitmap(bm);
		this.drawSquare(ca, this.buttonWidth-2, this.buttonHeight-2, this.greenStroke);
		iv.setImageBitmap(bm);
		// listener
		iv.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent ev) {
				return onRightTouch(ev);
			}
		});
		// position the button
		AbsoluteLayout.LayoutParams params = (AbsoluteLayout.LayoutParams)iv.getLayoutParams();
		params.y = this.buttonHeight * 2;
		params.x = this.buttonWidth + 60;
		//
		this.ivRightButton = iv;
		this.caRightButton = ca;
		this.bmRightButton = bm;
	}
	
	private void initMidButton(int width, int height) {
		ImageView iv = (ImageView)this.findViewById(R.id.ivBtnSoft);
		Canvas ca = new Canvas();
		Bitmap bm = Bitmap.createBitmap(60, this.buttonHeight, Bitmap.Config.RGB_565);
		ca.setBitmap(bm);
		Bitmap bg = BitmapFactory.decodeResource(this.getResources(), R.drawable.softkeyoff);
		ca.drawBitmap(bg, 10, ((float)this.buttonHeight)/2 - 20, null);
		iv.setImageBitmap(bm);
		// listener
		iv.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent ev) {
				return onMidTouch(ev);
			}
		});
		// position
		AbsoluteLayout.LayoutParams params = (AbsoluteLayout.LayoutParams)iv.getLayoutParams();
		params.y = this.buttonHeight * 2;
		params.x = this.buttonWidth;
		//
		this.ivMidButton = iv;
		this.caMidButton = ca;
		this.bmMidButton = bm;
		this.bmSoftOff = bg;
		this.bmSoftOn = BitmapFactory.decodeResource(this.getResources(), R.drawable.softkeyon);
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
		if (keycode == KeyEvent.KEYCODE_MENU) {
			return false;
		} else if (keycode == 58) { // right alt
			this.toggleButton = true;
			return false;
		}
		//
//		Log.d(TAG, "keydown "+String.valueOf(keycode));
		Object[] args = new Object[3];
		args[0] = 0; /* key down */
		args[1] = keycode;
		args[2] = new Character(Character.toChars(Settings.charmap.get(keycode, ev.getMetaState()))[0]).toString();
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
		if (keycode == KeyEvent.KEYCODE_MENU) {
			// menu key
			return false;
		} else if (keycode == KeyEvent.KEYCODE_BACK) {
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
//		Log.d(TAG, "keyup "+String.valueOf(keycode));
		Object[] args = new Object[3];
		args[0] = 1; /* key up */
		args[1] = keycode;
		args[2] = new Character(Character.toChars(Settings.charmap.get(keycode, ev.getMetaState()))[0]).toString();
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
		if (ev.getAction() == MotionEvent.ACTION_UP) {
			// toggle trackball function
			Settings.setTrackAsScroll(!Settings.getTrackAsScroll());
		}
		if (Settings.getTrackAsScroll() == false) {
			// use as mouse
			float dir = ev.getRawX();
			dir = dir == 0 ? 1 : dir/Math.abs(dir);
			float xDir = (float)Math.pow(ev.getRawX()/0.1666667, 3);
			//
			dir = ev.getRawY();
			dir = dir == 0 ? 1 : dir/Math.abs(dir);
			float yDir = (float)Math.pow(ev.getRawY()/0.1666667, 3);
			this.sendMouseEvent(2, xDir, yDir);
			//
		} else {
			// use as scroll
			float dir = ev.getRawY();
			if (dir != 0) {
				dir = -(dir/Math.abs(dir));
				this.sendScrollEvent((int)dir);
			}
		}
		//
		return true;
	}
	
	// mouse events
	
	private boolean onMouseMove(MotionEvent ev) {
		int type = 0;
		float xMove = 0f;
		float yMove = 0f;
		
		int pointerCount = ev.getPointerCount();
		
//		for (int i = 0; i < pointerCount; i++) {
//			int pointerId = ev.getPointerId(i);
//			
//			Log.v("Nicolas", "[Id=" + i + " - Index=" + i + "] X=" + ev.getX(pointerId) + " Y="
//					+ ev.getY(pointerId) + " Pressure="
//					+ ev.getPressure(pointerId));
//		}
		
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				//
				if (Settings.getTapToClick() && (pointerCount==1)) {
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
				if (Settings.getTapToClick() && (pointerCount==1)) {
					// it's a tap!
					long now = System.currentTimeMillis();
					long elapsed = now - this.lastTap;
					if (elapsed <= Settings.getClickTime()) {
						if (this.tapState == TAP_NONE) {
							// send the mouse down event
							this.lastTap = now;
							//
							this.tapTimer = new Timer();
							this.tapTimer.scheduleAtFixedRate(new TimerTask() {
								public void run() {
									firstTapUp();
								}
							}, 0, Settings.getClickTime());
							
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
					break;
				} else if (pointerCount == 2){
					// multitouch scroll
					// TODO this is ugly, I should improve this (Nicolas)
					type = -1;
					int pointer0 = ev.getPointerId(0);
					int pointer1 = ev.getPointerId(1);
					float posX = ev.getX(pointer0);
					float posY = ev.getY(pointer0);

					if (lastPointerCount == 2) {
						posX += ev.getX(pointer1);
						posX /= 2;
						posY += ev.getY(pointer1);
						posY /= 2;
					}
					
					xMove = posX - this.xHistory;
					yMove = posY - this.yHistory;
					
					this.xHistory = posX;
					this.yHistory = posY;
					break;
				}
		}
		if (type == -1) {
			scrollX += xMove;
			scrollY += yMove;
			int dir = 0;
			if (Math.abs(scrollX) > SCROLL_STEP) {
				// can't deal with X scrolling yet
				scrollX = 0f;
			}
			if (Math.abs(scrollY) > SCROLL_STEP) {
				if (scrollY > 0f) {
					dir = -1;
				} else {
					dir = 1;
				}
				scrollY = 0f;
			}
			this.sendScrollEvent(dir);
		} else {
			this.sendMouseEvent(type, xMove, yMove);
		}
		lastPointerCount = pointerCount;
		return true;
	}
	
	//
	
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
		double angleX = Math.acos(dotX)/Math.PI - 0.5;
		double angleY = Math.acos(dotY)/Math.PI;
		Log.d(TAG, String.valueOf(angleX * 400)+", "+String.valueOf(angleY * 400));
		//
		this.sendMouseEvent(2, (float)(angleX * 400), (float)(0 * 400));
		this.lastSpace.copy(this.currSpace);
	}
	
	// abstract mouse event
	
	private void sendMouseEvent(int type, float x, float y) {
		//
		float xDir =  x == 0 ? 1 : x/Math.abs(x);
		float yDir =  y == 0 ? 1 : y/Math.abs(y);
		//
		Object[] args = new Object[3];
		args[0] = type;
		args[1] = (float)(Math.pow(Math.abs(x), 1+((double)Settings.getSensitivity())/100d)) * xDir;
		args[2] = (float)(Math.pow(Math.abs(y), 1+((double)Settings.getSensitivity())/100d)) * yDir;
//		Log.d(TAG, String.valueOf(Settings.getSensitivity()));
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
	
	private void moveMouseWithSecondFinger(MotionEvent ev) {
		int pointerCount = ev.getPointerCount();
		// if it is a multitouch move event
		if (pointerCount == 2) {
//			int pointer0 = ev.getPointerId(0);
			int pointer1 = ev.getPointerId(1);
			
			float x = ev.getX(pointer1);
			float y = ev.getY(pointer1);
			
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
		InputMethodManager man = (InputMethodManager)this.getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
		//boolean result = man.showSoftInput(this.findViewById(R.id.ivBtnSoft), InputMethodManager.SHOW_IMPLICIT, new SoftResultReceiver(this.handler));
		man.toggleSoftInputFromWindow ( this.ivMidButton.getWindowToken (),
				InputMethodManager.SHOW_FORCED,
				InputMethodManager.HIDE_IMPLICIT_ONLY ); 
		//Log.d(TAG, "show keyboard result: "+String.valueOf(result));
		//
		
	}
	
	private void midButtonUp() {
		InputMethodManager man = (InputMethodManager)this.getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
		//
		man.toggleSoftInputFromWindow ( this.ivMidButton.getWindowToken (),
				InputMethodManager.SHOW_FORCED,
				InputMethodManager.HIDE_IMPLICIT_ONLY ); 
		
	}
	
	// drawing
	
	private void drawSquare(Canvas ca, int width, int height, Paint color) {
		//
		Rect r = new Rect();
		r.set(0, 0, width, height);
		ca.drawRect(r, color);
	}
	
	private void drawButtonOn(Canvas ca, Bitmap bm, ImageView iv) {
		this.drawSquare(ca, this.buttonWidth-2, this.buttonHeight-2, this.black);
		this.drawSquare(ca, this.buttonWidth-2, this.buttonHeight-2, this.greenFill);
		iv.setImageBitmap(bm);
	}
	
	private void drawButtonOff(Canvas ca, Bitmap bm, ImageView iv) {
		this.drawSquare(ca, this.buttonWidth-2, this.buttonHeight-2, this.black);
		this.drawSquare(ca, this.buttonWidth-2, this.buttonHeight-2, this.greenStroke);
		iv.setImageBitmap(bm);
	}
	
	private void drawSoftOn() {
		this.drawSquare(this.caMidButton, 60, this.buttonHeight, this.greenFill);
		this.caMidButton.drawBitmap(this.bmSoftOn, 10, ((float)this.buttonHeight)/2 - 20, null);
		this.ivMidButton.setImageBitmap(this.bmMidButton);
	}
	
	private void drawSoftOff() {
		this.drawSquare(this.caMidButton, 60, this.buttonHeight, this.black);
		this.caMidButton.drawBitmap(this.bmSoftOff, 10, ((float)this.buttonHeight)/2 - 20, null);
		this.ivMidButton.setImageBitmap(this.bmMidButton);
	}
}
