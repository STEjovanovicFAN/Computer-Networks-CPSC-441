
/**
 * Router Class
 *@author Stefan Jovanovic 10135783
 */
import java.util.*;
import java.net.*;
import java.io.*;
import cpsc441.a4.shared.*;


/**
 * Router Class
 *
 * This class implements the functionality of a router
 * when running the distance vector routing algorithm.
 *
 * The operation of the router is as follows:
 * 1. send/receive HELLO message
 * 2. while (!QUIT)
 *      receive ROUTE messages
 *      update mincost/nexthop/etc
 * 3. Cleanup and return
 *
 *
 * @author 	Stefan Jovanovic id: 10135783
 *
 */
public class Router {
	private int routerId1;
	private String serverName1;
	private int serverPort1;
	private int updateInterval1;
	private int[] minCostVec = null;
	private int[] linkCostVec = null;
	private int[][] minCostTable = null;
	private long timeOut = 0;
	private Timer threadTimer;
	private ObjectOutputStream outputStream = null; //outputstream to how we write to the main relay server
	private int count = 1; //used for counting
	private int[] nexthop = null;	// next hop vector
	private volatile boolean stop = false;

    /**
     * Constructor to initialize the rouer instance
     *
     * @param routerId			Unique ID of the router starting at 0
     * @param serverName		Name of the host running the network server
     * @param serverPort		TCP port number of the network server
     * @param updateInterval	Time interval for sending routing updates to neighboring routers (in milli-seconds)
     */
	public Router(int routerId, String serverName, int serverPort, int updateInterval) {
		// to be completed
		//initalize the router instance with the passed in values
		routerId1 = routerId;
		serverName1 = serverName;
		serverPort1 = serverPort;
		updateInterval1 = updateInterval;

		//create a thread timer
		threadTimer = new Timer(); //create a timer
	}


