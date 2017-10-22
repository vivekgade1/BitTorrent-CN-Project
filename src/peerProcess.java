import java.util.ArrayList;
import java.util.HashMap;


public class peerProcess {
	
	
	//Attributes
//	int/String/ my_id
//	HashMap<String, ArrayList<Boolean>> bitsfield;
//	ArrayList<Boolean>my_bitsfield;
//	int piece_cnt;
//	int my_cnt;
	
//	hashmap is_connected_peers;	//Fast access: currently connected, unconnected peers.
//	list of connected_peers;	//easy iterate
//	list of unconnected_peers;
	
//	hashmap<peer_id, int> download speed;
	
//	hashmap is_choked		//peers i have choked/unchoked
//	list of choked_peers
//	list of un_choked_peers
	
//	hashmap<peer,boolean> am_i_choked; 		//if this peer has choked me
//	hashmap<piece_index, int>piece_status: //0 means not downloaded, 1 = currently downloaded, 2 = downloaded
//	
	
	public static void main(String args[]){
	System.out.print("welcome");
//		start Listener
//		start server controller
//		start client controller
	}
	
	
	//Listener	> convert to Runnable class
	void listener(){
		
		while(IsAnyoneLeftToDownlaod() || IsSomethingLeftToDownload()){
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
	
	//server controller > convert to Runnable class
	void serverController(){
		while(IsAnyoneLeftToDownlaod()){
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
	
	
	//client controller > convert to Runnable class
	void clientController(){
		while(IsSomethingLeftToDownload()){
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
	
//	serverThread> exchange between 1 client and server only. This connection is currenly active
	void serverThread(){
//		private information about the client
		while(/*(!is_choked(cleint)) &&*/ IsAnyoneLeftToDownlaod(/*CURRENT CLIENT*/)){	//server could choke the client from SERVER CONTROLLER. stop sending then
//			wait for request
//			if(invalid request) break the connection
//			send the piece requested.
		}
	}
	
//	clientThread
//	hashmap<piece_index, int>piece_status: //0 means not downloaded, 1 = currently downloaded, 2 = downloaded 
	void clientThread(){
//		private information about the server
		
		while(IsSomethingLeftToDownload(/*FROM THIS SERVER*/)){
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

	
	
	Boolean IsSomethingLeftToDownload(){
		return true;
	}
	Boolean IsAnyoneLeftToDownlaod(){
		return true;
	}
	
}
