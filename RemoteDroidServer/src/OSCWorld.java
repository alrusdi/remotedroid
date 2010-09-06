import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Label;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.InetAddress;

import jsera.util.World;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPort;
import com.illposed.osc.OSCPortIn;

/**
 * 
 * @author jsera
 * 
 * 
 */

public class OSCWorld extends World {
	//
	private static final float sensitivity = 1.6f;

	//
	private OSCPortIn receiver;
	//
	private Robot robot;
	//
	private boolean shifted = false;
	private boolean modified = false;
	//
	private KeyTranslator translator;
	//
	private GraphicsDevice[] gDevices;
	private Rectangle[] gBounds;
	//
	private Label lbDebug;
	//
	private int scrollMod = -1;
	//
	private float xLeftover = 0; //for subpixel mouse accuracy
	private float yLeftover = 0; //for subpixel mouse accuracy

	public OSCWorld() {
		super();

	}

	public void onEnter() {
		try {
			this.robot = new Robot();
			this.robot.setAutoDelay(5);
			//
			this.translator = new KeyTranslator();
			//
			InetAddress local = InetAddress.getLocalHost();
			if (local.isLoopbackAddress()) {
				this.receiver = new OSCPortIn(OSCPort.defaultSCOSCPort());
			} else {
				this.receiver = new OSCPortIn(OSCPort.defaultSCOSCPort());
			}
			OSCListener listener = new OSCListener() {
				public void acceptMessage(java.util.Date time, OSCMessage message) {
					Object[] args = message.getArguments();
					if (args.length == 3) {
						mouseEvent(Integer.parseInt(args[0].toString()), Float.parseFloat(args[1]
								.toString()), Float.parseFloat(args[2].toString()));
					}
				}
			};
			this.receiver.addListener("/mouse", listener);
			//
			listener = new OSCListener() {
				public void acceptMessage(java.util.Date time, OSCMessage message) {
					Object[] args = message.getArguments();
					if (args.length == 1) {
						buttonEvent(Integer.parseInt(args[0].toString()), 0);
					}
				}
			};
			this.receiver.addListener("/leftbutton", listener);
			//
			listener = new OSCListener() {
				public void acceptMessage(java.util.Date time, OSCMessage message) {
					Object[] args = message.getArguments();
					if (args.length == 1) {
						buttonEvent(Integer.parseInt(args[0].toString()), 2);
					}
				}
			};
			this.receiver.addListener("/rightbutton", listener);
			//
			listener = new OSCListener() {
				public void acceptMessage(java.util.Date time, OSCMessage message) {
					Object[] args = message.getArguments();
					if (args.length == 3) {
						keyboardEvent(Integer.parseInt(args[0].toString()), Integer
								.parseInt(args[1].toString()), args[2].toString());
					}
					if (args.length == 2) { //handle raw keyboard event, no translations
						keyboardEvent(Integer.parseInt(args[0].toString()), Integer
								.parseInt(args[1].toString()));
					}
				}
			};
			this.receiver.addListener("/keyboard", listener);
			//
			listener = new OSCListener() {
				public void acceptMessage(java.util.Date time, OSCMessage message) {
					Object[] args = message.getArguments();
					if (args.length == 1) {
						scrollEvent(Integer.parseInt(args[0].toString()));
					}
				}
			};
			this.receiver.addListener("/wheel", listener);
			//
			listener = new OSCListener() {
				public void acceptMessage(java.util.Date time, OSCMessage message) {
					Object[] args = message.getArguments();
					if (args.length == 6) {
						orientEvent(Float.parseFloat(args[0].toString()), Float.parseFloat(args[1]
								.toString()), Float.parseFloat(args[2].toString()), Float
								.parseFloat(args[3].toString()), Float.parseFloat(args[4]
								.toString()), Float.parseFloat(args[5].toString()));
					}
				}
			};
			this.receiver.addListener("/orient", listener);
			//
			this.receiver.startListening();
			// debug
			GlobalData.oFrame.addKeyListener(new KeyListener() {
				public void keyReleased(KeyEvent e) {
					nativeKeyEvent(e);
				}

				public void keyPressed(KeyEvent e) {

				}

				public void keyTyped(KeyEvent e) {

				}
			});
			//
			this.gDevices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
			int l = this.gDevices.length;
			this.gBounds = new Rectangle[l];
			for (int i = 0; i < l; ++i) {
				this.gBounds[i] = this.gDevices[i].getDefaultConfiguration().getBounds();
			}
			// GlobalData.oBase.pause();
			this.initUI();
			//
			if (System.getProperty("os.name").compareToIgnoreCase("Mac OS X") == 0) {
				// hack for robot class bug.
				this.scrollMod = 1;
			}
		} catch (Exception ex) {

		}
	}

