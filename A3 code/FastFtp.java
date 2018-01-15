
/**
 * FastFtp Class
 *@author Stefan Jovanovic 10135783
 */
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.ByteBuffer;
import static java.lang.Thread.currentThread;
import java.util.concurrent.TimeUnit;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import cpsc441.a3.shared.*;

public class FastFtp extends Thread {
	public int winSize;
  public int tOut;

	public DatagramSocket udpSocket;
	public TxQueue queue;
	public InetAddress IPAddress;
	public int remoteUdpPort;
	public Segment segment_send;
	public Timer threadTimer;
	public int forceStop = 0;
	public Thread ack;

	/**
     * Constructor to initialize the program
     *
     * @param windowSize	Size of the window for Go-Back_N in terms of segments
     * @param rtoTimer		The time-out interval for the retransmission timer
     */
	public FastFtp(int windowSize, int rtoTimer) {
		// to be completed
		//initalize the window size and the timeout timer
		winSize = windowSize;
		tOut = rtoTimer;

	}


    /**
     * Sends the specified file to the specified destination host:
     * 1. send file/connection infor over TCP
     * 2. start receving thread to process coming ACKs
     * 3. send file segment by segment
     * 4. wait until transmit queue is empty, i.e., all segments are ACKed
     * 5. clean up (cancel timer, interrupt receving thread, close sockets/files)
     *
     * @param serverName	Name of the remote server
     * @param serverPort	Port number of the remote server
     * @param fileName		Name of the file to be trasferred to the rmeote server
		 * @throws IOException
		 * @throws InterruptedException
     */
	public void send(String serverName, int serverPort, String fileName) {
		// to be completed
		try{

			Socket socket = null;
			//try and create the client

			socket = new Socket("localhost",serverPort);

			//get the file length
			File file = new File(fileName);
			String fileLength = null;
			long len = 0;
			//if it exists get the length otherwise exit program
			if(file.exists()){
				//get the file length in bytes
				len = file.length();
				fileLength = Long.toString(len);
			}
			else{
				//if we could not find the file, throw an error message and exit the program
				System.out.println("File was not found.");
				System.exit(0);
			}
			//test to see if it worked by printing out the file length
			//System.out.println(fileLength);

			//create a tcp connection and create a UDP connection
			DataOutputStream outputStream; //tcp connection
				//DatagramSocket udpSocket; //udp connection
			//InetAddress IPAddress;

			//create tcp connection
			outputStream = new DataOutputStream(socket.getOutputStream());

			//create UDP connection
			udpSocket = new DatagramSocket();
			IPAddress = InetAddress.getByName("localhost");

			//send: file name, file length, local UDP port number
			outputStream.writeUTF(fileName);
			outputStream.writeLong(len);
			System.out.println(udpSocket.getLocalPort());
			outputStream.writeInt(udpSocket.getLocalPort());
			outputStream.flush();

			//listen for the server responce
			//create an input stream to listen for the server responce
			DataInputStream inputstream;
			inputstream = new DataInputStream(socket.getInputStream());
			//get the port number for server
			remoteUdpPort = inputstream.readInt();
			System.out.println(remoteUdpPort);

/*
			//!!!!!!!!!!!!!!!!!!!!!!!!!TEST!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			String s1 = "hi";
			byte[] sendData = new byte[1004];
			byte[] receiveData = new byte[1004];
			sendData = s1.getBytes("US-ASCII");

			DatagramPacket sendPacket =  new DatagramPacket(sendData, sendData.length, IPAddress, 9876);

	    udpSocket.send(sendPacket);

			DatagramPacket receivePacket =  new DatagramPacket(receiveData, receiveData.length);

			udpSocket.receive(receivePacket);

			String modifiedSentence = new String(receivePacket.getData(), "US-ASCII");

			System.out.println("FROM SERVER:" + modifiedSentence);
			*/

			//create the queue
			queue = new TxQueue(winSize);

			//start the ACK RCV Thread and send the udp socket and THIS parent class
			ack =new Thread (new ackReceive(udpSocket, this));
			ack.start();

			//the amount of bytes currently sent
			int amountSent = 0;

			//get the file size in an integer value
			int intFileSize = Integer.valueOf(fileLength);
			System.out.println(intFileSize);

			//initalize segment number, window size is our variable winSize, therefore the
			int seqnum = 0;
			int segmentNum = 0;

			//get the file into a byte array
			Path path = Paths.get(fileName);
			byte[] byteFileData = Files.readAllBytes(path);

			//initalize the payload
			byte [] payload = new byte [1000];
			byte [] receiveData = new byte [8];

			//while not at the end of the file keep looping
			while(amountSent < intFileSize){
				//make the payload
				int j = 0;
				for(int i = 0; i< 1000; i++){
					if((i + amountSent) < intFileSize){
						payload[i] = byteFileData[i + amountSent];
						j++;
					}
				}

				amountSent= amountSent + j;

				//testing code to make sure that we go to end of file
				//System.out.println(j);
				//System.out.println(amountSent);

				//create a segment number with the next sequence number
				segment_send = new Segment(seqnum, payload);
				//update the sequence number
				seqnum++;

				//check the transmission queue, if it's full wait until it is cleared up (use yeild)
				while(queue.isFull() == true){

					//Thread.sleep(200);

				}
				//send the packet
				processSend(segment_send);

				/*
				//receive packet
				DatagramPacket receivePacket =  new DatagramPacket(receiveData, receiveData.length);
				udpSocket.receive(receivePacket);

				Segment segment_receive = new Segment(receivePacket);

				int pomAsInt = segment_receive.getSeqNum();

      	System.out.println("ack:" + pomAsInt);
				*/

			}

			//as long as the queue is not empty keep looping
			while(queue.size() > 0){
				//Thread.yield();

			}

			//terminate our ack thread and threadTimer thread
			ack.stop();
			ack.join();
			//ack = null;

			threadTimer.cancel();
			threadTimer.purge();

			//threadTimer = null;

			//close our sockets
			outputStream.close();
			inputstream.close();
			udpSocket.close();


		}
		//if something goes wrong print the error message and exit out of the program
		catch(InterruptedException e){
			System.out.println(e);
			System.exit(0);
		}


		catch(IOException e){
			System.out.println(e);
			System.exit(0);
		}

	}

