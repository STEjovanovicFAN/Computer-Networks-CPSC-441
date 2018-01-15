
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

/**
 * @author stefan.jovanovic
 * @ID 10135783
 */
public class WorkerThread extends Thread{
	protected Socket cSocket = null;
	public String [] httpRequest = new String[20];
    protected String serverText   = "server 2225";

	/**
	 * Constructor of the thread
	 * initalizes the socket we need to write to, we get this from the main webserver thread
	 * 
	 * @param client socket
	 */
	public WorkerThread(Socket clientSocket){
		//initalize the cSocket of this class
		cSocket = clientSocket;
		
		//System.out.println("New thread was made");
		
	}
	
	/**
	 * main function of the worker thread 
	 * gets the http request of the client and parses the header request
	 * 
	 * makes sure that the get request is valid, if not we send http 400 error bad request with date, server, and connection
	 * makes sure that the client requests some object we have, if not we send a http 404 not found request with date, server and connection
	 * if the two above are vaild(ie.get and object), we send a http 200 OK header with all the specified criteria in the header, and then send the actual object in a byte stream
	 *  
	 * if any one of those 3 above are done, then the thread closes the socket and calls "return;" to close the thread   
	 * 
	 * @throws IOException if something in scanner, output stream,..,etc goes wrong 
	 * 
	 * @return just calls "return;", this is used to terminate the thread. Of course when we terminate the main thread all the rest of the threads bound to our main thread are also terminated
	 */
	public void run(){
		/*
		1: Parse the HTTP request
		2: Ensure well-formed request (return error otherwise)
		3: Determine if requested object exists (return error otherwise)
		4: Transmit the content of the object over the existing connection
		5: Close the connection
		*/
	
		//make an input stream and an output stream
		//input stream is what we get from the client
		//output stream is what we send to the client 
	
		OutputStream os; 
		Scanner inputStream;
		PrintWriter outputStream;
		String currentLine;

		try {
			//initalize the input and output stream with the client socket
			outputStream = new PrintWriter(new DataOutputStream(cSocket.getOutputStream()));
			inputStream = new Scanner(new InputStreamReader(cSocket.getInputStream()));
			
			
			//our counter that keeps track of the http request 
			int i = 0;
			//use the input stream to get the information that the client has passed 
			currentLine = inputStream.nextLine();
			//store the request header into our array
			httpRequest[i] = currentLine;
			i++;
			System.out.println(currentLine);
			
			currentLine = inputStream.nextLine();
			httpRequest[i] = currentLine;
			//System.out.println(currentLine);
			
			/*
			while(currentLine != null) {
				System.out.println(currentLine);
				currentLine = inputStream.nextLine();
				System.out.println("xxx");
				httpRequest[i] = currentLine;
				i++;
				System.out.println("end");
			}*/
			//System.out.println("Hi");
			
			
		}
		
		catch(IOException e) {
			//print error
			e.printStackTrace();
			
		}
		
		//System.out.println("bye");
		//so the lines we need to check are the first 2, to make sure the header request is correct
		String [] firstH = httpRequest[0].split("/", 2);
		String get1 = firstH[0] + "/";
		
		String [] firstH2 = firstH[1].split(" ", 2);
		
		//get the object.file that the http request wants
		String objFile = firstH2[0];
		String get2 = firstH2[1];
		
		//concatinate the get header
		get1 = get1 + " " + get2;
		
		//see if the concatination does not equal "GET / HTTP/1.1"
		if(!get1.equals("GET / HTTP/1.1")) {
			//see if the conection does not equal "GET / HTTP/1.0"
			if(!get1.equals("GET / HTTP/1.0")) {
				//if it doesnt then stop the thread and return a 400 bad request 

				try {
				    DataOutputStream outS = new DataOutputStream(cSocket.getOutputStream());
				    
				    //get the current date 
					DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					Date date = new Date();
					String curDate = dateFormat.format(date); // example: 2016/11/16 12:08:43
				    
				    //make the header string 
					String header = "HTTP/1.1 400 Bad Request\r\n" +
									"Date: " + curDate + "\r\n" +
									"Server: WebServer\r\n" +
									"Connection : close\r\n" +
									"\n"; //add a \n at the end of the header to denote the end of the header
					
					
					//convert the header string into an array of bytes 
					byte[] headerArray = header.getBytes();
					//write the header
					outS.write(headerArray);
					
					 //send the http responce
				    outS.flush();
				    //close
				    outS.close();
				    cSocket.close();
				    
				} catch (IOException e) {
					//catch if exception is thrown 
					e.printStackTrace();
				}	
				
				System.out.println("GET does not equal what we want");	
				//currentThread().stop();
				return;	
				
			}
					
		}
		
		//String check = httpRequest[1];
		//check if the header host is a valid 
		/*
		if(!check.equals("Host: localhost:2225")) {
			
			//if it doesnt then stop the thread and return a 400 bad request 

			try {
				os = cSocket.getOutputStream();
			    OutputStreamWriter osw = new OutputStreamWriter(os);
			    BufferedWriter bw = new BufferedWriter(osw);
			    bw.write("HTTP/1.1 400 Bad Request\n\n");
			    
			    bw.flush();
			    
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			System.out.println("GET does not equal what we want");	
			currentThread().stop();
			return;	
	
		}*/
		
		
		//System.out.println(objFile);
		
		//check if the object exists
		File file = new File(objFile);
		
		if(file.exists()) {
			System.out.println("Found this file");
			//if it does then pass a 200 OK request and the object it wants to request 
			// object name is in variable String objFile
			try {
				os = cSocket.getOutputStream();
				DataOutputStream outS = new DataOutputStream(cSocket.getOutputStream());
			    //OutputStreamWriter osw = new OutputStreamWriter(os);
			    //BufferedWriter bw = new BufferedWriter(osw);
			    //bw.write(status(404).entity().type( getAcceptType()).build(););
				
				//get the current date 
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				Date date = new Date();
				String curDate = dateFormat.format(date); //2016/11/16 12:08:43
				
				
				//get the last time the file was modified 
				file.lastModified();
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
				String formatted = sdf.format(file.lastModified());

				//get the file content length 
				double fileSizeBytes = file.length();
				
				//get path of the file 
				Path path1 = Paths.get(objFile);
				String fileType = Files.probeContentType(path1);
				
				//make the header string 
				String header = "HTTP/1.1 200 OK\r\n" +
								"Date: " + curDate + "\r\n" +
								"Server: WebServer\r\n" +
								"Last Modified: " + formatted +"\r\n" +
								"Content-Length: " + fileSizeBytes + "\r\n"+
								"Content-Type: " + fileType + "\r\n" +
								"Connection : close\r\n" +
								"\n"; //add a \n at the end of the header to denote the end of the header
				
				
				//convert the header string into an array of bytes 
				byte[] headerArray = header.getBytes();
				//write the header
				outS.write(headerArray);
			    
				//get path of the file
			    Path path = Paths.get(objFile);
			    //put the file into an array of bytes
			    byte [] data = Files.readAllBytes(path);
			    //write the byte array
			    outS.write(data);
			    
			    //send the http responce
			    outS.flush();
			    //close
			    outS.close();
			    
			} catch (IOException e) {
				e.printStackTrace();
			}	
			
			
		}
		
		//else if the onjFile does not exist then throw an 404 error
		else {
			System.out.println("did not find the file");
			try {
				 	DataOutputStream outS = new DataOutputStream(cSocket.getOutputStream());
				    
				    //get the current date 
					DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					Date date = new Date();
					String curDate = dateFormat.format(date); // example: 2016/11/16 12:08:43
				    
				    //make the header string 
					String header = "HTTP/1.1 404 Not Found\r\n" +
									"Date: " + curDate + "\r\n" +
									"Server: WebServer\r\n" +
									"Connection : close\r\n" +
									"\n"; //add a \n at the end of the header to denote the end of the header
					
					
					//convert the header string into an array of bytes 
					byte[] headerArray = header.getBytes();
					//write the header
					outS.write(headerArray);
					
					 //send the http responce
				    outS.flush();
				    //close
				    outS.close();
			    
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		
		//if it does then pass a 200 OK request and the object it wants to request 
		// object name is in variable String textFile
		

		System.out.println("end");
		//close the thread if it is finished
		try {
			//close the connection
			cSocket.close();
			//Thread.currentThread().interrupt();
            return;
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
		
	
}
