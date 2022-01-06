package state;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;

import main.MainPanel;
import state.VerletPhysics.Particle;
import state.VerletPhysics.Spring;
import util.GraphicsTools;
import util.MathTools;
import util.Point3D;
import util.Vector;
import util.Vector3D;

public class ClothPhysics extends State {
	
	Vector3D gravity = new Vector3D(0, -0.1, 0);
	Vector3D light = new Vector3D(0, -1, 0);
	
	Point3D camera = new Point3D(0, 70, -500);
	double xRot = 0.25;
	double yRot = 0;
	
	public boolean forward = false;
	public boolean backward = false;
	public boolean left = false;
	public boolean right = false;
	public boolean up = false;
	public boolean down = false;
	
	public boolean pressed = false;
	public java.awt.Point prevMouse = new java.awt.Point();
	java.awt.Point mouse = new java.awt.Point();
	
	ArrayList<Particle> particles = new ArrayList<Particle>();
	ArrayList<Spring> springs = new ArrayList<Spring>();
	
	Particle[][] ground = new Particle[12][12];
	ArrayList<Particle[]> groundTris = new ArrayList<Particle[]>();
	
	Particle[][] cloth;
	
	
	boolean drawTris = false;
	boolean drawSprings = true;
	boolean drawParticles = false;