	// check keyboard events

	private void nativeKeyEvent(KeyEvent ev) {
	}

	//

	private void mouseEvent(int type, float xOffset, float yOffset) {
		if (type == 2) {
			PointerInfo info = MouseInfo.getPointerInfo();
			if (info != null) {
				java.awt.Point p = info.getLocation();
				//for sub-pixel mouse accuracy, save leftover rounding value
				float ox = (xOffset * sensitivity) + xLeftover;
				float oy = (yOffset * sensitivity) + yLeftover;				
				int ix = Math.round(ox);
				int iy = Math.round(oy);
				xLeftover = ox-ix;
				yLeftover = oy-iy;
				//
				p.x += ix;
				p.y += iy;
				int l = this.gBounds.length;
				for (int i = 0; i < l; ++i) {
					if (this.gBounds[i].contains(p)) {
						this.robot.mouseMove(p.x, p.y);
						break;
					}
				}
				
				try{
					this.robot.mouseMove(p.x, p.y);//for systems with quirky bounds checking, allow mouse to move smoothly along to and left edges
				}catch(Exception e){}
				
			}
		}
	}

	private void buttonEvent(int type, int button) {
		if (button == 0) {
			button = InputEvent.BUTTON1_MASK;
		} else if (button == 2) {
			button = InputEvent.BUTTON3_MASK;
		}
		switch (type) {
		case 0:
			//
			this.robot.mousePress(button);
			this.robot.waitForIdle();
			break;
		case 1:
			//
			this.robot.mouseRelease(button);
			this.robot.waitForIdle();
			break;
		}
	}

	private void scrollEvent(int dir) {
		this.robot.mouseWheel(-dir * this.scrollMod);
	}

	//Raw keyboard event, no translation, intercepted when argument count is 2
	private void keyboardEvent(int type, int keycode) {
		switch (type) {
		case 0:
			// key down
			if (this.translator.isShift(keycode)) {
				this.shifted = true;
				this.keyPress(KeyEvent.VK_SHIFT);
			} else {
				this.keyPress(keycode);
			}
			break;
		case 1:
			// key up
			if (this.translator.isShift(keycode)) {
				this.shifted = false;
				keyRelease(KeyEvent.VK_SHIFT);
			} else {
				this.keyRelease(keycode);
			}
			break;
		}		
	}
	
