package de.gurkenlabs.litiengine.configuration;

import de.gurkenlabs.configuration.Configuration;
import de.gurkenlabs.configuration.ConfigurationGroup;

public class GameConfiguration extends Configuration {
  public final ClientConfiguration CLIENT;
  public final SoundConfiguration SOUND;
  public final GraphicConfiguration GRAPHICS;

  public GameConfiguration(ConfigurationGroup... groups) {
    super(groups);
    this.CLIENT = new ClientConfiguration();
    this.SOUND = new SoundConfiguration();
    this.GRAPHICS = new GraphicConfiguration();
    this.getConfigurationGroups().add(this.CLIENT);
    this.getConfigurationGroups().add(this.SOUND);
    this.getConfigurationGroups().add(this.GRAPHICS);
  }
}