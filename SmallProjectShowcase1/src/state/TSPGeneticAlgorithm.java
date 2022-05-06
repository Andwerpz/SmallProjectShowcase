package state;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import input.Button;
import input.InputManager;
import main.MainPanel;
import util.MathTools;

public class TSPGeneticAlgorithm extends State{
	
	InputManager im;
	
	static int numCities = 100;
	static int xMin = 10;
	static int yMin = 10;
	static int yMax = MainPanel.HEIGHT - 10;
	static int xMax = yMax;
	
	static ArrayList<City> cities;
	static int drawnCitySize = 4;
	
	ArrayList<Path> paths;
	
	Path bestPath;
	
	static int totalPaths = 1000;
	static int numPathsSelected = 500;
	static int iteration = 0;
	
	boolean restart = false;

	public TSPGeneticAlgorithm(StateManager gsm) {
		super(gsm);
		
		this.im = new InputManager();
		im.addInput(new Button(610, 70, 100, 25, "Restart", "btn_restart"));
		
		this.init();
	}

	public void init() {
		
		iteration = 0;
		
		cities = new ArrayList<>();
		for(int i = 0; i < numCities; i++) {
			cities.add(new City(Math.random() * (xMax - xMin) + xMin, Math.random() * (yMax - yMin) + yMin));
		}
		
		paths = new ArrayList<>();
		for(int i = 0; i < totalPaths; i++) {
			paths.add(new Path());
		}
		
		Collections.sort(paths, (a, b) -> Double.compare(a.getLength(), b.getLength()));
		bestPath = paths.get(0);
	}

	@Override
	public void tick(Point mouse2) {
		im.tick(mouse2);
		
		if(this.restart) {
			this.init();
			restart = false;
		}
		
		Collections.sort(paths, (a, b) -> Double.compare(a.getLength(), b.getLength()));
		bestPath = paths.get(0);
		
		//cull paths
		for(int i = this.paths.size() - 1; i >= numPathsSelected; i--) {
			this.paths.remove(i);
		}
		
		//generate next population
		while(this.paths.size() < totalPaths) {
			double selection = Math.random();
			Path nextPath = null;
			if(selection > 0.95) {
				nextPath = new Path();
			}
			else if(selection > 0.65) {
				//mutate
				Path p = this.paths.get((int) (Math.random() * numPathsSelected));
				nextPath = p.mutate();
			}
			else {
				//cross over
				Path a = this.paths.get((int) (Math.random() * numPathsSelected));
				Path b = this.paths.get((int) (Math.random() * numPathsSelected));
				nextPath = a.crossOver(b);
			}
			this.paths.add(nextPath);
		}
		
		iteration ++;
	}

	@Override
	public void draw(Graphics g) {
		
		im.draw(g);
		
		//draw best path
		bestPath.draw(g);
		
		
		//draw cities
		g.setColor(Color.BLACK);
		for(City c : cities) {
			g.fillRect((int) c.x - drawnCitySize / 2, (int) c.y - drawnCitySize / 2, drawnCitySize, drawnCitySize);
		}
		
		g.drawRect(5, 5, MainPanel.HEIGHT - 10, MainPanel.HEIGHT - 10);
		
		g.drawString("Iteration: " + iteration, 610, 30);
		g.drawString("Best Path Length: " + bestPath.getLength(), 610, 50);
		
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
		String which = im.mouseClicked(arg0);
		switch(which) {
		case "btn_restart":
			this.restart = true;
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
	
	static class City {
		public double x, y;
		
		public City(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}
	
	static class Path {
		public ArrayList<Integer> p;
		
		public Path() {
			p = new ArrayList<Integer>();
			this.generate();
		}
		
		public Path(Path path) {
			this.p = new ArrayList<Integer>();
			p.addAll(path.p);
		}
		
		public void generate() {
			p.clear();
			for(int i = 0; i < numCities; i++) {
				p.add(i);
			}
			Collections.shuffle(p);
		}
		
		public Path mutate() {
			Path nextPath = new Path(this);
			int which = (int) (Math.random() * 2);
			//inversion
			if(which == 0) {
				int l = (int) (Math.random() * (nextPath.p.size() - 1));
				int r = (int) (Math.random() * (nextPath.p.size() - l) + l);
				while(l <= r) {
					int temp = nextPath.p.get(l);
					nextPath.p.set(l, nextPath.p.get(r));
					nextPath.p.set(r, temp);
					l ++;
					r --;
				}
			}
			//shifting
			else if(which == 1) {
				int l = (int) (Math.random() * (nextPath.p.size() - 1));
				int r = (int) (Math.random() * (nextPath.p.size() - l) + l);
				int offset = (int) (Math.random() * (nextPath.p.size() - r));
				for(int i = 0; i < offset; i++) {
					int temp = nextPath.p.get(r + 1);
					nextPath.p.remove(r + 1);
					nextPath.p.add(l, temp);
					l ++;
					r ++;
				}
			}
			
			return nextPath;
		}
		
		public Path crossOver(Path a) {
			int l = (int) (Math.random() * (this.p.size() - 1));
			int r = (int) (Math.random() * (this.p.size() - l) + l);
			ArrayList<Integer> seg = new ArrayList<Integer>();
			HashSet<Integer> s = new HashSet<>();
			for(int i = l; i <= r; i++) {
				seg.add(p.get(i));
				s.add(p.get(i));
			}
			
			Path newPath = new Path();
			newPath.p.clear();
			for(int i = 0; i < a.p.size(); i++) {
				int next = a.p.get(i);
				if(!s.contains(next)) {
					newPath.p.add(next);
				}
			}
			newPath.p.addAll(seg);
			return newPath;
		}
		
		public void draw(Graphics g) {
			g.setColor(Color.BLUE);
			for(int i = 0; i < p.size(); i++) {
				City a = cities.get(p.get(i));
				City b = cities.get(p.get((i + 1) % cities.size()));
				g.drawLine((int) a.x, (int) a.y, (int) b.x, (int) b.y);
			}
		}
		
		public double getLength() {
			double sum = 0;
			for(int i = 0; i < p.size(); i++) {
				City a = cities.get(p.get(i));
				City b = cities.get(p.get((i + 1) % cities.size()));
				sum += MathTools.dist(a.x, a.y, b.x, b.y);
			}
			return sum;
		}
	}

}
