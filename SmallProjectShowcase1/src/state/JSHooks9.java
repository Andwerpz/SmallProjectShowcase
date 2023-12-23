package state;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import input.Button;
import input.InputManager;
import main.MainPanel;
import myutils.graphics.GraphicsTools;
import myutils.math.Vec2;
import myutils.misc.Pair;

public class JSHooks9 extends State {

	// -- HEURISTICS --
	// Implemented:
	// 1. hook 2 cannot have 3 assigned to it. If it does, then there are no valid digit placements. 

	// Not Implemented:
	// 2. a 5 has to appear in row and column 1
	// 3. a 7 has to appear in row and column 8

	public static final int GRID_SIZE = 6;
	public static final int CELL_SIZE_PX = 50;

	public static final int BOARD_SIZE_PX = GRID_SIZE * CELL_SIZE_PX;

	public static Color[] p_blue = { new Color(0x012a4a), new Color(0x013a63), new Color(0x01497c), new Color(0x014f86), new Color(0x2a6f97), new Color(0x2c7da0), new Color(0x468faf), new Color(0x61a5c2), new Color(0x89c2d9) };
	public static Color[] p_gray = { new Color(0x212529), new Color(0x343a40), new Color(0x495057), new Color(0x6c757d), new Color(0xadb5bd), new Color(0xced4da), new Color(0xdee2e6), new Color(0xe9ecef), new Color(0xf8f9fa) };

	public static Color[] p_rainbow_light = { new Color(0xffadad), new Color(0xffd6a5), new Color(0xfdffb6), new Color(0xcaffbf), new Color(0x9bf6ff), new Color(0xa0c4ff), new Color(0xbdb2ff), new Color(0xffc6ff), new Color(0xfffffc) };
	public static Color[] p_gradient_blue_purple = { new Color(0x9eddff), new Color(0xa0cbff), new Color(0xa0c4ff), new Color(0xc8b6ff), new Color(0xd8bbff), new Color(0xe7bfff), new Color(0xffc6ff), new Color(0xffceff), new Color(0xffd9ff) };

	public static Color[] hookColor = p_gradient_blue_purple;

	public static Color gridColor = Color.BLACK;
	public static Stroke gridlineStroke = new BasicStroke(1);
	public static Stroke hookStroke = new BasicStroke(3);
	public static Stroke borderStroke = new BasicStroke(5);

	private int[] rgcd = { 55, 1, 6, 1, 24, 3, 6, 7, 2 };
	private int[] cgcd = { 5, 1, 6, 1, 8, 1, 22, 7, 8 };

	private ArrayList<int[]> hookPlacements;
	private ArrayList<int[]> hookValues;

	private Board board;

	private InputManager im;

	public JSHooks9(StateManager gsm) {
		super(gsm);
	}

	@Override
	public void init() {
		this.generateAllHookPlacements();
		this.generateAllHookValues();

		this.board = new Board();
		this.board.setTestcase(this.generateRandomTestcase());

		this.im = new InputManager();
		this.im.addInput(new Button(10, 10, 100, 25, "New Testcase", "btn_new_testcase"));
		this.im.addInput(new Button(10, 40, 100, 25, "Next Grid", "btn_next_digit_placement"));
	}

	private void generateAllHookPlacements() {
		this.hookPlacements = new ArrayList<>();
		int[] a = new int[JSHooks9.GRID_SIZE];
		this.generateAllHookPlacementsHelper(0, a, this.hookPlacements);
		System.out.println("NR HOOK PLACEMENT CONFIGURATIONS : " + this.hookPlacements.size());
	}

	private void generateAllHookPlacementsHelper(int ind, int[] a, ArrayList<int[]> ans) {
		if (ind == JSHooks9.GRID_SIZE) {
			int[] nans = new int[JSHooks9.GRID_SIZE];
			for (int i = 0; i < a.length; i++) {
				nans[i] = a[i];
			}
			ans.add(nans);
			return;
		}
		for (int i = 0; i < 4; i++) {
			a[ind] = i;
			this.generateAllHookPlacementsHelper(ind + 1, a, ans);
		}
	}

