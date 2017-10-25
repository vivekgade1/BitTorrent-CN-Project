import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
//import java.util.logging.Logger;
import java.util.*;
import java.util.Properties;


public class peerProcess {

    public static int my_peer_id;
    public int listening_port;
    public ServerSocket listening_socket;
    public Thread listening_thread;
    public Thread server_controller;
    public Thread client_controller;
    public static  HashMap<String, String> config_info_map = new HashMap<>();
    public static  HashMap<Integer, String> peer_info_map = new HashMap<>();
    public static  HashMap<Integer, Boolean[]> peer_bitfields = new HashMap<>();
    static Logger myLogger;
    public static int[] my_bitsfield;
    //    Setup variables:
    public static int unchoking_interval;
    public static int opt_unchoking_interval;
    public static int piece_cnt=0;
    public static int my_cnt=0;
    public static int max_bitfield_count;

//	hashmap is_connected_peers;	//Fast access: currently connected, unconnected peers.
//	list of connected_peers;	//easy iterate
//	list of unconnected_peers;

//	Hashmap<Integer, Float> download speed;

//	hashmap is_choked		//peers i have choked/unchoked
//	list of choked_peers
//	list of un_choked_peers

//	hashmap<peer,boolean> am_i_choked; 		//if this peer has choked me
//	hashmap<piece_index, int>piece_status: //0 means not downloaded, 1 = currently downloaded, 2 = downloaded
//

//common.cfg
//    NumberOfPreferredNeighbors 2
//    UnchokingInterval 5
//    OptimisticUnchokingInterval 15
//    FileName TheFile.dat
//    FileSize 10000232
//    PieceSize 32768

//PeerInfo.cfg
//    [peer ID] [host name] [listening port] [has file or not]
//	  1001 192.168.0.18 6008 1
//    1002 192.168.0.18 6008 0
//	  1003 192.168.0.18 6008 0

    private static void initialSetup(String my_id) throws IOException {
//        boolean isFirstPeer = false;
        my_peer_id = Integer.parseInt(my_id);
//		read common.cfg and peer_info.cfg
        try {
            File common_config_file = new File("C:/Users/Vivek Gade/Documents/BitTorrent-CN-Project/src/common.cfg");
            FileInputStream commons_file_reader = new FileInputStream(common_config_file);
            BufferedReader commons_buff_reader = new BufferedReader(new InputStreamReader(commons_file_reader));

            File peer_info_file = new File("C:/Users/Vivek Gade/Documents/BitTorrent-CN-Project/src/PeerInfo.cfg");
            FileInputStream peer_file_reader = new FileInputStream(peer_info_file);
            BufferedReader peer_buff_reader = new BufferedReader(new InputStreamReader(peer_file_reader));
            String line = null;

            while((line = commons_buff_reader.readLine()) != null) {
                String[] parts = line.split(" ");
                config_info_map.put(parts[0],parts[1]);
            }
            line = null;
            int total_file_size = Integer.parseInt(config_info_map.get("FileSize"));
            int piece_size = Integer.parseInt(config_info_map.get("PieceSize"));
            if((total_file_size%piece_size)==0){
                piece_cnt = total_file_size/piece_size;
            }else{
                piece_cnt = (total_file_size/piece_size)+1;
            }

            // seting the max bit filed count value
            max_bitfield_count = piece_cnt*2;

            while((line = peer_buff_reader.readLine()) != null) {
                String[] parts = line.split(" ");
                peer_info_map.put(Integer.parseInt(parts[0]),parts[1] +" " + parts[2]);
                Boolean[] bitmap = new Boolean[piece_cnt];
                if(parts[3].equals("1")) Arrays.fill(bitmap, true);
                else Arrays.fill(bitmap, false);

                if(parts[0].equals(my_id)){
                    my_bitsfield = new int[piece_size];
                    if(parts[3].equals("1")){
                        Arrays.fill(my_bitsfield,2); // setting the bits field to 2 in my bits field because its the server and has the file.
                        my_cnt = max_bitfield_count; // setting the my count to max because i have the file.
                    }
                }
                peer_bitfields.put(Integer.parseInt(parts[0]),bitmap);
            }

            // Always close files.
            commons_buff_reader.close();
            peer_file_reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error in reading common or Peer info configuration.");
        }


//      set variables: bitsfield, mybitsfield, unchoking_interval, optimistic_inchoking_interval, piece_cnt, my_cnt
        unchoking_interval = Integer.parseInt(config_info_map.get("UnchokingInterval"));
        opt_unchoking_interval = Integer.parseInt(config_info_map.get("OptimisticUnchokingInterval"));

//		if first peer break the file into pieces and store in subdirectory.
//        	Create subdirectory on the fly with name: peer_1001 in current directory
        new File("peer_" + my_id).mkdir();
    }

    public static void main(String args[]) throws IOException {
        System.out.print("welcome: "+args[0]+"\n");
        myLogger = Logger.getLogger("InfoLogging");

        initialSetup(args[0]);

        peerProcess pp = new peerProcess();

        //		start Listening thread, and Server and Client controllers for peerProcess
        try {
            System.out.println("Spawning listening Thread : ");
            pp.listening_socket = new ServerSocket(pp.listening_port);
            pp.listening_thread = new Thread(new ListeningThread( pp.listening_socket, my_peer_id));
            pp.server_controller = new Thread(new ServerController(pp.listening_socket, my_peer_id));
            pp.client_controller = new Thread(new ClientController(pp.listening_socket, my_peer_id));
            //stop the threads
            pp.listening_thread.start();
            pp.server_controller.start();
            pp.client_controller.start();

        } catch (Exception e) {
            consoleLog(e.toString());
            System.exit(0);
        }
    }