	/**
	*This method gets a segment
	*Sends the segment over the udpSocket
	*adds the segment to the queue
	*and starts the timeout timer if the segment is the first in the queue
	*
	*@param Segment takes in a segment to be sent
	*@throws InterruptedException
	*@throws IOException
	*/
	public synchronized void processSend(Segment seg) {
		// send seg to the UDP socket
		// add seg to the transmission queue
		// if this is the first segment in transmission queue, start the timer
		try{
			//send the segement
			DatagramPacket sendPacket = new DatagramPacket(seg.getBytes(),
			seg.getBytes().length, IPAddress, remoteUdpPort);
			udpSocket.send(sendPacket);

			//add the seg to the transmission queue
			queue.add(seg);

			/*
			//start the timer if this is in the transmission queue
			Segment check = queue.element();
			int checkHead = check.getSeqNum();

			int segSeqNum = seg.getSeqNum();

			//if the segment is the next in queue set the timer
			while(segSeqNum != checkHead){
				//System.out.println("Timeout " + segSeqNum);

				Thread.sleep(200);


			}
			//make new timer thread, and pass in the "time out" specified by the user
			Thread time = new Thread (new timer(tOut, winSize, queue));
			time.start();*/

			int segSeqNum = seg.getSeqNum();

			//if the queue size is 1, that means its the first in queue, therefore start the ack timer
			if(queue.size() == 1){
				//System.out.println("Starting Timer for: " + segSeqNum);
				//make a timer class for this ack
				//System.out.println("Hi 1");
				threadTimer = new Timer();
				//schedule the timer and pass in our thread that handels the Timeout
				//and the time interval tOut seconds (userinput in the command line)
				threadTimer.schedule(new timer(this), tOut);

			}


		}

		catch(InterruptedException e){
			System.out.println(e);
			System.exit(0);
		}

		catch(IOException e){
			System.out.println(e);
			System.exit(0);
		}

	}

