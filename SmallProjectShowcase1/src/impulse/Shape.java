package impulse;

import util.Mat2;

public abstract class Shape {
	public enum Type {
		Circle, Poly, Count
	}

	public Body body;
	public double radius;
	public final Mat2 u = new Mat2();

	public Shape() {
	}

	public abstract Shape clone();

	public abstract void initialize();

	public abstract void computeMass(double density);

	public abstract void setOrient(double radians);

	public abstract Type getType();
}
