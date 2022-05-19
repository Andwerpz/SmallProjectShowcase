package state;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import main.MainPanel;
import util.GraphicsTools;
import util.Vec2;

public class InverseKinematics extends State {
	
	Segment root, head;
	
	boolean fixPositionFollow = false;

	public InverseKinematics(StateManager gsm) {
		super(gsm);
		
		root = new Segment(new Vec2(MainPanel.WIDTH / 2, MainPanel.HEIGHT / 2), 5, 0);
		root.root = true;
	
		Segment curRoot = root;
		for(int i = 0; i < 50; i++) {
			Segment next = new Segment(curRoot, 5, 0);
			curRoot.setChild(next);
			curRoot = next;
		}
		
		head = curRoot;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tick(java.awt.Point mouse2) {
		//root.update();
		if(fixPositionFollow) {
			head.fixPositionFollow(new Vec2(mouse2.x, mouse2.y));
		}
		else {
			head.follow(new Vec2(mouse2.x, mouse2.y));
		}
		
	}

	@Override
	public void draw(Graphics g) {
		
		GraphicsTools.enableAntialiasing(g);
		
		Graphics2D g2 = (Graphics2D) g;
		//g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		//g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		
		//root.rotate(0.5);
		
		//root.update();
		root.draw(g);
		
		g.drawString("Press and Hold 'F' to fix the root's position", 25, 25);
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		if(arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
			gsm.states.pop();
		}
		
		if(arg0.getKeyCode() == KeyEvent.VK_F) {
			this.fixPositionFollow = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		if(arg0.getKeyCode() == KeyEvent.VK_F) {
			this.fixPositionFollow = false;
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	class Segment {
		
		Vec2 a, b;
		double length;
		double angle, parentAngle;	//degrees
		
		boolean root = false;
		
		Segment child = null;
		Segment parent = null;
		
		public Segment(Vec2 a, double length, double angle) {
			this.a = new Vec2(a);
			this.length = length;
			this.angle = angle;
			this.parentAngle = 0;
			
			this.b = new Vec2(0, 0); calcB();
		}
		
		public Segment(Segment parent, double length, double angle) {
			this.a = new Vec2(parent.b.x, parent.b.y);
			this.length = length;
			this.angle = angle;
			this.parentAngle = parent.angle + parent.parentAngle;
			
			this.b = new Vec2(0, 0); calcB();
			
			this.parent = parent;
		}
		
		public void setChild(Segment child) {
			this.child = child;
			this.update();
		}
		
		public void setParent(Segment parent) {
			this.parent = parent;
		}
		
		public void follow(Vec2 p) {	//segment first turns itself in the direction of p, and then sets b to p. 
			
			if(!this.root) {
				this.parentAngle = this.parent.parentAngle + this.parent.angle;
			}
			
			double newAng = Math.toDegrees(Math.atan2(p.y - a.y, p.x - a.x));
			double diff = newAng - (this.parentAngle + this.angle);
			this.angle += newAng - (this.parentAngle + this.angle);
			
//			if(!this.root) {
//				this.angle = Math.min(this.angle, 90);
//				this.angle = Math.max(this.angle, -90);
//			}
			
			
			this.calcB();
			
			Vec2 toNewVector = new Vec2(this.b, p);
			
			this.b.addi(toNewVector);
			this.a.addi(toNewVector);
			
			if(parent != null) {
				this.parent.follow(a);
			}
		}
		
		
		public void fixPositionFollow(Vec2 p) {
			
			Segment root = this;
			while(root.parent != null) {
				root = root.parent;
			}
			
			Vec2 prevRoot = new Vec2(root.a);
			
			this.follow(p);
			
			Vec2 nextRoot = new Vec2(root.a);
			
			Vec2 offset = new Vec2(nextRoot, prevRoot);
			
			root = this;
			while(true) {
				root.a.addi(offset);
				root.b.addi(offset);
				if(root.parent != null) {
					root = root.parent;
				}
				else {
					break;
				}
			}
		}
		
		public void update() {
			calcB();
			
			if(this.child != null) {
				this.child.a.x = this.b.x;
				this.child.a.y = this.b.y;
				
				this.child.parentAngle = this.parentAngle + this.angle;
				
				this.child.update();
			}
		}
		
		public void draw(Graphics g) {
			//System.out.println(this.angle);
			
			g.drawLine((int) a.x, (int) a.y, (int) b.x, (int) b.y);
			
			if(this.child != null) {
				this.child.draw(g);
			}
		}
		
		public void rotate(double deg) {
			this.angle += deg;
		}
		
		public void calcB() {
			this.b.x = this.a.x + this.length * Math.cos(Math.toRadians(this.angle + this.parentAngle));
			this.b.y = this.a.y + this.length * Math.sin(Math.toRadians(this.angle + this.parentAngle));
		}
		
	}

}