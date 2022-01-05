package state;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import main.MainPanel;
import util.MathTools;
import util.Vector;

public class VerletPhysics extends State {
	
	Vector gravity = new Vector(0, 0.2);
	
	ArrayList<Particle> particles;
	ArrayList<Spring> springs;
	
	boolean particleSelected = false;
	int whichParticle = -1;
	
	HashSet<Integer> heldKeys = new HashSet<Integer>();

	public VerletPhysics(StateManager gsm) {
		super(gsm);

		particles = new ArrayList<Particle>();
		springs = new ArrayList<Spring>();
		
		ArrayList<ArrayList<Particle>> grid = new ArrayList<ArrayList<Particle>>();
		for(int i = 0; i < 21; i++) {
			grid.add(new ArrayList<Particle>());
			for(int j = 0; j < 61; j++) {
				Particle p = new Particle(new Vector(100 + j * 10, 50 + i * 10), new Vector(Math.random() * 10 - 5, Math.random() * 10 - 5));
				grid.get(i).add(p);
				
				particles.add(p);
				
				if(i == 0 && j % 10 == 0) {
					p.pinned = true;
				}
				if(j != 0) {
					springs.add(new Spring(1, 10, grid.get(i).get(j), grid.get(i).get(j - 1)));
				}
				if(i != 0) {
					springs.add(new Spring(1, 10, grid.get(i).get(j), grid.get(i - 1).get(j)));
				}
			}
		}
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tick(Point mouse2) {
		
		if(particleSelected) {
			Vector newPos = new Vector(mouse2.x, mouse2.y);
			particles.get(whichParticle).pos = new Vector(newPos);
			particles.get(whichParticle).prevPos = new Vector(newPos);
		}
		
		for(Particle p : particles) {
			p.tick();
		}
		for(int i = 0; i < 10; i++) {
			for(Spring s : springs) {
				s.tick();
			}
		}
	}

	@Override
	public void draw(Graphics g) {
		
		for(Particle p : particles) {
			p.draw(g);
		}
		for(Spring s : springs) {
			s.draw(g);
		}
		
		
		g.drawString("Press on vertices to move them around", 10, 10);
		g.drawString("Hold 'P' while releasing the mouse to lock the vertice in place", 10, 25);
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		
		heldKeys.add(arg0.getKeyCode());
		
		if(arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
			this.exit();
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		heldKeys.remove(arg0.getKeyCode());
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
		particleSelected = false;
		for(int i = 0; i < particles.size(); i++) {
			Particle p = particles.get(i);
			if(p.contains(new Point(arg0.getX(), arg0.getY()))) {
				particleSelected = true;
				whichParticle = i;
				p.pinned = true;
				return;
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		if(whichParticle != -1 && !heldKeys.contains(KeyEvent.VK_P)) {
			particles.get(whichParticle).pinned = false;
		}
		
		particleSelected = false;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	class Particle {
		
		Vector pos, prevPos;
		boolean pinned = false;	//does this particle move
		
		double radius = 3;
		
		public Particle(Vector pos) {
			this.pos = new Vector(pos);
			this.prevPos = new Vector(pos);
		}
		
		public Particle(Vector pos, Vector vel) {
			this.pos = new Vector(pos);
			this.prevPos = new Vector(pos);
			this.prevPos.subtractVector(vel);
		}
		
		public void tick() {
			
			if(pinned) {
				return;
			}
			
			Vector nextPos = new Vector(pos);
			Vector vel = new Vector(prevPos, pos);
			vel.multiply(1);
			nextPos.addVector(vel);
			
			nextPos.addVector(gravity);
			prevPos = new Vector(this.pos);
			this.pos = nextPos;
			
			constrain();
		}
		
		public void draw(Graphics g) {
			g.fillOval((int) (this.pos.x - this.radius), (int) (this.pos.y - this.radius), (int) (this.radius * 2), (int) (this.radius * 2));
		}
		
		//constrain the point to within the window
		public void constrain() {
			if(this.pos.x < 0) {
				this.pos.x = 0;
			}
			if(this.pos.y < 0) {
				this.pos.y = 0;
			}
			if(this.pos.x > MainPanel.WIDTH) {
				this.pos.x = MainPanel.WIDTH;
			}
			if(this.pos.y > MainPanel.HEIGHT) {
				this.pos.y = MainPanel.HEIGHT;
			}
		}
		
		public boolean contains(Point p) {
			double dist = MathTools.dist(p.x, p.y, this.pos.x, this.pos.y);
			return this.radius > dist;
		}
		
	}
	
	class Spring {
		
		double strength;	//strength of 1 will make the spring rigid
		double length; 	//length at which the two particles will be held
		Particle a, b;
		
		public Spring(double strength, double length, Particle a, Particle b) {
			this.a = a;
			this.b = b;
			this.strength = strength;
			this.length = length;
		}
		
		public void tick() {
			double dist = MathTools.dist(a.pos.x, a.pos.y, b.pos.x, b.pos.y);
			double diff = dist - length;
			
			Vector aToB = new Vector(a.pos, b.pos);
			Vector bToA = new Vector(b.pos, a.pos);
			
			
			
			if(!a.pinned && !b.pinned) {
				aToB.setMagnitude((diff / 2) * strength);
				bToA.setMagnitude((diff / 2) * strength);
				a.pos.addVector(aToB);
				b.pos.addVector(bToA);
			}
			else if(!b.pinned) {
				bToA.setMagnitude(diff * strength);
				b.pos.addVector(bToA);
			}
			else if(!a.pinned){
				aToB.setMagnitude(diff * strength);
				a.pos.addVector(aToB);
			}
			
			
			
		}
		
		public void draw(Graphics g) {
			double dist = MathTools.dist(a.pos.x, a.pos.y, b.pos.x, b.pos.y);
			
			if(this.length - 1 < dist && dist < this.length + 1) {
				g.setColor(Color.BLACK);
			}
			else if(dist > this.length) {	//stretched
				g.setColor(Color.BLUE);
			}
			else {	//compressed
				g.setColor(Color.RED);
			}
			
			g.drawLine((int) a.pos.x, (int) a.pos.y, (int) b.pos.x, (int) b.pos.y);
		}
		
	}

}
