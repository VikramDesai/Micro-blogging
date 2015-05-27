package whisper;

import java.io.Serializable;

/**
 * Class representing a single whisp.
 */
public class Whisp implements Comparable<Long>, Serializable {

  private static final long serialVersionUID = 354117518763744316L;

  /**
   * Whisp timestamp.
   */
  private final long ts;

  /**
   * Author username.
   */
  private final String user;

  /**
   * Whisp topic.
   */
  private final String topic;

  /**
   * Whisp content.
   */
  private final String text;

  /**
   * Create a whisp with the given fields.
   */
  public Whisp(String user, String topic, String text, long timestamp) {
    this.user = user;
    this.topic = topic;
    this.text = text;
    this.ts = timestamp;
  }

  public long getTimestamp() {
    return ts;
  }

  public String getUser() {
    return user;
  }

  public String getTopic() {
    return topic;
  }

  public String getText() {
    return text;
  }

  @Override
  public int compareTo(Long o) {
    return (ts < o) ? -1 : ((ts > o) ? 1 : 0);
  }

  @Override
  public String toString() {
    return topic + ": " + user + " @ " + ts + ": " + text;
  }

}
