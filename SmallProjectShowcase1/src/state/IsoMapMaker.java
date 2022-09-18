package state;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import input.Button;
import input.InputManager;
import input.ToggleButton;
import main.MainPanel;
import util.GraphicsTools;
import util.Mat2;
import util.MathTools;
import util.Vec2;

public class IsoMapMaker extends State {

	InputManager im;

	public static int spriteScale = 2;
	public static int originalSpriteSize = 32;
	public static int spriteSize = spriteScale * originalSpriteSize;
	public static Mat2 isoTransform = new Mat2(1 * spriteSize / 2d, -1 * spriteSize / 2d, 0.5 * spriteSize / 2d,
			0.5 * spriteSize / 2d);
	public static Mat2 invTransform = isoTransform.inv();

	ArrayList<BufferedImage> sprites;
	ArrayList<Block> blocks;

	Vec2 isoMouse = new Vec2(0, 0);
	Vec2 prevMouse = new Vec2(0, 0);

	Vec2 camera = new Vec2(0, 0); // in real space

	boolean leftMouse = false;
	boolean rightMouse = false;

	public IsoMapMaker(StateManager gsm) {
		super(gsm);

		im = new InputManager();

		ArrayList<BufferedImage> originalSprites = GraphicsTools.loadAnimation("various_tiles.png", originalSpriteSize, originalSpriteSize);
		sprites = new ArrayList<>();
		for (int i = 0; i < originalSprites.size(); i++) {
			BufferedImage img = originalSprites.get(i);
			sprites.add(GraphicsTools.scaleImage(img, spriteScale));
		}

		blocks = new ArrayList<>();

		for (int i = 0; i < 1; i++) {
			for (int j = 0; j < 1; j++) {
//				blocks.add(new Block(i, j, -100 * Math.random() - 10, (int) (Math.random() * sprites.size())));
				blocks.add(new Block(i, j, -100 * Math.random() - 10, 10));
			}
		}
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public void tick(Point mouse2) {
		im.tick(mouse2);

		double dx = (double) mouse2.x - prevMouse.x;
		double dy = (double) mouse2.y - prevMouse.y;
		if (rightMouse) { // controlling camera
			camera.subi(invTransform.mul(new Vec2(dx, dy)));
		}
		prevMouse.set(mouse2.x, mouse2.y);

		Vec2 nextMouse = new Vec2(mouse2.x - MainPanel.WIDTH / 2, mouse2.y - MainPanel.HEIGHT / 2);
		isoMouse = invTransform.mul(nextMouse).addi(camera);
		// isoMouse.x = Math.floor(isoMouse.x);
		// isoMouse.y = Math.floor(isoMouse.y);

		// upd blocks
		for (Block b : blocks) {
			// double nextZ = Math.sin((b.x + phaseCounter) / 3d) - Math.cos((b.y +
			// phaseCounter) / 3d);
			double nextZ = 0;
			double dist = MathTools.dist(isoMouse.x, isoMouse.y, b.x, b.y);
			if (b.x == Math.floor(isoMouse.x) && b.y == Math.floor(isoMouse.y)) {
				nextZ += 0.5;
			}

			double zdist = nextZ - b.z;
			b.z += zdist * 0.2;
		}
	}

	public void sortBlocks(ArrayList<Block> blocks) {
		blocks.sort((a, b) -> a.x == b.x ? (a.y == b.y ? Double.compare(a.z, b.z) : Integer.compare(a.y, b.y))
				: Integer.compare(a.x, b.x));
	}

	@Override
	public void draw(Graphics g) {
		// background
		g.fillRect(0, 0, MainPanel.WIDTH, MainPanel.HEIGHT);

		GraphicsTools.enableAntialiasing(g);
		Vec2 cameraTranslate = isoTransform.mul(camera).mul(-1);
		cameraTranslate.addi(new Vec2(MainPanel.WIDTH / 2, MainPanel.HEIGHT / 2));	//center camera positioning to center of screen
		cameraTranslate.x += -spriteSize / 2; // correct for sprite positioning in image
		g.translate((int) cameraTranslate.x, (int) cameraTranslate.y);

		// drawing blocks
		Graphics2D g2 = (Graphics2D) g;

		ArrayList<Block> renderedBlocks = new ArrayList<>();
		renderedBlocks.addAll(this.blocks);

		Block ghostBlock = new Block((int) Math.floor(isoMouse.x), (int) Math.floor(isoMouse.y), 0, 1);
		ghostBlock.alpha = 0.4;
		renderedBlocks.add(ghostBlock);

		sortBlocks(renderedBlocks);
		for (Block b : renderedBlocks) {
			b.draw(g);
		}

		g.translate((int) -cameraTranslate.x, (int) -cameraTranslate.y);
		g.drawString(isoMouse.x + " " + isoMouse.y, 10, 20);

		im.draw(g);
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
		String which = im.mouseClicked(arg0);
		which = which == null ? "" : which;

		switch (which) {
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		if (im.mousePressed(arg0)) {
			return;
		}
		if (arg0.getButton() == MouseEvent.BUTTON1) {
			this.leftMouse = true;
		}
		if (arg0.getButton() == MouseEvent.BUTTON3) {
			this.rightMouse = true;
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		im.mouseReleased(arg0);
		if (arg0.getButton() == MouseEvent.BUTTON1) {
			this.leftMouse = false;
		}
		if (arg0.getButton() == MouseEvent.BUTTON3) {
			this.rightMouse = false;
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		// TODO Auto-generated method stub

	}

	class Block {

		public int x, y;// real space coordinates
		public double z;
		public double alpha = 1;
		public int type;

		public Block(int x, int y, double z, int type) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.type = type;
		}

		// make sure to apply camera transformation to graphics object before calling
		// this
		public void draw(Graphics g) {
			Vec2 pos = new Vec2(x, y);
			pos = isoTransform.mul(pos);

			Graphics2D g2 = (Graphics2D) g;
			g2.setComposite(GraphicsTools.makeComposite(alpha));
			g.drawImage(sprites.get(type), (int) pos.x, (int) pos.y - (int) (z * (double) spriteSize / 2), null);
			g2.setComposite(GraphicsTools.makeComposite(1));
		}
	}

}
