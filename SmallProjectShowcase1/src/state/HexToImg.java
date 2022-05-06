package state;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import input.InputManager;
import input.SliderButton;
import main.MainPanel;

public class HexToImg extends State {
	
	InputManager im;
	
	public int width = 200;
	public int height;
	
	public int numSkip = 21;
	
	public int[] nums;

	public HexToImg(StateManager gsm) {
		super(gsm);
		
		this.nums = loadText();
		
		this.im = new InputManager();
		this.im.addInput(new SliderButton(10, MainPanel.HEIGHT - 100, 300, 10, 1, 250, "Width", "slider_width"));
		this.im.addInput(new SliderButton(10, MainPanel.HEIGHT - 50, 300, 10, 0, 100, "Skip", "slider_skip"));
		//barken
	}
	
	public static int[] loadText() {
		ArrayList<Integer> a = new ArrayList<>();
		try {
			//BufferedReader fin = new BufferedReader(new FileReader("/testbmp.txt"));
			BufferedReader fin = new BufferedReader(new FileReader("C:\\-=+GAME+=-\\-- Github --\\SmallProjectShowcase\\SmallProjectShowcase1\\res\\testbmp.txt"));
			
			//read in one line
			char[] line = fin.readLine().toCharArray();
			
			for(int i = 0; i + 1 < line.length; i += 2) {
				char first = line[i];
				char second = line[i + 1];
				
				if(first == 'x' || second == 'x') {	//checking for 0x
					continue;
				}
				
				int next = Integer.parseInt(first + "" + second, 16);
				a.add(next);
			}
			
			fin.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int[] nums = new int[a.size()];
		
		for(int i = 0; i < a.size(); i++) {
			nums[i] = a.get(i);
		}
		
		return nums;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tick(Point mouse2) {
		im.tick(mouse2);
		
		this.width = im.getVal("slider_width");
		this.numSkip = im.getVal("slider_skip");
	}

	@Override
	public void draw(Graphics g) {
		
		int pixelWidth = 4;
		int pixelHeight = 1;
		
		boolean flipImg = true;
		
		int counter = numSkip;
		int y = 0;
		outer:
		while(true) {
			for(int i = 0; i < width; i++) {
				int x = i * pixelWidth;
				
				//load the next 3 nums into rgb and draw
				int red = nums[counter];
				int green = nums[counter + 1];
				int blue = nums[counter + 2];
				
				int rgb = (red << 16) + (green << 8) + blue;
				
				g.setColor(new Color(rgb));
				if(flipImg) {
					g.fillRect(x, 300 - y, pixelWidth, pixelHeight);
				}
				else {
					g.fillRect(x, y, pixelWidth, pixelHeight);
				}
				
				counter += 3;
				if(counter + 2 >= nums.length) {
					break outer;
				}
			}
			y += pixelHeight;
		}
		
		im.draw(g);
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		im.keyPressed(arg0);
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		im.keyReleased(arg0);
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