	private void keyboardEvent(int type, int keycode, String value) {
		//
		KeyCodeData data;
		
		switch (type) {
		case 0:
			// key down

			// check if it isn't a mouse click (trackpad "enter" = left button)
			if (this.translator.isLeftClick(keycode)) {
				buttonEvent(0, 0);
				return;
			}
			//
			data = (KeyCodeData) translator.codes.get(new Integer(keycode));
			// it's not a mouse event, treat as key
			if (this.translator.isModifier(keycode)) {
				this.modified = true;
			}
			if (this.translator.isShift(keycode)) {
				this.shifted = true;
				this.keyPress(KeyEvent.VK_SHIFT);
			}
			if (this.translator.isCtrl(keycode)) {
				this.keyPress(KeyEvent.VK_CONTROL);
			}
			if (data != null) {
				// for some of the symbols, like at.
				if (!this.shifted && data.shifted) {
					this.keyPress(KeyEvent.VK_SHIFT);
				}
				if (this.modified) {
					if (data.modshifted && !this.shifted) {
						this.keyPress(KeyEvent.VK_SHIFT);
						//System.out.println("Keycode:"+String.valueOf(keycode)+", local:"+String.valueOf(data.localcode));
					}
					if (!data.modshifted && this.shifted) {
						this.keyRelease(KeyEvent.VK_SHIFT);
					}
					//
					if (data.modifiedcode != -1)
						this.keyPress(data.modifiedcode);
					//
					if (data.modshifted && !this.shifted) {
						this.keyRelease(KeyEvent.VK_SHIFT);
					}
					if (!data.modshifted && this.shifted) {
						this.keyPress(KeyEvent.VK_SHIFT);
					}
				} else {
					try {
						if (this.shifted && data.shiftedcode != -1) {
							this.keyPress(data.shiftedcode);
						} else {
							this.keyPress(data.localcode);
						}
					} catch (IllegalArgumentException e) {
						System.out.println("Invalid key code: " + data.localcode);
					}
				}
			}
			break;
		case 1:
			// key up

			// check if it isn't a mouse click (trackpad "enter" = left button)
			if (this.translator.isLeftClick(keycode)) {
				buttonEvent(1, 0);
				return;
			}
			//
			data = (KeyCodeData) translator.codes.get(new Integer(keycode));
			// it's not a mouse event, treat as key
			if (this.translator.isModifier(keycode)) {
				this.modified = false;
			}
			if (this.translator.isShift(keycode)) {
				this.shifted = false;
				keyRelease(KeyEvent.VK_SHIFT);
			}
			if (this.translator.isCtrl(keycode)) {
				keyRelease(KeyEvent.VK_CONTROL);
			}
			if (data != null) {
				// for some of the symbols, like at.
				if (!this.shifted && data.shifted) {
					this.keyRelease(KeyEvent.VK_SHIFT);
				}
				if (this.modified) {
					if (data.modshifted && !this.shifted) {
						this.keyPress(KeyEvent.VK_SHIFT);
					}
					if (!data.modshifted && this.shifted) {
						this.keyRelease(KeyEvent.VK_SHIFT);
					}
					//
					if (data.modifiedcode != -1)
						this.keyRelease(data.modifiedcode);
					//
					if (data.modshifted && !this.shifted) {
						this.keyRelease(KeyEvent.VK_SHIFT);
					}
					if (!data.modshifted && this.shifted) {
						this.keyPress(KeyEvent.VK_SHIFT);
					}
				} else {
					if (this.shifted && data.shiftedcode != -1) {
						this.keyRelease(data.shiftedcode);
					} else {
						this.keyRelease(data.localcode);
					}
				}
			}
			break;
		}
	}

	/**
	 * Calls the robot method and catches exceptions due to invalid key.
	 * 
	 * @param localcode
	 */
	private void keyPress(int localcode) {
		try {
			this.robot.keyPress(localcode);
		} catch (IllegalArgumentException e) {
			System.out.println("Invalid keyPress code: " + localcode);
		}
	}

	/**
	 * Calls the robot method and catches exceptions due to invalid key.
	 * 
	 * @param localcode
	 */
	private void keyRelease(int localcode) {
		try {
			this.robot.keyRelease(localcode);
		} catch (IllegalArgumentException e) {
			System.out.println("Invalid keyRelease code: " + localcode);
		}
	}

	private void orientEvent(float z, float x, float y, float rawz, float rawx, float rawy) {
		StringBuilder builder = new StringBuilder();
		this.addValue(builder, "z", z);
		this.addValue(builder, "x", x);
		this.addValue(builder, "y", y);
		this.addValue(builder, "rawz", rawz);
		this.addValue(builder, "rawx", rawx);
		this.addValue(builder, "rawy", rawy);
		//
		double len = Math.sqrt(x * x + y * y + z * z);
		this.addValue(builder, "len", (float) len);
		//
		this.lbDebug.setText(builder.toString());
	}

	// UI

	private void initUI() {

	}

	private void addValue(StringBuilder builder, String name, float value) {
		builder.append(name);
		builder.append(": ");
		builder.append(value);
		builder.append("\n");
	}

	//

	public void update(float elapsed) {

	}
}