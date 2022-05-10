package util;

public class FCLayer extends Layer{
	
	public static final int ACTIVATION_TYPE_SIGMOID = 0;
	public static final int ACTIVATION_TYPE_RELU = 1;
	public static final int ACTIVATION_TYPE_LINEAR = 2;
	
	//ACTIVATION
	int inputNodeAmt;
	
	public double[] inputNodes;
	public double[] inputNodeDerivatives;
	
	int outputNodeAmt;
	
	public double[] outputNodes;
	public double[] outputNodeDerivatives;
	
	//WEIGHTS
	public double[][] weights;
	public double[][] weightDerivatives;
	
	public int activationType;
	
	public FCLayer(int inputNodeAmt, int outputNodeAmt, int activationType) {
		
		this.activationType = activationType;
		
		this.inputNodeAmt = inputNodeAmt;
		this.outputNodeAmt = outputNodeAmt;
		
		this.generate(inputNodeAmt, outputNodeAmt);
		
		System.out.println(inputNodeAmt + " " + outputNodeAmt);
	}
	
	public void generate(int inputNodeAmt, int outputNodeAmt) {
		this.inputNodes = new double[inputNodeAmt];
		this.outputNodes = new double[outputNodeAmt];
		this.weights = new double[inputNodeAmt][outputNodeAmt];
		
		this.outputNodeDerivatives = new double[this.outputNodeAmt];
		this.inputNodeDerivatives = new double[this.inputNodeAmt];
		this.weightDerivatives = new double[this.inputNodeAmt][this.outputNodeAmt];
		
		for(int node = 0; node < inputNodeAmt; node++) {
			for(int weight = 0; weight < outputNodeAmt; weight++) {
				double nextWeight = 0;
				if(this.activationType == FCLayer.ACTIVATION_TYPE_SIGMOID) {
					double range = 1d / Math.sqrt(inputNodeAmt);
					nextWeight = (Math.random() * range * 2) - range;
				}
				else if(this.activationType == FCLayer.ACTIVATION_TYPE_RELU) {
					nextWeight = Math.random() * (Math.sqrt(2) / (double) this.inputNodeAmt);
				}
				else if(this.activationType == FCLayer.ACTIVATION_TYPE_LINEAR) {
					nextWeight = 0;
				}
				this.weights[node][weight] = nextWeight;
				//System.out.println(nextWeight);
			}
		}
	}
	
	public double getCost(double ans) {
		double cost = 0;
		for(int node = 0; node < this.outputNodes.length; node++) {
			if(this.outputNodeAmt == 1) {
				cost += Math.pow(ans - outputNodes[node], 2);
			}
			else {
				cost += Math.pow(((int) ans == node? 1d : 0d) - outputNodes[node], 2);
			}
		}
		
		return cost;
	}
	
	public void forwardPropogate(double[] input) {
		//clear activation
		this.outputNodes = new double[outputNodeAmt];
		this.inputNodes = new double[inputNodeAmt];
		
		int inputSize = input.length;
		
		for(int i = 0; i < inputSize; i++) {
			this.inputNodes[i] = input[i];
		}
		
		this.calculateActivation();
	}
	
	@Override
	public void forwardPropogate(double[][] input) {
		//clear activation
		this.outputNodes = new double[outputNodeAmt];
		this.inputNodes = new double[inputNodeAmt];
		
		int inputSize = input.length;
		
		for(int r = 0; r < inputSize; r++) {
			for(int c = 0; c < inputSize; c++) {
				this.inputNodes[r * inputSize + c] = input[r][c];
				//System.out.print(this.inputNodes[r * inputSize + c] + " ");
			}
			//System.out.println();
		}
		
		this.calculateActivation();
	}

	@Override
	public void forwardPropogate(Layer l) {
		
		//clear activation
		this.outputNodes = new double[outputNodeAmt];
		this.inputNodes = new double[inputNodeAmt];
		
		if(l instanceof FCLayer) {
			this.inputNodes = ((FCLayer) l).outputNodes;
		}
		
		this.calculateActivation();
	}
	
