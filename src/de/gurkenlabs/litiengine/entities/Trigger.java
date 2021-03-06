package de.gurkenlabs.litiengine.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.annotation.CollisionInfo;
import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.physics.CollisionType;
import de.gurkenlabs.util.geom.GeometricUtilities;

@CollisionInfo(collision = false)
@EntityInfo(renderType = RenderType.OVERLAY)
public class Trigger extends CollisionEntity implements IUpdateable {
  public enum TriggerActivation {
    COLLISION, INTERACT
  }

  public static final String USE_MESSAGE = "use";
  private static final Logger log = Logger.getLogger(Trigger.class.getName());

  private List<IEntity> activated;
  private final Collection<Consumer<TriggerEvent>> activatedConsumer;
  private final Collection<Function<TriggerEvent, String>> activatingPredicates;
  private final TriggerActivation activationType;
  private final List<Integer> activators;
  private final Map<String, String> arguments;
  private final Collection<Consumer<TriggerEvent>> deactivatedConsumer;
  private final boolean isOneTimeTrigger;
  private String message;
  private final List<Integer> targets;

  private boolean triggered;

  public Trigger(final String name, final String message) {
    this(TriggerActivation.COLLISION, name, message, false, new ConcurrentHashMap<>());
  }

  public Trigger(final TriggerActivation activation, final String name, final String message, final boolean isOneTime, final Map<String, String> arguments) {
    super();
    this.activatingPredicates = new CopyOnWriteArrayList<>();
    this.activatedConsumer = new CopyOnWriteArrayList<>();
    this.deactivatedConsumer = new CopyOnWriteArrayList<>();
    this.arguments = arguments;
    this.activators = new CopyOnWriteArrayList<>();
    this.targets = new CopyOnWriteArrayList<>();
    this.activated = new CopyOnWriteArrayList<>();
    this.setName(name);
    this.message = message;
    this.isOneTimeTrigger = isOneTime;
    this.activationType = activation;
  }

  public boolean activate(final IEntity activator, final int tar) {
    if (this.isOneTimeTrigger && this.triggered || this.getActivationType() == TriggerActivation.COLLISION && activator != null && this.activated.contains(activator)) {
      return false;
    }

    this.triggered = true;
    List<Integer> triggerTargets = this.getTargets(tar);

    final TriggerEvent te = new TriggerEvent(this, activator, triggerTargets);

    if (!this.checkActivationPredicates(te)) {
      return false;
    }

    // if we actually have a trigger target, we send the message to the target
    if (!triggerTargets.isEmpty()) {
      for (final int target : triggerTargets) {
        final IEntity entity = Game.getEnvironment().get(target);
        if (entity == null) {
          log.log(Level.WARNING, "trigger \'{0}\' was activated, but the trigger target \'{1}\' could not be found on the environment", new Object[] { this.getName(), target });
          continue;
        }

        entity.sendMessage(this, this.message);
        this.activated.add(activator);
      }
    }

    // also send the trigger event to all registered consumers
    for (final Consumer<TriggerEvent> cons : this.activatedConsumer) {
      cons.accept(te);
    }

    if (this.isOneTimeTrigger && this.triggered) {
      Game.getEnvironment().remove(this);
    }

    return true;
  }

  public void addActivator(final int mapId) {
    this.activators.add(mapId);
  }

  public void addTarget(final int mapId) {
    this.targets.add(mapId);
  }

  public TriggerActivation getActivationType() {
    return this.activationType;
  }

  public Map<String, String> getArguments() {
    return this.arguments;
  }

  public List<Integer> getActivators() {
    return this.activators;
  }

  public String getMessage() {
    return this.message;
  }

  public List<Integer> getTargets() {
    return this.targets;
  }

  /**
   * Checks whether the specified entity can interact with this trigger.
   * 
   * @param entity
   * @return
   */
  public boolean canTrigger(ICollisionEntity entity) {
    return entity.canCollideWith(this) && GeometricUtilities.intersects(this.getCollisionBox(), entity.getCollisionBox());
  }

