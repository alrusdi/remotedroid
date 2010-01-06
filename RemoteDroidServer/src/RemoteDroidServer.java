
import java.awt.event.*;
import java.util.*;

/**
 * to-do:
 * add mouse sensitivity scroller
 */


public class RemoteDroidServer {
	private static AppFrame f;
	
	public static void main(String[] args) {
		
		f = new AppFrame();
		f.setVisible(true);
		f.setResizable(false);
		f.setTitle("RemoteDroid Server");
		
		f.addWindowListener(new WindowAdapter() {
	    	public void windowClosing(WindowEvent e) {
		        f.setVisible(false);
		        f.dispose();
		        System.exit(0);
	    	}
	    });
		/*
		f = new Frame();
		
	    */
		f.init();
		//
		System.out.println(System.getProperty("os.name"));

	}
}