    /**
     * starts the router
     * opens a tcp connection with the main relay server
		 * processes the hello packet
		 * starts the timer timeout task
		 * while we dont get a quit packet from the main relay server continue in an infinte loop
		 * 		wait for a recieve packet
		 *    process the packet
		 * close everything and return forwarding table of the router
		 * @throws IOException
		 * @throws ClassNotFoundException
     * @return The forwarding table of the router
     */
	public RtnTable start() {
		// to be completed

		//1. open a TCP connection to the server ✓
		//2. send/receive/process HELLO ✓
		//3. start timer ✓
		//4. while not QUIT packet do
		//		dvr = receive a DvrPacket
		//		processDvr(dvr)
		//	end while
		//5. cancel timer, close socket, clean up ✓
		//6. return rounting table

		//first we need to open a TCP connection using the server name and port
		Socket routerSocket = null;
		DvrPacket dvrHELLO;
		DvrPacket dvrREC;
		ObjectInputStream inputStream = null; //input stream to recieve packets from the main relay server


		try{
			//initalize the client socket
			routerSocket = new Socket(serverName1, serverPort1);
			//initalize the outputStream to send things to the server
			outputStream = new ObjectOutputStream(routerSocket.getOutputStream());

			//send a packet to the main relay server initalize our router
			//send THIS router's id, the destination which is the server, and the type(Hello)
			dvrHELLO = new DvrPacket(routerId1, DvrPacket.SERVER, DvrPacket.HELLO);

			//next we need to send this packet over to the main relay server
			outputStream.writeObject(dvrHELLO);
			outputStream.flush();

			//create a inputstream to read an expected packet from the main relay
			inputStream = new ObjectInputStream(routerSocket.getInputStream());

			//get the reponse in a packet
			dvrREC = (DvrPacket) inputStream.readObject();

			//print that a packet was received and print the string representation of the packet
			System.out.println("received: " + dvrREC.toString());

			//record the link cost vector and athe minimum cost vector
			linkCostVec = dvrREC.getMinCost();
			minCostVec = dvrREC.getMinCost();

			//initialize the hop table
			nexthop = new int[minCostVec.length];

			//initalize the variables to get to the x router
			for(int i = 0; i < linkCostVec.length; i++){
				//if the cost to get to the router is 999 (undefined), then the next hop is 999 (undefined)
				if (minCostVec[i] == 999) {
					nexthop[i] = 999;
				}
				//otherwise if there is a cost to the next router initalize the router to get to it
				else{
					nexthop[i] = i;
				}
				//test message
				//System.out.println(nexthop[i]);

			}

			/*
						minCostTable

						cost to get from side router to next router

						 example for router id 0:

						 -use the dvr packet we got from server to initalize this routers physical link cost
						 -to get from router 0-0 its 0, to get from router 0-1 its Unknown, .....and so on


															   router
													0				1				2				3
										0			0			 999		 10		    3
					routers		1
										2
										3

						 -because we dont know each routers link cost we fill the rest of the blank spaces with 999

						 NOTE:THE SIZE OF THE minCostTable IS ALWAYS [X][X]

			*/

			//next we need to initalize the minCostTable for the routers
			minCostTable = new int [minCostVec.length][minCostVec.length];

			//now we need to pass in this routers minCostVec into the alocated slot
			//get the routers id, numRouters has the amount of routers in the System
			int id = routerId1;
			int numRouters = minCostVec.length;

			//go in a for loop and record the minCostVec in the approrieate id allocation in the minCostTable
			for(int i = 0; i < numRouters; i++){
				for(int j = 0; j < numRouters; j++){
					//if we are in THIS routers id's row put in its x minCostVec here
					if(id == i){
						minCostTable[i][j] = minCostVec[j];
						//testing line
						//System.out.println("recording row: " + i + ", and column: " + j + " with " + minCostVec[j]);

					}
					//else we are in another routers id row and don't know what the cost is, write it as 999
					else{
						minCostTable[i][j] = 999;

					}

				}

			}


			//find out how many routers there are in

			/*
			//Test code for debugging purposes
			for(int i = 0; i < minCost.length; i++){
				System.out.println(minCost[i]);
			}
			*/

			//convert updateInterval1 to long
			timeOut = Long.valueOf(updateInterval1);
			//schedule the timer and pass in our thread that handels the Timeout
			threadTimer.schedule(new timer(this), timeOut);

			//System.out.println(dvrREC.type);

			//infinte loop untill we recieve a quit type packet
			while(dvrREC.type != 2){
				//wait to receive a packet
				dvrREC = (DvrPacket) inputStream.readObject();

				//when we finally get a packet process it
				processDvr(dvrREC);

				//continue to listen for packets
			}

		}

		catch(ClassNotFoundException e){
			System.out.println(e);
			System.exit(0);
		}

		catch(IOException e){
			System.out.println(e);
			System.exit(0);
		}


		//if our code gets to here we know we received a quit packet; close all sockets, cancel the timer and Cleanup
		stop = true;
		threadTimer.cancel();
		threadTimer.purge();
		try{
			routerSocket.close();
			outputStream.close();
			inputStream.close();
		}

		catch(IOException e)
		{
			System.out.println(e);
			System.exit(0);
		}

		//test
		//System.out.println("Size of minCostVec: " + minCostVec.length);
		//System.out.println("Size of nexthop: " + nexthop.length);

		//return the routing table when we get the quit message from the relay server
		return new RtnTable(minCostVec, nexthop);
	}


