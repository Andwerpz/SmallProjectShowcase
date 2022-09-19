package input;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class InputManager {

	public ArrayList<Input> inputs;
	
	public InputManager() {
		this.inputs = new ArrayList<Input>();
	}
	
	public void addInput(Input i) {
		this.inputs.add(i);
	}
	
	public void tick(java.awt.Point mouse) {
		for(Input i : inputs) {
			i.hovering(mouse);
			i.tick(mouse);
		}
	}
	
	public void draw(Graphics g) {
		for(Input i : inputs) {
			i.draw(g);
		}
	}
	
	//gets value of slider button
	public int getVal(String name) {
		for(Input i : inputs) {
			if(i instanceof SliderButton && i.name.equals(name)) {
				return ((SliderButton) i).getVal();
			}
		}
		return -1;
	}
	
	//sets value of slider button
	public void setVal(String name, int val) {
		for(Input i : inputs) {
			if(i instanceof SliderButton && i.name.equals(name)) {
				((SliderButton) i).setVal(val);
			}
		}
	}
	
	//gets toggled state of toggle button
	public boolean getToggled(String name) {
		for(Input i : inputs) {
			if(i instanceof ToggleButton && i.name.equals(name)) {
				return ((ToggleButton) i).getToggled();
			}
		}
		return false;
	}
	
	public void setToggled(String name, boolean toggle) {
		for(Input i : inputs) {
			if(i instanceof ToggleButton && i.name.equals(name)) {
				((ToggleButton) i).setToggled(toggle);
			}
		}
	}
	
	//gets text from text input
	public String getText(String name) {
		for(Input i : inputs) {
			if(i instanceof TextField && i.name.equals(name)) {
				return ((TextField) i).getText();
			}
		}
		return null;
	}
	
	/**
	 * returns true if an input is pressed
	 * @param arg0
	 * @return
	 */
	
	public boolean mousePressed(MouseEvent arg0) {
		boolean pressed = false;
		for(Input i : inputs) {
			pressed |= i.pressed(arg0);
		}
		return pressed;
	}
	
	public void mouseReleased(MouseEvent arg0) {
		for(Input i : inputs) {
			i.released(arg0);
		}
	}
	
	public String mouseClicked(MouseEvent arg0) {
		String name = null;
		for(Input i : inputs) {
			if(i.clicked(arg0)) {
				name = i.name;
			}
		}
		return name;
	}
	
	public void keyPressed(KeyEvent arg0) {
		for(Input i : inputs) {
			i.keyPressed(arg0);
		}
	}
	
	public void keyTyped(KeyEvent arg0) {
		for(Input i : inputs) {
			i.keyTyped(arg0);
		}
	}
	
	public void keyReleased(KeyEvent arg0) {
		for(Input i : inputs) {
			i.keyReleased(arg0);
		}
	}
	
}
