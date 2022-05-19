package util;

public class Vec3 {
	public double x, y, z;

	public Vec3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vec3(Vec3 a, Vec3 b) {
		this.x = b.x - a.x;
		this.y = b.y - a.y;
		this.z = b.z - a.z;
	}
	
	public Vec3(Vec3 v) {
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
	}
	
	public Vec3 addi(Vec3 v) {
		this.x += v.x;
		this.y += v.y;
		this.z += v.z;
		return this;
	}
	
	public Vec3 subi(Vec3 v) {
		this.x -= v.x;
		this.y -= v.y;
		this.z -= v.z;
		return this;
	}
	
	public double length() {
		double xyDist = MathTools.dist(0, 0, x, y);
		return MathTools.dist(0, 0, xyDist, z);
	}
	
	public Vec3 muli(double val) {
		this.x *= val;
		this.y *= val;
		this.z *= val;
		return this;
	}
	
	public Vec3 normalize() {
		double mag = this.length();
		this.x /= mag;
		this.y /= mag;
		this.z /= mag;
		return this;
	}
	
	public Vec3 setLength(double mag) {
		this.normalize();
		this.x *= mag;
		this.y *= mag;
		this.z *= mag;
		return this;
	}
	
	public void rotateX(double xRot) {
		double x = this.x;
		double y = this.y;
		double z = this.z;
		this.x = x;
		this.y = ((y * Math.cos(xRot)) + (z * -Math.sin(xRot)));
		this.z = ((y * Math.sin(xRot)) + (z * Math.cos(xRot)));
	}
	public void rotateY(double yRot) {
		double x = this.x;
		double y = this.y;
		double z = this.z;
		this.x = (x * Math.cos(yRot)) + (z * Math.sin(yRot));
		this.y = y;
		this.z = (x * -Math.sin(yRot)) + (z * Math.cos(yRot));
	}
	public void rotateZ(double zRot) {
		double x = this.x;
		double y = this.y;
		double z = this.z;
		this.x = (x * Math.cos(zRot)) + (y * -Math.sin(zRot));
		this.y = (x * Math.sin(zRot)) + (y * Math.cos(zRot));
		this.z = z;
	}
}
