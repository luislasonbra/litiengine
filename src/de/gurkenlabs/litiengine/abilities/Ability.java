package de.gurkenlabs.litiengine.abilities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.abilities.effects.EffectArgument;
import de.gurkenlabs.litiengine.abilities.effects.IEffect;
import de.gurkenlabs.litiengine.annotation.AbilityInfo;
import de.gurkenlabs.litiengine.entities.IMovableCombatEntity;
import de.gurkenlabs.litiengine.environment.IEnvironment;
import de.gurkenlabs.litiengine.graphics.IRenderable;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.util.geom.GeometricUtilities;

/**
 * The Class Ability.
 */
@AbilityInfo
public abstract class Ability implements IRenderable {
  private final List<Consumer<AbilityExecution>> abilityCastConsumer;

  /** The attributes. */
  private final AbilityAttributes attributes;
  private final CastType castType;

  /** The current execution. */
  private AbilityExecution currentExecution;

  /** The tooltip. */
  private final String description;

  /** The effects. */
  private final List<IEffect> effects;

  /** The executing mob. */
  private final IMovableCombatEntity executor;

  /** The multi target. */
  private final boolean multiTarget;

  /** The ability type. */
  private final String name;

  private Point2D origin;

  private final AbilityOrigin originType;

  /**
   * Instantiates a new ability.
   *
   * @param executor
   *          the executing entity
   */
  protected Ability(final IMovableCombatEntity executor) {
    this.abilityCastConsumer = new CopyOnWriteArrayList<>();
    this.effects = new CopyOnWriteArrayList<>();

    final AbilityInfo info = this.getClass().getAnnotation(AbilityInfo.class);
    this.attributes = new AbilityAttributes(info);
    this.executor = executor;
    this.name = info.name();
    this.multiTarget = info.multiTarget();
    this.description = info.description();
    this.castType = info.castType();
    this.originType = info.origin();
  }

  public void addEffect(final IEffect effect) {
    this.getEffects().add(effect);
  }

  /**
   * Calculate impact area.
   *
   * @return the shape
   */
  public Shape calculateImpactArea() {
    return this.internalCalculateImpactArea(this.getExecutor().getAngle());
  }

  public Ellipse2D calculatePotentialImpactArea() {
    final int range = this.getAttributes().getImpact().getCurrentValue();
    final double arcX = this.getExecutor().getCollisionBox().getCenterX() - range * 0.5;
    final double arcY = this.getExecutor().getCollisionBox().getCenterY() - range * 0.5;

    return new Ellipse2D.Double(arcX, arcY, range, range);
  }

  /**
   * Can cast.
   *
   * @return true, if successful
   */
  public boolean canCast(final IGameLoop gameLoop) {
    return !this.getExecutor().isDead() && (this.getCurrentExecution() == null || this.getCurrentExecution().getExecutionTicks() == 0 || gameLoop.getDeltaTime(this.getCurrentExecution().getExecutionTicks()) >= this.getAttributes().getCooldown().getCurrentValue());
  }

  /**
   * Casts the ability by the temporal conditions of the specified game loop and
   * the spatial circumstances of the specified environment. An ability
   * execution will be taken out that start applying all the effects of this
   * ability.
   */
  public AbilityExecution cast(final IGameLoop gameLoop, final IEnvironment environment) {
    if (!this.canCast(gameLoop)) {
      return null;
    }
    this.currentExecution = new AbilityExecution(gameLoop, environment, this);

    for (final Consumer<AbilityExecution> castConsumer : this.abilityCastConsumer) {
      castConsumer.accept(this.currentExecution);
    }

    return this.getCurrentExecution();
  }

  /**
   * Gets the attributes.
   *
   * @return the attributes
   */
  public AbilityAttributes getAttributes() {
    return this.attributes;
  }

  public CastType getCastType() {
    return this.castType;
  }

  /**
   * Gets the cooldown in seconds.
   *
   * @return the cooldown in seconds
   */
  public float getCooldownInSeconds() {
    return (float) (this.getAttributes().getCooldown().getCurrentValue() * 0.001);
  }

  /**
   * Gets the current execution.
   *
   * @return the current execution
   */
  public AbilityExecution getCurrentExecution() {
    return this.currentExecution;
  }

  public String getDescription() {
    return this.description;
  }

  /**
   * Gets the executing mob.
   *
   * @return the executing mob
   */
  public IMovableCombatEntity getExecutor() {
    return this.executor;
  }

  public String getName() {
    return this.name;
  }

