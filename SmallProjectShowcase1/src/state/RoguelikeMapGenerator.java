package state;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Queue;
import java.util.Stack;
import java.util.StringTokenizer;

import input.Button;
import input.InputManager;
import main.MainPanel;
import util.GraphicsTools;
import util.Vec2;

public class RoguelikeMapGenerator extends State {
	
	InputManager im;
	
	static ArrayList<Tile> roomTiles = new ArrayList<Tile>();	//fight rooms
	static ArrayList<Tile> startTiles = new ArrayList<Tile>();	//spawn tiles
	static ArrayList<Tile> hallwayTiles = new ArrayList<Tile>();	//only placed if a fight room can't be placed
	static ArrayList<Tile> lootTiles = new ArrayList<Tile>();	//chests, shops, ect
	
	ArrayList<ArrayList<Integer>> map;
	BufferedImage mapTexture;
	BufferedImage wallTexture;

	int mapSize = 250;
	int tileSize = 32;
	
	Vec2 offset = new Vec2(-(mapSize * tileSize) / 2 + MainPanel.WIDTH / 2, -(mapSize * tileSize) / 2 + MainPanel.HEIGHT / 2);
	
	boolean drawGrid = false;
	
	java.awt.Point prevMouse = new java.awt.Point(0, 0);
	boolean pressedRight = false;
	boolean pressedLeft = false;
	
	static ArrayList<BufferedImage[]> walls;
	static ArrayList<BufferedImage> tileSpritesheet;
	
	//4 0 5
	//2 # 3
	//6 1 7
	
	int[] dx = {-1, 1, 0, 0, -1, -1, 1, 1};
	int[] dy = {0, 0, -1, 1, -1, 1, -1, 1};
	
	//Path Tiles / Wall Tile decoder:
	
	//for paths, 1 means same as path texture, 0 means other texture
	//for walls, 1 means lower elevation, zero means higher or equal elevation
	
	//1: 
	//1 1
	//1 1
	
	//2:   3:   4:   5:
	//1 0  0 1  0 0  0 0
	//0 0  0 0  0 1  1 0
	
	//6:   7:   8:   9:
	//1 1  1 1  0 1  1 0
	//1 0  0 1  1 1  1 1
	
	//10:  11:  12:  13:
	//1 1  0 1  0 0  1 0
	//0 0  0 1  1 1  1 0
	
	//14:  15:
	//1 0  0 1
	//0 1  1 0
	
	//walls only: 
	//16: base vertical wall
	//17: tileable vertical wall
	
	static ArrayList<Integer> oo = new ArrayList<Integer>(Arrays.asList(1, 1));
	static ArrayList<Integer> oz = new ArrayList<Integer>(Arrays.asList(1, 0));
	static ArrayList<Integer> zo = new ArrayList<Integer>(Arrays.asList(0, 1));
	static ArrayList<Integer> zz = new ArrayList<Integer>(Arrays.asList(0, 0));
	
	static HashMap<ArrayList<ArrayList<Integer>>, Integer> keyToPath = new HashMap<ArrayList<ArrayList<Integer>>, Integer>() {{
		put(new ArrayList<ArrayList<Integer>>(Arrays.asList(oo, oo)), 0);
		put(new ArrayList<ArrayList<Integer>>(Arrays.asList(oz, zz)), 1);
		put(new ArrayList<ArrayList<Integer>>(Arrays.asList(zo, zz)), 2);
		put(new ArrayList<ArrayList<Integer>>(Arrays.asList(zz, zo)), 3);
		put(new ArrayList<ArrayList<Integer>>(Arrays.asList(zz, oz)), 4);
		put(new ArrayList<ArrayList<Integer>>(Arrays.asList(oo, oz)), 5);
		put(new ArrayList<ArrayList<Integer>>(Arrays.asList(oo, zo)), 6);
		put(new ArrayList<ArrayList<Integer>>(Arrays.asList(zo, oo)), 7);
		put(new ArrayList<ArrayList<Integer>>(Arrays.asList(oz, oo)), 8);
		put(new ArrayList<ArrayList<Integer>>(Arrays.asList(oo, zz)), 9);
		put(new ArrayList<ArrayList<Integer>>(Arrays.asList(zo, zo)), 10);
		put(new ArrayList<ArrayList<Integer>>(Arrays.asList(zz, oo)), 11);
		put(new ArrayList<ArrayList<Integer>>(Arrays.asList(oz, oz)), 12);
		put(new ArrayList<ArrayList<Integer>>(Arrays.asList(oz, zo)), 13);
		put(new ArrayList<ArrayList<Integer>>(Arrays.asList(zo, oz)), 14);
	}};
	
