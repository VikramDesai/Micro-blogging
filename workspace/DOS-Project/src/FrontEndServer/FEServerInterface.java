package FrontEndServer;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import RingPackage.Token;

public interface FEServerInterface extends Remote{
	public String postMessage(String username,String topic, String comment)throws RemoteException ;
	public String unsubscribe(String username, String topic)throws RemoteException ;
	public String follow(String username, String topic) throws RemoteException;
	public ArrayList<String> retrieve(String username,int timestamp,boolean proj,boolean swineRumor,boolean fallFoliage)	throws RemoteException ;
	public void setPredecessor(Token predecessor)throws RemoteException ;
	public void setSuccessor(Token successor)throws RemoteException;
}
