package RMI;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Iterator;


import Dispatcher.DispatcherInterface;

public class RmiClient {

	DispatcherInterface dispatcher;
	Registry registry;
	String serverAddress = "localHost";
	String dispatcherPort = "3234";

	Boolean proj = false;
	Boolean swineRumor = false;
	Boolean fallFoliage = false;


	// System.out.println("sending " + text + " to " + serverAddress + ":"
	// + serverPort);

	public RmiClient() {
		try {
			registry = LocateRegistry.getRegistry(serverAddress, Integer
					.parseInt(dispatcherPort));
			dispatcher = (DispatcherInterface) (registry.lookup("dispatcher"));

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			System.err.println(e);
		}
	}
 // Calls the post message to 
	public void postMsg(String username, String topic, String comment) {
		 String response="";
		  try{
			response = dispatcher.postMessage(username, topic, comment);
			} catch (Exception e) {
				e.printStackTrace();
			}
		 
		 
		System.out.println(response);
		
	}
    // This method subscribes the user to a particular topic
	public void follow(String username, String topic) {
 
		 String subscribed="";
		try{
				subscribed = dispatcher.follow(username, topic);
				} catch (Exception e) {
					e.printStackTrace();
				}
			 
			 
			System.out.println(subscribed);
		

	}
    
	 //This method checks if the User has subscribed to a particular topic.
   //The unsubscribe checks if the user has subscribed to a particular topic and unsubscribes him from recieving further posts on it.
	public void unsubscribe(String username, String topic) {
 
		String subscribed="";
		try{
				subscribed = dispatcher.follow(username, topic);
				} catch (Exception e) {
					e.printStackTrace();
				}
			 
			 
			System.out.println(subscribed);
		
		
				}
  //  Calls the retrieve function at the Server.
	public void retrieve(String username,int timestamp) {
		ArrayList<String> Posts = new ArrayList<String>();
		
		try {

			String Comment = null;
			
			
			Posts = dispatcher.retrieve(username,timestamp, proj, swineRumor,
					fallFoliage);
			int n = Posts.size();

			for (int i = 0; i < n; i++) {
				String retrievedPost = Posts.get(i);

				System.out.println(retrievedPost);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
