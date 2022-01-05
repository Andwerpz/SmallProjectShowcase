package state;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import input.InputManager;
import input.Button;
import input.SliderButton;
import input.TextField;
import input.ToggleButton;
import main.MainPanel;
import util.ScrollWindow;
import util.TextBox;

public class MenuState extends State{
	
	MainScrollWindow sw;

	public MenuState(StateManager gsm) {
		super(gsm);
		
		sw = new MainScrollWindow(10, 10, MainPanel.WIDTH - 20, MainPanel.HEIGHT - 20, 800);
		
	}

	@Override
	public void init() {
		
	}

	@Override
	public void tick(Point mouse) {

		sw.tick(mouse);
		
	}

	@Override
	public void draw(Graphics g) {
		
		sw.draw(g);
		
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		sw.keyPressed(arg0);
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		sw.keyReleased(arg0);
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		sw.keyTyped(arg0);
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		sw.mouseClicked(arg0);
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		sw.mousePressed(arg0);
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		sw.mouseReleased(arg0);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		sw.mouseWheelMoved(arg0);
	}

}

class MainScrollWindow extends ScrollWindow{
	
	String[] projects = new String[] {"Inverse Kinematics", "Peggle", "Metaballs", "Fireworks", "Verlet Physics"};

	public MainScrollWindow(int x, int y, int width, int height, int realHeight) {
		super(x, y, width, height, realHeight);
		
		for(int i = 0; i < projects.length; i++) {
			im.addInput(new Button(20, i * 100 + 20, 150, 25, projects[i], projects[i]));
		}
	}

	@Override
	public void repaint(Graphics g, BufferedImage b) {
		im.draw(g);
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0) {
		if(this.containsPoint(mouse)) {

			String which = im.mouseClicked(convertMouseEvent(arg0));
			
			if(which == null) {
				return;
			}
			
			switch(which) {
			case "Inverse Kinematics":
				MainPanel.gsm.states.push(new InverseKinematics(MainPanel.gsm));
				break;
				
			case "Metaballs":
				MainPanel.gsm.states.push(new Metaballs(MainPanel.gsm));
				break;
				
			case "Fireworks":
				MainPanel.gsm.states.push(new Fireworks(MainPanel.gsm));
				break;
				
			case "Verlet Physics":
				MainPanel.gsm.states.push(new VerletPhysics(MainPanel.gsm));
				break;
			}
		}
	}
	
}
