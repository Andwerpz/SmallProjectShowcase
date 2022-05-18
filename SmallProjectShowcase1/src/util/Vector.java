package util;

import java.awt.Point;

public class Vector{
	
	public double x, y;
	
	public Vector() {
		this.x = 0;
		this.y = 0;
	}

	public Vector(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	//pointing from point a to point b
	
	public Vector(Vector a, Vector b) {
		this.x = b.x - a.x;
		this.y = b.y - a.y;
	}
	
	public Vector(Vector a) {	//so you don't have any pointer issues
		this.x = a.x;
		this.y = a.y;
	}

	public double getMagnitude() {
		return Math.sqrt(this.x * this.x + this.y * this.y);
	}
	
	public double getMagnitudeSquared() {
		return this.x * this.x + this.y * this.y;
	}
	
	public Vector normalize() {
		double mag = this.getMagnitude();
		this.x /= mag;
		this.y /= mag;
		return this;
	}
	
	public Vector setMagnitude(double mag) {
		this.normalize();
		this.x *= mag;
		this.y *= mag;
		return this;
	}
	
	public Vector rotateClockwise(double rad) {
		double x = this.x;
		double y = this.y;
		this.x = (x * Math.cos(rad)) + (y * Math.sin(rad));
		this.y = -(x * Math.sin(rad)) + (y * Math.cos(rad));
		return this;
	}
	
	public Vector rotateCounterClockwise(double rad) {
		double x = this.x;
		double y = this.y;
		this.x = (x * Math.cos(rad)) - (y * Math.sin(rad));
		this.y = (x * Math.sin(rad)) + (y * Math.cos(rad));
		return this;
	}
	
	public double getRotationRadians() {
		return Math.asin(this.y / this.getMagnitude());
	}
	
	public Vector addNew(Vector v) {
		return new Vector(this.x + v.x, this.y + v.y);
	}
	
	public Vector subNew(Vector vector) {
		return new Vector(this.x - vector.x, this.y - vector.y);
	}
	
	public Vector mulNew(double val) {
		return new Vector(this.x * val, this.y * val);
	}
	
	public Vector divNew(double val) {
		return new Vector(this.x / val, this.y / val);
	}
	
	public Vector negNew() {
		return new Vector(-this.x, -this.y);
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
	
	public Vector add(Vector v) {
		this.x += v.x;
		this.y += v.y;
		return this;
	}
	
	public Vector mul(double val) {
		this.x *= val;
		this.y *= val;
		return this;
	}
	
	public Vector neg() {
		this.x = -this.x;
		this.y = -this.y;
		return this;
	}

	public Vector sub(Vector vector) {
		this.x -= vector.x;
		this.y -= vector.y;
		return this;
	}

	public Vector div(double val) {
		this.x /= val;
		this.y /= val;
		return this;
	}
	
	
}
