package de.gurkenlabs.litiengine.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.List;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.ITile;
import de.gurkenlabs.litiengine.environment.tilemap.MapUtilities;
import de.gurkenlabs.litiengine.input.Input;

public class DebugRenderer {

  private DebugRenderer() {
  }

  public static void renderEntityDebugInfo(final Graphics2D g, final IEntity entity) {
    if (!Game.getConfiguration().debug().isDebugEnabled()) {
      return;
    }

    if (Game.getConfiguration().debug().renderEntityNames()) {
      drawMapId(g, entity);
    }

    if (Game.getConfiguration().debug().renderHitBoxes() && entity instanceof ICombatEntity) {
      g.setColor(Color.RED);
      RenderEngine.drawShape(g, ((ICombatEntity) entity).getHitBox());
    }

    if (Game.getConfiguration().debug().renderBoundingBoxes()) {
      g.setColor(Color.RED);
      RenderEngine.drawShape(g, entity.getBoundingBox());
    }

    if (Game.getConfiguration().debug().renderCollisionBoxes() && entity instanceof ICollisionEntity) {
      final ICollisionEntity collisionEntity = (ICollisionEntity) entity;
      g.setColor(collisionEntity.hasCollision() ? Color.RED : Color.ORANGE);
      RenderEngine.drawShape(g, collisionEntity.getCollisionBox());
    }
  }

  public static void renderMapDebugInfo(final Graphics2D g, final IMap map) {
    // draw collision boxes from shape layer
    if (Game.getConfiguration().debug().renderCollisionBoxes()) {
      for (final Rectangle2D shape : Game.getPhysicsEngine().getStaticCollisionBoxes()) {
        g.setColor(Color.RED);
        RenderEngine.drawShape(g, shape);
      }
    }

    if (Game.getConfiguration().debug().showTilesMetric()) {
      // draw mouse tile info
      drawTileBoundingBox(g, map, Input.mouse().getMapLocation());
    }
  }

  /**
   * Draw name.
   *
   * @param g
   *          the g
   * @param entity
   *          the entity
   */
  private static void drawMapId(final Graphics2D g, final IEntity entity) {
    g.setColor(Color.RED);
    g.setFont(g.getFont().deriveFont(Font.PLAIN, 4f));
    final int x = (int) Game.getCamera().getViewPortDimensionCenter(entity).getX() + 10;
    final int y = (int) Game.getCamera().getViewPortDimensionCenter(entity).getY();
    RenderEngine.drawText(g, Integer.toString(entity.getMapId()), x, y);
    final String locationString = "[x:" + new DecimalFormat("##.##").format(entity.getLocation().getX()) + ";y:" + new DecimalFormat("##.##").format(entity.getLocation().getY()) + "]";
    RenderEngine.drawText(g, locationString, x, y + 5.0);
  }

  private static void drawTileBoundingBox(final Graphics2D g, final IMap map, final Point2D location) {
    final Rectangle2D playerTile = MapUtilities.getTileBoundingBox(map, location);

    // draw rect
    g.setColor(Color.CYAN);
    RenderEngine.drawShape(g, playerTile);

    // draw coords
    final Point tileLocation = MapUtilities.getTileLocation(map, location);
    final String locationText = tileLocation.x + ", " + tileLocation.y;
    g.setFont(g.getFont().deriveFont(3f));
    final FontMetrics fm = g.getFontMetrics();
    final Point2D relative = Game.getCamera().getViewPortLocation(playerTile.getX(), playerTile.getY());
    RenderEngine.drawText(g, locationText, (float) (relative.getX() + playerTile.getWidth() + 3), (float) (relative.getY() + fm.getHeight()));

    final List<ITile> tiles = MapUtilities.getTilesByPixelLocation(map, location);
    final StringBuilder sb = new StringBuilder();
    for (final ITile tile : tiles) {
      sb.append("[gid: " + tile.getGridId() + "] ");
    }

    RenderEngine.drawText(g, sb.toString(), (float) (relative.getX() + playerTile.getWidth() + 3), (float) (relative.getY() + fm.getHeight() * 2 + 2));
  }
}