	static String[] defaultStartTiles = new String[] {
		  "0 0 0 0 0 0 0 0 0 2 0 0 0 0 0 0 0 0 0 \r\n"
		+ "0 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 0 \r\n"
		+ "0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 \r\n"
		+ "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 \r\n"
		+ "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 \r\n"
		+ "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 \r\n"
		+ "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 \r\n"
		+ "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 \r\n"
		+ "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 \r\n"
		+ "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 \r\n"
		+ "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 \r\n"
		+ "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 \r\n"
		+ "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 \r\n"
		+ "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 \r\n"
		+ "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 \r\n"
		+ "0 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 0 ",
		"/start1_texture.png"
	};
	
	String[] defaultRoomTiles = {
		  "0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 2 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 \r\n"
		+ "0 0 0 0 0 0 0 0 0 0 0 0 0 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 \r\n"
		+ "0 0 0 0 0 0 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 \r\n"
		+ "0 0 0 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 0 0 0 \r\n"
		+ "0 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 0 \r\n"
		+ "0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "2 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 2 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 \r\n"
		+ "0 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 0 \r\n"
		+ "0 0 0 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 0 0 0 \r\n"
		+ "0 0 0 0 0 0 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 \r\n"
		+ "0 0 0 0 0 0 0 0 0 0 0 0 0 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 \r\n"
		+ "0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 2 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 ",
		"/room4_texture.png",
		
		  "0 1 1 1 0 0 0 0 0 0 0 0 0 0 0 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 1 0 0 0 0 0 1 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "2 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 2 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 \r\n"
		+ "0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 \r\n"
		+ "0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 \r\n"
		+ "0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 \r\n"
		+ "0 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 0 \r\n"
		+ "0 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 0 \r\n"
		+ "0 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 0 \r\n"
		+ "0 0 0 0 1 1 1 1 1 1 1 1 1 1 1 0 0 0 0 \r\n"
		+ "0 0 0 0 1 1 1 1 1 1 1 1 1 1 1 0 0 0 0 \r\n"
		+ "0 0 0 0 0 1 1 1 1 1 1 1 1 1 0 0 0 0 0 \r\n"
		+ "0 0 0 0 0 0 1 1 1 1 1 1 1 0 0 0 0 0 0 \r\n"
		+ "0 0 0 0 0 0 0 1 1 1 1 1 0 0 0 0 0 0 0 \r\n"
		+ "0 0 0 0 0 0 0 0 1 1 1 0 0 0 0 0 0 0 0 \r\n"
		+ "0 0 0 0 0 0 0 0 0 2 0 0 0 0 0 0 0 0 0 ",
		"/room2_texture.png",
		
		  "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 2 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 \r\n"
		+ "2 1 1 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 2 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 ",
		"/room5_texture.png",
		
		  "0 1 1 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "2 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 2 \r\n"
		+ "0 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 0 0 0 0 0 0 0 0 0 0 0 0 0 2 0 0 0 0 0 0 0 0 0 0 0 0 ",
		"/room1_texture.png",
		
		  "0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 2 0 0 0 0 0 0 0 0 0 0 \r\n"
		+ "0 0 0 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 0 0 0 \r\n"
		+ "0 0 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 0 0 \r\n"
		+ "0 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 0 \r\n"
		+ "0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 0 0 1 1 1 1 0 0 0 0 0 0 0 0 1 1 1 1 0 0 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 0 0 1 1 1 1 0 0 0 0 0 0 0 0 1 1 1 1 0 0 1 1 1 1 1 2 \r\n"
		+ "0 1 1 1 1 1 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 1 1 1 1 1 0 \r\n"
		+ "2 1 1 1 1 1 0 0 1 1 1 1 0 0 0 0 0 0 0 0 1 1 1 1 0 0 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 0 0 1 1 1 1 0 0 0 0 0 0 0 0 1 1 1 1 0 0 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 1 1 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
		+ "0 0 1 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 1 1 1 1 1 1 1 1 1 1 0 0 \r\n"
		+ "0 0 0 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 1 1 1 1 1 1 1 1 1 0 0 0 \r\n"
		+ "0 0 0 0 1 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 1 1 1 1 1 1 1 1 0 0 0 0 \r\n"
		+ "0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 2 0 0 0 0 0 0 0 ",
		"/room3_texture.png"
		
	};
	
