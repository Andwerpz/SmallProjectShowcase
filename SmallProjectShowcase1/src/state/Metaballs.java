package state;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import main.MainPanel;
import util.MathTools;
import util.Vec2;

public class Metaballs extends State {

	ArrayList<Ball> balls;
	
	boolean ballSelected = false;
	int ballId = 0;
	Vec2 prevDiff;
	
	java.awt.Point prevMouse;
	
	public Metaballs(StateManager gsm) {
		super(gsm);

		balls = new ArrayList<Ball>();
		
		for(int i = 0; i < 3; i++) {
			Vec2 pos = new Vec2((Math.random() * (MainPanel.WIDTH - 200)) + 100, (Math.random() * (MainPanel.HEIGHT - 200)) + 100);
			Vec2 vel = new Vec2(2, 2);
			vel.rotate(Math.random() * Math.PI * 2);
			double radius = Math.random() * 15 + 60;
			balls.add(new Ball(pos.x, pos.y, vel.x, vel.y, radius, Math.random() * 225 + 30, Math.random() * 225 + 30, Math.random() * 225 + 30));
		}
		
		prevDiff = new Vec2(0, 0);
		prevMouse = new java.awt.Point();
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tick(Point mouse2) {
		for(Ball b : balls) {
			b.tick();
		}
		
		if(ballSelected) {
			balls.get(ballId).pos = new Vec2(mouse2.x, mouse2.y);
		}
		
		prevDiff.x = mouse2.x - prevMouse.x;
		prevDiff.y = mouse2.y - prevMouse.y;
		
		prevMouse.x = mouse2.x;
		prevMouse.y = mouse2.y;
	}

	@Override
	public void draw(Graphics g) {
		
		BufferedImage img = new BufferedImage(MainPanel.WIDTH, MainPanel.HEIGHT, BufferedImage.TYPE_INT_RGB);
		
		//Graphics gImg = img.getGraphics();
		//gImg.setColor(new Color(51, 51, 51));
		//gImg.fillRect(0, 0, img.getWidth(), img.getHeight());
		
		for(int i = 0; i < MainPanel.WIDTH; i++) {
			for(int j = 0; j < MainPanel.HEIGHT; j++) {
				double x = i;
				double y = j;
				
				double sum = 0;
				
				ArrayList<Double> weight = new ArrayList<Double>();
				
				for(Ball b : balls) {
					double dist = MathTools.dist(x, y, b.pos.x, b.pos.y);
//					if(dist < b.radius) {
//						int rgb = 255 + (255 << 8) + (255 << 16);
//						img.setRGB(i, j, rgb);
//						break;
//					}
					
					
					double add = MathTools.irwinHallDistribution(dist / b.radius) * 4;
					//double add = b.radius / dist;
				
					sum += add;
					
					weight.add(add);
				}
				
				if(sum > 1) {
					double red = 0;
					double green = 0;
					double blue = 0;
					
					for(int k = 0; k < weight.size(); k++) {
						Ball b = balls.get(k);
						double d = weight.get(k);
						
						double ratio = d / sum;
						
						red += b.r * ratio;
						green += b.g * ratio;
						blue += b.b * ratio;
					}
					
					//System.out.println(red + " " + green + " " + blue);
					
					int rgb = (int) red + ((int) green << 8) + ((int) blue << 16);
					img.setRGB(i, j, rgb);
				}
			}
		}
		
		g.drawImage(img, 0, 0, null);
		
//		g.setColor(Color.WHITE);
//		for(Ball b : balls) {
//			b.draw(g);
//		}
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
		for(int i = 0; i < balls.size(); i++) {
			Ball b = balls.get(i);
			double dist = MathTools.dist(arg0.getX(), arg0.getY(), b.pos.x, b.pos.y);
			if(dist < b.radius) {
				ballSelected = true;
				ballId = i;
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		ballSelected = false;
		balls.get(ballId).vel = new Vec2(prevDiff);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	class Ball {
		
		public Vec2 pos, vel;
		public double radius;
		
		public double r, g, b;
		
		public Ball(double x, double y, double xVel, double yVel, double radius, double r, double g, double b) {
			this.pos = new Vec2(x, y);
			this.vel = new Vec2(xVel, yVel);
			this.radius = radius;
			
			this.r = r;
			this.g = g;
			this.b = b;
		}
		
		public void tick() {
			//update position
			this.pos.addi(vel);
			
			//collision detection
			if(this.pos.y - this.radius < 0 || this.pos.y + this.radius > MainPanel.HEIGHT) {
				this.vel.y *= -1;
			}
			if(this.pos.x - this.radius < 0 || this.pos.x + this.radius > MainPanel.WIDTH) {
				this.vel.x *= -1;
			}
		}
		
		public void draw(Graphics g) {
			g.drawOval((int) (this.pos.x - this.radius), (int) (this.pos.y - this.radius), (int) (this.radius * 2), (int) (this.radius * 2));
		}
		
	}

}