package whisper;

import java.rmi.RemoteException;
import java.util.List;

/**
 * Interface for front end of Whisper.com. Legal topics must be in the list of
 * allowed topics. Legal usernames must be nonempty and contain no spaces. Legal
 * comments must be nonempty and be shorter than 50 characters.
 */
public interface FrontEnd extends RingNode {

  /**
   * Name of the stub in the RMI registry.
   */
  public static final String stubName = "FrontEnd";

  /**
   * Post a new whisp on a particular topic. Timestamps the whisp according to
   * the synchronized clock and acquires the write lock while posting.
   * @param username Username to post as.
   * @param topic Topic to post to.
   * @param comment Comment to post.
   * @return OK or an error code.
   */
  public RetCode post(String username, String topic, String comment) throws RemoteException;

  /**
   * Subscribe a user to a new topic.
   * @param username User to add subscription.
   * @param topic Topic to subscribe to.
   * @return OK or an error code.
   */
  public RetCode follow(String username, String topic) throws RemoteException;

  /**
   * Unsubscribe a user from a topic.
   * @param username Modify this user's subscriptions.
   * @param topic Remove this topic.
   * @return OK or an error code.
   */
  public RetCode unsubscribe(String username, String topic) throws RemoteException;

  /**
   * Fetch all whisps on a user's subscribed topics since the specified
   * timestamp. Acquires a read lock while querying the backend.
   * @param username User of topics to retrieve.
   * @param timestamp Cutoff timestamp.
   * @return A (possibly empty) list of whisps.
   */
  public List<Whisp> retrieve(String username, long timestamp) throws RemoteException;

}
