package util;

public class Vector3D {
	
	public double x, y, z;

	public Vector3D(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vector3D(Vector3D a, Vector3D b) {
		this.x = b.x - a.x;
		this.y = b.y - a.y;
		this.z = b.z - a.z;
	}
	
	public Vector3D(Vector3D v) {
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
	}
	
	public void addVector(Vector3D v) {
		this.x += v.x;
		this.y += v.y;
		this.z += v.z;
	}
	
	public void subtractVector(Vector3D v) {
		this.x -= v.x;
		this.y -= v.y;
		this.z -= v.z;
	}
	
	public double getMagnitude() {
		double xyDist = MathTools.dist(0, 0, x, y);
		return MathTools.dist(0, 0, xyDist, z);
	}
	
	public void multiply(double val) {
		this.x *= val;
		this.y *= val;
		this.z *= val;
	}
	
	public void normalize() {
		double mag = this.getMagnitude();
		this.x /= mag;
		this.y /= mag;
		this.z /= mag;
	}
	
	public void setMagnitude(double mag) {
		this.normalize();
		this.x *= mag;
		this.y *= mag;
		this.z *= mag;
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
