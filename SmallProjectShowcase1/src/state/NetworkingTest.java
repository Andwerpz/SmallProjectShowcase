package state;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

import input.Button;
import input.InputManager;
import input.TextField;

public class NetworkingTest extends State {
	
	private InputManager im;
	
	private String localIPAddress, publicIPAddress;
	private String ip = "localhost";
	private int port = 22222;
	
	// -- CLIENT --
	private boolean connectedToServer = false;
	private Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;
	
	// -- SERVER / HOST --
	private boolean isHosting = false;
	private NetworkingTestServer networkingTestServer;
	
	// -- GRAPHICS --
	private ArrayList<Point> mousePositions;
	private int numConnectedClients = 0;

	public NetworkingTest(StateManager gsm) {
		super(gsm);
		
		this.im = new InputManager();
		this.im.addInput(new Button(10, 40, 100, 25, "Copy Local IP", "btn_copy_local_ip"));
		this.im.addInput(new Button(10, 70, 100, 25, "Copy Public IP", "btn_copy_public_ip"));
		
		this.im.addInput(new TextField(10, 110, 100, "IP", "tf_ip"));
		this.im.addInput(new TextField(10, 140, 100, "Port", "tf_port"));
		this.im.addInput(new Button(10, 170, 100, 25, "Connect", "btn_connect"));
		
		this.mousePositions = new ArrayList<>();

		// Returns the instance of InetAddress containing local host name and address
		InetAddress localhost = null;
		try {
			localhost = InetAddress.getLocalHost();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		localIPAddress = localhost.getHostAddress().trim();
		System.out.println("Local IP Address : " + localIPAddress);

		// Find public IP address
		publicIPAddress = "";
		try {
			URL url_name = new URL("https://v4.ident.me/");

			BufferedReader sc = new BufferedReader(new InputStreamReader(url_name.openStream()));

			// reads system IPAddress
			publicIPAddress = sc.readLine().trim();
		} catch (Exception e) {
			publicIPAddress = "Cannot Execute Properly";
		}
		System.out.println("Public IP Address: " + publicIPAddress + "\n");
	}

	private boolean connect() {
		try {
			socket = new Socket(ip, port);
			dos = new DataOutputStream(socket.getOutputStream());
			dis = new DataInputStream(socket.getInputStream());
		} catch (IOException e) {
			System.out.println("Unable to connect to the address: " + ip + ":" + port + " | Starting a server");
			this.isHosting = true;
			this.networkingTestServer = new NetworkingTestServer(ip, port);
			return false;
		}
		System.out.println("Successfully connected to the address: " + ip + ":" + port);
		this.connectedToServer = true;
		return true;
	}

	@Override
	public void init() {
	}

	@Override
	public void tick(Point mouse2) {
		
		this.im.tick(mouse2);
		
		if(this.connectedToServer) {
			try {
				this.dos.writeInt(mouse2.x);
				this.dos.writeInt(mouse2.y);
				this.dos.flush();
			} catch(IOException e) {
				e.printStackTrace();
			}
			
			try {
				this.numConnectedClients = this.dis.readInt();
				this.mousePositions.clear();
				for(int i = 0; i < this.numConnectedClients; i++) {
					this.mousePositions.add(new Point(this.dis.readInt(), this.dis.readInt()));
				}
			} catch (IOException e) {
				//likely a connection reset. 
				if(this.isHosting) {
					System.out.println("BAD HOST");
					this.networkingTestServer.exit();
					this.isHosting = false;
				}
				while(!this.connect());
			}
		}
		
	}

	@Override
	public void draw(Graphics g) {
		this.im.draw(g);
		if(this.connectedToServer) {
			g.drawString(numConnectedClients + " client" + (numConnectedClients > 1? "s" : "") + " connected", 10, 10);
			g.drawString(this.isHosting? "HOST" : "CLIENT", 10, 30);
		}
		else {
			g.drawString("Not Connected to a Server", 10, 10);
		}
		for(Point p : this.mousePositions) {
			g.drawRect(p.x - 2, p.y - 2, 4, 4);
		}
	}
	
	private void copyToClipboard(String s) {
	    StringSelection selection = new StringSelection(s);
	    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	    clipboard.setContents(selection, selection);
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		this.im.keyPressed(arg0);
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		this.im.keyReleased(arg0);
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		this.im.keyTyped(arg0);
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		String which = this.im.mouseClicked(arg0);
		switch(which) {
		case "btn_copy_local_ip":
			this.copyToClipboard(this.localIPAddress);
			break;
			
		case "btn_copy_public_ip":
			this.copyToClipboard(this.publicIPAddress);
			break;
			
		case "btn_connect":
			this.ip = this.im.getText("tf_ip");
			this.port = Integer.parseInt(this.im.getText("tf_port"));
			if(this.networkingTestServer != null) {
				this.networkingTestServer.exit();
			}
			this.isHosting = false;
			while(!this.connect());
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		this.im.mousePressed(arg0);
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		this.im.mouseReleased(arg0);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		// TODO Auto-generated method stub

	}
	
	class NetworkingTestServer implements Runnable {
		private boolean isRunning = true;
		private Thread thread;
		
		private String ip;
		private int port;
		
		private ServerRequestListener serverRequestListener;
		
		private ServerSocket serverSocket;
		private ArrayList<Socket> clientSockets;
		
		public NetworkingTestServer(String ip, int port) {
			this.ip = ip;
			this.port = port;
			
			this.serverSocket = null;
			try {
				this.serverSocket = new ServerSocket(this.port, 8, InetAddress.getByName(this.ip));
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			this.clientSockets = new ArrayList<>();
			this.serverRequestListener = new ServerRequestListener(this.serverSocket);
			
			this.start();
		}
		
		private void start() {
			this.thread = new Thread(this);
			this.thread.start();
		}

		public void run() {
			while(this.isRunning) {
				this.clientSockets.addAll(this.serverRequestListener.getNewClients());
				
				// -- READ --
				ArrayList<Point> mousePositions = new ArrayList<>();
				for(int i = this.clientSockets.size() - 1; i >= 0; i--) {
					Socket s = this.clientSockets.get(i);
					try {
						DataInputStream dis = new DataInputStream(s.getInputStream());
						mousePositions.add(new Point(dis.readInt(), dis.readInt()));
					} catch(IOException e) {
						//Client Disconnected
						System.out.println("Client Disconnected");
						this.clientSockets.remove(i);
					}
				}
				
				// -- WRITE --
				for(int i = this.clientSockets.size() - 1; i >= 0; i--) {
					Socket s = this.clientSockets.get(i);
					try {
						DataOutputStream dos = new DataOutputStream(s.getOutputStream());
						
						dos.writeInt(this.clientSockets.size());
						for(Point p : mousePositions) {
							dos.writeInt(p.x);
							dos.writeInt(p.y);
						}
						dos.flush();
					} catch(IOException e) {
						e.printStackTrace();
					}
				}
				
			}
		}
		
		public void exit() {
			System.out.println("Closing server at " + ip + ":" + port);
			this.serverRequestListener.exit();
			
			try {
				this.serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			this.isRunning = false;
		}
		
	}
	
	class ServerRequestListener implements Runnable {
		private boolean isRunning = true;
		private Thread thread;
		
		private ServerSocket serverSocket;	//socket on which to listen for connection requests
		private ArrayList<Socket> newClients;
		
		public ServerRequestListener(ServerSocket serverSocket) {
			this.serverSocket = serverSocket;
			this.newClients = new ArrayList<>();
			this.start();
		}
		
		private void start() {
			this.thread = new Thread(this);
			this.thread.start();
		}
		
		public void run() {
			while(this.isRunning) {
				Socket s = listenForServerRequest();
				if(s != null) {
					this.newClients.add(s);
				}
			}
		}
		
		public ArrayList<Socket> getNewClients(){
			ArrayList<Socket> out = new ArrayList<>();
			out.addAll(this.newClients);
			this.newClients.clear();
			return out;
		}
		
		private Socket listenForServerRequest() {
			try {
				System.out.println("Listening for connection requests");
				Socket socket = this.serverSocket.accept();
				System.out.println("Client has joined");
				return socket;
			} catch (IOException e) {
				System.out.println("No connection requests");
			}
			return null;
		}
		
		public void exit() {
			this.isRunning = false;
		}
	}

}
