import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class peerProcess{
    static Peer my_info;
    public static int my_peer_id;
    public static HashMap<String, String> config_info_map = new HashMap<>();
    public static  HashMap<Integer,Peer> peer_info_map = new HashMap<>();
    public static int preferred_neighbor_limit;
    public static int unchoking_interval;
    public static int opt_unchoking_interval;
    public static int piece_cnt=0;
    public static int my_cnt=0;
    public static int max_bitfield_count;	//piece_count*2: every field in the my_bitsfield would be 2.
    public static String my_path;
    static ServerSocket listening_socket;
    static Thread listening_thread;
    static Peer presentOptUnchoked = null;
    static HashMap<Integer,ConnectionHandler> active_connections = new HashMap<Integer,ConnectionHandler>();
    static HashSet<Integer> unchoked_list = new HashSet<>();
    static List<Integer> interested_peers = Collections.synchronizedList(new ArrayList<Integer>());
    static boolean isRunning = true;


    private static void initialSetup(String my_id) throws IOException {
//        boolean isFirstPeer = false;
        my_peer_id = Integer.parseInt(my_id);
        my_path  = "peer_" + my_id + "/";
        new File("peer_" + my_id).mkdir();
//		read common.cfg and peer_info.cfg
        try {
            File common_config_file = new File("Common.cfg");
            FileInputStream commons_file_reader = new FileInputStream(common_config_file);
            BufferedReader commons_buff_reader = new BufferedReader(new InputStreamReader(commons_file_reader));

            File peer_info_file = new File("PeerInfo.cfg");
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
                peer_info_map.put(Integer.parseInt(parts[0]),new Peer(Integer.parseInt(parts[0]),
                        parts[1],
                        Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[3])));

            }

            // Always close files.
            commons_buff_reader.close();
            peer_file_reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error in reading common or Peer info configuration.");
        }


//      set variables: bitsfield, mybitsfield, unchoking_interval, optimistic_inchoking_interval, piece_cnt, my_cnt
        my_info = peerProcess.peer_info_map.get(peerProcess.my_peer_id);
        if(my_info.hasFile){
            splitFile();
        }
        preferred_neighbor_limit= Integer.parseInt(config_info_map.get("NumberOfPreferredNeighbors"));
        unchoking_interval = Integer.parseInt(config_info_map.get("UnchokingInterval"));
        opt_unchoking_interval = Integer.parseInt(config_info_map.get("OptimisticUnchokingInterval"));

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

        initialSetup(args[0]);
        try {
            peerProcess.listening_socket = new ServerSocket(peerProcess.my_info.port);
            peerProcess.listening_thread = new Thread(new ListeningThread(peerProcess.listening_socket));
            Scheduler schTask = new Scheduler(peerProcess.my_peer_id, peerProcess.opt_unchoking_interval, peerProcess.unchoking_interval, peerProcess.preferred_neighbor_limit);
            schTask.run();
            peerProcess.listening_thread.start();
            peerProcess.connectToPeers();

            while(isRunning){
                TimeUnit.SECONDS.sleep(3);
            }
            System.out.println("System is finally closed...");
            System.exit(0);

        } catch (Exception e) {
            System.exit(0);
        }
    }

    private static void connectToPeers() {
        if(!peerProcess.my_info.hasFile){
            for (int peerId: peerProcess.peer_info_map.keySet()) {
                if(peerId < peerProcess.my_peer_id){
                    try {
                        if(active_connections.get(peerId) == null) {
                            Peer peer = peerProcess.peer_info_map.get(peerId);
                            Socket requestSocket = null;
                            requestSocket = new Socket(peer.ip, peer.port);
                            ConnectionHandler connection = new ConnectionHandler(peerProcess.my_peer_id, peer.id, requestSocket);
                            active_connections.put(peerId,connection);
                            startConnection(connection);
                        }
                    }catch (ConnectException e) {
                        System.out.println("Connection is refused as the peer is not in the network.");
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        while(isRunning) {
            try {
                IsAnyoneLeftToDownload();
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        terminateAllConnections();

    }

    private static void startConnection(ConnectionHandler connection) {
        for (int pid : active_connections.keySet()) {
            ConnectionHandler con = active_connections.get(pid);
            if (con.equals(connection) && !con.isConnected) {
                con.isConnected = true;
                Thread t = new Thread(con);
                t.start();
            }
        }
    }

    public static void IsAnyoneLeftToDownload(){
        int count = 0 ;
        for (int pid : peerProcess.peer_info_map.keySet()) {
            //ConnectionHandler con = active_connections.get(pid);
            if(peer_info_map.get(pid).bitfield.cardinality() == piece_cnt){
                count++;
            }
        }

        if(count == peerProcess.peer_info_map.keySet().size()){
            //terminateAllConnections();
            isRunning = false;
        }
    };

    public static void IsSomethingLeftToDownload(){
        if(!peerProcess.my_info.hadFile && peerProcess.my_info.bitfield.cardinality() == piece_cnt && peerProcess.my_info.hasFile){
            mergeFile();
            Logging.writeLog("Peer [peer_ID "+peerProcess.my_peer_id+"] has downloaded the complete file.");
        }
    };

    public static void updatePeerBitfields(int peerId, BitSet bit_field){
        peerProcess.peer_info_map.get(peerId).updateBitField(bit_field);
    }

    public static void updateUnchokeList(HashSet<Integer> new_unchoke_list){
        if (new_unchoke_list.size() > 0) {
            for(int id : peerProcess.interested_peers){

                if (new_unchoke_list.contains(id) || ((peerProcess.presentOptUnchoked != null) && peerProcess.presentOptUnchoked.id == id)){
                    peer_info_map.get(id).is_choked = false;
                } else {
                    peer_info_map.get(id).is_choked = true;
                }
            }
            unchoked_list.clear();
            unchoked_list.addAll(new_unchoke_list);
        }
    }

    public static List<Integer> getInteresteddAndChokedList(){
        List<Integer> list = new ArrayList<Integer>();
        for (int p: peerProcess.interested_peers){
            if (peer_info_map.get(p).is_choked){
                list.add(p);
            }
        }
        return list;
    }

    public static List<Integer> getChokeList(){
        List<Integer> list = new ArrayList<Integer>();
        for (int p: peerProcess.peer_info_map.keySet()){
            if (peer_info_map.get(p).is_choked){
                list.add(p);
            }
        }
        return list;
    }


    private static void terminateAllConnections() {
        for (int pid : active_connections.keySet()) {
            ConnectionHandler con = active_connections.get(pid);
            if (con !=null){
                con.terminate();
            }
        }
        isRunning = false;
    };


    static class ListeningThread implements Runnable {

        private ServerSocket listeningSocket;
        private int remote_peer;
        Socket remoteSocket;
        Thread spawnThread; //clientThread, or serverThread

        public ListeningThread(ServerSocket socket) {
            this.listeningSocket = socket;
        }

        @Override
        public void run() {
            try {
                ConnectionHandler connection_from_client = null;
                while (isRunning) {
                    try {
                        connection_from_client = new ConnectionHandler(peerProcess.my_peer_id, this.listeningSocket.accept());
                        if (!connection_from_client.isConnected) {
                            connection_from_client.isConnected = true;
                            Thread t = new Thread(connection_from_client);
                            t.start();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally{
                try {
                    this.listeningSocket.close();
                    System.out.println("Listener is closed");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
