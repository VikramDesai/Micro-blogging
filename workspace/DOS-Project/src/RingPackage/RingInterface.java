package RingPackage;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;


public interface RingInterface extends Remote{
public double getId();
public void updatePredecessor(Token predecessor);
public void updateSuccessor(Token successor);
public ArrayList<Token> leaderElection(ArrayList<Token> listTokens,int elecId);
public void informLeader(Token leader);
	
}
