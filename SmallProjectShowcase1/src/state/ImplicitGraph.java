package state;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import main.MainPanel;
import util.Vector;

public class ImplicitGraph extends State{

	double maxX = MainPanel.WIDTH / 40;
	double minX = -MainPanel.WIDTH / 40;
	double maxY = MainPanel.HEIGHT / 40;
	double minY = -MainPanel.HEIGHT / 40;
	
	double squareSize = 0.1;
	
	boolean mousePressed = false;
	
	java.awt.Point prevMouse = new java.awt.Point(0, 0);
	
	public ImplicitGraph(StateManager gsm) {
		super(gsm);
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tick(Point mouse2) {
		//real dx and dy
		double dx = (double) (mouse2.x - prevMouse.x) * ((maxX - minX) / (double) MainPanel.WIDTH);
		double dy = (double) (mouse2.y - prevMouse.y) * ((maxY - minY) / (double) MainPanel.HEIGHT);
		
		if(mousePressed) {
			maxX -= dx;
			minX -= dx;
			maxY += dy;
			minY += dy;
		}
		
		prevMouse.x = mouse2.x;
		prevMouse.y = mouse2.y;
	}

	@Override
	public void draw(Graphics g) {
		
		double screenXSquareSize = (MainPanel.WIDTH / (maxX - minX)) * squareSize;
		double screenYSquareSize = (MainPanel.HEIGHT / (maxY - minY)) * squareSize;
		
		//determine what squares are visible
		int maxXSquare = (int) (maxX / squareSize) + 1;
		int minXSquare = (int) (minX / squareSize) - 1;
		int maxYSquare = (int) (maxY / squareSize) + 1;
		int minYSquare = (int) (minY / squareSize) - 1;
		
		//for each square on screen, sample the middle point of the square
		for(int i = minXSquare; i <= maxXSquare; i++) {
			for(int j = minYSquare; j <= maxYSquare; j++) {
				//real space coords for 
				double xReal = i * squareSize;
				double yReal = j * squareSize;
				
				//convert real square coordinates to screen space
				double xDiff = maxX - minX;
				double yDiff = maxY - minY;
				
				//remember to invert y screen space coords
				double xScreen = ((xReal - minX) / xDiff) * (double) MainPanel.WIDTH;
				double yScreen = (double) MainPanel.HEIGHT - (((yReal - minY) / yDiff) * (double) MainPanel.HEIGHT);
				
				//sample function
				double val = yReal * Math.sin(xReal) + xReal * Math.cos(yReal);
				
				if(val < 1) {
					g.fillRect((int) xScreen, (int) yScreen, (int) screenXSquareSize + 1, (int) screenYSquareSize + 1);
				}
				
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
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
		mousePressed = true;
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		mousePressed = false;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
