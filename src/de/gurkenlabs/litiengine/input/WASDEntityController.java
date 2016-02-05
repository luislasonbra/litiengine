package de.gurkenlabs.litiengine.input;

import java.awt.event.KeyEvent;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Direction;
import de.gurkenlabs.litiengine.entities.IMovableEntity;

public class WASDEntityController implements IKeyObserver {

  private final IMovableEntity entity;

  private final float stepSize;

  public WASDEntityController(final IMovableEntity entity, final float stepSize) {
    this.entity = entity;
    this.stepSize = stepSize;
    Input.KEYBOARD.registerForKeyDownEvents(this);
  }

  @Override
  public void handlePressedKey(final int keyCode) {
    Direction dir = Direction.UNDEFINED;
    switch (keyCode) {
    case KeyEvent.VK_W:
      dir = Direction.UP;
      break;
    case KeyEvent.VK_A:
      dir = Direction.LEFT;
      break;
    case KeyEvent.VK_S:
      dir = Direction.DOWN;
      break;
    case KeyEvent.VK_D:
      dir = Direction.RIGHT;

      break;
    }

    if (dir != Direction.UNDEFINED) {
      this.entity.setFacingDirection(dir);
      Game.getPhysicsEngine().move(this.entity, this.stepSize);
    }
  }

  @Override
  public void handleReleasedKey(final int keyCode) {

  }

  @Override
  public void handleTypedKey(final int keyCode) {

  }
}