  public Point2D getOrigin() {
    switch (this.originType) {
    case COLLISIONBOX_CENTER:
      return new Point2D.Double(this.executor.getCollisionBox().getCenterX(), this.executor.getCollisionBox().getCenterY());
    case DIMENSION_CENTER:
      return this.executor.getDimensionCenter();
    case CUSTOM:
      if (this.origin != null) {
        return new Point2D.Double(this.executor.getLocation().getX() + this.origin.getX(), this.executor.getLocation().getY() + this.origin.getY());
      }
      break;
    case LOCATION:
    default:
      break;
    }

    return this.executor.getLocation();
  }

  /**
   * Gets the remaining cooldown in seconds.
   *
   * @return the remaining cooldown in seconds
   */
  public float getRemainingCooldownInSeconds(final IGameLoop loop) {
    if (this.getCurrentExecution() == null || this.getExecutor() == null || this.getExecutor().isDead()) {
      return 0;
    }

    // calculate cooldown in seconds
    return (float) (!this.canCast(loop) ? (this.getAttributes().getCooldown().getCurrentValue() - loop.getDeltaTime(this.getCurrentExecution().getExecutionTicks())) * 0.001 : 0);
  }

  public boolean isCasting(final IGameLoop gameLoop) {
    return this.getCurrentExecution() != null && gameLoop.getDeltaTime(this.getCurrentExecution().getExecutionTicks()) < this.getAttributes().getDuration().getCurrentValue();
  }

  /**
   * Checks if is multi target.
   *
   * @return true, if is multi target
   */
  public boolean isMultiTarget() {
    return this.multiTarget;
  }

  public void onCast(final Consumer<AbilityExecution> castConsumer) {
    if (!this.abilityCastConsumer.contains(castConsumer)) {
      this.abilityCastConsumer.add(castConsumer);
    }
  }

  public void onEffectApplied(final Consumer<EffectArgument> consumer) {
    for (final IEffect effect : this.getEffects()) {
      // registers to all effects and their follow up effects recursively
      this.onEffectApplied(effect, consumer);
    }
  }

  public void onEffectCeased(final Consumer<EffectArgument> consumer) {
    for (final IEffect effect : this.getEffects()) {
      // registers to all effects and their follow up effects recursively
      this.onEffectCeased(effect, consumer);
    }
  }

  @Override
  public void render(final Graphics2D g) {
    g.setColor(new Color(255, 255, 0, 100));
    RenderEngine.fillShape(g, this.calculateImpactArea());
    final Stroke oldStroke = g.getStroke();
    g.setStroke(new BasicStroke(2f));
    g.setColor(new Color(255, 255, 0, 200));
    RenderEngine.drawShape(g, this.calculateImpactArea());
    g.setStroke(oldStroke);
  }

  /**
   * Sets a custom offset from the executors map location as origion of this
   * ability.
   * 
   * @param origin
   */
  public void setOrigin(final Point2D origin) {
    this.origin = origin;
  }

  /**
   * Gets the effects.
   *
   * @return the effects
   */
  protected List<IEffect> getEffects() {
    return this.effects;
  }

  protected Shape internalCalculateImpactArea(final float angle) {
    final int impact = this.getAttributes().getImpact().getCurrentValue();
    final int impactAngle = this.getAttributes().getImpactAngle().getCurrentValue();
    final double arcX = this.getOrigin().getX() - impact * 0.5;
    final double arcY = this.getOrigin().getY() - impact * 0.5;

    // project
    final Point2D appliedRange = GeometricUtilities.project(new Point2D.Double(arcX, arcY), angle, this.getAttributes().getRange().getCurrentValue() * 0.5);
    final double start = angle - 90;
    if (impactAngle % 360 == 0) {
      return new Ellipse2D.Double(appliedRange.getX(), appliedRange.getY(), impact, impact);
    }

    return new Arc2D.Double(appliedRange.getX(), appliedRange.getY(), impact, impact, start, impactAngle, Arc2D.PIE);
  }

  private void onEffectApplied(final IEffect effect, final Consumer<EffectArgument> consumer) {
    effect.onEffectApplied(consumer);

    for (final IEffect followUp : effect.getFollowUpEffects()) {
      this.onEffectApplied(followUp, consumer);
    }
  }

  private void onEffectCeased(final IEffect effect, final Consumer<EffectArgument> consumer) {
    effect.onEffectCeased(consumer);

    for (final IEffect followUp : effect.getFollowUpEffects()) {
      this.onEffectCeased(followUp, consumer);
    }
  }
}