    public static Boolean IsSomethingLeftToDownload(){
        //should keep checking infinitely until some peer comes online?
        my_cnt=-1;	//For testing only. remove this
        if(my_cnt<piece_cnt)return false;
        return true;
    }

    public static Boolean IsAnyoneLeftToDownload(){
        return false;
    }

    public static String getTime() {
        Date d = new Date();
        return d.toString();

    }

    public static void consoleLog(String message)
    {
        myLogger.info(getTime() + ": Peer " + message);
        System.out.println(getTime() + ": Peer " + message);
    }

}

//Listener
class ListeningThread implements Runnable{

    private ServerSocket listeningSocket;
    private int peerID;
    Socket remoteSocket;
    Thread sendingThread;

    public ListeningThread(ServerSocket socket, int peerID)
    {
        this.listeningSocket = socket;
        this.peerID = peerID;
    }

    @Override
    public void run(){

        while(peerProcess.IsAnyoneLeftToDownload() || peerProcess.IsSomethingLeftToDownload()){
//			server socket
//			server accept
//			wait for bitsfield
//			send my_bitsfield
//			wait for resonse
//			switch(resonse){
//			case "interested":
            //send_response = decideIfWantToSend()
//				send this above response to user
            //if(send_response == "unchoke")
//					start serverThread		//request-response cycle

//			case "have":
            //send_response = CheckIfYouNeed()	//compare the bitsfield also return the piece index I want(which I am not downloading currently)
//				if(send_response == -1) send "NotInterested"
//			    else(){
//					send "interested"
            //expect "unchoke"
            //				start clientThread	//request-response cycle
//				}
//			case default: drop_connection
//		}
        }
    }
}



//server controller
class ServerController implements Runnable{

    private ServerSocket listeningSocket;
    private int peerID;
    Socket remoteSocket;
    Thread sendingThread;

    public ServerController(ServerSocket socket, int peerID)
    {
        this.listeningSocket = socket;
        this.peerID = peerID;
    }

    @Override
    public void run(){
        while(peerProcess.IsAnyoneLeftToDownload()){
//			select top-k peers: who still need data which this server has
//			select 1 more optimistically

//			send "CHOKE" to all others(who are connected)  > This msg should be receive by client in clientThread.
//			for(every selected peer){
//				send TCP req if not connected already(and exchage bitsfield)
//				send "HAVE" to this peer;
//				wait for response
//				if(response == "INTERESTED"){
//					send "UNCHOKE"
//					start serverThread
//				}
//				else drop_connection
//			}

//			IMP: PAUSE for p=15 seconds now
        }
    }
}



//client controller
class ClientController implements Runnable{

    private ServerSocket listeningSocket;
    private int peerID;
    Socket remoteSocket;
    Thread sendingThread;

    public ClientController(ServerSocket socket, int peerID)
    {
        this.listeningSocket = socket;
        this.peerID = peerID;
    }

    @Override
    public void run(){
        while(peerProcess.IsSomethingLeftToDownload()){
//			list of peers = select peers who have not "choked me" and are not connected		(also check if they have something to offer? No?)
//			for(every selected peer){
//				send TCP req if not connected already(and exchage bitsfield)
//				send "INTERESTED" to this peer
//				wait for response
//				if(response=="UNCHOKE"){
//					start clientThread
//				}
//				else if "CHOKE"
//					save this status. and drop_connection
//				else drop_connection
//			}
        }
    }
}



//	serverThread> exchange between 1 client and server only. This connection is currenly active

class ServerThread implements Runnable{

    private ServerSocket listeningSocket;
    private int peerID;
    Socket remoteSocket;
    Thread sendingThread;

    public ServerThread(ServerSocket socket, int peerID)
    {
        this.listeningSocket = socket;
        this.peerID = peerID;
    }

    @Override
    public void run(){
//		private information about the client
        while(/*(!is_choked(cleint)) &&*/ peerProcess.IsAnyoneLeftToDownload(/*CURRENT CLIENT*/)){	//server could choke the client from SERVER CONTROLLER. stop sending then
//			wait for request
//			if(invalid request) break the connection
//			send the piece requested.
        }
    }
}



//	clientThread
//hashmap<piece_index, int>piece_status: //0 means not downloaded, 1 = currently downloaded, 2 = downloaded
class ClientThread implements Runnable{

    private ServerSocket listeningSocket;
    private int peerID;
    Socket remoteSocket;
    Thread sendingThread;

    public ClientThread(ServerSocket socket, int peerID)
    {
        this.listeningSocket = socket;
        this.peerID = peerID;
    }

    @Override
    public void run(){
//		private information about the server

        while(peerProcess.IsSomethingLeftToDownload(/*FROM THIS SERVER*/)){
//			piece = check Bitsfield of S; what you need to download from it; (that u are not downloading from other peer also)
//			change download status of piece = 1;
//			start(time)
//			request piece
//			wait for response		//NOTE: THE RESPONSE HERE COULD ALSO BE "CHOKE" or ("REQUEST" if server is also downloading from us). NEED to DIFFERENTIATE THIS.
//			stop(time)
//			updateAvgDS(sever, time)	//download speed
//			if(got_piece_correctly){
//				piece_status[piece] =2;
//				update my_bitsfield
//				update my_cnt;
//			}else{
//				if(piece_status[piece]==1)	//CHECK becz: somebody else may have downloaded it correctly and changed status to 2!
//					piece_status[piece] =0
//				drop_connection();
//			}
        }
    }
}