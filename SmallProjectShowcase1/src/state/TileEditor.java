package state;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;

import input.Button;
import input.InputManager;
import input.TextField;
import state.RoguelikeMapGenerator.Tile;
import util.TextBox;
import util.Vector;

public class TileEditor extends State {

	ArrayList<ArrayList<Integer>> tile;

	InputManager im;

	int tileSize = 30;
	int editorSize = 200;

	Vector offset = new Vector(0, 0);

	java.awt.Point prevMouse = new java.awt.Point(0, 0);
	boolean pressedRight = false;
	boolean pressedLeft = false;

	boolean drawGrid = true;

	int[] tileTypes = { 0, 1, 2 };
	int tileTypePointer = 1;

	public TileEditor(StateManager gsm) {
		super(gsm);

		tile = new ArrayList<ArrayList<Integer>>();

		for (int i = 0; i < editorSize; i++) {
			tile.add(new ArrayList<Integer>());
			for (int j = 0; j < editorSize; j++) {
				tile.get(i).add(0);
			}
		}

		im = new InputManager();

		im.addInput(new Button(10, 10, 100, 25, "Switch Tile", "switchTileBtn"));
		im.addInput(new Button(10, 45, 100, 25, "Save Tile", "saveTileBtn"));
		im.addInput(new Button(10, 80, 100, 25, "Rotate Editor", "rotateEditorBtn"));
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
			double realMouseX = this.prevMouse.x - this.offset.x;
			double realMouseY = this.prevMouse.y - this.offset.y;

			double tileMouseX = realMouseX / (double) this.tileSize;
			double tileMouseY = realMouseY / (double) this.tileSize;

			if (tileMouseY > 0 && tileMouseY < this.tile.size() && tileMouseX > 0
					&& tileMouseX < this.tile.get(0).size()) {
				this.tile.get((int) tileMouseY).set((int) tileMouseX, this.tileTypes[this.tileTypePointer]);
			}
		}
	}

	@Override
	public void draw(Graphics g) {
		for (int i = 0; i < tile.size(); i++) {
			for (int j = 0; j < tile.get(i).size(); j++) {
				int x = (int) offset.x + j * tileSize;
				int y = (int) offset.y + i * tileSize;

				

				if (tile.get(i).get(j) == 1) {
					g.setColor(Color.BLACK);
					g.fillRect(x, y, tileSize, tileSize);
				} else if (tile.get(i).get(j) == 2) {
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

	public void saveTile() {

		ArrayList<ArrayList<Integer>> saveTile = new ArrayList<ArrayList<Integer>>();

		int minX = tile.get(0).size();
		int maxX = 0;
		int minY = tile.size();
		int maxY = 0;

		boolean foundTile = false;

		for (int i = 0; i < tile.size(); i++) {
			for (int j = 0; j < tile.get(0).size(); j++) {
				int next = tile.get(i).get(j);
				if (next != 0) {
					foundTile = true;

					minX = Math.min(minX, j);
					maxX = Math.max(maxX, j);
					minY = Math.min(minY, i);
					maxY = Math.max(maxY, i);
				}
			}
		}

		if (!foundTile)
			return;

		for (int i = minY; i <= maxY; i++) {
			saveTile.add(new ArrayList<Integer>());
			for (int j = minX; j <= maxX; j++) {
				saveTile.get(i - minY).add(tile.get(i).get(j));
			}
		}

		RoguelikeMapGenerator r = new RoguelikeMapGenerator(this.gsm);
		r.addTile(saveTile);
	}
	
	//rotates the editor space 90 deg clockwise
	
	public void rotate() {
		
		ArrayList<ArrayList<Integer>> rotated = new ArrayList<ArrayList<Integer>>();
		
		for(int i = 0; i < this.editorSize; i++) {
			rotated.add(new ArrayList<Integer>());
			for(int j = 0; j < this.editorSize; j++) {
				rotated.get(i).add(0);
			}
		}
		
		for(int i = 0; i < this.tile.size(); i++) {
			for(int j = 0; j < this.tile.get(i).size(); j++) {
				rotated.get(j).set(editorSize - i - 1, this.tile.get(i).get(j));
			}
		}
		
		this.tile = rotated;
		
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
			this.exit();
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		String which = im.mouseClicked(arg0);

		switch (which) {
		case "switchTileBtn":
			this.tileTypePointer++;
			if (this.tileTypePointer >= this.tileTypes.length) {
				this.tileTypePointer = 0;
			}
			break;
			
		case "saveTileBtn":
			this.saveTile();
			break;
			
		case "rotateEditorBtn":
			this.rotate();
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

}
