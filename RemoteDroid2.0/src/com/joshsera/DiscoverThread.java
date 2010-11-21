package com.joshsera;

import android.util.*;
import java.io.*;
import java.net.*;

public class DiscoverThread extends Thread {
	//
	private static final String TAG = "DiscoverThread";
	private static int BUFFER_LENGTH = 1024;
	public static String MULTICAST_ADDRESS = "230.6.6.6";
	private static final String ID_REQUEST = "RemoteDroid:AnyoneHome";
	private static final String ID_REQUEST_RESPONSE = "RemoteDroid:ImHome";
	
	//
	private int port = 57111;
	private MulticastSocket socket;
	private DatagramSocket inSocket;
	private DiscoverListener listener;
	
	public DiscoverThread(DiscoverListener listener) {
		this.listener = listener;
	}
	
	public DiscoverThread(int port, DiscoverListener listener) {
		this.port = port;
		this.listener = listener;
	}
	
	public void run() {
		try {
			this.socket = new MulticastSocket(this.port);
			this.socket.joinGroup(InetAddress.getByName(MULTICAST_ADDRESS));
			this.inSocket = new DatagramSocket(this.port+1);
			this.sendIDRequest();
			this.waitForResponse();
		} catch (IOException e) {
			
		} catch (InterruptedException e) {
			
		}
	}
	
	public void closeSocket() {
		this.socket.close();
		this.inSocket.close();
	}
	
	private void sendIDRequest() throws IOException, InterruptedException {
		byte[] b = ID_REQUEST.getBytes();
		DatagramPacket packet = new DatagramPacket(b, b.length);
		packet.setAddress(InetAddress.getByName(MULTICAST_ADDRESS));
		packet.setPort(this.port);
		this.socket.send(packet);
		Thread.sleep(500);
	}
	
	private void waitForResponse() throws IOException {
		byte[] b = new byte[BUFFER_LENGTH];
		DatagramPacket packet = new DatagramPacket(b, b.length);
		//Log.d(TAG, "Going to wait for packet");
		while (true) {
			this.inSocket.receive(packet);
			this.handleReceivedPacket(packet);
		}
	}
	
	// 
	
	private void handleReceivedPacket(DatagramPacket packet) {
		String data = new String(packet.getData());
		//Log.d(TAG, "Got packet! data:"+data);
		//Log.d(TAG, "IP:"+packet.getAddress().getHostAddress());
		if (data.substring(0, ID_REQUEST_RESPONSE.length()).compareTo(ID_REQUEST_RESPONSE) == 0) {
			// We've received a response. Notify the listener
			this.listener.onAddressReceived(packet.getAddress().getHostAddress());
		}
	}
	
	//
	
	public static interface DiscoverListener {
		void onAddressReceived(String address);
	}
}
