package state;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import input.InputManager;
import input.SliderButton;
import main.Main;
import main.MainPanel;
import myutils.graphics.GraphicsTools;

public class AlphaBlendingTest extends State {
	
	private String testString = "AaBbCcDdEeFfGg12345";
	private Font testFont = new Font("Dialogue", Font.PLAIN, 24);
	private Color backgroundColor = Color.red;
	
	private InputManager im;

	public AlphaBlendingTest(StateManager gsm) {
		super(gsm);
		
		this.im = new InputManager();
		this.im.addInput(new SliderButton(10, 20, 100, 10, 12, 100, "Font Size", "sbtn_font_size"));
		this.im.setVal("sbtn_font_size", 52);
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tick(Point mouse2) {
		this.im.tick(mouse2);
	}

	@Override
	public void draw(Graphics g) {
		this.im.draw(g);
		
		g.setColor(backgroundColor);
		g.fillRect(50, 50, MainPanel.WIDTH - 100, MainPanel.HEIGHT - 100);
		
		//gradients
		g.setColor(Color.CYAN);
		
		GraphicsTools.enableTextAntialiasing(g);
		GraphicsTools.drawCenteredString(MainPanel.WIDTH / 2, MainPanel.HEIGHT / 2, g, testFont.deriveFont((float) this.im.getVal("sbtn_font_size")), testString, Color.WHITE);
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
		this.im.mouseClicked(arg0);
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
		this.im.mousePressed(arg0);
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		this.im.mouseReleased(arg0);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
