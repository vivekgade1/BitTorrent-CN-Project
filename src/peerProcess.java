import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.*;
import java.util.Timer;


public class peerProcess {

    public static int my_peer_id;
    public static ArrayList<Integer> present_connections = new ArrayList<>();
    public int listening_port;
    public ServerSocket listening_socket;
    public Thread listening_thread;
    public Thread server_controller;
    public Thread client_controller;
    public static  HashMap<String, String> config_info_map = new HashMap<>();
    public static  HashMap<Integer, String> peer_info_map = new HashMap<>();
    public static  HashMap<Integer, Boolean[]> peer_bitfields = new HashMap<>();
    static Logger myLogger;
    public static int[] my_bitfield;
    //    Setup variables:
    public static int unchoking_interval;
    public static int opt_unchoking_interval;
    public static int piece_cnt=0;
    public static int my_cnt=0;
    public static int max_bitfield_count;
    public static String my_path;

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
        my_path  = "peer_" + my_id + "/";
        new File("peer_" + my_id).mkdir();
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
                if(parts[3].equals("1") && parts[0].equals(my_id)){
                    Arrays.fill(bitmap, true);
                    splitFile();
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


    }

    public static void splitFile(){
        String FILE_NAME = my_path + config_info_map.get("FileName");
        byte PART_SIZE = Byte.parseByte(config_info_map.get("PieceSize"));
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
        Socket server_cc_socket = null;
        while(peerProcess.IsAnyoneLeftToDownload() || peerProcess.IsSomethingLeftToDownload()){
//			server socket
//			server accept
            try {
                server_cc_socket = this.listeningSocket.accept();
                //DataInputStream  in_from_client = new DataInputStream (server_cc_socket.getInputStream());
                //DataOutputStream out_to_client = new DataOutputStream(server_cc_socket.getOutputStream());

                ObjectOutputStream out_to_client_obj = new ObjectOutputStream(server_cc_socket.getOutputStream());
                ObjectInputStream in_frm_client_obj = new ObjectInputStream(server_cc_socket.getInputStream());

                int[] client_bitfield = (int[])in_frm_client_obj.readObject();
                System.out.println("Bit field received from the client");
                out_to_client_obj.writeObject(peerProcess.my_bitfield);
                System.out.println("Server bit field sent.");


                //byte[] rec_from_client = new byte[1024];
                //in_from_client.read(rec_from_client, 0, 1024);
                String connection_response =(String) in_frm_client_obj.readObject();
                System.out.println("interested message recieved from the client.");
                switch (connection_response){
                    case "interested":
                        sendingThread = new Thread(new ServerThread(this.listeningSocket,server_cc_socket, this.peerID,in_frm_client_obj,out_to_client_obj));
                        sendingThread.start();
                        break;
                    case "have":
                        break;
                    default:
                        server_cc_socket.close();
                        // drop connection
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
        try {
            this.listeningSocket.accept();
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
        while(peerProcess.IsAnyoneLeftToDownload()){
            int i = 0;

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
    public boolean Countdown (int seconds){
            if (seconds < 0) {
                System.err.println("Usage: java Countdown <time in secs>");
                System.exit(1);
            }
            final Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                int i = seconds;
                public void run() {
                    System.out.println(i--);
                    if (i< 0) {
                        timer.cancel();
                    }

                }
            }, 0, 1000);
            return true;
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
        this.peerID = peerID;
    }

    @Override
    public void run(){
        while(peerProcess.IsSomethingLeftToDownload()){
            try {
                for (int peer_id: peerProcess.peer_info_map.keySet()) {
                    if(peer_id < this.peerID && !peerProcess.present_connections.contains(peer_id)){
                        String[] peer_ip = peerProcess.peer_info_map.get(peer_id).split(" ");
                        this.remoteSocket = new Socket(peer_ip[0], Integer.parseInt(peer_ip[1]));
                        ObjectOutputStream out_to_server_obj = new ObjectOutputStream(this.remoteSocket.getOutputStream());
                        ObjectInputStream in_frm_server_obj = new ObjectInputStream(this.remoteSocket.getInputStream());

                        out_to_server_obj.writeObject(peerProcess.my_bitfield);
                        System.out.println("Client bitflied sent.");
                        int[] server_bitfield = (int[])in_frm_server_obj.readObject();
                        System.out.println("Server bitfield recieved.");
                        String message = null;
                        peerProcess.present_connections.add(peer_id);
                        if(updateServerBitField(server_bitfield,peerProcess.peer_bitfields.get(peer_id))){
                            message = "interested";
                            out_to_server_obj.writeObject(message);
                            System.out.println("Interested message sent.");
                            String message_frm_server = (String) in_frm_server_obj.readObject();
                            System.out.println("Received unchoke from the server");
                            System.out.println("received " + message_frm_server);
                            if(message_frm_server.equals("unchoke")){
                                sendingThread = new Thread(new ClientThread(this.listeningSocket, this.peerID , this.remoteSocket, in_frm_server_obj, out_to_server_obj));
                                sendingThread.start();
                            }else{
                                peerProcess.present_connections.remove(peer_id);
                                this.remoteSocket.close();
                            }

                        }else{
                            message = "not interested";
                            out_to_server_obj.writeObject(message);
                            peerProcess.present_connections.remove(peer_id);
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

    private boolean updateServerBitField(int[] server_bitfield,Boolean[] server_bitfield_present) {
        //ArrayList<Integer> piece_indxs = new ArrayList<>();
        Boolean flag = false;
        for (int i = 0; i < server_bitfield.length ; i++) {
            if(server_bitfield[i] == 2 && peerProcess.my_bitfield[i] ==0){
                //piece_indxs.add(i);
                flag = true;
            }
            if(server_bitfield[i] == 0){
                server_bitfield_present[i] = false;
            }else{
                server_bitfield_present[i] = true;
            }
        }
        return flag;
    }
}



//	serverThread> exchange between 1 client and server only. This connection is currenly active

class ServerThread implements Runnable{

    private Socket live_connection;
    private ServerSocket listeningSocket;
    private int peerID;
    private ObjectOutputStream out_to_client;
    private ObjectInputStream in_frm_client;

    Socket remoteSocket;
    Thread sendingThread;

    public ServerThread(ServerSocket socket, Socket connection ,int peerID,ObjectInputStream input ,ObjectOutputStream output)
    {
        this.listeningSocket = socket;
        this.live_connection = connection;
        this.peerID = peerID;
        this.in_frm_client = input;
        this.out_to_client = output;
    }

    @Override
    public void run(){

        while (!this.live_connection.isClosed()) {
            try {
                //DataInputStream  in_from_client = new DataInputStream (this.listeningSocket.getInputStream());
                //DataOutputStream out_to_client = new DataOutputStream(this.listeningSocket.getOutputStream());
                //if(mssg_from_client.equals("interested")){
                System.out.println("Server thread started.");
                String message = "unchoke";
                this.out_to_client.writeObject(message);
                System.out.println("Sent unchoke to client");
                Boolean conn_status = true;
                while (conn_status){
                    String piece_num = (String) this.in_frm_client.readObject();
                    System.out.println("Received piece number.");

                    String file_name = peerProcess.my_path + "/sample.txt.part" + piece_num;
                    sendFile(file_name, this.listeningSocket, this.in_frm_client, this.out_to_client);
                    conn_status = (Boolean) this.in_frm_client.readObject();
                }
                this.live_connection.close();
                System.out.println("Server connection closed");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
//		private information about the client
        //while(/*(!is_choked(cleint)) &&*/ peerProcess.IsAnyoneLeftToDownload(/*CURRENT CLIENT*/)){	//server could choke the client from SERVER CONTROLLER. stop sending then
//			wait for request
//			if(invalid request) break the connection
//			send the piece requested.
        }
        System.out.println("thread closed");
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
    private ObjectInputStream in_frm_server;
    private ObjectOutputStream out_to_server;
    private ServerSocket listeningSocket;
    private int peerID;
    Socket remoteSocket;
    Thread sendingThread;

    public ClientThread(ServerSocket socket, int peerID, Socket mySocket, ObjectInputStream input, ObjectOutputStream ouput)
    {
        this.listeningSocket = socket;
        this.peerID = peerID;
        this.remoteSocket = mySocket;
        this.out_to_server = ouput;
        this.in_frm_server = input;
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
//				update my_bitfield
//				update my_cnt;
//			}else{
//				if(piece_status[piece]==1)	//CHECK becz: somebody else may have downloaded it correctly and changed status to 2!
//					piece_status[piece] =0
//				drop_connection();
//			}
            try {
                //DataOutputStream out_to_server_obj = new DataOutputStream(this.remoteSocket.getOutputStream());
                //DataInputStream in_frm_server_obj = new DataInputStream(this.remoteSocket.getInputStream());
                //out_to_server_obj.writeObject(this.piece);
                int piece_id = getPieceId();
                while (piece_id != -1) {
                    System.out.println("Client thread started.");
                    this.out_to_server.writeObject(Integer.toString(piece_id));
                    System.out.println("Piece number sent to the server");
                    peerProcess.my_bitfield[piece_id] = 1;
                    byte[] RecData = new byte[1024];
                    this.in_frm_server.read(RecData,0,RecData.length);
                    String SaveFileName = peerProcess.my_path + "sample.txt.part"+piece_id;
                    OutputStream Fs = new FileOutputStream  (SaveFileName);
                    Fs.write(RecData);
                    System.out.println("File " + SaveFileName + " received.");
                    Fs.close();
                    peerProcess.my_bitfield[piece_id] = 2;
                    piece_id = getPieceId();
                    if(piece_id == -1){
                        this.out_to_server.writeObject(false);
                    }else{
                        this.out_to_server.writeObject(true);
                    }
                }
                System.out.println("Complete file received at Client");
                this.remoteSocket.close();
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private int getPieceId() {
        for (int i = 0; i < peerProcess.my_bitfield.length ; i++) {
            if(peerProcess.my_bitfield[i] == 0){
                return i;
            }
        }
        return -1;
    }
}