	private void generateAllHookValues() {
		this.hookValues = new ArrayList<>();
		int[] v = new int[JSHooks9.GRID_SIZE];
		for (int i = 0; i < v.length; i++) {
			v[i] = -1;
		}
		this.generateAllHookValuesHelper(0, v, this.hookValues);
		System.out.println("NR HOOK VALUE CONFIGURATIONS : " + this.hookValues.size());
	}

	private void generateAllHookValuesHelper(int ind, int[] v, ArrayList<int[]> ans) {
		if (ind == JSHooks9.GRID_SIZE) {
			int[] nans = new int[JSHooks9.GRID_SIZE];
			for (int i = 0; i < v.length; i++) {
				nans[v[i]] = i + 1;
			}
			for (int i = 0; i < nans.length / 2; i++) {
				int tmp = nans[i];
				nans[i] = nans[nans.length - 1 - i];
				nans[nans.length - 1 - i] = tmp;
			}
			ans.add(nans);
			return;
		}
		int curCells = ind * 2 + 1;
		for (int i = 0; i < Math.min(v.length, curCells); i++) {

			//Heuristic 1
			if (ind == 1 && i == 2) {
				continue;
			}

			if (v[i] == -1) {
				v[i] = ind;
				this.generateAllHookValuesHelper(ind + 1, v, ans);
				v[i] = -1;
			}
		}
	}

	private Testcase generateRandomTestcase() {
		int[] hookPlacement = this.hookPlacements.get((int) (Math.random() * this.hookPlacements.size()));
		int[] hookValues = this.hookValues.get((int) (Math.random() * this.hookValues.size()));

		Testcase t = new Testcase(hookPlacement, hookValues);

		return t;
	}

	@Override
	public void tick(Point mouse2) {
		this.im.tick(mouse2);
	}

