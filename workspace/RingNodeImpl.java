package whisper;

import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import whisper.Lock.Type;

/**
 * Implementation of the ring interface and all functionality common to the
 * frontend and backend nodes.
 */
public class RingNodeImpl implements RingNode {

  /**
   * Time in ms between distributed clock synchronizations (only used by
   * coordinator).
   */
  private final static long TIME_UPDATE_INTERVAL = 10000;

  /**
   * Random node id.
   */
  private final int id = new Random().nextInt();

  /**
   * Read-write lock object (only used by coordinator).
   */
  private final Lock distributedLock = new LockImpl();

  /**
   * Predecessor node in the logical ring.
   */
  private RingNode predecessor = this;

  /**
   * Successor node in the logical ring.
   */
  private RingNode successor = this;

  /**
   * The ring's coordinator node.
   */
  private RingNode leader;

  /**
   * Whether this node has initiated an election and is waiting for the ring
   * cycle to complete.
   */
  private boolean pendingElection = false;

  /**
   * The complete node ring received from the last election. Used by the clock
   * synchronization to know who the slaves are.
   */
  private final List<RingNode> lastRing = new ArrayList<RingNode>();

  /**
   * Timer handling periodic clock synchronizations (only used by coordinator).
   */
  private Timer timeUpdates;

  /**
   * This node's system clock offset to the synchronized time value.
   */
  private long timeOffset = 0;

  /**
   * RMI object registry.
   */
  private final Registry registry;

  /**
   * Name of RMI stub.
   */
  private final String stubName;

  /**
   * Set up the RMI registry with the given stub name.
   */
  public RingNodeImpl(String stubName) throws RemoteException, AlreadyBoundException {
    this.stubName = stubName;
    Registry tmp;
    try {
      tmp = LocateRegistry.createRegistry(PORT);
    } catch (RemoteException e) {
      tmp = LocateRegistry.getRegistry(PORT);
    }
    registry = tmp;
    Remote remote = UnicastRemoteObject.exportObject(this, 0);
    registry.bind(stubName, remote);
  }

  /**
   * Create a ring node without setting up any RMI infrastructure.
   */
  public RingNodeImpl() {
    this.stubName = null;
    this.registry = null;
  }

  /**
   * Shutdown RMI resources.
   */
  public void shutdown() throws Exception {
    if (registry != null) {
      registry.unbind(stubName);
      UnicastRemoteObject.unexportObject(registry, true);
    }
  }

  protected Registry getRegistry() {
    return registry;
  }

  @Override
  public int getId() {
    return id;
  }

  // //////////////////////////////////////////////////////////////////////
  // /////////////////// Ring construction functions. /////////////////////
  // //////////////////////////////////////////////////////////////////////

  /**
   * Have this node join the ring by attaching itself to the given successor.
   * Updates the necessary links to reform the logical ring.
   */
  protected void joinRing(RingNode successorNode) throws RemoteException {
    this.successor = successorNode;
    this.predecessor = successor.setPredecessor(this);
    predecessor.setSuccessor(this);
  }

  @Override
  public RingNode setPredecessor(RingNode newPredecessor) throws RemoteException {
    RingNode old = predecessor;
    predecessor = newPredecessor;
    return old;
  }

  @Override
  public RingNode setSuccessor(RingNode newSuccessor) throws RemoteException {
    RingNode old = successor;
    successor = newSuccessor;
    return old;
  }

  /**
   * Get the predecessor (for testing).
   */
  protected RingNode getPredecessor() {
    return predecessor;
  }

  /**
   * Get the successor (for testing).
   */
  protected RingNode getSuccessor() {
    return successor;
  }

  // //////////////////////////////////////////////////////////////////////
  // ///////////////////// Leader election functions. /////////////////////
  // //////////////////////////////////////////////////////////////////////

  /**
   * Have the node begin an election if it doesn't have one already in progress.
   */
  protected void startElection() throws RemoteException {
    if (!pendingElection) {
      election(id, new ArrayList<RingNode>());
    } else {
      System.out.println("ignoring start election");
    }
  }

  @Override
  public void election(int electionId, List<RingNode> nodes) throws RemoteException {
    if (electionId == id) {
      if (pendingElection) {
        // election finishing
        lastRing.clear();
        lastRing.addAll(nodes);
        int highestId = id;
        RingNode pendingLeader = this;
        for (RingNode node : nodes) {
          int nodeId = node.getId();
          if (nodeId > highestId) {
            highestId = nodeId;
            pendingLeader = node;
          }
        }
        // begin leader propagation
        successor.setLeader(electionId, pendingLeader);
        pendingElection = false;
      } else {
        // election starting
        pendingElection = true;
        successor.election(electionId, nodes);
      }
    } else {
      // forward election along ring
      nodes.add(this);
      successor.election(electionId, nodes);
    }
  }

  @Override
  public void setLeader(int electionId, RingNode leader) throws RemoteException {
    this.leader = leader;
    if (electionId != id) { // check if leader ring traversal has finished
      successor.setLeader(electionId, leader);
    }
    if (leader.getId() == id) {
      if (timeUpdates == null) {
        // this is the new leader, start periodic clock updates
        timeUpdates = new Timer();
        timeUpdates.schedule(new TimerTask() {
          @Override
          public void run() {
            updateTimes();
          }
        }, (TIME_UPDATE_INTERVAL >> 1), TIME_UPDATE_INTERVAL);
      }
    } else {
      if (timeUpdates != null) {
        // this is the old leader, cancel periodic clock updates
        timeUpdates.cancel();
        timeUpdates = null;
      }
    }
  }

  /**
   * Get the ring leader node.
   */
  protected RingNode getLeader() {
    return leader;
  }

  // //////////////////////////////////////////////////////////////////////
  // //////////////// Time synchronization functions. /////////////////////
  // //////////////////////////////////////////////////////////////////////

  /**
   * Start a clock synchronization round via the Berkeley algorithm. Gathers the
   * times from all nodes and averages them, then returns the averaged value.
   * Accounts for communication delay by estimates based on the duration to
   * request the time from each node.
   */
  protected void updateTimes() {
    try {
      startElection();
      long[] delays = new long[lastRing.size()]; // record communication delays
      long sum = 0;
      for (int i = 0; i < lastRing.size(); i++) {
        RingNode node = lastRing.get(i);
        long startTime = System.currentTimeMillis();
        sum += node.getTime();
        long duration = System.currentTimeMillis() - startTime;
        // approximate one-way delay as half the total delay
        delays[i] = (duration >> 1);
        sum += delays[i];
      }
      sum += getTime(); // add coordinator's time
      long newTime = sum / (lastRing.size() + 1); // take average
      for (int i = 0; i < lastRing.size(); i++) {
        lastRing.get(i).setTime(newTime + delays[i]); // send new times
      }
      setTime(newTime);
    } catch (RemoteException e) {
    }
  }

  @Override
  public long getTime() throws RemoteException {
    return (System.currentTimeMillis() - timeOffset);
  }

  @Override
  public void setTime(long time) throws RemoteException {
    timeOffset = System.currentTimeMillis() - time;
    System.out.println("set offset to " + timeOffset + ", new time " + getTime());
  }

  // //////////////////////////////////////////////////////////////////////
  // /////////////////// Mutual exclusion functions. //////////////////////
  // //////////////////////////////////////////////////////////////////////

  @Override
  public void acquireLock(Type type) throws RemoteException {
    distributedLock.acquire(type);
  }

  @Override
  public void releaseLock() throws RemoteException {
    distributedLock.release();
  }

}
