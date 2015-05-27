package whisper;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import whisper.Lock.Type;

/**
 * Interface for a member of the logical ring comprising the system. This
 * includes both frontend and backend components. Provides methods for all major
 * distributed components of the system: constructing the logical ring, holding
 * coordinator elections, Berkeley clock synchronization, and distributed mutual
 * exclusion with read-write locks.
 */
public interface RingNode extends Remote {

  /**
   * System-wide communication port.
   */
  public static final int PORT = 8383;

  /**
   * Get the id of the ring node. This is a fixed, randomly generated integer.
   */
  public int getId() throws RemoteException;

  /**
   * Set a new predecessor for the node. Returns the old predecessor.
   */
  public RingNode setPredecessor(RingNode predecessor) throws RemoteException;

  /**
   * Set a new successor for the node. Returns the old successor.
   */
  public RingNode setSuccessor(RingNode successor) throws RemoteException;

  /**
   * Forward an ongoing election. The election id is the id of the node that
   * initiated the election, and the node list is the list of all nodes that the
   * election has passed through, excluding the initiator. Once the election has
   * made a complete cycle, the initiator begins the leader cycle.
   */
  public void election(int electionId, List<RingNode> nodes) throws RemoteException;

  /**
   * Forward an ongoing cycle informing the ring of the new coordinator. The
   * election id is as for the election itself. The leader is the new
   * coordinator node.
   */
  public void setLeader(int electionId, RingNode leader) throws RemoteException;

  /**
   * Get the node's current synchronized time (the system time plus a
   * synchronization offset value).
   */
  public long getTime() throws RemoteException;

  /**
   * Set the node's current time to the given value.
   */
  public void setTime(long time) throws RemoteException;

  /**
   * Acquire a lock from the node of the given type. Follows the same semantics
   * as {@link Lock#acquire(Type)}.
   */
  public void acquireLock(Type type) throws RemoteException;

  /**
   * Release a lock held from the node. Follows the same semantics as
   * {@link Lock#release()}.
   */
  public void releaseLock() throws RemoteException;

}
