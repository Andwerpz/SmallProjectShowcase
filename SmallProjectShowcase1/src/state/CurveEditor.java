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

	public static int controlPointSize = 8;
	public static boolean drawControlPoints = true;

	private ArrayList<Curve> curves;

	private boolean drawingCurve = false;

	private boolean shiftDown = false;

	public CurveEditor(StateManager gsm) {
		super(gsm);

		this.curves = new ArrayList<>();
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public void tick(Point mouse2) {
		Vec2 mousePos = new Vec2(mouse2.x, mouse2.y);

		for (Curve c : this.curves) {
			c.tick(mousePos);
		}
	}

	@Override
	public void draw(Graphics g) {
		for (Curve c : this.curves) {
			c.draw(g);
		}

		g.drawString("LEFT CLICK to start drawing a curve, and RIGHT CLICK to end it.", 10, 10);
		g.drawString("Bezier Curve", 10, 30);
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
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

		if (this.drawingCurve) {
			Curve c = this.curves.get(this.curves.size() - 1);
			if (leftDown) {
				Vec2 va = new Vec2(mousePos);

				c.addControlPoint(va);
				c.holdLastControlPoint();
			}
			else if (rightDown) {
				this.drawingCurve = false;
				c.releaseControlPoint();
			}
		}
		else {

			//check if pressed on a control point
			for (Curve c : curves) {
				if (c.pressed(mousePos)) {
					return;
				}
			}

			if (leftDown) {
				this.drawingCurve = true;
				Curve c = new BezierSpline();

				this.curves.add(c);

				Vec2 va = new Vec2(mousePos);
				Vec2 vb = new Vec2(mousePos);

				c.addControlPoint(va);
				c.addControlPoint(vb);

				c.holdLastControlPoint();
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		if (this.drawingCurve) {
			return;
		}

		for (Curve c : this.curves) {
			c.released();
		}
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

	public Curve() {
		this.controlPoints = new ArrayList<>();
	}

	public void draw(Graphics g) {
		this._draw(g);

		//control points
		if (CurveEditor.drawControlPoints) {
			for (int i = 0; i < this.controlPoints.size(); i++) {
				Vec2 v = this.controlPoints.get(i);

				int cx = (int) v.x - CurveEditor.controlPointSize / 2;
				int cy = (int) v.y - CurveEditor.controlPointSize / 2;

				g.setColor(Color.WHITE);
				g.fillRect(cx, cy, CurveEditor.controlPointSize, CurveEditor.controlPointSize);

				g.setColor(Color.BLACK);
				g.drawRect(cx, cy, CurveEditor.controlPointSize, CurveEditor.controlPointSize);
			}
		}
	}

	public void tick(Vec2 mouse) {
		if (this.controlPointHeld) {
			this.controlPoints.get(this.controlPointHeldIndex).set(mouse);
		}

		this._tick(mouse);
	}

	public abstract void _draw(Graphics g);

	public abstract void _tick(Vec2 mouse);

	protected Vec2 lerp(Vec2 a, Vec2 b, float t) {
		return new Vec2(a.x * (1f - t) + b.x * t, a.y * (1f - t) + b.y * t);
	}

	public void addControlPoint(Vec2 controlPoint) {
		this.controlPoints.add(controlPoint);
	}

	public void holdLastControlPoint() {
		this.controlPointHeld = true;
		this.controlPointHeldIndex = this.controlPoints.size() - 1;
	}

	public void releaseControlPoint() {
		this.controlPointHeld = false;
	}

	public boolean pressed(Vec2 mouse) {
		//check each control point if it's pressed
		for (int i = 0; i < this.controlPoints.size(); i++) {
			Vec2 v = this.controlPoints.get(i);
			if (Math.abs(v.x - mouse.x) < CurveEditor.controlPointSize / 2 && Math.abs(v.y - mouse.y) < CurveEditor.controlPointSize / 2) {
				this.controlPointHeld = true;
				this.controlPointHeldIndex = i;
				return true;
			}
		}
		return false;
	}

	public void released() {
		this.releaseControlPoint();
	}

}

class Bezier extends Curve {
	//draws a bezier curve along the supplied control points 

	private int numSamples = 100;

	public Bezier() {
		super();
	}

	@Override
	public void _draw(Graphics g) {
		if (this.controlPoints.size() == 0) {
			return;
		}

		//curve
		float sampleLength = 1f / (numSamples - 1);
		for (int i = 0; i < numSamples - 1; i++) {
			Vec2 a = getPointOnCurve(i * sampleLength);
			Vec2 b = getPointOnCurve((i + 1) * sampleLength);
			g.drawLine((int) a.x, (int) a.y, (int) b.x, (int) b.y);
		}
	}

	@Override
	public void _tick(Vec2 mouse) {

	}

	//gets the point on the line that corresponds to the input t value
	public Vec2 getPointOnCurve(float t) {
		if (t < 0 || t > 1) {
			return null;
		}
		ArrayList<Vec2> lerpPoints = new ArrayList<>();
		lerpPoints.addAll(this.controlPoints);
		while (lerpPoints.size() != 1) {
			ArrayList<Vec2> nextPoints = new ArrayList<>();
			for (int i = 0; i < lerpPoints.size() - 1; i++) {
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
		super();

		this.curves = new ArrayList<Bezier>();
	}

	@Override
	public void _draw(Graphics g) {
		if (this.curves.size() == 0) {
			return;
		}

		for (Bezier b : curves) {
			b.draw(g);
		}
	}

	@Override
	public void _tick(Vec2 mouse) {

	}

	@Override
	public void addControlPoint(Vec2 controlPoint) {
		if (this.controlPoints.size() == 0) {
			this.controlPoints.add(controlPoint);
			return;
		}

		Vec2 controlA = this.controlPoints.get(this.controlPoints.size() - 1);
		Vec2 controlB = controlPoint;

		Vec2 toNew = new Vec2(controlA, controlB);

		System.out.println(toNew.length());

		Vec2 jointA = controlA.add(toNew.mul(1f / 3f));
		Vec2 jointB = controlA.add(toNew.mul(2f / 3f));

		this.controlPoints.add(jointA);
		this.controlPoints.add(jointB);
		this.controlPoints.add(controlB);

		Bezier b = new Bezier();
		b.addControlPoint(controlA);
		b.addControlPoint(jointA);
		b.addControlPoint(jointB);
		b.addControlPoint(controlB);

		this.curves.add(b);
	}

}