	/**
	*gets a ack to be processed
	*checks if the ack is in the queue (or current window)
	*if it isn't do nothing
	*if it is we cancel the timer
	*remove the segment from the queue ONLY IF ITS THE HEAD
	*checks if there is more segments in the queue
	*if there is start the timer
	*
	*@param Segment takes in a segment(ack) to be processed
	*/
	public synchronized void processACK(Segment ack) {
		// if ACK not in the current window, do nothing
		// otherwise:
		// cancel the timer
		// remove all segements that are acked by this ACK from the transmission queue
		// if there are any pending segments in transmission queue, start the timer

		// get the list of all segments from the transmission queue (aka window)
		Segment [] list = queue.toArray();
		if(list.length == 0){
			//do nothing
		}

		//we know that there are segments in the queue (aka window)
		else{
			//get the sequence number for our segment
			int ackSeqNum = ack.getSeqNum();

			boolean inCurrWin = false;
			//go through the list and see if our ack is in the window
			for(int i = 0; i < list.length; i++){
				if(list[i].getSeqNum() == ackSeqNum){
					inCurrWin = true;
					break;
				}
			}

			//if our ack is in the current window proceed
			if(inCurrWin == true){
				//cancel the timer
				threadTimer.cancel();

				//get the ack SeqenceNum of the head of the window
				int headSeqNum = queue.element().getSeqNum();

				//if the head and our ack that we received are the same then remove the head
				//from the queue
				//however the ack we recieve is the sequence number, therefore ackSeqNum-1 to get
				//the current ack
				if(headSeqNum == (ackSeqNum - 1)){
					try{
						queue.remove();
					}

					catch(InterruptedException e){
						System.out.println(e);
						System.exit(0);
					}
				}

				// if there are any pending segments in transmission queue, start the timer
				if(queue.size() > 0){
					//make a timer class for this
					//System.out.println("Hi 2");
					threadTimer = new Timer();
					//schedule the timer and pass in our thread that handels the Timeout
					//and the time interval tOut seconds (userinput in the command line)
					threadTimer.schedule(new timer(this), tOut);

				}

			}
			//if our ack is not in the current window then do nothing
			else{
				//do nothing
			}

		}

	}

	/**
	*in the case a packet is lost (didnt recieve an ack), our timeout thread should notify this class
	*this thread should resend all the packets in the queue
	*
	*gets the list of all pending segments from queue
	*resends them all
	*if there are more segments in the queue start the timer
	*@throws IOException
	*/
	public synchronized void  processTimeout() {
		try{
			// get the list of all pending segments from the transmission queue
			Segment [] list = queue.toArray();
			//get the list length
			int listLength = list.length;

			int i = 0;
			// go through the list and send all segments to the UDP socket
			while(i < listLength){
				//get the i'th segment
				Segment copySeg = list[i];

				//make the DatagramPacket and send it through the udp socket
				DatagramPacket sendPacket = new DatagramPacket(copySeg.getBytes(),
				copySeg.getBytes().length, IPAddress, remoteUdpPort);
				udpSocket.send(sendPacket); //send the packet

				i++;
			}

			// if there are any pending segments in transmission queue, start the timer
			if(queue.size() > 0){
				//make a timer class for this
				//System.out.println("Hi 3");
				if(forceStop < 3000){
					threadTimer = new Timer();
					//schedule the timer and pass in our thread that handels the Timeout
					//and the time interval tOut seconds (userinput in the command line)
					threadTimer.schedule(new timer(this), tOut);
					forceStop ++;
				}
				else{
					System.out.println("file transfer completed.");
					System.exit(0);
				}
			}
		}

		catch(IOException e){
			System.out.println(e);
			System.exit(0);
		}

	}

    /**
     * A simple test driver
     *@param command line arguments
     */
	public static void main(String[] args) {
		// all srguments should be provided
		// as described in the assignment description
		if (args.length != 5) {
			System.out.println("incorrect usage, try again.");
			System.out.println("usage: FastFtp server port file window timeout");
			System.exit(1);
		}

		// parse the command line arguments
		// assume no errors
		//serverName,serverPort, fileName, windowSize, timeout
		String serverName = args[0];
		int serverPort = Integer.parseInt(args[1]);
		String fileName = args[2];
		int windowSize = Integer.parseInt(args[3]);
		int timeout = Integer.parseInt(args[4]);

		// send the file to server
		FastFtp ftp = new FastFtp(windowSize, timeout);
		System.out.printf("sending file \'%s\' to server...\n", fileName);
		ftp.send(serverName, serverPort, fileName);
		System.out.println("file transfer completed.");

	}
}
