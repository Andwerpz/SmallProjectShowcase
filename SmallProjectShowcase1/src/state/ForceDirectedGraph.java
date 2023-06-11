package state;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.AreaAveragingScaleFilter;
import java.util.ArrayList;

import main.MainPanel;
import myutils.v10.algorithm.Graph;
import myutils.v10.graphics.GraphicsTools;
import myutils.v10.math.Vec2;
import util.MathTools;

public class ForceDirectedGraph extends State {

	private ArrayList<Node> nodes;
	private ArrayList<Edge> edges;

	private float dt = 1f;
	private int iterations = 10;
	private float repulsion = 0.5f;
	private float centerStr = 0.5f;
	private float edgeLength;
	private float edgeStrength;

	private Graph<Node> graph;

	private int grabbedNodeIndex = -1;

	private boolean mousePressed = false;

	public ForceDirectedGraph(StateManager gsm) {
		super(gsm);
	}

	@Override
	public void init() {
		this.nodes = new ArrayList<>();
		this.edges = new ArrayList<>();

		this.edgeLength = 70f;
		this.edgeStrength = 2f;

		this.graph = new Graph<Node>();

		int nrNodes = 30;
		for (int i = 0; i < nrNodes; i++) {
			float x = (float) (Math.random() * 400) - 150f + MainPanel.WIDTH / 2;
			float y = (float) (Math.random() * 400) - 150f + MainPanel.HEIGHT / 2;
			Node n = new Node(i, x, y);
			this.nodes.add(n);
			this.graph.addNode(n);
		}

		float edgePerNode = 2;
		float edgeProb = edgePerNode / nrNodes;
		for (int i = 0; i < nrNodes; i++) {
			for (int j = 0; j < i; j++) {
				if (Math.random() < edgeProb) {
					Edge e = new Edge(this.edgeStrength, this.edgeLength, this.nodes.get(i), this.nodes.get(j));
					this.edges.add(e);
					this.graph.addEdge(this.nodes.get(i), this.nodes.get(j));
				}
			}
		}
	}

	@Override
	public void tick(Point mouse2) {
		//grabbed node
		if (this.mousePressed) {
			Vec2 mousePos = new Vec2(mouse2.x, mouse2.y);
			if (this.grabbedNodeIndex != -1) {
				Node a = this.nodes.get(this.grabbedNodeIndex);

				a.pos.set(mousePos);
			}
		}

		for (int i = 0; i < this.iterations; i++) {
			this.iterate();
		}

	}

	private void iterate() {
		for (Edge e : this.edges) {
			e.tick();
		}

		//each node repels every other non adjacent node
		for (int i = 0; i < this.nodes.size(); i++) {
			for (int j = 0; j < i; j++) {
				Node a = this.nodes.get(i);
				Node b = this.nodes.get(j);

				if (this.graph.doesEdgeExist(a, b)) {
					continue;
				}

				Vec2 ab = new Vec2(a.pos, b.pos);

				float dist = ab.length() / this.edgeLength;

				float force = this.repulsion / (dist * dist);
				force = Math.min(this.repulsion, force);

				ab.normalize();
				a.applyImpulse(ab.mul(-force));
				b.applyImpulse(ab.mul(force));
			}
		}

		//attract all nodes to the center of the screen
		for (int i = 0; i < this.nodes.size(); i++) {
			Node a = this.nodes.get(i);
			Vec2 toCenter = new Vec2(a.pos, new Vec2(MainPanel.WIDTH / 2, MainPanel.HEIGHT / 2));
			if (toCenter.length() > this.centerStr) {
				toCenter.setLength(this.centerStr);
			}

			a.applyImpulse(toCenter);
		}

		for (Node n : this.nodes) {
			n.tick(this.dt);
		}
	}

	@Override
	public void draw(Graphics g) {
		for (Edge e : this.edges) {
			e.draw(g);
		}

		for (Node n : this.nodes) {
			n.draw(g);
		}
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
		this.mousePressed = true;

		Vec2 mousePos = new Vec2(arg0.getX(), arg0.getY());
		for (int i = this.nodes.size() - 1; i >= 0; i--) {
			Vec2 pos = this.nodes.get(i).pos;
			float dist = pos.distance(mousePos);
			if (dist < this.nodes.get(i).size) {
				this.grabbedNodeIndex = i;
				this.nodes.get(i).pinned = true;
				break;
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		this.mousePressed = false;

		if (this.grabbedNodeIndex != -1) {
			this.nodes.get(this.grabbedNodeIndex).pinned = false;
		}
		this.grabbedNodeIndex = -1;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		// TODO Auto-generated method stub

	}

	class Node {

		public int id;

		public Vec2 pos, acc;

		public float size = 20f;

		public boolean pinned = false;

		public Node(int id, float x, float y) {
			this.id = id;

			this.pos = new Vec2(x, y);
			this.acc = new Vec2(0);
		}

		public void tick(float dt) {
			Vec2 next_pos = this.pos.add(this.acc.mul(dt));

			this.pos.set(next_pos);

			this.acc.set(0, 0);
		}

		public void draw(Graphics g) {
			g.setColor(Color.WHITE);
			g.fillOval((int) (this.pos.x - this.size / 2f), (int) (this.pos.y - this.size / 2f), (int) this.size, (int) this.size);

			g.setColor(Color.BLACK);
			g.drawOval((int) (this.pos.x - this.size / 2f), (int) (this.pos.y - this.size / 2f), (int) this.size, (int) this.size);

			Font f = new Font("Dialogue", Font.PLAIN, 12);
			GraphicsTools.drawCenteredString((int) this.pos.x, (int) (this.pos.y + this.size / 2f), g, f, this.id + "", Color.BLACK);
		}

		public void applyImpulse(Vec2 impulse) {
			this.acc.addi(impulse);
		}
	}

	class Edge {

		double strength; //pretty much spring coeff
		double length; //rest length
		Node a, b;

		public Edge(float strength, double length, Node a, Node b) {
			this.a = a;
			this.b = b;
			this.strength = strength;
			this.length = length;
		}

		public void tick() {
			double dist = MathTools.dist(a.pos.x, a.pos.y, b.pos.x, b.pos.y);
			double diff = dist - length;

			double force = this.strength * Math.log(this.length / dist);

			Vec2 ab = new Vec2(this.a.pos, this.b.pos);
			ab.normalize();

			this.a.applyImpulse(ab.mul(-force));
			this.b.applyImpulse(ab.mul(force));
		}

		public void draw(Graphics g) {
			double dist = MathTools.dist(a.pos.x, a.pos.y, b.pos.x, b.pos.y);
			double diff = this.length - dist;
			double length_tolerance = 5f;

			if (-length_tolerance < diff && diff < length_tolerance) {
				g.setColor(Color.BLACK);
			}
			else if (diff > length_tolerance) { //compressed
				g.setColor(Color.BLUE);
			}
			else { //stretched
				g.setColor(Color.RED);
			}

			g.drawLine((int) a.pos.x, (int) a.pos.y, (int) b.pos.x, (int) b.pos.y);
		}

	}

}
