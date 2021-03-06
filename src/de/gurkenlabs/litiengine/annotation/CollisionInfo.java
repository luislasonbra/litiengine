package de.gurkenlabs.litiengine.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.gurkenlabs.core.Align;
import de.gurkenlabs.core.Valign;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CollisionInfo {
  Align align() default Align.CENTER;

  /**
   * Collision.
   *
   * @return true, if successful
   */
  boolean collision();

  /**
   * Collision box height factor.
   *
   * @return the float
   */
  int collisionBoxHeight() default -1;

  /**
   * Collision box width factor.
   *
   * @return the float
   */
  int collisionBoxWidth() default -1;

  Valign valign() default Valign.DOWN;
}
