
/**
 * UrlCache Class
 * 
 *
 */

import java.awt.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TimeZone;

public class UrlCache {

	//the hashmap will contain 3 variables 
	//(url of the object, date modified, )
	HashMap<String, String> catalog = new HashMap<String, String>();
	
	int fileCounter = 1;

    /**
     * Default constructor to initialize data structures used for caching/etc
	 * If the cache already exists then load it. If any errors then throw runtime exception.
	 *
     * @throws IOException if encounters any errors/exceptions
     */
	public UrlCache() throws IOException {
		/*
		 * 1)initialize your variables 
		 * 2)read the catalog, check if it exists
		 * 		1)if it does read it 
		 *  	2)if not make it
		 */
		
		//make a file directory 
		File dir = new File("File Folder");
		
		if(dir.exists()) {
			//System.out.println("Directory exists");
		}
		else {
			dir.mkdir();
		}
		
		//Initialize a files, variables and our catalog 
		String fileName = "dataFile.txt";
		File f = new File("File Folder/" + fileName);
		String currentLine;
				
		//check if the file exists, if it does get data from it  
		if (f.exists()) {
			//print test case 
			//System.out.println("File "+ fileName + " Exists");
			
	        try {
	        	//try creating file reader and buffer reader
	            FileReader FR = new FileReader("File Folder/" + fileName);
	            BufferedReader BR = new BufferedReader(FR);
	            
	            //read until the next line we read is null
	            while((currentLine = BR.readLine()) != null) { 
	            	//print test case
	                //System.out.println(currentLine);
	            	
	                //spilt the first line and get the data
	                String [] splitFirstLine = currentLine.split(" ", 3);
	                //System.out.println(splitFirstLine[1] + splitFirstLine[2]);
	                catalog.put(splitFirstLine[0], splitFirstLine[1] + " " + splitFirstLine[2]);

	               // System.out.println(splitFirstLine[0]);
	               // System.out.println(splitFirstLine[1]);
	                //System.out.println(splitFirstLine[2]);
	            }   
	            
	            //close the buffer reader 
	            BR.close();  
	            
	        }

	        catch(IOException e) {
	        	//if something goes wrong catch and print the error message 
	            e.printStackTrace();
	        }
			
		}else {
			//if our code gets here that means that the file does not currently exist
			//create a new file 
			f.getParentFile().mkdirs(); 
			f.createNewFile();
			//print test case
			System.out.println("File dataFile.txt created");
		}
		
		/*//this is a test case 
		int n = 0;
		while(n < i) {
			System.out.println(catalog[n]);
			n++;
		}*/
	
	}
	
