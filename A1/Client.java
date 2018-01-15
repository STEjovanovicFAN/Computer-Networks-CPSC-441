import java.util.*;
import java.net.*;
import java.io.*;

public class Client {

	public static void main(String[] args) throws IOException {
		//an array of all the urls 	
		String allUrls [] = {"people.ucalgary.ca/~mghaderi/index.html",
						     "people.ucalgary.ca/~mghaderi/test/uc.gif",
						     "people.ucalgary.ca/~mghaderi/test/a.pdf",
						     "people.ucalgary.ca:80/~mghaderi/test/test.html"};

		//make a new object urlCache, the constructor will initialize all the variables 
		UrlCache urlObject = new UrlCache();
		
		//take the first url as a test case
		String url = allUrls[0];
		
		//pass the url to the object and let the object handle the SITUALTIONAL GET  
		urlObject.getObject(url);
		
	}
	
}
