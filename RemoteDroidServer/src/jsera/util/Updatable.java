package jsera.util;


public interface Updatable {
	public void update(float elapsed);
	// events
	public void onEnter();
	public void onExit();
	public void onPlay();
	public void onPause();
}