package state;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import main.MainPanel;
import util.GraphicsTools;
import util.NeuralNetwork;
import util.Vector;

public class DrivingQLearning extends State {
	
	NeuralNetwork valueFunction;
	
	Car car;
	
	boolean accelerate = false;
	boolean reverse = false;
	boolean turnLeft = false;
	boolean turnRight = false;

	public DrivingQLearning(StateManager gsm) {
		super(gsm);
		valueFunction = new NeuralNetwork();
		car = new Car(MainPanel.WIDTH / 2, MainPanel.HEIGHT / 2, 0);
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tick(Point mouse2) {
		double accel = 0.5 + (accelerate? 0.5 : 0) + (reverse? -0.5 : 0);
		double rot = 0.5 + (turnLeft? -0.5 : 0) + (turnRight? 0.5 : 0);
		this.car.tick(accel, rot);
	}

	@Override
	public void draw(Graphics g) {
		GraphicsTools.enableAntialiasing(g);
		this.car.draw(g);
		
		g.drawString("Speed: " + this.car.vel.getMagnitude(), 10, 20);
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		if(arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
			this.exit();
		}
		
		if(arg0.getKeyCode() == KeyEvent.VK_W) {
			this.accelerate = true;
		}
		if(arg0.getKeyCode() == KeyEvent.VK_A) {
			this.turnLeft = true;
		}
		if(arg0.getKeyCode() == KeyEvent.VK_D) {
			this.turnRight = true;
		}
		if(arg0.getKeyCode() == KeyEvent.VK_S) {
			this.reverse = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		if(arg0.getKeyCode() == KeyEvent.VK_W) {
			this.accelerate = false;
		}
		if(arg0.getKeyCode() == KeyEvent.VK_A) {
			this.turnLeft = false;
		}
		if(arg0.getKeyCode() == KeyEvent.VK_D) {
			this.turnRight = false;
		}
		if(arg0.getKeyCode() == KeyEvent.VK_S) {
			this.reverse = false;
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
	
	class Car {
		
		double friction = 0.06;
		Vector pos, vel;
		double rot;
		
		double maxRot = Math.toRadians(3);
		double maxAccel = 0.4;
		double minRot = Math.toRadians(-3);
		double minAccel = -0.4;
		
		double size = 30;
		double[][] corners = new double[][] {
			{-0.5, -0.2},
			{0.5, -0.2},
			{0.5, 0.2},
			{-0.5, 0.2}
		};
		
		public Car(double x, double y, double rot) {
			this.pos = new Vector(x, y);
			this.vel = new Vector(0, 0);
			this.rot = rot;
		}
		
		//advance one timestep
		public void tick(double accel, double rot) {	//rot and accel from 0 - 1. 
			//first rotate the car, then accelerate along the bearing. 
			this.rot += minRot + (maxRot - minRot) * rot;
			Vector accelVector = new Vector(1, 0);
			accelVector.rotateCounterClockwise(this.rot);
			accelVector.setMagnitude(minAccel + (maxAccel - minAccel) * accel);
			this.vel.addVector(accelVector);
			
			//apply friction
			this.vel.multiply(1d - this.friction);
			
			//upd pos
			this.pos.addVector(this.vel);
		}
		
		public void draw(Graphics g) {
			double[][] drawnCorners = new double[4][2];
			for(int i = 0; i < 4; i++) {
				Vector c = new Vector(corners[i][0], corners[i][1]);
				c.multiply(this.size);
				c.rotateCounterClockwise(rot);
				c.addVector(this.pos);
				drawnCorners[i][0] = c.x;
				drawnCorners[i][1] = c.y;
			}
			for(int i = 0; i < 4; i++) {
				g.drawLine((int) drawnCorners[i][0], (int) drawnCorners[i][1], (int) drawnCorners[(i + 1) % 4][0], (int) drawnCorners[(i + 1) % 4][1]);
			}
		}
		
	}

}
