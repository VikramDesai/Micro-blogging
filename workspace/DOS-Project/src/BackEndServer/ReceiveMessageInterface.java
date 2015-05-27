package BackEndServer;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import RingPackage.Token;



	//the interface declares the methods implemented by the Server which extends it.
	public interface ReceiveMessageInterface extends Remote{
	
	  
	  public String postMsg(String username, String topic, String comment ) throws RemoteException;
	  public ArrayList<String> retrieve(String username,int timestamp,boolean proj,boolean swineRumor,boolean fallFoliage) throws RemoteException;
	  public void setPredecessor(Token predecessor) throws RemoteException;
	  public void setSuccessor(Token successor) throws RemoteException;
	  
	  
	}
	  
