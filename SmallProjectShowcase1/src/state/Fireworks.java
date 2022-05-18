package state;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;

import main.MainPanel;
import util.Vector;
import util.Vector3D;
import util.GraphicsTools;
import util.MathTools;

public class Fireworks extends State {
	
	ArrayList<Particle> particles;
	Vector3D camera = new Vector3D(0, 70, -50);
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
	
	Particle[][] ground = new Particle[11][11];

	public Fireworks(StateManager gsm) {
		super(gsm);

		particles = new ArrayList<Particle>();
		
		for(int i = 0; i < ground.length; i++) {
			for(int j = 0; j < ground[0].length; j++) {
				int x = i * 32 - 160;
				int z = j * 32 + 80;
				int y = -10;
				ground[i][j] = new Particle(x, y, z, 0, 0, 0, false, 90, Color.WHITE);
			}
		}
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tick(java.awt.Point mouse2) {
		
		double dx = mouse2.x - prevMouse.x;
		double dy = mouse2.y - prevMouse.y;
		
		if(pressed) {
			xRot += dy / 200;
			yRot += dx / 200;
		}
		
		prevMouse = new java.awt.Point(mouse2.x, mouse2.y);
		
		double moveSpeed = 3;
		
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
		
		if(Math.random() > 0.95) {
			particles.add(new Particle(
				Math.random() * 320d - 160d, -10, Math.random() * 320d + 80, 
				0, 2, 0, 
				true, 90, 
				new Color((int)(Math.random() * 225 + 30), (int)(Math.random() * 225 + 30), (int)(Math.random() * 225 + 30))));
		}
		
		for(int i = 0; i < particles.size(); i++) {
			Particle p = particles.get(i);
			p.tick();
			if(p.lifetime < 0) {
				if(p.explode) {
					p.explode();
				}
				particles.remove(p);
				i--;
				continue;
			}
			
		}
	}

	@Override
	public void draw(Graphics g) {
		
		g.setColor(new Color(51, 51, 51));
		g.fillRect(0, 0, MainPanel.WIDTH, MainPanel.HEIGHT);
		
		for(Particle[] pArr : ground) {
			for(Particle p : pArr) {
				p.draw(g);
			}
		}
		
		for(Particle p : particles) {
			p.draw(g);
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
		
		int lifetime;
		boolean explode;
		Vector3D vel;
		Vector3D pos;
		Color color;
		
		public Particle(double x, double y, double z, double xVel, double yVel, double zVel, boolean explode, int lifetime, Color color) {
			this.lifetime = lifetime;
			this.explode = explode;
			this.pos = new Vector3D(x, y, z);
			this.vel = new Vector3D(xVel, yVel, zVel);
			this.color = color;
		}
		
		public void tick() {
			//System.out.println(pos.x + " " + pos.y + " " + pos.z);
			this.pos.addVector(this.vel);	//update position	
			this.vel.y -= 0.015;	//gravity
			this.lifetime --;
		}
		
		public void draw(Graphics g) {
			Vector3D drawn = MathTools.cameraTransform(pos, camera, xRot, yRot);
			
			double[] zBuffer = new double[] {0};
			drawn = MathTools.projectVector(drawn, zBuffer);
			drawn = MathTools.scaleVector(drawn);
			
			if(zBuffer[0] > 0) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setComposite(GraphicsTools.makeComposite(1));
				
				int size = 3;
				
				if(explode) {
					size = 5;
				}
				
				else {
					g2.setComposite(GraphicsTools.makeComposite((double) this.lifetime / 90d));
				}
				
				g.setColor(this.color);
				g.fillOval((int) drawn.x, (int) drawn.y, size, size);
			}
			
		}
		
		public void explode() {
			for(int i = 0; i < 100; i++) {
				//generate random point within sphere
				Vector3D p = new Vector3D(0, 0, 0);
				while(true) {
					p.x = Math.random() * 2 - 1;
					p.y = Math.random() * 2 - 1;
					p.z = Math.random() * 2 - 1;
					
					if(MathTools.dist3D(p, new Vector3D(0, 0, 0)) < 1) {
						break;
					}
				}
				
				Vector3D v = new Vector3D(p);
				
				v.setMagnitude(Math.random() * 0.15 + 0.5);
				
				particles.add(new Particle(
						pos.x, pos.y, pos.z, 
						v.x, v.y, v.z, 
						false, 90, 
						new Color((int)(Math.random() * 225 + 30), (int)(Math.random() * 225 + 30), (int)(Math.random() * 225 + 30))));
			}
		}
		
	}

}
