package whisper;

import java.io.Serializable;

/**
 * Listing of server return codes.
 */
public enum RetCode implements Serializable {

  OK, // request completed successfully
  BAD_USER, // provided username was invalid
  BAD_TOPIC, // provided topic was invalid
  BAD_COMMENT, // provided comment text was invalid
  INTERNAL_ERROR, // unknown server-side error

}
