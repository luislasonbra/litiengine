package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.environment.tilemap.ITileAnimation;
import de.gurkenlabs.litiengine.environment.tilemap.ITileAnimationFrame;

@XmlRootElement(name = "animation")
@XmlAccessorType(XmlAccessType.FIELD)
public class Animation implements ITileAnimation {

  @XmlElement(name = "frame")
  private List<Frame> frames;

  @XmlTransient
  private List<ITileAnimationFrame> tileAnimationFrames;

  @XmlTransient
  private int totalDuration;

  @Override
  public List<ITileAnimationFrame> getFrames() {
    if (this.tileAnimationFrames != null) {
      return this.tileAnimationFrames;
    }

    List<ITileAnimationFrame> fr = new ArrayList<>();
    if (this.frames == null) {
      return fr;
    }

    for (ITileAnimationFrame frame : this.frames) {
      fr.add(frame);
    }

    this.tileAnimationFrames = fr;
    return this.tileAnimationFrames;
  }

  @Override
  public int getTotalDuration() {
    if (this.totalDuration > 0) {
      return this.totalDuration;
    }

    if (this.getFrames().size() == 0) {
      return 0;
    }

    for (ITileAnimationFrame frame : this.getFrames()) {
      if (frame != null) {
        this.totalDuration += frame.getDuration();
      }
    }

    return this.totalDuration;
  }
}