  public boolean isOneTimeTrigger() {
    return this.isOneTimeTrigger;
  }

  /**
   * Allows to register functions that contain additional checks for the trigger
   * activation. The return value of the function is considered the reason why
   * the trigger cannot be activated. If the function returns anything else than
   * null, the activation is cancelled and the result of the function is send to
   * the activator entity.
   * 
   * @param func
   */
  public void onActivating(final Function<TriggerEvent, String> func) {
    if (this.activatingPredicates.contains(func)) {
      return;
    }

    this.activatingPredicates.add(func);
  }

  public void onActivated(final Consumer<TriggerEvent> cons) {
    if (this.activatedConsumer.contains(cons)) {
      return;
    }

    this.activatedConsumer.add(cons);
  }

  public void onDeactivated(final Consumer<TriggerEvent> cons) {
    if (this.deactivatedConsumer.contains(cons)) {
      return;
    }

    this.deactivatedConsumer.add(cons);
  }

  @Override
  public String sendMessage(final Object sender, final String message) {
    if (this.activationType == TriggerActivation.COLLISION && sender != null && sender instanceof IEntity || message == null || message.isEmpty()) {
      return Boolean.toString(false);
    }

    if (sender instanceof IEntity) {
      final IEntity ent = (IEntity) sender;
      // already triggered by the entity
      if (this.activators.contains(ent.getMapId())) {
        return Boolean.toString(false);
      }

      if (message.equals(USE_MESSAGE)) {
        this.activate(ent, ent.getMapId());
        return Boolean.toString(true);
      }
    }

    return null;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  @Override
  public String toString() {
    return "trigger: " + this.getName() + "[" + this.getMapId() + "]";
  }

  @Override
  public void update(final IGameLoop loop) {
    if (Game.getEnvironment() == null || this.activationType != TriggerActivation.COLLISION || !Game.getPhysicsEngine().collides(this.getCollisionBox(), CollisionType.COLLTYPE_ENTITY)) {
      return;
    }

    final List<IEntity> collEntities = this.getEntitiesInCollisionBox();

    for (final IEntity ent : collEntities) {
      if (this.activated.contains(ent)) {
        continue;
      }

      this.activate(ent, ent.getMapId());
    }

    // send deactivation event
    for (final IEntity ent : this.activated) {
      if (!collEntities.contains(ent)) {
        for (final Consumer<TriggerEvent> cons : this.deactivatedConsumer) {
          List<Integer> triggerTargets = this.getTargets();
          if (triggerTargets.isEmpty()) {
            triggerTargets = new ArrayList<>();
            triggerTargets.add(ent.getMapId());
          }

          cons.accept(new TriggerEvent(this, ent, triggerTargets));
        }
      }
    }

    this.activated = collEntities;
  }

  private boolean checkActivationPredicates(TriggerEvent te) {
    // check if the trigger is allowed to be activated
    for (Function<TriggerEvent, String> pred : this.activatingPredicates) {
      String result = pred.apply(te);
      if (result != null && !result.isEmpty()) {
        te.getEntity().sendMessage(this, result);
        return false;
      }
    }

    return true;
  }

  private List<IEntity> getEntitiesInCollisionBox() {
    final List<IEntity> collEntities = new CopyOnWriteArrayList<>();
    for (final ICollisionEntity coll : Game.getPhysicsEngine().getCollisionEntities()) {
      if (!this.activators.isEmpty() && !this.activators.contains(coll.getMapId())) {
        continue;
      }

      if (coll.getCollisionBox().intersects(this.getCollisionBox())) {
        collEntities.add(coll);
      }
    }

    return collEntities;
  }

  private List<Integer> getTargets(int optionalTarget) {
    // always take local targets if there are any
    List<Integer> localTargets = this.getTargets();
    if (localTargets.isEmpty()) {

      // as a fall back send the message to the tar
      localTargets = new ArrayList<>();
      if (optionalTarget > 0) {
        localTargets.add(optionalTarget);
      }
    }

    return localTargets;
  }
}