	public RoguelikeMapGenerator(StateManager gsm) {
		super(gsm);
		
		loadWallTextures();
		tileSpritesheet = GraphicsTools.loadAnimation("/tile floor.png", 16, 16);
		
		im = new InputManager();
		
		im.addInput(new Button(10, 10, 100, 25, "Tile Editor", "tileEditorBtn"));
		im.addInput(new Button(10, 45, 100, 25, "Tile Texture Editor", "tileTextureEditorBtn"));
		
		this.map = new ArrayList<ArrayList<Integer>>();
		
		if(roomTiles.size() == 0) {
			this.loadDefaultTiles(defaultRoomTiles, roomTiles);
		}
		
		if(startTiles.size() == 0) {
			this.loadDefaultTiles(defaultStartTiles, startTiles);
		}
		
		this.generateMap();
		
//		for(ArrayList<Integer> a : this.map) {
//			for(Integer i : a) {
//				System.out.print(i + " ");
//			}
//			System.out.println();
//		}
		
		
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}
	
	public static void loadWallTextures() {
		
		walls = new ArrayList<BufferedImage[]>();
		
		String[] paths = {"/stone wall 9.png"};
		
		for(String s : paths) {
			ArrayList<BufferedImage> animation = GraphicsTools.loadAnimation(s, 16, 16);
			
			BufferedImage[] next = new BufferedImage[17];
			
			for(int i = 0; i < 17; i++) {
				next[i] = animation.get(i);
			}
			
			walls.add(next);
		}
		
	}

	@Override
	public void tick(Point mouse2) {
		im.tick(mouse2);
		
		int dx = mouse2.x - prevMouse.x;
		int dy = mouse2.y - prevMouse.y;

		if (pressedLeft) {
			offset.addi(new Vec2(dx, dy));
		}

		prevMouse = new java.awt.Point(mouse2.x, mouse2.y);

		if (pressedRight) {
			
		}
	}

	@Override
	public void draw(Graphics g) {
		
		//drawing tiles
		/*
		for (int i = 0; i < map.size(); i++) {
			for (int j = 0; j < map.get(i).size(); j++) {
				int x = (int) offset.x + j * tileSize;
				int y = (int) offset.y + i * tileSize;

				if (map.get(i).get(j) == 1) {
					g.setColor(Color.BLACK);
					g.fillRect(x, y, tileSize, tileSize);
				} else if (map.get(i).get(j) == 2) {
					g.setColor(Color.GREEN);
					g.fillRect(x, y, tileSize, tileSize);
				}
				
				if (drawGrid) {
					g.setColor(Color.BLACK);
					g.drawRect(x, y, tileSize, tileSize);
				}

			}
		}
		*/
		
		//background color
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, MainPanel.WIDTH, MainPanel.HEIGHT);
		