	/**
	* when the timout occurs this method will handel the timeout
	* resend to all routers THIS routers minCostVec
	* print out a message that it has sent with minCostVec values
	* create a new timer
	*@throws IOException
	*/
	public synchronized void processTimeout() {
		//this thread is used for updating all the other routers with it's link cost vector

		//create new packet
		DvrPacket sendDVR;

		//get the size of how many routers are in the system
		int numRouters = linkCostVec.length;

		try{
			//go through the list and update the rest of the routers with this routers linkCostVec
			//Note: do not send to the same router
			for(int i = 0; i < numRouters; i++){
				if(i != routerId1){
					sendDVR = new DvrPacket(routerId1, i, DvrPacket.ROUTE, minCostVec);
					//send this packet to the main relay server and to the destination
					outputStream.writeObject(sendDVR);

				}
			}

			//send all the packets
			outputStream.flush();

			//make an update message
			String output = "[" + count + "] updating neighbors with DV = [";

			//Test code for debugging purposes
			for(int i = 0; i < minCostVec.length; i++){
				if(i == 0)
					output = output + minCostVec[i] + ", ";
				else if(i == 1)
					output = output + minCostVec[i];
				else
					output = output + ", " + minCostVec[i];
			}
			output = output + "]";

			//print update message
			if(stop == false)
				System.out.println(output);

			count ++;

			//stop creating a new thread of threadTimer if we receive a quit packet
			if(stop == false){
				threadTimer = new Timer();
				threadTimer.schedule(new timer(this), timeOut);
			}
			//else if stop is not false stop making threads
			else{
				System.exit(0);
			}
		}

		catch(IOException e){
			System.out.println(e);
			System.exit(0);
		}

	}


	/*
	*	2d array for the min cost vector
	*
	*first row is the smallest cost to get to x router
	*
	*	the other 3 rows are the smallest for each router to get to x router
	*
	*	what you want to do is update the first row cost using the rest of the rows to find the smallest cost
	*	test every single combination and find the smallest cost, if smallest cost update first row if not dont update the first row
	*/

