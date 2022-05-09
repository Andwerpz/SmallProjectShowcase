package util;

public abstract class Layer {
	
	public abstract void forwardPropogate(double[] input);
	public abstract void forwardPropogate(double[][] input);
	public abstract void forwardPropogate(Layer l);
	public abstract void backPropogate(Layer l);
	
	public abstract void calculateActivation();
	public abstract void calculateDerivatives();
}
