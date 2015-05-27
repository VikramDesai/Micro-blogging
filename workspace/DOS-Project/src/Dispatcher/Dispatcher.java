package Dispatcher;

import java.net.MalformedURLException;
import java.rmi.AccessException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import FrontEndServer.FEServerInterface;

import Dispatcher.DispatcherInterface;

public class Dispatcher extends java.rmi.server.UnicastRemoteObject implements
		DispatcherInterface {

	private static final long serialVersionUID = 1L;
	FEServerInterface sender;
	FEServerInterface sender1;
	Registry frontEndServer1;
	Registry frontEndServer2;
	String feServer1Address = "localHost";
	String feServer2Address = "localHost";
	// String serverPort = "3232";

	String address;
	Registry dispatcher;
	int dispatchPort1 = 3234;
	String feSer1Port = "3235";
	String feSer2Port = "3236";

	public Dispatcher() throws RemoteException {
		try {
			address = "localHost";
		} catch (Exception e) {
			System.out.println("can't get inet address.");
		}

		System.out
				.println("this address=" + address + ",port=" + dispatchPort1);
		try {
			dispatcher = LocateRegistry.createRegistry(dispatchPort1);
		    dispatcher.rebind("dispatcher", this);
		} catch (RemoteException e) {
			System.out.println("remote exception" + e);
		}

		

		try{
			frontEndServer1 = LocateRegistry.getRegistry(feServer1Address,Integer.parseInt(feSer1Port));
			sender =   (FEServerInterface) frontEndServer1.lookup("frontEndServer_1");

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {

			System.err.println(e);
		}
		

		 
		
		  try { frontEndServer2 = LocateRegistry.getRegistry(feServer2Address,Integer.parseInt(feSer2Port));
		  sender1 = (FEServerInterface) (frontEndServer2
		  .lookup("frontEndServer_2"));
		  
		  } catch (RemoteException e) { e.printStackTrace(); } catch
		  (NotBoundException e) { System.err.println(e); }
		
	}

     
	 static public void main(String args[]){
		try{
			Dispatcher disp = new Dispatcher();
              //disp.startClient();		
		}
		catch (Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}


	public String postMessage(String username, String topic, String comment)
			throws RemoteException {

		String response = "";
		try {
			boolean flag1 = false;

			if (flag1 == false) {

				response = sender.postMessage(username, topic, comment);
				flag1 = true;
			}

			if (flag1 == true) {
			
				response = sender1.postMessage(username, topic, comment);

				flag1 = false;
			}

		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
		return (response);
	}

	public ArrayList<String> retrieve(String username,int timestamp, boolean proj,
			boolean swineRumor, boolean fallFoliage) throws RemoteException {

		ArrayList<String> Posts = new ArrayList<String>();
		try {
			boolean flag2 = false;

			if (flag2 == false) {

				Posts = sender
						.retrieve(username,timestamp, proj, swineRumor, fallFoliage);
				flag2 = true;
			}

			if (flag2 == true) {
				Posts = sender1
				.retrieve(username,timestamp, proj, swineRumor, fallFoliage);
				flag2 = false;
			}

		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}

		return Posts;
	}

	public String follow(String username, String topic) throws RemoteException {
		String subscribed = "";
		try {
			boolean flag3 = false;

			if (flag3 == false) {
				subscribed = sender.follow(username, topic);

				flag3 = true;
			}

			if (flag3 == true) {

				// call FEServer function to follow on feserver2
				subscribed = sender1.follow(username, topic);

			}

		} catch (Exception e) {

			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
		return subscribed;
	}

	public String unsubscribe(String username, String topic)
			throws RemoteException {
		String unsubscribed = "";
		try {
			boolean flag4 = false;

			if (flag4 == false) {

				unsubscribed = sender.unsubscribe(username, topic);
				flag4 = true;
			}

			if (flag4 == true) {
				unsubscribed = sender1.unsubscribe(username, topic);
			}

		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
		return unsubscribed;
	}

}