/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.physics;

import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.IUpdateable;

/**
 * The Interface IEntityNavigator.
 */
public interface IEntityNavigator extends IUpdateable {

  public Path getPath();

  public IPathFinder getPathFinder();

  public void navigate(Point2D target);
}