	@Override
	public void draw(Graphics g) {
		this.im.draw(g);

		GraphicsTools.enableTextAntialiasing(g);

		Graphics boardGraphics = g.create();

		Vec2 boardTranslate = new Vec2(0);
		boardTranslate.addi(new Vec2(-BOARD_SIZE_PX / 2));
		boardTranslate.addi(new Vec2(MainPanel.WIDTH / 2, MainPanel.HEIGHT / 2));

		boardGraphics.translate((int) boardTranslate.x, (int) boardTranslate.y);

		this.board.draw(boardGraphics);
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		this.im.keyPressed(arg0);
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		this.im.keyReleased(arg0);
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		this.im.keyTyped(arg0);
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		switch (this.im.mouseClicked(arg0)) {
		case "btn_new_testcase":
			this.board.setTestcase(this.generateRandomTestcase());
			break;

		case "btn_next_digit_placement":
			this.board.testcase.nextGrid();
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
		this.im.mousePressed(arg0);
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		this.im.mouseReleased(arg0);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		// TODO Auto-generated method stub

	}

	class Board {

		public Testcase testcase;

		public Board() {
			this.testcase = null;
		}

		public void draw(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;

			if (this.testcase != null) {
				//hook colors
				{
					g2.setComposite(GraphicsTools.makeComposite(1f));
					int minX = 0;
					int minY = 0;
					int curSize = JSHooks9.BOARD_SIZE_PX;

					for (int i = 0; i < JSHooks9.GRID_SIZE; i++) {
						int nextRot = this.testcase.hookPlacement[i];
						Color hookColor = JSHooks9.hookColor[i];
						g2.setColor(hookColor);

						switch (nextRot) {
						case 0:
							g2.fillRect(minX, minY, curSize, JSHooks9.CELL_SIZE_PX);
							g2.fillRect(minX, minY, JSHooks9.CELL_SIZE_PX, curSize);
							minX += JSHooks9.CELL_SIZE_PX;
							minY += JSHooks9.CELL_SIZE_PX;
							break;

						case 1:
							g2.fillRect(minX, minY, curSize, JSHooks9.CELL_SIZE_PX);
							g2.fillRect(minX + curSize - JSHooks9.CELL_SIZE_PX, minY, JSHooks9.CELL_SIZE_PX, curSize);
							minY += JSHooks9.CELL_SIZE_PX;
							break;

						case 2:
							g2.fillRect(minX, minY + curSize - JSHooks9.CELL_SIZE_PX, curSize, JSHooks9.CELL_SIZE_PX);
							g2.fillRect(minX + curSize - JSHooks9.CELL_SIZE_PX, minY, JSHooks9.CELL_SIZE_PX, curSize);
							break;

						case 3:
							g2.fillRect(minX, minY + curSize - JSHooks9.CELL_SIZE_PX, curSize, JSHooks9.CELL_SIZE_PX);
							g2.fillRect(minX, minY, JSHooks9.CELL_SIZE_PX, curSize);
							minX += JSHooks9.CELL_SIZE_PX;
							break;
						}

						curSize -= JSHooks9.CELL_SIZE_PX;
					}
					g2.setComposite(GraphicsTools.makeComposite(1f));
				}

				//hook outlines
				{
					g2.setColor(JSHooks9.gridColor);
					g2.setStroke(JSHooks9.hookStroke);

					int minX = 0;
					int minY = 0;
					int curSize = JSHooks9.BOARD_SIZE_PX;

					for (int i = 0; i < JSHooks9.GRID_SIZE - 1; i++) {
						int nextRot = this.testcase.hookPlacement[i];

						curSize -= JSHooks9.CELL_SIZE_PX;

						switch (nextRot) {
						case 0:
							minX += JSHooks9.CELL_SIZE_PX;
							minY += JSHooks9.CELL_SIZE_PX;
							g2.drawLine(minX, minY, minX + curSize, minY);
							g2.drawLine(minX, minY, minX, minY + curSize);

							break;

						case 1:
							minY += JSHooks9.CELL_SIZE_PX;
							g2.drawLine(minX, minY, minX + curSize, minY);
							g2.drawLine(minX + curSize, minY, minX + curSize, minY + curSize);
							break;

						case 2:
							g2.drawLine(minX, minY + curSize, minX + curSize, minY + curSize);
							g2.drawLine(minX + curSize, minY, minX + curSize, minY + curSize);
							break;

						case 3:
							minX += JSHooks9.CELL_SIZE_PX;
							g2.drawLine(minX, minY + curSize, minX + curSize, minY + curSize);
							g2.drawLine(minX, minY, minX, minY + curSize);
							break;
						}
					}
				}

				//filled grid cell backgrounds
				g2.setComposite(GraphicsTools.makeComposite(0.2f));
				for (int i = 0; i < JSHooks9.GRID_SIZE; i++) {
					for (int j = 0; j < JSHooks9.GRID_SIZE; j++) {
						if (this.testcase.grid[i][j] == 0) {
							continue;
						}
						g2.fillRect(j * JSHooks9.CELL_SIZE_PX, i * JSHooks9.CELL_SIZE_PX, JSHooks9.CELL_SIZE_PX, JSHooks9.CELL_SIZE_PX);
					}
				}

				//grid cell values
				g2.setComposite(GraphicsTools.makeComposite(1f));
				Font cellValueFont = new Font("Dialogue", Font.PLAIN, 24);
				//g2.setFont(cellValueFont);
				for (int i = 0; i < JSHooks9.GRID_SIZE; i++) {
					for (int j = 0; j < JSHooks9.GRID_SIZE; j++) {
						if (this.testcase.grid[i][j] == 0) {
							continue;
						}
						GraphicsTools.drawCenteredString(j * JSHooks9.CELL_SIZE_PX + JSHooks9.CELL_SIZE_PX / 2, i * JSHooks9.CELL_SIZE_PX + JSHooks9.CELL_SIZE_PX * 12 / 13, g2, cellValueFont, this.testcase.grid[i][j] + "", Color.BLACK);
					}
				}

				//row and col gcds.

			}

			g2.setColor(JSHooks9.gridColor);

//			//gridlines
//			g2.setStroke(JSHooks9.gridlineStroke);
//			for (int i = 0; i <= JSHooks9.GRID_SIZE; i++) {
//				for (int j = 0; j <= JSHooks9.GRID_SIZE; j++) {
//					g2.drawLine(i * CELL_SIZE_PX, 0, i * CELL_SIZE_PX, BOARD_SIZE_PX);
//					g2.drawLine(0, i * CELL_SIZE_PX, BOARD_SIZE_PX, i * CELL_SIZE_PX);
//				}
//			}

			//border
			g2.setStroke(JSHooks9.borderStroke);
			g2.drawLine(0, 0, 0, BOARD_SIZE_PX);
			g2.drawLine(0, 0, BOARD_SIZE_PX, 0);
			g2.drawLine(0, BOARD_SIZE_PX, BOARD_SIZE_PX, BOARD_SIZE_PX);
			g2.drawLine(BOARD_SIZE_PX, 0, BOARD_SIZE_PX, BOARD_SIZE_PX);
		}

		public void setTestcase(Testcase t) {
			this.testcase = t;
		}
	}
	
	class BooleanGrid extends Object {
		
		public boolean[] grid;
		
		public BooleanGrid() {
			this.grid = new boolean[JSHooks9.GRID_SIZE * JSHooks9.GRID_SIZE];
		}
		
		public BooleanGrid(BooleanGrid g) {
			this.grid = new boolean[JSHooks9.GRID_SIZE * JSHooks9.GRID_SIZE];
			for(int i = 0; i < g.grid.length; i++) {
				this.grid[i] = g.grid[i];
			}
		}
		
		public boolean get(int r, int c) {
			return this.grid[r * JSHooks9.GRID_SIZE + c];
		}
		
		public void set(int r, int c, boolean val) {
			this.grid[r * JSHooks9.GRID_SIZE + c] = val;
		}
		
		
		public boolean equals(Object b) {
			if(b == null) {
				return false;
			}
			if(b == this) {
				return true;
			}
			if(!(b instanceof BooleanGrid)) {
				return false;
			}
			
			BooleanGrid g = (BooleanGrid) b;
			
			if(g.grid.length != this.grid.length) {
				return false;
			}
			for(int i = 0; i < this.grid.length; i++) {
				if(this.grid[i] != g.grid[i]) {
					return false;
				}
			}
			return true;
		}
		
		public int hashCode() {
			return Arrays.hashCode(this.grid);
		}
		
		public String toString() {
			String ans = "";
			for(int i = 0; i < JSHooks9.GRID_SIZE; i++) {
				for(int j = 0; j < JSHooks9.GRID_SIZE; j++) {
					ans += (this.get(i, j)? 1 : 0) + " ";
				}
				ans += "\n";
			}
			return ans;
		}
		
	}

	class Testcase {

		//value in range [0, 4). 
		//top left, top right, bottom right, bottom left
		public int[] hookPlacement;
		public int[] hookValues;

		public int[][] grid;

		public int curGridInd = -1;
		public ArrayList<int[][]> allGrids;

		public Testcase(int[] hookPlacement, int[] hookValues) {
			this.hookPlacement = new int[JSHooks9.GRID_SIZE];
			this.hookValues = new int[JSHooks9.GRID_SIZE];
			for (int i = 0; i < JSHooks9.GRID_SIZE; i++) {
				this.hookPlacement[i] = hookPlacement[i];
				this.hookValues[i] = hookValues[i];
			}

			this.grid = new int[JSHooks9.GRID_SIZE][JSHooks9.GRID_SIZE];

			System.out.println("TESTCASE PLACEMENT / VALUES :");
			for (int i = 0; i < JSHooks9.GRID_SIZE; i++) {
				System.out.print(this.hookPlacement[i] + " ");
			}
			System.out.println();
			for (int i = 0; i < JSHooks9.GRID_SIZE; i++) {
				System.out.print(this.hookValues[i] + " ");
			}
			System.out.println();
			System.out.println();

			this.generateAllGrids();

			if (this.allGrids.size() != 0) {
				this.curGridInd = 0;
				this.grid = this.allGrids.get(this.curGridInd);
			}
		}

		public void nextGrid() {
			if (this.curGridInd == -1) {
				return;
			}
			this.curGridInd = (this.curGridInd + 1) % this.allGrids.size();
			this.grid = this.allGrids.get(this.curGridInd);
		}

		private void generateAllGrids() {
			this.allGrids = new ArrayList<>();
			int[][] gridHookValues = new int[JSHooks9.GRID_SIZE][JSHooks9.GRID_SIZE];
			int curHookSize = JSHooks9.GRID_SIZE;
			int minR = 0;
			int minC = 0;
			for (int i = 0; i < JSHooks9.GRID_SIZE; i++) {
				for (int j = 0; j < curHookSize; j++) {
					switch (this.hookPlacement[i]) {
					case 0:
						gridHookValues[minR][minC + j] = this.hookValues[i];
						gridHookValues[minR + j][minC] = this.hookValues[i];
						break;

					case 1:
						gridHookValues[minR][minC + j] = this.hookValues[i];
						gridHookValues[minR + j][minC + curHookSize - 1] = this.hookValues[i];
						break;

					case 2:
						gridHookValues[minR + curHookSize - 1][minC + j] = this.hookValues[i];
						gridHookValues[minR + j][minC + curHookSize - 1] = this.hookValues[i];
						break;

					case 3:
						gridHookValues[minR + curHookSize - 1][minC + j] = this.hookValues[i];
						gridHookValues[minR + j][minC] = this.hookValues[i];
						break;
					}
				}

				if (i == JSHooks9.GRID_SIZE - 1) {
					break;
				}
				switch (this.hookPlacement[i]) {
				case 0:
					minR++;
					minC++;
					break;

				case 1:
					minR++;
					break;

				case 2:
					break;

				case 3:
					minC++;
					break;
				}
				curHookSize--;
			}

			System.out.println("GRID HOOK VALUES : ");
			for (int i = 0; i < gridHookValues.length; i++) {
				for (int j = 0; j < gridHookValues[0].length; j++) {
					System.out.print(gridHookValues[i][j] + " ");
				}
				System.out.println();
			}
			System.out.println();
			
			BooleanGrid a = new BooleanGrid();
			a.set(minR, minC, true);
			int[] valueCnt = new int[JSHooks9.GRID_SIZE];
			valueCnt[0] = 1;
			//this.generateAllGridsHelper(minR, minC, valueCnt, a, gridHookValues, this.allGrids);
			HashSet<BooleanGrid> v = new HashSet<>();
			this.generateAllGridsHelper2(a, gridHookValues, valueCnt, v, this.allGrids);

			System.out.println("NUM DIGIT PLACEMENTS : " + this.allGrids.size());
		}

		private int[] dr = { -1, 1, 0, 0 };
		private int[] dc = { 0, 0, -1, 1 };
		
		private BooleanGrid generateAllGridsMakeMove(BooleanGrid a, int r, int c) {
			BooleanGrid ret = new BooleanGrid(a);
			ret.set(r, c, true);
			return ret;
		}
		
		private boolean generateAllGridsIsMoveValid(BooleanGrid a, int r, int c, int[][] gridHookValues, int[] valueCnt) {
			//is this cell already taken
			if(a.get(r, c)) {
				return false;
			}
			
			//is this adjacent to something
			boolean isAdjacent = false;
			for(int i = 0; i < 4; i++) {
				int nr = r + dr[i];
				int nc = c + dc[i];
				if(nr < 0 || nc < 0 || nr >= JSHooks9.GRID_SIZE || nc >= JSHooks9.GRID_SIZE) {
					continue;
				}
				if(a.get(nr, nc)) {
					isAdjacent = true;
					break;
				}
			}
			if(!isAdjacent) {
				return false;
			}
			
			//check if will violate 2x2 rule
			if (r != 0 && c != 0) {
				if (a.get(r, c - 1) && a.get(r - 1, c) && a.get(r - 1, c - 1)) {
					return false;
				}
			}
			if (r != 0 && c != JSHooks9.GRID_SIZE - 1) {
				if (a.get(r, c + 1) && a.get(r - 1, c) && a.get(r - 1, c + 1)) {
					return false;
				}
			}
			if (r != JSHooks9.GRID_SIZE - 1 && c != 0) {
				if (a.get(r, c - 1) && a.get(r + 1, c) && a.get(r + 1, c - 1)) {
					return false;
				}
			}
			if (r != JSHooks9.GRID_SIZE - 1 && c != JSHooks9.GRID_SIZE - 1) {
				if (a.get(r, c + 1) && a.get(r + 1, c) && a.get(r + 1, c + 1)) {
					return false;
				}
			}
			
			//value limit rule
			if(valueCnt[gridHookValues[r][c] - 1] == gridHookValues[r][c]) {
				return false;
			}
			
			return true;
		}
		
		private void generateAllGridsHelper2(BooleanGrid a, int[][] gridHookValues, int[] valueCnt, HashSet<BooleanGrid> v, ArrayList<int[][]> ans) {
			if(v.contains(a)) {
				return;
			}
			v.add(a);
			
			//check the number of values
			boolean done = true;
			for (int i = 0; i < valueCnt.length; i++) {
				if (valueCnt[i] != i + 1) {
					done = false;
				}
			}

			//save answer
			if (done) {
				System.out.println("FOUND ANSWER");
				int[][] nans = new int[JSHooks9.GRID_SIZE][JSHooks9.GRID_SIZE];
				for (int i = 0; i < JSHooks9.GRID_SIZE; i++) {
					for (int j = 0; j < JSHooks9.GRID_SIZE; j++) {
						nans[i][j] = a.get(i, j) ? gridHookValues[i][j] : 0;
					}
				}
				ans.add(nans);
				return;
			}
			
			//propogate further
			for(int i = 0; i < JSHooks9.GRID_SIZE; i++) {
				for(int j = 0; j < JSHooks9.GRID_SIZE; j++) {
					if(!this.generateAllGridsIsMoveValid(a, i, j, gridHookValues, valueCnt)) {
						continue;
					}
					
					valueCnt[gridHookValues[i][j] - 1] ++;
					this.generateAllGridsHelper2(this.generateAllGridsMakeMove(a, i, j), gridHookValues, valueCnt, v, ans);
					valueCnt[gridHookValues[i][j] - 1] --;
				}
			}
		}

		private void generateAllGridsHelper(int r, int c, int[] valueCnt, boolean[][] a, int[][] gridHookValues, ArrayList<int[][]> ans) {
			//check if previous placement was valid
			if (r != 0 && c != 0) {
				if (a[r][c] && a[r][c - 1] && a[r - 1][c] && a[r - 1][c - 1]) {
					return;
				}
			}
			if (r != 0 && c != JSHooks9.GRID_SIZE - 1) {
				if (a[r][c] && a[r][c + 1] && a[r - 1][c] && a[r - 1][c + 1]) {
					return;
				}
			}
			if (r != JSHooks9.GRID_SIZE - 1 && c != 0) {
				if (a[r][c] && a[r][c - 1] && a[r + 1][c] && a[r + 1][c - 1]) {
					return;
				}
			}
			if (r != JSHooks9.GRID_SIZE - 1 && c != JSHooks9.GRID_SIZE - 1) {
				if (a[r][c] && a[r][c + 1] && a[r + 1][c] && a[r + 1][c + 1]) {
					return;
				}
			}

			//check the number of values
			boolean invalidValueCnt = false;
			boolean done = true;
			for (int i = 0; i < valueCnt.length; i++) {
				if (valueCnt[i] > i + 1) {
					invalidValueCnt = true;
				}
				if (valueCnt[i] != i + 1) {
					done = false;
				}
			}
			if (invalidValueCnt) {
				return;
			}

			//save answer
			if (done) {
				int[][] nans = new int[JSHooks9.GRID_SIZE][JSHooks9.GRID_SIZE];
				for (int i = 0; i < a.length; i++) {
					for (int j = 0; j < a[0].length; j++) {
						nans[i][j] = a[i][j] ? gridHookValues[i][j] : 0;
					}
				}
				ans.add(nans);
				return;
			}

			//propogate further
			for (int i = 0; i < 4; i++) {
				int nr = r + dr[i];
				int nc = c + dc[i];
				if (nr < 0 || nc < 0 || nr >= JSHooks9.GRID_SIZE || nc >= JSHooks9.GRID_SIZE) {
					continue;
				}
				if (a[nr][nc]) {
					continue;
				}
				int hookValue = gridHookValues[nr][nc];

				a[nr][nc] = true;
				valueCnt[hookValue - 1]++;
				generateAllGridsHelper(nr, nc, valueCnt, a, gridHookValues, ans);
				a[nr][nc] = false;
				valueCnt[hookValue - 1]--;
			}
		}

	}

}