    /**
     * Downloads the object specified by the parameter url if the local copy is out of date.
	 *
     * @param url	URL of the object to be downloaded. It is a fully qualified URL.
     * @throws IOException if encounters any errors/exceptions
     */
	public void getObject(String url) throws IOException {
		/*
		 * url given as string 
		 * 1)splice the string into method url version 
		 * 2)extract the host and ports, default 80 for the port 
		 * 3)construct the HTTP get (page 115 of the text book)
		 * 4)Setup a TCP connection and send the HTTP GET [socket (hostname, port)]
		 * 5)Read and interpret the server response (ie. 200, 304 good| 404 or 400 is bad)
		 * 6)check the HTTP status (either be 200 or 304| if not 200 or 304 then something went wrong with your program)
		 * 7) 200 ok received:
		 * 		1)update the catalog 
		 * 		2)remove the HTTP Response Header and save the content (ie \r\n\r\n)
		 * 
		 */
		
		//print test case
		//System.out.println(url);
		
		//System.out.println(url);
		
		//initalize the variables we are using 
		String currentLine;
		int port = 80;
		String host = null;
		String copyURL = url;
		String path = "";
		
		//prepare to take the www. out of the url if the url has a www. 
		String splitURL = url;		
		//split the url into char elements and put them into the array
		String [] splitArray = splitURL.split(""); 
		
		//boolean to check if url has the www. in it 
		Boolean ifWWW = false; 
		String newURL = "";
		//check if the url contains "www." in the beginning by concatinaing the first 4 elements 
		String www = splitArray[0] + splitArray[1] + splitArray[2] + splitArray[3];
		if(www.equalsIgnoreCase("www.")) {
			//if it contains www. then leave them out 
			for(int i = 4; i < splitArray.length; i++) {
				newURL = newURL + splitArray[i];
			}
			//check boolean flag to know that we have a modified url to work with 
			ifWWW = true;
		}

		//test case 
		//System.out.println(newURL);	
		
		//check if it had www., if it did use the newURL for the copyURL 
		if(ifWWW == true) {
			//get newURL and use it for the next part 
			copyURL = newURL;	
		}
		
		//split the URL into 2 pieces by the first "/", first piece is the host and the second is the path name 
		String [] brokenURL = copyURL.split("/", 2);
		//check if the url has a port specified, we check this if the url has a ":"
		if(brokenURL[0].contains(":")) {
			//if it has a port then we split it into 2, the first one is the host name the second is the port number without the :
			String [] hostPlusPort = brokenURL[0].split(":", 2);
			host = hostPlusPort[0];
			port = Integer.parseInt(hostPlusPort[1]);
			
			
			//we have the host and the port, but we need the path
			//this splits and removes the "/" of the url	
			String [] splitPath = copyURL.split("/",2); 
			path = "/" + splitPath[1];
			
		}
		//if we find no ":" then we know that the url has no port
		else {
			host = brokenURL[0];
			path = "/" + brokenURL[1];
		}
		
		/*
		//try and find ":"
		String [] findSomething = temp.split("");
		for(int i = 0; i < findSomething.length; i++) {		
			if(findSomething[i].equals(":")) {
				//if the string contains a ":" then that means the next 2 chars are the port number 
				String []split1 = temp.split(":");
				//split[0] is before ":", and split[1] is after the ":"
				//now we need to take out the 2 numbers after the ":"
		
				String temp3 = split1[1];
				String []split3 = temp3.split("");
				String getPortNum = split3[1];
				String [] splitTheAfter = getPortNum.split("");
				String stringPort = splitTheAfter[0] + splitTheAfter[1];
				port = Integer.parseInt(stringPort);
				
				String changeURL = "";
				for(int z = 2; i < splitTheAfter.length;z++) {
					changeURL = changeURL + splitTheAfter[z];
				}
				
				//get rid of the :80 in the url 
				temp = split3[0] + changeURL;
				
				break;
			}
		}*/
		
		//check if the port number is good, ie not -1
		if(port < 0) {
			//if it is -1 then initialize it to 80, since we can't have -1
			port = 80;
		}
		
/*
		String [] split = copyURL.split("/"); //this splits and removes the "/" of the url
		
		host = split[0];
		//get the path name of the url 
		for(int i = 1; i < split.length; i++ ) {
			path = path + "/" + split[i];
		}
		
			
		//System.out.println(path);
/*
		//make a socket with the host and port 
		try {
			Socket socket = new Socket(InetAddress.getByName(host), port);
			outputStream = new PrintWriter(socket.getOutputStream());	
			
			outputStream.println("GET / " + path + " HTTP/1.1");//need to put the path name here
			outputStream.println("Host: " + "www." + host); 
			outputStream.println("");
			
			inputStream = new Scanner(new InputStreamReader(socket.getInputStream()));
			currentLine = inputStream.nextLine();
			while(currentLine != null) {
				System.out.println(currentLine);
				currentLine = inputStream.nextLine();
			}	
			inputStream.close();
			System.out.println("test");
		}
		catch(IOException e) {
			//trace the error if one happens
			e.printStackTrace();
		}
		*/
		
		//System.out.println(host);
		//System.out.println(path);
	//	System.out.println(port);
		
		//path = "~mghaderi/index.txt";
		/*
		Socket socket = new Socket(InetAddress.getByName(host), port);
		PrintWriter outputStream = new PrintWriter(new DataOutputStream(socket.getOutputStream()));
		//Scanner inputStream1 = new Scanner(new InputStreamReader(socket.getInputStream()));
		outputStream.print("GET /" + path + " HTTP/1.1\r\n");
		outputStream.print("Host: " + host + ":" + port + "\r\n\r\n");
		*/
	
		Socket socket = null;
		try {
			socket = new Socket(InetAddress.getByName(host), port);
			String firstLine = "GET /" + path + " HTTP/1.1\r\n";
			String secLine = "Host: " + host + ":" + port + "\r\n\r\n";
			
			String header = firstLine + secLine;
			//if the url is present in catalog, and "if-modified since" field also 
			byte [] httpHeader = header.getBytes("US-ASCII");
			
			socket.getOutputStream().write(httpHeader);
			socket.getOutputStream().flush();

		}
		catch(IOException e) {
			//print if something went wrong 
			e.printStackTrace();
			
		}
		byte [] header = new byte[2084];
		
		String headerString = "";
		int off = 0;
		
		//read byte by byte from the socket
		int bytesRead = 0;
		try {
			while(bytesRead != -1) {
				socket.getInputStream().read(header, off, 1);
				bytesRead = header[off];
				off++;
				headerString = new String(header, 0, off, "US-ASCII");
				//breack if we reach a new line that is empty 
				if(headerString.contains("\r\n\r\n")) {
					break;
				}
				
			}
			
		}
		catch(IOException e) {
			//print if something went wrong
			e.printStackTrace();
		}
		 
		//test to print header
		System.out.println(headerString);
		
		String [] headerSplit = headerString.split("\\r?\\n");
		
		//System.out.println(headerSplit[0]);
		
		//we will now take the response header which we got from the server and parse it
		//to see the response number it gave us (ie. 200, 300) and the last date modified
		
		//headerSplit[0] = Http/1.1 200 OK
		String [] responceOK = headerSplit[0].split(" ");
		//should be the responceOK[1]
		String isOK = responceOK[1];
		int changeOkToString = Integer.parseInt(isOK);
		
		//check if the responce we got was good
		if(changeOkToString != 200) {
			System.out.println("Something went Wrong,"+ changeOkToString +"was the responce we got"+ "exiting");
			System.exit(0);
			
		}	
		System.out.println(changeOkToString);
		
		//so now we know we have a valid http response code
		//next we need to grab the last date modified 
		//this will be headerSplit[3]
		String lastMod = headerSplit[3];
		//System.out.println(lastMod);
		String [] lastModSplit = lastMod.split(" ",2);
		lastMod = lastModSplit[1];
		System.out.println(lastMod);
		
		//now we have the last modified date of our code,
		//next we need to get the content type of inputstream 
		//this would be headerSplit[7]
		String contentType = headerSplit[7];
		//System.out.println(contentType);
		String [] contentTypeSplit = contentType.split(" ");
		contentType = contentTypeSplit[1];
		System.out.println(contentType);
		
		
		//we now need to get the content length, then make the content length an int
		//this would be headerSplit[6]
		String contentLength = headerSplit[6];
		String [] contentLengthSplit = contentLength.split(" ");
		contentLength = contentLengthSplit[1];
		System.out.println(contentLength);
		int contentInt = Integer.parseInt(contentLength);
		
		//now check to see if we already down loaded the object before 
		for (String key: catalog.keySet()) {
			//get a key
			if(key.equals(url)) {
				//if it equals a url then check the last time the object was modified 
				String [] splitValuekey = catalog.get(key).split(" ", 2);
				String lastModKey = splitValuekey[1];
				//if last modified == last modified for object we have, then dont download
				if(lastModKey.equals(lastMod)) {
					return;
				}
			}
			
		}

		//now we need to check what the content type is
		//if its a text/html then we convert the bytes to string
		//if its a image/gif then we convert the bytes to ______
		//if its a application/pdf then we convert the bytes to ______
		int counter = 0; 
		int amountBR = 0;
		byte [] object = new byte [contentInt];
		//String textOutput = "";
		//String str = " \r\n\r\n ";
		
		if(contentType.equals("text/html;")) {
			FileOutputStream fileOPS = new FileOutputStream("File Folder/file" + fileCounter + ".txt");
			fileCounter++;
			try {
				while(amountBR != -1) {
					//if our counter reaches the content length, break out of the loop 
					if(counter == contentInt) {
						break;
					}
					
					amountBR = socket.getInputStream().read(object, counter, 1);
					//write to file 'num_byte_read" bytes
					
					fileOPS.write(object[counter]);
					//fileOPS.write(byteArray);
					counter++;
					//textOutput = new String(object, 0, counter, "US-ASCII");								
					
				}

				//fileOPS.write(byteArray);
				fileOPS.close();
				
			}
			catch(IOException e) {
				//print if something went wrong
				e.printStackTrace();
			}
			
		}
		
		else if(contentType.equals("image/gif")) {
			FileOutputStream fos = new FileOutputStream("File Folder/file" + fileCounter + ".jpg");
			fileCounter++;
			try {
				while(amountBR != -1) {
					//if our counter reaches the content length, break out of the loop 
					if(counter == contentInt) {
						break;
					}
					
					amountBR = socket.getInputStream().read(object, counter, 1);
					//write to file 'num_byte_read" bytes
					
					fos.write(object[counter]);
					//fileOPS.write(byteArray);
					counter++;
					//textOutput = new String(object, 0, counter, "US-ASCII");								
					
				}

				//fos.write(byteArray);
				//fos.flush();
				fos.close();
			
			}
			catch(IOException e) {
				//print if something went wrong
				e.printStackTrace();
			}
			
		}
		
		else if(contentType.equals("application/pdf")) {
			OutputStream fos = new FileOutputStream("File Folder/file" + fileCounter + ".pdf");
			fileCounter++;
			try {
				while(amountBR != -1) {
					//if our counter reaches the content length, break out of the loop 
					if(counter == contentInt) {
						break;
					}
					
					amountBR = socket.getInputStream().read(object, counter, 1);
					//write to file 'num_byte_read" bytes
					//fos.write(object[counter]);
					
					//fileOPS.write(byteArray);
					counter++;
					//textOutput = new String(object, 0, counter, "US-ASCII");								
					
				}

				//fos.write(byteArray);
				//fos.flush();
				fos.write(object);
				fos.close();
			
			}
			catch(IOException e) {
				//print if something went wrong
				e.printStackTrace();
			}
			
		}
		
		else {
			//error handling if the content type is not a text, image, or pdf since we cannot handle it 
			System.out.println("The content type: " + contentType + " is not supported by this program, exiting");
			System.exit(0);
			
		}
		
		//System.out.println(textOutput);
		System.out.println("");
		
		//save object into the hashmap 
		catalog.put(url, "0 " + lastMod);
		
		//delete all the text in the dataFile.txt
		File f = new File("File Folder/dataFile.txt");
		f.delete();

		f.getParentFile().mkdirs(); 
		f.createNewFile();
				    
		FileOutputStream fos = new FileOutputStream(f); 
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		//get all the keys in the hash map, and save it to the file 
		for (String key: catalog.keySet()) {
			//save key and value to dataFile.txt
			bw.write(key + " " + catalog.get(key));
			bw.newLine();
		}
		 
		bw.close();

		//FileOutputStream fout = new FileOutputStream("File Folder/dataFile.txt");
		//ObjectOutputStream oos = new ObjectOutputStream(fout);
		//oos.writeObject(catalog);
		
		//push changes
		//toCatalogFile.flush();
		//close stream
		//toCatalogFile.close();
	
	}
	
