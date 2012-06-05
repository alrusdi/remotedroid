
import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.sun.media.sound.Toolkit;

/**
 * to-do:
 * add mouse sensitivity scroller
 */


public class RemoteDroidServer {
	private static AppFrame f;
	
	public static void main(String[] args) {
		
		System.out.println("RemoteDroid Server is running.");
			
		f = new AppFrame();
		f.setResizable(false);
		f.setTitle("RemoteDroid Server");
		
		f.addWindowListener(new WindowAdapter() {
	    	public void windowClosing(WindowEvent e) {
		        f.setVisible(false);
		        f.dispose();
		        System.exit(0);
	    	}
	    	
	    	public void windowIconified(WindowEvent e) {
	    		f.setVisible(false);
	    	}
	    });

		f.init();
		boolean windowShown = true;
		
		// Hide the GUI by default.
		for(String arg : args){
			if(arg.equals("--hidden")){
				System.out.println("Interface is hidden. Use the tray icon (if available) to restore it.");
				windowShown = false;
			}
		}

		f.setVisible(windowShown);
		
		final TrayIcon trayIcon;

		if (SystemTray.isSupported()) {
			
		    //SystemTray tray = SystemTray.getSystemTray();
		    ImageIcon icon = new ImageIcon(RemoteDroidServer.class.getResource("icon.gif"));

		    TrayIcon tray = new TrayIcon(icon.getImage().getScaledInstance(24, 24, Image.SCALE_DEFAULT), "Remotedroid Server");
		    
		    tray.addMouseListener(new MouseListener(){

				public void mouseClicked(MouseEvent e) {
					if(f.isVisible())
						f.setVisible(false);
					else
						f.setVisible(true);
				}
				public void mouseEntered(MouseEvent e) {}
				public void mouseExited(MouseEvent e) {}
				public void mousePressed(MouseEvent e) {}
				public void mouseReleased(MouseEvent e) {}
		    });
		    
		    try {
		    	SystemTray.getSystemTray().add(tray);
		    } catch (AWTException e) {
		    	e.printStackTrace();
		    }
		}

	}
}
