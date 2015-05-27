package whisper;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.List;
import java.util.Scanner;

/**
 * Command-line client implementation for talking to the Whisper.com front end.
 */
public class Client {

  /**
   * Create the client for talking to the specified front end server.
   */
  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      System.out.println("Usage: java whisper.Client <frontEndServer> <username>");
      System.exit(0);
    }
    try {
      new Client(args[0], args[1]).runInputLoop();
    } catch (Exception e) {
      System.out.println("Failed to contact front end server.");
    }
  }

  /**
   * Remote front end object.
   */
  private final FrontEnd frontEnd;

  /**
   * Active username being used in all requests.
   */
  private final String username;

  /**
   * Create the client as the specified user and contact the specified hostname.
   */
  protected Client(String frontEndHost, String username) throws AccessException, RemoteException,
      NotBoundException {
    this.frontEnd = (FrontEnd) LocateRegistry.getRegistry(frontEndHost, RingNode.PORT).lookup(
        FrontEnd.stubName);
    this.username = username;
  }

  /**
   * Run the loop of collecting and parsing a user request indefinitely.
   */
  public void runInputLoop() {
    Scanner s = new Scanner(System.in);
    System.out.print("Using username '" + username + "'.\nType 'quit' to exit\n> ");
    String input = s.nextLine().toLowerCase();
    while (!input.equals("quit")) {
      try {
        RetCode retCode = sendRequest(input);
        if (retCode != null) {
          System.out.print(processCode(retCode) + "\n> ");
        }
      } catch (RemoteException e) {
        System.out.print("Connection to server failed.\n> ");
      }
      input = s.nextLine().toLowerCase();
    }
  }

  /**
   * Parse and send the given request string if it parses successfully. Returns
   * null if the request does not parse.
   */
  private RetCode sendRequest(String request) throws RemoteException {
    String[] args = request.split(" ");
    if (args.length < 2) {
      return badRequest();
    }
    if (args[0].equals("follow")) {
      return frontEnd.follow(username, args[1]);
    } else if (args[0].equals("post")) {
      if (args.length < 3) {
        return badRequest();
      }
      StringBuilder comment = new StringBuilder(args[2]);
      for (int i = 3; i < args.length; i++) {
        comment.append(' ').append(args[i]);
      }
      return frontEnd.post(username, args[1], comment.toString());
    } else if (args[0].equals("retrieve")) {
      long ts;
      try {
        ts = Long.parseLong(args[1]);
      } catch (NumberFormatException e) {
        return badRequest();
      }
      List<Whisp> results = frontEnd.retrieve(username, ts);
      for (Whisp result : results) {
        System.out.println(result);
      }
      return RetCode.OK;
    } else if (args[0].equals("unsubscribe")) {
      return frontEnd.unsubscribe(username, args[1]);
    } else {
      return badRequest();
    }
  }

  private RetCode badRequest() {
    System.out.print("Bad request.\n> ");
    return null;
  }

  /**
   * Print a message corresponding to the specified return code.
   */
  private String processCode(RetCode code) {
    switch (code) {
      case OK:
        return "Request completed.";
      case BAD_USER:
        return "Invalid username supplied (doesn't exist).";
      case BAD_TOPIC:
        return "Invalid topic supplied (doesn't exist).";
      case BAD_COMMENT:
        return "Invalid comment supplied (must be 1 to 50 characters).";
      case INTERNAL_ERROR:
        return "An internal processing error occurred.";
      default:
        return null;
    }
  }

}
