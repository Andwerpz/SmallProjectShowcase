package state;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;

import input.Button;
import input.InputManager;

public class RoguelikeMapGenerator extends State {
	
	InputManager im;
	
	static ArrayList<Tile> tiles = new ArrayList<Tile>();

	public RoguelikeMapGenerator(StateManager gsm) {
		super(gsm);
		
		im = new InputManager();
		
		im.addInput(new Button(10, 10, 100, 25, "Tile Editor", "tileEditorBtn"));
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tick(Point mouse2) {
		im.tick(mouse2);
	}

	@Override
	public void draw(Graphics g) {
		im.draw(g);
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		im.keyPressed(arg0);
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public void addTile(ArrayList<ArrayList<Integer>> tile) {
		tiles.add(new Tile(tile));
	}

	public class Tile {
		
		//0 : air;
		//1 : wall / enclosed space
		//2 : connector
		
		ArrayList<ArrayList<Integer>> map;
		
		public Tile() {
			map = new ArrayList<ArrayList<Integer>>();
		}
		
		public Tile(ArrayList<ArrayList<Integer>> tile) {
			this.map = tile;
		}
		
	}
	
}
