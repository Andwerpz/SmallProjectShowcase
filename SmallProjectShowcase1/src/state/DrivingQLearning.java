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

import input.InputManager;
import input.SliderButton;
import input.ToggleButton;
import main.MainPanel;
import util.FCLayer;
import util.GraphicsTools;
import util.Layer;
import util.MathTools;
import util.NeuralNetwork;
import util.Vec2;

public class DrivingQLearning extends State {

	InputManager im;

	ArrayList<Car> cars;
	int numCars = 200;
	int selectedCar = 0;

	boolean accelerate = false;
	boolean reverse = false;
	boolean turnLeft = false;
	boolean turnRight = false;

	boolean testing = false;

	Vec2 camera;
	boolean mousePressed = false;
	Vec2 prevMouse = new Vec2(0, 0);

	ArrayList<ArrayList<double[]>> walls; // hit these and die
	ArrayList<ArrayList<double[]>> goals; // hit these and be rewarded
	
	int generation = 0;
	int curGenTimer = 0;
	int genTimeLimit = 300;
	
	double avgFitness = 0;

	public DrivingQLearning(StateManager gsm) {
		super(gsm);
		cars = new ArrayList<>();
		for (int i = 0; i < numCars; i++) {
			cars.add(new Car(0, 0));
		}
		this.generateMap();
		camera = new Vec2(0, 0);

		im = new InputManager();
		im.addInput(new SliderButton(10, 65, 200, 10, 100, 1000, "Time Limit", "slider_btn_time_limit"));
		im.setVal("slider_btn_time_limit", 300);
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public void tick(Point mouse2) {
		im.tick(mouse2);
		genTimeLimit = im.getVal("slider_btn_time_limit");
		
		//player controlled car
//		Car sCar = this.cars.get(this.selectedCar);
//		sCar.tick(0.5 + (accelerate ? 0.5 : 0) + (reverse ? -0.5 : 0),
//				0.5 + (turnLeft ? -0.5 : 0) + (turnRight ? 0.5 : 0), walls, goals);
		
		boolean allDead = true;
		curGenTimer ++;
		
		//tick all cars
		for(Car c : cars) {
			c.cpuTick(walls, goals);
			allDead &= c.dead;
		}
		
		if(allDead || curGenTimer >= genTimeLimit) {
			//reset for next generation
			curGenTimer = 0;
			generation ++;
			
			cars.sort((a, b) -> -Double.compare(a.fitness, b.fitness));
			
			for(int i = numCars - 1; i > numCars / 2; i--) {
				cars.remove(i);
			}
			
			//calc avg fitness
			this.avgFitness = 0;
			for(Car c : cars) {
				avgFitness += c.fitness;
			}
			avgFitness /= cars.size();
			
			ArrayList<Car> nextCars = new ArrayList<Car>();
			while(cars.size() + nextCars.size() < numCars) {
				double choice = Math.random();
				if(choice > 0.95) {
					//generate new random car
					nextCars.add(new Car(0, 0));
				}
				else if(choice > 0.65) {
					//mutate existing car
					nextCars.add(this.mutate(cars.get((int) (Math.random() * cars.size()))));
				}
				else {
					//combine two existing cars
					nextCars.add(this.combine(cars.get((int) (Math.random() * cars.size())), cars.get((int) (Math.random() * cars.size()))));
				}
			}
			cars.addAll(nextCars);
			
			for(Car c : cars) {
				c.reset();
			}
		}

		// adjust camera
		int dx = (int) (mouse2.x - prevMouse.x);
		int dy = (int) (mouse2.y - prevMouse.y);
		if (mousePressed) {
			this.camera.subi(new Vec2(dx, dy));
		}
		prevMouse = new Vec2(mouse2.x, mouse2.y);
	}
	
	//take average of weights between two cars, and mutate the result. 
	public Car combine(Car a, Car b) {
		Random rand = new Random();
		
		Car out = new Car(0, 0);
		NeuralNetwork net = out.moveNet;
		
		//combine weights
		for(int i = 0; i < net.layers.size(); i++) {
			FCLayer fl = (FCLayer) net.layers.get(i);
			for(int j = 0; j < fl.weights.length; j++) {
				double[] w = fl.weights[j];
				for(int k = 0; k < w.length; k++) {
					w[k] = (((FCLayer) a.moveNet.layers.get(i)).weights[j][k] + ((FCLayer) b.moveNet.layers.get(i)).weights[j][k]) / 2;
					w[k] += rand.nextGaussian();
				}
			}
		}
		
		return out;
	}
	
	double mutationChance = 0.1;	//chance for each weight to be mutated
	double mutationStrength = 1;
	
	//randomly change a portion of weights
	public Car mutate(Car a) {
		Random rand = new Random();
		
		//copy over neural network
		Car out = new Car(0, 0);
		out.moveNet.layers.clear();
		for(Layer l : a.moveNet.layers) {
			out.moveNet.layers.add(new FCLayer(out.moveNet, (FCLayer) l));
		}
		NeuralNetwork net = out.moveNet;
		
		//apply mutation
		for(Layer l : net.layers) {
			FCLayer fl = (FCLayer) l;
			for(double[] w : fl.weights) {
				for(int i = 0; i < w.length; i++) {
					if(Math.random() < mutationChance) {
						w[i] += rand.nextGaussian() * mutationStrength;
					}
				}
			}
		}
		
		return out;
	}

	@Override
	public void draw(Graphics g) {

		g.setColor(Color.BLACK);
		GraphicsTools.enableAntialiasing(g);

		Car sCar = this.cars.get(this.selectedCar);

		// --CAMERA SPACE--
		g.translate((int) (-camera.x + MainPanel.WIDTH / 2), (int) (-camera.y + MainPanel.HEIGHT / 2));

		for (Car c : cars) {
			c.draw(g);
		}

		// draw goals
		g.setColor(Color.GREEN);
		for (ArrayList<double[]> c : goals) {
			for (double[] d : c) {
				g.drawLine((int) d[0], (int) d[1], (int) d[2], (int) d[3]);
			}
		}

		// draw walls
		g.setColor(Color.BLACK);
		for (ArrayList<double[]> c : walls) {
			for (double[] d : c) {
				g.drawLine((int) d[0], (int) d[1], (int) d[2], (int) d[3]);
			}
		}

		// --SCREEN SPACE--
		g.translate((int) -(-camera.x + MainPanel.WIDTH / 2), (int) -(-camera.y + MainPanel.HEIGHT / 2));

		g.drawString("Generation: " + generation, 10, 20);
		g.drawString("Avg Fitness: " + avgFitness, 10, 40);

		im.draw(g);
	}

	static int mapCellSize = 150;
	static int mapLength = 100;

	// map consists of square cells.
	public void generateMap() {
		this.walls = new ArrayList<>();
		this.goals = new ArrayList<>();

		// generate cell layout
		HashSet<ArrayList<Integer>> s = new HashSet<>();
		int curX = 0;
		int curY = 0;
		ArrayList<int[]> cells = new ArrayList<>();
		int[][] dxy = new int[][] { { 1, 0 }, { -1, 0 }, { 0, -1 }, { 0, 1 } };
		while (true) {
			boolean isValid = true;
			curX = 0;
			curY = 0;
			cells.clear();
			s.clear();
			dxy = new int[][] { { 1, 0 }, { -1, 0 }, { 0, -1 }, { 0, 1 } };
			for (int i = 0; i < mapLength; i++) {
				if (i != 0) {
					shuffleArray(dxy);
				}
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

			this.walls.add(new ArrayList<>());
			this.goals.add(new ArrayList<>());

			// if there is a cell on a given side, then that wall becomes a goal
			dxyLoop: for (int k = 0; k < 4; k++) {
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

						if (j == -1) { // we're checking the previous cell. It already has a goal for us
							continue dxyLoop;
						}
						break;
					}
				}

				double[] nextWall = new double[] { wallTemplate[k][0] + x, wallTemplate[k][1] + y,
						wallTemplate[k][2] + x, wallTemplate[k][3] + y };
				if (isWall) {
					this.walls.get(i).add(nextWall);
				} else {
					this.goals.get(i).add(nextWall);
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
		mousePressed = true;
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		im.mouseReleased(arg0);
		mousePressed = false;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		// TODO Auto-generated method stub

	}

	static double goalReward = 1;
	static double wallReward = 0;

	// INPUT:
	// normalized velocity
	// normalized rotation
	// 31 prox sensors
	public static int inputSize = 35;

	// OUTPUT:
	// accelerate and turn left
	// accelerate and turn right
	// accelerate
	// turn left
	// turn right
	// idle
	// reverse
	public static int outputSize = 7;

	class Car {

		NeuralNetwork moveNet;

		double friction = 0.06;
		Vec2 pos, vel;
		double rot;

		double maxRot = Math.toRadians(7);
		double maxAccel = 0.4;
		double minRot = Math.toRadians(-7);
		double minAccel = -0.4;

		int whichCell; // which cell is this car currently at
		int maxCell; // maximum cell visited by this car

		double size = 30;
		double[][] corners = new double[][] { { -0.4, -0.2 }, { 0.4, -0.2 }, { 0.4, 0.2 }, { -0.4, 0.2 } };

		public double fitness;
		public boolean dead;
		
		//creates a brand new car with randomly generated weights
		public Car(double x, double y) {
			this.pos = new Vec2(x, y);
			this.vel = new Vec2(0, 0);
			this.rot = 0;
			this.whichCell = 0;
			this.maxCell = 0;
			this.fitness = 0;
			this.dead = false;

			this.moveNet = new NeuralNetwork(inputSize, outputSize);
			this.moveNet.layers.add(new FCLayer(moveNet, inputSize, 40, FCLayer.ACTIVATION_TYPE_SIGMOID));
			this.moveNet.layers.add(new FCLayer(moveNet, 40, 30, FCLayer.ACTIVATION_TYPE_SIGMOID));
			this.moveNet.layers.add(new FCLayer(moveNet, 30, outputSize, FCLayer.ACTIVATION_TYPE_SIGMOID));
		}
		
		//resets position
		public void reset() {
			this.pos = new Vec2(0, 0);
			this.vel = new Vec2(0, 0);
			this.rot = 0;
			this.whichCell = 0;
			this.maxCell = 0;
			this.fitness = 0;
			this.dead = false;
		}

		// advance one timestep
		public void tick(double accel, double rot, ArrayList<ArrayList<double[]>> walls,
				ArrayList<ArrayList<double[]>> goals) { // rot and accel from 0 - 1.
			// check if dead
			if (this.dead) {
				return;
			}

			// first rotate the car, then accelerate along the bearing.
			this.rot += minRot + (maxRot - minRot) * rot;
			Vec2 accelVector = new Vec2(1, 0);
			accelVector.rotate(this.rot);
			accelVector.normalize();
			accelVector.muli(minAccel + (maxAccel - minAccel) * accel);
			this.vel.addi(accelVector);

			// apply friction
			this.vel.muli(1d - this.friction);

			// upd pos
			this.pos.addi(this.vel);

			// upd fitness
			if (this.wallCollision(walls)) {
				this.fitness += DrivingQLearning.wallReward;
				this.dead = true;
			}
			if (this.goalCollision(goals) == 1) {
				this.fitness += DrivingQLearning.goalReward;
			}
		}

		// make the moveNet predict the best move and take that one
		public void cpuTick(ArrayList<ArrayList<double[]>> walls, ArrayList<ArrayList<double[]>> goals) {
			double[] moveNetOutput = this.moveNet.forwardPropogate(this.getInput(walls, null));
			int bestMove = (int) (Math.random() * moveNetOutput.length);
			double bestMoveVal = moveNetOutput[bestMove];
			for (int i = 0; i < moveNetOutput.length; i++) {
				if (moveNetOutput[i] > bestMoveVal) {
					bestMove = i;
					bestMoveVal = moveNetOutput[i];
				}
			}

			// accelerate and turn left
			// accelerate and turn right
			// accelerate
			// turn left
			// turn right
			// idle
			// reverse

			boolean accelerate = false, left = false, right = false, reverse = false;

			switch (bestMove) {
			case 0:
				accelerate = true;
				left = true;
				break;

			case 1:
				accelerate = true;
				right = true;
				break;

			case 2:
				accelerate = true;
				break;

			case 3:
				left = true;
				break;

			case 4:
				right = true;
				break;

			case 5:
				// do nothing
				break;

			case 6:
				reverse = true;
				break;
			}

			this.tick(0.5 + (accelerate ? 0.5 : 0) + (reverse ? -0.5 : 0), 0.5 + (left ? -0.5 : 0) + (right ? 0.5 : 0),
					walls, goals);
		}

		int numSightLines = 31;
		double viewConeRad = Math.toRadians(240);

		// what the car can see
		public double[] getInput(ArrayList<ArrayList<double[]>> walls, Graphics g) {
			ArrayList<Double> ans = new ArrayList<Double>();

			// add normalized rotation, and velocity vector
			Vec2 normVel = new Vec2(this.vel);

			Vec2 normRot = new Vec2(0, 1);
			normRot.rotate(this.rot);

			ans.add(normVel.x);
			ans.add(normVel.y);
			ans.add(normRot.x);
			ans.add(normRot.y);

			double curRot = -viewConeRad / 2d;
			double increment = viewConeRad / (double) (numSightLines - 1);
			for (int i = 0; i < numSightLines; i++) {
				Vec2 sightVector = new Vec2(1, 0);
				sightVector.rotate(curRot + this.rot);
				sightVector.muli(100000);
				sightVector.addi(this.pos);

				double minDist = Integer.MAX_VALUE;
				Vec2 minDistVector = null;
				for (ArrayList<double[]> c : walls) {
					for (double[] d : c) {
						Vec2 intersect = MathTools.line_lineCollision(this.pos.x, this.pos.y, sightVector.x,
								sightVector.y, d[0], d[1], d[2], d[3]);
						if (intersect != null) {
							double dist = intersect.sub(this.pos).length();
							if (dist < minDist) {
								minDist = dist;
								minDistVector = intersect;
							}
						}
					}
				}

//				g.setColor(Color.GREEN);
//				if(minDistVector != null) {
//					g.drawLine((int) this.pos.x, (int) this.pos.y, (int) minDistVector.x, (int) minDistVector.y);
//					g.drawRect((int) minDistVector.x - 2, (int) minDistVector.y - 2, 4, 4);
//				}

				ans.add(1d / minDist);
				curRot += increment;
			}

			double[] output = new double[ans.size()];
			for (int i = 0; i < output.length; i++) {
				output[i] = ans.get(i);
			}

			return output;
		}

		public void draw(Graphics g) {
			double[][] drawnCorners = new double[4][2];
			for (int i = 0; i < 4; i++) {
				Vec2 c = new Vec2(corners[i][0], corners[i][1]);
				c.muli(this.size);
				c.rotate(rot);
				c.addi(this.pos);
				drawnCorners[i][0] = c.x;
				drawnCorners[i][1] = c.y;
			}
			for (int i = 0; i < 4; i++) {
				g.drawLine((int) drawnCorners[i][0], (int) drawnCorners[i][1], (int) drawnCorners[(i + 1) % 4][0],
						(int) drawnCorners[(i + 1) % 4][1]);
			}
		}

		// checks if any part of the car collides with the line
		public boolean lineCollision(double x1, double y1, double x2, double y2) {
			double[][] corners = new double[4][2];
			for (int i = 0; i < 4; i++) {
				Vec2 c = new Vec2(this.corners[i][0], this.corners[i][1]);
				c.muli(this.size);
				c.rotate(rot);
				c.addi(this.pos);
				corners[i][0] = c.x;
				corners[i][1] = c.y;
			}

			for (int i = 0; i < 4; i++) {
				if (MathTools.line_lineCollision(x1, y1, x2, y2, corners[i][0], corners[i][1], corners[(i + 1) % 4][0],
						corners[(i + 1) % 4][1]) != null) {
					return true;
				}
			}
			return false;
		}

		// if you have last touched the ith goal, then you only need to check cells i,
		// and i + 1.
		public boolean wallCollision(ArrayList<ArrayList<double[]>> walls) {
			for (int i = this.whichCell; i < Math.min(walls.size(), this.whichCell + 2); i++) {
				for (double[] d : walls.get(i)) {
					if (this.lineCollision(d[0], d[1], d[2], d[3])) {
						return true;
					}
				}
			}
			return false;
		}

		// if you have last touched the ith goal, then you only need to check if you're
		// touching the i + 1th or i - 1th goal.
		// returns true only if max cell has been incremented.
		public int goalCollision(ArrayList<ArrayList<double[]>> goals) {
			// previous goal
			if (this.whichCell > 0) {
				double[] nextGoal = goals.get(this.whichCell - 1).get(0);
				if (this.lineCollision(nextGoal[0], nextGoal[1], nextGoal[2], nextGoal[3])) {
					this.whichCell--;
					return -1; // going backwards
				}
			}
			// next goal
			if (this.whichCell != goals.size() - 1) {
				double[] nextGoal = goals.get(this.whichCell + 1).get(0);
				if (this.lineCollision(nextGoal[0], nextGoal[1], nextGoal[2], nextGoal[3])) {
					this.whichCell++;
					if (this.maxCell < this.whichCell) {
						this.maxCell = this.whichCell;
						return 1; // going forwards
					}
				}
			}
			return 0;
		}

	}

}
