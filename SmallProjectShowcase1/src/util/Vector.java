package util;

import java.awt.Point;

public class Vector extends util.Point{
	
	public Vector() {
		super(0, 0);
	}

	public Vector(double x, double y) {
		super(x, y);
	}
	
	//pointing from point a to point b
	
	public Vector(util.Point a, util.Point b) {
		super(b.x - a.x, b.y - a.y);
	}
	
	public Vector(Vector a) {	//so you don't have any pointer issues
		super(a.x, a.y);
	}
	
	public Vector(util.Point a) {
		super(a.x, a.y);
	}

	public double getMagnitude() {
		return Math.sqrt(this.x * this.x + this.y * this.y);
	}
	
	public double getMagnitudeSquared() {
		return this.x * this.x + this.y * this.y;
	}
	
	public void normalize() {
		double mag = this.getMagnitude();
		this.x /= mag;
		this.y /= mag;
	}
	
	public void setMagnitude(double mag) {
		this.normalize();
		this.x *= mag;
		this.y *= mag;
	}
	
	public void rotateCounterClockwise(double rad) {
		double x = this.x;
		double y = this.y;
		this.x = (x * Math.cos(rad)) - (y * Math.sin(rad));
		this.y = (x * Math.sin(rad)) + (y * Math.cos(rad));
	}
	
	public double getRotationRadians() {
		return Math.asin(this.y / this.getMagnitude());
	}
	
	public Vector addi(Vector v) {
		this.x += v.x;
		this.y += v.y;
		return this;
	}
	
	public Vector add(Vector v) {
		return new Vector(this.x + v.x, this.y + v.y);
	}
	
	public Vector muli(double val) {
		this.x *= val;
		this.y *= val;
		return this;
	}
	
	public Vector mul(double val) {
		return new Vector(this.x * val, this.y * val);
	}


	public Vector set(double x, double y) {
		this.x = x;
		this.y = y;
		return this;
	}
	
	public Vector set(Vector vector) {
		this.x = vector.x;
		this.y = vector.y;
		return this;
	}
	
	public Vector neg() {
		return new Vector(-this.x, -this.y);
	}
	
	public Vector negi() {
		this.x = -this.x;
		this.y = -this.y;
		return this;
	}

	public Vector subi(Vector vector) {
		this.x -= vector.x;
		this.y -= vector.y;
		return this;
	}
	
	public Vector sub(Vector vector) {
		return new Vector(this.x - vector.x, this.y - vector.y);
	}

	public Vector divi(double val) {
		this.x /= val;
		this.y /= val;
		return this;
	}
	
	public Vector div(double val) {
		return new Vector(this.x / val, this.y / val);
	}

}
