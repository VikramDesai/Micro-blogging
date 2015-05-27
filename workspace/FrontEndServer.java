package whisper;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import whisper.Lock.Type;

/**
 * Implementation of the Whisper.com front end server.
 */
public class FrontEndServer extends RingNodeImpl implements FrontEnd {

  /**
   * Start up the front end server.
   */
  public static void main(String[] args) {
    if (args.length < 1) {
      System.out.println("Usage: java whisper.FrontEndServer <backEndServer>");
      System.exit(0);
    }
    try {
      new FrontEndServer(args[0]);
      System.out.println("Front end server started");
    } catch (Exception e) {
      System.out.println("Failed to create front end server: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Set of all allowed topics.
   */
  private static final Set<String> topics = new HashSet<String>();
  static {
    topics.add("proj1");
    topics.add("swine");
    topics.add("foliage");
  }

  /**
   * A topic must be in the list of allowed topics.
   */
  public static boolean checkTopic(String topic) {
    return topics.contains(topic);
  }

  /**
   * A username must be nonempty and contain no spaces.
   */
  public static boolean checkUser(String username) {
    return (!username.isEmpty() && username.indexOf(' ') == -1);
  }

  /**
   * A comment must be nonempty and be less than 50 characters.
   */
  public static boolean checkComment(String comment) {
    return (!comment.isEmpty() && comment.length() < 50);
  }

  /**
   * Backend remote object.
   */
  private final BackEnd backEnd;

  /**
   * Map of usernames to the sets of topics subscribed to by those users.
   */
  private final Map<String, Set<String>> subscriptions = new HashMap<String, Set<String>>();

  /**
   * Create the front end server and connect to the specified back end host.
   */
  public FrontEndServer(String backEndHost) throws IOException, NotBoundException,
      AlreadyBoundException {
    super(FrontEnd.stubName);
    this.backEnd = (BackEnd) LocateRegistry.getRegistry(backEndHost, RingNode.PORT).lookup(
        BackEnd.stubName);
    joinRing(backEnd);
    startElection();
  }

  @Override
  public RetCode follow(String username, String topic) throws RemoteException {
    if (!checkUser(username)) {
      return RetCode.BAD_USER;
    } else if (!checkTopic(topic)) {
      return RetCode.BAD_TOPIC;
    }
    Set<String> existing = subscriptions.get(username);
    if (existing == null) {
      // new user, may need to create new subscription set
      synchronized (subscriptions) {
        existing = subscriptions.get(username);
        if (existing == null) {
          // set should be concurrency-friendly
          existing = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
          subscriptions.put(username, existing);
        }
      }
    }
    existing.add(topic);
    return RetCode.OK;
  }

  @Override
  public RetCode post(String username, String topic, String comment) throws RemoteException {
    if (!checkUser(username)) {
      return RetCode.BAD_USER;
    } else if (!checkTopic(topic)) {
      return RetCode.BAD_TOPIC;
    } else if (!checkComment(comment)) {
      return RetCode.BAD_COMMENT;
    }
    Whisp whisp = new Whisp(username, topic, comment, getTime());
    RingNode leader = getLeader(); // get coordinator
    leader.acquireLock(Type.WRITE); // get coordinator's write lock
    RetCode code = backEnd.store(whisp); // lock acquired, post the whisp
    leader.releaseLock(); // release write lock
    return code;
  }

  @Override
  public List<Whisp> retrieve(String username, long timestamp) throws RemoteException {
    List<Whisp> results = new ArrayList<Whisp>();
    Set<String> existing = subscriptions.get(username);
    if (existing != null) {
      RingNode leader = getLeader(); // get coordinator
      leader.acquireLock(Type.READ); // get a read lock from the coordinator
      for (String topic : existing) {
        results.addAll(backEnd.query(topic, timestamp)); // query backend
      }
      leader.releaseLock(); // release read lock
    }
    return results;
  }

  @Override
  public RetCode unsubscribe(String username, String topic) throws RemoteException {
    if (!checkUser(username)) {
      return RetCode.BAD_USER;
    } else if (!checkTopic(topic)) {
      return RetCode.BAD_TOPIC;
    }
    Set<String> existing = subscriptions.get(username);
    if (existing != null) {
      existing.remove(topic);
    }
    return RetCode.OK;
  }

}
