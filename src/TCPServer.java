import java.io.*;
import java.net.*;

public class TCPServer {
 public static void main(String argv[]) throws Exception {
  String clientSentence;
  String capitalizedSentence;
  ServerSocket welcomeSocket = new ServerSocket(6789);

  while (true) {
	    Socket connectionSocket = welcomeSocket.accept();
	    DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
	    DataInputStream  inFromClient = new DataInputStream (connectionSocket.getInputStream());

	   byte[] RecFromClient = new byte["interested".getBytes().length];
	   inFromClient.read(RecFromClient, 0, "interested".getBytes().length);
	   System.out.println("11");
	   String s =new String(RecFromClient);
	   System.out.println(s+" is string s");

	  if(s.equals("interested")){
		  String message ="unchoke";
		  byte[] bytes = message.getBytes();
	      outToClient.write(bytes);
		  System.out.println("Written unchocke to client");
		  System.out.println("22");
		  byte[] RecPieceClient = new byte["0".getBytes().length];
		  inFromClient.read(RecPieceClient, 0, RecPieceClient.length);
		  String s1 =new String(RecPieceClient);
		  System.out.println(s1+" is string s");
		  String SourceFILE_NAME = "C://Users//hamsi//Desktop//CNProject//src//TextFile.txt";
		//
		//  int i =(int) RecFromClient;
		//   for(int i=0;i<5;i++)
		//   {
		  String fileName = SourceFILE_NAME+".part"+Integer.valueOf(s1);
		  sendFile(fileName, connectionSocket,  inFromClient, outToClient);
		//   }
	  }


  }
 }
  public static void sendFile(String fileName, Socket connectionSocket, DataInputStream inFromClient ,  DataOutputStream outToClient) {
		try {

	        File sendingFile = new File(fileName);
	        byte[] byteArray = new byte[(int) sendingFile.length()];

	        FileInputStream fis = new FileInputStream(sendingFile);
	        BufferedInputStream bis = new BufferedInputStream(fis);

	        DataInputStream dis = new DataInputStream(bis);
	        dis.readFully(byteArray, 0, byteArray.length);

//	        DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());


//	        outToClient.writeUTF(sendingFile.getName());
//	        outToClient.writeLong(byteArray.length);
	        outToClient.write(byteArray, 0, byteArray.length);
	        outToClient.flush();

	        System.out.println("File " + fileName + " sent to Client");

	    } catch (Exception e) {
	        System.err.println("Error! " + e);
	    }
	}
}