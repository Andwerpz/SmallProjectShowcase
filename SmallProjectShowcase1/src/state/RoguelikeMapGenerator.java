package state;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Queue;
import java.util.Stack;
import java.util.StringTokenizer;

import input.Button;
import input.InputManager;
import main.MainPanel;
import util.Vector;

public class RoguelikeMapGenerator extends State {
	
	InputManager im;
	
	static ArrayList<Tile> tiles = new ArrayList<Tile>();
	
	ArrayList<ArrayList<Integer>> map;

	int mapSize = 500;
	int tileSize = 30;
	
	Vector offset = new Vector(-(mapSize * tileSize) / 2 + MainPanel.WIDTH / 2, -(mapSize * tileSize) / 2 + MainPanel.HEIGHT / 2);
	
	boolean drawGrid = false;
	
	java.awt.Point prevMouse = new java.awt.Point(0, 0);
	boolean pressedRight = false;
	boolean pressedLeft = false;
	
	public RoguelikeMapGenerator(StateManager gsm) {
		super(gsm);
		
		im = new InputManager();
		
		im.addInput(new Button(10, 10, 100, 25, "Tile Editor", "tileEditorBtn"));
		
		this.map = new ArrayList<ArrayList<Integer>>();
		
		if(tiles.size() == 0) {
			this.loadDefaultTiles();
		}
		
		this.generateMap();
		
		for(ArrayList<Integer> a : this.map) {
			for(Integer i : a) {
				System.out.print(i + " ");
			}
			System.out.println();
		}
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tick(Point mouse2) {
		im.tick(mouse2);
		
		int dx = mouse2.x - prevMouse.x;
		int dy = mouse2.y - prevMouse.y;

		if (pressedLeft) {
			offset.addVector(new Vector(dx, dy));
		}

		prevMouse = new java.awt.Point(mouse2.x, mouse2.y);

		if (pressedRight) {
			
		}
	}

	@Override
	public void draw(Graphics g) {
		
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
		
		im.draw(g);
	}
	
	public void generateMap() {
		this.map = new ArrayList<ArrayList<Integer>>();
		for(int i = 0; i < mapSize; i++) {
			this.map.add(new ArrayList<Integer>());
			for(int j = 0; j < mapSize; j++) {
				this.map.get(map.size() - 1).add(0);
			}
		}
		
		//pick random tile to start
		Tile startTile = tiles.get((int) (Math.random() * (double) tiles.size()));
		
		Queue<int[]> exits = new ArrayDeque<int[]>();
		
		int startX = mapSize / 2;
		int startY = mapSize / 2;
		
		for(int i = 0; i < startTile.height; i++) {
			for(int j = 0; j < startTile.width; j++) {
				int next = startTile.map.get(i).get(j);
				this.map.get(i + startY).set(j + startX, next);
			}
		}
		
		for(int[] e : startTile.exits) {
			exits.add(new int[] {e[0] + startX, e[1] + startY});
		}
		
		int roomCounter = 0;
		
		int[] dx = {-1, 1, 0, 0, 0};
		int[] dy = {0, 0, -1, 1, 0};
		
		while(exits.size() != 0) {
			
			int[] nextExit = exits.poll();
			
			int ox = nextExit[0];
			int oy = nextExit[1];
			
			//try every tile in random order, until you get one
			
			//TODO implement some sort of rare room system
			Collections.shuffle(RoguelikeMapGenerator.tiles);
			
			tileLoop:
			for(Tile t : tiles) {
				for(int[] e : t.exits) {
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
					boolean isValid = true;
					
					outer:
					for(int i = 0; i < t.height; i++) {
						for(int j = 0; j < t.width; j++) {
							int x = j + ox - ex;
							int y = i + oy - ey;
							
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
									
									if(map.get(ny).get(nx) == 2 && t.map.get(i + dy[k]).get(j + dx[k]) == 2) {
										continue;
									}
									
									else if(map.get(ny).get(nx) != 0) {
										isValid = false;
										break outer;
									}
								}
							}
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
							
							if(tileVal != 0 && tileVal != 2) {
								map.get(y).set(x, t.map.get(i).get(j));
							}	
						}
					}
					
					//set current exit
					map.get(oy).set(ox, 2);
					
					//add exits to stack
					for(int[] exit : t.exits) {
						exits.add(new int[] {exit[0] + minX, exit[1] + minY});
					}
					
					roomCounter ++;
					
					break tileLoop;
				}
			}
			
			if(roomCounter > 100) {
				break;
			}
		}
	}
	
	public void loadDefaultTiles() {
		String[] defaultTiles = {
				  "0 0 2 0 0 \r\n"
				+ "0 1 1 1 0 \r\n"
				+ "0 1 1 1 0 \r\n"
				+ "0 1 1 1 0 \r\n"
				+ "0 1 1 1 0 \r\n"
				+ "0 1 1 1 0 \r\n"
				+ "0 1 1 1 0 \r\n"
				+ "0 1 1 1 0 \r\n"
				+ "0 1 1 1 0 \r\n"
				+ "0 1 1 1 0 \r\n"
				+ "0 1 1 1 0 \r\n"
				+ "0 1 1 1 0 \r\n"
				+ "0 0 2 0 0 ",
				
				  "0 0 0 0 0 0 0 0 0 0 0 0 0 \r\n"
				+ "0 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
				+ "2 1 1 1 1 1 1 1 1 1 1 1 2 \r\n"
				+ "0 1 1 1 1 1 1 1 1 1 1 1 0 \r\n"
				+ "0 0 0 0 0 0 0 0 0 0 0 0 0 ",
				
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
				+ "0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 "
				
		};
		
		for(String s : defaultTiles) {
			StringTokenizer lines = new StringTokenizer(s, "\r\n");
			ArrayList<ArrayList<Integer>> nextTile = new ArrayList<ArrayList<Integer>>();
			while(lines.hasMoreTokens()) {
				StringTokenizer st = new StringTokenizer(lines.nextToken());
				nextTile.add(new ArrayList<Integer>());
				while(st.hasMoreTokens()) {
					nextTile.get(nextTile.size() - 1).add(Integer.parseInt(st.nextToken()));
				}
			}
			tiles.add(new Tile(nextTile));
			System.out.println(tiles.get(tiles.size() - 1));
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
		tiles.add(new Tile(tile));
		System.out.println(tiles.get(tiles.size() - 1));
	}

	public class Tile {
		
		//0 : air;
		//1 : wall / enclosed space
		//2 : exit / connector
		
		ArrayList<ArrayList<Integer>> map;
		ArrayList<int[]> exits;
		
		int width;
		int height;
		
		public Tile() {
			map = new ArrayList<ArrayList<Integer>>();
		}
		
		public Tile(ArrayList<ArrayList<Integer>> tile) {
			this.map = tile;
			this.locateExits();
			
			this.width = map.get(0).size();
			this.height = map.size();
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
	
}
