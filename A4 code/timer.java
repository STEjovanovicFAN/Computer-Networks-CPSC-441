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

import cpsc441.a4.shared.*;
//class of timer
public class timer extends TimerTask {
  
  public Router router;

  /**
   *this constructor just initalizes Router variable
   *
   *@param Router parent that calls this thread
   */
  public timer(Router r){
    //get our calling code main
    router = r;
  }

  /**
   * timer run method
   * using the calling class FastFtp it just calls another thread to handle the
   */
  public void run(){
    //proccesses timer timeout
    router.processTimeout();

  }

}
