package de.gurkenlabs.litiengine.entities;

import java.awt.geom.Point2D;
import java.util.Random;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.annotation.CombatAttributesInfo;
import de.gurkenlabs.litiengine.graphics.animation.DecorMobAnimationController;
import de.gurkenlabs.litiengine.physics.MovementController;

@CombatAttributesInfo(health = 1)
public class DecorMob extends MovableCombatEntity {
  public enum MovementBehavior {
    IDLE,

    // rabbit
    RANDOM,

    // butterfly
    SHY;

    public static MovementBehavior get(final String behaviour) {
      if (behaviour == null || behaviour.isEmpty()) {
        return MovementBehavior.IDLE;
      }

      try {
        return MovementBehavior.valueOf(behaviour);
      } catch (final IllegalArgumentException iae) {
        return MovementBehavior.IDLE;
      }
    }
  }

  private class ShyDecorMobMovementController extends MovementController<DecorMob> {
    private int angle;
    private long lastAngleChange;
    private long nextAngleChange;

    public ShyDecorMobMovementController(final DecorMob movableEntity) {
      super(movableEntity);
      this.calculateNextAngleChange();
    }

    public void calculateNextAngleChange() {
      this.nextAngleChange = new Random().nextInt(3000) + (long) 2000;
    }

    @Override
    public void update(final IGameLoop gameLoop) {
      super.update(gameLoop);
      if (Game.getEnvironment() == null || DecorMob.this.isDead()) {
        return;
      }
      final long currentTick = gameLoop.getTicks();
      final long timeSinceLastAngleChange = gameLoop.getDeltaTime(this.lastAngleChange);
      if (this.angle == 0 || timeSinceLastAngleChange > this.nextAngleChange) {
        final Random rand = new Random();
        this.angle = rand.nextInt(360);
        this.lastAngleChange = currentTick;
        this.calculateNextAngleChange();
      }

      final float pixelsPerTick = gameLoop.getDeltaTime() * 0.001F * this.getEntity().getVelocity();
      this.getPhysicsEngine().move(this.getEntity(), this.angle, pixelsPerTick);
    }
  }

  private final MovementBehavior movementBehaviour;

  private final String mobType;

  public DecorMob(final Point2D location, final String mobType, final MovementBehavior behaviour, final short velocity) {
    super();
    this.mobType = mobType;
    this.setLocation(location);

    Game.getEntityControllerManager().addController(this, new DecorMobAnimationController(this));
    this.movementBehaviour = behaviour;

    switch (this.movementBehaviour) {
    case SHY:
      Game.getEntityControllerManager().addController(this, new ShyDecorMobMovementController(this));
      break;
    case RANDOM:
      break;
    default:
      break;
    }

    this.setVelocity(velocity);
  }

  public String getMobType() {
    return this.mobType;
  }

  public MovementBehavior getMovementBehavior() {
    return this.movementBehaviour;
  }
}
