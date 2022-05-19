package impulse;

import util.Vec2;

public class ImpulseMath {

	public static final double PI = (double) StrictMath.PI;
	public static final double EPSILON = 0.0001f;
	public static final double EPSILON_SQ = EPSILON * EPSILON;
	public static final double BIAS_RELATIVE = 0.95f;
	public static final double BIAS_ABSOLUTE = 0.01f;
	public static final double DT = 1.0f / 60.0f;
	public static final Vec2 GRAVITY = new Vec2(0.0f, -50.0f);
	public static final double RESTING = GRAVITY.mul(DT).lengthSq() + EPSILON;
	public static final double PENETRATION_ALLOWANCE = 0.05f;
	public static final double PENETRATION_CORRETION = 0.4f;

	public static boolean equal(double a, double b) {
		return StrictMath.abs(a - b) <= EPSILON;
	}

	public static double clamp(double min, double max, double a) {
		return (a < min ? min : (a > max ? max : a));
	}

	public static int round(double a) {
		return (int) (a + 0.5f);
	}

	public static double random(double min, double max) {
		return (double) ((max - min) * Math.random() + min);
	}

	public static int random(int min, int max) {
		return (int) ((max - min + 1) * Math.random() + min);
	}

	public static boolean gt(double a, double b) {
		return a >= b * BIAS_RELATIVE + a * BIAS_ABSOLUTE;
	}

}
