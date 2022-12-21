package state;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;

import util.Vec2;

public class CurveEditor extends State {
	
	private static final int CURVE_BEZIER = 0;
	private static final int CURVE_BEZIER_SPLINE = 1;
	
	private static int controlPointSize = 8;
	
	private boolean drawControlPoints = true;
	
	private ArrayList<Vec2> controlPoints;
	private ArrayList<Bezier> curves;
	
	private boolean drawingCurve = false;
	
	private boolean controlPointHeld = false;
	private int controlPointHeldIndex = 0;
	
	private boolean shiftDown = false;

	public CurveEditor(StateManager gsm) {
		super(gsm);
		
		this.controlPoints = new ArrayList<>();
		this.curves = new ArrayList<>();
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tick(Point mouse2) {
		if(this.controlPointHeld) {
			this.controlPoints.get(this.controlPointHeldIndex).set(new Vec2(mouse2.x, mouse2.y));
		}
	}

	@Override
	public void draw(Graphics g) {
		for(Bezier b : this.curves) {
			b.draw(g);
		}
		
		//control points
		if(this.drawControlPoints) {
			for(int i = 0; i < controlPoints.size(); i++) {
				int cx = (int) controlPoints.get(i).x - controlPointSize / 2;
				int cy = (int) controlPoints.get(i).y - controlPointSize / 2;
				
				g.setColor(Color.WHITE);
				g.fillRect(cx, cy, controlPointSize, controlPointSize);
				
				g.setColor(Color.BLACK);
				g.drawRect(cx, cy, controlPointSize, controlPointSize);
			}
		}
		
		g.drawString("LEFT CLICK to start drawing a curve, and RIGHT CLICK to end it.", 10, 10);
		g.drawString("Bezier Curve", 10, 30);
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
		boolean leftDown = arg0.getButton() == MouseEvent.BUTTON1;
		boolean rightDown = arg0.getButton() == MouseEvent.BUTTON3;
		
		Vec2 mousePos = new Vec2(arg0.getX(), arg0.getY());
		
		if(this.drawingCurve) {
			if(leftDown) {
				Vec2 va = new Vec2(mousePos);
				
				this.curves.get(this.curves.size() - 1).addControlPoint(va);
				
				this.controlPoints.add(va);
				this.controlPointHeldIndex = this.controlPoints.size() - 1;
			}
			else if(rightDown) {
				this.drawingCurve = false;
				this.controlPointHeld = false;
			}
		}
		else if(this.controlPointHeld) {
			if(leftDown) {
				this.controlPointHeld = false;
			}
		}
		else {
			if(leftDown) {
				this.drawingCurve = true;
				Bezier b = new Bezier();
				
				this.curves.add(b);
				
				Vec2 va = new Vec2(mousePos);
				Vec2 vb = new Vec2(mousePos);
				
				b.addControlPoint(va);
				b.addControlPoint(vb);
				
				this.controlPoints.add(va);
				this.controlPoints.add(vb);
				
				this.controlPointHeld = true;
				this.controlPointHeldIndex = this.controlPoints.size() - 1;
			}
		}
		
		if(leftDown) {
			if(this.drawingCurve) {
				
			}
			else if(this.controlPointHeld) {
				
			}
		}
		else if(rightDown) {
			if(this.drawingCurve) {
				
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}

abstract class Curve {
	
	protected boolean controlPointHeld = false;
	protected int controlPointHeldIndex = 0;
	
	protected ArrayList<Vec2> controlPoints;
	
	public abstract void draw(Graphics g);
	public abstract void tick(Vec2 mouse);
	
	protected Vec2 lerp(Vec2 a, Vec2 b, float t) {
		return new Vec2(a.x * (1f - t) + b.x * t, a.y * (1f - t) + b.y * t);
	}
	
	public void addControlPoint(Vec2 controlPoint) {
		this.controlPoints.add(controlPoint);
	}
	
}

class Bezier extends Curve {
	//draws a bezier curve along the supplied control points 
	
	private int numSamples = 100;
	
	public Bezier() {
		super();
	}
	
	@Override
	public void draw(Graphics g) {
		if(this.controlPoints.size() == 0) {
			return;
		}
		
		//curve
		float sampleLength = 1f / (float) (numSamples - 1);
		for(int i = 0; i < numSamples - 1; i++) {
			Vec2 a = getPointOnCurve(i * sampleLength);
			Vec2 b = getPointOnCurve((i + 1) * sampleLength);
			g.drawLine((int) a.x, (int) a.y, (int) b.x, (int) b.y);
		}
	}
	
	@Override
	public void tick(Vec2 mouse) {
		
	}
	
	//gets the point on the line that corresponds to the input t value
	public Vec2 getPointOnCurve(float t) {
		if(t < 0 || t > 1) {
			return null;
		}
		ArrayList<Vec2> lerpPoints = new ArrayList<>();
		lerpPoints.addAll(this.controlPoints);
		while(lerpPoints.size() != 1) {
			ArrayList<Vec2> nextPoints = new ArrayList<>();
			for(int i = 0; i < lerpPoints.size() - 1; i++) {
				Vec2 a = lerpPoints.get(i);
				Vec2 b = lerpPoints.get(i + 1);
				nextPoints.add(this.lerp(a, b, t));
			}
			lerpPoints = nextPoints;
		}
		return lerpPoints.get(0);
	}
	
}

class BezierSpline extends Curve {
	//forms a spline out of multiple degree 3 bezier curves
	
	private ArrayList<Bezier> curves;
	
	public BezierSpline() {
		
	}

	@Override
	public void draw(Graphics g) {
		if(this.curves.size() == 0) {
			return;
		}
		
		for(Bezier b : curves) {
			b.draw(g);
		}
	}
	
	@Override
	public void tick(Vec2 mouse) {
		
	}
	
}
