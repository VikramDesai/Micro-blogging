package whisper;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Random;

/**
 * Test setup for Whisper.com.
 */
public class TestClient extends Thread {

  private static double WRITE_PERCENT;

  private static int REQUESTS;

  public static void main(String[] args) throws Exception {
    if (args.length < 2) {
      System.out
          .println("Usage: java whisper.TestClient <frontEndServer> <threadCount> <writeProp> <requests>");
      System.exit(0);
    }
    String frontEnd = args[0];
    int threadCount = Integer.parseInt(args[1]);
    WRITE_PERCENT = Double.parseDouble(args[2]);
    REQUESTS = Integer.parseInt(args[3]);
    TestClient[] threads = new TestClient[threadCount];
    for (int i = 0; i < threadCount; i++) {
      threads[i] = new TestClient(frontEnd);
    }
    for (TestClient thread : threads) {
      thread.start();
    }
    for (TestClient thread : threads) {
      thread.join();
    }
    double total = 0;
    for (TestClient thread : threads) {
      total += thread.getAvg();
    }
    double avg = total / threadCount;
    System.out.println("avg is " + avg);
  }

  private final FrontEnd frontEnd;

  private double avg;

  public TestClient(String frontEnd) throws AccessException, RemoteException, NotBoundException {
    this.frontEnd = (FrontEnd) LocateRegistry.getRegistry(frontEnd, RingNode.PORT).lookup(
        FrontEnd.stubName);
  }

  @Override
  public void run() {
    try {
      frontEnd.follow("user", "proj1");
      frontEnd.follow("user", "swine");
      frontEnd.follow("user", "foliage");
    } catch (RemoteException e) {
      throw new RuntimeException(e);
    }
    Random rand = new Random();
    byte[] bytes = new byte[25];
    long total = 0;
    for (int i = 0; i < REQUESTS; i++) {
      String topic;
      if (rand.nextDouble() < 0.05) {
        topic = "bad";
      } else {
        double test = rand.nextDouble();
        if (test < 0.3333) {
          topic = "proj1";
        } else if (test < 0.6666) {
          topic = "swine";
        } else {
          topic = "foliage";
        }
      }
      rand.nextBytes(bytes);
      String text = new String(bytes).replace('\n', ' ').replace('\r', ' ');
      long start = System.nanoTime();
      try {
        if (rand.nextDouble() < WRITE_PERCENT) {
          frontEnd.post("user", topic, text);
        } else {
          frontEnd.retrieve("user", 1);
        }
      } catch (RemoteException e) {
        throw new RuntimeException(e);
      }
      long time = System.nanoTime() - start;
      total += time;
      try {
        Thread.sleep(20);
      } catch (InterruptedException e) {
      }
    }
    this.avg = total / 1000.0;
  }

  public double getAvg() {
    return avg;
  }

}
