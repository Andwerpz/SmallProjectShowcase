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

import input.InputManager;
import input.ToggleButton;
import main.MainPanel;
import util.GraphicsTools;
import util.Mat2;
import util.MathTools;
import util.Vec2;
import util.Vector;

public class ImpulseResolution extends State {
	
	InputManager im;

	public double dt = 1d / 60d;
	public int iterations = 4;
	public ArrayList<Body> bodies = new ArrayList<Body>();
	public ArrayList<Manifold> contacts = new ArrayList<Manifold>();

	boolean fillBodies = true;
	
	public ImpulseResolution(StateManager gsm) {
		super(gsm);

		im = new InputManager();
		im.addInput(new ToggleButton(10, 10, 100, 25, "Fill", "toggle_btn_fill"));
		im.setToggled("toggle_btn_fill", true);
		
		Body b;
		b = add(new Circle(50.0f), MainPanel.WIDTH / 2, MainPanel.HEIGHT / 2);
		b.setStatic();

		b = add(new Polygon((MainPanel.WIDTH - 100) / 2, 10.0f), MainPanel.WIDTH / 2, 30);
		b.setStatic();
		b.setOrient(0);
	}

	@Override
	public void init() {

	}

	@Override
	public void tick(Point mouse2) {
		this.fillBodies = im.getToggled("toggle_btn_fill");
		
		// Generate new collision info
		contacts.clear();
		for (int i = 0; i < bodies.size(); ++i) {
			Body A = bodies.get(i);

			for (int j = i + 1; j < bodies.size(); ++j) {
				Body B = bodies.get(j);

				if (A.invMass == 0 && B.invMass == 0) {
					continue;
				}

				Manifold m = new Manifold(A, B);
				m.solve();

				if (m.contactCount > 0) {
					contacts.add(m);
				}
			}
		}

		// Integrate forces
		for (int i = 0; i < bodies.size(); ++i) {
			integrateForces(bodies.get(i), dt);
		}

		// Initialize collision
		for (int i = 0; i < contacts.size(); ++i) {
			contacts.get(i).initialize();
		}

		// Solve collisions
		for (int j = 0; j < iterations; ++j) {
			for (int i = 0; i < contacts.size(); ++i) {
				contacts.get(i).applyImpulse();
			}
		}

		// Integrate velocities
		for (int i = 0; i < bodies.size(); ++i) {
			integrateVelocity(bodies.get(i), dt);
		}

		// Correct positions
		for (int i = 0; i < contacts.size(); ++i) {
			contacts.get(i).positionalCorrection();
		}

		// Clear all forces
		for (int i = 0; i < bodies.size(); ++i) {
			Body b = bodies.get(i);
			b.force.set(0, 0);
			b.torque = 0;
		}
	}

	public Body add(Shape shape, int x, int y) {
		Body b = new Body(shape, x, y);
		bodies.add(b);
		return b;
	}

	public void integrateForces(Body b, double dt) {
//		if(b->im == 0.0f)
//			return;
//		b->velocity += (b->force * b->im + gravity) * (dt / 2.0f);
//		b->angularVelocity += b->torque * b->iI * (dt / 2.0f);

		if (b.invMass == 0.0f) {
			return;
		}

		double dts = dt * 0.5f;

		b.velocity.addsi(b.force, b.invMass * dts);
		b.velocity.addsi(ImpulseMath.GRAVITY, dts);
		b.angularVelocity += b.torque * b.invInertia * dts;
	}

	public void integrateVelocity(Body b, double dt) {
//		if(b->im == 0.0f)
//			return;
//		b->position += b->velocity * dt;
//		b->orient += b->angularVelocity * dt;
//		b->SetOrient( b->orient );
//		IntegrateForces( b, dt );

		if (b.invMass == 0.0f) {
			return;
		}

		b.position.addsi(b.velocity, dt);
		b.orient += b.angularVelocity * dt;
		b.setOrient(b.orient);

		integrateForces(b, dt);
	}

