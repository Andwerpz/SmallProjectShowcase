package state;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import main.MainPanel;
import util.GraphicsTools;
import util.Vector;

public class ImplicitGraph extends State{

	double maxX = MainPanel.WIDTH / 40;
	double minX = -MainPanel.WIDTH / 40;
	double maxY = MainPanel.HEIGHT / 40;
	double minY = -MainPanel.HEIGHT / 40;
	
	double squareSize = .3;	//real space size
	
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
		
		//GraphicsTools.enableAntialiasing(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(2, BasicStroke.JOIN_ROUND, BasicStroke.CAP_ROUND));
		
		//for each square on screen, sample the middle point of the square
		for(int i = minXSquare; i <= maxXSquare; i++) {
			for(int j = minYSquare; j <= maxYSquare; j++) {
				//real space coords for 
				double x1Real = i * squareSize;
				double y1Real = j * squareSize;
				
				double x2Real = (i + 1) * squareSize;
				double y2Real = (j + 1) * squareSize;
				
				//convert real square coordinates to screen space
				double xDiff = maxX - minX;
				double yDiff = maxY - minY;
				
				//remember to invert y screen space coords
				double x1Screen = ((x1Real - minX) / xDiff) * (double) MainPanel.WIDTH;
				double y1Screen = (double) MainPanel.HEIGHT - (((y1Real - minY) / yDiff) * (double) MainPanel.HEIGHT);
				
				double x2Screen = ((x2Real - minX) / xDiff) * (double) MainPanel.WIDTH;
				double y2Screen = (double) MainPanel.HEIGHT - (((y2Real - minY) / yDiff) * (double) MainPanel.HEIGHT);
				
				x1Screen = (int) x1Screen;
				y1Screen = (int) y1Screen;
				
				x2Screen = (int) x2Screen;
				y2Screen = (int) y2Screen;
				
				//sample function
				double val = y1Real * Math.sin(x1Real) + x1Real * Math.cos(y1Real);
				
				boolean[] code = new boolean[4];
				code[0] = testFunc(y1Real, x1Real);
				code[1] = testFunc(y1Real, x2Real);
				code[2] = testFunc(y2Real, x2Real);
				code[3] = testFunc(y2Real, x1Real);
				
				drawSquare(g, code, (int) x1Screen, (int) y1Screen, (int) x2Screen, (int) y2Screen);
				
				
				//g2.setComposite(GraphicsTools.makeComposite(0.5));
				
				if(val < 1) {
					
					//g.fillRect((int) x1Screen, (int) y1Screen, (int) (Math.abs(x1Screen - x2Screen)), (int) (Math.abs(y1Screen - y2Screen)));
				}
				
			}
		}
	}
	
	public boolean testFunc(double x, double y) {
		double val = (y * Math.tan(x) + x * Math.sin(y));
		return val < 1;
	}
	
	//marching squares
	//top left and bottom right corner
	//code:
	//0 1
	//3 2
	public void drawSquare(Graphics g, boolean[] code, int x1, int y1, int x2, int y2) {
		
		g.setColor(Color.BLACK);
		Graphics2D g2 = (Graphics2D) g;
		g2.setComposite(GraphicsTools.makeComposite(1));
		
		int[][] poly = new int[0][0];	//x coords, y coords
		
		if(code[0] && code[1] && code[2] && code[3]) {
			//System.out.println("YES");
			poly = new int[2][];
			poly[0] = new int[] {x1, x2, x2, x1};
			poly[1] = new int[] {y1, y1, y2, y2};
		}
		else if(!code[0] && code[1] && code[2] && code[3]) {
			poly = new int[2][];
			poly[0] = new int[] {x1, (x1 + x2) / 2, x2, x2, x1};
			poly[1] = new int[] {(y1 + y2) / 2, y1, y1, y2, y2};
			
			g.drawLine(x1, (y1 + y2) / 2, (x1 + x2) / 2, y1);
		}
		else if(code[0] && !code[1] && code[2] && code[3]) {
			poly = new int[2][];
			poly[0] = new int[] {x1, (x1 + x2) / 2, x2, x2, x1};
			poly[1] = new int[] {y1, y1, (y1 + y2) / 2, y2, y2};
			
			g.drawLine((x1 + x2) / 2, y1, x2, (y1 + y2) / 2);
		}
		else if(code[0] && code[1] && !code[2] && code[3]) {
			poly = new int[2][];
			poly[0] = new int[] {x1, x2, x2, (x1 + x2) / 2, x1};
			poly[1] = new int[] {y1, y1, (y1 + y2) / 2, y2, y2};
			
			g.drawLine(x2, (y1 + y2) / 2, (x1 + x2) / 2, y2);
		}
		else if(code[0] && code[1] && code[2] && !code[3]) {
			poly = new int[2][];
			poly[0] = new int[] {x1, x2, x2, (x1 + x2) / 2, x1};
			poly[1] = new int[] {y1, y1, y2, y2, (y1 + y2) / 2};
			
			g.drawLine(x1, (y1 + y2) / 2, (x1 + x2) / 2, y2);
		}
		else if(!code[0] && !code[1] && code[2] && code[3]) {
			poly = new int[2][];
			poly[0] = new int[] {x1, x2, x2, x1};
			poly[1] = new int[] {(y1 + y2) / 2, (y1 + y2) / 2, y2, y2};
			
			g.drawLine(x1, (y1 + y2) / 2, x2, (y1 + y2) / 2);
		}
		else if(code[0] && !code[1] && !code[2] && code[3]) {
			poly = new int[2][];
			poly[0] = new int[] {x1, (x1 + x2) / 2, (x1 + x2) / 2, x1};
			poly[1] = new int[] {y1, y1, y2, y2};
			
			g.drawLine((x1 + x2) / 2, y1, (x1 + x2) / 2, y2);
		}
		else if(code[0] && code[1] && !code[2] && !code[3]) {
			poly = new int[2][];
			poly[0] = new int[] {x1, x2, x2, x1};
			poly[1] = new int[] {y1, y1, (y1 + y2) / 2, (y1 + y2) / 2};
			
			g.drawLine(x1, (y1 + y2) / 2, x2, (y1 + y2) / 2);
		}
		else if(!code[0] && code[1] && code[2] && !code[3]) {
			poly = new int[2][];
			poly[0] = new int[] {(x1 + x2) / 2, x2, x2, (x1 + x2) / 2};
			poly[1] = new int[] {y1, y1, y2, y2};
			
			g.drawLine((x1 + x2) / 2, y1, (x1 + x2) / 2, y2);
		}
		else if(!code[0] && !code[1] && !code[2] && code[3]) {
			poly = new int[2][];
			poly[0] = new int[] {x1, (x1 + x2) / 2, x1};
			poly[1] = new int[] {(y1 + y2) / 2, y2, y2};
			
			g.drawLine(x1, (y1 + y2) / 2, (x1 + x2) / 2, y2);
			//g.drawLine(x2, (y1 + y2) / 2, (x1 + x2) / 2, y2);
		}
		else if(code[0] && !code[1] && !code[2] && !code[3]) {
			poly = new int[2][];
			poly[0] = new int[] {x1, (x1 + x2) / 2, x1};
			poly[1] = new int[] {y1, y1, (y1 + y2) / 2};
			
			g.drawLine(x1, (y1 + y2) / 2, (x1 + x2) / 2, y1);
		}
		else if(!code[0] && code[1] && !code[2] && !code[3]) {
			poly = new int[2][];
			poly[0] = new int[] {(x1 + x2) / 2, x2, x2};
			poly[1] = new int[] {y1, y1, (y1 + y2) / 2};
			
			g.drawLine((x1 + x2) / 2, y1, x2, (y1 + y2) / 2);
		}
		else if(!code[0] && !code[1] && code[2] && !code[3]) {
			poly = new int[2][];
			poly[0] = new int[] {(x1 + x2) / 2, x2, x2};
			poly[1] = new int[] {y2, (y1 + y2) / 2, y2};
			
			g.drawLine(x2, (y1 + y2) / 2, (x1 + x2) / 2, y2);
		}
		//System.out.println(y1 + " " + y2);
		if(poly.length != 0) {
			g2.setComposite(GraphicsTools.makeComposite(0.1));
			//System.out.println(x1 + " " + x2);
			//g.setColor(Color.BLACK);
			g.fillPolygon(poly[0], poly[1], poly[0].length);
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
