package jsera.util;

import java.awt.*;

public abstract class World implements Updatable {
	protected int ID;
	protected static UIDHandler oUID = new UIDHandler();
	//
	protected String[] aImages;
	
	public World() {
		this.ID = oUID.getUID();
	}
	
	public void finalize() {
		oUID.releaseUID(this.ID);
	}
	
	public boolean equals(World o) {
		boolean result = false;
		if (o.ID == this.ID) {
			result = true;
		}
		return result;
	}
	
	// update moves anything that needs to be moved, etc.
	
	public void update(float elapsed) {
	}
	
	public void onEnter() {
	}
	
	public void onExit() {
	}
	
	public void onPlay() {
	}
	
	public void onPause() {
	}
	
	// render everything here
	
	public void paint(Graphics g) {
	}
	
	// seperate initialization thing
	public void init() {
	}
}