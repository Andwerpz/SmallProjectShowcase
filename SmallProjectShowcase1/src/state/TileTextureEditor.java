package state;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.imageio.ImageIO;

import input.Button;
import input.InputManager;
import input.ToggleButton;
import util.GraphicsTools;
import util.Vector;

public class TileTextureEditor extends State {
	
	int selectedTile = 0;
	
	InputManager im = new InputManager();
	
	Tile tile;
	ArrayList<ArrayList<BufferedImage>> tileTextures;
	
	java.awt.Point prevMouse = new java.awt.Point(0, 0);
	boolean pressedRight = false;
	boolean pressedLeft = false;
	
	boolean drawTextures = true;
	boolean drawGrid = false;
	
	//when enabled, the user will select a path texture and it will autoplace the tiles surrounding the
	//tiles the user draws
	boolean pathMode = false;	
	int selectedPath = 0;
	static ArrayList<BufferedImage[]> paths = new ArrayList<BufferedImage[]>();
	
	static ArrayList<BufferedImage[]> floorTiles = new ArrayList<BufferedImage[]>();
	
	Vector offset = new Vector(0, 0);
	
	static ArrayList<BufferedImage> textures;
	int selectedTexture = 0;
	
	int defaultTexture = 0;
	
	//only loop through first 4 if you want to check adjacent
	
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

	public TileTextureEditor(StateManager gsm) {
		super(gsm);
		
		loadTextures();
		
		im.addInput(new Button(10, 10, 50, 25, "<< Prev", "switchTexturePrevBtn"));
		im.addInput(new Button(60, 10, 50, 25, " Next >>", "switchTextureNextBtn"));
		im.addInput(new ToggleButton(10, 45, 100, 25, "Draw Grid", "drawGridBtn"));
		im.addInput(new Button(10, 80, 100, 25, "Save Tile", "saveTileBtn"));
		im.addInput(new ToggleButton(10, 115, 100, 25, "Path Mode", "pathModeBtn"));
		im.addInput(new Button(10, 150, 50, 25, "<< Prev", "switchPathPrevBtn"));
		im.addInput(new Button(60, 150, 50, 25, " Next >>", "switchPathNextBtn"));
		im.addInput(new Button(10, 185, 100, 25, "Next Tile", "switchTileBtn"));
		im.addInput(new Button(10, 220, 100, 25, "Tile Floor", "tileFloorBtn"));
		
		this.setTile(0);
	}
	
