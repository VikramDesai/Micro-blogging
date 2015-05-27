package whisper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of Whisper.com back end server.
 */
public class BackEndServer extends RingNodeImpl implements BackEnd {

  /**
   * Create the back end server using the specified database file.
   */
  public static void main(String[] args) {
    if (args.length < 1) {
      System.out.println("Usage: java whisper.BackEndServer <dbname>");
      System.exit(0);
    }
    try {
      new BackEndServer(args[0]);
      System.out.println("Back end server started");
      System.out.println("numthreads is " + Thread.getAllStackTraces().size());
    } catch (Exception e) {
      System.out.println("Failed to create back end server: " + e.getMessage());
      e.printStackTrace();
    }
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        double avg = queryTimeTotal / queryCount;
        System.out.println("average query time is " + avg);
      }
    });
  }

  /**
   * Running total of retrieval times.
   */
  private static double queryTimeTotal = 0.0;

  /**
   * Running count of retrieval requests.
   */
  private static double queryCount = 0.0;

  /**
   * Format of database entry: timestamp username topic text.
   */
  private static final String entryRegex = "(\\d+) (\\w+) (\\w+) (.*)";

  /**
   * Database file writer.
   */
  private final FileWriter dbWriter;

  /**
   * In-memory copy of database to avoid disk overhead during queries.
   */
  private final Map<String, List<Whisp>> db = new HashMap<String, List<Whisp>>();

  /**
   * Construct a new back end server using the specified database file. If the
   * file already exists, reads in all existing data to the in-memory database.
   */
  public BackEndServer(String filename) throws IOException, AlreadyBoundException {
    super(BackEnd.stubName);
    readDbFile(filename);
    dbWriter = new FileWriter(filename, true);
  }

  /**
   * Shutdown the server.
   */
  @Override
  public void shutdown() throws Exception {
    super.shutdown();
    dbWriter.close();
  }

  /**
   * Reconstructs the in-memory database from the given file.
   */
  private void readDbFile(String filename) throws IOException {
    File file = new File(filename);
    if (file.createNewFile()) {
      return;
    }
    Pattern pattern = Pattern.compile(entryRegex);
    BufferedReader reader = new BufferedReader(new FileReader(filename));
    String line = reader.readLine();
    while (line != null) {
      // match each line with regex
      Matcher m = pattern.matcher(line);
      if (!m.matches()) {
        throw new IOException("malformed database file");
      }
      Whisp whisp = new Whisp(m.group(2), m.group(3), m.group(4), Long.parseLong(m.group(1)));
      addToDb(whisp, true);
      line = reader.readLine();
    }
    reader.close();
  }

  /**
   * Add a whisp (either a new one or one from disk) to the in-memory database.
   */
  private void addToDb(Whisp whisp, boolean fromFile) {
    String topic = whisp.getTopic();
    List<Whisp> existing = db.get(topic);
    if (existing == null) {
      // may need to create new list
      // synchronize and check again to make sure another thread hasn't done
      // it already
      existing = db.get(topic);
      if (existing == null) {
        existing = new ArrayList<Whisp>();
        db.put(topic, existing);
      }
    }
    if (fromFile && !existing.isEmpty()) {
      // database file may not be in order, so can't assume end of the list
      // linear search is better than binary search because insertion point is
      // almost certainly at or very close to the end
      long timestamp = whisp.getTimestamp();
      for (int i = existing.size() - 1; i >= 0; i--) {
        if (existing.get(i).getTimestamp() <= timestamp) {
          existing.add(i + 1, whisp);
        }
      }
    } else {
      existing.add(whisp);
    }
  }

  /**
   * Writes a whisp to the persistent (on-disk) database file.
   */
  private void writeEntry(Whisp w) throws IOException {
    synchronized (dbWriter) {
      dbWriter.write(w.getTimestamp() + " " + w.getUser() + " " + w.getTopic() + " " + w.getText()
          + "\n");
      dbWriter.flush();
    }
  }

  @Override
  public List<Whisp> query(String topic, long timestamp) throws RemoteException {
    queryCount++;
    long start = System.nanoTime();
    List<Whisp> results = new ArrayList<Whisp>();
    List<Whisp> all = db.get(topic);
    if (all == null) {
      // bad topic, just return empty list
      return results;
    }
    int insertionPoint = Collections.binarySearch(all, timestamp);
    if (insertionPoint < 0) {
      // convert to regular list index
      insertionPoint = (insertionPoint + 1) * -1;
    }
    if (all.size() >= insertionPoint) {
      // grab appropriate sublist
      results.addAll(all.subList(insertionPoint, all.size()));
    }
    queryTimeTotal += (System.nanoTime() - start);
    return results;
  }

  @Override
  public RetCode store(Whisp whisp) throws RemoteException {
    addToDb(whisp, false);
    try {
      writeEntry(whisp);
      return RetCode.OK;
    } catch (IOException e) {
      return RetCode.INTERNAL_ERROR;
    }
  }

}
