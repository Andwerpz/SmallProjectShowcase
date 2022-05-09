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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

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

	ArrayList<double[]> walls; // hit these and die
	ArrayList<double[]> goals; // hit these and be rewarded

	public DrivingQLearning(StateManager gsm) {
		super(gsm);
		valueFunction = new NeuralNetwork();
		car = new Car(0, 0, 0);
		this.generateMap();
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public void tick(Point mouse2) {
		double accel = 0.5 + (accelerate ? 0.5 : 0) + (reverse ? -0.5 : 0);
		double rot = 0.5 + (turnLeft ? -0.5 : 0) + (turnRight ? 0.5 : 0);
		this.car.tick(accel, rot);
	}

	@Override
	public void draw(Graphics g) {
		g.translate((int) (-this.car.pos.x + MainPanel.WIDTH / 2), (int) (-this.car.pos.y + MainPanel.HEIGHT / 2)); // real
																													// space

		GraphicsTools.enableAntialiasing(g);
		this.car.draw(g);

		// draw goals
		g.setColor(Color.GREEN);
		for (double[] d : goals) {
			g.drawLine((int) d[0], (int) d[1], (int) d[2], (int) d[3]);
		}

		// draw walls
		g.setColor(Color.BLACK);
		for (double[] d : walls) {
			g.drawLine((int) d[0], (int) d[1], (int) d[2], (int) d[3]);
		}

		g.translate((int) -(-this.car.pos.x + MainPanel.WIDTH / 2), (int) -(-this.car.pos.y + MainPanel.HEIGHT / 2)); // screen
																														// space

		g.drawString("Speed: " + this.car.vel.getMagnitude(), 10, 20);
	}

	static int mapCellSize = 150;
	static int mapLength = 100;

	// map consists of square cells.
	public void generateMap() {
		this.walls = new ArrayList<double[]>();
		this.goals = new ArrayList<double[]>();

		// generate cell layout
		HashSet<ArrayList<Integer>> s = new HashSet<>();
		int curX = 0;
		int curY = 0;
		ArrayList<int[]> cells = new ArrayList<>();
		int[][] dxy = new int[][] { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
		while (true) {
			boolean isValid = true;
			curX = 0;
			curY = 0;
			cells.clear();
			s.clear();
			for (int i = 0; i < mapLength; i++) {
				shuffleArray(dxy);
				isValid = false;
				cells.add(new int[] { curX, curY });
				s.add(new ArrayList<Integer>(Arrays.asList(curX, curY)));
				for (int j = 0; j < 4; j++) {
					int nextX = curX + dxy[j][0];
					int nextY = curY + dxy[j][1];
					if (!s.contains(new ArrayList<Integer>(Arrays.asList(nextX, nextY)))) {
						curX = nextX;
						curY = nextY;
						isValid = true;
						break;
					}
				}
				if (!isValid) {
					break;
				}
			}
			if (isValid) {
				break;
			}
		}

		// create walls
		dxy = new int[][] { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
		double[][] wallTemplate = new double[][] {
				{ -mapCellSize / 2, -mapCellSize / 2, -mapCellSize / 2, mapCellSize / 2 }, // L
				{ mapCellSize / 2, -mapCellSize / 2, mapCellSize / 2, mapCellSize / 2 }, // R
				{ -mapCellSize / 2, -mapCellSize / 2, mapCellSize / 2, -mapCellSize / 2 }, // U
				{ -mapCellSize / 2, mapCellSize / 2, mapCellSize / 2, mapCellSize / 2 } // D
		};
		for (int i = 0; i < cells.size(); i++) {
			int cellX = cells.get(i)[0];
			int cellY = cells.get(i)[1];
			int x = cellX * mapCellSize;
			int y = cellY * mapCellSize;
			// if there is a cell on a given side, then that wall becomes a goal
			for (int k = 0; k < 4; k++) {
				int nextX = cellX + dxy[k][0];
				int nextY = cellY + dxy[k][1];
				boolean isWall = true;
				for (int j = -1; j <= 1; j += 2) {
					if (j + i == -1 || j + i == cells.size()) {
						continue;
					}
					int connectedCellX = cells.get(i + j)[0];
					int connectedCellY = cells.get(i + j)[1];

					if (connectedCellX == nextX && connectedCellY == nextY) {
						isWall = false;
						break;
					}
				}

				double[] nextWall = new double[] { wallTemplate[k][0] + x, wallTemplate[k][1] + y,
						wallTemplate[k][2] + x, wallTemplate[k][3] + y };
				if (isWall) {
					this.walls.add(nextWall);
				} else {
					this.goals.add(nextWall);
				}
			}

		}
	}

	static void shuffleArray(int[][] ar) {
		Random rnd = new Random();
		for (int i = ar.length - 1; i > 0; i--) {
			int index = rnd.nextInt(i + 1);
			int[] a = ar[index];
			ar[index] = ar[i];
			ar[i] = a;
		}
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
			this.exit();
		}

		if (arg0.getKeyCode() == KeyEvent.VK_W) {
			this.accelerate = true;
		}
		if (arg0.getKeyCode() == KeyEvent.VK_A) {
			this.turnLeft = true;
		}
		if (arg0.getKeyCode() == KeyEvent.VK_D) {
			this.turnRight = true;
		}
		if (arg0.getKeyCode() == KeyEvent.VK_S) {
			this.reverse = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		if (arg0.getKeyCode() == KeyEvent.VK_W) {
			this.accelerate = false;
		}
		if (arg0.getKeyCode() == KeyEvent.VK_A) {
			this.turnLeft = false;
		}
		if (arg0.getKeyCode() == KeyEvent.VK_D) {
			this.turnRight = false;
		}
		if (arg0.getKeyCode() == KeyEvent.VK_S) {
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

		double maxRot = Math.toRadians(7);
		double maxAccel = 0.4;
		double minRot = Math.toRadians(-7);
		double minAccel = -0.4;

		double size = 30;
		double[][] corners = new double[][] { { -0.4, -0.2 }, { 0.4, -0.2 }, { 0.4, 0.2 }, { -0.4, 0.2 } };

		public Car(double x, double y, double rot) {
			this.pos = new Vector(x, y);
			this.vel = new Vector(0, 0);
			this.rot = rot;
		}

		// advance one timestep
		public void tick(double accel, double rot) { // rot and accel from 0 - 1.
			// first rotate the car, then accelerate along the bearing.
			this.rot += minRot + (maxRot - minRot) * rot;
			Vector accelVector = new Vector(1, 0);
			accelVector.rotateCounterClockwise(this.rot);
			accelVector.setMagnitude(minAccel + (maxAccel - minAccel) * accel);
			this.vel.addVector(accelVector);

			// apply friction
			this.vel.multiply(1d - this.friction);

			// upd pos
			this.pos.addVector(this.vel);
		}

		public void draw(Graphics g) {
			double[][] drawnCorners = new double[4][2];
			for (int i = 0; i < 4; i++) {
				Vector c = new Vector(corners[i][0], corners[i][1]);
				c.multiply(this.size);
				c.rotateCounterClockwise(rot);
				c.addVector(this.pos);
				drawnCorners[i][0] = c.x;
				drawnCorners[i][1] = c.y;
			}
			for (int i = 0; i < 4; i++) {
				g.drawLine((int) drawnCorners[i][0], (int) drawnCorners[i][1], (int) drawnCorners[(i + 1) % 4][0],
						(int) drawnCorners[(i + 1) % 4][1]);
			}
		}
		
		//checks if any part of the car collides with the line
		public void lineCollision(double x1, double y1, double x2, double y2) {
			
		}

	}

}
