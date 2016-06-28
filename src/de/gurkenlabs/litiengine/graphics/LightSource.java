/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.function.Predicate;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.annotation.EmitterInfo;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.graphics.particles.Emitter;
import de.gurkenlabs.litiengine.graphics.particles.Particle;
import de.gurkenlabs.litiengine.tiled.tmx.IEnvironment;
import de.gurkenlabs.util.geom.GeometricUtilities;

// TODO: Auto-generated Javadoc
/**
 * The Class LightSource.
 */
@EmitterInfo(maxParticles = 1, spawnAmount = 1, spawnRate = 1000)
public class LightSource extends Emitter {

  /** The brightness. */
  private int brightness;

  /** The color. */
  private final Color color;
  
  private final IEnvironment environment;

  /** The radius. */
  private int radius;

  /**
   * Instantiates a new light source.
   *
   * @param location
   *          the location
   * @param radius
   *          the light inner radius
   * @param lightOuterRadius
   *          the light outer radius
   * @param brightness
   *          the brightness
   * @param lightColor
   *          the light color
   */
  public LightSource(final IEnvironment environment, final Point2D location, final int radius, final int brightness, final Color lightColor) {
    super(location);
    this.color = lightColor;
    this.environment = environment;
    this.setRadius(radius);
    this.setBrightness(brightness);
    this.setLocation(location);
  }

