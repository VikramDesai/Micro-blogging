package FrontEndServer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import BackEndServer.ReceiveMessageInterface;
import RingPackage.RingInterface;
import RingPackage.Token;

public class FEServer2 extends java.rmi.server.UnicastRemoteObject implements
		FEServerInterface {

	private static final long serialVersionUID = 1L;
	static ArrayList<String> proj1 = new ArrayList<String>();
	static ArrayList<String> swine = new ArrayList<String>();
	static ArrayList<String> foliage = new ArrayList<String>();

	Boolean proj = false;
	Boolean swineRumor = false;
	Boolean fallFoliage = false;

	Token feServerToken_2 = new Token();
	Token leader = new Token();

	String username = null;
	int operation = 0;
	String topic = null;
	String comment = null;
	int timestamp = 0;

	ReceiveMessageInterface frontEndServer2;
	static RingInterface frontEndServer1_token;
	static RingInterface backEndServer_token;
	FEServerInterface frontEndServer1;
	String address;
	Registry feserver2;
	Registry frontEnd2;
	Registry frontEnd1;
	int feserver2Port = 3236;
	int serverPort = 3232;
	int feServer1Port = 3235;
	String serverAddress = "localhost";
	String feServer1Address = "localhost";

	public FEServer2() throws RemoteException {

		try {
			address = "localHost";
		} catch (Exception e) {
			System.out.println("can't get inet address.");
		}

		System.out
				.println("this address=" + address + ",port=" + feserver2Port);
		try {
			feserver2 = LocateRegistry.createRegistry(feserver2Port);
			feserver2.rebind("frontEndServer_2", this);
			feserver2.rebind("frontEndToken_2", feServerToken_2);
		} catch (RemoteException e) {
			System.out.println("remote exception" + e);
		}

		try {
			frontEnd2 = LocateRegistry.getRegistry(serverAddress, serverPort);
			frontEndServer2 = (ReceiveMessageInterface) (frontEnd2
					.lookup("rmiServer"));
			backEndServer_token = (RingInterface) (frontEnd2
					.lookup("backendToken"));
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {

			System.err.println(e);
		}

		try {
			frontEnd1 = LocateRegistry.getRegistry(feServer1Address,
					feServer1Port);
			frontEndServer1 = (FEServerInterface) (frontEnd1
					.lookup("frontEndServer_1"));
			frontEndServer1_token = (RingInterface) (frontEnd2
					.lookup("feServer1Token"));

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {

			System.err.println(e);
		}

	}

	static public void main(String args[]) {
		try {
			FEServer2 feServer_2 = new FEServer2();
			feServer_2.setSuccessor((Token) backEndServer_token);
			feServer_2.setPredecessor((Token) frontEndServer1_token);
			feServer_2.election();//start election
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void initialisePeer() throws RemoteException {

		try {
			frontEndServer1.setSuccessor(feServerToken_2);
			frontEndServer2.setPredecessor(feServerToken_2);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String postMessage(String username, String topic, String comment)
			throws RemoteException {
		this.username = username;
		this.topic = topic;
		this.comment = comment;
		String response = "";
		try {
			response = frontEndServer2.postMsg(username, topic, comment);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return (response);

	}

	public void isSubscribed(String username) {

		try {

			proj = false;
			Iterator<String> iter1 = this.proj1.iterator();
			String currItem1 = null;
			while (iter1.hasNext() == true) {
				currItem1 = iter1.next();
				System.out.println(currItem1);
				if (username.equalsIgnoreCase(currItem1)) {
					System.out.println("User Exists");
					proj = true;
				}
			}

			swineRumor = false;
			Iterator<String> iter2 = this.swine.iterator();
			String currItem2 = null;
			while (iter1.hasNext() == true) {
				currItem2 = iter2.next();
				System.out.println(currItem2);
				if (username.equalsIgnoreCase(currItem2)) {

					swineRumor = true;
				}
			}

			fallFoliage = false;
			Iterator<String> iter3 = this.foliage.iterator();
			String currItem3 = null;
			while (iter3.hasNext() == true) {
				currItem3 = iter3.next();
				if (username.equalsIgnoreCase(currItem3)) {
					System.out.println("User Exists");
					fallFoliage = true;
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// The unsubscribe checks if the user has subscribed to a particular topic
	// and unsubscribes him from recieving further posts on it.
	public String unsubscribe(String username, String topic)
			throws RemoteException {

		String unsubscribed = "";

		try {
			System.out.println("Entered the unsubscribe class");
			this.username = username;
			this.topic = topic;

			System.out.println(username);
			try {

				isSubscribed(username);

				System.out.println("handler returned");

				if (proj == true && topic.equalsIgnoreCase("#proj1")) {

					Iterator<String> iter1 = proj1.iterator();
					String currItem1 = null;
					while (iter1.hasNext() == true) {
						currItem1 = iter1.next();
						System.out.println(currItem1);
						if (username.equalsIgnoreCase(currItem1)) {
							System.out.println("User exits");
							iter1.remove();
							unsubscribed = "User has Unsubscribed to the post on Surviving The Project";

						}

					}
				}

				if ((swineRumor == true) && (topic.equalsIgnoreCase("#swine"))) {

					Iterator<String> iter1 = swine.iterator();
					String currItem1 = null;
					while (iter1.hasNext() == true) {
						currItem1 = iter1.next();
						System.out.println(currItem1);
						if (username.equalsIgnoreCase(currItem1)) {
							System.out.println("User exits");
							iter1.remove();
							unsubscribed = "User has Unsubscribed to the post on SwineFlu Rumors";
						}

					}
				}

				if ((fallFoliage == true)
						&& (topic.equalsIgnoreCase("#foliage"))) {

					Iterator<String> iter1 = foliage.iterator();
					String currItem1 = null;
					while (iter1.hasNext() == true) {
						currItem1 = iter1.next();
						System.out.println(currItem1);
						if (username.equalsIgnoreCase(currItem1)) {
							System.out.println("User exits");
							iter1.remove();
							unsubscribed = "User has Unsubscribed to the post on SwineFlu Rumors";
						}

					}
				}

				if ((proj == false) && (swineRumor == false)
						&& (fallFoliage == false)) {

					unsubscribed = "Error : user has not subscribed to any topics";
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return (unsubscribed);

	}

	public String follow(String username, String topic) throws RemoteException {

		String subscribed = "";
		try {
			try {
				// Checks if the topic #proj1
				if (topic.equals("#proj1")) {

					boolean itemFound = false;
					Iterator<String> iter = proj1.iterator();
					String currItem = null;
					while (iter.hasNext() == true) {
						currItem = iter.next();
						System.out.println(currItem);
						System.out.println(username);
						if (username.equalsIgnoreCase(currItem)) {
							subscribed = "User" + username
									+ "has already subscribed to the topic";
							itemFound = true;

						}
					}

					if (itemFound == false) {
						// if the user is not found.Add the user.
						subscribed = "Adding user to the #proj1";
						proj1.add(username);

					}

				}
				// Checks if the topic #swine
				if (topic.equals("#swine")) {
					boolean itemFound = false;
					Iterator<String> iter = this.swine.iterator();
					String currItem = null;
					while (iter.hasNext() == true) {
						currItem = iter.next();
						if (username.equalsIgnoreCase(currItem)) {
							subscribed = "User" + username
									+ "already subscribed to this topic";
							itemFound = true;
						}
					}

					if (itemFound == false) {
						// if the user is not found.Add the user.
						swine.add(username);
						subscribed = "Addded the user to swineflu rumours ";
					}
				} // Checks if the topic #foliage
				if (topic.equalsIgnoreCase("#foliage")) {

					boolean itemFound = false;
					Iterator<String> iter = this.foliage.iterator();
					String currItem = null;
					while (iter.hasNext() == true) {
						currItem = iter.next();
						if (currItem == username) {

							subscribed = "User" + username
									+ " already subscribed to this topic";
							itemFound = true;
						}
					}

					if (itemFound == false) {
						// if the user is not found.Add the user.
						foliage.add(username);
						subscribed = "Addded the user to fall foliage";
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return (subscribed);
	}

	public ArrayList<String> retrieve(String username, int timestamp,
			boolean proj, boolean swineRumor, boolean fallFoliage)
			throws RemoteException {

		ArrayList<String> Posts = new ArrayList<String>();
		try {

			this.username = username;
			this.timestamp = timestamp;
			try {

				String Comment = null;

				isSubscribed(username);

				Posts = frontEndServer2.retrieve(username, timestamp, proj,
						swineRumor, fallFoliage);
				int n = Posts.size();

				/*
				 * for (int i = 0; i < n; i++) { String retrievedPost =
				 * Posts.get(i);
				 * 
				 * System.out.println(retrievedPost);
				 * 
				 * }
				 */
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return (Posts);
	}

	public void setPredecessor(Token predecessor) {

		feServerToken_2.updatePredecessor(predecessor);

	}

	public void setSuccessor(Token successor) {

		feServerToken_2.updateSuccessor(successor);
	}

	public void election() {

		double largeID = 0;
		ArrayList<Token> objectTokens = new ArrayList<Token>();
		int startId = 0;

		Random rand = new Random();
		startId = rand.nextInt();
		objectTokens = feServerToken_2.leaderElection(objectTokens, startId);

		Iterator it = null;
		for (it = objectTokens.iterator(); it.hasNext();) {
			Token t = (Token) it.next();
			if (t.getId() > largeID) {
				largeID = t.getId();
				 leader = t;
			}

		}
               feServerToken_2.informLeader((Token)leader);	
               System.out.println("Informing the leader");
	}
	
	

}
