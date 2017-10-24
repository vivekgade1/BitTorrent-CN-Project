import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
//import java.util.logging.Logger;
import java.util.logging.Logger;


public class peerProcess {
	
	public static int my_peer_id;
    public int listening_port;
    public ServerSocket listening_socket;
    public Thread listening_thread;
    public Thread server_controller;
    public Thread client_controller;
  
    static Logger myLogger;
    
//    Setup variables:
    public static int unchoking_interval;
    public static int opt_unchoking_interval;
    HashMap<Integer, ArrayList<Boolean>> bitsfield;
    ArrayList<Boolean>my_bitsfield;
    public static int piece_cnt=0;
    public static int my_cnt=0;
	
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

    private static void initialSetup(String my_id){
//        boolean isFirstPeer = false;
        my_peer_id = Integer.parseInt(my_id);
//		read common.cfg
//      read peerinfo.cfg
//      set variables: bitsfield, mybitsfield, unchoking_interval, optimistic_inchoking_interval, piece_cnt, my_cnt
//		if first peer break the file into pieces and store in subdirectory. 
//        	Create subdirectory on the fly with name: peer_1001 in current directory
//        
    }
    
	public static void main(String args[]){
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

	public static Boolean IsAnyoneLeftToDownlaod(){
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
		
		while(peerProcess.IsAnyoneLeftToDownlaod() || peerProcess.IsSomethingLeftToDownload()){
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
		while(peerProcess.IsAnyoneLeftToDownlaod()){
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
		while(/*(!is_choked(cleint)) &&*/ peerProcess.IsAnyoneLeftToDownlaod(/*CURRENT CLIENT*/)){	//server could choke the client from SERVER CONTROLLER. stop sending then
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