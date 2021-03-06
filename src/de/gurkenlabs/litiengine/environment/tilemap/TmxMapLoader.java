package de.gurkenlabs.litiengine.environment.tilemap;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import de.gurkenlabs.litiengine.environment.tilemap.xml.Map;

/**
 * This class allows to deserialzie a tmx file into an IMap instance.
 */
public final class TmxMapLoader implements IMapLoader {
  private static final Logger log = Logger.getLogger(TmxMapLoader.class.getName());

  /*
   * (non-Javadoc)
   *
   * @see liti.level.ITmxParser#ParseMap(java.lang.String)
   */
  @Override
  public IMap loadMap(final String path) {
    try {
      final JAXBContext jaxbContext = JAXBContext.newInstance(Map.class);
      final Unmarshaller um = jaxbContext.createUnmarshaller();

      InputStream stream = ClassLoader.getSystemResourceAsStream(path);
      if (stream == null) {
        stream = new FileInputStream(path);
      }

      final Map map = (Map) um.unmarshal(stream);

      // by default the map is named by the source file
      String name = path;
      final int pos = name.lastIndexOf('.');
      if (pos > 0) {
        name = name.substring(0, pos);
      }

      int lastBackslash = name.lastIndexOf('/');
      if (lastBackslash != -1) {
        name = name.substring(lastBackslash + 1, name.length());
      } else {
        int lastForwardSlash = name.lastIndexOf('\\');
        if (lastForwardSlash != -1) {
          name = name.substring(lastForwardSlash + 1, name.length());
        }
      }

      map.setFileName(name);
      map.setPath(path);
      map.updateTileTerrain();

      return map;
    } catch (final JAXBException | IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return null;
  }

}
