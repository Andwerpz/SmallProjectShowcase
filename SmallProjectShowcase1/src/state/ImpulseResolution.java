package state;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.util.ArrayList;

import impulse.Body;
import impulse.Circle;
import impulse.ImpulseMath;
import impulse.ImpulseScene;
import impulse.Manifold;
import impulse.Polygon;
import impulse.Shape;
import input.InputManager;
import input.ToggleButton;
import main.MainPanel;
import util.GraphicsTools;
import util.Mat2;
import util.MathTools;
import util.Vec2;

public class ImpulseResolution extends State {

	InputManager im;
	
	ImpulseScene is;

	public ImpulseResolution(StateManager gsm) {
		super(gsm);

		im = new InputManager();
		im.addInput(new ToggleButton(10, 10, 100, 25, "Fill", "toggle_btn_fill"));
		im.setToggled("toggle_btn_fill", true);
		
		is = new ImpulseScene();

		Body b;
		b = is.add(new Circle(50.0f), MainPanel.WIDTH / 2, MainPanel.HEIGHT / 2);
		b.setStatic();

		b = is.add(new Polygon((MainPanel.WIDTH - 100) / 2, 10.0f), MainPanel.WIDTH / 2, 30);
		b.setStatic();
		b.setOrient(0);
	}

	@Override
	public void init() {

	}

	@Override
	public void tick(Point mouse2) {
		is.fillBodies = im.getToggled("toggle_btn_fill");
		is.tick();
	}

	@Override
	public void draw(Graphics g) {
		GraphicsTools.enableAntialiasing(g);

		is.draw(g);
		im.draw(g);
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		if(arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
			this.exit();
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		im.mouseClicked(arg0);
		int x = arg0.getX();
		int y = -(arg0.getY() - MainPanel.HEIGHT / 2) + MainPanel.HEIGHT / 2;

		if(arg0.getButton() == MouseEvent.BUTTON1) {
			float r = (float) ImpulseMath.random(10.0f, 50.0f);
			int vertCount = ImpulseMath.random(3, Polygon.MAX_POLY_VERTEX_COUNT);

			Vec2[] verts = Vec2.arrayOf(vertCount);
			for (int i = 0; i < vertCount; i++) {
				verts[i].set(ImpulseMath.random(-r, r), ImpulseMath.random(-r, r));
			}

			Body b = is.add(new Polygon(verts), x, y);
			b.setOrient(ImpulseMath.random(-ImpulseMath.PI, ImpulseMath.PI));
			b.restitution = 0.2f;
			b.dynamicFriction = 0.2f;
			b.staticFriction = 0.4f;
		}
		if(arg0.getButton() == MouseEvent.BUTTON3) {
			float r = (float) ImpulseMath.random(10.0f, 30.0f);

			is.add(new Circle(r), x, y);
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
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		im.mouseReleased(arg0);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		// TODO Auto-generated method stub

	}

}