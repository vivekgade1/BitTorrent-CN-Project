import java.net.*;
import java.io.*;
import java.nio.*;
import java.util.*;

public class ConnectionHandler implements Runnable{
    int peerId;
    int remoteId = -1;
    boolean isconnection = false;
    public Socket socket = null;
    private ObjectInputStream read_frm_soc;
    private boolean isunchoked;
    private  ObjectOutputStream output_to_soc;
    boolean isConnected;

    public ConnectionHandler(int peerId,Socket socket){
        this.socket = socket;
        this.peerId = peerId;
        this.isunchoked = false;
        this.isConnected = false;

        try {
            output_to_soc = new ObjectOutputStream(this.socket.getOutputStream());
            output_to_soc.flush();
            read_frm_soc = new ObjectInputStream(this.socket.getInputStream());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            output_to_soc = null;
            read_frm_soc = null;
            e.printStackTrace();
        }

    }

    public ConnectionHandler(int peerId,int remoteId,Socket socket){
        this(peerId,socket);
        this.remoteId = remoteId;

    }

    @Override
    public void run() {
        if (socket == null)
            System.out.println("socket is null ");
        if(!socket.isClosed()){
            byte[] message = null;
            try{
                output_to_soc.writeObject(Handshake.sendMessage(this.peerId));
                Handshake.receiveMessage((byte[]) read_frm_soc.readObject()); // receive handshake
                if(this.remoteId != -1){
                    Logging.writeLog("Peer [peer_ID "+this.peerId+"] makes a connection to Peer [peer_ID "+this.remoteId+"].");
                }else{
                    this.remoteId = Integer.parseInt(Handshake.received_peerID);
                    Logging.writeLog("Peer [peer_ID "+peerProcess.my_peer_id+"] is connected from [peer_ID "+this.remoteId+"].");
                }
                peerProcess.active_connections.put(this.remoteId,this);

                // bit field exchange
                output_to_soc.writeObject(MessageType.sendBitfield(peerProcess.my_info.bitfield));
                byte[] received_bitfield = (byte[])read_frm_soc.readObject();
                peerProcess.updatePeerBitfields(this.remoteId, MessageType.receiveBitfield(received_bitfield));
                Peer remote_peer = peerProcess.peer_info_map.get(this.remoteId);


                if(!peerProcess.my_info.hasFile) { // because server doesn't send.
                    if (peerProcess.my_info.getPieceToDownload(remote_peer.bitfield) != -1) {
                        output_to_soc.writeObject(MessageType.sendInterested());
                    } else {
                        output_to_soc.writeObject(MessageType.sendNotInterested());
                    }
                }

                while(!socket.isClosed() || isConnected)
                {
                    //receive the message sent from the client
                    try {
                        message = (byte[])read_frm_soc.readObject();

                        Byte type = message[4];

                        switch (type.intValue()){
                            case 0:// choke
                                // nothing just add him to the choke list.
                                Logging.writeLog("Peer [peer_ID "+this.peerId+"] is choked by Peer [peer_ID "+this.remoteId+"].");

                                remote_peer.remote_choke = true;

                                break;
                            case 1:// unchoke
                                Logging.writeLog("Peer [peer_ID "+this.peerId+"] is unchoked by Peer [peer_ID "+this.remoteId+"].");
                                int piece_indx = peerProcess.my_info.getPieceToDownload(remote_peer.bitfield);
                                remote_peer.remote_choke = false;
                                if(piece_indx != -1){
                                    output_to_soc.writeObject(MessageType.sendRequest(piece_indx));
                                }else{
                                    MessageType.sendNotInterested();
                                }
                                break;
                            case 2:// interested
                                Logging.writeLog("Peer [peer_ID "+this.peerId+"] received the 'interested' message from [peer_ID "+this.remoteId+"].");

                                if(!peerProcess.interested_peers.contains(this.remoteId)){
                                    peerProcess.interested_peers.add(this.remoteId);
                                }
                                if(remote_peer.is_choked){
                                    output_to_soc.writeObject(MessageType.sendChoke());
                                }else{
                                    output_to_soc.writeObject(MessageType.sendUnchoke());
                                }
                                break;
                            case 3:// not interested
                                Logging.writeLog("Peer [peer_ID "+this.peerId+"] received the 'not interested' message from [peer_ID "+this.remoteId+"].");
                                if(peerProcess.interested_peers.contains(remoteId)){
                                    peerProcess.interested_peers.remove(peerProcess.interested_peers.indexOf(remoteId));
                                }
                                break;
                            case 4: //have
                                Logging.writeLog("Peer [peer_ID "+this.peerId+"] received the 'have' message from [peer_ID "+this.remoteId+"].");

                                int piece_ind = MessageType.receiveHave(message);
                                remote_peer.setPiece(piece_ind);
                                if(peerProcess.my_info.bitfield.get(piece_ind)){
                                    output_to_soc.writeObject(MessageType.sendNotInterested());
                                }else{
                                    output_to_soc.writeObject(MessageType.sendInterested());
                                }
                                break;
                            case 5:
                                peerProcess.updatePeerBitfields(this.peerId, MessageType.receiveBitfield(received_bitfield));
                                break;
                            case 6: // request
                                int req_piece = MessageType.receiveRequest(message);
                                output_to_soc.writeObject(MessageType.sendPiece(req_piece));
                                break;
                            case 7:// piece
                                byte [] received_payload = MessageType.receivePiece(message);
                                int received_piece = saveFile(received_payload);
                                peerProcess.my_info.setPiece(received_piece);
                                Logging.writeLog("Peer [peer_ID "+this.peerId+"] has downloaded the piece [ "+received_piece+" ] from [peer_ID "+this.remoteId+"].");

                                sendHaveToAll(received_piece);
                                remote_peer.set_downloadrate(received_payload.length);
                                int new_piece = peerProcess.my_info.getPieceToDownload(remote_peer.bitfield);
                                if(!remote_peer.remote_choke &&  new_piece!= -1){
                                    output_to_soc.writeObject(MessageType.sendRequest(new_piece));
                                }
                                break;
                        }

                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        //e.printStackTrace();
                        socket.isClosed();
                        isConnected = false;
                    }

                }
            }
            catch(ClassNotFoundException classnot){
                socket.isClosed();
                isConnected = false;
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else {
            isConnected = false;
            try {
                socket.close();
                isConnected = false;
            } catch (IOException e) {

            }

        }


    }

    private int saveFile(byte[] received_payload) {
        int piece_id = ByteBuffer.wrap( Arrays.copyOfRange(received_payload,0,4)).getInt();
        String SaveFileName = peerProcess.my_path + peerProcess.config_info_map.get("FileName")+ ".part" + piece_id;
        OutputStream Fs = null;
        try {
            Fs = new FileOutputStream(SaveFileName);
            Fs.write(received_payload,4,received_payload.length-4);
            System.out.println("File " + SaveFileName + " received.");
            Fs.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return piece_id;
    }

    public void sendHave(int piece_indx) throws IOException {
        output_to_soc.writeObject(MessageType.sendHave(piece_indx));
    }

    public void send(byte[] message){
        try {
            output_to_soc.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendHaveToAll(int index) throws IOException {
        //List<SocketConnectionHandler> cons = this.phandler.ConnectionTable.values();
        for (int  id :  peerProcess.peer_info_map.keySet()){
            if (peerProcess.my_peer_id != id){
                ConnectionHandler con = peerProcess.active_connections.get(id);
                if (con !=null){
                    System.out.println("Sending have to :" + id);
                    con.sendHave(index);
                }
            }
        }
    }

    public void terminate(){
        try {
            isConnected = false;
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
