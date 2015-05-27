package whisper;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for a read-write lock. Multiple readers are allowed concurrently,
 * but only a single writer at once, and readers and writers cannot hold locks
 * concurrently. Lock behavior is undefined if callers make illegal calls to
 * {@link #release()} (i.e., calling it when no locks are held).
 */
public interface Lock extends Remote {

  /**
   * Enum type specifying either a read or a read/write lock.
   */
  public enum Type {
    READ, WRITE
  }

  /**
   * Acquire a lock of the specified type (either read or read/write). Blocks
   * until the lock can be granted, then returns, at which point the caller is
   * considered to hold the requested lock. Order of requests is honored, so
   * even if a read request arrives when only readers are active, any pending
   * write requests will be granted first.
   */
  public void acquire(Type type) throws RemoteException;

  /**
   * Release the caller's lock and return immediately (never blocks). If the
   * caller holds multiple read locks (for whatever reason), should be called
   * once for each lock. It is assumed that the caller actually holds at least
   * one lock -- calling when this is not true will result in undefined lock
   * behavior.
   */
  public void release() throws RemoteException;

}
