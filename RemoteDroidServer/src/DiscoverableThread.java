import java.io.*;
import java.net.*;

public class DiscoverableThread extends Thread {
	//
	private static int BUFFER_LENGTH = 1024;
	public static String MULTICAST_ADDRESS = "230.6.6.6";
	private static final String ID_REQUEST = "RemoteDroid:AnyoneHome";
	private static final String ID_REQUEST_RESPONSE = "RemoteDroid:ImHome";
	
	//
	private int port = 57111;
	private MulticastSocket socket;

	public DiscoverableThread() {
		// TODO Auto-generated constructor stub
	}
	
	public DiscoverableThread(int port) {
		this.port = port;
	}

	public DiscoverableThread(Runnable target) {
		super(target);
		// TODO Auto-generated constructor stub
	}

	public DiscoverableThread(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public DiscoverableThread(ThreadGroup group, Runnable target) {
		super(group, target);
		// TODO Auto-generated constructor stub
	}

	public DiscoverableThread(ThreadGroup group, String name) {
		super(group, name);
		// TODO Auto-generated constructor stub
	}

	public DiscoverableThread(Runnable target, String name) {
		super(target, name);
		// TODO Auto-generated constructor stub
	}

	public DiscoverableThread(ThreadGroup group, Runnable target, String name) {
		super(group, target, name);
		// TODO Auto-generated constructor stub
	}

	public DiscoverableThread(ThreadGroup group, Runnable target, String name,
			long stackSize) {
		super(group, target, name, stackSize);
		// TODO Auto-generated constructor stub
	}
	
	//
	
	public void run() {
		try {
			byte[] b = new byte[BUFFER_LENGTH];
			DatagramPacket packet = new DatagramPacket(b, b.length);
			this.socket = new MulticastSocket(this.port);
			this.socket.joinGroup(InetAddress.getByName(MULTICAST_ADDRESS));
			while (true) {
				this.socket.receive(packet);
				this.handlePacket(packet);
			}
		} catch (IOException e) {
			
		} catch (InterruptedException e) {
			
		}
	}
	
	private void handlePacket(DatagramPacket packet) throws IOException, InterruptedException {
		String data = new String(packet.getData());
		System.out.println("Got data:"+data);
		if (data.substring(0, ID_REQUEST.length()).equals(ID_REQUEST)) {
			System.out.println("Request message!");
			// we'll send a response!
			byte[] b = ID_REQUEST_RESPONSE.getBytes();
			DatagramPacket p = new DatagramPacket(b, b.length);
			p.setAddress(packet.getAddress());
			p.setPort(this.port+1);
			// wait half a second just in case.
			Thread.sleep(500);
			DatagramSocket outSocket = new DatagramSocket();
			//outSocket.joinGroup(InetAddress.getByName(MULTICAST_ADDRESS));
			outSocket.send(p);
		}
	}
}
