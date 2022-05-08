package state;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Stack;

import input.Button;
import input.InputManager;
import input.ToggleButton;
import main.MainPanel;
import util.GraphicsTools;

public class TicTacToeQLearning extends State{
	
	InputManager im;
	
	HashMap<String, Double> valueMap;	//1 = good for X, -1 = good for O
	double discount = 0.8;
	double exploitChance = 0.9;	//chance to just choose best value move when training
	double learningRate = 0.01;
	int gamesPerTick = 1000;
	
	char whichPlayer = 'X';
	
	boolean playerMove = false;
	int wherePlayerMove = -1;
	
	String board;
	
	int boardCellSize = 150;
	
	int boardX = MainPanel.WIDTH / 2 - (boardCellSize * 3) / 2;
	int boardY = MainPanel.HEIGHT / 2 - (boardCellSize * 3) / 2;
	
	boolean training = false;
	int gamesPlayed = 0;
	int xWins = 0;
	int oWins = 0;
	int ties = 0;
	Stack<String> boardSequence;

	public TicTacToeQLearning(StateManager gsm) {
		super(gsm);
		
		im = new InputManager();
		im.addInput(new ToggleButton(650, 10, 100, 25, "Train", "tb_train"));
		im.addInput(new Button(650, 40, 100, 25, "CPU Move", "btn_cpu_move"));
		
		board = ".........";
		boardSequence = new Stack<String>();
		
		boardSequence.push(board);
		valueMap = new HashMap<>();
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tick(Point mouse2) {
		im.tick(mouse2);
		
		this.training = im.getToggled("tb_train");
		
		int nextMove = -1;
		
		if(training) {
			for(int g = 0; g < this.gamesPerTick; g++) {
				this.train();
			}
		}
		else {
			if(playerMove) {
				playerMove = false;
				nextMove = this.wherePlayerMove;
			}
		}
		
		if(nextMove != -1) {
			this.board = performMove(this.board, nextMove, this.whichPlayer);
			boardSequence.push(new String(board));
			this.whichPlayer = this.whichPlayer == 'X'? 'O' : 'X';
		}
		
		char whoWon = checkForWin(this.board);
		if(whoWon != '.' || this.checkForTie(board)) {
			
			switch(whoWon) {
			case 'X':
				xWins ++;
				break;
				
			case 'O':
				oWins ++;
				break;
				
			case '.':
				ties ++;
				break;
			}
			
			boardSequence.clear();
			
			this.whichPlayer = 'X';
			board = ".........";
			boardSequence.push(board);
			gamesPlayed ++;
		}
		
	}
	
	public void train() {
		while(!(checkForWin(this.board) != '.' || checkForTie(this.board))) {
			//generate all possible moves, and look up their values
			int nextMove = -1;
			int bestMove = -1;
			double bestValue = whichPlayer == 'X'? Integer.MIN_VALUE : Integer.MAX_VALUE;
			ArrayList<Integer> moves = new ArrayList<>();
			for(int i = 0; i < 9; i++) {
				if(board.charAt(i) == '.') {
					String nextBoard = performMove(this.board, i, this.whichPlayer);
					double nextVal = valueMap.getOrDefault(nextBoard, 0d);
					if((whichPlayer == 'X' && nextVal > bestValue) || (whichPlayer == 'O' && nextVal < bestValue)) {
						bestValue = nextVal;
						bestMove = i;
					}
					moves.add(i);
				}
			}
			
			Collections.shuffle(moves);
			
			if(Math.random() < exploitChance) {	//pick best move
				nextMove = bestMove;
			}
			else {	//pick random move
				nextMove = moves.get((int) (Math.random()) * moves.size());
			}
			
			if(nextMove != -1) {
				this.board = performMove(this.board, nextMove, this.whichPlayer);
				boardSequence.push(new String(board));
				this.whichPlayer = this.whichPlayer == 'X'? 'O' : 'X';
			}
		}
		
		char whoWon = checkForWin(this.board);
		if(whoWon != '.' || this.checkForTie(board)) {
			//System.out.println(whoWon + " won");
			switch(whoWon) {
			case 'X':
				xWins ++;
				break;
				
			case 'O':
				oWins ++;
				break;
				
			case '.':
				ties ++;
				break;
			}
			
			//upd value map, and reset board
			double curVal = whoWon == '.'? 0 : (whoWon == 'X'? 1 : -1);
			//System.out.println(curVal);
			
			valueMap.put(board, curVal);
			
			double prevVal = curVal;
			boardSequence.pop();
			while(boardSequence.size() != 0) {
				String nextB = boardSequence.pop();
				valueMap.put(nextB, valueMap.getOrDefault(nextB, 0d) + (prevVal - valueMap.getOrDefault(nextB, 0d)) * learningRate);	//average the next state's val with the current
				prevVal = valueMap.get(nextB);	//save the updated current value
				//System.out.println(prevVal);
			}
			
			boardSequence.clear();
			
			this.whichPlayer = 'X';
			board = ".........";
			boardSequence.push(board);
			gamesPlayed ++;
		}
	}

	@Override
	public void draw(Graphics g) {
		GraphicsTools.enableTextAntialiasing(g);
		//draw board
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				char next = this.board.charAt(i * 3 + j);
				int x = boardX + j * boardCellSize;
				int y = boardY + i * boardCellSize;
				g.drawRect(x, y, boardCellSize, boardCellSize);
				String piece = "";
				if(next == 'X') {
					piece = "X";
				}
				else if(next == 'O'){
					piece = "O";
				}
				
				int fontSize = 50;
				Font font = new Font("Dialogue", Font.BOLD, fontSize);
				int stringWidth = GraphicsTools.calculateTextWidth(piece, font);
				g.setFont(font);
				
				g.drawString(piece, (x + (boardCellSize - stringWidth) / 2), (y + (boardCellSize + fontSize) / 2));
			}
		}
		