	/*
	*  if dvr.sourceid == DvrPacket.SERVER
	*     this is a link cost change message
	*     update link cost vector         //link cost vector is the local cost of going to x amount of routers
	*     update min cost vector					//min cost vector is the algorithm used to find the min cost of going to x router
	*  else
	*     this is a regular routung update from a neighbor
	*     update min cost vector
	*
	* take care of the timer
	*
	* @param DvrPacket
	*/
  public void processDvr(DvrPacket dvr) {

		//if we get a packet cancel the timer
		threadTimer.purge();
		//if the packet came from the relay server, then this is just a link cost change message
		if(dvr.sourceid == DvrPacket.SERVER){
			//print a message to signify that there is a link cost change
			System.out.println("link cost changed: " + dvr.toString());

			//update the link cost vector
			linkCostVec = dvr.getMinCost();

			int numRouters = linkCostVec.length;

			//test
			//System.out.println("linkCostVec: " + linkCostVec.length);

			/*

			 So when we call update in the server:
			     	- Server sends an update message to every router
						- The way we know it is from the server is if the packet we recieved has an id of 100 (100 is the server id)

			 When we get the update packet get the update for the physical links FOR THIS ROUTER
			 So in other words it only changes the physical link values THIS ROUTER HAS

			 example router id 0:
			  		R0 -> R2 cost is: 10
						UPDATE happens
						R0 -> R2 cost is: 1

			 if the updated value < current value to get to X router
			 			- change the minCostVec to the updated value
						- change our minCostTable to the updated value
						- update our next hop to where we changed the value

			*/
			int rI;
			int currI;

			//check and see if the updated link cost takes less then the current cost
			for(int i = 0; i < numRouters; i++){
				//get the physical link cost to router X
				rI = linkCostVec[i];
				//get the current link cost to router X
				currI = minCostVec[i];

				//if the updated cost link is less than the current cost
				if(rI < currI){
					//use the updated cost
					//update minCostVec with the updated cost
					minCostVec[i] = rI;
					//update minCostTable with the updated cost
					minCostTable[routerId1][i] = rI;
					//update the nexthop with the updated router
					nexthop[i] = i;

				}

			}

		}

		//if the packet did not come from the relay server, it is then a regular update from a neighbour
		else{
			//update the min cost vector
			/*
					example id 0:

																 router
													0				1				2				3
										0			0			 999		 10		    3
 					routers		1    999     999     999     999
										2    999     999     999     999
										3    999     999     999     999

						 - So initally we know that the table looks like this
						 - we only have the row for THIS routers id, so in this case the first row

						 - when we get to this part of the code we received an update packet from another neightbour
						 - this update looks like this

						 example id 3:
						 	[3, 1, 1, 0]

						 - Since we have undefined for all of row id 3, update the values of row 3
						 - So we get

																	 router
														0				1				2				3
											0			0			 999		 10		    3
						routers		1    999     999     999     999
											2    999     999     999     999
											3     3       1       1       0


							- Since we now know router id 3 minCostVec we can now make an algorithm to update router id 0
							  				using router id 3 cost

							- Take the updated id row
							- algorithm to calculate the total cost [cost to get to router x] + [cost to get to router y through x]

							example:
							   check if cost from 3 to 1 is better then current cost 1

							   		[cost to get to router 3] + [cost from router 3 to router 1] = 3 + 1 = 4
								    question: is 4 less than 999?
										yes
										replace minCostVec of router id 0 in minCostTable[0][1] to 4
										update the nexthop to be 3 if it is replaced

								 check if the cost from 3 to 2 is better then current cost to 2

										[cost to get to router 3] + [cost from router 3 to router 2] = 3 + 1 = 4
										question: is 4 less than 10?
										yes
										replace minCostVec of router id 0 in minCostTable[0][2] to 4
										update the nexthop to be 3 if it is replaced


							- Don't need to check for 3 to 3 because it will be the same cost from 0 to 3
							- Same is applied from 3 to 0 becasue
							- But we can do it anyway it will not affect the outcome

							FINALIZATION: update the minCostVec to send an update to the rest of the routers
			*/


			//get the id of the packet and the minCostVec of the packet
			int packetID = dvr.sourceid;
			int[] packetMinCostVec = dvr.mincost;
			//get the # of total routers in the system
			int numRouters = minCostVec.length;

			//go into our table and replace the id packets row with the updated values
			for(int i = 0; i < numRouters; i++){
				minCostTable[packetID][i] = packetMinCostVec[i];
			}

			//rows are now updated for the packet id
			//next we need to check if getting to the destination is faster going through that router

			//calculate cost to get to router x
			int costToX = minCostTable[routerId1][packetID];
			int costToY;
			int currCost;
			//[cost to get to router x] + [cost to get to router y through x]
			for(int i = 0; i < numRouters; i++){
				//calculate the cost to get to Y through X
				costToY = costToX + minCostTable[packetID][i];
				//get the current cost to get to Y
				currCost = minCostTable[routerId1][i];

				//if the cost to go from X to Y is smaller then the current cost to X, replace the currCost with costToY
				if(costToY < currCost){
					//update the minCostVec and the minCostTable with the new value
					minCostVec[i] = costToY;
					//System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
					//System.out.println(minCostVec.length);
					minCostTable[routerId1][i] = costToY;
					//update the nexthop
					nexthop[i] = packetID;

					//test message
					//System.out.println("UPDATED: " + minCostVec[0] +" "+ minCostVec[1] +" " + minCostVec[2] +" " + minCostVec[3] +" ");

				}

			}

		}

		//start the timer again
		threadTimer = new Timer();
		threadTimer.schedule(new timer(this), timeOut);

  }

    /**
     * A simple test driver
     *
		 @param command line arguments
     */
	public static void main(String[] args) {
		// default parameters
		int routerId = 0;
		String serverName = "localhost";
		int serverPort = 2227;
		int updateInterval = 1000; //milli-seconds

		if (args.length == 4) {
			routerId = Integer.parseInt(args[0]);
			serverName = args[1];
			serverPort = Integer.parseInt(args[2]);
			updateInterval = Integer.parseInt(args[3]);
		} else {
			System.out.println("incorrect usage, try again.");
			System.exit(0);
		}

		// print the parameters
		System.out.printf("starting Router #%d with parameters:\n", routerId);
		System.out.printf("Relay server host name: %s\n", serverName);
		System.out.printf("Relay server port number: %d\n", serverPort);
		System.out.printf("Routing update intwerval: %d (milli-seconds)\n", updateInterval);

		// start the router
		// the start() method blocks until the router receives a QUIT message
		Router router = new Router(routerId, serverName, serverPort, updateInterval);
		RtnTable rtn = router.start();
		System.out.println("Router terminated normally");

		// print the computed routing table
		System.out.println();
		System.out.println("Routing Table at Router #" + routerId);
		System.out.print(rtn.toString());
	}

}
