import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class clientThread extends Thread {
	public Socket socket;
	public String ipadd = "localhost";
	public int port;
	
	public clientThread(int portNumber) {
		port = portNumber;
		
		
	}
	
	public void start() {	
		try {
			socket = new Socket(ipadd, port);
	
			PrintWriter outputStream = new PrintWriter(new DataOutputStream(socket.getOutputStream()));
			Scanner inputStream = new Scanner(new InputStreamReader(socket.getInputStream()));
			
			//String firstLine = "GET /" + path + " HTTP/1.1\r\n";
			//String secLine = "Host: " + host + ":" + port + "\r\n\r\n";
			String firstLine = "GET / HTTP/1.1\r\n";
			String secLine = "Host: :" + port + "\r\n\r\n";
			
			String header = firstLine + secLine;
			//if the url is present in catalog, and "if-modified since" field also 
			byte [] httpHeader = header.getBytes("US-ASCII");
			
			socket.getOutputStream().write(httpHeader);
			socket.getOutputStream().flush();
		}
		
		catch(IOException e) {
			
		}
	}
	
	
	
	
	public static void main(String[] args) {
		//make a new thread with the port number
		int port = 2225;
		(new clientThread(port)).start();
		
	}

}