	public ClothPhysics(StateManager gsm) {
		super(gsm);
		
		int tileSize = 150;
		int numTiles = 12;
		
		ground = new Particle[numTiles][numTiles];
		
		for(int i = 0; i < ground.length; i++) {
			for(int j = 0; j < ground[0].length; j++) {
				int x = i * tileSize - tileSize * 6;
				int z = j * tileSize - tileSize * 6;
				int y = 0;
				ground[i][j] = new Particle(new Vector3D(x, y, z));
			}
		}
		
		for(int i = 0; i < ground.length - 1; i++) {
			for(int j = 0; j < ground[0].length - 1; j++) {
				Particle[] t1 = {ground[i][j], ground[i][j + 1], ground[i + 1][j]};
				Particle[] t2 = {ground[i][j + 1], ground[i + 1][j + 1], ground[i + 1][j]};
				
				groundTris.add(t1);
				groundTris.add(t2);
			}
		}
		
		this.cloth = new Particle[100][100];
		
		int increment = 5;
		
		for(int i = 0; i < cloth.length; i++) {
			for(int j = 0; j < cloth[0].length; j++) {
				int x = i * increment;
				int z = j * increment;
				int y = 200;
				cloth[i][j] = new Particle(new Vector3D(x, y, z));
				particles.add(cloth[i][j]);
				
				//structural springs
				if(j != 0) {
					springs.add(new Spring(1, increment, cloth[i][j], cloth[i][j - 1]));
				}
				if(i != 0) {
					springs.add(new Spring(1, increment, cloth[i][j], cloth[i - 1][j]));
				}
				
				//shear springs
				if(j != 0 && i != 0) {
					springs.add(new Spring(1, Math.sqrt(2) * increment, cloth[i][j], cloth[i - 1][j - 1]));
				}
				if(j != cloth[0].length - 1 && i != 0) {
					springs.add(new Spring(1, Math.sqrt(2) * increment, cloth[i][j], cloth[i - 1][j + 1]));
				}
				
				//bend springs
				if(j > 1) {
					springs.add(new Spring(0.5, increment * 2, cloth[i][j], cloth[i][j - 2]));
				}
				if(i > 1) {
					springs.add(new Spring(0.5, increment * 2, cloth[i][j], cloth[i - 2][j]));
				}
			}
		}
		
		cloth[0][0].pinned = true;
		cloth[0][cloth[0].length - 1].pinned = true;
		cloth[cloth.length - 1][0].pinned = true;
		cloth[cloth.length - 1][cloth[0].length - 1].pinned = true;
		
		
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tick(Point mouse2) {
		this.mouse = mouse2;
		
		
	}

	@Override
	public void draw(Graphics g) {
		double dx = mouse.x - prevMouse.x;
		double dy = mouse.y - prevMouse.y;
		
		if(pressed) {
			xRot += dy / 200;
			yRot += dx / 200;
		}
		
		prevMouse = new java.awt.Point(mouse.x, mouse.y);
		
		double moveSpeed = 6;
		
		Vector3D vLookDir = new Vector3D(0, 0, 1);
		vLookDir.rotateX(xRot);	vLookDir.rotateY(yRot);
		
		Vector3D forwardDir = new Vector3D(vLookDir);
		forwardDir.setMagnitude(moveSpeed);
		
		Vector left = new Vector(forwardDir.x, forwardDir.z);
		left.rotateCounterClockwise(Math.toRadians(90));
		left.setMagnitude(moveSpeed);
		
		if(this.left) {
			camera.addVector(new Vector3D(left.x, 0, left.y));
		}
		if(this.right) {
			left.setMagnitude(-moveSpeed);
			camera.addVector(new Vector3D(left.x, 0, left.y));
		}
		if(this.up) {
			camera.y += moveSpeed;
		}
		if(this.down) {
			camera.y -= moveSpeed;
		}
		
		if(this.forward) {
			camera.addVector(forwardDir);
		}
		
		if(this.backward) {
			forwardDir.setMagnitude(-moveSpeed);
			camera.addVector(forwardDir);
		}
		
		for(Particle p : particles) {
			p.tick();
		}
		
		for(int i = 0; i < 5; i++) {
			for(Spring s : springs) {
				s.tick();
			}
		}
		//background
		g.setColor(new Color(51, 51, 51));
		g.fillRect(0, 0, MainPanel.WIDTH, MainPanel.HEIGHT);
		
		//tiled ground
		g.setColor(new Color(150, 150, 150));
		
		int toggle = 0;
		
		for(int i = 0; i < groundTris.size(); i += 2) {
			for(int j = i; j < i + 2; j++) {
				Particle[] tri = groundTris.get(j);
				
				double[] az = {0};
				double[] bz = {0};
				double[] cz = {0};
				
				Point3D a = tri[0].projectPoint(az);
				Point3D b = tri[1].projectPoint(bz);
				Point3D c = tri[2].projectPoint(cz);
				
				if(az[0] > 0 && bz[0] > 0 && cz[0] > 0) {
					int[] x = {(int) a.x, (int) b.x, (int) c.x};
					int[] y = {
							(int) (MainPanel.HEIGHT / 2 - (a.y - MainPanel.HEIGHT / 2)), 
							(int) (MainPanel.HEIGHT / 2 - (b.y - MainPanel.HEIGHT / 2)), 
							(int) (MainPanel.HEIGHT / 2 - (c.y - MainPanel.HEIGHT / 2))};
					
					g.fillPolygon(x, y, 3);
				}
			}
			
			if(toggle == 0) {
				g.setColor(new Color(200, 200, 200));
				toggle = 1;
			}
			
			else {
				toggle = 0;
				g.setColor(new Color(150, 150, 150));
			}
		}
		
		//debug stuff
		if(drawParticles) {
			g.setColor(Color.WHITE);
			for(Particle p : particles) {
				p.draw(g);
			}
		}
//		
		if(drawSprings) {
			for(Spring s : springs) {
				s.draw(g);
			}
		}
		
		
		//draw cloth tris
		
		if(drawTris) {
			ArrayList<Triangle> tris = new ArrayList<Triangle>();
			
			for(int i = 0; i < cloth.length - 1; i++) {
				for(int j = 0; j < cloth[0].length - 1; j++) {
					Particle[] t1 = {cloth[i][j], cloth[i][j + 1], cloth[i + 1][j]};
					Particle[] t2 = {cloth[i][j + 1], cloth[i + 1][j + 1], cloth[i + 1][j]};
					
					tris.add(new Triangle(t1[0].pos, t1[1].pos, t1[2].pos));
					tris.add(new Triangle(t2[0].pos, t2[1].pos, t2[2].pos));
				}
			}
			
			ArrayList<Triangle> projected = new ArrayList<Triangle>();
			
			for(Triangle t : tris) {
				if(t.project() == 1) {
					projected.add(t);
				}
			}
			
			projected.sort((a, b) -> Double.compare(a.zBuffer, b.zBuffer));
			
			for(Triangle t : projected) {
				t.draw(g);
			}
		}
		
		
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		if(arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
			this.exit();
		}
		if(arg0.getKeyCode() == KeyEvent.VK_W) {
			this.forward = true;
		}
		else if(arg0.getKeyCode() == KeyEvent.VK_S) {
			this.backward = true;
		}
		else if(arg0.getKeyCode() == KeyEvent.VK_A) {
			this.left = true;
		}
		else if(arg0.getKeyCode() == KeyEvent.VK_D) {
			this.right = true;
		}
		else if(arg0.getKeyCode() == KeyEvent.VK_SHIFT) {
			this.down = true;
		}
		else if(arg0.getKeyCode() == KeyEvent.VK_SPACE) {
			this.up = true;
		}
		
		else if(arg0.getKeyCode() == KeyEvent.VK_C) {
			cloth[0][0].pinned = false;
			cloth[0][cloth[0].length - 1].pinned = false;
			cloth[cloth.length - 1][0].pinned = false;
			cloth[cloth.length - 1][cloth[0].length - 1].pinned = false;
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		if(arg0.getKeyCode() == KeyEvent.VK_W) {
			this.forward = false;
		}
		else if(arg0.getKeyCode() == KeyEvent.VK_S) {
			this.backward = false;
		}
		else if(arg0.getKeyCode() == KeyEvent.VK_A) {
			this.left = false;
		}
		else if(arg0.getKeyCode() == KeyEvent.VK_D) {
			this.right = false;
		}
		else if(arg0.getKeyCode() == KeyEvent.VK_SHIFT) {
			this.down = false;
		}
		else if(arg0.getKeyCode() == KeyEvent.VK_SPACE) {
			this.up = false;
		}
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
		pressed = true;
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		pressed = false;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	class Particle {
		
		Vector3D pos, prevPos;
		boolean pinned = false;	//does this particle move
		
		double radius = 2;
		
		double friction = 1;
		double groundFriction = 0.7;
		
		public Particle(Vector3D pos) {
			this.pos = new Vector3D(pos);
			this.prevPos = new Vector3D(pos);
		}
		
		public Particle(Vector3D pos, Vector3D vel) {
			this.pos = new Vector3D(pos);
			this.prevPos = new Vector3D(pos);
			this.prevPos.subtractVector(vel);
		}
		
		public void tick() {
			
			if(pinned) {
				return;
			}
			
			Vector3D nextPos = new Vector3D(pos);
			Vector3D vel = new Vector3D(prevPos, pos);
			vel.multiply(this.pos.y < 0.001? this.groundFriction : this.friction);
			nextPos.addVector(vel);
			
			nextPos.addVector(gravity);
			prevPos = new Vector3D(this.pos);
			this.pos = nextPos;
			
			constrain();
		}
		
		public void draw(Graphics g) {
			double[] zBuffer = {0};
			Point3D drawn = this.projectPoint(zBuffer);
			
			if(zBuffer[0] > 0) {
				g.fillOval(
						(int) (drawn.x - this.radius), 
						(int) (MainPanel.HEIGHT / 2 - (drawn.y - MainPanel.HEIGHT / 2) - this.radius), 
						(int) (this.radius * 2), 
						(int) (this.radius * 2));
			}
		}
		
		//takes point in 3D space, and projects it to the screen given camera info
		public Point3D projectPoint(double[] zBuffer) {
			Point3D drawn = MathTools.cameraTransform(pos, camera, xRot, yRot);
			
			drawn = MathTools.projectPoint(drawn, zBuffer);
			drawn = MathTools.scalePoint(drawn);

			return drawn;
		}
		
		//constrain the point to above the ground, y = 0
		public void constrain() {
			if(this.pos.y < 0) {
				this.pos.y = 0;
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
			double dist = MathTools.dist3D(a.pos, b.pos);
			double diff = dist - length;
			
			Vector3D aToB = new Vector3D(a.pos, b.pos);
			Vector3D bToA = new Vector3D(b.pos, a.pos);
			
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
			double dist = MathTools.dist3D(a.pos, b.pos);
			
			if(this.length - 1 < dist && dist < this.length + 1) {
				g.setColor(Color.BLACK);
			}
			else if(dist > this.length) {	//stretched
				g.setColor(Color.BLUE);
			}
			else {	//compressed
				g.setColor(Color.RED);
			}
			
			double[] az = {0};
			double[] bz = {0};
			
			Point3D aDrawn = a.projectPoint(az);
			Point3D bDrawn = b.projectPoint(bz);
			
			if(az[0] > 0 && bz[0] > 0) {
				g.drawLine(
						(int) aDrawn.x, 
						(int) (MainPanel.HEIGHT / 2 - (aDrawn.y - MainPanel.HEIGHT / 2)), 
						(int) bDrawn.x, 
						(int) (MainPanel.HEIGHT / 2 - (bDrawn.y - MainPanel.HEIGHT / 2)));
			}
		}
		
	}
	
	class Triangle {
		
		Point3D[] p;
		double zBuffer = 0;
		double brightness = 0;
		
		public Triangle(Point3D a, Point3D b, Point3D c) {
			this.p = new Point3D[] {new Point3D(a), new Point3D(b), new Point3D(c)};
		}
		
		//only call if already projected
		public void draw(Graphics g) {
			int[] x = {(int) p[0].x, (int) p[1].x, (int) p[2].x};
			int[] y = {
					(int) (MainPanel.HEIGHT / 2 - (p[0].y - MainPanel.HEIGHT / 2)), 
					(int) (MainPanel.HEIGHT / 2 - (p[1].y - MainPanel.HEIGHT / 2)), 
					(int) (MainPanel.HEIGHT / 2 - (p[2].y - MainPanel.HEIGHT / 2))};
			
			int color = (int) (255d * brightness);
			//System.out.println(color);
			
			color = Math.max(color, 0);
			color = Math.min(color, 255);
			
			g.setColor(new Color(color, color, color));
			g.fillPolygon(x, y, 3);
		}
		
		//return -1 if failed
		public int project() {
			
			Vector3D v1 = new Vector3D(p[0], p[2]);
			Vector3D v2 = new Vector3D(p[0], p[1]);
			
			Vector3D cross = MathTools.crossProduct(v1, v2);
			
			cross.normalize();
			
			brightness = MathTools.dotProduct3D(cross, light);
			
			p[0] = MathTools.cameraTransform(p[0], camera, xRot, yRot);
			p[1] = MathTools.cameraTransform(p[1], camera, xRot, yRot);
			p[2] = MathTools.cameraTransform(p[2], camera, xRot, yRot);
			
			if(p[0].z < 0 || p[1].z < 0 || p[2].z < 0) {	//behind the camera
				return -1;
			}
			
			double[] az = {0};
			double[] bz = {0};
			double[] cz = {0};
			
			p[0] = MathTools.projectPoint(p[0], az);
			p[1] = MathTools.projectPoint(p[1], bz);
			p[2] = MathTools.projectPoint(p[2], cz);
			
			zBuffer = (az[0] + bz[0] + cz[0]) / 3d; 
			
			if(zBuffer > 0) {
				p[0] = MathTools.scalePoint(p[0]);
				p[1] = MathTools.scalePoint(p[1]);
				p[2] = MathTools.scalePoint(p[2]);
				
				return 1;
			}
			return -1;
		}
		
	}

}
