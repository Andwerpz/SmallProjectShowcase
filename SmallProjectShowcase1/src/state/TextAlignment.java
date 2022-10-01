package state;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;

import input.InputManager;
import input.SliderButton;
import main.MainPanel;
import util.FontUtils;
import util.GraphicsTools;

public class TextAlignment extends State {

	private Font font;
	private int fontSize;
	private String text = "AaBbCcDdEeFfGg12345";

	private InputManager im;

	public TextAlignment(StateManager gsm) {
		super(gsm);
		FontUtils.loadFonts();
		this.font = FontUtils.deriveSize(52, FontUtils.CSGOFont);
		//this.font = new Font("Dialogue", Font.PLAIN, 52);
		
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
		this.fontSize = this.im.getVal("sbtn_font_size");
	}

	@Override
	public void draw(Graphics g) {
		this.im.draw(g);
		this.font = FontUtils.deriveSize(fontSize, font);
		
		GraphicsTools.enableAntialiasing(g);
		g.setColor(Color.RED);
		g.setFont(font);
		g.drawLine(MainPanel.WIDTH / 2, 0, MainPanel.WIDTH / 2, MainPanel.HEIGHT);
		g.drawLine(0, MainPanel.HEIGHT / 2, MainPanel.WIDTH, MainPanel.HEIGHT / 2);

		int textMaxDescent = GraphicsTools.getFontMaxDescent(font);
		int textMaxAscent = GraphicsTools.getFontMaxAscent(font);

		int textWidth = GraphicsTools.calculateTextWidth(text, font);
		int textX = MainPanel.WIDTH / 2 - textWidth / 2;
		int textY = MainPanel.HEIGHT / 2;
		
		int sampleAscent = GraphicsTools.getFontSampleAscent(font);
		textY += sampleAscent / 2;
		
		Graphics2D g2 = (Graphics2D) g;
		Rectangle textBounds = GraphicsTools.getStringBounds(g, text, textX, textY);
		g.setColor(Color.BLUE);
		g2.draw(textBounds);

		g.setColor(Color.BLACK);
		// baseline
		g.drawLine(0, textY, MainPanel.WIDTH, textY);

		// maxDescent
		g.drawLine(0, textY + textMaxDescent, MainPanel.WIDTH, textY + textMaxDescent);

		// maxAscent
		g.drawLine(0, textY - textMaxAscent, MainPanel.WIDTH, textY - textMaxAscent);
		
		Rectangle imgTexBounds = new Rectangle(textX, textY - textMaxAscent, textWidth, textMaxAscent + textMaxDescent);
		g.setColor(Color.GREEN);
		g2.draw(imgTexBounds);

		g.setColor(Color.BLACK);
		g.setFont(font);
		g.drawString(text, textX, textY);
		
//		BufferedImage textImg = GraphicsTools.generateTextImage(text, font, Color.BLACK);
//		g.drawImage(textImg, textX, textY, null);
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
