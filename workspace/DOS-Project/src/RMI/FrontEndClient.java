package RMI;

import java.io.*;
import java.util.*;


//The FrontEndClient implements the work thread.
public class FrontEndClient implements Runnable {

	Thread newThread;

	public FrontEndClient() {

		newThread = new Thread(this, "Worker Thread");
		newThread.start();
	}

	//Implements the option menu for each transaction.
	public void run() {

		Boolean flag = true;
		RmiClient feClient = new RmiClient();

		String username = "";
		int operation = 0;
		int topicId = 0;
		String topic = null;
		String comment = "";
		int timestamp = 0;

	
			System.out.print("Enter the username: ");
			BufferedReader read1 = new BufferedReader(new InputStreamReader(
					System.in));
			try {
				username = read1.readLine();
			} catch (IOException ioe) {
				System.out.println("Error reading user name.");
			}
			System.out.println("User  is " + username);
             
			while (flag) {
			System.out
					.print("Enter the operations you wish to perform :\n 1:Post \n 2:Subscribe to a Topic \n 3:Unsubscribe to a Topic \n 4.Retrive Comments \n 5.Exit \n ");
			BufferedReader read2 = new BufferedReader(new InputStreamReader(
					System.in));
			try {
				operation = Integer.parseInt(read2.readLine());
			} catch (IOException ioe) {
				System.out.println("Error reading user name.");
			}
            if(operation == 4){

            	System.out.println("Enter the integer time-Stamp you'd like to retrieve posts from: ex:2 or 5");
            	BufferedReader read6 = new BufferedReader(new InputStreamReader(System.in));
            	try{
            		timestamp = Integer.parseInt(read6.readLine());
            	}catch (IOException ioe) {
    				System.out.println("Error reading the time Stamp.");
    				
            	
            }
			if (operation == 5) {

				System.out.println("Exiting");

			}

			
				
			 if (operation >= 1 && operation <= 3) {
				System.out
						.print("Enter topic number(1,2 or 3): \n 1.Surviving Project 1 \n 2.Swine flu rumors \n 3.Fall foliage ");
				BufferedReader read4 = new BufferedReader(
						new InputStreamReader(System.in));
				try {
					topicId = Integer.parseInt(read4.readLine());
				} catch (IOException ioe) {
					System.out.println("Error reading topic.");
				}
				System.out.println("Topic id is " + topicId);
				if (operation == 1) {
					System.out.print("Enter your comment: ");
					BufferedReader read5 = new BufferedReader(
							new InputStreamReader(System.in));
					try {
						comment = read5.readLine();

					} catch (IOException ioe) {
						System.out.println("Error reading comment.");
					}

				}
			}

			switch (operation) {
			case 1:

				if (topicId == 1)

				{
					topic = "#proj1";
				} else if (topicId == 2) {
					topic = "#swine";
				} else {
					topic = "#foliage";
				}
				feClient.postMsg(username, topic, comment);
				break;
			case 2:
				if (topicId == 1)

				{
					topic = "#proj1";
				} else if (topicId == 2) {
					topic = "#swine";
				} else {
					topic = "#foliage";
				}
				feClient.follow(username, topic);
				break;
			case 3:

				if (topicId == 1)

				{
					topic = "#proj1";
				} else if (topicId == 2) {
					topic = "#swine";
				} else {
					topic = "#foliage";
				}
				feClient.unsubscribe(username, topic);
				break;

			case 4:
              
				feClient.retrieve(username,timestamp);

				break;

			case 5:

				flag = false;
				System.exit(0);
			}
		}

	}

}
}
