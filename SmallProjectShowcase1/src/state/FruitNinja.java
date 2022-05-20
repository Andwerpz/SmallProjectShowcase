package state;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.Arrays;

import impulse.Body;
import impulse.Circle;
import impulse.ImpulseMath;
import impulse.ImpulseScene;
import impulse.Polygon;
import main.MainPanel;
import util.GraphicsTools;
import util.MathTools;
import util.Vec2;

public class FruitNinja extends State {

	ImpulseScene is;

	boolean mousePressed = false;
	boolean mouseReleased = false;

	Vec2 vecPressed = new Vec2(0, 0);
	Vec2 vecReleased = new Vec2(0, 0);
	
	Vec2 mouse = new Vec2(0, 0);

	public FruitNinja(StateManager gsm) {
		super(gsm);

		is = new ImpulseScene();
//		is.fillBodies = false;
//		is.doGravity = false;
//		is.doCollision = false;

		Body b = is.add(new Polygon(MainPanel.WIDTH / 2, 5.0f), MainPanel.WIDTH / 2, 0);
		b.setStatic();
		b.setOrient(0);
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public void tick(Point mouse2) {
		mouse.set(mouse2.x, mouse2.y);
		
		is.tick();
		
		if(mouseReleased) {
			//handle cut
			mousePressed = false;
			mouseReleased = false;
			
			Vec2 prs = new Vec2(vecPressed);
			Vec2 rel = new Vec2(vecReleased);
			
			//loop through all polys
			ArrayList<Body> cutPolys = new ArrayList<>();
			for(int i = is.bodies.size() - 1; i >= 0; i--) {
				Body b = is.bodies.get(i);
				
				//check that the body isn't fixed
				if(b.invMass == 0) {
					continue;
				}
				
				Vec2 modelPressed = prs.sub(b.position);
				Vec2 modelReleased = rel.sub(b.position);
				
				modelReleased.rotate(-b.orient);
				modelPressed.rotate(-b.orient);
				
				if(b.shape instanceof Polygon) {
					Vec2 i1 = null;
					Vec2 i2 = null;
					//check for double intersect
					Polygon p = (Polygon) b.shape;
					for(int j = 0; j < p.vertexCount; j++) {
						Vec2 v1 = p.vertices[j];
						Vec2 v2 = p.vertices[(j + 1) % p.vertexCount];
						Vec2 intersect = MathTools.line_lineCollision(v1.x, v1.y, v2.x, v2.y, modelPressed.x, modelPressed.y, modelReleased.x, modelReleased.y);
						if(intersect != null) {
							if(i1 == null) {
								//first intersect
								i1 = intersect;
							}
							else {
								i2 = intersect;
								break;
							}
						}
					}
					
					if(i2 == null) {
						//not a full slice
						continue;
					}
					
					//full slice
					ArrayList<Vec2> posDot = new ArrayList<>();
					ArrayList<Vec2> negDot = new ArrayList<>();
					
					//find norm to cutting line
					Vec2 normal = new Vec2(modelPressed, modelReleased);
					normal.rotate(Math.toRadians(90));
					normal.normalize();
					
					//determine on which side of the cutting line each vertex is on
					for(int j = 0; j < p.vertexCount; j++) {
						Vec2 v1 = p.vertices[j];
						double dot = MathTools.dotProduct(normal, new Vec2(modelPressed, v1));
						if(dot >= 0) {
							posDot.add(new Vec2(v1));
						}
						else {
							negDot.add(new Vec2(v1));
						}
					}
					
					posDot.add(new Vec2(i1));
					posDot.add(new Vec2(i2));
					
					negDot.add(new Vec2(i1));
					negDot.add(new Vec2(i2));
					
					Polygon posPoly = new Polygon(posDot);
					Polygon negPoly = new Polygon(negDot);
					
					//calculate position of the new polys
					Vec2[] posHull = MathTools.calculateConvexHull(posDot);
					Vec2[] negHull = MathTools.calculateConvexHull(negDot);
					
					Vec2 posCenter = MathTools.getCentroid(posHull);
					Vec2 negCenter = MathTools.getCentroid(negHull);
					
					//rotate center offsets into real space
					posCenter.rotate(b.orient);
					negCenter.rotate(b.orient);
					
					Body b1 = new Body(posPoly, posCenter.x + b.position.x, posCenter.y + b.position.y);
					Body b2 = new Body(negPoly, negCenter.x + b.position.x, negCenter.y + b.position.y);
					
					b1.setOrient(b.orient);
					b2.setOrient(b.orient);
					
					//rotate normal into real space to add a 'push' to cut polys
					normal.rotate(b.orient);
					normal.muli(10);
					
					b2.velocity.set(b.velocity);
					b1.velocity.set(b.velocity);
					
					b1.velocity.addi(normal);
					b2.velocity.subi(normal);
					
					cutPolys.add(b1);
					cutPolys.add(b2);
					
					//remove old poly
					is.bodies.remove(i);
				}
			}
			
			//add new polys
			is.bodies.addAll(cutPolys);
		}
	}

	@Override
	public void draw(Graphics g) {
		GraphicsTools.enableAntialiasing(g);

		is.draw(g);
		
		if(mousePressed) {
			//draw cutting line
			g.drawLine((int) vecPressed.x, (int) (-(vecPressed.y - MainPanel.HEIGHT / 2) + MainPanel.HEIGHT / 2), (int) mouse.x, (int) mouse.y);
		}
		
		g.drawString("Num Bodies: " + is.bodies.size(), 10, 10);
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
		// real space coords
		int x = arg0.getX();
		int y = -(arg0.getY() - MainPanel.HEIGHT / 2) + MainPanel.HEIGHT / 2;

		if(arg0.getButton() == MouseEvent.BUTTON3) {
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

		else if(arg0.getButton() == MouseEvent.BUTTON1) {
			// initiate cut
			mousePressed = true;
			vecPressed.set(x, y);
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// real space coords
		int x = arg0.getX();
		int y = -(arg0.getY() - MainPanel.HEIGHT / 2) + MainPanel.HEIGHT / 2;
		
		if(arg0.getButton() == MouseEvent.BUTTON1) {
			// initiate cut
			mouseReleased = true;
			vecReleased.set(x, y);
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		// TODO Auto-generated method stub

	}

}
