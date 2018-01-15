

/**
 * WebServer Class
 * 
 */

import java.util.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;


/**
 * @author stefan.jovanovic
 * ID: 10135783
 *
 */
public class WebServer extends Thread {

	//server socket (is a single thread) that listens for clients
	//client socket is the socket that our server socket found 
	public ServerSocket serverSocket;
	public Socket clientSocket = null;
	private volatile boolean running = true;
	public int serverPort = 0;
	
	//this is our exit boolean 
	private volatile boolean exit;

	public String [] httpRequest = new String[20];
	
	
    /**
     * Default constructor to initialize the web server
     * 
     * @param port 	The server port at which the web server listens > 1024
     * 
     */
	public WebServer(int port) {
		//when we make a thread this is the part that initializes the web server Socket
		serverPort = port;
		
		//Initialize our exit boolean to false to keep running the server in listening mode 
		exit = false;
	}
	
	
    /**
     * The main loop of the web server
     *   Opens a server socket at the specified server port
	 *   Remains in listening mode until shutdown signal
	 *   When in listening mode listens for client http requests
	 * 	 Timesout in 1 sec when it cannot find a client http request
	 * 	 If it does find a client http request, makes a new worker thread and lets worker thread resolve the http request  
	 * 
	 * @throws SocketTimeoutException if we the server socket times out 
	 * @throws IOException if something goes wrong with our code
     */
	public void run() {
		//set up the server and timeout connection
		try {
			//open the server socket 
			serverSocket = new ServerSocket(serverPort);
			//set the timeout connection for the server 
			serverSocket.setSoTimeout(1000);
		
		} 
		catch (IOException e) {
			//print the error if anythign went wrong 
			e.printStackTrace();
			
		}
		
		
		//Algoritum for our while loop
		/*while not stopped do
			2: Listen for connection requests
			3: Accept a new connection request from a client
			4: Spawn a new worker thread to handle the new connection
			5: end while
		*/
		
		//while our exit != true listen for any connection requests
		while(!exit) {
			try {
				//listen for a new connection, if a connection is found get the client socket and store it 
				//if it doesn't find a client socket then it throws an exception and DOES NOT MAKE A NEW THREAD
				clientSocket = serverSocket.accept();

				//create a new worker thread to handle the new connection, pass through the client socket 
				//is not called if serverSocket.accept times out 
				Thread x = new Thread(new WorkerThread(clientSocket));
				x.run();
			}
			catch(SocketTimeoutException e) {
				//do nothing if our serverSocket timesout
				//allows the process to check the shutdown flag
				
				//System.out.println("Server thread timed out");
				
			} catch (IOException e) {
				//if an exception occurs catch it 
				e.printStackTrace();
			}
			
			
		}
		
		//close the server socket
		try {
			serverSocket.close();
		}
		catch (IOException e) {
			//if an exception occurs catch it 
			e.printStackTrace();
		}
	}


	/**
     * Signals the server to shutdown.
	 *
     */
	public void shutdown() {
		//if this method is called, we need to tell the server to shut down
		//to do this we will need to change our boolean "exit" to true
		exit = true;
		
		
		//System.out.println("exit variable: " + exit);
		
	}

	
	/**
	 * A simple driver that creates a main webserver thread 
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		int serverPort = 2225;

		// parse command line args
		if (args.length == 1) {
			serverPort = Integer.parseInt(args[0]);
		}
		
		if (args.length >= 2) {
			System.out.println("wrong number of arguments");
			System.out.println("usage: WebServer <port>");
			System.exit(0);
		}
		
		System.out.println("starting the server on port " + serverPort);
		
		WebServer server = new WebServer(serverPort);
		
		//start the WebServer thread 
		server.start();
		System.out.println("server started. Type \"quit\" to stop");
		System.out.println(".....................................");

		//scanner takes in user input  
		Scanner keyboard = new Scanner(System.in);
		//keep taking in user input, unless user inputs "quit"
		while ( !keyboard.next().equals("quit") );
		
		//when we input quit we need to shutdown the server 
		server.shutdown();
		
		//if we get here then the user inputed quit, so we let the user know that we are shutting down the server 
		System.out.println();
		System.out.println("shutting down the server...");
		server.shutdown();
		System.out.println("server stopped");
	}
	
}
