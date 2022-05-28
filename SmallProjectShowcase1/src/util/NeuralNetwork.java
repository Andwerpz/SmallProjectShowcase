package util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

public class NeuralNetwork {
	
	public double learningRate = 0.001;

	public int inputSize;
	public int outputSize;
	
	public ArrayList<Layer> layers;

	public NeuralNetwork(int inputSize, int outputSize) {
		this.inputSize = inputSize;
		this.outputSize = outputSize;
		this.layers = new ArrayList<>();
	}

	public double[] forwardPropogate(double[] input) {
		
		this.layers.get(0).forwardPropogate(input);
		
		for(int i = 1; i < this.layers.size(); i++) {
			this.layers.get(i).forwardPropogate(this.layers.get(i - 1));
		}
		
		FCLayer fl = (FCLayer) this.layers.get(this.layers.size() - 1);
		
		double[] ans = new double[outputSize];
		
		for(int i = 0; i < outputSize; i++) {
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
