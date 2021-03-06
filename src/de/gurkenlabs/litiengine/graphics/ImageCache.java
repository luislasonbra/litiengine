package de.gurkenlabs.litiengine.graphics;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.util.io.CompressionUtilities;
import de.gurkenlabs.util.io.FileUtilities;
import de.gurkenlabs.util.io.ImageSerializer;

/**
 * The Class ImageCache.
 */
public final class ImageCache {
  /** The Constant CACHE_DIRECTORY. */
  public static final String CACHE_DIRECTORY = "cache/";

  public static final String CACHE_DUMP_NAME = "imagecache.dump";

  public static final ImageCache IMAGES = new ImageCache("images");
  public static final String IMAGES_DIRECTORY = "images";

  public static final String MAP_DIRECTORY = "map";

  public static final ImageCache MAPS = new ImageCache("map");

  public static final ImageCache SPRITES = new ImageCache("sprites");

  public static final String SPRITES_DIRECTORY = "sprites";

  private static final Logger log = Logger.getLogger(ImageCache.class.getName());

  /** The cache. */
  private final ConcurrentHashMap<String, BufferedImage> cache;

  /** The sub folder. */
  private final String subFolder;

  /**
   * Instantiates a new image cache.
   *
   * @param subfolder
   *          the subfolder
   */
  private ImageCache(final String subfolder) {
    this.cache = new ConcurrentHashMap<>();
    this.subFolder = subfolder;
  }

  public static void loadCache() {
    if (new File(CACHE_DIRECTORY).exists()) {
      log.log(Level.INFO, "cache dump \'{0}\' was not loaded because the cache folder \'{1}\' already exists.", new Object[] { CACHE_DUMP_NAME, CACHE_DIRECTORY });
      return;
    }

    try (final InputStream in = FileUtilities.getGameResource(CACHE_DUMP_NAME)) {
      if (in == null) {
        log.log(Level.INFO, "loading cache dump from \'{0}\' failed", new Object[] { CACHE_DUMP_NAME });
        return;
      }

      CompressionUtilities.unzip(in, new File(CACHE_DIRECTORY));
      log.log(Level.INFO, "cache loaded from \'{0}\'", new Object[] { CACHE_DUMP_NAME });
    } catch (IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  public static void saveCache(final String path) {
    final File cacheFile = new File(path, CACHE_DUMP_NAME);
    try {
      if (cacheFile.exists()) {
        Files.delete(cacheFile.toPath().toAbsolutePath());
      }

      CompressionUtilities.zip(new File(CACHE_DIRECTORY), cacheFile);
      log.log(Level.INFO, "cache dumped to \'{0}\'", new Object[] { cacheFile.toPath() });
    } catch (final IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  public void clearPersistent() {
    final File dir = new File(this.getSubFolderName());
    if (!dir.exists() || !dir.isDirectory()) {
      return;
    }

    log.log(Level.INFO, "deleted \'{0}\'", new Object[] { dir.toString() });
    FileUtilities.deleteDir(dir);
    this.cache.clear();
  }

  public void clear() {
    this.cache.clear();
  }

  public void clear(final String regex) {
    List<String> remove = new ArrayList<>();
    for (String key : this.cache.keySet()) {
      if (key.matches(regex)) {
        remove.add(key);
      }
    }

    for (String key : remove) {
      this.cache.remove(key);
    }
  }

  /**
   * Contains key.
   *
   * @param key
   *          the key
   * @return true, if successful
   */
  public boolean containsKey(final String key) {
    return this.cache.containsKey(key) || new File(this.getFileName(key)).exists();
  }

  /**
   * Gets the.
   *
   * @param key
   *          the key
   * @return the buffered image
   */
  public BufferedImage get(final String key) {
    if (this.cache.containsKey(key)) {
      return this.cache.get(key);
    }

    return this.loadImage(key);
  }

  /**
   * Load all.
   */
  public void loadAll() {
    final File dir = new File(this.getSubFolderName());
    if (!dir.exists() || !dir.isDirectory()) {
      return;
    }
    final File[] directoryListing = dir.listFiles();
    if (directoryListing != null) {
      for (final File child : directoryListing) {
        if (!child.isFile()) {
          continue;
        }

        final BufferedImage img = this.loadImage(child.getName());

        // clean up cached file if the image is null
        if (img == null) {
          try {
            Files.delete(child.toPath().toAbsolutePath());
          } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
          }
        }
      }
    }
  }

  /**
   * Put.
   *
   * @param key
   *          the key
   * @param value
   *          the value
   * @return the buffered image
   */
  public BufferedImage put(final String key, final BufferedImage value) {
    return this.cache.put(key, value);
  }

  /**
   * Put persistent.
   *
   * @param key
   *          the key
   * @param value
   *          the value
   * @return the buffered image
   */
  public BufferedImage putPersistent(final String key, final BufferedImage value) {
    if (key == null || key.isEmpty() || value == null) {
      return null;
    }

    this.cache.put(key, value);
    this.saveImage(key, value);
    return value;
  }

  /**
   * Gets the file name.
   *
   * @param key
   *          the key
   * @return the file name
   */
  private String getFileName(final String key) {
    return this.getSubFolderName() + "\\" + key;
  }

  /**
   * Gets the sub folder name.
   *
   * @return the sub folder name
   */
  private String getSubFolderName() {
    return CACHE_DIRECTORY + this.subFolder;
  }

  /**
   * Load image.
   *
   * @param key
   *          the key
   * @return the buffered image
   */
  private synchronized BufferedImage loadImage(final String key) {
    final BufferedImage img = ImageSerializer.loadImage(this.getFileName(key));
    if (img == null) {
      return null;
    }

    this.cache.put(key, img);
    return img;
  }

  /**
   * Save image.
   *
   * @param key
   *          the key
   * @param value
   *          the value
   */
  private void saveImage(final String key, final BufferedImage value) {
    ImageSerializer.saveImage(this.getFileName(key), value);
  }
}
