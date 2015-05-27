package whisper;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Implementation of the read-write lock interface.
 */
public class LockImpl implements Lock {

  /**
   * A pending request for a lock.
   */
  private class Request {

    /**
     * Lock request type (read or write);
     */
    private final Type type;

    /**
     * Whether the lock has been granted.
     */
    private boolean granted = false;

    /**
     * Create a new request of the given type.
     */
    public Request(Type type) {
      this.type = type;
    }

    /**
     * Get the lock request type.
     */
    public Type getType() {
      return type;
    }

    /**
     * Get whether the request has been granted.
     */
    public boolean isGranted() {
      return granted;
    }

    /**
     * Grant the lock request.
     */
    public void grant() {
      granted = true;
    }

  }

  /**
   * Queue of pending requests.
   */
  private final Queue<Request> queue = new LinkedList<Request>();

  /**
   * Whether there's an active writer.
   */
  private boolean writer = false;

  /**
   * Number of active readers.
   */
  private int readers = 0;

  @Override
  public void acquire(Type type) throws RemoteException {
    Request req = null;
    synchronized (queue) {
      if (type == Type.READ) {
        // no need to queue if there's no current or pending writer
        if (!writer && queue.isEmpty()) {
          readers++;
          return;
        }
      } else {
        // no need to queue if nobody else wants the lock
        if (!writer && readers == 0) {
          writer = true;
          return;
        }
      }
      // otherwise, queue the request
      req = new Request(type);
      queue.add(req);
    }
    synchronized (req) {
      // request might already have been notified if a concurrent release
      // thread beat us into the synchronization block, so check
      // for that before waiting to prevent possibility
      // of getting stuck in the wait forever
      if (!req.isGranted()) {
        try {
          req.wait();
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  @Override
  public void release() throws RemoteException {
    synchronized (queue) {
      if (writer) {
        if (!writer || readers > 0) { // sanity check for illegal caller
          throw new RuntimeException("illegal release");
        }
        // if there's a writer and everyone's following the rules,
        // we must be releasing the write lock
        writer = false;
      } else {
        if (readers == 0) { // another sanity check
          throw new RuntimeException("no active readers");
        }
        // otherwise a reader is finished
        readers--;
      }
      boolean done = false;
      while (!queue.isEmpty() && !done) {
        Request nextReq = queue.peek();
        if (nextReq.getType() == Type.READ) {
          done = writer; // can always read if no writer
          if (!done) {
            readers++;
          }
        } else {
          done = (writer || readers > 0); // must have no writer or readers
          if (!done) {
            writer = true;
          }
        }
        if (!done) {
          // we can grant the request and remove it from the queue
          queue.remove();
          synchronized (nextReq) {
            nextReq.grant();
            nextReq.notify();
          }
        }
      }
    }
  }

}