	@Override
	public void draw(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		GraphicsTools.enableAntialiasing(g);

		AffineTransform realSpaceTransform = new AffineTransform();
		realSpaceTransform.scale(1.0, -1.0);
		realSpaceTransform.translate(0, -MainPanel.HEIGHT);

		g2d.transform(realSpaceTransform);

		for (Body b : bodies) {
			Vec2 pos = b.position;
			if (b.shape instanceof Circle) {
				if(this.fillBodies) {
					g.fillOval((int) (pos.x - b.shape.radius), (int) (pos.y - b.shape.radius), (int) (b.shape.radius * 2),
							(int) (b.shape.radius * 2));
				}
				else {
					g.drawOval((int) (pos.x - b.shape.radius), (int) (pos.y - b.shape.radius), (int) (b.shape.radius * 2),
							(int) (b.shape.radius * 2));
					
					Vector facing = new Vector(0, b.shape.radius);
					facing.rotateCounterClockwise(b.orient);
					
					Vector a = new Vector(b.position.x, b.position.y);
					Vector aFacing = a.add(facing);
					
					g.drawLine((int) a.x, (int) a.y, (int) aFacing.x, (int) aFacing.y);
				}
			} 
			else if (b.shape instanceof Polygon) {
				Polygon p = (Polygon) b.shape;
				
				int[] cx = new int[p.vertexCount];
				int[] cy = new int[p.vertexCount];
				for(int i = 0; i < p.vertexCount; i++) {
					Vec2 v = new Vec2(p.vertices[i]);
					b.shape.u.muli(v);
					v.addi(b.position);
					cx[i] = (int) v.x;
					cy[i] = (int) v.y;
				}
				
				if(this.fillBodies) {
					g.fillPolygon(cx, cy, p.vertexCount);
				}
				else {
					g.drawPolygon(cx, cy, p.vertexCount);
				}
			}
		}

		try {
			g2d.transform(realSpaceTransform.createInverse());
		} catch (NoninvertibleTransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		im.draw(g);
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
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

		if (arg0.getButton() == MouseEvent.BUTTON1) {
			float r = (float) ImpulseMath.random(10.0f, 50.0f);
			int vertCount = ImpulseMath.random(3, Polygon.MAX_POLY_VERTEX_COUNT);

			Vec2[] verts = Vec2.arrayOf(vertCount);
			for (int i = 0; i < vertCount; i++) {
				verts[i].set(ImpulseMath.random(-r, r), ImpulseMath.random(-r, r));
			}

			Body b = add(new Polygon(verts), x, y);
			b.setOrient(ImpulseMath.random(-ImpulseMath.PI, ImpulseMath.PI));
			b.restitution = 0.2f;
			b.dynamicFriction = 0.2f;
			b.staticFriction = 0.4f;
		}
		if (arg0.getButton() == MouseEvent.BUTTON3) {
			float r = (float) ImpulseMath.random(10.0f, 30.0f);

			add(new Circle(r), x, y);
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

class ImpulseMath {

	public static final double PI = (double) StrictMath.PI;
	public static final double EPSILON = 0.0001f;
	public static final double EPSILON_SQ = EPSILON * EPSILON;
	public static final double BIAS_RELATIVE = 0.95f;
	public static final double BIAS_ABSOLUTE = 0.01f;
	public static final double DT = 1.0f / 60.0f;
	public static final Vec2 GRAVITY = new Vec2(0.0f, -50.0f);
	public static final double RESTING = GRAVITY.mul(DT).lengthSq() + EPSILON;
	public static final double PENETRATION_ALLOWANCE = 0.05f;
	public static final double PENETRATION_CORRETION = 0.4f;

	public static boolean equal(double a, double b) {
		return StrictMath.abs(a - b) <= EPSILON;
	}

	public static double clamp(double min, double max, double a) {
		return (a < min ? min : (a > max ? max : a));
	}

	public static int round(double a) {
		return (int) (a + 0.5f);
	}

	public static double random(double min, double max) {
		return (double) ((max - min) * Math.random() + min);
	}

	public static int random(int min, int max) {
		return (int) ((max - min + 1) * Math.random() + min);
	}

	public static boolean gt(double a, double b) {
		return a >= b * BIAS_RELATIVE + a * BIAS_ABSOLUTE;
	}

}

abstract class Shape {
	public enum Type {
		Circle, Poly, Count
	}

	public Body body;
	public double radius;
	public final Mat2 u = new Mat2();

	public Shape() {
	}

	public abstract Shape clone();

	public abstract void initialize();

	public abstract void computeMass(double density);

	public abstract void setOrient(double radians);

	public abstract Type getType();
}

class Body {
	public final Vec2 position = new Vec2();
	public final Vec2 velocity = new Vec2();
	public final Vec2 force = new Vec2();
	public double angularVelocity;
	public double torque;
	public double orient;
	public double mass, invMass, inertia, invInertia;
	public double staticFriction;
	public double dynamicFriction;
	public double restitution;
	public final Shape shape;

	public Body(Shape shape, int x, int y) {
		this.shape = shape;

		position.set(x, y);
		velocity.set(0, 0);
		angularVelocity = 0;
		torque = 0;
		orient = ImpulseMath.random(-ImpulseMath.PI, ImpulseMath.PI);
		force.set(0, 0);
		staticFriction = 0.5f;
		dynamicFriction = 0.3f;
		restitution = 0.2f;

		shape.body = this;
		shape.initialize();
	}

	public void applyForce(Vec2 f) {
		// force += f;
		force.addi(f);
	}

	public void applyImpulse(Vec2 impulse, Vec2 contactVector) {
		// velocity += im * impulse;
		// angularVelocity += iI * Cross( contactVector, impulse );

		velocity.addsi(impulse, invMass);
		angularVelocity += invInertia * Vec2.cross(contactVector, impulse);
	}

	public void setStatic() {
		inertia = 0.0f;
		invInertia = 0.0f;
		mass = 0.0f;
		invMass = 0.0f;
	}

	public void setOrient(double radians) {
		orient = radians;
		shape.setOrient(radians);
	}
}

class Circle extends Shape {
	public Circle(double r) {
		radius = r;
	}

	@Override
	public Shape clone() {
		return new Circle(radius);
	}

	@Override
	public void initialize() {
		computeMass(1.0f);
	}

	@Override
	public void computeMass(double density) {
		body.mass = ImpulseMath.PI * radius * radius * density;
		body.invMass = (body.mass != 0.0f) ? 1.0f / body.mass : 0.0f;
		body.inertia = body.mass * radius * radius;
		body.invInertia = (body.inertia != 0.0f) ? 1.0f / body.inertia : 0.0f;
	}

	@Override
	public void setOrient(double radians) {
	}

	@Override
	public Type getType() {
		return Type.Circle;
	}
}

class Polygon extends Shape {
	public static final int MAX_POLY_VERTEX_COUNT = 64;

	public int vertexCount;
	public Vec2[] vertices = Vec2.arrayOf(MAX_POLY_VERTEX_COUNT);
	public Vec2[] normals = Vec2.arrayOf(MAX_POLY_VERTEX_COUNT);

	public Polygon() {
	}

	public Polygon(Vec2... verts) {
		set(verts);
	}

	public Polygon(double hw, double hh) {
		setBox(hw, hh);
	}

	@Override
	public Shape clone() {
//		PolygonShape *poly = new PolygonShape( );
//	    poly->u = u;
//	    for(uint32 i = 0; i < m_vertexCount; ++i)
//	    {
//	      poly->m_vertices[i] = m_vertices[i];
//	      poly->m_normals[i] = m_normals[i];
//	    }
//	    poly->m_vertexCount = m_vertexCount;
//	    return poly;

		Polygon p = new Polygon();
		p.u.set(u);
		for (int i = 0; i < vertexCount; i++) {
			p.vertices[i].set(vertices[i]);
			p.normals[i].set(normals[i]);
		}
		p.vertexCount = vertexCount;

		return p;
	}

	@Override
	public void initialize() {
		computeMass(1.0f);
	}

	@Override
	public void computeMass(double density) {
		// Calculate centroid and moment of inertia
		Vec2 c = new Vec2(0.0f, 0.0f); // centroid
		double area = 0.0f;
		double I = 0.0f;
		final double k_inv3 = 1.0f / 3.0f;

		for (int i = 0; i < vertexCount; ++i) {
			// Triangle vertices, third vertex implied as (0, 0)
			Vec2 p1 = vertices[i];
			Vec2 p2 = vertices[(i + 1) % vertexCount];

			double D = Vec2.cross(p1, p2);
			double triangleArea = 0.5f * D;

			area += triangleArea;

			// Use area to weight the centroid average, not just vertex position
			double weight = triangleArea * k_inv3;
			c.addsi(p1, weight);
			c.addsi(p2, weight);

			double intx2 = p1.x * p1.x + p2.x * p1.x + p2.x * p2.x;
			double inty2 = p1.y * p1.y + p2.y * p1.y + p2.y * p2.y;
			I += (0.25f * k_inv3 * D) * (intx2 + inty2);
		}

		c.muli(1.0f / area);

		// Translate vertices to centroid (make the centroid (0, 0)
		// for the polygon in model space)
		// Not really necessary, but I like doing this anyway
		for (int i = 0; i < vertexCount; ++i) {
			vertices[i].subi(c);
		}

		body.mass = density * area;
		body.invMass = (body.mass != 0.0f) ? 1.0f / body.mass : 0.0f;
		body.inertia = I * density;
		body.invInertia = (body.inertia != 0.0f) ? 1.0f / body.inertia : 0.0f;
	}

	@Override
	public void setOrient(double radians) {
		u.set(radians);
	}

	@Override
	public Type getType() {
		return Type.Poly;
	}

	public void setBox(double hw, double hh) {
		vertexCount = 4;
		vertices[0].set(-hw, -hh);
		vertices[1].set(hw, -hh);
		vertices[2].set(hw, hh);
		vertices[3].set(-hw, hh);
		normals[0].set(0.0f, -1.0f);
		normals[1].set(1.0f, 0.0f);
		normals[2].set(0.0f, 1.0f);
		normals[3].set(-1.0f, 0.0f);
	}

	public void set(Vec2... verts) {
		// Find the right most point on the hull
		int rightMost = 0;
		double highestXCoord = verts[0].x;
		for (int i = 1; i < verts.length; ++i) {
			double x = verts[i].x;

			if (x > highestXCoord) {
				highestXCoord = x;
				rightMost = i;
			}
			// If matching x then take farthest negative y
			else if (x == highestXCoord) {
				if (verts[i].y < verts[rightMost].y) {
					rightMost = i;
				}
			}
		}

		int[] hull = new int[MAX_POLY_VERTEX_COUNT];
		int outCount = 0;
		int indexHull = rightMost;

		for (;;) {
			hull[outCount] = indexHull;

			// Search for next index that wraps around the hull
			// by computing cross products to find the most counter-clockwise
			// vertex in the set, given the previos hull index
			int nextHullIndex = 0;
			for (int i = 1; i < verts.length; ++i) {
				// Skip if same coordinate as we need three unique
				// points in the set to perform a cross product
				if (nextHullIndex == indexHull) {
					nextHullIndex = i;
					continue;
				}

				// Cross every set of three unique vertices
				// Record each counter clockwise third vertex and add
				// to the output hull
				// See : http://www.oocities.org/pcgpe/math2d.html
				Vec2 e1 = verts[nextHullIndex].sub(verts[hull[outCount]]);
				Vec2 e2 = verts[i].sub(verts[hull[outCount]]);
				double c = Vec2.cross(e1, e2);
				if (c < 0.0f) {
					nextHullIndex = i;
				}

				// Cross product is zero then e vectors are on same line
				// therefore want to record vertex farthest along that line
				if (c == 0.0f && e2.lengthSq() > e1.lengthSq()) {
					nextHullIndex = i;
				}
			}

			++outCount;
			indexHull = nextHullIndex;

			// Conclude algorithm upon wrap-around
			if (nextHullIndex == rightMost) {
				vertexCount = outCount;
				break;
			}
		}

		// Copy vertices into shape's vertices
		for (int i = 0; i < vertexCount; ++i) {
			vertices[i].set(verts[hull[i]]);
		}

		// Compute face normals
		for (int i = 0; i < vertexCount; ++i) {
			Vec2 face = vertices[(i + 1) % vertexCount].sub(vertices[i]);

			// Calculate normal with 2D cross product between vector and scalar
			normals[i].set(face.y, -face.x);
			normals[i].normalize();
		}
	}

	public Vec2 getSupport(Vec2 dir) {
		double bestProjection = -Float.MAX_VALUE;
		Vec2 bestVertex = null;

		for (int i = 0; i < vertexCount; ++i) {
			Vec2 v = vertices[i];
			double projection = Vec2.dot(v, dir);

			if (projection > bestProjection) {
				bestVertex = v;
				bestProjection = projection;
			}
		}

		return bestVertex;
	}
}

class Manifold {
	public Body A;
	public Body B;
	public double penetration;
	public final Vec2 normal = new Vec2();
	public final Vec2[] contacts = { new Vec2(), new Vec2() };
	public int contactCount;
	public double e;
	public double df;
	public double sf;

	public Manifold(Body a, Body b) {
		A = a;
		B = b;
	}

	public void solve() {
		int ia = A.shape.getType().ordinal();
		int ib = B.shape.getType().ordinal();

		Collisions.dispatch[ia][ib].handleCollision(this, A, B);
	}

	public void initialize() {
		// Calculate average restitution
		// e = std::min( A->restitution, B->restitution );
		e = StrictMath.min(A.restitution, B.restitution);

		// Calculate static and dynamic friction
		// sf = std::sqrt( A->staticFriction * A->staticFriction );
		// df = std::sqrt( A->dynamicFriction * A->dynamicFriction );
		sf = (double) StrictMath.sqrt(A.staticFriction * A.staticFriction + B.staticFriction * B.staticFriction);
		df = (double) StrictMath.sqrt(A.dynamicFriction * A.dynamicFriction + B.dynamicFriction * B.dynamicFriction);

		for (int i = 0; i < contactCount; ++i) {
			// Calculate radii from COM to contact
			// Vec2 ra = contacts[i] - A->position;
			// Vec2 rb = contacts[i] - B->position;
			Vec2 ra = contacts[i].sub(A.position);
			Vec2 rb = contacts[i].sub(B.position);

			// Vec2 rv = B->velocity + Cross( B->angularVelocity, rb ) -
			// A->velocity - Cross( A->angularVelocity, ra );
			Vec2 rv = B.velocity.add(Vec2.cross(B.angularVelocity, rb, new Vec2())).subi(A.velocity)
					.subi(Vec2.cross(A.angularVelocity, ra, new Vec2()));

			// Determine if we should perform a resting collision or not
			// The idea is if the only thing moving this object is gravity,
			// then the collision should be performed without any restitution
			// if(rv.LenSqr( ) < (dt * gravity).LenSqr( ) + EPSILON)
			if (rv.lengthSq() < ImpulseMath.RESTING) {
				e = 0.0f;
			}
		}
	}

	public void applyImpulse() {
		// Early out and positional correct if both objects have infinite mass
		// if(Equal( A->im + B->im, 0 ))
		if (ImpulseMath.equal(A.invMass + B.invMass, 0)) {
			infiniteMassCorrection();
			return;
		}

		for (int i = 0; i < contactCount; ++i) {
			// Calculate radii from COM to contact
			// Vec2 ra = contacts[i] - A->position;
			// Vec2 rb = contacts[i] - B->position;
			Vec2 ra = contacts[i].sub(A.position);
			Vec2 rb = contacts[i].sub(B.position);

			// Relative velocity
			// Vec2 rv = B->velocity + Cross( B->angularVelocity, rb ) -
			// A->velocity - Cross( A->angularVelocity, ra );
			Vec2 rv = B.velocity.add(Vec2.cross(B.angularVelocity, rb, new Vec2())).subi(A.velocity)
					.subi(Vec2.cross(A.angularVelocity, ra, new Vec2()));

			// Relative velocity along the normal
			// real contactVel = Dot( rv, normal );
			double contactVel = Vec2.dot(rv, normal);

			// Do not resolve if velocities are separating
			if (contactVel > 0) {
				return;
			}

			// real raCrossN = Cross( ra, normal );
			// real rbCrossN = Cross( rb, normal );
			// real invMassSum = A->im + B->im + Sqr( raCrossN ) * A->iI + Sqr(
			// rbCrossN ) * B->iI;
			double raCrossN = Vec2.cross(ra, normal);
			double rbCrossN = Vec2.cross(rb, normal);
			double invMassSum = A.invMass + B.invMass + (raCrossN * raCrossN) * A.invInertia
					+ (rbCrossN * rbCrossN) * B.invInertia;

			// Calculate impulse scalar
			double j = -(1.0f + e) * contactVel;
			j /= invMassSum;
			j /= contactCount;

			// Apply impulse
			Vec2 impulse = normal.mul(j);
			A.applyImpulse(impulse.neg(), ra);
			B.applyImpulse(impulse, rb);

			// Friction impulse
			// rv = B->velocity + Cross( B->angularVelocity, rb ) -
			// A->velocity - Cross( A->angularVelocity, ra );
			rv = B.velocity.add(Vec2.cross(B.angularVelocity, rb, new Vec2())).subi(A.velocity)
					.subi(Vec2.cross(A.angularVelocity, ra, new Vec2()));

			// Vec2 t = rv - (normal * Dot( rv, normal ));
			// t.Normalize( );
			Vec2 t = new Vec2(rv);
			t.addsi(normal, -Vec2.dot(rv, normal));
			t.normalize();

			// j tangent magnitude
			double jt = -Vec2.dot(rv, t);
			jt /= invMassSum;
			jt /= contactCount;

			// Don't apply tiny friction impulses
			if (ImpulseMath.equal(jt, 0.0f)) {
				return;
			}

			// Coulumb's law
			Vec2 tangentImpulse;
			// if(std::abs( jt ) < j * sf)
			if (StrictMath.abs(jt) < j * sf) {
				// tangentImpulse = t * jt;
				tangentImpulse = t.mul(jt);
			} else {
				// tangentImpulse = t * -j * df;
				tangentImpulse = t.mul(j).muli(-df);
			}

			// Apply friction impulse
			// A->ApplyImpulse( -tangentImpulse, ra );
			// B->ApplyImpulse( tangentImpulse, rb );
			A.applyImpulse(tangentImpulse.neg(), ra);
			B.applyImpulse(tangentImpulse, rb);
		}
	}

	public void positionalCorrection() {
		// const real k_slop = 0.05f; // Penetration allowance
		// const real percent = 0.4f; // Penetration percentage to correct
		// Vec2 correction = (std::max( penetration - k_slop, 0.0f ) / (A->im +
		// B->im)) * normal * percent;
		// A->position -= correction * A->im;
		// B->position += correction * B->im;

		double correction = StrictMath.max(penetration - ImpulseMath.PENETRATION_ALLOWANCE, 0.0f)
				/ (A.invMass + B.invMass) * ImpulseMath.PENETRATION_CORRETION;

		A.position.addsi(normal, -A.invMass * correction);
		B.position.addsi(normal, B.invMass * correction);
	}

	public void infiniteMassCorrection() {
		A.velocity.set(0, 0);
		B.velocity.set(0, 0);
	}
}

class Collisions {

	public static CollisionCallback[][] dispatch = {
			{ CollisionCircleCircle.instance, CollisionCirclePolygon.instance },
			{ CollisionPolygonCircle.instance, CollisionPolygonPolygon.instance } };

}

interface CollisionCallback {
	public void handleCollision(Manifold m, Body a, Body b);
}

class CollisionCircleCircle implements CollisionCallback {
	public static final CollisionCircleCircle instance = new CollisionCircleCircle();

	@Override
	public void handleCollision(Manifold m, Body a, Body b) {
		Circle A = (Circle) a.shape;
		Circle B = (Circle) b.shape;

		// Calculate translational vector, which is normal
		// Vec2 normal = b->position - a->position;
		Vec2 normal = b.position.sub(a.position);

		// real dist_sqr = normal.LenSqr( );
		// real radius = A->radius + B->radius;
		double dist_sqr = normal.lengthSq();
		double radius = A.radius + B.radius;

		// Not in contact
		if (dist_sqr >= radius * radius) {
			m.contactCount = 0;
			return;
		}

		double distance = (double) StrictMath.sqrt(dist_sqr);

		m.contactCount = 1;

		if (distance == 0.0f) {
			// m->penetration = A->radius;
			// m->normal = Vec2( 1, 0 );
			// m->contacts [0] = a->position;
			m.penetration = A.radius;
			m.normal.set(1.0f, 0.0f);
			m.contacts[0].set(a.position);
		} else {
			// m->penetration = radius - distance;
			// m->normal = normal / distance; // Faster than using Normalized since
			// we already performed sqrt
			// m->contacts[0] = m->normal * A->radius + a->position;
			m.penetration = radius - distance;
			m.normal.set(normal).divi(distance);
			m.contacts[0].set(m.normal).muli(A.radius).addi(a.position);
		}
	}
}

class CollisionCirclePolygon implements CollisionCallback {
	public static final CollisionCirclePolygon instance = new CollisionCirclePolygon();

	@Override
	public void handleCollision(Manifold m, Body a, Body b) {
		Circle A = (Circle) a.shape;
		Polygon B = (Polygon) b.shape;

		m.contactCount = 0;

		// Transform circle center to Polygon model space
		// Vec2 center = a->position;
		// center = B->u.Transpose( ) * (center - b->position);
		Vec2 center = B.u.transpose().muli(a.position.sub(b.position));

		// Find edge with minimum penetration
		// Exact concept as using support points in Polygon vs Polygon
		double separation = -Float.MAX_VALUE;
		int faceNormal = 0;
		for (int i = 0; i < B.vertexCount; ++i) {
			// real s = Dot( B->m_normals[i], center - B->m_vertices[i] );
			double s = Vec2.dot(B.normals[i], center.sub(B.vertices[i]));

			if (s > A.radius) {
				return;
			}

			if (s > separation) {
				separation = s;
				faceNormal = i;
			}
		}

		// Grab face's vertices
		Vec2 v1 = B.vertices[faceNormal];
		int i2 = faceNormal + 1 < B.vertexCount ? faceNormal + 1 : 0;
		Vec2 v2 = B.vertices[i2];

		// Check to see if center is within polygon
		if (separation < ImpulseMath.EPSILON) {
			// m->contact_count = 1;
			// m->normal = -(B->u * B->m_normals[faceNormal]);
			// m->contacts[0] = m->normal * A->radius + a->position;
			// m->penetration = A->radius;

			m.contactCount = 1;
			B.u.mul(B.normals[faceNormal], m.normal).negi();
			m.contacts[0].set(m.normal).muli(A.radius).addi(a.position);
			m.penetration = A.radius;
			return;
		}

		// Determine which voronoi region of the edge center of circle lies within
		// real dot1 = Dot( center - v1, v2 - v1 );
		// real dot2 = Dot( center - v2, v1 - v2 );
		// m->penetration = A->radius - separation;
		double dot1 = Vec2.dot(center.sub(v1), v2.sub(v1));
		double dot2 = Vec2.dot(center.sub(v2), v1.sub(v2));
		m.penetration = A.radius - separation;

		// Closest to v1
		if (dot1 <= 0.0f) {
			if (Vec2.distanceSq(center, v1) > A.radius * A.radius) {
				return;
			}

			// m->contact_count = 1;
			// Vec2 n = v1 - center;
			// n = B->u * n;
			// n.Normalize( );
			// m->normal = n;
			// v1 = B->u * v1 + b->position;
			// m->contacts[0] = v1;

			m.contactCount = 1;
			B.u.muli(m.normal.set(v1).subi(center)).normalize();
			B.u.mul(v1, m.contacts[0]).addi(b.position);
		}

		// Closest to v2
		else if (dot2 <= 0.0f) {
			if (Vec2.distanceSq(center, v2) > A.radius * A.radius) {
				return;
			}

			// m->contact_count = 1;
			// Vec2 n = v2 - center;
			// v2 = B->u * v2 + b->position;
			// m->contacts[0] = v2;
			// n = B->u * n;
			// n.Normalize( );
			// m->normal = n;

			m.contactCount = 1;
			B.u.muli(m.normal.set(v2).subi(center)).normalize();
			B.u.mul(v2, m.contacts[0]).addi(b.position);
		}

		// Closest to face
		else {
			Vec2 n = B.normals[faceNormal];

			if (Vec2.dot(center.sub(v1), n) > A.radius) {
				return;
			}

			// n = B->u * n;
			// m->normal = -n;
			// m->contacts[0] = m->normal * A->radius + a->position;
			// m->contact_count = 1;

			m.contactCount = 1;
			B.u.mul(n, m.normal).negi();
			m.contacts[0].set(a.position).addsi(m.normal, A.radius);
		}
	}
}

class CollisionPolygonCircle implements CollisionCallback {
	public static final CollisionPolygonCircle instance = new CollisionPolygonCircle();

	@Override
	public void handleCollision(Manifold m, Body a, Body b) {
		CollisionCirclePolygon.instance.handleCollision(m, b, a);

		if (m.contactCount > 0) {
			m.normal.negi();
		}
	}
}

class CollisionPolygonPolygon implements CollisionCallback {
	public static final CollisionPolygonPolygon instance = new CollisionPolygonPolygon();

	@Override
	public void handleCollision(Manifold m, Body a, Body b) {
		Polygon A = (Polygon) a.shape;
		Polygon B = (Polygon) b.shape;
		m.contactCount = 0;

		// Check for a separating axis with A's face planes
		int[] faceA = { 0 };
		double penetrationA = findAxisLeastPenetration(faceA, A, B);
		if (penetrationA >= 0.0f) {
			return;
		}

		// Check for a separating axis with B's face planes
		int[] faceB = { 0 };
		double penetrationB = findAxisLeastPenetration(faceB, B, A);
		if (penetrationB >= 0.0f) {
			return;
		}

		int referenceIndex;
		boolean flip; // Always point from a to b

		Polygon RefPoly; // Reference
		Polygon IncPoly; // Incident

		// Determine which shape contains reference face
		if (ImpulseMath.gt(penetrationA, penetrationB)) {
			RefPoly = A;
			IncPoly = B;
			referenceIndex = faceA[0];
			flip = false;
		} else {
			RefPoly = B;
			IncPoly = A;
			referenceIndex = faceB[0];
			flip = true;
		}

		// World space incident face
		Vec2[] incidentFace = Vec2.arrayOf(2);

		findIncidentFace(incidentFace, RefPoly, IncPoly, referenceIndex);

		// y
		// ^ .n ^
		// +---c ------posPlane--
		// x < | i |\
		// +---+ c-----negPlane--
		// \ v
		// r
		//
		// r : reference face
		// i : incident poly
		// c : clipped point
		// n : incident normal

		// Setup reference face vertices
		Vec2 v1 = RefPoly.vertices[referenceIndex];
		referenceIndex = referenceIndex + 1 == RefPoly.vertexCount ? 0 : referenceIndex + 1;
		Vec2 v2 = RefPoly.vertices[referenceIndex];

		// Transform vertices to world space
		// v1 = RefPoly->u * v1 + RefPoly->body->position;
		// v2 = RefPoly->u * v2 + RefPoly->body->position;
		v1 = RefPoly.u.mul(v1).addi(RefPoly.body.position);
		v2 = RefPoly.u.mul(v2).addi(RefPoly.body.position);

		// Calculate reference face side normal in world space
		// Vec2 sidePlaneNormal = (v2 - v1);
		// sidePlaneNormal.Normalize( );
		Vec2 sidePlaneNormal = v2.sub(v1);
		sidePlaneNormal.normalize();

		// Orthogonalize
		// Vec2 refFaceNormal( sidePlaneNormal.y, -sidePlaneNormal.x );
		Vec2 refFaceNormal = new Vec2(sidePlaneNormal.y, -sidePlaneNormal.x);

		// ax + by = c
		// c is distance from origin
		// real refC = Dot( refFaceNormal, v1 );
		// real negSide = -Dot( sidePlaneNormal, v1 );
		// real posSide = Dot( sidePlaneNormal, v2 );
		double refC = Vec2.dot(refFaceNormal, v1);
		double negSide = -Vec2.dot(sidePlaneNormal, v1);
		double posSide = Vec2.dot(sidePlaneNormal, v2);

		// Clip incident face to reference face side planes
		// if(Clip( -sidePlaneNormal, negSide, incidentFace ) < 2)
		if (clip(sidePlaneNormal.neg(), negSide, incidentFace) < 2) {
			return; // Due to doubleing point error, possible to not have required
					// points
		}

		// if(Clip( sidePlaneNormal, posSide, incidentFace ) < 2)
		if (clip(sidePlaneNormal, posSide, incidentFace) < 2) {
			return; // Due to doubleing point error, possible to not have required
					// points
		}

		// Flip
		m.normal.set(refFaceNormal);
		if (flip) {
			m.normal.negi();
		}

		// Keep points behind reference face
		int cp = 0; // clipped points behind reference face
		double separation = Vec2.dot(refFaceNormal, incidentFace[0]) - refC;
		if (separation <= 0.0f) {
			m.contacts[cp].set(incidentFace[0]);
			m.penetration = -separation;
			++cp;
		} else {
			m.penetration = 0;
		}

		separation = Vec2.dot(refFaceNormal, incidentFace[1]) - refC;

		if (separation <= 0.0f) {
			m.contacts[cp].set(incidentFace[1]);

			m.penetration += -separation;
			++cp;

			// Average penetration
			m.penetration /= cp;
		}

		m.contactCount = cp;
	}

	public double findAxisLeastPenetration(int[] faceIndex, Polygon A, Polygon B) {
		double bestDistance = -Float.MAX_VALUE;
		int bestIndex = 0;

		for (int i = 0; i < A.vertexCount; ++i) {
			// Retrieve a face normal from A
			// Vec2 n = A->m_normals[i];
			// Vec2 nw = A->u * n;
			Vec2 nw = A.u.mul(A.normals[i]);

			// Transform face normal into B's model space
			// Mat2 buT = B->u.Transpose( );
			// n = buT * nw;
			Mat2 buT = B.u.transpose();
			Vec2 n = buT.mul(nw);

			// Retrieve support point from B along -n
			// Vec2 s = B->GetSupport( -n );
			Vec2 s = B.getSupport(n.neg());

			// Retrieve vertex on face from A, transform into
			// B's model space
			// Vec2 v = A->m_vertices[i];
			// v = A->u * v + A->body->position;
			// v -= B->body->position;
			// v = buT * v;
			Vec2 v = buT.muli(A.u.mul(A.vertices[i]).addi(A.body.position).subi(B.body.position));

			// Compute penetration distance (in B's model space)
			// real d = Dot( n, s - v );
			double d = Vec2.dot(n, s.sub(v));

			// Store greatest distance
			if (d > bestDistance) {
				bestDistance = d;
				bestIndex = i;
			}
		}

		faceIndex[0] = bestIndex;
		return bestDistance;
	}

	public void findIncidentFace(Vec2[] v, Polygon RefPoly, Polygon IncPoly, int referenceIndex) {
		Vec2 referenceNormal = RefPoly.normals[referenceIndex];

		// Calculate normal in incident's frame of reference
		// referenceNormal = RefPoly->u * referenceNormal; // To world space
		// referenceNormal = IncPoly->u.Transpose( ) * referenceNormal; // To
		// incident's model space
		referenceNormal = RefPoly.u.mul(referenceNormal); // To world space
		referenceNormal = IncPoly.u.transpose().mul(referenceNormal); // To
																		// incident's
																		// model
																		// space

		// Find most anti-normal face on incident polygon
		int incidentFace = 0;
		double minDot = Float.MAX_VALUE;
		for (int i = 0; i < IncPoly.vertexCount; ++i) {
			// real dot = Dot( referenceNormal, IncPoly->m_normals[i] );
			double dot = Vec2.dot(referenceNormal, IncPoly.normals[i]);

			if (dot < minDot) {
				minDot = dot;
				incidentFace = i;
			}
		}

		// Assign face vertices for incidentFace
		// v[0] = IncPoly->u * IncPoly->m_vertices[incidentFace] +
		// IncPoly->body->position;
		// incidentFace = incidentFace + 1 >= (int32)IncPoly->m_vertexCount ? 0 :
		// incidentFace + 1;
		// v[1] = IncPoly->u * IncPoly->m_vertices[incidentFace] +
		// IncPoly->body->position;

		v[0] = IncPoly.u.mul(IncPoly.vertices[incidentFace]).addi(IncPoly.body.position);
		incidentFace = incidentFace + 1 >= (int) IncPoly.vertexCount ? 0 : incidentFace + 1;
		v[1] = IncPoly.u.mul(IncPoly.vertices[incidentFace]).addi(IncPoly.body.position);
	}

	public int clip(Vec2 n, double c, Vec2[] face) {
		int sp = 0;
		Vec2[] out = { new Vec2(face[0]), new Vec2(face[1]) };

		// Retrieve distances from each endpoint to the line
		// d = ax + by - c
		// real d1 = Dot( n, face[0] ) - c;
		// real d2 = Dot( n, face[1] ) - c;
		double d1 = Vec2.dot(n, face[0]) - c;
		double d2 = Vec2.dot(n, face[1]) - c;

		// If negative (behind plane) clip
		// if(d1 <= 0.0f) out[sp++] = face[0];
		// if(d2 <= 0.0f) out[sp++] = face[1];
		if (d1 <= 0.0f)
			out[sp++].set(face[0]);
		if (d2 <= 0.0f)
			out[sp++].set(face[1]);

		// If the points are on different sides of the plane
		if (d1 * d2 < 0.0f) // less than to ignore -0.0f
		{
			// Push intersection point
			// real alpha = d1 / (d1 - d2);
			// out[sp] = face[0] + alpha * (face[1] - face[0]);
			// ++sp;

			double alpha = d1 / (d1 - d2);

			out[sp++].set(face[1]).subi(face[0]).muli(alpha).addi(face[0]);
		}

		// Assign our new converted values
		face[0] = out[0];
		face[1] = out[1];

		// assert( sp != 3 );

		return sp;
	}
}