	public void setTile(int which) {
		this.tile = RoguelikeMapGenerator.roomTiles.get(which);
		
		tileTextures = new ArrayList<ArrayList<BufferedImage>>();
		
		for(int i = 0; i < this.tile.map.size(); i++) {
			tileTextures.add(new ArrayList<BufferedImage>());
			for(int j = 0; j < this.tile.map.get(0).size(); j++) {
				
				int tileVal = tile.map.get(i).get(j);
				
//				if(tileVal == 1) {
//					tileTextures.get(i).add(textures.get(defaultTexture));
//				}
//				else {
//					tileTextures.get(i).add(new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB));
//				}
				
				tileTextures.get(i).add(new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB));
				
			}
		}	
	}
	
	public static void loadTextures() {
		
		textures = new ArrayList<BufferedImage>();
		
		//textures.addAll(GraphicsTools.loadAnimation("/grass spritesheet 4.png", 16, 16));
		textures.addAll(GraphicsTools.loadAnimation("/tile floor.png", 16, 16));
		
		//loadPaths();
		loadFloorTiles();
	}
	
	public static void loadPaths() {
		
		paths = new ArrayList<BufferedImage[]>();
		
		String[] pathPaths = {
				"/dirt path.png"
		};
		
		for(String s : pathPaths) {
			ArrayList<BufferedImage> imgs = GraphicsTools.loadAnimation(s, 16, 16);
			
			BufferedImage[] nextPath = new BufferedImage[15];
			for(int i = 0; i < imgs.size(); i++) {
				nextPath[i] = imgs.get(i);
			}
			
			paths.add(nextPath);
		}
	}
	
	public static void loadFloorTiles() {
		floorTiles = new ArrayList<BufferedImage[]>();
		
		String[] floorPaths = {
				"/tile floor.png"
		};
		
		for(String s : floorPaths) {
			ArrayList<BufferedImage> imgs = GraphicsTools.loadAnimation(s, 16, 16);
			
			BufferedImage[] nextPath = new BufferedImage[6];
			for(int i = 0; i < imgs.size(); i++) {
				nextPath[i] = imgs.get(i);
			}
			
			floorTiles.add(nextPath);
		}
	}
	
	public void tileFloor() {
		
		boolean[][] v = new boolean[this.tile.height][this.tile.width];
		
		for(int i = 0; i < this.tile.height; i++) {
			for(int j = 0; j < this.tile.width; j++) {
				if(this.tile.map.get(i).get(j) == 1 && !v[i][j]) {
					boolean canPlaceLarge = true;
					if(i + 1 == this.tile.height || j + 1 == this.tile.width) {
						canPlaceLarge = false;
					}
					else if(
							this.tile.map.get(i + 1).get(j) != 1 ||
							this.tile.map.get(i + 1).get(j + 1) != 1 ||
							this.tile.map.get(i).get(j + 1) != 1 ||
							v[i + 1][j] ||
							v[i + 1][j + 1] ||
							v[i][j + 1]){
						canPlaceLarge = false;
					}
					
					if(canPlaceLarge && Math.random() > 0.95) {	//large 2x2 tile
						v[i + 1][j] = true;
						v[i + 1][j + 1] = true;
						v[i][j + 1] = true;
						v[i][j] = true;
						this.tileTextures.get(i).set(j, floorTiles.get(0)[2]);
						this.tileTextures.get(i).set(j + 1, floorTiles.get(0)[3]);
						this.tileTextures.get(i + 1).set(j + 1, floorTiles.get(0)[4]);
						this.tileTextures.get(i + 1).set(j, floorTiles.get(0)[5]);
					}
					else if(Math.random() > 0.8) {	//small 1/2 tiles
						v[i][j] = true;
						this.tileTextures.get(i).set(j, floorTiles.get(0)[0]);
					}
					else {	//regular tile
						v[i][j] = true;
						this.tileTextures.get(i).set(j, floorTiles.get(0)[1]);
					}
				}
			}
		}
	}
	
	public void saveTile() {
		BufferedImage saveImg = new BufferedImage(this.tile.width * this.tile.gridTileSize, this.tile.height * this.tile.gridTileSize, BufferedImage.TYPE_INT_ARGB);
		
		Graphics gImg = saveImg.getGraphics();
		
		tile.draw(gImg, 0, 0);
		
		for(int i = 0; i < tileTextures.size(); i++) {
			for(int j = 0; j < tileTextures.get(0).size(); j++) {
				int x = j * tile.gridTileSize;
				int y = i * tile.gridTileSize;
				
				gImg.drawImage(tileTextures.get(i).get(j), x, y, 32, 32, null);
			}
		}
		
		GraphicsTools.saveBufferedImageToFile(saveImg);
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tick(Point mouse2) {
		im.tick(mouse2);
		
		this.drawGrid = im.getToggled("drawGridBtn");
		this.pathMode = im.getToggled("pathModeBtn");

		int dx = mouse2.x - prevMouse.x;
		int dy = mouse2.y - prevMouse.y;

		if (pressedLeft) {
			offset.add(new Vector(dx, dy));
		}	

		prevMouse = new java.awt.Point(mouse2.x, mouse2.y);

		if (pressedRight) {
			double realMouseX = this.prevMouse.x - this.offset.x;
			double realMouseY = this.prevMouse.y - this.offset.y;

			int tileMouseX = (int) (realMouseX / (double) this.tile.gridTileSize);
			int tileMouseY = (int) (realMouseY / (double) this.tile.gridTileSize);

			if (tileMouseY >= 0 && tileMouseY < this.tile.map.size() && tileMouseX >= 0
					&& tileMouseX < this.tile.map.get(0).size()) {	//inside grid bounds
				int val = tile.map.get((int) tileMouseY).get((int) tileMouseX);
				
				if(val == 1) {	//is a floor tile
					
					BufferedImage selectedTex = textures.get(selectedTexture);
					if(pathMode) {
						selectedTex = paths.get(selectedPath)[0];
					}
					
					tileTextures.get((int) tileMouseY).set((int) tileMouseX, selectedTex);
					
					if(pathMode) {
						
						for(int i = 0; i < this.tileTextures.size(); i++) {
							for(int j = 0; j < this.tileTextures.get(0).size(); j++) {
								if(this.tileTextures.get(i).get(j) == selectedTex) {
									for(int k = 0; k < this.dx.length; k++) {
										int newI = i + this.dx[k];
										int newJ = j + this.dy[k];
										
										System.out.println(this.dx[k] + " " + this.dy[k]);
										
										this.setPathTile(selectedTex, newI, newJ);
									}
								}
							}
						}
					}
					
				}
				
			}
		}
	}
	
	public void setPathTile(BufferedImage pathTex, int row, int col) {
		
		if(
				row < 0 || 
				col < 0 || 
				row >= tileTextures.size() || 
				col >= tileTextures.get(0).size() || 
				tile.map.get(row).get(col) != 1 ||
				tileTextures.get(row).get(col) == pathTex) {
			return;
		}
		
		
		boolean[] isPathTile = new boolean[8];
		
		for(int i = 0; i < dx.length; i++) {
			int x = row + dx[i];
			int y = col + dy[i];
			
			if(x < 0 || y < 0 || x >= tileTextures.size() || y >= tileTextures.get(0).size() || tile.map.get(x).get(y) != 1) {
				continue;
			}
			
			if(tileTextures.get(x).get(y) == pathTex) {
				isPathTile[i] = true;
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
		
		if(isPathTile[2] || isPathTile[4] || isPathTile[0]) {
			key.get(0).set(0, 1);
		}
		if(isPathTile[0] || isPathTile[5] || isPathTile[3]) {
			key.get(0).set(1, 1);
		}
		if(isPathTile[2] || isPathTile[6] || isPathTile[1]) {
			key.get(1).set(0, 1);
		}
		if(isPathTile[1] || isPathTile[7] || isPathTile[3]) {
			key.get(1).set(1, 1);
		}
		
		tileTextures.get(row).set(col, TileTextureEditor.paths.get(this.selectedPath)[keyToPath.get(key)]);
	}

	@Override
	public void draw(Graphics g) {
		
		tile.drawGrid(g, this.offset.x, this.offset.y, true);
		
		if(drawTextures) {
			tile.draw(g, this.offset.x, this.offset.y);
			
			for (int i = 0; i < this.tile.map.size(); i++) {
				for (int j = 0; j < this.tile.map.get(i).size(); j++) {
					int x = (int) offset.x + j * this.tile.gridTileSize;
					int y = (int) offset.y + i * this.tile.gridTileSize;

					g.drawImage(this.tileTextures.get(i).get(j), x, y, this.tile.gridTileSize, this.tile.gridTileSize, null);
					
					if(this.drawGrid) {
						g.drawRect(x, y, tile.gridTileSize, tile.gridTileSize);
					}
				}
			}
		}
		
		
		
		im.draw(g);
		
		g.drawImage(textures.get(selectedTexture), 120, 10, 32, 32, null);
		g.drawRect(120, 10, 32, 32);
		
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		if(arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
			this.exit();
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		String which = im.mouseClicked(arg0);
		
		switch(which) {
		case "switchTextureNextBtn":
			selectedTexture ++;
			if(selectedTexture == textures.size()) {
				selectedTexture = 0;
			}
			break;
			
		case "switchTexturePrevBtn":
			selectedTexture --;
			if(selectedTexture == -1) {
				selectedTexture = textures.size() - 1;
			}
			break;
			
		case "saveTileBtn":
			this.saveTile();
			break;
			
		case "switchPathNextBtn":
			selectedPath ++;
			if(selectedPath == paths.size()) {
				selectedPath = 0;
			}
			break;
			
		case "switchPathPrevBtn":
			selectedPath --;
			if(selectedPath == -1) {
				selectedPath = paths.size() - 1;
			}
			break;
			
		case "switchTileBtn":
			selectedTile ++;
			if(selectedTile == RoguelikeMapGenerator.roomTiles.size()) {
				selectedTile = 0;
			}
			this.setTile(selectedTile);
			break;
			
		case "tileFloorBtn":
			this.tileFloor();
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
		
	}

}
