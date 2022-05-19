package state;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;

import input.InputManager;
import input.ToggleButton;
import main.MainPanel;
import state.VerletPhysics.Particle;
import state.VerletPhysics.Spring;
import util.GraphicsTools;
import util.MathTools;
import util.Vec2;
import util.Vec3;

public class ClothPhysics extends State {
	
	InputManager im;
	
	Vec3 gravity = new Vec3(0, -0.1, 0);
	Vec3 light = new Vec3(0, -1, 0);
	
	Vec3 camera = new Vec3(0, 450, 0);
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
	
	
	boolean drawTris = true;
	boolean drawSprings = false;
	boolean drawParticles = false;
	
	int springIterations = 10;

	public ClothPhysics(StateManager gsm) {
		super(gsm);
		
		im = new InputManager();
		
		im.addInput(new ToggleButton(10, 10, 100, 25, "Draw Tris", "toggle_btn_draw_tris"));
		im.addInput(new ToggleButton(10, 40, 100, 25, "Draw Springs", "toggle_btn_draw_springs"));
		im.addInput(new ToggleButton(10, 70, 100, 25, "Draw Vertices", "toggle_btn_draw_vertices"));
		
		im.setToggled("toggle_btn_draw_tris", true);
		
		int tileSize = 150;
		int numTiles = 12;
		
		ground = new Particle[numTiles][numTiles];
		
		for(int i = 0; i < ground.length; i++) {
			for(int j = 0; j < ground[0].length; j++) {
				int x = i * tileSize - tileSize * 6;
				int z = j * tileSize - tileSize * 6;
				int y = 0;
				ground[i][j] = new Particle(new Vec3(x, y, z));
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
		
		this.cloth = new Particle[50][50];
		
		int increment = 10;
		
		for(int i = 0; i < cloth.length; i++) {
			for(int j = 0; j < cloth[0].length; j++) {
				int x = i * increment;
				int z = j * increment;
				int y = 400;
				cloth[i][j] = new Particle(new Vec3(x, y, z));
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
					springs.add(new Spring(0.75, Math.sqrt(2) * increment, cloth[i][j], cloth[i - 1][j - 1]));
				}
				if(j != cloth[0].length - 1 && i != 0) {
					springs.add(new Spring(0.75, Math.sqrt(2) * increment, cloth[i][j], cloth[i - 1][j + 1]));
				}
				
				//bend springs
				if(j > 1) {
					springs.add(new Spring(0.25, increment * 2, cloth[i][j], cloth[i][j - 2]));
				}
				if(i > 1) {
					springs.add(new Spring(0.25, increment * 2, cloth[i][j], cloth[i - 2][j]));
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
		im.tick(mouse2);
		this.drawTris = im.getToggled("toggle_btn_draw_tris");
		this.drawSprings = im.getToggled("toggle_btn_draw_springs");
		this.drawParticles = im.getToggled("toggle_btn_draw_vertices");
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
		
		Vec3 vLookDir = new Vec3(0, 0, 1);
		vLookDir.rotateX(xRot);	vLookDir.rotateY(yRot);
		
		Vec3 forwardDir = new Vec3(vLookDir);
		forwardDir.normalize();
		forwardDir.muli(moveSpeed);
		
		Vec2 left = new Vec2(forwardDir.x, forwardDir.z);
		left.rotate(Math.toRadians(90));
		left.normalize();
		left.muli(moveSpeed);
		
		if(this.left) {
			camera.addi(new Vec3(left.x, 0, left.y));
		}
		if(this.right) {
			left.setLength(-moveSpeed);
			
			camera.addi(new Vec3(left.x, 0, left.y));
		}
		if(this.up) {
			camera.y += moveSpeed;
		}
		if(this.down) {
			camera.y -= moveSpeed;
		}
		
		if(this.forward) {
			camera.addi(forwardDir);
		}
		
		if(this.backward) {
			forwardDir.setLength(-moveSpeed);
			camera.addi(forwardDir);
		}
		
		for(Particle p : particles) {
			p.tick();
			
			//spheres
			p.constrain(new Vec3(350, 150, 150), 50);
			p.constrain(new Vec3(150, 150, 350), 50);
			p.constrain(new Vec3(150, 150, 150), 50);
			p.constrain(new Vec3(350, 150, 350), 50);
		}
		
		for(int i = 0; i < this.springIterations; i++) {
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
				
				Vec3 a = tri[0].projectPoint(az);
				Vec3 b = tri[1].projectPoint(bz);
				Vec3 c = tri[2].projectPoint(cz);
				
				if(az[0] > 0 && bz[0] > 0 && cz[0] > 0) {
					int[] x = {(int) a.x, (int) b.x, (int) c.x};
					int[] y = {(int) a.y, (int) b.y, (int) c.y};
					
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
			
			projected.sort((a, b) -> -Double.compare(a.zBuffer, b.zBuffer));
			
			for(Triangle t : projected) {
				t.draw(g);
			}
		}
		
		im.draw(g);
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
		
		else if(arg0.getKeyCode() == KeyEvent.VK_Z) {
			this.drawSprings = !drawSprings;
		}
		
		else if(arg0.getKeyCode() == KeyEvent.VK_X) {
			this.drawParticles = !drawParticles;
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
		im.mouseClicked(arg0);
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
		pressed = true;
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		im.mouseReleased(arg0);
		pressed = false;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	class Particle {
		
		Vec3 pos, prevPos;
		boolean pinned = false;	//does this particle move
		
		double radius = 2;
		
		double friction = 1;
		double groundFriction = 0.7;
		
		public Particle(Vec3 pos) {
			this.pos = new Vec3(pos);
			this.prevPos = new Vec3(pos);
		}
		
		public Particle(Vec3 pos, Vec3 vel) {
			this.pos = new Vec3(pos);
			this.prevPos = new Vec3(pos);
			this.prevPos.subi(vel);
		}
		
		public void tick() {
			
			if(pinned) {
				return;
			}
			
			Vec3 nextPos = new Vec3(pos);
			Vec3 vel = new Vec3(prevPos, pos);
			vel.muli(this.pos.y < 0.001? this.groundFriction : this.friction);
			nextPos.addi(vel);
			
			nextPos.addi(gravity);
			prevPos = new Vec3(this.pos);
			this.pos = nextPos;
			
			constrain();
		}
		
		public void draw(Graphics g) {
			double[] zBuffer = {0};
			Vec3 drawn = this.projectPoint(zBuffer);
			
			if(zBuffer[0] > 0) {
				g.fillOval(
						(int) (drawn.x - this.radius), 
						(int) (drawn.y - this.radius), 
						(int) (this.radius * 2), 
						(int) (this.radius * 2));
			}
		}
		
		//takes point in 3D space, and projects it to the screen given camera info
		public Vec3 projectPoint(double[] zBuffer) {
			Vec3 drawn = MathTools.cameraTransform(pos, camera, xRot, yRot);
			
			drawn = MathTools.projectVector(drawn, zBuffer);
			drawn = MathTools.scaleVector(drawn);

			return drawn;
		}
		
		//constrain the point to above the ground, y = 0
		public void constrain() {
			if(this.pos.y < 0) {
				this.pos.y = 0;
			}
		}
		
		public void constrain(Vec3 center, double radius) {
			double dist = MathTools.dist3D(center, this.pos);
			if(dist < radius) {
				Vec3 toSurface = new Vec3(center, this.pos);
				toSurface.setLength(radius - dist);
				this.pos.addi(toSurface);
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
			
			Vec3 aToB = new Vec3(a.pos, b.pos);
			Vec3 bToA = new Vec3(b.pos, a.pos);
			
			if(!a.pinned && !b.pinned) {
				aToB.setLength((diff / 2) * strength);
				bToA.setLength((diff / 2) * strength);
				a.pos.addi(aToB);
				b.pos.addi(bToA);
			}
			else if(!b.pinned) {
				bToA.setLength(diff * strength);
				b.pos.addi(bToA);
			}
			else if(!a.pinned){
				aToB.setLength(diff * strength);
				a.pos.addi(aToB);
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
			
			Vec3 aDrawn = a.projectPoint(az);
			Vec3 bDrawn = b.projectPoint(bz);
			
			if(az[0] > 0 && bz[0] > 0) {
				g.drawLine(
						(int) aDrawn.x, 
						(int) aDrawn.y, 
						(int) bDrawn.x, 
						(int) bDrawn.y);
			}
		}
		
	}
	
	class Triangle {
		
		Vec3[] p;
		double zBuffer = 0;
		double brightness = 0;
		
		public Triangle(Vec3 a, Vec3 b, Vec3 c) {
			this.p = new Vec3[] {new Vec3(a), new Vec3(b), new Vec3(c)};
		}
		
		//only call if already projected
		public void draw(Graphics g) {
			int[] x = {(int) p[0].x, (int) p[1].x, (int) p[2].x};
			int[] y = {(int) p[0].y, (int) p[1].y, (int) p[2].y};
//			int[] y = {
//					(int) (MainPanel.HEIGHT / 2 - (p[0].y - MainPanel.HEIGHT / 2)), 
//					(int) (MainPanel.HEIGHT / 2 - (p[1].y - MainPanel.HEIGHT / 2)), 
//					(int) (MainPanel.HEIGHT / 2 - (p[2].y - MainPanel.HEIGHT / 2))};
			
			int color = (int) (255d * brightness);
			//System.out.println(color);
			
			color = Math.max(color, 0);
			color = Math.min(color, 255);
			
			g.setColor(new Color(color, color, color));
			g.fillPolygon(x, y, 3);
		}
		
		//return -1 if failed
		public int project() {
			
			Vec3 v1 = new Vec3(p[0], p[2]);
			Vec3 v2 = new Vec3(p[0], p[1]);
			
			Vec3 cross = MathTools.crossProduct(v1, v2);
			
			cross.normalize();
			
			brightness = MathTools.dotProduct(cross, light);
			
			p[0] = MathTools.cameraTransform(p[0], camera, xRot, yRot);
			p[1] = MathTools.cameraTransform(p[1], camera, xRot, yRot);
			p[2] = MathTools.cameraTransform(p[2], camera, xRot, yRot);
			
			if(p[0].z < 0 || p[1].z < 0 || p[2].z < 0) {	//behind the camera
				return -1;
			}
			
			double[] az = {0};
			double[] bz = {0};
			double[] cz = {0};
			
			p[0] = MathTools.projectVector(p[0], az);
			p[1] = MathTools.projectVector(p[1], bz);
			p[2] = MathTools.projectVector(p[2], cz);
			
			zBuffer = (az[0] + bz[0] + cz[0]) / 3d; 
			
			if(zBuffer > 0) {
				p[0] = MathTools.scaleVector(p[0]);
				p[1] = MathTools.scaleVector(p[1]);
				p[2] = MathTools.scaleVector(p[2]);
				
				return 1;
			}
			return -1;
		}
		
	}

}
