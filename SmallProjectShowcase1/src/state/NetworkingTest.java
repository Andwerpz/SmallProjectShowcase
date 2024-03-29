package state;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

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
	private boolean connectionAttemptFailed = false;
	private Socket socket;
	private PacketListener packetListener;
	private PacketSender packetSender;
	private Color clientColor;
	private int strokeWidth = 5;
	private ArrayList<int[]> leaderboard;
	
	// -- SERVER / HOST --
	private boolean isHosting = false;
	private NetworkingTestServer networkingTestServer;
	
	// -- GRAPHICS --
	private ArrayList<Point> mousePositions;
	private int numConnectedClients = 0;
	private boolean mousePressed = false;
	private Point prevMouse = new Point(0, 0);
	
	private int canvasWidth = 640;
	private int canvasHeight = 550;
	private int canvasX = 135;
	private int canvasY = 25;
	private BufferedImage canvas;
	
	private ArrayList<int[]> drawnLines;	//buffer drawn lines to be sent to the server

	public NetworkingTest(StateManager gsm) {
		super(gsm);
		
		this.im = new InputManager();
		this.im.addInput(new Button(10, 40, 100, 25, "Copy Local IP", "btn_copy_local_ip"));
		this.im.addInput(new Button(10, 70, 100, 25, "Copy Public IP", "btn_copy_public_ip"));
		
		this.im.addInput(new TextField(10, 110, 100, "IP", "tf_connect_ip"));
		this.im.addInput(new TextField(10, 130, 100, "Port", "tf_connect_port"));
		this.im.addInput(new Button(10, 150, 100, 25, "Connect", "btn_connect"));
		this.im.addInput(new Button(10, 180, 100, 25, "Disconnect", "btn_disconnect"));
		
		this.im.addInput(new TextField(10, 220, 100, "Port", "tf_host_port"));
		this.im.addInput(new Button(10, 240, 100, 25, "Host", "btn_start_hosting"));
		this.im.addInput(new Button(10, 270, 100, 25, "Stop Hosting", "btn_stop_hosting"));
		
		int r = (int) (Math.random() * 256);
		int g = (int) (Math.random() * 256);
		int b = (int) (Math.random() * 256);
		int rgb = (r << 16) + (g << 8) + (b << 0);
		
		this.clientColor = new Color(r / 255f, g / 255f, b / 255f);
		this.im.addInput(new TextField(10, 320, 100, "RGB Hex", "tf_rgb_hex"));
		this.im.setText("tf_rgb_hex", Integer.toHexString(rgb));
		
		this.leaderboard = new ArrayList<>();
		
		this.mousePositions = new ArrayList<>();
		
		this.canvas = new BufferedImage(this.canvasWidth, this.canvasHeight, BufferedImage.TYPE_INT_ARGB);
		this.drawnLines = new ArrayList<>();
		
		this.packetSender = new PacketSender();

		InetAddress localhost = null;
		try {
			localhost = InetAddress.getLocalHost();
		} catch (UnknownHostException e1) {
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
		this.connectionAttemptFailed = false;
		try {
			this.socket = new Socket(this.ip, this.port);
		} catch (IOException e) {
			this.connectionAttemptFailed = true;
			System.out.println("Unable to connect to the address: " + ip + ":" + port);
			return false;
		}
		System.out.println("Successfully connected to the address: " + ip + ":" + port);
		this.connectedToServer = true;
		this.packetListener = new PacketListener(this.socket, "Client");
		this.drawnLines = new ArrayList<>();
		((TextField) this.im.getInput("tf_rgb_hex")).disable();
		return true;
	}
	
	private void disconnect() {
		this.connectedToServer = false;
		this.connectionAttemptFailed = false;
		
		try {
			if(this.socket != null) {
				this.socket.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(this.packetListener != null) {
			this.packetListener.exit();
		}
		
		((TextField) this.im.getInput("tf_rgb_hex")).enable();
	}
	
	private void startHosting(int port) {
		if(this.isHosting) {
			this.stopHosting();
		}
		this.isHosting = true;
		this.networkingTestServer = new NetworkingTestServer(this.localIPAddress, port);
		this.ip = this.localIPAddress;
		this.port = port;
		System.out.println("Starting a server at " + ip + ":" + port);
		this.connect();
	}
	
	private void stopHosting() {
		this.isHosting = false;
		if(this.networkingTestServer != null) {
			this.networkingTestServer.exit();
		}
	}

	@Override
	public void init() {
	}

	@Override
	public void tick(Point mouse2) {
		
		this.im.tick(mouse2);
		
		String hexColor = this.im.getText("tf_rgb_hex").toUpperCase();
		try {
			int rgb = Integer.parseInt(hexColor, 16);
			this.clientColor = new Color(rgb);
		} catch(NumberFormatException e) {
			//invalid hex string
		}
		
		if(this.mousePressed) {
			int x1 = this.prevMouse.x - this.canvasX;
			int y1 = this.prevMouse.y - this.canvasY;
			int x2 = mouse2.x - this.canvasX;
			int y2 = mouse2.y - this.canvasY;
			
			Graphics gImg = this.canvas.getGraphics();
			gImg.setColor(this.clientColor);
			Graphics2D gImg2D = (Graphics2D) gImg;
			gImg2D.setStroke(new BasicStroke(this.strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			gImg.drawLine(x1, y1, x2, y2);
			
			this.drawnLines.add(new int[] {x1, y1, x2, y2, this.clientColor.getRGB(), strokeWidth});
		}
		
		if(this.connectedToServer) {
			// -- WRITE --
			try {
				this.packetSender.writeInt(mouse2.x);
				this.packetSender.writeInt(mouse2.y);
				
				this.packetSender.writeInt(this.drawnLines.size());
				for(int[] a : this.drawnLines) {
					this.packetSender.write(a);
				}
				this.packetSender.flush(this.socket);
				
				this.drawnLines.clear();
			} catch(IOException e) {
				e.printStackTrace();
			} 
			
			// -- READ --
			if(this.packetListener == null || !this.packetListener.isConnected) {	//lost connection to server
				this.disconnect();
			}
			
			while(this.packetListener.nextPacket()) {
				this.numConnectedClients = this.packetListener.readInt();
				
				//Mouse positions
				int numMouses = this.packetListener.readInt();
				this.mousePositions.clear();
				for(int i = 0; i < numMouses; i++) {
					int mouseX = this.packetListener.readInt();
					int mouseY = this.packetListener.readInt();
					this.mousePositions.add(new Point(mouseX, mouseY));
				}
				
				//Lines drawn
				int numLines = this.packetListener.readInt();
				for(int i = 0; i < numLines; i++) {
					int[] line = this.packetListener.readNInts(6);
					
					int strokeWidth = line[5];
					Graphics gImg = this.canvas.getGraphics();
					gImg.setColor(new Color(line[4]));
					Graphics2D gImg2D = (Graphics2D) gImg;
					gImg2D.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
					gImg.drawLine(line[0], line[1], line[2], line[3]);
				}
				
				//Update pixels
				int numUpdatePixels = this.packetListener.readInt();
				for(int i = 0; i < numUpdatePixels; i++) {
					int[] pixel = this.packetListener.readNInts(3);
					this.canvas.setRGB(pixel[0], pixel[1], pixel[2]);
				}
				
				//Leaderboard;
				int numLeaderboardPositions = this.packetListener.readInt();
				this.leaderboard.clear();
				for(int i = 0; i < numLeaderboardPositions; i++) {
					this.leaderboard.add(this.packetListener.readNInts(2));
				}
			}
			
		}
		
		this.prevMouse = new Point(mouse2.x, mouse2.y);
	}

	@Override
	public void draw(Graphics g) {
		this.im.draw(g);
		g.drawImage(this.canvas, this.canvasX, this.canvasY, null);
		g.drawRect(canvasX, canvasY, canvasWidth, canvasHeight);
		
		g.drawString("Color: ", 10, 315);
		g.setColor(this.clientColor);
		g.fillRect(100, 305, 10, 10);
		g.setColor(Color.BLACK);
		g.drawRect(100, 305, 10, 10);
		
		if(this.connectedToServer) {
			g.drawString(numConnectedClients + " client" + (numConnectedClients > 1? "s" : "") + " connected", 10, 10);
			g.drawString(this.isHosting? "HOST" : "CLIENT", 10, 30);
			
			//leaderboard
			int y = 351;
			int increment = 15;
			for(int i = 0; i < this.leaderboard.size(); i++) {
				int[] a = this.leaderboard.get(i);
				g.drawString((i + 1) + " : " + a[1], 10, y);
				if(a[0] >> 24 == 0) {
					a[0] = Color.white.getRGB();
				}
				g.setColor(new Color(a[0]));
				g.fillRect(100, y - 10, 10, 10);
				g.setColor(Color.BLACK);
				g.drawRect(100, y - 10, 10, 10);
				y += increment;
			}
		}
		else {
			if(this.connectionAttemptFailed) {
				g.drawString("Connection Attempt Failed", 10, 10);
			}
			else {
				g.drawString("Not Connected to a Server", 10, 10);
			}
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
		
		if(arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
			if(this.networkingTestServer != null) {
				this.networkingTestServer.exit();
			}
			if(this.packetListener != null) {
				this.packetListener.exit();
			}
			this.disconnect();
			this.exit();
		}
		
		else if(arg0.getKeyCode() == KeyEvent.VK_SEMICOLON) {
			this.strokeWidth = this.strokeWidth == 5? 50 : 5;
		}
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
		if(which != null) {
			switch(which) {
			case "btn_copy_local_ip":
				this.copyToClipboard(this.localIPAddress);
				break;
				
			case "btn_copy_public_ip":
				this.copyToClipboard(this.publicIPAddress);
				break;
				
			case "btn_connect":
				this.ip = this.im.getText("tf_connect_ip");
				this.port = Integer.parseInt(this.im.getText("tf_connect_port"));
				if(this.packetListener != null) {
					this.packetListener.exit();
				}
				this.connect();
				break;
				
			case "btn_disconnect":
				this.disconnect();
				break;
				
			case "btn_start_hosting":
				this.startHosting(Integer.parseInt(this.im.getText("tf_host_port")));
				break;
				
			case "btn_stop_hosting":
				this.stopHosting();
				break;
			}
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
		this.mousePressed = true;
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		this.im.mouseReleased(arg0);
		this.mousePressed = false;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		// TODO Auto-generated method stub

	}
	
	class NetworkingTestServer implements Runnable {
		private boolean isRunning = true;
		private Thread thread;
		
		private int FPS = 60;
		private long targetTime = 1000 / FPS;
		
		private String ip;
		private int port;
		
		private ServerRequestListener serverRequestListener;
		
		private ServerSocket serverSocket;
		private ArrayList<Socket> clientSockets;
		private ArrayList<PacketListener> packetListeners;
		private PacketSender packetSender;
		
		private int canvasWidth = 640;
		private int canvasHeight = 550;
		private BufferedImage canvas;	//to keep track of progress
		
		private LeaderboardCalculator leaderboardCalculator;
		
		private long noClientTimeoutMillis = 15000;
		private long firstNoClientTime = 0;
		private boolean prevTickNoClients = false;
		
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
			this.packetListeners = new ArrayList<>();
			this.serverRequestListener = new ServerRequestListener(this.serverSocket);
			this.packetSender = new PacketSender();
			
			this.canvas = new BufferedImage(this.canvasWidth, this.canvasHeight, BufferedImage.TYPE_INT_ARGB);
			
			this.leaderboardCalculator = new LeaderboardCalculator(this.canvas);
			
			this.start();
		}
		
		private void start() {
			this.thread = new Thread(this);
			this.thread.start();
		}

		public void run() {
			long start, elapsed, wait;
			while(isRunning) {
				start = System.nanoTime();
				
				tick();
				
				elapsed = System.nanoTime() - start;
				wait = targetTime - elapsed / 1000000;
				
				if(wait < 0) {
					wait = 5;
				}
				
				try {
					this.thread.sleep(wait);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		public void tick() {
			if(this.serverRequestListener.hasNewClients()) {
				ArrayList<Socket> newClients = this.serverRequestListener.getNewClients();
				for(Socket s : newClients) {
					PacketListener l = new PacketListener(s, "Server");
					this.clientSockets.add(s);
					this.packetListeners.add(l);
				}
			}
			
			// -- READ --	//should open for whenever
			ArrayList<Point> mousePositions = new ArrayList<>();
			ArrayList<int[]> newDrawnLines = new ArrayList<>();
			for(int i = this.clientSockets.size() - 1; i >= 0; i--) {
				if(!this.packetListeners.get(i).isConnected()) {
					//client disconnected
					System.out.println("Client disconnected");
					this.packetListeners.get(i).exit();
					this.packetListeners.remove(i);
					try {
						this.clientSockets.get(i).close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					this.clientSockets.remove(i);
					continue;
				}
				
				while(this.packetListeners.get(i).nextPacket()) {
					int mouseX = this.packetListeners.get(i).readInt();
					int mouseY = this.packetListeners.get(i).readInt();
					mousePositions.add(new Point(mouseX, mouseY));
					
					int numLines = this.packetListeners.get(i).readInt();
					for(int j = 0; j < numLines; j++) {
						int[] line = this.packetListeners.get(i).readNInts(6);
						
						//draw line on canvas
						int strokeWidth = line[5];
						Graphics gImg = this.canvas.getGraphics();
						gImg.setColor(new Color(line[4]));
						Graphics2D gImg2D = (Graphics2D) gImg;
						gImg2D.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
						gImg.drawLine(line[0], line[1], line[2], line[3]);
						
						//save line to send to clients
						newDrawnLines.add(line);
					}
				}
				
			}
			
			// -- WRITE --	//should run at set tickrate
			ArrayList<int[]> leaderboard = this.leaderboardCalculator.getLeaderboard();
			
			for(int i = this.clientSockets.size() - 1; i >= 0; i--) {
				Socket s = this.clientSockets.get(i);
				try {
					this.packetSender.writeInt(this.clientSockets.size());
					this.packetSender.writeInt(mousePositions.size());
					for(Point p : mousePositions) {
						this.packetSender.writeInt(p.x);
						this.packetSender.writeInt(p.y);
					}
					
					this.packetSender.writeInt(newDrawnLines.size());
					for(int[] a : newDrawnLines) {
						this.packetSender.write(a);
					}
					
					int numUpdatePixels = 100;
					this.packetSender.writeInt(numUpdatePixels);
					for(int j = 0; j < numUpdatePixels; j++) {
						int x = (int) (Math.random() * this.canvasWidth);
						int y = (int) (Math.random() * this.canvasHeight);
						int rgb = this.canvas.getRGB(x, y);
						this.packetSender.write(new int[] {x, y, rgb});
					}
					
					this.packetSender.writeInt(leaderboard.size());
					for(int j = 0; j < leaderboard.size(); j++) {
						this.packetSender.write(leaderboard.get(j));
					}
					
					this.packetSender.flush(s);
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
			
			if(this.clientSockets.size() == 0) {	//no more clients :((
				if(this.prevTickNoClients) {
					if(System.currentTimeMillis() - this.firstNoClientTime > this.noClientTimeoutMillis) {
						System.out.println("No clients, shutting down server");
						this.exit();
					}
				}
				else {
					this.firstNoClientTime = System.currentTimeMillis();
					this.prevTickNoClients = true;
				}
			}
			else {
				this.prevTickNoClients = false;
			}
		}
		
		public void exit() {
			System.out.println("Closing server at " + ip + ":" + port);
			this.serverRequestListener.exit();
			
			for(int i = 0; i < this.clientSockets.size(); i++) {
				try {
					this.packetListeners.get(i).exit();
					this.clientSockets.get(i).close();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
			
			try {
				this.serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			this.leaderboardCalculator.exit();
			
			this.isRunning = false;
		}
		
	}
	
	class LeaderboardCalculator implements Runnable {
		private boolean isRunning = true;
		private Thread thread;
		
		private ArrayList<int[]> leaderboard;
		private BufferedImage canvas;	//stored as a pointer
		
		public LeaderboardCalculator(BufferedImage canvas) {
			this.leaderboard = new ArrayList<>();
			this.canvas = canvas;
			this.start();
		}
		
		private void start() {
			this.thread = new Thread(this);
			this.thread.start();
		}
		
		public void run() {
			while(this.isRunning) {
				this.updateLeaderboard();
			}
		}
		
		public ArrayList<int[]> getLeaderboard(){
			return this.leaderboard;
		}
		
		private void updateLeaderboard() {
			HashMap<Integer, Integer> map = new HashMap<>();
			for(int i = 0; i < this.canvas.getWidth(); i++) {
				for(int j = 0; j < this.canvas.getHeight(); j++) {
					int rgb = this.canvas.getRGB(i, j);
					map.put(rgb, map.getOrDefault(rgb, 0) + 1);
				}
			}
			ArrayList<int[]> nextLeaderboard = new ArrayList<>();
			for(int i : map.keySet()) {
				nextLeaderboard.add(new int[] {i, map.get(i)});
			}
			nextLeaderboard.sort((a, b) -> -Integer.compare(a[1], b[1]));
			this.leaderboard = nextLeaderboard;
		}
		
		public void exit() {
			this.isRunning = false;
		}
	}
	
	class PacketSender {
		private ArrayList<Byte> packet;
		
		public PacketSender() {
			this.packet = new ArrayList<>();
		}
		
		public void flush(Socket socket) throws IOException {
			int packetSize = this.packet.size();
			byte[] packetArr = new byte[packetSize];
			for(int i = 0; i < packetSize; i++) {
				packetArr[i] = packet.get(i);
			}
			this.packet.clear();
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			dos.writeInt(packetSize);
			dos.write(packetArr);
			dos.flush();
		}
		
		public void writeInt(int a) {
			this.packet.add((byte) (0xFF & (a >> 24)));
			this.packet.add((byte) (0xFF & (a >> 16)));
			this.packet.add((byte) (0xFF & (a >> 8)));
			this.packet.add((byte) (0xFF & (a >> 0)));
		}
		
		public void write(int[] a) {
			for(int i : a) {
				this.writeInt(i);
			}
		}
	}
	
	class PacketListener implements Runnable {
		private boolean isRunning = true;
		private Thread thread;
		private String name;
		
		private Socket socket;	//socket on which to listen for packets
		private Queue<byte[]> packetQueue;
		private byte[] packet;
		private int readPtr;
		private boolean isConnected;
		
		private long lastPacketTime;
		private long timeoutMillis = 5000;
		
		public PacketListener(Socket socket, String name) {
			this.socket = socket;
			this.packetQueue = new ArrayDeque<>();
			this.name = name;
			this.isConnected = true;
			this.lastPacketTime = System.currentTimeMillis();
			
			this.start();
		}
		
		private void start() {
			this.thread = new Thread(this);
			this.thread.start();
		}
		
		public void run() {
			while(this.isRunning) {
				this.listenForPackets();
			}
		}
		
		public boolean nextPacket() {
			if(this.packetQueue.size() == 0) {
				return false;
			}
			this.packet = this.packetQueue.poll();
			this.readPtr = 0;
			return true;
		}
		
		public boolean isConnected() {
			long timeFromLastPacket = System.currentTimeMillis() - lastPacketTime;
			return timeFromLastPacket < timeoutMillis && isConnected;
		}
		
		public int readInt() throws ArrayIndexOutOfBoundsException {
			int ans = 0;
			for(int i = 0; i < 4; i++) {
				ans <<= 8;
				ans |= (int) this.readByte() & 0xFF;
			}
			return ans;
		}
		
		public byte readByte() throws ArrayIndexOutOfBoundsException {
			byte ans = this.packet[this.readPtr];
			this.readPtr ++;
			return ans;
		}
		
		public int[] readNInts(int n) throws ArrayIndexOutOfBoundsException {
			int[] ans = new int[n];
			for(int i = 0; i < n; i++) {
				ans[i] = this.readInt();
			}
			return ans;
		}
		
		private void listenForPackets() {
			try {
				DataInputStream dis = new DataInputStream(this.socket.getInputStream());
				int packetSize = dis.readInt();
				this.packetQueue.add(this.readNBytes(packetSize, dis));
				//System.out.println(this.name + " read packet of size " + packetSize);
			} catch(IOException e) {
				//probably closed connection
				e.printStackTrace();
				this.isConnected = false;
			}
			this.lastPacketTime = System.currentTimeMillis();
		}
		
		private byte[] readNBytes(int n, DataInputStream dis) throws IOException {
			byte[] packet = new byte[n];
			for(int i = 0; i < n; i++) {
				packet[i] = dis.readByte();
			}
			return packet;
		}
		
		public void exit() {
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
		
		public boolean hasNewClients() {
			return this.newClients.size() != 0;
		}
		
		public ArrayList<Socket> getNewClients(){
			ArrayList<Socket> out = new ArrayList<>();
			out.addAll(this.newClients);
			this.newClients = new ArrayList<>();
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
