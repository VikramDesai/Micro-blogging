package BackEndServer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Random;
import java.util.StringTokenizer;

import FrontEndServer.FEServerInterface;
import RingPackage.RingInterface;
import RingPackage.Token;

public class ServerRMI extends java.rmi.server.UnicastRemoteObject implements
		ReceiveMessageInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String address;
	Registry registry;

	double serverId = 0;

	Token backendToken_1 = null;
	Token leader = new Token();

	private String DATE_FORMAT_REQ = "yyyy-MM-dd hh:MM:ss";
	FEServerInterface backendsender1;
	FEServerInterface backendsender2;
	Registry backend1;
	Registry backend2;
	int feserver1port = 3235;
	int feserver2port = 3236;
	String FEServer1Add = "localhost";
	String FEServer2Add = "localhost";

	// ServerRMI server1 = new ServerRMI();

	String username = null;
	String timestamp = null;
	String topic = null;
	String comment = null;

	Boolean proj = false;
	Boolean swineFlu = false;
	Boolean fall = false;

	String[] tokens = new String[6];
	String strLine = null;

	// private static int timeFlag = 1;

	// ServerRMI server = new ServerRMI();

	Boolean topicSurvivingProj = false;
	Boolean topicSwineFluRumors = false;
	Boolean topicFallFoliage = false;

	// Constructor binds the server to the registry
	public ServerRMI() throws RemoteException {
           
		backendToken_1 = new Token();
		backendToken_1.setId();
		try {
			address = "localHost";
		} catch (Exception e) {
			System.out.println("can't get inet address.");
		}

		int port = 3232;
		System.out.println("this address=" + address + ",port=" + port);
		try {
			registry = LocateRegistry.createRegistry(port);
			registry.rebind("rmiServer", this);
			registry.rebind("backendToken", backendToken_1);
		} catch (RemoteException e) {
			System.out.println("remote exception" + e);
		}

		
		setPredecessor(backendToken_1);
		setSuccessor(backendToken_1);
		/* creating a RMI object for FEServer1 */
		/*
		 * try{
		 * 
		 * backend1 = LocateRegistry.getRegistry(FEServer1Add, feserver1port);
		 * backendsender1 = (FEServerInterface)(backend1
		 * .lookup("frontEndServer_1")); }catch(RemoteException e){
		 * System.out.println("remote exception"+ e); }catch (NotBoundException
		 * e) { System.err.println(e); }
		 * 
		 * try{
		 * 
		 * backend2 = LocateRegistry.getRegistry(FEServer1Add, feserver2port);
		 * backendsender2 = (FEServerInterface)(backend2
		 * .lookup("frontEndServer_2")); }catch(RemoteException e){
		 * System.out.println("remote exception"+ e); }catch (NotBoundException
		 * e) { System.err.println(e); }
		 */
	}

	static public void main(String args[]) {
		try {
			ServerRMI server = new ServerRMI();

			//server.setPredecessor(backendToken_1);
			//server.setSuccessor(backendToken_1);
			server.election();// start election

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	// Posts the user comment to the text file.
	public String postMsg(String username, String postTopic, String Comment)
			throws RemoteException {

		String serResponse = "";
		int logicTimeStamp = 0;
		Calendar calValue = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_REQ);

		if ((Comment.length() == 0) && (Comment.length() > 50)) {
			serResponse = "Invalid Comment";
		}

		try {

			File file = new File(
					"C:\\677\\workspace\\DOS-Project\\src\\whisps.txt");

			FileOutputStream fileEntry = new FileOutputStream(file, true);

			String timeStamp = dateFormat.format(calValue.getTime());

			logicTimeStamp++;

			String postData = timeStamp + "||" + postTopic + "||" + username
					+ "||" + Comment + "||" + "@whisp" + "||"
					+ String.valueOf(logicTimeStamp).toString();
			String newLine = "\n";

			fileEntry.write(postData.getBytes());
			fileEntry.flush();
			fileEntry.write(newLine.getBytes());
			fileEntry.close();
			serResponse = "Successfully Posted";
			System.out.println("Successfully Posted");
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}

		return serResponse;
	}

	// The method retrieve retrieves the posts on topics that the subscriber
	// subscribed after a given time stamp.
	public ArrayList<String> retrieve(String username, int timestamp,
			boolean proj, boolean swineRumor, boolean fallFoliage)
			throws RemoteException {

		DateFormat formatter;

		// formatter = new SimpleDateFormat(DATE_FORMAT_REQ);

		boolean fileP = false;
		boolean fileS = false;
		boolean fileF = false;

		String Final = null;
		int tokCount = 0;

		this.username = username;
		// String [][]Str1 = null
		ArrayList<String> Str1 = new ArrayList<String>();

		// Calendar calValue=Calendar.getInstance();
		// Calendar calDateDiff = Calendar.getInstance();

		// This is for the timeStamp

		try {

			/*
			 * try{
			 * 
			 * Date date ;
			 * 
			 * date = (Date)formatter.parse(timestamp);
			 * 
			 * calValue.setTime(date); System.out.println("Set Date is " +date
			 * ); } catch (ParseException e) {
			 * System.out.println("Exception :"+e); }
			 */FileInputStream fstream = new FileInputStream(
					"C:\\677\\workspace\\DOS-Project\\src\\whisps.txt");

			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				System.out.println("Reading each line" + strLine);
				// Tokenize the String
				StringTokenizer stringTok = new StringTokenizer(strLine, "||");
				while (stringTok.hasMoreTokens() && tokCount < 7) {
					tokens[tokCount] = stringTok.nextToken();
					System.out.println(tokens[tokCount]);
					tokCount++;
				}
				System.out.println("String Tokenised");
				// Set the timefield of the read line.
				// calDateDiff.setTime(formatter.parse(tokens[0]));

				int tokenValue = Integer.parseInt(tokens[5]);

				if (tokenValue > timestamp) {
					// System.out.println("Comparing time stamps");

					// Check the TOPIC in the readline
					if (tokens[1].equals("#proj1")) {
						fileP = true;
					}
					// Check the TOPIC in the readline
					if (tokens[1].equals("#swine")) {

						fileS = true;
					}
					// Check the TOPIC in the readline
					if (tokens[1].equals("#foliage")) {
						fileF = true;
					}

					// Check to see if the user had subscribed to any of the
					// Topics
					if ((proj && fileP) || (swineRumor && fileS)
							|| (fallFoliage && fileF)) {

						Final = tokens[1] + " " + tokens[2] + " " + tokens[3]
								+ " " + tokens[4] + "\n";
						Str1.add(Final);

					}

				} else {
					String Response = "Invalid Date";
					Str1.add(Response);
				}
				tokCount = 0;

			}
			in.close();

		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());

		}
		return Str1;
	}

	/* Upon startup the Server tries to contact the 2 frontEnd Servers */

	

	public void setPredecessor(Token predecessor) throws RemoteException {
        try{
		backendToken_1.updatePredecessor(predecessor);
        }catch (Exception e){
        	
        	System.err.println("Error: " + e.getMessage());
        
        }
	}

	public void setSuccessor(Token successor) throws RemoteException{
       try{
	 	backendToken_1.updateSuccessor(successor);
       }catch (Exception e){
    	   
    	   System.err.println("Error: " + e.getMessage());   
       }
	}

	public void election() {

		double largeID = 0;
		ArrayList<Token> objectTokens = new ArrayList<Token>();
		int startId = 0;

		Random rand = new Random();
		startId = rand.nextInt();
		objectTokens = backendToken_1.leaderElection(objectTokens, startId);

		Iterator it = null;
		for (it = objectTokens.iterator(); it.hasNext();) {
			Token t = (Token) it.next();
			if (t.getId() > largeID) {
				largeID = t.getId();
				leader = t;
			}

		}
		backendToken_1.informLeader((Token) leader);
		System.out.println("Informing the leader");
	}

}
