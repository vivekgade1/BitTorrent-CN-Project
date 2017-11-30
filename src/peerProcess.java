import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.*;


public class peerProcess {
    public static int my_peer_id;
    public static ArrayList<Integer> present_connections = new ArrayList<>();
    public static Set<Integer> present_client_connections = new HashSet<>();
    public static Set<Integer> present_server_connections = new HashSet<>();

    public int listening_port;
    public ServerSocket listening_socket;
    public Thread listening_thread;
    public Thread server_controller;
    public Thread client_controller;
    public static  HashMap<String, String> config_info_map = new HashMap<>();
    public static  HashMap<Integer, String> peer_info_map = new HashMap<>();
    public static  HashMap<Integer, Boolean[]> peer_bitfields = new HashMap<>();	//ith piece present or not

    public static  HashMap<Integer, Integer> peer_download_time = new HashMap<>();
    public static  HashMap<Integer, Integer> peer_download_cnt = new HashMap<>();
    public static  HashMap<Integer, Double> peer_download_rate = new HashMap<>();
    public static  HashMap<Integer, Boolean> peer_choke_status= new HashMap<>();	//choked or not

    static Logger myLogger;
    public static int[] my_bitfield;	//0 means not downloaded, 1 = currently downloading, 2 = downloaded
    //    Setup variables:
    public static int preferred_neighbor_limit;
    public static int unchoking_interval;
    public static int opt_unchoking_interval;
    public static int piece_cnt=0;
    public static int my_cnt=0;
    public static int max_bitfield_count;	//piece_count*2: every field in the my_bitsfield would be 2.
    public static String my_path;

//	hashmap is_connected_peers;	//Fast access: currently connected, unconnected peers.
//	list of connected_peers;	//easy iterate
//	list of unconnected_peers;

//	Hashmap<Integer, Float> download speed;

//	hashmap is_choked		//peers i have choked/unchoked
//	list of choked_peers
//	list of un_choked_peers

//	hashmap<peer,boolean> am_i_choked; 		//if this peer has choked me
//	hashmap<piece_index, int>piece_status: //0 means not downloaded, 1 = currently downloading, 2 = downloaded
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
        my_path  = "peer_" + my_id + "/";
        new File("peer_" + my_id).mkdir();
//		read common.cfg and peer_info.cfg
        try {
            File common_config_file = new File("src/common.cfg");
            FileInputStream commons_file_reader = new FileInputStream(common_config_file);
            BufferedReader commons_buff_reader = new BufferedReader(new InputStreamReader(commons_file_reader));

            File peer_info_file = new File("src/PeerInfo.cfg");
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
            if((total_file_size%piece_size)== 0){
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
                if(parts[3].equals("1")){
                    Arrays.fill(bitmap, true);
                    if(parts[0].equals(my_id))splitFile();
                }
                else Arrays.fill(bitmap, false);

                if(parts[0].equals(my_id)){
                    my_bitfield = new int[piece_cnt];
                    if(parts[3].equals("1")){
                        Arrays.fill(my_bitfield,2); // setting the bits field to 2 in my bits field because its the server and has the file.
                        my_cnt = max_bitfield_count; // setting the my count to max because i have the file.
                    }
                }
                peer_bitfields.put(Integer.parseInt(parts[0]),bitmap);
                peer_choke_status.put(Integer.parseInt(parts[0]),false);
            }

            // Always close files.
            commons_buff_reader.close();
            peer_file_reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error in reading common or Peer info configuration.");
        }


//      set variables: bitsfield, mybitsfield, unchoking_interval, optimistic_inchoking_interval, piece_cnt, my_cnt
        preferred_neighbor_limit= Integer.parseInt(config_info_map.get("NumberOfPreferredNeighbors"));
        unchoking_interval = Integer.parseInt(config_info_map.get("UnchokingInterval"));
        opt_unchoking_interval = Integer.parseInt(config_info_map.get("OptimisticUnchokingInterval"));

//		if first peer break the file into pieces and store in subdirectory.
//        	Create subdirectory on the fly with name: peer_1001 in current directory


    }

    public static void splitFile(){
        String FILE_NAME = my_path + config_info_map.get("FileName");
        int PART_SIZE = Integer.parseInt(config_info_map.get("PieceSize"));
        File inputFile = new File(FILE_NAME);
        FileInputStream inputStream;
        String newFileName;
        FileOutputStream filePart;
        int fileSize = (int) inputFile.length();
        int nChunks = 0, read = 0, readLength = PART_SIZE;
        byte[] byteChunkPart;
        try {
            inputStream = new FileInputStream(inputFile);
            while (fileSize > 0) {
                if (fileSize <= PART_SIZE) {
                    readLength = fileSize;
                }
                byteChunkPart = new byte[readLength];
                read = inputStream.read(byteChunkPart, 0, readLength);
                fileSize -= read;
                assert (read == byteChunkPart.length);
                nChunks++;
                newFileName = FILE_NAME + ".part"
                        + Integer.toString(nChunks - 1);
                filePart = new FileOutputStream(new File(newFileName));
                filePart.write(byteChunkPart);
                filePart.flush();
                filePart.close();
                byteChunkPart = null;
                filePart = null;
            }
            inputStream.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static void mergeFile(){
        String Source_FILE_NAME = config_info_map.get("FileName");
        File merged_file = new File(my_path + Source_FILE_NAME);
        FileOutputStream fos;
        FileInputStream fis;

        byte[] fileBytes;
        int bytesRead = 0;
        List<File> list = new ArrayList<File>();

        for(int i=0;i< piece_cnt;i++)
            list.add(new File(my_path + Source_FILE_NAME+".part"+i));

        try {
            fos = new FileOutputStream(merged_file,false);
            for (File file : list) {
                fis = new FileInputStream(file);
                fileBytes = new byte[(int) file.length()];
                bytesRead = fis.read(fileBytes, 0,(int)  file.length());
                fos.write(fileBytes);
                fos.flush();
                fileBytes = null;
                fis.close();
                fis = null;
            }
            fos.close();
            fos = null;
        }catch (Exception exception){
            exception.printStackTrace();
        }

    }

    public static void main(String args[]) throws IOException {
        System.out.print("welcome: "+args[0]+"\n");
        myLogger = Logger.getLogger("InfoLogging");

        initialSetup(args[0]);

        peerProcess pp = new peerProcess();
        pp.listening_port = Integer.parseInt(peerProcess.peer_info_map.get(my_peer_id).split(" ")[1]);

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
//        my_cnt=-1;	//For testing only. remove this
//        if(my_cnt<piece_cnt)return false;
        my_cnt = Arrays.stream(my_bitfield).sum();
        if(my_cnt == max_bitfield_count){
            mergeFile();
            return false;
        }else{
            return true;
        }
    }

    public static Boolean IsAnyoneLeftToDownload(){
        for (int peer_id: peer_bitfields.keySet()) {
            Boolean[] peer_bit_field = peer_bitfields.get(peer_id);
            for (int i = 0; i < peer_bit_field.length  ; i++) {
                if(!peer_bit_field[i]){
                    return true;
                }
            }
        }
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

    public static void updatePeerBitfields(int peer_id, int[] peer_bitfield) {

    	for (int i = 0; i < peer_bitfield.length ; i++) {
            if(peer_bitfield[i] == 2){
            	peer_bitfields.get(peer_id)[i] = true;
            }else{
            	peer_bitfields.get(peer_id)[i] = false;
            }
        }

    }

    public static int getPieceId(int server_peer_id) {
        for (int i = 0; i < peerProcess.my_bitfield.length ; i++) {
            if(peerProcess.my_bitfield[i] == 0 && peerProcess.peer_bitfields.get(server_peer_id)[i]==true){
                return i;
            }
        }
        return -1;
    }

    public static int havePieceId(int client_peer_id) {
        for (int i = 0; i < peerProcess.my_bitfield.length ; i++) {
            if(peerProcess.my_bitfield[i] == 2 && peerProcess.peer_bitfields.get(client_peer_id)[i]==false){
                return i;
            }
        }
        return -1;
    }

}

//Listener
class ListeningThread implements Runnable{

    private ServerSocket listeningSocket;
    private int peerID;
    Socket remoteSocket;
    Thread spawnThread; //clientThread, or serverThread

    public ListeningThread(ServerSocket socket, int peerID)
    {
        this.listeningSocket = socket;
        this.peerID = peerID;
    }

    @Override
    public void run(){
        Socket server_cc_socket = null;
        while(peerProcess.IsAnyoneLeftToDownload() || peerProcess.IsSomethingLeftToDownload()){
            try {
                server_cc_socket = this.listeningSocket.accept();
                ObjectOutputStream out_stream = new ObjectOutputStream(server_cc_socket.getOutputStream());
                ObjectInputStream input_stream = new ObjectInputStream(server_cc_socket.getInputStream());

                // Hand shake start
                byte[] hs_msg = (byte[])input_stream.readObject();
                Handshake.receiveMessage(hs_msg);
                int received_peer_id= Integer.parseInt(Handshake.received_peerID);
                out_stream.writeObject(Handshake.sendMessage(peerProcess.my_peer_id));
                // Hand shake end
                Logging.writeLog("Peer [peer_ID "+peerProcess.my_peer_id+"] is connected from Peer [peer_ID "+received_peer_id+"].");
                // bitfield exchange
                byte[] received_bitfield = (byte[])input_stream.readObject();
                peerProcess.updatePeerBitfields(received_peer_id, MessageType.receiveBitfeild(received_bitfield));
                out_stream.writeObject(MessageType.sendBitfeild(peerProcess.my_bitfield));
                byte[] connection_response =(byte[]) input_stream.readObject();

                // check for interested
                String receivedMessage = null;

                if(MessageType.receiveInterested(connection_response)) {
                    receivedMessage = "interested"; // Now this node is a Server
                    Logging.writeLog("Peer [peer_ID "+peerProcess.my_peer_id+"] received the 'interested' message from [peer_ID "+received_peer_id+"].");

                }
                else if(MessageType.receiveHave(connection_response) != -1) {
                    receivedMessage = "have"; //// Now this node is a Client
                    Logging.writeLog("Peer [peer_ID "+peerProcess.my_peer_id+"] received the 'have' message from [peer_ID "+received_peer_id+"].");

                }
                else if(MessageType.receiveNotInterested(connection_response)){
                    receivedMessage = "notInterested"; // Close the connection.
                    Logging.writeLog("Peer [peer_ID "+peerProcess.my_peer_id+"] received the 'not interested' message from [peer_ID "+received_peer_id+"].");

                }

                switch (receivedMessage){
                    case "interested":	//request from CLIENT controller. Client can also send "not interested"?
                    	if((peerProcess.present_client_connections.size() >= peerProcess.preferred_neighbor_limit) || peerProcess.peer_choke_status.get(received_peer_id)){ // checking the connection limit to send unchoke
                    		out_stream.writeObject(MessageType.sendChoke());
                    		server_cc_socket.close();
                    	}else{
                            out_stream.writeObject(MessageType.sendUnchoke());
                            System.out.println("Sent unchoke to client");
                            spawnThread = new Thread(new ServerThread(this.listeningSocket,server_cc_socket, received_peer_id,input_stream,out_stream, -1));
                    		peerProcess.present_client_connections.add(received_peer_id);
                    		spawnThread.start();		//TODO HANDLE in thread: //1. server_cc_socket.close(); 2.  present_client_connections.remove()
                    	}
                        break;
                    case "notInterested":
                        server_cc_socket.close(); // closing the connection as the client is not interested.
                        break;
                    default:	//case "have": and default
                            //request FROM serverController and Initiate a client thread now.
                    		int piece_id = MessageType.receiveHave(connection_response);
                    		if(piece_id!=-1) {
                                if(peerProcess.my_bitfield[piece_id]==0){
                                    out_stream.writeObject(MessageType.sendInterested());// send interested message after recieving have.
                                    boolean unchoke_condition = MessageType.receiveUnchoke((byte[]) input_stream.readObject()); // receive unchoke after accepting the interested.
                                    if(unchoke_condition){
                                        System.out.println("Recieved unchoke.");
                                        Logging.writeLog("Peer [peer_ID "+peerProcess.my_peer_id+"] is unchoked by Peer [peer_ID "+received_peer_id+"].");

                                        spawnThread = new Thread(new ClientThread(this.listeningSocket,server_cc_socket, received_peer_id,input_stream,out_stream, piece_id));
                                        peerProcess.present_client_connections.add(received_peer_id);
                                        spawnThread.start();	//TODO HANDLE in thread: //1. server_cc_socket.close(); 2.  present_client_connections.remove()
                                    }else{
                                        Logging.writeLog("Peer [peer_ID "+peerProcess.my_peer_id+"] is choked by Peer [peer_ID "+received_peer_id+"].");

                                        System.out.println("received choke.");
                                        server_cc_socket.close();
                                    }
                                }else{
                        			//send "not interested" for the have message message received.
                                    out_stream.writeObject(MessageType.sendNotInterested());
                                    server_cc_socket.close();
                                }
                            }	//case: "default"
                            else {
                                server_cc_socket.close();
                                // drop connection
                            }
                    	break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
//			wait for bitsfield
//			send my_bitfield
//			wait for resonse
//			switch(resonse){
//			case "interested":
            //send_response = decideIfWantToSend()
//				send this above response to user
            //if(send_response == "unchoke")		//when server sends "unchoke", it expects "request", and replies with "piece"
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
        //closing the lisenting port...
        try {
            this.listeningSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Listener is closed");
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
//        while(peerProcess.IsAnyoneLeftToDownload()){
//    	IMP: PAUSE for p=5 seconds now

//            int i = 0;

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

    	while(peerProcess.IsAnyoneLeftToDownload()){
        	try {
				TimeUnit.SECONDS.sleep(peerProcess.unchoking_interval);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

            ArrayList<Integer> topk = new ArrayList<>();
            selectTopK(topk);	//whom it can offer something


         // Iterating over keys only: https://stackoverflow.com/questions/1066589/iterate-through-a-hashmap
            for (Integer peer_id : peerProcess.peer_info_map.keySet()) {

                if(peer_id != peerProcess.my_peer_id) {
                    if (topk.contains(peer_id)) {
                        peerProcess.peer_choke_status.put(peer_id, false);    //unchoke them

                        //if not presently connected, try to connect. send "HAVE piece_id"
                        if (!peerProcess.present_client_connections.contains(peer_id)) {

                            try {
                                String[] peer_ip = peerProcess.peer_info_map.get(peer_id).split(" ");
                                this.remoteSocket = new Socket(peer_ip[0], Integer.parseInt(peer_ip[1]));
                                ObjectOutputStream out_to_client_obj = new ObjectOutputStream(this.remoteSocket.getOutputStream());
                                ObjectInputStream in_frm_client_obj = new ObjectInputStream(this.remoteSocket.getInputStream());

                                // Hand shake.
                                out_to_client_obj.writeObject(Handshake.sendMessage(peerProcess.my_peer_id));
                                Handshake.receiveMessage((byte[]) in_frm_client_obj.readObject());
                                int client_peer_id = Integer.parseInt(Handshake.received_peerID);
                                Logging.writeLog("Peer [peer_ID "+peerProcess.my_peer_id+"] makes a connection to Peer [peer_ID "+client_peer_id+"].");

                                // Sending the bit field and update the bit field.
                                out_to_client_obj.writeObject(MessageType.sendBitfeild(peerProcess.my_bitfield));
                                System.out.println("Server bitflied sent.");
                                byte[] client_bitfield_message = (byte[]) in_frm_client_obj.readObject();
                                peerProcess.updatePeerBitfields(peer_id, MessageType.receiveBitfeild(client_bitfield_message));
                                System.out.println("Client bitfield recieved.");

                                String message = null;
                                peerProcess.present_client_connections.add(peer_id);

                                int piece_id = peerProcess.havePieceId(peer_id); // get the peice which i have.
                                out_to_client_obj.writeObject(MessageType.sendHave(piece_id));
                                if (piece_id != -1) {
                                    System.out.println("HAVE message sent.");
                                    byte[] response = (byte[]) in_frm_client_obj.readObject();
                                    System.out.println("Received response from the client for the have message");
                                    if (MessageType.receiveInterested(response)) {
                                        // starting the server thread as received interested.
                                        Logging.writeLog("Peer [peer_ID "+peerProcess.my_peer_id+"] received the 'interested' message from [peer_ID "+client_peer_id+"].");

                                        sendingThread = new Thread(new ServerThread(this.listeningSocket, this.remoteSocket, client_peer_id, in_frm_client_obj, out_to_client_obj, piece_id));
                                        sendingThread.start();
                                    } else {
                                        // closing the connection as received not interested fromt he client.
                                        Logging.writeLog("Peer [peer_ID "+peerProcess.my_peer_id+"] received the 'not interested' message from [peer_ID "+client_peer_id+"].");

                                        peerProcess.present_server_connections.remove(peer_id);
                                        this.remoteSocket.close();
                                    }
                                } else {
                                    // exit if the piece id is -1
                                    System.out.println("Since peice id is :" + piece_id + " closing the connection with :"+ peer_id);
                                    this.remoteSocket.close();
                                }

                            } catch (Exception e) {
                                System.out.println("connection is refused. No peer in the network.");
                            }
                        }

                    } else {
                        //If this server was currently sending. it will not send next piece. and send choke to it from Server Thread
                        //if(topk.size() != 0){
                                peerProcess.peer_choke_status.put(peer_id, true);    //choke them
                        //}
//            		TODO Ensure CLEAN EXIT:
//            		in SERVER THREAD: send "choke" on exit: CHECK WHILE LOOP
//            	    in Client THREAD: handle receive "choke"

                    }

                }
            }

        }
    }
    public void selectTopK(ArrayList<Integer> topk){
    	System.out.println("Selecting top k peers.");

        if(!peerProcess.IsSomethingLeftToDownload()){
            ArrayList<Integer> entries = new ArrayList<>(peerProcess.peer_bitfields.keySet());
            entries.remove(entries.indexOf(peerProcess.my_peer_id));
            selectInterestedNeighbours(entries);
            while (topk.size() < Math.min((peerProcess.preferred_neighbor_limit),entries.size())){
                Collections.shuffle(entries);
                topk.add(entries.get(0));
                entries.remove(0);
            }

        }else{
            Set<Entry<Integer, Double>> entries = peerProcess.peer_download_rate.entrySet();

//  http://www.java67.com/2015/01/how-to-sort-hashmap-in-java-based-on.html
            // Sort method needs a List, so let's first convert Set to List in Java
            ArrayList<Entry<Integer, Double>> listOfEntries = new ArrayList<Entry<Integer, Double>>(entries);

            Comparator<Entry<Integer,Double>> valueComparator = new Comparator<Entry<Integer, Double>>() {
                @Override public int compare(Entry<Integer, Double> e1, Entry<Integer, Double> e2) {
                    Double v1 = e1.getValue(); Double v2 = e2.getValue();
                    return v1.compareTo(v2);
                }
            };

            // sorting HashMap by values using comparator
            Collections.sort(listOfEntries, valueComparator);

            for(Entry<Integer, Double> entry : listOfEntries){
//    		if i have something to offer to this peer, add it
                if(peerProcess.havePieceId(entry.getKey())!=-1){
                    topk.add(entry.getKey());
                    if(topk.size()==peerProcess.preferred_neighbor_limit)
                        break;
                }

//    		sortedByValue.put(entry.getKey(), entry.getValue());

            }

        }
        Logging.writeLog("Peer [peer_ID "+peerProcess.my_peer_id+"] has the preferred neighbours ["+topk.toArray().toString()+"].");

    }

    private void selectInterestedNeighbours(ArrayList<Integer> entries) {
        for (int peer_id: entries) {
            Boolean[] p_bit_field = peerProcess.peer_bitfields.get(peer_id);
            int count = 0;
            boolean flag = false;
            while(count < p_bit_field.length){
                if(!p_bit_field[count] && peerProcess.my_bitfield[count] == 2){
                    flag = true;
                    break;
                }
                count++;
            }
            if(!flag){
                entries.remove(entries.indexOf(peer_id));
            }
        }
    }

}



//client controller
class ClientController implements Runnable{

    private ServerSocket listeningSocket;
    private int peerID;
    Socket remoteSocket;
    Thread sendingThread;
    //public ArrayList req_pieces;

    public ClientController(ServerSocket socket, int peerID)
    {
        this.listeningSocket = socket;
        this.peerID = peerID;	//this is my peer id
    }

    @Override
    public void run(){
        while(peerProcess.IsSomethingLeftToDownload()){
            try {
                for (int peer_id: peerProcess.peer_info_map.keySet()) {	//this is general peer_id
                    if(peer_id < this.peerID && !peerProcess.present_server_connections.contains(peer_id)){
                        String[] peer_ip = peerProcess.peer_info_map.get(peer_id).split(" ");
                        this.remoteSocket = new Socket(peer_ip[0], Integer.parseInt(peer_ip[1]));
                        ObjectOutputStream out_to_server_obj = new ObjectOutputStream(this.remoteSocket.getOutputStream());
                        ObjectInputStream in_frm_server_obj = new ObjectInputStream(this.remoteSocket.getInputStream());


                        // Hand shake
                        out_to_server_obj.writeObject(Handshake.sendMessage(peerProcess.my_peer_id));
                        Handshake.receiveMessage((byte[]) in_frm_server_obj.readObject());	//Handshake

                        if(Handshake.verifyHandshake(peer_id) != 1){
                            System.out.println("hand shake failed with  : " + peer_id);
                            this.remoteSocket.close();
                            continue;
                        }

                        int client_peer_id = Integer.parseInt(Handshake.received_peerID);
                        Logging.writeLog("Peer [peer_ID "+peerProcess.my_peer_id+"] makes a connection to Peer [peer_ID "+client_peer_id+"].");

                        // hand shake end

                        // bit field exchange.
                        out_to_server_obj.writeObject(MessageType.sendBitfeild(peerProcess.my_bitfield));
                        System.out.println("Client bitflied sent.");
                        byte[] server_bitfield_msg = (byte[])in_frm_server_obj.readObject();
                        peerProcess.updatePeerBitfields(peer_id, MessageType.receiveBitfeild(server_bitfield_msg));
                        System.out.println("Server bitfield recieved.");

                        peerProcess.present_server_connections.add(peer_id);
                        System.out.println("connecting with :" + peer_id);

                        if(peerProcess.getPieceId(peer_id)!= -1){	//updateServerBitField(server_bitfield,peerProcess.peer_bitfields.get(peer_id))){
                            out_to_server_obj.writeObject(MessageType.sendInterested());
                            System.out.println("Interested message sent.");
                            byte[] message_frm_server =  (byte[]) in_frm_server_obj.readObject();

                            if(MessageType.receiveUnchoke(message_frm_server)){
                                System.out.println("Received unchoke from the server");
                                Logging.writeLog("Peer [peer_ID "+peerProcess.my_peer_id+"] is unchoked by Peer [peer_ID "+client_peer_id+"].");

                                sendingThread = new Thread(new ClientThread(this.listeningSocket, this.remoteSocket, peer_id, in_frm_server_obj, out_to_server_obj, -1));
                                sendingThread.start();
                            }else{
                                // received choke.
                                System.out.println("Received Choke.");
                                Logging.writeLog("Peer [peer_ID "+peerProcess.my_peer_id+"] is choked by Peer [peer_ID "+client_peer_id+"].");

                                peerProcess.present_server_connections.remove(peer_id);
                                System.out.println("Closed the connection with : " + peer_id);
                                this.remoteSocket.close();
                            }

                        }else{
                            out_to_server_obj.writeObject(MessageType.sendNotInterested());
                            peerProcess.present_server_connections.remove(peer_id);
                            this.remoteSocket.close();
                        }
                    }
                }
            } catch (ConnectException e){
                System.out.println("connection is refused");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

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
        System.out.println("Client controller closed");
    }
}



//	serverThread> exchange between 1 client and server only. This connection is currenly active

class ServerThread implements Runnable{

    private int piece_id;
    private ServerSocket listeningSocket;
	private Socket live_connection;		//remoteSocket
    private int client_peer_id;			//client peer id
    private ObjectOutputStream out_to_client;
    private ObjectInputStream in_frm_client;

    public ServerThread(ServerSocket socket, Socket connection, int peerID, ObjectInputStream input, ObjectOutputStream output, int piece_id)
    {
        this.listeningSocket = socket;
        this.live_connection = connection;
        this.client_peer_id = peerID;
        this.in_frm_client = input;
        this.out_to_client = output;
        this.piece_id = piece_id;

    }

    @Override
    public void run(){
        System.out.println("Server thread started.");
        while(!peerProcess.peer_choke_status.get(client_peer_id)){
            try {
                this.piece_id = MessageType.receiveRequest((byte[]) this.in_frm_client.readObject());
                //long end = System.currentTimeMillis();
                if(this.piece_id == -1){
                    break;
                }
                /*if(start != 0) {
                    int present_time = peerProcess.peer_download_time.get(client_peer_id) == null ? 0 : peerProcess.peer_download_time.get(client_peer_id);
                    int present_cnt = peerProcess.peer_download_cnt.get(client_peer_id) == null ? 0 : peerProcess.peer_download_time.get(client_peer_id);
                    peerProcess.peer_download_time.put(client_peer_id, present_time + (int) (end - start));
                    peerProcess.peer_download_cnt.put(client_peer_id, present_cnt + 1);

                    double rate = (double) peerProcess.peer_download_time.get(client_peer_id) / peerProcess.peer_download_cnt.get(client_peer_id);
                    peerProcess.peer_download_rate.put(client_peer_id, rate);
                }*/

                this.out_to_client.writeObject(MessageType.sendPiece(this.piece_id));
                peerProcess.peer_bitfields.get(client_peer_id)[piece_id] = true; // update the piece



                //String file_name = peerProcess.my_path + "/sample.txt.part" + this.piece_id;
                //sendFile(file_name, this.listeningSocket, this.in_frm_client, this.out_to_client);

            } catch (Exception e) {
                e.printStackTrace();
            }
//		private information about the client
        //while(/*(!is_choked(cleint)) &&*/ peerProcess.IsAnyoneLeftToDownload(/*CURRENT CLIENT*/)){	//server could choke the client from SERVER CONTROLLER. stop sending then
//			wait for request
//			if(invalid request) break the connection
//			send the piece requested.
        }

        try {
            if(this.piece_id != -1 || peerProcess.peer_choke_status.get(client_peer_id)){// This is for Have condition...
                this.out_to_client.writeObject(MessageType.sendChoke());
                //TimeUnit.SECONDS.sleep(0);
                System.out.println("sent choke to :" + client_peer_id);
            }
            this.live_connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Server thread closed");
    }

    public static void sendFile(String file_name, ServerSocket server_cc_socket, ObjectInputStream in_from_client , ObjectOutputStream out_to_client) {
            try {

                File file_to_send = new File(file_name);
                byte[] byteArray = new byte[(int) file_to_send.length()];

                FileInputStream file_input_strm = new FileInputStream(file_to_send);
                BufferedInputStream file_buff_strm = new BufferedInputStream(file_input_strm);

                DataInputStream data_strm = new DataInputStream(file_buff_strm);
                data_strm.readFully(byteArray, 0, byteArray.length);
                out_to_client.write(byteArray);
                out_to_client.flush();
                System.out.println("File " + file_name + " sent to Client.");


            } catch (Exception e) {
                System.err.println("File sending Error!! name : "+ file_name + e);
            }
        }
}


    //	clientThread
//hashmap<piece_index, int>piece_status: //0 means not downloaded, 1 = currently downloaded, 2 = downloaded
class ClientThread implements Runnable{
    private int piece_id;
    private ObjectInputStream in_frm_server;
    private ObjectOutputStream out_to_server;
    private ServerSocket listeningSocket;
    private int server_peer_id;	//
    Socket remoteSocket;
    Thread sendingThread;

    public ClientThread(ServerSocket socket, Socket mySocket, int peerID, ObjectInputStream input, ObjectOutputStream ouput, int piece_id)
    {
        this.listeningSocket = socket;
        this.server_peer_id = peerID;
        this.remoteSocket = mySocket;
        this.out_to_server = ouput;
        this.in_frm_server = input;
        this.piece_id = piece_id;
    }

    @Override
    public void run(){
//		private information about the server

//        while(peerProcess.IsSomethingLeftToDownload(/*FROM THIS SERVER*/)){
//			piece = check Bitsfield of S; what you need to download from it; (that u are not downloading from other peer also)
//			change download status of piece = 1;
//			start(time)
//			request piece
//			wait for response		//NOTE: THE RESPONSE HERE COULD ALSO BE "CHOKE" or ("REQUEST" if server is also downloading from us). NEED to DIFFERENTIATE THIS.
//			stop(time)
//			updateAvgDS(sever, time)	//download speed
//			if(got_piece_correctly){
//				piece_status[piece] =2;
//				update my_bitfield
//				update my_cnt;
//			}else{
//				if(piece_status[piece]==1)	//CHECK becz: somebody else may have downloaded it correctly and changed status to 2!
//					piece_status[piece] =0
//				drop_connection();
//			}
//        }

    	try {
            System.out.println("Client thread started.");

            if(this.piece_id == -1){
                this.piece_id = peerProcess.getPieceId(server_peer_id);
            }
            while (this.remoteSocket.isConnected() && this.piece_id !=-1) {

                long start = System.currentTimeMillis();
                this.out_to_server.writeObject(MessageType.sendRequest(this.piece_id));
                System.out.println("Piece number sent to the server");
                peerProcess.my_bitfield[piece_id] = 1;
                byte [] response_frm_server = (byte[]) this.in_frm_server.readObject();
                byte[] received_piece = null;
                if(MessageType.receiveChoke(response_frm_server)){
                    Logging.writeLog("Peer [peer_ID "+peerProcess.my_peer_id+"] is choked by Peer [peer_ID "+server_peer_id+"].");

                    break;
                }else{
                    received_piece = MessageType.receivePiece(response_frm_server);
                }
                //byte[] RecData = new byte[1024];
                //this.in_frm_server.read(RecData, 0, RecData.length);
                long end = System.currentTimeMillis();

                String SaveFileName = peerProcess.my_path + peerProcess.config_info_map.get("FileName")+ ".part" + piece_id;

                OutputStream Fs = new FileOutputStream(SaveFileName);
                Fs.write(received_piece);
                System.out.println("File " + SaveFileName + " received.");
                Fs.close();

                int present_time = peerProcess.peer_download_time.get(server_peer_id) == null ? 0 : peerProcess.peer_download_time.get(server_peer_id);
                int present_cnt = peerProcess.peer_download_cnt.get(server_peer_id) == null ? 0 : peerProcess.peer_download_time.get(server_peer_id);
                peerProcess.peer_download_time.put(server_peer_id, present_time + (int) (end - start));
                peerProcess.peer_download_cnt.put(server_peer_id, present_cnt + 1);

                double rate = (double) peerProcess.peer_download_time.get(server_peer_id) / peerProcess.peer_download_cnt.get(server_peer_id);
                peerProcess.peer_download_rate.put(server_peer_id, rate);

                peerProcess.my_bitfield[piece_id] = 2;

                System.out.println("Complete piece "+ piece_id +"file received at Client");
                Logging.writeLog("Peer [peer_ID "+peerProcess.my_peer_id+"] has downloaded the piece ["+ piece_id+ "] from Peer [peer_ID "+server_peer_id+"].");

                this.piece_id = peerProcess.getPieceId(server_peer_id);
                if(this.piece_id == -1){
                    this.out_to_server.writeObject(MessageType.sendRequest(this.piece_id));
                }

            }
            peerProcess.present_server_connections.remove(this.server_peer_id);
            this.remoteSocket.close();
            Logging.writeLog("Peer [peer_ID "+peerProcess.my_peer_id+"] received the complete file. ");


        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}