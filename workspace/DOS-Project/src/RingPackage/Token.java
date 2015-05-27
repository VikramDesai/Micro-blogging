package RingPackage;

import java.io.Serializable;
import java.util.*;

public class Token implements RingInterface,Serializable{
	
	double id=0;
	int count=0;
	double initiatorId=0;
	Token predecessor;
	Token successor;
	Token coordinator;
	
	public void Token(){
	
		Random rand = new Random();
		id=rand.nextDouble();
	}
	
    public void setId(){
    	Random rand = new Random();
		id=rand.nextDouble();
    }
	
	public double getId(){
		
		return id;
	}
	
	public void updatePredecessor(Token predecessor )
	{
		this.predecessor = predecessor; 
	}
	
	public void updateSuccessor(Token successor){
		this.successor = successor;
	}
	
	public ArrayList<Token> leaderElection(ArrayList<Token> listTokens,int elecId){
	    
	    double initiatorId=0;
	    
	    if(count == 0){
		 initiatorId=this.getId();
	    }
		int elecBeginId = elecId;
		elecId=0;
		listTokens.add(this);
		
		if(this.successor.getId() != initiatorId){
		this.successor.leaderElection(listTokens, elecId);
		count++;
		}
      return listTokens;
	}
	


public void informLeader(Token leader){
	 
	 if(count == 0){
	 initiatorId=this.getId();
	 }
	 
	 this.coordinator = leader;
	 
	if(this.successor.getId() != initiatorId){
	
		this.successor.informLeader(leader);
		count++;
	}
	
}

}