	public void calculateActivation() {
		//loop through input nodes
		for(int node = 0; node < this.inputNodes.length; node++) {
			double val = this.inputNodes[node];
			double[] curWeights = this.weights[node];
			
			//loop through output nodes
			for(int weight = 0; weight < this.outputNodes.length; weight++) {
				this.outputNodes[weight] += val * curWeights[weight];
				//System.out.println(curWeights[weight]);
			}
			
		}
		
		for(int node = 0; node < this.outputNodes.length; node++) {
			//activation function
			
			if(this.activationType == FCLayer.ACTIVATION_TYPE_SIGMOID) {
				this.outputNodes[node] = MathTools.sigmoid(this.outputNodes[node]);
			}
			else if(this.activationType == FCLayer.ACTIVATION_TYPE_RELU) {
				this.outputNodes[node] = MathTools.relu(this.outputNodes[node]);
			}
			else if(this.activationType == FCLayer.ACTIVATION_TYPE_LINEAR) {
				this.outputNodes[node] = this.outputNodes[node];
			}
			//System.out.println(this.outputNodes[node] + " ");
		}
		//System.out.println();
	}
	
	//if this layer is the output layer
	public void backPropogate(double[] ans) {
		
		//clear derivatives
		this.outputNodeDerivatives = new double[this.outputNodeAmt];
		this.inputNodeDerivatives = new double[this.inputNodeAmt];
		this.weightDerivatives = new double[this.inputNodeAmt][this.outputNodeAmt];
		
		//calc output node derivatives
		for(int node = 0; node < this.outputNodes.length; node++) {
			//calc cost function derivative
			this.outputNodeDerivatives[node] = (this.outputNodes[node] - ans[node]) * 2;
		}
		
//		System.out.println("---------");
//		for(double[] d : this.weights) {
//			for(double i : d) {
//				System.out.println(i);
//			}
//			System.out.println();
//		}
//		System.out.println();
		
		this.calculateDerivatives();
	}

	//loads the derivative of the output of the output nodes to the cost
	@Override
	public void backPropogate(Layer l) {
		
		//clear derivatives
		this.outputNodeDerivatives = new double[this.outputNodeAmt];
		this.inputNodeDerivatives = new double[this.inputNodeAmt];
		this.weightDerivatives = new double[this.inputNodeAmt][this.outputNodeAmt];
		
		if(l instanceof FCLayer) {
			this.outputNodeDerivatives = ((FCLayer) l).inputNodeDerivatives;
		}
		
		this.calculateDerivatives();
	}
	
	@Override
	public void calculateDerivatives() {
		
		//apply sigmoid derivative to output node derivatives
		for(int node = 0; node < this.outputNodeAmt; node++) {
			if(this.activationType == FCLayer.ACTIVATION_TYPE_SIGMOID) {
				//outputNodeDerivatives store the value after applying the sigmoid function
				//since we need the input to calc the derivative, use logit function to get the input
				this.outputNodeDerivatives[node] *= MathTools.sigmoidDerivative(MathTools.logit(this.outputNodes[node]));
			}
			else if(this.activationType == FCLayer.ACTIVATION_TYPE_RELU) {
				//if the node output == 0, then the derivative = 0, else, derivative = 1.
				this.outputNodeDerivatives[node] *= MathTools.reluDerivative(this.outputNodes[node]);
			}
			else if(this.activationType == FCLayer.ACTIVATION_TYPE_LINEAR) {
				//derivative is always 1
				this.outputNodeDerivatives[node] *= 1;
			}
		}
		
		
		//calc weight and input node derivatives
		for(int node = 0; node < this.inputNodes.length; node++) {
			double[] curWeights = this.weights[node];
			double[] curWeightDerivatives = this.weightDerivatives[node];
			
			//loop through weights
			for(int weight = 0; weight < curWeights.length; weight++) {
				this.inputNodeDerivatives[node] += this.outputNodeDerivatives[weight] * curWeights[weight];
				curWeightDerivatives[weight] = this.outputNodeDerivatives[weight] * this.inputNodes[node];
			}
		}
		
		//adjust weights
		for(int node = 0; node < this.weights.length; node++) {
			for(int weight = 0; weight < this.weights[0].length; weight++) {
				this.weights[node][weight] -= this.weightDerivatives[node][weight] * NeuralNetwork.LEARNING_RATE;
			}
		}
	}

}
