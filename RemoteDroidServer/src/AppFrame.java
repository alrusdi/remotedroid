import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.jar.*;

import javax.swing.*;


public class AppFrame extends Frame {
	/**
	 * 
	 */
	
	private static final long serialVersionUID = 1L;
	//
	public static JarFile jar;
	public static String basePath = "";
	public static InetAddress localAddr;
	
	//
	private String[] textLines = new String[6];
	//
	private Image imLogo;
	private Image imHelp;
	private Font fontTitle;
	private Font fontText;
	//
	private Timer timer;
	//
	private int height = 510;
	private int width = 540;
	//
	private OSCWorld world;
	//
	private String appName = "RemoteDroid Server R2"; //added R2 so that version 2 of client will not confuse users as R2 is not needed for all features, and a future Client v3.0 might still use R2/v2.0 of the server
	//
	private Toolkit toolkit;
	private MediaTracker tracker;
	
	public AppFrame() {
		super();
		GlobalData.oFrame = this;
		this.setSize(this.width, this.height);
		//
		this.toolkit = Toolkit.getDefaultToolkit();
		this.tracker = new MediaTracker(this);
		//
		//this.init();
		// get local IP
		String sHost = "";
		try {
			localAddr = InetAddress.getLocalHost();
			if (localAddr.isLoopbackAddress()) {
				localAddr = LinuxInetAddress.getLocalHost();
			}
			sHost = localAddr.getHostAddress();
		} catch (UnknownHostException ex) {
			sHost = "Error finding local IP.";
		}
		//
		this.textLines[0] = "The RemoteDroid server application is now running.";
		this.textLines[1] = "";
		this.textLines[2] = "Your IP address is: "+sHost;
		this.textLines[3] = "";
		this.textLines[4] = "Enter this IP address on the start screen of the";
		this.textLines[5] = "RemoteDroid application on your phone to begin.";
		//
		try {
			URL fileURL = this.getClass().getProtectionDomain().getCodeSource().getLocation();
			String sBase = fileURL.toString();
			if ("jar".equals(sBase.substring(sBase.length()-3, sBase.length()))) {
				jar = new JarFile(new File(fileURL.toURI()));
				
			} else {
				basePath = System.getProperty("user.dir") + "\\res\\";
			}
		} catch (Exception ex) {
			this.textLines[1] = "exception: "+ex.toString();
			
		}
		
	}
	
	public Image getImage(String sImage) {
		Image imReturn = null;
		try {
			if (jar == null) {
				imReturn = this.toolkit.createImage(this.getClass().getClassLoader().getResource(sImage));
			} else {
				//
				BufferedInputStream bis = new BufferedInputStream(jar.getInputStream(jar.getEntry(sImage)));
				ByteArrayOutputStream buffer=new ByteArrayOutputStream(4096);
				int b;
				while((b=bis.read())!=-1) {
					buffer.write(b);
				}
				byte[] imageBuffer=buffer.toByteArray();
				imReturn = this.toolkit.createImage(imageBuffer);
				bis.close();
				buffer.close();
			}
		} catch (IOException ex) {
			
		}
		return imReturn;
	}
	
	public void init() {
		//
		try {
			this.imLogo = this.getImage("icon.gif");
			tracker.addImage(this.imLogo, 0);
			tracker.waitForID(0);
		} catch (InterruptedException inex) {
			
		}
		//
		try {
			this.imHelp = this.getImage("helpphoto.jpg");
			tracker.addImage(this.imHelp, 1);
			tracker.waitForID(1);
		} catch (InterruptedException ie) {
		}
		//
		this.fontTitle = new Font("Verdana", Font.BOLD, 16);
		this.fontText = new Font("Verdana", Font.PLAIN, 11);
		this.setBackground(Color.BLACK);
		this.setForeground(Color.WHITE);
		//
		this.timer = new Timer(500, new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				world = new OSCWorld();
				world.onEnter();
				//
				repaint();
				timer.stop();
			}
		});
		this.timer.start();
		
	}
	
	public void paint(Graphics g) {
		g.setColor(this.getBackground());
		g.fillRect(0, 0, this.width, this.height);
		g.setColor(this.getForeground());
		//
		g.drawImage(this.imLogo, 10, 30, this);
		g.setFont(this.fontTitle);
		g.drawString(this.appName, 70, 55);
		//
		g.setFont(this.fontText);
		int startY = 90;
		int l = 6;
		for (int i = 0;i<l;++i) {
			g.drawString(this.textLines[i], 10, startY);
			startY += 13;
		}
		//
		g.drawImage(this.imHelp, 20, startY+10, this);
	}
	/*
	*/
}