		g.drawImage(mapTexture, (int) offset.x, (int) offset.y, tileSize * mapSize, tileSize * mapSize, null);
		g.drawImage(wallTexture, (int) offset.x, (int) offset.y, tileSize * mapSize, tileSize * mapSize, null);
		
		im.draw(g);
	}
	
	int minConnectorLength = 5;
	int maxConnectorLength = 10;
	
	ArrayList<ArrayList<Integer>> tileOccupied;
	
	public void generateMap() {
		this.mapTexture = new BufferedImage(mapSize * tileSize, mapSize * tileSize, BufferedImage.TYPE_INT_ARGB);
		this.map = new ArrayList<ArrayList<Integer>>();
		this.tileOccupied = new ArrayList<ArrayList<Integer>>();
		for(int i = 0; i < mapSize; i++) {
			this.map.add(new ArrayList<Integer>());
			this.tileOccupied.add(new ArrayList<Integer>());
			for(int j = 0; j < mapSize; j++) {
				this.map.get(map.size() - 1).add(0);
				this.tileOccupied.get(tileOccupied.size() - 1).add(0);
			}
		}
		
		//pick random tile to start
		//Tile startTile = roomTiles.get((int) (Math.random() * (double) roomTiles.size()));
		Tile startTile = startTiles.get((int) (Math.random() * (double) startTiles.size()));
		
		Queue<int[]> exits = new ArrayDeque<int[]>();
		Queue<Vec2> exitDir = new ArrayDeque<Vec2>();
		
		int startX = mapSize / 2;
		int startY = mapSize / 2;
		
		for(int i = 0; i < startTile.height; i++) {
			for(int j = 0; j < startTile.width; j++) {
				int next = startTile.map.get(i).get(j);
				this.map.get(i + startY).set(j + startX, next);
			}
		}
		
		for(int i = -2; i < startTile.height + 2; i++) {
			for(int j = -2; j < startTile.width + 2; j++) {
				int x = j + startX;
				int y = i + startY;
				if(x < 0 || x >= this.mapSize || y < 0 || y >= this.mapSize) {
					continue;
				}
				this.tileOccupied.get(i + startY).set(j + startX, 1);
			}
		}
		
		for(int[] e : startTile.exits) {
			exits.add(new int[] {e[0] + startX, e[1] + startY});
			
			Vec2 eDir = new Vec2(0, 0);
			for(int i = 0; i < 4; i++) {
				int x = e[0] + dx[i];
				int y = e[1] + dy[i];
				
				if(
						x < 0 || x >= startTile.map.get(0).size() || 
						y < 0 || y >= startTile.map.size()) {
					eDir = new Vec2(dx[i], dy[i]);
					break;
				}
				else if(startTile.map.get(e[1] + dy[i]).get(e[0] + dx[i]) == 1) {
					eDir = new Vec2(-dx[i], -dy[i]);
					break;
				}
			}
			//System.out.println(eDir);
			exitDir.add(eDir);
		}
		
		Graphics gMap = this.mapTexture.getGraphics();
		gMap.drawImage(startTile.texture, startX * tileSize, startY * tileSize, null);
		
		int roomCounter = 0;
		
		while(exits.size() != 0) {
			if(roomCounter > 10000) {
				break;
			}
			
			//System.out.println(exitDir);
			
			if(this.addTileToMap(roomTiles, exits, exitDir) != -1) {
				roomCounter ++;
			}
		}
		
		this.processWallTextures();
	}
	
	public int addTileToMap(ArrayList<Tile> tiles, Queue<int[]> exits, Queue<Vec2> exitDir) {
		if(exits.size() == 0) {
			return -1;
		}
		
		int[] dx = {-1, 1, 0, 0, 0};
		int[] dy = {0, 0, -1, 1, 0};
		
		int[] nextExit = exits.poll();
		Vec2 nextExitDir = exitDir.poll();
		
		//try every tile in random order, until you get one
		
		//TODO implement some sort of rare room system
		Collections.shuffle(tiles);
		
		for(Tile t : tiles) {
			for(int[] e : t.exits) {
				
				//determine if both exits are facing the same direction
				if(
						e[0] + nextExitDir.x < 0 || e[0] + nextExitDir.x >= t.map.get(0).size() ||
						e[1] + nextExitDir.y < 0 || e[1] + nextExitDir.y >= t.map.size() ||
						t.map.get(e[1] + (int) nextExitDir.y).get(e[0] + (int) nextExitDir.x) == 0) {
					continue;
				}
				
				//the exits are compatible
				//place the tile, and move it back if placement is invalid
				
				for(int con = minConnectorLength; con <= maxConnectorLength; con++) {
				
					int ox = nextExit[0] + (int) nextExitDir.x * con;	//origin x
					int oy = nextExit[1] + (int) nextExitDir.y * con;
					
					int ex = e[0];
					int ey = e[1];
					
					int minX = ox - ex;
					int maxX = ox - ex + t.width;
					int minY = oy - ey;
					int maxY = oy - ey + t.height;
					
					if(minX < 0 || minY < 0 || maxX >= mapSize || maxY >= mapSize) {	//tile out of bounds
						continue;
					}
					
					//check whether exit placement is valid
					//just check against bounding boxes
					boolean isValid = true;
					
					outer:
					for(int i = 0; i < t.height; i++) {
						for(int j = 0; j < t.width; j++) {
							int x = j + ox - ex;
							int y = i + oy - ey;
							
							for(int k = 0; k < this.dx.length; k++) {
								
								if(x + this.dx[k] < 0 || y + this.dy[k] < 0 || x + this.dx[k] >= this.mapSize || y + this.dy[k] >= this.mapSize) {
									continue;
								}
								
								if(this.tileOccupied.get(y + this.dy[k]).get(x + this.dx[k]) == 1) {
									isValid = false;
									break outer;
								}
							}
							
							/*
							int mapVal = map.get(y).get(x);
							int tileVal = t.map.get(i).get(j);
							
							
							if(tileVal == 2) {
								if(mapVal != 2 && mapVal != 0) {
									isValid = false;
									break outer;
								}
							}
							else if(tileVal == 0) {
								//do nothing
							}
							else {
								//we know the tile is a floor tile
								//check this tile, and all adjacent tiles
								
								for(int k = 0; k < dx.length; k++) {
									int nx = x + dx[k];
									int ny = y + dy[k];
									
									if(ny < 0 || nx < 0 || ny >= map.size() || nx >= map.get(0).size() ||
											i + dy[k] < 0 || j + dx[k] < 0 || i + dy[k] >= t.map.get(0).size() || j + dx[k] >= t.map.get(0).size()) {	//out of bounds
										
										continue;
									}
									
									if(map.get(ny).get(nx) == 2 && t.map.get(i + dy[k]).get(j + dx[k]) == 2) {
										continue;
									}
									
									else if(map.get(ny).get(nx) != 0) {
										isValid = false;
										break outer;
									}
								}
							}
							*/
						}
					}
				
					if(!isValid) {
						continue;
					}
					
					for(int i = 0; i < t.height; i++) {
						for(int j = 0; j < t.width; j++) {
							int x = j + ox - ex;
							int y = i + oy - ey;
							
							int mapVal = map.get(y).get(x);
							int tileVal = t.map.get(i).get(j);
							
							if(t.map.get(i).get(j) != 2) {
								map.get(y).set(x, t.map.get(i).get(j));
							}
							
							//this.tileOccupied.get(y).set(x, 1);
						}
					}
					
					for(int i = -2; i < t.height + 2; i++) {
						for(int j = -2; j < t.width + 2; j++) {
							int x = j + ox - ex;
							int y = i + oy - ey;
							if(x < 0 || x >= this.mapSize || y < 0 || y >= this.mapSize) {
								continue;
							}
							this.tileOccupied.get(y).set(x, 1);
						}
					}
					
					//draw tile texture onto map
					Graphics gMap = this.mapTexture.getGraphics();
					gMap.drawImage(t.texture, minX * tileSize, minY * tileSize, null);
					
					//set current exit
					map.get(oy).set(ox, 2);
					map.get(nextExit[1]).set(nextExit[0], 2);
					
					//set pathway between the two
					int px = nextExit[0] + (int) nextExitDir.x;
					int py = nextExit[1] + (int) nextExitDir.y;
					
					//System.out.println("[" + ox + ", " + oy + "], [" + px + ", " + py + "]");
					
					while(px != ox || py != oy) {
						map.get(py).set(px, 1);
						
						gMap.drawImage(tileSpritesheet.get(1), px * this.tileSize, py * this.tileSize, this.tileSize, this.tileSize, null);
						
						px += (int) nextExitDir.x;
						py += (int) nextExitDir.y;
					}
					
					gMap.drawImage(tileSpritesheet.get(1), ox * this.tileSize, oy * this.tileSize, this.tileSize, this.tileSize, null);
					gMap.drawImage(tileSpritesheet.get(1), nextExit[0] * this.tileSize, nextExit[1] * this.tileSize, this.tileSize, this.tileSize, null);
					
					//add exits to stack
					for(int[] exit : t.exits) {
						exits.add(new int[] {exit[0] + minX, exit[1] + minY});
						
						Vec2 eDir = new Vec2(0, 0);
						for(int i = 0; i < 4; i++) {
							int x = exit[0] + dx[i];
							int y = exit[1] + dy[i];
							
							if(
									x < 0 || x >= t.map.get(0).size() || 
									y < 0 || y >= t.map.size()) {
								eDir = new Vec2(dx[i], dy[i]);
								break;
							}
							else if(t.map.get(exit[1] + dy[i]).get(exit[0] + dx[i]) == 1) {
								eDir = new Vec2(-dx[i], -dy[i]);
								break;
							}
						}
						//System.out.println(eDir);
						exitDir.add(eDir);
					}
					
					
					
					//System.out.println(nextExitDir);
					
					return 1;
				}
			}

		}
		return -1;
	}
	
	//includes out of tile textures - now it's just a black background
	public void processWallTextures() {
		
		//change map to suit needs
		for(int i = this.mapSize - 1; i > 0; i--) {
			for(int j = 0; j < this.mapSize; j++) {
				if(this.map.get(i - 1).get(j) != 0) {
					this.map.get(i).set(j, 1);
				}
			}
		}
		
		this.wallTexture = new BufferedImage(mapSize * tileSize, mapSize * tileSize, BufferedImage.TYPE_INT_ARGB);
		Graphics gImg = this.wallTexture.getGraphics();
		
		BufferedImage[] wallTex = walls.get(0); 	//stone wall
		
		//first draw vertical textures
		for(int i = 0; i < this.mapSize - 1; i++) {
			for(int j = 0; j < this.mapSize; j++) {
				
				if(this.map.get(i).get(j) != 0) {
					continue;
				}
				
				boolean foundLower = false;
				
				for(int k = 0; k < dx.length; k++) {
					int x = j + dx[k];
					int y = i + dy[k];
					
					if(x >= 0 && y >= 0 && y < this.mapSize && x < this.mapSize && this.map.get(y).get(x) == 1) {
						foundLower = true;
						break;
					}
				}
				
				int x = j * this.tileSize;
				int y = (i - 0) * this.tileSize;
				
				if(foundLower) {
					//gImg.drawImage(wallTex[16], x, y, tileSize, tileSize, null);
				}
				
				if(this.map.get(i).get(j) == 0 && this.map.get(i + 1).get(j) != 0) {
					x = j * this.tileSize;
					y = (i - 0) * this.tileSize;
					
					gImg.drawImage(wallTex[15], x, y, tileSize, tileSize, null);
					
				}
			}
		}
		
		//draw floor textures now
		for(int i = 0; i < this.mapSize; i++) {
			for(int j = 0; j < this.mapSize; j++) {
				if(this.map.get(i).get(j) == 0) {
					
					if((i - 1) < 0) {
						continue;
					}
					
					BufferedImage img = this.setWallTile(wallTex, i, j);
					
					int x = j * this.tileSize;
					int y = (i - 1) * this.tileSize;
					
					
					
					gImg.drawImage(img, x, y, tileSize, tileSize, null);
				}
			}
		}
		
		//change back map
		for(int i = this.mapSize - 1; i > 0; i--) {
			for(int j = 0; j < this.mapSize; j++) {
				if(i + 1 == this.mapSize) {
					if(this.map.get(i).get(j) == 1) {
						this.map.get(i).set(j, 0);
					}
				}
				else {
					if(this.map.get(i + 1).get(j) == 0) {
						this.map.get(i).set(j, 0);
					}
				}
			}
		}
	}
	
	public BufferedImage setWallTile(BufferedImage[] wallTex, int row, int col) {
		
		boolean[] isLower = new boolean[8];
		
		for(int i = 0; i < dx.length; i++) {
			int x = row + dx[i];
			int y = col + dy[i];
			
			if(x < 0 || y < 0 || x >= map.size() || y >= map.get(0).size()) {
				continue;
			}
			
			if(map.get(x).get(y) != 0) {
				isLower[i] = true;
			}
		}
		
		//have an array of 2x2 associated with each path tile texture. 
		//use the booleans to set the key for this specific path tile
		
		//4 0 5
		//2 # 3
		//6 1 7
		
		ArrayList<ArrayList<Integer>> key = new ArrayList<ArrayList<Integer>>();
		
		key.add(new ArrayList<Integer>(Arrays.asList(0, 0)));
		key.add(new ArrayList<Integer>(Arrays.asList(0, 0)));
		
		if(isLower[2] || isLower[4] || isLower[0]) {
			key.get(0).set(0, 1);
		}
		if(isLower[0] || isLower[5] || isLower[3]) {
			key.get(0).set(1, 1);
		}
		if(isLower[2] || isLower[6] || isLower[1]) {
			key.get(1).set(0, 1);
		}
		if(isLower[1] || isLower[7] || isLower[3]) {
			key.get(1).set(1, 1);
		}
		
		if(this.keyToPath.containsKey(key)) {
			return wallTex[this.keyToPath.get(key)];
		}
		
		//return tileSpritesheet.get(17);	//green grass
		BufferedImage blackground = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		return blackground;
		
	}
	
	public void loadDefaultTiles(String[] source, ArrayList<Tile> destination) {
		for(int i = 0; i < source.length; i += 2) {
			String s = source[i];
			String path = source[i + 1];
			StringTokenizer lines = new StringTokenizer(s, "\r\n");
			ArrayList<ArrayList<Integer>> nextTile = new ArrayList<ArrayList<Integer>>();
			while(lines.hasMoreTokens()) {
				StringTokenizer st = new StringTokenizer(lines.nextToken());
				nextTile.add(new ArrayList<Integer>());
				while(st.hasMoreTokens()) {
					nextTile.get(nextTile.size() - 1).add(Integer.parseInt(st.nextToken()));
				}
			}
			
			BufferedImage img = GraphicsTools.loadImage(path);
			BufferedImage copy = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
			
			for(int j = 0; j < img.getHeight(); j++) {
				for(int k = 0; k < img.getWidth(); k++) {
					int RGB = img.getRGB(k, j);
					
					if(RGB == (255 << 24)) {
						RGB = 0;
					}
					
					//System.out.println(Integer.toBinaryString(RGB));
					
					copy.setRGB(k, j, RGB);
				}
			}
			
			destination.add(new Tile(nextTile, copy));
			System.out.println(destination.get(destination.size() - 1));
		}
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		im.keyPressed(arg0);
		
		if(arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
			this.exit();
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		im.keyReleased(arg0);
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		String which = im.mouseClicked(arg0);
		
		switch(which) {
		case "tileEditorBtn":
			this.gsm.states.add(new TileEditor(this.gsm));
			break;
			 
		case "tileTextureEditorBtn":
			TileTextureEditor state = new TileTextureEditor(this.gsm);
			this.gsm.states.add(state);
			state.setTile(0);
			break;
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
		im.mousePressed(arg0);
		if (arg0.getButton() == MouseEvent.BUTTON1) {
			pressedLeft = true;
		}
		if (arg0.getButton() == MouseEvent.BUTTON3) {
			pressedRight = true;
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		im.mouseReleased(arg0);
		if (arg0.getButton() == MouseEvent.BUTTON1) {
			pressedLeft = false;
		}
		if (arg0.getButton() == MouseEvent.BUTTON3) {
			pressedRight = false;
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		if (this.tileSize == 1 && arg0.getUnitsToScroll() > 0) {
			return;
		}

		// need to correct for zoom by offsetting view
		double realMouseX = this.prevMouse.x - this.offset.x;
		double realMouseY = this.prevMouse.y - this.offset.y;

		// System.out.println(realMouseX + " " + realMouseY);

		double tileMouseX = realMouseX / (double) this.tileSize;
		double tileMouseY = realMouseY / (double) this.tileSize;

		int diff = arg0.getUnitsToScroll() < 0 ? 1 : -1;

		this.offset.x -= tileMouseX * diff;
		this.offset.y -= tileMouseY * diff;

		this.tileSize += diff;
	}
	
	public void addTile(ArrayList<ArrayList<Integer>> tile) {
		roomTiles.add(new Tile(tile, new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)));
		System.out.println(roomTiles.get(roomTiles.size() - 1));
	}

	
	
}

class Tile {
		
	//0 : air;
	//1 : wall / enclosed space
	//2 : exit / connector
	
	ArrayList<ArrayList<Integer>> map;
	ArrayList<int[]> exits;
	
	int width;
	int height;
	
	BufferedImage texture;
	
	int gridTileSize = 32;
	
	public Tile() {
		map = new ArrayList<ArrayList<Integer>>();
	}
	
	public Tile(ArrayList<ArrayList<Integer>> tile, BufferedImage img) {
		this.map = tile;
		this.locateExits();
		
		this.width = map.get(0).size();
		this.height = map.size();
		
		this.texture = img;
	}
	
	public void locateExits() {
		exits = new ArrayList<int[]>();
		for(int i = 0; i < this.map.size(); i++) {
			for(int j = 0; j < this.map.get(i).size(); j++) {
				if(this.map.get(i).get(j) == 2) {
					exits.add(new int[] {j, i});
				}
			}
		}
	}
	
	public void draw(Graphics g, double xOffset, double yOffset) {
		g.drawImage(this.texture, (int) xOffset, (int) yOffset, null);
	}
	
	public void drawGrid(Graphics g, double xOffset, double yOffset, boolean drawGrid) {
		for (int i = 0; i < map.size(); i++) {
			for (int j = 0; j < map.get(i).size(); j++) {
				int x = (int) xOffset + j * gridTileSize;
				int y = (int) yOffset + i * gridTileSize;

				

				if (map.get(i).get(j) == 1) {
					g.setColor(Color.BLACK);
					g.fillRect(x, y, gridTileSize, gridTileSize);
				} else if (map.get(i).get(j) == 2) {
					g.setColor(Color.GREEN);
					g.fillRect(x, y, gridTileSize, gridTileSize);
				}
				
				if (drawGrid) {
					g.setColor(Color.BLACK);
					g.drawRect(x, y, gridTileSize, gridTileSize);
				}

			}
		}
	}
	
	public String toString() {
		String out = "";
		for(ArrayList<Integer> a : map) {
			for(Integer b : a) {
				out += b + " ";
			}
			out += "\n";
		}
		return out;
	}
	
}
