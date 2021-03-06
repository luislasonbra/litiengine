package de.gurkenlabs.litiengine.entities;

public interface IMovableCombatEntity extends ICombatEntity, IMovableEntity {
  public Direction getFacingDirection();

  public boolean isIdle();

  public void setFacingDirection(Direction facingDirection);

}