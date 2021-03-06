package de.gurkenlabs.litiengine.util.geom;

import org.junit.Assert;
import org.junit.Test;

import de.gurkenlabs.util.geom.GeometricUtilities;
import de.gurkenlabs.util.geom.Vector2D;

public class GeometricUtilitiesTests {

  @Test
  public void testScaleWithRatio() {
    int width1 = 16;
    int height1 = 32;

    int width2 = 32;
    int height2 = 16;

    Vector2D newDimension1 = GeometricUtilities.scaleWithRatio(width1, height1, 16);
    Vector2D newDimension2 = GeometricUtilities.scaleWithRatio(width2, height2, 16);

    Assert.assertEquals(8, newDimension1.getX(), 0.0001);
    Assert.assertEquals(16, newDimension1.getY(), 0.0001);

    Assert.assertEquals(16, newDimension2.getX(), 0.0001);
    Assert.assertEquals(8, newDimension2.getY(), 0.0001);
  }
}
