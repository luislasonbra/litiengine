package de.gurkenlabs.litiengine.entities.ai;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.states.IState;

public interface IEntityState<T extends IEntity> extends IState {

  public T getEntity();
}