  @Override
  protected Particle createNewParticle() {
    final Particle part = new LightParticle(this.radius * 2, this.radius * 2, this.radius / 2, 0, 0, 0, 0, 0, this.radius, this.radius, 0, new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), 50));
    // in the end the particle should have a size of 10
    final float delta = this.radius / (float) this.getParticleUpdateRate();
    part.setDeltaWidth(delta);
    part.setDeltaHeight(delta);
    return part;
  }

  /**
   * Gets the obstructed vision area.
   *
   * @param mob
   *          the mob
   * @param center
   *          the center
   * @return the obstructed vision area
   */
  private Area getObstructedVisionArea(final IEntity mob, final Point2D center) {
    /** The gradient radius for our shadow. */
    final float OBSTRUCTED_VISION_RADIUS = 200f;
    final Polygon SHADOW_POLYGON = new Polygon();

    final Ellipse2D shadowEllipse = getShadowEllipse(mob);

    final Rectangle2D bounds = shadowEllipse.getBounds2D();

    // radius of Entity's bounding circle
    final float r = (float) bounds.getWidth() / 2f;
    final float ry = (float) bounds.getHeight() / 2f;

    // get relative center of entity
    final Point2D relativeCenter = Game.getScreenManager().getCamera().getViewPortLocation(new Point((int) (bounds.getX() + r), (int) (bounds.getY() + ry)));
    final double cx = relativeCenter.getX();
    final double cy = relativeCenter.getY();

    // get direction from light to entity center
    final double dx = cx - center.getX();
    final double dy = cy - center.getY();

    // get euclidean distance from entity to center
    final double distSq = dx * dx + dy * dy; // avoid sqrt for performance

    // normalize the direction to a unit vector
    final float len = (float) Math.sqrt(distSq);
    double nx = dx;
    double ny = dy;
    if (len != 0) { // avoid division by 0
      nx /= len;
      ny /= len;
    }

    // get perpendicular of unit vector
    final double px = -ny;
    final double py = nx;

    // our perpendicular points in either direction from radius
    final Point2D.Double A = new Point2D.Double(cx - px * r, cy - py * ry);
    final Point2D.Double B = new Point2D.Double(cx + px * r, cy + py * ry);

    // project the points by our SHADOW_EXTRUDE amount
    final Point2D C = GeometricUtilities.project(center, A, OBSTRUCTED_VISION_RADIUS);
    final Point2D D = GeometricUtilities.project(center, B, OBSTRUCTED_VISION_RADIUS);

    // construct a polygon from our points
    SHADOW_POLYGON.reset();
    SHADOW_POLYGON.addPoint((int) A.getX(), (int) A.getY());
    SHADOW_POLYGON.addPoint((int) B.getX(), (int) B.getY());
    SHADOW_POLYGON.addPoint((int) D.getX(), (int) D.getY());
    SHADOW_POLYGON.addPoint((int) C.getX(), (int) C.getY());

    final Point2D shadowRenderLocation = Game.getScreenManager().getCamera().getViewPortLocation(new Point2D.Double(shadowEllipse.getX(), shadowEllipse.getY()));
    final Ellipse2D relativeEllipse = new Ellipse2D.Double(shadowRenderLocation.getX(), shadowRenderLocation.getY(), shadowEllipse.getWidth(), shadowEllipse.getHeight());

    final Area ellipseArea = new Area(relativeEllipse);
    final Area shadowArea = new Area(SHADOW_POLYGON);
    shadowArea.add(ellipseArea);
    return shadowArea;
  }

  /**
   * Gets the shadow ellipse.
   *
   * @param mob
   *          the mob
   * @return the shadow ellipse
   */
  private static Ellipse2D getShadowEllipse(final IEntity mob) {
    final int ShadowHeight = (int) (mob.getHeight() / 4);
    final int ShadowWidth = (int) (mob.getWidth() / 3);

    final int yOffset = (int) mob.getHeight();
    final double x = mob.getLocation().getX() + (mob.getWidth() - ShadowWidth) / 2;
    final double y = mob.getLocation().getY() + yOffset - ShadowHeight / 2;
    return new Ellipse2D.Double(x, y, ShadowWidth, ShadowHeight);
  }

  /**
   * Checks if is in range.
   *
   * @param center
   *          the center
   * @param radius
   *          the radius
   * @return the predicate<? super mob>
   */
  private static Predicate<? super IEntity> isInRange(final Point2D center, final float radius) {
    return mob -> new Ellipse2D.Double(center.getX() - radius, center.getY() - radius, radius * 2, radius * 2).contains(mob.getDimensionCenter());
  }

  /**
   * Gets the brightness.
   *
   * @return the brightness
   */
  public int getBrightness() {
    return this.brightness;
  }

  /**
   * Gets the color.
   *
   * @return the color
   */
  public Color getColor() {
    return this.color;
  }

  /**
   * Gets the radius.
   *
   * @return the radius
   */
  public int getRadius() {
    return this.radius;
  }

  @Override
  public void render(final Graphics2D g) {
    super.render(g);
    this.renderShadows(g);
  }

  /**
   * Sets the brightness.
   *
   * @param brightness
   *          the new brightness
   */
  public void setBrightness(final int brightness) {
    this.brightness = brightness;
  }

  /**
   * Renders the shadows using simple vector math. The steps are as follows:
   *
   * <pre>
   * for each entity
   *     if entity is not moving:
   *         ignore entity
   *     if entity is too far from mouse:
   *         ignore entity
   *
   *     determine unit vector from mouse to entity center
   *     get perpendicular of unit vector
   *
   *     Create Points A + B:
   *         extrude perpendicular in either direction, by the half-size of the entity
   *     Create Points C + D:
   *         extrude A + B away from mouse position
   *
   *     construct polygon with points A, B, C, D
   *
   *     render with RadialGradientPaint to give it a "fade-out" appearance
   * </pre>
   *
   * @param g
   *          the graphics to use for rendering
   * @param center
   *          the center
   */
  private void renderShadows(final Graphics2D g) {
    /** The gradient radius for our shadow. */
    final float SHADOW_GRADIENT_SIZE = 100f;

    if (!this.environment.getCombatEntities().stream().anyMatch(isInRange(this.getDimensionCenter(), SHADOW_GRADIENT_SIZE))) {
      return;
    }

    /**
     * } The fractions for our shadow gradient, going from 0.0 (black) to 1.0
     * (transparent).
     */
    final float[] SHADOW_GRADIENT_FRACTIONS = new float[] { 0f, 1f };

    /**
     * The colors for our shadow, going from opaque black to transparent black.
     */
    final Color[] SHADOW_GRADIENT_COLORS = new Color[] { new Color(0, 0, 0, .3f), new Color(0f, 0f, 0f, 0f) };
    // we'll use a radial gradient
    final Paint GRADIENT_PAINT = new RadialGradientPaint(Game.getScreenManager().getCamera().getViewPortDimensionCenter(this), SHADOW_GRADIENT_SIZE, SHADOW_GRADIENT_FRACTIONS, SHADOW_GRADIENT_COLORS);

    // old Paint object for resetting it later
    final Paint oldPaint = g.getPaint();
    g.setPaint(GRADIENT_PAINT);

    // for each entity
    for (final ICombatEntity mob : this.environment.getCombatEntities()) {
      if (mob.isDead() || !isInRange(this.getDimensionCenter(), SHADOW_GRADIENT_SIZE).test(mob)) {
        continue;
      }

      final Shape obstructedVision = getObstructedVisionArea(mob, Game.getScreenManager().getCamera().getViewPortDimensionCenter(this));
      // fill the polygon with the gradient paint

      g.fill(obstructedVision);
    }

    // reset to old Paint object
    g.setPaint(oldPaint);
  }

  /**
   * Sets the radius.
   *
   * @param radius
   *          the new radius
   */
  private void setRadius(final int radius) {
    this.radius = radius;
  }
}