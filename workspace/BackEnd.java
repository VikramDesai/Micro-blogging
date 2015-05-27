package whisper;

import java.rmi.RemoteException;
import java.util.List;

/**
 * Interface for database backend of Whisper.com.
 */
public interface BackEnd extends RingNode {

  /**
   * Name of the stub in the RMI registry.
   */
  public static final String stubName = "BackEnd";

  /**
   * Store a new whisp to the database. Caller must hold the write lock.
   * @param whisp The new whisp to store.
   * @return OK or an internal error code.
   */
  public RetCode store(Whisp whisp) throws RemoteException;

  /**
   * Query the database for all whisps on a topic since a specified timestamp.
   * Caller must hold a read lock.
   * @param topic Topic to query.
   * @param timestamp Timestamp cutoff to query.
   * @return A (possible empty) list of whisps.
   */
  public List<Whisp> query(String topic, long timestamp) throws RemoteException;

}
