package util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

public class NeuralNetwork {
	
	public static final double LEARNING_RATE = 0.0001;
	
	//INPUT: 
	//diff from goal
	//normalized vector to goal
	//velocity
	//rotation vector
	public static final int INPUT_SIZE = 35;
	
	//OUTPUT:
	//accelerate and turn left
	//accelerate and turn right
	//accelerate
	//turn left
	//turn right
	//idle
	//reverse
	public static final int OUTPUT_SIZE = 7;
	
	public ArrayList<Layer> layers;

	public NeuralNetwork() {
		this.generateNetwork();
	}

	public void generateNetwork() {
		this.layers = new ArrayList<>();
		
		this.layers.add(new FCLayer(INPUT_SIZE, 40, FCLayer.ACTIVATION_TYPE_SIGMOID));
		this.layers.add(new FCLayer(40, 30, FCLayer.ACTIVATION_TYPE_SIGMOID));
		this.layers.add(new FCLayer(30, OUTPUT_SIZE, FCLayer.ACTIVATION_TYPE_SIGMOID));
		
	}

	public double[] forwardPropogate(double[] input) {
		
		this.layers.get(0).forwardPropogate(input);
		
		for(int i = 1; i < this.layers.size(); i++) {
			this.layers.get(i).forwardPropogate(this.layers.get(i - 1));
		}
		
		FCLayer fl = (FCLayer) this.layers.get(this.layers.size() - 1);
		
		double[] ans = new double[OUTPUT_SIZE];
		
		for(int i = 0; i < OUTPUT_SIZE; i++) {
			ans[i] = fl.outputNodes[i];
		}
		
		return ans;
	}

	
	//does backprop and nudges the weights
	public void backPropogate(double[] input, double[] key) {
		
		//run forward prop to generate the activations
		this.forwardPropogate(input);
		
		FCLayer fl = ((FCLayer) this.layers.get(this.layers.size() - 1));
		
		//run backprop
		fl.backPropogate(key);
		
		for(int i = this.layers.size() - 2; i >= 0; i--) {
			this.layers.get(i).backPropogate(this.layers.get(i + 1));
		}
		
	}

}
