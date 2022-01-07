package state;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;

import input.InputManager;
import input.TextField;
import state.RoguelikeMapGenerator.Tile;
import util.TextBox;
import util.Vector;

public class TileEditor extends State {
	
	ArrayList<ArrayList<Integer>> tile;
	
	InputManager im;
	
	int tileSize = 30;
	
	Vector offset = new Vector(0, 0);
	
	java.awt.Point prevMouse = new java.awt.Point(0, 0);
	boolean pressedRight = false;
	boolean pressedLeft = false;

	public TileEditor(StateManager gsm) {
		super(gsm);

		tile = new ArrayList<ArrayList<Integer>>();
		
		for(int i = 0; i < 200; i++) {
			tile.add(new ArrayList<Integer>());
			for(int j = 0; j < 200; j++) {
				tile.get(i).add(0);
			}
		}
		
		im = new InputManager();
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tick(Point mouse2) {
		int dx = mouse2.x - prevMouse.x;
		int dy = mouse2.y - prevMouse.y;
		
		if(pressedLeft) {
			offset.addVector(new Vector(dx, dy));
		}
		
		prevMouse = new java.awt.Point(mouse2.x, mouse2.y);
	}

	@Override
	public void draw(Graphics g) {
		for(int i = 0; i < tile.size(); i++) {
			for(int j = 0; j < tile.get(i).size(); j++) {
				int x = (int) offset.x + j * tileSize;
				int y = (int) offset.y + i * tileSize;
				
				g.drawRect(x, y, tileSize, tileSize);
			}
		}
	}
	
	@Override
	public void keyPressed(KeyEvent arg0) {
		if(arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
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
		// TODO Auto-generated method stub
		
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
		if(arg0.getButton() == MouseEvent.BUTTON1) {
			pressedLeft = true;
		}
		if(arg0.getButton() == MouseEvent.BUTTON2) {
			pressedRight = true;
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		if(arg0.getButton() == MouseEvent.BUTTON1) {
			pressedLeft = false;
		}
		if(arg0.getButton() == MouseEvent.BUTTON2) {
			pressedRight = false;
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		
		if(this.tileSize == 1 && arg0.getUnitsToScroll() > 0) {
			return;
		}
		
		//need to correct for zoom by offsetting view
		double realMouseX = this.prevMouse.x - this.offset.x;
		double realMouseY = this.prevMouse.y - this.offset.y;
		
		//System.out.println(realMouseX + " " + realMouseY);
		
		double tileMouseX = realMouseX / (double) this.tileSize;
		double tileMouseY = realMouseY / (double) this.tileSize;
		
		int diff = arg0.getUnitsToScroll() < 0? 1 : -1;
		
		this.offset.x -= tileMouseX * diff;
		this.offset.y -= tileMouseY * diff;
		
		this.tileSize += diff;
	}

}