		im.draw(g);
		
		g.setFont(new Font("Dialogue", 0, 12));
		g.drawString("Games Played: " + this.gamesPlayed, 10, 20);
		g.drawString("Current Value: " + this.valueMap.getOrDefault(this.board, 0d), 10, 40);
		g.drawString("X Wins: " + xWins, 10, 60);
		g.drawString("O Wins: " + oWins, 10, 80);
		g.drawString("Ties: " + ties, 10, 100);
	}
	
	//gets best valued move, and plays it
	public void cpuMove() {
		int bestMove = -1;
		double bestValue = whichPlayer == 'X'? Integer.MIN_VALUE : Integer.MAX_VALUE;
		for(int i = 0; i < 9; i++) {
			if(board.charAt(i) == '.') {
				String nextBoard = performMove(this.board, i, this.whichPlayer);
				double nextVal = valueMap.getOrDefault(nextBoard, 0d);
				if((whichPlayer == 'X' && nextVal > bestValue) || (whichPlayer == 'O' && nextVal < bestValue)) {
					bestValue = nextVal;
					bestMove = i;
				}
			}
		}
		
		this.playerMove = true;
		this.wherePlayerMove = bestMove;
	}
	
	//returns new board after performing the move at index where
	public String performMove(String board, int where, char whichPlayer) {
		return board.substring(0, where) + whichPlayer + board.substring(where + 1);
	}
	
	//returns 'X' or 'O' if win, '.' if otherwise
	public char checkForWin(String board) {
		//convert to 2d char array
		char[][] b = new char[3][3];
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				b[i][j] = board.charAt(i * 3 + j);
			}
		}
		
		//check for win
		for(int i = 0; i < 3; i++) {
			if(b[i][0] != '.' && b[i][0] == b[i][1] && b[i][1] == b[i][2]) {
				return b[i][0];
			}
			else if(b[0][i] != '.' && b[0][i] == b[1][i] && b[1][i] == b[2][i]) {
				return b[0][i];
			}
		}
		if(b[0][0] != '.' && b[0][0] == b[1][1] && b[1][1] == b[2][2]) {
			return b[0][0];
		}
		else if(b[0][2] != '.' && b[0][2] == b[1][1] && b[1][1] == b[2][0]) {
			return b[0][2];
		}
		return '.';
	}
	
	public boolean checkForTie(String board) {
		for(int i = 0; i < board.length(); i++) {
			char c = board.charAt(i);
			if(c == '.') {
				return false;
			}
		}
		return true;
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		im.keyPressed(arg0);
		if(arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
			this.exit();
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		im.keyReleased(arg0);
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		String which = im.mouseClicked(arg0);
		which = which == null? "" : which;
		
		switch(which) {
		case "btn_cpu_move":
			this.cpuMove();
			break;
		}
		
		int r = (arg0.getY() - this.boardY) / this.boardCellSize;
		int c = (arg0.getX() - this.boardX) / this.boardCellSize;
		
		if(r >= 0 && r < 3 && c >= 0 && c < 3) {
			int where = r * 3 + c;
			if(this.board.charAt(where) == '.') {
				this.playerMove = true;
				this.wherePlayerMove = where;
			}
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
		im.mousePressed(arg0);
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		im.mouseReleased(arg0);
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
