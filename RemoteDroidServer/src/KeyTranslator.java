import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class KeyTranslator {
	//
	public HashMap codes;
	//
	private int[] modifiers;
	private int[] shifts;
	private int[] ctrls;
	private int[] leftClicks;
	//
	protected Document myDoc;
	
	public KeyTranslator() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		String sPath = "config.xml";
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			if (AppFrame.jar == null) {
				this.myDoc = builder.parse( new File((AppFrame.basePath+sPath).replace('\\', '/')) );//ensure seperator is '/' as linux is picky
			} else {
				this.myDoc = builder.parse(AppFrame.jar.getInputStream(AppFrame.jar.getJarEntry(sPath)));
			}
	    } catch (SAXException sxe) {
	       // Error generated during parsing
	       Exception  x = sxe;
	       if (sxe.getException() != null)
	           x = sxe.getException();
	       x.printStackTrace();
	
	    } catch (ParserConfigurationException pce) {
	       // Parser with specified options can't be built
	       pce.printStackTrace();
	
	    } catch (IOException ioe) {
	       // I/O error
	       ioe.printStackTrace();
	    }
		Element config = this.myDoc.getDocumentElement();
		// how many modifier nodes?
		NodeList mods = config.getElementsByTagName("modifier");
		int l = mods.getLength();
		int i;
		this.modifiers = new int[l];
		for (i=0;i<l;i++) {
			this.modifiers[i] = Integer.parseInt(((Element)mods.item(i)).getAttribute("code"));
		}
		// shift keys
		mods = config.getElementsByTagName("shift");
		l = mods.getLength();
		this.shifts = new int[l];
		for (i=0;i<l;i++) {
			this.shifts[i] = Integer.parseInt(((Element)mods.item(i)).getAttribute("code"));
		}
		// ctrl keys
		mods = config.getElementsByTagName("ctrl");
		l = mods.getLength();
		this.ctrls = new int[l];
		for (i=0;i<l;++i) {
			this.ctrls[i] = Integer.parseInt(((Element)mods.item(i)).getAttribute("code"));
		}
		// fill the keycodedata hashmap
		this.codes = new HashMap();
		KeyCodeData data;
		int keycode;
		Element keydata;
		mods = config.getElementsByTagName("key");
		l = mods.getLength();
		for (i=0;i<l;++i) {
			data = new KeyCodeData();
			keydata = (Element)mods.item(i);
			data.name = keydata.getAttribute("name");
			data.modshifted = "1".compareTo(keydata.getAttribute("modshift")) == 0;
			data.localcode = Integer.parseInt(keydata.getAttribute("localcode"));
			data.modifiedcode = Integer.parseInt(keydata.getAttribute("modified"));
			data.shifted = "1".compareTo(keydata.getAttribute("shifted")) == 0;
			data.shiftedcode = Integer.parseInt(keydata.getAttribute("shiftedcode"));
			keycode = Integer.parseInt(keydata.getAttribute("code"));
			//
			this.codes.put(new Integer(keycode), data);
		}
		// simulated mouse button
		mods = config.getElementsByTagName("leftclick");
		l = mods.getLength();
		this.leftClicks = new int[l];
		for (i=0;i<l;++i) {
			this.leftClicks[i] = Integer.parseInt(((Element)mods.item(i)).getAttribute("code"));
		}
	}
	
	public boolean isModifier(int keycode) {
		int i;
		int l = this.modifiers.length;
		for (i=0;i<l;++i) {
			if (keycode == this.modifiers[i]) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isShift(int keycode) {
		int i;
		int l = this.shifts.length;
		for (i=0;i<l;++i) {
			if (keycode == this.shifts[i]) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isCtrl(int keycode) {
		int i;
		int l = this.ctrls.length;
		for (i=0;i<l;++i) {
			if (keycode == this.ctrls[i]) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isLeftClick(int keycode) {
		int i;
		int l = this.leftClicks.length;
		for (i=0;i<l;++i) {
			if (keycode == this.leftClicks[i]) {
				return true;
			}
		}
		return false;
	}
	
}
