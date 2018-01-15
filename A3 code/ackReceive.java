/**
 * ackReceive Class
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
import java.lang.Thread;

import cpsc441.a3.shared.*;

public class ackReceive implements Runnable{

  //public variables
  private volatile boolean stop;
  public DatagramSocket udpSocket;
  public TxQueue queue;
  public FastFtp mainFTP;

  /**
   *this constructor just initalizes variables
   *
   *@param DatagramSocket the udp socket
   *@param FastFtp the parent that calls this thread
   */
  public ackReceive(DatagramSocket udp, FastFtp ftp){
    udpSocket = udp;
    mainFTP = ftp;

  }
  /**
   * ackReceive run method
   * it just runs in an infinte while loop listening for ACKs from server
   *
   *@throws IOException
   */
  public void run(){


    //  System.out.println("Starting ackReceive Thread:");
      byte [] receiveData = new byte [8];

      stop = true;
      //keep looping until the calling code terminates this thread
      //int wantedACK = 1;
      int forceStop = 0;
      while(stop){
        //wait for a bit before recieving
        //This.Thread.yeild();

        //if queue is empty wait and keep looping
      /*  if(queue.isEmpty()){
          //Thread.sleep(200);
          forceStop ++;
          if(forceStop == 10000){
            System.out.println("hi 4");
            stop = false;
          }
        }*/

        //  stop = false;

      //  }

        //get the head of the queue sequence number
      //  Segment head = queue.element();
        //int headSeq = head.getSeqNum() + 1;
        try{
          DatagramPacket receivePacket =  new DatagramPacket(receiveData, receiveData.length);
          udpSocket.receive(receivePacket); //get the ack through our udp socket

          //convert the DatagramPacket data into a segment
          Segment segment_receive = new Segment(receivePacket);

          //now we have the segment, next we need to process it
          //so call processACK in our main class
          mainFTP.processACK(segment_receive);
          //this should handle the processing of this ack

        }

        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //Note: we process the ack in main, method:processACK NOT HERE!!
        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!


        //get the ack from the sequence number
      //  int getAck = segment_receive.getSeqNum();
        //Thread.sleep(100);
      //  System.out.println(headSeq +" and " + getAck);


        //while loop that checks if the ack we received is the first in the queue
        //while(headSeq != getAck){
          //try an receive an ack
        //  receivePacket =  new DatagramPacket(receiveData, receiveData.length);
          //udpSocket.receive(receivePacket); //get the ack through our udp socket

          //convert the DatagramPacket data into a segment
        //  segment_receive = new Segment(receivePacket);
          //get the ack from the sequence number
        //  getAck = segment_receive.getSeqNum();
          //Thread.sleep(100);
        //  System.out.println(headSeq +" and " + getAck);



        //we know at this point that the head of the queue is equal to the ack we received
        //therefore remove the first element of the queue
        //if(headSeq == getAck){
        //  Segment remove = queue.remove();
      //  }
        //this message is for debugging purposes
        //System.out.println("ack:" + getAck);




      //if something goes wrong catch the exception and exit program
      catch(IOException e){
        System.out.println(e);
        System.exit(0);
      }
    }

  }
  /**
   * ackReceive stop method
   * it stops and terminates the while loop in the run method
   */
  public void stop(){
    stop = false;
    //System.out.println("terminated ack thread");

  }



}
