package impulse;

public interface CollisionCallback {
	public void handleCollision(Manifold m, Body a, Body b);
}
