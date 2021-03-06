package de.gurkenlabs.litiengine.abilities;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.abilities.effects.IEffect;
import de.gurkenlabs.litiengine.environment.IEnvironment;

/**
 * The Class AbilityExecution.
 */
public class AbilityExecution implements IUpdateable {
  /** The executed ability. */
  private final Ability ability;
  private final List<IEffect> appliedEffects;

  private final Point2D castLocation;

  /** The execution ticks. */
  private final long executionTicks;

  /** The impact area. */
  private final Shape impactArea;

  private final IEnvironment environment;

  /**
   * Instantiates a new ability execution.
   *
   * @param ability
   *          the ability
   */
  public AbilityExecution(final IGameLoop gameLoop, final IEnvironment environment, final Ability ability) {
    this.appliedEffects = new CopyOnWriteArrayList<>();
    this.ability = ability;
    this.executionTicks = gameLoop.getTicks();
    this.impactArea = ability.calculateImpactArea();
    this.castLocation = ability.getExecutor().getDimensionCenter();
    this.environment = environment;
    gameLoop.attach(this);
  }

  /**
   * Gets the executed ability.
   *
   * @return the executed ability
   */
  public Ability getAbility() {
    return this.ability;
  }

  public List<IEffect> getAppliedEffects() {
    return this.appliedEffects;
  }

  public Point2D getCastLocation() {
    return this.castLocation;
  }

  /**
   * Gets the impact area.
   *
   * @return the impact area
   */
  public Shape getExecutionImpactArea() {
    return this.impactArea;
  }

  /**
   * Gets the ticks.
   *
   * @return the ticks
   */
  public long getExecutionTicks() {
    return this.executionTicks;
  }

  /**
   * 1. Apply all ability effects after their delay. 2. Unregister this instance
   * after all effects were applied. 3. Effects will apply their follow up
   * effects on their own.
   */
  @Override
  public void update(final IGameLoop loop) {
    // if there a no effects to apply -> unregister this instance and we're done
    if (this.getAbility().getEffects().isEmpty() || this.getAbility().getEffects().size() == this.getAppliedEffects().size()) {
      loop.detach(this);
      return;
    }

    // handle all effects from the ability that were not applied yet
    for (final IEffect effect : this.getAbility().getEffects()) {
      // if the ability was not executed yet or the delay of the effect is not
      // yet reached
      if (this.getAppliedEffects().contains(effect) || loop.getDeltaTime(this.getExecutionTicks()) < effect.getDelay()) {
        continue;
      }

      effect.apply(loop, this.environment, this.getExecutionImpactArea());
      this.getAppliedEffects().add(effect);
    }
  }
}
