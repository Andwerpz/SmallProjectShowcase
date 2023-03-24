package state;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.HashSet;

import input.InputManager;
import input.ToggleButton;
import myutils.v10.math.Vec2;
import myutils.v10.math.MathUtils;

public class ConvexPartitioning extends State {

	//generates optimal convex partitions for polygons

	private InputManager im;

	public static int controlPointSize = 8;
	public static boolean drawControlPoints = false;
	public static boolean drawTrianglePartitions = true;
	public static boolean drawConvexPartitions = true;
	public static boolean drawPolygons = false;

	private ArrayList<Vec2> nextPolygonPoints; //ones that are currently being drawn

	private ArrayList<Vec2> controlPoints;
	private ArrayList<Polygon> polygons;

	private boolean drawingPolygon = false;

	public ConvexPartitioning(StateManager gsm) {
		super(gsm);
		this.nextPolygonPoints = new ArrayList<>();
		this.controlPoints = new ArrayList<>();
		this.polygons = new ArrayList<>();

		this.im = new InputManager();
		this.im.addInput(new ToggleButton(10, 20, 150, 25, "Draw Vertices", "tbtn_draw_vertices"));
		this.im.addInput(new ToggleButton(10, 50, 150, 25, "Draw Polygon Edges", "tbtn_draw_edges"));
		this.im.addInput(new ToggleButton(10, 80, 150, 25, "Draw Triangle Partitions", "tbtn_draw_tris"));
		this.im.addInput(new ToggleButton(10, 110, 150, 25, "Draw Convex Partitions", "tbtn_draw_convex"));

		this.im.setToggled("tbtn_draw_vertices", true);
		this.im.setToggled("tbtn_draw_edges", true);
		this.im.setToggled("tbtn_draw_tris", true);
		this.im.setToggled("tbtn_draw_convex", true);
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public void tick(Point mouse2) {
		Vec2 mousePos = new Vec2(mouse2.x, mouse2.y);
		this.im.tick(mouse2);

		drawControlPoints = this.im.getToggled("tbtn_draw_vertices");
		drawTrianglePartitions = this.im.getToggled("tbtn_draw_tris");
		drawConvexPartitions = this.im.getToggled("tbtn_draw_convex");
		drawPolygons = this.im.getToggled("tbtn_draw_edges");

		if (this.drawingPolygon) {
			this.nextPolygonPoints.get(this.nextPolygonPoints.size() - 1).set(mousePos);
		}

		for (Polygon p : this.polygons) {
			p.tick();
		}
	}

	@Override
	public void draw(Graphics g) {
		for (Polygon p : this.polygons) {
			p.draw(g);
		}

		for (int i = 0; i < this.nextPolygonPoints.size() - 1; i++) {
			Vec2 a = this.nextPolygonPoints.get(i);
			Vec2 b = this.nextPolygonPoints.get((i + 1) % this.nextPolygonPoints.size());

			g.setColor(Color.BLACK);
			g.drawLine((int) a.x, (int) a.y, (int) b.x, (int) b.y);
		}

		for (int i = 0; i < this.nextPolygonPoints.size(); i++) {
			Vec2 v = this.nextPolygonPoints.get(i);

			int cx = (int) v.x - ConvexPartitioning.controlPointSize / 2;
			int cy = (int) v.y - ConvexPartitioning.controlPointSize / 2;

			g.setColor(Color.WHITE);
			g.fillRect(cx, cy, ConvexPartitioning.controlPointSize, ConvexPartitioning.controlPointSize);

			g.setColor(Color.BLACK);
			g.drawRect(cx, cy, ConvexPartitioning.controlPointSize, ConvexPartitioning.controlPointSize);
		}

		g.setColor(Color.BLACK);
		g.drawString("LEFT CLICK to start drawing a polygon, and RIGHT CLICK to end it.", 10, 10);

		this.im.draw(g);
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
		if (this.im.mousePressed(arg0)) {
			return;
		}

		boolean leftDown = arg0.getButton() == MouseEvent.BUTTON1;
		boolean rightDown = arg0.getButton() == MouseEvent.BUTTON3;

		Vec2 mousePos = new Vec2(arg0.getX(), arg0.getY());

		if (this.drawingPolygon) {
			float epsilon = 0.01f;

			if (leftDown) {
				//check whether the new line intersects with any other line
				Vec2 a = new Vec2(this.nextPolygonPoints.get(this.nextPolygonPoints.size() - 2));
				Vec2 b = new Vec2(this.nextPolygonPoints.get(this.nextPolygonPoints.size() - 1));
				a.addi(new Vec2(a, b).mul(epsilon));
				b.addi(new Vec2(b, a).mul(epsilon));

				for (int i = 0; i < this.nextPolygonPoints.size() - 2; i++) {
					Vec2 u = new Vec2(this.nextPolygonPoints.get(i));
					Vec2 v = new Vec2(this.nextPolygonPoints.get(i + 1));
					u.addi(new Vec2(u, v).mul(epsilon));
					v.addi(new Vec2(v, u).mul(epsilon));

					if (MathUtils.lineSegment_lineSegmentIntersect(a, b, u, v) != null) {
						//crosses another line
						return;
					}
				}

				this.nextPolygonPoints.add(mousePos);
			}
			else if (rightDown) {
				if (this.nextPolygonPoints.size() < 3) {
					return;
				}

				{
					Vec2 a = new Vec2(this.nextPolygonPoints.get(this.nextPolygonPoints.size() - 2));
					Vec2 b = new Vec2(this.nextPolygonPoints.get(this.nextPolygonPoints.size() - 1));
					a.addi(new Vec2(a, b).mul(epsilon));
					b.addi(new Vec2(b, a).mul(epsilon));

					for (int i = 0; i < this.nextPolygonPoints.size() - 2; i++) {
						Vec2 u = new Vec2(this.nextPolygonPoints.get(i));
						Vec2 v = new Vec2(this.nextPolygonPoints.get(i + 1));
						u.addi(new Vec2(u, v).mul(epsilon));
						v.addi(new Vec2(v, u).mul(epsilon));

						if (MathUtils.lineSegment_lineSegmentIntersect(a, b, u, v) != null) {
							//crosses another line
							return;
						}
					}
				}

				{
					Vec2 a = new Vec2(this.nextPolygonPoints.get(this.nextPolygonPoints.size() - 1));
					Vec2 b = new Vec2(this.nextPolygonPoints.get(0));
					a.addi(new Vec2(a, b).mul(epsilon));
					b.addi(new Vec2(b, a).mul(epsilon));

					for (int i = 0; i < this.nextPolygonPoints.size() - 1; i++) {
						Vec2 u = new Vec2(this.nextPolygonPoints.get(i));
						Vec2 v = new Vec2(this.nextPolygonPoints.get(i + 1));
						u.addi(new Vec2(u, v).mul(epsilon));
						v.addi(new Vec2(v, u).mul(epsilon));

						if (MathUtils.lineSegment_lineSegmentIntersect(a, b, u, v) != null) {
							//crosses another line
							return;
						}
					}
				}

				Polygon p = new Polygon(this.nextPolygonPoints);
				this.nextPolygonPoints.clear();
				this.polygons.add(p);

				this.drawingPolygon = false;
			}
		}
		else {
			if (leftDown) {
				this.drawingPolygon = true;

				this.nextPolygonPoints.clear();
				this.nextPolygonPoints.add(new Vec2(mousePos));
				this.nextPolygonPoints.add(new Vec2(mousePos));
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		this.im.mouseReleased(arg0);

		Vec2 mousePos = new Vec2(arg0.getX(), arg0.getY());
		if (this.drawingPolygon) {
			return;
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		// TODO Auto-generated method stub

	}

}

class Polygon {

	protected boolean controlPointHeld = false;
	protected int controlPointHeldIndex = 0;

	protected ArrayList<Vec2> controlPoints;

	protected ArrayList<int[]> concavePartitions;
	protected ArrayList<int[]> trianglePartitions;

	public Polygon(ArrayList<Vec2> controlPoints) {
		this.controlPoints = new ArrayList<>();
		this.concavePartitions = new ArrayList<>();
		this.trianglePartitions = new ArrayList<>();

		this.controlPoints.addAll(controlPoints);
		this.concavePartitions = MathUtils.calculateConvexPartition(this.controlPoints);
		this.trianglePartitions = MathUtils.calculateTrianglePartition(this.controlPoints);
	}

	public void draw(Graphics g) {
		//triangle partitions
		if (ConvexPartitioning.drawTrianglePartitions) {
			for (int[] tri : this.trianglePartitions) {
				for (int i = 0; i < tri.length; i++) {
					Vec2 a = this.controlPoints.get(tri[i]);
					Vec2 b = this.controlPoints.get(tri[(i + 1) % tri.length]);

					g.setColor(Color.RED);
					g.drawLine((int) a.x, (int) a.y, (int) b.x, (int) b.y);
				}
			}
		}

		//concave partitions
		if (ConvexPartitioning.drawConvexPartitions) {
			for (int[] partition : this.concavePartitions) {
				for (int i = 0; i < partition.length; i++) {
					Vec2 a = this.controlPoints.get(partition[i]);
					Vec2 b = this.controlPoints.get(partition[(i + 1) % partition.length]);

					g.setColor(Color.BLUE);
					g.drawLine((int) a.x, (int) a.y, (int) b.x, (int) b.y);
				}
			}
		}

		//polygon bounds
		if (ConvexPartitioning.drawPolygons) {
			for (int i = 0; i < this.controlPoints.size(); i++) {
				Vec2 a = this.controlPoints.get(i);
				Vec2 b = this.controlPoints.get((i + 1) % this.controlPoints.size());

				g.setColor(Color.BLACK);
				g.drawLine((int) a.x, (int) a.y, (int) b.x, (int) b.y);
			}
		}

		//control points
		if (ConvexPartitioning.drawControlPoints) {
			for (int i = 0; i < this.controlPoints.size(); i++) {
				Vec2 v = this.controlPoints.get(i);

				int cx = (int) v.x - ConvexPartitioning.controlPointSize / 2;
				int cy = (int) v.y - ConvexPartitioning.controlPointSize / 2;

				g.setColor(Color.WHITE);
				g.fillRect(cx, cy, ConvexPartitioning.controlPointSize, ConvexPartitioning.controlPointSize);

				g.setColor(Color.BLACK);
				g.drawRect(cx, cy, ConvexPartitioning.controlPointSize, ConvexPartitioning.controlPointSize);
			}
		}
	}

	public void tick() {

	}

}
