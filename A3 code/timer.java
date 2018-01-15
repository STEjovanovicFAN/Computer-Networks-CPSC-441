/**
 * timer Class
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
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import cpsc441.a3.shared.*;

public class timer extends TimerTask {

  public int timeOut;
  public int windowSize;
  public TxQueue queue;
  public FastFtp mainFTP;

  /**
   *this constructor just initalizes FastFtp variable
   *
   *@param FastFtp parent that calls this thread
   */
  public timer(FastFtp ftp){
    //get our calling code main
    mainFTP = ftp;
  }

  /**
   * timer run method
   * using the calling class FastFtp it just calls another thread to handle the
   */
  public void run(){
    //if the timer thread times out handle the time out by calling our
    //calling class method: processTimeout

    //try{
    // call processTimeout() in the main class
    //FastFtp callPT = new FastFtp(timeOut, windowSize);
    //callPT.processTimeout(queue);

    //if this method gets called handle the timeout in our main method
    //processTimeout should resend all the threads currently in the queue
    mainFTP.processTimeout();

    //}
/*
    catch(IOException e){
			System.out.println(e);
			System.exit(0);
		}
*/
  }

}