    /**
     * Returns the Last-Modified time associated with the object specified by the parameter url.
	 *
     * @param url 	URL of the object 
	 * @return the Last-Modified time in millisecond as in Date.getTime()
     */
	public long getLastModified(String url) {
		/*
		 * Suppose to throw and exception 
		 * look up for the url in the catalog 
		 * strip the port number 
		 */
		long millis = 0;
		String date = "";/*
		
		LinkedList <Object> linkedList = new LinkedList<>(); 
		
		String key= null;
        String value="somename";
        for(Map.Entry entry: catalog.entrySet()){
            if(value.equals(entry.getValue())){
                key = (String) entry.getKey();
                break; //breaking because its one to one map
            }
            linkedList.add(entry);
            
            
        }
            
        String firstEl = "";
        String secEl = "";
        long finalDate = 0;
        /*
        String[] array;
        //people.ucalgary.ca/~mghaderi/index.html=0 Fri, 18 Aug 2017 20:44:24 GMT
        for(int i = 0; i < linkedList.size(); i++) {
        	//String.ValueOf(linkedList.get(i)); 
        	firstEl = "" + linkedList.get(i);
        	array = firstEl.split("=", 2);
        	firstEl = array[0];
        	//System.out.println(firstEl);
        	//if its equal to the url then extract the time 
        	if(url.equals(firstEl)) {
        		//System.out.println(url + " "+ firstEl);
        		//get value
        		secEl = array[1];
        		//split value
        		array = secEl.split(" ", 2);
        		finalDate = Long.valueOf(array[1]).longValue();
        		
        	}
        	
        }
        
        System.out.println(secEl);

		/*
		if (catalog.containsKey(url)) {
			 Object value = catalog.get(url);
			 System.out.println("Key : " + url +" value :"+ value);
		}
		
		*//*
		for (String key: catalog.keySet()) {
			//get a key
			if(key.equals(url)) {
				//if it equals the url passed by the method then check the last time it was modified in milli seconds 
				String [] splitValuekey = catalog.get(key).split(" ", 2);
				String lastModKey = splitValuekey[1];
				String [] getSecValue = lastModKey.split(" ",2);
				date = getSecValue[1];
				//System.out.println("date :" + date);
			}
		}

		
		long temp = Long.parseLong(date);
		
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		String httpDate = sdf.format(new Date(temp));
		
		millis = Long.valueOf(httpDate).longValue();
		*/
		return millis;
	}

}
