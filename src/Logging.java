import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logging {
	private static String getCurrentTime(){
		 DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		 Date date = new Date();
		 System.out.println(dateFormat.format(date));
		 return new String(dateFormat.format(date));
	 }

	 static void writeLog(String message){
		  String mypeerid = message.substring(message.indexOf('[') + 8 , message.indexOf(']'));

			try {
				createFile(mypeerid);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		   String FileName = "log_peer_"+mypeerid+".log";
           String  writemessage = "["+getCurrentTime()+"]: "+message;

           FileWriter fw = null;
			try {
				fw = new FileWriter(FileName,true);
			} catch (IOException e) {
				e.printStackTrace();
			}
           try {
				fw.write("\n"+writemessage);
			} catch (IOException e) {
				e.printStackTrace();
			}
           try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	 }
	private static void createFile(String my_id) throws IOException {
		    try {
			   	     File file = new File("log_peer_"+my_id+".log");
			   	     boolean fvar = file.createNewFile();
			   	     if (fvar){
			   	          System.out.println("File has been created successfully");
			   	     }
			   	     else{
			   	          System.out.println("File already present at the specified location");
			   	     }
		   	} catch (IOException e) {
			         e.printStackTrace();
			}
	}
	public static void main(String[] args){
		writeLog("Peer [peer_ID 1001] makes a connection to peer 2");
	}
}
