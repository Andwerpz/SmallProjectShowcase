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
import util.GraphicsTools;
import util.MathTools;
import util.NeuralNetwork;
import util.Vec2;

public class DrivingQLearning extends State {
	
	InputManager im;

	ArrayList<Car> cars;
	int numCars = 100;
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

	public DrivingQLearning(StateManager gsm) {
		super(gsm);
		cars = new ArrayList<>();
		for(int i = 0; i < numCars; i++) {
			cars.add(new Car(0, 0));
		}
		this.generateMap();
		camera = new Vec2(0, 0);
		
		im = new InputManager();
		im.addInput(new SliderButton(10, 80, 125, 10, 0, 100, "Exploit Chance", "slider_btn_exploit_chance"));
		im.setVal("slider_btn_exploit_chance", 0);
		im.addInput(new ToggleButton(10, 100, 100, 25, "Toggle Test", "toggle_btn_test"));
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public void tick(Point mouse2) {
		im.tick(mouse2);
		exploitChance = (double) im.getVal("slider_btn_exploit_chance") / 100d;
		testing = im.getToggled("toggle_btn_test");
		
		boolean allDead = true;
		
		//train cars
		for(Car c : cars) {
			if(testing) {
				c.test(walls, goals, null);
			}
			else {
				if(!c.dead) {
					allDead = false;
				}
				else {
					continue;
				}
				c.train(walls, goals, null);
			}
		}
		
		//train the value func with the information provided, and reset cars
		if(allDead) {
			for(Car c : cars) {
				c.experienceReplay();
				c.reset();
			}
		}
		
		//adjust camera
		int dx = (int) (mouse2.x - prevMouse.x);
		int dy = (int) (mouse2.y - prevMouse.y);
		if(mousePressed) {
			this.camera.sub(new Vec2(dx, dy));
		}
		prevMouse = new Vec2(mouse2.x, mouse2.y);
	}

	@Override
	public void draw(Graphics g) {
		
		g.setColor(Color.BLACK);
		GraphicsTools.enableAntialiasing(g);
		
		Car sCar = this.cars.get(this.selectedCar);
		
		// --CAMERA SPACE--
		g.translate((int) (-camera.x + MainPanel.WIDTH / 2), (int) (-camera.y + MainPanel.HEIGHT / 2));

		for(Car c : cars) {
			c.draw(g);
		}

		// draw goals
//		g.setColor(Color.GREEN);
//		for(ArrayList<double[]> c : goals) {
//			for (double[] d : c) {
//				g.drawLine((int) d[0], (int) d[1], (int) d[2], (int) d[3]);
//			}
//		}
		

		// draw walls
		g.setColor(Color.BLACK);
		for(ArrayList<double[]> c : walls) {
			for (double[] d : c) {
				g.drawLine((int) d[0], (int) d[1], (int) d[2], (int) d[3]);
			}
		}
		
		
		// --SCREEN SPACE--
		g.translate((int) -(-camera.x + MainPanel.WIDTH / 2), (int) -(-camera.y + MainPanel.HEIGHT / 2));

		g.drawString("Speed: " + sCar.vel.length(), 10, 20);
		g.drawString("Pos: " + sCar.pos, 10, 40);
		g.drawString("Trials: " + trials, 10, 60);
		
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
				if(i != 0) {
					//shuffleArray(dxy);
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
			dxyLoop:
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
						
						if(j == -1) {	//we're checking the previous cell. It already has a goal for us
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
	
	static NeuralNetwork valueFunction = new NeuralNetwork();
	static double exploitChance = 0;
	static double discount = 0.3;
	static double learningRate = 0.001;
	static int trials = 0;
	
	static double goalReward = 5;
	static double timeReward = -0.1;
	static double wallReward = -1;

	class Car {

		double friction = 0.06;
		Vec2 pos, vel;
		double rot;

		double maxRot = Math.toRadians(7);
		double maxAccel = 0.4;
		double minRot = Math.toRadians(-7);
		double minAccel = -0.4;
		
		int whichCell;	//which cell is this car currently at
		int maxCell;	//maximum cell visited by this car

		double size = 30;
		double[][] corners = new double[][] { { -0.4, -0.2 }, { 0.4, -0.2 }, { 0.4, 0.2 }, { -0.4, 0.2 } };
		
		boolean dead = false;
		
		//STATE REPLAY
		ArrayList<double[]> states;	//inputs
		ArrayList<Integer> moves;	//chosen move
		ArrayList<Double> rewards;	//upd reward

		public Car(double x, double y) {
			this.pos = new Vec2(x, y);
			this.vel = new Vec2(0, 0);
			this.rot = 0;
			this.whichCell = 0;
			this.maxCell = 0;
			this.states = new ArrayList<double[]>();
			this.moves = new ArrayList<Integer>();
			this.rewards = new ArrayList<Double>();
		}
		
		public void reset() {
			this.pos = new Vec2(0, 0);
			this.vel = new Vec2(0, 0);
			this.rot = 0;
			this.whichCell = 0;
			this.maxCell = 0;
			this.states = new ArrayList<double[]>();
			this.moves = new ArrayList<Integer>();
			this.rewards = new ArrayList<Double>();
			this.dead = false;
		}

		// advance one timestep
		public void tick(double accel, double rot) { // rot and accel from 0 - 1.
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
		}
		
		public void test(ArrayList<ArrayList<double[]>> walls, ArrayList<ArrayList<double[]>> goals, Graphics g) {
			//get q values
			double[] s1Input = this.getInput(walls, g);
			double[] s1Output = DrivingQLearning.valueFunction.forwardPropogate(s1Input);
			
			//determine move
			double maxActivation = s1Output[0];
			int s1Move = 0;
			for(int i = 0; i < s1Output.length; i++) {
				if(maxActivation < s1Output[i]) {
					maxActivation = s1Output[i];
					s1Move = i;
				}
			}
			
			//perform tick
			switch(s1Move) {
			case 0:
				this.tick(1, 0);
				break;
				
			case 1:
				this.tick(1, 1);
				break;
				
			case 2:
				this.tick(1, 0.5);
				break;
				
			case 3:
				this.tick(0.5, 0);
				break;
				
			case 4:
				this.tick(0.5, 1);
				break;
				
			case 5:
				this.tick(0.5, 0.5);
				break;
				
			case 6:
				this.tick(0, 0.5);
				break;
			}
			
			//get reward + estimated future reward * discount
			this.goalCollision(goals);
			if(this.wallCollision(walls)) {
				this.reset();
			}
		}
		
		//does a tick, and then trains the function network based off of reward gained. 
		public void train(ArrayList<ArrayList<double[]>> walls, ArrayList<ArrayList<double[]>> goals, Graphics g) {
			//get input for current state
			double[] s1Input = this.getInput(walls, g);
			
//			for(double d : s1Input) {
//				System.out.println(d);
//			}
//			System.out.println();
			
			double[] s1Output = DrivingQLearning.valueFunction.forwardPropogate(s1Input);
			
			//choose move
			//OUTPUT:
			//0: accelerate and turn left
			//1: accelerate and turn right
			//2: accelerate
			//3: turn left
			//4: turn right
			//5: idle
			//6: reverse
			int s1Move = -1;
			if(Math.random() < DrivingQLearning.exploitChance) {	//choose best move
				double maxActivation = s1Output[0];
				s1Move = 0;
				for(int i = 0; i < s1Output.length; i++) {
					if(maxActivation < s1Output[i]) {
						maxActivation = s1Output[i];
						s1Move = i;
					}
				}
			}
			else {	//random move
				s1Move = (int) (Math.random() * s1Output.length);
			}
			
			//perform tick
			switch(s1Move) {
			case 0:
				this.tick(1, 0);
				break;
				
			case 1:
				this.tick(1, 1);
				break;
				
			case 2:
				this.tick(1, 0.5);
				break;
				
			case 3:
				this.tick(0.5, 0);
				break;
				
			case 4:
				this.tick(0.5, 1);
				break;
				
			case 5:
				this.tick(0.5, 0.5);
				break;
				
			case 6:
				this.tick(0, 0.5);
				break;
			}
			
			//get output of s2.
			double[] s2Input = this.getInput(walls, g);
			double[] s2Output = DrivingQLearning.valueFunction.forwardPropogate(s2Input);
			
			//get reward + estimated future reward * discount
			double reward = 0;
			reward += DrivingQLearning.timeReward;
			reward += this.goalCollision(goals) == 1? DrivingQLearning.goalReward : 0;
			boolean wallCollision = false;
			if(this.wallCollision(walls)) {
				wallCollision = true;
				reward += DrivingQLearning.wallReward;
			}
			double maxEstimatedReward = Integer.MIN_VALUE;
			for(double d : s2Output) {
				maxEstimatedReward = Math.max(d, maxEstimatedReward);
			}
			reward += maxEstimatedReward * DrivingQLearning.discount;
			
			//put prev state + move + reward combo into memory for later
			this.states.add(s1Input);
			this.rewards.add(s1Output[s1Move] + (reward - s1Output[s1Move]) * DrivingQLearning.learningRate);	//adjusted reward after factoring in learning rate
			this.moves.add(s1Move);
			
			//reset and train function if hit wall or reached end of track
			if(wallCollision || this.whichCell >= goals.size() - 3) {
				this.dead = true;
				DrivingQLearning.trials ++;
			}
		}
		
		public void experienceReplay() {
			for(int i = 0; i < this.states.size(); i++) {
				double[] nextState = this.states.get(i);
				double[] nextOutput = DrivingQLearning.valueFunction.forwardPropogate(nextState);
				int nextMove = this.moves.get(i);
				double nextReward = this.rewards.get(i);
				nextOutput[nextMove] = nextReward;
				DrivingQLearning.valueFunction.backPropogate(nextState, nextOutput);
			}
		}
		
		int numSightLines = 31;
		double viewConeRad = Math.toRadians(240);
		
		//what the car can see
		public double[] getInput(ArrayList<ArrayList<double[]>> walls, Graphics g) {
			ArrayList<Double> ans = new ArrayList<Double>();
			
			//add normalized rotation, and velocity vector
			Vec2 normVel = new Vec2(this.vel);
			
			Vec2 normRot = new Vec2(0, 1);
			normRot.rotate(this.rot);
			
			ans.add(normVel.x);
			ans.add(normVel.y);
			ans.add(normRot.x);
			ans.add(normRot.y);
			
			double curRot = -viewConeRad / 2d;
			double increment = viewConeRad / (double) (numSightLines - 1);
			for(int i = 0; i < numSightLines; i++) {
				Vec2 sightVector = new Vec2(1, 0);
				sightVector.rotate(curRot + this.rot);
				sightVector.muli(100000);
				sightVector.addi(this.pos);
				
				double minDist = Integer.MAX_VALUE;
				Vec2 minDistVector = null;
				for(ArrayList<double[]> c : walls) {
					for(double[] d : c) {
						Vec2 intersect = MathTools.line_lineCollision(this.pos.x, this.pos.y, sightVector.x, sightVector.y, d[0], d[1], d[2], d[3]);
						if(intersect != null) {
							double dist = intersect.sub(this.pos).length();
							if(dist < minDist) {
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
			for(int i = 0; i < output.length; i++) {
				output[i] = ans.get(i);
			}
			
			return output;
		}

		public void draw(Graphics g) {
			double[][] drawnCorners = new double[4][2];
			for (int i = 0; i < 4; i++) {
				Vec2 c = new Vec2(corners[i][0], corners[i][1]);
				c.mul(this.size);
				c.rotate(rot);
				c.add(this.pos);
				drawnCorners[i][0] = c.x;
				drawnCorners[i][1] = c.y;
			}
			for (int i = 0; i < 4; i++) {
				g.drawLine((int) drawnCorners[i][0], (int) drawnCorners[i][1], (int) drawnCorners[(i + 1) % 4][0],
						(int) drawnCorners[(i + 1) % 4][1]);
			}
		}
		
		//checks if any part of the car collides with the line
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
			
			for(int i = 0; i < 4; i++) {
				if(MathTools.line_lineCollision(x1, y1, x2, y2, corners[i][0], corners[i][1], corners[(i + 1) % 4][0], corners[(i + 1) % 4][1]) != null) {
					return true;
				}
			}
			return false;
		}
		
		//if you have last touched the ith goal, then you only need to check cells i, and i + 1. 
		public boolean wallCollision(ArrayList<ArrayList<double[]>> walls) {
			for(int i = this.whichCell; i < Math.min(walls.size(), this.whichCell + 2); i++) {
				for(double[] d : walls.get(i)) {
					if(this.lineCollision(d[0], d[1], d[2], d[3])) {
						return true;
					}
				}
			}
			return false;
		}
		
		//if you have last touched the ith goal, then you only need to check if you're touching the i + 1th or i - 1th goal. 
		//returns true only if max cell has been incremented.
		public int goalCollision(ArrayList<ArrayList<double[]>> goals) {
			//previous goal
			if(this.whichCell != 0) {
				double[] nextGoal = goals.get(this.whichCell - 1).get(0);
				if(this.lineCollision(nextGoal[0], nextGoal[1], nextGoal[2], nextGoal[3])) {
					this.whichCell --;
					return -1;	//going backwards
				}
			}
			//next goal
			if(this.whichCell != goals.size() - 1) {
				double[] nextGoal = goals.get(this.whichCell + 1).get(0);
				if(this.lineCollision(nextGoal[0], nextGoal[1], nextGoal[2], nextGoal[3])) {
					this.whichCell ++;
					if(this.maxCell < this.whichCell) {
						this.maxCell = this.whichCell;
						return 1;	//going forwards
					}
				}
			}
			return 0;
		}

	}

}
