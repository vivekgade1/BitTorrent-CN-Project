//TCPlient.java

import java.io.*;
import java.net.*;
import java.util.Arrays;

public class TCPClient {
 public static void main(String argv[]) throws Exception {
  String sentence;
  String fromServer;
  Socket clientSocket = new Socket("localhost", 6789);

  DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
  DataInputStream  inFromServer = new DataInputStream (clientSocket.getInputStream());

  String message ="interested";
  byte[] bytes = message.getBytes();
  byte[] RecFromServer = new byte[message.length()];
  outToServer.write(bytes);
  System.out.println("Written interested to server");

  byte[] RecUnchoke = new byte["unchoke".getBytes().length];
  inFromServer.read(RecUnchoke, 0, RecUnchoke.length);
  System.out.println("1111");
  String s =new String(RecUnchoke);
  System.out.println(s+" is string s");

  if(s.equals("unchoke")){
	  System.out.println("got unchocke from server");
	  message ="0";
	  bytes = message.getBytes();
	  outToServer.write(bytes);
	  System.out.println("Written 0 to server");
	  System.out.println("2222");
	  byte[] RecData = new byte[1024];
	  int RecBytes;
	  int i=0;
	  while ((RecBytes = inFromServer.read(RecData, 0, RecData.length)) > 0)
	  {
	        String SaveFileName = "TextFileFromServer.txt.part"+i++;
	    	OutputStream Fs = new FileOutputStream  (SaveFileName);
	    	Fs.write(RecData, 0, RecBytes);
	        System.out.println("File " + SaveFileName + " received.");
	    	Fs.close();
	  }
  }

  }
}