package de.gurkenlabs.litiengine.graphics.particles;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.gurkenlabs.litiengine.IGameLoop;

/**
 * The Class ShimmerParticle.
 */
public class ShimmerParticle extends RectangleFillParticle {

  /** The bounding box. */
  private final Rectangle2D boundingBox;

  /**
   * Instantiates a new shimmer particle.
   *
   * @param boundingBox
   *          the bounding box
   * @param width
   *          the width
   * @param height
   *          the height
   * @param color
   *          the color
   */
  public ShimmerParticle(final Rectangle2D boundingBox, final float width, final float height, final Color color) {
    super(width, height, color, 0);
    this.boundingBox = boundingBox;
  }

  /**
   * Gets the bounding box.
   *
   * @return the bounding box
   */
  public Rectangle2D getBoundingBox() {
    return this.boundingBox;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.graphics.particles.Particle#update()
   */
  @Override
  public void update(final IGameLoop loop, final Point2D emitterOrigin, final float updateRateFactor) {
    super.update(loop, emitterOrigin, updateRateFactor);
    final Point2D emitterLocation = new Point2D.Double(this.getBoundingBox().getX(), this.getBoundingBox().getY());
    final Point2D relativeParticleLocation = this.getRelativeLocation(emitterLocation);
    if (relativeParticleLocation.getX() < this.getBoundingBox().getX()) {
      this.setDeltaX(-this.getDx());
    }

    if (relativeParticleLocation.getX() > this.getBoundingBox().getX() + this.getBoundingBox().getWidth()) {
      this.setDeltaX(-this.getDx());
    }

    if (relativeParticleLocation.getY() < this.getBoundingBox().getY()) {
      this.setDeltyY(-this.getDy());
    }

    if (relativeParticleLocation.getY() > this.getBoundingBox().getY() + this.getBoundingBox().getHeight()) {
      this.setDeltyY(-this.getDy());
    }
  }

}
