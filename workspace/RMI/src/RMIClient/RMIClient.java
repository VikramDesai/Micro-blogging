package RMIClient;

import java.rmi.*;
import java.rmi.registry.*;
import java.net.*;

public class RMIClient{
  static public void main(String args[]){
    ReceiveMessageInterface rmiServer;
    Registry registry;
    String serverAddress=args[0];
    String serverPort=args[1];
    String text=args[2];
    System.out.println
           ("sending " + text + " to " +serverAddress + ":" + serverPort);
    try{
      registry=LocateRegistry.getRegistry
                      (serverAddress,(new Integer(serverPort)).intValue());
      rmiServer=(ReceiveMessageInterface)(registry.lookup("rmiServer"));
      // call the remote method
      rmiServer.receiveMessage(text);
    }
    catch(RemoteException e){
      e.printStackTrace();
    }
    catch(NotBoundException e){
      System.err.println(e);
    }
  }
} 
