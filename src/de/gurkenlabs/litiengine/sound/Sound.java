package de.gurkenlabs.litiengine.sound;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import de.gurkenlabs.util.io.FileUtilities;
import de.gurkenlabs.util.io.StreamUtilities;

/**
 * This class implements all required functionality to load sounds from the file
 * system and provide a stream that can later on be used for the sound playback.
 */
public final class Sound {
  private static final Map<String, Sound> sounds = new ConcurrentHashMap<>();
  private static final Logger log = Logger.getLogger(Sound.class.getName());

  private AudioFormat format;

  private final String name;

  private AudioInputStream stream;

  private byte[] streamData;

  /**
   * Creates a new Sound instance by the specified file path. Loads the sound
   * data into a byte array and also retrieves information about the format of
   * the sound file.
   * 
   * Note that the constructor is private. In order to load files use the static
   * methods {@link #find(String)} or {@link #load(String)} methods depending on
   * whether you already loaded the sound or not.
   * 
   * @param path
   *          The path to load the sound from.
   */
  private Sound(final String path) {
    this.name = FileUtilities.getFileName(path);

    final InputStream is = FileUtilities.getGameResource(path);

    try {
      AudioInputStream in = AudioSystem.getAudioInputStream(is);
      if (in != null) {
        final AudioFormat baseFormat = in.getFormat();
        final AudioFormat decodedFormat = this.getOutFormat(baseFormat);
        // Get AudioInputStream that will be decoded by underlying VorbisSPI
        in = AudioSystem.getAudioInputStream(decodedFormat, in);
        this.stream = in;
        this.streamData = StreamUtilities.getBytes(this.stream);
        this.format = this.stream.getFormat();
      }
    } catch (final UnsupportedAudioFileException | IOException e) {

      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  /**
   * Finds and returns a sound previously loaded by calling the
   * {@link #load(String)} function.
   * 
   * @param name
   *          The name of the sound to find (no extension needed).
   * @return The sound that is identified by the specified name, if it was
   *         previously loaded or null.
   */
  public static Sound find(final String name) {
    if (name == null || name.isEmpty()) {
      return null;
    }

    return sounds.get(FileUtilities.getFileName(name));
  }

  /**
   * Loads the sound from the specified path and makes it accessible via
   * {@link #find(String)} method.
   * 
   * @param path
   *          The path of the file to be loaded.(Can be relative or absolute)
   * @return The loaded Sound from the specified path.
   */
  public static Sound load(final String path) {
    String fileName = FileUtilities.getFileName(path);
    Sound sound = sounds.get(fileName);
    if (sound != null) {
      return sound;
    }

    sound = new Sound(path);
    sounds.put(fileName, sound);
    return sound;
  }

  public AudioFormat getFormat() {
    return this.format;
  }

  public String getName() {
    return this.name;
  }

  public byte[] getStreamData() {
    if (this.streamData == null) {
      return new byte[0];
    }

    return this.streamData.clone();
  }

  private AudioFormat getOutFormat(final AudioFormat inFormat) {
    final int ch = inFormat.getChannels();
    final float rate = inFormat.getSampleRate();
    return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, rate, 16, ch, ch * 2, rate, false);
  }
}